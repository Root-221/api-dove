package sn.dove.backend.service.criteria;

import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;
import java.util.Optional;
import org.springdoc.core.annotations.ParameterObject;
import tech.jhipster.service.Criteria;
import tech.jhipster.service.filter.*;

/**
 * Criteria class for the {@link sn.dove.backend.domain.Message} entity. This class is used
 * in {@link sn.dove.backend.web.rest.MessageResource} to receive all the possible filtering options from
 * the Http GET request parameters.
 * For example the following could be a valid request:
 * {@code /messages?id.greaterThan=5&attr1.contains=something&attr2.specified=false}
 * As Spring is unable to properly convert the types, unless specific {@link Filter} class are used, we need to use
 * fix type specific filters.
 */
@ParameterObject
@SuppressWarnings("common-java:DuplicatedBlocks")
public class MessageCriteria implements Serializable, Criteria {

    @Serial
    private static final long serialVersionUID = 1L;

    private UUIDFilter id;

    private InstantFilter dateEnvoi;

    private UUIDFilter reponsesId;

    private UUIDFilter signalementsId;

    private UUIDFilter emetteurId;

    private UUIDFilter metierId;

    private UUIDFilter discussionId;

    private UUIDFilter messageParentId;

    private Boolean distinct;

    public MessageCriteria() {}

    public MessageCriteria(MessageCriteria other) {
        this.id = other.optionalId().map(UUIDFilter::copy).orElse(null);
        this.dateEnvoi = other.optionalDateEnvoi().map(InstantFilter::copy).orElse(null);
        this.reponsesId = other.optionalReponsesId().map(UUIDFilter::copy).orElse(null);
        this.signalementsId = other.optionalSignalementsId().map(UUIDFilter::copy).orElse(null);
        this.emetteurId = other.optionalEmetteurId().map(UUIDFilter::copy).orElse(null);
        this.metierId = other.optionalMetierId().map(UUIDFilter::copy).orElse(null);
        this.discussionId = other.optionalDiscussionId().map(UUIDFilter::copy).orElse(null);
        this.messageParentId = other.optionalMessageParentId().map(UUIDFilter::copy).orElse(null);
        this.distinct = other.distinct;
    }

    @Override
    public MessageCriteria copy() {
        return new MessageCriteria(this);
    }

    public UUIDFilter getId() {
        return id;
    }

    public Optional<UUIDFilter> optionalId() {
        return Optional.ofNullable(id);
    }

    public UUIDFilter id() {
        if (id == null) {
            setId(new UUIDFilter());
        }
        return id;
    }

    public void setId(UUIDFilter id) {
        this.id = id;
    }

    public InstantFilter getDateEnvoi() {
        return dateEnvoi;
    }

    public Optional<InstantFilter> optionalDateEnvoi() {
        return Optional.ofNullable(dateEnvoi);
    }

    public InstantFilter dateEnvoi() {
        if (dateEnvoi == null) {
            setDateEnvoi(new InstantFilter());
        }
        return dateEnvoi;
    }

    public void setDateEnvoi(InstantFilter dateEnvoi) {
        this.dateEnvoi = dateEnvoi;
    }

    public UUIDFilter getReponsesId() {
        return reponsesId;
    }

    public Optional<UUIDFilter> optionalReponsesId() {
        return Optional.ofNullable(reponsesId);
    }

    public UUIDFilter reponsesId() {
        if (reponsesId == null) {
            setReponsesId(new UUIDFilter());
        }
        return reponsesId;
    }

    public void setReponsesId(UUIDFilter reponsesId) {
        this.reponsesId = reponsesId;
    }

    public UUIDFilter getSignalementsId() {
        return signalementsId;
    }

    public Optional<UUIDFilter> optionalSignalementsId() {
        return Optional.ofNullable(signalementsId);
    }

    public UUIDFilter signalementsId() {
        if (signalementsId == null) {
            setSignalementsId(new UUIDFilter());
        }
        return signalementsId;
    }

