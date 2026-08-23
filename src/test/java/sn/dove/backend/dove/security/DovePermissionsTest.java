package sn.dove.backend.dove.security;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

/**
 * DovePermissions.BY_ROLE drives every @PreAuthorize / DoveCurrentUserService.requirePermission
 * check in the app; a wrong entry here silently over- or under-grants access. Pins down the
 * roles most likely to cause damage if mis-scoped: BUSINESS_USER must stay read/feedback-only,
 * and only ADMIN gets audit-log visibility and RBAC/user management.
 */
class DovePermissionsTest {

    @Test
    void businessUserCannotEditOrPublishContent() {
        assertThat(DovePermissions.BY_ROLE.get("BUSINESS_USER"))
            .contains("READ_CONTENT", "SUBMIT_FEEDBACK")
            .doesNotContain("EDIT_CONTENT", "CREATE_CONTENT", "PUBLISH_CONTENT", "VALIDATE_CONTENT", "MANAGE_USERS", "VIEW_AUDIT");
    }

    @Test
    void onlyAdminHasAuditAndRbacPermissions() {
        for (var entry : DovePermissions.BY_ROLE.entrySet()) {
            if ("ADMIN".equals(entry.getKey())) {
                assertThat(entry.getValue()).contains("VIEW_AUDIT", "MANAGE_RBAC", "MANAGE_USERS");
            } else {
                assertThat(entry.getValue()).doesNotContain("VIEW_AUDIT", "MANAGE_RBAC", "MANAGE_USERS");
            }
        }
    }

    @Test
    void everyKnownRoleCanAtLeastReadContent() {
        assertThat(DovePermissions.BY_ROLE.values()).allSatisfy(permissions -> assertThat(permissions).contains("READ_CONTENT"));
    }
}
