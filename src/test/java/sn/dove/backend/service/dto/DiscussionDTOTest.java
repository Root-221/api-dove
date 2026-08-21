package sn.dove.backend.service.dto;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;
import org.junit.jupiter.api.Test;
import sn.dove.backend.web.rest.TestUtil;

class DiscussionDTOTest {

    @Test
    void dtoEqualsVerifier() throws Exception {
        TestUtil.equalsVerifier(DiscussionDTO.class);
        DiscussionDTO discussionDTO1 = new DiscussionDTO();
        discussionDTO1.setId(UUID.randomUUID());
        DiscussionDTO discussionDTO2 = new DiscussionDTO();
        assertThat(discussionDTO1).isNotEqualTo(discussionDTO2);
        discussionDTO2.setId(discussionDTO1.getId());
        assertThat(discussionDTO1).isEqualTo(discussionDTO2);
        discussionDTO2.setId(UUID.randomUUID());
        assertThat(discussionDTO1).isNotEqualTo(discussionDTO2);
        discussionDTO1.setId(null);
        assertThat(discussionDTO1).isNotEqualTo(discussionDTO2);
    }
}
