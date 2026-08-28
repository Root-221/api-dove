package sn.dove.backend.dove.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.JsonNodeFactory;
import tools.jackson.databind.node.ObjectNode;

class DoveScopeConsistencyServiceTest {

    private final InMemoryResourceStore store = new InMemoryResourceStore();
    private final DoveScopeConsistencyService consistency = new DoveScopeConsistencyService(store);

    @Test
    void normalizeBusinessUserKeepsOnlyTheStableJobAndDerivedBusinessUnit() {
        ObjectNode unit = document("bu-1", "Direction Grand Public");
        ObjectNode job = document("job-1", "Conseiller clientèle");
        job.put("businessUnitId", "bu-1");
        store.add("metiers", job);
        store.add("businessUnits", unit);

        ObjectNode user = document("user-1", "Awa Ndiaye");
        user.put("role", "BUSINESS_USER");
        ObjectNode requestedScope = user.putObject("accessScope");
        requestedScope.putArray("businessUnitIds").add("wrong-bu");
        requestedScope.putArray("businessJobIds").add("job-1");
        requestedScope.putArray("applicationIds").add("wrong-app");
        requestedScope.putArray("moduleIds").add("wrong-module");

        consistency.normalizeUser(user);

        assertThat(user.path("accessScope").path("businessUnitIds")).extracting(node -> node.asText()).containsExactly("bu-1");
        assertThat(user.path("accessScope").path("businessJobIds")).extracting(node -> node.asText()).containsExactly("job-1");
        assertThat(user.path("accessScope").path("applicationIds")).isEmpty();
        assertThat(user.path("accessScope").path("moduleIds")).isEmpty();
    }

    @Test
    void normalizeEnablementKeepsOnlyItsBusinessUnit() {
        ObjectNode unit = document("bu-1", "Direction Entreprises");
        store.add("businessUnits", unit);

        ObjectNode user = document("user-1", "Awa Ndiaye");
        user.put("role", "USER_ENABLEMENT");
        ObjectNode scope = user.putObject("accessScope");
        scope.putArray("businessUnitIds").add("bu-1");
        scope.putArray("businessJobIds").add("legacy-job");
        scope.putArray("applicationIds").add("legacy-app");
        scope.putArray("moduleIds").add("legacy-module");

        consistency.normalizeUser(user);

        assertThat(user.path("accessScope").path("businessUnitIds")).extracting(node -> node.asText()).containsExactly("bu-1");
        assertThat(user.path("accessScope").path("businessJobIds")).isEmpty();
        assertThat(user.path("accessScope").path("applicationIds")).isEmpty();
        assertThat(user.path("accessScope").path("moduleIds")).isEmpty();
    }

    @Test
    void normalizeUserDoesNotRequireAnyExistingApplicationOrModule() {
        ObjectNode unit = document("bu-1", "Direction Entreprises");
        ObjectNode job = document("job-1", "Chargé d'affaires");
        job.put("businessUnitId", "bu-1");
        store.add("businessUnits", unit);
        store.add("metiers", job);

        ObjectNode user = document("user-1", "Awa Ndiaye");
        user.put("role", "NANDITE");
        ObjectNode scope = user.putObject("accessScope");
        scope.putArray("businessJobIds").add("job-1");

        assertThatCode(() -> consistency.normalizeUser(user)).doesNotThrowAnyException();
        assertThat(user.path("accessScope").path("applicationIds")).isEmpty();
        assertThat(user.path("accessScope").path("moduleIds")).isEmpty();
    }

    @Test
    void normalizeAdminAlwaysProducesAGlobalScope() {
        ObjectNode user = document("admin-1", "Administrateur");
        user.put("role", "ADMIN");
        ObjectNode scope = user.putObject("accessScope");
        scope.put("global", false);
        scope.putArray("businessJobIds").add("job-1");
        scope.putArray("moduleIds").add("module-1");

        consistency.normalizeUser(user);

        assertThat(user.path("accessScope").path("global").asBoolean()).isTrue();
        assertThat(user.path("accessScope").path("businessJobIds")).isEmpty();
        assertThat(user.path("accessScope").path("moduleIds")).isEmpty();
    }

