package sn.dove.backend.dove.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import sn.dove.backend.dove.domain.DoveResource;

public interface DoveResourceRepository extends JpaRepository<DoveResource, UUID> {
    List<DoveResource> findAllByResourceTypeOrderByCreatedAtAsc(String resourceType);

    /**
     * Same ordering as {@link #findAllByResourceTypeOrderByCreatedAtAsc(String)} but with the
     * row cap pushed to SQL (LIMIT via the Pageable) instead of loading every row and truncating
     * in Java.
     */
    List<DoveResource> findAllByResourceTypeOrderByCreatedAtAsc(String resourceType, Pageable pageable);

    /** Most-recent-first, bounded via the Pageable's page size (SQL LIMIT, no OFFSET). */
    List<DoveResource> findAllByResourceTypeOrderByCreatedAtDesc(String resourceType, Pageable pageable);

    Optional<DoveResource> findByResourceTypeAndExternalId(String resourceType, String externalId);

    Optional<DoveResource> findFirstByResourceTypeAndExternalSubject(String resourceType, String externalSubject);

    List<DoveResource> findAllByResourceTypeAndOwnerUserIdOrderByCreatedAtDesc(
        String resourceType,
        String ownerUserId,
        Pageable pageable
    );

    boolean existsByResourceType(String resourceType);
}
