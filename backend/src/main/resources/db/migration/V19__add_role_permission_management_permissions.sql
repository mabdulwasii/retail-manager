-- ========================================
-- Migration V19: Add Role Permission Management Permissions
-- ========================================
-- This migration adds granular permissions for role permission management
-- to support the new RoleController endpoints for managing role-permission assignments.
--
-- New Permissions Added:
-- 1. ROLE_PERMISSION_ADD - Add permissions to roles
-- 2. ROLE_PERMISSION_REMOVE - Remove permissions from roles
--
-- These permissions provide finer-grained control over role management,
-- separating permission management from general role updates.

-- ========================================
-- ADD NEW ROLE PERMISSION MANAGEMENT PERMISSIONS
-- ========================================
INSERT INTO permissions (id, name, description, resource, action, created_at, updated_at, version)
SELECT 'perm-role-permission-add', 'ROLE_PERMISSION_ADD', 'Add permissions to roles', 'ROLE', 'PERMISSION_ADD', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0
WHERE NOT EXISTS (SELECT 1 FROM permissions WHERE name = 'ROLE_PERMISSION_ADD');

INSERT INTO permissions (id, name, description, resource, action, created_at, updated_at, version)
SELECT 'perm-role-permission-remove', 'ROLE_PERMISSION_REMOVE', 'Remove permissions from roles', 'ROLE', 'PERMISSION_REMOVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0
WHERE NOT EXISTS (SELECT 1 FROM permissions WHERE name = 'ROLE_PERMISSION_REMOVE');

-- ========================================
-- GRANT NEW PERMISSIONS TO SYSTEM_ADMIN
-- ========================================
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r, permissions p
WHERE r.name = 'SYSTEM_ADMIN'
AND p.name IN ('ROLE_PERMISSION_ADD', 'ROLE_PERMISSION_REMOVE')
AND NOT EXISTS (
    SELECT 1 FROM role_permissions rp
    WHERE rp.role_id = r.id AND rp.permission_id = p.id
);

-- ========================================
-- GRANT NEW PERMISSIONS TO OWNER
-- ========================================
-- Owner can manage role permissions within their tenant
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r, permissions p
WHERE r.name = 'OWNER'
AND p.name IN ('ROLE_PERMISSION_ADD', 'ROLE_PERMISSION_REMOVE')
AND NOT EXISTS (
    SELECT 1 FROM role_permissions rp
    WHERE rp.role_id = r.id AND rp.permission_id = p.id
);
