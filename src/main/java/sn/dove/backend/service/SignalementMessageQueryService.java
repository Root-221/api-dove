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
import sn.dove.backend.domain.SignalementMessage;
import sn.dove.backend.repository.SignalementMessageRepository;
import sn.dove.backend.service.criteria.SignalementMessageCriteria;
import sn.dove.backend.service.dto.SignalementMessageDTO;
import sn.dove.backend.service.mapper.SignalementMessageMapper;
import tech.jhipster.service.QueryService;

/**
 * Service for executing complex queries for {@link SignalementMessage} entities in the database.
 * The main input is a {@link SignalementMessageCriteria} which gets converted to {@link Specification},
 * in a way that all the filters must apply.
 * It returns a {@link Page} of {@link SignalementMessageDTO} which fulfills the criteria.
 */
@Service
@Transactional(readOnly = true)
public class SignalementMessageQueryService extends QueryService<SignalementMessage> {

    private static final Logger LOG = LoggerFactory.getLogger(SignalementMessageQueryService.class);

    private final SignalementMessageRepository signalementMessageRepository;

    private final SignalementMessageMapper signalementMessageMapper;

    public SignalementMessageQueryService(
        SignalementMessageRepository signalementMessageRepository,
        SignalementMessageMapper signalementMessageMapper
    ) {
        this.signalementMessageRepository = signalementMessageRepository;
        this.signalementMessageMapper = signalementMessageMapper;
    }

    /**
     * Return a {@link Page} of {@link SignalementMessageDTO} which matches the criteria from the database.
     * @param criteria The object which holds all the filters, which the entities should match.
     * @param page The page, which should be returned.
     * @return the matching entities.
     */
    @Transactional(readOnly = true)
    public Page<SignalementMessageDTO> findByCriteria(SignalementMessageCriteria criteria, Pageable page) {
        LOG.debug("find by criteria : {}, page: {}", criteria, page);
        final Specification<SignalementMessage> specification = createSpecification(criteria);
        return signalementMessageRepository.findAll(specification, page).map(signalementMessageMapper::toDto);
    }

    /**
     * Return the number of matching entities in the database.
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the number of matching entities.
     */
    @Transactional(readOnly = true)
    public long countByCriteria(SignalementMessageCriteria criteria) {
        LOG.debug("count by criteria : {}", criteria);
        final Specification<SignalementMessage> specification = createSpecification(criteria);
        return signalementMessageRepository.count(specification);
    }

    /**
     * Function to convert {@link SignalementMessageCriteria} to a {@link Specification}
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the matching {@link Specification} of the entity.
     */
    protected Specification<SignalementMessage> createSpecification(SignalementMessageCriteria criteria) {
        Specification<SignalementMessage> specification = Specification.unrestricted();
        specification = specification.and((root, query, builder) -> {
            if (Long.class != query.getResultType()) {
                root.fetch(SignalementMessage_.utilisateur, JoinType.LEFT);
                root.fetch(SignalementMessage_.message, JoinType.LEFT);
            }
            return null;
        });
        if (criteria != null) {
            // This has to be called first, because the distinct method returns null
            specification = specification.and(
                Specification.allOf(
                    Boolean.TRUE.equals(criteria.getDistinct()) ? distinct(criteria.getDistinct()) : Specification.unrestricted(),
                    buildSpecification(criteria.getId(), SignalementMessage_.id),
                    buildRangeSpecification(criteria.getDateSignalement(), SignalementMessage_.dateSignalement),
                    buildSpecification(criteria.getUtilisateurId(), root ->
                        root.join(SignalementMessage_.utilisateur, JoinType.LEFT).get(Utilisateur_.id)
                    ),
                    buildSpecification(criteria.getMessageId(), root ->
                        root.join(SignalementMessage_.message, JoinType.LEFT).get(Message_.id)
                    )
                )
            );
        }
        return specification;
    }
}
