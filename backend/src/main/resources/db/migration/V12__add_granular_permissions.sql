-- ========================================
-- Migration V12: Add Granular Permission System
-- ========================================
-- This migration implements fine-grained permission-based access control (PBAC)
-- replacing coarse-grained role checks with granular CRUD permissions.
--
-- Permission Naming: {RESOURCE}_{ACTION}
-- Example: PRODUCT_CREATE, PRODUCT_READ, CATEGORY_UPDATE
--
-- See docs/PERMISSION_MATRIX.md for complete matrix

-- ========================================
-- PRODUCT PERMISSIONS
-- ========================================

INSERT INTO permissions (id, name, description, resource, action, created_at, updated_at, version)
SELECT 'perm-product-create', 'PRODUCT_CREATE', 'Create new products', 'PRODUCT', 'CREATE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0
WHERE NOT EXISTS (SELECT 1 FROM permissions WHERE name = 'PRODUCT_CREATE');

INSERT INTO permissions (id, name, description, resource, action, created_at, updated_at, version)
SELECT 'perm-product-read', 'PRODUCT_READ', 'View product details', 'PRODUCT', 'READ', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0
WHERE NOT EXISTS (SELECT 1 FROM permissions WHERE name = 'PRODUCT_READ');

INSERT INTO permissions (id, name, description, resource, action, created_at, updated_at, version)
SELECT 'perm-product-list', 'PRODUCT_LIST', 'List and search products', 'PRODUCT', 'LIST', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0
WHERE NOT EXISTS (SELECT 1 FROM permissions WHERE name = 'PRODUCT_LIST');

INSERT INTO permissions (id, name, description, resource, action, created_at, updated_at, version)
SELECT 'perm-product-update', 'PRODUCT_UPDATE', 'Edit product details', 'PRODUCT', 'UPDATE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0
WHERE NOT EXISTS (SELECT 1 FROM permissions WHERE name = 'PRODUCT_UPDATE');

INSERT INTO permissions (id, name, description, resource, action, created_at, updated_at, version)
SELECT 'perm-product-delete', 'PRODUCT_DELETE', 'Delete products', 'PRODUCT', 'DELETE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0
WHERE NOT EXISTS (SELECT 1 FROM permissions WHERE name = 'PRODUCT_DELETE');

-- ========================================
-- CATEGORY PERMISSIONS
-- ========================================

INSERT INTO permissions (id, name, description, resource, action, created_at, updated_at, version)
SELECT 'perm-category-create', 'CATEGORY_CREATE', 'Create product categories', 'CATEGORY', 'CREATE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0
WHERE NOT EXISTS (SELECT 1 FROM permissions WHERE name = 'CATEGORY_CREATE');

INSERT INTO permissions (id, name, description, resource, action, created_at, updated_at, version)
SELECT 'perm-category-read', 'CATEGORY_READ', 'View category details', 'CATEGORY', 'READ', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0
WHERE NOT EXISTS (SELECT 1 FROM permissions WHERE name = 'CATEGORY_READ');

INSERT INTO permissions (id, name, description, resource, action, created_at, updated_at, version)
SELECT 'perm-category-list', 'CATEGORY_LIST', 'List and search categories', 'CATEGORY', 'LIST', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0
WHERE NOT EXISTS (SELECT 1 FROM permissions WHERE name = 'CATEGORY_LIST');

INSERT INTO permissions (id, name, description, resource, action, created_at, updated_at, version)
SELECT 'perm-category-update', 'CATEGORY_UPDATE', 'Edit category details', 'CATEGORY', 'UPDATE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0
WHERE NOT EXISTS (SELECT 1 FROM permissions WHERE name = 'CATEGORY_UPDATE');

INSERT INTO permissions (id, name, description, resource, action, created_at, updated_at, version)
SELECT 'perm-category-delete', 'CATEGORY_DELETE', 'Delete categories', 'CATEGORY', 'DELETE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0
WHERE NOT EXISTS (SELECT 1 FROM permissions WHERE name = 'CATEGORY_DELETE');

-- ========================================
-- INVENTORY PERMISSIONS
-- ========================================

INSERT INTO permissions (id, name, description, resource, action, created_at, updated_at, version)
SELECT 'perm-inventory-create', 'INVENTORY_CREATE', 'Add stock to inventory', 'INVENTORY', 'CREATE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0
WHERE NOT EXISTS (SELECT 1 FROM permissions WHERE name = 'INVENTORY_CREATE');

