package sn.dove.backend.service;

import java.util.Optional;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sn.dove.backend.domain.Metier;
import sn.dove.backend.repository.MetierRepository;
import sn.dove.backend.service.dto.MetierDTO;
import sn.dove.backend.service.mapper.MetierMapper;

/**
 * Service Implementation for managing {@link sn.dove.backend.domain.Metier}.
 */
@Service
@Transactional
public class MetierService {

    private static final Logger LOG = LoggerFactory.getLogger(MetierService.class);

    private final MetierRepository metierRepository;

    private final MetierMapper metierMapper;

    public MetierService(MetierRepository metierRepository, MetierMapper metierMapper) {
        this.metierRepository = metierRepository;
        this.metierMapper = metierMapper;
    }

    /**
     * Save a metier.
     *
     * @param metierDTO the entity to save.
     * @return the persisted entity.
     */
    public MetierDTO save(MetierDTO metierDTO) {
        LOG.debug("Request to save Metier : {}", metierDTO);
        Metier metier = metierMapper.toEntity(metierDTO);
        metier = metierRepository.save(metier);
        return metierMapper.toDto(metier);
    }

    /**
     * Update a metier.
     *
     * @param metierDTO the entity to save.
     * @return the persisted entity.
     */
    public MetierDTO update(MetierDTO metierDTO) {
        LOG.debug("Request to update Metier : {}", metierDTO);
        Metier metier = metierMapper.toEntity(metierDTO);
        metier = metierRepository.save(metier);
        return metierMapper.toDto(metier);
    }

    /**
     * Partially update a metier.
     *
     * @param metierDTO the entity to update partially.
     * @return the persisted entity.
     */
    public Optional<MetierDTO> partialUpdate(MetierDTO metierDTO) {
        LOG.debug("Request to partially update Metier : {}", metierDTO);

        return metierRepository
            .findById(metierDTO.getId())
            .map(existingMetier -> {
                metierMapper.partialUpdate(existingMetier, metierDTO);

                return existingMetier;
            })
            .map(metierRepository::save)
            .map(metierMapper::toDto);
    }

    /**
     * Get one metier by id.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    @Transactional(readOnly = true)
    public Optional<MetierDTO> findOne(UUID id) {
        LOG.debug("Request to get Metier : {}", id);
        return metierRepository.findById(id).map(metierMapper::toDto);
    }

    /**
     * Delete the metier by id.
     *
     * @param id the id of the entity.
     */
    public void delete(UUID id) {
        LOG.debug("Request to delete Metier : {}", id);
        metierRepository.deleteById(id);
    }
}
