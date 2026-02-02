-- Fix schema inconsistencies between JPA entities and database schema
-- This migration addresses missing columns and data type mismatches

-- ========================================
-- FIX INVENTORY TABLE SCHEMA
-- ========================================

-- Add missing last_stock_update column to inventory table
ALTER TABLE inventory ADD COLUMN IF NOT EXISTS last_stock_update TIMESTAMP;

-- Remove computed columns FIRST (before altering column types they depend on)
ALTER TABLE inventory DROP COLUMN IF EXISTS available_stock;
ALTER TABLE inventory DROP COLUMN IF EXISTS total_value;

-- Update inventory table data types to match entity (Integer instead of DECIMAL for stock)
-- Note: current_stock removed in V60, using purchase_quantity instead
-- Convert existing DECIMAL stock columns to INTEGER
ALTER TABLE inventory
  ALTER COLUMN reserved_stock TYPE INTEGER USING reserved_stock::INTEGER,
  ALTER COLUMN minimum_stock TYPE INTEGER USING minimum_stock::INTEGER,
  ALTER COLUMN maximum_stock TYPE INTEGER USING maximum_stock::INTEGER,
  ALTER COLUMN reorder_point TYPE INTEGER USING reorder_point::INTEGER;

-- ========================================
-- INSERT REQUIRED SYSTEM ROLES
-- ========================================

-- Insert SUPER_ADMIN role if it doesn't exist
INSERT INTO roles (id, name, description, is_system, tenant_id, created_at, updated_at, version)
SELECT
    'super-admin-role-id',
    'SUPER_ADMIN',
    'System Administrator with full access to all tenants and features',
    true,
    null,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP,
    0
WHERE NOT EXISTS (SELECT 1 FROM roles WHERE name = 'SUPER_ADMIN');

-- Insert TENANT_ADMIN role if it doesn't exist
INSERT INTO roles (id, name, description, is_system, tenant_id, created_at, updated_at, version)
SELECT
    'tenant-admin-role-id',
    'TENANT_ADMIN',
    'Tenant Administrator with full access to tenant resources',
    true,
    null,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP,
    0
WHERE NOT EXISTS (SELECT 1 FROM roles WHERE name = 'TENANT_ADMIN');

-- Insert SHOP_MANAGER role if it doesn't exist
INSERT INTO roles (id, name, description, is_system, tenant_id, created_at, updated_at, version)
SELECT
    'shop-manager-role-id',
    'SHOP_MANAGER',
    'Shop Manager with access to shop operations and management',
    true,
    null,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP,
    0
WHERE NOT EXISTS (SELECT 1 FROM roles WHERE name = 'SHOP_MANAGER');

-- Insert SHOP_EMPLOYEE role if it doesn't exist
INSERT INTO roles (id, name, description, is_system, tenant_id, created_at, updated_at, version)
SELECT
    'shop-employee-role-id',
    'SHOP_EMPLOYEE',
    'Shop Employee with limited access to sales and inventory',
    true,
    null,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP,
    0
WHERE NOT EXISTS (SELECT 1 FROM roles WHERE name = 'SHOP_EMPLOYEE');

-- Insert INVESTOR role if it doesn't exist
INSERT INTO roles (id, name, description, is_system, tenant_id, created_at, updated_at, version)
SELECT
    'investor-role-id',
    'INVESTOR',
    'Investor with access to investment tracking and returns',
    true,
    null,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP,
    0
WHERE NOT EXISTS (SELECT 1 FROM roles WHERE name = 'INVESTOR');

-- ========================================
-- INSERT REQUIRED SYSTEM PERMISSIONS
-- ========================================

-- System Administration Permissions
INSERT INTO permissions (id, name, description, resource, action, created_at, updated_at, version)
SELECT
    'perm-system-admin',
    'SYSTEM_ADMIN',
    'Full system administration access',
    'SYSTEM',
    'ADMIN',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP,
    0
WHERE NOT EXISTS (SELECT 1 FROM permissions WHERE name = 'SYSTEM_ADMIN');

-- Tenant Management Permissions
INSERT INTO permissions (id, name, description, resource, action, created_at, updated_at, version)
SELECT
    'perm-tenant-manage',
    'TENANT_MANAGE',
    'Manage tenant configuration and settings',
    'TENANT',
    'MANAGE',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP,
    0
WHERE NOT EXISTS (SELECT 1 FROM permissions WHERE name = 'TENANT_MANAGE');

-- Shop Management Permissions
INSERT INTO permissions (id, name, description, resource, action, created_at, updated_at, version)
SELECT
    'perm-shop-manage',
    'SHOP_MANAGE',
    'Manage shop configuration and operations',
    'SHOP',
    'MANAGE',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP,
    0
WHERE NOT EXISTS (SELECT 1 FROM permissions WHERE name = 'SHOP_MANAGE');

-- Product Management Permissions
INSERT INTO permissions (id, name, description, resource, action, created_at, updated_at, version)
SELECT
    'perm-product-manage',
    'PRODUCT_MANAGE',
    'Manage products and inventory',
    'PRODUCT',
    'MANAGE',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP,
    0
WHERE NOT EXISTS (SELECT 1 FROM permissions WHERE name = 'PRODUCT_MANAGE');

-- Sales Permissions
INSERT INTO permissions (id, name, description, resource, action, created_at, updated_at, version)
SELECT
    'perm-sales-create',
    'SALES_CREATE',
    'Create and process sales transactions',
    'SALES',
    'CREATE',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP,
    0
WHERE NOT EXISTS (SELECT 1 FROM permissions WHERE name = 'SALES_CREATE');

