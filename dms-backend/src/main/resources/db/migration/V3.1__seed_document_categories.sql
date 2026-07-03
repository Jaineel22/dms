-- ============================================================
-- Phase 3: Seed Default Document Categories
-- ============================================================

INSERT INTO document_categories (name, description, is_active, created_by)
VALUES
    ('Purchase', 'Purchase requests and orders',        TRUE, 'SYSTEM'),
    ('Invoice',  'Invoices and billing documents',      TRUE, 'SYSTEM'),
    ('HR',       'Human Resources documents',           TRUE, 'SYSTEM'),
    ('Finance',  'Financial reports and statements',    TRUE, 'SYSTEM'),
    ('Legal',    'Legal agreements and contracts',      TRUE, 'SYSTEM'),
    ('General',  'General documents',                   TRUE, 'SYSTEM')
ON DUPLICATE KEY UPDATE name = VALUES(name);