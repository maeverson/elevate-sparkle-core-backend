package com.elevate.sparkle.adapter.out.persistence.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

/**
 * JPA entity for Journey Definition
 */
@Entity
@Table(name = "journey_definitions")
public class JourneyDefinitionEntity {
    
    @Id
    private UUID id;
    
    @Column(nullable = false, unique = true, length = 200)
    private String name;
    
    @Column(columnDefinition = "TEXT")
    private String description;
    
    @Column(name = "current_published_version_id")
    private UUID currentPublishedVersionId;
    
    @Column(name = "created_by", nullable = false)
    private UUID createdBy;
    
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;
    
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
    
    @Column(nullable = false)
    private Boolean archived = false;
    
    // Constructors
    public JourneyDefinitionEntity() {
    }
    
    public JourneyDefinitionEntity(
            UUID id,
            String name,
            String description,
            UUID createdBy,
            Instant createdAt
    ) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.createdBy = createdBy;
        this.createdAt = createdAt;
        this.updatedAt = createdAt;
        this.archived = false;
    }
    
    // Getters and Setters
    public UUID getId() {
        return id;
    }
    
    public void setId(UUID id) {
        this.id = id;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public UUID getCurrentPublishedVersionId() {
        return currentPublishedVersionId;
    }
    
    public void setCurrentPublishedVersionId(UUID currentPublishedVersionId) {
        this.currentPublishedVersionId = currentPublishedVersionId;
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
    
    public Instant getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    public Boolean getArchived() {
        return archived;
    }
    
    public void setArchived(Boolean archived) {
        this.archived = archived;
    }
}
