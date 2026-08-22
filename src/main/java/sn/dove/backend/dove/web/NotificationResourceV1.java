package sn.dove.backend.dove.web;

import tools.jackson.databind.node.ObjectNode;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/me/notifications")
public class NotificationResourceV1 {

    private final DoveApiSupport api;

    public NotificationResourceV1(DoveApiSupport api) {
        this.api = api;
    }

    @GetMapping
    public List<ObjectNode> list(@RequestParam(defaultValue = "false") boolean unreadOnly) {
        String userId = api.currentUserId();
        return api
            .store
            .list("notifications")
            .stream()
            .filter(notification -> userId.equals(notification.path("userId").asText()))
            .filter(notification -> !unreadOnly || !notification.path("read").asBoolean(false))
            .toList();
    }

    @PatchMapping("/{id}/read")
    public ObjectNode markRead(@PathVariable String id) {
        ObjectNode notification = api.require("notifications", id);
        if (!api.currentUserId().equals(notification.path("userId").asText())) {
            throw api.notFound("notification");
        }
        ObjectNode patch = tools.jackson.databind.node.JsonNodeFactory.instance.objectNode();
        patch.put("read", true);
        return api.store.patch("notifications", id, patch).orElseThrow(() -> api.notFound("notification"));
    }

    @PostMapping("/mark-all-read")
    public ResponseEntity<Void> markAllRead() {
        String userId = api.currentUserId();
        ObjectNode patch = tools.jackson.databind.node.JsonNodeFactory.instance.objectNode();
        patch.put("read", true);
        api
            .store
            .list("notifications")
            .stream()
            .filter(notification -> userId.equals(notification.path("userId").asText()))
            .filter(notification -> !notification.path("read").asBoolean(false))
            .forEach(notification -> api.store.patch("notifications", notification.path("id").asText(), patch));
        return ResponseEntity.noContent().build();
    }
}
