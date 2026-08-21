package sn.dove.backend.repository;

import java.util.UUID;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;
import sn.dove.backend.domain.Metier;

/**
 * Spring Data JPA repository for the Metier entity.
 */
@SuppressWarnings("unused")
@Repository
public interface MetierRepository extends JpaRepository<Metier, UUID>, JpaSpecificationExecutor<Metier> {}
