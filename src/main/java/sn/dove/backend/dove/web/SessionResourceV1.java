package sn.dove.backend.dove.web;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.JsonNodeFactory;
import tools.jackson.databind.node.ObjectNode;
import java.util.List;
import java.util.Set;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import sn.dove.backend.dove.config.DoveProperties;

@RestController
@RequestMapping("/api/v1")
public class SessionResourceV1 {

    private final DoveApiSupport api;
    private final DoveProperties properties;

    public SessionResourceV1(DoveApiSupport api, DoveProperties properties) {
        this.api = api;
        this.properties = properties;
    }

    @GetMapping("/me")
    public ObjectNode me() {
        return api.currentUser();
    }

    @PatchMapping("/me/preferences")
    public ObjectNode updatePreferences(@RequestBody JsonNode input) {
        ObjectNode patch = JsonNodeFactory.instance.objectNode();
        ObjectNode preferences = patch.putObject("preferences");
        Set<String> allowed = Set.of("language", "theme", "compactMode");
        input.properties().stream().filter(entry -> allowed.contains(entry.getKey())).forEach(entry ->
            preferences.set(entry.getKey(), entry.getValue())
        );
        String userId = api.currentUserId();
        ObjectNode updated = api.store.patch("utilisateurs", userId, patch).orElseThrow(() -> api.notFound("user"));
        api.events.audit(userId, "UPDATE_PREFERENCES", userId, "SUCCESS");
        return updated;
    }

    @GetMapping("/dev/users")
    public List<ObjectNode> developmentUsers() {
        if (!properties.getAuth().isDevHeaderEnabled()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
        return api.store.list("utilisateurs");
    }
}
