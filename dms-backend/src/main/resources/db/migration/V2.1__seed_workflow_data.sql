-- ============================================================
-- Phase 2: Seed Default Workflows and Steps
-- ============================================================

-- ── Purchase Approval Workflow ────────────────────────────────────────────────
INSERT INTO workflow_definitions (name, description, is_active, created_by) VALUES
('Purchase Approval', 'Standard purchase request approval workflow', TRUE, 'SYSTEM');

-- Steps: Employee → Team Lead → Manager → Director
INSERT INTO workflow_steps (workflow_id, step_number, approval_level, role_name, is_mandatory, timeout_hours)
VALUES
(
    (SELECT id FROM workflow_definitions WHERE name = 'Purchase Approval'),
    1, 1, 'USER', TRUE, 24
),
(
    (SELECT id FROM workflow_definitions WHERE name = 'Purchase Approval'),
    2, 2, 'USER', TRUE, 24
),
(
    (SELECT id FROM workflow_definitions WHERE name = 'Purchase Approval'),
    3, 3, 'USER', TRUE, 24
),
(
    (SELECT id FROM workflow_definitions WHERE name = 'Purchase Approval'),
    4, 4, 'USER', TRUE, 24
);

-- ── Leave Approval Workflow ───────────────────────────────────────────────────
INSERT INTO workflow_definitions (name, description, is_active, created_by) VALUES
('Leave Approval', 'Leave request approval workflow', TRUE, 'SYSTEM');

-- Steps: Employee → Team Lead → Manager (optional Director)
INSERT INTO workflow_steps (workflow_id, step_number, approval_level, role_name, is_mandatory, timeout_hours)
VALUES
(
    (SELECT id FROM workflow_definitions WHERE name = 'Leave Approval'),
    1, 1, 'USER', TRUE, 24
),
(
    (SELECT id FROM workflow_definitions WHERE name = 'Leave Approval'),
    2, 2, 'USER', TRUE, 24
),
(
    (SELECT id FROM workflow_definitions WHERE name = 'Leave Approval'),
    3, 3, 'USER', FALSE, 48
);