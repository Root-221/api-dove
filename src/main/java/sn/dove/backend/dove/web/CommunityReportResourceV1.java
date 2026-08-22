package sn.dove.backend.dove.web;

import tools.jackson.databind.node.ObjectNode;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/v1")
public class CommunityReportResourceV1 {

    private final DoveApiSupport api;

    public CommunityReportResourceV1(DoveApiSupport api) {
        this.api = api;
    }

    @GetMapping("/me/community-reports")
    @PreAuthorize("@doveAuthorization.has('CONTRIBUTE')")
    public List<ObjectNode> mine() {
        String userId = api.currentUserId();
        return api
            .store
            .list("communityReports")
            .stream()
            .filter(report -> userId.equals(report.path("reporterId").asText()))
            .sorted(Comparator.comparing(report -> report.path("createdAt").asText(), Comparator.reverseOrder()))
            .toList();
    }

    @GetMapping("/community/reports")
    @PreAuthorize("@doveAuthorization.has('MANAGE_FEEDBACK')")
    public List<ObjectNode> list() {
        return api
            .store
            .list("communityReports")
            .stream()
            .sorted(Comparator.comparing(report -> report.path("createdAt").asText(), Comparator.reverseOrder()))
            .toList();
    }

    @PostMapping("/community/reports/{id}/start-processing")
    @PreAuthorize("@doveAuthorization.has('MANAGE_FEEDBACK')")
    public ObjectNode startProcessing(@PathVariable String id) {
        ObjectNode report = api.require("communityReports", id);
        if (!"NEW".equals(report.path("status").asText())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Community report is not new");
        }
        return changeStatus(report, "IN_PROGRESS");
    }

    @PostMapping("/community/reports/{id}/resolve")
    @PreAuthorize("@doveAuthorization.has('MANAGE_FEEDBACK')")
    public ObjectNode resolve(@PathVariable String id) {
        ObjectNode report = api.require("communityReports", id);
        if ("RESOLVED".equals(report.path("status").asText())) {
            return report;
        }
        return changeStatus(report, "RESOLVED");
    }

    private ObjectNode changeStatus(ObjectNode report, String status) {
        String actorId = api.currentUserId();
        ObjectNode patch = tools.jackson.databind.node.JsonNodeFactory.instance.objectNode();
        patch.put("status", status);
        patch.put("assigneeId", actorId);
        patch.put("updatedAt", Instant.now().toString());
        if ("RESOLVED".equals(status)) patch.put("resolvedAt", Instant.now().toString());
        ObjectNode updated = api
            .store
            .patch("communityReports", report.path("id").asText(), patch)
            .orElseThrow(() -> api.notFound("community report"));
        api.events.notification(
            report.path("reporterId").asText(),
            "COMMUNITY",
            "Signalement de communauté mis à jour",
            "Votre signalement est maintenant " + ("RESOLVED".equals(status) ? "traité" : "en cours d’examen") + ".",
            "/app/nandite/reports"
        );
        api.events.audit(actorId, "COMMUNITY_REPORT_" + status, report.path("id").asText(), "SUCCESS");
        return updated;
    }
}
