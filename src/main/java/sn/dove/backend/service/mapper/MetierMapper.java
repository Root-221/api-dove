package sn.dove.backend.service.mapper;

import org.mapstruct.*;
import sn.dove.backend.domain.Metier;
import sn.dove.backend.service.dto.MetierDTO;

/**
 * Mapper for the entity {@link Metier} and its DTO {@link MetierDTO}.
 */
@Mapper(componentModel = "spring")
public interface MetierMapper extends EntityMapper<MetierDTO, Metier> {}
