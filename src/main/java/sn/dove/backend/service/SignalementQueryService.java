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
import sn.dove.backend.domain.Signalement;
import sn.dove.backend.repository.SignalementRepository;
import sn.dove.backend.service.criteria.SignalementCriteria;
import sn.dove.backend.service.dto.SignalementDTO;
import sn.dove.backend.service.mapper.SignalementMapper;
import tech.jhipster.service.QueryService;

/**
 * Service for executing complex queries for {@link Signalement} entities in the database.
 * The main input is a {@link SignalementCriteria} which gets converted to {@link Specification},
 * in a way that all the filters must apply.
 * It returns a {@link Page} of {@link SignalementDTO} which fulfills the criteria.
 */
@Service
@Transactional(readOnly = true)
public class SignalementQueryService extends QueryService<Signalement> {

    private static final Logger LOG = LoggerFactory.getLogger(SignalementQueryService.class);

    private final SignalementRepository signalementRepository;

    private final SignalementMapper signalementMapper;

    public SignalementQueryService(SignalementRepository signalementRepository, SignalementMapper signalementMapper) {
        this.signalementRepository = signalementRepository;
        this.signalementMapper = signalementMapper;
    }

    /**
     * Return a {@link Page} of {@link SignalementDTO} which matches the criteria from the database.
     * @param criteria The object which holds all the filters, which the entities should match.
     * @param page The page, which should be returned.
     * @return the matching entities.
     */
    @Transactional(readOnly = true)
    public Page<SignalementDTO> findByCriteria(SignalementCriteria criteria, Pageable page) {
        LOG.debug("find by criteria : {}, page: {}", criteria, page);
        final Specification<Signalement> specification = createSpecification(criteria);
        return signalementRepository.findAll(specification, page).map(signalementMapper::toDto);
    }

    /**
     * Return the number of matching entities in the database.
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the number of matching entities.
     */
    @Transactional(readOnly = true)
    public long countByCriteria(SignalementCriteria criteria) {
        LOG.debug("count by criteria : {}", criteria);
        final Specification<Signalement> specification = createSpecification(criteria);
        return signalementRepository.count(specification);
    }

    /**
     * Function to convert {@link SignalementCriteria} to a {@link Specification}
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the matching {@link Specification} of the entity.
     */
    protected Specification<Signalement> createSpecification(SignalementCriteria criteria) {
        Specification<Signalement> specification = Specification.unrestricted();
        specification = specification.and((root, query, builder) -> {
            if (Long.class != query.getResultType()) {
                root.fetch(Signalement_.utilisateur, JoinType.LEFT);
                root.fetch(Signalement_.contenu, JoinType.LEFT);
            }
            return null;
        });
        if (criteria != null) {
            // This has to be called first, because the distinct method returns null
            specification = specification.and(
                Specification.allOf(
                    Boolean.TRUE.equals(criteria.getDistinct()) ? distinct(criteria.getDistinct()) : Specification.unrestricted(),
                    buildSpecification(criteria.getId(), Signalement_.id),
                    buildRangeSpecification(criteria.getDateSignalement(), Signalement_.dateSignalement),
                    buildSpecification(criteria.getStatut(), Signalement_.statut),
                    buildSpecification(criteria.getUtilisateurId(), root ->
                        root.join(Signalement_.utilisateur, JoinType.LEFT).get(Utilisateur_.id)
                    ),
                    buildSpecification(criteria.getContenuId(), root -> root.join(Signalement_.contenu, JoinType.LEFT).get(Contenu_.id))
                )
            );
        }
        return specification;
    }
}
