package sn.dove.backend.service.dto;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;
import org.junit.jupiter.api.Test;
import sn.dove.backend.web.rest.TestUtil;

class ReactionDTOTest {

    @Test
    void dtoEqualsVerifier() throws Exception {
        TestUtil.equalsVerifier(ReactionDTO.class);
        ReactionDTO reactionDTO1 = new ReactionDTO();
        reactionDTO1.setId(UUID.randomUUID());
        ReactionDTO reactionDTO2 = new ReactionDTO();
        assertThat(reactionDTO1).isNotEqualTo(reactionDTO2);
        reactionDTO2.setId(reactionDTO1.getId());
        assertThat(reactionDTO1).isEqualTo(reactionDTO2);
        reactionDTO2.setId(UUID.randomUUID());
        assertThat(reactionDTO1).isNotEqualTo(reactionDTO2);
        reactionDTO1.setId(null);
        assertThat(reactionDTO1).isNotEqualTo(reactionDTO2);
    }
}
