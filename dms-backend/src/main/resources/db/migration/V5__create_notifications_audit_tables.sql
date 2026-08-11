-- ============================================================
-- V5: Notifications and Audit Tables
-- Tables: notifications, notification_preferences, audit_logs
-- ============================================================

CREATE TABLE notifications (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id         BIGINT NOT NULL,
    type            VARCHAR(50) NOT NULL,
    priority        VARCHAR(20) NOT NULL DEFAULT 'MEDIUM',
    title           VARCHAR(255) NOT NULL,
    message         TEXT NOT NULL,
    link            VARCHAR(500) NULL,
    is_read         BOOLEAN NOT NULL DEFAULT FALSE,
    is_email_sent   BOOLEAN NOT NULL DEFAULT FALSE,
    email_sent_at   TIMESTAMP NULL,
    read_at         TIMESTAMP NULL,
    is_active       BOOLEAN NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by      VARCHAR(100) NULL,
    updated_by      VARCHAR(100) NULL,
    CONSTRAINT fk_nt_user FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE INDEX idx_nt_user ON notifications(user_id);
CREATE INDEX idx_nt_user_read ON notifications(user_id, is_read);

CREATE TABLE notification_preferences (
    id                          BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id                     BIGINT NOT NULL UNIQUE,
    email_enabled               BOOLEAN NOT NULL DEFAULT TRUE,
    in_app_enabled              BOOLEAN NOT NULL DEFAULT TRUE,
    approval_required_email     BOOLEAN NULL DEFAULT TRUE,
    approval_required_in_app    BOOLEAN NULL DEFAULT TRUE,
    approved_email              BOOLEAN NULL DEFAULT TRUE,
    approved_in_app             BOOLEAN NULL DEFAULT TRUE,
    rejected_email              BOOLEAN NULL DEFAULT TRUE,
    rejected_in_app             BOOLEAN NULL DEFAULT TRUE,
    commented_email             BOOLEAN NULL DEFAULT TRUE,
    commented_in_app            BOOLEAN NULL DEFAULT TRUE,
    escalated_email             BOOLEAN NULL DEFAULT TRUE,
    escalated_in_app            BOOLEAN NULL DEFAULT TRUE,
    is_active                   BOOLEAN NOT NULL DEFAULT TRUE,
    created_at                  TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at                  TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by                  VARCHAR(100) NULL,
    updated_by                  VARCHAR(100) NULL,
    CONSTRAINT fk_np_user FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE TABLE audit_logs (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id         BIGINT NOT NULL,
    entity_type     VARCHAR(50) NOT NULL,
    entity_id       BIGINT NOT NULL,
    action          VARCHAR(50) NOT NULL,
    old_value       JSON NULL,
    new_value       JSON NULL,
    ip_address      VARCHAR(45) NULL,
    user_agent      TEXT NULL,
    session_id      VARCHAR(100) NULL,
    request_id      VARCHAR(100) NULL,
    created_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_al_user ON audit_logs(user_id);
CREATE INDEX idx_al_entity ON audit_logs(entity_type, entity_id);
CREATE INDEX idx_al_created_at ON audit_logs(created_at);
