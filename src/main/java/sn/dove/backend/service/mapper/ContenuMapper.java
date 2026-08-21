package sn.dove.backend.service.mapper;

import org.mapstruct.*;
import sn.dove.backend.domain.Application;
import sn.dove.backend.domain.Contenu;
import sn.dove.backend.domain.Metier;
import sn.dove.backend.domain.Module;
import sn.dove.backend.domain.Utilisateur;
import sn.dove.backend.service.dto.ApplicationDTO;
import sn.dove.backend.service.dto.ContenuDTO;
import sn.dove.backend.service.dto.MetierDTO;
import sn.dove.backend.service.dto.ModuleDTO;
import sn.dove.backend.service.dto.UtilisateurDTO;

/**
 * Mapper for the entity {@link Contenu} and its DTO {@link ContenuDTO}.
 */
@Mapper(componentModel = "spring")
public interface ContenuMapper extends EntityMapper<ContenuDTO, Contenu> {
    @Mapping(target = "application", source = "application", qualifiedByName = "applicationNom")
    @Mapping(target = "module", source = "module", qualifiedByName = "moduleLibelle")
    @Mapping(target = "metier", source = "metier", qualifiedByName = "metierLibelle")
    @Mapping(target = "auteur", source = "auteur", qualifiedByName = "utilisateurLogin")
    @Mapping(target = "validateur", source = "validateur", qualifiedByName = "utilisateurLogin")
    ContenuDTO toDto(Contenu s);

    @Named("applicationNom")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    @Mapping(target = "nom", source = "nom")
    ApplicationDTO toDtoApplicationNom(Application application);

    @Named("moduleLibelle")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    @Mapping(target = "libelle", source = "libelle")
    ModuleDTO toDtoModuleLibelle(Module module);

    @Named("metierLibelle")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    @Mapping(target = "libelle", source = "libelle")
    MetierDTO toDtoMetierLibelle(Metier metier);

    @Named("utilisateurLogin")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    @Mapping(target = "login", source = "login")
    UtilisateurDTO toDtoUtilisateurLogin(Utilisateur utilisateur);
}
