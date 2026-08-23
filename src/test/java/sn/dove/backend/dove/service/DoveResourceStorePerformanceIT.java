package sn.dove.backend.dove.service;

import static org.assertj.core.api.Assertions.assertThat;

import tools.jackson.databind.node.JsonNodeFactory;
import tools.jackson.databind.node.ObjectNode;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import sn.dove.backend.IntegrationTest;

/**
 * Benchmark for the "full-table-scan on every authenticated request" fix in
 * DoveCurrentUserService: it used to resolve the caller by loading every row of type
 * "utilisateurs" via {@link DoveResourceStore#list(String)} and filtering in Java, now it uses
 * {@link DoveResourceStore#findByExternalSubject(String, String)}, an indexed native query.
 *
 * This seeds a realistic number of user rows and times both approaches against the same data, to
 * put a real number on the fix rather than just asserting "it compiles". Not part of the regular
 * fast test loop by design (seeding thousands of rows takes real seconds) - run explicitly.
 */
@IntegrationTest
class DoveResourceStorePerformanceIT {

    private static final Logger log = LoggerFactory.getLogger(DoveResourceStorePerformanceIT.class);
    private static final int ROW_COUNT = 3000;

    @Autowired
    private DoveResourceStore store;

    @Test
    void indexedLookupStaysFastAsTheTableGrows_unlikeTheFullTableScanItReplaced() {
        String type = "utilisateurs-bench-" + UUID.randomUUID();
        String targetSubject = "keycloak-sub-target";

        for (int i = 0; i < ROW_COUNT; i++) {
            ObjectNode user = JsonNodeFactory.instance.objectNode();
            user.put("externalSubject", i == ROW_COUNT / 2 ? targetSubject : "keycloak-sub-" + UUID.randomUUID());
            user.put("status", "ACTIVE");
            store.create(type, user);
        }

        // Warm-up pass (JIT/connection pool) for both approaches, discarded.
        fullScanLookup(type, targetSubject);
        store.findByExternalSubject(type, targetSubject);

        long scanStart = System.nanoTime();
        ObjectNode viaScan = fullScanLookup(type, targetSubject);
        long scanMs = (System.nanoTime() - scanStart) / 1_000_000;

        long indexedStart = System.nanoTime();
        ObjectNode viaIndex = store.findByExternalSubject(type, targetSubject).orElseThrow();
        long indexedMs = (System.nanoTime() - indexedStart) / 1_000_000;

        log.info(
            "DoveCurrentUserService lookup benchmark @ {} rows -- old (store.list + Java filter): {} ms | new (findByExternalSubject, indexed): {} ms | ratio: {}x",
            ROW_COUNT,
            scanMs,
            indexedMs,
            indexedMs == 0 ? "n/a" : String.format("%.1f", scanMs / (double) Math.max(indexedMs, 1))
        );

        assertThat(viaScan.path("externalSubject").asText()).isEqualTo(targetSubject);
        assertThat(viaIndex.path("externalSubject").asText()).isEqualTo(targetSubject);
        // Loose bound (not a tight timing assertion, which would be flaky): the old approach
        // deserializes and scans every one of the ROW_COUNT rows on every call, so it cannot be
        // as fast as an indexed point lookup once the table has any real size to it.
        assertThat(indexedMs).isLessThanOrEqualTo(Math.max(scanMs, 1));
    }

    /** Reproduces the exact pattern DoveCurrentUserService used before the fix. */
    private ObjectNode fullScanLookup(String type, String subject) {
        List<ObjectNode> all = store.list(type);
        return all.stream().filter(u -> subject.equals(u.path("externalSubject").asText())).findFirst().orElseThrow();
    }
}
