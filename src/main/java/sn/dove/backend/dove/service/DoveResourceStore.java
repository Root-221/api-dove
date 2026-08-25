package sn.dove.backend.dove.service;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import javax.sql.DataSource;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sn.dove.backend.dove.domain.DoveResource;
import sn.dove.backend.dove.repository.DoveResourceRepository;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ObjectNode;

@Service
@Transactional
public class DoveResourceStore {

    /** Absolute ceiling applied to any caller-supplied row cap, regardless of the value asked for. */
    private static final int MAX_BOUNDED_ROWS = 5_000;

    private final DoveResourceRepository repository;
    private final ObjectMapper objectMapper;
    private final boolean isH2;

    public DoveResourceStore(DoveResourceRepository repository, ObjectMapper objectMapper, DataSource dataSource) {
        this.repository = repository;
        this.objectMapper = objectMapper;
        this.isH2 = isH2Database(dataSource);
    }

    private boolean isH2Database(DataSource dataSource) {
        try (java.sql.Connection connection = dataSource.getConnection()) {
            return "H2".equalsIgnoreCase(connection.getMetaData().getDatabaseProductName());
        } catch (Exception e) {
            return false;
        }
    }

    @Transactional(readOnly = true)
    public List<ObjectNode> list(String type) {
        return repository.findAllByResourceTypeOrderByCreatedAtAsc(type).stream().map(this::toJson).toList();
    }

    /**
     * Same as {@link #list(String)} but with the row cap pushed to SQL (LIMIT) instead of
     * loading every row and truncating in Java. Intended as a safety net for endpoints whose
     * correctness does not depend on seeing every single row (or as a hard upper bound on an
     * otherwise-unbounded read), not as an application-level pagination mechanism.
     */
    @Transactional(readOnly = true)
    public List<ObjectNode> list(String type, int maxRows) {
        return repository
            .findAllByResourceTypeOrderByCreatedAtAsc(type, PageRequest.of(0, boundedLimit(maxRows)))
            .stream()
            .map(this::toJson)
            .toList();
    }

    /** Most-recent-first, bounded via SQL ORDER BY + LIMIT (no in-Java sort/truncation). */
    @Transactional(readOnly = true)
    public List<ObjectNode> listRecent(String type, int limit) {
        return repository
            .findAllByResourceTypeOrderByCreatedAtDesc(type, PageRequest.of(0, boundedLimit(limit)))
            .stream()
            .map(this::toJson)
            .toList();
    }

    /**
     * Most-recent-first, bounded list of a given type belonging to a given user (JSON payload
     * field "userId"), e.g. one user's notifications. Both the userId filter and the LIMIT are
     * pushed to SQL so this never loads other users' rows just to discard them in Java.
     */
    @Transactional(readOnly = true)
    public List<ObjectNode> listRecentForUser(String type, String userId, int limit) {
        if (isH2) {
            return list(type).stream()
                .filter(node -> userId.equals(node.path("userId").asText()))
                .sorted((a, b) -> {
                    String ca = a.path("createdAt").asText("");
                    String cb = b.path("createdAt").asText("");
                    return cb.compareTo(ca); // Descending order
                })
                .limit(boundedLimit(limit))
                .toList();
        }
        return repository.findRecentByResourceTypeAndUserId(type, userId, boundedLimit(limit)).stream().map(this::toJson).toList();
    }

    @Transactional(readOnly = true)
    public Optional<ObjectNode> find(String type, String id) {
        return repository.findByResourceTypeAndExternalId(type, id).map(this::toJson);
    }

    /**
     * Looks up a resource by the auth-provider identifier stored in its payload under
     * "externalSubject" (e.g. the Keycloak JWT subject), via an indexed DB lookup rather than
     * listing every resource of that type and filtering in Java.
     */
    @Transactional(readOnly = true)
    public Optional<ObjectNode> findByExternalSubject(String type, String subject) {
        if (isH2) {
            return list(type).stream()
                .filter(node -> subject.equals(node.path("externalSubject").asText()))
                .findFirst();
        }
        return repository.findByResourceTypeAndExternalSubject(type, subject).map(this::toJson);
    }

    @Transactional(readOnly = true)
    public boolean hasAny(String type) {
        return repository.existsByResourceType(type);
    }

    private static int boundedLimit(int requested) {
        return Math.max(1, Math.min(requested, MAX_BOUNDED_ROWS));
    }

    public ObjectNode create(String type, JsonNode input) {
        ObjectNode value = copyObject(input);
        String externalId = text(value, "id").orElseGet(() -> UUID.randomUUID().toString());
        value.put("id", externalId);
        if (repository.findByResourceTypeAndExternalId(type, externalId).isPresent()) {
            throw new DuplicateResourceException("Resource already exists: " + type + "/" + externalId);
        }

        Instant now = Instant.now();
        DoveResource entity = new DoveResource();
        entity.setId(UUID.randomUUID());
        entity.setResourceType(type);
        entity.setExternalId(externalId);
        entity.setPayload(write(value));
        entity.setCreatedAt(now);
        entity.setUpdatedAt(now);
        repository.save(entity);
        return value;
    }

    public Optional<ObjectNode> replace(String type, String id, JsonNode input) {
        return repository.findByResourceTypeAndExternalId(type, id).map(entity -> {
            ObjectNode value = copyObject(input);
            value.put("id", id);
            entity.setPayload(write(value));
            entity.setUpdatedAt(Instant.now());
            repository.save(entity);
            return value;
        });
    }

    public Optional<ObjectNode> patch(String type, String id, JsonNode patch) {
        return repository.findByResourceTypeAndExternalId(type, id).map(entity -> {
            ObjectNode value = toJson(entity);
            patch.properties().forEach(field -> {
                if (field.getValue().isNull()) {
                    value.remove(field.getKey());
                } else {
                    value.set(field.getKey(), field.getValue());
                }
            });
            value.put("id", id);
            entity.setPayload(write(value));
            entity.setUpdatedAt(Instant.now());
            repository.save(entity);
            return value;
        });
    }

    public boolean delete(String type, String id) {
        return repository
            .findByResourceTypeAndExternalId(type, id)
            .map(entity -> {
                repository.delete(entity);
                return true;
            })
            .orElse(false);
    }

    public void deleteAll() {
        repository.deleteAll();
    }

    public ObjectNode upsert(String type, JsonNode input) {
        ObjectNode value = copyObject(input);
        String id = text(value, "id").orElseGet(() -> UUID.randomUUID().toString());
        value.put("id", id);
        return replace(type, id, value).orElseGet(() -> create(type, value));
    }

    public static Optional<String> text(JsonNode node, String field) {
        JsonNode value = node.get(field);
        return value == null || value.isNull() || value.asText().isBlank() ? Optional.empty() : Optional.of(value.asText());
    }

    private ObjectNode copyObject(JsonNode value) {
        if (!value.isObject()) {
            throw new IllegalArgumentException("A JSON object is required");
        }
        return ((ObjectNode) value).deepCopy();
    }

    private ObjectNode toJson(DoveResource entity) {
        try {
            return (ObjectNode) objectMapper.readTree(entity.getPayload());
        } catch (JacksonException exception) {
            throw new IllegalStateException("Invalid persisted JSON for " + entity.getExternalId(), exception);
        }
    }

    private String write(JsonNode value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JacksonException exception) {
            throw new IllegalArgumentException("Invalid JSON payload", exception);
        }
    }
}
