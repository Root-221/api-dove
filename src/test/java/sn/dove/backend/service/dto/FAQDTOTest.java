package sn.dove.backend.service.dto;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;
import org.junit.jupiter.api.Test;
import sn.dove.backend.web.rest.TestUtil;

class FAQDTOTest {

    @Test
    void dtoEqualsVerifier() throws Exception {
        TestUtil.equalsVerifier(FAQDTO.class);
        FAQDTO fAQDTO1 = new FAQDTO();
        fAQDTO1.setId(UUID.randomUUID());
        FAQDTO fAQDTO2 = new FAQDTO();
        assertThat(fAQDTO1).isNotEqualTo(fAQDTO2);
        fAQDTO2.setId(fAQDTO1.getId());
        assertThat(fAQDTO1).isEqualTo(fAQDTO2);
        fAQDTO2.setId(UUID.randomUUID());
        assertThat(fAQDTO1).isNotEqualTo(fAQDTO2);
        fAQDTO1.setId(null);
        assertThat(fAQDTO1).isNotEqualTo(fAQDTO2);
    }
}
