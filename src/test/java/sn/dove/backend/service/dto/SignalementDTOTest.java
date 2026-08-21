package sn.dove.backend.service.dto;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;
import org.junit.jupiter.api.Test;
import sn.dove.backend.web.rest.TestUtil;

class SignalementDTOTest {

    @Test
    void dtoEqualsVerifier() throws Exception {
        TestUtil.equalsVerifier(SignalementDTO.class);
        SignalementDTO signalementDTO1 = new SignalementDTO();
        signalementDTO1.setId(UUID.randomUUID());
        SignalementDTO signalementDTO2 = new SignalementDTO();
        assertThat(signalementDTO1).isNotEqualTo(signalementDTO2);
        signalementDTO2.setId(signalementDTO1.getId());
        assertThat(signalementDTO1).isEqualTo(signalementDTO2);
        signalementDTO2.setId(UUID.randomUUID());
        assertThat(signalementDTO1).isNotEqualTo(signalementDTO2);
        signalementDTO1.setId(null);
        assertThat(signalementDTO1).isNotEqualTo(signalementDTO2);
    }
}
