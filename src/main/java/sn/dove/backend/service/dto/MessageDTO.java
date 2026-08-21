package sn.dove.backend.service.dto;

import jakarta.persistence.Lob;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * A DTO for the {@link sn.dove.backend.domain.Message} entity.
 */
@SuppressWarnings("common-java:DuplicatedBlocks")
public class MessageDTO implements Serializable {

    private UUID id;

    @Lob
    private String contenu;

    @NotNull
    private Instant dateEnvoi;

    @NotNull
    private UtilisateurDTO emetteur;

    private MetierDTO metier;

    @NotNull
    private DiscussionDTO discussion;

    private MessageDTO messageParent;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getContenu() {
        return contenu;
    }

    public void setContenu(String contenu) {
        this.contenu = contenu;
    }

    public Instant getDateEnvoi() {
        return dateEnvoi;
    }

    public void setDateEnvoi(Instant dateEnvoi) {
        this.dateEnvoi = dateEnvoi;
    }

    public UtilisateurDTO getEmetteur() {
        return emetteur;
    }

    public void setEmetteur(UtilisateurDTO emetteur) {
        this.emetteur = emetteur;
    }

    public MetierDTO getMetier() {
        return metier;
    }

    public void setMetier(MetierDTO metier) {
        this.metier = metier;
    }

    public DiscussionDTO getDiscussion() {
        return discussion;
    }

    public void setDiscussion(DiscussionDTO discussion) {
        this.discussion = discussion;
    }

    public MessageDTO getMessageParent() {
        return messageParent;
    }

    public void setMessageParent(MessageDTO messageParent) {
        this.messageParent = messageParent;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof MessageDTO)) {
            return false;
        }

        MessageDTO messageDTO = (MessageDTO) o;
        if (this.id == null) {
            return false;
        }
        return Objects.equals(this.id, messageDTO.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id);
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "MessageDTO{" +
            "id='" + getId() + "'" +
            ", contenu='" + getContenu() + "'" +
            ", dateEnvoi='" + getDateEnvoi() + "'" +
            ", emetteur=" + getEmetteur() +
            ", metier=" + getMetier() +
            ", discussion=" + getDiscussion() +
            ", messageParent=" + getMessageParent() +
            "}";
    }
}
