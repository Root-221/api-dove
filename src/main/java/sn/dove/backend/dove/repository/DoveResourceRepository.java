package sn.dove.backend.dove.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
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

    /**
     * Looks up a single "utilisateurs" (or other) resource by the auth-provider identifier
     * stored in its JSON payload under "externalSubject" - distinct from the resource's own
     * business id (external_id column). Backed by a partial functional index, see the
     * 20260823090000_added_dove_resource_lookup_indexes changelog.
     */
    @Query(
        value = "SELECT * FROM dove_resource WHERE resource_type = :type AND payload::jsonb ->> 'externalSubject' = :subject LIMIT 1",
        nativeQuery = true
    )
    Optional<DoveResource> findByResourceTypeAndExternalSubject(@Param("type") String type, @Param("subject") String subject);

    /**
     * Most-recent-first, bounded list of resources of a given type belonging to a given user
     * (JSON payload field "userId"), e.g. one user's notifications. Backed by a partial
     * functional index, see the 20260823090000_added_dove_resource_lookup_indexes changelog.
     */
    @Query(
        value = "SELECT * FROM dove_resource WHERE resource_type = :type AND payload::jsonb ->> 'userId' = :userId ORDER BY created_at DESC LIMIT :limit",
        nativeQuery = true
    )
    List<DoveResource> findRecentByResourceTypeAndUserId(@Param("type") String type, @Param("userId") String userId, @Param("limit") int limit);

    boolean existsByResourceType(String resourceType);
}
