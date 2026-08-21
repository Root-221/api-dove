package sn.dove.backend.service.criteria;

import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;
import java.util.Optional;
import org.springdoc.core.annotations.ParameterObject;
import sn.dove.backend.domain.enumeration.StatutContenu;
import sn.dove.backend.domain.enumeration.TypeContenu;
import tech.jhipster.service.Criteria;
import tech.jhipster.service.filter.*;

/**
 * Criteria class for the {@link sn.dove.backend.domain.Contenu} entity. This class is used
 * in {@link sn.dove.backend.web.rest.ContenuResource} to receive all the possible filtering options from
 * the Http GET request parameters.
 * For example the following could be a valid request:
 * {@code /contenus?id.greaterThan=5&attr1.contains=something&attr2.specified=false}
 * As Spring is unable to properly convert the types, unless specific {@link Filter} class are used, we need to use
 * fix type specific filters.
 */
@ParameterObject
@SuppressWarnings("common-java:DuplicatedBlocks")
public class ContenuCriteria implements Serializable, Criteria {

    /**
     * Class for filtering TypeContenu
     */
    public static class TypeContenuFilter extends Filter<TypeContenu> {

        public TypeContenuFilter() {}

        public TypeContenuFilter(TypeContenuFilter filter) {
            super(filter);
        }

        @Override
        public TypeContenuFilter copy() {
            return new TypeContenuFilter(this);
        }
    }

    /**
     * Class for filtering StatutContenu
     */
    public static class StatutContenuFilter extends Filter<StatutContenu> {

        public StatutContenuFilter() {}

        public StatutContenuFilter(StatutContenuFilter filter) {
            super(filter);
        }

        @Override
        public StatutContenuFilter copy() {
            return new StatutContenuFilter(this);
        }
    }

    @Serial
    private static final long serialVersionUID = 1L;

    private UUIDFilter id;

    private StringFilter titre;

    private TypeContenuFilter typeContenu;

    private StatutContenuFilter statut;

    private IntegerFilter version;

    private InstantFilter datePublication;

    private InstantFilter derniereMiseAJour;

    private InstantFilter prochaineRevue;

    private IntegerFilter progression;

    private InstantFilter dateCreation;

    private UUIDFilter applicationId;

    private UUIDFilter moduleId;

    private UUIDFilter metierId;

    private UUIDFilter auteurId;

    private UUIDFilter validateurId;

    private Boolean distinct;

    public ContenuCriteria() {}

    public ContenuCriteria(ContenuCriteria other) {
        this.id = other.optionalId().map(UUIDFilter::copy).orElse(null);
        this.titre = other.optionalTitre().map(StringFilter::copy).orElse(null);
        this.typeContenu = other.optionalTypeContenu().map(TypeContenuFilter::copy).orElse(null);
        this.statut = other.optionalStatut().map(StatutContenuFilter::copy).orElse(null);
        this.version = other.optionalVersion().map(IntegerFilter::copy).orElse(null);
        this.datePublication = other.optionalDatePublication().map(InstantFilter::copy).orElse(null);
        this.derniereMiseAJour = other.optionalDerniereMiseAJour().map(InstantFilter::copy).orElse(null);
        this.prochaineRevue = other.optionalProchaineRevue().map(InstantFilter::copy).orElse(null);
        this.progression = other.optionalProgression().map(IntegerFilter::copy).orElse(null);
        this.dateCreation = other.optionalDateCreation().map(InstantFilter::copy).orElse(null);
        this.applicationId = other.optionalApplicationId().map(UUIDFilter::copy).orElse(null);
        this.moduleId = other.optionalModuleId().map(UUIDFilter::copy).orElse(null);
        this.metierId = other.optionalMetierId().map(UUIDFilter::copy).orElse(null);
        this.auteurId = other.optionalAuteurId().map(UUIDFilter::copy).orElse(null);
        this.validateurId = other.optionalValidateurId().map(UUIDFilter::copy).orElse(null);
        this.distinct = other.distinct;
    }

