package sn.dove.backend.service.criteria;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Function;
import org.assertj.core.api.Condition;
import org.junit.jupiter.api.Test;

class ReactionCriteriaTest {

    @Test
    void newReactionCriteriaHasAllFiltersNullTest() {
        var reactionCriteria = new ReactionCriteria();
        assertThat(reactionCriteria).is(criteriaFiltersAre(Objects::isNull));
    }

    @Test
    void reactionCriteriaFluentMethodsCreatesFiltersTest() {
        var reactionCriteria = new ReactionCriteria();

        setAllFilters(reactionCriteria);

        assertThat(reactionCriteria).is(criteriaFiltersAre(Objects::nonNull));
    }

    @Test
    void reactionCriteriaCopyCreatesNullFilterTest() {
        var reactionCriteria = new ReactionCriteria();
        var copy = reactionCriteria.copy();

        assertThat(reactionCriteria).satisfies(
            criteria ->
                assertThat(criteria).is(copyFiltersAre(copy, (a, b) -> a == null || a instanceof Boolean ? a == b : a != b && a.equals(b))),
            criteria -> assertThat(criteria).isEqualTo(copy),
            criteria -> assertThat(criteria).hasSameHashCodeAs(copy)
        );

        assertThat(copy).satisfies(
            criteria -> assertThat(criteria).is(criteriaFiltersAre(Objects::isNull)),
            criteria -> assertThat(criteria).isEqualTo(reactionCriteria)
        );
    }

    @Test
    void reactionCriteriaCopyDuplicatesEveryExistingFilterTest() {
        var reactionCriteria = new ReactionCriteria();
        setAllFilters(reactionCriteria);

        var copy = reactionCriteria.copy();

        assertThat(reactionCriteria).satisfies(
            criteria ->
                assertThat(criteria).is(copyFiltersAre(copy, (a, b) -> a == null || a instanceof Boolean ? a == b : a != b && a.equals(b))),
            criteria -> assertThat(criteria).isEqualTo(copy),
            criteria -> assertThat(criteria).hasSameHashCodeAs(copy)
        );

        assertThat(copy).satisfies(
            criteria -> assertThat(criteria).is(criteriaFiltersAre(Objects::nonNull)),
            criteria -> assertThat(criteria).isEqualTo(reactionCriteria)
        );
    }

    @Test
    void toStringVerifier() {
        var reactionCriteria = new ReactionCriteria();

        assertThat(reactionCriteria).hasToString("ReactionCriteria{}");
    }

    private static void setAllFilters(ReactionCriteria reactionCriteria) {
        reactionCriteria.id();
        reactionCriteria.type();
        reactionCriteria.utilisateurId();
        reactionCriteria.messageId();
        reactionCriteria.distinct();
    }

    private static Condition<ReactionCriteria> criteriaFiltersAre(Function<Object, Boolean> condition) {
        return new Condition<>(
            criteria ->
                condition.apply(criteria.getId()) &&
                condition.apply(criteria.getType()) &&
                condition.apply(criteria.getUtilisateurId()) &&
                condition.apply(criteria.getMessageId()) &&
                condition.apply(criteria.getDistinct()),
            "every filter matches"
        );
    }

    private static Condition<ReactionCriteria> copyFiltersAre(ReactionCriteria copy, BiFunction<Object, Object, Boolean> condition) {
        return new Condition<>(
            criteria ->
                condition.apply(criteria.getId(), copy.getId()) &&
                condition.apply(criteria.getType(), copy.getType()) &&
                condition.apply(criteria.getUtilisateurId(), copy.getUtilisateurId()) &&
                condition.apply(criteria.getMessageId(), copy.getMessageId()) &&
                condition.apply(criteria.getDistinct(), copy.getDistinct()),
            "every filter matches"
        );
    }
}
