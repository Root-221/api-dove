package sn.dove.backend.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.security.OAuthFlow;
import io.swagger.v3.oas.models.security.OAuthFlows;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.security.Scopes;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Lets Swagger UI's "Authorize" button log in through Keycloak (authorization code + PKCE)
 * so "Try it out" calls carry a valid bearer token, instead of failing with 401.
 */
@Configuration
public class OpenApiConfiguration {

    private static final String SCHEME_NAME = "oauth2";

    @Value("${spring.security.oauth2.client.provider.oidc.issuer-uri}")
    private String issuerUri;

    @Bean
    public OpenAPI openApiOAuthConfiguration() {
        return new OpenAPI()
            .components(
                new Components()
                    .addSecuritySchemes(
                        SCHEME_NAME,
                        new SecurityScheme()
                            .type(SecurityScheme.Type.OAUTH2)
                            .flows(
                                new OAuthFlows()
                                    .authorizationCode(
                                        new OAuthFlow()
                                            .authorizationUrl(issuerUri + "/protocol/openid-connect/auth")
                                            .tokenUrl(issuerUri + "/protocol/openid-connect/token")
                                            .scopes(new Scopes().addString("openid", "openid").addString("profile", "profile"))
                                    )
                            )
                    )
            )
            .addSecurityItem(new SecurityRequirement().addList(SCHEME_NAME));
    }
}
