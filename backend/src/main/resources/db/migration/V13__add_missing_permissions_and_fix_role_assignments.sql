-- ========================================
-- Migration V13: Add Missing Permissions and Fix Role Assignments
-- ========================================
-- This migration adds all missing permission constants and updates MANAGER role
-- with permissions that were missing from V12.
--
-- Issues Fixed:
-- 1. Add 19 missing permission constants used in controllers
-- 2. Replace old ANALYTICS_VIEW_SHOP/TENANT with feature-based permissions
-- 3. Grant missing permissions to MANAGER role (SHOP_LIST, ROLE_LIST, ROLE_READ, etc.)

-- ========================================
-- SHOP PERMISSIONS (additions)
-- ========================================
INSERT INTO permissions (id, name, description, resource, action, created_at, updated_at, version)
SELECT 'perm-shop-list-all', 'SHOP_LIST_ALL', 'List all shops across tenant', 'SHOP', 'LIST_ALL', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0
WHERE NOT EXISTS (SELECT 1 FROM permissions WHERE name = 'SHOP_LIST_ALL');

-- ========================================
-- ROLE PERMISSIONS (additions)
-- ========================================
INSERT INTO permissions (id, name, description, resource, action, created_at, updated_at, version)
SELECT 'perm-role-assign', 'ROLE_ASSIGN', 'Assign roles to users', 'ROLE', 'ASSIGN', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0
WHERE NOT EXISTS (SELECT 1 FROM permissions WHERE name = 'ROLE_ASSIGN');

-- ========================================
-- INVENTORY PERMISSIONS (additions)
-- ========================================
INSERT INTO permissions (id, name, description, resource, action, created_at, updated_at, version)
SELECT 'perm-inventory-adjust', 'INVENTORY_ADJUST', 'Adjust inventory stock levels', 'INVENTORY', 'ADJUST', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0
WHERE NOT EXISTS (SELECT 1 FROM permissions WHERE name = 'INVENTORY_ADJUST');

INSERT INTO permissions (id, name, description, resource, action, created_at, updated_at, version)
SELECT 'perm-inventory-reserve', 'INVENTORY_RESERVE', 'Reserve inventory stock', 'INVENTORY', 'RESERVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0
WHERE NOT EXISTS (SELECT 1 FROM permissions WHERE name = 'INVENTORY_RESERVE');

INSERT INTO permissions (id, name, description, resource, action, created_at, updated_at, version)
SELECT 'perm-inventory-history', 'INVENTORY_HISTORY', 'View inventory history', 'INVENTORY', 'HISTORY', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0
WHERE NOT EXISTS (SELECT 1 FROM permissions WHERE name = 'INVENTORY_HISTORY');

INSERT INTO permissions (id, name, description, resource, action, created_at, updated_at, version)
SELECT 'perm-inventory-forecast', 'INVENTORY_FORECAST', 'View inventory forecasts', 'INVENTORY', 'FORECAST', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0
WHERE NOT EXISTS (SELECT 1 FROM permissions WHERE name = 'INVENTORY_FORECAST');

-- ========================================
-- SALES PERMISSIONS (additions)
-- ========================================
INSERT INTO permissions (id, name, description, resource, action, created_at, updated_at, version)
SELECT 'perm-sales-void', 'SALES_VOID', 'Void sales transactions', 'SALES', 'VOID', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0
WHERE NOT EXISTS (SELECT 1 FROM permissions WHERE name = 'SALES_VOID');

-- ========================================
-- RECEIPT PERMISSIONS (additions)
-- ========================================
INSERT INTO permissions (id, name, description, resource, action, created_at, updated_at, version)
SELECT 'perm-receipt-email', 'RECEIPT_EMAIL', 'Mark receipts as emailed', 'RECEIPT', 'EMAIL', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0
WHERE NOT EXISTS (SELECT 1 FROM permissions WHERE name = 'RECEIPT_EMAIL');

-- ========================================
-- EXPENSE PERMISSIONS (additions)
-- ========================================
INSERT INTO permissions (id, name, description, resource, action, created_at, updated_at, version)
SELECT 'perm-expense-approve', 'EXPENSE_APPROVE', 'Approve or reject expenses', 'EXPENSE', 'APPROVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0
WHERE NOT EXISTS (SELECT 1 FROM permissions WHERE name = 'EXPENSE_APPROVE');

-- ========================================
-- INVESTMENT PERMISSIONS (additions)
-- ========================================
INSERT INTO permissions (id, name, description, resource, action, created_at, updated_at, version)
SELECT 'perm-investment-close', 'INVESTMENT_CLOSE', 'Close investments', 'INVESTMENT', 'CLOSE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0
WHERE NOT EXISTS (SELECT 1 FROM permissions WHERE name = 'INVESTMENT_CLOSE');

INSERT INTO permissions (id, name, description, resource, action, created_at, updated_at, version)
SELECT 'perm-investment-profit-distribute', 'INVESTMENT_PROFIT_DISTRIBUTE', 'Distribute investment profits', 'INVESTMENT', 'PROFIT_DISTRIBUTE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0
WHERE NOT EXISTS (SELECT 1 FROM permissions WHERE name = 'INVESTMENT_PROFIT_DISTRIBUTE');

