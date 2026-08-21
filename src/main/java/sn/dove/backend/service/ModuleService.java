package sn.dove.backend.service;

import java.util.Optional;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sn.dove.backend.domain.Module;
import sn.dove.backend.repository.ModuleRepository;
import sn.dove.backend.service.dto.ModuleDTO;
import sn.dove.backend.service.mapper.ModuleMapper;

/**
 * Service Implementation for managing {@link sn.dove.backend.domain.Module}.
 */
@Service
@Transactional
public class ModuleService {

    private static final Logger LOG = LoggerFactory.getLogger(ModuleService.class);

    private final ModuleRepository moduleRepository;

    private final ModuleMapper moduleMapper;

    public ModuleService(ModuleRepository moduleRepository, ModuleMapper moduleMapper) {
        this.moduleRepository = moduleRepository;
        this.moduleMapper = moduleMapper;
    }

    /**
     * Save a module.
     *
     * @param moduleDTO the entity to save.
     * @return the persisted entity.
     */
    public ModuleDTO save(ModuleDTO moduleDTO) {
        LOG.debug("Request to save Module : {}", moduleDTO);
        Module module = moduleMapper.toEntity(moduleDTO);
        module = moduleRepository.save(module);
        return moduleMapper.toDto(module);
    }

    /**
     * Update a module.
     *
     * @param moduleDTO the entity to save.
     * @return the persisted entity.
     */
    public ModuleDTO update(ModuleDTO moduleDTO) {
        LOG.debug("Request to update Module : {}", moduleDTO);
        Module module = moduleMapper.toEntity(moduleDTO);
        module = moduleRepository.save(module);
        return moduleMapper.toDto(module);
    }

    /**
     * Partially update a module.
     *
     * @param moduleDTO the entity to update partially.
     * @return the persisted entity.
     */
    public Optional<ModuleDTO> partialUpdate(ModuleDTO moduleDTO) {
        LOG.debug("Request to partially update Module : {}", moduleDTO);

        return moduleRepository
            .findById(moduleDTO.getId())
            .map(existingModule -> {
                moduleMapper.partialUpdate(existingModule, moduleDTO);

                return existingModule;
            })
            .map(moduleRepository::save)
            .map(moduleMapper::toDto);
    }

    /**
     * Get one module by id.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    @Transactional(readOnly = true)
    public Optional<ModuleDTO> findOne(UUID id) {
        LOG.debug("Request to get Module : {}", id);
        return moduleRepository.findById(id).map(moduleMapper::toDto);
    }

    /**
     * Delete the module by id.
     *
     * @param id the id of the entity.
     */
    public void delete(UUID id) {
        LOG.debug("Request to delete Module : {}", id);
        moduleRepository.deleteById(id);
    }
}
