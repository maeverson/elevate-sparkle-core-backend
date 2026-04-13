package com.elevate.sparkle.adapter.persistence.projection;

import com.elevate.sparkle.domain.event.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.sql.Timestamp;
import java.util.UUID;

/**
 * Updates execution_summary projection table based on domain events.
 * Uses @TransactionalEventListener to ensure projections are updated
 * only after the event is successfully persisted in event_store.
 */
@Component
public class ExecutionProjectionUpdater {
    
    private static final Logger logger = LoggerFactory.getLogger(ExecutionProjectionUpdater.class);
    
    private final JdbcTemplate jdbcTemplate;
    
    public ExecutionProjectionUpdater(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }
    
    /**
     * Handles journey start by creating initial execution_summary record.
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onJourneyStarted(JourneyStartedEvent event) {
        try {
            logger.debug("Creating execution summary for execution: {}", event.aggregateId());
            
            String journeyName = fetchJourneyName(event.journeyDefinitionId());
            
            int totalSteps = countTotalSteps(event.journeyDefinitionId(), event.journeyVersion());
            
            String sql = """
                INSERT INTO execution_summary (
                    execution_id, journey_id, journey_name, status,
                    started_at, total_steps, completed_steps, failed_steps,
                    created_at, updated_at
                ) VALUES (?, ?, ?, 'RUNNING', ?, ?, 0, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
                """;
            
            jdbcTemplate.update(sql,
                event.aggregateId(),
                event.journeyDefinitionId(),
                journeyName,
                Timestamp.from(event.occurredAt()),
                totalSteps
            );
            
            logger.info("Execution summary created for execution: {}", event.aggregateId());
            
        } catch (Exception e) {
            logger.error("Failed to create execution summary for execution: {}", event.aggregateId(), e);
            // Don't rethrow - projection updates should not fail the main transaction
        }
    }
    
    /**
     * Handles journey completion by updating status and completion info.
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onJourneyCompleted(JourneyCompletedEvent event) {
        try {
            logger.debug("Updating execution summary to COMPLETED for execution: {}", event.aggregateId());
            
            String sql = """
                UPDATE execution_summary
                SET status = 'COMPLETED',
                    completed_at = ?,
                    duration_ms = ?,
                    updated_at = CURRENT_TIMESTAMP
                WHERE execution_id = ?
                """;
            
            int updated = jdbcTemplate.update(sql,
                Timestamp.from(event.completedAt()),
                event.totalDurationMs(),
                event.aggregateId()
            );
            
            if (updated == 0) {
                logger.warn("No execution summary found to update for execution: {}", event.aggregateId());
            } else {
                logger.info("Execution summary updated to COMPLETED for execution: {}", event.aggregateId());
            }
            
        } catch (Exception e) {
            logger.error("Failed to update execution summary for completed execution: {}", event.aggregateId(), e);
        }
    }
    
    /**
     * Handles journey failure by updating status.
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onJourneyFailed(JourneyFailedEvent event) {
        try {
            logger.debug("Updating execution summary to FAILED for execution: {}", event.aggregateId());
            
            String sql = """
                UPDATE execution_summary
                SET status = 'FAILED',
                    completed_at = ?,
                    duration_ms = EXTRACT(EPOCH FROM (? - started_at)) * 1000,
                    updated_at = CURRENT_TIMESTAMP
                WHERE execution_id = ?
                """;
            
            int updated = jdbcTemplate.update(sql,
                Timestamp.from(event.failedAt()),
                Timestamp.from(event.failedAt()),
                event.aggregateId()
            );
            
            if (updated == 0) {
                logger.warn("No execution summary found to update for execution: {}", event.aggregateId());
            } else {
                logger.info("Execution summary updated to FAILED for execution: {}", event.aggregateId());
            }
            
        } catch (Exception e) {
            logger.error("Failed to update execution summary for failed execution: {}", event.aggregateId(), e);
        }
    }
    
    /**
     * Handles step completion by incrementing completed_steps counter.
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onStepCompleted(StepCompletedEvent event) {
        try {
            logger.debug("Incrementing completed_steps for execution: {}", event.aggregateId());
            
            String sql = """
                UPDATE execution_summary
                SET completed_steps = completed_steps + 1,
                    updated_at = CURRENT_TIMESTAMP
                WHERE execution_id = ?
                """;
            
            jdbcTemplate.update(sql, event.aggregateId());
            
        } catch (Exception e) {
            logger.error("Failed to increment completed_steps for execution: {}", event.aggregateId(), e);
        }
    }
    
    /**
     * Handles step failure by incrementing failed_steps counter.
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onStepFailed(StepFailedEvent event) {
        try {
            logger.debug("Incrementing failed_steps for execution: {}", event.aggregateId());
            
            String sql = """
                UPDATE execution_summary
                SET failed_steps = failed_steps + 1,
                    updated_at = CURRENT_TIMESTAMP
                WHERE execution_id = ?
                """;
            
            jdbcTemplate.update(sql, event.aggregateId());
            
        } catch (Exception e) {
            logger.error("Failed to increment failed_steps for execution: {}", event.aggregateId(), e);
        }
    }
    
    /**
     * Fetches journey name from journey_definitions table.
     * Returns "Unknown Journey" if not found to avoid null values.
     */
    private String fetchJourneyName(UUID journeyId) {
        try {
            String sql = "SELECT name FROM journey_definitions WHERE id = ?";
            return jdbcTemplate.queryForObject(sql, String.class, journeyId);
        } catch (Exception e) {
            logger.warn("Failed to fetch journey name for journey: {}, using default", journeyId, e);
            return "Unknown Journey";
        }
    }
    
    /**
     * Counts total steps in a journey version by parsing the DSL.
     * Returns 0 if unable to count (will be updated as steps execute).
     */
    private int countTotalSteps(UUID journeyId, String version) {
        try {
            String sql = """
                SELECT jsonb_array_length(dsl_definition->'steps')
                FROM journey_versions
                WHERE journey_id = ? AND version = ?
                """;
            Integer count = jdbcTemplate.queryForObject(sql, Integer.class, journeyId, version);
            return count != null ? count : 0;
        } catch (Exception e) {
            logger.warn("Failed to count total steps for journey {} version {}, using 0", 
                journeyId, version, e);
            return 0;
        }
    }
}
