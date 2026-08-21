package sn.dove.backend.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static sn.dove.backend.domain.CommunauteNanditeTestSamples.*;
import static sn.dove.backend.domain.DiscussionTestSamples.*;
import static sn.dove.backend.domain.UtilisateurTestSamples.*;

import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.Test;
import sn.dove.backend.web.rest.TestUtil;

class CommunauteNanditeTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(CommunauteNandite.class);
        CommunauteNandite communauteNandite1 = getCommunauteNanditeSample1();
        CommunauteNandite communauteNandite2 = new CommunauteNandite();
        assertThat(communauteNandite1).isNotEqualTo(communauteNandite2);

        communauteNandite2.setId(communauteNandite1.getId());
        assertThat(communauteNandite1).isEqualTo(communauteNandite2);

        communauteNandite2 = getCommunauteNanditeSample2();
        assertThat(communauteNandite1).isNotEqualTo(communauteNandite2);
    }

    @Test
    void discussionsTest() {
        CommunauteNandite communauteNandite = getCommunauteNanditeRandomSampleGenerator();
        Discussion discussionBack = getDiscussionRandomSampleGenerator();

        communauteNandite.addDiscussions(discussionBack);
        assertThat(communauteNandite.getDiscussionses()).containsOnly(discussionBack);
        assertThat(discussionBack.getCommunauteNandite()).isEqualTo(communauteNandite);

        communauteNandite.removeDiscussions(discussionBack);
        assertThat(communauteNandite.getDiscussionses()).doesNotContain(discussionBack);
        assertThat(discussionBack.getCommunauteNandite()).isNull();

        communauteNandite.discussionses(new HashSet<>(Set.of(discussionBack)));
        assertThat(communauteNandite.getDiscussionses()).containsOnly(discussionBack);
        assertThat(discussionBack.getCommunauteNandite()).isEqualTo(communauteNandite);

        communauteNandite.setDiscussionses(new HashSet<>());
        assertThat(communauteNandite.getDiscussionses()).doesNotContain(discussionBack);
        assertThat(discussionBack.getCommunauteNandite()).isNull();
    }

    @Test
    void membresTest() {
        CommunauteNandite communauteNandite = getCommunauteNanditeRandomSampleGenerator();
        Utilisateur utilisateurBack = getUtilisateurRandomSampleGenerator();

        communauteNandite.addMembres(utilisateurBack);
        assertThat(communauteNandite.getMembreses()).containsOnly(utilisateurBack);

        communauteNandite.removeMembres(utilisateurBack);
        assertThat(communauteNandite.getMembreses()).doesNotContain(utilisateurBack);

        communauteNandite.membreses(new HashSet<>(Set.of(utilisateurBack)));
        assertThat(communauteNandite.getMembreses()).containsOnly(utilisateurBack);

        communauteNandite.setMembreses(new HashSet<>());
        assertThat(communauteNandite.getMembreses()).doesNotContain(utilisateurBack);
    }
}
