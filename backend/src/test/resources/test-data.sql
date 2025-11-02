-- ========================================
-- Test Data SQL for Realistic Integration Testing
-- ========================================
-- This file contains realistic test data that mirrors production environment.
-- It ensures tests validate actual role-permission relationships from the database.
--
-- Usage:
-- - Can be loaded via @Sql annotation in integration tests
-- - Provides consistent test data across test runs
-- - Matches production role-permission mappings from migrations V12-V14
--
-- ========================================

-- ========================================
-- 1. TENANTS
-- ========================================
INSERT INTO tenants (id, name, description, status, registration_number, tax_id, created_at, updated_at, version)
VALUES
    ('test-tenant-001', 'Test Retail Corp', 'Test tenant for integration testing', 'ACTIVE', 'REG-TEST-001', 'TAX-TEST-001', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    ('test-tenant-002', 'Demo Electronics Ltd', 'Demo tenant for testing multi-tenancy', 'ACTIVE', 'REG-TEST-002', 'TAX-TEST-002', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0)
ON CONFLICT (id) DO NOTHING;

-- ========================================
-- 2. SHOPS
-- ========================================
INSERT INTO shops (id, name, tenant_id, address, city, state, country, postal_code, phone, email, status, created_at, updated_at, version)
VALUES
    ('test-shop-001', 'Downtown Store', 'test-tenant-001', '123 Main St', 'New York', 'NY', 'USA', '10001', '+1-555-0101', 'downtown@testretail.com', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    ('test-shop-002', 'Uptown Branch', 'test-tenant-001', '456 Broadway', 'New York', 'NY', 'USA', '10002', '+1-555-0102', 'uptown@testretail.com', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    ('test-shop-003', 'Demo Shop', 'test-tenant-002', '789 Tech Ave', 'San Francisco', 'CA', 'USA', '94102', '+1-555-0103', 'demo@electronics.com', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0)
ON CONFLICT (id) DO NOTHING;

-- ========================================
-- 3. USERS (Keycloak synced users)
-- ========================================
-- Note: In real environment, these come from Keycloak via UserSyncService
INSERT INTO users (id, keycloak_id, tenant_id, username, email, first_name, last_name, phone_number, status, created_at, updated_at, version)
VALUES
    ('user-admin-001', 'kc-admin-001', 'test-tenant-001', 'admin@testretail.com', 'admin@testretail.com', 'System', 'Admin', '+1-555-1001', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    ('user-owner-001', 'kc-owner-001', 'test-tenant-001', 'owner@testretail.com', 'owner@testretail.com', 'John', 'Owner', '+1-555-1002', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    ('user-manager-001', 'kc-manager-001', 'test-tenant-001', 'manager@testretail.com', 'manager@testretail.com', 'Jane', 'Manager', '+1-555-1003', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    ('user-employee-001', 'kc-employee-001', 'test-tenant-001', 'employee@testretail.com', 'employee@testretail.com', 'Bob', 'Employee', '+1-555-1004', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    ('user-investor-001', 'kc-investor-001', 'test-tenant-001', 'investor@testretail.com', 'investor@testretail.com', 'Alice', 'Investor', '+1-555-1005', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0)
ON CONFLICT (id) DO NOTHING;

-- ========================================
-- 4. USER-ROLE ASSIGNMENTS
-- ========================================
-- Assign roles to users (matches production role hierarchy)
INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id
FROM users u, roles r
WHERE (u.id = 'user-admin-001' AND r.name = 'SYSTEM_ADMIN')
   OR (u.id = 'user-owner-001' AND r.name = 'OWNER')
   OR (u.id = 'user-manager-001' AND r.name = 'MANAGER')
   OR (u.id = 'user-employee-001' AND r.name = 'EMPLOYEE')
   OR (u.id = 'user-investor-001' AND r.name = 'INVESTOR')
ON CONFLICT DO NOTHING;

-- ========================================
-- 5. USER-SHOP ASSIGNMENTS
-- ========================================
-- Assign users to shops (determines which shops they can access)
INSERT INTO user_shops (user_id, shop_id, is_primary)
VALUES
    ('user-admin-001', 'test-shop-001', true),
    ('user-owner-001', 'test-shop-001', true),
    ('user-owner-001', 'test-shop-002', false),
    ('user-manager-001', 'test-shop-001', true),
    ('user-employee-001', 'test-shop-001', true),
    ('user-investor-001', 'test-shop-001', true)
ON CONFLICT DO NOTHING;

-- ========================================
-- 6. CATEGORIES
-- ========================================
INSERT INTO categories (id, name, shop_id, tenant_id, description, status, created_at, updated_at, version)
VALUES
    ('cat-001', 'Electronics', 'test-shop-001', 'test-tenant-001', 'Electronic devices and accessories', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    ('cat-002', 'Clothing', 'test-shop-001', 'test-tenant-001', 'Apparel and fashion items', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    ('cat-003', 'Food & Beverage', 'test-shop-001', 'test-tenant-001', 'Food and drink products', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0)
ON CONFLICT (id) DO NOTHING;

-- ========================================
-- 7. PRODUCTS (Master Catalog)
-- ========================================
INSERT INTO products (id, name, description, sku, barcode, category_id, shop_id, tenant_id, unit_price, cost_price, status, created_at, updated_at, version)
VALUES
    ('prod-001', 'Wireless Mouse', 'Ergonomic wireless mouse with USB receiver', 'MOUSE-001', '1234567890123', 'cat-001', 'test-shop-001', 'test-tenant-001', 25.99, 15.00, 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    ('prod-002', 'USB Keyboard', 'Standard USB keyboard with numeric pad', 'KB-001', '1234567890124', 'cat-001', 'test-shop-001', 'test-tenant-001', 35.99, 20.00, 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    ('prod-003', 'Cotton T-Shirt', 'Premium cotton t-shirt, multiple sizes', 'TSHIRT-001', '1234567890125', 'cat-002', 'test-shop-001', 'test-tenant-001', 19.99, 10.00, 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    ('prod-004', 'Energy Drink', 'Refreshing energy drink, 250ml can', 'DRINK-001', '1234567890126', 'cat-003', 'test-shop-001', 'test-tenant-001', 2.99, 1.50, 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0)
ON CONFLICT (id) DO NOTHING;

-- ========================================
-- 8. INVENTORY (Stock Tracking)
-- ========================================
INSERT INTO inventory (id, product_id, shop_id, tenant_id, current_stock, reserved_stock, minimum_stock, maximum_stock, reorder_point, unit_cost, location, batch_number, expiry_date, status, last_stock_update, created_at, updated_at, version)
VALUES
    ('inv-001', 'prod-001', 'test-shop-001', 'test-tenant-001', 100, 5, 20, 200, 30, 15.00, 'A1-B2', 'BATCH-MOUSE-001', NULL, 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    ('inv-002', 'prod-002', 'test-shop-001', 'test-tenant-001', 75, 3, 15, 150, 25, 20.00, 'A2-C1', 'BATCH-KB-001', NULL, 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    ('inv-003', 'prod-003', 'test-shop-001', 'test-tenant-001', 200, 10, 50, 500, 75, 10.00, 'B1-A3', 'BATCH-SHIRT-001', NULL, 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    ('inv-004', 'prod-004', 'test-shop-001', 'test-tenant-001', 500, 0, 100, 1000, 150, 1.50, 'C1-D2', 'BATCH-DRINK-001', CURRENT_DATE + INTERVAL '6 months', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0)
ON CONFLICT (id) DO NOTHING;

-- ========================================
-- 9. EXPENSE CATEGORIES
-- ========================================
INSERT INTO expense_categories (id, name, description, tenant_id, created_at, updated_at, version)
VALUES
    ('exp-cat-001', 'Utilities', 'Electricity, water, internet, etc.', 'test-tenant-001', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    ('exp-cat-002', 'Rent', 'Shop rental payments', 'test-tenant-001', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    ('exp-cat-003', 'Salaries', 'Employee salaries and wages', 'test-tenant-001', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0)
ON CONFLICT (id) DO NOTHING;

-- ========================================
-- 10. SALES TRANSACTIONS
-- ========================================
INSERT INTO sales_transactions (id, transaction_number, shop_id, tenant_id, cashier_id, customer_name, customer_email, customer_phone, total_amount, payment_method, transaction_date, status, created_at, updated_at, version)
VALUES
    ('txn-001', 'TXN-2024-001', 'test-shop-001', 'test-tenant-001', 'user-employee-001', 'John Customer', 'john@customer.com', '+1-555-2001', 51.98, 'CASH', CURRENT_TIMESTAMP - INTERVAL '2 days', 'COMPLETED', CURRENT_TIMESTAMP - INTERVAL '2 days', CURRENT_TIMESTAMP - INTERVAL '2 days', 0),
    ('txn-002', 'TXN-2024-002', 'test-shop-001', 'test-tenant-001', 'user-employee-001', 'Jane Customer', 'jane@customer.com', '+1-555-2002', 25.99, 'CARD', CURRENT_TIMESTAMP - INTERVAL '1 day', 'COMPLETED', CURRENT_TIMESTAMP - INTERVAL '1 day', CURRENT_TIMESTAMP - INTERVAL '1 day', 0),
    ('txn-003', 'TXN-2024-003', 'test-shop-001', 'test-tenant-001', 'user-employee-001', NULL, NULL, NULL, 19.99, 'CASH', CURRENT_TIMESTAMP, 'COMPLETED', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0)
ON CONFLICT (id) DO NOTHING;

-- ========================================
-- 11. SALES TRANSACTION ITEMS
-- ========================================
INSERT INTO sales_transaction_items (id, transaction_id, product_id, product_name, product_sku, quantity, unit_price, subtotal, created_at, updated_at, version)
VALUES
    ('item-001', 'txn-001', 'prod-001', 'Wireless Mouse', 'MOUSE-001', 2, 25.99, 51.98, CURRENT_TIMESTAMP - INTERVAL '2 days', CURRENT_TIMESTAMP - INTERVAL '2 days', 0),
    ('item-002', 'txn-002', 'prod-001', 'Wireless Mouse', 'MOUSE-001', 1, 25.99, 25.99, CURRENT_TIMESTAMP - INTERVAL '1 day', CURRENT_TIMESTAMP - INTERVAL '1 day', 0),
    ('item-003', 'txn-003', 'prod-003', 'Cotton T-Shirt', 'TSHIRT-001', 1, 19.99, 19.99, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0)
ON CONFLICT (id) DO NOTHING;

-- ========================================
-- 12. RECEIPTS
-- ========================================
INSERT INTO receipts (id, receipt_number, transaction_id, tenant_id, format, status, receipt_content, printable_content, generated_at, created_at, updated_at, version)
VALUES
    ('rcp-001', 'RCP-2024-001', 'txn-001', 'test-tenant-001', 'TEXT', 'GENERATED', '=== RECEIPT ===\nTest Retail Corp\nDowntown Store\n123 Main St\n\nTransaction: TXN-2024-001\nCashier: Bob Employee\nDate: ' || (CURRENT_TIMESTAMP - INTERVAL '2 days')::TEXT || '\n\nItems:\n- Wireless Mouse x2 - $51.98\n\nTotal: $51.98\nPayment: CASH\nThank you!', '=== RECEIPT (PRINTABLE) ===\nTest Retail Corp\n\nTransaction: TXN-2024-001\nTotal: $51.98', CURRENT_TIMESTAMP - INTERVAL '2 days', CURRENT_TIMESTAMP - INTERVAL '2 days', CURRENT_TIMESTAMP - INTERVAL '2 days', 0),
    ('rcp-002', 'RCP-2024-002', 'txn-002', 'test-tenant-001', 'TEXT', 'GENERATED', '=== RECEIPT ===\nTest Retail Corp\nDowntown Store\n\nTransaction: TXN-2024-002\nTotal: $25.99', '=== RECEIPT (PRINTABLE) ===\nTest Retail Corp\nTotal: $25.99', CURRENT_TIMESTAMP - INTERVAL '1 day', CURRENT_TIMESTAMP - INTERVAL '1 day', CURRENT_TIMESTAMP - INTERVAL '1 day', 0)
ON CONFLICT (id) DO NOTHING;

-- ========================================
-- 13. EXPENSES
-- ========================================
INSERT INTO expenses (id, expense_number, shop_id, tenant_id, category_id, amount, description, expense_date, recorded_by, approved_by, status, created_at, updated_at, version)
VALUES
    ('exp-001', 'EXP-2024-001', 'test-shop-001', 'test-tenant-001', 'exp-cat-001', 250.00, 'Monthly electricity bill', CURRENT_DATE - INTERVAL '5 days', 'user-manager-001', 'user-owner-001', 'APPROVED', CURRENT_TIMESTAMP - INTERVAL '5 days', CURRENT_TIMESTAMP - INTERVAL '4 days', 0),
    ('exp-002', 'EXP-2024-002', 'test-shop-001', 'test-tenant-001', 'exp-cat-002', 2000.00, 'Monthly rent payment', CURRENT_DATE - INTERVAL '3 days', 'user-manager-001', NULL, 'PENDING', CURRENT_TIMESTAMP - INTERVAL '3 days', CURRENT_TIMESTAMP - INTERVAL '3 days', 0),
    ('exp-003', 'EXP-2024-003', 'test-shop-001', 'test-tenant-001', 'exp-cat-003', 5000.00, 'Monthly salaries', CURRENT_DATE - INTERVAL '1 day', 'user-manager-001', 'user-owner-001', 'APPROVED', CURRENT_TIMESTAMP - INTERVAL '1 day', CURRENT_TIMESTAMP, 0)
ON CONFLICT (id) DO NOTHING;

-- ========================================
-- 14. INVESTMENTS
-- ========================================
INSERT INTO investments (id, investment_number, tenant_id, shop_id, name, description, total_amount, start_date, end_date, status, created_by, created_at, updated_at, version)
VALUES
    ('inv-001', 'INV-2024-001', 'test-tenant-001', 'test-shop-001', 'Q1 Expansion Fund', 'Capital for store expansion and inventory', 50000.00, CURRENT_DATE - INTERVAL '90 days', CURRENT_DATE + INTERVAL '275 days', 'ACTIVE', 'user-owner-001', CURRENT_TIMESTAMP - INTERVAL '90 days', CURRENT_TIMESTAMP - INTERVAL '90 days', 0),
    ('inv-002', 'INV-2024-002', 'test-tenant-001', 'test-shop-002', 'Equipment Upgrade', 'New POS systems and equipment', 25000.00, CURRENT_DATE - INTERVAL '30 days', CURRENT_DATE + INTERVAL '335 days', 'ACTIVE', 'user-owner-001', CURRENT_TIMESTAMP - INTERVAL '30 days', CURRENT_TIMESTAMP - INTERVAL '30 days', 0)
ON CONFLICT (id) DO NOTHING;

-- ========================================
-- 15. INVESTOR SHARES
-- ========================================
INSERT INTO investor_shares (id, investment_id, investor_id, share_percentage, amount_invested, created_at, updated_at, version)
VALUES
    ('share-001', 'inv-001', 'user-investor-001', 20.00, 10000.00, CURRENT_TIMESTAMP - INTERVAL '90 days', CURRENT_TIMESTAMP - INTERVAL '90 days', 0),
    ('share-002', 'inv-001', 'user-owner-001', 80.00, 40000.00, CURRENT_TIMESTAMP - INTERVAL '90 days', CURRENT_TIMESTAMP - INTERVAL '90 days', 0),
    ('share-003', 'inv-002', 'user-investor-001', 40.00, 10000.00, CURRENT_TIMESTAMP - INTERVAL '30 days', CURRENT_TIMESTAMP - INTERVAL '30 days', 0),
    ('share-004', 'inv-002', 'user-owner-001', 60.00, 15000.00, CURRENT_TIMESTAMP - INTERVAL '30 days', CURRENT_TIMESTAMP - INTERVAL '30 days', 0)
ON CONFLICT (id) DO NOTHING;

-- ========================================
-- 16. PRODUCT RETURNS
-- ========================================
INSERT INTO product_returns (id, return_number, transaction_id, shop_id, tenant_id, return_reason, total_refund, return_date, processed_by, approved_by, status, created_at, updated_at, version)
VALUES
    ('ret-001', 'RET-2024-001', 'txn-001', 'test-shop-001', 'test-tenant-001', 'Defective product', 25.99, CURRENT_TIMESTAMP - INTERVAL '1 day', 'user-employee-001', 'user-manager-001', 'APPROVED', CURRENT_TIMESTAMP - INTERVAL '1 day', CURRENT_TIMESTAMP, 0)
ON CONFLICT (id) DO NOTHING;

-- ========================================
-- 17. RETURN ITEMS
-- ========================================
INSERT INTO return_items (id, return_id, product_id, product_name, product_sku, quantity, unit_price, subtotal, created_at, updated_at, version)
VALUES
    ('ret-item-001', 'ret-001', 'prod-001', 'Wireless Mouse', 'MOUSE-001', 1, 25.99, 25.99, CURRENT_TIMESTAMP - INTERVAL '1 day', CURRENT_TIMESTAMP - INTERVAL '1 day', 0)
ON CONFLICT (id) DO NOTHING;

-- ========================================
-- 18. INVENTORY MOVEMENTS
-- ========================================
INSERT INTO inventory_movements (id, inventory_id, movement_type, quantity, previous_stock, new_stock, reference_id, reference_type, reason, performed_by, movement_date, created_at, version)
VALUES
    ('mov-001', 'inv-001', 'SALE', -2, 100, 98, 'txn-001', 'SALES_TRANSACTION', 'Sale transaction TXN-2024-001', 'user-employee-001', CURRENT_TIMESTAMP - INTERVAL '2 days', CURRENT_TIMESTAMP - INTERVAL '2 days', 0),
    ('mov-002', 'inv-001', 'SALE', -1, 98, 97, 'txn-002', 'SALES_TRANSACTION', 'Sale transaction TXN-2024-002', 'user-employee-001', CURRENT_TIMESTAMP - INTERVAL '1 day', CURRENT_TIMESTAMP - INTERVAL '1 day', 0),
    ('mov-003', 'inv-001', 'RETURN', 1, 97, 98, 'ret-001', 'PRODUCT_RETURN', 'Product return RET-2024-001', 'user-employee-001', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    ('mov-004', 'inv-001', 'ADJUSTMENT', 2, 98, 100, NULL, 'MANUAL', 'Stock count adjustment', 'user-manager-001', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0)
ON CONFLICT (id) DO NOTHING;

-- ========================================
-- 19. AUDIT LOGS
-- ========================================
INSERT INTO audit_logs (id, tenant_id, shop_id, user_id, username, action, entity_type, entity_id, changes, ip_address, user_agent, timestamp, created_at, version)
VALUES
    ('audit-001', 'test-tenant-001', 'test-shop-001', 'user-manager-001', 'manager@testretail.com', 'CREATE', 'EXPENSE', 'exp-001', '{"amount": 250.00, "category": "Utilities"}', '192.168.1.100', 'Mozilla/5.0', CURRENT_TIMESTAMP - INTERVAL '5 days', CURRENT_TIMESTAMP - INTERVAL '5 days', 0),
    ('audit-002', 'test-tenant-001', 'test-shop-001', 'user-owner-001', 'owner@testretail.com', 'UPDATE', 'EXPENSE', 'exp-001', '{"status": "PENDING -> APPROVED"}', '192.168.1.101', 'Mozilla/5.0', CURRENT_TIMESTAMP - INTERVAL '4 days', CURRENT_TIMESTAMP - INTERVAL '4 days', 0),
    ('audit-003', 'test-tenant-001', 'test-shop-001', 'user-employee-001', 'employee@testretail.com', 'CREATE', 'SALES_TRANSACTION', 'txn-001', '{"total": 51.98, "payment": "CASH"}', '192.168.1.102', 'Mozilla/5.0', CURRENT_TIMESTAMP - INTERVAL '2 days', CURRENT_TIMESTAMP - INTERVAL '2 days', 0)
ON CONFLICT (id) DO NOTHING;

-- ========================================
-- 20. FEATURE FLAGS
-- ========================================
INSERT INTO feature_flags (id, feature_name, enabled, tenant_id, shop_id, description, created_at, updated_at, version)
VALUES
    ('flag-001', 'ANALYTICS', true, NULL, NULL, 'Enable analytics module globally', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    ('flag-002', 'INVESTMENT', true, NULL, NULL, 'Enable investment tracking globally', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    ('flag-003', 'FRAUD_DETECTION', true, 'test-tenant-001', NULL, 'Enable fraud detection for tenant', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0)
ON CONFLICT (id) DO NOTHING;

-- ========================================
-- NOTES:
-- ========================================
-- 1. All IDs use predictable prefixes for easy test assertions:
--    - Tenants: test-tenant-###
--    - Shops: test-shop-###
--    - Users: user-{role}-###
--    - Products: prod-###
--    - Inventory: inv-###
--    - Transactions: txn-###
--    - Receipts: rcp-###
--    - Expenses: exp-###
--    - Investments: inv-###
--    - Returns: ret-###
--
-- 2. Timestamps use CURRENT_TIMESTAMP with intervals for time-based testing
-- 3. Foreign key relationships are properly maintained
-- 4. Role-permission mappings come from migrations (V12-V14), not this file
-- 5. This data is tenant-isolated and multi-shop aware
-- 6. Data covers all major entities for comprehensive integration testing
--
-- Entity Counts (for test assertions):
-- - 2 Tenants
-- - 3 Shops
-- - 5 Users (1 per role)
-- - 3 Categories
-- - 4 Products
-- - 4 Inventory records
-- - 3 Expense Categories
-- - 3 Sales Transactions
-- - 3 Transaction Items
-- - 2 Receipts
-- - 3 Expenses
-- - 2 Investments
-- - 4 Investor Shares
-- - 1 Product Return
-- - 4 Inventory Movements
-- - 3 Audit Logs
-- - 3 Feature Flags
--
-- Usage in Tests:
-- @Sql(scripts = "/test-data.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
-- @Sql(statements = "DELETE FROM ...", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
--
-- With TestConstants:
-- assertEquals(TestConstants.TEST_TENANT_ID, tenant.getId());
-- assertEquals(TestConstants.EXPECTED_PRODUCT_COUNT, products.size());
