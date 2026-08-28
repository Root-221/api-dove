package sn.dove.backend.dove.web;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.ObjectNode;
import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.net.URI;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.springframework.core.io.Resource;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaTypeFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import sn.dove.backend.dove.config.DoveProperties;
import sn.dove.backend.dove.storage.ObjectStorageService;

@RestController
@RequestMapping("/api/v1/media")
public class MediaResourceV1 {

    private static final Set<String> ALLOWED_TYPES = Set.of("VIDEO", "IMAGE", "DOCUMENT");
    private static final Set<String> ALLOWED_MIME_TYPES = Set.of(
        "video/mp4",
        "video/quicktime",
        "image/png",
        "image/jpeg",
        "image/webp",
        "application/pdf",
        "application/octet-stream"
    );

    private final DoveApiSupport api;
    private final ObjectStorageService storage;
    private final DoveProperties properties;

    public MediaResourceV1(DoveApiSupport api, ObjectStorageService storage, DoveProperties properties) {
        this.api = api;
        this.storage = storage;
        this.properties = properties;
    }

    @GetMapping
    @PreAuthorize("@doveAuthorization.has('READ_CONTENT')")
    public List<ObjectNode> list() {
        ObjectNode user = api.currentUser();
        return api.store.list("medias").stream().filter(media -> canAccessMedia(user, media)).map(this::withPlaybackUrl).toList();
    }

    @GetMapping("/{id}")
    @PreAuthorize("@doveAuthorization.has('READ_CONTENT')")
    public ObjectNode media(@PathVariable String id) {
        ObjectNode user = api.currentUser();
        ObjectNode media = api.require("medias", id);
        if (!canAccessMedia(user, media)) {
            throw api.notFound("media");
        }
        return withPlaybackUrl(media);
    }

