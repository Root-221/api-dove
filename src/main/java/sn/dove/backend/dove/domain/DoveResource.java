package sn.dove.backend.dove.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.persistence.Version;
import java.time.Instant;
import java.util.UUID;

/**
 * Persisted API document used by the DOVE v1 facade.
 *
 * <p>The generated JHipster entities remain available for compatibility with the original class
 * diagram. This aggregate stores the richer, multi-format API representation atomically while the
 * domain is stabilising. Every mutation is still transactional, versioned and persisted in
 * PostgreSQL; JSON Server is never used at runtime.
 */
@Entity
@Table(
    name = "dove_resource",
    uniqueConstraints = @UniqueConstraint(name = "ux_dove_resource_type_external", columnNames = { "resource_type", "external_id" })
)
public class DoveResource {

    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "resource_type", nullable = false, length = 80)
    private String resourceType;

    @Column(name = "external_id", nullable = false, length = 80)
    private String externalId;

    @Column(name = "payload", nullable = false, columnDefinition = "text")
    private String payload;

    @Version
    @Column(name = "record_version", nullable = false)
    private long recordVersion;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getResourceType() {
        return resourceType;
    }

    public void setResourceType(String resourceType) {
        this.resourceType = resourceType;
    }

    public String getExternalId() {
        return externalId;
    }

    public void setExternalId(String externalId) {
        this.externalId = externalId;
    }

    public String getPayload() {
        return payload;
    }

    public void setPayload(String payload) {
        this.payload = payload;
    }

    public long getRecordVersion() {
        return recordVersion;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}
