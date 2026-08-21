package sn.dove.backend.service.mapper;

import org.mapstruct.*;
import sn.dove.backend.domain.Role;
import sn.dove.backend.domain.Utilisateur;
import sn.dove.backend.domain.UtilisateurRole;
import sn.dove.backend.service.dto.RoleDTO;
import sn.dove.backend.service.dto.UtilisateurDTO;
import sn.dove.backend.service.dto.UtilisateurRoleDTO;

/**
 * Mapper for the entity {@link UtilisateurRole} and its DTO {@link UtilisateurRoleDTO}.
 */
@Mapper(componentModel = "spring")
public interface UtilisateurRoleMapper extends EntityMapper<UtilisateurRoleDTO, UtilisateurRole> {
    @Mapping(target = "utilisateur", source = "utilisateur", qualifiedByName = "utilisateurLogin")
    @Mapping(target = "role", source = "role", qualifiedByName = "roleRole")
    UtilisateurRoleDTO toDto(UtilisateurRole s);

    @Named("utilisateurLogin")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    @Mapping(target = "login", source = "login")
    UtilisateurDTO toDtoUtilisateurLogin(Utilisateur utilisateur);

    @Named("roleRole")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    @Mapping(target = "role", source = "role")
    RoleDTO toDtoRoleRole(Role role);
}
