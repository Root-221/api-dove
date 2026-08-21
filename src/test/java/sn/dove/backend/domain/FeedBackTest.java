package sn.dove.backend.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static sn.dove.backend.domain.ContenuTestSamples.*;
import static sn.dove.backend.domain.FeedBackTestSamples.*;
import static sn.dove.backend.domain.UtilisateurTestSamples.*;

import org.junit.jupiter.api.Test;
import sn.dove.backend.web.rest.TestUtil;

class FeedBackTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(FeedBack.class);
        FeedBack feedBack1 = getFeedBackSample1();
        FeedBack feedBack2 = new FeedBack();
        assertThat(feedBack1).isNotEqualTo(feedBack2);

        feedBack2.setId(feedBack1.getId());
        assertThat(feedBack1).isEqualTo(feedBack2);

        feedBack2 = getFeedBackSample2();
        assertThat(feedBack1).isNotEqualTo(feedBack2);
    }

    @Test
    void utilisateurTest() {
        FeedBack feedBack = getFeedBackRandomSampleGenerator();
        Utilisateur utilisateurBack = getUtilisateurRandomSampleGenerator();

        feedBack.setUtilisateur(utilisateurBack);
        assertThat(feedBack.getUtilisateur()).isEqualTo(utilisateurBack);

        feedBack.utilisateur(null);
        assertThat(feedBack.getUtilisateur()).isNull();
    }

    @Test
    void contenuTest() {
        FeedBack feedBack = getFeedBackRandomSampleGenerator();
        Contenu contenuBack = getContenuRandomSampleGenerator();

        feedBack.setContenu(contenuBack);
        assertThat(feedBack.getContenu()).isEqualTo(contenuBack);

        feedBack.contenu(null);
        assertThat(feedBack.getContenu()).isNull();
    }
}
