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
 * A Utilisateur.
 */
@Entity
@Table(name = "utilisateurs")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@SuppressWarnings("common-java:DuplicatedBlocks")
public class Utilisateur implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue
    @Column(name = "id")
    private UUID id;

    @NotNull
    @Size(max = 100)
    @Column(name = "nom", length = 100, nullable = false)
    private String nom;

    @NotNull
    @Size(max = 100)
    @Column(name = "prenom", length = 100, nullable = false)
    private String prenom;

    @NotNull
    @Size(max = 255)
    @Column(name = "email", length = 255, nullable = false, unique = true)
    private String email;

    @NotNull
    @Size(max = 100)
    @Column(name = "login", length = 100, nullable = false, unique = true)
    private String login;

    @ManyToMany(fetch = FetchType.LAZY, mappedBy = "membreses")
    @Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
    @JsonIgnoreProperties(value = { "discussionses", "membreses" }, allowSetters = true)
    private Set<CommunauteNandite> communauteNandites = new HashSet<>();

    // jhipster-needle-entity-add-field - JHipster will add fields here

    public UUID getId() {
        return this.id;
    }

    public Utilisateur id(UUID id) {
        this.setId(id);
        return this;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getNom() {
        return this.nom;
    }

    public Utilisateur nom(String nom) {
        this.setNom(nom);
        return this;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getPrenom() {
        return this.prenom;
    }

    public Utilisateur prenom(String prenom) {
        this.setPrenom(prenom);
        return this;
    }

    public void setPrenom(String prenom) {
        this.prenom = prenom;
    }

    public String getEmail() {
        return this.email;
    }

    public Utilisateur email(String email) {
        this.setEmail(email);
        return this;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getLogin() {
        return this.login;
    }

    public Utilisateur login(String login) {
        this.setLogin(login);
        return this;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public Set<CommunauteNandite> getCommunauteNandites() {
        return this.communauteNandites;
    }

    public void setCommunauteNandites(Set<CommunauteNandite> communauteNandites) {
        if (this.communauteNandites != null) {
            this.communauteNandites.forEach(i -> i.removeMembres(this));
        }
        if (communauteNandites != null) {
            communauteNandites.forEach(i -> i.addMembres(this));
        }
        this.communauteNandites = communauteNandites;
    }

    public Utilisateur communauteNandites(Set<CommunauteNandite> communauteNandites) {
        this.setCommunauteNandites(communauteNandites);
        return this;
    }

    public Utilisateur addCommunauteNandite(CommunauteNandite communauteNandite) {
        this.communauteNandites.add(communauteNandite);
        communauteNandite.getMembreses().add(this);
        return this;
    }

    public Utilisateur removeCommunauteNandite(CommunauteNandite communauteNandite) {
        this.communauteNandites.remove(communauteNandite);
        communauteNandite.getMembreses().remove(this);
        return this;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Utilisateur)) {
            return false;
        }
        return getId() != null && getId().equals(((Utilisateur) o).getId());
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "Utilisateur{" +
            "id=" + getId() +
            ", nom='" + getNom() + "'" +
            ", prenom='" + getPrenom() + "'" +
            ", email='" + getEmail() + "'" +
            ", login='" + getLogin() + "'" +
            "}";
    }
}
