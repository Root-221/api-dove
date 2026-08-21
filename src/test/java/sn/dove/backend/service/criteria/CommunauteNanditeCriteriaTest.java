package sn.dove.backend.service.criteria;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Function;
import org.assertj.core.api.Condition;
import org.junit.jupiter.api.Test;

class CommunauteNanditeCriteriaTest {

    @Test
    void newCommunauteNanditeCriteriaHasAllFiltersNullTest() {
        var communauteNanditeCriteria = new CommunauteNanditeCriteria();
        assertThat(communauteNanditeCriteria).is(criteriaFiltersAre(Objects::isNull));
    }

    @Test
    void communauteNanditeCriteriaFluentMethodsCreatesFiltersTest() {
        var communauteNanditeCriteria = new CommunauteNanditeCriteria();

        setAllFilters(communauteNanditeCriteria);

        assertThat(communauteNanditeCriteria).is(criteriaFiltersAre(Objects::nonNull));
    }

    @Test
    void communauteNanditeCriteriaCopyCreatesNullFilterTest() {
        var communauteNanditeCriteria = new CommunauteNanditeCriteria();
        var copy = communauteNanditeCriteria.copy();

        assertThat(communauteNanditeCriteria).satisfies(
            criteria ->
                assertThat(criteria).is(copyFiltersAre(copy, (a, b) -> a == null || a instanceof Boolean ? a == b : a != b && a.equals(b))),
            criteria -> assertThat(criteria).isEqualTo(copy),
            criteria -> assertThat(criteria).hasSameHashCodeAs(copy)
        );

        assertThat(copy).satisfies(
            criteria -> assertThat(criteria).is(criteriaFiltersAre(Objects::isNull)),
            criteria -> assertThat(criteria).isEqualTo(communauteNanditeCriteria)
        );
    }

    @Test
    void communauteNanditeCriteriaCopyDuplicatesEveryExistingFilterTest() {
        var communauteNanditeCriteria = new CommunauteNanditeCriteria();
        setAllFilters(communauteNanditeCriteria);

        var copy = communauteNanditeCriteria.copy();

        assertThat(communauteNanditeCriteria).satisfies(
            criteria ->
                assertThat(criteria).is(copyFiltersAre(copy, (a, b) -> a == null || a instanceof Boolean ? a == b : a != b && a.equals(b))),
            criteria -> assertThat(criteria).isEqualTo(copy),
            criteria -> assertThat(criteria).hasSameHashCodeAs(copy)
        );

        assertThat(copy).satisfies(
            criteria -> assertThat(criteria).is(criteriaFiltersAre(Objects::nonNull)),
            criteria -> assertThat(criteria).isEqualTo(communauteNanditeCriteria)
        );
    }

    @Test
    void toStringVerifier() {
        var communauteNanditeCriteria = new CommunauteNanditeCriteria();

        assertThat(communauteNanditeCriteria).hasToString("CommunauteNanditeCriteria{}");
    }

    private static void setAllFilters(CommunauteNanditeCriteria communauteNanditeCriteria) {
        communauteNanditeCriteria.id();
        communauteNanditeCriteria.nom();
        communauteNanditeCriteria.discussionsId();
        communauteNanditeCriteria.membresId();
        communauteNanditeCriteria.distinct();
    }

    private static Condition<CommunauteNanditeCriteria> criteriaFiltersAre(Function<Object, Boolean> condition) {
        return new Condition<>(
            criteria ->
                condition.apply(criteria.getId()) &&
                condition.apply(criteria.getNom()) &&
                condition.apply(criteria.getDiscussionsId()) &&
                condition.apply(criteria.getMembresId()) &&
                condition.apply(criteria.getDistinct()),
            "every filter matches"
        );
    }

    private static Condition<CommunauteNanditeCriteria> copyFiltersAre(
        CommunauteNanditeCriteria copy,
        BiFunction<Object, Object, Boolean> condition
    ) {
        return new Condition<>(
            criteria ->
                condition.apply(criteria.getId(), copy.getId()) &&
                condition.apply(criteria.getNom(), copy.getNom()) &&
                condition.apply(criteria.getDiscussionsId(), copy.getDiscussionsId()) &&
                condition.apply(criteria.getMembresId(), copy.getMembresId()) &&
                condition.apply(criteria.getDistinct(), copy.getDistinct()),
            "every filter matches"
        );
    }
}
