package sn.dove.backend.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;
import sn.dove.backend.domain.CommunauteNandite;

/**
 * Spring Data JPA repository for the CommunauteNandite entity.
 *
 * When extending this class, extend CommunauteNanditeRepositoryWithBagRelationships too.
 * For more information refer to https://github.com/jhipster/generator-jhipster/issues/17990.
 */
@Repository
public interface CommunauteNanditeRepository
    extends
        CommunauteNanditeRepositoryWithBagRelationships,
        JpaRepository<CommunauteNandite, UUID>,
        JpaSpecificationExecutor<CommunauteNandite>
{
    default Optional<CommunauteNandite> findOneWithEagerRelationships(UUID id) {
        return this.fetchBagRelationships(this.findById(id));
    }

    default List<CommunauteNandite> findAllWithEagerRelationships() {
        return this.fetchBagRelationships(this.findAll());
    }

    default Page<CommunauteNandite> findAllWithEagerRelationships(Pageable pageable) {
        return this.fetchBagRelationships(this.findAll(pageable));
    }
}