    @Override
    public ContenuCriteria copy() {
        return new ContenuCriteria(this);
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

    public TypeContenuFilter getTypeContenu() {
        return typeContenu;
    }

    public Optional<TypeContenuFilter> optionalTypeContenu() {
        return Optional.ofNullable(typeContenu);
    }

    public TypeContenuFilter typeContenu() {
        if (typeContenu == null) {
            setTypeContenu(new TypeContenuFilter());
        }
        return typeContenu;
    }

    public void setTypeContenu(TypeContenuFilter typeContenu) {
        this.typeContenu = typeContenu;
    }

    public StatutContenuFilter getStatut() {
        return statut;
    }

    public Optional<StatutContenuFilter> optionalStatut() {
        return Optional.ofNullable(statut);
    }

    public StatutContenuFilter statut() {
        if (statut == null) {
            setStatut(new StatutContenuFilter());
        }
        return statut;
    }

    public void setStatut(StatutContenuFilter statut) {
        this.statut = statut;
    }

    public IntegerFilter getVersion() {
        return version;
    }

    public Optional<IntegerFilter> optionalVersion() {
        return Optional.ofNullable(version);
    }

    public IntegerFilter version() {
        if (version == null) {
            setVersion(new IntegerFilter());
        }
        return version;
    }

    public void setVersion(IntegerFilter version) {
        this.version = version;
    }

    public InstantFilter getDatePublication() {
        return datePublication;
    }

    public Optional<InstantFilter> optionalDatePublication() {
        return Optional.ofNullable(datePublication);
    }

    public InstantFilter datePublication() {
        if (datePublication == null) {
            setDatePublication(new InstantFilter());
        }
        return datePublication;
    }

    public void setDatePublication(InstantFilter datePublication) {
        this.datePublication = datePublication;
    }

    public InstantFilter getDerniereMiseAJour() {
        return derniereMiseAJour;
    }

    public Optional<InstantFilter> optionalDerniereMiseAJour() {
        return Optional.ofNullable(derniereMiseAJour);
    }

    public InstantFilter derniereMiseAJour() {
        if (derniereMiseAJour == null) {
            setDerniereMiseAJour(new InstantFilter());
        }
        return derniereMiseAJour;
    }

    public void setDerniereMiseAJour(InstantFilter derniereMiseAJour) {
        this.derniereMiseAJour = derniereMiseAJour;
    }

    public InstantFilter getProchaineRevue() {
        return prochaineRevue;
    }

    public Optional<InstantFilter> optionalProchaineRevue() {
        return Optional.ofNullable(prochaineRevue);
    }

    public InstantFilter prochaineRevue() {
        if (prochaineRevue == null) {
            setProchaineRevue(new InstantFilter());
        }
        return prochaineRevue;
    }

    public void setProchaineRevue(InstantFilter prochaineRevue) {
        this.prochaineRevue = prochaineRevue;
    }

    public IntegerFilter getProgression() {
        return progression;
    }

    public Optional<IntegerFilter> optionalProgression() {
        return Optional.ofNullable(progression);
    }

    public IntegerFilter progression() {
        if (progression == null) {
            setProgression(new IntegerFilter());
        }
        return progression;
    }

    public void setProgression(IntegerFilter progression) {
        this.progression = progression;
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

    public UUIDFilter getApplicationId() {
        return applicationId;
    }

    public Optional<UUIDFilter> optionalApplicationId() {
        return Optional.ofNullable(applicationId);
    }

    public UUIDFilter applicationId() {
        if (applicationId == null) {
            setApplicationId(new UUIDFilter());
        }
        return applicationId;
    }

    public void setApplicationId(UUIDFilter applicationId) {
        this.applicationId = applicationId;
    }

    public UUIDFilter getModuleId() {
        return moduleId;
    }

    public Optional<UUIDFilter> optionalModuleId() {
        return Optional.ofNullable(moduleId);
    }

    public UUIDFilter moduleId() {
        if (moduleId == null) {
            setModuleId(new UUIDFilter());
        }
        return moduleId;
    }

    public void setModuleId(UUIDFilter moduleId) {
        this.moduleId = moduleId;
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

    public UUIDFilter getAuteurId() {
        return auteurId;
    }

    public Optional<UUIDFilter> optionalAuteurId() {
        return Optional.ofNullable(auteurId);
    }

    public UUIDFilter auteurId() {
        if (auteurId == null) {
            setAuteurId(new UUIDFilter());
        }
        return auteurId;
    }

    public void setAuteurId(UUIDFilter auteurId) {
        this.auteurId = auteurId;
    }

    public UUIDFilter getValidateurId() {
        return validateurId;
    }

    public Optional<UUIDFilter> optionalValidateurId() {
        return Optional.ofNullable(validateurId);
    }

    public UUIDFilter validateurId() {
        if (validateurId == null) {
            setValidateurId(new UUIDFilter());
        }
        return validateurId;
    }

    public void setValidateurId(UUIDFilter validateurId) {
        this.validateurId = validateurId;
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
        final ContenuCriteria that = (ContenuCriteria) o;
        return (
            Objects.equals(id, that.id) &&
            Objects.equals(titre, that.titre) &&
            Objects.equals(typeContenu, that.typeContenu) &&
            Objects.equals(statut, that.statut) &&
            Objects.equals(version, that.version) &&
            Objects.equals(datePublication, that.datePublication) &&
            Objects.equals(derniereMiseAJour, that.derniereMiseAJour) &&
            Objects.equals(prochaineRevue, that.prochaineRevue) &&
            Objects.equals(progression, that.progression) &&
            Objects.equals(dateCreation, that.dateCreation) &&
            Objects.equals(applicationId, that.applicationId) &&
            Objects.equals(moduleId, that.moduleId) &&
            Objects.equals(metierId, that.metierId) &&
            Objects.equals(auteurId, that.auteurId) &&
            Objects.equals(validateurId, that.validateurId) &&
            Objects.equals(distinct, that.distinct)
        );
    }

    @Override
    public int hashCode() {
        return Objects.hash(
            id,
            titre,
            typeContenu,
            statut,
            version,
            datePublication,
            derniereMiseAJour,
            prochaineRevue,
            progression,
            dateCreation,
            applicationId,
            moduleId,
            metierId,
            auteurId,
            validateurId,
            distinct
        );
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "ContenuCriteria{" +
            optionalId().map(f -> "id=" + f + ", ").orElse("") +
            optionalTitre().map(f -> "titre=" + f + ", ").orElse("") +
            optionalTypeContenu().map(f -> "typeContenu=" + f + ", ").orElse("") +
            optionalStatut().map(f -> "statut=" + f + ", ").orElse("") +
            optionalVersion().map(f -> "version=" + f + ", ").orElse("") +
            optionalDatePublication().map(f -> "datePublication=" + f + ", ").orElse("") +
            optionalDerniereMiseAJour().map(f -> "derniereMiseAJour=" + f + ", ").orElse("") +
            optionalProchaineRevue().map(f -> "prochaineRevue=" + f + ", ").orElse("") +
            optionalProgression().map(f -> "progression=" + f + ", ").orElse("") +
            optionalDateCreation().map(f -> "dateCreation=" + f + ", ").orElse("") +
            optionalApplicationId().map(f -> "applicationId=" + f + ", ").orElse("") +
            optionalModuleId().map(f -> "moduleId=" + f + ", ").orElse("") +
            optionalMetierId().map(f -> "metierId=" + f + ", ").orElse("") +
            optionalAuteurId().map(f -> "auteurId=" + f + ", ").orElse("") +
            optionalValidateurId().map(f -> "validateurId=" + f + ", ").orElse("") +
            optionalDistinct().map(f -> "distinct=" + f + ", ").orElse("") +
        "}";
    }
}
