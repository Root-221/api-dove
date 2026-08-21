package sn.dove.backend.service;

import java.util.Optional;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sn.dove.backend.domain.UtilisateurRole;
import sn.dove.backend.repository.UtilisateurRoleRepository;
import sn.dove.backend.service.dto.UtilisateurRoleDTO;
import sn.dove.backend.service.mapper.UtilisateurRoleMapper;

/**
 * Service Implementation for managing {@link sn.dove.backend.domain.UtilisateurRole}.
 */
@Service
@Transactional
public class UtilisateurRoleService {

    private static final Logger LOG = LoggerFactory.getLogger(UtilisateurRoleService.class);

    private final UtilisateurRoleRepository utilisateurRoleRepository;

    private final UtilisateurRoleMapper utilisateurRoleMapper;

    public UtilisateurRoleService(UtilisateurRoleRepository utilisateurRoleRepository, UtilisateurRoleMapper utilisateurRoleMapper) {
        this.utilisateurRoleRepository = utilisateurRoleRepository;
        this.utilisateurRoleMapper = utilisateurRoleMapper;
    }

    /**
     * Save a utilisateurRole.
     *
     * @param utilisateurRoleDTO the entity to save.
     * @return the persisted entity.
     */
    public UtilisateurRoleDTO save(UtilisateurRoleDTO utilisateurRoleDTO) {
        LOG.debug("Request to save UtilisateurRole : {}", utilisateurRoleDTO);
        UtilisateurRole utilisateurRole = utilisateurRoleMapper.toEntity(utilisateurRoleDTO);
        utilisateurRole = utilisateurRoleRepository.save(utilisateurRole);
        return utilisateurRoleMapper.toDto(utilisateurRole);
    }

    /**
     * Update a utilisateurRole.
     *
     * @param utilisateurRoleDTO the entity to save.
     * @return the persisted entity.
     */
    public UtilisateurRoleDTO update(UtilisateurRoleDTO utilisateurRoleDTO) {
        LOG.debug("Request to update UtilisateurRole : {}", utilisateurRoleDTO);
        UtilisateurRole utilisateurRole = utilisateurRoleMapper.toEntity(utilisateurRoleDTO);
        utilisateurRole = utilisateurRoleRepository.save(utilisateurRole);
        return utilisateurRoleMapper.toDto(utilisateurRole);
    }

    /**
     * Partially update a utilisateurRole.
     *
     * @param utilisateurRoleDTO the entity to update partially.
     * @return the persisted entity.
     */
    public Optional<UtilisateurRoleDTO> partialUpdate(UtilisateurRoleDTO utilisateurRoleDTO) {
        LOG.debug("Request to partially update UtilisateurRole : {}", utilisateurRoleDTO);

        return utilisateurRoleRepository
            .findById(utilisateurRoleDTO.getId())
            .map(existingUtilisateurRole -> {
                utilisateurRoleMapper.partialUpdate(existingUtilisateurRole, utilisateurRoleDTO);

                return existingUtilisateurRole;
            })
            .map(utilisateurRoleRepository::save)
            .map(utilisateurRoleMapper::toDto);
    }

    /**
     * Get all the utilisateurRoles with eager load of many-to-many relationships.
     *
     * @return the list of entities.
     */
    public Page<UtilisateurRoleDTO> findAllWithEagerRelationships(Pageable pageable) {
        return utilisateurRoleRepository.findAllWithEagerRelationships(pageable).map(utilisateurRoleMapper::toDto);
    }

    /**
     * Get one utilisateurRole by id.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    @Transactional(readOnly = true)
    public Optional<UtilisateurRoleDTO> findOne(UUID id) {
        LOG.debug("Request to get UtilisateurRole : {}", id);
        return utilisateurRoleRepository.findOneWithEagerRelationships(id).map(utilisateurRoleMapper::toDto);
    }

    /**
     * Delete the utilisateurRole by id.
     *
     * @param id the id of the entity.
     */
    public void delete(UUID id) {
        LOG.debug("Request to delete UtilisateurRole : {}", id);
        utilisateurRoleRepository.deleteById(id);
    }
}
