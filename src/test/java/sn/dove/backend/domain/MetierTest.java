package sn.dove.backend.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static sn.dove.backend.domain.MetierTestSamples.*;

import org.junit.jupiter.api.Test;
import sn.dove.backend.web.rest.TestUtil;

class MetierTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(Metier.class);
        Metier metier1 = getMetierSample1();
        Metier metier2 = new Metier();
        assertThat(metier1).isNotEqualTo(metier2);

        metier2.setId(metier1.getId());
        assertThat(metier1).isEqualTo(metier2);

        metier2 = getMetierSample2();
        assertThat(metier1).isNotEqualTo(metier2);
    }
}
