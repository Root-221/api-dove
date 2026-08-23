package sn.dove.backend.dove.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import tools.jackson.databind.node.JsonNodeFactory;
import tools.jackson.databind.node.ObjectNode;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import sn.dove.backend.IntegrationTest;

/**
 * Covers the DoveResourceStore changes made for the "full-table-scan on every request"
 * (findByExternalSubject) and "unbounded list endpoints" (listRecent/listRecentForUser/
 * list(type, maxRows)) fixes, plus the duplicate-resource 409 mapping. Runs against the real
 * Testcontainers Postgres because the lookups rely on native jsonb queries that H2 can't run.
 *
 * Each test uses its own randomly-suffixed resource type so it can't collide with real
 * "utilisateurs"/"notifications"/"auditEvents"/"contenus" data or with other tests.
 */
@IntegrationTest
class DoveResourceStoreIT {

    @Autowired
    private DoveResourceStore store;

    @Test
    void findByExternalSubject_resolvesAndReportsNotFound() {
        String type = uniqueType("users");
        String subject = "keycloak-sub-" + UUID.randomUUID();

        ObjectNode user = objectNode();
        user.put("externalSubject", subject);
        user.put("status", "ACTIVE");
        ObjectNode created = store.create(type, user);

        assertThat(store.findByExternalSubject(type, subject))
            .isPresent()
            .get()
            .satisfies(found -> assertThat(found.path("id").asText()).isEqualTo(created.path("id").asText()));

        assertThat(store.findByExternalSubject(type, "no-such-subject")).isEmpty();
        assertThat(store.findByExternalSubject(uniqueType("users"), subject)).isEmpty();
    }

    @Test
    void listRecentForUser_boundsScopesToUserAndOrdersMostRecentFirst() throws InterruptedException {
        String type = uniqueType("notifications");
        String userId = "user-" + UUID.randomUUID();
        String otherUserId = "other-user-" + UUID.randomUUID();

        for (int i = 0; i < 7; i++) {
            ObjectNode notification = objectNode();
            notification.put("userId", userId);
            notification.put("index", i);
            store.create(type, notification);
            Thread.sleep(2); // guarantee strictly increasing created_at for a deterministic order
        }
        // Noise belonging to a different user: must never leak into the bounded result.
        ObjectNode noise = objectNode();
        noise.put("userId", otherUserId);
        store.create(type, noise);

        List<ObjectNode> recent = store.listRecentForUser(type, userId, 3);

        assertThat(recent).hasSize(3);
        assertThat(recent).allSatisfy(n -> assertThat(n.path("userId").asText()).isEqualTo(userId));
        assertThat(recent.stream().map(n -> n.path("index").asInt()).toList()).containsExactly(6, 5, 4);
    }

    @Test
    void listRecent_boundsAndOrdersMostRecentFirst() throws InterruptedException {
        String type = uniqueType("auditEvents");
        for (int i = 0; i < 5; i++) {
            ObjectNode event = objectNode();
            event.put("index", i);
            store.create(type, event);
            Thread.sleep(2);
        }

        List<ObjectNode> recent = store.listRecent(type, 2);

        assertThat(recent).hasSize(2);
        assertThat(recent.stream().map(n -> n.path("index").asInt()).toList()).containsExactly(4, 3);
    }

    @Test
    void listWithMaxRows_boundsResultSizeAtTheDatabase() {
        String type = uniqueType("contenus");
        for (int i = 0; i < 6; i++) {
            ObjectNode content = objectNode();
            content.put("index", i);
            store.create(type, content);
        }

        assertThat(store.list(type, 4)).hasSize(4);
        assertThat(store.list(type)).hasSize(6);
    }

    @Test
    void create_rejectsDuplicateExternalIdAsDuplicateResourceException() {
        String type = uniqueType("dup");
        String id = UUID.randomUUID().toString();

        ObjectNode first = objectNode();
        first.put("id", id);
        store.create(type, first);

        ObjectNode second = objectNode();
        second.put("id", id);
        assertThatThrownBy(() -> store.create(type, second)).isInstanceOf(DuplicateResourceException.class);
    }

    private static String uniqueType(String prefix) {
        return "test-" + prefix + "-" + UUID.randomUUID();
    }

    private static ObjectNode objectNode() {
        return JsonNodeFactory.instance.objectNode();
    }
}
