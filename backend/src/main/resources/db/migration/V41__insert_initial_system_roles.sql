-- Migration V41: Insert initial system roles if they don't exist
-- Description: Ensures all system roles exist for embedded deployments where no initial data exists
-- Author: Claude Code
-- Date: 2025-12-25

-- Insert SYSTEM_ADMIN role (was SUPER_ADMIN in older versions)
INSERT INTO roles (id, name, description, is_system, tenant_id, created_at, updated_at, version)
SELECT
    'system-admin-role-id',
    'SYSTEM_ADMIN',
    'System Administrator with full platform access across all tenants',
    true,
    NULL,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP,
    0
WHERE NOT EXISTS (SELECT 1 FROM roles WHERE name = 'SYSTEM_ADMIN');

-- Insert TENANT_ADMIN role
INSERT INTO roles (id, name, description, is_system, tenant_id, created_at, updated_at, version)
SELECT
    'tenant-admin-role-id',
    'TENANT_ADMIN',
    'Tenant Administrator with full access within their tenant',
    true,
    NULL,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP,
    0
WHERE NOT EXISTS (SELECT 1 FROM roles WHERE name = 'TENANT_ADMIN');

-- Insert OWNER role
INSERT INTO roles (id, name, description, is_system, tenant_id, created_at, updated_at, version)
SELECT
    'owner-role-id',
    'OWNER',
    'Shop Owner with full business control and financial oversight',
    true,
    NULL,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP,
    0
WHERE NOT EXISTS (SELECT 1 FROM roles WHERE name = 'OWNER');

-- Insert MANAGER role
INSERT INTO roles (id, name, description, is_system, tenant_id, created_at, updated_at, version)
SELECT
    'manager-role-id',
    'MANAGER',
    'Shop Manager with operational control',
    true,
    NULL,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP,
    0
WHERE NOT EXISTS (SELECT 1 FROM roles WHERE name = 'MANAGER');

-- Insert EMPLOYEE role
INSERT INTO roles (id, name, description, is_system, tenant_id, created_at, updated_at, version)
SELECT
    'employee-role-id',
    'EMPLOYEE',
    'Shop Employee with limited access',
    true,
    NULL,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP,
    0
WHERE NOT EXISTS (SELECT 1 FROM roles WHERE name = 'EMPLOYEE');

-- Insert INVESTOR role
INSERT INTO roles (id, name, description, is_system, tenant_id, created_at, updated_at, version)
SELECT
    'investor-role-id',
    'INVESTOR',
    'Investor with access to investment tracking and reports',
    true,
    NULL,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP,
    0
WHERE NOT EXISTS (SELECT 1 FROM roles WHERE name = 'INVESTOR');

-- Insert CASHIER role
INSERT INTO roles (id, name, description, is_system, tenant_id, created_at, updated_at, version)
SELECT
    'cashier-role-id',
    'CASHIER',
    'Cashier with access to sales and basic inventory operations',
    true,
    NULL,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP,
    0
WHERE NOT EXISTS (SELECT 1 FROM roles WHERE name = 'CASHIER');

-- Insert ACCOUNTANT role
INSERT INTO roles (id, name, description, is_system, tenant_id, created_at, updated_at, version)
SELECT
    'accountant-role-id',
    'ACCOUNTANT',
    'Accountant with access to financial reports and analytics',
    true,
    NULL,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP,
    0
WHERE NOT EXISTS (SELECT 1 FROM roles WHERE name = 'ACCOUNTANT');

-- Insert AUDITOR role
INSERT INTO roles (id, name, description, is_system, tenant_id, created_at, updated_at, version)
SELECT
    'auditor-role-id',
    'AUDITOR',
    'Auditor with read-only access for compliance and verification',
    true,
    NULL,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP,
    0
WHERE NOT EXISTS (SELECT 1 FROM roles WHERE name = 'AUDITOR');

-- Insert CUSTOMER role
INSERT INTO roles (id, name, description, is_system, tenant_id, created_at, updated_at, version)
SELECT
    'customer-role-id',
    'CUSTOMER',
    'Customer with access to receipts and purchase history',
    true,
    NULL,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP,
    0
WHERE NOT EXISTS (SELECT 1 FROM roles WHERE name = 'CUSTOMER');
