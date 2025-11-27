-- Migration V33: Add EXPENSE_SUMMARY permission
-- Description: Add expense summary and analytics permission for SYSTEM_ADMIN, TENANT_ADMIN, OWNER, MANAGER, and INVESTOR roles
-- Author: Claude Code
-- Date: 2025-11-28

-- Add EXPENSE_SUMMARY permission to permissions table
INSERT INTO permissions (id, name, description, resource, action, created_at, updated_at, version)
VALUES
    ('perm-expense-summary', 'EXPENSE_SUMMARY', 'View expense summary and analytics', 'EXPENSE', 'SUMMARY', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0)
ON CONFLICT (name) DO NOTHING;

-- Assign EXPENSE_SUMMARY permission to SYSTEM_ADMIN, TENANT_ADMIN, OWNER, MANAGER, and INVESTOR roles
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r
CROSS JOIN permissions p
WHERE r.name IN ('SYSTEM_ADMIN', 'TENANT_ADMIN', 'OWNER', 'MANAGER', 'INVESTOR')
  AND p.name = 'EXPENSE_SUMMARY'
ON CONFLICT DO NOTHING;
