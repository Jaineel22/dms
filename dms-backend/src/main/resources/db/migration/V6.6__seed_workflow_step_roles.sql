-- ============================================================
-- V6.6: Seed the ROLE_* rows that WorkflowValidator checks a
-- workflow step's roleName against.
--
-- The three default workflows seeded in V4.2 use step role names
-- MANAGER, FINANCE, LEGAL -- but WorkflowValidator.validateWorkflowSteps()
-- requires a matching ROLE_<name> row in `roles`, which only ever had
-- ROLE_ADMIN / ROLE_USER / ROLE_VIEWER. That made it impossible to create
-- any NEW workflow using the exact step-role vocabulary the app's own
-- seed data (and RoleConstants) already use.
--
-- roles.name has a UNIQUE constraint (V1), so this is idempotent.
-- ============================================================

INSERT INTO roles (name, description, is_active) VALUES
    ('ROLE_MANAGER',  'Manager role for workflow approval steps',  TRUE),
    ('ROLE_FINANCE',  'Finance role for workflow approval steps',  TRUE),
    ('ROLE_LEGAL',    'Legal role for workflow approval steps',    TRUE),
    ('ROLE_HR',       'HR role for workflow approval steps',       TRUE),
    ('ROLE_DIRECTOR', 'Director role for workflow approval steps', TRUE)
ON DUPLICATE KEY UPDATE description = VALUES(description);
