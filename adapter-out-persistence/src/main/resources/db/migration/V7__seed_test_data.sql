-- Seed test data for journey orchestrator MVP
-- Creates sample journeys, versions, and execution history for testing dashboard

-- ============================================
-- Journey Definitions
-- ============================================

-- Default system admin user ID for seed data
-- In production, this should reference an actual user from the users table

-- Journey 1: HTTP API Call Journey
INSERT INTO journey_definitions (id, name, description, created_by, created_at, updated_at) VALUES
('550e8400-e29b-41d4-a716-446655440001', 
 'HTTP API Integration', 
 'Calls external REST API and processes response',
 '00000000-0000-0000-0000-000000000001', 
 CURRENT_TIMESTAMP, 
 CURRENT_TIMESTAMP);

-- Journey 2: Email Notification Journey  
INSERT INTO journey_definitions (id, name, description, created_by, created_at, updated_at) VALUES
('550e8400-e29b-41d4-a716-446655440002', 
 'Email Notification Service', 
 'Sends templated emails with attachments',
 '00000000-0000-0000-0000-000000000001', 
 CURRENT_TIMESTAMP, 
 CURRENT_TIMESTAMP);

-- Journey 3: Data Processing Pipeline
INSERT INTO journey_definitions (id, name, description, created_by, created_at, updated_at) VALUES
('550e8400-e29b-41d4-a716-446655440003', 
 'Data Processing Pipeline', 
 'ETL pipeline for processing customer data',
 '00000000-0000-0000-0000-000000000001', 
 CURRENT_TIMESTAMP, 
 CURRENT_TIMESTAMP);

-- ============================================
-- Journey Versions
-- ============================================

-- HTTP API Journey - Version 1.0.0
INSERT INTO journey_versions (id, journey_definition_id, version_number, dsl, status, created_by, created_at) VALUES
(gen_random_uuid(),
 '550e8400-e29b-41d4-a716-446655440001',
 '1.0.0',
 '{
   "id": "http-api-journey",
   "name": "HTTP API Integration",
   "steps": [
     {
       "id": "fetch-data",
       "name": "Fetch Data from API",
       "type": "HTTP_CALL",
       "config": {
         "url": "https://api.example.com/data",
         "method": "GET",
         "retryCount": 3
       }
     },
     {
       "id": "validate-response",
       "name": "Validate Response",
       "type": "VALIDATION",
       "config": {
         "schema": "response-schema-v1"
       }
     },
     {
       "id": "transform-data",
       "name": "Transform Data",
       "type": "TRANSFORMATION",
       "config": {
         "mapping": "standard-mapper"
       }
     }
   ]
 }'::jsonb,
 'PUBLISHED',
 '00000000-0000-0000-0000-000000000001',
 CURRENT_TIMESTAMP);

-- Email Journey - Version 1.0.0
INSERT INTO journey_versions (id, journey_definition_id, version_number, dsl, status, created_by, created_at) VALUES
(gen_random_uuid(),
 '550e8400-e29b-41d4-a716-446655440002',
 '1.0.0',
 '{
   "id": "email-notification-journey",
   "name": "Email Notification Service",
   "steps": [
     {
       "id": "prepare-template",
       "name": "Prepare Email Template",
       "type": "TEMPLATE_ENGINE",
       "config": {
         "template": "welcome-email-v2"
       }
     },
     {
       "id": "attach-files",
       "name": "Attach Documents",
       "type": "FILE_HANDLER",
       "config": {
         "attachments": ["terms.pdf", "guide.pdf"]
       }
     },
     {
       "id": "send-email",
       "name": "Send Email",
       "type": "EMAIL_SENDER",
       "config": {
         "provider": "sendgrid",
         "retryCount": 2
       }
     }
   ]
 }'::jsonb,
 'PUBLISHED',
 '00000000-0000-0000-0000-000000000001',
 CURRENT_TIMESTAMP);

