-- Event Store table for event sourcing
-- This is an append-only log - NO UPDATES OR DELETES allowed

CREATE TABLE event_store (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    aggregate_id UUID NOT NULL,
    aggregate_type VARCHAR(100) NOT NULL,
    event_type VARCHAR(100) NOT NULL,
    event_version INTEGER NOT NULL DEFAULT 1,
    sequence_number BIGINT NOT NULL,
    payload JSONB NOT NULL,
    metadata JSONB NOT NULL DEFAULT '{}',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    -- Ensure no duplicate sequence numbers per aggregate
    CONSTRAINT uk_aggregate_sequence UNIQUE (aggregate_id, sequence_number)
);

-- Index for fast aggregate event loading (critical for performance)
CREATE INDEX idx_event_store_aggregate_id ON event_store(aggregate_id, sequence_number);

-- Index for querying by event type (useful for projections)
CREATE INDEX idx_event_store_event_type ON event_store(event_type, created_at);

-- Index for time-based queries
CREATE INDEX idx_event_store_created_at ON event_store(created_at);

-- Optional: Index for JSONB payload queries (if needed)
CREATE INDEX idx_event_store_payload ON event_store USING GIN(payload);

-- Snapshot table for performance optimization (optional, prepared for future)
CREATE TABLE event_snapshot (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    aggregate_id UUID NOT NULL UNIQUE,
    aggregate_type VARCHAR(100) NOT NULL,
    sequence_number BIGINT NOT NULL,
    state JSONB NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_snapshot_aggregate_id ON event_snapshot(aggregate_id);

-- Comments for documentation
COMMENT ON TABLE event_store IS 'Append-only event store for event sourcing. Never update or delete.';
COMMENT ON COLUMN event_store.aggregate_id IS 'The ID of the aggregate (execution ID)';
COMMENT ON COLUMN event_store.sequence_number IS 'Sequential number for ordering events within an aggregate';
COMMENT ON COLUMN event_store.payload IS 'JSON payload containing event data';
COMMENT ON COLUMN event_store.metadata IS 'JSON metadata (correlation ID, causation ID, user, etc.)';
COMMENT ON TABLE event_snapshot IS 'Snapshots of aggregate state for performance optimization';
