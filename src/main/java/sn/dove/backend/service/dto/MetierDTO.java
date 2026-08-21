package sn.dove.backend.service.dto;

import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

/**
 * A DTO for the {@link sn.dove.backend.domain.Metier} entity.
 */
@SuppressWarnings("common-java:DuplicatedBlocks")
public class MetierDTO implements Serializable {

    private UUID id;

    @NotNull
    @Size(max = 150)
    private String libelle;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getLibelle() {
        return libelle;
    }

    public void setLibelle(String libelle) {
        this.libelle = libelle;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof MetierDTO)) {
            return false;
        }

        MetierDTO metierDTO = (MetierDTO) o;
        if (this.id == null) {
            return false;
        }
        return Objects.equals(this.id, metierDTO.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id);
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "MetierDTO{" +
            "id='" + getId() + "'" +
            ", libelle='" + getLibelle() + "'" +
            "}";
    }
}
