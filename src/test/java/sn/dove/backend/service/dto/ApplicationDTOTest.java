package sn.dove.backend.service.dto;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;
import org.junit.jupiter.api.Test;
import sn.dove.backend.web.rest.TestUtil;

class ApplicationDTOTest {

    @Test
    void dtoEqualsVerifier() throws Exception {
        TestUtil.equalsVerifier(ApplicationDTO.class);
        ApplicationDTO applicationDTO1 = new ApplicationDTO();
        applicationDTO1.setId(UUID.randomUUID());
        ApplicationDTO applicationDTO2 = new ApplicationDTO();
        assertThat(applicationDTO1).isNotEqualTo(applicationDTO2);
        applicationDTO2.setId(applicationDTO1.getId());
        assertThat(applicationDTO1).isEqualTo(applicationDTO2);
        applicationDTO2.setId(UUID.randomUUID());
        assertThat(applicationDTO1).isNotEqualTo(applicationDTO2);
        applicationDTO1.setId(null);
        assertThat(applicationDTO1).isNotEqualTo(applicationDTO2);
    }
}
