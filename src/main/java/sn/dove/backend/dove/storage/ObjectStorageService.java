package sn.dove.backend.dove.storage;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.time.Instant;
import java.util.Map;
import org.springframework.core.io.Resource;

public interface ObjectStorageService {
    UploadTicket prepareUpload(String uploadId, String mediaId, String fileName, String mimeType, long sizeBytes);

    default void writeUpload(
        String uploadId,
        String storageKey,
        String contentType,
        long contentLength,
        InputStream input
    ) throws IOException {
        throw new UnsupportedOperationException("Uploads are sent directly to the configured cloud provider");
    }

    void completeUpload(String uploadId, String storageKey);

    URI playbackUrl(String storageKey);

    default Resource localContent(String storageKey) {
        throw new UnsupportedOperationException("Content is hosted by the configured cloud provider");
    }

    void delete(String storageKey);

    record UploadTicket(String uploadId, String mediaId, URI uploadUrl, Map<String, String> requiredHeaders, Instant expiresAt, String storageKey) {}
}
