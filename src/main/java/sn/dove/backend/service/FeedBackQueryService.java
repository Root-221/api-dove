package sn.dove.backend.service;

import jakarta.persistence.criteria.JoinType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sn.dove.backend.domain.*; // for static metamodels
import sn.dove.backend.domain.FeedBack;
import sn.dove.backend.repository.FeedBackRepository;
import sn.dove.backend.service.criteria.FeedBackCriteria;
import sn.dove.backend.service.dto.FeedBackDTO;
import sn.dove.backend.service.mapper.FeedBackMapper;
import tech.jhipster.service.QueryService;

/**
 * Service for executing complex queries for {@link FeedBack} entities in the database.
 * The main input is a {@link FeedBackCriteria} which gets converted to {@link Specification},
 * in a way that all the filters must apply.
 * It returns a {@link Page} of {@link FeedBackDTO} which fulfills the criteria.
 */
@Service
@Transactional(readOnly = true)
public class FeedBackQueryService extends QueryService<FeedBack> {

    private static final Logger LOG = LoggerFactory.getLogger(FeedBackQueryService.class);

    private final FeedBackRepository feedBackRepository;

    private final FeedBackMapper feedBackMapper;

    public FeedBackQueryService(FeedBackRepository feedBackRepository, FeedBackMapper feedBackMapper) {
        this.feedBackRepository = feedBackRepository;
        this.feedBackMapper = feedBackMapper;
    }

    /**
     * Return a {@link Page} of {@link FeedBackDTO} which matches the criteria from the database.
     * @param criteria The object which holds all the filters, which the entities should match.
     * @param page The page, which should be returned.
     * @return the matching entities.
     */
    @Transactional(readOnly = true)
    public Page<FeedBackDTO> findByCriteria(FeedBackCriteria criteria, Pageable page) {
        LOG.debug("find by criteria : {}, page: {}", criteria, page);
        final Specification<FeedBack> specification = createSpecification(criteria);
        return feedBackRepository.findAll(specification, page).map(feedBackMapper::toDto);
    }

    /**
     * Return the number of matching entities in the database.
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the number of matching entities.
     */
    @Transactional(readOnly = true)
    public long countByCriteria(FeedBackCriteria criteria) {
        LOG.debug("count by criteria : {}", criteria);
        final Specification<FeedBack> specification = createSpecification(criteria);
        return feedBackRepository.count(specification);
    }

    /**
     * Function to convert {@link FeedBackCriteria} to a {@link Specification}
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the matching {@link Specification} of the entity.
     */
    protected Specification<FeedBack> createSpecification(FeedBackCriteria criteria) {
        Specification<FeedBack> specification = Specification.unrestricted();
        specification = specification.and((root, query, builder) -> {
            if (Long.class != query.getResultType()) {
                root.fetch(FeedBack_.utilisateur, JoinType.LEFT);
                root.fetch(FeedBack_.contenu, JoinType.LEFT);
            }
            return null;
        });
        if (criteria != null) {
            // This has to be called first, because the distinct method returns null
            specification = specification.and(
                Specification.allOf(
                    Boolean.TRUE.equals(criteria.getDistinct()) ? distinct(criteria.getDistinct()) : Specification.unrestricted(),
                    buildSpecification(criteria.getId(), FeedBack_.id),
                    buildRangeSpecification(criteria.getNote(), FeedBack_.note),
                    buildRangeSpecification(criteria.getDateCreation(), FeedBack_.dateCreation),
                    buildStringSpecification(criteria.getStatutTraitement(), FeedBack_.statutTraitement),
                    buildSpecification(criteria.getUtilisateurId(), root ->
                        root.join(FeedBack_.utilisateur, JoinType.LEFT).get(Utilisateur_.id)
                    ),
                    buildSpecification(criteria.getContenuId(), root -> root.join(FeedBack_.contenu, JoinType.LEFT).get(Contenu_.id))
                )
            );
        }
        return specification;
    }
}
