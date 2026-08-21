package sn.dove.backend.service;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sn.dove.backend.domain.*; // for static metamodels
import sn.dove.backend.domain.Metier;
import sn.dove.backend.repository.MetierRepository;
import sn.dove.backend.service.criteria.MetierCriteria;
import sn.dove.backend.service.dto.MetierDTO;
import sn.dove.backend.service.mapper.MetierMapper;
import tech.jhipster.service.QueryService;

/**
 * Service for executing complex queries for {@link Metier} entities in the database.
 * The main input is a {@link MetierCriteria} which gets converted to {@link Specification},
 * in a way that all the filters must apply.
 * It returns a {@link List} of {@link MetierDTO} which fulfills the criteria.
 */
@Service
@Transactional(readOnly = true)
public class MetierQueryService extends QueryService<Metier> {

    private static final Logger LOG = LoggerFactory.getLogger(MetierQueryService.class);

    private final MetierRepository metierRepository;

    private final MetierMapper metierMapper;

    public MetierQueryService(MetierRepository metierRepository, MetierMapper metierMapper) {
        this.metierRepository = metierRepository;
        this.metierMapper = metierMapper;
    }

    /**
     * Return a {@link List} of {@link MetierDTO} which matches the criteria from the database.
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the matching entities.
     */
    @Transactional(readOnly = true)
    public List<MetierDTO> findByCriteria(MetierCriteria criteria) {
        LOG.debug("find by criteria : {}", criteria);
        final Specification<Metier> specification = createSpecification(criteria);
        return metierMapper.toDto(metierRepository.findAll(specification));
    }

    /**
     * Return the number of matching entities in the database.
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the number of matching entities.
     */
    @Transactional(readOnly = true)
    public long countByCriteria(MetierCriteria criteria) {
        LOG.debug("count by criteria : {}", criteria);
        final Specification<Metier> specification = createSpecification(criteria);
        return metierRepository.count(specification);
    }

    /**
     * Function to convert {@link MetierCriteria} to a {@link Specification}
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the matching {@link Specification} of the entity.
     */
    protected Specification<Metier> createSpecification(MetierCriteria criteria) {
        Specification<Metier> specification = Specification.unrestricted();
        if (criteria != null) {
            // This has to be called first, because the distinct method returns null
            specification = specification.and(
                Specification.allOf(
                    Boolean.TRUE.equals(criteria.getDistinct()) ? distinct(criteria.getDistinct()) : Specification.unrestricted(),
                    buildSpecification(criteria.getId(), Metier_.id),
                    buildStringSpecification(criteria.getLibelle(), Metier_.libelle)
                )
            );
        }
        return specification;
    }
}
