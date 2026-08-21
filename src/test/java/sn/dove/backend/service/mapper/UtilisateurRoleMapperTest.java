package sn.dove.backend.service.mapper;

import static sn.dove.backend.domain.UtilisateurRoleAsserts.*;
import static sn.dove.backend.domain.UtilisateurRoleTestSamples.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class UtilisateurRoleMapperTest {

    private UtilisateurRoleMapper utilisateurRoleMapper;

    @BeforeEach
    void setUp() {
        utilisateurRoleMapper = new UtilisateurRoleMapperImpl();
    }

    @Test
    void shouldConvertToDtoAndBack() {
        var expected = getUtilisateurRoleSample1();
        var actual = utilisateurRoleMapper.toEntity(utilisateurRoleMapper.toDto(expected));
        assertUtilisateurRoleAllPropertiesEquals(expected, actual);
    }
}
