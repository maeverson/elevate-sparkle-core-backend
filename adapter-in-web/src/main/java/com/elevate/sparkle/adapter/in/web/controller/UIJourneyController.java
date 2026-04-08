package com.elevate.sparkle.adapter.in.web.controller;

import com.elevate.sparkle.adapter.in.web.dto.ApiResponse;
import com.elevate.sparkle.adapter.in.web.dto.ui.*;
import com.elevate.sparkle.adapter.in.web.service.JourneyTranslationService;
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

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * BFF Controller for Journey Builder UI
 * Provides UI-optimized endpoints with automatic translation between UI Model and Engine DSL
 */
@RestController
@RequestMapping("/api/ui/journeys")
@Tag(name = "Journey Builder UI", description = "BFF endpoints for visual journey builder")
public class UIJourneyController {
    
    private static final Logger logger = LoggerFactory.getLogger(UIJourneyController.class);
    
    private final JourneyTranslationService translationService;
    private final CreateJourneyUseCase createJourneyUseCase;
    private final CreateJourneyVersionUseCase createVersionUseCase;
    private final GetJourneyUseCase getJourneyUseCase;
    private final GetJourneyVersionUseCase getVersionUseCase;
    
    public UIJourneyController(
            JourneyTranslationService translationService,
            CreateJourneyUseCase createJourneyUseCase,
            CreateJourneyVersionUseCase createVersionUseCase,
            GetJourneyUseCase getJourneyUseCase,
            GetJourneyVersionUseCase getVersionUseCase
    ) {
        this.translationService = translationService;
        this.createJourneyUseCase = createJourneyUseCase;
        this.createVersionUseCase = createVersionUseCase;
        this.getJourneyUseCase = getJourneyUseCase;
        this.getVersionUseCase = getVersionUseCase;
    }
    
    /**
     * GET /api/ui/journeys - List all journeys with UI metadata
     */
    @GetMapping
    @Operation(summary = "List all journeys for UI")
    public ResponseEntity<ApiResponse<List<JourneyListItem>>> listJourneys(
            @RequestParam(required = false, defaultValue = "false") boolean activeOnly
    ) {
        logger.info("Listing journeys for UI: activeOnly={}", activeOnly);
        
        try {
            List<JourneyDefinition> journeys = activeOnly ? 
                    getJourneyUseCase.findAllActive() : 
                    getJourneyUseCase.findAll();
            
            List<JourneyListItem> items = journeys.stream()
                    .map(this::toListItem)
                    .collect(Collectors.toList());
            
            return ResponseEntity.ok(ApiResponse.success(items));
            
        } catch (Exception e) {
            logger.error("Failed to list journeys", e);
            return ResponseEntity
                    .internalServerError()
                    .body(ApiResponse.error(500, "Failed to list journeys: " + e.getMessage()));
        }
    }
    
