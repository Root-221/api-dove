package sn.dove.backend.service;

import jakarta.persistence.criteria.JoinType;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sn.dove.backend.domain.*; // for static metamodels
import sn.dove.backend.domain.Media;
import sn.dove.backend.repository.MediaRepository;
import sn.dove.backend.service.criteria.MediaCriteria;
import sn.dove.backend.service.dto.MediaDTO;
import sn.dove.backend.service.mapper.MediaMapper;
import tech.jhipster.service.QueryService;

/**
 * Service for executing complex queries for {@link Media} entities in the database.
 * The main input is a {@link MediaCriteria} which gets converted to {@link Specification},
 * in a way that all the filters must apply.
 * It returns a {@link List} of {@link MediaDTO} which fulfills the criteria.
 */
@Service
@Transactional(readOnly = true)
public class MediaQueryService extends QueryService<Media> {

    private static final Logger LOG = LoggerFactory.getLogger(MediaQueryService.class);

    private final MediaRepository mediaRepository;

    private final MediaMapper mediaMapper;

    public MediaQueryService(MediaRepository mediaRepository, MediaMapper mediaMapper) {
        this.mediaRepository = mediaRepository;
        this.mediaMapper = mediaMapper;
    }

    /**
     * Return a {@link List} of {@link MediaDTO} which matches the criteria from the database.
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the matching entities.
     */
    @Transactional(readOnly = true)
    public List<MediaDTO> findByCriteria(MediaCriteria criteria) {
        LOG.debug("find by criteria : {}", criteria);
        final Specification<Media> specification = createSpecification(criteria);
        return mediaMapper.toDto(mediaRepository.findAll(specification));
    }

    /**
     * Return the number of matching entities in the database.
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the number of matching entities.
     */
    @Transactional(readOnly = true)
    public long countByCriteria(MediaCriteria criteria) {
        LOG.debug("count by criteria : {}", criteria);
        final Specification<Media> specification = createSpecification(criteria);
        return mediaRepository.count(specification);
    }

    /**
     * Function to convert {@link MediaCriteria} to a {@link Specification}
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the matching {@link Specification} of the entity.
     */
    protected Specification<Media> createSpecification(MediaCriteria criteria) {
        Specification<Media> specification = Specification.unrestricted();
        specification = specification.and((root, query, builder) -> {
            if (Long.class != query.getResultType()) {
                root.fetch(Media_.contenu, JoinType.LEFT);
            }
            return null;
        });
        if (criteria != null) {
            // This has to be called first, because the distinct method returns null
            specification = specification.and(
                Specification.allOf(
                    Boolean.TRUE.equals(criteria.getDistinct()) ? distinct(criteria.getDistinct()) : Specification.unrestricted(),
                    buildSpecification(criteria.getId(), Media_.id),
                    buildStringSpecification(criteria.getTitre(), Media_.titre),
                    buildRangeSpecification(criteria.getEtape(), Media_.etape),
                    buildStringSpecification(criteria.getUrl(), Media_.url),
                    buildSpecification(criteria.getContenuId(), root -> root.join(Media_.contenu, JoinType.LEFT).get(Contenu_.id))
                )
            );
        }
        return specification;
    }
}
