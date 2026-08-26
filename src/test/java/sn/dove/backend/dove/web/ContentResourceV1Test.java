package sn.dove.backend.dove.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.http.ResponseEntity;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import sn.dove.backend.dove.config.DoveProperties;
import sn.dove.backend.dove.security.DoveCurrentUserService;
import sn.dove.backend.dove.service.DoveDomainEvents;
import sn.dove.backend.dove.service.DoveResourceStore;
import sn.dove.backend.dove.service.DoveScopeConsistencyService;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.JsonNodeFactory;
import tools.jackson.databind.node.ObjectNode;

class ContentResourceV1Test {

    @Test
    void createDerivesJobsAndAlwaysMakesTheCreatorAuthorAndOwner() {
        InMemoryResourceStore store = new InMemoryResourceStore();
        ObjectNode unit = document("bu-1", "Direction Grand Public");
        ObjectNode firstJob = document("job-1", "Commercial");
        firstJob.put("businessUnitId", "bu-1");
        ObjectNode secondJob = document("job-2", "Recouvrement");
        secondJob.put("businessUnitId", "bu-1");
        ObjectNode application = document("app-1", "Parcours Client");
        application.putArray("businessUnitIds").add("bu-1");
        application.putArray("businessJobIds").add("job-1").add("job-2");
        ObjectNode module = document("module-1", "Commande");
        module.put("applicationId", "app-1");
        module.putArray("businessUnitIds").add("bu-1");
        module.putArray("businessJobIds").add("job-1").add("job-2");
        store.add("businessUnits", unit);
        store.add("metiers", firstJob);
        store.add("metiers", secondJob);
        store.add("applications", application);
        store.add("modules", module);

        ObjectNode creator = document("creator-1", "Awa Ndiaye");
        creator.put("role", "USER_ENABLEMENT");
        ObjectNode scope = creator.putObject("accessScope");
        scope.put("global", false);
        scope.putArray("businessUnitIds").add("bu-1");
        scope.putArray("businessJobIds").add("job-1");
        scope.putArray("applicationIds");
        scope.putArray("moduleIds");
        DoveCurrentUserService users = new FixedCurrentUserService(store, creator);
        DoveScopeConsistencyService consistency = new DoveScopeConsistencyService(store);
        ContentResourceV1 resource = new ContentResourceV1(
            new DoveApiSupport(store, users, new DoveDomainEvents(store)),
            consistency
        );

        ObjectNode input = JsonNodeFactory.instance.objectNode();
        input.put("title", "Créer une commande");
        input.put("description", "Procédure complète");
        input.put("applicationId", "app-1");
        input.put("moduleId", "module-1");
        input.put("businessJobId", "job-from-client");
        input.put("ownerId", "another-user");
        input.putArray("formats").add("FAQ");
        input.putArray("faqItems");
        input.putArray("summary");
        input.putArray("steps");
        input.putArray("tags");

        ObjectNode created = resource.create(input);

        assertThat(created.path("authorId").asText()).isEqualTo("creator-1");
        assertThat(created.path("ownerId").asText()).isEqualTo("creator-1");
        assertThat(created.path("businessJobIds")).extracting(node -> node.asText()).containsExactly("job-1", "job-2");
        assertThat(created.path("businessUnitIds")).extracting(node -> node.asText()).containsExactly("bu-1");
        assertThat(created.path("businessJobId").asText()).isEqualTo("job-1");
    }

    @Test
    void validatorCanValidateTheirOwnSubmittedContent() {
        InMemoryResourceStore store = new InMemoryResourceStore();
        addReferenceHierarchy(store);
        ObjectNode validator = enablementUser("validator-1", true);
        ContentResourceV1 resource = resource(store, validator);
        ObjectNode content = content("content-1", "validator-1", "EN_ATTENTE_VALIDATION");
        store.add("contenus", content);

        assertThat(resource.validationQueue()).extracting(item -> item.path("id").asText()).containsExactly("content-1");

        ObjectNode transition = JsonNodeFactory.instance.objectNode();
        transition.put("targetStatus", "VALIDER");
        ObjectNode validated = resource.transition("content-1", transition);

        assertThat(validated.path("status").asText()).isEqualTo("VALIDER");
        assertThat(validated.path("validatorId").asText()).isEqualTo("validator-1");
        assertThat(validated.path("validatedAt").asText()).isNotBlank();
    }

