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
import sn.dove.backend.domain.Message;
import sn.dove.backend.repository.MessageRepository;
import sn.dove.backend.service.criteria.MessageCriteria;
import sn.dove.backend.service.dto.MessageDTO;
import sn.dove.backend.service.mapper.MessageMapper;
import tech.jhipster.service.QueryService;

/**
 * Service for executing complex queries for {@link Message} entities in the database.
 * The main input is a {@link MessageCriteria} which gets converted to {@link Specification},
 * in a way that all the filters must apply.
 * It returns a {@link Page} of {@link MessageDTO} which fulfills the criteria.
 */
@Service
@Transactional(readOnly = true)
public class MessageQueryService extends QueryService<Message> {

    private static final Logger LOG = LoggerFactory.getLogger(MessageQueryService.class);

    private final MessageRepository messageRepository;

    private final MessageMapper messageMapper;

    public MessageQueryService(MessageRepository messageRepository, MessageMapper messageMapper) {
        this.messageRepository = messageRepository;
        this.messageMapper = messageMapper;
    }

    /**
     * Return a {@link Page} of {@link MessageDTO} which matches the criteria from the database.
     * @param criteria The object which holds all the filters, which the entities should match.
     * @param page The page, which should be returned.
     * @return the matching entities.
     */
    @Transactional(readOnly = true)
    public Page<MessageDTO> findByCriteria(MessageCriteria criteria, Pageable page) {
        LOG.debug("find by criteria : {}, page: {}", criteria, page);
        final Specification<Message> specification = createSpecification(criteria);
        return messageRepository.findAll(specification, page).map(messageMapper::toDto);
    }

    /**
     * Return the number of matching entities in the database.
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the number of matching entities.
     */
    @Transactional(readOnly = true)
    public long countByCriteria(MessageCriteria criteria) {
        LOG.debug("count by criteria : {}", criteria);
        final Specification<Message> specification = createSpecification(criteria);
        return messageRepository.count(specification);
    }

    /**
     * Function to convert {@link MessageCriteria} to a {@link Specification}
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the matching {@link Specification} of the entity.
     */
    protected Specification<Message> createSpecification(MessageCriteria criteria) {
        Specification<Message> specification = Specification.unrestricted();
        specification = specification.and((root, query, builder) -> {
            if (Long.class != query.getResultType()) {
                root.fetch(Message_.emetteur, JoinType.LEFT);
                root.fetch(Message_.metier, JoinType.LEFT);
                root.fetch(Message_.discussion, JoinType.LEFT);
                root.fetch(Message_.messageParent, JoinType.LEFT);
            }
            return null;
        });
        if (criteria != null) {
            // This has to be called first, because the distinct method returns null
            specification = specification.and(
                Specification.allOf(
                    Boolean.TRUE.equals(criteria.getDistinct()) ? distinct(criteria.getDistinct()) : Specification.unrestricted(),
                    buildSpecification(criteria.getId(), Message_.id),
                    buildRangeSpecification(criteria.getDateEnvoi(), Message_.dateEnvoi),
                    buildSpecification(criteria.getReponsesId(), root -> root.join(Message_.reponseses, JoinType.LEFT).get(Message_.id)),
                    buildSpecification(criteria.getSignalementsId(), root ->
                        root.join(Message_.signalementses, JoinType.LEFT).get(SignalementMessage_.id)
                    ),
                    buildSpecification(criteria.getEmetteurId(), root -> root.join(Message_.emetteur, JoinType.LEFT).get(Utilisateur_.id)),
                    buildSpecification(criteria.getMetierId(), root -> root.join(Message_.metier, JoinType.LEFT).get(Metier_.id)),
                    buildSpecification(criteria.getDiscussionId(), root ->
                        root.join(Message_.discussion, JoinType.LEFT).get(Discussion_.id)
                    ),
                    buildSpecification(criteria.getMessageParentId(), root ->
                        root.join(Message_.messageParent, JoinType.LEFT).get(Message_.id)
                    )
                )
            );
        }
        return specification;
    }
}
