package sn.dove.backend.dove.security;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import java.time.Duration;
import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.server.ResponseStatusException;
import sn.dove.backend.dove.config.DoveProperties;
import sn.dove.backend.dove.service.DoveResourceStore;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.ObjectNode;

@Service
public class DoveCurrentUserService {

    private static final String USERS = "utilisateurs";

    private final DoveResourceStore store;
    private final DoveProperties properties;

    /**
     * Short-lived cache from JWT subject to the resolved (pre-permission) user document.
     * requireUser() is called on every authenticated request, so even the indexed DB lookup
     * adds up at volume; a short TTL keeps role/status changes visible quickly (well under
     * typical access-token lifetimes) while avoiding a DB round trip on most requests. Missing
     * subjects are provisioned once by the cache loader as active BUSINESS_USER profiles.
     * The cached node is only ever read (requireActive) and deep-copied (withPermissions) before
     * being handed to a caller, never mutated in place, so sharing the same cached instance across
     * requests is safe.
     */
    private final Cache<String, ObjectNode> userBySubject = Caffeine.newBuilder()
        .expireAfterWrite(Duration.ofSeconds(30))
        .maximumSize(2_000)
        .build();

    public DoveCurrentUserService(DoveResourceStore store, DoveProperties properties) {
        this.store = store;
        this.properties = properties;
    }

