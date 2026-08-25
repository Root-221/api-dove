package sn.dove.backend.dove.service;

import java.text.Normalizer;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.function.Predicate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.ObjectNode;

/**
 * Enforces the organizational hierarchy stored in the DOVE JSON resources.
 *
 * <p>Those relations cannot use database foreign keys while the v1 facade persists JSON
 * documents, so every administrative mutation must be checked here before it reaches the store.
 */
@Service
public class DoveScopeConsistencyService {

    private static final Set<String> USER_ROLES = Set.of("BUSINESS_USER", "NANDITE", "USER_ENABLEMENT", "ADMIN");

    private final DoveResourceStore store;

    public DoveScopeConsistencyService(DoveResourceStore store) {
        this.store = store;
    }

    public ObjectNode normalizeReference(String resourceType, String id, ObjectNode value) {
        value.put("name", requiredText(value, "name"));
        requireUniqueName(resourceType, id, value.path("name").asText(), parentField(resourceType), value);

        switch (resourceType) {
            case "businessUnits" -> value.put("status", referenceStatus(value, "ACTIVE", Set.of("ACTIVE", "ARCHIVED")));
            case "metiers" -> normalizeBusinessJob(id, value);
            case "applications" -> normalizeApplication(id, value);
            case "modules" -> normalizeModule(id, value);
            default -> throw badRequest("Type de référentiel inconnu.");
        }
        return value;
    }

    /**
     * Canonical user scope only stores stable organizational assignments.
     *
     * <p>Applications and modules are deliberately not assigned to users: they are resolved from
     * the reference hierarchy at request time. A new application or module therefore becomes
     * available immediately to every eligible user without rewriting thousands of user rows.
     */
    public ObjectNode normalizeUser(ObjectNode user) {
        String role = requiredText(user, "role");
        if (!USER_ROLES.contains(role)) throw badRequest("Le rôle utilisateur est invalide.");
        user.put("role", role);
        String status = user.path("status").asText("ACTIVE");
        if (!Set.of("ACTIVE", "DISABLED").contains(status)) {
            throw badRequest("Le statut utilisateur est invalide.");
        }
        user.put("status", status);

        if ("ADMIN".equals(role)) {
            ObjectNode scope = user.putObject("accessScope");
            scope.put("global", true);
            scope.putArray("applicationIds");
            scope.putArray("businessUnitIds");
            scope.putArray("businessJobIds");
            scope.putArray("moduleIds");
            return user;
        }

        JsonNode requestedScope = user.path("accessScope");
        if (!requestedScope.isObject()) throw badRequest("Le périmètre utilisateur est obligatoire.");

        ObjectNode scope = user.putObject("accessScope");
        scope.put("global", false);
        if ("USER_ENABLEMENT".equals(role)) {
            List<String> businessUnitIds = ids(requestedScope.path("businessUnitIds"));
            // Compatibility with users created by the former UI, where the BU was derived from a
            // selected job. The canonical stored result still contains the BU only.
            if (businessUnitIds.isEmpty()) {
                List<String> legacyJobIds = ids(requestedScope.path("businessJobIds"));
                if (legacyJobIds.size() == 1) {
                    ObjectNode legacyJob = requireOperational(
                        "metiers",
                        legacyJobIds.getFirst(),
                        "Le métier sélectionné n'existe pas ou est archivé."
                    );
                    businessUnitIds = List.of(requiredText(legacyJob, "businessUnitId"));
                }
            }
            if (businessUnitIds.size() != 1) {
                throw badRequest("Un User Enablement doit être rattaché à une seule Business Unit.");
            }
            String businessUnitId = businessUnitIds.getFirst();
            requireOperational(
                "businessUnits",
                businessUnitId,
                "La Business Unit sélectionnée n'existe pas ou est archivée."
            );
            putIds(scope, "businessUnitIds", List.of(businessUnitId));
            putIds(scope, "businessJobIds", List.of());
        } else {
            List<String> jobIds = requiredIds(requestedScope, "businessJobIds");
            if (jobIds.size() != 1) throw badRequest("Un utilisateur doit être rattaché à un seul métier principal.");
            String jobId = jobIds.getFirst();
            ObjectNode job = requireOperational("metiers", jobId, "Le métier sélectionné n'existe pas ou est archivé.");
            String businessUnitId = requiredText(job, "businessUnitId");
            requireOperational("businessUnits", businessUnitId, "La Business Unit du métier n'existe pas ou est archivée.");
            putIds(scope, "businessUnitIds", List.of(businessUnitId));
            putIds(scope, "businessJobIds", List.of(jobId));
        }
        putIds(scope, "applicationIds", List.of());
        putIds(scope, "moduleIds", List.of());
        return user;
    }

