package sn.dove.backend.dove.web;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.ObjectNode;
import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/v1")
public class FeedbackResourceV1 {

    private static final Set<String> REPORT_REASONS = Set.of("INCORRECT", "OUTDATED", "INCOMPLETE", "OTHER");

    private final DoveApiSupport api;

    public FeedbackResourceV1(DoveApiSupport api) {
        this.api = api;
    }

    @GetMapping("/feedback")
    @PreAuthorize("@doveAuthorization.has('MANAGE_FEEDBACK')")
    public List<ObjectNode> feedback() {
        ObjectNode user = api.currentUser();
        return api
            .store
            .list("feedBacks")
            .stream()
            .filter(item -> api.store.find("contenus", item.path("contentId").asText()).map(content -> api.users.isInMutationScope(user, content)).orElse(false))
            .toList();
    }

    @GetMapping("/me/feedback")
    @PreAuthorize("@doveAuthorization.has('SUBMIT_FEEDBACK')")
    public List<ObjectNode> myFeedback() {
        String userId = api.currentUserId();
        return api.store.list("feedBacks").stream().filter(item -> userId.equals(item.path("userId").asText())).toList();
    }

    @PostMapping("/feedback")
    @PreAuthorize("@doveAuthorization.has('SUBMIT_FEEDBACK')")
    public ObjectNode submit(@RequestBody JsonNode input) {
        ObjectNode user = api.currentUser();
        String contentId = api.requireText(input, "contentId");
        ObjectNode content = api.require("contenus", contentId);
        if (!api.users.canReadContent(user, content, true)) {
            throw api.notFound("content");
        }
        String kind = input.path("kind").asText(input.path("comment").asText().isBlank() ? "SATISFACTION" : "COMMENT");
        boolean report = "OBSOLESCENCE".equals(kind);
        if (report) {
            if (!"NANDITE".equals(user.path("role").asText())) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only a Nandite can report content");
            }
            String reason = api.requireText(input, "reason");
            if (!REPORT_REASONS.contains(reason)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid report reason");
            }
        }
        if (input.path("comment").asText("").length() > 500) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Comment must not exceed 500 characters");
        }
        ObjectNode feedback = api.newDocument(input);
        String now = Instant.now().toString();
        feedback.put("id", UUID.randomUUID().toString());
        feedback.put("userId", user.path("id").asText());
        feedback.put("kind", kind);
        if (report) {
            feedback.put("helpful", false);
        }
        feedback.put("status", "NEW");
        feedback.put("createdAt", now);
        if (!content.path("ownerId").asText().isBlank()) {
            feedback.put("assigneeId", content.path("ownerId").asText());
        }
        ObjectNode created = api.store.create("feedBacks", feedback);
        if (report) {
            ObjectNode contentPatch = tools.jackson.databind.node.JsonNodeFactory.instance.objectNode();
            contentPatch.put("status", "A_REVISER");
            contentPatch.put("updatedAt", now);
            api.store.patch("contenus", contentId, contentPatch);
            api.events.notification(content.path("ownerId").asText(), "FEEDBACK", "Contenu signalé", "Le contenu « " + content.path("title").asText() + " » doit être revu.", "/manage/reports");
        }
        api.events.audit(user.path("id").asText(), report ? "REPORT_CONTENT" : "SUBMIT_FEEDBACK", created.path("id").asText(), "SUCCESS");
        return created;
    }

    @PostMapping("/feedback/{id}/start-processing")
    @PreAuthorize("@doveAuthorization.has('MANAGE_FEEDBACK')")
    public ObjectNode startProcessing(@PathVariable String id) {
        ObjectNode feedback = scopedFeedback(id);
        if (!"NEW".equals(feedback.path("status").asText())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Feedback is not new");
        }
        return changeStatus(feedback, "IN_PROGRESS", false);
    }

    @PostMapping("/feedback/{id}/resolve")
    @PreAuthorize("@doveAuthorization.has('MANAGE_FEEDBACK')")
    public ObjectNode resolve(@PathVariable String id) {
        ObjectNode feedback = scopedFeedback(id);
        if ("RESOLVED".equals(feedback.path("status").asText())) {
            return feedback;
        }
        return changeStatus(feedback, "RESOLVED", true);
    }

    private ObjectNode scopedFeedback(String id) {
        ObjectNode user = api.currentUser();
        ObjectNode feedback = api.require("feedBacks", id);
        boolean inScope = api
            .store
            .find("contenus", feedback.path("contentId").asText())
            .map(content -> api.users.isInMutationScope(user, content))
            .orElse(false);
        if (!inScope) {
            throw api.notFound("feedback");
        }
        return feedback;
    }

    private ObjectNode changeStatus(ObjectNode feedback, String status, boolean resolved) {
        String actorId = api.currentUserId();
        ObjectNode patch = tools.jackson.databind.node.JsonNodeFactory.instance.objectNode();
        patch.put("status", status);
        patch.put("assigneeId", actorId);
        if (resolved) patch.put("resolvedAt", Instant.now().toString());
        ObjectNode updated = api.store.patch("feedBacks", feedback.path("id").asText(), patch).orElseThrow(() -> api.notFound("feedback"));
        api.events.notification(
            feedback.path("userId").asText(),
            "FEEDBACK",
            resolved ? "Signalement traité" : "Signalement pris en charge",
            resolved ? "La correction de votre signalement est terminée." : "Votre signalement est en cours de traitement.",
            "/app/nandite/reports"
        );
        api.events.audit(actorId, resolved ? "RESOLVE_FEEDBACK" : "START_FEEDBACK_PROCESSING", feedback.path("id").asText(), "SUCCESS");
        return updated;
    }
}
