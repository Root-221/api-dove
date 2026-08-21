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
import sn.dove.backend.domain.FAQ;
import sn.dove.backend.repository.FAQRepository;
import sn.dove.backend.service.criteria.FAQCriteria;
import sn.dove.backend.service.dto.FAQDTO;
import sn.dove.backend.service.mapper.FAQMapper;
import tech.jhipster.service.QueryService;

/**
 * Service for executing complex queries for {@link FAQ} entities in the database.
 * The main input is a {@link FAQCriteria} which gets converted to {@link Specification},
 * in a way that all the filters must apply.
 * It returns a {@link Page} of {@link FAQDTO} which fulfills the criteria.
 */
@Service
@Transactional(readOnly = true)
public class FAQQueryService extends QueryService<FAQ> {

    private static final Logger LOG = LoggerFactory.getLogger(FAQQueryService.class);

    private final FAQRepository fAQRepository;

    private final FAQMapper fAQMapper;

    public FAQQueryService(FAQRepository fAQRepository, FAQMapper fAQMapper) {
        this.fAQRepository = fAQRepository;
        this.fAQMapper = fAQMapper;
    }

    /**
     * Return a {@link Page} of {@link FAQDTO} which matches the criteria from the database.
     * @param criteria The object which holds all the filters, which the entities should match.
     * @param page The page, which should be returned.
     * @return the matching entities.
     */
    @Transactional(readOnly = true)
    public Page<FAQDTO> findByCriteria(FAQCriteria criteria, Pageable page) {
        LOG.debug("find by criteria : {}, page: {}", criteria, page);
        final Specification<FAQ> specification = createSpecification(criteria);
        return fAQRepository.findAll(specification, page).map(fAQMapper::toDto);
    }

    /**
     * Return the number of matching entities in the database.
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the number of matching entities.
     */
    @Transactional(readOnly = true)
    public long countByCriteria(FAQCriteria criteria) {
        LOG.debug("count by criteria : {}", criteria);
        final Specification<FAQ> specification = createSpecification(criteria);
        return fAQRepository.count(specification);
    }

    /**
     * Function to convert {@link FAQCriteria} to a {@link Specification}
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the matching {@link Specification} of the entity.
     */
    protected Specification<FAQ> createSpecification(FAQCriteria criteria) {
        Specification<FAQ> specification = Specification.unrestricted();
        specification = specification.and((root, query, builder) -> {
            if (Long.class != query.getResultType()) {
                root.fetch(FAQ_.contenu, JoinType.LEFT);
            }
            return null;
        });
        if (criteria != null) {
            // This has to be called first, because the distinct method returns null
            specification = specification.and(
                Specification.allOf(
                    Boolean.TRUE.equals(criteria.getDistinct()) ? distinct(criteria.getDistinct()) : Specification.unrestricted(),
                    buildSpecification(criteria.getId(), FAQ_.id),
                    buildStringSpecification(criteria.getTitre(), FAQ_.titre),
                    buildSpecification(criteria.getContenuId(), root -> root.join(FAQ_.contenu, JoinType.LEFT).get(Contenu_.id))
                )
            );
        }
        return specification;
    }
}
