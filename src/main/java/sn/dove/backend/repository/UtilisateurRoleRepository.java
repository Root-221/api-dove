package sn.dove.backend.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import sn.dove.backend.domain.UtilisateurRole;

/**
 * Spring Data JPA repository for the UtilisateurRole entity.
 */
@Repository
public interface UtilisateurRoleRepository extends JpaRepository<UtilisateurRole, UUID>, JpaSpecificationExecutor<UtilisateurRole> {
    default Optional<UtilisateurRole> findOneWithEagerRelationships(UUID id) {
        return this.findOneWithToOneRelationships(id);
    }

    default List<UtilisateurRole> findAllWithEagerRelationships() {
        return this.findAllWithToOneRelationships();
    }

    default Page<UtilisateurRole> findAllWithEagerRelationships(Pageable pageable) {
        return this.findAllWithToOneRelationships(pageable);
    }

    @Query(
        value = "select utilisateurRole from UtilisateurRole utilisateurRole left join fetch utilisateurRole.utilisateur left join fetch utilisateurRole.role",
        countQuery = "select count(utilisateurRole) from UtilisateurRole utilisateurRole"
    )
    Page<UtilisateurRole> findAllWithToOneRelationships(Pageable pageable);

    @Query(
        "select utilisateurRole from UtilisateurRole utilisateurRole left join fetch utilisateurRole.utilisateur left join fetch utilisateurRole.role"
    )
    List<UtilisateurRole> findAllWithToOneRelationships();

    @Query(
        "select utilisateurRole from UtilisateurRole utilisateurRole left join fetch utilisateurRole.utilisateur left join fetch utilisateurRole.role where utilisateurRole.id =:id"
    )
    Optional<UtilisateurRole> findOneWithToOneRelationships(@Param("id") UUID id);
}
