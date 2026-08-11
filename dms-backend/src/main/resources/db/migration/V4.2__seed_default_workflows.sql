-- ============================================================
-- V4.2: Seed default workflow definitions + steps
-- Assumes workflow_definitions has a UNIQUE constraint on `name`
-- and workflow_steps has a UNIQUE constraint on
-- (workflow_definition_id, step_number), as established in Phase 2.
-- ============================================================

INSERT INTO workflow_definitions (name, description, is_active, created_at, updated_at)
VALUES
    ('Purchase Approval', 'Approval workflow for purchase related documents', TRUE, NOW(), NOW()),
    ('Leave Approval', 'Approval workflow for leave requests', TRUE, NOW(), NOW()),
    ('Contract Approval', 'Approval workflow for contract documents', TRUE, NOW(), NOW())
ON DUPLICATE KEY UPDATE
    description = VALUES(description),
    updated_at = NOW();

-- Purchase Approval: Manager -> Finance
INSERT INTO workflow_steps (workflow_id, step_number, approval_level, role_name, timeout_hours, created_at, updated_at)
SELECT id, 1, 1, 'MANAGER', 48, NOW(), NOW() FROM workflow_definitions WHERE name = 'Purchase Approval'
ON DUPLICATE KEY UPDATE updated_at = NOW();

INSERT INTO workflow_steps (workflow_id, step_number, approval_level, role_name, timeout_hours, created_at, updated_at)
SELECT id, 2, 2, 'FINANCE', 48, NOW(), NOW() FROM workflow_definitions WHERE name = 'Purchase Approval'
ON DUPLICATE KEY UPDATE updated_at = NOW();

-- Leave Approval: Manager only
INSERT INTO workflow_steps (workflow_id, step_number, approval_level, role_name, timeout_hours, created_at, updated_at)
SELECT id, 1, 1, 'MANAGER', 24, NOW(), NOW() FROM workflow_definitions WHERE name = 'Leave Approval'
ON DUPLICATE KEY UPDATE updated_at = NOW();

-- Contract Approval: Manager -> Legal -> Admin
INSERT INTO workflow_steps (workflow_id, step_number, approval_level, role_name, timeout_hours, created_at, updated_at)
SELECT id, 1, 1, 'MANAGER', 48, NOW(), NOW() FROM workflow_definitions WHERE name = 'Contract Approval'
ON DUPLICATE KEY UPDATE updated_at = NOW();

INSERT INTO workflow_steps (workflow_id, step_number, approval_level, role_name, timeout_hours, created_at, updated_at)
SELECT id, 2, 2, 'LEGAL', 72, NOW(), NOW() FROM workflow_definitions WHERE name = 'Contract Approval'
ON DUPLICATE KEY UPDATE updated_at = NOW();

INSERT INTO workflow_steps (workflow_id, step_number, approval_level, role_name, timeout_hours, created_at, updated_at)
SELECT id, 3, 4, 'ADMIN', 48, NOW(), NOW() FROM workflow_definitions WHERE name = 'Contract Approval'
ON DUPLICATE KEY UPDATE updated_at = NOW();