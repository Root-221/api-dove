package sn.dove.backend.service.mapper;

import org.mapstruct.*;
import sn.dove.backend.domain.Contenu;
import sn.dove.backend.domain.Media;
import sn.dove.backend.service.dto.ContenuDTO;
import sn.dove.backend.service.dto.MediaDTO;

/**
 * Mapper for the entity {@link Media} and its DTO {@link MediaDTO}.
 */
@Mapper(componentModel = "spring")
public interface MediaMapper extends EntityMapper<MediaDTO, Media> {
    @Mapping(target = "contenu", source = "contenu", qualifiedByName = "contenuTitre")
    MediaDTO toDto(Media s);

    @Named("contenuTitre")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    @Mapping(target = "titre", source = "titre")
    ContenuDTO toDtoContenuTitre(Contenu contenu);
}
