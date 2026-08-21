package sn.dove.backend.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import sn.dove.backend.domain.enumeration.StatutContenu;
import sn.dove.backend.domain.enumeration.TypeContenu;

/**
 * A Contenu.
 */
@Entity
@Table(name = "contenus")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@SuppressWarnings("common-java:DuplicatedBlocks")
public class Contenu implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue
    @Column(name = "id")
    private UUID id;

    @NotNull
    @Size(max = 255)
    @Column(name = "titre", length = 255, nullable = false)
    private String titre;

    @Lob
    @Column(name = "description", nullable = false)
    private String description;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "type_contenu", nullable = false)
    private TypeContenu typeContenu;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "statut", nullable = false)
    private StatutContenu statut;

    @NotNull
    @Min(value = 1)
    @Column(name = "version", nullable = false)
    private Integer version;

    @Column(name = "date_publication")
    private Instant datePublication;

    @NotNull
    @Column(name = "derniere_mise_a_jour", nullable = false)
    private Instant derniereMiseAJour;

    @Column(name = "prochaine_revue")
    private Instant prochaineRevue;

    @Lob
    @Column(name = "mots_cles")
    private String motsCles;

    @Lob
    @Column(name = "transcription")
    private String transcription;

    @Min(value = 0)
    @Max(value = 100)
    @Column(name = "progression")
    private Integer progression;

    @NotNull
    @Column(name = "date_creation", nullable = false)
    private Instant dateCreation;

    @ManyToOne(optional = false)
    @NotNull
    @JsonIgnoreProperties(value = { "moduleses" }, allowSetters = true)
    private Application application;

    @ManyToOne(optional = false)
    @NotNull
    @JsonIgnoreProperties(value = { "application" }, allowSetters = true)
    private Module module;

    @ManyToOne(optional = false)
    @NotNull
    private Metier metier;

    @ManyToOne(optional = false)
    @NotNull
    @JsonIgnoreProperties(value = { "communauteNandites" }, allowSetters = true)
    private Utilisateur auteur;

    @ManyToOne(fetch = FetchType.LAZY)
    @JsonIgnoreProperties(value = { "communauteNandites" }, allowSetters = true)
    private Utilisateur validateur;

    // jhipster-needle-entity-add-field - JHipster will add fields here

    public UUID getId() {
        return this.id;
    }

    public Contenu id(UUID id) {
        this.setId(id);
        return this;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getTitre() {
        return this.titre;
    }

    public Contenu titre(String titre) {
        this.setTitre(titre);
        return this;
    }

    public void setTitre(String titre) {
        this.titre = titre;
    }

    public String getDescription() {
        return this.description;
    }

    public Contenu description(String description) {
        this.setDescription(description);
        return this;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public TypeContenu getTypeContenu() {
        return this.typeContenu;
    }

    public Contenu typeContenu(TypeContenu typeContenu) {
        this.setTypeContenu(typeContenu);
        return this;
    }

    public void setTypeContenu(TypeContenu typeContenu) {
        this.typeContenu = typeContenu;
    }

    public StatutContenu getStatut() {
        return this.statut;
    }

    public Contenu statut(StatutContenu statut) {
        this.setStatut(statut);
        return this;
    }

    public void setStatut(StatutContenu statut) {
        this.statut = statut;
    }

    public Integer getVersion() {
        return this.version;
    }

    public Contenu version(Integer version) {
        this.setVersion(version);
        return this;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    public Instant getDatePublication() {
        return this.datePublication;
    }

    public Contenu datePublication(Instant datePublication) {
        this.setDatePublication(datePublication);
        return this;
    }

    public void setDatePublication(Instant datePublication) {
        this.datePublication = datePublication;
    }

    public Instant getDerniereMiseAJour() {
        return this.derniereMiseAJour;
    }

    public Contenu derniereMiseAJour(Instant derniereMiseAJour) {
        this.setDerniereMiseAJour(derniereMiseAJour);
        return this;
    }

    public void setDerniereMiseAJour(Instant derniereMiseAJour) {
        this.derniereMiseAJour = derniereMiseAJour;
    }

    public Instant getProchaineRevue() {
        return this.prochaineRevue;
    }

    public Contenu prochaineRevue(Instant prochaineRevue) {
        this.setProchaineRevue(prochaineRevue);
        return this;
    }

    public void setProchaineRevue(Instant prochaineRevue) {
        this.prochaineRevue = prochaineRevue;
    }

    public String getMotsCles() {
        return this.motsCles;
    }

    public Contenu motsCles(String motsCles) {
        this.setMotsCles(motsCles);
        return this;
    }

    public void setMotsCles(String motsCles) {
        this.motsCles = motsCles;
    }

    public String getTranscription() {
        return this.transcription;
    }

    public Contenu transcription(String transcription) {
        this.setTranscription(transcription);
        return this;
    }

    public void setTranscription(String transcription) {
        this.transcription = transcription;
    }

    public Integer getProgression() {
        return this.progression;
    }

    public Contenu progression(Integer progression) {
        this.setProgression(progression);
        return this;
    }

    public void setProgression(Integer progression) {
        this.progression = progression;
    }

    public Instant getDateCreation() {
        return this.dateCreation;
    }

    public Contenu dateCreation(Instant dateCreation) {
        this.setDateCreation(dateCreation);
        return this;
    }

    public void setDateCreation(Instant dateCreation) {
        this.dateCreation = dateCreation;
    }

    public Application getApplication() {
        return this.application;
    }

    public void setApplication(Application application) {
        this.application = application;
    }

    public Contenu application(Application application) {
        this.setApplication(application);
        return this;
    }

    public Module getModule() {
        return this.module;
    }

    public void setModule(Module module) {
        this.module = module;
    }

    public Contenu module(Module module) {
        this.setModule(module);
        return this;
    }

    public Metier getMetier() {
        return this.metier;
    }

    public void setMetier(Metier metier) {
        this.metier = metier;
    }

    public Contenu metier(Metier metier) {
        this.setMetier(metier);
        return this;
    }

    public Utilisateur getAuteur() {
        return this.auteur;
    }

    public void setAuteur(Utilisateur utilisateur) {
        this.auteur = utilisateur;
    }

    public Contenu auteur(Utilisateur utilisateur) {
        this.setAuteur(utilisateur);
        return this;
    }

    public Utilisateur getValidateur() {
        return this.validateur;
    }

    public void setValidateur(Utilisateur utilisateur) {
        this.validateur = utilisateur;
    }

    public Contenu validateur(Utilisateur utilisateur) {
        this.setValidateur(utilisateur);
        return this;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Contenu)) {
            return false;
        }
        return getId() != null && getId().equals(((Contenu) o).getId());
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "Contenu{" +
            "id=" + getId() +
            ", titre='" + getTitre() + "'" +
            ", description='" + getDescription() + "'" +
            ", typeContenu='" + getTypeContenu() + "'" +
            ", statut='" + getStatut() + "'" +
            ", version=" + getVersion() +
            ", datePublication='" + getDatePublication() + "'" +
            ", derniereMiseAJour='" + getDerniereMiseAJour() + "'" +
            ", prochaineRevue='" + getProchaineRevue() + "'" +
            ", motsCles='" + getMotsCles() + "'" +
            ", transcription='" + getTranscription() + "'" +
            ", progression=" + getProgression() +
            ", dateCreation='" + getDateCreation() + "'" +
            "}";
    }
}
