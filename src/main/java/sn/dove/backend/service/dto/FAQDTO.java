package sn.dove.backend.service.dto;

import jakarta.persistence.Lob;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

/**
 * A DTO for the {@link sn.dove.backend.domain.FAQ} entity.
 */
@SuppressWarnings("common-java:DuplicatedBlocks")
public class FAQDTO implements Serializable {

    private UUID id;

    @NotNull
    @Size(max = 255)
    private String titre;

    @Lob
    private String description;

    @Lob
    private String question;

    @Lob
    private String reponse;

    @NotNull
    private ContenuDTO contenu;

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

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public String getReponse() {
        return reponse;
    }

    public void setReponse(String reponse) {
        this.reponse = reponse;
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
        if (!(o instanceof FAQDTO)) {
            return false;
        }

        FAQDTO fAQDTO = (FAQDTO) o;
        if (this.id == null) {
            return false;
        }
        return Objects.equals(this.id, fAQDTO.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id);
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "FAQDTO{" +
            "id='" + getId() + "'" +
            ", titre='" + getTitre() + "'" +
            ", description='" + getDescription() + "'" +
            ", question='" + getQuestion() + "'" +
            ", reponse='" + getReponse() + "'" +
            ", contenu=" + getContenu() +
            "}";
    }
}
