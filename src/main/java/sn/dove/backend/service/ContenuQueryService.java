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
import sn.dove.backend.domain.Contenu;
import sn.dove.backend.repository.ContenuRepository;
import sn.dove.backend.service.criteria.ContenuCriteria;
import sn.dove.backend.service.dto.ContenuDTO;
import sn.dove.backend.service.mapper.ContenuMapper;
import tech.jhipster.service.QueryService;

/**
 * Service for executing complex queries for {@link Contenu} entities in the database.
 * The main input is a {@link ContenuCriteria} which gets converted to {@link Specification},
 * in a way that all the filters must apply.
 * It returns a {@link Page} of {@link ContenuDTO} which fulfills the criteria.
 */
@Service
@Transactional(readOnly = true)
public class ContenuQueryService extends QueryService<Contenu> {

    private static final Logger LOG = LoggerFactory.getLogger(ContenuQueryService.class);

    private final ContenuRepository contenuRepository;

    private final ContenuMapper contenuMapper;

    public ContenuQueryService(ContenuRepository contenuRepository, ContenuMapper contenuMapper) {
        this.contenuRepository = contenuRepository;
        this.contenuMapper = contenuMapper;
    }

    /**
     * Return a {@link Page} of {@link ContenuDTO} which matches the criteria from the database.
     * @param criteria The object which holds all the filters, which the entities should match.
     * @param page The page, which should be returned.
     * @return the matching entities.
     */
    @Transactional(readOnly = true)
    public Page<ContenuDTO> findByCriteria(ContenuCriteria criteria, Pageable page) {
        LOG.debug("find by criteria : {}, page: {}", criteria, page);
        final Specification<Contenu> specification = createSpecification(criteria);
        return contenuRepository.findAll(specification, page).map(contenuMapper::toDto);
    }

    /**
     * Return the number of matching entities in the database.
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the number of matching entities.
     */
    @Transactional(readOnly = true)
    public long countByCriteria(ContenuCriteria criteria) {
        LOG.debug("count by criteria : {}", criteria);
        final Specification<Contenu> specification = createSpecification(criteria);
        return contenuRepository.count(specification);
    }

    /**
     * Function to convert {@link ContenuCriteria} to a {@link Specification}
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the matching {@link Specification} of the entity.
     */
    protected Specification<Contenu> createSpecification(ContenuCriteria criteria) {
        Specification<Contenu> specification = Specification.unrestricted();
        specification = specification.and((root, query, builder) -> {
            if (Long.class != query.getResultType()) {
                root.fetch(Contenu_.application, JoinType.LEFT);
                root.fetch(Contenu_.module, JoinType.LEFT);
                root.fetch(Contenu_.metier, JoinType.LEFT);
                root.fetch(Contenu_.auteur, JoinType.LEFT);
                root.fetch(Contenu_.validateur, JoinType.LEFT);
            }
            return null;
        });
        if (criteria != null) {
            // This has to be called first, because the distinct method returns null
            specification = specification.and(
                Specification.allOf(
                    Boolean.TRUE.equals(criteria.getDistinct()) ? distinct(criteria.getDistinct()) : Specification.unrestricted(),
                    buildSpecification(criteria.getId(), Contenu_.id),
                    buildStringSpecification(criteria.getTitre(), Contenu_.titre),
                    buildSpecification(criteria.getTypeContenu(), Contenu_.typeContenu),
                    buildSpecification(criteria.getStatut(), Contenu_.statut),
                    buildRangeSpecification(criteria.getVersion(), Contenu_.version),
                    buildRangeSpecification(criteria.getDatePublication(), Contenu_.datePublication),
                    buildRangeSpecification(criteria.getDerniereMiseAJour(), Contenu_.derniereMiseAJour),
                    buildRangeSpecification(criteria.getProchaineRevue(), Contenu_.prochaineRevue),
                    buildRangeSpecification(criteria.getProgression(), Contenu_.progression),
                    buildRangeSpecification(criteria.getDateCreation(), Contenu_.dateCreation),
                    buildSpecification(criteria.getApplicationId(), root ->
                        root.join(Contenu_.application, JoinType.LEFT).get(Application_.id)
                    ),
                    buildSpecification(criteria.getModuleId(), root -> root.join(Contenu_.module, JoinType.LEFT).get(Module_.id)),
                    buildSpecification(criteria.getMetierId(), root -> root.join(Contenu_.metier, JoinType.LEFT).get(Metier_.id)),
                    buildSpecification(criteria.getAuteurId(), root -> root.join(Contenu_.auteur, JoinType.LEFT).get(Utilisateur_.id)),
                    buildSpecification(criteria.getValidateurId(), root ->
                        root.join(Contenu_.validateur, JoinType.LEFT).get(Utilisateur_.id)
                    )
                )
            );
        }
        return specification;
    }
}
