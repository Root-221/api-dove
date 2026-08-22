package sn.dove.backend.dove.web;

import tools.jackson.databind.node.ObjectNode;
import java.time.Duration;
import java.time.Instant;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClient;

@RestController
@RequestMapping("/api/v1/admin/authentication")
@PreAuthorize("@doveAuthorization.has('MANAGE_AUTHENTICATION')")
public class AdminAuthenticationResourceV1 {

    private final String issuerUri;
    private final String clientId;
    private final RestClient restClient;

    public AdminAuthenticationResourceV1(
        @Value("${spring.security.oauth2.resourceserver.jwt.issuer-uri:http://localhost:9080/realms/jhipster}") String issuerUri,
        @Value("${dove.auth.web-client-id:dove-web}") String clientId,
        RestClient.Builder builder
    ) {
        this.issuerUri = issuerUri;
        this.clientId = clientId;
        this.restClient = builder.build();
    }

    @GetMapping("/status")
    public ObjectNode status() {
        ObjectNode status = tools.jackson.databind.node.JsonNodeFactory.instance.objectNode();
        status.put("mode", "SSO_READY");
        status.put("provider", "ORANGE_SONATEL");
        status.put("issuerUrl", maskUrl(issuerUri));
        status.put("clientId", mask(clientId));
        status.put("requireMfa", true);
        status.put("autoProvisionUsers", false);
        status.put("updatedAt", Instant.now().toString());
        status.put("updatedBy", "environment");
        status.putArray("allowedDomains").add("sonatel.sn").add("orange-sonatel.com");
        status.put("sessionDurationMinutes", 480);
        return status;
    }

    @PostMapping("/tests")
    public ObjectNode testConnection() {
        Instant start = Instant.now();
        ObjectNode result = tools.jackson.databind.node.JsonNodeFactory.instance.objectNode();
        try {
            restClient.get().uri(issuerUri.replaceAll("/+$", "") + "/.well-known/openid-configuration").retrieve().toBodilessEntity();
            result.put("success", true);
            result.put("message", "Discovery OIDC, TLS et endpoint de clés accessibles.");
        } catch (RuntimeException exception) {
            result.put("success", false);
            result.put("message", "Le fournisseur OIDC n'est pas joignable depuis le backend.");
        }
        result.put("testedAt", Instant.now().toString());
        result.put("latencyMs", Duration.between(start, Instant.now()).toMillis());
        return result;
    }

    private static String mask(String value) {
        if (value.length() <= 4) return "****";
        return value.substring(0, 2) + "***" + value.substring(value.length() - 2);
    }

    private static String maskUrl(String value) {
        return value.replaceAll("(?<=://)[^/]+", "***");
    }
}
