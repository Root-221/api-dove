package sn.dove.backend.service.dto;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;
import org.junit.jupiter.api.Test;
import sn.dove.backend.web.rest.TestUtil;

class MetierDTOTest {

    @Test
    void dtoEqualsVerifier() throws Exception {
        TestUtil.equalsVerifier(MetierDTO.class);
        MetierDTO metierDTO1 = new MetierDTO();
        metierDTO1.setId(UUID.randomUUID());
        MetierDTO metierDTO2 = new MetierDTO();
        assertThat(metierDTO1).isNotEqualTo(metierDTO2);
        metierDTO2.setId(metierDTO1.getId());
        assertThat(metierDTO1).isEqualTo(metierDTO2);
        metierDTO2.setId(UUID.randomUUID());
        assertThat(metierDTO1).isNotEqualTo(metierDTO2);
        metierDTO1.setId(null);
        assertThat(metierDTO1).isNotEqualTo(metierDTO2);
    }
}
