package sn.dove.backend.dove.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sn.dove.backend.dove.security.DoveCurrentUserService;
import sn.dove.backend.dove.service.DoveDomainEvents;
import sn.dove.backend.dove.service.DoveResourceStore;

/**
 * Covers the fix for "the most recently added discussion doesn't show first": the community feed
 * used to call {@code store.list("discussions")} (ascending, oldest-first, unbounded), now calls
 * {@code store.listRecent("discussions", 200)} (descending, newest-first, bounded - same pattern
 * already used for notifications/audit).
 */
@ExtendWith(MockitoExtension.class)
class CommunityResourceV1Test {

    @Mock
    private DoveResourceStore store;

    @Mock
    private DoveCurrentUserService users;

    @Mock
    private DoveDomainEvents events;

    @Test
    void list_delegatesToListRecent_newestFirstAndBounded() {
        CommunityResourceV1 resource = new CommunityResourceV1(new DoveApiSupport(store, users, events));

        when(store.listRecent("discussions", 200)).thenReturn(List.of());

        List<?> result = resource.list();

        verify(store).listRecent("discussions", 200);
        assertThat(result).isNotNull();
    }
}
