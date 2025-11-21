-- ========================================
-- Migration V14: Add TENANT Permissions to SYSTEM_ADMIN Role
-- ========================================
-- This migration grants all TENANT_* permissions to the SYSTEM_ADMIN role.
-- These permissions were missing from V12/V13 migrations.
--
-- Fixes:
-- - SYSTEM_ADMIN can now manage tenants (TENANT_CREATE, TENANT_READ, TENANT_LIST, TENANT_UPDATE, TENANT_DELETE)
-- - Aligns with TenantAdminController permission requirements
-- - Enables realistic testing with proper role-permission mappings

-- ========================================
-- GRANT TENANT PERMISSIONS TO SYSTEM_ADMIN
-- ========================================
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r, permissions p
WHERE r.name = 'SYSTEM_ADMIN'
AND p.name IN (
    'TENANT_CREATE',
    'TENANT_READ',
    'TENANT_LIST',
    'TENANT_UPDATE',
    'TENANT_DELETE'
)
AND NOT EXISTS (
    SELECT 1 FROM role_permissions rp
    WHERE rp.role_id = r.id AND rp.permission_id = p.id
);

-- ========================================
-- Verification Query (optional - for manual testing)
-- ========================================
-- SELECT
--     r.name as role_name,
--     p.name as permission_name,
--     p.description
-- FROM roles r
-- JOIN role_permissions rp ON r.id = rp.role_id
-- JOIN permissions p ON rp.permission_id = p.id
-- WHERE r.name = 'SYSTEM_ADMIN'
-- AND p.name LIKE 'TENANT_%'
-- ORDER BY p.name;
