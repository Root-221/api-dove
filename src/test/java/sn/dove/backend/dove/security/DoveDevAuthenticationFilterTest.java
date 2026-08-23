package sn.dove.backend.dove.security;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import org.springframework.mock.env.MockEnvironment;
import sn.dove.backend.dove.config.DoveProperties;

/**
 * Verifies the fail-fast guard added to DoveDevAuthenticationFilter: it must refuse to
 * construct (and therefore refuse to let the application start) if dove.auth.dev-header-enabled
 * is true while the "prod" Spring profile is active, since that flag allows a full
 * authentication bypass via the X-Dove-User-Id header.
 */
class DoveDevAuthenticationFilterTest {

    @Test
    void refusesToStartWhenDevHeaderEnabledInProd() {
        DoveProperties properties = devHeaderEnabled(true);
        MockEnvironment environment = environmentWithProfile("prod");

        assertThatThrownBy(() -> new DoveDevAuthenticationFilter(properties, environment))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("dev-header-enabled");
    }

    @Test
    void startsWhenDevHeaderDisabledInProd() {
        DoveProperties properties = devHeaderEnabled(false);
        MockEnvironment environment = environmentWithProfile("prod");

        assertThatCode(() -> new DoveDevAuthenticationFilter(properties, environment)).doesNotThrowAnyException();
    }

    @Test
    void startsWhenDevHeaderEnabledOutsideProd() {
        DoveProperties properties = devHeaderEnabled(true);
        MockEnvironment environment = environmentWithProfile("dev");

        assertThatCode(() -> new DoveDevAuthenticationFilter(properties, environment)).doesNotThrowAnyException();
    }

    private static DoveProperties devHeaderEnabled(boolean enabled) {
        DoveProperties properties = new DoveProperties();
        properties.getAuth().setDevHeaderEnabled(enabled);
        return properties;
    }

    private static MockEnvironment environmentWithProfile(String profile) {
        MockEnvironment environment = new MockEnvironment();
        environment.setActiveProfiles(profile);
        return environment;
    }
}
