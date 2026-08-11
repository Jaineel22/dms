-- ============================================================
-- V6: Fix created_by/updated_by column types
-- workflow_instances and comments were created with BIGINT
-- created_by/updated_by columns in V4, inconsistent with every
-- other BaseEntity-backed table (VARCHAR(100), matching
-- BaseEntity.createdBy/updatedBy which stores the actor's email).
-- ============================================================

ALTER TABLE workflow_instances
    MODIFY COLUMN created_by VARCHAR(100) NULL,
    MODIFY COLUMN updated_by VARCHAR(100) NULL;

ALTER TABLE comments
    MODIFY COLUMN created_by VARCHAR(100) NULL,
    MODIFY COLUMN updated_by VARCHAR(100) NULL;