    public void requireValidResourceTuple(JsonNode resource) {
        String applicationId = requiredText(resource, "applicationId");
        String businessJobId = requiredText(resource, "businessJobId");
        String moduleId = requiredText(resource, "moduleId");
        ObjectNode application = requireOperational(
            "applications",
            applicationId,
            "L'application sélectionnée n'existe pas ou est archivée."
        );
        ObjectNode businessJob = requireOperational("metiers", businessJobId, "Le métier sélectionné n'existe pas ou est archivé.");
        ObjectNode module = requireOperational("modules", moduleId, "Le module sélectionné n'existe pas ou est archivé.");

        if (!applicationId.equals(module.path("applicationId").asText())) {
            throw badRequest("Le module sélectionné n'appartient pas à l'application.");
        }
        ObjectNode businessUnit = requireOperational(
            "businessUnits",
            businessJob.path("businessUnitId").asText(),
            "La Business Unit du métier n'existe pas ou est archivée."
        );
        if (!applicationBusinessUnitIds(application).contains(businessUnit.path("id").asText())) {
            throw badRequest("La Business Unit du métier n'est pas autorisée sur cette application.");
        }
    }

    /**
     * Derives a content's métier scope from the Business Units assigned to its application. The
     * editor never chooses a métier: a User Enablement receives every active métier in their BU,
     * while other scoped users only receive their own métier when its BU is assigned.
     */
    public void normalizeContentScope(ObjectNode actor, ObjectNode content) {
        String applicationId = requiredText(content, "applicationId");
        String moduleId = requiredText(content, "moduleId");
        ObjectNode application = requireOperational(
            "applications",
            applicationId,
            "L'application sélectionnée n'existe pas ou est archivée."
        );
        ObjectNode module = requireOperational("modules", moduleId, "Le module sélectionné n'existe pas ou est archivé.");
        if (!applicationId.equals(module.path("applicationId").asText())) {
            throw badRequest("Le module sélectionné n'appartient pas à l'application.");
        }

        Set<String> actorBusinessUnitIds = new LinkedHashSet<>(ids(actor.path("accessScope").path("businessUnitIds")));
        Set<String> actorBusinessJobIds = new LinkedHashSet<>(ids(actor.path("accessScope").path("businessJobIds")));
        boolean global = actor.path("accessScope").path("global").asBoolean(false);
        boolean enablement = "USER_ENABLEMENT".equals(actor.path("role").asText());
        Set<String> applicationBusinessUnitIds = new LinkedHashSet<>(applicationBusinessUnitIds(application));
        List<ObjectNode> jobs = store
            .list("metiers")
            .stream()
            .filter(job -> "ACTIVE".equals(job.path("status").asText("ACTIVE")))
            .filter(job -> applicationBusinessUnitIds.contains(job.path("businessUnitId").asText()))
            .filter(job ->
                global ||
                (enablement
                        ? actorBusinessUnitIds.contains(job.path("businessUnitId").asText())
                        : actorBusinessJobIds.contains(job.path("id").asText()))
            )
            .toList();
        if (jobs.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "L'application est hors du périmètre métier de l'utilisateur.");
        }

        List<String> jobIds = jobs.stream().map(job -> job.path("id").asText()).toList();
        List<String> businessUnitIds = jobs
            .stream()
            .map(job -> job.path("businessUnitId").asText())
            .filter(value -> !value.isBlank())
            .distinct()
            .toList();
        businessUnitIds.forEach(businessUnitId ->
            requireOperational(
                "businessUnits",
                businessUnitId,
                "La Business Unit d'un métier de l'application n'existe pas ou est archivée."
            )
        );