INSERT INTO permissions (id, name, description, resource, action, created_at, updated_at, version)
SELECT 'perm-inventory-read', 'INVENTORY_READ', 'View inventory details', 'INVENTORY', 'READ', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0
WHERE NOT EXISTS (SELECT 1 FROM permissions WHERE name = 'INVENTORY_READ');

INSERT INTO permissions (id, name, description, resource, action, created_at, updated_at, version)
SELECT 'perm-inventory-list', 'INVENTORY_LIST', 'List and search inventory', 'INVENTORY', 'LIST', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0
WHERE NOT EXISTS (SELECT 1 FROM permissions WHERE name = 'INVENTORY_LIST');

INSERT INTO permissions (id, name, description, resource, action, created_at, updated_at, version)
SELECT 'perm-inventory-update', 'INVENTORY_UPDATE', 'Adjust stock levels', 'INVENTORY', 'UPDATE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0
WHERE NOT EXISTS (SELECT 1 FROM permissions WHERE name = 'INVENTORY_UPDATE');

INSERT INTO permissions (id, name, description, resource, action, created_at, updated_at, version)
SELECT 'perm-inventory-delete', 'INVENTORY_DELETE', 'Remove inventory records', 'INVENTORY', 'DELETE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0
WHERE NOT EXISTS (SELECT 1 FROM permissions WHERE name = 'INVENTORY_DELETE');

INSERT INTO permissions (id, name, description, resource, action, created_at, updated_at, version)
SELECT 'perm-inventory-history-view', 'INVENTORY_HISTORY_VIEW', 'View inventory history and movements', 'INVENTORY', 'HISTORY_VIEW', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0
WHERE NOT EXISTS (SELECT 1 FROM permissions WHERE name = 'INVENTORY_HISTORY_VIEW');

-- ========================================
-- SALES PERMISSIONS
-- ========================================

INSERT INTO permissions (id, name, description, resource, action, created_at, updated_at, version)
SELECT 'perm-sales-create', 'SALES_CREATE', 'Process sales transactions', 'SALES', 'CREATE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0
WHERE NOT EXISTS (SELECT 1 FROM permissions WHERE name = 'SALES_CREATE');

INSERT INTO permissions (id, name, description, resource, action, created_at, updated_at, version)
SELECT 'perm-sales-read', 'SALES_READ', 'View sale transaction details', 'SALES', 'READ', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0
WHERE NOT EXISTS (SELECT 1 FROM permissions WHERE name = 'SALES_READ');

INSERT INTO permissions (id, name, description, resource, action, created_at, updated_at, version)
SELECT 'perm-sales-list', 'SALES_LIST', 'List and search sales', 'SALES', 'LIST', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0
WHERE NOT EXISTS (SELECT 1 FROM permissions WHERE name = 'SALES_LIST');

INSERT INTO permissions (id, name, description, resource, action, created_at, updated_at, version)
SELECT 'perm-sales-update', 'SALES_UPDATE', 'Edit or void sales transactions', 'SALES', 'UPDATE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0
WHERE NOT EXISTS (SELECT 1 FROM permissions WHERE name = 'SALES_UPDATE');

INSERT INTO permissions (id, name, description, resource, action, created_at, updated_at, version)
SELECT 'perm-sales-delete', 'SALES_DELETE', 'Delete sales records', 'SALES', 'DELETE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0
WHERE NOT EXISTS (SELECT 1 FROM permissions WHERE name = 'SALES_DELETE');

-- ========================================
-- RECEIPT PERMISSIONS
-- ========================================

INSERT INTO permissions (id, name, description, resource, action, created_at, updated_at, version)
SELECT 'perm-receipt-create', 'RECEIPT_CREATE', 'Generate receipts', 'RECEIPT', 'CREATE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0
WHERE NOT EXISTS (SELECT 1 FROM permissions WHERE name = 'RECEIPT_CREATE');

INSERT INTO permissions (id, name, description, resource, action, created_at, updated_at, version)
SELECT 'perm-receipt-read', 'RECEIPT_READ', 'View receipt details', 'RECEIPT', 'READ', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0
WHERE NOT EXISTS (SELECT 1 FROM permissions WHERE name = 'RECEIPT_READ');

