package sn.dove.backend.service;

import java.util.Optional;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sn.dove.backend.domain.Contenu;
import sn.dove.backend.repository.ContenuRepository;
import sn.dove.backend.service.dto.ContenuDTO;
import sn.dove.backend.service.mapper.ContenuMapper;

/**
 * Service Implementation for managing {@link sn.dove.backend.domain.Contenu}.
 */
@Service
@Transactional
public class ContenuService {

    private static final Logger LOG = LoggerFactory.getLogger(ContenuService.class);

    private final ContenuRepository contenuRepository;

    private final ContenuMapper contenuMapper;

    public ContenuService(ContenuRepository contenuRepository, ContenuMapper contenuMapper) {
        this.contenuRepository = contenuRepository;
        this.contenuMapper = contenuMapper;
    }

    /**
     * Save a contenu.
     *
     * @param contenuDTO the entity to save.
     * @return the persisted entity.
     */
    public ContenuDTO save(ContenuDTO contenuDTO) {
        LOG.debug("Request to save Contenu : {}", contenuDTO);
        Contenu contenu = contenuMapper.toEntity(contenuDTO);
        contenu = contenuRepository.save(contenu);
        return contenuMapper.toDto(contenu);
    }

    /**
     * Update a contenu.
     *
     * @param contenuDTO the entity to save.
     * @return the persisted entity.
     */
    public ContenuDTO update(ContenuDTO contenuDTO) {
        LOG.debug("Request to update Contenu : {}", contenuDTO);
        Contenu contenu = contenuMapper.toEntity(contenuDTO);
        contenu = contenuRepository.save(contenu);
        return contenuMapper.toDto(contenu);
    }

    /**
     * Partially update a contenu.
     *
     * @param contenuDTO the entity to update partially.
     * @return the persisted entity.
     */
    public Optional<ContenuDTO> partialUpdate(ContenuDTO contenuDTO) {
        LOG.debug("Request to partially update Contenu : {}", contenuDTO);

        return contenuRepository
            .findById(contenuDTO.getId())
            .map(existingContenu -> {
                contenuMapper.partialUpdate(existingContenu, contenuDTO);

                return existingContenu;
            })
            .map(contenuRepository::save)
            .map(contenuMapper::toDto);
    }

    /**
     * Get all the contenus with eager load of many-to-many relationships.
     *
     * @return the list of entities.
     */
    public Page<ContenuDTO> findAllWithEagerRelationships(Pageable pageable) {
        return contenuRepository.findAllWithEagerRelationships(pageable).map(contenuMapper::toDto);
    }

    /**
     * Get one contenu by id.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    @Transactional(readOnly = true)
    public Optional<ContenuDTO> findOne(UUID id) {
        LOG.debug("Request to get Contenu : {}", id);
        return contenuRepository.findOneWithEagerRelationships(id).map(contenuMapper::toDto);
    }

    /**
     * Delete the contenu by id.
     *
     * @param id the id of the entity.
     */
    public void delete(UUID id) {
        LOG.debug("Request to delete Contenu : {}", id);
        contenuRepository.deleteById(id);
    }
}
