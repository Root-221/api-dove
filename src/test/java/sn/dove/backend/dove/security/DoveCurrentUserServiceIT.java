package sn.dove.backend.dove.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import tools.jackson.databind.node.JsonNodeFactory;
import tools.jackson.databind.node.ObjectNode;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.server.ResponseStatusException;
import sn.dove.backend.IntegrationTest;
import sn.dove.backend.dove.service.DoveResourceStore;

/**
 * Exercises DoveCurrentUserService.requireUser() end-to-end (real MySQL, real
 * DoveResourceStore) to verify the indexed externalSubject lookup added to fix the
 * full-table-scan-on-every-request problem still resolves the caller correctly, including the
 * automatic provisioning and disabled-account cases. dove.auth.dev-header-enabled is false by
 * default in the test profile, so requireUser() takes the JWT-subject branch exercised here.
 */
@IntegrationTest
class DoveCurrentUserServiceIT {

    private static final String USERS = "utilisateurs";

    @Autowired
    private DoveCurrentUserService service;

    @Autowired
    private DoveResourceStore store;

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void requireUser_resolvesActiveUserByJwtSubject() {
        String subject = "sub-" + UUID.randomUUID();
        ObjectNode user = JsonNodeFactory.instance.objectNode();
        user.put("externalSubject", subject);
        user.put("status", "ACTIVE");
        user.put("role", "BUSINESS_USER");
        ObjectNode created = store.create(USERS, user);

        SecurityContextHolder.getContext().setAuthentication(jwtAuthentication(subject));

        ObjectNode resolved = service.requireUser();

        assertThat(resolved.path("id").asText()).isEqualTo(created.path("id").asText());
        assertThat(resolved.path("permissions").isArray()).isTrue();
    }

    @Test
    void requireUser_autoProvisionsUnknownSubjectAsBusinessUser() {
        String subject = "sub-" + UUID.randomUUID();
        SecurityContextHolder.getContext().setAuthentication(jwtAuthentication(subject));

        ObjectNode resolved = service.requireUser();

        assertThat(resolved.path("id").asText()).isEqualTo(subject);
        assertThat(resolved.path("externalSubject").asText()).isEqualTo(subject);
        assertThat(resolved.path("role").asText()).isEqualTo("BUSINESS_USER");
        assertThat(resolved.path("status").asText()).isEqualTo("ACTIVE");
        assertThat(resolved.path("accessScope").path("businessJobIds")).isEmpty();
        assertThat(store.findByExternalSubject(USERS, subject)).isPresent();
    }

    @Test
    void requireUser_rejectsInactiveAccount() {
        String subject = "sub-" + UUID.randomUUID();
        ObjectNode user = JsonNodeFactory.instance.objectNode();
        user.put("externalSubject", subject);
        user.put("status", "DISABLED");
        user.put("role", "BUSINESS_USER");
        store.create(USERS, user);

        SecurityContextHolder.getContext().setAuthentication(jwtAuthentication(subject));

        assertThatThrownBy(() -> service.requireUser())
            .isInstanceOfSatisfying(ResponseStatusException.class, ex -> assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN))
            .hasMessageContaining("disabled");
    }

    private static JwtAuthenticationToken jwtAuthentication(String subject) {
        Jwt jwt = Jwt.withTokenValue("test-token")
            .header("alg", "none")
            .claim("sub", subject)
            .claim("preferred_username", "new.user")
            .claim("email", "new.user@sonatel.sn")
            .claim("given_name", "New")
            .claim("family_name", "User")
            .issuedAt(Instant.now())
            .expiresAt(Instant.now().plusSeconds(60))
            .build();
        return new JwtAuthenticationToken(jwt, List.of(new SimpleGrantedAuthority("ROLE_USER")));
    }
}