INSERT INTO permissions (id, name, description, resource, action, created_at, updated_at, version)
SELECT 'perm-receipt-list', 'RECEIPT_LIST', 'List receipts', 'RECEIPT', 'LIST', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0
WHERE NOT EXISTS (SELECT 1 FROM permissions WHERE name = 'RECEIPT_LIST');

INSERT INTO permissions (id, name, description, resource, action, created_at, updated_at, version)
SELECT 'perm-receipt-send', 'RECEIPT_SEND', 'Email or print receipts', 'RECEIPT', 'SEND', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0
WHERE NOT EXISTS (SELECT 1 FROM permissions WHERE name = 'RECEIPT_SEND');

-- ========================================
-- EXPENSE PERMISSIONS
-- ========================================

INSERT INTO permissions (id, name, description, resource, action, created_at, updated_at, version)
SELECT 'perm-expense-create', 'EXPENSE_CREATE', 'Record expenses', 'EXPENSE', 'CREATE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0
WHERE NOT EXISTS (SELECT 1 FROM permissions WHERE name = 'EXPENSE_CREATE');

INSERT INTO permissions (id, name, description, resource, action, created_at, updated_at, version)
SELECT 'perm-expense-read', 'EXPENSE_READ', 'View expense details', 'EXPENSE', 'READ', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0
WHERE NOT EXISTS (SELECT 1 FROM permissions WHERE name = 'EXPENSE_READ');

INSERT INTO permissions (id, name, description, resource, action, created_at, updated_at, version)
SELECT 'perm-expense-list', 'EXPENSE_LIST', 'List and search expenses', 'EXPENSE', 'LIST', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0
WHERE NOT EXISTS (SELECT 1 FROM permissions WHERE name = 'EXPENSE_LIST');

INSERT INTO permissions (id, name, description, resource, action, created_at, updated_at, version)
SELECT 'perm-expense-update', 'EXPENSE_UPDATE', 'Edit expense records', 'EXPENSE', 'UPDATE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0
WHERE NOT EXISTS (SELECT 1 FROM permissions WHERE name = 'EXPENSE_UPDATE');

INSERT INTO permissions (id, name, description, resource, action, created_at, updated_at, version)
SELECT 'perm-expense-delete', 'EXPENSE_DELETE', 'Delete expense records', 'EXPENSE', 'DELETE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0
WHERE NOT EXISTS (SELECT 1 FROM permissions WHERE name = 'EXPENSE_DELETE');

-- ========================================
-- EXPENSE CATEGORY PERMISSIONS
-- ========================================

INSERT INTO permissions (id, name, description, resource, action, created_at, updated_at, version)
SELECT 'perm-expense-category-create', 'EXPENSE_CATEGORY_CREATE', 'Create expense categories', 'EXPENSE_CATEGORY', 'CREATE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0
WHERE NOT EXISTS (SELECT 1 FROM permissions WHERE name = 'EXPENSE_CATEGORY_CREATE');

INSERT INTO permissions (id, name, description, resource, action, created_at, updated_at, version)
SELECT 'perm-expense-category-read', 'EXPENSE_CATEGORY_READ', 'View expense category details', 'EXPENSE_CATEGORY', 'READ', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0
WHERE NOT EXISTS (SELECT 1 FROM permissions WHERE name = 'EXPENSE_CATEGORY_READ');

INSERT INTO permissions (id, name, description, resource, action, created_at, updated_at, version)
SELECT 'perm-expense-category-list', 'EXPENSE_CATEGORY_LIST', 'List expense categories', 'EXPENSE_CATEGORY', 'LIST', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0
WHERE NOT EXISTS (SELECT 1 FROM permissions WHERE name = 'EXPENSE_CATEGORY_LIST');

INSERT INTO permissions (id, name, description, resource, action, created_at, updated_at, version)
SELECT 'perm-expense-category-update', 'EXPENSE_CATEGORY_UPDATE', 'Edit expense categories', 'EXPENSE_CATEGORY', 'UPDATE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0
WHERE NOT EXISTS (SELECT 1 FROM permissions WHERE name = 'EXPENSE_CATEGORY_UPDATE');