-- Data Pipeline - Version 1.0.0
INSERT INTO journey_versions (id, journey_definition_id, version_number, dsl, status, created_by, created_at) VALUES
(gen_random_uuid(),
 '550e8400-e29b-41d4-a716-446655440003',
 '1.0.0',
 '{
   "id": "data-processing-pipeline",
   "name": "Data Processing Pipeline",
   "steps": [
     {
       "id": "extract-data",
       "name": "Extract from Database",
       "type": "DATABASE_QUERY",
       "config": {
         "query": "SELECT * FROM customers WHERE updated_at > :lastRun"
       }
     },
     {
       "id": "clean-data",
       "name": "Clean and Normalize",
       "type": "DATA_CLEANER",
       "config": {
         "rules": ["remove-duplicates", "normalize-names"]
       }
     },
     {
       "id": "enrich-data",
       "name": "Enrich with External Data",
       "type": "HTTP_CALL",
       "config": {
         "url": "https://enrichment.api/enrich"
       }
     },
     {
       "id": "load-data",
       "name": "Load to Data Warehouse",
       "type": "DATABASE_WRITE",
       "config": {
         "target": "warehouse.customers"
       }
     }
   ]
 }'::jsonb,
 'PUBLISHED',
 '00000000-0000-0000-0000-000000000001',
 CURRENT_TIMESTAMP);

-- ============================================
-- Execution History (last 30 days)
-- ============================================

