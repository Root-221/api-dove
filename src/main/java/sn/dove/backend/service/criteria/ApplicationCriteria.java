package sn.dove.backend.service.criteria;

import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;
import java.util.Optional;
import org.springdoc.core.annotations.ParameterObject;
import tech.jhipster.service.Criteria;
import tech.jhipster.service.filter.*;

/**
 * Criteria class for the {@link sn.dove.backend.domain.Application} entity. This class is used
 * in {@link sn.dove.backend.web.rest.ApplicationResource} to receive all the possible filtering options from
 * the Http GET request parameters.
 * For example the following could be a valid request:
 * {@code /applications?id.greaterThan=5&attr1.contains=something&attr2.specified=false}
 * As Spring is unable to properly convert the types, unless specific {@link Filter} class are used, we need to use
 * fix type specific filters.
 */
@ParameterObject
@SuppressWarnings("common-java:DuplicatedBlocks")
public class ApplicationCriteria implements Serializable, Criteria {

    @Serial
    private static final long serialVersionUID = 1L;

    private UUIDFilter id;

    private StringFilter nom;

    private LocalDateFilter dateAjout;

    private UUIDFilter modulesId;

    private Boolean distinct;

    public ApplicationCriteria() {}

    public ApplicationCriteria(ApplicationCriteria other) {
        this.id = other.optionalId().map(UUIDFilter::copy).orElse(null);
        this.nom = other.optionalNom().map(StringFilter::copy).orElse(null);
        this.dateAjout = other.optionalDateAjout().map(LocalDateFilter::copy).orElse(null);
        this.modulesId = other.optionalModulesId().map(UUIDFilter::copy).orElse(null);
        this.distinct = other.distinct;
    }

    @Override
    public ApplicationCriteria copy() {
        return new ApplicationCriteria(this);
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

    public LocalDateFilter getDateAjout() {
        return dateAjout;
    }

    public Optional<LocalDateFilter> optionalDateAjout() {
        return Optional.ofNullable(dateAjout);
    }

    public LocalDateFilter dateAjout() {
        if (dateAjout == null) {
            setDateAjout(new LocalDateFilter());
        }
        return dateAjout;
    }

    public void setDateAjout(LocalDateFilter dateAjout) {
        this.dateAjout = dateAjout;
    }

    public UUIDFilter getModulesId() {
        return modulesId;
    }

    public Optional<UUIDFilter> optionalModulesId() {
        return Optional.ofNullable(modulesId);
    }

    public UUIDFilter modulesId() {
        if (modulesId == null) {
            setModulesId(new UUIDFilter());
        }
        return modulesId;
    }

    public void setModulesId(UUIDFilter modulesId) {
        this.modulesId = modulesId;
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
        final ApplicationCriteria that = (ApplicationCriteria) o;
        return (
            Objects.equals(id, that.id) &&
            Objects.equals(nom, that.nom) &&
            Objects.equals(dateAjout, that.dateAjout) &&
            Objects.equals(modulesId, that.modulesId) &&
            Objects.equals(distinct, that.distinct)
        );
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, nom, dateAjout, modulesId, distinct);
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "ApplicationCriteria{" +
            optionalId().map(f -> "id=" + f + ", ").orElse("") +
            optionalNom().map(f -> "nom=" + f + ", ").orElse("") +
            optionalDateAjout().map(f -> "dateAjout=" + f + ", ").orElse("") +
            optionalModulesId().map(f -> "modulesId=" + f + ", ").orElse("") +
            optionalDistinct().map(f -> "distinct=" + f + ", ").orElse("") +
        "}";
    }
}
