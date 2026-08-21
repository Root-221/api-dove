package sn.dove.backend.service;

import java.util.Optional;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sn.dove.backend.domain.Reaction;
import sn.dove.backend.repository.ReactionRepository;
import sn.dove.backend.service.dto.ReactionDTO;
import sn.dove.backend.service.mapper.ReactionMapper;

/**
 * Service Implementation for managing {@link sn.dove.backend.domain.Reaction}.
 */
@Service
@Transactional
public class ReactionService {

    private static final Logger LOG = LoggerFactory.getLogger(ReactionService.class);

    private final ReactionRepository reactionRepository;

    private final ReactionMapper reactionMapper;

    public ReactionService(ReactionRepository reactionRepository, ReactionMapper reactionMapper) {
        this.reactionRepository = reactionRepository;
        this.reactionMapper = reactionMapper;
    }

    /**
     * Save a reaction.
     *
     * @param reactionDTO the entity to save.
     * @return the persisted entity.
     */
    public ReactionDTO save(ReactionDTO reactionDTO) {
        LOG.debug("Request to save Reaction : {}", reactionDTO);
        Reaction reaction = reactionMapper.toEntity(reactionDTO);
        reaction = reactionRepository.save(reaction);
        return reactionMapper.toDto(reaction);
    }

    /**
     * Update a reaction.
     *
     * @param reactionDTO the entity to save.
     * @return the persisted entity.
     */
    public ReactionDTO update(ReactionDTO reactionDTO) {
        LOG.debug("Request to update Reaction : {}", reactionDTO);
        Reaction reaction = reactionMapper.toEntity(reactionDTO);
        reaction = reactionRepository.save(reaction);
        return reactionMapper.toDto(reaction);
    }

    /**
     * Partially update a reaction.
     *
     * @param reactionDTO the entity to update partially.
     * @return the persisted entity.
     */
    public Optional<ReactionDTO> partialUpdate(ReactionDTO reactionDTO) {
        LOG.debug("Request to partially update Reaction : {}", reactionDTO);

        return reactionRepository
            .findById(reactionDTO.getId())
            .map(existingReaction -> {
                reactionMapper.partialUpdate(existingReaction, reactionDTO);

                return existingReaction;
            })
            .map(reactionRepository::save)
            .map(reactionMapper::toDto);
    }

    /**
     * Get all the reactions with eager load of many-to-many relationships.
     *
     * @return the list of entities.
     */
    public Page<ReactionDTO> findAllWithEagerRelationships(Pageable pageable) {
        return reactionRepository.findAllWithEagerRelationships(pageable).map(reactionMapper::toDto);
    }

    /**
     * Get one reaction by id.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    @Transactional(readOnly = true)
    public Optional<ReactionDTO> findOne(UUID id) {
        LOG.debug("Request to get Reaction : {}", id);
        return reactionRepository.findOneWithEagerRelationships(id).map(reactionMapper::toDto);
    }

    /**
     * Delete the reaction by id.
     *
     * @param id the id of the entity.
     */
    public void delete(UUID id) {
        LOG.debug("Request to delete Reaction : {}", id);
        reactionRepository.deleteById(id);
    }
}
