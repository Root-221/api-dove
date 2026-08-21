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
import sn.dove.backend.domain.enumeration.StatutSignalement;

/**
 * A Signalement.
 */
@Entity
@Table(name = "signalements")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@SuppressWarnings("common-java:DuplicatedBlocks")
public class Signalement implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue
    @Column(name = "id")
    private UUID id;

    @Lob
    @Column(name = "motif", nullable = false)
    private String motif;

    @NotNull
    @Column(name = "date_signalement", nullable = false)
    private Instant dateSignalement;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "statut", nullable = false)
    private StatutSignalement statut;

    @ManyToOne(optional = false)
    @NotNull
    @JsonIgnoreProperties(value = { "communauteNandites" }, allowSetters = true)
    private Utilisateur utilisateur;

    @ManyToOne(optional = false)
    @NotNull
    @JsonIgnoreProperties(value = { "application", "module", "metier", "auteur", "validateur" }, allowSetters = true)
    private Contenu contenu;

    // jhipster-needle-entity-add-field - JHipster will add fields here

    public UUID getId() {
        return this.id;
    }

    public Signalement id(UUID id) {
        this.setId(id);
        return this;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getMotif() {
        return this.motif;
    }

    public Signalement motif(String motif) {
        this.setMotif(motif);
        return this;
    }

    public void setMotif(String motif) {
        this.motif = motif;
    }

    public Instant getDateSignalement() {
        return this.dateSignalement;
    }

    public Signalement dateSignalement(Instant dateSignalement) {
        this.setDateSignalement(dateSignalement);
        return this;
    }

    public void setDateSignalement(Instant dateSignalement) {
        this.dateSignalement = dateSignalement;
    }

    public StatutSignalement getStatut() {
        return this.statut;
    }

    public Signalement statut(StatutSignalement statut) {
        this.setStatut(statut);
        return this;
    }

    public void setStatut(StatutSignalement statut) {
        this.statut = statut;
    }

    public Utilisateur getUtilisateur() {
        return this.utilisateur;
    }

    public void setUtilisateur(Utilisateur utilisateur) {
        this.utilisateur = utilisateur;
    }

    public Signalement utilisateur(Utilisateur utilisateur) {
        this.setUtilisateur(utilisateur);
        return this;
    }

    public Contenu getContenu() {
        return this.contenu;
    }

    public void setContenu(Contenu contenu) {
        this.contenu = contenu;
    }

    public Signalement contenu(Contenu contenu) {
        this.setContenu(contenu);
        return this;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Signalement)) {
            return false;
        }
        return getId() != null && getId().equals(((Signalement) o).getId());
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "Signalement{" +
            "id=" + getId() +
            ", motif='" + getMotif() + "'" +
            ", dateSignalement='" + getDateSignalement() + "'" +
            ", statut='" + getStatut() + "'" +
            "}";
    }
}
