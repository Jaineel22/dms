-- ============================================================
-- V4.1: Link documents to workflow execution
-- ============================================================

ALTER TABLE documents ADD COLUMN workflow_status VARCHAR(50) NOT NULL DEFAULT 'DRAFT';
ALTER TABLE documents ADD COLUMN workflow_instance_id BIGINT NULL;
ALTER TABLE documents ADD COLUMN approved_at TIMESTAMP NULL;
ALTER TABLE documents ADD COLUMN approved_by BIGINT NULL;

ALTER TABLE documents
    ADD CONSTRAINT fk_doc_workflow_instance FOREIGN KEY (workflow_instance_id) REFERENCES workflow_instances(id);

ALTER TABLE documents
    ADD CONSTRAINT fk_doc_approved_by FOREIGN KEY (approved_by) REFERENCES users(id);

CREATE INDEX idx_doc_workflow_status ON documents(workflow_status);
CREATE INDEX idx_doc_workflow_instance ON documents(workflow_instance_id);