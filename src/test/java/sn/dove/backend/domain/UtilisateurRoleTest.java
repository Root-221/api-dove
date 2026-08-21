package sn.dove.backend.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static sn.dove.backend.domain.RoleTestSamples.*;
import static sn.dove.backend.domain.UtilisateurRoleTestSamples.*;
import static sn.dove.backend.domain.UtilisateurTestSamples.*;

import org.junit.jupiter.api.Test;
import sn.dove.backend.web.rest.TestUtil;

class UtilisateurRoleTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(UtilisateurRole.class);
        UtilisateurRole utilisateurRole1 = getUtilisateurRoleSample1();
        UtilisateurRole utilisateurRole2 = new UtilisateurRole();
        assertThat(utilisateurRole1).isNotEqualTo(utilisateurRole2);

        utilisateurRole2.setId(utilisateurRole1.getId());
        assertThat(utilisateurRole1).isEqualTo(utilisateurRole2);

        utilisateurRole2 = getUtilisateurRoleSample2();
        assertThat(utilisateurRole1).isNotEqualTo(utilisateurRole2);
    }

    @Test
    void utilisateurTest() {
        UtilisateurRole utilisateurRole = getUtilisateurRoleRandomSampleGenerator();
        Utilisateur utilisateurBack = getUtilisateurRandomSampleGenerator();

        utilisateurRole.setUtilisateur(utilisateurBack);
        assertThat(utilisateurRole.getUtilisateur()).isEqualTo(utilisateurBack);

        utilisateurRole.utilisateur(null);
        assertThat(utilisateurRole.getUtilisateur()).isNull();
    }

    @Test
    void roleTest() {
        UtilisateurRole utilisateurRole = getUtilisateurRoleRandomSampleGenerator();
        Role roleBack = getRoleRandomSampleGenerator();

        utilisateurRole.setRole(roleBack);
        assertThat(utilisateurRole.getRole()).isEqualTo(roleBack);

        utilisateurRole.role(null);
        assertThat(utilisateurRole.getRole()).isNull();
    }
}
