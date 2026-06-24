-- Seed Roles
INSERT INTO roles (name, description) VALUES 
('ROLE_ADMIN', 'System Administrator - Full Access'),
('ROLE_USER', 'Standard User - Document Creator and Viewer')
ON DUPLICATE KEY UPDATE name = VALUES(name);

-- Seed Departments
INSERT INTO departments (name, code, description) VALUES 
('Information Technology', 'IT', 'IT Department'),
('Human Resources', 'HR', 'Human Resources Department'),
('Finance', 'FIN', 'Finance Department'),
('Legal', 'LEGAL', 'Legal Department')
ON DUPLICATE KEY UPDATE name = VALUES(name);

-- Seed Admin User
-- Password: Admin@123 (BCrypt encoded)
INSERT INTO users (
    first_name, 
    last_name, 
    email, 
    password, 
    employee_id, 
    designation,
    role_id, 
    department_id,
    employee_level,
    is_active
) VALUES (
    'Admin',
    'User',
    'admin@dms.com',
    '$2a$10$tU5MvYYY9hCMCFZ/2tR/GefE0BbYBYhLtN6V2K9tZhVZ6KtZhVZ6K',
    'ADMIN001',
    'System Administrator',
    (SELECT id FROM roles WHERE name = 'ROLE_ADMIN'),
    (SELECT id FROM departments WHERE code = 'IT'),
    4,
    true
) ON DUPLICATE KEY UPDATE email = VALUES(email);

-- Note: For production, please change the admin password immediately
-- The password hash above is for 'Admin@123'