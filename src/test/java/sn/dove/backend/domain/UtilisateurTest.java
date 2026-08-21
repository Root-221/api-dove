package sn.dove.backend.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static sn.dove.backend.domain.CommunauteNanditeTestSamples.*;
import static sn.dove.backend.domain.UtilisateurTestSamples.*;

import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.Test;
import sn.dove.backend.web.rest.TestUtil;

class UtilisateurTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(Utilisateur.class);
        Utilisateur utilisateur1 = getUtilisateurSample1();
        Utilisateur utilisateur2 = new Utilisateur();
        assertThat(utilisateur1).isNotEqualTo(utilisateur2);

        utilisateur2.setId(utilisateur1.getId());
        assertThat(utilisateur1).isEqualTo(utilisateur2);

        utilisateur2 = getUtilisateurSample2();
        assertThat(utilisateur1).isNotEqualTo(utilisateur2);
    }

    @Test
    void communauteNanditeTest() {
        Utilisateur utilisateur = getUtilisateurRandomSampleGenerator();
        CommunauteNandite communauteNanditeBack = getCommunauteNanditeRandomSampleGenerator();

        utilisateur.addCommunauteNandite(communauteNanditeBack);
        assertThat(utilisateur.getCommunauteNandites()).containsOnly(communauteNanditeBack);
        assertThat(communauteNanditeBack.getMembreses()).containsOnly(utilisateur);

        utilisateur.removeCommunauteNandite(communauteNanditeBack);
        assertThat(utilisateur.getCommunauteNandites()).doesNotContain(communauteNanditeBack);
        assertThat(communauteNanditeBack.getMembreses()).doesNotContain(utilisateur);

        utilisateur.communauteNandites(new HashSet<>(Set.of(communauteNanditeBack)));
        assertThat(utilisateur.getCommunauteNandites()).containsOnly(communauteNanditeBack);
        assertThat(communauteNanditeBack.getMembreses()).containsOnly(utilisateur);

        utilisateur.setCommunauteNandites(new HashSet<>());
        assertThat(utilisateur.getCommunauteNandites()).doesNotContain(communauteNanditeBack);
        assertThat(communauteNanditeBack.getMembreses()).doesNotContain(utilisateur);
    }
}
