package sn.dove.backend.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import sn.dove.backend.domain.SignalementMessage;

/**
 * Spring Data JPA repository for the SignalementMessage entity.
 */
@Repository
public interface SignalementMessageRepository
    extends JpaRepository<SignalementMessage, UUID>, JpaSpecificationExecutor<SignalementMessage>
{
    default Optional<SignalementMessage> findOneWithEagerRelationships(UUID id) {
        return this.findOneWithToOneRelationships(id);
    }

    default List<SignalementMessage> findAllWithEagerRelationships() {
        return this.findAllWithToOneRelationships();
    }

    default Page<SignalementMessage> findAllWithEagerRelationships(Pageable pageable) {
        return this.findAllWithToOneRelationships(pageable);
    }

    @Query(
        value = "select signalementMessage from SignalementMessage signalementMessage left join fetch signalementMessage.utilisateur",
        countQuery = "select count(signalementMessage) from SignalementMessage signalementMessage"
    )
    Page<SignalementMessage> findAllWithToOneRelationships(Pageable pageable);

    @Query("select signalementMessage from SignalementMessage signalementMessage left join fetch signalementMessage.utilisateur")
    List<SignalementMessage> findAllWithToOneRelationships();

    @Query(
        "select signalementMessage from SignalementMessage signalementMessage left join fetch signalementMessage.utilisateur where signalementMessage.id =:id"
    )
    Optional<SignalementMessage> findOneWithToOneRelationships(@Param("id") UUID id);
}
