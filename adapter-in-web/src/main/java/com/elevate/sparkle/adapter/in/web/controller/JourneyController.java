package com.elevate.sparkle.adapter.in.web.controller;

import com.elevate.sparkle.adapter.in.web.dto.*;
import com.elevate.sparkle.adapter.in.web.mapper.JourneyDSLMapper;
import com.elevate.sparkle.application.port.in.*;
import com.elevate.sparkle.domain.model.JourneyDefinition;
import com.elevate.sparkle.domain.model.JourneyVersion;
import com.elevate.sparkle.domain.valueobject.JourneyDSL;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * REST Controller for Journey Definition Management (Control Plane)
 */
@RestController
@RequestMapping("/api/journeys")
@Tag(name = "Journey Management", description = "CRUD operations for journey definitions and versions")
public class JourneyController {
    
    private static final Logger logger = LoggerFactory.getLogger(JourneyController.class);
    
    private final CreateJourneyUseCase createJourneyUseCase;
    private final CreateJourneyVersionUseCase createVersionUseCase;
    private final PublishJourneyVersionUseCase publishVersionUseCase;
    private final GetJourneyUseCase getJourneyUseCase;
    private final GetJourneyVersionUseCase getVersionUseCase;
    private final JourneyDSLMapper dslMapper;
    
    public JourneyController(
            CreateJourneyUseCase createJourneyUseCase,
            CreateJourneyVersionUseCase createVersionUseCase,
            PublishJourneyVersionUseCase publishVersionUseCase,
            GetJourneyUseCase getJourneyUseCase,
            GetJourneyVersionUseCase getVersionUseCase,
            JourneyDSLMapper dslMapper
    ) {
        this.createJourneyUseCase = createJourneyUseCase;
        this.createVersionUseCase = createVersionUseCase;
        this.publishVersionUseCase = publishVersionUseCase;
        this.getJourneyUseCase = getJourneyUseCase;
        this.getVersionUseCase = getVersionUseCase;
        this.dslMapper = dslMapper;
    }
    
    /**
     * Create a new journey definition
     */
    @PostMapping
    @Operation(summary = "Create a new journey definition")
    public ResponseEntity<ApiResponse<JourneyDefinitionResponse>> createJourney(
            @RequestBody CreateJourneyRequest request,
            Authentication authentication
    ) {
        logger.info("Creating journey: name={}", request.name());
        
        try {
            UUID userId = getUserId(authentication);
            
            CreateJourneyUseCase.CreateJourneyCommand command = 
                    new CreateJourneyUseCase.CreateJourneyCommand(
                            request.name(),
                            request.description(),
                            userId
                    );
            
            JourneyDefinition journey = createJourneyUseCase.execute(command);
            JourneyDefinitionResponse response = toDefinitionResponse(journey);
            
            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(ApiResponse.success(response, 201));
                    
        } catch (IllegalArgumentException e) {
            logger.error("Invalid request: {}", e.getMessage());
            return ResponseEntity
                    .badRequest()
                    .body(ApiResponse.error(400, e.getMessage()));
        } catch (Exception e) {
            logger.error("Failed to create journey", e);
            return ResponseEntity
                    .internalServerError()
                    .body(ApiResponse.error(500, "Failed to create journey: " + e.getMessage()));
        }
    }
    
    /**
     * Get all journeys
     */
    @GetMapping
    @Operation(summary = "List all journey definitions")
    public ResponseEntity<ApiResponse<List<JourneyDefinitionResponse>>> listJourneys(
            @RequestParam(required = false, defaultValue = "false") boolean activeOnly
    ) {
        logger.info("Listing journeys: activeOnly={}", activeOnly);
        
        try {
            List<JourneyDefinition> journeys = activeOnly ? 
                    getJourneyUseCase.findAllActive() : 
                    getJourneyUseCase.findAll();
            
            List<JourneyDefinitionResponse> response = journeys.stream()
                    .map(this::toDefinitionResponse)
                    .collect(Collectors.toList());
            
            return ResponseEntity.ok(ApiResponse.success(response));
            
        } catch (Exception e) {
            logger.error("Failed to list journeys", e);
            return ResponseEntity
                    .internalServerError()
                    .body(ApiResponse.error(500, "Failed to list journeys: " + e.getMessage()));
        }
    }
    
