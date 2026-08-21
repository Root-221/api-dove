package sn.dove.backend.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

/**
 * A SignalementMessage.
 */
@Entity
@Table(name = "signalement_messages")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@SuppressWarnings("common-java:DuplicatedBlocks")
public class SignalementMessage implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue
    @Column(name = "id")
    private UUID id;

    @Lob
    @Column(name = "motif", nullable = false)
    private String motif;

    @NotNull
    @Column(name = "date_signalement", nullable = false)
    private Instant dateSignalement;

    @ManyToOne(optional = false)
    @NotNull
    @JsonIgnoreProperties(value = { "communauteNandites" }, allowSetters = true)
    private Utilisateur utilisateur;

    @ManyToOne(optional = false)
    @NotNull
    @JsonIgnoreProperties(
        value = { "reponseses", "signalementses", "emetteur", "metier", "discussion", "messageParent" },
        allowSetters = true
    )
    private Message message;

    // jhipster-needle-entity-add-field - JHipster will add fields here

    public UUID getId() {
        return this.id;
    }

    public SignalementMessage id(UUID id) {
        this.setId(id);
        return this;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getMotif() {
        return this.motif;
    }

    public SignalementMessage motif(String motif) {
        this.setMotif(motif);
        return this;
    }

    public void setMotif(String motif) {
        this.motif = motif;
    }

    public Instant getDateSignalement() {
        return this.dateSignalement;
    }

    public SignalementMessage dateSignalement(Instant dateSignalement) {
        this.setDateSignalement(dateSignalement);
        return this;
    }

    public void setDateSignalement(Instant dateSignalement) {
        this.dateSignalement = dateSignalement;
    }

    public Utilisateur getUtilisateur() {
        return this.utilisateur;
    }

    public void setUtilisateur(Utilisateur utilisateur) {
        this.utilisateur = utilisateur;
    }

    public SignalementMessage utilisateur(Utilisateur utilisateur) {
        this.setUtilisateur(utilisateur);
        return this;
    }

    public Message getMessage() {
        return this.message;
    }

    public void setMessage(Message message) {
        this.message = message;
    }

    public SignalementMessage message(Message message) {
        this.setMessage(message);
        return this;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof SignalementMessage)) {
            return false;
        }
        return getId() != null && getId().equals(((SignalementMessage) o).getId());
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "SignalementMessage{" +
            "id=" + getId() +
            ", motif='" + getMotif() + "'" +
            ", dateSignalement='" + getDateSignalement() + "'" +
            "}";
    }
}
