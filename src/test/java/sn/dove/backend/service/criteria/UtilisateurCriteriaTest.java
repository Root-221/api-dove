package sn.dove.backend.service.criteria;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Function;
import org.assertj.core.api.Condition;
import org.junit.jupiter.api.Test;

class UtilisateurCriteriaTest {

    @Test
    void newUtilisateurCriteriaHasAllFiltersNullTest() {
        var utilisateurCriteria = new UtilisateurCriteria();
        assertThat(utilisateurCriteria).is(criteriaFiltersAre(Objects::isNull));
    }

    @Test
    void utilisateurCriteriaFluentMethodsCreatesFiltersTest() {
        var utilisateurCriteria = new UtilisateurCriteria();

        setAllFilters(utilisateurCriteria);

        assertThat(utilisateurCriteria).is(criteriaFiltersAre(Objects::nonNull));
    }

    @Test
    void utilisateurCriteriaCopyCreatesNullFilterTest() {
        var utilisateurCriteria = new UtilisateurCriteria();
        var copy = utilisateurCriteria.copy();

        assertThat(utilisateurCriteria).satisfies(
            criteria ->
                assertThat(criteria).is(copyFiltersAre(copy, (a, b) -> a == null || a instanceof Boolean ? a == b : a != b && a.equals(b))),
            criteria -> assertThat(criteria).isEqualTo(copy),
            criteria -> assertThat(criteria).hasSameHashCodeAs(copy)
        );

        assertThat(copy).satisfies(
            criteria -> assertThat(criteria).is(criteriaFiltersAre(Objects::isNull)),
            criteria -> assertThat(criteria).isEqualTo(utilisateurCriteria)
        );
    }

    @Test
    void utilisateurCriteriaCopyDuplicatesEveryExistingFilterTest() {
        var utilisateurCriteria = new UtilisateurCriteria();
        setAllFilters(utilisateurCriteria);

        var copy = utilisateurCriteria.copy();

        assertThat(utilisateurCriteria).satisfies(
            criteria ->
                assertThat(criteria).is(copyFiltersAre(copy, (a, b) -> a == null || a instanceof Boolean ? a == b : a != b && a.equals(b))),
            criteria -> assertThat(criteria).isEqualTo(copy),
            criteria -> assertThat(criteria).hasSameHashCodeAs(copy)
        );

        assertThat(copy).satisfies(
            criteria -> assertThat(criteria).is(criteriaFiltersAre(Objects::nonNull)),
            criteria -> assertThat(criteria).isEqualTo(utilisateurCriteria)
        );
    }

    @Test
    void toStringVerifier() {
        var utilisateurCriteria = new UtilisateurCriteria();

        assertThat(utilisateurCriteria).hasToString("UtilisateurCriteria{}");
    }

    private static void setAllFilters(UtilisateurCriteria utilisateurCriteria) {
        utilisateurCriteria.id();
        utilisateurCriteria.nom();
        utilisateurCriteria.prenom();
        utilisateurCriteria.email();
        utilisateurCriteria.login();
        utilisateurCriteria.communauteNanditeId();
        utilisateurCriteria.distinct();
    }

    private static Condition<UtilisateurCriteria> criteriaFiltersAre(Function<Object, Boolean> condition) {
        return new Condition<>(
            criteria ->
                condition.apply(criteria.getId()) &&
                condition.apply(criteria.getNom()) &&
                condition.apply(criteria.getPrenom()) &&
                condition.apply(criteria.getEmail()) &&
                condition.apply(criteria.getLogin()) &&
                condition.apply(criteria.getCommunauteNanditeId()) &&
                condition.apply(criteria.getDistinct()),
            "every filter matches"
        );
    }

    private static Condition<UtilisateurCriteria> copyFiltersAre(UtilisateurCriteria copy, BiFunction<Object, Object, Boolean> condition) {
        return new Condition<>(
            criteria ->
                condition.apply(criteria.getId(), copy.getId()) &&
                condition.apply(criteria.getNom(), copy.getNom()) &&
                condition.apply(criteria.getPrenom(), copy.getPrenom()) &&
                condition.apply(criteria.getEmail(), copy.getEmail()) &&
                condition.apply(criteria.getLogin(), copy.getLogin()) &&
                condition.apply(criteria.getCommunauteNanditeId(), copy.getCommunauteNanditeId()) &&
                condition.apply(criteria.getDistinct(), copy.getDistinct()),
            "every filter matches"
        );
    }
}
