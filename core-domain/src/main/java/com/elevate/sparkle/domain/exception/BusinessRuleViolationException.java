package com.elevate.sparkle.domain.exception;

/**
 * Thrown when a business rule is violated
 */
public class BusinessRuleViolationException extends DomainException {
    
    public BusinessRuleViolationException(String message) {
        super(message);
    }
}
