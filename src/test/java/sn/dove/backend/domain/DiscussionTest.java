package sn.dove.backend.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static sn.dove.backend.domain.CommunauteNanditeTestSamples.*;
import static sn.dove.backend.domain.DiscussionTestSamples.*;
import static sn.dove.backend.domain.MessageTestSamples.*;
import static sn.dove.backend.domain.UtilisateurTestSamples.*;

import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.Test;
import sn.dove.backend.web.rest.TestUtil;

class DiscussionTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(Discussion.class);
        Discussion discussion1 = getDiscussionSample1();
        Discussion discussion2 = new Discussion();
        assertThat(discussion1).isNotEqualTo(discussion2);

        discussion2.setId(discussion1.getId());
        assertThat(discussion1).isEqualTo(discussion2);

        discussion2 = getDiscussionSample2();
        assertThat(discussion1).isNotEqualTo(discussion2);
    }

    @Test
    void messagesTest() {
        Discussion discussion = getDiscussionRandomSampleGenerator();
        Message messageBack = getMessageRandomSampleGenerator();

        discussion.addMessages(messageBack);
        assertThat(discussion.getMessageses()).containsOnly(messageBack);
        assertThat(messageBack.getDiscussion()).isEqualTo(discussion);

        discussion.removeMessages(messageBack);
        assertThat(discussion.getMessageses()).doesNotContain(messageBack);
        assertThat(messageBack.getDiscussion()).isNull();

        discussion.messageses(new HashSet<>(Set.of(messageBack)));
        assertThat(discussion.getMessageses()).containsOnly(messageBack);
        assertThat(messageBack.getDiscussion()).isEqualTo(discussion);

        discussion.setMessageses(new HashSet<>());
        assertThat(discussion.getMessageses()).doesNotContain(messageBack);
        assertThat(messageBack.getDiscussion()).isNull();
    }

    @Test
    void createurTest() {
        Discussion discussion = getDiscussionRandomSampleGenerator();
        Utilisateur utilisateurBack = getUtilisateurRandomSampleGenerator();

        discussion.setCreateur(utilisateurBack);
        assertThat(discussion.getCreateur()).isEqualTo(utilisateurBack);

        discussion.createur(null);
        assertThat(discussion.getCreateur()).isNull();
    }

    @Test
    void communauteNanditeTest() {
        Discussion discussion = getDiscussionRandomSampleGenerator();
        CommunauteNandite communauteNanditeBack = getCommunauteNanditeRandomSampleGenerator();

        discussion.setCommunauteNandite(communauteNanditeBack);
        assertThat(discussion.getCommunauteNandite()).isEqualTo(communauteNanditeBack);

        discussion.communauteNandite(null);
        assertThat(discussion.getCommunauteNandite()).isNull();
    }
}
