package com.elevate.sparkle.adapter.in.web.controller;

import com.elevate.sparkle.adapter.in.web.dto.ApiResponse;
import com.elevate.sparkle.application.port.in.GetDashboardMetricsUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * REST API for Dashboard metrics and statistics
 */
@RestController
@RequestMapping("/api/dashboard")
@Tag(name = "Dashboard", description = "Dashboard metrics and statistics APIs")
public class DashboardController {
    
    private static final Logger logger = LoggerFactory.getLogger(DashboardController.class);
    
    private final GetDashboardMetricsUseCase getDashboardMetrics;
    
    public DashboardController(GetDashboardMetricsUseCase getDashboardMetrics) {
        this.getDashboardMetrics = getDashboardMetrics;
    }
    
    /**
     * Get statistics for a specific journey
     */
    @GetMapping("/journeys/{journeyId}/statistics")
    @Operation(summary = "Get journey statistics")
    public ResponseEntity<ApiResponse<GetDashboardMetricsUseCase.JourneyStatistics>> getJourneyStatistics(
            @PathVariable UUID journeyId
    ) {
        logger.info("Getting statistics for journey: {}", journeyId);
        
        try {
            GetDashboardMetricsUseCase.JourneyStatistics stats = 
                    getDashboardMetrics.getJourneyStatistics(journeyId);
            
            return ResponseEntity.ok(ApiResponse.success(stats));
            
        } catch (IllegalArgumentException e) {
            logger.error("Journey not found: {}", journeyId, e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(400, "Journey not found: " + journeyId));
        } catch (Exception e) {
            logger.error("Failed to get journey statistics", e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error(500, "Failed to get statistics: " + e.getMessage()));
        }
    }
    
    /**
     * Get execution metrics for a time period
     */
    @GetMapping("/metrics")
    @Operation(summary = "Get execution metrics")
    public ResponseEntity<ApiResponse<GetDashboardMetricsUseCase.ExecutionMetrics>> getExecutionMetrics(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to
    ) {
        logger.info("Getting execution metrics from {} to {}", from, to);
        
        try {
            GetDashboardMetricsUseCase.ExecutionMetrics metrics = 
                    getDashboardMetrics.getExecutionMetrics(from, to);
            
            return ResponseEntity.ok(ApiResponse.success(metrics));
            
        } catch (Exception e) {
            logger.error("Failed to get execution metrics", e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error(500, "Failed to get metrics: " + e.getMessage()));
        }
    }
    
    /**
     * Get failed steps summary
     */
    @GetMapping("/failed-steps")
    @Operation(summary = "Get failed steps summary")
    public ResponseEntity<ApiResponse<List<GetDashboardMetricsUseCase.FailedStepSummary>>> getFailedSteps(
            @RequestParam(required = false, defaultValue = "50") Integer limit
    ) {
        logger.info("Getting failed steps, limit: {}", limit);
        
        try {
            List<GetDashboardMetricsUseCase.FailedStepSummary> failedSteps = 
                    getDashboardMetrics.getFailedSteps(limit);
            
            return ResponseEntity.ok(ApiResponse.success(failedSteps));
            
        } catch (Exception e) {
            logger.error("Failed to get failed steps", e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error(500, "Failed to get failed steps: " + e.getMessage()));
        }
    }
}
