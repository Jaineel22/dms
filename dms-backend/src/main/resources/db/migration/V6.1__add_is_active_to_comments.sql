-- ============================================================
-- V6.1: comments was created in V4 without is_active, but the
-- Comment entity extends BaseEntity, which requires it.
-- ============================================================

ALTER TABLE comments
    ADD COLUMN is_active BOOLEAN NOT NULL DEFAULT TRUE AFTER is_internal;
