package sn.dove.backend.service;

import java.util.Optional;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sn.dove.backend.domain.Application;
import sn.dove.backend.repository.ApplicationRepository;
import sn.dove.backend.service.dto.ApplicationDTO;
import sn.dove.backend.service.mapper.ApplicationMapper;

/**
 * Service Implementation for managing {@link sn.dove.backend.domain.Application}.
 */
@Service
@Transactional
public class ApplicationService {

    private static final Logger LOG = LoggerFactory.getLogger(ApplicationService.class);

    private final ApplicationRepository applicationRepository;

    private final ApplicationMapper applicationMapper;

    public ApplicationService(ApplicationRepository applicationRepository, ApplicationMapper applicationMapper) {
        this.applicationRepository = applicationRepository;
        this.applicationMapper = applicationMapper;
    }

    /**
     * Save a application.
     *
     * @param applicationDTO the entity to save.
     * @return the persisted entity.
     */
    public ApplicationDTO save(ApplicationDTO applicationDTO) {
        LOG.debug("Request to save Application : {}", applicationDTO);
        Application application = applicationMapper.toEntity(applicationDTO);
        application = applicationRepository.save(application);
        return applicationMapper.toDto(application);
    }

    /**
     * Update a application.
     *
     * @param applicationDTO the entity to save.
     * @return the persisted entity.
     */
    public ApplicationDTO update(ApplicationDTO applicationDTO) {
        LOG.debug("Request to update Application : {}", applicationDTO);
        Application application = applicationMapper.toEntity(applicationDTO);
        application = applicationRepository.save(application);
        return applicationMapper.toDto(application);
    }

    /**
     * Partially update a application.
     *
     * @param applicationDTO the entity to update partially.
     * @return the persisted entity.
     */
    public Optional<ApplicationDTO> partialUpdate(ApplicationDTO applicationDTO) {
        LOG.debug("Request to partially update Application : {}", applicationDTO);

        return applicationRepository
            .findById(applicationDTO.getId())
            .map(existingApplication -> {
                applicationMapper.partialUpdate(existingApplication, applicationDTO);

                return existingApplication;
            })
            .map(applicationRepository::save)
            .map(applicationMapper::toDto);
    }

    /**
     * Get one application by id.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    @Transactional(readOnly = true)
    public Optional<ApplicationDTO> findOne(UUID id) {
        LOG.debug("Request to get Application : {}", id);
        return applicationRepository.findById(id).map(applicationMapper::toDto);
    }

    /**
     * Delete the application by id.
     *
     * @param id the id of the entity.
     */
    public void delete(UUID id) {
        LOG.debug("Request to delete Application : {}", id);
        applicationRepository.deleteById(id);
    }
}