-- HTTP API Journey - Successful executions
INSERT INTO execution_summary (
    execution_id, journey_id, journey_name, status,
    started_at, completed_at, duration_ms,
    total_steps, completed_steps, failed_steps,
    created_at, updated_at
) VALUES
-- Success case 1 (yesterday)
(gen_random_uuid(), '550e8400-e29b-41d4-a716-446655440001', 'HTTP API Integration', 'COMPLETED',
 CURRENT_TIMESTAMP - INTERVAL '1 day', CURRENT_TIMESTAMP - INTERVAL '1 day' + INTERVAL '2 seconds', 2000,
 3, 3, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
 
-- Success case 2 (2 days ago)
(gen_random_uuid(), '550e8400-e29b-41d4-a716-446655440001', 'HTTP API Integration', 'COMPLETED',
 CURRENT_TIMESTAMP - INTERVAL '2 days', CURRENT_TIMESTAMP - INTERVAL '2 days' + INTERVAL '1.5 seconds', 1500,
 3, 3, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
 
-- Success case 3 (3 days ago)
(gen_random_uuid(), '550e8400-e29b-41d4-a716-446655440001', 'HTTP API Integration', 'COMPLETED',
 CURRENT_TIMESTAMP - INTERVAL '3 days', CURRENT_TIMESTAMP - INTERVAL '3 days' + INTERVAL '1.8 seconds', 1800,
 3, 3, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- HTTP API Journey - Failed execution (API timeout)
INSERT INTO execution_summary (
    execution_id, journey_id, journey_name, status,
    started_at, completed_at, duration_ms,
    total_steps, completed_steps, failed_steps,
    created_at, updated_at
) VALUES
(gen_random_uuid(), '550e8400-e29b-41d4-a716-446655440001', 'HTTP API Integration', 'FAILED',
 CURRENT_TIMESTAMP - INTERVAL '5 hours', CURRENT_TIMESTAMP - INTERVAL '5 hours' + INTERVAL '30 seconds', 30000,
 3, 0, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- HTTP API Journey - Currently running
INSERT INTO execution_summary (
    execution_id, journey_id, journey_name, status,
    started_at, completed_at, duration_ms,
    total_steps, completed_steps, failed_steps,
    created_at, updated_at
) VALUES
(gen_random_uuid(), '550e8400-e29b-41d4-a716-446655440001', 'HTTP API Integration', 'RUNNING',
 CURRENT_TIMESTAMP - INTERVAL '30 seconds', NULL, NULL,
 3, 1, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Email Journey - Successful executions
INSERT INTO execution_summary (
    execution_id, journey_id, journey_name, status,
    started_at, completed_at, duration_ms,
    total_steps, completed_steps, failed_steps,
    created_at, updated_at
) VALUES
-- Success case 1
(gen_random_uuid(), '550e8400-e29b-41d4-a716-446655440002', 'Email Notification Service', 'COMPLETED',
 CURRENT_TIMESTAMP - INTERVAL '6 hours', CURRENT_TIMESTAMP - INTERVAL '6 hours' + INTERVAL '5 seconds', 5000,
 3, 3, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
 
-- Success case 2
(gen_random_uuid(), '550e8400-e29b-41d4-a716-446655440002', 'Email Notification Service', 'COMPLETED',
 CURRENT_TIMESTAMP - INTERVAL '12 hours', CURRENT_TIMESTAMP - INTERVAL '12 hours' + INTERVAL '4.5 seconds', 4500,
 3, 3, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
 
-- Success case 3
(gen_random_uuid(), '550e8400-e29b-41d4-a716-446655440002', 'Email Notification Service', 'COMPLETED',
 CURRENT_TIMESTAMP - INTERVAL '18 hours', CURRENT_TIMESTAMP - INTERVAL '18 hours' + INTERVAL '4.8 seconds', 4800,
 3, 3, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Email Journey - Failed execution (SMTP error)
INSERT INTO execution_summary (
    execution_id, journey_id, journey_name, status,
    started_at, completed_at, duration_ms,
    total_steps, completed_steps, failed_steps,
    created_at, updated_at
) VALUES
(gen_random_uuid(), '550e8400-e29b-41d4-a716-446655440002', 'Email Notification Service', 'FAILED',
 CURRENT_TIMESTAMP - INTERVAL '2 hours', CURRENT_TIMESTAMP - INTERVAL '2 hours' + INTERVAL '10 seconds', 10000,
 3, 2, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Data Pipeline - Successful executions
INSERT INTO execution_summary (
    execution_id, journey_id, journey_name, status,
    started_at, completed_at, duration_ms,
    total_steps, completed_steps, failed_steps,
    created_at, updated_at
) VALUES
-- Success case 1
(gen_random_uuid(), '550e8400-e29b-41d4-a716-446655440003', 'Data Processing Pipeline', 'COMPLETED',
 CURRENT_TIMESTAMP - INTERVAL '1 day', CURRENT_TIMESTAMP - INTERVAL '1 day' + INTERVAL '45 seconds', 45000,
 4, 4, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
 
-- Success case 2
(gen_random_uuid(), '550e8400-e29b-41d4-a716-446655440003', 'Data Processing Pipeline', 'COMPLETED',
 CURRENT_TIMESTAMP - INTERVAL '2 days', CURRENT_TIMESTAMP - INTERVAL '2 days' + INTERVAL '42 seconds', 42000,
 4, 4, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Data Pipeline - Failed execution (enrichment API down)
INSERT INTO execution_summary (
    execution_id, journey_id, journey_name, status,
    started_at, completed_at, duration_ms,
    total_steps, completed_steps, failed_steps,
    created_at, updated_at
) VALUES
(gen_random_uuid(), '550e8400-e29b-41d4-a716-446655440003', 'Data Processing Pipeline', 'FAILED',
 CURRENT_TIMESTAMP - INTERVAL '8 hours', CURRENT_TIMESTAMP - INTERVAL '8 hours' + INTERVAL '25 seconds', 25000,
 4, 2, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Data Pipeline - Multiple older executions for statistics
INSERT INTO execution_summary (
    execution_id, journey_id, journey_name, status,
    started_at, completed_at, duration_ms,
    total_steps, completed_steps, failed_steps,
    created_at, updated_at
)
SELECT 
    gen_random_uuid(),
    '550e8400-e29b-41d4-a716-446655440003',
    'Data Processing Pipeline',
    CASE 
        WHEN random() < 0.85 THEN 'COMPLETED'
        ELSE 'FAILED'
    END,
    CURRENT_TIMESTAMP - (random() * INTERVAL '30 days'),
    CURRENT_TIMESTAMP - (random() * INTERVAL '30 days') + INTERVAL '40 seconds',
    40000 + (random() * 20000)::bigint,
    4,
    CASE WHEN random() < 0.85 THEN 4 ELSE 2 END,
    CASE WHEN random() < 0.85 THEN 0 ELSE 1 END,
    CURRENT_TIMESTAMP, 
    CURRENT_TIMESTAMP
FROM generate_series(1, 20);

-- ============================================
-- Verification Query
-- ============================================

-- Show journey statistics
DO $$
DECLARE
    journey_count INT;
    version_count INT;
    execution_count INT;
BEGIN
    SELECT COUNT(*) INTO journey_count FROM journey_definitions;
    SELECT COUNT(*) INTO version_count FROM journey_versions;
    SELECT COUNT(*) INTO execution_count FROM execution_summary;
    
    RAISE NOTICE 'Test data seeded successfully:';
    RAISE NOTICE '  - Journey Definitions: %', journey_count;
    RAISE NOTICE '  - Journey Versions: %', version_count;
    RAISE NOTICE '  - Execution Records: %', execution_count;
END $$;
