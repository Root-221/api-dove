package sn.dove.backend.service.mapper;

import org.mapstruct.*;
import sn.dove.backend.domain.Contenu;
import sn.dove.backend.domain.Signalement;
import sn.dove.backend.domain.Utilisateur;
import sn.dove.backend.service.dto.ContenuDTO;
import sn.dove.backend.service.dto.SignalementDTO;
import sn.dove.backend.service.dto.UtilisateurDTO;

/**
 * Mapper for the entity {@link Signalement} and its DTO {@link SignalementDTO}.
 */
@Mapper(componentModel = "spring")
public interface SignalementMapper extends EntityMapper<SignalementDTO, Signalement> {
    @Mapping(target = "utilisateur", source = "utilisateur", qualifiedByName = "utilisateurLogin")
    @Mapping(target = "contenu", source = "contenu", qualifiedByName = "contenuTitre")
    SignalementDTO toDto(Signalement s);

    @Named("utilisateurLogin")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    @Mapping(target = "login", source = "login")
    UtilisateurDTO toDtoUtilisateurLogin(Utilisateur utilisateur);

    @Named("contenuTitre")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    @Mapping(target = "titre", source = "titre")
    ContenuDTO toDtoContenuTitre(Contenu contenu);
}
