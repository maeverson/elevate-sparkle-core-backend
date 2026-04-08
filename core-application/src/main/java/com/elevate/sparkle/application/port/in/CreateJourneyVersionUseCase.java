package com.elevate.sparkle.application.port.in;

import com.elevate.sparkle.domain.model.JourneyVersion;
import com.elevate.sparkle.domain.valueobject.JourneyDSL;

import java.util.UUID;

/**
 * Use case for creating a new version of a journey
 */
public interface CreateJourneyVersionUseCase {
    
    /**
     * Create a new version of a journey
     */
    JourneyVersion execute(CreateVersionCommand command);
    
    /**
     * Command for creating a journey version
     */
    record CreateVersionCommand(
            UUID journeyDefinitionId,
            String versionNumber,
            JourneyDSL dsl,
            UUID createdBy,
            String changeNotes
    ) {
        public CreateVersionCommand {
            if (journeyDefinitionId == null) {
                throw new IllegalArgumentException("journeyDefinitionId cannot be null");
            }
            if (versionNumber == null || versionNumber.isBlank()) {
                throw new IllegalArgumentException("versionNumber cannot be blank");
            }
            if (dsl == null) {
                throw new IllegalArgumentException("DSL cannot be null");
            }
            if (createdBy == null) {
                throw new IllegalArgumentException("createdBy cannot be null");
            }
        }
    }
}
