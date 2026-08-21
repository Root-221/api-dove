package sn.dove.backend.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import sn.dove.backend.domain.FeedBack;

/**
 * Spring Data JPA repository for the FeedBack entity.
 */
@Repository
public interface FeedBackRepository extends JpaRepository<FeedBack, UUID>, JpaSpecificationExecutor<FeedBack> {
    default Optional<FeedBack> findOneWithEagerRelationships(UUID id) {
        return this.findOneWithToOneRelationships(id);
    }

    default List<FeedBack> findAllWithEagerRelationships() {
        return this.findAllWithToOneRelationships();
    }

    default Page<FeedBack> findAllWithEagerRelationships(Pageable pageable) {
        return this.findAllWithToOneRelationships(pageable);
    }

    @Query(
        value = "select feedBack from FeedBack feedBack left join fetch feedBack.utilisateur left join fetch feedBack.contenu",
        countQuery = "select count(feedBack) from FeedBack feedBack"
    )
    Page<FeedBack> findAllWithToOneRelationships(Pageable pageable);

    @Query("select feedBack from FeedBack feedBack left join fetch feedBack.utilisateur left join fetch feedBack.contenu")
    List<FeedBack> findAllWithToOneRelationships();

    @Query(
        "select feedBack from FeedBack feedBack left join fetch feedBack.utilisateur left join fetch feedBack.contenu where feedBack.id =:id"
    )
    Optional<FeedBack> findOneWithToOneRelationships(@Param("id") UUID id);
}
