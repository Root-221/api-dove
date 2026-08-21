package sn.dove.backend.service.criteria;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Function;
import org.assertj.core.api.Condition;
import org.junit.jupiter.api.Test;

class ApplicationCriteriaTest {

    @Test
    void newApplicationCriteriaHasAllFiltersNullTest() {
        var applicationCriteria = new ApplicationCriteria();
        assertThat(applicationCriteria).is(criteriaFiltersAre(Objects::isNull));
    }

    @Test
    void applicationCriteriaFluentMethodsCreatesFiltersTest() {
        var applicationCriteria = new ApplicationCriteria();

        setAllFilters(applicationCriteria);

        assertThat(applicationCriteria).is(criteriaFiltersAre(Objects::nonNull));
    }

    @Test
    void applicationCriteriaCopyCreatesNullFilterTest() {
        var applicationCriteria = new ApplicationCriteria();
        var copy = applicationCriteria.copy();

        assertThat(applicationCriteria).satisfies(
            criteria ->
                assertThat(criteria).is(copyFiltersAre(copy, (a, b) -> a == null || a instanceof Boolean ? a == b : a != b && a.equals(b))),
            criteria -> assertThat(criteria).isEqualTo(copy),
            criteria -> assertThat(criteria).hasSameHashCodeAs(copy)
        );

        assertThat(copy).satisfies(
            criteria -> assertThat(criteria).is(criteriaFiltersAre(Objects::isNull)),
            criteria -> assertThat(criteria).isEqualTo(applicationCriteria)
        );
    }

    @Test
    void applicationCriteriaCopyDuplicatesEveryExistingFilterTest() {
        var applicationCriteria = new ApplicationCriteria();
        setAllFilters(applicationCriteria);

        var copy = applicationCriteria.copy();

        assertThat(applicationCriteria).satisfies(
            criteria ->
                assertThat(criteria).is(copyFiltersAre(copy, (a, b) -> a == null || a instanceof Boolean ? a == b : a != b && a.equals(b))),
            criteria -> assertThat(criteria).isEqualTo(copy),
            criteria -> assertThat(criteria).hasSameHashCodeAs(copy)
        );

        assertThat(copy).satisfies(
            criteria -> assertThat(criteria).is(criteriaFiltersAre(Objects::nonNull)),
            criteria -> assertThat(criteria).isEqualTo(applicationCriteria)
        );
    }

    @Test
    void toStringVerifier() {
        var applicationCriteria = new ApplicationCriteria();

        assertThat(applicationCriteria).hasToString("ApplicationCriteria{}");
    }

    private static void setAllFilters(ApplicationCriteria applicationCriteria) {
        applicationCriteria.id();
        applicationCriteria.nom();
        applicationCriteria.dateAjout();
        applicationCriteria.modulesId();
        applicationCriteria.distinct();
    }

    private static Condition<ApplicationCriteria> criteriaFiltersAre(Function<Object, Boolean> condition) {
        return new Condition<>(
            criteria ->
                condition.apply(criteria.getId()) &&
                condition.apply(criteria.getNom()) &&
                condition.apply(criteria.getDateAjout()) &&
                condition.apply(criteria.getModulesId()) &&
                condition.apply(criteria.getDistinct()),
            "every filter matches"
        );
    }

    private static Condition<ApplicationCriteria> copyFiltersAre(ApplicationCriteria copy, BiFunction<Object, Object, Boolean> condition) {
        return new Condition<>(
            criteria ->
                condition.apply(criteria.getId(), copy.getId()) &&
                condition.apply(criteria.getNom(), copy.getNom()) &&
                condition.apply(criteria.getDateAjout(), copy.getDateAjout()) &&
                condition.apply(criteria.getModulesId(), copy.getModulesId()) &&
                condition.apply(criteria.getDistinct(), copy.getDistinct()),
            "every filter matches"
        );
    }
}