    @Test
    void normalizeModuleInheritsBusinessUnitsAndDerivedJobsFromApplication() {
        ObjectNode unit = document("bu-1", "Direction Grand Public");
        ObjectNode application = document("app-1", "Parcours Client");
        application.putArray("businessUnitIds").add("bu-1");
        application.putArray("businessJobIds").add("job-1");
        ObjectNode expectedJob = document("job-1", "Conseiller clientèle");
        expectedJob.put("businessUnitId", "bu-1");
        ObjectNode otherJob = document("job-2", "Gestionnaire de stock");
        otherJob.put("businessUnitId", "bu-2");
        store.add("businessUnits", unit);
        store.add("applications", application);
        store.add("metiers", expectedJob);
        store.add("metiers", otherJob);

        ObjectNode module = document("module-1", "Inventaire");
        module.put("applicationId", "app-1");
        module.putArray("businessJobIds").add("job-2");

        consistency.normalizeReference("modules", "module-1", module);

        assertThat(module.path("businessUnitIds")).extracting(node -> node.asText()).containsExactly("bu-1");
        assertThat(module.path("businessJobIds")).extracting(node -> node.asText()).containsExactly("job-1");
    }

    @Test
    void normalizeContentScopeInheritsEveryApplicationJobFromTheEnablementBusinessUnit() {
        ObjectNode firstUnit = document("bu-1", "Direction Grand Public");
        ObjectNode secondUnit = document("bu-2", "Direction Entreprises");
        ObjectNode firstJob = document("job-1", "Conseiller clientèle");
        firstJob.put("businessUnitId", "bu-1");
        ObjectNode secondJob = document("job-2", "Responsable boutique");
        secondJob.put("businessUnitId", "bu-1");
        ObjectNode otherUnitJob = document("job-3", "Chargé d'affaires");
        otherUnitJob.put("businessUnitId", "bu-2");
        ObjectNode application = document("app-1", "Parcours Client");
        application.putArray("businessUnitIds").add("bu-1").add("bu-2");
        application.putArray("businessJobIds").add("job-1").add("job-2").add("job-3");
        ObjectNode module = document("module-1", "Souscription");
        module.put("applicationId", "app-1");
        module.putArray("businessUnitIds").add("bu-1").add("bu-2");
        module.putArray("businessJobIds").add("job-1").add("job-2").add("job-3");
        store.add("businessUnits", firstUnit);
        store.add("businessUnits", secondUnit);
        store.add("metiers", firstJob);
        store.add("metiers", secondJob);
        store.add("metiers", otherUnitJob);
        store.add("applications", application);
        store.add("modules", module);

        ObjectNode enablement = document("user-1", "Awa Ndiaye");
        enablement.put("role", "USER_ENABLEMENT");
        ObjectNode scope = enablement.putObject("accessScope");
        scope.put("global", false);
        scope.putArray("businessUnitIds").add("bu-1");
        scope.putArray("businessJobIds").add("job-1");
        ObjectNode content = document("content-1", "Souscrire une offre");
        content.put("applicationId", "app-1");
        content.put("moduleId", "module-1");
        content.put("businessJobId", "job-3");

        consistency.normalizeContentScope(enablement, content);

        assertThat(content.path("businessJobIds")).extracting(node -> node.asText()).containsExactly("job-1", "job-2");
        assertThat(content.path("businessUnitIds")).extracting(node -> node.asText()).containsExactly("bu-1");
        assertThat(content.path("businessJobId").asText()).isEqualTo("job-1");
    }

    @Test
    void normalizeApplicationDerivesEveryActiveJobFromSelectedBusinessUnits() {
        ObjectNode firstUnit = document("bu-1", "Direction Grand Public");
        ObjectNode secondUnit = document("bu-2", "Direction Entreprises");
        ObjectNode firstJob = document("job-1", "Conseiller clientèle");
        firstJob.put("businessUnitId", "bu-1");
        ObjectNode secondJob = document("job-2", "Responsable boutique");
        secondJob.put("businessUnitId", "bu-1");
        ObjectNode otherJob = document("job-3", "Chargé d'affaires");
        otherJob.put("businessUnitId", "bu-2");
        store.add("businessUnits", firstUnit);
        store.add("businessUnits", secondUnit);
        store.add("metiers", firstJob);
        store.add("metiers", secondJob);
        store.add("metiers", otherJob);

        ObjectNode application = document("app-1", "Parcours Client");
        application.put("code", "PARCOURS");
        application.putArray("businessUnitIds").add("bu-1");

        consistency.normalizeReference("applications", "app-1", application);

        assertThat(application.path("businessUnitIds")).extracting(node -> node.asText()).containsExactly("bu-1");
        assertThat(application.path("businessJobIds")).extracting(node -> node.asText()).containsExactly("job-1", "job-2");
    }