-- Investment Permissions
INSERT INTO permissions (id, name, description, resource, action, created_at, updated_at, version)
SELECT
    'perm-investment-view',
    'INVESTMENT_VIEW',
    'View investment data and returns',
    'INVESTMENT',
    'VIEW',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP,
    0
WHERE NOT EXISTS (SELECT 1 FROM permissions WHERE name = 'INVESTMENT_VIEW');

-- ========================================
-- ASSIGN PERMISSIONS TO ROLES
-- ========================================

-- SUPER_ADMIN gets all permissions
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r, permissions p
WHERE r.name = 'SUPER_ADMIN'
AND NOT EXISTS (
    SELECT 1 FROM role_permissions rp
    WHERE rp.role_id = r.id AND rp.permission_id = p.id
);

-- TENANT_ADMIN gets tenant and shop management permissions
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r, permissions p
WHERE r.name = 'TENANT_ADMIN'
AND p.name IN ('TENANT_MANAGE', 'SHOP_MANAGE', 'PRODUCT_MANAGE', 'SALES_CREATE')
AND NOT EXISTS (
    SELECT 1 FROM role_permissions rp
    WHERE rp.role_id = r.id AND rp.permission_id = p.id
);

-- SHOP_MANAGER gets shop operations permissions
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r, permissions p
WHERE r.name = 'SHOP_MANAGER'
AND p.name IN ('SHOP_MANAGE', 'PRODUCT_MANAGE', 'SALES_CREATE')
AND NOT EXISTS (
    SELECT 1 FROM role_permissions rp
    WHERE rp.role_id = r.id AND rp.permission_id = p.id
);

-- SHOP_EMPLOYEE gets sales permissions
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r, permissions p
WHERE r.name = 'SHOP_EMPLOYEE'
AND p.name IN ('SALES_CREATE')
AND NOT EXISTS (
    SELECT 1 FROM role_permissions rp
    WHERE rp.role_id = r.id AND rp.permission_id = p.id
);

-- INVESTOR gets investment view permissions
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r, permissions p
WHERE r.name = 'INVESTOR'
AND p.name IN ('INVESTMENT_VIEW')
AND NOT EXISTS (
    SELECT 1 FROM role_permissions rp
    WHERE rp.role_id = r.id AND rp.permission_id = p.id
);

-- ========================================
-- CREATE DEFAULT TENANT AND SHOP
-- ========================================

-- Insert default tenant if it doesn't exist
INSERT INTO tenants (id, name, description, contact_email, address, city, country, status, created_at, updated_at, version)
SELECT
    'default-tenant-id',
    'Default Tenant',
    'Default tenant for Shop Manager system',
    'admin@shopmanager.com',
    '123 Main Street',
    'Springfield',
    'USA',
    'ACTIVE',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP,
    0
WHERE NOT EXISTS (SELECT 1 FROM tenants WHERE id = 'default-tenant-id');

-- Insert default shop if it doesn't exist
INSERT INTO shops (id, tenant_id, name, description, email, address, city, country, phone_number, status, created_at, updated_at, version)
SELECT
    'default-shop-id',
    'default-tenant-id',
    'Default Shop',
    'Default shop for Shop Manager system',
    'shop@shopmanager.com',
    '456 Shop Street',
    'Springfield',
    'USA',
    '+1-555-0100',
    'ACTIVE',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP,
    0
WHERE NOT EXISTS (SELECT 1 FROM shops WHERE id = 'default-shop-id');

-- ========================================
-- FIX DATA TYPE INCONSISTENCIES
-- ========================================

-- Fix expense tables UUID vs VARCHAR inconsistencies
-- Change expense_categories shop_id to VARCHAR(36) to match shops table
ALTER TABLE expense_categories
  ALTER COLUMN shop_id TYPE VARCHAR(36) USING shop_id::VARCHAR(36);

-- Change expenses shop_id to VARCHAR(36) to match shops table
ALTER TABLE expenses
  ALTER COLUMN shop_id TYPE VARCHAR(36) USING shop_id::VARCHAR(36);

-- Change expenses user fields to VARCHAR(36) to match users table
ALTER TABLE expenses
  ALTER COLUMN expense_created_by TYPE VARCHAR(36) USING expense_created_by::VARCHAR(36),
  ALTER COLUMN approved_by TYPE VARCHAR(36) USING approved_by::VARCHAR(36);

-- Add foreign key constraints that were missing (with existence check)
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'fk_expense_categories_shop') THEN
        ALTER TABLE expense_categories
        ADD CONSTRAINT fk_expense_categories_shop
        FOREIGN KEY (shop_id) REFERENCES shops(id);
    END IF;
END$$;

DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'fk_expenses_shop') THEN
        ALTER TABLE expenses
        ADD CONSTRAINT fk_expenses_shop
        FOREIGN KEY (shop_id) REFERENCES shops(id);
    END IF;
END$$;

DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'fk_expenses_created_by') THEN
        ALTER TABLE expenses
        ADD CONSTRAINT fk_expenses_created_by
        FOREIGN KEY (expense_created_by) REFERENCES users(id);
    END IF;
END$$;

DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'fk_expenses_approved_by') THEN
        ALTER TABLE expenses
        ADD CONSTRAINT fk_expenses_approved_by
        FOREIGN KEY (approved_by) REFERENCES users(id);
    END IF;
END$$;

-- ========================================
-- UPDATE INDEXES FOR PERFORMANCE
-- ========================================

-- Add missing indexes for new columns
CREATE INDEX IF NOT EXISTS idx_inventory_last_stock_update ON inventory(last_stock_update);

-- Refresh statistics for query planner
ANALYZE inventory;
ANALYZE roles;
ANALYZE permissions;
ANALYZE role_permissions;