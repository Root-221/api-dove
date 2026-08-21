package sn.dove.backend.service.dto;

import jakarta.persistence.Lob;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;
import sn.dove.backend.domain.enumeration.StatutSignalement;

/**
 * A DTO for the {@link sn.dove.backend.domain.Signalement} entity.
 */
@SuppressWarnings("common-java:DuplicatedBlocks")
public class SignalementDTO implements Serializable {

    private UUID id;

    @Lob
    private String motif;

    @NotNull
    private Instant dateSignalement;

    @NotNull
    private StatutSignalement statut;

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

    public String getMotif() {
        return motif;
    }

    public void setMotif(String motif) {
        this.motif = motif;
    }

    public Instant getDateSignalement() {
        return dateSignalement;
    }

    public void setDateSignalement(Instant dateSignalement) {
        this.dateSignalement = dateSignalement;
    }

    public StatutSignalement getStatut() {
        return statut;
    }

    public void setStatut(StatutSignalement statut) {
        this.statut = statut;
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
        if (!(o instanceof SignalementDTO)) {
            return false;
        }

        SignalementDTO signalementDTO = (SignalementDTO) o;
        if (this.id == null) {
            return false;
        }
        return Objects.equals(this.id, signalementDTO.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id);
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "SignalementDTO{" +
            "id='" + getId() + "'" +
            ", motif='" + getMotif() + "'" +
            ", dateSignalement='" + getDateSignalement() + "'" +
            ", statut='" + getStatut() + "'" +
            ", utilisateur=" + getUtilisateur() +
            ", contenu=" + getContenu() +
            "}";
    }
}
