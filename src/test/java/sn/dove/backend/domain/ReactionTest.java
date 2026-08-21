package sn.dove.backend.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static sn.dove.backend.domain.MessageTestSamples.*;
import static sn.dove.backend.domain.ReactionTestSamples.*;
import static sn.dove.backend.domain.UtilisateurTestSamples.*;

import org.junit.jupiter.api.Test;
import sn.dove.backend.web.rest.TestUtil;

class ReactionTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(Reaction.class);
        Reaction reaction1 = getReactionSample1();
        Reaction reaction2 = new Reaction();
        assertThat(reaction1).isNotEqualTo(reaction2);

        reaction2.setId(reaction1.getId());
        assertThat(reaction1).isEqualTo(reaction2);

        reaction2 = getReactionSample2();
        assertThat(reaction1).isNotEqualTo(reaction2);
    }

    @Test
    void utilisateurTest() {
        Reaction reaction = getReactionRandomSampleGenerator();
        Utilisateur utilisateurBack = getUtilisateurRandomSampleGenerator();

        reaction.setUtilisateur(utilisateurBack);
        assertThat(reaction.getUtilisateur()).isEqualTo(utilisateurBack);

        reaction.utilisateur(null);
        assertThat(reaction.getUtilisateur()).isNull();
    }

    @Test
    void messageTest() {
        Reaction reaction = getReactionRandomSampleGenerator();
        Message messageBack = getMessageRandomSampleGenerator();

        reaction.setMessage(messageBack);
        assertThat(reaction.getMessage()).isEqualTo(messageBack);

        reaction.message(null);
        assertThat(reaction.getMessage()).isNull();
    }
}
