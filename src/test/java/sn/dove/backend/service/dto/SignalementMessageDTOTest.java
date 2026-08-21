package sn.dove.backend.service.dto;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;
import org.junit.jupiter.api.Test;
import sn.dove.backend.web.rest.TestUtil;

class SignalementMessageDTOTest {

    @Test
    void dtoEqualsVerifier() throws Exception {
        TestUtil.equalsVerifier(SignalementMessageDTO.class);
        SignalementMessageDTO signalementMessageDTO1 = new SignalementMessageDTO();
        signalementMessageDTO1.setId(UUID.randomUUID());
        SignalementMessageDTO signalementMessageDTO2 = new SignalementMessageDTO();
        assertThat(signalementMessageDTO1).isNotEqualTo(signalementMessageDTO2);
        signalementMessageDTO2.setId(signalementMessageDTO1.getId());
        assertThat(signalementMessageDTO1).isEqualTo(signalementMessageDTO2);
        signalementMessageDTO2.setId(UUID.randomUUID());
        assertThat(signalementMessageDTO1).isNotEqualTo(signalementMessageDTO2);
        signalementMessageDTO1.setId(null);
        assertThat(signalementMessageDTO1).isNotEqualTo(signalementMessageDTO2);
    }
}
