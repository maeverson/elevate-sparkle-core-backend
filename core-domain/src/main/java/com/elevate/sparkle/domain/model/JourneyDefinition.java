package com.elevate.sparkle.domain.model;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Domain entity representing a Journey Definition.
 * A journey is a reusable workflow template that can have multiple versions.
 */
public class JourneyDefinition {
    
    private UUID id;
    private String name;
    private String description;
    private UUID currentPublishedVersionId;
    private UUID createdBy;
    private Instant createdAt;
    private Instant updatedAt;
    private boolean archived;
    
    // Constructor
    public JourneyDefinition(
            UUID id,
            String name,
            String description,
            UUID createdBy,
            Instant createdAt
    ) {
        this.id = Objects.requireNonNull(id, "id cannot be null");
        this.name = Objects.requireNonNull(name, "name cannot be null");
        this.description = description;
        this.createdBy = Objects.requireNonNull(createdBy, "createdBy cannot be null");
        this.createdAt = Objects.requireNonNull(createdAt, "createdAt cannot be null");
        this.updatedAt = createdAt;
        this.archived = false;
        
        validateName();
    }
    
    // Factory method for creating new journey
    public static JourneyDefinition create(String name, String description, UUID createdBy) {
        return new JourneyDefinition(
                UUID.randomUUID(),
                name,
                description,
                createdBy,
                Instant.now()
        );
    }
    
    // Business logic
    public void updateInfo(String name, String description) {
        this.name = Objects.requireNonNull(name, "name cannot be null");
        this.description = description;
        this.updatedAt = Instant.now();
        validateName();
    }
    
    public void publishVersion(UUID versionId) {
        this.currentPublishedVersionId = Objects.requireNonNull(versionId, "versionId cannot be null");
        this.updatedAt = Instant.now();
    }
    
    public void archive() {
        this.archived = true;
        this.updatedAt = Instant.now();
    }
    
    public void unarchive() {
        this.archived = false;
        this.updatedAt = Instant.now();
    }
    
    private void validateName() {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Journey name cannot be blank");
        }
        if (name.length() > 200) {
            throw new IllegalArgumentException("Journey name cannot exceed 200 characters");
        }
    }
    
    public boolean hasPublishedVersion() {
        return currentPublishedVersionId != null;
    }
    
    // Getters
    public UUID getId() {
        return id;
    }
    
    public String getName() {
        return name;
    }
    
    public String getDescription() {
        return description;
    }
    
    public UUID getCurrentPublishedVersionId() {
        return currentPublishedVersionId;
    }
    
    public UUID getCreatedBy() {
        return createdBy;
    }
    
    public Instant getCreatedAt() {
        return createdAt;
    }
    
    public Instant getUpdatedAt() {
        return updatedAt;
    }
    
    public boolean isArchived() {
        return archived;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof JourneyDefinition that)) return false;
        return Objects.equals(id, that.id);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
