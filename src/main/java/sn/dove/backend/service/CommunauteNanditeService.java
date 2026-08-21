package sn.dove.backend.service;

import java.util.Optional;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sn.dove.backend.domain.CommunauteNandite;
import sn.dove.backend.repository.CommunauteNanditeRepository;
import sn.dove.backend.service.dto.CommunauteNanditeDTO;
import sn.dove.backend.service.mapper.CommunauteNanditeMapper;

/**
 * Service Implementation for managing {@link sn.dove.backend.domain.CommunauteNandite}.
 */
@Service
@Transactional
public class CommunauteNanditeService {

    private static final Logger LOG = LoggerFactory.getLogger(CommunauteNanditeService.class);

    private final CommunauteNanditeRepository communauteNanditeRepository;

    private final CommunauteNanditeMapper communauteNanditeMapper;

    public CommunauteNanditeService(
        CommunauteNanditeRepository communauteNanditeRepository,
        CommunauteNanditeMapper communauteNanditeMapper
    ) {
        this.communauteNanditeRepository = communauteNanditeRepository;
        this.communauteNanditeMapper = communauteNanditeMapper;
    }

    /**
     * Save a communauteNandite.
     *
     * @param communauteNanditeDTO the entity to save.
     * @return the persisted entity.
     */
    public CommunauteNanditeDTO save(CommunauteNanditeDTO communauteNanditeDTO) {
        LOG.debug("Request to save CommunauteNandite : {}", communauteNanditeDTO);
        CommunauteNandite communauteNandite = communauteNanditeMapper.toEntity(communauteNanditeDTO);
        communauteNandite = communauteNanditeRepository.save(communauteNandite);
        return communauteNanditeMapper.toDto(communauteNandite);
    }

    /**
     * Update a communauteNandite.
     *
     * @param communauteNanditeDTO the entity to save.
     * @return the persisted entity.
     */
    public CommunauteNanditeDTO update(CommunauteNanditeDTO communauteNanditeDTO) {
        LOG.debug("Request to update CommunauteNandite : {}", communauteNanditeDTO);
        CommunauteNandite communauteNandite = communauteNanditeMapper.toEntity(communauteNanditeDTO);
        communauteNandite = communauteNanditeRepository.save(communauteNandite);
        return communauteNanditeMapper.toDto(communauteNandite);
    }

    /**
     * Partially update a communauteNandite.
     *
     * @param communauteNanditeDTO the entity to update partially.
     * @return the persisted entity.
     */
    public Optional<CommunauteNanditeDTO> partialUpdate(CommunauteNanditeDTO communauteNanditeDTO) {
        LOG.debug("Request to partially update CommunauteNandite : {}", communauteNanditeDTO);

        return communauteNanditeRepository
            .findById(communauteNanditeDTO.getId())
            .map(existingCommunauteNandite -> {
                communauteNanditeMapper.partialUpdate(existingCommunauteNandite, communauteNanditeDTO);

                return existingCommunauteNandite;
            })
            .map(communauteNanditeRepository::save)
            .map(communauteNanditeMapper::toDto);
    }

    /**
     * Get all the communauteNandites with eager load of many-to-many relationships.
     *
     * @return the list of entities.
     */
    public Page<CommunauteNanditeDTO> findAllWithEagerRelationships(Pageable pageable) {
        return communauteNanditeRepository.findAllWithEagerRelationships(pageable).map(communauteNanditeMapper::toDto);
    }

    /**
     * Get one communauteNandite by id.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    @Transactional(readOnly = true)
    public Optional<CommunauteNanditeDTO> findOne(UUID id) {
        LOG.debug("Request to get CommunauteNandite : {}", id);
        return communauteNanditeRepository.findOneWithEagerRelationships(id).map(communauteNanditeMapper::toDto);
    }

    /**
     * Delete the communauteNandite by id.
     *
     * @param id the id of the entity.
     */
    public void delete(UUID id) {
        LOG.debug("Request to delete CommunauteNandite : {}", id);
        communauteNanditeRepository.deleteById(id);
    }
}
