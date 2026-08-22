package sn.dove.backend.dove.web;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.ObjectNode;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/v1")
public class ContentResourceV1 {

    private static final List<String> PUBLIC_STATUSES = List.of("PUBLIER", "A_REVISER");
    private static final Set<String> FORMATS = Set.of("VIDEO", "FICHEPRATIQUE", "FAQ");
    private static final Map<String, List<String>> TRANSITIONS = Map.of(
        "BROUILLON",
        List.of("EN_ATTENTE_VALIDATION"),
        "EN_ATTENTE_VALIDATION",
        List.of("BROUILLON", "VALIDER"),
        "VALIDER",
        List.of("BROUILLON", "PUBLIER"),
        "PUBLIER",
        List.of("A_REVISER", "ARCHIVER"),
        "A_REVISER",
        List.of("BROUILLON", "ARCHIVER"),
        "ARCHIVER",
        List.of("BROUILLON")
    );

    private final DoveApiSupport api;

    public ContentResourceV1(DoveApiSupport api) {
        this.api = api;
    }

    @GetMapping("/contents")
    @PreAuthorize("@doveAuthorization.has('READ_CONTENT')")
    public DovePage<ObjectNode> contents(
        @RequestParam(defaultValue = "") String q,
        @RequestParam(required = false) List<String> applicationId,
        @RequestParam(required = false) List<String> businessJobId,
        @RequestParam(required = false) List<String> moduleId,
        @RequestParam(required = false) List<String> format,
        @RequestParam(required = false) List<String> status,
        @RequestParam(required = false) String ownerId,
        @RequestParam(required = false) String validatorId,
        @RequestParam(required = false) Boolean featured,
        @RequestParam(defaultValue = "false") boolean browseOtherBusinessJobs,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size,
        @RequestParam(defaultValue = "updatedAt,desc") String sort
    ) {
        ObjectNode user = api.currentUser();
        String normalizedQuery = DoveApiSupport.normalize(q);
        List<String> applications = safe(applicationId);
        List<String> jobs = safe(businessJobId);
        List<String> modules = safe(moduleId);
        List<String> formats = safe(format);
        List<String> statuses = safe(status);
        List<ObjectNode> result = canonicalContents()
            .stream()
            .filter(content -> canAccess(user, content, jobs, browseOtherBusinessJobs))
            .filter(content -> matchesVisibility(user, content, statuses))
            .filter(content -> applications.isEmpty() || applications.contains(content.path("applicationId").asText()))
            .filter(content -> jobs.isEmpty() || jobs.contains(content.path("businessJobId").asText()))
            .filter(content -> modules.isEmpty() || modules.contains(content.path("moduleId").asText()))
            .filter(content -> formats.isEmpty() || formats.stream().anyMatch(value -> hasFormat(content, value)))
            .filter(content -> statuses.isEmpty() || statuses.contains(content.path("status").asText()))
            .filter(content -> ownerId == null || ownerId.isBlank() || ownerId.equals(content.path("ownerId").asText()))
            .filter(content -> validatorId == null || validatorId.isBlank() || validatorId.equals(content.path("validatorId").asText()))
            .filter(content -> featured == null || featured == content.path("featured").asBoolean(false))
            .filter(content -> normalizedQuery.isBlank() || searchableText(content).contains(normalizedQuery))
            .sorted(comparator(sort))
            .toList();
        if (!normalizedQuery.isBlank()) {
            List<ObjectNode> exactTitleMatches = result
                .stream()
                .filter(content -> DoveApiSupport.normalize(content.path("title").asText()).equals(normalizedQuery))
                .toList();
            if (!exactTitleMatches.isEmpty()) {
                result = exactTitleMatches;
            }
        }
        return DovePage.of(result, page, size);
    }

    @GetMapping("/contents/{id}")
    @PreAuthorize("@doveAuthorization.has('READ_CONTENT')")
    public ObjectNode content(@PathVariable String id) {
        ObjectNode user = api.currentUser();
        ObjectNode content = api.require("contenus", id);
        boolean explicitCross = api.users.permissions(user).contains("READ_CROSS_BUSINESS_JOB");
        if (!canAccess(user, content, explicitCross ? List.of(content.path("businessJobId").asText()) : List.of(), explicitCross)) {
            throw api.notFound("content");
        }
        return content;
    }

