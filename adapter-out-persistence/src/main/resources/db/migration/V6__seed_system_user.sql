-- Create system/admin user for seed data and system operations
-- This user is referenced by journey_definitions created_by field
-- Password hash is for: "admin123" (change in production!)

INSERT INTO users (
    id,
    username,
    email,
    password_hash,
    first_name,
    last_name,
    active,
    created_at,
    updated_at
) VALUES (
    '00000000-0000-0000-0000-000000000001',
    'system',
    'system@sparkle.local',
    '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', -- BCrypt hash for "admin123"
    'System',
    'Administrator',
    true,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
) ON CONFLICT (id) DO NOTHING;

-- Assign admin role to system user
INSERT INTO user_roles (user_id, role) VALUES
    ('00000000-0000-0000-0000-000000000001', 'ADMIN'),
    ('00000000-0000-0000-0000-000000000001', 'USER')
ON CONFLICT DO NOTHING;

COMMENT ON TABLE users IS 'Application users with authentication credentials';
COMMENT ON COLUMN users.password_hash IS 'BCrypt password hash (minimum cost factor 10)';
