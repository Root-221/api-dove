package sn.dove.backend.dove.security;

import java.util.List;
import java.util.Map;

public final class DovePermissions {

    public static final Map<String, List<String>> BY_ROLE = Map.of(
        "BUSINESS_USER",
        List.of("READ_CONTENT", "SUBMIT_FEEDBACK"),
        "NANDITE",
        List.of("READ_CONTENT", "SUBMIT_FEEDBACK", "CONTRIBUTE"),
        "USER_ENABLEMENT",
        List.of(
            "READ_CONTENT",
            "READ_CROSS_BUSINESS_JOB",
            "SUBMIT_FEEDBACK",
            "REVIEW_PROPOSALS",
            "MANAGE_FEEDBACK",
            "CREATE_CONTENT",
            "EDIT_CONTENT",
            "SUBMIT_CONTENT_FOR_VALIDATION",
            "VALIDATE_CONTENT",
            "PUBLISH_CONTENT",
            "ARCHIVE_CONTENT",
            "VIEW_ANALYTICS"
        ),
        "ADMIN",
        List.of(
            "READ_CONTENT",
            "SUBMIT_FEEDBACK",
            "VIEW_ANALYTICS",
            "MANAGE_USERS",
            "MANAGE_RBAC",
            "MANAGE_REFERENCE_DATA",
            "MANAGE_AUTHENTICATION",
            "MANAGE_OPERATIONS",
            "VIEW_AUDIT"
        )
    );

    private DovePermissions() {}
}
