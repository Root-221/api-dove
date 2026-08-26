package sn.dove.backend.dove.storage;

import jakarta.annotation.PreDestroy;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import sn.dove.backend.dove.config.DoveProperties;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.http.urlconnection.UrlConnectionHttpClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Configuration;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

/** S3-compatible storage adapter for OpenShift Data Foundation / NooBaa. */
@Service
@ConditionalOnProperty(name = "dove.storage.provider", havingValue = "s3")
public class S3ObjectStorageService implements ObjectStorageService {

    private final S3Client client;
    private final String bucket;
    private final String publicBaseUrl;

    public S3ObjectStorageService(DoveProperties properties) {
        DoveProperties.Storage storage = properties.getStorage();
        DoveProperties.S3 s3 = storage.getS3();
        String endpoint = required(s3.getEndpoint(), "DOVE_STORAGE_S3_ENDPOINT");
        this.bucket = required(s3.getBucket(), "DOVE_STORAGE_S3_BUCKET");
        String accessKey = required(s3.getAccessKey(), "DOVE_STORAGE_S3_ACCESS_KEY");
        String secretKey = required(s3.getSecretKey(), "DOVE_STORAGE_S3_SECRET_KEY");
        this.publicBaseUrl = required(storage.getPublicBaseUrl(), "DOVE_STORAGE_PUBLIC_BASE_URL").replaceAll("/+$", "");

        URI endpointUri = URI.create(endpoint);
        if (!"https".equalsIgnoreCase(endpointUri.getScheme())) {
            throw new IllegalStateException("DOVE_STORAGE_S3_ENDPOINT must use HTTPS");
        }

        this.client = S3Client
            .builder()
            .endpointOverride(endpointUri)
            .region(Region.of(required(s3.getRegion(), "DOVE_STORAGE_S3_REGION")))
            .credentialsProvider(StaticCredentialsProvider.create(AwsBasicCredentials.create(accessKey, secretKey)))
            .serviceConfiguration(S3Configuration.builder().pathStyleAccessEnabled(s3.isPathStyleAccessEnabled()).build())
            .httpClient(UrlConnectionHttpClient.builder().build())
            .build();
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
    public void writeUpload(
        String uploadId,
        String storageKey,
        String contentType,
        long contentLength,
        InputStream input
    ) throws IOException {
        validateStorageKey(storageKey);
        client.putObject(
            request -> request.bucket(bucket).key(storageKey).contentType(contentType).contentLength(contentLength),
            RequestBody.fromInputStream(input, contentLength)
        );
    }

    @Override
    public void completeUpload(String uploadId, String storageKey) {
        validateStorageKey(storageKey);
        try {
            client.headObject(HeadObjectRequest.builder().bucket(bucket).key(storageKey).build());
        } catch (S3Exception exception) {
            if (exception.statusCode() == 404) {
                throw new IllegalStateException("The S3 upload body has not been received", exception);
            }
            throw exception;
        }
    }

    @Override
    public URI playbackUrl(String storageKey) {
        validateStorageKey(storageKey);
        return URI.create(publicBaseUrl + "/api/v1/media/content/" + storageKey);
    }

    @Override
    public Resource localContent(String storageKey) {
        validateStorageKey(storageKey);
        ResponseInputStream<GetObjectResponse> input = client.getObject(request -> request.bucket(bucket).key(storageKey));
        long contentLength = input.response().contentLength();
        return new InputStreamResource(input) {
            @Override
            public long contentLength() {
                return contentLength;
            }

            @Override
            public String getFilename() {
                return storageKey.substring(storageKey.lastIndexOf('/') + 1);
            }
        };
    }

    @Override
    public void delete(String storageKey) {
        validateStorageKey(storageKey);
        client.deleteObject(request -> request.bucket(bucket).key(storageKey));
    }

    @PreDestroy
    void close() {
        client.close();
    }

    private static void validateStorageKey(String storageKey) {
        if (storageKey == null || !storageKey.matches("[a-fA-F0-9-]{36}\\.[a-z0-9]{1,8}")) {
            throw new IllegalArgumentException("Invalid S3 storage key");
        }
    }

    private static String extension(String fileName) {
        int dot = fileName.lastIndexOf('.');
        if (dot < 0 || dot == fileName.length() - 1) {
            return ".bin";
        }
        String extension = fileName.substring(dot).toLowerCase();
        return extension.matches("\\.[a-z0-9]{1,8}") ? extension : ".bin";
    }

    private static String required(String value, String environmentVariable) {
        if (value == null || value.isBlank()) {
            throw new IllegalStateException(environmentVariable + " is required when DOVE_STORAGE_PROVIDER=s3");
        }
        return value;
    }
}
