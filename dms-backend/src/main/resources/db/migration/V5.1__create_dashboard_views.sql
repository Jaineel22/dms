-- ============================================================
-- Dashboard Views
-- ============================================================

-- Document Statistics View
CREATE OR REPLACE VIEW v_document_stats AS
SELECT
    COUNT(*) AS total_documents,
    SUM(CASE WHEN status = 'DRAFT' THEN 1 ELSE 0 END) AS draft_count,
    SUM(CASE WHEN status = 'UNDER_REVIEW' THEN 1 ELSE 0 END) AS under_review_count,
    SUM(CASE WHEN status = 'APPROVED' THEN 1 ELSE 0 END) AS approved_count,
    SUM(CASE WHEN status = 'REJECTED' THEN 1 ELSE 0 END) AS rejected_count,
    SUM(CASE WHEN is_archived = true THEN 1 ELSE 0 END) AS archived_count,
    COALESCE(SUM(file_size), 0) AS total_storage_bytes
FROM documents;

-- User Activity View
CREATE OR REPLACE VIEW v_user_activity AS
SELECT
    u.id AS user_id,
    u.first_name,
    u.last_name,
    u.email,
    COUNT(DISTINCT d.id) AS documents_uploaded,
    COUNT(DISTINCT wi.id) AS workflows_submitted,
    COUNT(DISTINCT a.id) AS approvals_performed,
    MAX(wi.submitted_at) AS last_activity
FROM users u
LEFT JOIN documents d ON d.owner_id = u.id
LEFT JOIN workflow_instances wi ON wi.submitted_by = u.id
LEFT JOIN approvals a ON a.approver_id = u.id
WHERE u.is_active = true
GROUP BY u.id, u.first_name, u.last_name, u.email;