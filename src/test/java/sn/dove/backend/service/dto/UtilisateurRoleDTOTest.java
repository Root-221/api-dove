package sn.dove.backend.service.dto;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;
import org.junit.jupiter.api.Test;
import sn.dove.backend.web.rest.TestUtil;

class UtilisateurRoleDTOTest {

    @Test
    void dtoEqualsVerifier() throws Exception {
        TestUtil.equalsVerifier(UtilisateurRoleDTO.class);
        UtilisateurRoleDTO utilisateurRoleDTO1 = new UtilisateurRoleDTO();
        utilisateurRoleDTO1.setId(UUID.randomUUID());
        UtilisateurRoleDTO utilisateurRoleDTO2 = new UtilisateurRoleDTO();
        assertThat(utilisateurRoleDTO1).isNotEqualTo(utilisateurRoleDTO2);
        utilisateurRoleDTO2.setId(utilisateurRoleDTO1.getId());
        assertThat(utilisateurRoleDTO1).isEqualTo(utilisateurRoleDTO2);
        utilisateurRoleDTO2.setId(UUID.randomUUID());
        assertThat(utilisateurRoleDTO1).isNotEqualTo(utilisateurRoleDTO2);
        utilisateurRoleDTO1.setId(null);
        assertThat(utilisateurRoleDTO1).isNotEqualTo(utilisateurRoleDTO2);
    }
}
