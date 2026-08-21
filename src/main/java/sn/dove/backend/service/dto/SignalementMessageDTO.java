package sn.dove.backend.service.dto;

import jakarta.persistence.Lob;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * A DTO for the {@link sn.dove.backend.domain.SignalementMessage} entity.
 */
@SuppressWarnings("common-java:DuplicatedBlocks")
public class SignalementMessageDTO implements Serializable {

    private UUID id;

    @Lob
    private String motif;

    @NotNull
    private Instant dateSignalement;

    @NotNull
    private UtilisateurDTO utilisateur;

    @NotNull
    private MessageDTO message;

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

    public UtilisateurDTO getUtilisateur() {
        return utilisateur;
    }

    public void setUtilisateur(UtilisateurDTO utilisateur) {
        this.utilisateur = utilisateur;
    }

    public MessageDTO getMessage() {
        return message;
    }

    public void setMessage(MessageDTO message) {
        this.message = message;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof SignalementMessageDTO)) {
            return false;
        }

        SignalementMessageDTO signalementMessageDTO = (SignalementMessageDTO) o;
        if (this.id == null) {
            return false;
        }
        return Objects.equals(this.id, signalementMessageDTO.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id);
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "SignalementMessageDTO{" +
            "id='" + getId() + "'" +
            ", motif='" + getMotif() + "'" +
            ", dateSignalement='" + getDateSignalement() + "'" +
            ", utilisateur=" + getUtilisateur() +
            ", message=" + getMessage() +
            "}";
    }
}
