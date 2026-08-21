package sn.dove.backend.service;

import java.util.Optional;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sn.dove.backend.domain.SignalementMessage;
import sn.dove.backend.repository.SignalementMessageRepository;
import sn.dove.backend.service.dto.SignalementMessageDTO;
import sn.dove.backend.service.mapper.SignalementMessageMapper;

/**
 * Service Implementation for managing {@link sn.dove.backend.domain.SignalementMessage}.
 */
@Service
@Transactional
public class SignalementMessageService {

    private static final Logger LOG = LoggerFactory.getLogger(SignalementMessageService.class);

    private final SignalementMessageRepository signalementMessageRepository;

    private final SignalementMessageMapper signalementMessageMapper;

    public SignalementMessageService(
        SignalementMessageRepository signalementMessageRepository,
        SignalementMessageMapper signalementMessageMapper
    ) {
        this.signalementMessageRepository = signalementMessageRepository;
        this.signalementMessageMapper = signalementMessageMapper;
    }

    /**
     * Save a signalementMessage.
     *
     * @param signalementMessageDTO the entity to save.
     * @return the persisted entity.
     */
    public SignalementMessageDTO save(SignalementMessageDTO signalementMessageDTO) {
        LOG.debug("Request to save SignalementMessage : {}", signalementMessageDTO);
        SignalementMessage signalementMessage = signalementMessageMapper.toEntity(signalementMessageDTO);
        signalementMessage = signalementMessageRepository.save(signalementMessage);
        return signalementMessageMapper.toDto(signalementMessage);
    }

    /**
     * Update a signalementMessage.
     *
     * @param signalementMessageDTO the entity to save.
     * @return the persisted entity.
     */
    public SignalementMessageDTO update(SignalementMessageDTO signalementMessageDTO) {
        LOG.debug("Request to update SignalementMessage : {}", signalementMessageDTO);
        SignalementMessage signalementMessage = signalementMessageMapper.toEntity(signalementMessageDTO);
        signalementMessage = signalementMessageRepository.save(signalementMessage);
        return signalementMessageMapper.toDto(signalementMessage);
    }

    /**
     * Partially update a signalementMessage.
     *
     * @param signalementMessageDTO the entity to update partially.
     * @return the persisted entity.
     */
    public Optional<SignalementMessageDTO> partialUpdate(SignalementMessageDTO signalementMessageDTO) {
        LOG.debug("Request to partially update SignalementMessage : {}", signalementMessageDTO);

        return signalementMessageRepository
            .findById(signalementMessageDTO.getId())
            .map(existingSignalementMessage -> {
                signalementMessageMapper.partialUpdate(existingSignalementMessage, signalementMessageDTO);

                return existingSignalementMessage;
            })
            .map(signalementMessageRepository::save)
            .map(signalementMessageMapper::toDto);
    }

    /**
     * Get all the signalementMessages with eager load of many-to-many relationships.
     *
     * @return the list of entities.
     */
    public Page<SignalementMessageDTO> findAllWithEagerRelationships(Pageable pageable) {
        return signalementMessageRepository.findAllWithEagerRelationships(pageable).map(signalementMessageMapper::toDto);
    }

    /**
     * Get one signalementMessage by id.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    @Transactional(readOnly = true)
    public Optional<SignalementMessageDTO> findOne(UUID id) {
        LOG.debug("Request to get SignalementMessage : {}", id);
        return signalementMessageRepository.findOneWithEagerRelationships(id).map(signalementMessageMapper::toDto);
    }

    /**
     * Delete the signalementMessage by id.
     *
     * @param id the id of the entity.
     */
    public void delete(UUID id) {
        LOG.debug("Request to delete SignalementMessage : {}", id);
        signalementMessageRepository.deleteById(id);
    }
}
