package sn.dove.backend.service.dto;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;
import org.junit.jupiter.api.Test;
import sn.dove.backend.web.rest.TestUtil;

class CommunauteNanditeDTOTest {

    @Test
    void dtoEqualsVerifier() throws Exception {
        TestUtil.equalsVerifier(CommunauteNanditeDTO.class);
        CommunauteNanditeDTO communauteNanditeDTO1 = new CommunauteNanditeDTO();
        communauteNanditeDTO1.setId(UUID.randomUUID());
        CommunauteNanditeDTO communauteNanditeDTO2 = new CommunauteNanditeDTO();
        assertThat(communauteNanditeDTO1).isNotEqualTo(communauteNanditeDTO2);
        communauteNanditeDTO2.setId(communauteNanditeDTO1.getId());
        assertThat(communauteNanditeDTO1).isEqualTo(communauteNanditeDTO2);
        communauteNanditeDTO2.setId(UUID.randomUUID());
        assertThat(communauteNanditeDTO1).isNotEqualTo(communauteNanditeDTO2);
        communauteNanditeDTO1.setId(null);
        assertThat(communauteNanditeDTO1).isNotEqualTo(communauteNanditeDTO2);
    }
}
