package sn.dove.backend.dove.storage;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import sn.dove.backend.dove.config.DoveProperties;

@Service
@ConditionalOnProperty(name = "dove.storage.provider", havingValue = "local", matchIfMissing = true)
public class LocalObjectStorageService implements ObjectStorageService {

    private static final Logger LOG = LoggerFactory.getLogger(LocalObjectStorageService.class);

    private final Path root;
    private final String publicBaseUrl;

    public LocalObjectStorageService(DoveProperties properties) throws IOException {
        this.root = properties.getStorage().getLocalDirectory().toAbsolutePath().normalize();
        this.publicBaseUrl = properties.getStorage().getPublicBaseUrl().replaceAll("/+$", "");
        Files.createDirectories(root);
        LOG.warn(
            "Using local file storage at {} - not suitable for multi-instance deployment, switch to gateway storage before scaling horizontally.",
            root
        );
    }

    @Override
    public UploadTicket prepareUpload(String uploadId, String mediaId, String fileName, String mimeType, long sizeBytes) {
        String storageKey = mediaId + extension(fileName);
        URI uploadUrl = URI.create(publicBaseUrl + "/api/v1/media/uploads/" + uploadId + "/content");
        return new UploadTicket(
            uploadId,
            mediaId,
            uploadUrl,
            Map.of("Content-Type", mimeType),
            Instant.now().plus(15, ChronoUnit.MINUTES),
            storageKey
        );
    }

    @Override
    public void writeUpload(String uploadId, String storageKey, String contentType, long contentLength, InputStream input) throws IOException {
        Path target = resolve(uploadId + ".upload");
        Files.copy(input, target, StandardCopyOption.REPLACE_EXISTING);
    }

    @Override
    public void completeUpload(String uploadId, String storageKey) {
        Path temporary = resolve(uploadId + ".upload");
        Path target = resolve(storageKey);
        try {
            if (!Files.exists(temporary)) {
                throw new IllegalStateException("The upload body has not been received");
            }
            Files.move(temporary, target, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to finalise local upload", exception);
        }
    }

    @Override
    public URI playbackUrl(String storageKey) {
        return URI.create(publicBaseUrl + "/api/v1/media/content/" + storageKey);
    }

    @Override
    public Resource localContent(String storageKey) {
        Path path = resolve(storageKey);
        if (!Files.isRegularFile(path)) {
            throw new IllegalArgumentException("Media content not found");
        }
        return new FileSystemResource(path);
    }

    @Override
    public void delete(String storageKey) {
        try {
            Files.deleteIfExists(resolve(storageKey));
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to delete local media", exception);
        }
    }

    private Path resolve(String key) {
        Path resolved = root.resolve(key).normalize();
        if (!resolved.startsWith(root)) {
            throw new IllegalArgumentException("Invalid storage key");
        }
        return resolved;
    }

    private static String extension(String fileName) {
        int dot = fileName.lastIndexOf('.');
        if (dot < 0 || dot == fileName.length() - 1) {
            return ".bin";
        }
        String extension = fileName.substring(dot).toLowerCase();
        return extension.matches("\\.[a-z0-9]{1,8}") ? extension : ".bin";
    }
}
