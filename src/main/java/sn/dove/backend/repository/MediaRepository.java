package sn.dove.backend.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import sn.dove.backend.domain.Media;

/**
 * Spring Data JPA repository for the Media entity.
 */
@Repository
public interface MediaRepository extends JpaRepository<Media, UUID>, JpaSpecificationExecutor<Media> {
    default Optional<Media> findOneWithEagerRelationships(UUID id) {
        return this.findOneWithToOneRelationships(id);
    }

    default List<Media> findAllWithEagerRelationships() {
        return this.findAllWithToOneRelationships();
    }

    default Page<Media> findAllWithEagerRelationships(Pageable pageable) {
        return this.findAllWithToOneRelationships(pageable);
    }

    @Query(value = "select media from Media media left join fetch media.contenu", countQuery = "select count(media) from Media media")
    Page<Media> findAllWithToOneRelationships(Pageable pageable);

    @Query("select media from Media media left join fetch media.contenu")
    List<Media> findAllWithToOneRelationships();

    @Query("select media from Media media left join fetch media.contenu where media.id =:id")
    Optional<Media> findOneWithToOneRelationships(@Param("id") UUID id);
}
