package com.elevate.sparkle.domain.model;

import com.elevate.sparkle.domain.valueobject.JourneyDSL;
import com.elevate.sparkle.domain.valueobject.VersionStatus;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Domain entity representing a specific version of a Journey Definition.
 * Versions are immutable once created - no modifications allowed.
 */
public class JourneyVersion {
    
    private UUID id;
    private UUID journeyDefinitionId;
    private String versionNumber;
    private JourneyDSL dsl;
    private VersionStatus status;
    private UUID createdBy;
    private Instant createdAt;
    private Instant publishedAt;
    private String changeNotes;
    
    // Constructor
    public JourneyVersion(
            UUID id,
            UUID journeyDefinitionId,
            String versionNumber,
            JourneyDSL dsl,
            UUID createdBy,
            Instant createdAt,
            String changeNotes
    ) {
        this.id = Objects.requireNonNull(id, "id cannot be null");
        this.journeyDefinitionId = Objects.requireNonNull(journeyDefinitionId, "journeyDefinitionId cannot be null");
        this.versionNumber = Objects.requireNonNull(versionNumber, "versionNumber cannot be null");
        this.dsl = Objects.requireNonNull(dsl, "dsl cannot be null");
        this.createdBy = Objects.requireNonNull(createdBy, "createdBy cannot be null");
        this.createdAt = Objects.requireNonNull(createdAt, "createdAt cannot be null");
        this.changeNotes = changeNotes;
        this.status = VersionStatus.DRAFT;
        
        validateVersionNumber();
    }
    
    // Factory method
    public static JourneyVersion create(
            UUID journeyDefinitionId,
            String versionNumber,
            JourneyDSL dsl,
            UUID createdBy,
            String changeNotes
    ) {
        return new JourneyVersion(
                UUID.randomUUID(),
                journeyDefinitionId,
                versionNumber,
                dsl,
                createdBy,
                Instant.now(),
                changeNotes
        );
    }
    
    // Business logic
    public void publish() {
        if (status == VersionStatus.PUBLISHED) {
            throw new IllegalStateException("Version already published");
        }
        if (status == VersionStatus.ARCHIVED) {
            throw new IllegalStateException("Cannot publish archived version");
        }
        
        this.status = VersionStatus.PUBLISHED;
        this.publishedAt = Instant.now();
    }
    
    public void archive() {
        this.status = VersionStatus.ARCHIVED;
    }
    
    public boolean canBeExecuted() {
        return status == VersionStatus.PUBLISHED;
    }
    
    private void validateVersionNumber() {
        if (versionNumber == null || versionNumber.isBlank()) {
            throw new IllegalArgumentException("Version number cannot be blank");
        }
        // Simple semantic versioning validation
        if (!versionNumber.matches("^\\d+\\.\\d+\\.\\d+$")) {
            throw new IllegalArgumentException(
                    "Version number must follow semantic versioning (e.g., 1.0.0)"
            );
        }
    }
    
    // Getters
    public UUID getId() {
        return id;
    }
    
    public UUID getJourneyDefinitionId() {
        return journeyDefinitionId;
    }
    
    public String getVersionNumber() {
        return versionNumber;
    }
    
    public JourneyDSL getDsl() {
        return dsl;
    }
    
    public VersionStatus getStatus() {
        return status;
    }
    
    public UUID getCreatedBy() {
        return createdBy;
    }
    
    public Instant getCreatedAt() {
        return createdAt;
    }
    
    public Instant getPublishedAt() {
        return publishedAt;
    }
    
    public String getChangeNotes() {
        return changeNotes;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof JourneyVersion that)) return false;
        return Objects.equals(id, that.id);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
