package sn.dove.backend.service.criteria;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Function;
import org.assertj.core.api.Condition;
import org.junit.jupiter.api.Test;

class FeedBackCriteriaTest {

    @Test
    void newFeedBackCriteriaHasAllFiltersNullTest() {
        var feedBackCriteria = new FeedBackCriteria();
        assertThat(feedBackCriteria).is(criteriaFiltersAre(Objects::isNull));
    }

    @Test
    void feedBackCriteriaFluentMethodsCreatesFiltersTest() {
        var feedBackCriteria = new FeedBackCriteria();

        setAllFilters(feedBackCriteria);

        assertThat(feedBackCriteria).is(criteriaFiltersAre(Objects::nonNull));
    }

    @Test
    void feedBackCriteriaCopyCreatesNullFilterTest() {
        var feedBackCriteria = new FeedBackCriteria();
        var copy = feedBackCriteria.copy();

        assertThat(feedBackCriteria).satisfies(
            criteria ->
                assertThat(criteria).is(copyFiltersAre(copy, (a, b) -> a == null || a instanceof Boolean ? a == b : a != b && a.equals(b))),
            criteria -> assertThat(criteria).isEqualTo(copy),
            criteria -> assertThat(criteria).hasSameHashCodeAs(copy)
        );

        assertThat(copy).satisfies(
            criteria -> assertThat(criteria).is(criteriaFiltersAre(Objects::isNull)),
            criteria -> assertThat(criteria).isEqualTo(feedBackCriteria)
        );
    }

    @Test
    void feedBackCriteriaCopyDuplicatesEveryExistingFilterTest() {
        var feedBackCriteria = new FeedBackCriteria();
        setAllFilters(feedBackCriteria);

        var copy = feedBackCriteria.copy();

        assertThat(feedBackCriteria).satisfies(
            criteria ->
                assertThat(criteria).is(copyFiltersAre(copy, (a, b) -> a == null || a instanceof Boolean ? a == b : a != b && a.equals(b))),
            criteria -> assertThat(criteria).isEqualTo(copy),
            criteria -> assertThat(criteria).hasSameHashCodeAs(copy)
        );

        assertThat(copy).satisfies(
            criteria -> assertThat(criteria).is(criteriaFiltersAre(Objects::nonNull)),
            criteria -> assertThat(criteria).isEqualTo(feedBackCriteria)
        );
    }

    @Test
    void toStringVerifier() {
        var feedBackCriteria = new FeedBackCriteria();

        assertThat(feedBackCriteria).hasToString("FeedBackCriteria{}");
    }

    private static void setAllFilters(FeedBackCriteria feedBackCriteria) {
        feedBackCriteria.id();
        feedBackCriteria.note();
        feedBackCriteria.dateCreation();
        feedBackCriteria.statutTraitement();
        feedBackCriteria.utilisateurId();
        feedBackCriteria.contenuId();
        feedBackCriteria.distinct();
    }

    private static Condition<FeedBackCriteria> criteriaFiltersAre(Function<Object, Boolean> condition) {
        return new Condition<>(
            criteria ->
                condition.apply(criteria.getId()) &&
                condition.apply(criteria.getNote()) &&
                condition.apply(criteria.getDateCreation()) &&
                condition.apply(criteria.getStatutTraitement()) &&
                condition.apply(criteria.getUtilisateurId()) &&
                condition.apply(criteria.getContenuId()) &&
                condition.apply(criteria.getDistinct()),
            "every filter matches"
        );
    }

    private static Condition<FeedBackCriteria> copyFiltersAre(FeedBackCriteria copy, BiFunction<Object, Object, Boolean> condition) {
        return new Condition<>(
            criteria ->
                condition.apply(criteria.getId(), copy.getId()) &&
                condition.apply(criteria.getNote(), copy.getNote()) &&
                condition.apply(criteria.getDateCreation(), copy.getDateCreation()) &&
                condition.apply(criteria.getStatutTraitement(), copy.getStatutTraitement()) &&
                condition.apply(criteria.getUtilisateurId(), copy.getUtilisateurId()) &&
                condition.apply(criteria.getContenuId(), copy.getContenuId()) &&
                condition.apply(criteria.getDistinct(), copy.getDistinct()),
            "every filter matches"
        );
    }
}
