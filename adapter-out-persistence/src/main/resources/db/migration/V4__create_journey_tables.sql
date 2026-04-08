-- Journey management tables for workflow definitions and versioning

-- Journey definitions (template/schema for workflows)
CREATE TABLE journey_definitions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(200) NOT NULL,
    description TEXT,
    current_published_version_id UUID,
    created_by UUID NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    archived BOOLEAN NOT NULL DEFAULT FALSE,
    
    CONSTRAINT uk_journey_name UNIQUE (name)
);

CREATE INDEX idx_journey_definitions_created_by ON journey_definitions(created_by);
CREATE INDEX idx_journey_definitions_created_at ON journey_definitions(created_at DESC);
CREATE INDEX idx_journey_definitions_archived ON journey_definitions(archived);

-- Journey versions (immutable versions of journey definitions)
CREATE TABLE journey_versions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    journey_definition_id UUID NOT NULL,
    version_number VARCHAR(20) NOT NULL,
    dsl JSONB NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'DRAFT',
    created_by UUID NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    published_at TIMESTAMP,
    change_notes TEXT,
    
    CONSTRAINT fk_journey_version_definition 
        FOREIGN KEY (journey_definition_id) 
        REFERENCES journey_definitions(id) 
        ON DELETE CASCADE,
    
    CONSTRAINT uk_journey_version 
        UNIQUE (journey_definition_id, version_number),
    
    CONSTRAINT chk_version_status 
        CHECK (status IN ('DRAFT', 'PUBLISHED', 'ARCHIVED'))
);

CREATE INDEX idx_journey_versions_definition ON journey_versions(journey_definition_id, created_at DESC);
CREATE INDEX idx_journey_versions_status ON journey_versions(status);
CREATE INDEX idx_journey_versions_created_at ON journey_versions(created_at DESC);
CREATE INDEX idx_journey_versions_dsl ON journey_versions USING GIN(dsl);

-- Add foreign key from journey_definitions to current published version
ALTER TABLE journey_definitions
    ADD CONSTRAINT fk_journey_current_version
    FOREIGN KEY (current_published_version_id)
    REFERENCES journey_versions(id)
    ON DELETE SET NULL;

-- Comments
COMMENT ON TABLE journey_definitions IS 'Journey workflow definitions (templates)';
COMMENT ON TABLE journey_versions IS 'Immutable versions of journey definitions with DSL';
COMMENT ON COLUMN journey_versions.dsl IS 'JSON DSL defining the workflow structure';
COMMENT ON COLUMN journey_versions.status IS 'DRAFT, PUBLISHED, or ARCHIVED';
COMMENT ON COLUMN journey_versions.version_number IS 'Semantic version number (e.g., 1.0.0)';
