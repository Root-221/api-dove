package sn.dove.backend.dove.web;

import tools.jackson.databind.node.ObjectNode;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/me/favorites")
@PreAuthorize("@doveAuthorization.has('READ_CONTENT')")
public class FavoriteResourceV1 {

    private final DoveApiSupport api;

    public FavoriteResourceV1(DoveApiSupport api) {
        this.api = api;
    }

    @GetMapping
    public List<ObjectNode> list() {
        ObjectNode user = api.currentUser();
        String userId = user.path("id").asText();
        return api
            .store
            .list("favorites")
            .stream()
            .filter(favorite -> userId.equals(favorite.path("userId").asText()))
            .map(favorite -> api.store.find("contenus", favorite.path("contentId").asText()).orElse(null))
            .filter(content -> content != null && api.users.canReadContent(user, content, true))
            .filter(content -> List.of("PUBLIER", "A_REVISER").contains(content.path("status").asText()))
            .toList();
    }

    @PutMapping("/{contentId}")
    public ObjectNode add(@PathVariable String contentId) {
        ObjectNode user = api.currentUser();
        ObjectNode content = api.require("contenus", contentId);
        if (!api.users.canReadContent(user, content, true) || !List.of("PUBLIER", "A_REVISER").contains(content.path("status").asText())) {
            throw api.notFound("content");
        }
        String userId = user.path("id").asText();
        boolean exists = api
            .store
            .list("favorites")
            .stream()
            .anyMatch(favorite -> userId.equals(favorite.path("userId").asText()) && contentId.equals(favorite.path("contentId").asText()));
        if (!exists) {
            ObjectNode favorite = tools.jackson.databind.node.JsonNodeFactory.instance.objectNode();
            favorite.put("id", UUID.randomUUID().toString());
            favorite.put("userId", userId);
            favorite.put("contentId", contentId);
            favorite.put("createdAt", Instant.now().toString());
            api.store.create("favorites", favorite);
        }
        return content;
    }

    @DeleteMapping("/{contentId}")
    public ResponseEntity<Void> remove(@PathVariable String contentId) {
        String userId = api.currentUserId();
        api
            .store
            .list("favorites")
            .stream()
            .filter(favorite -> userId.equals(favorite.path("userId").asText()) && contentId.equals(favorite.path("contentId").asText()))
            .forEach(favorite -> api.store.delete("favorites", favorite.path("id").asText()));
        return ResponseEntity.noContent().build();
    }
}
