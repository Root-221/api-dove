package sn.dove.backend.dove.storage;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.net.URI;
import org.junit.jupiter.api.Test;
import sn.dove.backend.dove.config.DoveProperties;

class S3ObjectStorageServiceTest {

    @Test
    void preparesBackendProxiedUploadForInternalS3Endpoint() {
        DoveProperties properties = properties("https://s3.openshift-storage.svc:443");
        S3ObjectStorageService service = new S3ObjectStorageService(properties);
        try {
            ObjectStorageService.UploadTicket ticket = service.prepareUpload(
                "upload-id",
                "7ecf1716-aa0b-4d80-a8b6-e2a25914aacf",
                "guide.PDF",
                "application/pdf",
                42
            );

            assertThat(ticket.uploadUrl()).isEqualTo(URI.create("https://api.dove.example/api/v1/media/uploads/upload-id/content"));
            assertThat(ticket.storageKey()).isEqualTo("7ecf1716-aa0b-4d80-a8b6-e2a25914aacf.pdf");
            assertThat(ticket.requiredHeaders()).containsEntry("Content-Type", "application/pdf");
        } finally {
            service.close();
        }
    }

    @Test
    void rejectsUnencryptedS3Endpoint() {
        assertThatThrownBy(() -> new S3ObjectStorageService(properties("http://s3.internal")))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("HTTPS");
    }

    private static DoveProperties properties(String endpoint) {
        DoveProperties properties = new DoveProperties();
        properties.getStorage().setPublicBaseUrl("https://api.dove.example");
        properties.getStorage().getS3().setEndpoint(endpoint);
        properties.getStorage().getS3().setBucket("dove-test");
        properties.getStorage().getS3().setAccessKey("test-access-key");
        properties.getStorage().getS3().setSecretKey("test-secret-key");
        return properties;
    }
}