    public void setSignalementsId(UUIDFilter signalementsId) {
        this.signalementsId = signalementsId;
    }

    public UUIDFilter getEmetteurId() {
        return emetteurId;
    }

    public Optional<UUIDFilter> optionalEmetteurId() {
        return Optional.ofNullable(emetteurId);
    }

    public UUIDFilter emetteurId() {
        if (emetteurId == null) {
            setEmetteurId(new UUIDFilter());
        }
        return emetteurId;
    }

    public void setEmetteurId(UUIDFilter emetteurId) {
        this.emetteurId = emetteurId;
    }

    public UUIDFilter getMetierId() {
        return metierId;
    }

    public Optional<UUIDFilter> optionalMetierId() {
        return Optional.ofNullable(metierId);
    }

    public UUIDFilter metierId() {
        if (metierId == null) {
            setMetierId(new UUIDFilter());
        }
        return metierId;
    }

    public void setMetierId(UUIDFilter metierId) {
        this.metierId = metierId;
    }

    public UUIDFilter getDiscussionId() {
        return discussionId;
    }

    public Optional<UUIDFilter> optionalDiscussionId() {
        return Optional.ofNullable(discussionId);
    }

    public UUIDFilter discussionId() {
        if (discussionId == null) {
            setDiscussionId(new UUIDFilter());
        }
        return discussionId;
    }

    public void setDiscussionId(UUIDFilter discussionId) {
        this.discussionId = discussionId;
    }

    public UUIDFilter getMessageParentId() {
        return messageParentId;
    }

    public Optional<UUIDFilter> optionalMessageParentId() {
        return Optional.ofNullable(messageParentId);
    }

    public UUIDFilter messageParentId() {
        if (messageParentId == null) {
            setMessageParentId(new UUIDFilter());
        }
        return messageParentId;
    }

    public void setMessageParentId(UUIDFilter messageParentId) {
        this.messageParentId = messageParentId;
    }

    public Boolean getDistinct() {
        return distinct;
    }

    public Optional<Boolean> optionalDistinct() {
        return Optional.ofNullable(distinct);
    }

    public Boolean distinct() {
        if (distinct == null) {
            setDistinct(true);
        }
        return distinct;
    }

    public void setDistinct(Boolean distinct) {
        this.distinct = distinct;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final MessageCriteria that = (MessageCriteria) o;
        return (
            Objects.equals(id, that.id) &&
            Objects.equals(dateEnvoi, that.dateEnvoi) &&
            Objects.equals(reponsesId, that.reponsesId) &&
            Objects.equals(signalementsId, that.signalementsId) &&
            Objects.equals(emetteurId, that.emetteurId) &&
            Objects.equals(metierId, that.metierId) &&
            Objects.equals(discussionId, that.discussionId) &&
            Objects.equals(messageParentId, that.messageParentId) &&
            Objects.equals(distinct, that.distinct)
        );
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, dateEnvoi, reponsesId, signalementsId, emetteurId, metierId, discussionId, messageParentId, distinct);
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "MessageCriteria{" +
            optionalId().map(f -> "id=" + f + ", ").orElse("") +
            optionalDateEnvoi().map(f -> "dateEnvoi=" + f + ", ").orElse("") +
            optionalReponsesId().map(f -> "reponsesId=" + f + ", ").orElse("") +
            optionalSignalementsId().map(f -> "signalementsId=" + f + ", ").orElse("") +
            optionalEmetteurId().map(f -> "emetteurId=" + f + ", ").orElse("") +
            optionalMetierId().map(f -> "metierId=" + f + ", ").orElse("") +
            optionalDiscussionId().map(f -> "discussionId=" + f + ", ").orElse("") +
            optionalMessageParentId().map(f -> "messageParentId=" + f + ", ").orElse("") +
            optionalDistinct().map(f -> "distinct=" + f + ", ").orElse("") +
        "}";
    }
}
