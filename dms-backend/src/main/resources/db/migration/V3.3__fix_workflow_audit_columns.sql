ALTER TABLE workflow_steps ADD COLUMN created_by VARCHAR(100);
ALTER TABLE workflow_steps ADD COLUMN updated_by VARCHAR(100);
ALTER TABLE workflow_steps ADD COLUMN is_active BOOLEAN NOT NULL DEFAULT TRUE;