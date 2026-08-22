package sn.dove.backend.dove.web;

import java.time.Instant;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.JsonNodeFactory;
import tools.jackson.databind.node.ObjectNode;

@RestController
@RequestMapping("/api/v1/me")
@PreAuthorize("@doveAuthorization.has('READ_CONTENT')")
public class ProgressResourceV1 {

    private final DoveApiSupport api;

    public ProgressResourceV1(DoveApiSupport api) {
        this.api = api;
    }

    @GetMapping("/progress")
    public List<ObjectNode> progress() {
        String userId = api.currentUserId();
        return api.store.list("contentProgress").stream().filter(item -> userId.equals(item.path("userId").asText())).toList();
    }

    @GetMapping("/contents/{contentId}/progress")
    public ObjectNode progress(@PathVariable String contentId) {
        ObjectNode user = api.currentUser();
        requireReadable(user, contentId);
        return api.store.find("contentProgress", progressId(user.path("id").asText(), contentId)).orElseGet(() -> empty(user, contentId));
    }

    @PutMapping("/contents/{contentId}/progress")
    public ObjectNode update(@PathVariable String contentId, @RequestBody JsonNode input) {
        ObjectNode user = api.currentUser();
        requireReadable(user, contentId);
        int requested = input.has("progressPercent") ? input.path("progressPercent").asInt() : input.path("progress").asInt();
        int progress = Math.max(0, Math.min(100, requested));
        String now = Instant.now().toString();
        String userId = user.path("id").asText();
        String id = progressId(userId, contentId);
        ObjectNode current = api.store.find("contentProgress", id).orElseGet(() -> empty(user, contentId));
        current.put("progress", Math.max(progress, current.path("progress").asInt(0)));
        current.put("progressPercent", current.path("progress").asInt());
        current.put("lastPositionSeconds", Math.max(0, input.path("lastPositionSeconds").asInt(0)));
        current.put("lastUsedAt", now);
        current.put("completed", current.path("progress").asInt() == 100);
        if (current.path("progress").asInt() == 100 && current.path("completedAt").asText().isBlank()) {
            current.put("completedAt", now);
        }
        return api.store.upsert("contentProgress", current);
    }

    private void requireReadable(ObjectNode user, String contentId) {
        ObjectNode content = api.require("contenus", contentId);
        boolean cross = api.users.permissions(user).contains("READ_CROSS_BUSINESS_JOB");
        boolean own = user.path("id").asText().equals(content.path("authorId").asText());
        if (!own && !api.users.canReadContent(user, content, cross)) {
            throw api.notFound("content");
        }
    }

    private static ObjectNode empty(ObjectNode user, String contentId) {
        ObjectNode value = JsonNodeFactory.instance.objectNode();
        value.put("id", progressId(user.path("id").asText(), contentId));
        value.put("userId", user.path("id").asText());
        value.put("contentId", contentId);
        value.put("progress", 0);
        value.put("progressPercent", 0);
        value.put("lastPositionSeconds", 0);
        value.put("completed", false);
        return value;
    }

    private static String progressId(String userId, String contentId) {
        return userId + ":" + contentId;
    }
}
