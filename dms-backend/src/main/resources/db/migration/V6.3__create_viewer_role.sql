-- ============================================================
-- V6.3: Seed the VIEWER role
--
-- DashboardController gates several read-only endpoints behind
-- hasAnyRole('ADMIN', 'VIEWER') (RoleConstants.HAS_ROLE_ADMIN_OR_VIEWER),
-- but ROLE_VIEWER was never seeded, so those endpoints are effectively
-- ADMIN-only.
--
-- roles.name has a UNIQUE constraint (V1), so ON DUPLICATE KEY UPDATE
-- makes this migration idempotent.
-- ============================================================

INSERT INTO roles (name, description, is_active)
VALUES ('ROLE_VIEWER', 'Read-only access to dashboards and reports', TRUE)
ON DUPLICATE KEY UPDATE
    description = VALUES(description),
    is_active   = TRUE;
