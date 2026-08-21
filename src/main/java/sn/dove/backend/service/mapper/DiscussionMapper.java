package sn.dove.backend.service.mapper;

import java.util.Objects;
import java.util.UUID;
import org.mapstruct.*;
import sn.dove.backend.domain.CommunauteNandite;
import sn.dove.backend.domain.Discussion;
import sn.dove.backend.domain.Utilisateur;
import sn.dove.backend.service.dto.CommunauteNanditeDTO;
import sn.dove.backend.service.dto.DiscussionDTO;
import sn.dove.backend.service.dto.UtilisateurDTO;

/**
 * Mapper for the entity {@link Discussion} and its DTO {@link DiscussionDTO}.
 */
@Mapper(componentModel = "spring")
public interface DiscussionMapper extends EntityMapper<DiscussionDTO, Discussion> {
    @Mapping(target = "createur", source = "createur", qualifiedByName = "utilisateurLogin")
    @Mapping(target = "communauteNandite", source = "communauteNandite", qualifiedByName = "communauteNanditeId")
    DiscussionDTO toDto(Discussion s);

    @Named("utilisateurLogin")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    @Mapping(target = "login", source = "login")
    UtilisateurDTO toDtoUtilisateurLogin(Utilisateur utilisateur);

    @Named("communauteNanditeId")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    CommunauteNanditeDTO toDtoCommunauteNanditeId(CommunauteNandite communauteNandite);

    default String map(UUID value) {
        return Objects.toString(value, null);
    }
}
