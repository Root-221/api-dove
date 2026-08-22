package sn.dove.backend.dove.web;

import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.ObjectNode;
import java.util.List;
import java.util.Map;
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
        return api
            .store
            .list("roles")
            .stream()
            .map(role -> {
                ObjectNode result = role.deepCopy();
                String code = role.path("code").asText(role.path("role").asText());
                ArrayNode permissions = result.putArray("permissionCodes");
                DovePermissions.BY_ROLE.getOrDefault(code, List.of()).forEach(permissions::add);
                result.put("system", true);
                return result;
            })
            .toList();
    }
}
