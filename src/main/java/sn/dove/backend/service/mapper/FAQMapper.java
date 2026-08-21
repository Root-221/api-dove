package sn.dove.backend.service.mapper;

import org.mapstruct.*;
import sn.dove.backend.domain.Contenu;
import sn.dove.backend.domain.FAQ;
import sn.dove.backend.service.dto.ContenuDTO;
import sn.dove.backend.service.dto.FAQDTO;

/**
 * Mapper for the entity {@link FAQ} and its DTO {@link FAQDTO}.
 */
@Mapper(componentModel = "spring")
public interface FAQMapper extends EntityMapper<FAQDTO, FAQ> {
    @Mapping(target = "contenu", source = "contenu", qualifiedByName = "contenuTitre")
    FAQDTO toDto(FAQ s);

    @Named("contenuTitre")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    @Mapping(target = "titre", source = "titre")
    ContenuDTO toDtoContenuTitre(Contenu contenu);
}
