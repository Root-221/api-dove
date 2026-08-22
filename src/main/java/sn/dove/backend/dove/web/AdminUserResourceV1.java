package sn.dove.backend.dove.web;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.ObjectNode;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/v1/admin/users")
@PreAuthorize("@doveAuthorization.has('MANAGE_USERS')")
public class AdminUserResourceV1 {

    private final DoveApiSupport api;

    public AdminUserResourceV1(DoveApiSupport api) {
        this.api = api;
    }

    @GetMapping
    public List<ObjectNode> list() {
        return api.store.list("utilisateurs");
    }

    @GetMapping("/{id}")
    public ObjectNode get(@PathVariable String id) {
        return api.require("utilisateurs", id);
    }

    @PostMapping
    public ObjectNode create(@RequestBody JsonNode input) {
        ObjectNode user = api.newDocument(input);
        for (String field : List.of("firstName", "lastName", "email", "role")) api.requireText(input, field);
        String email = user.path("email").asText().toLowerCase();
        boolean exists = api.store.list("utilisateurs").stream().anyMatch(candidate -> email.equalsIgnoreCase(candidate.path("email").asText()));
        if (exists) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "A user with this email already exists");
        }
        if (user.path("id").asText().isBlank()) user.put("id", UUID.randomUUID().toString());
        if (user.path("login").asText().isBlank()) user.put("login", email.substring(0, email.indexOf('@')));
        user.put("status", user.path("status").asText("ACTIVE"));
        user.put("lastLoginAt", user.path("lastLoginAt").asText(Instant.now().toString()));
        if (!user.has("accessScope")) {
            ObjectNode scope = user.putObject("accessScope");
            scope.put("global", false);
            scope.putArray("applicationIds");
            scope.putArray("businessUnitIds");
            scope.putArray("businessJobIds");
            scope.putArray("moduleIds");
        }
        ObjectNode created = api.store.create("utilisateurs", user);
        api.events.audit(api.currentUserId(), "CREATE_USER", created.path("id").asText(), "SUCCESS");
        return created;
    }

    @PatchMapping("/{id}")
    public ObjectNode patch(@PathVariable String id, @RequestBody JsonNode input) {
        protectLastAdmin(id, input);
        ObjectNode updated = api.store.patch("utilisateurs", id, input).orElseThrow(() -> api.notFound("user"));
        api.events.audit(api.currentUserId(), "UPDATE_USER", id, "SUCCESS");
        return updated;
    }

    @PutMapping("/{id}/access")
    @Transactional
    public ObjectNode updateAccess(@PathVariable String id, @RequestBody JsonNode input) {
        ObjectNode user = api.require("utilisateurs", id);
        String role = input.path("role").asText(input.path("roleCodes").path(0).asText(user.path("role").asText()));
        ObjectNode patch = tools.jackson.databind.node.JsonNodeFactory.instance.objectNode();
        patch.put("role", role);
        patch.put("status", input.path("status").asText(user.path("status").asText("ACTIVE")));
        if (input.has("accessScope")) {
            patch.set("accessScope", input.path("accessScope"));
        } else {
            ObjectNode scope = patch.putObject("accessScope");
            scope.put("global", "ADMIN".equals(role));
            for (String field : List.of("applicationIds", "businessUnitIds", "businessJobIds", "moduleIds")) {
                scope.set(field, input.has(field) ? input.path(field) : user.path("accessScope").path(field));
            }
        }
        if (input.has("additionalPermissions")) patch.set("additionalPermissions", input.path("additionalPermissions"));
        if (input.has("additionalPermissionCodes")) patch.set("additionalPermissions", input.path("additionalPermissionCodes"));
        if (input.has("contentValidatorId")) patch.set("contentValidatorId", input.path("contentValidatorId"));
        protectLastAdmin(id, patch);
        ObjectNode updated = api.store.patch("utilisateurs", id, patch).orElseThrow(() -> api.notFound("user"));
        cascadeValidator(id, updated.path("contentValidatorId").asText());
        api.events.audit(api.currentUserId(), "UPDATE_USER_ACCESS", id, "SUCCESS");
        return updated;
    }

    @PostMapping("/imports")
    @Transactional
    public List<ObjectNode> importUsers(@RequestBody JsonNode input) {
        JsonNode users = input.isArray() ? input : input.path("users");
        if (!users.isArray()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "users array is required");
        }
        java.util.ArrayList<ObjectNode> imported = new java.util.ArrayList<>();
        users.forEach(user -> imported.add(api.store.upsert("utilisateurs", user)));
        api.events.audit(api.currentUserId(), "IMPORT_USERS", Integer.toString(imported.size()), "SUCCESS");
        return imported;
    }

    private void protectLastAdmin(String id, JsonNode patch) {
        ObjectNode current = api.require("utilisateurs", id);
        boolean currentlyActiveAdmin = "ADMIN".equals(current.path("role").asText()) && "ACTIVE".equals(current.path("status").asText());
        String nextRole = patch.path("role").asText(current.path("role").asText());
        String nextStatus = patch.path("status").asText(current.path("status").asText());
        if (!currentlyActiveAdmin || ("ADMIN".equals(nextRole) && "ACTIVE".equals(nextStatus))) return;
        long activeAdmins = api
            .store
            .list("utilisateurs")
            .stream()
            .filter(user -> "ADMIN".equals(user.path("role").asText()) && "ACTIVE".equals(user.path("status").asText()))
            .count();
        if (activeAdmins <= 1) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "The last active administrator cannot be disabled");
        }
    }

    private void cascadeValidator(String ownerId, String validatorId) {
        if (validatorId.isBlank()) return;
        ObjectNode patch = tools.jackson.databind.node.JsonNodeFactory.instance.objectNode();
        patch.put("validatorId", validatorId);
        api
            .store
            .list("contenus")
            .stream()
            .filter(content -> ownerId.equals(content.path("ownerId").asText()))
            .filter(content -> List.of("BROUILLON", "EN_ATTENTE_VALIDATION").contains(content.path("status").asText()))
            .forEach(content -> api.store.patch("contenus", content.path("id").asText(), patch));
    }
}
