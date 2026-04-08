-- Create execution_summary projection table for CQRS read model
-- This table stores aggregated execution data for fast dashboard queries

CREATE TABLE execution_summary (
    execution_id UUID PRIMARY KEY,
    journey_id UUID NOT NULL,
    journey_name VARCHAR(255) NOT NULL,
    status VARCHAR(50) NOT NULL,
    started_at TIMESTAMP NOT NULL,
    completed_at TIMESTAMP,
    duration_ms BIGINT,
    total_steps INT NOT NULL DEFAULT 0,
    completed_steps INT NOT NULL DEFAULT 0,
    failed_steps INT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_execution_summary_journey 
        FOREIGN KEY (journey_id) REFERENCES journey_definitions(id) ON DELETE CASCADE,
    
    CONSTRAINT chk_status 
        CHECK (status IN ('RUNNING', 'COMPLETED', 'FAILED', 'CANCELLED')),
    
    CONSTRAINT chk_step_counts 
        CHECK (completed_steps >= 0 AND failed_steps >= 0 AND total_steps >= 0),
    
    CONSTRAINT chk_duration 
        CHECK (duration_ms IS NULL OR duration_ms >= 0)
);

-- Index for journey-specific queries
CREATE INDEX idx_execution_summary_journey_id 
    ON execution_summary(journey_id);

-- Index for status filtering
CREATE INDEX idx_execution_summary_status 
    ON execution_summary(status);

-- Index for date range queries
CREATE INDEX idx_execution_summary_started_at 
    ON execution_summary(started_at DESC);

-- Composite index for dashboard queries (journey + status + date)
CREATE INDEX idx_execution_summary_dashboard 
    ON execution_summary(journey_id, status, started_at DESC);

-- Index for completed executions ordering
CREATE INDEX idx_execution_summary_completed 
    ON execution_summary(completed_at DESC) 
    WHERE completed_at IS NOT NULL;

-- Comment descriptions
COMMENT ON TABLE execution_summary IS 'CQRS projection for fast execution queries in dashboards';
COMMENT ON COLUMN execution_summary.execution_id IS 'Unique execution identifier (matches aggregate root ID)';
COMMENT ON COLUMN execution_summary.journey_id IS 'Reference to journey definition';
COMMENT ON COLUMN execution_summary.journey_name IS 'Denormalized journey name for quick display';
COMMENT ON COLUMN execution_summary.status IS 'Current execution status (RUNNING, COMPLETED, FAILED, CANCELLED)';
COMMENT ON COLUMN execution_summary.duration_ms IS 'Execution duration in milliseconds (null if still running)';
COMMENT ON COLUMN execution_summary.total_steps IS 'Total number of steps in the journey';
COMMENT ON COLUMN execution_summary.completed_steps IS 'Number of successfully completed steps';
COMMENT ON COLUMN execution_summary.failed_steps IS 'Number of failed steps';
