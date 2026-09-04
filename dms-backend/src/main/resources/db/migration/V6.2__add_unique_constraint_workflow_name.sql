-- ============================================================
-- V6.2: Enforce a UNIQUE constraint on workflow_definitions.name
--
-- V4.2 seeds workflow definitions with "ON DUPLICATE KEY UPDATE",
-- assuming `name` is unique -- but V2 created the column without a
-- UNIQUE constraint, so re-running the seed (or manual inserts)
-- accumulates duplicate rows.
--
-- This migration de-duplicates the table (keeping the newest row per
-- name), re-points child records to the surviving row, then adds the
-- constraint. Guarded so it is safe to run more than once.
-- ============================================================

-- Step 1: re-point workflow instances from duplicate definitions to the
--         surviving (highest-id) definition with the same name.
UPDATE workflow_instances wi
JOIN workflow_definitions dup ON wi.workflow_definition_id = dup.id
JOIN (
    SELECT name, MAX(id) AS keep_id
    FROM workflow_definitions
    GROUP BY name
) keep ON keep.name = dup.name
SET wi.workflow_definition_id = keep.keep_id
WHERE dup.id <> keep.keep_id;

-- Step 2: re-point user->workflow assignments the same way.
UPDATE user_workflows uw
JOIN workflow_definitions dup ON uw.workflow_id = dup.id
JOIN (
    SELECT name, MAX(id) AS keep_id
    FROM workflow_definitions
    GROUP BY name
) keep ON keep.name = dup.name
SET uw.workflow_id = keep.keep_id
WHERE dup.id <> keep.keep_id;

-- Step 3: delete the duplicate definitions. Their workflow_steps rows are
--         removed automatically by the ON DELETE CASCADE from V2.
DELETE wd FROM workflow_definitions wd
JOIN (
    SELECT name, MAX(id) AS keep_id
    FROM workflow_definitions
    GROUP BY name
) keep ON keep.name = wd.name
WHERE wd.id <> keep.keep_id;

-- Step 4: add the UNIQUE constraint only if it is not already present.
SET @constraint_exists := (
    SELECT COUNT(*)
    FROM information_schema.table_constraints
    WHERE table_schema = DATABASE()
      AND table_name = 'workflow_definitions'
      AND constraint_name = 'uk_workflow_name'
);
SET @ddl := IF(@constraint_exists = 0,
    'ALTER TABLE workflow_definitions ADD CONSTRAINT uk_workflow_name UNIQUE (name)',
    'SELECT 1');
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