    /**
     * Get journey by ID
     */
    @GetMapping("/{journeyId}")
    @Operation(summary = "Get journey definition by ID")
    public ResponseEntity<ApiResponse<JourneyDefinitionResponse>> getJourney(
            @PathVariable UUID journeyId
    ) {
        logger.info("Getting journey: id={}", journeyId);
        
        try {
            JourneyDefinition journey = getJourneyUseCase.findById(journeyId)
                    .orElseThrow(() -> new IllegalArgumentException("Journey not found: " + journeyId));
            
            JourneyDefinitionResponse response = toDefinitionResponse(journey);
            
            return ResponseEntity.ok(ApiResponse.success(response));
            
        } catch (IllegalArgumentException e) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(404, e.getMessage()));
        } catch (Exception e) {
            logger.error("Failed to get journey", e);
            return ResponseEntity
                    .internalServerError()
                    .body(ApiResponse.error(500, "Failed to get journey: " + e.getMessage()));
        }
    }
    
    /**
     * Create a new version of a journey
     */
    @PostMapping("/{journeyId}/versions")
    @Operation(summary = "Create a new version of a journey")
    public ResponseEntity<ApiResponse<JourneyVersionResponse>> createVersion(
            @PathVariable UUID journeyId,
            @RequestBody CreateJourneyVersionRequest request,
            Authentication authentication
    ) {
        logger.info("Creating version: journeyId={}, version={}", journeyId, request.versionNumber());
        
        try {
            UUID userId = getUserId(authentication);
            
            JourneyDSL dsl = dslMapper.fromMap(request.dsl());
            
            CreateJourneyVersionUseCase.CreateVersionCommand command = 
                    new CreateJourneyVersionUseCase.CreateVersionCommand(
                            journeyId,
                            request.versionNumber(),
                            dsl,
                            userId,
                            request.changeNotes()
                    );
            
            JourneyVersion version = createVersionUseCase.execute(command);
            JourneyVersionResponse response = toVersionResponse(version);
            
            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(ApiResponse.success(response, 201));
                    
        } catch (IllegalArgumentException e) {
            logger.error("Invalid request: {}", e.getMessage());
            return ResponseEntity
                    .badRequest()
                    .body(ApiResponse.error(400, e.getMessage()));
        } catch (Exception e) {
            logger.error("Failed to create version", e);
            return ResponseEntity
                    .internalServerError()
                    .body(ApiResponse.error(500, "Failed to create version: " + e.getMessage()));
        }
    }
    
    /**
     * Get all versions of a journey
     */
    @GetMapping("/{journeyId}/versions")
    @Operation(summary = "List all versions of a journey")
    public ResponseEntity<ApiResponse<List<JourneyVersionResponse>>> listVersions(
            @PathVariable UUID journeyId
    ) {
        logger.info("Listing versions: journeyId={}", journeyId);
        
        try {
            List<JourneyVersion> versions = getVersionUseCase.findAllByJourney(journeyId);
            
            List<JourneyVersionResponse> response = versions.stream()
                    .map(this::toVersionResponse)
                    .collect(Collectors.toList());
            
            return ResponseEntity.ok(ApiResponse.success(response));
            
        } catch (Exception e) {
            logger.error("Failed to list versions", e);
            return ResponseEntity
                    .internalServerError()
                    .body(ApiResponse.error(500, "Failed to list versions: " + e.getMessage()));
        }
    }
    
    /**
     * Publish a journey version
     */
    @PostMapping("/{journeyId}/versions/{versionId}/publish")
    @Operation(summary = "Publish a journey version")
    public ResponseEntity<ApiResponse<String>> publishVersion(
            @PathVariable UUID journeyId,
            @PathVariable UUID versionId
    ) {
        logger.info("Publishing version: journeyId={}, versionId={}", journeyId, versionId);
        
        try {
            publishVersionUseCase.execute(journeyId, versionId);
            
            return ResponseEntity.ok(ApiResponse.success("Version published successfully"));
            
        } catch (IllegalArgumentException e) {
            logger.error("Invalid request: {}", e.getMessage());
            return ResponseEntity
                    .badRequest()
                    .body(ApiResponse.error(400, e.getMessage()));
        } catch (Exception e) {
            logger.error("Failed to publish version", e);
            return ResponseEntity
                    .internalServerError()
                    .body(ApiResponse.error(500, "Failed to publish version: " + e.getMessage()));
        }
    }
    
    // ========== Helper Methods ==========
    
    private JourneyDefinitionResponse toDefinitionResponse(JourneyDefinition journey) {
        return new JourneyDefinitionResponse(
                journey.getId(),
                journey.getName(),
                journey.getDescription(),
                journey.getCurrentPublishedVersionId(),
                journey.getCreatedBy(),
                journey.getCreatedAt(),
                journey.getUpdatedAt(),
                journey.isArchived()
        );
    }
    
    private JourneyVersionResponse toVersionResponse(JourneyVersion version) {
        return new JourneyVersionResponse(
                version.getId(),
                version.getJourneyDefinitionId(),
                version.getVersionNumber(),
                dslMapper.toMap(version.getDsl()),
                version.getStatus().name(),
                version.getCreatedBy(),
                version.getCreatedAt(),
                version.getPublishedAt(),
                version.getChangeNotes()
        );
    }
    
    private UUID getUserId(Authentication authentication) {
        // For now, use a dummy user ID
        // In production, extract from JWT or User Details
        return UUID.fromString("00000000-0000-0000-0000-000000000001");
    }
}
