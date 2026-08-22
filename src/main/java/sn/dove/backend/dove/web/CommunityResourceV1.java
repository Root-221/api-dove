package sn.dove.backend.dove.web;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.ObjectNode;
import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/v1/community/posts")
public class CommunityResourceV1 {

    private static final Set<String> REPORT_REASONS = Set.of("SPAM", "OFFENSIVE", "INAPPROPRIATE", "MISINFORMATION", "OTHER");

    private final DoveApiSupport api;

    public CommunityResourceV1(DoveApiSupport api) {
        this.api = api;
    }

    @GetMapping
    @PreAuthorize("@doveAuthorization.has('CONTRIBUTE')")
    public List<ObjectNode> list() {
        return api.store.list("discussions");
    }

    @GetMapping("/{id}")
    @PreAuthorize("@doveAuthorization.has('CONTRIBUTE')")
    public ObjectNode get(@PathVariable String id) {
        return api.require("discussions", id);
    }

    @PostMapping
    @PreAuthorize("@doveAuthorization.has('CONTRIBUTE')")
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
        ObjectNode created = api.store.create("discussions", post);
        notifyNandites(
            user.path("id").asText(),
            "Nouvelle discussion",
            user.path("firstName").asText() + " a publié « " + created.path("title").asText() + " ».",
            "/app/nandite/community"
        );
        api.events.audit(user.path("id").asText(), "CREATE_COMMUNITY_POST", created.path("id").asText(), "SUCCESS");
        return created;
    }

    @PutMapping("/{id}/like")
    @PreAuthorize("@doveAuthorization.has('CONTRIBUTE')")
    public ObjectNode like(@PathVariable String id) {
        return setMembership(id, "likedBy", true);
    }

    @DeleteMapping("/{id}/like")
    @PreAuthorize("@doveAuthorization.has('CONTRIBUTE')")
    public ObjectNode unlike(@PathVariable String id) {
        return setMembership(id, "likedBy", false);
    }

    @PutMapping("/{id}/saved")
    @PreAuthorize("@doveAuthorization.has('CONTRIBUTE')")
    public ObjectNode save(@PathVariable String id) {
        return setMembership(id, "savedBy", true);
    }

    @DeleteMapping("/{id}/saved")
    @PreAuthorize("@doveAuthorization.has('CONTRIBUTE')")
    public ObjectNode unsave(@PathVariable String id) {
        return setMembership(id, "savedBy", false);
    }

    @PostMapping("/{id}/comments")
    @PreAuthorize("@doveAuthorization.has('CONTRIBUTE')")
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
        Set<String> recipients = new LinkedHashSet<>();
        recipients.add(post.path("authorId").asText());
        post.path("comments").forEach(value -> recipients.add(value.path("authorId").asText()));
        recipients.remove(user.path("id").asText());
        recipients.remove("");
        recipients.forEach(recipientId ->
            api.events.notification(
                recipientId,
                "COMMUNITY",
                "Nouvelle réponse",
                user.path("firstName").asText() + " a répondu à « " + post.path("title").asText() + " ».",
                "/app/nandite/community"
            )
        );
        api.events.audit(user.path("id").asText(), "COMMENT_COMMUNITY_POST", comment.path("id").asText(), "SUCCESS");
        return comment;
    }

    @PostMapping("/{id}/reports")
    @PreAuthorize("@doveAuthorization.has('CONTRIBUTE')")
    public ObjectNode reportPost(@PathVariable String id, @RequestBody JsonNode input) {
        ObjectNode post = api.require("discussions", id);
        return report(post, null, input);
    }

    @PostMapping("/{id}/comments/{commentId}/reports")
    @PreAuthorize("@doveAuthorization.has('CONTRIBUTE')")
    public ObjectNode reportComment(@PathVariable String id, @PathVariable String commentId, @RequestBody JsonNode input) {
        ObjectNode post = api.require("discussions", id);
        ObjectNode comment = null;
        for (JsonNode value : post.path("comments")) {
            if (commentId.equals(value.path("id").asText()) && value.isObject()) {
                comment = (ObjectNode) value;
                break;
            }
        }
        if (comment == null) {
            throw api.notFound("comment");
        }
        return report(post, comment, input);
    }

    private ObjectNode setMembership(String id, String field, boolean present) {
        ObjectNode post = api.require("discussions", id);
        String userId = api.currentUserId();
        ArrayNode values = post.withArray(field);
        for (int index = values.size() - 1; index >= 0; index--) {
            if (userId.equals(values.get(index).asText())) values.remove(index);
        }
        if (present) values.add(userId);
        ObjectNode updated = api.store.replace("discussions", id, post).orElseThrow(() -> api.notFound("post"));
        if (present && "likedBy".equals(field) && !userId.equals(post.path("authorId").asText())) {
            ObjectNode user = api.currentUser();
            api.events.notification(
                post.path("authorId").asText(),
                "COMMUNITY",
                "Votre publication a été appréciée",
                user.path("firstName").asText() + " aime « " + post.path("title").asText() + " ».",
                "/app/nandite/community"
            );
        }
        return updated;
    }

    private ObjectNode report(ObjectNode post, ObjectNode comment, JsonNode input) {
        ObjectNode reporter = api.currentUser();
        String reporterId = reporter.path("id").asText();
        String targetType = comment == null ? "DISCUSSION" : "COMMENT";
        String targetId = comment == null ? post.path("id").asText() : comment.path("id").asText();
        String targetAuthorId = comment == null ? post.path("authorId").asText() : comment.path("authorId").asText();
        if (reporterId.equals(targetAuthorId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "A user cannot report their own message");
        }
        String reason = api.requireText(input, "reason");
        if (!REPORT_REASONS.contains(reason)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid community report reason");
        }
        String reportComment = input.path("comment").asText("").trim();
        if (reportComment.length() > 500) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "comment is limited to 500 characters");
        }
        boolean alreadyReported = api
            .store
            .list("communityReports")
            .stream()
            .anyMatch(reportValue ->
                reporterId.equals(reportValue.path("reporterId").asText()) &&
                targetId.equals(reportValue.path("targetId").asText()) &&
                !"RESOLVED".equals(reportValue.path("status").asText())
            );
        if (alreadyReported) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "This message has already been reported by the user");
        }

        ObjectNode value = tools.jackson.databind.node.JsonNodeFactory.instance.objectNode();
        value.put("id", UUID.randomUUID().toString());
        value.put("postId", post.path("id").asText());
        value.put("postTitle", post.path("title").asText());
        value.put("targetType", targetType);
        value.put("targetId", targetId);
        value.put("targetAuthorId", targetAuthorId);
        value.put("targetAuthorName", comment == null ? post.path("authorName").asText() : comment.path("authorName").asText());
        value.put("targetMessage", comment == null ? post.path("message").asText() : comment.path("message").asText());
        value.put("reporterId", reporterId);
        value.put("reporterName", reporter.path("firstName").asText() + " " + reporter.path("lastName").asText());
        value.put("reason", reason);
        if (!reportComment.isBlank()) value.put("comment", reportComment);
        value.put("status", "NEW");
        value.put("createdAt", Instant.now().toString());
        ObjectNode created = api.store.create("communityReports", value);

        api
            .store
            .list("utilisateurs")
            .stream()
            .filter(user -> "ACTIVE".equals(user.path("status").asText()))
            .filter(user -> "USER_ENABLEMENT".equals(user.path("role").asText()))
            .forEach(user ->
                api.events.notification(
                    user.path("id").asText(),
                    "COMMUNITY",
                    "Message de communauté signalé",
                    "Un " + (comment == null ? "message" : "commentaire") + " de « " + post.path("title").asText() + " » doit être examiné.",
                    "/manage/reports"
                )
            );
        api.events.audit(reporterId, "REPORT_COMMUNITY_" + targetType, created.path("id").asText(), "SUCCESS");
        return created;
    }

    private void notifyNandites(String actorId, String title, String message, String route) {
        api
            .store
            .list("utilisateurs")
            .stream()
            .filter(user -> "ACTIVE".equals(user.path("status").asText()))
            .filter(user -> "NANDITE".equals(user.path("role").asText()))
            .filter(user -> !actorId.equals(user.path("id").asText()))
            .forEach(user -> api.events.notification(user.path("id").asText(), "COMMUNITY", title, message, route));
    }
}
