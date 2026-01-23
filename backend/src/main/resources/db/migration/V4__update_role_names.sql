-- Migration to update role names to match new naming convention
-- SUPER_ADMIN -> SYSTEM_ADMIN
-- SHOP_MANAGER -> MANAGER
-- SHOP_EMPLOYEE -> EMPLOYEE
-- Add missing roles: OWNER, CASHIER, ACCOUNTANT, AUDITOR, CUSTOMER

-- Update existing role names
UPDATE roles SET name = 'SYSTEM_ADMIN' WHERE name = 'SUPER_ADMIN';
UPDATE roles SET name = 'MANAGER' WHERE name = 'SHOP_MANAGER';
UPDATE roles SET name = 'EMPLOYEE' WHERE name = 'SHOP_EMPLOYEE';

-- Insert missing roles if they don't exist
INSERT INTO roles (id, name, description, is_system, tenant_id, created_at, updated_at, version)
SELECT
    'owner-role-id',
    'OWNER',
    'Shop Owner with full business control and financial oversight',
    true,
    null,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP,
    0
WHERE NOT EXISTS (SELECT 1 FROM roles WHERE name = 'OWNER');

INSERT INTO roles (id, name, description, is_system, tenant_id, created_at, updated_at, version)
SELECT
    'cashier-role-id',
    'CASHIER',
    'Cashier with access to sales and basic inventory operations',
    true,
    null,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP,
    0
WHERE NOT EXISTS (SELECT 1 FROM roles WHERE name = 'CASHIER');

INSERT INTO roles (id, name, description, is_system, tenant_id, created_at, updated_at, version)
SELECT
    'accountant-role-id',
    'ACCOUNTANT',
    'Accountant with access to financial reports and analytics',
    true,
    null,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP,
    0
WHERE NOT EXISTS (SELECT 1 FROM roles WHERE name = 'ACCOUNTANT');

INSERT INTO roles (id, name, description, is_system, tenant_id, created_at, updated_at, version)
SELECT
    'auditor-role-id',
    'AUDITOR',
    'Auditor with read-only access for compliance and verification',
    true,
    null,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP,
    0
WHERE NOT EXISTS (SELECT 1 FROM roles WHERE name = 'AUDITOR');

INSERT INTO roles (id, name, description, is_system, tenant_id, created_at, updated_at, version)
SELECT
    'customer-role-id',
    'CUSTOMER',
    'Customer with access to receipts and purchase history',
    true,
    null,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP,
    0
WHERE NOT EXISTS (SELECT 1 FROM roles WHERE name = 'CUSTOMER');
