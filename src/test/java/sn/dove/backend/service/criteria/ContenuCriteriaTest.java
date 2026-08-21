package sn.dove.backend.service.criteria;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Function;
import org.assertj.core.api.Condition;
import org.junit.jupiter.api.Test;

class ContenuCriteriaTest {

    @Test
    void newContenuCriteriaHasAllFiltersNullTest() {
        var contenuCriteria = new ContenuCriteria();
        assertThat(contenuCriteria).is(criteriaFiltersAre(Objects::isNull));
    }

    @Test
    void contenuCriteriaFluentMethodsCreatesFiltersTest() {
        var contenuCriteria = new ContenuCriteria();

        setAllFilters(contenuCriteria);

        assertThat(contenuCriteria).is(criteriaFiltersAre(Objects::nonNull));
    }

    @Test
    void contenuCriteriaCopyCreatesNullFilterTest() {
        var contenuCriteria = new ContenuCriteria();
        var copy = contenuCriteria.copy();

        assertThat(contenuCriteria).satisfies(
            criteria ->
                assertThat(criteria).is(copyFiltersAre(copy, (a, b) -> a == null || a instanceof Boolean ? a == b : a != b && a.equals(b))),
            criteria -> assertThat(criteria).isEqualTo(copy),
            criteria -> assertThat(criteria).hasSameHashCodeAs(copy)
        );

        assertThat(copy).satisfies(
            criteria -> assertThat(criteria).is(criteriaFiltersAre(Objects::isNull)),
            criteria -> assertThat(criteria).isEqualTo(contenuCriteria)
        );
    }

    @Test
    void contenuCriteriaCopyDuplicatesEveryExistingFilterTest() {
        var contenuCriteria = new ContenuCriteria();
        setAllFilters(contenuCriteria);

        var copy = contenuCriteria.copy();

        assertThat(contenuCriteria).satisfies(
            criteria ->
                assertThat(criteria).is(copyFiltersAre(copy, (a, b) -> a == null || a instanceof Boolean ? a == b : a != b && a.equals(b))),
            criteria -> assertThat(criteria).isEqualTo(copy),
            criteria -> assertThat(criteria).hasSameHashCodeAs(copy)
        );

        assertThat(copy).satisfies(
            criteria -> assertThat(criteria).is(criteriaFiltersAre(Objects::nonNull)),
            criteria -> assertThat(criteria).isEqualTo(contenuCriteria)
        );
    }

    @Test
    void toStringVerifier() {
        var contenuCriteria = new ContenuCriteria();

        assertThat(contenuCriteria).hasToString("ContenuCriteria{}");
    }

    private static void setAllFilters(ContenuCriteria contenuCriteria) {
        contenuCriteria.id();
        contenuCriteria.titre();
        contenuCriteria.typeContenu();
        contenuCriteria.statut();
        contenuCriteria.version();
        contenuCriteria.datePublication();
        contenuCriteria.derniereMiseAJour();
        contenuCriteria.prochaineRevue();
        contenuCriteria.progression();
        contenuCriteria.dateCreation();
        contenuCriteria.applicationId();
        contenuCriteria.moduleId();
        contenuCriteria.metierId();
        contenuCriteria.auteurId();
        contenuCriteria.validateurId();
        contenuCriteria.distinct();
    }

    private static Condition<ContenuCriteria> criteriaFiltersAre(Function<Object, Boolean> condition) {
        return new Condition<>(
            criteria ->
                condition.apply(criteria.getId()) &&
                condition.apply(criteria.getTitre()) &&
                condition.apply(criteria.getTypeContenu()) &&
                condition.apply(criteria.getStatut()) &&
                condition.apply(criteria.getVersion()) &&
                condition.apply(criteria.getDatePublication()) &&
                condition.apply(criteria.getDerniereMiseAJour()) &&
                condition.apply(criteria.getProchaineRevue()) &&
                condition.apply(criteria.getProgression()) &&
                condition.apply(criteria.getDateCreation()) &&
                condition.apply(criteria.getApplicationId()) &&
                condition.apply(criteria.getModuleId()) &&
                condition.apply(criteria.getMetierId()) &&
                condition.apply(criteria.getAuteurId()) &&
                condition.apply(criteria.getValidateurId()) &&
                condition.apply(criteria.getDistinct()),
            "every filter matches"
        );
    }

    private static Condition<ContenuCriteria> copyFiltersAre(ContenuCriteria copy, BiFunction<Object, Object, Boolean> condition) {
        return new Condition<>(
            criteria ->
                condition.apply(criteria.getId(), copy.getId()) &&
                condition.apply(criteria.getTitre(), copy.getTitre()) &&
                condition.apply(criteria.getTypeContenu(), copy.getTypeContenu()) &&
                condition.apply(criteria.getStatut(), copy.getStatut()) &&
                condition.apply(criteria.getVersion(), copy.getVersion()) &&
                condition.apply(criteria.getDatePublication(), copy.getDatePublication()) &&
                condition.apply(criteria.getDerniereMiseAJour(), copy.getDerniereMiseAJour()) &&
                condition.apply(criteria.getProchaineRevue(), copy.getProchaineRevue()) &&
                condition.apply(criteria.getProgression(), copy.getProgression()) &&
                condition.apply(criteria.getDateCreation(), copy.getDateCreation()) &&
                condition.apply(criteria.getApplicationId(), copy.getApplicationId()) &&
                condition.apply(criteria.getModuleId(), copy.getModuleId()) &&
                condition.apply(criteria.getMetierId(), copy.getMetierId()) &&
                condition.apply(criteria.getAuteurId(), copy.getAuteurId()) &&
                condition.apply(criteria.getValidateurId(), copy.getValidateurId()) &&
                condition.apply(criteria.getDistinct(), copy.getDistinct()),
            "every filter matches"
        );
    }
}
