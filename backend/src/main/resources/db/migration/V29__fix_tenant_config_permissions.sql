-- Migration V29: Fix tenant configuration permissions
-- Description: Fix V27 migration error - use correct column names for permissions table
-- Author: Claude Code
-- Date: 2025-11-11

-- Delete any incorrectly created permissions from V27 (if they somehow got created)
DELETE FROM permissions WHERE name IN ('TENANT_CONFIG_READ', 'TENANT_CONFIG_CREATE', 'TENANT_CONFIG_UPDATE', 'TENANT_CONFIG_DELETE');

-- Add configuration permissions to permissions table with CORRECT column names
-- Permissions table has 'resource' and 'action' columns, not 'resource_type'
INSERT INTO permissions (id, name, description, resource, action, created_at, updated_at, version)
VALUES
    ('perm-tenant-config-read', 'TENANT_CONFIG_READ', 'View tenant configuration settings', 'TENANT_CONFIG', 'READ', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    ('perm-tenant-config-create', 'TENANT_CONFIG_CREATE', 'Create tenant configuration settings', 'TENANT_CONFIG', 'CREATE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    ('perm-tenant-config-update', 'TENANT_CONFIG_UPDATE', 'Update tenant configuration settings', 'TENANT_CONFIG', 'UPDATE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    ('perm-tenant-config-delete', 'TENANT_CONFIG_DELETE', 'Delete tenant configuration settings', 'TENANT_CONFIG', 'DELETE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0)
ON CONFLICT (name) DO NOTHING;

-- Assign configuration permissions to appropriate roles
-- SYSTEM_ADMIN gets all permissions
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r
CROSS JOIN permissions p
WHERE r.name = 'SYSTEM_ADMIN'
  AND p.name IN ('TENANT_CONFIG_READ', 'TENANT_CONFIG_CREATE', 'TENANT_CONFIG_UPDATE', 'TENANT_CONFIG_DELETE')
ON CONFLICT DO NOTHING;

-- TENANT_ADMIN gets all configuration permissions
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r
CROSS JOIN permissions p
WHERE r.name = 'TENANT_ADMIN'
  AND p.name IN ('TENANT_CONFIG_READ', 'TENANT_CONFIG_CREATE', 'TENANT_CONFIG_UPDATE', 'TENANT_CONFIG_DELETE')
ON CONFLICT DO NOTHING;

-- OWNER gets read, create, and update permissions
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r
CROSS JOIN permissions p
WHERE r.name = 'OWNER'
  AND p.name IN ('TENANT_CONFIG_READ', 'TENANT_CONFIG_CREATE', 'TENANT_CONFIG_UPDATE')
ON CONFLICT DO NOTHING;

-- MANAGER gets read permission only
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r
CROSS JOIN permissions p
WHERE r.name = 'MANAGER'
  AND p.name = 'TENANT_CONFIG_READ'
ON CONFLICT DO NOTHING;
