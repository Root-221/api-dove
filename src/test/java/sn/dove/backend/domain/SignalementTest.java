package sn.dove.backend.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static sn.dove.backend.domain.ContenuTestSamples.*;
import static sn.dove.backend.domain.SignalementTestSamples.*;
import static sn.dove.backend.domain.UtilisateurTestSamples.*;

import org.junit.jupiter.api.Test;
import sn.dove.backend.web.rest.TestUtil;

class SignalementTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(Signalement.class);
        Signalement signalement1 = getSignalementSample1();
        Signalement signalement2 = new Signalement();
        assertThat(signalement1).isNotEqualTo(signalement2);

        signalement2.setId(signalement1.getId());
        assertThat(signalement1).isEqualTo(signalement2);

        signalement2 = getSignalementSample2();
        assertThat(signalement1).isNotEqualTo(signalement2);
    }

    @Test
    void utilisateurTest() {
        Signalement signalement = getSignalementRandomSampleGenerator();
        Utilisateur utilisateurBack = getUtilisateurRandomSampleGenerator();

        signalement.setUtilisateur(utilisateurBack);
        assertThat(signalement.getUtilisateur()).isEqualTo(utilisateurBack);

        signalement.utilisateur(null);
        assertThat(signalement.getUtilisateur()).isNull();
    }

    @Test
    void contenuTest() {
        Signalement signalement = getSignalementRandomSampleGenerator();
        Contenu contenuBack = getContenuRandomSampleGenerator();

        signalement.setContenu(contenuBack);
        assertThat(signalement.getContenu()).isEqualTo(contenuBack);

        signalement.contenu(null);
        assertThat(signalement.getContenu()).isNull();
    }
}
