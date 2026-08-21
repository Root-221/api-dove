package sn.dove.backend.service.mapper;

import java.util.Objects;
import java.util.UUID;
import org.mapstruct.*;
import sn.dove.backend.domain.Message;
import sn.dove.backend.domain.SignalementMessage;
import sn.dove.backend.domain.Utilisateur;
import sn.dove.backend.service.dto.MessageDTO;
import sn.dove.backend.service.dto.SignalementMessageDTO;
import sn.dove.backend.service.dto.UtilisateurDTO;

/**
 * Mapper for the entity {@link SignalementMessage} and its DTO {@link SignalementMessageDTO}.
 */
@Mapper(componentModel = "spring")
public interface SignalementMessageMapper extends EntityMapper<SignalementMessageDTO, SignalementMessage> {
    @Mapping(target = "utilisateur", source = "utilisateur", qualifiedByName = "utilisateurLogin")
    @Mapping(target = "message", source = "message", qualifiedByName = "messageId")
    SignalementMessageDTO toDto(SignalementMessage s);

    @Named("utilisateurLogin")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    @Mapping(target = "login", source = "login")
    UtilisateurDTO toDtoUtilisateurLogin(Utilisateur utilisateur);

    @Named("messageId")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    MessageDTO toDtoMessageId(Message message);

    default String map(UUID value) {
        return Objects.toString(value, null);
    }
}
