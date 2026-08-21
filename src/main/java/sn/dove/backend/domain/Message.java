package sn.dove.backend.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

/**
 * A Message.
 */
@Entity
@Table(name = "messages")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@SuppressWarnings("common-java:DuplicatedBlocks")
public class Message implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue
    @Column(name = "id")
    private UUID id;

    @Lob
    @Column(name = "contenu", nullable = false)
    private String contenu;

    @NotNull
    @Column(name = "date_envoi", nullable = false)
    private Instant dateEnvoi;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "messageParent")
    @Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
    @JsonIgnoreProperties(
        value = { "reponseses", "signalementses", "emetteur", "metier", "discussion", "messageParent" },
        allowSetters = true
    )
    private Set<Message> reponseses = new HashSet<>();

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "message")
    @Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
    @JsonIgnoreProperties(value = { "utilisateur", "message" }, allowSetters = true)
    private Set<SignalementMessage> signalementses = new HashSet<>();

    @ManyToOne(optional = false)
    @NotNull
    @JsonIgnoreProperties(value = { "communauteNandites" }, allowSetters = true)
    private Utilisateur emetteur;

    @ManyToOne(fetch = FetchType.LAZY)
    private Metier metier;

    @ManyToOne(optional = false)
    @NotNull
    @JsonIgnoreProperties(value = { "messageses", "createur", "communauteNandite" }, allowSetters = true)
    private Discussion discussion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JsonIgnoreProperties(
        value = { "reponseses", "signalementses", "emetteur", "metier", "discussion", "messageParent" },
        allowSetters = true
    )
    private Message messageParent;

    // jhipster-needle-entity-add-field - JHipster will add fields here

    public UUID getId() {
        return this.id;
    }

    public Message id(UUID id) {
        this.setId(id);
        return this;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getContenu() {
        return this.contenu;
    }

    public Message contenu(String contenu) {
        this.setContenu(contenu);
        return this;
    }

    public void setContenu(String contenu) {
        this.contenu = contenu;
    }

    public Instant getDateEnvoi() {
        return this.dateEnvoi;
    }

    public Message dateEnvoi(Instant dateEnvoi) {
        this.setDateEnvoi(dateEnvoi);
        return this;
    }

    public void setDateEnvoi(Instant dateEnvoi) {
        this.dateEnvoi = dateEnvoi;
    }

    public Set<Message> getReponseses() {
        return this.reponseses;
    }

    public void setReponseses(Set<Message> messages) {
        if (this.reponseses != null) {
            this.reponseses.forEach(i -> i.setMessageParent(null));
        }
        if (messages != null) {
            messages.forEach(i -> i.setMessageParent(this));
        }
        this.reponseses = messages;
    }

    public Message reponseses(Set<Message> messages) {
        this.setReponseses(messages);
        return this;
    }

    public Message addReponses(Message message) {
        this.reponseses.add(message);
        message.setMessageParent(this);
        return this;
    }

    public Message removeReponses(Message message) {
        this.reponseses.remove(message);
        message.setMessageParent(null);
        return this;
    }

    public Set<SignalementMessage> getSignalementses() {
        return this.signalementses;
    }

    public void setSignalementses(Set<SignalementMessage> signalementMessages) {
        if (this.signalementses != null) {
            this.signalementses.forEach(i -> i.setMessage(null));
        }
        if (signalementMessages != null) {
            signalementMessages.forEach(i -> i.setMessage(this));
        }
        this.signalementses = signalementMessages;
    }

    public Message signalementses(Set<SignalementMessage> signalementMessages) {
        this.setSignalementses(signalementMessages);
        return this;
    }

    public Message addSignalements(SignalementMessage signalementMessage) {
        this.signalementses.add(signalementMessage);
        signalementMessage.setMessage(this);
        return this;
    }

    public Message removeSignalements(SignalementMessage signalementMessage) {
        this.signalementses.remove(signalementMessage);
        signalementMessage.setMessage(null);
        return this;
    }

    public Utilisateur getEmetteur() {
        return this.emetteur;
    }

    public void setEmetteur(Utilisateur utilisateur) {
        this.emetteur = utilisateur;
    }

    public Message emetteur(Utilisateur utilisateur) {
        this.setEmetteur(utilisateur);
        return this;
    }

    public Metier getMetier() {
        return this.metier;
    }

    public void setMetier(Metier metier) {
        this.metier = metier;
    }

    public Message metier(Metier metier) {
        this.setMetier(metier);
        return this;
    }

    public Discussion getDiscussion() {
        return this.discussion;
    }

    public void setDiscussion(Discussion discussion) {
        this.discussion = discussion;
    }

    public Message discussion(Discussion discussion) {
        this.setDiscussion(discussion);
        return this;
    }

    public Message getMessageParent() {
        return this.messageParent;
    }

    public void setMessageParent(Message message) {
        this.messageParent = message;
    }

    public Message messageParent(Message message) {
        this.setMessageParent(message);
        return this;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Message)) {
            return false;
        }
        return getId() != null && getId().equals(((Message) o).getId());
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "Message{" +
            "id=" + getId() +
            ", contenu='" + getContenu() + "'" +
            ", dateEnvoi='" + getDateEnvoi() + "'" +
            "}";
    }
}
