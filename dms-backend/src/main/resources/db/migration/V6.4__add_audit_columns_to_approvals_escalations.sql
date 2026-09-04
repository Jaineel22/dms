-- ============================================================
-- V6.4: Add audit columns to `approvals` and `escalations`
--
-- Both tables were created in V4 without the audit columns every
-- other table carries. This aligns them with the rest of the schema.
--
-- Note: the Approval / Escalation JPA entities do not extend
-- BaseEntity, so nothing populates these columns from the app layer
-- yet -- the DB defaults fill them. They exist for schema consistency
-- and future auditing.
--
-- created_by / updated_by are VARCHAR(100) (the actor's email), matching
-- BaseEntity and the fix already applied to other tables in V3.3 / V6 --
-- deliberately NOT BIGINT.
--
-- Guarded on a sentinel column so the migration is safe to re-run.
-- ============================================================

-- ── approvals ──────────────────────────────────────────────
SET @has_cols := (
    SELECT COUNT(*)
    FROM information_schema.columns
    WHERE table_schema = DATABASE()
      AND table_name = 'approvals'
      AND column_name = 'created_at'
);
SET @ddl := IF(@has_cols = 0,
    'ALTER TABLE approvals
        ADD COLUMN created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
        ADD COLUMN updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
        ADD COLUMN created_by VARCHAR(100) NULL,
        ADD COLUMN updated_by VARCHAR(100) NULL,
        ADD COLUMN is_active BOOLEAN NOT NULL DEFAULT TRUE',
    'SELECT 1');
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- ── escalations ────────────────────────────────────────────
SET @has_cols := (
    SELECT COUNT(*)
    FROM information_schema.columns
    WHERE table_schema = DATABASE()
      AND table_name = 'escalations'
      AND column_name = 'created_at'
);
SET @ddl := IF(@has_cols = 0,
    'ALTER TABLE escalations
        ADD COLUMN created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
        ADD COLUMN updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
        ADD COLUMN created_by VARCHAR(100) NULL,
        ADD COLUMN updated_by VARCHAR(100) NULL,
        ADD COLUMN is_active BOOLEAN NOT NULL DEFAULT TRUE',
    'SELECT 1');
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
