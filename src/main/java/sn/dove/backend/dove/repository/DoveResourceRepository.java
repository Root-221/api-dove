package sn.dove.backend.dove.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import sn.dove.backend.dove.domain.DoveResource;

public interface DoveResourceRepository extends JpaRepository<DoveResource, UUID> {
    List<DoveResource> findAllByResourceTypeOrderByCreatedAtAsc(String resourceType);

    Optional<DoveResource> findByResourceTypeAndExternalId(String resourceType, String externalId);

    boolean existsByResourceType(String resourceType);
}
