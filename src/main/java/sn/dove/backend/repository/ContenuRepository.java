package sn.dove.backend.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import sn.dove.backend.domain.Contenu;

/**
 * Spring Data JPA repository for the Contenu entity.
 */
@Repository
public interface ContenuRepository extends JpaRepository<Contenu, UUID>, JpaSpecificationExecutor<Contenu> {
    default Optional<Contenu> findOneWithEagerRelationships(UUID id) {
        return this.findOneWithToOneRelationships(id);
    }

    default List<Contenu> findAllWithEagerRelationships() {
        return this.findAllWithToOneRelationships();
    }

    default Page<Contenu> findAllWithEagerRelationships(Pageable pageable) {
        return this.findAllWithToOneRelationships(pageable);
    }

    @Query(
        value = "select contenu from Contenu contenu left join fetch contenu.application left join fetch contenu.module left join fetch contenu.metier left join fetch contenu.auteur left join fetch contenu.validateur",
        countQuery = "select count(contenu) from Contenu contenu"
    )
    Page<Contenu> findAllWithToOneRelationships(Pageable pageable);

    @Query(
        "select contenu from Contenu contenu left join fetch contenu.application left join fetch contenu.module left join fetch contenu.metier left join fetch contenu.auteur left join fetch contenu.validateur"
    )
    List<Contenu> findAllWithToOneRelationships();

    @Query(
        "select contenu from Contenu contenu left join fetch contenu.application left join fetch contenu.module left join fetch contenu.metier left join fetch contenu.auteur left join fetch contenu.validateur where contenu.id =:id"
    )
    Optional<Contenu> findOneWithToOneRelationships(@Param("id") UUID id);
}
