package sn.dove.backend.dove.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import sn.dove.backend.dove.security.DoveDevAuthenticationFilter;

@Configuration
@EnableConfigurationProperties(DoveProperties.class)
public class DoveConfiguration {

    /**
     * The development authentication filter belongs to the Spring Security
     * chain. Disabling its automatic servlet registration prevents it from
     * being invoked a second time outside that chain.
     */
    @Bean
    FilterRegistrationBean<DoveDevAuthenticationFilter> doveDevAuthenticationFilterRegistration(
        DoveDevAuthenticationFilter filter
    ) {
        FilterRegistrationBean<DoveDevAuthenticationFilter> registration = new FilterRegistrationBean<>(filter);
        registration.setEnabled(false);
        return registration;
    }
}
