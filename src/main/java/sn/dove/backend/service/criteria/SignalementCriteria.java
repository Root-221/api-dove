package sn.dove.backend.service.criteria;

import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;
import java.util.Optional;
import org.springdoc.core.annotations.ParameterObject;
import sn.dove.backend.domain.enumeration.StatutSignalement;
import tech.jhipster.service.Criteria;
import tech.jhipster.service.filter.*;

/**
 * Criteria class for the {@link sn.dove.backend.domain.Signalement} entity. This class is used
 * in {@link sn.dove.backend.web.rest.SignalementResource} to receive all the possible filtering options from
 * the Http GET request parameters.
 * For example the following could be a valid request:
 * {@code /signalements?id.greaterThan=5&attr1.contains=something&attr2.specified=false}
 * As Spring is unable to properly convert the types, unless specific {@link Filter} class are used, we need to use
 * fix type specific filters.
 */
@ParameterObject
@SuppressWarnings("common-java:DuplicatedBlocks")
public class SignalementCriteria implements Serializable, Criteria {

    /**
     * Class for filtering StatutSignalement
     */
    public static class StatutSignalementFilter extends Filter<StatutSignalement> {

        public StatutSignalementFilter() {}

        public StatutSignalementFilter(StatutSignalementFilter filter) {
            super(filter);
        }

        @Override
        public StatutSignalementFilter copy() {
            return new StatutSignalementFilter(this);
        }
    }

    @Serial
    private static final long serialVersionUID = 1L;

    private UUIDFilter id;

    private InstantFilter dateSignalement;

    private StatutSignalementFilter statut;

    private UUIDFilter utilisateurId;

    private UUIDFilter contenuId;

    private Boolean distinct;

    public SignalementCriteria() {}

    public SignalementCriteria(SignalementCriteria other) {
        this.id = other.optionalId().map(UUIDFilter::copy).orElse(null);
        this.dateSignalement = other.optionalDateSignalement().map(InstantFilter::copy).orElse(null);
        this.statut = other.optionalStatut().map(StatutSignalementFilter::copy).orElse(null);
        this.utilisateurId = other.optionalUtilisateurId().map(UUIDFilter::copy).orElse(null);
        this.contenuId = other.optionalContenuId().map(UUIDFilter::copy).orElse(null);
        this.distinct = other.distinct;
    }

    @Override
    public SignalementCriteria copy() {
        return new SignalementCriteria(this);
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

    public StatutSignalementFilter getStatut() {
        return statut;
    }

    public Optional<StatutSignalementFilter> optionalStatut() {
        return Optional.ofNullable(statut);
    }

    public StatutSignalementFilter statut() {
        if (statut == null) {
            setStatut(new StatutSignalementFilter());
        }
        return statut;
    }

    public void setStatut(StatutSignalementFilter statut) {
        this.statut = statut;
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
        final SignalementCriteria that = (SignalementCriteria) o;
        return (
            Objects.equals(id, that.id) &&
            Objects.equals(dateSignalement, that.dateSignalement) &&
            Objects.equals(statut, that.statut) &&
            Objects.equals(utilisateurId, that.utilisateurId) &&
            Objects.equals(contenuId, that.contenuId) &&
            Objects.equals(distinct, that.distinct)
        );
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, dateSignalement, statut, utilisateurId, contenuId, distinct);
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "SignalementCriteria{" +
            optionalId().map(f -> "id=" + f + ", ").orElse("") +
            optionalDateSignalement().map(f -> "dateSignalement=" + f + ", ").orElse("") +
            optionalStatut().map(f -> "statut=" + f + ", ").orElse("") +
            optionalUtilisateurId().map(f -> "utilisateurId=" + f + ", ").orElse("") +
            optionalContenuId().map(f -> "contenuId=" + f + ", ").orElse("") +
            optionalDistinct().map(f -> "distinct=" + f + ", ").orElse("") +
        "}";
    }
}
