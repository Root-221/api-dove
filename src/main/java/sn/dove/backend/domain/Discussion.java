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
 * A Discussion.
 */
@Entity
@Table(name = "discussions")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@SuppressWarnings("common-java:DuplicatedBlocks")
public class Discussion implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue
    @Column(name = "id")
    private UUID id;

    @NotNull
    @Size(max = 255)
    @Column(name = "titre", length = 255, nullable = false)
    private String titre;

    @NotNull
    @Column(name = "date_creation", nullable = false)
    private Instant dateCreation;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "discussion")
    @Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
    @JsonIgnoreProperties(
        value = { "reponseses", "signalementses", "emetteur", "metier", "discussion", "messageParent" },
        allowSetters = true
    )
    private Set<Message> messageses = new HashSet<>();

    @ManyToOne(optional = false)
    @NotNull
    @JsonIgnoreProperties(value = { "communauteNandites" }, allowSetters = true)
    private Utilisateur createur;

    @ManyToOne(optional = false)
    @NotNull
    @JsonIgnoreProperties(value = { "discussionses", "membreses" }, allowSetters = true)
    private CommunauteNandite communauteNandite;

    // jhipster-needle-entity-add-field - JHipster will add fields here

    public UUID getId() {
        return this.id;
    }

    public Discussion id(UUID id) {
        this.setId(id);
        return this;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getTitre() {
        return this.titre;
    }

    public Discussion titre(String titre) {
        this.setTitre(titre);
        return this;
    }

    public void setTitre(String titre) {
        this.titre = titre;
    }

    public Instant getDateCreation() {
        return this.dateCreation;
    }

    public Discussion dateCreation(Instant dateCreation) {
        this.setDateCreation(dateCreation);
        return this;
    }

    public void setDateCreation(Instant dateCreation) {
        this.dateCreation = dateCreation;
    }

    public Set<Message> getMessageses() {
        return this.messageses;
    }

    public void setMessageses(Set<Message> messages) {
        if (this.messageses != null) {
            this.messageses.forEach(i -> i.setDiscussion(null));
        }
        if (messages != null) {
            messages.forEach(i -> i.setDiscussion(this));
        }
        this.messageses = messages;
    }

    public Discussion messageses(Set<Message> messages) {
        this.setMessageses(messages);
        return this;
    }

    public Discussion addMessages(Message message) {
        this.messageses.add(message);
        message.setDiscussion(this);
        return this;
    }

    public Discussion removeMessages(Message message) {
        this.messageses.remove(message);
        message.setDiscussion(null);
        return this;
    }

    public Utilisateur getCreateur() {
        return this.createur;
    }

    public void setCreateur(Utilisateur utilisateur) {
        this.createur = utilisateur;
    }

    public Discussion createur(Utilisateur utilisateur) {
        this.setCreateur(utilisateur);
        return this;
    }

    public CommunauteNandite getCommunauteNandite() {
        return this.communauteNandite;
    }

    public void setCommunauteNandite(CommunauteNandite communauteNandite) {
        this.communauteNandite = communauteNandite;
    }

    public Discussion communauteNandite(CommunauteNandite communauteNandite) {
        this.setCommunauteNandite(communauteNandite);
        return this;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Discussion)) {
            return false;
        }
        return getId() != null && getId().equals(((Discussion) o).getId());
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "Discussion{" +
            "id=" + getId() +
            ", titre='" + getTitre() + "'" +
            ", dateCreation='" + getDateCreation() + "'" +
            "}";
    }
}
