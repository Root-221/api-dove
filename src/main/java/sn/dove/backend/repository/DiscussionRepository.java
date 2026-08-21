package sn.dove.backend.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import sn.dove.backend.domain.Discussion;

/**
 * Spring Data JPA repository for the Discussion entity.
 */
@Repository
public interface DiscussionRepository extends JpaRepository<Discussion, UUID>, JpaSpecificationExecutor<Discussion> {
    default Optional<Discussion> findOneWithEagerRelationships(UUID id) {
        return this.findOneWithToOneRelationships(id);
    }

    default List<Discussion> findAllWithEagerRelationships() {
        return this.findAllWithToOneRelationships();
    }

    default Page<Discussion> findAllWithEagerRelationships(Pageable pageable) {
        return this.findAllWithToOneRelationships(pageable);
    }

    @Query(
        value = "select discussion from Discussion discussion left join fetch discussion.createur",
        countQuery = "select count(discussion) from Discussion discussion"
    )
    Page<Discussion> findAllWithToOneRelationships(Pageable pageable);

    @Query("select discussion from Discussion discussion left join fetch discussion.createur")
    List<Discussion> findAllWithToOneRelationships();

    @Query("select discussion from Discussion discussion left join fetch discussion.createur where discussion.id =:id")
    Optional<Discussion> findOneWithToOneRelationships(@Param("id") UUID id);
}
