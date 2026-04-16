-- Add tenant_id to users table
ALTER TABLE users ADD COLUMN tenant_id VARCHAR(255);
CREATE INDEX idx_users_tenant_id ON users(tenant_id);

-- Create connectors table
CREATE TABLE connectors (
    id VARCHAR(255) PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    type VARCHAR(50) NOT NULL,
    config TEXT,
    status VARCHAR(50) NOT NULL DEFAULT 'ACTIVE',
    tenant_id VARCHAR(255) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

CREATE INDEX idx_connectors_tenant_id ON connectors(tenant_id);
CREATE INDEX idx_connectors_type ON connectors(type);
CREATE INDEX idx_connectors_status ON connectors(status);
