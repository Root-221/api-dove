package sn.dove.backend.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import sn.dove.backend.domain.CommunauteNandite;

public interface CommunauteNanditeRepositoryWithBagRelationships {
    Optional<CommunauteNandite> fetchBagRelationships(Optional<CommunauteNandite> communauteNandite);

    List<CommunauteNandite> fetchBagRelationships(List<CommunauteNandite> communauteNandites);

    Page<CommunauteNandite> fetchBagRelationships(Page<CommunauteNandite> communauteNandites);
}
