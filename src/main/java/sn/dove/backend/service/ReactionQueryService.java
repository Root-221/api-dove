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
import sn.dove.backend.domain.Reaction;
import sn.dove.backend.repository.ReactionRepository;
import sn.dove.backend.service.criteria.ReactionCriteria;
import sn.dove.backend.service.dto.ReactionDTO;
import sn.dove.backend.service.mapper.ReactionMapper;
import tech.jhipster.service.QueryService;

/**
 * Service for executing complex queries for {@link Reaction} entities in the database.
 * The main input is a {@link ReactionCriteria} which gets converted to {@link Specification},
 * in a way that all the filters must apply.
 * It returns a {@link Page} of {@link ReactionDTO} which fulfills the criteria.
 */
@Service
@Transactional(readOnly = true)
public class ReactionQueryService extends QueryService<Reaction> {

    private static final Logger LOG = LoggerFactory.getLogger(ReactionQueryService.class);

    private final ReactionRepository reactionRepository;

    private final ReactionMapper reactionMapper;

    public ReactionQueryService(ReactionRepository reactionRepository, ReactionMapper reactionMapper) {
        this.reactionRepository = reactionRepository;
        this.reactionMapper = reactionMapper;
    }

    /**
     * Return a {@link Page} of {@link ReactionDTO} which matches the criteria from the database.
     * @param criteria The object which holds all the filters, which the entities should match.
     * @param page The page, which should be returned.
     * @return the matching entities.
     */
    @Transactional(readOnly = true)
    public Page<ReactionDTO> findByCriteria(ReactionCriteria criteria, Pageable page) {
        LOG.debug("find by criteria : {}, page: {}", criteria, page);
        final Specification<Reaction> specification = createSpecification(criteria);
        return reactionRepository.findAll(specification, page).map(reactionMapper::toDto);
    }

    /**
     * Return the number of matching entities in the database.
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the number of matching entities.
     */
    @Transactional(readOnly = true)
    public long countByCriteria(ReactionCriteria criteria) {
        LOG.debug("count by criteria : {}", criteria);
        final Specification<Reaction> specification = createSpecification(criteria);
        return reactionRepository.count(specification);
    }

    /**
     * Function to convert {@link ReactionCriteria} to a {@link Specification}
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the matching {@link Specification} of the entity.
     */
    protected Specification<Reaction> createSpecification(ReactionCriteria criteria) {
        Specification<Reaction> specification = Specification.unrestricted();
        specification = specification.and((root, query, builder) -> {
            if (Long.class != query.getResultType()) {
                root.fetch(Reaction_.utilisateur, JoinType.LEFT);
                root.fetch(Reaction_.message, JoinType.LEFT);
            }
            return null;
        });
        if (criteria != null) {
            // This has to be called first, because the distinct method returns null
            specification = specification.and(
                Specification.allOf(
                    Boolean.TRUE.equals(criteria.getDistinct()) ? distinct(criteria.getDistinct()) : Specification.unrestricted(),
                    buildSpecification(criteria.getId(), Reaction_.id),
                    buildStringSpecification(criteria.getType(), Reaction_.type),
                    buildSpecification(criteria.getUtilisateurId(), root ->
                        root.join(Reaction_.utilisateur, JoinType.LEFT).get(Utilisateur_.id)
                    ),
                    buildSpecification(criteria.getMessageId(), root -> root.join(Reaction_.message, JoinType.LEFT).get(Message_.id))
                )
            );
        }
        return specification;
    }
}