-- ========================================
-- RETURN PERMISSIONS (additions)
-- ========================================
INSERT INTO permissions (id, name, description, resource, action, created_at, updated_at, version)
SELECT 'perm-return-approve', 'RETURN_APPROVE', 'Approve product returns', 'RETURN', 'APPROVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0
WHERE NOT EXISTS (SELECT 1 FROM permissions WHERE name = 'RETURN_APPROVE');

-- ========================================
-- ANALYTICS PERMISSIONS (Feature-based replacements)
-- ========================================
INSERT INTO permissions (id, name, description, resource, action, created_at, updated_at, version)
SELECT 'perm-analytics-sales-view', 'ANALYTICS_SALES_VIEW', 'View sales and revenue analytics', 'ANALYTICS', 'SALES_VIEW', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0
WHERE NOT EXISTS (SELECT 1 FROM permissions WHERE name = 'ANALYTICS_SALES_VIEW');

INSERT INTO permissions (id, name, description, resource, action, created_at, updated_at, version)
SELECT 'perm-analytics-investment-view', 'ANALYTICS_INVESTMENT_VIEW', 'View investment ROI analytics', 'ANALYTICS', 'INVESTMENT_VIEW', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0
WHERE NOT EXISTS (SELECT 1 FROM permissions WHERE name = 'ANALYTICS_INVESTMENT_VIEW');

INSERT INTO permissions (id, name, description, resource, action, created_at, updated_at, version)
SELECT 'perm-analytics-manage', 'ANALYTICS_MANAGE', 'Manage analytics cache and exports', 'ANALYTICS', 'MANAGE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0
WHERE NOT EXISTS (SELECT 1 FROM permissions WHERE name = 'ANALYTICS_MANAGE');

-- ========================================
-- FRAUD PERMISSIONS (additions)
-- ========================================
INSERT INTO permissions (id, name, description, resource, action, created_at, updated_at, version)
SELECT 'perm-fraud-list', 'FRAUD_LIST', 'List fraud alerts and assessments', 'FRAUD', 'LIST', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0
WHERE NOT EXISTS (SELECT 1 FROM permissions WHERE name = 'FRAUD_LIST');

INSERT INTO permissions (id, name, description, resource, action, created_at, updated_at, version)
SELECT 'perm-fraud-investigate', 'FRAUD_INVESTIGATE', 'Investigate fraud alerts', 'FRAUD', 'INVESTIGATE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0
WHERE NOT EXISTS (SELECT 1 FROM permissions WHERE name = 'FRAUD_INVESTIGATE');

INSERT INTO permissions (id, name, description, resource, action, created_at, updated_at, version)
SELECT 'perm-fraud-resolve', 'FRAUD_RESOLVE', 'Resolve fraud alerts and assessments', 'FRAUD', 'RESOLVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0
WHERE NOT EXISTS (SELECT 1 FROM permissions WHERE name = 'FRAUD_RESOLVE');

INSERT INTO permissions (id, name, description, resource, action, created_at, updated_at, version)
SELECT 'perm-fraud-detect', 'FRAUD_DETECT', 'Manage fraud detection rules', 'FRAUD', 'DETECT', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0
WHERE NOT EXISTS (SELECT 1 FROM permissions WHERE name = 'FRAUD_DETECT');

-- ========================================
-- MIGRATE OLD ANALYTICS PERMISSIONS
-- ========================================
-- Replace ANALYTICS_VIEW_SHOP with new feature-based permissions for existing role assignments

INSERT INTO role_permissions (role_id, permission_id)
SELECT DISTINCT rp.role_id, p.id
FROM role_permissions rp
JOIN permissions old_perm ON rp.permission_id = old_perm.id
CROSS JOIN permissions p
WHERE old_perm.name = 'ANALYTICS_VIEW_SHOP'
AND p.name IN ('ANALYTICS_SALES_VIEW', 'ANALYTICS_INVESTMENT_VIEW')
AND NOT EXISTS (
    SELECT 1 FROM role_permissions rp2
    WHERE rp2.role_id = rp.role_id AND rp2.permission_id = p.id
);

-- Replace ANALYTICS_VIEW_TENANT with all analytics permissions for existing role assignments
INSERT INTO role_permissions (role_id, permission_id)
SELECT DISTINCT rp.role_id, p.id
FROM role_permissions rp
JOIN permissions old_perm ON rp.permission_id = old_perm.id
CROSS JOIN permissions p
WHERE old_perm.name = 'ANALYTICS_VIEW_TENANT'
AND p.name IN ('ANALYTICS_SALES_VIEW', 'ANALYTICS_INVESTMENT_VIEW', 'ANALYTICS_MANAGE')
AND NOT EXISTS (
    SELECT 1 FROM role_permissions rp2
    WHERE rp2.role_id = rp.role_id AND rp2.permission_id = p.id
);