        putIds(content, "businessJobIds", jobIds);
        putIds(content, "businessUnitIds", businessUnitIds);
        // Temporary read projection for clients and legacy integrations that still display one métier.
        content.put("businessJobId", jobIds.getFirst());
    }

    public void requireArchivable(String resourceType, String id) {
        switch (resourceType) {
            case "businessUnits" -> requireNoDependency(
                hasActiveMatch("metiers", job -> id.equals(job.path("businessUnitId").asText())) ||
                    hasActiveArrayReference("applications", "businessUnitIds", id) ||
                    hasActiveScopeReference("businessUnitIds", id),
                "Cette Business Unit contient encore des métiers actifs, des applications ou des utilisateurs rattachés."
            );
            case "metiers" -> requireNoDependency(
                hasActiveScopeReference("businessJobIds", id) ||
                    hasActiveProposalReference("businessJobId", id) ||
                    hasActiveContentReference("businessJobId", id) ||
                    hasActiveContentArrayReference("businessJobIds", id),
                "Ce métier est encore utilisé par un utilisateur, une proposition ou un contenu."
            );
            case "applications" -> requireNoDependency(
                    hasActiveMatch("modules", module -> id.equals(module.path("applicationId").asText())) ||
                    hasActiveProposalReference("applicationId", id) ||
                    hasActiveContentReference("applicationId", id),
                "Cette application possède encore des modules, propositions ou contenus actifs."
            );
            case "modules" -> requireNoDependency(
                hasActiveProposalReference("moduleId", id) ||
                    hasActiveContentReference("moduleId", id),
                "Ce module est encore utilisé par des propositions ou des contenus actifs."
            );
            default -> throw badRequest("Type de référentiel inconnu.");
        }
    }

    /** Keeps stored module projections aligned with the application's authoritative BU scope. */
    public void synchronizeApplicationModules(JsonNode application) {
        String applicationId = requiredText(application, "id");
        List<String> businessUnitIds = applicationBusinessUnitIds(application);
        List<String> jobIds = activeJobIdsForBusinessUnits(businessUnitIds);
        store
            .list("modules")
            .stream()
            .filter(module -> applicationId.equals(module.path("applicationId").asText()))
            .forEach(module -> {
                ObjectNode patch = tools.jackson.databind.node.JsonNodeFactory.instance.objectNode();
                putIds(patch, "businessUnitIds", businessUnitIds);
                putIds(patch, "businessJobIds", jobIds);
                store.patch("modules", module.path("id").asText(), patch);
            });
    }

    /** Refreshes compatibility job projections when a BU gains, loses or moves a métier. */
    public void synchronizeBusinessUnitApplications(String businessUnitId) {
        if (businessUnitId == null || businessUnitId.isBlank()) return;
        store
            .list("applications")
            .stream()
            .filter(application -> applicationBusinessUnitIds(application).contains(businessUnitId))
            .forEach(application -> {
                List<String> jobIds = activeJobIdsForBusinessUnits(applicationBusinessUnitIds(application));
                ObjectNode patch = tools.jackson.databind.node.JsonNodeFactory.instance.objectNode();
                putIds(patch, "businessJobIds", jobIds);
                ObjectNode updated = store
                    .patch("applications", application.path("id").asText(), patch)
                    .orElse(application);
                synchronizeApplicationModules(updated);
            });
    }

    private void normalizeBusinessJob(String id, ObjectNode value) {
        String businessUnitId = requiredText(value, "businessUnitId");
        requireActive("businessUnits", businessUnitId, "La Business Unit sélectionnée n'existe pas ou est archivée.");
        ObjectNode existing = store.find("metiers", id).orElse(null);
        if (
            existing != null &&
            !businessUnitId.equals(existing.path("businessUnitId").asText()) &&
            hasActiveScopeReference("businessJobIds", id)
        ) {
            throw conflict("Le métier ne peut pas changer de Business Unit tant que des utilisateurs lui sont rattachés.");
        }
        value.put("businessUnitId", businessUnitId);
        value.put("status", referenceStatus(value, "ACTIVE", Set.of("ACTIVE", "ARCHIVED")));
    }

    private void normalizeApplication(String id, ObjectNode value) {
        String code = requiredText(value, "code").toUpperCase(Locale.ROOT);
        value.put("code", code);
        requireUniqueField("applications", id, "code", code, "Ce code application existe déjà.");
        List<String> businessUnitIds = ids(value.path("businessUnitIds"));
        if (businessUnitIds.isEmpty()) {
            businessUnitIds = ids(value.path("businessJobIds"))
                .stream()
                .map(jobId -> requireOperational("metiers", jobId, "Un métier historique de l'application est invalide."))
                .map(job -> requiredText(job, "businessUnitId"))
                .distinct()
                .toList();
        }
        if (businessUnitIds.isEmpty()) {
            throw badRequest("Une application doit être rattachée à au moins une Business Unit active.");
        }
        businessUnitIds.forEach(businessUnitId ->
            requireOperational(
                "businessUnits",
                businessUnitId,
                "Une Business Unit sélectionnée n'existe pas ou est archivée."
            )
        );
        List<String> jobIds = activeJobIdsForBusinessUnits(businessUnitIds);
        putIds(value, "businessUnitIds", businessUnitIds);
        putIds(value, "businessJobIds", jobIds);
        value.put("status", referenceStatus(value, "UPCOMING", Set.of("ACTIVE", "UPCOMING", "ARCHIVED")));

        ObjectNode existing = store.find("applications", id).orElse(null);
        if (existing != null) {
            Set<String> removedBusinessUnitIds = new LinkedHashSet<>(applicationBusinessUnitIds(existing));
            removedBusinessUnitIds.removeAll(businessUnitIds);
            if (!removedBusinessUnitIds.isEmpty() && hasActiveApplicationBusinessUnitUsage(id, removedBusinessUnitIds)) {
                throw conflict(
                    "Des contenus ou propositions utilisent encore une Business Unit retirée de cette application."
                );
            }
        }
    }

    private void normalizeModule(String id, ObjectNode value) {
        String applicationId = requiredText(value, "applicationId");
        ObjectNode application = requireActive(
            "applications",
            applicationId,
            "L'application sélectionnée n'existe pas ou est archivée."
        );
        List<String> businessUnitIds = applicationBusinessUnitIds(application);
        if (businessUnitIds.isEmpty()) {
            throw badRequest("L'application du module doit être rattachée à au moins une Business Unit active.");
        }
        List<String> jobIds = activeJobIdsForBusinessUnits(businessUnitIds);

        ObjectNode existing = store.find("modules", id).orElse(null);
        boolean hierarchyChanged = existing != null && !applicationId.equals(existing.path("applicationId").asText());
        if (
            hierarchyChanged &&
            (hasActiveProposalReference("moduleId", id) ||
                hasActiveContentReference("moduleId", id))
        ) {
            throw conflict("Le rattachement d'un module utilisé ne peut pas être modifié. Retirez d'abord ses affectations et contenus.");
        }

        value.put("applicationId", applicationId);
        putIds(value, "businessUnitIds", businessUnitIds);
        putIds(value, "businessJobIds", jobIds);
        value.put("status", referenceStatus(value, "ACTIVE", Set.of("ACTIVE", "ARCHIVED")));
    }

    private boolean hasActiveApplicationBusinessUnitUsage(String applicationId, Set<String> businessUnitIds) {
        boolean contentDependency = store
            .list("contenus")
            .stream()
            .filter(content -> !"ARCHIVER".equals(content.path("status").asText()))
            .anyMatch(content ->
                applicationId.equals(content.path("applicationId").asText()) &&
                ids(content.path("businessUnitIds")).stream().anyMatch(businessUnitIds::contains)
            );
        boolean proposalDependency = store
            .list("proposals")
            .stream()
            .filter(proposal -> !List.of("CONVERTED", "REJECTED").contains(proposal.path("status").asText()))
            .anyMatch(proposal ->
                applicationId.equals(proposal.path("applicationId").asText()) &&
                store
                    .find("metiers", proposal.path("businessJobId").asText())
                    .map(job -> businessUnitIds.contains(job.path("businessUnitId").asText()))
                    .orElse(false)
            );
        return contentDependency || proposalDependency;
    }

    private List<String> applicationBusinessUnitIds(JsonNode application) {
        List<String> directIds = ids(application.path("businessUnitIds"));
        if (!directIds.isEmpty()) return directIds;
        return ids(application.path("businessJobIds"))
            .stream()
            .map(jobId -> store.find("metiers", jobId).orElse(null))
            .filter(java.util.Objects::nonNull)
            .map(job -> job.path("businessUnitId").asText())
            .filter(id -> !id.isBlank())
            .distinct()
            .toList();
    }

    private List<String> activeJobIdsForBusinessUnits(List<String> businessUnitIds) {
        Set<String> allowedIds = new LinkedHashSet<>(businessUnitIds);
        return store
            .list("metiers")
            .stream()
            .filter(job -> "ACTIVE".equals(job.path("status").asText("ACTIVE")))
            .filter(job -> allowedIds.contains(job.path("businessUnitId").asText()))
            .map(job -> job.path("id").asText())
            .filter(id -> !id.isBlank())
            .distinct()
            .toList();
    }

    private void requireUniqueName(String type, String id, String name, String parentField, JsonNode value) {
        String normalizedName = normalize(name);
        boolean duplicate = store
            .list(type)
            .stream()
            .filter(candidate -> !id.equals(candidate.path("id").asText()))
            .filter(this::isActive)
            .filter(candidate -> parentField == null || candidate.path(parentField).asText().equals(value.path(parentField).asText()))
            .anyMatch(candidate -> normalize(candidate.path("name").asText()).equals(normalizedName));
        if (duplicate) throw conflict("Un élément actif porte déjà ce nom dans ce périmètre.");
    }

    private void requireUniqueField(String type, String id, String field, String value, String message) {
        boolean duplicate = store
            .list(type)
            .stream()
            .filter(candidate -> !id.equals(candidate.path("id").asText()))
            .filter(this::isActive)
            .anyMatch(candidate -> value.equalsIgnoreCase(candidate.path(field).asText()));
        if (duplicate) throw conflict(message);
    }

    private List<String> requiredIds(JsonNode value, String field) {
        List<String> result = ids(value.path(field));
        if (result.isEmpty()) throw badRequest(field + " doit contenir au moins une valeur.");
        return result;
    }

    private List<String> ids(JsonNode value) {
        if (!value.isArray()) return List.of();
        LinkedHashSet<String> result = new LinkedHashSet<>();
        value.forEach(item -> {
            String id = item.asText().trim();
            if (!id.isBlank()) result.add(id);
        });
        return result.stream().toList();
    }

    private void putIds(ObjectNode target, String field, List<String> ids) {
        ArrayNode array = target.putArray(field);
        ids.forEach(array::add);
    }

    private ObjectNode requireActive(String type, String id, String message) {
        return store.find(type, id).filter(this::isActive).orElseThrow(() -> badRequest(message));
    }

    private ObjectNode requireOperational(String type, String id, String message) {
        return store
            .find(type, id)
            .filter(value -> "ACTIVE".equals(value.path("status").asText("ACTIVE")))
            .orElseThrow(() -> badRequest(message));
    }

    private boolean hasActiveArrayReference(String type, String field, String id) {
        return hasActiveMatch(type, value -> contains(value.path(field), id));
    }

    private boolean hasActiveScopeReference(String field, String id) {
        return store
            .list("utilisateurs")
            .stream()
            .filter(user -> "ACTIVE".equals(user.path("status").asText("ACTIVE")))
            .anyMatch(user -> contains(user.path("accessScope").path(field), id));
    }

    private boolean hasActiveContentReference(String field, String id) {
        return store
            .list("contenus")
            .stream()
            .filter(content -> !"ARCHIVER".equals(content.path("status").asText()))
            .anyMatch(content -> id.equals(content.path(field).asText()));
    }

    private boolean hasActiveContentArrayReference(String field, String id) {
        return store
            .list("contenus")
            .stream()
            .filter(content -> !"ARCHIVER".equals(content.path("status").asText()))
            .anyMatch(content -> contains(content.path(field), id));
    }

    private boolean hasActiveProposalReference(String field, String id) {
        return store
            .list("proposals")
            .stream()
            .filter(proposal -> !List.of("CONVERTED", "REJECTED").contains(proposal.path("status").asText()))
            .anyMatch(proposal -> id.equals(proposal.path(field).asText()));
    }

    private boolean hasActiveMatch(String type, Predicate<ObjectNode> predicate) {
        return store.list(type).stream().filter(this::isActive).anyMatch(predicate);
    }

    private boolean isActive(JsonNode value) {
        return !"ARCHIVED".equals(value.path("status").asText());
    }

    private String referenceStatus(JsonNode value, String fallback, Set<String> allowedStatuses) {
        String status = value.path("status").asText(fallback);
        if (!allowedStatuses.contains(status)) {
            throw badRequest("Le statut du référentiel est invalide.");
        }
        return status;
    }

    private String requiredText(JsonNode value, String field) {
        String text = value.path(field).asText().trim();
        if (text.isBlank()) throw badRequest(field + " est obligatoire.");
        return text;
    }

    private String parentField(String resourceType) {
        return switch (resourceType) {
            case "metiers" -> "businessUnitId";
            case "modules" -> "applicationId";
            default -> null;
        };
    }

    private boolean contains(JsonNode values, String expected) {
        if (!values.isArray()) return false;
        for (JsonNode value : values) {
            if (expected.equals(value.asText())) return true;
        }
        return false;
    }

    private String normalize(String value) {
        return Normalizer
            .normalize(value, Normalizer.Form.NFD)
            .replaceAll("\\p{M}", "")
            .toLowerCase(Locale.ROOT)
            .replaceAll("[^a-z0-9]+", " ")
            .trim();
    }

    private void requireNoDependency(boolean dependencyExists, String message) {
        if (dependencyExists) throw conflict(message);
    }

    private ResponseStatusException badRequest(String message) {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
    }

    private ResponseStatusException conflict(String message) {
        return new ResponseStatusException(HttpStatus.CONFLICT, message);
    }
}
