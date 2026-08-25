package sn.dove.backend.dove.web;

import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.ObjectNode;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import sn.dove.backend.dove.security.DovePermissions;

@RestController
@RequestMapping("/api/v1/admin")
@PreAuthorize("@doveAuthorization.has('MANAGE_RBAC')")
public class AdminRbacResourceV1 {

    private final DoveApiSupport api;

    public AdminRbacResourceV1(DoveApiSupport api) {
        this.api = api;
    }

    @GetMapping("/permissions")
    public List<Map<String, String>> permissions() {
        return DovePermissions
            .BY_ROLE
            .values()
            .stream()
            .flatMap(List::stream)
            .distinct()
            .sorted()
            .map(code -> Map.of("code", code, "label", code.replace('_', ' ')))
            .toList();
    }

    @GetMapping("/roles")
    public List<ObjectNode> roles() {
        Map<String, ObjectNode> storedRoles = api
            .store
            .list("roles")
            .stream()
            .collect(
                Collectors.toMap(role -> role.path("code").asText(role.path("role").asText()), Function.identity(), (first, ignored) -> first)
            );
        return DovePermissions
            .BY_ROLE
            .entrySet()
            .stream()
            .sorted(Map.Entry.comparingByKey())
            .map(entry -> {
                String code = entry.getKey();
                ObjectNode result = storedRoles
                    .getOrDefault(code, tools.jackson.databind.node.JsonNodeFactory.instance.objectNode())
                    .deepCopy();
                if (result.path("id").asText().isBlank()) {
                    result.put("id", UUID.nameUUIDFromBytes(("dove-role:" + code).getBytes(StandardCharsets.UTF_8)).toString());
                }
                result.put("code", code);
                result.put("role", code);
                ArrayNode permissions = result.putArray("permissionCodes");
                entry.getValue().forEach(permissions::add);
                result.put("system", true);
                return result;
            })
            .toList();
    }
}
