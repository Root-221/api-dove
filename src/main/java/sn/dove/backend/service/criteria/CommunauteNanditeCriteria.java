package sn.dove.backend.service.criteria;

import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;
import java.util.Optional;
import org.springdoc.core.annotations.ParameterObject;
import tech.jhipster.service.Criteria;
import tech.jhipster.service.filter.*;

/**
 * Criteria class for the {@link sn.dove.backend.domain.CommunauteNandite} entity. This class is used
 * in {@link sn.dove.backend.web.rest.CommunauteNanditeResource} to receive all the possible filtering options from
 * the Http GET request parameters.
 * For example the following could be a valid request:
 * {@code /communaute-nandites?id.greaterThan=5&attr1.contains=something&attr2.specified=false}
 * As Spring is unable to properly convert the types, unless specific {@link Filter} class are used, we need to use
 * fix type specific filters.
 */
@ParameterObject
@SuppressWarnings("common-java:DuplicatedBlocks")
public class CommunauteNanditeCriteria implements Serializable, Criteria {

    @Serial
    private static final long serialVersionUID = 1L;

    private UUIDFilter id;

    private StringFilter nom;

    private UUIDFilter discussionsId;

    private UUIDFilter membresId;

    private Boolean distinct;

    public CommunauteNanditeCriteria() {}

    public CommunauteNanditeCriteria(CommunauteNanditeCriteria other) {
        this.id = other.optionalId().map(UUIDFilter::copy).orElse(null);
        this.nom = other.optionalNom().map(StringFilter::copy).orElse(null);
        this.discussionsId = other.optionalDiscussionsId().map(UUIDFilter::copy).orElse(null);
        this.membresId = other.optionalMembresId().map(UUIDFilter::copy).orElse(null);
        this.distinct = other.distinct;
    }

    @Override
    public CommunauteNanditeCriteria copy() {
        return new CommunauteNanditeCriteria(this);
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

    public StringFilter getNom() {
        return nom;
    }

    public Optional<StringFilter> optionalNom() {
        return Optional.ofNullable(nom);
    }

    public StringFilter nom() {
        if (nom == null) {
            setNom(new StringFilter());
        }
        return nom;
    }

    public void setNom(StringFilter nom) {
        this.nom = nom;
    }

    public UUIDFilter getDiscussionsId() {
        return discussionsId;
    }

    public Optional<UUIDFilter> optionalDiscussionsId() {
        return Optional.ofNullable(discussionsId);
    }

    public UUIDFilter discussionsId() {
        if (discussionsId == null) {
            setDiscussionsId(new UUIDFilter());
        }
        return discussionsId;
    }

    public void setDiscussionsId(UUIDFilter discussionsId) {
        this.discussionsId = discussionsId;
    }

    public UUIDFilter getMembresId() {
        return membresId;
    }

    public Optional<UUIDFilter> optionalMembresId() {
        return Optional.ofNullable(membresId);
    }

    public UUIDFilter membresId() {
        if (membresId == null) {
            setMembresId(new UUIDFilter());
        }
        return membresId;
    }

    public void setMembresId(UUIDFilter membresId) {
        this.membresId = membresId;
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
        final CommunauteNanditeCriteria that = (CommunauteNanditeCriteria) o;
        return (
            Objects.equals(id, that.id) &&
            Objects.equals(nom, that.nom) &&
            Objects.equals(discussionsId, that.discussionsId) &&
            Objects.equals(membresId, that.membresId) &&
            Objects.equals(distinct, that.distinct)
        );
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, nom, discussionsId, membresId, distinct);
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "CommunauteNanditeCriteria{" +
            optionalId().map(f -> "id=" + f + ", ").orElse("") +
            optionalNom().map(f -> "nom=" + f + ", ").orElse("") +
            optionalDiscussionsId().map(f -> "discussionsId=" + f + ", ").orElse("") +
            optionalMembresId().map(f -> "membresId=" + f + ", ").orElse("") +
            optionalDistinct().map(f -> "distinct=" + f + ", ").orElse("") +
        "}";
    }
}
