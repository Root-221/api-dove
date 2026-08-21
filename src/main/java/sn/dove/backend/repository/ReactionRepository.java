package sn.dove.backend.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import sn.dove.backend.domain.Reaction;

/**
 * Spring Data JPA repository for the Reaction entity.
 */
@Repository
public interface ReactionRepository extends JpaRepository<Reaction, UUID>, JpaSpecificationExecutor<Reaction> {
    default Optional<Reaction> findOneWithEagerRelationships(UUID id) {
        return this.findOneWithToOneRelationships(id);
    }

    default List<Reaction> findAllWithEagerRelationships() {
        return this.findAllWithToOneRelationships();
    }

    default Page<Reaction> findAllWithEagerRelationships(Pageable pageable) {
        return this.findAllWithToOneRelationships(pageable);
    }

    @Query(
        value = "select reaction from Reaction reaction left join fetch reaction.utilisateur left join fetch reaction.message",
        countQuery = "select count(reaction) from Reaction reaction"
    )
    Page<Reaction> findAllWithToOneRelationships(Pageable pageable);

    @Query("select reaction from Reaction reaction left join fetch reaction.utilisateur left join fetch reaction.message")
    List<Reaction> findAllWithToOneRelationships();

    @Query(
        "select reaction from Reaction reaction left join fetch reaction.utilisateur left join fetch reaction.message where reaction.id =:id"
    )
    Optional<Reaction> findOneWithToOneRelationships(@Param("id") UUID id);
}
