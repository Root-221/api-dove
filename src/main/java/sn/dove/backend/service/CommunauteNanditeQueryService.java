package sn.dove.backend.service;

import jakarta.persistence.criteria.JoinType;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sn.dove.backend.domain.*; // for static metamodels
import sn.dove.backend.domain.CommunauteNandite;
import sn.dove.backend.repository.CommunauteNanditeRepository;
import sn.dove.backend.service.criteria.CommunauteNanditeCriteria;
import sn.dove.backend.service.dto.CommunauteNanditeDTO;
import sn.dove.backend.service.mapper.CommunauteNanditeMapper;
import tech.jhipster.service.QueryService;

/**
 * Service for executing complex queries for {@link CommunauteNandite} entities in the database.
 * The main input is a {@link CommunauteNanditeCriteria} which gets converted to {@link Specification},
 * in a way that all the filters must apply.
 * It returns a {@link List} of {@link CommunauteNanditeDTO} which fulfills the criteria.
 */
@Service
@Transactional(readOnly = true)
public class CommunauteNanditeQueryService extends QueryService<CommunauteNandite> {

    private static final Logger LOG = LoggerFactory.getLogger(CommunauteNanditeQueryService.class);

    private final CommunauteNanditeRepository communauteNanditeRepository;

    private final CommunauteNanditeMapper communauteNanditeMapper;

    public CommunauteNanditeQueryService(
        CommunauteNanditeRepository communauteNanditeRepository,
        CommunauteNanditeMapper communauteNanditeMapper
    ) {
        this.communauteNanditeRepository = communauteNanditeRepository;
        this.communauteNanditeMapper = communauteNanditeMapper;
    }

    /**
     * Return a {@link List} of {@link CommunauteNanditeDTO} which matches the criteria from the database.
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the matching entities.
     */
    @Transactional(readOnly = true)
    public List<CommunauteNanditeDTO> findByCriteria(CommunauteNanditeCriteria criteria) {
        LOG.debug("find by criteria : {}", criteria);
        final Specification<CommunauteNandite> specification = createSpecification(criteria);
        return communauteNanditeMapper.toDto(
            communauteNanditeRepository.fetchBagRelationships(communauteNanditeRepository.findAll(specification))
        );
    }

    /**
     * Return the number of matching entities in the database.
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the number of matching entities.
     */
    @Transactional(readOnly = true)
    public long countByCriteria(CommunauteNanditeCriteria criteria) {
        LOG.debug("count by criteria : {}", criteria);
        final Specification<CommunauteNandite> specification = createSpecification(criteria);
        return communauteNanditeRepository.count(specification);
    }

    /**
     * Function to convert {@link CommunauteNanditeCriteria} to a {@link Specification}
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the matching {@link Specification} of the entity.
     */
    protected Specification<CommunauteNandite> createSpecification(CommunauteNanditeCriteria criteria) {
        Specification<CommunauteNandite> specification = Specification.unrestricted();
        if (criteria != null) {
            // This has to be called first, because the distinct method returns null
            specification = specification.and(
                Specification.allOf(
                    Boolean.TRUE.equals(criteria.getDistinct()) ? distinct(criteria.getDistinct()) : Specification.unrestricted(),
                    buildSpecification(criteria.getId(), CommunauteNandite_.id),
                    buildStringSpecification(criteria.getNom(), CommunauteNandite_.nom),
                    buildSpecification(criteria.getDiscussionsId(), root ->
                        root.join(CommunauteNandite_.discussionses, JoinType.LEFT).get(Discussion_.id)
                    ),
                    buildSpecification(criteria.getMembresId(), root ->
                        root.join(CommunauteNandite_.membreses, JoinType.LEFT).get(Utilisateur_.id)
                    )
                )
            );
        }
        return specification;
    }
}
