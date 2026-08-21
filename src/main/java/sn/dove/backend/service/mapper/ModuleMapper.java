package sn.dove.backend.service.mapper;

import java.util.Objects;
import java.util.UUID;
import org.mapstruct.*;
import sn.dove.backend.domain.Application;
import sn.dove.backend.domain.Module;
import sn.dove.backend.service.dto.ApplicationDTO;
import sn.dove.backend.service.dto.ModuleDTO;

/**
 * Mapper for the entity {@link Module} and its DTO {@link ModuleDTO}.
 */
@Mapper(componentModel = "spring")
public interface ModuleMapper extends EntityMapper<ModuleDTO, Module> {
    @Mapping(target = "application", source = "application", qualifiedByName = "applicationId")
    ModuleDTO toDto(Module s);

    @Named("applicationId")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    ApplicationDTO toDtoApplicationId(Application application);

    default String map(UUID value) {
        return Objects.toString(value, null);
    }
}
