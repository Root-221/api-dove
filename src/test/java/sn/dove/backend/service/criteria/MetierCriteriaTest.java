package sn.dove.backend.service.criteria;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Function;
import org.assertj.core.api.Condition;
import org.junit.jupiter.api.Test;

class MetierCriteriaTest {

    @Test
    void newMetierCriteriaHasAllFiltersNullTest() {
        var metierCriteria = new MetierCriteria();
        assertThat(metierCriteria).is(criteriaFiltersAre(Objects::isNull));
    }

    @Test
    void metierCriteriaFluentMethodsCreatesFiltersTest() {
        var metierCriteria = new MetierCriteria();

        setAllFilters(metierCriteria);

        assertThat(metierCriteria).is(criteriaFiltersAre(Objects::nonNull));
    }

    @Test
    void metierCriteriaCopyCreatesNullFilterTest() {
        var metierCriteria = new MetierCriteria();
        var copy = metierCriteria.copy();

        assertThat(metierCriteria).satisfies(
            criteria ->
                assertThat(criteria).is(copyFiltersAre(copy, (a, b) -> a == null || a instanceof Boolean ? a == b : a != b && a.equals(b))),
            criteria -> assertThat(criteria).isEqualTo(copy),
            criteria -> assertThat(criteria).hasSameHashCodeAs(copy)
        );

        assertThat(copy).satisfies(
            criteria -> assertThat(criteria).is(criteriaFiltersAre(Objects::isNull)),
            criteria -> assertThat(criteria).isEqualTo(metierCriteria)
        );
    }

    @Test
    void metierCriteriaCopyDuplicatesEveryExistingFilterTest() {
        var metierCriteria = new MetierCriteria();
        setAllFilters(metierCriteria);

        var copy = metierCriteria.copy();

        assertThat(metierCriteria).satisfies(
            criteria ->
                assertThat(criteria).is(copyFiltersAre(copy, (a, b) -> a == null || a instanceof Boolean ? a == b : a != b && a.equals(b))),
            criteria -> assertThat(criteria).isEqualTo(copy),
            criteria -> assertThat(criteria).hasSameHashCodeAs(copy)
        );

        assertThat(copy).satisfies(
            criteria -> assertThat(criteria).is(criteriaFiltersAre(Objects::nonNull)),
            criteria -> assertThat(criteria).isEqualTo(metierCriteria)
        );
    }

    @Test
    void toStringVerifier() {
        var metierCriteria = new MetierCriteria();

        assertThat(metierCriteria).hasToString("MetierCriteria{}");
    }

    private static void setAllFilters(MetierCriteria metierCriteria) {
        metierCriteria.id();
        metierCriteria.libelle();
        metierCriteria.distinct();
    }

    private static Condition<MetierCriteria> criteriaFiltersAre(Function<Object, Boolean> condition) {
        return new Condition<>(
            criteria ->
                condition.apply(criteria.getId()) && condition.apply(criteria.getLibelle()) && condition.apply(criteria.getDistinct()),
            "every filter matches"
        );
    }

    private static Condition<MetierCriteria> copyFiltersAre(MetierCriteria copy, BiFunction<Object, Object, Boolean> condition) {
        return new Condition<>(
            criteria ->
                condition.apply(criteria.getId(), copy.getId()) &&
                condition.apply(criteria.getLibelle(), copy.getLibelle()) &&
                condition.apply(criteria.getDistinct(), copy.getDistinct()),
            "every filter matches"
        );
    }
}
