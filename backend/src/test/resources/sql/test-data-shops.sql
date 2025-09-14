-- Test data setup for shop integration tests
-- This script provides baseline test data for shop-related integration tests

-- Insert test shops with different statuses and configurations
INSERT INTO shops (id, tenant_id, name, description, address, city, state, country, postal_code, phone_number, email, tax_id, status, opening_date, created_at, updated_at, version)
VALUES
    -- Active test shop
    ('test-shop-001', 'tenant-test-001', 'Test Electronics Store', 'Electronics and gadgets for testing', '100 Test Avenue', 'TestCity', 'TestState', 'TestCountry', '12345', '+1-555-001-0001', 'test001@electronics.com', 'TAX001', 'ACTIVE', '2024-01-01 09:00:00', NOW(), NOW(), 0),

    -- Inactive test shop
    ('test-shop-002', 'tenant-test-002', 'Inactive Test Shop', 'Shop for testing inactive status', '200 Test Boulevard', 'TestCity', 'TestState', 'TestCountry', '12346', '+1-555-001-0002', 'test002@inactive.com', 'TAX002', 'INACTIVE', '2024-01-02 10:00:00', NOW(), NOW(), 0),

    -- Suspended test shop
    ('test-shop-003', 'tenant-test-003', 'Suspended Test Shop', 'Shop for testing suspended status', '300 Test Lane', 'TestCity', 'TestState', 'TestCountry', '12347', '+1-555-001-0003', 'test003@suspended.com', 'TAX003', 'SUSPENDED', '2024-01-03 11:00:00', NOW(), NOW(), 0),

    -- Multi-tenant isolation test shops
    ('test-shop-mt-001', 'tenant-multi-001', 'Multi-Tenant Shop A', 'Shop A for multi-tenant testing', '400 Tenant A Street', 'TenantACity', 'TenantAState', 'TenantACountry', '54321', '+1-555-001-1001', 'tenanta@multitenant.com', 'TAXA', 'ACTIVE', '2024-01-10 09:00:00', NOW(), NOW(), 0),

    ('test-shop-mt-002', 'tenant-multi-002', 'Multi-Tenant Shop B', 'Shop B for multi-tenant testing', '500 Tenant B Avenue', 'TenantBCity', 'TenantBState', 'TenantBCountry', '54322', '+1-555-001-1002', 'tenantb@multitenant.com', 'TAXB', 'ACTIVE', '2024-01-10 10:00:00', NOW(), NOW(), 0);

-- Insert test users for shops (if users table exists)
-- This assumes users table structure - adjust as needed
-- INSERT INTO users (id, username, email, first_name, last_name, created_at, updated_at, version)
-- VALUES
--     ('test-user-001', 'test-shop-owner-001', 'owner001@test.com', 'Test', 'Owner1', NOW(), NOW(), 0),
--     ('test-user-002', 'test-shop-manager-002', 'manager002@test.com', 'Test', 'Manager2', NOW(), NOW(), 0);

-- Insert shop-user relationships (if shop_users table exists)
-- INSERT INTO shop_users (shop_id, user_id)
-- VALUES
--     ('test-shop-001', 'test-user-001'),
--     ('test-shop-002', 'test-user-002');

-- Insert test feature flags for shops
INSERT INTO feature_flags (id, shop_id, feature_name, enabled, description, created_by, last_modified_by, created_at, updated_at, version)
VALUES
    -- Global feature flags
    ('test-ff-global-001', NULL, 'multi-tenant.enabled', TRUE, 'Multi-tenant testing enabled globally', 'TEST_SYSTEM', 'TEST_SYSTEM', NOW(), NOW(), 0),
    ('test-ff-global-002', NULL, 'investment.enabled', TRUE, 'Investment features enabled globally', 'TEST_SYSTEM', 'TEST_SYSTEM', NOW(), NOW(), 0),
    ('test-ff-global-003', NULL, 'analytics.enabled', TRUE, 'Analytics features enabled globally', 'TEST_SYSTEM', 'TEST_SYSTEM', NOW(), NOW(), 0),

    -- Shop-specific feature flags
    ('test-ff-shop-001-mt', 'test-shop-001', 'multi-tenant.enabled', TRUE, 'Multi-tenant enabled for test shop 001', 'TEST_SYSTEM', 'TEST_SYSTEM', NOW(), NOW(), 0),
    ('test-ff-shop-001-inv', 'test-shop-001', 'investment.enabled', TRUE, 'Investment enabled for test shop 001', 'TEST_SYSTEM', 'TEST_SYSTEM', NOW(), NOW(), 0),
    ('test-ff-shop-002-mt', 'test-shop-002', 'multi-tenant.enabled', FALSE, 'Multi-tenant disabled for test shop 002', 'TEST_SYSTEM', 'TEST_SYSTEM', NOW(), NOW(), 0),
    ('test-ff-shop-003-analytics', 'test-shop-003', 'analytics.enabled', FALSE, 'Analytics disabled for suspended shop 003', 'TEST_SYSTEM', 'TEST_SYSTEM', NOW(), NOW(), 0);

-- Insert test shop customizations
INSERT INTO shop_customizations (id, shop_id, primary_color, secondary_color, theme_variant, font_size, dashboard_layout, show_banner, enable_animations, show_advanced_features, receipt_show_logo, created_at, updated_at, version)
VALUES
    ('test-custom-001', 'test-shop-001', '#FF5733', '#33A1FF', 'LIGHT', 'MEDIUM', 'GRID', TRUE, TRUE, FALSE, TRUE, NOW(), NOW(), 0),
    ('test-custom-002', 'test-shop-002', '#8B0000', '#FFD700', 'DARK', 'LARGE', 'LIST', FALSE, FALSE, TRUE, FALSE, NOW(), NOW(), 0),
    ('test-custom-mt-001', 'test-shop-mt-001', '#228B22', '#FF69B4', 'LIGHT', 'SMALL', 'CARD', TRUE, TRUE, FALSE, TRUE, NOW(), NOW(), 0);

-- Update shop configuration embedded objects if needed
-- This would depend on how ShopConfiguration is mapped