INSERT INTO permissions (id, name, description, resource, action, created_at, updated_at, version)
SELECT 'perm-expense-category-delete', 'EXPENSE_CATEGORY_DELETE', 'Delete expense categories', 'EXPENSE_CATEGORY', 'DELETE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0
WHERE NOT EXISTS (SELECT 1 FROM permissions WHERE name = 'EXPENSE_CATEGORY_DELETE');

-- ========================================
-- INVESTMENT PERMISSIONS
-- ========================================

INSERT INTO permissions (id, name, description, resource, action, created_at, updated_at, version)
SELECT 'perm-investment-create', 'INVESTMENT_CREATE', 'Create investments', 'INVESTMENT', 'CREATE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0
WHERE NOT EXISTS (SELECT 1 FROM permissions WHERE name = 'INVESTMENT_CREATE');

INSERT INTO permissions (id, name, description, resource, action, created_at, updated_at, version)
SELECT 'perm-investment-read', 'INVESTMENT_READ', 'View investment details', 'INVESTMENT', 'READ', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0
WHERE NOT EXISTS (SELECT 1 FROM permissions WHERE name = 'INVESTMENT_READ');

INSERT INTO permissions (id, name, description, resource, action, created_at, updated_at, version)
SELECT 'perm-investment-list', 'INVESTMENT_LIST', 'List investments', 'INVESTMENT', 'LIST', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0
WHERE NOT EXISTS (SELECT 1 FROM permissions WHERE name = 'INVESTMENT_LIST');

INSERT INTO permissions (id, name, description, resource, action, created_at, updated_at, version)
SELECT 'perm-investment-update', 'INVESTMENT_UPDATE', 'Edit investment records', 'INVESTMENT', 'UPDATE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0
WHERE NOT EXISTS (SELECT 1 FROM permissions WHERE name = 'INVESTMENT_UPDATE');

INSERT INTO permissions (id, name, description, resource, action, created_at, updated_at, version)
SELECT 'perm-investment-delete', 'INVESTMENT_DELETE', 'Delete investment records', 'INVESTMENT', 'DELETE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0
WHERE NOT EXISTS (SELECT 1 FROM permissions WHERE name = 'INVESTMENT_DELETE');

-- ========================================
-- RETURN PERMISSIONS
-- ========================================

INSERT INTO permissions (id, name, description, resource, action, created_at, updated_at, version)
SELECT 'perm-return-create', 'RETURN_CREATE', 'Process product returns', 'RETURN', 'CREATE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0
WHERE NOT EXISTS (SELECT 1 FROM permissions WHERE name = 'RETURN_CREATE');

INSERT INTO permissions (id, name, description, resource, action, created_at, updated_at, version)
SELECT 'perm-return-read', 'RETURN_READ', 'View return details', 'RETURN', 'READ', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0
WHERE NOT EXISTS (SELECT 1 FROM permissions WHERE name = 'RETURN_READ');

INSERT INTO permissions (id, name, description, resource, action, created_at, updated_at, version)
SELECT 'perm-return-list', 'RETURN_LIST', 'List returns', 'RETURN', 'LIST', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0
WHERE NOT EXISTS (SELECT 1 FROM permissions WHERE name = 'RETURN_LIST');

INSERT INTO permissions (id, name, description, resource, action, created_at, updated_at, version)
SELECT 'perm-return-update', 'RETURN_UPDATE', 'Edit return status', 'RETURN', 'UPDATE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0
WHERE NOT EXISTS (SELECT 1 FROM permissions WHERE name = 'RETURN_UPDATE');

INSERT INTO permissions (id, name, description, resource, action, created_at, updated_at, version)
SELECT 'perm-return-delete', 'RETURN_DELETE', 'Delete return records', 'RETURN', 'DELETE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0
WHERE NOT EXISTS (SELECT 1 FROM permissions WHERE name = 'RETURN_DELETE');

-- ========================================
-- TENANT PERMISSIONS
-- ========================================

INSERT INTO permissions (id, name, description, resource, action, created_at, updated_at, version)
SELECT 'perm-tenant-create', 'TENANT_CREATE', 'Create tenants', 'TENANT', 'CREATE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0
WHERE NOT EXISTS (SELECT 1 FROM permissions WHERE name = 'TENANT_CREATE');

INSERT INTO permissions (id, name, description, resource, action, created_at, updated_at, version)
SELECT 'perm-tenant-read', 'TENANT_READ', 'View tenant details', 'TENANT', 'READ', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0
WHERE NOT EXISTS (SELECT 1 FROM permissions WHERE name = 'TENANT_READ');