    public ObjectNode requireUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication is required");
        }

        if (!properties.getAuth().isDevHeaderEnabled()) {
            if (!(authentication instanceof JwtAuthenticationToken jwtAuthentication)) {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "A Keycloak access token is required");
            }
            String subject = jwtAuthentication.getToken().getSubject();
            if (subject == null || subject.isBlank()) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "The access token has no stable subject");
            }
            ObjectNode source = userBySubject.get(subject, key -> resolveOrProvision(jwtAuthentication.getToken(), key));
            return withPermissions(requireActive(source));
        }

        // Dev-header path only: matches against id/externalSubject/login/email case-insensitively,
        // which doesn't map cleanly onto the indexed exact-match lookups above. This branch only
        // runs when dove.auth.dev-header-enabled=true, which DoveDevAuthenticationFilter now
        // refuses to allow together with the "prod" profile, and local/dev datasets are tiny, so
        // the full scan here is intentionally left as-is rather than risking a behavior change to
        // the case-insensitive matching semantics for a path that never runs in production.
        Set<String> candidates = new LinkedHashSet<>();
        candidates.add(authentication.getName());
        if (authentication instanceof JwtAuthenticationToken jwtAuthentication) {
            add(candidates, jwtAuthentication.getToken().getSubject());
            add(candidates, jwtAuthentication.getToken().getClaimAsString("preferred_username"));
            add(candidates, jwtAuthentication.getToken().getClaimAsString("email"));
        }
        return store
            .list(USERS)
            .stream()
            .filter(user -> matches(user, candidates))
            .findFirst()
            .map(this::requireActive)
            .map(this::withPermissions)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "The authenticated identity is not provisioned in DOVE"));
    }

    public boolean hasPermission(String permission) {
        return permissions(requireUser()).contains(permission);
    }

    /**
     * Creates the DOVE profile on the first successful Keycloak authentication. The stable JWT
     * subject is used as both the DOVE id and externalSubject, making the operation idempotent and
     * protected by the existing (resource_type, external_id) database uniqueness constraint.
     * Functional claims and Keycloak roles are deliberately ignored.
     *
     * <p>An admin-invited profile (created via the Administration &gt; Utilisateurs "Inviter"/import
     * flow, before this person ever logged in) has no externalSubject yet. Such a profile is linked
     * to this subject by matching the JWT email instead of being shadowed by a second, freshly
     * auto-provisioned BUSINESS_USER profile, so the role/scope the admin assigned actually applies.
     */
    private ObjectNode resolveOrProvision(Jwt token, String subject) {
        Optional<ObjectNode> bySubject = store.findByExternalSubject(USERS, subject);
        if (bySubject.isPresent()) {
            return bySubject.get();
        }

        Optional<ObjectNode> invited = findInvitedByEmail(token.getClaimAsString("email"));
        if (invited.isPresent()) {
            ObjectNode patch = tools.jackson.databind.node.JsonNodeFactory.instance.objectNode();
            patch.put("externalSubject", subject);
            patch.put("lastLoginAt", Instant.now().toString());
            String id = invited.get().path("id").asText();
            return store.patch(USERS, id, patch).orElseGet(() -> store.create(USERS, defaultBusinessUser(token, subject)));
        }

        return store.create(USERS, defaultBusinessUser(token, subject));
    }

    private Optional<ObjectNode> findInvitedByEmail(String email) {
        if (email == null || email.isBlank()) {
            return Optional.empty();
        }
        return store
            .list(USERS)
            .stream()
            .filter(user -> user.path("externalSubject").asText().isBlank())
            .filter(user -> email.equalsIgnoreCase(user.path("email").asText()))
            .findFirst();
    }

    private ObjectNode defaultBusinessUser(Jwt token, String subject) {
        String username = firstNonBlank(token.getClaimAsString("preferred_username"), token.getClaimAsString("email"), subject);
        String displayName = firstNonBlank(token.getClaimAsString("name"), username);
        String firstName = firstNonBlank(token.getClaimAsString("given_name"), firstName(displayName), username);
        String lastName = firstNonBlank(token.getClaimAsString("family_name"), lastName(displayName), "Keycloak");
        String email = firstNonBlank(token.getClaimAsString("email"), username.contains("@") ? username : username + "@keycloak.local");

        ObjectNode user = tools.jackson.databind.node.JsonNodeFactory.instance.objectNode();
        user.put("id", subject);
        user.put("externalSubject", subject);
        user.put("login", username);
        user.put("firstName", firstName);
        user.put("lastName", lastName);
        user.put("email", email.trim().toLowerCase());
        user.put("role", "BUSINESS_USER");
        user.putArray("additionalPermissions");
        user.put("status", "ACTIVE");
        user.put("lastLoginAt", Instant.now().toString());
        user.put("provisioningSource", "KEYCLOAK");
        ObjectNode scope = user.putObject("accessScope");
        scope.put("global", false);
        scope.putArray("applicationIds");
        scope.putArray("businessUnitIds");
        scope.putArray("businessJobIds");
        scope.putArray("moduleIds");
        return user;
    }

    private static String firstName(String displayName) {
        int separator = displayName.indexOf(' ');
        return separator > 0 ? displayName.substring(0, separator) : displayName;
    }

    private static String lastName(String displayName) {
        int separator = displayName.indexOf(' ');
        return separator > 0 && separator < displayName.length() - 1 ? displayName.substring(separator + 1) : "";
    }

    private static String firstNonBlank(String... candidates) {
        for (String candidate : candidates) {
            if (candidate != null && !candidate.isBlank()) return candidate.trim();
        }
        return "";
    }

    public void requirePermission(String permission) {
        if (!hasPermission(permission)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Missing DOVE permission: " + permission);
        }
    }

    /**
     * Evicts an updated user's authentication projection once the surrounding transaction commits.
     * This makes role and additional-permission changes effective on the very next request.
     */
    public void invalidateUser(JsonNode user) {
        String subject = user.path("externalSubject").asText();
        if (subject.isBlank()) return;

        Runnable invalidate = () -> userBySubject.invalidate(subject);
        if (TransactionSynchronizationManager.isActualTransactionActive()) {
            TransactionSynchronizationManager.registerSynchronization(
                new TransactionSynchronization() {
                    @Override
                    public void afterCommit() {
                        invalidate.run();
                    }
                }
            );
        } else {
            invalidate.run();
        }
    }

    public Set<String> permissions(ObjectNode user) {
        Set<String> result = new LinkedHashSet<>(DovePermissions.BY_ROLE.getOrDefault(user.path("role").asText(), List.of()));
        JsonNode additional = user.path("additionalPermissions");
        if (additional.isArray()) {
            additional.forEach(value -> result.add(value.asText()));
        }
        return result;
    }

    public boolean isGlobal(ObjectNode user) {
        return user.path("accessScope").path("global").asBoolean(false);
    }

    public boolean isInMutationScope(ObjectNode user, JsonNode resource) {
        if (isGlobal(user)) {
            return true;
        }
        String applicationId = resource.path("applicationId").asText();
        if (applicationId.isBlank() || !canSeeApplication(user, applicationId)) {
            return false;
        }
        String moduleId = resource.path("moduleId").asText();
        if (!moduleId.isBlank()) {
            ObjectNode module = store.find("modules", moduleId).orElse(null);
            if (
                module == null ||
                !applicationId.equals(module.path("applicationId").asText()) ||
                !canSeeModule(user, module, false)
            ) {
                return false;
            }
        }

        LinkedHashSet<String> resourceJobIds = values(resource.path("businessJobIds"));
        String legacyJobId = resource.path("businessJobId").asText();
        if (!legacyJobId.isBlank()) resourceJobIds.add(legacyJobId);
        if (resourceJobIds.isEmpty()) {
            store.find("applications", applicationId).ifPresent(application -> resourceJobIds.addAll(values(application.path("businessJobIds"))));
        }
        return matchesOrganizationalJobs(user, resourceJobIds);
    }

    public boolean canReadContent(ObjectNode user, JsonNode content, boolean explicitCrossBusinessJob) {
        if (isInMutationScope(user, content)) {
            return true;
        }
        boolean publicStatus = List.of("PUBLIER", "A_REVISER").contains(content.path("status").asText());
        return (
            publicStatus &&
            explicitCrossBusinessJob &&
            permissions(user).contains("READ_CROSS_BUSINESS_JOB") &&
            canSeeApplication(user, content.path("applicationId").asText())
        );
    }

    public boolean canSeeApplication(ObjectNode user, String id) {
        return (
            isGlobal(user) ||
            store.find("applications", id).filter(this::isAvailableReference).filter(application ->
                intersects(
                    values(user.path("accessScope").path("businessUnitIds")),
                    applicationBusinessUnitIds(application)
                )
            ).isPresent()
        );
    }

    public boolean canSeeBusinessJob(ObjectNode user, String id, boolean browse) {
        return (
            isGlobal(user) ||
            contains(user.path("accessScope").path("businessJobIds"), id) ||
            (isEnablement(user) && jobMatchesBusinessUnit(user, id)) ||
            (browse && permissions(user).contains("READ_CROSS_BUSINESS_JOB") && jobMatchesBusinessUnit(user, id))
        );
    }

    public boolean canSeeModule(ObjectNode user, JsonNode module, boolean browse) {
        if (isGlobal(user)) {
            return true;
        }
        if (!isAvailableReference(module)) {
            return false;
        }
        if (!canSeeApplication(user, module.path("applicationId").asText())) {
            return false;
        }
        return true;
    }

    public boolean sharesOrganizationalScope(ObjectNode current, ObjectNode candidate, boolean browse) {
        if (isGlobal(current)) {
            return true;
        }
        JsonNode currentScope = current.path("accessScope");
        JsonNode candidateScope = candidate.path("accessScope");
        if (intersects(currentScope.path("businessJobIds"), candidateScope.path("businessJobIds"))) {
            return true;
        }
        return (
            (isEnablement(current) || (browse && permissions(current).contains("READ_CROSS_BUSINESS_JOB"))) &&
            intersects(currentScope.path("businessUnitIds"), candidateScope.path("businessUnitIds"))
        );
    }

    private boolean isEnablement(ObjectNode user) {
        return "USER_ENABLEMENT".equals(user.path("role").asText());
    }

    private boolean matchesOrganizationalJobs(ObjectNode user, Set<String> jobIds) {
        if (jobIds.isEmpty()) {
            return false;
        }
        JsonNode scope = user.path("accessScope");
        for (String jobId : jobIds) {
            if (contains(scope.path("businessJobIds"), jobId) || (isEnablement(user) && jobMatchesBusinessUnit(user, jobId))) {
                return true;
            }
        }
        return false;
    }

    private boolean jobMatchesBusinessUnit(ObjectNode user, String jobId) {
        JsonNode businessUnitIds = user.path("accessScope").path("businessUnitIds");
        return store
            .find("metiers", jobId)
            .filter(job -> contains(businessUnitIds, job.path("businessUnitId").asText()))
            .isPresent();
    }

    private ObjectNode withPermissions(ObjectNode source) {
        ObjectNode user = source.deepCopy();
        projectEffectiveScope(user);
        ArrayNode permissions = user.putArray("permissions");
        permissions(user).forEach(permissions::add);
        return user;
    }

    /**
     * Builds the compatibility projection consumed by the frontend from the current reference
     * hierarchy. The persisted user remains limited to its BU/job assignment.
     */
    private void projectEffectiveScope(ObjectNode user) {
        JsonNode sourceScope = user.path("accessScope");
        if (!sourceScope.isObject() || isGlobal(user)) {
            return;
        }

        LinkedHashSet<String> businessUnitIds = values(sourceScope.path("businessUnitIds"));
        LinkedHashSet<String> businessJobIds = values(sourceScope.path("businessJobIds"));
        if (isEnablement(user)) {
            businessJobIds.clear();
            store
                .list("metiers")
                .stream()
                .filter(this::isAvailableReference)
                .filter(job -> businessUnitIds.contains(job.path("businessUnitId").asText()))
                .map(job -> job.path("id").asText())
                .filter(id -> !id.isBlank())
                .forEach(businessJobIds::add);
        } else {
            businessJobIds
                .stream()
                .map(jobId -> store.find("metiers", jobId).orElse(null))
                .filter(java.util.Objects::nonNull)
                .map(job -> job.path("businessUnitId").asText())
                .filter(id -> !id.isBlank())
                .forEach(businessUnitIds::add);
        }

        LinkedHashSet<String> applicationIds = new LinkedHashSet<>();
        store
            .list("applications")
            .stream()
            .filter(this::isAvailableReference)
            .filter(application -> intersects(businessUnitIds, applicationBusinessUnitIds(application)))
            .map(application -> application.path("id").asText())
            .filter(id -> !id.isBlank())
            .forEach(applicationIds::add);

        LinkedHashSet<String> moduleIds = new LinkedHashSet<>();
        store
            .list("modules")
            .stream()
            .filter(this::isAvailableReference)
            .filter(module -> applicationIds.contains(module.path("applicationId").asText()))
            .map(module -> module.path("id").asText())
            .filter(id -> !id.isBlank())
            .forEach(moduleIds::add);

        ObjectNode effectiveScope = user.putObject("accessScope");
        effectiveScope.put("global", false);
        putValues(effectiveScope, "businessUnitIds", businessUnitIds);
        putValues(effectiveScope, "businessJobIds", businessJobIds);
        putValues(effectiveScope, "applicationIds", applicationIds);
        putValues(effectiveScope, "moduleIds", moduleIds);
    }

    private boolean isAvailableReference(JsonNode value) {
        return !"ARCHIVED".equals(value.path("status").asText());
    }

    private LinkedHashSet<String> applicationBusinessUnitIds(JsonNode application) {
        LinkedHashSet<String> directIds = values(application.path("businessUnitIds"));
        if (!directIds.isEmpty()) return directIds;

        LinkedHashSet<String> result = new LinkedHashSet<>();
        values(application.path("businessJobIds"))
            .stream()
            .map(jobId -> store.find("metiers", jobId).orElse(null))
            .filter(java.util.Objects::nonNull)
            .map(job -> job.path("businessUnitId").asText())
            .forEach(value -> add(result, value));
        return result;
    }

    private ObjectNode requireActive(ObjectNode user) {
        if (!"ACTIVE".equals(user.path("status").asText())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "The DOVE account is disabled");
        }
        return user;
    }

    private boolean matches(ObjectNode user, Set<String> candidates) {
        return candidates
            .stream()
            .filter(value -> value != null && !value.isBlank())
            .anyMatch(value ->
                List.of("id", "externalSubject", "login", "email")
                    .stream()
                    .map(user::path)
                    .map(JsonNode::asText)
                    .anyMatch(value::equalsIgnoreCase)
            );
    }

    private static boolean contains(JsonNode array, String expected) {
        if (!array.isArray() || expected.isBlank()) {
            return false;
        }
        for (JsonNode value : array) {
            if (expected.equals(value.asText())) {
                return true;
            }
        }
        return false;
    }

    private static boolean intersects(JsonNode left, JsonNode right) {
        if (!left.isArray() || !right.isArray()) {
            return false;
        }
        for (JsonNode value : right) {
            if (contains(left, value.asText())) {
                return true;
            }
        }
        return false;
    }

    private static boolean intersects(Set<String> left, Set<String> right) {
        return left.stream().anyMatch(right::contains);
    }

    private static LinkedHashSet<String> values(JsonNode array) {
        LinkedHashSet<String> result = new LinkedHashSet<>();
        if (array.isArray()) {
            array.forEach(value -> add(result, value.asText()));
        }
        return result;
    }

    private static void putValues(ObjectNode target, String field, Set<String> values) {
        ArrayNode array = target.putArray(field);
        values.forEach(array::add);
    }

    private static void add(Set<String> values, String value) {
        Optional.ofNullable(value)
            .filter(candidate -> !candidate.isBlank())
            .ifPresent(values::add);
    }
}
