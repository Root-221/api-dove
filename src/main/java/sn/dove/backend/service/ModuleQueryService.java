package sn.dove.backend.service;

import jakarta.persistence.criteria.JoinType;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sn.dove.backend.domain.*; // for static metamodels
import sn.dove.backend.domain.Module;
import sn.dove.backend.repository.ModuleRepository;
import sn.dove.backend.service.criteria.ModuleCriteria;
import sn.dove.backend.service.dto.ModuleDTO;
import sn.dove.backend.service.mapper.ModuleMapper;
import tech.jhipster.service.QueryService;

/**
 * Service for executing complex queries for {@link Module} entities in the database.
 * The main input is a {@link ModuleCriteria} which gets converted to {@link Specification},
 * in a way that all the filters must apply.
 * It returns a {@link List} of {@link ModuleDTO} which fulfills the criteria.
 */
@Service
@Transactional(readOnly = true)
public class ModuleQueryService extends QueryService<Module> {

    private static final Logger LOG = LoggerFactory.getLogger(ModuleQueryService.class);

    private final ModuleRepository moduleRepository;

    private final ModuleMapper moduleMapper;

    public ModuleQueryService(ModuleRepository moduleRepository, ModuleMapper moduleMapper) {
        this.moduleRepository = moduleRepository;
        this.moduleMapper = moduleMapper;
    }

    /**
     * Return a {@link List} of {@link ModuleDTO} which matches the criteria from the database.
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the matching entities.
     */
    @Transactional(readOnly = true)
    public List<ModuleDTO> findByCriteria(ModuleCriteria criteria) {
        LOG.debug("find by criteria : {}", criteria);
        final Specification<Module> specification = createSpecification(criteria);
        return moduleMapper.toDto(moduleRepository.findAll(specification));
    }

    /**
     * Return the number of matching entities in the database.
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the number of matching entities.
     */
    @Transactional(readOnly = true)
    public long countByCriteria(ModuleCriteria criteria) {
        LOG.debug("count by criteria : {}", criteria);
        final Specification<Module> specification = createSpecification(criteria);
        return moduleRepository.count(specification);
    }

    /**
     * Function to convert {@link ModuleCriteria} to a {@link Specification}
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the matching {@link Specification} of the entity.
     */
    protected Specification<Module> createSpecification(ModuleCriteria criteria) {
        Specification<Module> specification = Specification.unrestricted();
        specification = specification.and((root, query, builder) -> {
            if (Long.class != query.getResultType()) {
                root.fetch(Module_.application, JoinType.LEFT);
            }
            return null;
        });
        if (criteria != null) {
            // This has to be called first, because the distinct method returns null
            specification = specification.and(
                Specification.allOf(
                    Boolean.TRUE.equals(criteria.getDistinct()) ? distinct(criteria.getDistinct()) : Specification.unrestricted(),
                    buildSpecification(criteria.getId(), Module_.id),
                    buildStringSpecification(criteria.getLibelle(), Module_.libelle),
                    buildSpecification(criteria.getApplicationId(), root ->
                        root.join(Module_.application, JoinType.LEFT).get(Application_.id)
                    )
                )
            );
        }
        return specification;
    }
}
