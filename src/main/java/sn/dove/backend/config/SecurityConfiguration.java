package sn.dove.backend.config;

import static org.springframework.security.config.Customizer.withDefaults;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.Supplier;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.*;
import org.springframework.security.oauth2.server.resource.web.authentication.BearerTokenAuthenticationFilter;
import org.springframework.util.StringUtils;
import sn.dove.backend.dove.config.DoveProperties;
import sn.dove.backend.dove.security.DoveDevAuthenticationFilter;
import sn.dove.backend.security.*;
import sn.dove.backend.security.oauth2.AudienceValidator;
import tech.jhipster.config.JHipsterProperties;

@Configuration
@EnableMethodSecurity(securedEnabled = true)
public class SecurityConfiguration {

    private final JHipsterProperties jHipsterProperties;
    private final DoveDevAuthenticationFilter doveDevAuthenticationFilter;
    private final DoveProperties doveProperties;

    @Value("${spring.security.oauth2.resourceserver.jwt.issuer-uri:http://localhost:9080/realms/jhipster}")
    private String issuerUri;

    public SecurityConfiguration(
        JHipsterProperties jHipsterProperties,
        DoveDevAuthenticationFilter doveDevAuthenticationFilter,
        DoveProperties doveProperties
    ) {
        this.jHipsterProperties = jHipsterProperties;
        this.doveDevAuthenticationFilter = doveDevAuthenticationFilter;
        this.doveProperties = doveProperties;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) {
        boolean isDev = env.acceptsProfiles(Profiles.of("dev"));
        http.cors(withDefaults())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .csrf(csrf ->
                csrf
                    .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                    .csrfTokenRequestHandler(new SpaCsrfTokenRequestHandler())
                    .ignoringRequestMatchers("/api/v1/**")
            )
            .authorizeHttpRequests(authz -> {
                if (doveProperties.getAuth().isDevHeaderEnabled()) {
                    authz.requestMatchers("/v3/api-docs/**", "/swagger-ui.html", "/swagger-ui/**").permitAll();
                } else {
                    authz
                        .requestMatchers("/v3/api-docs/**", "/swagger-ui.html", "/swagger-ui/**")
                        .hasAuthority(AuthoritiesConstants.ADMIN);
                }

                // prettier-ignore
                authz
                    .requestMatchers("/api/v1/media/content/**").permitAll()
                    .requestMatchers("/api/v1/**").authenticated()
                    // The generated legacy JHipster resources are deliberately not part of
                    // the public contract. Keeping them closed prevents bypassing Dove's
                    // permission and business-scope checks through /api/* endpoints.
                    .requestMatchers("/api/**").denyAll()
                    .requestMatchers("/management/health").permitAll()
                    .requestMatchers("/management/health/**").permitAll()
                    .requestMatchers("/management/**").hasAuthority(AuthoritiesConstants.ADMIN)
                    .requestMatchers("/error").permitAll()
                    .anyRequest().denyAll();
            })
            .oauth2ResourceServer(oauth2 -> oauth2.jwt(jwt -> jwt.jwtAuthenticationConverter(authenticationConverter())))
            .addFilterBefore(doveDevAuthenticationFilter, BearerTokenAuthenticationFilter.class);
        return http.build();
    }

    Converter<Jwt, AbstractAuthenticationToken> authenticationConverter() {
        JwtAuthenticationConverter jwtAuthenticationConverter = new JwtAuthenticationConverter();
        jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(
            new Converter<Jwt, Collection<GrantedAuthority>>() {
                @Override
                public Collection<GrantedAuthority> convert(Jwt jwt) {
                    return SecurityUtils.extractAuthorityFromClaims(jwt.getClaims());
                }
            }
        );
        return jwtAuthenticationConverter;
    }

    @Bean
    JwtDecoder jwtDecoder() {
        if (doveProperties.getAuth().isDevHeaderEnabled()) {
            return NimbusJwtDecoder
                .withSecretKey(new SecretKeySpec("dove-local-development-signing-key".getBytes(StandardCharsets.UTF_8), "HmacSHA256"))
                .build();
        }
        NimbusJwtDecoder jwtDecoder = JwtDecoders.fromOidcIssuerLocation(issuerUri);

        OAuth2TokenValidator<Jwt> audienceValidator = new AudienceValidator(jHipsterProperties.getSecurity().getOauth2().getAudience());
        OAuth2TokenValidator<Jwt> withIssuer = JwtValidators.createDefaultWithIssuer(issuerUri);
        OAuth2TokenValidator<Jwt> withAudience = new DelegatingOAuth2TokenValidator<>(withIssuer, audienceValidator);

        jwtDecoder.setJwtValidator(withAudience);
        return jwtDecoder;
    }

    /**
     * Custom CSRF handler to provide BREACH protection for Single-Page Applications (SPA).
     *
     * @see <a href="https://docs.spring.io/spring-security/reference/servlet/exploits/csrf.html#csrf-integration-javascript-spa">Spring Security Documentation - Integrating with CSRF Protection</a>
     * @see <a href="https://github.com/jhipster/generator-jhipster/pull/25907">JHipster - use customized SpaCsrfTokenRequestHandler to handle CSRF token</a>
     * @see <a href="https://stackoverflow.com/q/74447118/65681">CSRF protection not working with Spring Security 6</a>
     */
    static final class SpaCsrfTokenRequestHandler implements CsrfTokenRequestHandler {

        private final CsrfTokenRequestHandler plain = new CsrfTokenRequestAttributeHandler();
        private final CsrfTokenRequestHandler xor = new XorCsrfTokenRequestAttributeHandler();

        @Override
        public void handle(HttpServletRequest request, HttpServletResponse response, Supplier<CsrfToken> csrfToken) {
            /*
             * Always use XorCsrfTokenRequestAttributeHandler to provide BREACH protection of
             * the CsrfToken when it is rendered in the response body.
             */
            this.xor.handle(request, response, csrfToken);

            // Render the token value to a cookie by causing the deferred token to be loaded.
            csrfToken.get();
        }

        @Override
        public String resolveCsrfTokenValue(HttpServletRequest request, CsrfToken csrfToken) {
            /*
             * If the request contains a request header, use CsrfTokenRequestAttributeHandler
             * to resolve the CsrfToken. This applies when a single-page application includes
             * the header value automatically, which was obtained via a cookie containing the
             * raw CsrfToken.
             */
            if (StringUtils.hasText(request.getHeader(csrfToken.getHeaderName()))) {
                return this.plain.resolveCsrfTokenValue(request, csrfToken);
            }
            /*
             * In all other cases (e.g. if the request contains a request parameter), use
             * XorCsrfTokenRequestAttributeHandler to resolve the CsrfToken. This applies
             * when a server-side rendered form includes the _csrf request parameter as a
             * hidden input.
             */
            return this.xor.resolveCsrfTokenValue(request, csrfToken);
        }
    }
}
