package sn.dove.backend.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static sn.dove.backend.domain.DiscussionTestSamples.*;
import static sn.dove.backend.domain.MessageTestSamples.*;
import static sn.dove.backend.domain.MessageTestSamples.*;
import static sn.dove.backend.domain.MetierTestSamples.*;
import static sn.dove.backend.domain.SignalementMessageTestSamples.*;
import static sn.dove.backend.domain.UtilisateurTestSamples.*;

import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.Test;
import sn.dove.backend.web.rest.TestUtil;

class MessageTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(Message.class);
        Message message1 = getMessageSample1();
        Message message2 = new Message();
        assertThat(message1).isNotEqualTo(message2);

        message2.setId(message1.getId());
        assertThat(message1).isEqualTo(message2);

        message2 = getMessageSample2();
        assertThat(message1).isNotEqualTo(message2);
    }

    @Test
    void reponsesTest() {
        Message message = getMessageRandomSampleGenerator();
        Message messageBack = getMessageRandomSampleGenerator();

        message.addReponses(messageBack);
        assertThat(message.getReponseses()).containsOnly(messageBack);
        assertThat(messageBack.getMessageParent()).isEqualTo(message);

        message.removeReponses(messageBack);
        assertThat(message.getReponseses()).doesNotContain(messageBack);
        assertThat(messageBack.getMessageParent()).isNull();

        message.reponseses(new HashSet<>(Set.of(messageBack)));
        assertThat(message.getReponseses()).containsOnly(messageBack);
        assertThat(messageBack.getMessageParent()).isEqualTo(message);

        message.setReponseses(new HashSet<>());
        assertThat(message.getReponseses()).doesNotContain(messageBack);
        assertThat(messageBack.getMessageParent()).isNull();
    }

    @Test
    void signalementsTest() {
        Message message = getMessageRandomSampleGenerator();
        SignalementMessage signalementMessageBack = getSignalementMessageRandomSampleGenerator();

        message.addSignalements(signalementMessageBack);
        assertThat(message.getSignalementses()).containsOnly(signalementMessageBack);
        assertThat(signalementMessageBack.getMessage()).isEqualTo(message);

        message.removeSignalements(signalementMessageBack);
        assertThat(message.getSignalementses()).doesNotContain(signalementMessageBack);
        assertThat(signalementMessageBack.getMessage()).isNull();

        message.signalementses(new HashSet<>(Set.of(signalementMessageBack)));
        assertThat(message.getSignalementses()).containsOnly(signalementMessageBack);
        assertThat(signalementMessageBack.getMessage()).isEqualTo(message);

        message.setSignalementses(new HashSet<>());
        assertThat(message.getSignalementses()).doesNotContain(signalementMessageBack);
        assertThat(signalementMessageBack.getMessage()).isNull();
    }

    @Test
    void emetteurTest() {
        Message message = getMessageRandomSampleGenerator();
        Utilisateur utilisateurBack = getUtilisateurRandomSampleGenerator();

        message.setEmetteur(utilisateurBack);
        assertThat(message.getEmetteur()).isEqualTo(utilisateurBack);

        message.emetteur(null);
        assertThat(message.getEmetteur()).isNull();
    }

    @Test
    void metierTest() {
        Message message = getMessageRandomSampleGenerator();
        Metier metierBack = getMetierRandomSampleGenerator();

        message.setMetier(metierBack);
        assertThat(message.getMetier()).isEqualTo(metierBack);

        message.metier(null);
        assertThat(message.getMetier()).isNull();
    }

    @Test
    void discussionTest() {
        Message message = getMessageRandomSampleGenerator();
        Discussion discussionBack = getDiscussionRandomSampleGenerator();

        message.setDiscussion(discussionBack);
        assertThat(message.getDiscussion()).isEqualTo(discussionBack);

        message.discussion(null);
        assertThat(message.getDiscussion()).isNull();
    }

    @Test
    void messageParentTest() {
        Message message = getMessageRandomSampleGenerator();
        Message messageBack = getMessageRandomSampleGenerator();

        message.setMessageParent(messageBack);
        assertThat(message.getMessageParent()).isEqualTo(messageBack);

        message.messageParent(null);
        assertThat(message.getMessageParent()).isNull();
    }
}