    @GetMapping("/contents/similar")
    @PreAuthorize("@doveAuthorization.has('CREATE_CONTENT') or @doveAuthorization.has('EDIT_CONTENT')")
    public Map<String, List<ObjectNode>> similar(
        @RequestParam String title,
        @RequestParam(required = false) String applicationId,
        @RequestParam(required = false) String businessJobId,
        @RequestParam(required = false) String moduleId,
        @RequestParam(required = false) String excludeContentId,
        @RequestParam(defaultValue = "5") int limit
    ) {
        String normalized = DoveApiSupport.normalize(title);
        if (normalized.length() < 3) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "title must contain at least 3 characters");
        }
        ObjectNode user = api.currentUser();
        List<ObjectNode> items = canonicalContents()
            .stream()
            .filter(content -> api.users.isInMutationScope(user, content))
            .filter(content -> excludeContentId == null || !excludeContentId.equals(content.path("id").asText()))
            .filter(content -> DoveApiSupport.equalsText(content, "applicationId", applicationId))
            .filter(content -> DoveApiSupport.equalsText(content, "businessJobId", businessJobId))
            .filter(content -> DoveApiSupport.equalsText(content, "moduleId", moduleId))
            .map(content -> similarityItem(content, normalized))
            .filter(item -> item.path("similarityScore").asDouble() >= 0.35)
            .sorted(Comparator.comparingDouble(item -> -item.path("similarityScore").asDouble()))
            .limit(Math.min(10, Math.max(1, limit)))
            .toList();
        return Map.of("items", items);
    }

    @PostMapping("/contents")
    @PreAuthorize("@doveAuthorization.has('CREATE_CONTENT') or @doveAuthorization.has('CONTRIBUTE')")
    public ObjectNode create(@RequestBody JsonNode input) {
        ObjectNode user = api.currentUser();
        ObjectNode value = api.newDocument(input);
        validateEditorInput(value);
        if (!api.users.isInMutationScope(user, value)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Content is outside the mutation scope");
        }
        String normalizedTitle = DoveApiSupport.normalize(value.path("title").asText());
        api
            .store
            .list("contenus")
            .stream()
            .filter(existing -> api.users.isInMutationScope(user, existing))
            .filter(existing -> sameContext(existing, value))
            .filter(existing -> DoveApiSupport.normalize(existing.path("title").asText()).equals(normalizedTitle))
            .findFirst()
            .ifPresent(existing -> {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "CONTENT_ALREADY_EXISTS:" + existing.path("id").asText());
            });

        String now = Instant.now().toString();
        value.put("id", UUID.randomUUID().toString());
        value.put("status", "BROUILLON");
        value.put("authorId", user.path("id").asText());
        if (value.path("ownerId").asText().isBlank()) {
            value.put("ownerId", user.path("id").asText());
        }
        value.put("version", 1);
        value.put("updatedAt", now);
        value.put("viewCount", 0);
        value.put("helpfulCount", 0);
        value.put("featured", value.path("featured").asBoolean(false));
        normalizeFormats(value);
        ObjectNode created = api.store.create("contenus", value);
        api.events.audit(user.path("id").asText(), "CREATE_CONTENT", created.path("id").asText(), "SUCCESS");
        return created;
    }

    @PutMapping("/contents/{id}")
    @PreAuthorize("@doveAuthorization.has('EDIT_CONTENT')")
    public ObjectNode update(@PathVariable String id, @RequestBody JsonNode input) {
        ObjectNode user = api.currentUser();
        ObjectNode current = api.require("contenus", id);
        if (!api.users.isInMutationScope(user, current)) {
            throw api.notFound("content");
        }
        ObjectNode value = api.newDocument(input);
        validateEditorInput(value);
        if (!api.users.isInMutationScope(user, value)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Content is outside the mutation scope");
        }
        copySystemField(current, value, "status");
        copySystemField(current, value, "authorId");
        copySystemField(current, value, "publishedById");
        copySystemField(current, value, "validatedAt");
        copySystemField(current, value, "publishedAt");
        copySystemField(current, value, "viewCount");
        copySystemField(current, value, "helpfulCount");
        value.put("version", current.path("version").asInt(1) + 1);
        value.put("updatedAt", Instant.now().toString());
        normalizeFormats(value);
        ObjectNode updated = api.store.replace("contenus", id, value).orElseThrow(() -> api.notFound("content"));
        api.events.audit(user.path("id").asText(), "UPDATE_CONTENT", id, "SUCCESS");
        return updated;
    }

    @PatchMapping("/contents/{id}")
    @PreAuthorize("@doveAuthorization.has('EDIT_CONTENT')")
    public ObjectNode patch(@PathVariable String id, @RequestBody JsonNode input) {
        ObjectNode user = api.currentUser();
        ObjectNode current = api.require("contenus", id);
        if (!api.users.isInMutationScope(user, current)) {
            throw api.notFound("content");
        }
        ObjectNode patch = api.newDocument(input);
        patch.put("version", current.path("version").asInt(1) + 1);
        patch.put("updatedAt", Instant.now().toString());
        ObjectNode updated = api.store.patch("contenus", id, patch).orElseThrow(() -> api.notFound("content"));
        api.events.audit(user.path("id").asText(), "PATCH_CONTENT", id, "SUCCESS");
        return updated;
    }

    @PostMapping("/contents/{id}/transitions")
    public ObjectNode transition(@PathVariable String id, @RequestBody JsonNode input) {
        ObjectNode user = api.currentUser();
        ObjectNode content = api.require("contenus", id);
        if (!api.users.isInMutationScope(user, content)) {
            throw api.notFound("content");
        }
        String target = api.requireText(input, "targetStatus");
        String currentStatus = content.path("status").asText();
        if (!TRANSITIONS.getOrDefault(currentStatus, List.of()).contains(target)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Invalid content transition");
        }
        api.users.requirePermission(permissionForTransition(target));
        if ("VALIDER".equals(target)) {
            String validatorId = content.path("validatorId").asText();
            if (!validatorId.equals(user.path("id").asText()) || validatorId.equals(content.path("ownerId").asText())) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only the assigned independent validator can validate");
            }
        }
        String now = Instant.now().toString();
        ObjectNode patch = tools.jackson.databind.node.JsonNodeFactory.instance.objectNode();
        patch.put("status", target);
        patch.put("version", content.path("version").asInt(1) + 1);
        patch.put("updatedAt", now);
        if ("VALIDER".equals(target)) {
            patch.put("validatedAt", now);
        }
        if ("PUBLIER".equals(target)) {
            patch.put("publishedAt", now);
            patch.put("publishedById", user.path("id").asText());
        }
        ObjectNode updated = api.store.patch("contenus", id, patch).orElseThrow(() -> api.notFound("content"));
        api.events.audit(user.path("id").asText(), "CONTENT_" + target, id, "SUCCESS");
        String ownerId = content.path("ownerId").asText();
        if (!ownerId.isBlank() && !ownerId.equals(user.path("id").asText())) {
            api.events.notification(ownerId, "WORKFLOW", "Statut du contenu mis à jour", "Le contenu « " + content.path("title").asText() + " » est maintenant " + target + ".", "/manage/contents");
        }
        return updated;
    }

    @PutMapping("/contents/{id}/featured")
    @PreAuthorize("@doveAuthorization.has('EDIT_CONTENT')")
    public ObjectNode featured(@PathVariable String id, @RequestBody JsonNode input) {
        ObjectNode user = api.currentUser();
        ObjectNode content = api.require("contenus", id);
        if (!api.users.isInMutationScope(user, content)) {
            throw api.notFound("content");
        }
        boolean featured = input.path("featured").asBoolean(false);
        if (featured && !"PUBLIER".equals(content.path("status").asText())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Only published content can be featured");
        }
        ObjectNode patch = tools.jackson.databind.node.JsonNodeFactory.instance.objectNode();
        patch.put("featured", featured);
        patch.put("updatedAt", Instant.now().toString());
        return api.store.patch("contenus", id, patch).orElseThrow(() -> api.notFound("content"));
    }

    @GetMapping("/content-validation-queue")
    @PreAuthorize("@doveAuthorization.has('VALIDATE_CONTENT')")
    public List<ObjectNode> validationQueue() {
        ObjectNode user = api.currentUser();
        return api
            .store
            .list("contenus")
            .stream()
            .filter(content -> "EN_ATTENTE_VALIDATION".equals(content.path("status").asText()))
            .filter(content -> user.path("id").asText().equals(content.path("validatorId").asText()))
            .toList();
    }

    private boolean canAccess(ObjectNode user, ObjectNode content, List<String> requestedJobs, boolean browseOtherBusinessJobs) {
        String userId = user.path("id").asText();
        if (userId.equals(content.path("authorId").asText())) {
            return true;
        }
        if (api.users.permissions(user).contains("EDIT_CONTENT") && api.users.isInMutationScope(user, content)) {
            return true;
        }
        boolean explicitCross = browseOtherBusinessJobs || requestedJobs.contains(content.path("businessJobId").asText());
        return api.users.canReadContent(user, content, explicitCross);
    }

    private boolean matchesVisibility(ObjectNode user, ObjectNode content, List<String> requestedStatuses) {
        if (api.users.permissions(user).contains("EDIT_CONTENT") || api.users.isGlobal(user)) {
            return true;
        }
        if (user.path("id").asText().equals(content.path("authorId").asText())) {
            return true;
        }
        return PUBLIC_STATUSES.contains(content.path("status").asText()) &&
        (requestedStatuses.isEmpty() || requestedStatuses.stream().allMatch(PUBLIC_STATUSES::contains));
    }

    private static Comparator<ObjectNode> comparator(String sort) {
        String[] parts = sort.split(",", 2);
        String field = Set.of("title", "viewCount", "updatedAt").contains(parts[0]) ? parts[0] : "updatedAt";
        boolean descending = parts.length < 2 || !"asc".equalsIgnoreCase(parts[1]);
        Comparator<ObjectNode> comparator = "viewCount".equals(field)
            ? Comparator.comparingInt(content -> content.path(field).asInt())
            : Comparator.comparing(content -> content.path(field).asText());
        return descending ? comparator.reversed() : comparator;
    }

    /**
     * Legacy data may still contain one row per format. Once a richer aggregate
     * exists for the same title and functional context, only that canonical
     * content is exposed in search results. Direct access by an old identifier
     * remains possible during migration.
     */
    private List<ObjectNode> canonicalContents() {
        Map<String, ObjectNode> canonical = new LinkedHashMap<>();
        for (ObjectNode content : api.store.list("contenus")) {
            String key = DoveApiSupport.normalize(content.path("title").asText()) +
            "|" +
            content.path("applicationId").asText() +
            "|" +
            content.path("businessJobId").asText() +
            "|" +
            content.path("moduleId").asText();
            ObjectNode current = canonical.get(key);
            if (current == null || contentRichness(content) > contentRichness(current)) {
                canonical.put(key, content);
            }
        }
        return List.copyOf(canonical.values());
    }

    private static int contentRichness(ObjectNode content) {
        return formats(content).size() * 100 +
        content.path("videoItems").size() * 10 +
        content.path("steps").size() * 10 +
        content.path("faqItems").size() * 10 +
        (content.path("description").asText().isBlank() ? 0 : 1);
    }

    private static boolean hasFormat(ObjectNode content, String format) {
        JsonNode formats = content.path("formats");
        if (formats.isArray()) {
            for (JsonNode value : formats) {
                if (format.equals(value.asText())) {
                    return true;
                }
            }
        }
        return format.equals(content.path("type").asText());
    }

    private static String searchableText(ObjectNode content) {
        List<String> values = new ArrayList<>();
        values.add(content.path("title").asText());
        values.add(content.path("description").asText());
        values.add(content.path("objective").asText());
        content.path("tags").forEach(value -> values.add(value.asText()));
        content.path("summary").forEach(value -> values.add(value.asText()));
        content.path("steps").forEach(step -> values.add(step.path("title").asText() + " " + step.path("description").asText()));
        content.path("faqItems").forEach(item -> values.add(item.path("question").asText() + " " + item.path("answer").asText()));
        return DoveApiSupport.normalize(String.join(" ", values));
    }

    private static void validateEditorInput(ObjectNode value) {
        for (String field : List.of("title", "description", "applicationId", "businessJobId", "moduleId")) {
            if (value.path(field).asText().isBlank()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, field + " is required");
            }
        }
        if (formats(value).isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "At least one content format is required");
        }
    }

    private static List<String> formats(ObjectNode value) {
        List<String> formats = new ArrayList<>();
        value.path("formats").forEach(format -> {
            if (FORMATS.contains(format.asText())) {
                formats.add(format.asText());
            }
        });
        String primary = value.path("type").asText();
        if (formats.isEmpty() && FORMATS.contains(primary)) {
            formats.add(primary);
        }
        return formats.stream().distinct().toList();
    }

    private static void normalizeFormats(ObjectNode value) {
        List<String> formats = formats(value);
        ArrayNode array = value.putArray("formats");
        formats.forEach(array::add);
        value.put("type", formats.get(0));
        value.put("typeContenu", formats.get(0));
        if (!formats.contains("VIDEO")) {
            value.remove("mediaId");
            value.putArray("videoItems");
        }
        if (!formats.contains("FICHEPRATIQUE")) {
            value.putArray("steps");
        }
        if (!formats.contains("FAQ")) {
            value.putArray("faqItems");
        }
    }

    private static boolean sameContext(ObjectNode left, ObjectNode right) {
        return List.of("applicationId", "businessJobId", "moduleId").stream().allMatch(field -> left.path(field).asText().equals(right.path(field).asText()));
    }

    private static ObjectNode similarityItem(ObjectNode content, String normalizedTitle) {
        String candidate = DoveApiSupport.normalize(content.path("title").asText());
        double score = similarity(normalizedTitle, candidate);
        ObjectNode result = tools.jackson.databind.node.JsonNodeFactory.instance.objectNode();
        for (String field : List.of("id", "title", "applicationId", "businessJobId", "moduleId", "status")) {
            result.set(field, content.path(field));
        }
        ArrayNode existingFormats = result.putArray("formats");
        formats(content).forEach(existingFormats::add);
        ArrayNode missingFormats = result.putArray("missingFormats");
        FORMATS.stream().filter(format -> !formats(content).contains(format)).sorted().forEach(missingFormats::add);
        result.put("similarityScore", Math.round(score * 100.0) / 100.0);
        result.put("canEdit", true);
        return result;
    }

    private static double similarity(String left, String right) {
        if (left.equals(right)) {
            return 1.0;
        }
        if (left.contains(right) || right.contains(left)) {
            return 0.85;
        }
        String[] leftTokens = left.split(" ");
        String[] rightTokens = right.split(" ");
        Map<String, Boolean> tokens = new HashMap<>();
        for (String token : leftTokens) tokens.put(token, false);
        int common = 0;
        for (String token : rightTokens) {
            if (tokens.containsKey(token)) common++;
        }
        return (2.0 * common) / Math.max(1, leftTokens.length + rightTokens.length);
    }

    private static String permissionForTransition(String target) {
        return switch (target) {
            case "EN_ATTENTE_VALIDATION" -> "SUBMIT_CONTENT_FOR_VALIDATION";
            case "VALIDER" -> "VALIDATE_CONTENT";
            case "PUBLIER" -> "PUBLISH_CONTENT";
            case "ARCHIVER" -> "ARCHIVE_CONTENT";
            default -> "EDIT_CONTENT";
        };
    }

    private static void copySystemField(ObjectNode source, ObjectNode target, String field) {
        JsonNode value = source.get(field);
        if (value != null && !value.isNull()) {
            target.set(field, value);
        }
    }

    private static List<String> safe(List<String> values) {
        return values == null ? List.of() : values;
    }
}