    @Test
    void authorCannotValidateContent() {
        InMemoryResourceStore store = new InMemoryResourceStore();
        addReferenceHierarchy(store);
        ObjectNode author = enablementUser("author-1", false);
        ContentResourceV1 resource = resource(store, author);
        store.add("contenus", content("content-1", "author-1", "EN_ATTENTE_VALIDATION"));
        ObjectNode transition = JsonNodeFactory.instance.objectNode();
        transition.put("targetStatus", "VALIDER");

        assertThatThrownBy(() -> resource.transition("content-1", transition))
            .isInstanceOfSatisfying(ResponseStatusException.class, exception ->
                assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN)
            );
    }

    @Test
    void authorCanPublishOnlyAfterValidation() {
        InMemoryResourceStore store = new InMemoryResourceStore();
        addReferenceHierarchy(store);
        ObjectNode author = enablementUser("author-1", false);
        ContentResourceV1 resource = resource(store, author);
        store.add("contenus", content("pending", "author-1", "EN_ATTENTE_VALIDATION"));
        store.add("contenus", content("validated", "author-1", "VALIDER"));
        ObjectNode transition = JsonNodeFactory.instance.objectNode();
        transition.put("targetStatus", "PUBLIER");

        assertThatThrownBy(() -> resource.transition("pending", transition))
            .isInstanceOfSatisfying(ResponseStatusException.class, exception ->
                assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.CONFLICT)
            );

        assertThat(resource.transition("validated", transition).path("status").asText()).isEqualTo("PUBLIER");
    }

    @Test
    void partialUpdateCannotBypassTheWorkflow() {
        InMemoryResourceStore store = new InMemoryResourceStore();
        addReferenceHierarchy(store);
        ObjectNode author = enablementUser("author-1", false);
        ContentResourceV1 resource = resource(store, author);
        store.add("contenus", content("content-1", "author-1", "BROUILLON"));
        ObjectNode patch = JsonNodeFactory.instance.objectNode();
        patch.put("title", "Titre corrigé");
        patch.put("status", "PUBLIER");
        patch.put("validatorId", "author-1");
        patch.put("validatedAt", "2026-08-25T10:00:00Z");
        patch.put("publishedAt", "2026-08-25T10:01:00Z");
        patch.put("publishedById", "author-1");
        patch.put("featured", true);
        patch.put("viewCount", 10_000);
        patch.put("helpfulCount", 10_000);

        ObjectNode updated = resource.patch("content-1", patch);

        assertThat(updated.path("title").asText()).isEqualTo("Titre corrigé");
        assertThat(updated.path("status").asText()).isEqualTo("BROUILLON");
        assertThat(updated.has("validatorId")).isFalse();
        assertThat(updated.has("validatedAt")).isFalse();
        assertThat(updated.has("publishedAt")).isFalse();
        assertThat(updated.has("publishedById")).isFalse();
        assertThat(updated.has("featured")).isFalse();
        assertThat(updated.has("viewCount")).isFalse();
        assertThat(updated.has("helpfulCount")).isFalse();
    }

    @Test
    void deleteRemovesContentAndItsUserReferences() {
        InMemoryResourceStore store = new InMemoryResourceStore();
        addReferenceHierarchy(store);
        ObjectNode author = enablementUser("author-1", false);
        ContentResourceV1 resource = resource(store, author);
        store.add("contenus", content("content-1", "author-1", "PUBLIER"));
        for (String type : List.of("favorites", "feedBacks", "contentProgress")) {
            ObjectNode reference = document(type + "-1", type);
            reference.put("contentId", "content-1");
            store.add(type, reference);
        }

        ResponseEntity<Void> response = resource.delete("content-1");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        assertThat(store.find("contenus", "content-1")).isEmpty();
        assertThat(store.list("favorites")).isEmpty();
        assertThat(store.list("feedBacks")).isEmpty();
        assertThat(store.list("contentProgress")).isEmpty();
    }

    @Test
    void deleteRejectsContentOutsideTheEnablementBusinessUnit() {
        InMemoryResourceStore store = new InMemoryResourceStore();
        addReferenceHierarchy(store);
        ObjectNode outsider = enablementUser("outsider-1", false);
        ((tools.jackson.databind.node.ArrayNode) outsider.path("accessScope").path("businessUnitIds"))
            .removeAll()
            .add("bu-2");
        ContentResourceV1 resource = resource(store, outsider);
        store.add("contenus", content("content-1", "author-1", "BROUILLON"));

        assertThatThrownBy(() -> resource.delete("content-1"))
            .isInstanceOfSatisfying(ResponseStatusException.class, exception ->
                assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND)
            );
        assertThat(store.find("contenus", "content-1")).isPresent();
    }

    private static ContentResourceV1 resource(InMemoryResourceStore store, ObjectNode user) {
        DoveCurrentUserService users = new FixedCurrentUserService(store, user);
        return new ContentResourceV1(
            new DoveApiSupport(store, users, new DoveDomainEvents(store)),
            new DoveScopeConsistencyService(store)
        );
    }

    private static ObjectNode enablementUser(String id, boolean validator) {
        ObjectNode user = document(id, id);
        user.put("role", "USER_ENABLEMENT");
        if (validator) user.putArray("additionalPermissions").add("VALIDATE_CONTENT");
        ObjectNode scope = user.putObject("accessScope");
        scope.put("global", false);
        scope.putArray("businessUnitIds").add("bu-1");
        scope.putArray("businessJobIds");
        scope.putArray("applicationIds");
        scope.putArray("moduleIds");
        return user;
    }

    private static void addReferenceHierarchy(InMemoryResourceStore store) {
        ObjectNode unit = document("bu-1", "Direction Grand Public");
        ObjectNode job = document("job-1", "Commercial");
        job.put("businessUnitId", "bu-1");
        ObjectNode application = document("app-1", "Parcours Client");
        application.putArray("businessUnitIds").add("bu-1");
        application.putArray("businessJobIds").add("job-1");
        ObjectNode module = document("module-1", "Commande");
        module.put("applicationId", "app-1");
        module.putArray("businessUnitIds").add("bu-1");
        module.putArray("businessJobIds").add("job-1");
        store.add("businessUnits", unit);
        store.add("metiers", job);
        store.add("applications", application);
        store.add("modules", module);
    }

    private static ObjectNode content(String id, String ownerId, String status) {
        ObjectNode content = document(id, id);
        content.put("title", id);
        content.put("status", status);
        content.put("applicationId", "app-1");
        content.put("moduleId", "module-1");
        content.put("businessJobId", "job-1");
        content.putArray("businessJobIds").add("job-1");
        content.putArray("businessUnitIds").add("bu-1");
        content.put("ownerId", ownerId);
        content.put("authorId", ownerId);
        content.put("version", 1);
        return content;
    }

    private static ObjectNode document(String id, String name) {
        ObjectNode value = JsonNodeFactory.instance.objectNode();
        value.put("id", id);
        value.put("name", name);
        value.put("status", "ACTIVE");
        return value;
    }

    private static final class FixedCurrentUserService extends DoveCurrentUserService {

        private final ObjectNode user;

        private FixedCurrentUserService(DoveResourceStore store, ObjectNode user) {
            super(store, new DoveProperties());
            this.user = user;
        }

        @Override
        public ObjectNode requireUser() {
            return user;
        }
    }

    private static final class InMemoryResourceStore extends DoveResourceStore {

        private final Map<String, List<ObjectNode>> values = new HashMap<>();

        private InMemoryResourceStore() {
            super(null, null);
        }

        void add(String type, ObjectNode value) {
            values.computeIfAbsent(type, ignored -> new ArrayList<>()).add(value);
        }

        @Override
        public List<ObjectNode> list(String type) {
            return List.copyOf(values.getOrDefault(type, List.of()));
        }

        @Override
        public Optional<ObjectNode> find(String type, String id) {
            return list(type).stream().filter(value -> id.equals(value.path("id").asText())).findFirst();
        }

        @Override
        public ObjectNode create(String type, JsonNode value) {
            ObjectNode document = ((ObjectNode) value).deepCopy();
            add(type, document);
            return document;
        }

        @Override
        public Optional<ObjectNode> patch(String type, String id, JsonNode patch) {
            return find(type, id).map(existing -> {
                patch.properties().forEach(field -> {
                    if (field.getValue().isNull()) existing.remove(field.getKey());
                    else existing.set(field.getKey(), field.getValue());
                });
                return existing.deepCopy();
            });
        }

        @Override
        public boolean delete(String type, String id) {
            return values.getOrDefault(type, new ArrayList<>()).removeIf(value -> id.equals(value.path("id").asText()));
        }
    }
}