INSERT INTO permissions (id, name, description, resource, action, created_at, updated_at, version)
SELECT 'perm-tenant-list', 'TENANT_LIST', 'List tenants', 'TENANT', 'LIST', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0
WHERE NOT EXISTS (SELECT 1 FROM permissions WHERE name = 'TENANT_LIST');

INSERT INTO permissions (id, name, description, resource, action, created_at, updated_at, version)
SELECT 'perm-tenant-update', 'TENANT_UPDATE', 'Edit tenant settings', 'TENANT', 'UPDATE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0
WHERE NOT EXISTS (SELECT 1 FROM permissions WHERE name = 'TENANT_UPDATE');

INSERT INTO permissions (id, name, description, resource, action, created_at, updated_at, version)
SELECT 'perm-tenant-delete', 'TENANT_DELETE', 'Delete tenants', 'TENANT', 'DELETE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0
WHERE NOT EXISTS (SELECT 1 FROM permissions WHERE name = 'TENANT_DELETE');

-- ========================================
-- SHOP PERMISSIONS
-- ========================================

INSERT INTO permissions (id, name, description, resource, action, created_at, updated_at, version)
SELECT 'perm-shop-create', 'SHOP_CREATE', 'Create shops', 'SHOP', 'CREATE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0
WHERE NOT EXISTS (SELECT 1 FROM permissions WHERE name = 'SHOP_CREATE');

INSERT INTO permissions (id, name, description, resource, action, created_at, updated_at, version)
SELECT 'perm-shop-read', 'SHOP_READ', 'View shop details', 'SHOP', 'READ', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0
WHERE NOT EXISTS (SELECT 1 FROM permissions WHERE name = 'SHOP_READ');

INSERT INTO permissions (id, name, description, resource, action, created_at, updated_at, version)
SELECT 'perm-shop-list', 'SHOP_LIST', 'List shops', 'SHOP', 'LIST', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0
WHERE NOT EXISTS (SELECT 1 FROM permissions WHERE name = 'SHOP_LIST');

INSERT INTO permissions (id, name, description, resource, action, created_at, updated_at, version)
SELECT 'perm-shop-update', 'SHOP_UPDATE', 'Edit shop settings', 'SHOP', 'UPDATE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0
WHERE NOT EXISTS (SELECT 1 FROM permissions WHERE name = 'SHOP_UPDATE');

INSERT INTO permissions (id, name, description, resource, action, created_at, updated_at, version)
SELECT 'perm-shop-delete', 'SHOP_DELETE', 'Delete shops', 'SHOP', 'DELETE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0
WHERE NOT EXISTS (SELECT 1 FROM permissions WHERE name = 'SHOP_DELETE');

-- ========================================
-- USER PERMISSIONS
-- ========================================

INSERT INTO permissions (id, name, description, resource, action, created_at, updated_at, version)
SELECT 'perm-user-create', 'USER_CREATE', 'Create users', 'USER', 'CREATE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0
WHERE NOT EXISTS (SELECT 1 FROM permissions WHERE name = 'USER_CREATE');

INSERT INTO permissions (id, name, description, resource, action, created_at, updated_at, version)
SELECT 'perm-user-read', 'USER_READ', 'View user details', 'USER', 'READ', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0
WHERE NOT EXISTS (SELECT 1 FROM permissions WHERE name = 'USER_READ');

INSERT INTO permissions (id, name, description, resource, action, created_at, updated_at, version)
SELECT 'perm-user-list', 'USER_LIST', 'List users', 'USER', 'LIST', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0
WHERE NOT EXISTS (SELECT 1 FROM permissions WHERE name = 'USER_LIST');

INSERT INTO permissions (id, name, description, resource, action, created_at, updated_at, version)
SELECT 'perm-user-update', 'USER_UPDATE', 'Edit user details', 'USER', 'UPDATE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0
WHERE NOT EXISTS (SELECT 1 FROM permissions WHERE name = 'USER_UPDATE');

INSERT INTO permissions (id, name, description, resource, action, created_at, updated_at, version)
SELECT 'perm-user-delete', 'USER_DELETE', 'Delete users', 'USER', 'DELETE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0
WHERE NOT EXISTS (SELECT 1 FROM permissions WHERE name = 'USER_DELETE');

