-- ============================================================
-- Phase 3: Document Management Tables
-- ============================================================

-- 1. Document Categories
CREATE TABLE IF NOT EXISTS document_categories (
    id          BIGINT PRIMARY KEY AUTO_INCREMENT,
    name        VARCHAR(100) NOT NULL UNIQUE,
    description TEXT,
    is_active   BOOLEAN   DEFAULT TRUE,
    created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by  VARCHAR(100),
    updated_by  VARCHAR(100),
    INDEX idx_category_active (is_active)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 2. Documents
CREATE TABLE IF NOT EXISTS documents (
    id              BIGINT PRIMARY KEY AUTO_INCREMENT,
    document_number VARCHAR(50)  NOT NULL UNIQUE,
    title           VARCHAR(255) NOT NULL,
    description     TEXT,
    department_id   BIGINT,
    category_id     BIGINT,
    owner_id        BIGINT       NOT NULL,
    current_version INT          NOT NULL DEFAULT 1,
    status          VARCHAR(50)  NOT NULL DEFAULT 'DRAFT',
    file_path       VARCHAR(500) NOT NULL,
    file_name       VARCHAR(255) NOT NULL,
    file_size       BIGINT,
    mime_type       VARCHAR(100),
    is_confidential BOOLEAN   DEFAULT FALSE,
    expiry_date     DATE,
    tags            VARCHAR(500),
    is_archived     BOOLEAN   DEFAULT FALSE,
    archived_at     TIMESTAMP NULL,
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by      VARCHAR(100),
    updated_by      VARCHAR(100),

    FOREIGN KEY (department_id) REFERENCES departments(id)          ON DELETE SET NULL,
    FOREIGN KEY (category_id)   REFERENCES document_categories(id)  ON DELETE SET NULL,
    FOREIGN KEY (owner_id)      REFERENCES users(id),

    INDEX idx_document_number     (document_number),
    INDEX idx_document_status     (status),
    INDEX idx_document_owner      (owner_id),
    INDEX idx_document_category   (category_id),
    INDEX idx_document_department (department_id),
    INDEX idx_document_archived   (is_archived)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 3. Document Versions
CREATE TABLE IF NOT EXISTS document_versions (
    id             BIGINT PRIMARY KEY AUTO_INCREMENT,
    document_id    BIGINT       NOT NULL,
    version_number INT          NOT NULL,
    file_path      VARCHAR(500) NOT NULL,
    file_name      VARCHAR(255) NOT NULL,
    file_size      BIGINT,
    uploaded_by    BIGINT       NOT NULL,
    upload_reason  VARCHAR(255),
    created_at     TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    FOREIGN KEY (document_id) REFERENCES documents(id) ON DELETE CASCADE,
    FOREIGN KEY (uploaded_by) REFERENCES users(id),

    UNIQUE KEY uk_document_version     (document_id, version_number),
    INDEX      idx_version_document    (document_id),
    INDEX      idx_version_uploaded_by (uploaded_by)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;