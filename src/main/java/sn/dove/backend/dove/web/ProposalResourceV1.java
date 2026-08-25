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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import sn.dove.backend.dove.service.DoveScopeConsistencyService;

@RestController
@RequestMapping("/api/v1")
public class ProposalResourceV1 {

    private final DoveApiSupport api;
    private final DoveScopeConsistencyService consistency;

    public ProposalResourceV1(DoveApiSupport api, DoveScopeConsistencyService consistency) {
        this.api = api;
        this.consistency = consistency;
    }

    @GetMapping("/me/proposals")
    @PreAuthorize("@doveAuthorization.has('CONTRIBUTE')")
    public List<ObjectNode> mine() {
        String userId = api.currentUserId();
        return api.store.list("proposals").stream().filter(proposal -> userId.equals(proposal.path("authorId").asText())).toList();
    }

    @GetMapping("/proposals/review-queue")
    @PreAuthorize("@doveAuthorization.has('REVIEW_PROPOSALS')")
    public List<ObjectNode> reviewQueue() {
        ObjectNode user = api.currentUser();
        return api
            .store
            .list("proposals")
            .stream()
            .filter(proposal -> api.users.isInMutationScope(user, proposal))
            .filter(proposal -> List.of("SUBMITTED", "UNDER_REVIEW").contains(proposal.path("status").asText()))
            .toList();
    }

    @GetMapping("/proposals/{id}")
    public ObjectNode proposal(@PathVariable String id) {
        ObjectNode user = api.currentUser();
        ObjectNode proposal = api.require("proposals", id);
        boolean own = user.path("id").asText().equals(proposal.path("authorId").asText());
        boolean reviewer = api.users.permissions(user).contains("REVIEW_PROPOSALS") && api.users.isInMutationScope(user, proposal);
        if (!own && !reviewer) {
            throw api.notFound("proposal");
        }
        return proposal;
    }

    @PostMapping("/proposals")
    @PreAuthorize("@doveAuthorization.has('CONTRIBUTE')")
    public ObjectNode create(@RequestBody JsonNode input) {
        ObjectNode user = api.currentUser();
        ObjectNode proposal = api.newDocument(input);
        for (String field : List.of("title", "situation", "treatment", "applicationId", "businessJobId", "moduleId")) {
            api.requireText(input, field);
        }
        consistency.requireValidResourceTuple(proposal);
        if (!api.users.isInMutationScope(user, proposal)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Proposal is outside the user's scope");
        }
        proposal.put("id", UUID.randomUUID().toString());
        proposal.put("authorId", user.path("id").asText());
        proposal.put("status", "SUBMITTED");
        proposal.put("createdAt", Instant.now().toString());
        ObjectNode created = api.store.create("proposals", proposal);
        api.events.audit(user.path("id").asText(), "CREATE_PROPOSAL", created.path("id").asText(), "SUCCESS");
        notifyReviewers(user, created);
        return created;
    }

    @PostMapping("/proposals/{id}/start-review")
    @PreAuthorize("@doveAuthorization.has('REVIEW_PROPOSALS')")
    public ObjectNode startReview(@PathVariable String id) {
        ObjectNode proposal = scopedForReview(id);
        if (!"SUBMITTED".equals(proposal.path("status").asText())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Proposal is not submitted");
        }
        return patchDecision(proposal, "UNDER_REVIEW", null, null);
    }

