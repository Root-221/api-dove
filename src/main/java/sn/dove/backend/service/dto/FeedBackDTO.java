package sn.dove.backend.service.dto;

import jakarta.persistence.Lob;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * A DTO for the {@link sn.dove.backend.domain.FeedBack} entity.
 */
@SuppressWarnings("common-java:DuplicatedBlocks")
public class FeedBackDTO implements Serializable {

    private UUID id;

    @Lob
    private String commentaire;

    @Min(value = 0)
    @Max(value = 5)
    private Integer note;

    @NotNull
    private Instant dateCreation;

    @NotNull
    @Size(max = 80)
    private String statutTraitement;

    @NotNull
    private UtilisateurDTO utilisateur;

    @NotNull
    private ContenuDTO contenu;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getCommentaire() {
        return commentaire;
    }

    public void setCommentaire(String commentaire) {
        this.commentaire = commentaire;
    }

    public Integer getNote() {
        return note;
    }

    public void setNote(Integer note) {
        this.note = note;
    }

    public Instant getDateCreation() {
        return dateCreation;
    }

    public void setDateCreation(Instant dateCreation) {
        this.dateCreation = dateCreation;
    }

    public String getStatutTraitement() {
        return statutTraitement;
    }

    public void setStatutTraitement(String statutTraitement) {
        this.statutTraitement = statutTraitement;
    }

    public UtilisateurDTO getUtilisateur() {
        return utilisateur;
    }

    public void setUtilisateur(UtilisateurDTO utilisateur) {
        this.utilisateur = utilisateur;
    }

    public ContenuDTO getContenu() {
        return contenu;
    }

    public void setContenu(ContenuDTO contenu) {
        this.contenu = contenu;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof FeedBackDTO)) {
            return false;
        }

        FeedBackDTO feedBackDTO = (FeedBackDTO) o;
        if (this.id == null) {
            return false;
        }
        return Objects.equals(this.id, feedBackDTO.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id);
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "FeedBackDTO{" +
            "id='" + getId() + "'" +
            ", commentaire='" + getCommentaire() + "'" +
            ", note=" + getNote() +
            ", dateCreation='" + getDateCreation() + "'" +
            ", statutTraitement='" + getStatutTraitement() + "'" +
            ", utilisateur=" + getUtilisateur() +
            ", contenu=" + getContenu() +
            "}";
    }
}