    @PostMapping("/uploads")
    @PreAuthorize("@doveAuthorization.has('CREATE_CONTENT')")
    public ResponseEntity<ObjectNode> prepareUpload(@RequestBody JsonNode input) {
        String type = api.requireText(input, "type");
        String fileName = api.requireText(input, "fileName");
        String mimeType = api.requireText(input, "mimeType");
        long sizeBytes = input.path("sizeBytes").asLong(-1);
        if (!ALLOWED_TYPES.contains(type) || !ALLOWED_MIME_TYPES.contains(mimeType)) {
            throw new ResponseStatusException(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "Unsupported media type");
        }
        if (sizeBytes < 0 || sizeBytes > properties.getStorage().getMaxUploadBytes()) {
            throw new ResponseStatusException(HttpStatus.PAYLOAD_TOO_LARGE, "Media exceeds the configured upload limit");
        }

        String uploadId = UUID.randomUUID().toString();
        String mediaId = UUID.randomUUID().toString();
        ObjectStorageService.UploadTicket ticket = storage.prepareUpload(uploadId, mediaId, fileName, mimeType, sizeBytes);
        ObjectNode upload = tools.jackson.databind.node.JsonNodeFactory.instance.objectNode();
        upload.put("id", uploadId);
        upload.put("uploadId", uploadId);
        upload.put("mediaId", mediaId);
        upload.put("storageKey", ticket.storageKey());
        upload.put("fileName", fileName);
        upload.put("mimeType", mimeType);
        upload.put("sizeBytes", sizeBytes);
        upload.put("status", "UPLOADING");
        upload.put("createdBy", api.currentUserId());
        upload.put("expiresAt", ticket.expiresAt().toString());
        api.store.create("mediaUploads", upload);

        ObjectNode media = tools.jackson.databind.node.JsonNodeFactory.instance.objectNode();
        media.put("id", mediaId);
        media.put("type", type);
        media.put("title", input.path("title").asText(fileName));
        media.put("duration", input.path("duration").asText("00:00"));
        media.put("url", "");
        media.put("thumbnailUrl", "/assets/mock/images/step-placeholder.svg");
        media.put("subtitlesAvailable", false);
        media.put("mimeType", mimeType);
        media.put("sizeBytes", sizeBytes);
        media.put("storageKey", ticket.storageKey());
        media.put("status", "UPLOADING");
        media.put("createdBy", api.currentUserId());
        api.store.create("medias", media);

        ObjectNode response = tools.jackson.databind.node.JsonNodeFactory.instance.objectNode();
        response.put("uploadId", uploadId);
        response.put("mediaId", mediaId);
        response.put("uploadUrl", ticket.uploadUrl().toString());
        response.set("requiredHeaders", tools.jackson.databind.node.JsonNodeFactory.instance.pojoNode(ticket.requiredHeaders()));
        response.put("expiresAt", ticket.expiresAt().toString());
        response.put("status", "UPLOADING");
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/uploads/{uploadId}/content")
    @PreAuthorize("@doveAuthorization.has('CREATE_CONTENT')")
    public ResponseEntity<Void> uploadBody(@PathVariable String uploadId, HttpServletRequest request) throws IOException {
        ObjectNode upload = api.require("mediaUploads", uploadId);
        if (!api.currentUserId().equals(upload.path("createdBy").asText())) {
            throw api.notFound("upload");
        }
        long contentLength = request.getContentLengthLong();
        long expectedLength = upload.path("sizeBytes").asLong(-1);
        if (contentLength < 0) {
            throw new ResponseStatusException(HttpStatus.LENGTH_REQUIRED, "Content-Length is required");
        }
        if (contentLength != expectedLength) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Uploaded size does not match the prepared upload");
        }
        if (contentLength > properties.getStorage().getMaxUploadBytes()) {
            throw new ResponseStatusException(HttpStatus.PAYLOAD_TOO_LARGE);
        }
        storage.writeUpload(
            uploadId,
            upload.path("storageKey").asText(),
            upload.path("mimeType").asText("application/octet-stream"),
            contentLength,
            request.getInputStream()
        );
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/uploads/{uploadId}/complete")
    @PreAuthorize("@doveAuthorization.has('CREATE_CONTENT')")
    public ObjectNode completeUpload(@PathVariable String uploadId) {
        ObjectNode upload = api.require("mediaUploads", uploadId);
        if (!api.currentUserId().equals(upload.path("createdBy").asText())) {
            throw api.notFound("upload");
        }
        if ("READY".equals(upload.path("status").asText())) {
            return withPlaybackUrl(api.require("medias", upload.path("mediaId").asText()));
        }
        storage.completeUpload(uploadId, upload.path("storageKey").asText());
        ObjectNode ready = tools.jackson.databind.node.JsonNodeFactory.instance.objectNode();
        ready.put("status", "READY");
        ready.put("completedAt", Instant.now().toString());
        api.store.patch("mediaUploads", uploadId, ready);
        ObjectNode mediaPatch = tools.jackson.databind.node.JsonNodeFactory.instance.objectNode();
        mediaPatch.put("status", "READY");
        mediaPatch.put("url", storage.playbackUrl(upload.path("storageKey").asText()).toString());
        ObjectNode media = api
            .store
            .patch("medias", upload.path("mediaId").asText(), mediaPatch)
            .orElseThrow(() -> api.notFound("media"));
        api.events.audit(api.currentUserId(), "COMPLETE_MEDIA_UPLOAD", media.path("id").asText(), "SUCCESS");
        return media;
    }

    @PostMapping("/external")
    @PreAuthorize("@doveAuthorization.has('CREATE_CONTENT')")
    public ResponseEntity<ObjectNode> registerExternal(@RequestBody JsonNode input) {
        String sourceUrl = api.requireText(input, "sourceUrl");
        URI uri = URI.create(sourceUrl);
        if (!List.of("https", "http").contains(uri.getScheme())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Only HTTP(S) media URLs are accepted");
        }
        ObjectNode media = api.newDocument(input);
        api.putCreationMetadata(media);
        media.put("url", sourceUrl);
        media.put("thumbnailUrl", input.path("thumbnailUrl").asText("/assets/mock/images/step-placeholder.svg"));
        media.put("subtitlesAvailable", input.path("subtitlesAvailable").asBoolean(false));
        media.put("status", "READY");
        media.put("createdBy", api.currentUserId());
        return ResponseEntity.status(HttpStatus.CREATED).body(api.store.create("medias", media));
    }

    @GetMapping("/{id}/playback-url")
    @PreAuthorize("@doveAuthorization.has('READ_CONTENT')")
    public Map<String, String> playbackUrl(@PathVariable String id) {
        ObjectNode user = api.currentUser();
        ObjectNode media = api.require("medias", id);
        if (!canAccessMedia(user, media)) {
            throw api.notFound("media");
        }
        String storageKey = media.path("storageKey").asText();
        String url = storageKey.isBlank() ? media.path("url").asText() : storage.playbackUrl(storageKey).toString();
        return Map.of("url", url, "expiresAt", Instant.now().plusSeconds(600).toString());
    }

    @GetMapping("/content/{storageKey:.+}")
    public ResponseEntity<Resource> localContent(@PathVariable String storageKey) {
        Resource content = storage.localContent(storageKey);
        return ResponseEntity
            .ok()
            .cacheControl(CacheControl.noCache())
            .contentType(MediaTypeFactory.getMediaType(content).orElse(org.springframework.http.MediaType.APPLICATION_OCTET_STREAM))
            .header(HttpHeaders.CONTENT_DISPOSITION, "inline")
            .body(content);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("@doveAuthorization.has('EDIT_CONTENT')")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        ObjectNode media = api.require("medias", id);
        ObjectNode user = api.currentUser();
        boolean ownsMedia = user.path("id").asText().equals(media.path("createdBy").asText());
        boolean editableReference = api
            .store
            .list("contenus")
            .stream()
            .filter(content -> referencesMedia(content, id))
            .anyMatch(content -> api.users.isInMutationScope(user, content));
        if (!api.users.isGlobal(user) && !ownsMedia && !editableReference) {
            throw api.notFound("media");
        }
        boolean referenced = api
            .store
            .list("contenus")
            .stream()
            .anyMatch(content -> referencesMedia(content, id));
        if (referenced) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "The media is referenced by content");
        }
        String storageKey = media.path("storageKey").asText();
        if (!storageKey.isBlank()) {
            storage.delete(storageKey);
        }
        api.store.delete("medias", id);
        api.events.audit(api.currentUserId(), "DELETE_MEDIA", id, "SUCCESS");
        return ResponseEntity.noContent().build();
    }

    private ObjectNode withPlaybackUrl(ObjectNode source) {
        ObjectNode media = source.deepCopy();
        String storageKey = media.path("storageKey").asText();
        if (!storageKey.isBlank() && "READY".equals(media.path("status").asText())) {
            media.put("url", storage.playbackUrl(storageKey).toString());
        }
        return media;
    }

    private boolean canAccessMedia(ObjectNode user, ObjectNode media) {
        if (api.users.isGlobal(user) || user.path("id").asText().equals(media.path("createdBy").asText())) {
            return true;
        }
        String mediaId = media.path("id").asText();
        String linkedContentId = media.path("contenuId").asText();
        return api
            .store
            .list("contenus")
            .stream()
            .filter(content -> linkedContentId.equals(content.path("id").asText()) || referencesMedia(content, mediaId))
            .anyMatch(content -> canAccessContent(user, content));
    }

    private boolean canAccessContent(ObjectNode user, ObjectNode content) {
        boolean ownsContent = user.path("id").asText().equals(content.path("authorId").asText());
        boolean canEdit = api.users.permissions(user).contains("EDIT_CONTENT") && api.users.isInMutationScope(user, content);
        boolean publicContent = List.of("PUBLIER", "A_REVISER").contains(content.path("status").asText());
        boolean cross = api.users.permissions(user).contains("READ_CROSS_BUSINESS_JOB");
        return ownsContent || canEdit || (publicContent && api.users.canReadContent(user, content, cross));
    }

    private static boolean referencesMedia(ObjectNode content, String mediaId) {
        if (mediaId.equals(content.path("mediaId").asText())) {
            return true;
        }
        for (JsonNode video : content.path("videoItems")) {
            if (mediaId.equals(video.path("mediaId").asText())) {
                return true;
            }
        }
        return false;
    }
}
