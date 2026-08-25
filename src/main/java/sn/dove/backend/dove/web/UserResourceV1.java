package sn.dove.backend.dove.web;

import tools.jackson.databind.node.ObjectNode;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/users")
@PreAuthorize("@doveAuthorization.has('READ_CONTENT')")
public class UserResourceV1 {

    private final DoveApiSupport api;

    public UserResourceV1(DoveApiSupport api) {
        this.api = api;
    }

    @GetMapping
    public List<ObjectNode> list(@RequestParam(defaultValue = "false") boolean availableForBrowsing) {
        ObjectNode current = api.currentUser();
        return api
            .store
            .list("utilisateurs")
            .stream()
            .filter(user -> visible(current, user, availableForBrowsing))
            .map(UserResourceV1::publicProfile)
            .toList();
    }

    @GetMapping("/{id}")
    public ObjectNode get(@PathVariable String id) {
        ObjectNode current = api.currentUser();
        ObjectNode user = api.require("utilisateurs", id);
        if (!visible(current, user, true)) {
            throw api.notFound("user");
        }
        return publicProfile(user);
    }

    private boolean visible(ObjectNode current, ObjectNode candidate, boolean browse) {
        if (api.users.isGlobal(current) || current.path("id").asText().equals(candidate.path("id").asText())) {
            return true;
        }
        return api.users.sharesOrganizationalScope(current, candidate, browse);
    }

    private static ObjectNode publicProfile(ObjectNode source) {
        ObjectNode result = source.deepCopy();
        result.remove(List.of("externalSubject", "additionalPermissions"));
        return result;
    }

}
