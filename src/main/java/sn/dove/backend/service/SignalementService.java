package sn.dove.backend.service;

import java.util.Optional;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sn.dove.backend.domain.Signalement;
import sn.dove.backend.repository.SignalementRepository;
import sn.dove.backend.service.dto.SignalementDTO;
import sn.dove.backend.service.mapper.SignalementMapper;

/**
 * Service Implementation for managing {@link sn.dove.backend.domain.Signalement}.
 */
@Service
@Transactional
public class SignalementService {

    private static final Logger LOG = LoggerFactory.getLogger(SignalementService.class);

    private final SignalementRepository signalementRepository;

    private final SignalementMapper signalementMapper;

    public SignalementService(SignalementRepository signalementRepository, SignalementMapper signalementMapper) {
        this.signalementRepository = signalementRepository;
        this.signalementMapper = signalementMapper;
    }

    /**
     * Save a signalement.
     *
     * @param signalementDTO the entity to save.
     * @return the persisted entity.
     */
    public SignalementDTO save(SignalementDTO signalementDTO) {
        LOG.debug("Request to save Signalement : {}", signalementDTO);
        Signalement signalement = signalementMapper.toEntity(signalementDTO);
        signalement = signalementRepository.save(signalement);
        return signalementMapper.toDto(signalement);
    }

    /**
     * Update a signalement.
     *
     * @param signalementDTO the entity to save.
     * @return the persisted entity.
     */
    public SignalementDTO update(SignalementDTO signalementDTO) {
        LOG.debug("Request to update Signalement : {}", signalementDTO);
        Signalement signalement = signalementMapper.toEntity(signalementDTO);
        signalement = signalementRepository.save(signalement);
        return signalementMapper.toDto(signalement);
    }

    /**
     * Partially update a signalement.
     *
     * @param signalementDTO the entity to update partially.
     * @return the persisted entity.
     */
    public Optional<SignalementDTO> partialUpdate(SignalementDTO signalementDTO) {
        LOG.debug("Request to partially update Signalement : {}", signalementDTO);

        return signalementRepository
            .findById(signalementDTO.getId())
            .map(existingSignalement -> {
                signalementMapper.partialUpdate(existingSignalement, signalementDTO);

                return existingSignalement;
            })
            .map(signalementRepository::save)
            .map(signalementMapper::toDto);
    }

    /**
     * Get all the signalements with eager load of many-to-many relationships.
     *
     * @return the list of entities.
     */
    public Page<SignalementDTO> findAllWithEagerRelationships(Pageable pageable) {
        return signalementRepository.findAllWithEagerRelationships(pageable).map(signalementMapper::toDto);
    }

    /**
     * Get one signalement by id.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    @Transactional(readOnly = true)
    public Optional<SignalementDTO> findOne(UUID id) {
        LOG.debug("Request to get Signalement : {}", id);
        return signalementRepository.findOneWithEagerRelationships(id).map(signalementMapper::toDto);
    }

    /**
     * Delete the signalement by id.
     *
     * @param id the id of the entity.
     */
    public void delete(UUID id) {
        LOG.debug("Request to delete Signalement : {}", id);
        signalementRepository.deleteById(id);
    }
}