-- ========================================
-- ROLE PERMISSIONS
-- ========================================

INSERT INTO permissions (id, name, description, resource, action, created_at, updated_at, version)
SELECT 'perm-role-create', 'ROLE_CREATE', 'Create custom roles', 'ROLE', 'CREATE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0
WHERE NOT EXISTS (SELECT 1 FROM permissions WHERE name = 'ROLE_CREATE');

INSERT INTO permissions (id, name, description, resource, action, created_at, updated_at, version)
SELECT 'perm-role-read', 'ROLE_READ', 'View role details', 'ROLE', 'READ', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0
WHERE NOT EXISTS (SELECT 1 FROM permissions WHERE name = 'ROLE_READ');

INSERT INTO permissions (id, name, description, resource, action, created_at, updated_at, version)
SELECT 'perm-role-list', 'ROLE_LIST', 'List roles', 'ROLE', 'LIST', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0
WHERE NOT EXISTS (SELECT 1 FROM permissions WHERE name = 'ROLE_LIST');

INSERT INTO permissions (id, name, description, resource, action, created_at, updated_at, version)
SELECT 'perm-role-update', 'ROLE_UPDATE', 'Edit role permissions', 'ROLE', 'UPDATE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0
WHERE NOT EXISTS (SELECT 1 FROM permissions WHERE name = 'ROLE_UPDATE');

INSERT INTO permissions (id, name, description, resource, action, created_at, updated_at, version)
SELECT 'perm-role-delete', 'ROLE_DELETE', 'Delete custom roles', 'ROLE', 'DELETE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0
WHERE NOT EXISTS (SELECT 1 FROM permissions WHERE name = 'ROLE_DELETE');

-- ========================================
-- PERMISSION PERMISSIONS
-- ========================================

INSERT INTO permissions (id, name, description, resource, action, created_at, updated_at, version)
SELECT 'perm-permission-read', 'PERMISSION_READ', 'View permission details', 'PERMISSION', 'READ', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0
WHERE NOT EXISTS (SELECT 1 FROM permissions WHERE name = 'PERMISSION_READ');

INSERT INTO permissions (id, name, description, resource, action, created_at, updated_at, version)
SELECT 'perm-permission-list', 'PERMISSION_LIST', 'List all permissions', 'PERMISSION', 'LIST', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0
WHERE NOT EXISTS (SELECT 1 FROM permissions WHERE name = 'PERMISSION_LIST');

-- ========================================
-- AUDIT LOG PERMISSIONS
-- ========================================

INSERT INTO permissions (id, name, description, resource, action, created_at, updated_at, version)
SELECT 'perm-audit-log-view-shop', 'AUDIT_LOG_VIEW_SHOP', 'View shop-level audit logs', 'AUDIT_LOG', 'VIEW_SHOP', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0
WHERE NOT EXISTS (SELECT 1 FROM permissions WHERE name = 'AUDIT_LOG_VIEW_SHOP');

INSERT INTO permissions (id, name, description, resource, action, created_at, updated_at, version)
SELECT 'perm-audit-log-view-tenant', 'AUDIT_LOG_VIEW_TENANT', 'View tenant-level audit logs', 'AUDIT_LOG', 'VIEW_TENANT', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0
WHERE NOT EXISTS (SELECT 1 FROM permissions WHERE name = 'AUDIT_LOG_VIEW_TENANT');

-- ========================================
-- ANALYTICS PERMISSIONS
-- ========================================

INSERT INTO permissions (id, name, description, resource, action, created_at, updated_at, version)
SELECT 'perm-analytics-view-shop', 'ANALYTICS_VIEW_SHOP', 'View shop-level analytics', 'ANALYTICS', 'VIEW_SHOP', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0
WHERE NOT EXISTS (SELECT 1 FROM permissions WHERE name = 'ANALYTICS_VIEW_SHOP');

INSERT INTO permissions (id, name, description, resource, action, created_at, updated_at, version)
SELECT 'perm-analytics-view-tenant', 'ANALYTICS_VIEW_TENANT', 'View tenant-level analytics', 'ANALYTICS', 'VIEW_TENANT', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0
WHERE NOT EXISTS (SELECT 1 FROM permissions WHERE name = 'ANALYTICS_VIEW_TENANT');

-- ========================================
-- FRAUD PERMISSIONS
-- ========================================

