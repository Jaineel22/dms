-- ============================================================
-- Seed Notification Preferences for existing users
-- ============================================================

INSERT INTO notification_preferences (user_id, email_enabled, in_app_enabled, created_at, updated_at)
SELECT id, true, true, NOW(), NOW() FROM users u
WHERE NOT EXISTS (
    SELECT 1 FROM notification_preferences np WHERE np.user_id = u.id
);