package sn.dove.backend.dove.web;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.ObjectNode;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/community/posts")
@PreAuthorize("@doveAuthorization.has('CONTRIBUTE')")
public class CommunityResourceV1 {

    private final DoveApiSupport api;

    public CommunityResourceV1(DoveApiSupport api) {
        this.api = api;
    }

    @GetMapping
    public List<ObjectNode> list() {
        return api.store.list("discussions");
    }

    @GetMapping("/{id}")
    public ObjectNode get(@PathVariable String id) {
        return api.require("discussions", id);
    }

    @PostMapping
    public ObjectNode create(@RequestBody JsonNode input) {
        ObjectNode user = api.currentUser();
        ObjectNode post = api.newDocument(input);
        api.requireText(input, "title");
        api.requireText(input, "message");
        post.put("id", UUID.randomUUID().toString());
        post.put("authorId", user.path("id").asText());
        post.put("authorName", user.path("firstName").asText() + " " + user.path("lastName").asText());
        post.put("createdAt", Instant.now().toString());
        post.putArray("likedBy");
        post.putArray("savedBy");
        post.putArray("comments");
        return api.store.create("discussions", post);
    }

    @PutMapping("/{id}/like")
    public ObjectNode like(@PathVariable String id) {
        return setMembership(id, "likedBy", true);
    }

    @DeleteMapping("/{id}/like")
    public ObjectNode unlike(@PathVariable String id) {
        return setMembership(id, "likedBy", false);
    }

    @PutMapping("/{id}/saved")
    public ObjectNode save(@PathVariable String id) {
        return setMembership(id, "savedBy", true);
    }

    @DeleteMapping("/{id}/saved")
    public ObjectNode unsave(@PathVariable String id) {
        return setMembership(id, "savedBy", false);
    }

    @PostMapping("/{id}/comments")
    public ObjectNode comment(@PathVariable String id, @RequestBody JsonNode input) {
        String message = api.requireText(input, "message");
        ObjectNode user = api.currentUser();
        ObjectNode post = api.require("discussions", id);
        ObjectNode comment = tools.jackson.databind.node.JsonNodeFactory.instance.objectNode();
        comment.put("id", UUID.randomUUID().toString());
        comment.put("authorId", user.path("id").asText());
        comment.put("authorName", user.path("firstName").asText() + " " + user.path("lastName").asText());
        comment.put("message", message);
        comment.put("createdAt", Instant.now().toString());
        ObjectNode updated = post.deepCopy();
        updated.withArray("comments").add(comment);
        api.store.replace("discussions", id, updated);
        return comment;
    }

    private ObjectNode setMembership(String id, String field, boolean present) {
        ObjectNode post = api.require("discussions", id);
        String userId = api.currentUserId();
        ArrayNode values = post.withArray(field);
        for (int index = values.size() - 1; index >= 0; index--) {
            if (userId.equals(values.get(index).asText())) values.remove(index);
        }
        if (present) values.add(userId);
        return api.store.replace("discussions", id, post).orElseThrow(() -> api.notFound("post"));
    }
}
