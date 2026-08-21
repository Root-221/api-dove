package sn.dove.backend.service.mapper;

import java.util.Objects;
import java.util.UUID;
import org.mapstruct.*;
import sn.dove.backend.domain.Discussion;
import sn.dove.backend.domain.Message;
import sn.dove.backend.domain.Metier;
import sn.dove.backend.domain.Utilisateur;
import sn.dove.backend.service.dto.DiscussionDTO;
import sn.dove.backend.service.dto.MessageDTO;
import sn.dove.backend.service.dto.MetierDTO;
import sn.dove.backend.service.dto.UtilisateurDTO;

/**
 * Mapper for the entity {@link Message} and its DTO {@link MessageDTO}.
 */
@Mapper(componentModel = "spring")
public interface MessageMapper extends EntityMapper<MessageDTO, Message> {
    @Mapping(target = "emetteur", source = "emetteur", qualifiedByName = "utilisateurLogin")
    @Mapping(target = "metier", source = "metier", qualifiedByName = "metierLibelle")
    @Mapping(target = "discussion", source = "discussion", qualifiedByName = "discussionId")
    @Mapping(target = "messageParent", source = "messageParent", qualifiedByName = "messageId")
    MessageDTO toDto(Message s);

    @Named("messageId")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    MessageDTO toDtoMessageId(Message message);

    @Named("utilisateurLogin")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    @Mapping(target = "login", source = "login")
    UtilisateurDTO toDtoUtilisateurLogin(Utilisateur utilisateur);

    @Named("metierLibelle")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    @Mapping(target = "libelle", source = "libelle")
    MetierDTO toDtoMetierLibelle(Metier metier);

    @Named("discussionId")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    DiscussionDTO toDtoDiscussionId(Discussion discussion);

    default String map(UUID value) {
        return Objects.toString(value, null);
    }
}
