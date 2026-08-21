package sn.dove.backend.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static sn.dove.backend.domain.ContenuTestSamples.*;
import static sn.dove.backend.domain.MediaTestSamples.*;

import org.junit.jupiter.api.Test;
import sn.dove.backend.web.rest.TestUtil;

class MediaTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(Media.class);
        Media media1 = getMediaSample1();
        Media media2 = new Media();
        assertThat(media1).isNotEqualTo(media2);

        media2.setId(media1.getId());
        assertThat(media1).isEqualTo(media2);

        media2 = getMediaSample2();
        assertThat(media1).isNotEqualTo(media2);
    }

    @Test
    void contenuTest() {
        Media media = getMediaRandomSampleGenerator();
        Contenu contenuBack = getContenuRandomSampleGenerator();

        media.setContenu(contenuBack);
        assertThat(media.getContenu()).isEqualTo(contenuBack);

        media.contenu(null);
        assertThat(media.getContenu()).isNull();
    }
}
