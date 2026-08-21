package sn.dove.backend.service.criteria;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Function;
import org.assertj.core.api.Condition;
import org.junit.jupiter.api.Test;

class SignalementCriteriaTest {

    @Test
    void newSignalementCriteriaHasAllFiltersNullTest() {
        var signalementCriteria = new SignalementCriteria();
        assertThat(signalementCriteria).is(criteriaFiltersAre(Objects::isNull));
    }

    @Test
    void signalementCriteriaFluentMethodsCreatesFiltersTest() {
        var signalementCriteria = new SignalementCriteria();

        setAllFilters(signalementCriteria);

        assertThat(signalementCriteria).is(criteriaFiltersAre(Objects::nonNull));
    }

    @Test
    void signalementCriteriaCopyCreatesNullFilterTest() {
        var signalementCriteria = new SignalementCriteria();
        var copy = signalementCriteria.copy();

        assertThat(signalementCriteria).satisfies(
            criteria ->
                assertThat(criteria).is(copyFiltersAre(copy, (a, b) -> a == null || a instanceof Boolean ? a == b : a != b && a.equals(b))),
            criteria -> assertThat(criteria).isEqualTo(copy),
            criteria -> assertThat(criteria).hasSameHashCodeAs(copy)
        );

        assertThat(copy).satisfies(
            criteria -> assertThat(criteria).is(criteriaFiltersAre(Objects::isNull)),
            criteria -> assertThat(criteria).isEqualTo(signalementCriteria)
        );
    }

    @Test
    void signalementCriteriaCopyDuplicatesEveryExistingFilterTest() {
        var signalementCriteria = new SignalementCriteria();
        setAllFilters(signalementCriteria);

        var copy = signalementCriteria.copy();

        assertThat(signalementCriteria).satisfies(
            criteria ->
                assertThat(criteria).is(copyFiltersAre(copy, (a, b) -> a == null || a instanceof Boolean ? a == b : a != b && a.equals(b))),
            criteria -> assertThat(criteria).isEqualTo(copy),
            criteria -> assertThat(criteria).hasSameHashCodeAs(copy)
        );

        assertThat(copy).satisfies(
            criteria -> assertThat(criteria).is(criteriaFiltersAre(Objects::nonNull)),
            criteria -> assertThat(criteria).isEqualTo(signalementCriteria)
        );
    }

    @Test
    void toStringVerifier() {
        var signalementCriteria = new SignalementCriteria();

        assertThat(signalementCriteria).hasToString("SignalementCriteria{}");
    }

    private static void setAllFilters(SignalementCriteria signalementCriteria) {
        signalementCriteria.id();
        signalementCriteria.dateSignalement();
        signalementCriteria.statut();
        signalementCriteria.utilisateurId();
        signalementCriteria.contenuId();
        signalementCriteria.distinct();
    }

    private static Condition<SignalementCriteria> criteriaFiltersAre(Function<Object, Boolean> condition) {
        return new Condition<>(
            criteria ->
                condition.apply(criteria.getId()) &&
                condition.apply(criteria.getDateSignalement()) &&
                condition.apply(criteria.getStatut()) &&
                condition.apply(criteria.getUtilisateurId()) &&
                condition.apply(criteria.getContenuId()) &&
                condition.apply(criteria.getDistinct()),
            "every filter matches"
        );
    }

    private static Condition<SignalementCriteria> copyFiltersAre(SignalementCriteria copy, BiFunction<Object, Object, Boolean> condition) {
        return new Condition<>(
            criteria ->
                condition.apply(criteria.getId(), copy.getId()) &&
                condition.apply(criteria.getDateSignalement(), copy.getDateSignalement()) &&
                condition.apply(criteria.getStatut(), copy.getStatut()) &&
                condition.apply(criteria.getUtilisateurId(), copy.getUtilisateurId()) &&
                condition.apply(criteria.getContenuId(), copy.getContenuId()) &&
                condition.apply(criteria.getDistinct(), copy.getDistinct()),
            "every filter matches"
        );
    }
}