INSERT INTO permissions (id, name, description, resource, action, created_at, updated_at, version)
SELECT 'perm-fraud-view', 'FRAUD_VIEW', 'View fraud alerts and risk assessments', 'FRAUD', 'VIEW', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0
WHERE NOT EXISTS (SELECT 1 FROM permissions WHERE name = 'FRAUD_VIEW');

INSERT INTO permissions (id, name, description, resource, action, created_at, updated_at, version)
SELECT 'perm-fraud-manage', 'FRAUD_MANAGE', 'Manage fraud rules and configurations', 'FRAUD', 'MANAGE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0
WHERE NOT EXISTS (SELECT 1 FROM permissions WHERE name = 'FRAUD_MANAGE');

-- ========================================
-- ASSIGN PERMISSIONS TO SYSTEM_ADMIN ROLE
-- ========================================
-- System Admin gets ALL permissions

INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r
CROSS JOIN permissions p
WHERE r.name = 'SYSTEM_ADMIN'
AND NOT EXISTS (
    SELECT 1 FROM role_permissions rp
    WHERE rp.role_id = r.id AND rp.permission_id = p.id
);

-- ========================================
-- ASSIGN PERMISSIONS TO TENANT_ADMIN ROLE
-- ========================================
-- Tenant Admin gets all permissions except SYSTEM_ADMIN and TENANT_DELETE

INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r, permissions p
WHERE r.name = 'TENANT_ADMIN'
AND p.name NOT IN ('SYSTEM_ADMIN', 'TENANT_DELETE', 'TENANT_CREATE')
AND NOT EXISTS (
    SELECT 1 FROM role_permissions rp
    WHERE rp.role_id = r.id AND rp.permission_id = p.id
);

-- ========================================
-- ASSIGN PERMISSIONS TO OWNER ROLE
-- ========================================
-- Owner gets all operational permissions (no role management, no tenant delete)

INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r, permissions p
WHERE r.name = 'OWNER'
AND p.name IN (
    -- Tenant & Shop
    'TENANT_READ', 'TENANT_LIST', 'TENANT_UPDATE',
    'SHOP_CREATE', 'SHOP_READ', 'SHOP_LIST', 'SHOP_UPDATE',
    -- Users
    'USER_CREATE', 'USER_READ', 'USER_LIST', 'USER_UPDATE', 'USER_DELETE',
    -- Permissions & Roles (read-only)
    'PERMISSION_READ', 'PERMISSION_LIST', 'ROLE_READ', 'ROLE_LIST',
    -- Products & Categories
    'PRODUCT_CREATE', 'PRODUCT_READ', 'PRODUCT_LIST', 'PRODUCT_UPDATE', 'PRODUCT_DELETE',
    'CATEGORY_CREATE', 'CATEGORY_READ', 'CATEGORY_LIST', 'CATEGORY_UPDATE', 'CATEGORY_DELETE',
    -- Inventory
    'INVENTORY_CREATE', 'INVENTORY_READ', 'INVENTORY_LIST', 'INVENTORY_UPDATE', 'INVENTORY_DELETE', 'INVENTORY_HISTORY_VIEW',
    -- Sales & Receipts
    'SALES_CREATE', 'SALES_READ', 'SALES_LIST', 'SALES_UPDATE', 'SALES_DELETE',
    'RECEIPT_CREATE', 'RECEIPT_READ', 'RECEIPT_LIST', 'RECEIPT_SEND',
    -- Financial
    'EXPENSE_CREATE', 'EXPENSE_READ', 'EXPENSE_LIST', 'EXPENSE_UPDATE', 'EXPENSE_DELETE',
    'EXPENSE_CATEGORY_CREATE', 'EXPENSE_CATEGORY_READ', 'EXPENSE_CATEGORY_LIST', 'EXPENSE_CATEGORY_UPDATE', 'EXPENSE_CATEGORY_DELETE',
    'INVESTMENT_CREATE', 'INVESTMENT_READ', 'INVESTMENT_LIST', 'INVESTMENT_UPDATE', 'INVESTMENT_DELETE',
    'RETURN_CREATE', 'RETURN_READ', 'RETURN_LIST', 'RETURN_UPDATE', 'RETURN_DELETE',
    -- Audit & Analytics
    'AUDIT_LOG_VIEW_SHOP', 'AUDIT_LOG_VIEW_TENANT',
    'ANALYTICS_VIEW_SHOP', 'ANALYTICS_VIEW_TENANT',
    'FRAUD_VIEW', 'FRAUD_MANAGE'
)
AND NOT EXISTS (
    SELECT 1 FROM role_permissions rp
    WHERE rp.role_id = r.id AND rp.permission_id = p.id
);

