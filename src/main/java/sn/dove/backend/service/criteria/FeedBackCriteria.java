package sn.dove.backend.service.criteria;

import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;
import java.util.Optional;
import org.springdoc.core.annotations.ParameterObject;
import tech.jhipster.service.Criteria;
import tech.jhipster.service.filter.*;

/**
 * Criteria class for the {@link sn.dove.backend.domain.FeedBack} entity. This class is used
 * in {@link sn.dove.backend.web.rest.FeedBackResource} to receive all the possible filtering options from
 * the Http GET request parameters.
 * For example the following could be a valid request:
 * {@code /feed-backs?id.greaterThan=5&attr1.contains=something&attr2.specified=false}
 * As Spring is unable to properly convert the types, unless specific {@link Filter} class are used, we need to use
 * fix type specific filters.
 */
@ParameterObject
@SuppressWarnings("common-java:DuplicatedBlocks")
public class FeedBackCriteria implements Serializable, Criteria {

    @Serial
    private static final long serialVersionUID = 1L;

    private UUIDFilter id;

    private IntegerFilter note;

    private InstantFilter dateCreation;

    private StringFilter statutTraitement;

    private UUIDFilter utilisateurId;

    private UUIDFilter contenuId;

    private Boolean distinct;

    public FeedBackCriteria() {}

    public FeedBackCriteria(FeedBackCriteria other) {
        this.id = other.optionalId().map(UUIDFilter::copy).orElse(null);
        this.note = other.optionalNote().map(IntegerFilter::copy).orElse(null);
        this.dateCreation = other.optionalDateCreation().map(InstantFilter::copy).orElse(null);
        this.statutTraitement = other.optionalStatutTraitement().map(StringFilter::copy).orElse(null);
        this.utilisateurId = other.optionalUtilisateurId().map(UUIDFilter::copy).orElse(null);
        this.contenuId = other.optionalContenuId().map(UUIDFilter::copy).orElse(null);
        this.distinct = other.distinct;
    }

    @Override
    public FeedBackCriteria copy() {
        return new FeedBackCriteria(this);
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

    public IntegerFilter getNote() {
        return note;
    }

    public Optional<IntegerFilter> optionalNote() {
        return Optional.ofNullable(note);
    }

    public IntegerFilter note() {
        if (note == null) {
            setNote(new IntegerFilter());
        }
        return note;
    }

    public void setNote(IntegerFilter note) {
        this.note = note;
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

    public StringFilter getStatutTraitement() {
        return statutTraitement;
    }

    public Optional<StringFilter> optionalStatutTraitement() {
        return Optional.ofNullable(statutTraitement);
    }

    public StringFilter statutTraitement() {
        if (statutTraitement == null) {
            setStatutTraitement(new StringFilter());
        }
        return statutTraitement;
    }

    public void setStatutTraitement(StringFilter statutTraitement) {
        this.statutTraitement = statutTraitement;
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

    public UUIDFilter getContenuId() {
        return contenuId;
    }

    public Optional<UUIDFilter> optionalContenuId() {
        return Optional.ofNullable(contenuId);
    }

    public UUIDFilter contenuId() {
        if (contenuId == null) {
            setContenuId(new UUIDFilter());
        }
        return contenuId;
    }

    public void setContenuId(UUIDFilter contenuId) {
        this.contenuId = contenuId;
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
        final FeedBackCriteria that = (FeedBackCriteria) o;
        return (
            Objects.equals(id, that.id) &&
            Objects.equals(note, that.note) &&
            Objects.equals(dateCreation, that.dateCreation) &&
            Objects.equals(statutTraitement, that.statutTraitement) &&
            Objects.equals(utilisateurId, that.utilisateurId) &&
            Objects.equals(contenuId, that.contenuId) &&
            Objects.equals(distinct, that.distinct)
        );
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, note, dateCreation, statutTraitement, utilisateurId, contenuId, distinct);
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "FeedBackCriteria{" +
            optionalId().map(f -> "id=" + f + ", ").orElse("") +
            optionalNote().map(f -> "note=" + f + ", ").orElse("") +
            optionalDateCreation().map(f -> "dateCreation=" + f + ", ").orElse("") +
            optionalStatutTraitement().map(f -> "statutTraitement=" + f + ", ").orElse("") +
            optionalUtilisateurId().map(f -> "utilisateurId=" + f + ", ").orElse("") +
            optionalContenuId().map(f -> "contenuId=" + f + ", ").orElse("") +
            optionalDistinct().map(f -> "distinct=" + f + ", ").orElse("") +
        "}";
    }
}
