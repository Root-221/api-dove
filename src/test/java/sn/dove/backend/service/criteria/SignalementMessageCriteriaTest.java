package sn.dove.backend.service.criteria;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Function;
import org.assertj.core.api.Condition;
import org.junit.jupiter.api.Test;

class SignalementMessageCriteriaTest {

    @Test
    void newSignalementMessageCriteriaHasAllFiltersNullTest() {
        var signalementMessageCriteria = new SignalementMessageCriteria();
        assertThat(signalementMessageCriteria).is(criteriaFiltersAre(Objects::isNull));
    }

    @Test
    void signalementMessageCriteriaFluentMethodsCreatesFiltersTest() {
        var signalementMessageCriteria = new SignalementMessageCriteria();

        setAllFilters(signalementMessageCriteria);

        assertThat(signalementMessageCriteria).is(criteriaFiltersAre(Objects::nonNull));
    }

    @Test
    void signalementMessageCriteriaCopyCreatesNullFilterTest() {
        var signalementMessageCriteria = new SignalementMessageCriteria();
        var copy = signalementMessageCriteria.copy();

        assertThat(signalementMessageCriteria).satisfies(
            criteria ->
                assertThat(criteria).is(copyFiltersAre(copy, (a, b) -> a == null || a instanceof Boolean ? a == b : a != b && a.equals(b))),
            criteria -> assertThat(criteria).isEqualTo(copy),
            criteria -> assertThat(criteria).hasSameHashCodeAs(copy)
        );

        assertThat(copy).satisfies(
            criteria -> assertThat(criteria).is(criteriaFiltersAre(Objects::isNull)),
            criteria -> assertThat(criteria).isEqualTo(signalementMessageCriteria)
        );
    }

    @Test
    void signalementMessageCriteriaCopyDuplicatesEveryExistingFilterTest() {
        var signalementMessageCriteria = new SignalementMessageCriteria();
        setAllFilters(signalementMessageCriteria);

        var copy = signalementMessageCriteria.copy();

        assertThat(signalementMessageCriteria).satisfies(
            criteria ->
                assertThat(criteria).is(copyFiltersAre(copy, (a, b) -> a == null || a instanceof Boolean ? a == b : a != b && a.equals(b))),
            criteria -> assertThat(criteria).isEqualTo(copy),
            criteria -> assertThat(criteria).hasSameHashCodeAs(copy)
        );

        assertThat(copy).satisfies(
            criteria -> assertThat(criteria).is(criteriaFiltersAre(Objects::nonNull)),
            criteria -> assertThat(criteria).isEqualTo(signalementMessageCriteria)
        );
    }

    @Test
    void toStringVerifier() {
        var signalementMessageCriteria = new SignalementMessageCriteria();

        assertThat(signalementMessageCriteria).hasToString("SignalementMessageCriteria{}");
    }

    private static void setAllFilters(SignalementMessageCriteria signalementMessageCriteria) {
        signalementMessageCriteria.id();
        signalementMessageCriteria.dateSignalement();
        signalementMessageCriteria.utilisateurId();
        signalementMessageCriteria.messageId();
        signalementMessageCriteria.distinct();
    }

    private static Condition<SignalementMessageCriteria> criteriaFiltersAre(Function<Object, Boolean> condition) {
        return new Condition<>(
            criteria ->
                condition.apply(criteria.getId()) &&
                condition.apply(criteria.getDateSignalement()) &&
                condition.apply(criteria.getUtilisateurId()) &&
                condition.apply(criteria.getMessageId()) &&
                condition.apply(criteria.getDistinct()),
            "every filter matches"
        );
    }

    private static Condition<SignalementMessageCriteria> copyFiltersAre(
        SignalementMessageCriteria copy,
        BiFunction<Object, Object, Boolean> condition
    ) {
        return new Condition<>(
            criteria ->
                condition.apply(criteria.getId(), copy.getId()) &&
                condition.apply(criteria.getDateSignalement(), copy.getDateSignalement()) &&
                condition.apply(criteria.getUtilisateurId(), copy.getUtilisateurId()) &&
                condition.apply(criteria.getMessageId(), copy.getMessageId()) &&
                condition.apply(criteria.getDistinct(), copy.getDistinct()),
            "every filter matches"
        );
    }
}
