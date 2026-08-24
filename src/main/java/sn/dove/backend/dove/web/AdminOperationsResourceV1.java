package sn.dove.backend.dove.web;

import java.io.IOException;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import sn.dove.backend.dove.config.DoveSeedLoader;
import tools.jackson.databind.node.ObjectNode;

@RestController
@RequestMapping("/api/v1/admin/operations")
@PreAuthorize("@doveAuthorization.has('MANAGE_OPERATIONS')")
public class AdminOperationsResourceV1 {

    private final DoveApiSupport api;
    private final DoveSeedLoader seedLoader;

    public AdminOperationsResourceV1(DoveApiSupport api, DoveSeedLoader seedLoader) {
        this.api = api;
        this.seedLoader = seedLoader;
    }

    @GetMapping("/health")
    public List<ObjectNode> health() {
        return api.store.list("serviceHealth");
    }

    @GetMapping("/incidents")
    public List<ObjectNode> incidents() {
        return api.store.list("incidents");
    }

    @GetMapping("/backups")
    public List<ObjectNode> backups() {
        return api.store.list("backups");
    }

    @PostMapping("/backups")
    public ObjectNode createBackup() {
        ObjectNode backup = tools.jackson.databind.node.JsonNodeFactory.instance.objectNode();
        backup.put("id", UUID.randomUUID().toString());
        backup.put("createdAt", Instant.now().toString());
        backup.put("size", "En cours de calcul");
        backup.put("status", "AVAILABLE");
        ObjectNode created = api.store.create("backups", backup);
        api.events.audit(api.currentUserId(), "TRIGGER_BACKUP", created.path("id").asText(), "SUCCESS");
        return created;
    }

    @PostMapping("/backups/{id}/restore-tests")
    public ObjectNode verifyRestore(@PathVariable String id) {
        api.require("backups", id);
        ObjectNode patch = tools.jackson.databind.node.JsonNodeFactory.instance.objectNode();
        patch.put("status", "VERIFIED");
        patch.put("lastRestoreTestAt", Instant.now().toString());
        ObjectNode updated = api.store.patch("backups", id, patch).orElseThrow(() -> api.notFound("backup"));
        api.events.audit(api.currentUserId(), "VERIFY_RESTORE", id, "SUCCESS");
        return updated;
    }

    @PostMapping("/reset-database")
    public ObjectNode resetDatabase() throws IOException {
        api.store.deleteAll();
        int count = seedLoader.loadSeeds(true);
        api.events.audit(api.currentUserId(), "RESET_DATABASE", "SUCCESS", "SUCCESS");
        ObjectNode result = tools.jackson.databind.node.JsonNodeFactory.instance.objectNode();
        result.put("success", true);
        result.put("message", "Base de données réinitialisée avec succès depuis db.json (" + count + " ressources importées).");
        return result;
    }
}
