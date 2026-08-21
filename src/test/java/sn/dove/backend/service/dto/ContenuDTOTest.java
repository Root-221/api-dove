package sn.dove.backend.service.dto;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;
import org.junit.jupiter.api.Test;
import sn.dove.backend.web.rest.TestUtil;

class ContenuDTOTest {

    @Test
    void dtoEqualsVerifier() throws Exception {
        TestUtil.equalsVerifier(ContenuDTO.class);
        ContenuDTO contenuDTO1 = new ContenuDTO();
        contenuDTO1.setId(UUID.randomUUID());
        ContenuDTO contenuDTO2 = new ContenuDTO();
        assertThat(contenuDTO1).isNotEqualTo(contenuDTO2);
        contenuDTO2.setId(contenuDTO1.getId());
        assertThat(contenuDTO1).isEqualTo(contenuDTO2);
        contenuDTO2.setId(UUID.randomUUID());
        assertThat(contenuDTO1).isNotEqualTo(contenuDTO2);
        contenuDTO1.setId(null);
        assertThat(contenuDTO1).isNotEqualTo(contenuDTO2);
    }
}
