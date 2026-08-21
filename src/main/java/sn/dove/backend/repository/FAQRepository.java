package sn.dove.backend.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import sn.dove.backend.domain.FAQ;

/**
 * Spring Data JPA repository for the FAQ entity.
 */
@Repository
public interface FAQRepository extends JpaRepository<FAQ, UUID>, JpaSpecificationExecutor<FAQ> {
    default Optional<FAQ> findOneWithEagerRelationships(UUID id) {
        return this.findOneWithToOneRelationships(id);
    }

    default List<FAQ> findAllWithEagerRelationships() {
        return this.findAllWithToOneRelationships();
    }

    default Page<FAQ> findAllWithEagerRelationships(Pageable pageable) {
        return this.findAllWithToOneRelationships(pageable);
    }

    @Query(value = "select fAQ from FAQ fAQ left join fetch fAQ.contenu", countQuery = "select count(fAQ) from FAQ fAQ")
    Page<FAQ> findAllWithToOneRelationships(Pageable pageable);

    @Query("select fAQ from FAQ fAQ left join fetch fAQ.contenu")
    List<FAQ> findAllWithToOneRelationships();

    @Query("select fAQ from FAQ fAQ left join fetch fAQ.contenu where fAQ.id =:id")
    Optional<FAQ> findOneWithToOneRelationships(@Param("id") UUID id);
}
