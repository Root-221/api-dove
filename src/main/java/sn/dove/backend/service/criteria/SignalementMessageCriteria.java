package sn.dove.backend.service.criteria;

import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;
import java.util.Optional;
import org.springdoc.core.annotations.ParameterObject;
import tech.jhipster.service.Criteria;
import tech.jhipster.service.filter.*;

/**
 * Criteria class for the {@link sn.dove.backend.domain.SignalementMessage} entity. This class is used
 * in {@link sn.dove.backend.web.rest.SignalementMessageResource} to receive all the possible filtering options from
 * the Http GET request parameters.
 * For example the following could be a valid request:
 * {@code /signalement-messages?id.greaterThan=5&attr1.contains=something&attr2.specified=false}
 * As Spring is unable to properly convert the types, unless specific {@link Filter} class are used, we need to use
 * fix type specific filters.
 */
@ParameterObject
@SuppressWarnings("common-java:DuplicatedBlocks")
public class SignalementMessageCriteria implements Serializable, Criteria {

    @Serial
    private static final long serialVersionUID = 1L;

    private UUIDFilter id;

    private InstantFilter dateSignalement;

    private UUIDFilter utilisateurId;

    private UUIDFilter messageId;

    private Boolean distinct;

    public SignalementMessageCriteria() {}

    public SignalementMessageCriteria(SignalementMessageCriteria other) {
        this.id = other.optionalId().map(UUIDFilter::copy).orElse(null);
        this.dateSignalement = other.optionalDateSignalement().map(InstantFilter::copy).orElse(null);
        this.utilisateurId = other.optionalUtilisateurId().map(UUIDFilter::copy).orElse(null);
        this.messageId = other.optionalMessageId().map(UUIDFilter::copy).orElse(null);
        this.distinct = other.distinct;
    }

    @Override
    public SignalementMessageCriteria copy() {
        return new SignalementMessageCriteria(this);
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

    public InstantFilter getDateSignalement() {
        return dateSignalement;
    }

    public Optional<InstantFilter> optionalDateSignalement() {
        return Optional.ofNullable(dateSignalement);
    }

    public InstantFilter dateSignalement() {
        if (dateSignalement == null) {
            setDateSignalement(new InstantFilter());
        }
        return dateSignalement;
    }

    public void setDateSignalement(InstantFilter dateSignalement) {
        this.dateSignalement = dateSignalement;
    }

    public UUIDFilter getUtilisateurId() {
        return utilisateurId;
    }

    public Optional<UUIDFilter> optionalUtilisateurId() {
        return Optional.ofNullable(utilisateurId);
    }

    public UUIDFilter utilisateurId() {
        if (utilisateurId == null) {
            setUtilisateurId(new UUIDFilter());
        }
        return utilisateurId;
    }

    public void setUtilisateurId(UUIDFilter utilisateurId) {
        this.utilisateurId = utilisateurId;
    }

    public UUIDFilter getMessageId() {
        return messageId;
    }

    public Optional<UUIDFilter> optionalMessageId() {
        return Optional.ofNullable(messageId);
    }

    public UUIDFilter messageId() {
        if (messageId == null) {
            setMessageId(new UUIDFilter());
        }
        return messageId;
    }

    public void setMessageId(UUIDFilter messageId) {
        this.messageId = messageId;
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
        final SignalementMessageCriteria that = (SignalementMessageCriteria) o;
        return (
            Objects.equals(id, that.id) &&
            Objects.equals(dateSignalement, that.dateSignalement) &&
            Objects.equals(utilisateurId, that.utilisateurId) &&
            Objects.equals(messageId, that.messageId) &&
            Objects.equals(distinct, that.distinct)
        );
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, dateSignalement, utilisateurId, messageId, distinct);
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "SignalementMessageCriteria{" +
            optionalId().map(f -> "id=" + f + ", ").orElse("") +
            optionalDateSignalement().map(f -> "dateSignalement=" + f + ", ").orElse("") +
            optionalUtilisateurId().map(f -> "utilisateurId=" + f + ", ").orElse("") +
            optionalMessageId().map(f -> "messageId=" + f + ", ").orElse("") +
            optionalDistinct().map(f -> "distinct=" + f + ", ").orElse("") +
        "}";
    }
}