    @PostMapping("/proposals/{id}/accept")
    @PreAuthorize("@doveAuthorization.has('REVIEW_PROPOSALS')")
    @Transactional
    public ObjectNode accept(@PathVariable String id, @RequestBody(required = false) JsonNode input) {
        ObjectNode proposal = scopedForReview(id);
        if ("CONVERTED".equals(proposal.path("status").asText())) {
            return proposal;
        }
        if (!"UNDER_REVIEW".equals(proposal.path("status").asText())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Proposal must be under review");
        }
        ObjectNode reviewer = api.currentUser();
        consistency.requireValidResourceTuple(proposal);
        String now = Instant.now().toString();
        String contentId = UUID.randomUUID().toString();
        ObjectNode content = tools.jackson.databind.node.JsonNodeFactory.instance.objectNode();
        content.put("id", contentId);
        content.put("title", proposal.path("title").asText());
        content.put("description", proposal.path("situation").asText());
        content.put("type", "FICHEPRATIQUE");
        content.putArray("formats").add("FICHEPRATIQUE");
        content.put("typeContenu", "FICHEPRATIQUE");
        content.put("status", "BROUILLON");
        for (String field : List.of("applicationId", "moduleId")) content.set(field, proposal.path(field));
        consistency.normalizeContentScope(reviewer, content);
        content.put("ownerId", reviewer.path("id").asText());
        content.put("authorId", proposal.path("authorId").asText());
        if (input != null && !input.path("validatorId").asText().isBlank()) content.put("validatorId", input.path("validatorId").asText());
        content.put("version", 1);
        content.put("updatedAt", now);
        content.put("viewCount", 0);
        content.put("helpfulCount", 0);
        content.put("featured", false);
        content.putArray("tags").add("nandite").add("cas-pratique");
        content.putArray("summary").add(proposal.path("situation").asText()).add(proposal.path("treatment").asText());
        content.put("objective", "Répondre au cas terrain : " + proposal.path("title").asText());
        content.putArray("warnings").add("Faire valider la procédure avant publication.");
        content
            .putArray("steps")
            .addObject()
            .put("order", 1)
            .put("title", "Situation")
            .put("description", proposal.path("situation").asText());
        content
            .withArray("steps")
            .addObject()
            .put("order", 2)
            .put("title", "Traitement proposé")
            .put("description", proposal.path("treatment").asText());
        content.putArray("faqItems");
        content.putArray("videoItems");
        api.store.create("contenus", content);

        ObjectNode updated = patchDecision(proposal, "CONVERTED", contentId, null);
        api.events.notification(proposal.path("authorId").asText(), "PROPOSAL", "Proposition convertie", "« " + proposal.path("title").asText() + " » a été transformée en brouillon éditorial.", "/app/nandite/contents");
        api.events.audit(reviewer.path("id").asText(), "CONVERT_PROPOSAL_TO_DRAFT", id, "SUCCESS");
        return updated;
    }

    @PostMapping("/proposals/{id}/reject")
    @PreAuthorize("@doveAuthorization.has('REVIEW_PROPOSALS')")
    public ObjectNode reject(@PathVariable String id, @RequestBody JsonNode input) {
        ObjectNode proposal = scopedForReview(id);
        if (!"UNDER_REVIEW".equals(proposal.path("status").asText())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Proposal must be under review");
        }
        String reason = api.requireText(input, "reason");
        ObjectNode updated = patchDecision(proposal, "REJECTED", null, reason);
        api.events.notification(proposal.path("authorId").asText(), "PROPOSAL", "Proposition à reprendre", reason, "/app/nandite/contents");
        return updated;
    }

    private ObjectNode scopedForReview(String id) {
        ObjectNode user = api.currentUser();
        ObjectNode proposal = api.require("proposals", id);
        if (!api.users.isInMutationScope(user, proposal)) {
            throw api.notFound("proposal");
        }
        return proposal;
    }

    private ObjectNode patchDecision(ObjectNode proposal, String status, String contentId, String reason) {
        String reviewerId = api.currentUserId();
        ObjectNode patch = tools.jackson.databind.node.JsonNodeFactory.instance.objectNode();
        patch.put("status", status);
        patch.put("reviewerId", reviewerId);
        if (contentId != null) patch.put("resultingContentId", contentId);
        if (reason != null) patch.put("rejectionReason", reason);
        if (List.of("CONVERTED", "REJECTED").contains(status)) patch.put("decidedAt", Instant.now().toString());
        ObjectNode updated = api.store.patch("proposals", proposal.path("id").asText(), patch).orElseThrow(() -> api.notFound("proposal"));
        api.events.audit(reviewerId, "PROPOSAL_" + status, proposal.path("id").asText(), "SUCCESS");
        return updated;
    }

    private void notifyReviewers(ObjectNode author, ObjectNode proposal) {
        api
            .store
            .list("utilisateurs")
            .stream()
            .filter(user -> "USER_ENABLEMENT".equals(user.path("role").asText()))
            .filter(user -> api.users.isInMutationScope(user, proposal))
            .forEach(user -> api.events.notification(user.path("id").asText(), "PROPOSAL", "Nouvelle proposition Nandité", "« " + proposal.path("title").asText() + " » attend votre décision.", "/manage/proposals"));
    }
}
