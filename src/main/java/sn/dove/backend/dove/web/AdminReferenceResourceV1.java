package sn.dove.backend.dove.web;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.ObjectNode;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/v1/admin/reference")
@PreAuthorize("@doveAuthorization.has('MANAGE_REFERENCE_DATA')")
public class AdminReferenceResourceV1 {

    private static final Map<String, String> TYPES = Map.of(
        "applications",
        "applications",
        "business-units",
        "businessUnits",
        "business-jobs",
        "metiers",
        "modules",
        "modules"
    );

    private final DoveApiSupport api;

    public AdminReferenceResourceV1(DoveApiSupport api) {
        this.api = api;
    }

    @GetMapping("/{type}")
    public List<ObjectNode> list(@PathVariable String type) {
        return api.store.list(storeType(type));
    }

    @GetMapping("/{type}/{id}")
    public ObjectNode get(@PathVariable String type, @PathVariable String id) {
        return api.require(storeType(type), id);
    }

    @PostMapping("/{type}")
    public ObjectNode create(@PathVariable String type, @RequestBody JsonNode input) {
        api.requireText(input, "name");
        ObjectNode value = api.newDocument(input);
        if (value.path("id").asText().isBlank()) value.put("id", UUID.randomUUID().toString());
        value.put("updatedAt", Instant.now().toString());
        ObjectNode created = api.store.create(storeType(type), value);
        api.events.audit(api.currentUserId(), "CREATE_REFERENCE_" + type.toUpperCase(), created.path("id").asText(), "SUCCESS");
        return created;
    }

    @PutMapping("/{type}/{id}")
    public ObjectNode update(@PathVariable String type, @PathVariable String id, @RequestBody JsonNode input) {
        api.requireText(input, "name");
        ObjectNode value = api.newDocument(input);
        value.put("id", id);
        value.put("updatedAt", Instant.now().toString());
        String resourceType = storeType(type);
        ObjectNode updated = api.store.replace(resourceType, id, value).orElseGet(() -> api.store.create(resourceType, value));
        api.events.audit(api.currentUserId(), "UPDATE_REFERENCE_" + type.toUpperCase(), id, "SUCCESS");
        return updated;
    }

    @PostMapping("/{type}/{id}/archive")
    public ObjectNode archive(@PathVariable String type, @PathVariable String id) {
        ObjectNode patch = tools.jackson.databind.node.JsonNodeFactory.instance.objectNode();
        patch.put("status", "ARCHIVED");
        patch.put("archivedAt", Instant.now().toString());
        ObjectNode updated = api.store.patch(storeType(type), id, patch).orElseThrow(() -> api.notFound(type));
        api.events.audit(api.currentUserId(), "ARCHIVE_REFERENCE_" + type.toUpperCase(), id, "SUCCESS");
        return updated;
    }

    private static String storeType(String type) {
        String value = TYPES.get(type);
        if (value == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
        return value;
    }
}