-- Remove old analytics permission assignments
DELETE FROM role_permissions
WHERE permission_id IN (
    SELECT id FROM permissions
    WHERE name IN ('ANALYTICS_VIEW_SHOP', 'ANALYTICS_VIEW_TENANT')
);

-- ========================================
-- GRANT NEW PERMISSIONS TO SYSTEM_ADMIN
-- ========================================
-- System Admin gets ALL new permissions

INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r, permissions p
WHERE r.name = 'SYSTEM_ADMIN'
AND p.name IN (
    'SHOP_LIST_ALL', 'ROLE_ASSIGN',
    'INVENTORY_ADJUST', 'INVENTORY_RESERVE', 'INVENTORY_HISTORY', 'INVENTORY_FORECAST',
    'SALES_VOID', 'RECEIPT_EMAIL', 'EXPENSE_APPROVE',
    'INVESTMENT_CLOSE', 'INVESTMENT_PROFIT_DISTRIBUTE', 'RETURN_APPROVE',
    'ANALYTICS_SALES_VIEW', 'ANALYTICS_INVESTMENT_VIEW', 'ANALYTICS_MANAGE',
    'FRAUD_LIST', 'FRAUD_INVESTIGATE', 'FRAUD_RESOLVE', 'FRAUD_DETECT'
)
AND NOT EXISTS (
    SELECT 1 FROM role_permissions rp
    WHERE rp.role_id = r.id AND rp.permission_id = p.id
);

-- ========================================
-- GRANT MISSING PERMISSIONS TO MANAGER
-- ========================================
-- Add operational permissions that MANAGER should have had from V12

INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r, permissions p
WHERE r.name = 'MANAGER'
AND p.name IN (
    -- Shop permissions (add missing SHOP_LIST)
    'SHOP_LIST',
    -- Role permissions (add missing READ and LIST)
    'ROLE_READ', 'ROLE_LIST',
    -- Inventory (operational)
    'INVENTORY_ADJUST', 'INVENTORY_RESERVE', 'INVENTORY_HISTORY', 'INVENTORY_FORECAST', 'INVENTORY_DELETE',
    -- Sales (void capability)
    'SALES_VOID',
    -- Receipt (email capability)
    'RECEIPT_EMAIL',
    -- Expense (approval capability)
    'EXPENSE_APPROVE',
    -- Return (approval capability)
    'RETURN_APPROVE',
    -- Analytics (shop-level)
    'ANALYTICS_SALES_VIEW', 'ANALYTICS_INVESTMENT_VIEW',
    -- Fraud (list and investigate)
    'FRAUD_LIST', 'FRAUD_INVESTIGATE'
)
AND NOT EXISTS (
    SELECT 1 FROM role_permissions rp
    WHERE rp.role_id = r.id AND rp.permission_id = p.id
);

-- ========================================
-- GRANT LIMITED PERMISSIONS TO EMPLOYEE
-- ========================================
-- Add basic operational permissions for employees

INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r, permissions p
WHERE r.name = 'EMPLOYEE'
AND p.name IN (
    -- Inventory (view history)
    'INVENTORY_HISTORY',
    -- Sales (no void for employees)
    -- Receipt (can send/email)
    'RECEIPT_EMAIL'
)
AND NOT EXISTS (
    SELECT 1 FROM role_permissions rp
    WHERE rp.role_id = r.id AND rp.permission_id = p.id
);

-- ========================================
-- GRANT ADDITIONAL PERMISSIONS TO OWNER
-- ========================================
-- Add new operational permissions to OWNER role

INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r, permissions p
WHERE r.name = 'OWNER'
AND p.name IN (
    -- Shop
    'SHOP_LIST_ALL',
    -- Role (assign capability)
    'ROLE_ASSIGN',
    -- Inventory
    'INVENTORY_ADJUST', 'INVENTORY_RESERVE', 'INVENTORY_HISTORY', 'INVENTORY_FORECAST',
    -- Sales
    'SALES_VOID',
    -- Receipt
    'RECEIPT_EMAIL',
    -- Expense
    'EXPENSE_APPROVE',
    -- Investment
    'INVESTMENT_CLOSE', 'INVESTMENT_PROFIT_DISTRIBUTE',
    -- Return
    'RETURN_APPROVE',
    -- Analytics (all)
    'ANALYTICS_SALES_VIEW', 'ANALYTICS_INVESTMENT_VIEW', 'ANALYTICS_MANAGE',
    -- Fraud (all)
    'FRAUD_LIST', 'FRAUD_INVESTIGATE', 'FRAUD_RESOLVE', 'FRAUD_DETECT'
)
AND NOT EXISTS (
    SELECT 1 FROM role_permissions rp
    WHERE rp.role_id = r.id AND rp.permission_id = p.id
);

-- ========================================
-- UPDATE INVESTOR ROLE (if exists)
-- ========================================
-- Grant view-only analytics permissions to investors

INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r, permissions p
WHERE r.name = 'INVESTOR'
AND p.name IN (
    'ANALYTICS_INVESTMENT_VIEW'
)
AND NOT EXISTS (
    SELECT 1 FROM role_permissions rp
    WHERE rp.role_id = r.id AND rp.permission_id = p.id
);
