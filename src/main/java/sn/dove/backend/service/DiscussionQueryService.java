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
import sn.dove.backend.domain.Discussion;
import sn.dove.backend.repository.DiscussionRepository;
import sn.dove.backend.service.criteria.DiscussionCriteria;
import sn.dove.backend.service.dto.DiscussionDTO;
import sn.dove.backend.service.mapper.DiscussionMapper;
import tech.jhipster.service.QueryService;

/**
 * Service for executing complex queries for {@link Discussion} entities in the database.
 * The main input is a {@link DiscussionCriteria} which gets converted to {@link Specification},
 * in a way that all the filters must apply.
 * It returns a {@link Page} of {@link DiscussionDTO} which fulfills the criteria.
 */
@Service
@Transactional(readOnly = true)
public class DiscussionQueryService extends QueryService<Discussion> {

    private static final Logger LOG = LoggerFactory.getLogger(DiscussionQueryService.class);

    private final DiscussionRepository discussionRepository;

    private final DiscussionMapper discussionMapper;

    public DiscussionQueryService(DiscussionRepository discussionRepository, DiscussionMapper discussionMapper) {
        this.discussionRepository = discussionRepository;
        this.discussionMapper = discussionMapper;
    }

    /**
     * Return a {@link Page} of {@link DiscussionDTO} which matches the criteria from the database.
     * @param criteria The object which holds all the filters, which the entities should match.
     * @param page The page, which should be returned.
     * @return the matching entities.
     */
    @Transactional(readOnly = true)
    public Page<DiscussionDTO> findByCriteria(DiscussionCriteria criteria, Pageable page) {
        LOG.debug("find by criteria : {}, page: {}", criteria, page);
        final Specification<Discussion> specification = createSpecification(criteria);
        return discussionRepository.findAll(specification, page).map(discussionMapper::toDto);
    }

    /**
     * Return the number of matching entities in the database.
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the number of matching entities.
     */
    @Transactional(readOnly = true)
    public long countByCriteria(DiscussionCriteria criteria) {
        LOG.debug("count by criteria : {}", criteria);
        final Specification<Discussion> specification = createSpecification(criteria);
        return discussionRepository.count(specification);
    }

    /**
     * Function to convert {@link DiscussionCriteria} to a {@link Specification}
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the matching {@link Specification} of the entity.
     */
    protected Specification<Discussion> createSpecification(DiscussionCriteria criteria) {
        Specification<Discussion> specification = Specification.unrestricted();
        specification = specification.and((root, query, builder) -> {
            if (Long.class != query.getResultType()) {
                root.fetch(Discussion_.createur, JoinType.LEFT);
                root.fetch(Discussion_.communauteNandite, JoinType.LEFT);
            }
            return null;
        });
        if (criteria != null) {
            // This has to be called first, because the distinct method returns null
            specification = specification.and(
                Specification.allOf(
                    Boolean.TRUE.equals(criteria.getDistinct()) ? distinct(criteria.getDistinct()) : Specification.unrestricted(),
                    buildSpecification(criteria.getId(), Discussion_.id),
                    buildStringSpecification(criteria.getTitre(), Discussion_.titre),
                    buildRangeSpecification(criteria.getDateCreation(), Discussion_.dateCreation),
                    buildSpecification(criteria.getMessagesId(), root -> root.join(Discussion_.messageses, JoinType.LEFT).get(Message_.id)),
                    buildSpecification(criteria.getCreateurId(), root ->
                        root.join(Discussion_.createur, JoinType.LEFT).get(Utilisateur_.id)
                    ),
                    buildSpecification(criteria.getCommunauteNanditeId(), root ->
                        root.join(Discussion_.communauteNandite, JoinType.LEFT).get(CommunauteNandite_.id)
                    )
                )
            );
        }
        return specification;
    }
}
