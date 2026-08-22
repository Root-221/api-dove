package sn.dove.backend.dove.web;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.ObjectNode;
import java.text.Normalizer;
import java.time.Instant;
import java.util.Locale;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import sn.dove.backend.dove.security.DoveCurrentUserService;
import sn.dove.backend.dove.service.DoveDomainEvents;
import sn.dove.backend.dove.service.DoveResourceStore;

@Component
public class DoveApiSupport {

    public final DoveResourceStore store;
    public final DoveCurrentUserService users;
    public final DoveDomainEvents events;

    public DoveApiSupport(DoveResourceStore store, DoveCurrentUserService users, DoveDomainEvents events) {
        this.store = store;
        this.users = users;
        this.events = events;
    }

    public ObjectNode require(String type, String id) {
        return store.find(type, id).orElseThrow(() -> notFound(type));
    }

    public ResponseStatusException notFound(String type) {
        return new ResponseStatusException(HttpStatus.NOT_FOUND, type + " not found");
    }

    public ObjectNode currentUser() {
        return users.requireUser();
    }

    public String currentUserId() {
        return currentUser().path("id").asText();
    }

    public ObjectNode newDocument(JsonNode input) {
        if (!input.isObject()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "A JSON object is required");
        }
        return ((ObjectNode) input).deepCopy();
    }

    public String requireText(JsonNode input, String field) {
        String value = input.path(field).asText().trim();
        if (value.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, field + " is required");
        }
        return value;
    }

    public void putCreationMetadata(ObjectNode value) {
        String now = Instant.now().toString();
        value.put("id", UUID.randomUUID().toString());
        value.put("createdAt", now);
        value.put("updatedAt", now);
    }

    public static String normalize(String value) {
        return Normalizer
            .normalize(value == null ? "" : value, Normalizer.Form.NFD)
            .replaceAll("\\p{M}", "")
            .toLowerCase(Locale.ROOT)
            .replaceAll("[^a-z0-9]+", " ")
            .trim();
    }

    public static boolean equalsText(JsonNode node, String field, String expected) {
        return expected == null || expected.isBlank() || expected.equals(node.path(field).asText());
    }
}