    @Test
    void normalizeApplicationAcceptsABusinessUnitBeforeItsFirstJobIsCreated() {
        store.add("businessUnits", document("bu-1", "Nouvelle direction"));
        ObjectNode application = document("app-1", "Nouvelle application");
        application.put("code", "NEWAPP");
        application.putArray("businessUnitIds").add("bu-1");

        consistency.normalizeReference("applications", "app-1", application);

        assertThat(application.path("businessUnitIds")).extracting(node -> node.asText()).containsExactly("bu-1");
        assertThat(application.path("businessJobIds")).isEmpty();
    }

    @Test
    void aNewJobIsProjectedToExistingApplicationsAndModulesOfItsBusinessUnit() {
        ObjectNode unit = document("bu-1", "Direction Grand Public");
        ObjectNode firstJob = document("job-1", "Conseiller clientèle");
        firstJob.put("businessUnitId", "bu-1");
        ObjectNode application = document("app-1", "Parcours Client");
        application.putArray("businessUnitIds").add("bu-1");
        application.putArray("businessJobIds").add("job-1");
        ObjectNode module = document("module-1", "Commande");
        module.put("applicationId", "app-1");
        module.putArray("businessUnitIds").add("bu-1");
        module.putArray("businessJobIds").add("job-1");
        store.add("businessUnits", unit);
        store.add("metiers", firstJob);
        store.add("applications", application);
        store.add("modules", module);

        ObjectNode newJob = document("job-2", "Responsable boutique");
        newJob.put("businessUnitId", "bu-1");
        store.add("metiers", newJob);
        consistency.synchronizeBusinessUnitApplications("bu-1");

        assertThat(application.path("businessJobIds")).extracting(node -> node.asText()).containsExactly("job-1", "job-2");
        assertThat(module.path("businessJobIds")).extracting(node -> node.asText()).containsExactly("job-1", "job-2");
    }

    @Test
    void archiveBusinessUnitIsRejectedWhileItContainsAnActiveJob() {
        ObjectNode job = document("job-1", "Conseiller clientèle");
        job.put("businessUnitId", "bu-1");
        store.add("metiers", job);

        assertThatThrownBy(() -> consistency.requireArchivable("businessUnits", "bu-1"))
            .isInstanceOfSatisfying(ResponseStatusException.class, error ->
                assertThat(error.getStatusCode()).isEqualTo(HttpStatus.CONFLICT)
            )
            .hasMessageContaining("métiers actifs");
    }

    private ObjectNode document(String id, String name) {
        ObjectNode value = JsonNodeFactory.instance.objectNode();
        value.put("id", id);
        value.put("name", name);
        value.put("status", "ACTIVE");
        return value;
    }

    private static final class InMemoryResourceStore extends DoveResourceStore {

        private final Map<String, List<ObjectNode>> values = new HashMap<>();

        private InMemoryResourceStore() {
            super(null, null);
        }

        void add(String type, ObjectNode value) {
            values.computeIfAbsent(type, ignored -> new java.util.ArrayList<>()).add(value);
        }

        @Override
        public List<ObjectNode> list(String type) {
            return values.getOrDefault(type, List.of());
        }

        @Override
        public Optional<ObjectNode> find(String type, String id) {
            return list(type).stream().filter(value -> id.equals(value.path("id").asText())).findFirst();
        }

        @Override
        public Optional<ObjectNode> patch(String type, String id, JsonNode patch) {
            return find(type, id).map(existing -> {
                patch.properties().forEach(field -> existing.set(field.getKey(), field.getValue()));
                return existing;
            });
        }
    }
}
