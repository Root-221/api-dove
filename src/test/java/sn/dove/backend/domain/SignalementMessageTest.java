package sn.dove.backend.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static sn.dove.backend.domain.MessageTestSamples.*;
import static sn.dove.backend.domain.SignalementMessageTestSamples.*;
import static sn.dove.backend.domain.UtilisateurTestSamples.*;

import org.junit.jupiter.api.Test;
import sn.dove.backend.web.rest.TestUtil;

class SignalementMessageTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(SignalementMessage.class);
        SignalementMessage signalementMessage1 = getSignalementMessageSample1();
        SignalementMessage signalementMessage2 = new SignalementMessage();
        assertThat(signalementMessage1).isNotEqualTo(signalementMessage2);

        signalementMessage2.setId(signalementMessage1.getId());
        assertThat(signalementMessage1).isEqualTo(signalementMessage2);

        signalementMessage2 = getSignalementMessageSample2();
        assertThat(signalementMessage1).isNotEqualTo(signalementMessage2);
    }

    @Test
    void utilisateurTest() {
        SignalementMessage signalementMessage = getSignalementMessageRandomSampleGenerator();
        Utilisateur utilisateurBack = getUtilisateurRandomSampleGenerator();

        signalementMessage.setUtilisateur(utilisateurBack);
        assertThat(signalementMessage.getUtilisateur()).isEqualTo(utilisateurBack);

        signalementMessage.utilisateur(null);
        assertThat(signalementMessage.getUtilisateur()).isNull();
    }

    @Test
    void messageTest() {
        SignalementMessage signalementMessage = getSignalementMessageRandomSampleGenerator();
        Message messageBack = getMessageRandomSampleGenerator();

        signalementMessage.setMessage(messageBack);
        assertThat(signalementMessage.getMessage()).isEqualTo(messageBack);

        signalementMessage.message(null);
        assertThat(signalementMessage.getMessage()).isNull();
    }
}
