package com.elevate.sparkle.adapter.out.persistence.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

/**
 * JPA Entity for event_store table.
 * This is a pure persistence entity - not a domain object.
 */
@Entity
@Table(
    name = "event_store",
    uniqueConstraints = @UniqueConstraint(
        name = "uk_aggregate_sequence",
        columnNames = {"aggregate_id", "sequence_number"}
    ),
    indexes = {
        @Index(name = "idx_event_store_aggregate_id", columnList = "aggregate_id, sequence_number"),
        @Index(name = "idx_event_store_event_type", columnList = "event_type, created_at"),
        @Index(name = "idx_event_store_created_at", columnList = "created_at")
    }
)
public class EventStoreEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @Column(name = "aggregate_id", nullable = false)
    private UUID aggregateId;
    
    @Column(name = "aggregate_type", nullable = false, length = 100)
    private String aggregateType;
    
    @Column(name = "event_type", nullable = false, length = 100)
    private String eventType;
    
    @Column(name = "event_version", nullable = false)
    private Integer eventVersion = 1;
    
    @Column(name = "sequence_number", nullable = false)
    private Long sequenceNumber;
    
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "payload", nullable = false, columnDefinition = "jsonb")
    private Map<String, Object> payload;
    
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "metadata", nullable = false, columnDefinition = "jsonb")
    private Map<String, Object> metadata;
    
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;
    
    public EventStoreEntity() {
    }
    
    public UUID getId() {
        return id;
    }
    
    public void setId(UUID id) {
        this.id = id;
    }
    
    public UUID getAggregateId() {
        return aggregateId;
    }
    
    public void setAggregateId(UUID aggregateId) {
        this.aggregateId = aggregateId;
    }
    
    public String getAggregateType() {
        return aggregateType;
    }
    
    public void setAggregateType(String aggregateType) {
        this.aggregateType = aggregateType;
    }
    
    public String getEventType() {
        return eventType;
    }
    
    public void setEventType(String eventType) {
        this.eventType = eventType;
    }
    
    public Integer getEventVersion() {
        return eventVersion;
    }
    
    public void setEventVersion(Integer eventVersion) {
        this.eventVersion = eventVersion;
    }
    
    public Long getSequenceNumber() {
        return sequenceNumber;
    }
    
    public void setSequenceNumber(Long sequenceNumber) {
        this.sequenceNumber = sequenceNumber;
    }
    
    public Map<String, Object> getPayload() {
        return payload;
    }
    
    public void setPayload(Map<String, Object> payload) {
        this.payload = payload;
    }
    
    public Map<String, Object> getMetadata() {
        return metadata;
    }
    
    public void setMetadata(Map<String, Object> metadata) {
        this.metadata = metadata;
    }
    
    public Instant getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
    
    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
        if (eventVersion == null) {
            eventVersion = 1;
        }
    }
}
