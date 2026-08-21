package sn.dove.backend.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static sn.dove.backend.domain.ContenuTestSamples.*;
import static sn.dove.backend.domain.FAQTestSamples.*;

import org.junit.jupiter.api.Test;
import sn.dove.backend.web.rest.TestUtil;

class FAQTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(FAQ.class);
        FAQ fAQ1 = getFAQSample1();
        FAQ fAQ2 = new FAQ();
        assertThat(fAQ1).isNotEqualTo(fAQ2);

        fAQ2.setId(fAQ1.getId());
        assertThat(fAQ1).isEqualTo(fAQ2);

        fAQ2 = getFAQSample2();
        assertThat(fAQ1).isNotEqualTo(fAQ2);
    }

    @Test
    void contenuTest() {
        FAQ fAQ = getFAQRandomSampleGenerator();
        Contenu contenuBack = getContenuRandomSampleGenerator();

        fAQ.setContenu(contenuBack);
        assertThat(fAQ.getContenu()).isEqualTo(contenuBack);

        fAQ.contenu(null);
        assertThat(fAQ.getContenu()).isNull();
    }
}
