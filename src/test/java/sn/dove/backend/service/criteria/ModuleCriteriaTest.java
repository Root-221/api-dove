package sn.dove.backend.service.criteria;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Function;
import org.assertj.core.api.Condition;
import org.junit.jupiter.api.Test;

class ModuleCriteriaTest {

    @Test
    void newModuleCriteriaHasAllFiltersNullTest() {
        var moduleCriteria = new ModuleCriteria();
        assertThat(moduleCriteria).is(criteriaFiltersAre(Objects::isNull));
    }

    @Test
    void moduleCriteriaFluentMethodsCreatesFiltersTest() {
        var moduleCriteria = new ModuleCriteria();

        setAllFilters(moduleCriteria);

        assertThat(moduleCriteria).is(criteriaFiltersAre(Objects::nonNull));
    }

    @Test
    void moduleCriteriaCopyCreatesNullFilterTest() {
        var moduleCriteria = new ModuleCriteria();
        var copy = moduleCriteria.copy();

        assertThat(moduleCriteria).satisfies(
            criteria ->
                assertThat(criteria).is(copyFiltersAre(copy, (a, b) -> a == null || a instanceof Boolean ? a == b : a != b && a.equals(b))),
            criteria -> assertThat(criteria).isEqualTo(copy),
            criteria -> assertThat(criteria).hasSameHashCodeAs(copy)
        );

        assertThat(copy).satisfies(
            criteria -> assertThat(criteria).is(criteriaFiltersAre(Objects::isNull)),
            criteria -> assertThat(criteria).isEqualTo(moduleCriteria)
        );
    }

    @Test
    void moduleCriteriaCopyDuplicatesEveryExistingFilterTest() {
        var moduleCriteria = new ModuleCriteria();
        setAllFilters(moduleCriteria);

        var copy = moduleCriteria.copy();

        assertThat(moduleCriteria).satisfies(
            criteria ->
                assertThat(criteria).is(copyFiltersAre(copy, (a, b) -> a == null || a instanceof Boolean ? a == b : a != b && a.equals(b))),
            criteria -> assertThat(criteria).isEqualTo(copy),
            criteria -> assertThat(criteria).hasSameHashCodeAs(copy)
        );

        assertThat(copy).satisfies(
            criteria -> assertThat(criteria).is(criteriaFiltersAre(Objects::nonNull)),
            criteria -> assertThat(criteria).isEqualTo(moduleCriteria)
        );
    }

    @Test
    void toStringVerifier() {
        var moduleCriteria = new ModuleCriteria();

        assertThat(moduleCriteria).hasToString("ModuleCriteria{}");
    }

    private static void setAllFilters(ModuleCriteria moduleCriteria) {
        moduleCriteria.id();
        moduleCriteria.libelle();
        moduleCriteria.applicationId();
        moduleCriteria.distinct();
    }

    private static Condition<ModuleCriteria> criteriaFiltersAre(Function<Object, Boolean> condition) {
        return new Condition<>(
            criteria ->
                condition.apply(criteria.getId()) &&
                condition.apply(criteria.getLibelle()) &&
                condition.apply(criteria.getApplicationId()) &&
                condition.apply(criteria.getDistinct()),
            "every filter matches"
        );
    }

    private static Condition<ModuleCriteria> copyFiltersAre(ModuleCriteria copy, BiFunction<Object, Object, Boolean> condition) {
        return new Condition<>(
            criteria ->
                condition.apply(criteria.getId(), copy.getId()) &&
                condition.apply(criteria.getLibelle(), copy.getLibelle()) &&
                condition.apply(criteria.getApplicationId(), copy.getApplicationId()) &&
                condition.apply(criteria.getDistinct(), copy.getDistinct()),
            "every filter matches"
        );
    }
}
