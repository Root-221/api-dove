package sn.dove.backend.service.criteria;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Function;
import org.assertj.core.api.Condition;
import org.junit.jupiter.api.Test;

class UtilisateurRoleCriteriaTest {

    @Test
    void newUtilisateurRoleCriteriaHasAllFiltersNullTest() {
        var utilisateurRoleCriteria = new UtilisateurRoleCriteria();
        assertThat(utilisateurRoleCriteria).is(criteriaFiltersAre(Objects::isNull));
    }

    @Test
    void utilisateurRoleCriteriaFluentMethodsCreatesFiltersTest() {
        var utilisateurRoleCriteria = new UtilisateurRoleCriteria();

        setAllFilters(utilisateurRoleCriteria);

        assertThat(utilisateurRoleCriteria).is(criteriaFiltersAre(Objects::nonNull));
    }

    @Test
    void utilisateurRoleCriteriaCopyCreatesNullFilterTest() {
        var utilisateurRoleCriteria = new UtilisateurRoleCriteria();
        var copy = utilisateurRoleCriteria.copy();

        assertThat(utilisateurRoleCriteria).satisfies(
            criteria ->
                assertThat(criteria).is(copyFiltersAre(copy, (a, b) -> a == null || a instanceof Boolean ? a == b : a != b && a.equals(b))),
            criteria -> assertThat(criteria).isEqualTo(copy),
            criteria -> assertThat(criteria).hasSameHashCodeAs(copy)
        );

        assertThat(copy).satisfies(
            criteria -> assertThat(criteria).is(criteriaFiltersAre(Objects::isNull)),
            criteria -> assertThat(criteria).isEqualTo(utilisateurRoleCriteria)
        );
    }

    @Test
    void utilisateurRoleCriteriaCopyDuplicatesEveryExistingFilterTest() {
        var utilisateurRoleCriteria = new UtilisateurRoleCriteria();
        setAllFilters(utilisateurRoleCriteria);

        var copy = utilisateurRoleCriteria.copy();

        assertThat(utilisateurRoleCriteria).satisfies(
            criteria ->
                assertThat(criteria).is(copyFiltersAre(copy, (a, b) -> a == null || a instanceof Boolean ? a == b : a != b && a.equals(b))),
            criteria -> assertThat(criteria).isEqualTo(copy),
            criteria -> assertThat(criteria).hasSameHashCodeAs(copy)
        );

        assertThat(copy).satisfies(
            criteria -> assertThat(criteria).is(criteriaFiltersAre(Objects::nonNull)),
            criteria -> assertThat(criteria).isEqualTo(utilisateurRoleCriteria)
        );
    }

    @Test
    void toStringVerifier() {
        var utilisateurRoleCriteria = new UtilisateurRoleCriteria();

        assertThat(utilisateurRoleCriteria).hasToString("UtilisateurRoleCriteria{}");
    }

    private static void setAllFilters(UtilisateurRoleCriteria utilisateurRoleCriteria) {
        utilisateurRoleCriteria.id();
        utilisateurRoleCriteria.utilisateurId();
        utilisateurRoleCriteria.roleId();
        utilisateurRoleCriteria.distinct();
    }

    private static Condition<UtilisateurRoleCriteria> criteriaFiltersAre(Function<Object, Boolean> condition) {
        return new Condition<>(
            criteria ->
                condition.apply(criteria.getId()) &&
                condition.apply(criteria.getUtilisateurId()) &&
                condition.apply(criteria.getRoleId()) &&
                condition.apply(criteria.getDistinct()),
            "every filter matches"
        );
    }

    private static Condition<UtilisateurRoleCriteria> copyFiltersAre(
        UtilisateurRoleCriteria copy,
        BiFunction<Object, Object, Boolean> condition
    ) {
        return new Condition<>(
            criteria ->
                condition.apply(criteria.getId(), copy.getId()) &&
                condition.apply(criteria.getUtilisateurId(), copy.getUtilisateurId()) &&
                condition.apply(criteria.getRoleId(), copy.getRoleId()) &&
                condition.apply(criteria.getDistinct(), copy.getDistinct()),
            "every filter matches"
        );
    }
}
