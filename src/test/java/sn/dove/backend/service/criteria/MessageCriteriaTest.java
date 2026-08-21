package sn.dove.backend.service.criteria;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Function;
import org.assertj.core.api.Condition;
import org.junit.jupiter.api.Test;

class MessageCriteriaTest {

    @Test
    void newMessageCriteriaHasAllFiltersNullTest() {
        var messageCriteria = new MessageCriteria();
        assertThat(messageCriteria).is(criteriaFiltersAre(Objects::isNull));
    }

    @Test
    void messageCriteriaFluentMethodsCreatesFiltersTest() {
        var messageCriteria = new MessageCriteria();

        setAllFilters(messageCriteria);

        assertThat(messageCriteria).is(criteriaFiltersAre(Objects::nonNull));
    }

    @Test
    void messageCriteriaCopyCreatesNullFilterTest() {
        var messageCriteria = new MessageCriteria();
        var copy = messageCriteria.copy();

        assertThat(messageCriteria).satisfies(
            criteria ->
                assertThat(criteria).is(copyFiltersAre(copy, (a, b) -> a == null || a instanceof Boolean ? a == b : a != b && a.equals(b))),
            criteria -> assertThat(criteria).isEqualTo(copy),
            criteria -> assertThat(criteria).hasSameHashCodeAs(copy)
        );

        assertThat(copy).satisfies(
            criteria -> assertThat(criteria).is(criteriaFiltersAre(Objects::isNull)),
            criteria -> assertThat(criteria).isEqualTo(messageCriteria)
        );
    }

    @Test
    void messageCriteriaCopyDuplicatesEveryExistingFilterTest() {
        var messageCriteria = new MessageCriteria();
        setAllFilters(messageCriteria);

        var copy = messageCriteria.copy();

        assertThat(messageCriteria).satisfies(
            criteria ->
                assertThat(criteria).is(copyFiltersAre(copy, (a, b) -> a == null || a instanceof Boolean ? a == b : a != b && a.equals(b))),
            criteria -> assertThat(criteria).isEqualTo(copy),
            criteria -> assertThat(criteria).hasSameHashCodeAs(copy)
        );

        assertThat(copy).satisfies(
            criteria -> assertThat(criteria).is(criteriaFiltersAre(Objects::nonNull)),
            criteria -> assertThat(criteria).isEqualTo(messageCriteria)
        );
    }

    @Test
    void toStringVerifier() {
        var messageCriteria = new MessageCriteria();

        assertThat(messageCriteria).hasToString("MessageCriteria{}");
    }

    private static void setAllFilters(MessageCriteria messageCriteria) {
        messageCriteria.id();
        messageCriteria.dateEnvoi();
        messageCriteria.reponsesId();
        messageCriteria.signalementsId();
        messageCriteria.emetteurId();
        messageCriteria.metierId();
        messageCriteria.discussionId();
        messageCriteria.messageParentId();
        messageCriteria.distinct();
    }

    private static Condition<MessageCriteria> criteriaFiltersAre(Function<Object, Boolean> condition) {
        return new Condition<>(
            criteria ->
                condition.apply(criteria.getId()) &&
                condition.apply(criteria.getDateEnvoi()) &&
                condition.apply(criteria.getReponsesId()) &&
                condition.apply(criteria.getSignalementsId()) &&
                condition.apply(criteria.getEmetteurId()) &&
                condition.apply(criteria.getMetierId()) &&
                condition.apply(criteria.getDiscussionId()) &&
                condition.apply(criteria.getMessageParentId()) &&
                condition.apply(criteria.getDistinct()),
            "every filter matches"
        );
    }

    private static Condition<MessageCriteria> copyFiltersAre(MessageCriteria copy, BiFunction<Object, Object, Boolean> condition) {
        return new Condition<>(
            criteria ->
                condition.apply(criteria.getId(), copy.getId()) &&
                condition.apply(criteria.getDateEnvoi(), copy.getDateEnvoi()) &&
                condition.apply(criteria.getReponsesId(), copy.getReponsesId()) &&
                condition.apply(criteria.getSignalementsId(), copy.getSignalementsId()) &&
                condition.apply(criteria.getEmetteurId(), copy.getEmetteurId()) &&
                condition.apply(criteria.getMetierId(), copy.getMetierId()) &&
                condition.apply(criteria.getDiscussionId(), copy.getDiscussionId()) &&
                condition.apply(criteria.getMessageParentId(), copy.getMessageParentId()) &&
                condition.apply(criteria.getDistinct(), copy.getDistinct()),
            "every filter matches"
        );
    }
}
