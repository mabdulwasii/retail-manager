-- Migration V28: Fix system roles
-- Description: Mark all default roles as system roles and ensure tenant_id is NULL for system roles
-- Author: Claude Code
-- Date: 2025-11-11

-- Set all default roles as system roles
-- These roles are predefined and should be available to all tenants
UPDATE roles
SET is_system = true, tenant_id = NULL
WHERE name IN (
    'SYSTEM_ADMIN',
    'TENANT_ADMIN',
    'OWNER',
    'MANAGER',
    'EMPLOYEE',
    'INVESTOR',
    'CASHIER',
    'ACCOUNTANT',
    'AUDITOR',
    'CUSTOMER'
);

-- Add comment explaining system roles
COMMENT ON COLUMN roles.is_system IS 'System roles are predefined and available to all tenants. They cannot be modified or deleted via API.';
COMMENT ON COLUMN roles.tenant_id IS 'For custom roles only. System roles have NULL tenant_id. Custom roles belong to a specific tenant.';