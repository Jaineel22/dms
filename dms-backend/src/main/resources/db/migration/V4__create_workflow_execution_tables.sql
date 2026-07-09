-- ============================================================
-- V4: Workflow Execution Tables
-- Tables: workflow_instances, approvals, comments, escalations
-- ============================================================

CREATE TABLE workflow_instances (
    id                      BIGINT AUTO_INCREMENT PRIMARY KEY,
    document_id             BIGINT NOT NULL,
    workflow_definition_id  BIGINT NOT NULL,
    current_step_id         BIGINT NULL,
    current_approver_id     BIGINT NULL,
    status                  VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    submitted_by            BIGINT NOT NULL,
    submitted_at            TIMESTAMP NOT NULL,
    completed_at            TIMESTAMP NULL,
    deadline_at             TIMESTAMP NULL,
    is_active               BOOLEAN NOT NULL DEFAULT TRUE,
    created_at              TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at              TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by              BIGINT NULL,
    updated_by              BIGINT NULL,
    CONSTRAINT fk_wi_document FOREIGN KEY (document_id) REFERENCES documents(id),
    CONSTRAINT fk_wi_workflow_definition FOREIGN KEY (workflow_definition_id) REFERENCES workflow_definitions(id),
    CONSTRAINT fk_wi_current_step FOREIGN KEY (current_step_id) REFERENCES workflow_steps(id),
    CONSTRAINT fk_wi_current_approver FOREIGN KEY (current_approver_id) REFERENCES users(id),
    CONSTRAINT fk_wi_submitted_by FOREIGN KEY (submitted_by) REFERENCES users(id),
    CONSTRAINT chk_wi_status CHECK (status IN ('PENDING','IN_PROGRESS','APPROVED','REJECTED','EXPIRED'))
);

CREATE INDEX idx_wi_document ON workflow_instances(document_id);
CREATE INDEX idx_wi_current_approver ON workflow_instances(current_approver_id);
CREATE INDEX idx_wi_status ON workflow_instances(status);
CREATE INDEX idx_wi_submitted_by ON workflow_instances(submitted_by);
CREATE INDEX idx_wi_active_deadline ON workflow_instances(is_active, deadline_at);

CREATE TABLE approvals (
    id                      BIGINT AUTO_INCREMENT PRIMARY KEY,
    workflow_instance_id    BIGINT NOT NULL,
    step_id                 BIGINT NOT NULL,
    approver_id             BIGINT NOT NULL,
    action                  VARCHAR(20) NOT NULL,
    comments                VARCHAR(1000) NULL,
    attachment_path         VARCHAR(500) NULL,
    performed_at            TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    is_current              BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT fk_ap_workflow_instance FOREIGN KEY (workflow_instance_id) REFERENCES workflow_instances(id),
    CONSTRAINT fk_ap_step FOREIGN KEY (step_id) REFERENCES workflow_steps(id),
    CONSTRAINT fk_ap_approver FOREIGN KEY (approver_id) REFERENCES users(id),
    CONSTRAINT chk_ap_action CHECK (action IN ('APPROVED','REJECTED','SENT_BACK'))
);

CREATE INDEX idx_ap_workflow_instance ON approvals(workflow_instance_id);
CREATE INDEX idx_ap_approver ON approvals(approver_id);
CREATE INDEX idx_ap_is_current ON approvals(is_current);

CREATE TABLE comments (
    id                      BIGINT AUTO_INCREMENT PRIMARY KEY,
    document_id             BIGINT NOT NULL,
    user_id                 BIGINT NOT NULL,
    parent_comment_id       BIGINT NULL,
    content                 VARCHAR(2000) NOT NULL,
    is_internal             BOOLEAN NOT NULL DEFAULT FALSE,
    created_at              TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at              TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by              BIGINT NULL,
    updated_by              BIGINT NULL,
    CONSTRAINT fk_cm_document FOREIGN KEY (document_id) REFERENCES documents(id),
    CONSTRAINT fk_cm_user FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT fk_cm_parent FOREIGN KEY (parent_comment_id) REFERENCES comments(id)
);

CREATE INDEX idx_cm_document ON comments(document_id);
CREATE INDEX idx_cm_parent ON comments(parent_comment_id);
CREATE INDEX idx_cm_user ON comments(user_id);

CREATE TABLE escalations (
    id                      BIGINT AUTO_INCREMENT PRIMARY KEY,
    workflow_instance_id    BIGINT NOT NULL,
    from_step_id            BIGINT NOT NULL,
    from_user_id            BIGINT NOT NULL,
    to_user_id              BIGINT NOT NULL,
    reason                  VARCHAR(500) NULL,
    escalated_at            TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    resolved_at             TIMESTAMP NULL,
    resolved_action         VARCHAR(20) NULL,
    CONSTRAINT fk_es_workflow_instance FOREIGN KEY (workflow_instance_id) REFERENCES workflow_instances(id),
    CONSTRAINT fk_es_from_step FOREIGN KEY (from_step_id) REFERENCES workflow_steps(id),
    CONSTRAINT fk_es_from_user FOREIGN KEY (from_user_id) REFERENCES users(id),
    CONSTRAINT fk_es_to_user FOREIGN KEY (to_user_id) REFERENCES users(id)
);

CREATE INDEX idx_es_workflow_instance ON escalations(workflow_instance_id);
CREATE INDEX idx_es_to_user_unresolved ON escalations(to_user_id, resolved_at);