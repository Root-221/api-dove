package sn.dove.backend.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static sn.dove.backend.domain.ApplicationTestSamples.*;
import static sn.dove.backend.domain.ContenuTestSamples.*;
import static sn.dove.backend.domain.MetierTestSamples.*;
import static sn.dove.backend.domain.ModuleTestSamples.*;
import static sn.dove.backend.domain.UtilisateurTestSamples.*;

import org.junit.jupiter.api.Test;
import sn.dove.backend.web.rest.TestUtil;

class ContenuTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(Contenu.class);
        Contenu contenu1 = getContenuSample1();
        Contenu contenu2 = new Contenu();
        assertThat(contenu1).isNotEqualTo(contenu2);

        contenu2.setId(contenu1.getId());
        assertThat(contenu1).isEqualTo(contenu2);

        contenu2 = getContenuSample2();
        assertThat(contenu1).isNotEqualTo(contenu2);
    }

    @Test
    void applicationTest() {
        Contenu contenu = getContenuRandomSampleGenerator();
        Application applicationBack = getApplicationRandomSampleGenerator();

        contenu.setApplication(applicationBack);
        assertThat(contenu.getApplication()).isEqualTo(applicationBack);

        contenu.application(null);
        assertThat(contenu.getApplication()).isNull();
    }

    @Test
    void moduleTest() {
        Contenu contenu = getContenuRandomSampleGenerator();
        Module moduleBack = getModuleRandomSampleGenerator();

        contenu.setModule(moduleBack);
        assertThat(contenu.getModule()).isEqualTo(moduleBack);

        contenu.module(null);
        assertThat(contenu.getModule()).isNull();
    }

    @Test
    void metierTest() {
        Contenu contenu = getContenuRandomSampleGenerator();
        Metier metierBack = getMetierRandomSampleGenerator();

        contenu.setMetier(metierBack);
        assertThat(contenu.getMetier()).isEqualTo(metierBack);

        contenu.metier(null);
        assertThat(contenu.getMetier()).isNull();
    }

    @Test
    void auteurTest() {
        Contenu contenu = getContenuRandomSampleGenerator();
        Utilisateur utilisateurBack = getUtilisateurRandomSampleGenerator();

        contenu.setAuteur(utilisateurBack);
        assertThat(contenu.getAuteur()).isEqualTo(utilisateurBack);

        contenu.auteur(null);
        assertThat(contenu.getAuteur()).isNull();
    }

    @Test
    void validateurTest() {
        Contenu contenu = getContenuRandomSampleGenerator();
        Utilisateur utilisateurBack = getUtilisateurRandomSampleGenerator();

        contenu.setValidateur(utilisateurBack);
        assertThat(contenu.getValidateur()).isEqualTo(utilisateurBack);

        contenu.validateur(null);
        assertThat(contenu.getValidateur()).isNull();
    }
}
