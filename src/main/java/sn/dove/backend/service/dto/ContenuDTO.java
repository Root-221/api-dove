package sn.dove.backend.service.dto;

import jakarta.persistence.Lob;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;
import sn.dove.backend.domain.enumeration.StatutContenu;
import sn.dove.backend.domain.enumeration.TypeContenu;

/**
 * A DTO for the {@link sn.dove.backend.domain.Contenu} entity.
 */
@SuppressWarnings("common-java:DuplicatedBlocks")
public class ContenuDTO implements Serializable {

    private UUID id;

    @NotNull
    @Size(max = 255)
    private String titre;

    @Lob
    private String description;

    @NotNull
    private TypeContenu typeContenu;

    @NotNull
    private StatutContenu statut;

    @NotNull
    @Min(value = 1)
    private Integer version;

    private Instant datePublication;

    @NotNull
    private Instant derniereMiseAJour;

    private Instant prochaineRevue;

    @Lob
    private String motsCles;

    @Lob
    private String transcription;

    @Min(value = 0)
    @Max(value = 100)
    private Integer progression;

    @NotNull
    private Instant dateCreation;

    @NotNull
    private ApplicationDTO application;

    @NotNull
    private ModuleDTO module;

    @NotNull
    private MetierDTO metier;

    @NotNull
    private UtilisateurDTO auteur;

    private UtilisateurDTO validateur;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getTitre() {
        return titre;
    }

    public void setTitre(String titre) {
        this.titre = titre;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public TypeContenu getTypeContenu() {
        return typeContenu;
    }

    public void setTypeContenu(TypeContenu typeContenu) {
        this.typeContenu = typeContenu;
    }

    public StatutContenu getStatut() {
        return statut;
    }

    public void setStatut(StatutContenu statut) {
        this.statut = statut;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    public Instant getDatePublication() {
        return datePublication;
    }

    public void setDatePublication(Instant datePublication) {
        this.datePublication = datePublication;
    }

    public Instant getDerniereMiseAJour() {
        return derniereMiseAJour;
    }

    public void setDerniereMiseAJour(Instant derniereMiseAJour) {
        this.derniereMiseAJour = derniereMiseAJour;
    }

    public Instant getProchaineRevue() {
        return prochaineRevue;
    }

    public void setProchaineRevue(Instant prochaineRevue) {
        this.prochaineRevue = prochaineRevue;
    }

    public String getMotsCles() {
        return motsCles;
    }

    public void setMotsCles(String motsCles) {
        this.motsCles = motsCles;
    }

    public String getTranscription() {
        return transcription;
    }

    public void setTranscription(String transcription) {
        this.transcription = transcription;
    }

    public Integer getProgression() {
        return progression;
    }

    public void setProgression(Integer progression) {
        this.progression = progression;
    }

    public Instant getDateCreation() {
        return dateCreation;
    }

    public void setDateCreation(Instant dateCreation) {
        this.dateCreation = dateCreation;
    }

    public ApplicationDTO getApplication() {
        return application;
    }

    public void setApplication(ApplicationDTO application) {
        this.application = application;
    }

    public ModuleDTO getModule() {
        return module;
    }

    public void setModule(ModuleDTO module) {
        this.module = module;
    }

    public MetierDTO getMetier() {
        return metier;
    }

    public void setMetier(MetierDTO metier) {
        this.metier = metier;
    }

    public UtilisateurDTO getAuteur() {
        return auteur;
    }

    public void setAuteur(UtilisateurDTO auteur) {
        this.auteur = auteur;
    }

    public UtilisateurDTO getValidateur() {
        return validateur;
    }

    public void setValidateur(UtilisateurDTO validateur) {
        this.validateur = validateur;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ContenuDTO)) {
            return false;
        }

        ContenuDTO contenuDTO = (ContenuDTO) o;
        if (this.id == null) {
            return false;
        }
        return Objects.equals(this.id, contenuDTO.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id);
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "ContenuDTO{" +
            "id='" + getId() + "'" +
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
            ", application=" + getApplication() +
            ", module=" + getModule() +
            ", metier=" + getMetier() +
            ", auteur=" + getAuteur() +
            ", validateur=" + getValidateur() +
            "}";
    }
}
