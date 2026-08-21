package sn.dove.backend.service;

import java.util.Optional;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sn.dove.backend.domain.FAQ;
import sn.dove.backend.repository.FAQRepository;
import sn.dove.backend.service.dto.FAQDTO;
import sn.dove.backend.service.mapper.FAQMapper;

/**
 * Service Implementation for managing {@link sn.dove.backend.domain.FAQ}.
 */
@Service
@Transactional
public class FAQService {

    private static final Logger LOG = LoggerFactory.getLogger(FAQService.class);

    private final FAQRepository fAQRepository;

    private final FAQMapper fAQMapper;

    public FAQService(FAQRepository fAQRepository, FAQMapper fAQMapper) {
        this.fAQRepository = fAQRepository;
        this.fAQMapper = fAQMapper;
    }

    /**
     * Save a fAQ.
     *
     * @param fAQDTO the entity to save.
     * @return the persisted entity.
     */
    public FAQDTO save(FAQDTO fAQDTO) {
        LOG.debug("Request to save FAQ : {}", fAQDTO);
        FAQ fAQ = fAQMapper.toEntity(fAQDTO);
        fAQ = fAQRepository.save(fAQ);
        return fAQMapper.toDto(fAQ);
    }

    /**
     * Update a fAQ.
     *
     * @param fAQDTO the entity to save.
     * @return the persisted entity.
     */
    public FAQDTO update(FAQDTO fAQDTO) {
        LOG.debug("Request to update FAQ : {}", fAQDTO);
        FAQ fAQ = fAQMapper.toEntity(fAQDTO);
        fAQ = fAQRepository.save(fAQ);
        return fAQMapper.toDto(fAQ);
    }

    /**
     * Partially update a fAQ.
     *
     * @param fAQDTO the entity to update partially.
     * @return the persisted entity.
     */
    public Optional<FAQDTO> partialUpdate(FAQDTO fAQDTO) {
        LOG.debug("Request to partially update FAQ : {}", fAQDTO);

        return fAQRepository
            .findById(fAQDTO.getId())
            .map(existingFAQ -> {
                fAQMapper.partialUpdate(existingFAQ, fAQDTO);

                return existingFAQ;
            })
            .map(fAQRepository::save)
            .map(fAQMapper::toDto);
    }

    /**
     * Get all the fAQS with eager load of many-to-many relationships.
     *
     * @return the list of entities.
     */
    public Page<FAQDTO> findAllWithEagerRelationships(Pageable pageable) {
        return fAQRepository.findAllWithEagerRelationships(pageable).map(fAQMapper::toDto);
    }

    /**
     * Get one fAQ by id.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    @Transactional(readOnly = true)
    public Optional<FAQDTO> findOne(UUID id) {
        LOG.debug("Request to get FAQ : {}", id);
        return fAQRepository.findOneWithEagerRelationships(id).map(fAQMapper::toDto);
    }

    /**
     * Delete the fAQ by id.
     *
     * @param id the id of the entity.
     */
    public void delete(UUID id) {
        LOG.debug("Request to delete FAQ : {}", id);
        fAQRepository.deleteById(id);
    }
}
