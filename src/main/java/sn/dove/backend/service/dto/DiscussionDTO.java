package sn.dove.backend.service.dto;

import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * A DTO for the {@link sn.dove.backend.domain.Discussion} entity.
 */
@SuppressWarnings("common-java:DuplicatedBlocks")
public class DiscussionDTO implements Serializable {

    private UUID id;

    @NotNull
    @Size(max = 255)
    private String titre;

    @NotNull
    private Instant dateCreation;

    @NotNull
    private UtilisateurDTO createur;

    @NotNull
    private CommunauteNanditeDTO communauteNandite;

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

    public Instant getDateCreation() {
        return dateCreation;
    }

    public void setDateCreation(Instant dateCreation) {
        this.dateCreation = dateCreation;
    }

    public UtilisateurDTO getCreateur() {
        return createur;
    }

    public void setCreateur(UtilisateurDTO createur) {
        this.createur = createur;
    }

    public CommunauteNanditeDTO getCommunauteNandite() {
        return communauteNandite;
    }

    public void setCommunauteNandite(CommunauteNanditeDTO communauteNandite) {
        this.communauteNandite = communauteNandite;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof DiscussionDTO)) {
            return false;
        }

        DiscussionDTO discussionDTO = (DiscussionDTO) o;
        if (this.id == null) {
            return false;
        }
        return Objects.equals(this.id, discussionDTO.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id);
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "DiscussionDTO{" +
            "id='" + getId() + "'" +
            ", titre='" + getTitre() + "'" +
            ", dateCreation='" + getDateCreation() + "'" +
            ", createur=" + getCreateur() +
            ", communauteNandite=" + getCommunauteNandite() +
            "}";
    }
}
