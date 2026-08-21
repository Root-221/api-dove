package sn.dove.backend.service;

import java.util.Optional;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sn.dove.backend.domain.Discussion;
import sn.dove.backend.repository.DiscussionRepository;
import sn.dove.backend.service.dto.DiscussionDTO;
import sn.dove.backend.service.mapper.DiscussionMapper;

/**
 * Service Implementation for managing {@link sn.dove.backend.domain.Discussion}.
 */
@Service
@Transactional
public class DiscussionService {

    private static final Logger LOG = LoggerFactory.getLogger(DiscussionService.class);

    private final DiscussionRepository discussionRepository;

    private final DiscussionMapper discussionMapper;

    public DiscussionService(DiscussionRepository discussionRepository, DiscussionMapper discussionMapper) {
        this.discussionRepository = discussionRepository;
        this.discussionMapper = discussionMapper;
    }

    /**
     * Save a discussion.
     *
     * @param discussionDTO the entity to save.
     * @return the persisted entity.
     */
    public DiscussionDTO save(DiscussionDTO discussionDTO) {
        LOG.debug("Request to save Discussion : {}", discussionDTO);
        Discussion discussion = discussionMapper.toEntity(discussionDTO);
        discussion = discussionRepository.save(discussion);
        return discussionMapper.toDto(discussion);
    }

    /**
     * Update a discussion.
     *
     * @param discussionDTO the entity to save.
     * @return the persisted entity.
     */
    public DiscussionDTO update(DiscussionDTO discussionDTO) {
        LOG.debug("Request to update Discussion : {}", discussionDTO);
        Discussion discussion = discussionMapper.toEntity(discussionDTO);
        discussion = discussionRepository.save(discussion);
        return discussionMapper.toDto(discussion);
    }

    /**
     * Partially update a discussion.
     *
     * @param discussionDTO the entity to update partially.
     * @return the persisted entity.
     */
    public Optional<DiscussionDTO> partialUpdate(DiscussionDTO discussionDTO) {
        LOG.debug("Request to partially update Discussion : {}", discussionDTO);

        return discussionRepository
            .findById(discussionDTO.getId())
            .map(existingDiscussion -> {
                discussionMapper.partialUpdate(existingDiscussion, discussionDTO);

                return existingDiscussion;
            })
            .map(discussionRepository::save)
            .map(discussionMapper::toDto);
    }

    /**
     * Get all the discussions with eager load of many-to-many relationships.
     *
     * @return the list of entities.
     */
    public Page<DiscussionDTO> findAllWithEagerRelationships(Pageable pageable) {
        return discussionRepository.findAllWithEagerRelationships(pageable).map(discussionMapper::toDto);
    }

    /**
     * Get one discussion by id.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    @Transactional(readOnly = true)
    public Optional<DiscussionDTO> findOne(UUID id) {
        LOG.debug("Request to get Discussion : {}", id);
        return discussionRepository.findOneWithEagerRelationships(id).map(discussionMapper::toDto);
    }

    /**
     * Delete the discussion by id.
     *
     * @param id the id of the entity.
     */
    public void delete(UUID id) {
        LOG.debug("Request to delete Discussion : {}", id);
        discussionRepository.deleteById(id);
    }
}
