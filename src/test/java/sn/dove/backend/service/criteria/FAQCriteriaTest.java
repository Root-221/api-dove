package sn.dove.backend.service.criteria;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Function;
import org.assertj.core.api.Condition;
import org.junit.jupiter.api.Test;

class FAQCriteriaTest {

    @Test
    void newFAQCriteriaHasAllFiltersNullTest() {
        var fAQCriteria = new FAQCriteria();
        assertThat(fAQCriteria).is(criteriaFiltersAre(Objects::isNull));
    }

    @Test
    void fAQCriteriaFluentMethodsCreatesFiltersTest() {
        var fAQCriteria = new FAQCriteria();

        setAllFilters(fAQCriteria);

        assertThat(fAQCriteria).is(criteriaFiltersAre(Objects::nonNull));
    }

    @Test
    void fAQCriteriaCopyCreatesNullFilterTest() {
        var fAQCriteria = new FAQCriteria();
        var copy = fAQCriteria.copy();

        assertThat(fAQCriteria).satisfies(
            criteria ->
                assertThat(criteria).is(copyFiltersAre(copy, (a, b) -> a == null || a instanceof Boolean ? a == b : a != b && a.equals(b))),
            criteria -> assertThat(criteria).isEqualTo(copy),
            criteria -> assertThat(criteria).hasSameHashCodeAs(copy)
        );

        assertThat(copy).satisfies(
            criteria -> assertThat(criteria).is(criteriaFiltersAre(Objects::isNull)),
            criteria -> assertThat(criteria).isEqualTo(fAQCriteria)
        );
    }

    @Test
    void fAQCriteriaCopyDuplicatesEveryExistingFilterTest() {
        var fAQCriteria = new FAQCriteria();
        setAllFilters(fAQCriteria);

        var copy = fAQCriteria.copy();

        assertThat(fAQCriteria).satisfies(
            criteria ->
                assertThat(criteria).is(copyFiltersAre(copy, (a, b) -> a == null || a instanceof Boolean ? a == b : a != b && a.equals(b))),
            criteria -> assertThat(criteria).isEqualTo(copy),
            criteria -> assertThat(criteria).hasSameHashCodeAs(copy)
        );

        assertThat(copy).satisfies(
            criteria -> assertThat(criteria).is(criteriaFiltersAre(Objects::nonNull)),
            criteria -> assertThat(criteria).isEqualTo(fAQCriteria)
        );
    }

    @Test
    void toStringVerifier() {
        var fAQCriteria = new FAQCriteria();

        assertThat(fAQCriteria).hasToString("FAQCriteria{}");
    }

    private static void setAllFilters(FAQCriteria fAQCriteria) {
        fAQCriteria.id();
        fAQCriteria.titre();
        fAQCriteria.contenuId();
        fAQCriteria.distinct();
    }

    private static Condition<FAQCriteria> criteriaFiltersAre(Function<Object, Boolean> condition) {
        return new Condition<>(
            criteria ->
                condition.apply(criteria.getId()) &&
                condition.apply(criteria.getTitre()) &&
                condition.apply(criteria.getContenuId()) &&
                condition.apply(criteria.getDistinct()),
            "every filter matches"
        );
    }

    private static Condition<FAQCriteria> copyFiltersAre(FAQCriteria copy, BiFunction<Object, Object, Boolean> condition) {
        return new Condition<>(
            criteria ->
                condition.apply(criteria.getId(), copy.getId()) &&
                condition.apply(criteria.getTitre(), copy.getTitre()) &&
                condition.apply(criteria.getContenuId(), copy.getContenuId()) &&
                condition.apply(criteria.getDistinct(), copy.getDistinct()),
            "every filter matches"
        );
    }
}
