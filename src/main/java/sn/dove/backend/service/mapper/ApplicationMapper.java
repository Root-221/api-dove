package sn.dove.backend.service.mapper;

import org.mapstruct.*;
import sn.dove.backend.domain.Application;
import sn.dove.backend.service.dto.ApplicationDTO;

/**
 * Mapper for the entity {@link Application} and its DTO {@link ApplicationDTO}.
 */
@Mapper(componentModel = "spring")
public interface ApplicationMapper extends EntityMapper<ApplicationDTO, Application> {}
