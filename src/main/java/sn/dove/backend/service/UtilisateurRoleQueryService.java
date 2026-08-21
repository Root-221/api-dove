package sn.dove.backend.service;

import jakarta.persistence.criteria.JoinType;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sn.dove.backend.domain.*; // for static metamodels
import sn.dove.backend.domain.UtilisateurRole;
import sn.dove.backend.repository.UtilisateurRoleRepository;
import sn.dove.backend.service.criteria.UtilisateurRoleCriteria;
import sn.dove.backend.service.dto.UtilisateurRoleDTO;
import sn.dove.backend.service.mapper.UtilisateurRoleMapper;
import tech.jhipster.service.QueryService;

/**
 * Service for executing complex queries for {@link UtilisateurRole} entities in the database.
 * The main input is a {@link UtilisateurRoleCriteria} which gets converted to {@link Specification},
 * in a way that all the filters must apply.
 * It returns a {@link List} of {@link UtilisateurRoleDTO} which fulfills the criteria.
 */
@Service
@Transactional(readOnly = true)
public class UtilisateurRoleQueryService extends QueryService<UtilisateurRole> {

    private static final Logger LOG = LoggerFactory.getLogger(UtilisateurRoleQueryService.class);

    private final UtilisateurRoleRepository utilisateurRoleRepository;

    private final UtilisateurRoleMapper utilisateurRoleMapper;

    public UtilisateurRoleQueryService(UtilisateurRoleRepository utilisateurRoleRepository, UtilisateurRoleMapper utilisateurRoleMapper) {
        this.utilisateurRoleRepository = utilisateurRoleRepository;
        this.utilisateurRoleMapper = utilisateurRoleMapper;
    }

    /**
     * Return a {@link List} of {@link UtilisateurRoleDTO} which matches the criteria from the database.
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the matching entities.
     */
    @Transactional(readOnly = true)
    public List<UtilisateurRoleDTO> findByCriteria(UtilisateurRoleCriteria criteria) {
        LOG.debug("find by criteria : {}", criteria);
        final Specification<UtilisateurRole> specification = createSpecification(criteria);
        return utilisateurRoleMapper.toDto(utilisateurRoleRepository.findAll(specification));
    }

    /**
     * Return the number of matching entities in the database.
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the number of matching entities.
     */
    @Transactional(readOnly = true)
    public long countByCriteria(UtilisateurRoleCriteria criteria) {
        LOG.debug("count by criteria : {}", criteria);
        final Specification<UtilisateurRole> specification = createSpecification(criteria);
        return utilisateurRoleRepository.count(specification);
    }

    /**
     * Function to convert {@link UtilisateurRoleCriteria} to a {@link Specification}
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the matching {@link Specification} of the entity.
     */
    protected Specification<UtilisateurRole> createSpecification(UtilisateurRoleCriteria criteria) {
        Specification<UtilisateurRole> specification = Specification.unrestricted();
        specification = specification.and((root, query, builder) -> {
            if (Long.class != query.getResultType()) {
                root.fetch(UtilisateurRole_.utilisateur, JoinType.LEFT);
                root.fetch(UtilisateurRole_.role, JoinType.LEFT);
            }
            return null;
        });
        if (criteria != null) {
            // This has to be called first, because the distinct method returns null
            specification = specification.and(
                Specification.allOf(
                    Boolean.TRUE.equals(criteria.getDistinct()) ? distinct(criteria.getDistinct()) : Specification.unrestricted(),
                    buildSpecification(criteria.getId(), UtilisateurRole_.id),
                    buildSpecification(criteria.getUtilisateurId(), root ->
                        root.join(UtilisateurRole_.utilisateur, JoinType.LEFT).get(Utilisateur_.id)
                    ),
                    buildSpecification(criteria.getRoleId(), root -> root.join(UtilisateurRole_.role, JoinType.LEFT).get(Role_.id))
                )
            );
        }
        return specification;
    }
}
