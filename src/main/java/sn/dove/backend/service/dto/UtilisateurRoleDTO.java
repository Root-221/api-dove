package sn.dove.backend.service.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

/**
 * A DTO for the {@link sn.dove.backend.domain.UtilisateurRole} entity.
 */
@Schema(description = "Classe d'association entre Utilisateur et Role.")
@SuppressWarnings("common-java:DuplicatedBlocks")
public class UtilisateurRoleDTO implements Serializable {

    private UUID id;

    @NotNull
    private UtilisateurDTO utilisateur;

    @NotNull
    private RoleDTO role;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UtilisateurDTO getUtilisateur() {
        return utilisateur;
    }

    public void setUtilisateur(UtilisateurDTO utilisateur) {
        this.utilisateur = utilisateur;
    }

    public RoleDTO getRole() {
        return role;
    }

    public void setRole(RoleDTO role) {
        this.role = role;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof UtilisateurRoleDTO)) {
            return false;
        }

        UtilisateurRoleDTO utilisateurRoleDTO = (UtilisateurRoleDTO) o;
        if (this.id == null) {
            return false;
        }
        return Objects.equals(this.id, utilisateurRoleDTO.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id);
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "UtilisateurRoleDTO{" +
            "id='" + getId() + "'" +
            ", utilisateur=" + getUtilisateur() +
            ", role=" + getRole() +
            "}";
    }
}
