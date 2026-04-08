package com.elevate.sparkle.application.port.in;

import com.elevate.sparkle.domain.model.JourneyDefinition;

import java.util.UUID;

/**
 * Use case for creating a new journey definition
 */
public interface CreateJourneyUseCase {
    
    /**
     * Create a new journey definition
     */
    JourneyDefinition execute(CreateJourneyCommand command);
    
    /**
     * Command for creating a journey
     */
    record CreateJourneyCommand(
            String name,
            String description,
            UUID createdBy
    ) {
        public CreateJourneyCommand {
            if (name == null || name.isBlank()) {
                throw new IllegalArgumentException("Journey name cannot be blank");
            }
            if (createdBy == null) {
                throw new IllegalArgumentException("createdBy cannot be null");
            }
        }
    }
}
