-- ============================================================
-- Phase 2: Hierarchy & Workflow Foundation Tables
-- ============================================================

-- 1. Workflow Definitions
CREATE TABLE IF NOT EXISTS workflow_definitions (
    id          BIGINT PRIMARY KEY AUTO_INCREMENT,
    name        VARCHAR(100) NOT NULL,
    description TEXT,
    department_id BIGINT,
    is_active   BOOLEAN      DEFAULT TRUE,
    created_at  TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP    DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by  VARCHAR(100),
    updated_by  VARCHAR(100),

    FOREIGN KEY (department_id) REFERENCES departments(id) ON DELETE CASCADE,
    INDEX idx_workflow_department (department_id),
    INDEX idx_workflow_active     (is_active)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 2. Workflow Steps
CREATE TABLE IF NOT EXISTS workflow_steps (
    id             BIGINT PRIMARY KEY AUTO_INCREMENT,
    workflow_id    BIGINT  NOT NULL,
    step_number    INT     NOT NULL,
    approval_level INT     NOT NULL,
    role_name      VARCHAR(50) NOT NULL,
    is_mandatory   BOOLEAN DEFAULT TRUE,
    timeout_hours  INT     DEFAULT 24,
    created_at     TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at     TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    FOREIGN KEY (workflow_id) REFERENCES workflow_definitions(id) ON DELETE CASCADE,
    INDEX idx_workflow_steps (workflow_id, step_number),
    INDEX idx_workflow_level (approval_level),
    UNIQUE KEY uk_workflow_step (workflow_id, step_number)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 3. Hierarchy History (Audit for manager changes)
CREATE TABLE IF NOT EXISTS hierarchy_history (
    id                  BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id             BIGINT       NOT NULL,
    previous_manager_id BIGINT,
    new_manager_id      BIGINT,
    changed_by          BIGINT       NOT NULL,
    change_reason       VARCHAR(255),
    is_active           BOOLEAN      DEFAULT TRUE,
    created_at          TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP    DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by          VARCHAR(100),
    updated_by          VARCHAR(100),

    FOREIGN KEY (user_id)    REFERENCES users(id),
    FOREIGN KEY (changed_by) REFERENCES users(id),
    INDEX idx_hierarchy_user       (user_id),
    INDEX idx_hierarchy_changed_by (changed_by)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 4. User-Workflow Mapping
CREATE TABLE IF NOT EXISTS user_workflows (
    id          BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id     BIGINT    NOT NULL,
    workflow_id BIGINT    NOT NULL,
    is_active   BOOLEAN   DEFAULT TRUE,
    assigned_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    assigned_by BIGINT,
    created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by  VARCHAR(100),
    updated_by  VARCHAR(100),

    FOREIGN KEY (user_id)     REFERENCES users(id)                ON DELETE CASCADE,
    FOREIGN KEY (workflow_id) REFERENCES workflow_definitions(id) ON DELETE CASCADE,
    FOREIGN KEY (assigned_by) REFERENCES users(id),

    UNIQUE KEY uk_user_workflow  (user_id, workflow_id),
    INDEX idx_user_workflow_user   (user_id),
    INDEX idx_user_workflow_active (is_active)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;