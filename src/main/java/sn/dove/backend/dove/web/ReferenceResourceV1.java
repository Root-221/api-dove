package sn.dove.backend.dove.web;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.ObjectNode;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
public class ReferenceResourceV1 {

    private final DoveApiSupport api;

    public ReferenceResourceV1(DoveApiSupport api) {
        this.api = api;
    }

    @GetMapping("/applications")
    public List<ObjectNode> applications() {
        ObjectNode user = api.currentUser();
        return api
            .store
            .list("applications")
            .stream()
            .filter(application -> api.users.canSeeApplication(user, application.path("id").asText()))
            .filter(application -> !"ARCHIVED".equals(application.path("status").asText()))
            .toList();
    }

    @GetMapping("/business-units")
    public List<ObjectNode> businessUnits() {
        ObjectNode user = api.currentUser();
        JsonNode visibleIds = user.path("accessScope").path("businessUnitIds");
        boolean global = api.users.isGlobal(user);
        return api
            .store
            .list("businessUnits")
            .stream()
            .filter(unit -> global || contains(visibleIds, unit.path("id").asText()))
            .filter(unit -> !"ARCHIVED".equals(unit.path("status").asText()))
            .toList();
    }

    @GetMapping("/business-jobs")
    public List<ObjectNode> businessJobs(@RequestParam(defaultValue = "false") boolean availableForBrowsing) {
        ObjectNode user = api.currentUser();
        return api
            .store
            .list("metiers")
            .stream()
            .filter(job -> api.users.canSeeBusinessJob(user, job.path("id").asText(), availableForBrowsing))
            .filter(job -> !"ARCHIVED".equals(job.path("status").asText()))
            .toList();
    }

    @GetMapping("/modules")
    public List<ObjectNode> modules(
        @RequestParam(required = false) String applicationId,
        @RequestParam(required = false) String businessJobId,
        @RequestParam(defaultValue = "false") boolean availableForBrowsing,
        @RequestParam(required = false) String q
    ) {
        ObjectNode user = api.currentUser();
        boolean browsing =
            availableForBrowsing || (businessJobId != null && !contains(user.path("accessScope").path("businessJobIds"), businessJobId));
        String query = DoveApiSupport.normalize(q);
        return api
            .store
            .list("modules")
            .stream()
            .filter(module -> api.users.canSeeModule(user, module, browsing))
            .filter(module -> !"ARCHIVED".equals(module.path("status").asText()))
            .filter(module -> DoveApiSupport.equalsText(module, "applicationId", applicationId))
            .filter(module -> businessJobId == null || businessJobId.isBlank() || contains(module.path("businessJobIds"), businessJobId))
            .filter(module -> query.isBlank() || DoveApiSupport.normalize(module.path("name").asText()).contains(query))
            .toList();
    }

    @GetMapping("/applications/{id}")
    public ObjectNode application(@PathVariable String id) {
        ObjectNode user = api.currentUser();
        if (!api.users.canSeeApplication(user, id)) {
            throw api.notFound("application");
        }
        return api.require("applications", id);
    }

    @GetMapping("/business-jobs/{id}")
    public ObjectNode businessJob(@PathVariable String id) {
        ObjectNode user = api.currentUser();
        if (!api.users.canSeeBusinessJob(user, id, true)) {
            throw api.notFound("business job");
        }
        return api.require("metiers", id);
    }

    @GetMapping("/modules/{id}")
    public ObjectNode module(@PathVariable String id) {
        ObjectNode user = api.currentUser();
        ObjectNode module = api.require("modules", id);
        if (!api.users.canSeeModule(user, module, true)) {
            throw api.notFound("module");
        }
        return module;
    }

    private static boolean contains(JsonNode values, String expected) {
        if (!values.isArray()) {
            return false;
        }
        for (JsonNode value : values) {
            if (expected.equals(value.asText())) {
                return true;
            }
        }
        return false;
    }
}
