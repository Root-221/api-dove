package sn.dove.backend.service.criteria;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Function;
import org.assertj.core.api.Condition;
import org.junit.jupiter.api.Test;

class MediaCriteriaTest {

    @Test
    void newMediaCriteriaHasAllFiltersNullTest() {
        var mediaCriteria = new MediaCriteria();
        assertThat(mediaCriteria).is(criteriaFiltersAre(Objects::isNull));
    }

    @Test
    void mediaCriteriaFluentMethodsCreatesFiltersTest() {
        var mediaCriteria = new MediaCriteria();

        setAllFilters(mediaCriteria);

        assertThat(mediaCriteria).is(criteriaFiltersAre(Objects::nonNull));
    }

    @Test
    void mediaCriteriaCopyCreatesNullFilterTest() {
        var mediaCriteria = new MediaCriteria();
        var copy = mediaCriteria.copy();

        assertThat(mediaCriteria).satisfies(
            criteria ->
                assertThat(criteria).is(copyFiltersAre(copy, (a, b) -> a == null || a instanceof Boolean ? a == b : a != b && a.equals(b))),
            criteria -> assertThat(criteria).isEqualTo(copy),
            criteria -> assertThat(criteria).hasSameHashCodeAs(copy)
        );

        assertThat(copy).satisfies(
            criteria -> assertThat(criteria).is(criteriaFiltersAre(Objects::isNull)),
            criteria -> assertThat(criteria).isEqualTo(mediaCriteria)
        );
    }

    @Test
    void mediaCriteriaCopyDuplicatesEveryExistingFilterTest() {
        var mediaCriteria = new MediaCriteria();
        setAllFilters(mediaCriteria);

        var copy = mediaCriteria.copy();

        assertThat(mediaCriteria).satisfies(
            criteria ->
                assertThat(criteria).is(copyFiltersAre(copy, (a, b) -> a == null || a instanceof Boolean ? a == b : a != b && a.equals(b))),
            criteria -> assertThat(criteria).isEqualTo(copy),
            criteria -> assertThat(criteria).hasSameHashCodeAs(copy)
        );

        assertThat(copy).satisfies(
            criteria -> assertThat(criteria).is(criteriaFiltersAre(Objects::nonNull)),
            criteria -> assertThat(criteria).isEqualTo(mediaCriteria)
        );
    }

    @Test
    void toStringVerifier() {
        var mediaCriteria = new MediaCriteria();

        assertThat(mediaCriteria).hasToString("MediaCriteria{}");
    }

    private static void setAllFilters(MediaCriteria mediaCriteria) {
        mediaCriteria.id();
        mediaCriteria.titre();
        mediaCriteria.etape();
        mediaCriteria.url();
        mediaCriteria.contenuId();
        mediaCriteria.distinct();
    }

    private static Condition<MediaCriteria> criteriaFiltersAre(Function<Object, Boolean> condition) {
        return new Condition<>(
            criteria ->
                condition.apply(criteria.getId()) &&
                condition.apply(criteria.getTitre()) &&
                condition.apply(criteria.getEtape()) &&
                condition.apply(criteria.getUrl()) &&
                condition.apply(criteria.getContenuId()) &&
                condition.apply(criteria.getDistinct()),
            "every filter matches"
        );
    }

    private static Condition<MediaCriteria> copyFiltersAre(MediaCriteria copy, BiFunction<Object, Object, Boolean> condition) {
        return new Condition<>(
            criteria ->
                condition.apply(criteria.getId(), copy.getId()) &&
                condition.apply(criteria.getTitre(), copy.getTitre()) &&
                condition.apply(criteria.getEtape(), copy.getEtape()) &&
                condition.apply(criteria.getUrl(), copy.getUrl()) &&
                condition.apply(criteria.getContenuId(), copy.getContenuId()) &&
                condition.apply(criteria.getDistinct(), copy.getDistinct()),
            "every filter matches"
        );
    }
}
