-- ========================================
-- Test Data SQL for Realistic Integration Testing with UUIDs
-- ========================================
-- This file contains realistic test data using UUID primary keys.
-- All data matches production database structure and uses PostgreSQL-specific features.
--
-- UUID Pattern: XXXe8400-e29b-41d4-a716-4466554400YY
-- - XXX = Entity type prefix (550=tenant, 650=shop, 750=user, etc.)
-- - YY = Sequential number (01, 02, 03, etc.)
--
-- Usage with TestContainers:
-- @Sql(scripts = "/test-data.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
-- ========================================

-- ========================================
-- 1. TENANTS (inserted without contact_user_id initially)
-- ========================================
INSERT INTO tenants (id, name, description, status, company_registration, tax_id, contact_email, primary_address, created_at, updated_at, version)
VALUES
    ('550e8400-e29b-41d4-a716-446655440001', 'Test Retail Corp', 'Test tenant for integration testing', 'INACTIVE', 'REG-TEST-001', 'TAX-TEST-001', 'admin@testretail.com', '123 Business St', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    ('550e8400-e29b-41d4-a716-446655440002', 'Demo Electronics Ltd', 'Demo tenant for testing multi-tenancy', 'INACTIVE', 'REG-TEST-002', 'TAX-TEST-002', 'admin@demoelectronics.com', '456 Commerce Ave', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0)
ON CONFLICT (id) DO NOTHING;

-- ========================================
-- 2. SHOPS (must be inserted before users since users now have shop_id FK)
-- ========================================
INSERT INTO shops (id, name, tenant_id, address, city, state, country, postal_code, phone_number, email, status, created_at, updated_at, version)
VALUES
    ('650e8400-e29b-41d4-a716-446655440001', 'Downtown Store', '550e8400-e29b-41d4-a716-446655440001', '123 Main St', 'New York', 'NY', 'USA', '10001', '+1-555-0101', 'downtown@testretail.com', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    ('650e8400-e29b-41d4-a716-446655440002', 'Uptown Branch', '550e8400-e29b-41d4-a716-446655440001', '456 Broadway', 'New York', 'NY', 'USA', '10002', '+1-555-0102', 'uptown@testretail.com', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    ('650e8400-e29b-41d4-a716-446655440003', 'Demo Shop', '550e8400-e29b-41d4-a716-446655440002', '789 Tech Ave', 'San Francisco', 'CA', 'USA', '94102', '+1-555-0103', 'demo@electronics.com', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0)
ON CONFLICT (id) DO NOTHING;

-- ========================================
-- 3. USERS (Keycloak synced users) - Now with shop assignments
-- ========================================
-- Note: Users are now assigned to specific shops (branches)
-- User 001, 002, 004 -> Downtown Store (Shop 001)
-- User 003 -> Uptown Branch (Shop 002)
-- User 005 (investor) -> Downtown Store (Shop 001)
INSERT INTO users (id, keycloak_id, tenant_id, shop_id, username, email, first_name, last_name, phone_number, status, created_at, updated_at, version)
VALUES
    ('750e8400-e29b-41d4-a716-446655440001', 'kc-admin-001', '550e8400-e29b-41d4-a716-446655440001', '650e8400-e29b-41d4-a716-446655440001', 'admin@testretail.com', 'admin@testretail.com', 'System', 'Admin', '+1-555-1001', 'INACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    ('750e8400-e29b-41d4-a716-446655440002', 'kc-owner-001', '550e8400-e29b-41d4-a716-446655440001', '650e8400-e29b-41d4-a716-446655440001', 'owner@testretail.com', 'owner@testretail.com', 'John', 'Owner', '+1-555-1002', 'INACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    ('750e8400-e29b-41d4-a716-446655440003', 'kc-manager-001', '550e8400-e29b-41d4-a716-446655440001', '650e8400-e29b-41d4-a716-446655440002', 'manager@testretail.com', 'manager@testretail.com', 'Jane', 'Manager', '+1-555-1003', 'INACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    ('750e8400-e29b-41d4-a716-446655440004', 'kc-employee-001', '550e8400-e29b-41d4-a716-446655440001', '650e8400-e29b-41d4-a716-446655440001', 'employee@testretail.com', 'employee@testretail.com', 'Bob', 'Employee', '+1-555-1004', 'INACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    ('750e8400-e29b-41d4-a716-446655440005', 'kc-investor-001', '550e8400-e29b-41d4-a716-446655440001', '650e8400-e29b-41d4-a716-446655440001', 'investor@testretail.com', 'investor@testretail.com', 'Alice', 'Investor', '+1-555-1005', 'INACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0)
ON CONFLICT (id) DO NOTHING;

-- ========================================
-- 4. TEST ROLES WITH RECOGNIZABLE IDS
-- ========================================
-- Create test-specific roles with stable, recognizable IDs
-- Pattern: test-role-{name} (e.g., test-role-admin, test-role-owner)
-- These roles are separate from production Flyway-created roles
INSERT INTO roles (id, name, description, is_system, tenant_id, created_at, updated_at, version) VALUES
    ('test-role-admin', 'TEST_ADMIN', 'Test System Administrator', true, null, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    ('test-role-owner', 'TEST_OWNER', 'Test Shop Owner', true, null, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    ('test-role-manager', 'TEST_MANAGER', 'Test Shop Manager', true, null, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    ('test-role-employee', 'TEST_EMPLOYEE', 'Test Shop Employee', true, null, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    ('test-role-investor', 'TEST_INVESTOR', 'Test Investor', true, null, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0)
ON CONFLICT (id) DO NOTHING;

-- ========================================
-- 4.5. ASSIGN PERMISSIONS TO TEST ROLES
-- ========================================
-- Grant permissions to test roles by looking up permission IDs from Flyway migrations
-- Uses SELECT to find permissions by name (since Flyway creates them with auto-generated IDs)

-- TEST_ADMIN gets all permissions (matches WithMockPermissionsSecurityContextFactory SYSTEM_ADMIN/SUPER_ADMIN)
INSERT INTO role_permissions (role_id, permission_id)
SELECT 'test-role-admin', p.id
FROM permissions p
WHERE p.name IN (
    -- System Admin
    'SYSTEM_ADMIN',
    -- Tenant
    'TENANT_CREATE', 'TENANT_READ', 'TENANT_LIST', 'TENANT_UPDATE', 'TENANT_DELETE',
    -- Shop
    'SHOP_CREATE', 'SHOP_READ', 'SHOP_LIST', 'SHOP_LIST_ALL', 'SHOP_UPDATE', 'SHOP_DELETE',
    -- User
    'USER_CREATE', 'USER_READ', 'USER_LIST', 'USER_LIST_ALL', 'USER_UPDATE', 'USER_DELETE',
    -- Role
    'ROLE_CREATE', 'ROLE_READ', 'ROLE_LIST', 'ROLE_UPDATE', 'ROLE_DELETE', 'ROLE_ASSIGN',
    -- Permission
    'PERMISSION_READ', 'PERMISSION_LIST',
    -- Product
    'PRODUCT_CREATE', 'PRODUCT_READ', 'PRODUCT_LIST', 'PRODUCT_UPDATE', 'PRODUCT_DELETE',
    -- Category
    'CATEGORY_CREATE', 'CATEGORY_READ', 'CATEGORY_LIST', 'CATEGORY_UPDATE', 'CATEGORY_DELETE',
    -- Inventory
    'INVENTORY_CREATE', 'INVENTORY_READ', 'INVENTORY_LIST', 'INVENTORY_UPDATE', 'INVENTORY_DELETE',
    'INVENTORY_ADJUST', 'INVENTORY_RESERVE', 'INVENTORY_HISTORY', 'INVENTORY_HISTORY_VIEW', 'INVENTORY_FORECAST',
    -- Sales
    'SALES_CREATE', 'SALES_READ', 'SALES_LIST', 'SALES_UPDATE', 'SALES_DELETE', 'SALES_VOID',
    -- Receipt
    'RECEIPT_CREATE', 'RECEIPT_READ', 'RECEIPT_LIST', 'RECEIPT_SEND', 'RECEIPT_EMAIL',
    -- Expense
    'EXPENSE_CREATE', 'EXPENSE_READ', 'EXPENSE_LIST', 'EXPENSE_UPDATE', 'EXPENSE_DELETE', 'EXPENSE_APPROVE',
    'EXPENSE_CATEGORY_CREATE', 'EXPENSE_CATEGORY_READ', 'EXPENSE_CATEGORY_LIST', 'EXPENSE_CATEGORY_UPDATE', 'EXPENSE_CATEGORY_DELETE',
    -- Investment
    'INVESTMENT_CREATE', 'INVESTMENT_READ', 'INVESTMENT_LIST', 'INVESTMENT_UPDATE', 'INVESTMENT_DELETE',
    'INVESTMENT_CLOSE', 'INVESTMENT_PROFIT_DISTRIBUTE',
    -- Return
    'RETURN_CREATE', 'RETURN_READ', 'RETURN_LIST', 'RETURN_UPDATE', 'RETURN_DELETE', 'RETURN_APPROVE',
    -- Audit
    'AUDIT_LOG_VIEW_SHOP', 'AUDIT_LOG_VIEW_TENANT',
    -- Analytics
    'ANALYTICS_SALES_VIEW', 'ANALYTICS_INVESTMENT_VIEW', 'ANALYTICS_MANAGE',
    -- Fraud
    'FRAUD_VIEW', 'FRAUD_MANAGE', 'FRAUD_LIST', 'FRAUD_INVESTIGATE', 'FRAUD_RESOLVE', 'FRAUD_DETECT'
)
ON CONFLICT DO NOTHING;

-- TEST_OWNER gets owner permissions (matches WithMockPermissionsSecurityContextFactory)
INSERT INTO role_permissions (role_id, permission_id)
SELECT 'test-role-owner', p.id
FROM permissions p
WHERE p.name IN (
    -- Tenant
    'TENANT_READ',
    -- Shop
    'SHOP_CREATE', 'SHOP_READ', 'SHOP_LIST', 'SHOP_LIST_ALL', 'SHOP_UPDATE', 'SHOP_DELETE',
    -- User
    'USER_CREATE', 'USER_READ', 'USER_LIST', 'USER_UPDATE', 'USER_DELETE',
    -- Role
    'ROLE_CREATE', 'ROLE_READ', 'ROLE_LIST', 'ROLE_UPDATE', 'ROLE_DELETE', 'ROLE_ASSIGN',
    -- Permission
    'PERMISSION_READ', 'PERMISSION_LIST',
    -- Product
    'PRODUCT_CREATE', 'PRODUCT_READ', 'PRODUCT_LIST', 'PRODUCT_UPDATE', 'PRODUCT_DELETE',
    -- Category
    'CATEGORY_CREATE', 'CATEGORY_READ', 'CATEGORY_LIST', 'CATEGORY_UPDATE', 'CATEGORY_DELETE',
    -- Inventory
    'INVENTORY_CREATE', 'INVENTORY_READ', 'INVENTORY_LIST', 'INVENTORY_UPDATE', 'INVENTORY_DELETE',
    'INVENTORY_ADJUST', 'INVENTORY_RESERVE', 'INVENTORY_HISTORY', 'INVENTORY_HISTORY_VIEW', 'INVENTORY_FORECAST',
    -- Sales
    'SALES_CREATE', 'SALES_READ', 'SALES_LIST', 'SALES_UPDATE', 'SALES_DELETE', 'SALES_VOID',
    -- Receipt
    'RECEIPT_CREATE', 'RECEIPT_READ', 'RECEIPT_LIST', 'RECEIPT_SEND', 'RECEIPT_EMAIL',
    -- Expense
    'EXPENSE_CREATE', 'EXPENSE_READ', 'EXPENSE_LIST', 'EXPENSE_UPDATE', 'EXPENSE_DELETE', 'EXPENSE_APPROVE',
    'EXPENSE_CATEGORY_CREATE', 'EXPENSE_CATEGORY_READ', 'EXPENSE_CATEGORY_LIST', 'EXPENSE_CATEGORY_UPDATE', 'EXPENSE_CATEGORY_DELETE',
    -- Investment
    'INVESTMENT_CREATE', 'INVESTMENT_READ', 'INVESTMENT_LIST', 'INVESTMENT_UPDATE', 'INVESTMENT_DELETE',
    'INVESTMENT_CLOSE', 'INVESTMENT_PROFIT_DISTRIBUTE',
    -- Return
    'RETURN_CREATE', 'RETURN_READ', 'RETURN_LIST', 'RETURN_UPDATE', 'RETURN_DELETE', 'RETURN_APPROVE',
    -- Audit
    'AUDIT_LOG_VIEW_SHOP', 'AUDIT_LOG_VIEW_TENANT',
    -- Analytics
    'ANALYTICS_SALES_VIEW', 'ANALYTICS_INVESTMENT_VIEW', 'ANALYTICS_MANAGE',
    -- Fraud
    'FRAUD_VIEW', 'FRAUD_MANAGE', 'FRAUD_LIST', 'FRAUD_INVESTIGATE', 'FRAUD_RESOLVE', 'FRAUD_DETECT'
)
ON CONFLICT DO NOTHING;

-- TEST_MANAGER gets manager permissions (matches WithMockPermissionsSecurityContextFactory)
INSERT INTO role_permissions (role_id, permission_id)
SELECT 'test-role-manager', p.id
FROM permissions p
WHERE p.name IN (
    -- Shop
    'SHOP_READ', 'SHOP_LIST',
    -- User
    'USER_CREATE', 'USER_READ', 'USER_LIST', 'USER_UPDATE',
    -- Role
    'ROLE_READ', 'ROLE_LIST',
    -- Product
    'PRODUCT_CREATE', 'PRODUCT_READ', 'PRODUCT_LIST', 'PRODUCT_UPDATE',
    -- Category
    'CATEGORY_CREATE', 'CATEGORY_READ', 'CATEGORY_LIST', 'CATEGORY_UPDATE',
    -- Inventory
    'INVENTORY_CREATE', 'INVENTORY_READ', 'INVENTORY_LIST', 'INVENTORY_UPDATE', 'INVENTORY_DELETE',
    'INVENTORY_ADJUST', 'INVENTORY_RESERVE', 'INVENTORY_HISTORY', 'INVENTORY_HISTORY_VIEW', 'INVENTORY_FORECAST',
    -- Sales
    'SALES_CREATE', 'SALES_READ', 'SALES_LIST', 'SALES_UPDATE', 'SALES_VOID',
    -- Receipt
    'RECEIPT_CREATE', 'RECEIPT_READ', 'RECEIPT_LIST', 'RECEIPT_SEND', 'RECEIPT_EMAIL',
    -- Expense
    'EXPENSE_CREATE', 'EXPENSE_READ', 'EXPENSE_LIST', 'EXPENSE_UPDATE', 'EXPENSE_APPROVE',
    'EXPENSE_CATEGORY_CREATE', 'EXPENSE_CATEGORY_READ', 'EXPENSE_CATEGORY_LIST', 'EXPENSE_CATEGORY_UPDATE',
    -- Investment
    'INVESTMENT_CREATE', 'INVESTMENT_READ', 'INVESTMENT_LIST', 'INVESTMENT_UPDATE',
    -- Return
    'RETURN_CREATE', 'RETURN_READ', 'RETURN_LIST', 'RETURN_UPDATE', 'RETURN_APPROVE',
    -- Audit
    'AUDIT_LOG_VIEW_SHOP',
    -- Analytics
    'ANALYTICS_SALES_VIEW', 'ANALYTICS_INVESTMENT_VIEW',
    -- Fraud
    'FRAUD_VIEW', 'FRAUD_LIST', 'FRAUD_INVESTIGATE'
)
ON CONFLICT DO NOTHING;

-- TEST_EMPLOYEE gets employee permissions (matches WithMockPermissionsSecurityContextFactory CASHIER role)
INSERT INTO role_permissions (role_id, permission_id)
SELECT 'test-role-employee', p.id
FROM permissions p
WHERE p.name IN (
    -- Product
    'PRODUCT_READ', 'PRODUCT_LIST',
    -- Category
    'CATEGORY_READ', 'CATEGORY_LIST',
    -- Inventory
    'INVENTORY_CREATE', 'INVENTORY_READ', 'INVENTORY_LIST', 'INVENTORY_HISTORY',
    -- Sales
    'SALES_CREATE', 'SALES_READ', 'SALES_LIST',
    -- Receipt
    'RECEIPT_CREATE', 'RECEIPT_READ', 'RECEIPT_LIST', 'RECEIPT_SEND', 'RECEIPT_EMAIL',
    -- Expense
    'EXPENSE_CREATE', 'EXPENSE_READ', 'EXPENSE_LIST',
    'EXPENSE_CATEGORY_READ', 'EXPENSE_CATEGORY_LIST',
    -- Return
    'RETURN_CREATE', 'RETURN_READ', 'RETURN_LIST'
)
ON CONFLICT DO NOTHING;

-- TEST_INVESTOR gets investor permissions (matches WithMockPermissionsSecurityContextFactory)
INSERT INTO role_permissions (role_id, permission_id)
SELECT 'test-role-investor', p.id
FROM permissions p
WHERE p.name IN (
    -- Investment
    'INVESTMENT_CREATE', 'INVESTMENT_READ', 'INVESTMENT_LIST',
    -- Analytics
    'ANALYTICS_INVESTMENT_VIEW'
)
ON CONFLICT DO NOTHING;

-- ========================================
-- 5. USER-ROLE ASSIGNMENTS
-- ========================================
-- Assign test roles to test users using recognizable test role IDs
INSERT INTO user_roles (user_id, role_id) VALUES
    ('750e8400-e29b-41d4-a716-446655440001', 'test-role-admin'),     -- admin@testretail.com
    ('750e8400-e29b-41d4-a716-446655440002', 'test-role-owner'),      -- owner@testretail.com
    ('750e8400-e29b-41d4-a716-446655440003', 'test-role-manager'),    -- manager@testretail.com
    ('750e8400-e29b-41d4-a716-446655440004', 'test-role-employee'),   -- employee@testretail.com
    ('750e8400-e29b-41d4-a716-446655440005', 'test-role-investor')    -- investor@testretail.com
ON CONFLICT DO NOTHING;

-- ========================================
-- 6. USER-SHOP ASSIGNMENTS
-- ========================================
-- Note: User-Shop relationship is tenant-based, not direct shop assignment
-- Users belong to a tenant and have access to all shops within that tenant

-- ========================================
-- 7. CATEGORIES
-- ========================================
INSERT INTO categories (id, name, shop_id, description, created_at, updated_at, version)
VALUES
    ('950e8400-e29b-41d4-a716-446655440001', 'Electronics', '650e8400-e29b-41d4-a716-446655440001', 'Electronic devices and accessories', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    ('950e8400-e29b-41d4-a716-446655440002', 'Clothing', '650e8400-e29b-41d4-a716-446655440001', 'Apparel and fashion items', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    ('950e8400-e29b-41d4-a716-446655440003', 'Food & Beverage', '650e8400-e29b-41d4-a716-446655440001', 'Food and drink products', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0)
ON CONFLICT (id) DO NOTHING;

-- ========================================
-- 8. PRODUCTS (Master Catalog)
-- Note: price and cost_price removed in V32 - pricing is now in inventory table
-- ========================================
INSERT INTO products (id, name, description, sku, barcode, category_id, shop_id, status, created_at, updated_at, version)
VALUES
    ('850e8400-e29b-41d4-a716-446655440001', 'Wireless Mouse', 'Ergonomic wireless mouse with USB receiver', 'MOUSE-001', '1234567890123', '950e8400-e29b-41d4-a716-446655440001', '650e8400-e29b-41d4-a716-446655440001', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    ('850e8400-e29b-41d4-a716-446655440002', 'USB Keyboard', 'Standard USB keyboard with numeric pad', 'KB-001', '1234567890124', '950e8400-e29b-41d4-a716-446655440001', '650e8400-e29b-41d4-a716-446655440001', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    ('850e8400-e29b-41d4-a716-446655440003', 'Cotton T-Shirt', 'Premium cotton t-shirt, multiple sizes', 'TSHIRT-001', '1234567890125', '950e8400-e29b-41d4-a716-446655440002', '650e8400-e29b-41d4-a716-446655440001', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    ('850e8400-e29b-41d4-a716-446655440004', 'Energy Drink', 'Refreshing energy drink, 250ml can', 'DRINK-001', '1234567890126', '950e8400-e29b-41d4-a716-446655440003', '650e8400-e29b-41d4-a716-446655440001', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0)
ON CONFLICT (id) DO NOTHING;

-- ========================================
-- 9. INVENTORY (Stock Tracking)
-- Note: Inventory is shop-scoped only. Tenant is derived via shop.tenant relationship.
-- Note: V32 renamed unit_cost to cost_price and added selling_price (batch-specific pricing)
-- ========================================
INSERT INTO inventory (id, product_id, shop_id, current_stock, reserved_stock, minimum_stock, maximum_stock, reorder_point, cost_price, selling_price, location, batch_number, expiry_date, status, created_at, updated_at, version)
VALUES
    ('a50e8400-e29b-41d4-a716-446655440001', '850e8400-e29b-41d4-a716-446655440001', '650e8400-e29b-41d4-a716-446655440001', 100, 5, 20, 200, 30, 15.00, 25.99, 'A1-B2', 'BATCH-MOUSE-001', NULL, 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    ('a50e8400-e29b-41d4-a716-446655440002', '850e8400-e29b-41d4-a716-446655440002', '650e8400-e29b-41d4-a716-446655440001', 75, 3, 15, 150, 25, 20.00, 35.99, 'A2-C1', 'BATCH-KB-001', NULL, 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    ('a50e8400-e29b-41d4-a716-446655440003', '850e8400-e29b-41d4-a716-446655440003', '650e8400-e29b-41d4-a716-446655440001', 200, 10, 50, 500, 75, 10.00, 19.99, 'B1-A3', 'BATCH-SHIRT-001', NULL, 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    ('a50e8400-e29b-41d4-a716-446655440004', '850e8400-e29b-41d4-a716-446655440004', '650e8400-e29b-41d4-a716-446655440001', 500, 0, 100, 1000, 150, 1.50, 2.99, 'C1-D2', 'BATCH-DRINK-001', CURRENT_DATE + INTERVAL '6 months', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0)
ON CONFLICT (id) DO NOTHING;

-- ========================================
-- 10. EXPENSE CATEGORIES
-- Note: Expense categories are shop-scoped. Tenant is derived via shop.tenant.
-- ========================================
INSERT INTO expense_categories (id, name, description, shop_id, is_active, requires_approval, tax_deductible, auto_approval_enabled, created_at, updated_at, version)
VALUES
    ('b50e8400-e29b-41d4-a716-446655440001', 'Utilities', 'Electricity, water, internet, etc.', '650e8400-e29b-41d4-a716-446655440001', true, true, true, false, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    ('b50e8400-e29b-41d4-a716-446655440002', 'Rent', 'Shop rental payments', '650e8400-e29b-41d4-a716-446655440001', true, true, true, false, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    ('b50e8400-e29b-41d4-a716-446655440003', 'Salaries', 'Employee salaries and wages', '650e8400-e29b-41d4-a716-446655440001', true, true, true, false, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0)
ON CONFLICT (id) DO NOTHING;

-- ========================================
-- 11. SALES TRANSACTIONS
-- Note: Shop-scoped only. Requires subtotal. Status column added in V7.
-- ========================================
INSERT INTO sales_transactions (id, transaction_number, shop_id, cashier_id, customer_name, customer_email, customer_phone, subtotal, total_amount, payment_method, transaction_date, status, created_at, updated_at, version)
VALUES
    ('c50e8400-e29b-41d4-a716-446655440001', 'TXN-2024-001', '650e8400-e29b-41d4-a716-446655440001', '750e8400-e29b-41d4-a716-446655440004', 'John Customer', 'john@customer.com', '+1-555-2001', 51.98, 51.98, 'CASH', CURRENT_TIMESTAMP - INTERVAL '2 days', 'COMPLETED', CURRENT_TIMESTAMP - INTERVAL '2 days', CURRENT_TIMESTAMP - INTERVAL '2 days', 0),
    ('c50e8400-e29b-41d4-a716-446655440002', 'TXN-2024-002', '650e8400-e29b-41d4-a716-446655440001', '750e8400-e29b-41d4-a716-446655440004', 'Jane Customer', 'jane@customer.com', '+1-555-2002', 25.99, 25.99, 'CARD', CURRENT_TIMESTAMP - INTERVAL '1 day', 'COMPLETED', CURRENT_TIMESTAMP - INTERVAL '1 day', CURRENT_TIMESTAMP - INTERVAL '1 day', 0),
    ('c50e8400-e29b-41d4-a716-446655440003', 'TXN-2024-003', '650e8400-e29b-41d4-a716-446655440001', '750e8400-e29b-41d4-a716-446655440004', NULL, NULL, NULL, 19.99, 19.99, 'CASH', CURRENT_TIMESTAMP, 'COMPLETED', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0)
ON CONFLICT (id) DO NOTHING;

-- ========================================
-- 12. LINE ITEMS (Sales Transaction Items)
-- Note: V32 added product_sku and product_category for denormalization
-- Note: Only using core required columns to avoid schema mismatch issues
-- ========================================
INSERT INTO line_items (id, transaction_id, product_id, product_name, product_sku, quantity, unit_price, discount_amount, tax_amount, line_total, created_at, updated_at, version)
VALUES
    ('d50e8400-e29b-41d4-a716-446655440001', 'c50e8400-e29b-41d4-a716-446655440001', '850e8400-e29b-41d4-a716-446655440001', 'Wireless Mouse', 'MOUSE-001', 2, 25.99, 0, 0, 51.98, CURRENT_TIMESTAMP - INTERVAL '2 days', CURRENT_TIMESTAMP - INTERVAL '2 days', 0),
    ('d50e8400-e29b-41d4-a716-446655440002', 'c50e8400-e29b-41d4-a716-446655440002', '850e8400-e29b-41d4-a716-446655440001', 'Wireless Mouse', 'MOUSE-001', 1, 25.99, 0, 0, 25.99, CURRENT_TIMESTAMP - INTERVAL '1 day', CURRENT_TIMESTAMP - INTERVAL '1 day', 0),
    ('d50e8400-e29b-41d4-a716-446655440003', 'c50e8400-e29b-41d4-a716-446655440003', '850e8400-e29b-41d4-a716-446655440003', 'Cotton T-Shirt', 'TSHIRT-001', 1, 19.99, 0, 0, 19.99, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0)
ON CONFLICT (id) DO NOTHING;

-- ========================================
-- 13. RECEIPTS
-- Note: No tenant_id. Receipt linked to transaction which is shop-scoped.
-- ========================================
INSERT INTO receipts (id, receipt_number, transaction_id, issued_date, generated_at, format, status, created_at, updated_at, version)
VALUES
    ('e50e8400-e29b-41d4-a716-446655440001', 'RCP-2024-001', 'c50e8400-e29b-41d4-a716-446655440001', CURRENT_TIMESTAMP - INTERVAL '2 days', CURRENT_TIMESTAMP - INTERVAL '2 days', 'PDF', 'GENERATED', CURRENT_TIMESTAMP - INTERVAL '2 days', CURRENT_TIMESTAMP - INTERVAL '2 days', 0),
    ('e50e8400-e29b-41d4-a716-446655440002', 'RCP-2024-002', 'c50e8400-e29b-41d4-a716-446655440002', CURRENT_TIMESTAMP - INTERVAL '1 day', CURRENT_TIMESTAMP - INTERVAL '1 day', 'PDF', 'GENERATED', CURRENT_TIMESTAMP - INTERVAL '1 day', CURRENT_TIMESTAMP - INTERVAL '1 day', 0)
ON CONFLICT (id) DO NOTHING;

-- ========================================
-- 14. EXPENSES
-- Note: Uses title, expense_created_by, approved_by. No tenant_id or expense_number.
-- ========================================
INSERT INTO expenses (id, shop_id, title, description, category_id, amount, expense_date, expense_created_by, approved_by, status, created_at, updated_at, version)
VALUES
    ('f50e8400-e29b-41d4-a716-446655440001', '650e8400-e29b-41d4-a716-446655440001', 'Electricity Bill', 'Monthly electricity bill', 'b50e8400-e29b-41d4-a716-446655440001', 250.00, CURRENT_DATE - INTERVAL '5 days', '750e8400-e29b-41d4-a716-446655440003', '750e8400-e29b-41d4-a716-446655440002', 'APPROVED', CURRENT_TIMESTAMP - INTERVAL '5 days', CURRENT_TIMESTAMP - INTERVAL '4 days', 0),
    ('f50e8400-e29b-41d4-a716-446655440002', '650e8400-e29b-41d4-a716-446655440001', 'Rent Payment', 'Monthly rent payment', 'b50e8400-e29b-41d4-a716-446655440002', 2000.00, CURRENT_DATE - INTERVAL '3 days', '750e8400-e29b-41d4-a716-446655440003', NULL, 'PENDING_APPROVAL', CURRENT_TIMESTAMP - INTERVAL '3 days', CURRENT_TIMESTAMP - INTERVAL '3 days', 0),
    ('f50e8400-e29b-41d4-a716-446655440003', '650e8400-e29b-41d4-a716-446655440001', 'Salaries', 'Monthly salaries', 'b50e8400-e29b-41d4-a716-446655440003', 5000.00, CURRENT_DATE - INTERVAL '1 day', '750e8400-e29b-41d4-a716-446655440003', '750e8400-e29b-41d4-a716-446655440002', 'APPROVED', CURRENT_TIMESTAMP - INTERVAL '1 day', CURRENT_TIMESTAMP, 0)
ON CONFLICT (id) DO NOTHING;

-- ========================================
-- 15. INVESTMENTS
-- TODO: Investment schema changed - needs update
-- ========================================
-- Commented out - schema mismatch (test data has name/description/start_date/end_date, DB has investor_id/profit_sharing_model)
-- INSERT INTO investments ...

-- ========================================
-- 16. INVESTOR SHARES
-- ========================================
-- Commented out - depends on investments which is commented out
-- INSERT INTO investor_shares ...

-- ========================================
-- 17. PRODUCT RETURNS
-- TODO: Verify schema matches
-- ========================================
-- Commented out for now - may have schema mismatches
-- INSERT INTO product_returns ...

-- ========================================
-- 18. RETURN ITEMS
-- ========================================
-- Commented out - depends on product_returns
-- INSERT INTO return_items ...

-- ========================================
-- 19. INVENTORY MOVEMENTS
-- TODO: Verify schema matches
-- ========================================
-- Commented out for now - may have schema mismatches
-- INSERT INTO inventory_movements ...

-- ========================================
-- 20. AUDIT LOGS
-- TODO: Verify schema matches
-- ========================================
-- Commented out for now - may have schema mismatches
-- INSERT INTO audit_logs ...

-- ========================================
-- 21. FEATURE FLAGS
-- TODO: Verify schema matches
-- ========================================
-- Commented out for now - may have schema mismatches
-- INSERT INTO feature_flags ...

-- ========================================
-- NOTES:
-- ========================================
-- All IDs use UUID format with sequential patterns for easy identification:
-- - 550e84XX... = Tenants
-- - 650e84XX... = Shops
-- - 750e84XX... = Users
-- - 850e84XX... = Products
-- - 950e84XX... = Categories
-- - a50e84XX... = Inventory
-- - b50e84XX... = Expense Categories
-- - c50e84XX... = Sales Transactions
-- - d50e84XX... = Transaction Items
-- - e50e84XX... = Receipts
-- - f50e84XX... = Expenses
-- - 050e84XX... = Investments (note leading zero)
-- - 150e84XX... = Investor Shares
-- - 250e84XX... = Returns
-- - 350e84XX... = Return Items
-- - 450e84XX... = Inventory Movements
--
-- Entity Counts (for test assertions - use TestConstants):
-- - 2 Tenants, 3 Shops, 5 Users (1 per role)
-- - 3 Categories, 4 Products, 4 Inventory records
-- - 3 Expense Categories, 3 Sales Transactions, 3 Transaction Items
-- - 2 Receipts, 3 Expenses, 2 Investments, 4 Investor Shares
-- - 1 Product Return, 4 Inventory Movements, 3 Audit Logs, 3 Feature Flags
--
-- Usage with TestContainers + @Sql:
-- class MyControllerTest extends AbstractIntegrationTest {
--     // test-data.sql automatically loaded via AbstractIntegrationTest
--     // Use TestConstants for all IDs and counts
-- }
