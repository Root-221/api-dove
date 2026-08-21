package sn.dove.backend.service.dto;

import jakarta.persistence.Lob;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

/**
 * A DTO for the {@link sn.dove.backend.domain.Media} entity.
 */
@SuppressWarnings("common-java:DuplicatedBlocks")
public class MediaDTO implements Serializable {

    private UUID id;

    @NotNull
    @Size(max = 255)
    private String titre;

    @Min(value = 0)
    private Integer etape;

    @Lob
    private String description;

    @NotNull
    @Size(max = 512)
    private String url;

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

    public Integer getEtape() {
        return etape;
    }

    public void setEtape(Integer etape) {
        this.etape = etape;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
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
        if (!(o instanceof MediaDTO)) {
            return false;
        }

        MediaDTO mediaDTO = (MediaDTO) o;
        if (this.id == null) {
            return false;
        }
        return Objects.equals(this.id, mediaDTO.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id);
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "MediaDTO{" +
            "id='" + getId() + "'" +
            ", titre='" + getTitre() + "'" +
            ", etape=" + getEtape() +
            ", description='" + getDescription() + "'" +
            ", url='" + getUrl() + "'" +
            ", contenu=" + getContenu() +
            "}";
    }
}
