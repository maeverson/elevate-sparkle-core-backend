package com.elevate.sparkle.domain.valueobject;

/**
 * Status of a Journey Version
 */
public enum VersionStatus {
    /**
     * Version is in draft state - can be modified
     */
    DRAFT,
    
    /**
     * Version is published - immutable and can be executed
     */
    PUBLISHED,
    
    /**
     * Version is archived - cannot be executed
     */
    ARCHIVED
}
