package com.elevate.sparkle.domain.exception;

/**
 * Thrown when an entity is not found
 */
public class EntityNotFoundException extends DomainException {
    
    public EntityNotFoundException(String entityName, String identifier) {
        super(String.format("%s with identifier '%s' not found", entityName, identifier));
    }
}
