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
-- 4. ROLES (System roles for testing)
-- ========================================
-- Insert system roles with same IDs as migrations for consistency
INSERT INTO roles (id, name, description, is_system, created_at, updated_at, version)
VALUES
    ('super-admin-role-id', 'SYSTEM_ADMIN', 'System Administrator with full access to all tenants', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    ('tenant-admin-role-id', 'TENANT_ADMIN', 'Tenant Administrator with full access to tenant resources', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    ('owner-role-id', 'OWNER', 'Shop Owner with full business control', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    ('manager-role-id', 'MANAGER', 'Shop Manager with operational access', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    ('employee-role-id', 'EMPLOYEE', 'Shop Employee with basic access', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    ('investor-role-id', 'INVESTOR', 'Investor with investment management access', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0)
ON CONFLICT (id) DO NOTHING;

-- ========================================
-- 4.5. PERMISSIONS AND ROLE-PERMISSION ASSIGNMENTS
-- ========================================
-- NOTE: In tests, Flyway migrations run but their data is not visible within test transactions.
-- We must explicitly insert permissions and role_permissions here for tests to work.

-- Insert investment-related permissions needed for tests
INSERT INTO permissions (id, name, description, resource, action, created_at, updated_at, version)
VALUES
    ('perm-investment-create', 'INVESTMENT_CREATE', 'Create investments', 'INVESTMENT', 'CREATE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    ('perm-investment-read', 'INVESTMENT_READ', 'View investment details', 'INVESTMENT', 'READ', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    ('perm-investment-list', 'INVESTMENT_LIST', 'List investments', 'INVESTMENT', 'LIST', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    ('perm-investment-update', 'INVESTMENT_UPDATE', 'Edit investment records', 'INVESTMENT', 'UPDATE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    ('perm-investment-delete', 'INVESTMENT_DELETE', 'Delete investment records', 'INVESTMENT', 'DELETE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    ('perm-investment-close', 'INVESTMENT_CLOSE', 'Close investments', 'INVESTMENT', 'CLOSE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    ('perm-investment-profit-distribute', 'INVESTMENT_PROFIT_DISTRIBUTE', 'Distribute investment profits', 'INVESTMENT', 'PROFIT_DISTRIBUTE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    ('perm-analytics-investment-view', 'ANALYTICS_INVESTMENT_VIEW', 'View investment ROI analytics', 'ANALYTICS', 'INVESTMENT_VIEW', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0)
ON CONFLICT (id) DO NOTHING;

-- Grant permissions to SYSTEM_ADMIN role (all investment permissions)
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r, permissions p
WHERE r.name = 'SYSTEM_ADMIN'
AND p.name IN (
    'INVESTMENT_CREATE', 'INVESTMENT_READ', 'INVESTMENT_LIST', 'INVESTMENT_UPDATE', 'INVESTMENT_DELETE',
    'INVESTMENT_CLOSE', 'INVESTMENT_PROFIT_DISTRIBUTE', 'ANALYTICS_INVESTMENT_VIEW'
)
ON CONFLICT DO NOTHING;

-- Grant permissions to OWNER role
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r, permissions p
WHERE r.name = 'OWNER'
AND p.name IN (
    'INVESTMENT_CREATE', 'INVESTMENT_READ', 'INVESTMENT_LIST', 'INVESTMENT_UPDATE', 'INVESTMENT_DELETE',
    'INVESTMENT_CLOSE', 'INVESTMENT_PROFIT_DISTRIBUTE', 'ANALYTICS_INVESTMENT_VIEW'
)
ON CONFLICT DO NOTHING;

-- Grant permissions to MANAGER role
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r, permissions p
WHERE r.name = 'MANAGER'
AND p.name IN (
    'INVESTMENT_CREATE', 'INVESTMENT_READ', 'INVESTMENT_LIST', 'INVESTMENT_UPDATE',
    'ANALYTICS_INVESTMENT_VIEW'
)
ON CONFLICT DO NOTHING;

-- Grant permissions to INVESTOR role
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r, permissions p
WHERE r.name = 'INVESTOR'
AND p.name IN (
    'INVESTMENT_CREATE', 'INVESTMENT_READ', 'INVESTMENT_LIST', 'ANALYTICS_INVESTMENT_VIEW'
)
ON CONFLICT DO NOTHING;

-- ========================================
-- 5. USER-ROLE ASSIGNMENTS
-- ========================================
INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id
FROM users u, roles r
WHERE (u.id = '750e8400-e29b-41d4-a716-446655440001' AND r.name = 'SYSTEM_ADMIN')
   OR (u.id = '750e8400-e29b-41d4-a716-446655440002' AND r.name = 'OWNER')
   OR (u.id = '750e8400-e29b-41d4-a716-446655440003' AND r.name = 'MANAGER')
   OR (u.id = '750e8400-e29b-41d4-a716-446655440004' AND r.name = 'EMPLOYEE')
   OR (u.id = '750e8400-e29b-41d4-a716-446655440005' AND r.name = 'INVESTOR')
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
-- ========================================
INSERT INTO products (id, name, description, sku, barcode, category_id, shop_id, price, cost_price, status, created_at, updated_at, version)
VALUES
    ('850e8400-e29b-41d4-a716-446655440001', 'Wireless Mouse', 'Ergonomic wireless mouse with USB receiver', 'MOUSE-001', '1234567890123', '950e8400-e29b-41d4-a716-446655440001', '650e8400-e29b-41d4-a716-446655440001', 25.99, 15.00, 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    ('850e8400-e29b-41d4-a716-446655440002', 'USB Keyboard', 'Standard USB keyboard with numeric pad', 'KB-001', '1234567890124', '950e8400-e29b-41d4-a716-446655440001', '650e8400-e29b-41d4-a716-446655440001', 35.99, 20.00, 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    ('850e8400-e29b-41d4-a716-446655440003', 'Cotton T-Shirt', 'Premium cotton t-shirt, multiple sizes', 'TSHIRT-001', '1234567890125', '950e8400-e29b-41d4-a716-446655440002', '650e8400-e29b-41d4-a716-446655440001', 19.99, 10.00, 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    ('850e8400-e29b-41d4-a716-446655440004', 'Energy Drink', 'Refreshing energy drink, 250ml can', 'DRINK-001', '1234567890126', '950e8400-e29b-41d4-a716-446655440003', '650e8400-e29b-41d4-a716-446655440001', 2.99, 1.50, 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0)
ON CONFLICT (id) DO NOTHING;

-- ========================================
-- 9. INVENTORY (Stock Tracking)
-- Note: Inventory is shop-scoped only. Tenant is derived via shop.tenant relationship.
-- ========================================
INSERT INTO inventory (id, product_id, shop_id, current_stock, reserved_stock, minimum_stock, maximum_stock, reorder_point, unit_cost, location, batch_number, expiry_date, status, created_at, updated_at, version)
VALUES
    ('a50e8400-e29b-41d4-a716-446655440001', '850e8400-e29b-41d4-a716-446655440001', '650e8400-e29b-41d4-a716-446655440001', 100, 5, 20, 200, 30, 15.00, 'A1-B2', 'BATCH-MOUSE-001', NULL, 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    ('a50e8400-e29b-41d4-a716-446655440002', '850e8400-e29b-41d4-a716-446655440002', '650e8400-e29b-41d4-a716-446655440001', 75, 3, 15, 150, 25, 20.00, 'A2-C1', 'BATCH-KB-001', NULL, 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    ('a50e8400-e29b-41d4-a716-446655440003', '850e8400-e29b-41d4-a716-446655440003', '650e8400-e29b-41d4-a716-446655440001', 200, 10, 50, 500, 75, 10.00, 'B1-A3', 'BATCH-SHIRT-001', NULL, 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    ('a50e8400-e29b-41d4-a716-446655440004', '850e8400-e29b-41d4-a716-446655440004', '650e8400-e29b-41d4-a716-446655440001', 500, 0, 100, 1000, 150, 1.50, 'C1-D2', 'BATCH-DRINK-001', CURRENT_DATE + INTERVAL '6 months', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0)
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
-- Note: Entity only has product relationship, no product_name/sku columns
-- ========================================
INSERT INTO line_items (id, transaction_id, product_id, quantity, unit_price, line_total, created_at, updated_at, version)
VALUES
    ('d50e8400-e29b-41d4-a716-446655440001', 'c50e8400-e29b-41d4-a716-446655440001', '850e8400-e29b-41d4-a716-446655440001', 2, 25.99, 51.98, CURRENT_TIMESTAMP - INTERVAL '2 days', CURRENT_TIMESTAMP - INTERVAL '2 days', 0),
    ('d50e8400-e29b-41d4-a716-446655440002', 'c50e8400-e29b-41d4-a716-446655440002', '850e8400-e29b-41d4-a716-446655440001', 1, 25.99, 25.99, CURRENT_TIMESTAMP - INTERVAL '1 day', CURRENT_TIMESTAMP - INTERVAL '1 day', 0),
    ('d50e8400-e29b-41d4-a716-446655440003', 'c50e8400-e29b-41d4-a716-446655440003', '850e8400-e29b-41d4-a716-446655440003', 1, 19.99, 19.99, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0)
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
