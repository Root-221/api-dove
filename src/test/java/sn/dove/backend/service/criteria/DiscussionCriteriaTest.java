package sn.dove.backend.service.criteria;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Function;
import org.assertj.core.api.Condition;
import org.junit.jupiter.api.Test;

class DiscussionCriteriaTest {

    @Test
    void newDiscussionCriteriaHasAllFiltersNullTest() {
        var discussionCriteria = new DiscussionCriteria();
        assertThat(discussionCriteria).is(criteriaFiltersAre(Objects::isNull));
    }

    @Test
    void discussionCriteriaFluentMethodsCreatesFiltersTest() {
        var discussionCriteria = new DiscussionCriteria();

        setAllFilters(discussionCriteria);

        assertThat(discussionCriteria).is(criteriaFiltersAre(Objects::nonNull));
    }

    @Test
    void discussionCriteriaCopyCreatesNullFilterTest() {
        var discussionCriteria = new DiscussionCriteria();
        var copy = discussionCriteria.copy();

        assertThat(discussionCriteria).satisfies(
            criteria ->
                assertThat(criteria).is(copyFiltersAre(copy, (a, b) -> a == null || a instanceof Boolean ? a == b : a != b && a.equals(b))),
            criteria -> assertThat(criteria).isEqualTo(copy),
            criteria -> assertThat(criteria).hasSameHashCodeAs(copy)
        );

        assertThat(copy).satisfies(
            criteria -> assertThat(criteria).is(criteriaFiltersAre(Objects::isNull)),
            criteria -> assertThat(criteria).isEqualTo(discussionCriteria)
        );
    }

    @Test
    void discussionCriteriaCopyDuplicatesEveryExistingFilterTest() {
        var discussionCriteria = new DiscussionCriteria();
        setAllFilters(discussionCriteria);

        var copy = discussionCriteria.copy();

        assertThat(discussionCriteria).satisfies(
            criteria ->
                assertThat(criteria).is(copyFiltersAre(copy, (a, b) -> a == null || a instanceof Boolean ? a == b : a != b && a.equals(b))),
            criteria -> assertThat(criteria).isEqualTo(copy),
            criteria -> assertThat(criteria).hasSameHashCodeAs(copy)
        );

        assertThat(copy).satisfies(
            criteria -> assertThat(criteria).is(criteriaFiltersAre(Objects::nonNull)),
            criteria -> assertThat(criteria).isEqualTo(discussionCriteria)
        );
    }

    @Test
    void toStringVerifier() {
        var discussionCriteria = new DiscussionCriteria();

        assertThat(discussionCriteria).hasToString("DiscussionCriteria{}");
    }

    private static void setAllFilters(DiscussionCriteria discussionCriteria) {
        discussionCriteria.id();
        discussionCriteria.titre();
        discussionCriteria.dateCreation();
        discussionCriteria.messagesId();
        discussionCriteria.createurId();
        discussionCriteria.communauteNanditeId();
        discussionCriteria.distinct();
    }

    private static Condition<DiscussionCriteria> criteriaFiltersAre(Function<Object, Boolean> condition) {
        return new Condition<>(
            criteria ->
                condition.apply(criteria.getId()) &&
                condition.apply(criteria.getTitre()) &&
                condition.apply(criteria.getDateCreation()) &&
                condition.apply(criteria.getMessagesId()) &&
                condition.apply(criteria.getCreateurId()) &&
                condition.apply(criteria.getCommunauteNanditeId()) &&
                condition.apply(criteria.getDistinct()),
            "every filter matches"
        );
    }

    private static Condition<DiscussionCriteria> copyFiltersAre(DiscussionCriteria copy, BiFunction<Object, Object, Boolean> condition) {
        return new Condition<>(
            criteria ->
                condition.apply(criteria.getId(), copy.getId()) &&
                condition.apply(criteria.getTitre(), copy.getTitre()) &&
                condition.apply(criteria.getDateCreation(), copy.getDateCreation()) &&
                condition.apply(criteria.getMessagesId(), copy.getMessagesId()) &&
                condition.apply(criteria.getCreateurId(), copy.getCreateurId()) &&
                condition.apply(criteria.getCommunauteNanditeId(), copy.getCommunauteNanditeId()) &&
                condition.apply(criteria.getDistinct(), copy.getDistinct()),
            "every filter matches"
        );
    }
}
