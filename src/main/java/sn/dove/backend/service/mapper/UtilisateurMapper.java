package sn.dove.backend.service.mapper;

import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.mapstruct.*;
import sn.dove.backend.domain.CommunauteNandite;
import sn.dove.backend.domain.Utilisateur;
import sn.dove.backend.service.dto.CommunauteNanditeDTO;
import sn.dove.backend.service.dto.UtilisateurDTO;

/**
 * Mapper for the entity {@link Utilisateur} and its DTO {@link UtilisateurDTO}.
 */
@Mapper(componentModel = "spring")
public interface UtilisateurMapper extends EntityMapper<UtilisateurDTO, Utilisateur> {
    @Mapping(target = "communauteNandites", source = "communauteNandites", qualifiedByName = "communauteNanditeIdSet")
    UtilisateurDTO toDto(Utilisateur s);

    @Mapping(target = "communauteNandites", ignore = true)
    @Mapping(target = "removeCommunauteNandite", ignore = true)
    Utilisateur toEntity(UtilisateurDTO utilisateurDTO);

    @Named("communauteNanditeId")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    CommunauteNanditeDTO toDtoCommunauteNanditeId(CommunauteNandite communauteNandite);

    @Named("communauteNanditeIdSet")
    default Set<CommunauteNanditeDTO> toDtoCommunauteNanditeIdSet(Set<CommunauteNandite> communauteNandite) {
        return communauteNandite.stream().map(this::toDtoCommunauteNanditeId).collect(Collectors.toSet());
    }

    default String map(UUID value) {
        return Objects.toString(value, null);
    }
}
