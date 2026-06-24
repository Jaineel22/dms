-- V1__create_initial_schema.sql
-- DMS Phase 1 - Initial Schema
-- Character Set: UTF8MB4

CREATE TABLE roles (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(50) UNIQUE NOT NULL,
    description VARCHAR(255),
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE departments (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) UNIQUE NOT NULL,
    code VARCHAR(10) UNIQUE NOT NULL,
    description TEXT,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    INDEX idx_department_code (code),
    INDEX idx_department_name (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE users (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    first_name VARCHAR(50) NOT NULL,
    last_name VARCHAR(50) NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    employee_id VARCHAR(20) UNIQUE NOT NULL,
    designation VARCHAR(100),
    phone_number VARCHAR(20),
    profile_image VARCHAR(255),
    is_active BOOLEAN DEFAULT TRUE,
    last_login_at TIMESTAMP NULL,
    password_changed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    login_attempts INT DEFAULT 0,
    locked_until TIMESTAMP NULL,
    manager_id BIGINT NULL,
    employee_level INT DEFAULT 1,
    role_id BIGINT NOT NULL,
    department_id BIGINT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100),

    FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE RESTRICT,
    FOREIGN KEY (department_id) REFERENCES departments(id) ON DELETE RESTRICT,
    FOREIGN KEY (manager_id) REFERENCES users(id) ON DELETE SET NULL,

    INDEX idx_user_email (email),
    INDEX idx_user_employee_id (employee_id),
    INDEX idx_user_role (role_id),
    INDEX idx_user_department (department_id),
    INDEX idx_user_active (is_active),
    INDEX idx_user_manager (manager_id),
    INDEX idx_user_level (employee_level),

    CONSTRAINT uk_user_email UNIQUE (email),
    CONSTRAINT uk_user_employee_id UNIQUE (employee_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Views for reporting
CREATE VIEW v_user_department_role AS
SELECT
    u.id,
    u.first_name,
    u.last_name,
    u.email,
    u.employee_id,
    u.designation,
    u.is_active,
    u.last_login_at,
    u.employee_level,
    u.manager_id,
    r.name AS role_name,
    d.name AS department_name,
    d.code AS department_code
FROM users u
JOIN roles r ON u.role_id = r.id
JOIN departments d ON u.department_id = d.id
WHERE u.is_active = TRUE;