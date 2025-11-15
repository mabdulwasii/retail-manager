-- Migration V30: Add USER_LIST_ALL permission
-- Description: Add system-wide user listing permission for SYSTEM_ADMIN role
-- Author: Claude Code
-- Date: 2025-11-15

-- Add USER_LIST_ALL permission to permissions table
INSERT INTO permissions (id, name, description, resource, action, created_at, updated_at, version)
VALUES
    ('perm-user-list-all', 'USER_LIST_ALL', 'List all users across all tenants in the system', 'USER', 'LIST_ALL', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0)
ON CONFLICT (name) DO NOTHING;

-- Assign USER_LIST_ALL permission to SYSTEM_ADMIN role only
-- This is a system-wide permission, not tenant-scoped
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r
CROSS JOIN permissions p
WHERE r.name = 'SYSTEM_ADMIN'
  AND p.name = 'USER_LIST_ALL'
ON CONFLICT DO NOTHING;
