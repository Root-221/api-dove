package sn.dove.backend.service.mapper;

import org.mapstruct.*;
import sn.dove.backend.domain.Message;
import sn.dove.backend.domain.Reaction;
import sn.dove.backend.domain.Utilisateur;
import sn.dove.backend.service.dto.MessageDTO;
import sn.dove.backend.service.dto.ReactionDTO;
import sn.dove.backend.service.dto.UtilisateurDTO;

/**
 * Mapper for the entity {@link Reaction} and its DTO {@link ReactionDTO}.
 */
@Mapper(componentModel = "spring")
public interface ReactionMapper extends EntityMapper<ReactionDTO, Reaction> {
    @Mapping(target = "utilisateur", source = "utilisateur", qualifiedByName = "utilisateurLogin")
    @Mapping(target = "message", source = "message", qualifiedByName = "messageContenu")
    ReactionDTO toDto(Reaction s);

    @Named("utilisateurLogin")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    @Mapping(target = "login", source = "login")
    UtilisateurDTO toDtoUtilisateurLogin(Utilisateur utilisateur);

    @Named("messageContenu")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    @Mapping(target = "contenu", source = "contenu")
    MessageDTO toDtoMessageContenu(Message message);

    default String map(byte[] value) {
        return new String(value);
    }
}
