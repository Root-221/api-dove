package sn.dove.backend.service.dto;

import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

/**
 * A DTO for the {@link sn.dove.backend.domain.Module} entity.
 */
@SuppressWarnings("common-java:DuplicatedBlocks")
public class ModuleDTO implements Serializable {

    private UUID id;

    @NotNull
    @Size(max = 150)
    private String libelle;

    @NotNull
    private ApplicationDTO application;

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

    public ApplicationDTO getApplication() {
        return application;
    }

    public void setApplication(ApplicationDTO application) {
        this.application = application;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ModuleDTO)) {
            return false;
        }

        ModuleDTO moduleDTO = (ModuleDTO) o;
        if (this.id == null) {
            return false;
        }
        return Objects.equals(this.id, moduleDTO.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id);
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "ModuleDTO{" +
            "id='" + getId() + "'" +
            ", libelle='" + getLibelle() + "'" +
            ", application=" + getApplication() +
            "}";
    }
}
