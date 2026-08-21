package sn.dove.backend.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.io.Serial;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

/**
 * A CommunauteNandite.
 */
@Entity
@Table(name = "communautes_nandite")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@SuppressWarnings("common-java:DuplicatedBlocks")
public class CommunauteNandite implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue
    @Column(name = "id")
    private UUID id;

    @NotNull
    @Size(max = 150)
    @Column(name = "nom", length = 150, nullable = false)
    private String nom;

    @Lob
    @Column(name = "description")
    private String description;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "communauteNandite")
    @Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
    @JsonIgnoreProperties(value = { "messageses", "createur", "communauteNandite" }, allowSetters = true)
    private Set<Discussion> discussionses = new HashSet<>();

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "rel_communautes_nandite__membres",
        joinColumns = @JoinColumn(name = "communautes_nandite_id"),
        inverseJoinColumns = @JoinColumn(name = "membres_id")
    )
    @Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
    @JsonIgnoreProperties(value = { "communauteNandites" }, allowSetters = true)
    private Set<Utilisateur> membreses = new HashSet<>();

    // jhipster-needle-entity-add-field - JHipster will add fields here

    public UUID getId() {
        return this.id;
    }

    public CommunauteNandite id(UUID id) {
        this.setId(id);
        return this;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getNom() {
        return this.nom;
    }

    public CommunauteNandite nom(String nom) {
        this.setNom(nom);
        return this;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getDescription() {
        return this.description;
    }

    public CommunauteNandite description(String description) {
        this.setDescription(description);
        return this;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Set<Discussion> getDiscussionses() {
        return this.discussionses;
    }

    public void setDiscussionses(Set<Discussion> discussions) {
        if (this.discussionses != null) {
            this.discussionses.forEach(i -> i.setCommunauteNandite(null));
        }
        if (discussions != null) {
            discussions.forEach(i -> i.setCommunauteNandite(this));
        }
        this.discussionses = discussions;
    }

    public CommunauteNandite discussionses(Set<Discussion> discussions) {
        this.setDiscussionses(discussions);
        return this;
    }

    public CommunauteNandite addDiscussions(Discussion discussion) {
        this.discussionses.add(discussion);
        discussion.setCommunauteNandite(this);
        return this;
    }

    public CommunauteNandite removeDiscussions(Discussion discussion) {
        this.discussionses.remove(discussion);
        discussion.setCommunauteNandite(null);
        return this;
    }

    public Set<Utilisateur> getMembreses() {
        return this.membreses;
    }

    public void setMembreses(Set<Utilisateur> utilisateurs) {
        this.membreses = utilisateurs;
    }

    public CommunauteNandite membreses(Set<Utilisateur> utilisateurs) {
        this.setMembreses(utilisateurs);
        return this;
    }

    public CommunauteNandite addMembres(Utilisateur utilisateur) {
        this.membreses.add(utilisateur);
        return this;
    }

    public CommunauteNandite removeMembres(Utilisateur utilisateur) {
        this.membreses.remove(utilisateur);
        return this;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof CommunauteNandite)) {
            return false;
        }
        return getId() != null && getId().equals(((CommunauteNandite) o).getId());
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "CommunauteNandite{" +
            "id=" + getId() +
            ", nom='" + getNom() + "'" +
            ", description='" + getDescription() + "'" +
            "}";
    }
}
