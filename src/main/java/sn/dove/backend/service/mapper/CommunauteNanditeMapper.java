package sn.dove.backend.service.mapper;

import java.util.Set;
import java.util.stream.Collectors;
import org.mapstruct.*;
import sn.dove.backend.domain.CommunauteNandite;
import sn.dove.backend.domain.Utilisateur;
import sn.dove.backend.service.dto.CommunauteNanditeDTO;
import sn.dove.backend.service.dto.UtilisateurDTO;

/**
 * Mapper for the entity {@link CommunauteNandite} and its DTO {@link CommunauteNanditeDTO}.
 */
@Mapper(componentModel = "spring")
public interface CommunauteNanditeMapper extends EntityMapper<CommunauteNanditeDTO, CommunauteNandite> {
    @Mapping(target = "membreses", source = "membreses", qualifiedByName = "utilisateurLoginSet")
    CommunauteNanditeDTO toDto(CommunauteNandite s);

    @Mapping(target = "removeMembres", ignore = true)
    CommunauteNandite toEntity(CommunauteNanditeDTO communauteNanditeDTO);

    @Named("utilisateurLogin")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    @Mapping(target = "login", source = "login")
    UtilisateurDTO toDtoUtilisateurLogin(Utilisateur utilisateur);

    @Named("utilisateurLoginSet")
    default Set<UtilisateurDTO> toDtoUtilisateurLoginSet(Set<Utilisateur> utilisateur) {
        return utilisateur.stream().map(this::toDtoUtilisateurLogin).collect(Collectors.toSet());
    }
}
