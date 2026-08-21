package sn.dove.backend.service.dto;

import jakarta.persistence.Lob;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

/**
 * A DTO for the {@link sn.dove.backend.domain.CommunauteNandite} entity.
 */
@SuppressWarnings("common-java:DuplicatedBlocks")
public class CommunauteNanditeDTO implements Serializable {

    private UUID id;

    @NotNull
    @Size(max = 150)
    private String nom;

    @Lob
    private String description;

    private Set<UtilisateurDTO> membreses = new HashSet<>();

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Set<UtilisateurDTO> getMembreses() {
        return membreses;
    }

    public void setMembreses(Set<UtilisateurDTO> membreses) {
        this.membreses = membreses;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof CommunauteNanditeDTO)) {
            return false;
        }

        CommunauteNanditeDTO communauteNanditeDTO = (CommunauteNanditeDTO) o;
        if (this.id == null) {
            return false;
        }
        return Objects.equals(this.id, communauteNanditeDTO.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id);
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "CommunauteNanditeDTO{" +
            "id='" + getId() + "'" +
            ", nom='" + getNom() + "'" +
            ", description='" + getDescription() + "'" +
            ", membreses=" + getMembreses() +
            "}";
    }
}
