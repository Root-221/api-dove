package sn.dove.backend.dove.config;

import java.net.URI;
import java.util.Arrays;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

/** Fails fast when a cloud profile is connected to an unsafe or mismatched environment. */
@Component
@Profile({ "preprod", "prod" })
public class DoveCloudConfigurationValidator implements ApplicationRunner {

    private final Environment environment;
    private final DoveProperties properties;

    public DoveCloudConfigurationValidator(Environment environment, DoveProperties properties) {
        this.environment = environment;
        this.properties = properties;
    }

    @Override
    public void run(ApplicationArguments arguments) {
        if (properties.getSeed().isEnabled()) {
            throw invalid("dove.seed.enabled must remain false");
        }
        if (properties.getAuth().isDevHeaderEnabled()) {
            throw invalid("dove.auth.dev-header-enabled must remain false");
        }
        if (!"s3".equals(properties.getStorage().getProvider())) {
            throw invalid("DOVE_STORAGE_PROVIDER must be s3");
        }

        requireHttps(properties.getStorage().getPublicBaseUrl(), "DOVE_STORAGE_PUBLIC_BASE_URL");
        requireHttps(properties.getStorage().getS3().getEndpoint(), "DOVE_STORAGE_S3_ENDPOINT");

        String issuer = required("spring.security.oauth2.resourceserver.jwt.issuer-uri", "DOVE_KEYCLOAK_ISSUER_URI");
        requireHttps(issuer, "DOVE_KEYCLOAK_ISSUER_URI");
        if (!issuer.endsWith("/realms/DOVE")) {
            throw invalid("DOVE_KEYCLOAK_ISSUER_URI must target the DOVE realm");
        }

        String databaseUrl = required("spring.datasource.url", "DOVE_DATABASE_URL");
        if (!databaseUrl.startsWith("jdbc:mysql://")) {
            throw invalid("DOVE_DATABASE_URL must be a MySQL JDBC URL");
        }
        if (active("prod") && !databaseUrl.contains("/MYSDOVEPRD")) {
            throw invalid("the prod profile must target MYSDOVEPRD");
        }
        if (active("preprod") && !databaseUrl.contains("/MYSDOVEDEV")) {
            throw invalid("the preprod profile must target MYSDOVEDEV");
        }
    }

    private String required(String propertyName, String environmentVariable) {
        String value = environment.getProperty(propertyName);
        if (value == null || value.isBlank() || value.startsWith("__")) {
            throw invalid(environmentVariable + " is required");
        }
        return value;
    }

    private void requireHttps(String value, String environmentVariable) {
        if (value == null || value.isBlank() || !"https".equalsIgnoreCase(URI.create(value).getScheme())) {
            throw invalid(environmentVariable + " must be an HTTPS URL");
        }
    }

    private boolean active(String profile) {
        return Arrays.asList(environment.getActiveProfiles()).contains(profile);
    }

    private static IllegalStateException invalid(String message) {
        return new IllegalStateException("Invalid DOVE cloud configuration: " + message);
    }
}
