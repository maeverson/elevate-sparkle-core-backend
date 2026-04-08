package com.elevate.sparkle.adapter.out.persistence.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

/**
 * JPA entity for Journey Version
 */
@Entity
@Table(name = "journey_versions")
public class JourneyVersionEntity {
    
    @Id
    private UUID id;
    
    @Column(name = "journey_definition_id", nullable = false)
    private UUID journeyDefinitionId;
    
    @Column(name = "version_number", nullable = false, length = 20)
    private String versionNumber;
    
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(nullable = false, columnDefinition = "jsonb")
    private Map<String, Object> dsl;
    
    @Column(nullable = false, length = 20)
    private String status = "DRAFT";
    
    @Column(name = "created_by", nullable = false)
    private UUID createdBy;
    
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;
    
    @Column(name = "published_at")
    private Instant publishedAt;
    
    @Column(name = "change_notes", columnDefinition = "TEXT")
    private String changeNotes;
    
    // Constructors
    public JourneyVersionEntity() {
    }
    
    public JourneyVersionEntity(
            UUID id,
            UUID journeyDefinitionId,
            String versionNumber,
            Map<String, Object> dsl,
            UUID createdBy,
            Instant createdAt,
            String changeNotes
    ) {
        this.id = id;
        this.journeyDefinitionId = journeyDefinitionId;
        this.versionNumber = versionNumber;
        this.dsl = dsl;
        this.createdBy = createdBy;
        this.createdAt = createdAt;
        this.changeNotes = changeNotes;
        this.status = "DRAFT";
    }
    
    // Getters and Setters
    public UUID getId() {
        return id;
    }
    
    public void setId(UUID id) {
        this.id = id;
    }
    
    public UUID getJourneyDefinitionId() {
        return journeyDefinitionId;
    }
    
    public void setJourneyDefinitionId(UUID journeyDefinitionId) {
        this.journeyDefinitionId = journeyDefinitionId;
    }
    
    public String getVersionNumber() {
        return versionNumber;
    }
    
    public void setVersionNumber(String versionNumber) {
        this.versionNumber = versionNumber;
    }
    
    public Map<String, Object> getDsl() {
        return dsl;
    }
    
    public void setDsl(Map<String, Object> dsl) {
        this.dsl = dsl;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    public UUID getCreatedBy() {
        return createdBy;
    }
    
    public void setCreatedBy(UUID createdBy) {
        this.createdBy = createdBy;
    }
    
    public Instant getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
    
    public Instant getPublishedAt() {
        return publishedAt;
    }
    
    public void setPublishedAt(Instant publishedAt) {
        this.publishedAt = publishedAt;
    }
    
    public String getChangeNotes() {
        return changeNotes;
    }
    
    public void setChangeNotes(String changeNotes) {
        this.changeNotes = changeNotes;
    }
}
