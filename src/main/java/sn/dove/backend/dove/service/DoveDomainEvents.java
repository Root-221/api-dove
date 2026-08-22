package sn.dove.backend.dove.service;

import tools.jackson.databind.node.ObjectNode;
import java.time.Instant;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class DoveDomainEvents {

    private final DoveResourceStore store;

    public DoveDomainEvents(DoveResourceStore store) {
        this.store = store;
    }

    public ObjectNode audit(String actorId, String action, String resource, String result) {
        ObjectNode event = tools.jackson.databind.node.JsonNodeFactory.instance.objectNode();
        event.put("id", UUID.randomUUID().toString());
        event.put("userId", actorId);
        event.put("action", action);
        event.put("resource", resource);
        event.put("result", result);
        event.put("createdAt", Instant.now().toString());
        return store.create("auditEvents", event);
    }

    public ObjectNode notification(String userId, String kind, String title, String message, String route) {
        ObjectNode notification = tools.jackson.databind.node.JsonNodeFactory.instance.objectNode();
        notification.put("id", UUID.randomUUID().toString());
        notification.put("userId", userId);
        notification.put("kind", kind);
        notification.put("title", title);
        notification.put("message", message);
        if (route != null && !route.isBlank()) {
            notification.put("route", route);
        }
        notification.put("read", false);
        notification.put("createdAt", Instant.now().toString());
        return store.create("notifications", notification);
    }
}