    /**
     * GET /api/ui/journeys/{id} - Get journey with UI Model translation
     */
    @GetMapping("/{journeyId}")
    @Operation(summary = "Get journey as UI Model")
    public ResponseEntity<ApiResponse<JourneyUIModel>> getJourney(
            @PathVariable UUID journeyId,
            @RequestParam(required = false) String version
    ) {
        logger.info("Getting journey for UI: id={}, version={}", journeyId, version);
        
        try {
            JourneyDefinition journey = getJourneyUseCase.findById(journeyId)
                    .orElseThrow(() -> new IllegalArgumentException("Journey not found: " + journeyId));
            
            // Get specific version or latest published
            JourneyVersion journeyVersion;
            if (version != null) {
                journeyVersion = getVersionUseCase.findByJourneyAndVersion(journeyId, version)
                        .orElseThrow(() -> new IllegalArgumentException(
                                "Version not found: " + version));
            } else {
                journeyVersion = getVersionUseCase.findPublishedVersion(journeyId)
                        .orElseThrow(() -> new IllegalArgumentException(
                                "No published version found for journey: " + journeyId));
            }
            
            // Translate Engine DSL -> UI Model
            JourneyUIModel.JourneyMetadata metadata = new JourneyUIModel.JourneyMetadata(
                    journey.getCreatedBy().toString(),
                    journeyVersion.getCreatedBy().toString(),
                    journey.getCreatedAt().toString(),
                    journeyVersion.getCreatedAt().toString(),
                    journeyVersion.getStatus().toString()
            );
            
            JourneyUIModel uiModel = translationService.translateToUIModel(
                    journey.getId(),
                    journey.getName(),
                    journey.getDescription(),
                    journeyVersion.getVersionNumber(),
                    journeyVersion.getDsl(),
                    metadata
            );
            
            return ResponseEntity.ok(ApiResponse.success(uiModel));
            
        } catch (IllegalArgumentException e) {
            logger.error("Journey not found: {}", e.getMessage());
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
     * POST /api/ui/journeys - Create new journey from UI Model
     */
    @PostMapping
    @Operation(summary = "Create journey from UI Model")
    public ResponseEntity<ApiResponse<JourneyUIModel>> createJourney(
            @RequestBody CreateJourneyUIRequest request,
            Authentication authentication
    ) {
        logger.info("Creating journey from UI: name={}", request.name());
        
        try {
            UUID userId = getUserId(authentication);
            
            // Create journey definition
            CreateJourneyUseCase.CreateJourneyCommand createCommand = 
                    new CreateJourneyUseCase.CreateJourneyCommand(
                            request.name(),
                            request.description(),
                            userId
                    );
            
            JourneyDefinition journey = createJourneyUseCase.execute(createCommand);
            
            // Translate UI Model -> Engine DSL
            JourneyDSL dsl = translationService.translateToEngineDSL(request.uiModel());
            
            // Create initial version (v1.0)
            CreateJourneyVersionUseCase.CreateVersionCommand versionCommand = 
                    new CreateJourneyVersionUseCase.CreateVersionCommand(
                            journey.getId(),
                            "1.0",
                            dsl,
                            userId,
                            "Initial version"
                    );
            
            JourneyVersion version = createVersionUseCase.execute(versionCommand);
            
            // Return UI Model
            JourneyUIModel.JourneyMetadata metadata = new JourneyUIModel.JourneyMetadata(
                    userId.toString(),
                    userId.toString(),
                    journey.getCreatedAt().toString(),
                    version.getCreatedAt().toString(),
                    version.getStatus().toString()
            );
            
            JourneyUIModel uiModel = translationService.translateToUIModel(
                    journey.getId(),
                    journey.getName(),
                    journey.getDescription(),
                    version.getVersionNumber(),
                    dsl,
                    metadata
            );
            
            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(ApiResponse.success(uiModel, 201));
                    
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
     * PUT /api/ui/journeys/{id} - Update journey from UI Model (creates new version)
     */
    @PutMapping("/{journeyId}")
    @Operation(summary = "Update journey from UI Model")
    public ResponseEntity<ApiResponse<JourneyUIModel>> updateJourney(
            @PathVariable UUID journeyId,
            @RequestBody UpdateJourneyUIRequest request,
            Authentication authentication
    ) {
        logger.info("Updating journey from UI: id={}", journeyId);
        
        try {
            UUID userId = getUserId(authentication);
            
            // Verify journey exists
            JourneyDefinition journey = getJourneyUseCase.findById(journeyId)
                    .orElseThrow(() -> new IllegalArgumentException("Journey not found: " + journeyId));
            
            // Translate UI Model -> Engine DSL
            JourneyDSL dsl = translationService.translateToEngineDSL(request.uiModel());
            
            // Create new version
            CreateJourneyVersionUseCase.CreateVersionCommand versionCommand = 
                    new CreateJourneyVersionUseCase.CreateVersionCommand(
                            journeyId,
                            request.version(),
                            dsl,
                            userId,
                            request.changeNotes()
                    );
            
            JourneyVersion version = createVersionUseCase.execute(versionCommand);
            
            // Return UI Model
            JourneyUIModel.JourneyMetadata metadata = new JourneyUIModel.JourneyMetadata(
                    journey.getCreatedBy().toString(),
                    userId.toString(),
                    journey.getCreatedAt().toString(),
                    version.getCreatedAt().toString(),
                    version.getStatus().toString()
            );
            
            JourneyUIModel uiModel = translationService.translateToUIModel(
                    journey.getId(),
                    journey.getName(),
                    journey.getDescription(),
                    version.getVersionNumber(),
                    dsl,
                    metadata
            );
            
            return ResponseEntity.ok(ApiResponse.success(uiModel));
                    
        } catch (IllegalArgumentException e) {
            logger.error("Invalid request: {}", e.getMessage());
            return ResponseEntity
                    .badRequest()
                    .body(ApiResponse.error(400, e.getMessage()));
        } catch (Exception e) {
            logger.error("Failed to update journey", e);
            return ResponseEntity
                    .internalServerError()
                    .body(ApiResponse.error(500, "Failed to update journey: " + e.getMessage()));
        }
    }
    
    /**
     * POST /api/ui/journeys/validate - Validate UI Model without saving
     */
    @PostMapping("/validate")
    @Operation(summary = "Validate journey UI Model")
    public ResponseEntity<ApiResponse<ValidationResult>> validateJourney(
            @RequestBody JourneyUIModel uiModel
    ) {
        logger.info("Validating journey UI Model");
        
        try {
            // Try to translate to validate structure
            translationService.translateToEngineDSL(uiModel);
            
            // If no exception, validation passed
            ValidationResult result = new ValidationResult(true, List.of(), List.of());
            
            return ResponseEntity.ok(ApiResponse.success(result));
            
        } catch (IllegalArgumentException e) {
            logger.warn("Validation failed: {}", e.getMessage());
            
            ValidationResult.ValidationError error = new ValidationResult.ValidationError(
                    "VALIDATION_ERROR",
                    e.getMessage(),
                    null,
                    "ERROR"
            );
            
            ValidationResult result = new ValidationResult(false, List.of(error), List.of());
            
            return ResponseEntity.ok(ApiResponse.success(result));
            
        } catch (Exception e) {
            logger.error("Validation error", e);
            return ResponseEntity
                    .internalServerError()
                    .body(ApiResponse.error(500, "Validation error: " + e.getMessage()));
        }
    }
    
    // ========================================================================
    // HELPER METHODS
    // ========================================================================
    
    private UUID getUserId(Authentication authentication) {
        if (authentication == null || authentication.getPrincipal() == null) {
            return UUID.fromString("00000000-0000-0000-0000-000000000000"); // System user for testing
        }
        // Extract user ID from authentication
        return UUID.randomUUID(); // TODO: Get from actual auth principal
    }
    
    private JourneyListItem toListItem(JourneyDefinition journey) {
        // Derive status from journey state
        String status = journey.isArchived() ? "ARCHIVED" : 
                       (journey.hasPublishedVersion() ? "PUBLISHED" : "DRAFT");
        
        return new JourneyListItem(
                journey.getId(),
                journey.getName(),
                journey.getDescription(),
                status,
                DateTimeFormatter.ISO_INSTANT.format(journey.getCreatedAt()),
                DateTimeFormatter.ISO_INSTANT.format(journey.getUpdatedAt())
        );
    }
    
    /**
     * Simplified journey list item for listing endpoint
     */
    public record JourneyListItem(
            UUID id,
            String name,
            String description,
            String status,
            String createdAt,
            String updatedAt
    ) {}
    
    /**
     * Request for updating journey
     */
    public record UpdateJourneyUIRequest(
            String version,
            String changeNotes,
            JourneyUIModel uiModel
    ) {}
}
