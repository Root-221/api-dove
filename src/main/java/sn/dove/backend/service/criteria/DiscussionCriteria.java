package sn.dove.backend.service.criteria;

import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;
import java.util.Optional;
import org.springdoc.core.annotations.ParameterObject;
import tech.jhipster.service.Criteria;
import tech.jhipster.service.filter.*;

/**
 * Criteria class for the {@link sn.dove.backend.domain.Discussion} entity. This class is used
 * in {@link sn.dove.backend.web.rest.DiscussionResource} to receive all the possible filtering options from
 * the Http GET request parameters.
 * For example the following could be a valid request:
 * {@code /discussions?id.greaterThan=5&attr1.contains=something&attr2.specified=false}
 * As Spring is unable to properly convert the types, unless specific {@link Filter} class are used, we need to use
 * fix type specific filters.
 */
@ParameterObject
@SuppressWarnings("common-java:DuplicatedBlocks")
public class DiscussionCriteria implements Serializable, Criteria {

    @Serial
    private static final long serialVersionUID = 1L;

    private UUIDFilter id;

    private StringFilter titre;

    private InstantFilter dateCreation;

    private UUIDFilter messagesId;

    private UUIDFilter createurId;

    private UUIDFilter communauteNanditeId;

    private Boolean distinct;

    public DiscussionCriteria() {}

    public DiscussionCriteria(DiscussionCriteria other) {
        this.id = other.optionalId().map(UUIDFilter::copy).orElse(null);
        this.titre = other.optionalTitre().map(StringFilter::copy).orElse(null);
        this.dateCreation = other.optionalDateCreation().map(InstantFilter::copy).orElse(null);
        this.messagesId = other.optionalMessagesId().map(UUIDFilter::copy).orElse(null);
        this.createurId = other.optionalCreateurId().map(UUIDFilter::copy).orElse(null);
        this.communauteNanditeId = other.optionalCommunauteNanditeId().map(UUIDFilter::copy).orElse(null);
        this.distinct = other.distinct;
    }

    @Override
    public DiscussionCriteria copy() {
        return new DiscussionCriteria(this);
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

    public StringFilter getTitre() {
        return titre;
    }

    public Optional<StringFilter> optionalTitre() {
        return Optional.ofNullable(titre);
    }

    public StringFilter titre() {
        if (titre == null) {
            setTitre(new StringFilter());
        }
        return titre;
    }

    public void setTitre(StringFilter titre) {
        this.titre = titre;
    }

    public InstantFilter getDateCreation() {
        return dateCreation;
    }

    public Optional<InstantFilter> optionalDateCreation() {
        return Optional.ofNullable(dateCreation);
    }

    public InstantFilter dateCreation() {
        if (dateCreation == null) {
            setDateCreation(new InstantFilter());
        }
        return dateCreation;
    }

    public void setDateCreation(InstantFilter dateCreation) {
        this.dateCreation = dateCreation;
    }

    public UUIDFilter getMessagesId() {
        return messagesId;
    }

    public Optional<UUIDFilter> optionalMessagesId() {
        return Optional.ofNullable(messagesId);
    }

    public UUIDFilter messagesId() {
        if (messagesId == null) {
            setMessagesId(new UUIDFilter());
        }
        return messagesId;
    }

    public void setMessagesId(UUIDFilter messagesId) {
        this.messagesId = messagesId;
    }

    public UUIDFilter getCreateurId() {
        return createurId;
    }

    public Optional<UUIDFilter> optionalCreateurId() {
        return Optional.ofNullable(createurId);
    }

    public UUIDFilter createurId() {
        if (createurId == null) {
            setCreateurId(new UUIDFilter());
        }
        return createurId;
    }

    public void setCreateurId(UUIDFilter createurId) {
        this.createurId = createurId;
    }

    public UUIDFilter getCommunauteNanditeId() {
        return communauteNanditeId;
    }

    public Optional<UUIDFilter> optionalCommunauteNanditeId() {
        return Optional.ofNullable(communauteNanditeId);
    }

    public UUIDFilter communauteNanditeId() {
        if (communauteNanditeId == null) {
            setCommunauteNanditeId(new UUIDFilter());
        }
        return communauteNanditeId;
    }

    public void setCommunauteNanditeId(UUIDFilter communauteNanditeId) {
        this.communauteNanditeId = communauteNanditeId;
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
        final DiscussionCriteria that = (DiscussionCriteria) o;
        return (
            Objects.equals(id, that.id) &&
            Objects.equals(titre, that.titre) &&
            Objects.equals(dateCreation, that.dateCreation) &&
            Objects.equals(messagesId, that.messagesId) &&
            Objects.equals(createurId, that.createurId) &&
            Objects.equals(communauteNanditeId, that.communauteNanditeId) &&
            Objects.equals(distinct, that.distinct)
        );
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, titre, dateCreation, messagesId, createurId, communauteNanditeId, distinct);
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "DiscussionCriteria{" +
            optionalId().map(f -> "id=" + f + ", ").orElse("") +
            optionalTitre().map(f -> "titre=" + f + ", ").orElse("") +
            optionalDateCreation().map(f -> "dateCreation=" + f + ", ").orElse("") +
            optionalMessagesId().map(f -> "messagesId=" + f + ", ").orElse("") +
            optionalCreateurId().map(f -> "createurId=" + f + ", ").orElse("") +
            optionalCommunauteNanditeId().map(f -> "communauteNanditeId=" + f + ", ").orElse("") +
            optionalDistinct().map(f -> "distinct=" + f + ", ").orElse("") +
        "}";
    }
}
