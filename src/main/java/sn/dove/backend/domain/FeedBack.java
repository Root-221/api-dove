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

/**
 * A FeedBack.
 */
@Entity
@Table(name = "feed_backs")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@SuppressWarnings("common-java:DuplicatedBlocks")
public class FeedBack implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue
    @Column(name = "id")
    private UUID id;

    @Lob
    @Column(name = "commentaire")
    private String commentaire;

    @Min(value = 0)
    @Max(value = 5)
    @Column(name = "note")
    private Integer note;

    @NotNull
    @Column(name = "date_creation", nullable = false)
    private Instant dateCreation;

    @NotNull
    @Size(max = 80)
    @Column(name = "statut_traitement", length = 80, nullable = false)
    private String statutTraitement;

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

    public FeedBack id(UUID id) {
        this.setId(id);
        return this;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getCommentaire() {
        return this.commentaire;
    }

    public FeedBack commentaire(String commentaire) {
        this.setCommentaire(commentaire);
        return this;
    }

    public void setCommentaire(String commentaire) {
        this.commentaire = commentaire;
    }

    public Integer getNote() {
        return this.note;
    }

    public FeedBack note(Integer note) {
        this.setNote(note);
        return this;
    }

    public void setNote(Integer note) {
        this.note = note;
    }

    public Instant getDateCreation() {
        return this.dateCreation;
    }

    public FeedBack dateCreation(Instant dateCreation) {
        this.setDateCreation(dateCreation);
        return this;
    }

    public void setDateCreation(Instant dateCreation) {
        this.dateCreation = dateCreation;
    }

    public String getStatutTraitement() {
        return this.statutTraitement;
    }

    public FeedBack statutTraitement(String statutTraitement) {
        this.setStatutTraitement(statutTraitement);
        return this;
    }

    public void setStatutTraitement(String statutTraitement) {
        this.statutTraitement = statutTraitement;
    }

    public Utilisateur getUtilisateur() {
        return this.utilisateur;
    }

    public void setUtilisateur(Utilisateur utilisateur) {
        this.utilisateur = utilisateur;
    }

    public FeedBack utilisateur(Utilisateur utilisateur) {
        this.setUtilisateur(utilisateur);
        return this;
    }

    public Contenu getContenu() {
        return this.contenu;
    }

    public void setContenu(Contenu contenu) {
        this.contenu = contenu;
    }

    public FeedBack contenu(Contenu contenu) {
        this.setContenu(contenu);
        return this;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof FeedBack)) {
            return false;
        }
        return getId() != null && getId().equals(((FeedBack) o).getId());
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "FeedBack{" +
            "id=" + getId() +
            ", commentaire='" + getCommentaire() + "'" +
            ", note=" + getNote() +
            ", dateCreation='" + getDateCreation() + "'" +
            ", statutTraitement='" + getStatutTraitement() + "'" +
            "}";
    }


}
