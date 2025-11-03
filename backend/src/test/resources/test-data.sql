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
-- 1. TENANTS
-- ========================================
INSERT INTO tenants (id, name, description, status, registration_number, tax_id, created_at, updated_at, version)
VALUES
    ('550e8400-e29b-41d4-a716-446655440001', 'Test Retail Corp', 'Test tenant for integration testing', 'ACTIVE', 'REG-TEST-001', 'TAX-TEST-001', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    ('550e8400-e29b-41d4-a716-446655440002', 'Demo Electronics Ltd', 'Demo tenant for testing multi-tenancy', 'ACTIVE', 'REG-TEST-002', 'TAX-TEST-002', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0)
ON CONFLICT (id) DO NOTHING;

-- ========================================
-- 2. SHOPS
-- ========================================
INSERT INTO shops (id, name, tenant_id, address, city, state, country, postal_code, phone, email, status, created_at, updated_at, version)
VALUES
    ('650e8400-e29b-41d4-a716-446655440001', 'Downtown Store', '550e8400-e29b-41d4-a716-446655440001', '123 Main St', 'New York', 'NY', 'USA', '10001', '+1-555-0101', 'downtown@testretail.com', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    ('650e8400-e29b-41d4-a716-446655440002', 'Uptown Branch', '550e8400-e29b-41d4-a716-446655440001', '456 Broadway', 'New York', 'NY', 'USA', '10002', '+1-555-0102', 'uptown@testretail.com', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    ('650e8400-e29b-41d4-a716-446655440003', 'Demo Shop', '550e8400-e29b-41d4-a716-446655440002', '789 Tech Ave', 'San Francisco', 'CA', 'USA', '94102', '+1-555-0103', 'demo@electronics.com', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0)
ON CONFLICT (id) DO NOTHING;

-- ========================================
-- 3. USERS (Keycloak synced users)
-- ========================================
INSERT INTO users (id, keycloak_id, tenant_id, username, email, first_name, last_name, phone_number, status, created_at, updated_at, version)
VALUES
    ('750e8400-e29b-41d4-a716-446655440001', 'kc-admin-001', '550e8400-e29b-41d4-a716-446655440001', 'admin@testretail.com', 'admin@testretail.com', 'System', 'Admin', '+1-555-1001', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    ('750e8400-e29b-41d4-a716-446655440002', 'kc-owner-001', '550e8400-e29b-41d4-a716-446655440001', 'owner@testretail.com', 'owner@testretail.com', 'John', 'Owner', '+1-555-1002', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    ('750e8400-e29b-41d4-a716-446655440003', 'kc-manager-001', '550e8400-e29b-41d4-a716-446655440001', 'manager@testretail.com', 'manager@testretail.com', 'Jane', 'Manager', '+1-555-1003', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    ('750e8400-e29b-41d4-a716-446655440004', 'kc-employee-001', '550e8400-e29b-41d4-a716-446655440001', 'employee@testretail.com', 'employee@testretail.com', 'Bob', 'Employee', '+1-555-1004', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    ('750e8400-e29b-41d4-a716-446655440005', 'kc-investor-001', '550e8400-e29b-41d4-a716-446655440001', 'investor@testretail.com', 'investor@testretail.com', 'Alice', 'Investor', '+1-555-1005', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0)
ON CONFLICT (id) DO NOTHING;

-- ========================================
-- 4. USER-ROLE ASSIGNMENTS
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
-- 5. USER-SHOP ASSIGNMENTS
-- ========================================
INSERT INTO user_shops (user_id, shop_id, is_primary)
VALUES
    ('750e8400-e29b-41d4-a716-446655440001', '650e8400-e29b-41d4-a716-446655440001', true),
    ('750e8400-e29b-41d4-a716-446655440002', '650e8400-e29b-41d4-a716-446655440001', true),
    ('750e8400-e29b-41d4-a716-446655440002', '650e8400-e29b-41d4-a716-446655440002', false),
    ('750e8400-e29b-41d4-a716-446655440003', '650e8400-e29b-41d4-a716-446655440001', true),
    ('750e8400-e29b-41d4-a716-446655440004', '650e8400-e29b-41d4-a716-446655440001', true),
    ('750e8400-e29b-41d4-a716-446655440005', '650e8400-e29b-41d4-a716-446655440001', true)
ON CONFLICT DO NOTHING;

-- ========================================
-- 6. CATEGORIES
-- ========================================
INSERT INTO categories (id, name, shop_id, tenant_id, description, status, created_at, updated_at, version)
VALUES
    ('950e8400-e29b-41d4-a716-446655440001', 'Electronics', '650e8400-e29b-41d4-a716-446655440001', '550e8400-e29b-41d4-a716-446655440001', 'Electronic devices and accessories', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    ('950e8400-e29b-41d4-a716-446655440002', 'Clothing', '650e8400-e29b-41d4-a716-446655440001', '550e8400-e29b-41d4-a716-446655440001', 'Apparel and fashion items', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    ('950e8400-e29b-41d4-a716-446655440003', 'Food & Beverage', '650e8400-e29b-41d4-a716-446655440001', '550e8400-e29b-41d4-a716-446655440001', 'Food and drink products', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0)
ON CONFLICT (id) DO NOTHING;

-- ========================================
-- 7. PRODUCTS (Master Catalog)
-- ========================================
INSERT INTO products (id, name, description, sku, barcode, category_id, shop_id, tenant_id, unit_price, cost_price, status, created_at, updated_at, version)
VALUES
    ('850e8400-e29b-41d4-a716-446655440001', 'Wireless Mouse', 'Ergonomic wireless mouse with USB receiver', 'MOUSE-001', '1234567890123', '950e8400-e29b-41d4-a716-446655440001', '650e8400-e29b-41d4-a716-446655440001', '550e8400-e29b-41d4-a716-446655440001', 25.99, 15.00, 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    ('850e8400-e29b-41d4-a716-446655440002', 'USB Keyboard', 'Standard USB keyboard with numeric pad', 'KB-001', '1234567890124', '950e8400-e29b-41d4-a716-446655440001', '650e8400-e29b-41d4-a716-446655440001', '550e8400-e29b-41d4-a716-446655440001', 35.99, 20.00, 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    ('850e8400-e29b-41d4-a716-446655440003', 'Cotton T-Shirt', 'Premium cotton t-shirt, multiple sizes', 'TSHIRT-001', '1234567890125', '950e8400-e29b-41d4-a716-446655440002', '650e8400-e29b-41d4-a716-446655440001', '550e8400-e29b-41d4-a716-446655440001', 19.99, 10.00, 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    ('850e8400-e29b-41d4-a716-446655440004', 'Energy Drink', 'Refreshing energy drink, 250ml can', 'DRINK-001', '1234567890126', '950e8400-e29b-41d4-a716-446655440003', '650e8400-e29b-41d4-a716-446655440001', '550e8400-e29b-41d4-a716-446655440001', 2.99, 1.50, 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0)
ON CONFLICT (id) DO NOTHING;

-- ========================================
-- 8. INVENTORY (Stock Tracking)
-- ========================================
INSERT INTO inventory (id, product_id, shop_id, tenant_id, current_stock, reserved_stock, minimum_stock, maximum_stock, reorder_point, unit_cost, location, batch_number, expiry_date, status, last_stock_update, created_at, updated_at, version)
VALUES
    ('a50e8400-e29b-41d4-a716-446655440001', '850e8400-e29b-41d4-a716-446655440001', '650e8400-e29b-41d4-a716-446655440001', '550e8400-e29b-41d4-a716-446655440001', 100, 5, 20, 200, 30, 15.00, 'A1-B2', 'BATCH-MOUSE-001', NULL, 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    ('a50e8400-e29b-41d4-a716-446655440002', '850e8400-e29b-41d4-a716-446655440002', '650e8400-e29b-41d4-a716-446655440001', '550e8400-e29b-41d4-a716-446655440001', 75, 3, 15, 150, 25, 20.00, 'A2-C1', 'BATCH-KB-001', NULL, 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    ('a50e8400-e29b-41d4-a716-446655440003', '850e8400-e29b-41d4-a716-446655440003', '650e8400-e29b-41d4-a716-446655440001', '550e8400-e29b-41d4-a716-446655440001', 200, 10, 50, 500, 75, 10.00, 'B1-A3', 'BATCH-SHIRT-001', NULL, 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    ('a50e8400-e29b-41d4-a716-446655440004', '850e8400-e29b-41d4-a716-446655440004', '650e8400-e29b-41d4-a716-446655440001', '550e8400-e29b-41d4-a716-446655440001', 500, 0, 100, 1000, 150, 1.50, 'C1-D2', 'BATCH-DRINK-001', CURRENT_DATE + INTERVAL '6 months', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0)
ON CONFLICT (id) DO NOTHING;

-- ========================================
-- 9. EXPENSE CATEGORIES
-- ========================================
INSERT INTO expense_categories (id, name, description, tenant_id, created_at, updated_at, version)
VALUES
    ('b50e8400-e29b-41d4-a716-446655440001', 'Utilities', 'Electricity, water, internet, etc.', '550e8400-e29b-41d4-a716-446655440001', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    ('b50e8400-e29b-41d4-a716-446655440002', 'Rent', 'Shop rental payments', '550e8400-e29b-41d4-a716-446655440001', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    ('b50e8400-e29b-41d4-a716-446655440003', 'Salaries', 'Employee salaries and wages', '550e8400-e29b-41d4-a716-446655440001', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0)
ON CONFLICT (id) DO NOTHING;

-- ========================================
-- 10. SALES TRANSACTIONS
-- ========================================
INSERT INTO sales_transactions (id, transaction_number, shop_id, tenant_id, cashier_id, customer_name, customer_email, customer_phone, total_amount, payment_method, transaction_date, status, created_at, updated_at, version)
VALUES
    ('c50e8400-e29b-41d4-a716-446655440001', 'TXN-2024-001', '650e8400-e29b-41d4-a716-446655440001', '550e8400-e29b-41d4-a716-446655440001', '750e8400-e29b-41d4-a716-446655440004', 'John Customer', 'john@customer.com', '+1-555-2001', 51.98, 'CASH', CURRENT_TIMESTAMP - INTERVAL '2 days', 'COMPLETED', CURRENT_TIMESTAMP - INTERVAL '2 days', CURRENT_TIMESTAMP - INTERVAL '2 days', 0),
    ('c50e8400-e29b-41d4-a716-446655440002', 'TXN-2024-002', '650e8400-e29b-41d4-a716-446655440001', '550e8400-e29b-41d4-a716-446655440001', '750e8400-e29b-41d4-a716-446655440004', 'Jane Customer', 'jane@customer.com', '+1-555-2002', 25.99, 'CARD', CURRENT_TIMESTAMP - INTERVAL '1 day', 'COMPLETED', CURRENT_TIMESTAMP - INTERVAL '1 day', CURRENT_TIMESTAMP - INTERVAL '1 day', 0),
    ('c50e8400-e29b-41d4-a716-446655440003', 'TXN-2024-003', '650e8400-e29b-41d4-a716-446655440001', '550e8400-e29b-41d4-a716-446655440001', '750e8400-e29b-41d4-a716-446655440004', NULL, NULL, NULL, 19.99, 'CASH', CURRENT_TIMESTAMP, 'COMPLETED', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0)
ON CONFLICT (id) DO NOTHING;

-- ========================================
-- 11. SALES TRANSACTION ITEMS
-- ========================================
INSERT INTO sales_transaction_items (id, transaction_id, product_id, product_name, product_sku, quantity, unit_price, subtotal, created_at, updated_at, version)
VALUES
    ('d50e8400-e29b-41d4-a716-446655440001', 'c50e8400-e29b-41d4-a716-446655440001', '850e8400-e29b-41d4-a716-446655440001', 'Wireless Mouse', 'MOUSE-001', 2, 25.99, 51.98, CURRENT_TIMESTAMP - INTERVAL '2 days', CURRENT_TIMESTAMP - INTERVAL '2 days', 0),
    ('d50e8400-e29b-41d4-a716-446655440002', 'c50e8400-e29b-41d4-a716-446655440002', '850e8400-e29b-41d4-a716-446655440001', 'Wireless Mouse', 'MOUSE-001', 1, 25.99, 25.99, CURRENT_TIMESTAMP - INTERVAL '1 day', CURRENT_TIMESTAMP - INTERVAL '1 day', 0),
    ('d50e8400-e29b-41d4-a716-446655440003', 'c50e8400-e29b-41d4-a716-446655440003', '850e8400-e29b-41d4-a716-446655440003', 'Cotton T-Shirt', 'TSHIRT-001', 1, 19.99, 19.99, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0)
ON CONFLICT (id) DO NOTHING;

-- ========================================
-- 12. RECEIPTS
-- ========================================
INSERT INTO receipts (id, receipt_number, transaction_id, tenant_id, format, status, receipt_content, printable_content, generated_at, created_at, updated_at, version)
VALUES
    ('e50e8400-e29b-41d4-a716-446655440001', 'RCP-2024-001', 'c50e8400-e29b-41d4-a716-446655440001', '550e8400-e29b-41d4-a716-446655440001', 'TEXT', 'GENERATED', '=== RECEIPT ===\nTest Retail Corp\nDowntown Store\n123 Main St\n\nTransaction: TXN-2024-001\nCashier: Bob Employee\nDate: ' || (CURRENT_TIMESTAMP - INTERVAL '2 days')::TEXT || '\n\nItems:\n- Wireless Mouse x2 - $51.98\n\nTotal: $51.98\nPayment: CASH\nThank you!', '=== RECEIPT (PRINTABLE) ===\nTest Retail Corp\n\nTransaction: TXN-2024-001\nTotal: $51.98', CURRENT_TIMESTAMP - INTERVAL '2 days', CURRENT_TIMESTAMP - INTERVAL '2 days', CURRENT_TIMESTAMP - INTERVAL '2 days', 0),
    ('e50e8400-e29b-41d4-a716-446655440002', 'RCP-2024-002', 'c50e8400-e29b-41d4-a716-446655440002', '550e8400-e29b-41d4-a716-446655440001', 'TEXT', 'GENERATED', '=== RECEIPT ===\nTest Retail Corp\nDowntown Store\n\nTransaction: TXN-2024-002\nTotal: $25.99', '=== RECEIPT (PRINTABLE) ===\nTest Retail Corp\nTotal: $25.99', CURRENT_TIMESTAMP - INTERVAL '1 day', CURRENT_TIMESTAMP - INTERVAL '1 day', CURRENT_TIMESTAMP - INTERVAL '1 day', 0)
ON CONFLICT (id) DO NOTHING;

-- ========================================
-- 13. EXPENSES
-- ========================================
INSERT INTO expenses (id, expense_number, shop_id, tenant_id, category_id, amount, description, expense_date, recorded_by, approved_by, status, created_at, updated_at, version)
VALUES
    ('f50e8400-e29b-41d4-a716-446655440001', 'EXP-2024-001', '650e8400-e29b-41d4-a716-446655440001', '550e8400-e29b-41d4-a716-446655440001', 'b50e8400-e29b-41d4-a716-446655440001', 250.00, 'Monthly electricity bill', CURRENT_DATE - INTERVAL '5 days', '750e8400-e29b-41d4-a716-446655440003', '750e8400-e29b-41d4-a716-446655440002', 'APPROVED', CURRENT_TIMESTAMP - INTERVAL '5 days', CURRENT_TIMESTAMP - INTERVAL '4 days', 0),
    ('f50e8400-e29b-41d4-a716-446655440002', 'EXP-2024-002', '650e8400-e29b-41d4-a716-446655440001', '550e8400-e29b-41d4-a716-446655440001', 'b50e8400-e29b-41d4-a716-446655440002', 2000.00, 'Monthly rent payment', CURRENT_DATE - INTERVAL '3 days', '750e8400-e29b-41d4-a716-446655440003', NULL, 'PENDING', CURRENT_TIMESTAMP - INTERVAL '3 days', CURRENT_TIMESTAMP - INTERVAL '3 days', 0),
    ('f50e8400-e29b-41d4-a716-446655440003', 'EXP-2024-003', '650e8400-e29b-41d4-a716-446655440001', '550e8400-e29b-41d4-a716-446655440001', 'b50e8400-e29b-41d4-a716-446655440003', 5000.00, 'Monthly salaries', CURRENT_DATE - INTERVAL '1 day', '750e8400-e29b-41d4-a716-446655440003', '750e8400-e29b-41d4-a716-446655440002', 'APPROVED', CURRENT_TIMESTAMP - INTERVAL '1 day', CURRENT_TIMESTAMP, 0)
ON CONFLICT (id) DO NOTHING;

-- ========================================
-- 14. INVESTMENTS
-- ========================================
INSERT INTO investments (id, investment_number, tenant_id, shop_id, name, description, total_amount, start_date, end_date, status, created_by, created_at, updated_at, version)
VALUES
    ('050e8400-e29b-41d4-a716-446655440001', 'INV-2024-001', '550e8400-e29b-41d4-a716-446655440001', '650e8400-e29b-41d4-a716-446655440001', 'Q1 Expansion Fund', 'Capital for store expansion and inventory', 50000.00, CURRENT_DATE - INTERVAL '90 days', CURRENT_DATE + INTERVAL '275 days', 'ACTIVE', '750e8400-e29b-41d4-a716-446655440002', CURRENT_TIMESTAMP - INTERVAL '90 days', CURRENT_TIMESTAMP - INTERVAL '90 days', 0),
    ('050e8400-e29b-41d4-a716-446655440002', 'INV-2024-002', '550e8400-e29b-41d4-a716-446655440001', '650e8400-e29b-41d4-a716-446655440002', 'Equipment Upgrade', 'New POS systems and equipment', 25000.00, CURRENT_DATE - INTERVAL '30 days', CURRENT_DATE + INTERVAL '335 days', 'ACTIVE', '750e8400-e29b-41d4-a716-446655440002', CURRENT_TIMESTAMP - INTERVAL '30 days', CURRENT_TIMESTAMP - INTERVAL '30 days', 0)
ON CONFLICT (id) DO NOTHING;

-- ========================================
-- 15. INVESTOR SHARES
-- ========================================
INSERT INTO investor_shares (id, investment_id, investor_id, share_percentage, amount_invested, created_at, updated_at, version)
VALUES
    ('150e8400-e29b-41d4-a716-446655440001', '050e8400-e29b-41d4-a716-446655440001', '750e8400-e29b-41d4-a716-446655440005', 20.00, 10000.00, CURRENT_TIMESTAMP - INTERVAL '90 days', CURRENT_TIMESTAMP - INTERVAL '90 days', 0),
    ('150e8400-e29b-41d4-a716-446655440002', '050e8400-e29b-41d4-a716-446655440001', '750e8400-e29b-41d4-a716-446655440002', 80.00, 40000.00, CURRENT_TIMESTAMP - INTERVAL '90 days', CURRENT_TIMESTAMP - INTERVAL '90 days', 0),
    ('150e8400-e29b-41d4-a716-446655440003', '050e8400-e29b-41d4-a716-446655440002', '750e8400-e29b-41d4-a716-446655440005', 40.00, 10000.00, CURRENT_TIMESTAMP - INTERVAL '30 days', CURRENT_TIMESTAMP - INTERVAL '30 days', 0),
    ('150e8400-e29b-41d4-a716-446655440004', '050e8400-e29b-41d4-a716-446655440002', '750e8400-e29b-41d4-a716-446655440002', 60.00, 15000.00, CURRENT_TIMESTAMP - INTERVAL '30 days', CURRENT_TIMESTAMP - INTERVAL '30 days', 0)
ON CONFLICT (id) DO NOTHING;

-- ========================================
-- 16. PRODUCT RETURNS
-- ========================================
INSERT INTO product_returns (id, return_number, transaction_id, shop_id, tenant_id, return_reason, total_refund, return_date, processed_by, approved_by, status, created_at, updated_at, version)
VALUES
    ('250e8400-e29b-41d4-a716-446655440001', 'RET-2024-001', 'c50e8400-e29b-41d4-a716-446655440001', '650e8400-e29b-41d4-a716-446655440001', '550e8400-e29b-41d4-a716-446655440001', 'Defective product', 25.99, CURRENT_TIMESTAMP - INTERVAL '1 day', '750e8400-e29b-41d4-a716-446655440004', '750e8400-e29b-41d4-a716-446655440003', 'APPROVED', CURRENT_TIMESTAMP - INTERVAL '1 day', CURRENT_TIMESTAMP, 0)
ON CONFLICT (id) DO NOTHING;

-- ========================================
-- 17. RETURN ITEMS
-- ========================================
INSERT INTO return_items (id, return_id, product_id, product_name, product_sku, quantity, unit_price, subtotal, created_at, updated_at, version)
VALUES
    ('350e8400-e29b-41d4-a716-446655440001', '250e8400-e29b-41d4-a716-446655440001', '850e8400-e29b-41d4-a716-446655440001', 'Wireless Mouse', 'MOUSE-001', 1, 25.99, 25.99, CURRENT_TIMESTAMP - INTERVAL '1 day', CURRENT_TIMESTAMP - INTERVAL '1 day', 0)
ON CONFLICT (id) DO NOTHING;

-- ========================================
-- 18. INVENTORY MOVEMENTS
-- ========================================
INSERT INTO inventory_movements (id, inventory_id, movement_type, quantity, previous_stock, new_stock, reference_id, reference_type, reason, performed_by, movement_date, created_at, version)
VALUES
    ('450e8400-e29b-41d4-a716-446655440001', 'a50e8400-e29b-41d4-a716-446655440001', 'SALE', -2, 100, 98, 'c50e8400-e29b-41d4-a716-446655440001', 'SALES_TRANSACTION', 'Sale transaction TXN-2024-001', '750e8400-e29b-41d4-a716-446655440004', CURRENT_TIMESTAMP - INTERVAL '2 days', CURRENT_TIMESTAMP - INTERVAL '2 days', 0),
    ('450e8400-e29b-41d4-a716-446655440002', 'a50e8400-e29b-41d4-a716-446655440001', 'SALE', -1, 98, 97, 'c50e8400-e29b-41d4-a716-446655440002', 'SALES_TRANSACTION', 'Sale transaction TXN-2024-002', '750e8400-e29b-41d4-a716-446655440004', CURRENT_TIMESTAMP - INTERVAL '1 day', CURRENT_TIMESTAMP - INTERVAL '1 day', 0),
    ('450e8400-e29b-41d4-a716-446655440003', 'a50e8400-e29b-41d4-a716-446655440001', 'RETURN', 1, 97, 98, '250e8400-e29b-41d4-a716-446655440001', 'PRODUCT_RETURN', 'Product return RET-2024-001', '750e8400-e29b-41d4-a716-446655440004', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    ('450e8400-e29b-41d4-a716-446655440004', 'a50e8400-e29b-41d4-a716-446655440001', 'ADJUSTMENT', 2, 98, 100, NULL, 'MANUAL', 'Stock count adjustment', '750e8400-e29b-41d4-a716-446655440003', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0)
ON CONFLICT (id) DO NOTHING;

-- ========================================
-- 19. AUDIT LOGS
-- ========================================
INSERT INTO audit_logs (id, tenant_id, shop_id, user_id, username, action, entity_type, entity_id, changes, ip_address, user_agent, timestamp, created_at, version)
VALUES
    ('550e8400-e29b-41d4-a716-446655440011', '550e8400-e29b-41d4-a716-446655440001', '650e8400-e29b-41d4-a716-446655440001', '750e8400-e29b-41d4-a716-446655440003', 'manager@testretail.com', 'CREATE', 'EXPENSE', 'f50e8400-e29b-41d4-a716-446655440001', '{"amount": 250.00, "category": "Utilities"}', '192.168.1.100', 'Mozilla/5.0', CURRENT_TIMESTAMP - INTERVAL '5 days', CURRENT_TIMESTAMP - INTERVAL '5 days', 0),
    ('550e8400-e29b-41d4-a716-446655440012', '550e8400-e29b-41d4-a716-446655440001', '650e8400-e29b-41d4-a716-446655440001', '750e8400-e29b-41d4-a716-446655440002', 'owner@testretail.com', 'UPDATE', 'EXPENSE', 'f50e8400-e29b-41d4-a716-446655440001', '{"status": "PENDING -> APPROVED"}', '192.168.1.101', 'Mozilla/5.0', CURRENT_TIMESTAMP - INTERVAL '4 days', CURRENT_TIMESTAMP - INTERVAL '4 days', 0),
    ('550e8400-e29b-41d4-a716-446655440013', '550e8400-e29b-41d4-a716-446655440001', '650e8400-e29b-41d4-a716-446655440001', '750e8400-e29b-41d4-a716-446655440004', 'employee@testretail.com', 'CREATE', 'SALES_TRANSACTION', 'c50e8400-e29b-41d4-a716-446655440001', '{"total": 51.98, "payment": "CASH"}', '192.168.1.102', 'Mozilla/5.0', CURRENT_TIMESTAMP - INTERVAL '2 days', CURRENT_TIMESTAMP - INTERVAL '2 days', 0)
ON CONFLICT (id) DO NOTHING;

-- ========================================
-- 20. FEATURE FLAGS
-- ========================================
INSERT INTO feature_flags (id, feature_name, enabled, tenant_id, shop_id, description, created_at, updated_at, version)
VALUES
    ('650e8400-e29b-41d4-a716-446655440011', 'ANALYTICS', true, NULL, NULL, 'Enable analytics module globally', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    ('650e8400-e29b-41d4-a716-446655440012', 'INVESTMENT', true, NULL, NULL, 'Enable investment tracking globally', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    ('650e8400-e29b-41d4-a716-446655440013', 'FRAUD_DETECTION', true, '550e8400-e29b-41d4-a716-446655440001', NULL, 'Enable fraud detection for tenant', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0)
ON CONFLICT (id) DO NOTHING;

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
