package sn.dove.backend.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import sn.dove.backend.domain.Signalement;

/**
 * Spring Data JPA repository for the Signalement entity.
 */
@Repository
public interface SignalementRepository extends JpaRepository<Signalement, UUID>, JpaSpecificationExecutor<Signalement> {
    default Optional<Signalement> findOneWithEagerRelationships(UUID id) {
        return this.findOneWithToOneRelationships(id);
    }

    default List<Signalement> findAllWithEagerRelationships() {
        return this.findAllWithToOneRelationships();
    }

    default Page<Signalement> findAllWithEagerRelationships(Pageable pageable) {
        return this.findAllWithToOneRelationships(pageable);
    }

    @Query(
        value = "select signalement from Signalement signalement left join fetch signalement.utilisateur left join fetch signalement.contenu",
        countQuery = "select count(signalement) from Signalement signalement"
    )
    Page<Signalement> findAllWithToOneRelationships(Pageable pageable);

    @Query("select signalement from Signalement signalement left join fetch signalement.utilisateur left join fetch signalement.contenu")
    List<Signalement> findAllWithToOneRelationships();

    @Query(
        "select signalement from Signalement signalement left join fetch signalement.utilisateur left join fetch signalement.contenu where signalement.id =:id"
    )
    Optional<Signalement> findOneWithToOneRelationships(@Param("id") UUID id);
}
