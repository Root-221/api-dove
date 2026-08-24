package sn.dove.backend.dove.security;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import java.time.Duration;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;
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
     * typical access-token lifetimes) while avoiding a DB round trip on most requests. Only
     * positive lookups are cached (Caffeine's Cache#get skips caching a null mapping result),
     * so an unprovisioned subject never produces a stale "not found" for a user who was
     * provisioned moments later. The cached node is only ever read (requireActive) and
     * deep-copied (withPermissions) before being handed to a caller, never mutated in place, so
     * sharing the same cached instance across requests is safe.
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
            ObjectNode source = userBySubject.get(subject, key -> store.findByExternalSubject(USERS, key).orElse(null));
            if (source == null) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "The authenticated identity is not provisioned in DOVE");
            }
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

    public void requirePermission(String permission) {
        if (!hasPermission(permission)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Missing DOVE permission: " + permission);
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
        JsonNode scope = user.path("accessScope");
        return (
            contains(scope.path("applicationIds"), resource.path("applicationId").asText()) &&
            contains(scope.path("businessJobIds"), resource.path("businessJobId").asText()) &&
            contains(scope.path("moduleIds"), resource.path("moduleId").asText())
        );
    }

    public boolean canReadContent(ObjectNode user, JsonNode content, boolean explicitCrossBusinessJob) {
        if (isInMutationScope(user, content)) {
            return true;
        }
        boolean publicStatus = List.of("PUBLIER", "A_REVISER").contains(content.path("status").asText());
        JsonNode scope = user.path("accessScope");
        return (
            publicStatus &&
            explicitCrossBusinessJob &&
            permissions(user).contains("READ_CROSS_BUSINESS_JOB") &&
            contains(scope.path("applicationIds"), content.path("applicationId").asText())
        );
    }

    public boolean canSeeApplication(ObjectNode user, String id) {
        if (isGlobal(user) || contains(user.path("accessScope").path("applicationIds"), id)) {
            return true;
        }
        ObjectNode app = store.find("applications", id).orElse(null);
        if (app != null && app.path("businessJobIds").isArray()) {
            for (JsonNode jobId : app.path("businessJobIds")) {
                if (contains(user.path("accessScope").path("businessJobIds"), jobId.asText())) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean canSeeBusinessJob(ObjectNode user, String id, boolean browse) {
        return (
            isGlobal(user) ||
            contains(user.path("accessScope").path("businessJobIds"), id) ||
            (browse && permissions(user).contains("READ_CROSS_BUSINESS_JOB"))
        );
    }

    public boolean canSeeModule(ObjectNode user, JsonNode module, boolean browse) {
        if (isGlobal(user)) {
            return true;
        }
        if (!canSeeApplication(user, module.path("applicationId").asText())) {
            return false;
        }
        if (contains(user.path("accessScope").path("moduleIds"), module.path("id").asText())) {
            return true;
        }
        JsonNode jobIds = module.path("businessJobIds");
        if (jobIds.isArray() && !jobIds.isEmpty()) {
            for (JsonNode jobId : jobIds) {
                if (contains(user.path("accessScope").path("businessJobIds"), jobId.asText())) {
                    return true;
                }
            }
            return browse && permissions(user).contains("READ_CROSS_BUSINESS_JOB");
        }
        return (
            user.path("accessScope").path("moduleIds").isEmpty() ||
            contains(user.path("accessScope").path("applicationIds"), module.path("applicationId").asText())
        );
    }

    private ObjectNode withPermissions(ObjectNode source) {
        ObjectNode user = source.deepCopy();
        ArrayNode permissions = user.putArray("permissions");
        permissions(user).forEach(permissions::add);
        return user;
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

    private static void add(Set<String> values, String value) {
        Optional.ofNullable(value)
            .filter(candidate -> !candidate.isBlank())
            .ifPresent(values::add);
    }
}
