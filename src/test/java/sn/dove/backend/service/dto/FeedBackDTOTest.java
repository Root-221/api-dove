package sn.dove.backend.service.dto;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;
import org.junit.jupiter.api.Test;
import sn.dove.backend.web.rest.TestUtil;

class FeedBackDTOTest {

    @Test
    void dtoEqualsVerifier() throws Exception {
        TestUtil.equalsVerifier(FeedBackDTO.class);
        FeedBackDTO feedBackDTO1 = new FeedBackDTO();
        feedBackDTO1.setId(UUID.randomUUID());
        FeedBackDTO feedBackDTO2 = new FeedBackDTO();
        assertThat(feedBackDTO1).isNotEqualTo(feedBackDTO2);
        feedBackDTO2.setId(feedBackDTO1.getId());
        assertThat(feedBackDTO1).isEqualTo(feedBackDTO2);
        feedBackDTO2.setId(UUID.randomUUID());
        assertThat(feedBackDTO1).isNotEqualTo(feedBackDTO2);
        feedBackDTO1.setId(null);
        assertThat(feedBackDTO1).isNotEqualTo(feedBackDTO2);
    }
}
