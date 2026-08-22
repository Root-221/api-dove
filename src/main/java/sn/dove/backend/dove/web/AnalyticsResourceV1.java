package sn.dove.backend.dove.web;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.ObjectNode;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
public class AnalyticsResourceV1 {

    private final DoveApiSupport api;

    public AnalyticsResourceV1(DoveApiSupport api) {
        this.api = api;
    }

    @GetMapping("/analytics/summary")
    @PreAuthorize("@doveAuthorization.has('VIEW_ANALYTICS')")
    public List<ObjectNode> summary() {
        ObjectNode user = api.currentUser();
        return api
            .store
            .list("analyticsMetrics")
            .stream()
            .filter(metric -> visibleMetric(user, metric))
            .toList();
    }

    @GetMapping("/analytics/popular-searches")
    @PreAuthorize("@doveAuthorization.has('VIEW_ANALYTICS')")
    public List<ObjectNode> popularSearches() {
        ObjectNode user = api.currentUser();
        return api
            .store
            .list("popularSearches")
            .stream()
            .filter(search -> api.users.isInMutationScope(user, search))
            .toList();
    }

    @PostMapping("/events/searches")
    @PreAuthorize("@doveAuthorization.has('READ_CONTENT')")
    public void searchEvent(@RequestBody JsonNode input) {
        recordEvent("SEARCH", input);
    }

    @PostMapping("/events/content-views")
    @PreAuthorize("@doveAuthorization.has('READ_CONTENT')")
    public void contentView(@RequestBody JsonNode input) {
        recordEvent("CONTENT_VIEW", input);
    }

    private boolean visibleMetric(ObjectNode user, ObjectNode metric) {
        if (api.users.isGlobal(user)) {
            return true;
        }
        if (metric.path("businessJobId").asText().isBlank()) {
            return false;
        }
        ObjectNode scoped = metric.deepCopy();
        if (scoped.path("applicationId").asText().isBlank()) {
            scoped.put("applicationId", user.path("accessScope").path("applicationIds").path(0).asText());
        }
        if (scoped.path("moduleId").asText().isBlank()) {
            scoped.put("moduleId", user.path("accessScope").path("moduleIds").path(0).asText());
        }
        return api.users.isInMutationScope(user, scoped);
    }

    private void recordEvent(String kind, JsonNode input) {
        ObjectNode event = api.newDocument(input);
        event.put("id", UUID.randomUUID().toString());
        event.put("kind", kind);
        event.put("userId", api.currentUserId());
        event.put("createdAt", Instant.now().toString());
        api.store.create("usageEvents", event);
    }
}
