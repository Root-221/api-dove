package sn.dove.backend.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import sn.dove.backend.domain.CommunauteNandite;

/**
 * Utility repository to load bag relationships based on https://vladmihalcea.com/hibernate-multiplebagfetchexception/
 */
public class CommunauteNanditeRepositoryWithBagRelationshipsImpl implements CommunauteNanditeRepositoryWithBagRelationships {

    private static final String ID_PARAMETER = "id";
    private static final String COMMUNAUTENANDITES_PARAMETER = "communauteNandites";

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public Optional<CommunauteNandite> fetchBagRelationships(Optional<CommunauteNandite> communauteNandite) {
        return communauteNandite.map(this::fetchMembreses);
    }

    @Override
    public Page<CommunauteNandite> fetchBagRelationships(Page<CommunauteNandite> communauteNandites) {
        return new PageImpl<>(
            fetchBagRelationships(communauteNandites.getContent()),
            communauteNandites.getPageable(),
            communauteNandites.getTotalElements()
        );
    }

    @Override
    public List<CommunauteNandite> fetchBagRelationships(List<CommunauteNandite> communauteNandites) {
        return Optional.of(communauteNandites).map(this::fetchMembreses).orElse(List.of());
    }

    CommunauteNandite fetchMembreses(CommunauteNandite result) {
        return entityManager
            .createQuery(
                "select communauteNandite from CommunauteNandite communauteNandite left join fetch communauteNandite.membreses where communauteNandite.id = :id",
                CommunauteNandite.class
            )
            .setParameter(ID_PARAMETER, result.getId())
            .getSingleResult();
    }

    List<CommunauteNandite> fetchMembreses(List<CommunauteNandite> communauteNandites) {
        HashMap<Object, Integer> order = new HashMap<>();
        IntStream.range(0, communauteNandites.size()).forEach(index -> order.put(communauteNandites.get(index).getId(), index));
        List<CommunauteNandite> result = entityManager
            .createQuery(
                "select communauteNandite from CommunauteNandite communauteNandite left join fetch communauteNandite.membreses where communauteNandite in :communauteNandites",
                CommunauteNandite.class
            )
            .setParameter(COMMUNAUTENANDITES_PARAMETER, communauteNandites)
            .getResultList();
        result.sort((o1, o2) -> Integer.compare(order.get(o1.getId()), order.get(o2.getId())));
        return result;
    }
}
