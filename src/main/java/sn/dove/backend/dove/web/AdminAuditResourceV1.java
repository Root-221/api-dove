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

    private final DoveApiSupport api;

    public AdminAuditResourceV1(DoveApiSupport api) {
        this.api = api;
    }

    @GetMapping
    public List<ObjectNode> list() {
        return api.store.list("auditEvents");
    }

    @GetMapping("/{id}")
    public ObjectNode get(@PathVariable String id) {
        return api.require("auditEvents", id);
    }
}
