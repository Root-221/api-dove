package sn.dove.backend.service.mapper;

import org.mapstruct.*;
import sn.dove.backend.domain.Role;
import sn.dove.backend.service.dto.RoleDTO;

/**
 * Mapper for the entity {@link Role} and its DTO {@link RoleDTO}.
 */
@Mapper(componentModel = "spring")
public interface RoleMapper extends EntityMapper<RoleDTO, Role> {}
