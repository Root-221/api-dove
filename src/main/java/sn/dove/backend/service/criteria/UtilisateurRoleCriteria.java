package sn.dove.backend.service.criteria;

import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;
import java.util.Optional;
import org.springdoc.core.annotations.ParameterObject;
import tech.jhipster.service.Criteria;
import tech.jhipster.service.filter.*;

/**
 * Criteria class for the {@link sn.dove.backend.domain.UtilisateurRole} entity. This class is used
 * in {@link sn.dove.backend.web.rest.UtilisateurRoleResource} to receive all the possible filtering options from
 * the Http GET request parameters.
 * For example the following could be a valid request:
 * {@code /utilisateur-roles?id.greaterThan=5&attr1.contains=something&attr2.specified=false}
 * As Spring is unable to properly convert the types, unless specific {@link Filter} class are used, we need to use
 * fix type specific filters.
 */
@ParameterObject
@SuppressWarnings("common-java:DuplicatedBlocks")
public class UtilisateurRoleCriteria implements Serializable, Criteria {

    @Serial
    private static final long serialVersionUID = 1L;

    private UUIDFilter id;

    private UUIDFilter utilisateurId;

    private UUIDFilter roleId;

    private Boolean distinct;

    public UtilisateurRoleCriteria() {}

    public UtilisateurRoleCriteria(UtilisateurRoleCriteria other) {
        this.id = other.optionalId().map(UUIDFilter::copy).orElse(null);
        this.utilisateurId = other.optionalUtilisateurId().map(UUIDFilter::copy).orElse(null);
        this.roleId = other.optionalRoleId().map(UUIDFilter::copy).orElse(null);
        this.distinct = other.distinct;
    }

    @Override
    public UtilisateurRoleCriteria copy() {
        return new UtilisateurRoleCriteria(this);
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

    public UUIDFilter getRoleId() {
        return roleId;
    }

    public Optional<UUIDFilter> optionalRoleId() {
        return Optional.ofNullable(roleId);
    }

    public UUIDFilter roleId() {
        if (roleId == null) {
            setRoleId(new UUIDFilter());
        }
        return roleId;
    }

    public void setRoleId(UUIDFilter roleId) {
        this.roleId = roleId;
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
        final UtilisateurRoleCriteria that = (UtilisateurRoleCriteria) o;
        return (
            Objects.equals(id, that.id) &&
            Objects.equals(utilisateurId, that.utilisateurId) &&
            Objects.equals(roleId, that.roleId) &&
            Objects.equals(distinct, that.distinct)
        );
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, utilisateurId, roleId, distinct);
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "UtilisateurRoleCriteria{" +
            optionalId().map(f -> "id=" + f + ", ").orElse("") +
            optionalUtilisateurId().map(f -> "utilisateurId=" + f + ", ").orElse("") +
            optionalRoleId().map(f -> "roleId=" + f + ", ").orElse("") +
            optionalDistinct().map(f -> "distinct=" + f + ", ").orElse("") +
        "}";
    }
}
