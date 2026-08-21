package sn.dove.backend.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static sn.dove.backend.domain.ApplicationTestSamples.*;
import static sn.dove.backend.domain.ModuleTestSamples.*;

import org.junit.jupiter.api.Test;
import sn.dove.backend.web.rest.TestUtil;

class ModuleTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(Module.class);
        Module module1 = getModuleSample1();
        Module module2 = new Module();
        assertThat(module1).isNotEqualTo(module2);

        module2.setId(module1.getId());
        assertThat(module1).isEqualTo(module2);

        module2 = getModuleSample2();
        assertThat(module1).isNotEqualTo(module2);
    }

    @Test
    void applicationTest() {
        Module module = getModuleRandomSampleGenerator();
        Application applicationBack = getApplicationRandomSampleGenerator();

        module.setApplication(applicationBack);
        assertThat(module.getApplication()).isEqualTo(applicationBack);

        module.application(null);
        assertThat(module.getApplication()).isNull();
    }
}