-- ========================================
-- ASSIGN PERMISSIONS TO MANAGER ROLE
-- ========================================
-- Manager gets shop-level operational permissions

INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r, permissions p
WHERE r.name = 'MANAGER'
AND p.name IN (
    -- Shop (read-only)
    'SHOP_READ',
    -- Users (read-only)
    'USER_READ', 'USER_LIST',
    -- Products & Categories
    'PRODUCT_CREATE', 'PRODUCT_READ', 'PRODUCT_LIST', 'PRODUCT_UPDATE', 'PRODUCT_DELETE',
    'CATEGORY_CREATE', 'CATEGORY_READ', 'CATEGORY_LIST', 'CATEGORY_UPDATE', 'CATEGORY_DELETE',
    -- Inventory
    'INVENTORY_CREATE', 'INVENTORY_READ', 'INVENTORY_LIST', 'INVENTORY_UPDATE', 'INVENTORY_HISTORY_VIEW',
    -- Sales & Receipts
    'SALES_CREATE', 'SALES_READ', 'SALES_LIST', 'SALES_UPDATE',
    'RECEIPT_CREATE', 'RECEIPT_READ', 'RECEIPT_LIST', 'RECEIPT_SEND',
    -- Financial
    'EXPENSE_CREATE', 'EXPENSE_READ', 'EXPENSE_LIST', 'EXPENSE_UPDATE',
    'EXPENSE_CATEGORY_CREATE', 'EXPENSE_CATEGORY_READ', 'EXPENSE_CATEGORY_LIST', 'EXPENSE_CATEGORY_UPDATE', 'EXPENSE_CATEGORY_DELETE',
    'RETURN_CREATE', 'RETURN_READ', 'RETURN_LIST', 'RETURN_UPDATE',
    -- Audit & Analytics (shop-level only)
    'AUDIT_LOG_VIEW_SHOP',
    'ANALYTICS_VIEW_SHOP',
    'FRAUD_VIEW'
)
AND NOT EXISTS (
    SELECT 1 FROM role_permissions rp
    WHERE rp.role_id = r.id AND rp.permission_id = p.id
);

-- ========================================
-- ASSIGN PERMISSIONS TO EMPLOYEE ROLE
-- ========================================
-- Employee/Cashier gets transaction permissions

INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r, permissions p
WHERE r.name = 'EMPLOYEE'
AND p.name IN (
    -- Shop (read-only)
    'SHOP_READ',
    -- Products & Categories (read-only)
    'PRODUCT_READ', 'PRODUCT_LIST',
    'CATEGORY_READ', 'CATEGORY_LIST',
    -- Inventory (read-only)
    'INVENTORY_READ', 'INVENTORY_LIST',
    -- Sales & Receipts
    'SALES_CREATE', 'SALES_READ', 'SALES_LIST',
    'RECEIPT_CREATE', 'RECEIPT_READ', 'RECEIPT_LIST', 'RECEIPT_SEND',
    -- Returns
    'RETURN_CREATE', 'RETURN_READ', 'RETURN_LIST'
)
AND NOT EXISTS (
    SELECT 1 FROM role_permissions rp
    WHERE rp.role_id = r.id AND rp.permission_id = p.id
);

-- ========================================
-- ASSIGN PERMISSIONS TO INVESTOR ROLE
-- ========================================
-- Investor gets limited read-only access

INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r, permissions p
WHERE r.name = 'INVESTOR'
AND p.name IN (
    'INVESTMENT_READ', 'INVESTMENT_LIST',
    'SALES_READ', 'SALES_LIST' -- For profit calculation
)
AND NOT EXISTS (
    SELECT 1 FROM role_permissions rp
    WHERE rp.role_id = r.id AND rp.permission_id = p.id
);

-- ========================================
-- MIGRATION COMPLETE
-- ========================================
-- Total Permissions Added: ~80
-- See docs/PERMISSION_MATRIX.md for complete matrix
