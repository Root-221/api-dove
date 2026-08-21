package sn.dove.backend.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static sn.dove.backend.domain.ApplicationTestSamples.*;
import static sn.dove.backend.domain.ModuleTestSamples.*;

import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.Test;
import sn.dove.backend.web.rest.TestUtil;

class ApplicationTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(Application.class);
        Application application1 = getApplicationSample1();
        Application application2 = new Application();
        assertThat(application1).isNotEqualTo(application2);

        application2.setId(application1.getId());
        assertThat(application1).isEqualTo(application2);

        application2 = getApplicationSample2();
        assertThat(application1).isNotEqualTo(application2);
    }

    @Test
    void modulesTest() {
        Application application = getApplicationRandomSampleGenerator();
        Module moduleBack = getModuleRandomSampleGenerator();

        application.addModules(moduleBack);
        assertThat(application.getModuleses()).containsOnly(moduleBack);
        assertThat(moduleBack.getApplication()).isEqualTo(application);

        application.removeModules(moduleBack);
        assertThat(application.getModuleses()).doesNotContain(moduleBack);
        assertThat(moduleBack.getApplication()).isNull();

        application.moduleses(new HashSet<>(Set.of(moduleBack)));
        assertThat(application.getModuleses()).containsOnly(moduleBack);
        assertThat(moduleBack.getApplication()).isEqualTo(application);

        application.setModuleses(new HashSet<>());
        assertThat(application.getModuleses()).doesNotContain(moduleBack);
        assertThat(moduleBack.getApplication()).isNull();
    }
}
