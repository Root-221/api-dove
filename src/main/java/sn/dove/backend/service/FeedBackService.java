package sn.dove.backend.service;

import java.util.Optional;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sn.dove.backend.domain.FeedBack;
import sn.dove.backend.repository.FeedBackRepository;
import sn.dove.backend.service.dto.FeedBackDTO;
import sn.dove.backend.service.mapper.FeedBackMapper;

/**
 * Service Implementation for managing {@link sn.dove.backend.domain.FeedBack}.
 */
@Service
@Transactional
public class FeedBackService {

    private static final Logger LOG = LoggerFactory.getLogger(FeedBackService.class);

    private final FeedBackRepository feedBackRepository;

    private final FeedBackMapper feedBackMapper;

    public FeedBackService(FeedBackRepository feedBackRepository, FeedBackMapper feedBackMapper) {
        this.feedBackRepository = feedBackRepository;
        this.feedBackMapper = feedBackMapper;
    }

    /**
     * Save a feedBack.
     *
     * @param feedBackDTO the entity to save.
     * @return the persisted entity.
     */
    public FeedBackDTO save(FeedBackDTO feedBackDTO) {
        LOG.debug("Request to save FeedBack : {}", feedBackDTO);
        FeedBack feedBack = feedBackMapper.toEntity(feedBackDTO);
        feedBack = feedBackRepository.save(feedBack);
        return feedBackMapper.toDto(feedBack);
    }

    /**
     * Update a feedBack.
     *
     * @param feedBackDTO the entity to save.
     * @return the persisted entity.
     */
    public FeedBackDTO update(FeedBackDTO feedBackDTO) {
        LOG.debug("Request to update FeedBack : {}", feedBackDTO);
        FeedBack feedBack = feedBackMapper.toEntity(feedBackDTO);
        feedBack = feedBackRepository.save(feedBack);
        return feedBackMapper.toDto(feedBack);
    }

    /**
     * Partially update a feedBack.
     *
     * @param feedBackDTO the entity to update partially.
     * @return the persisted entity.
     */
    public Optional<FeedBackDTO> partialUpdate(FeedBackDTO feedBackDTO) {
        LOG.debug("Request to partially update FeedBack : {}", feedBackDTO);

        return feedBackRepository
            .findById(feedBackDTO.getId())
            .map(existingFeedBack -> {
                feedBackMapper.partialUpdate(existingFeedBack, feedBackDTO);

                return existingFeedBack;
            })
            .map(feedBackRepository::save)
            .map(feedBackMapper::toDto);
    }

    /**
     * Get all the feedBacks with eager load of many-to-many relationships.
     *
     * @return the list of entities.
     */
    public Page<FeedBackDTO> findAllWithEagerRelationships(Pageable pageable) {
        return feedBackRepository.findAllWithEagerRelationships(pageable).map(feedBackMapper::toDto);
    }

    /**
     * Get one feedBack by id.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    @Transactional(readOnly = true)
    public Optional<FeedBackDTO> findOne(UUID id) {
        LOG.debug("Request to get FeedBack : {}", id);
        return feedBackRepository.findOneWithEagerRelationships(id).map(feedBackMapper::toDto);
    }

    /**
     * Delete the feedBack by id.
     *
     * @param id the id of the entity.
     */
    public void delete(UUID id) {
        LOG.debug("Request to delete FeedBack : {}", id);
        feedBackRepository.deleteById(id);
    }
}
