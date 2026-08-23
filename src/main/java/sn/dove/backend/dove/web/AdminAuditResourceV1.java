package sn.dove.backend.dove.web;

import tools.jackson.databind.node.ObjectNode;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/audit-events")
@PreAuthorize("@doveAuthorization.has('VIEW_AUDIT')")
public class AdminAuditResourceV1 {

    /**
     * The audit log has no upper bound in practice; cap the default view to the most recent
     * events, pushed to SQL (ORDER BY created_at DESC LIMIT) instead of loading the entire
     * table into memory on every request. See DoveResourceStore.listRecent.
     */
    private static final int MAX_AUDIT_EVENTS = 300;

    private final DoveApiSupport api;

    public AdminAuditResourceV1(DoveApiSupport api) {
        this.api = api;
    }

    @GetMapping
    public List<ObjectNode> list() {
        return api.store.listRecent("auditEvents", MAX_AUDIT_EVENTS);
    }

    @GetMapping("/{id}")
    public ObjectNode get(@PathVariable String id) {
        return api.require("auditEvents", id);
    }
}
