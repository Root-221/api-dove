package sn.dove.backend.dove.storage;

import java.net.URI;
import java.time.Instant;
import java.util.Map;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import sn.dove.backend.dove.config.DoveProperties;

/** Adapter for Sonatel's future object-storage gateway (S3, Swift or another cloud provider). */
@Service
@ConditionalOnProperty(name = "dove.storage.provider", havingValue = "gateway")
public class GatewayObjectStorageService implements ObjectStorageService {

    private final RestClient client;

    public GatewayObjectStorageService(RestClient.Builder builder, DoveProperties properties) {
        String baseUrl = properties.getStorage().getGatewayBaseUrl();
        if (baseUrl == null || baseUrl.isBlank()) {
            throw new IllegalStateException("DOVE_STORAGE_GATEWAY_URL is required when the storage provider is gateway");
        }
        RestClient.Builder configured = builder.baseUrl(baseUrl);
        String apiKey = properties.getStorage().getGatewayApiKey();
        if (apiKey != null && !apiKey.isBlank()) {
            configured.defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey);
        }
        this.client = configured.build();
    }

    @Override
    public UploadTicket prepareUpload(String uploadId, String mediaId, String fileName, String mimeType, long sizeBytes) {
        GatewayUpload response = client
            .post()
            .uri("/uploads")
            .body(Map.of("uploadId", uploadId, "mediaId", mediaId, "fileName", fileName, "mimeType", mimeType, "sizeBytes", sizeBytes))
            .retrieve()
            .body(GatewayUpload.class);
        if (response == null) {
            throw new IllegalStateException("The storage gateway returned an empty upload ticket");
        }
        return new UploadTicket(uploadId, mediaId, response.uploadUrl(), response.requiredHeaders(), response.expiresAt(), response.storageKey());
    }

    @Override
    public void completeUpload(String uploadId, String storageKey) {
        client.post().uri("/uploads/{uploadId}/complete", uploadId).body(Map.of("storageKey", storageKey)).retrieve().toBodilessEntity();
    }

    @Override
    public URI playbackUrl(String storageKey) {
        GatewayPlayback response = client.get().uri(uri -> uri.path("/objects/playback-url").queryParam("key", storageKey).build()).retrieve().body(GatewayPlayback.class);
        if (response == null) {
            throw new IllegalStateException("The storage gateway returned no playback URL");
        }
        return response.url();
    }

    @Override
    public Resource localContent(String storageKey) {
        return ObjectStorageService.super.localContent(storageKey);
    }

    @Override
    public void delete(String storageKey) {
        client.delete().uri(uri -> uri.path("/objects").queryParam("key", storageKey).build()).retrieve().toBodilessEntity();
    }

    private record GatewayUpload(URI uploadUrl, Map<String, String> requiredHeaders, Instant expiresAt, String storageKey) {}

    private record GatewayPlayback(URI url) {}
}
