-- Fix NULL version values across all versioned entities
-- This migration ensures all entities with optimistic locking have valid version values

-- ========================================
-- UPDATE CORE DOMAIN ENTITIES
-- ========================================

-- Update shops with NULL version
UPDATE shops SET version = 0 WHERE version IS NULL;

-- Update tenants with NULL version
UPDATE tenants SET version = 0 WHERE version IS NULL;

-- Update users with NULL version
UPDATE users SET version = 0 WHERE version IS NULL;

-- Update roles with NULL version
UPDATE roles SET version = 0 WHERE version IS NULL;

-- Update permissions with NULL version
UPDATE permissions SET version = 0 WHERE version IS NULL;

-- Update products with NULL version
UPDATE products SET version = 0 WHERE version IS NULL;

-- Update categories with NULL version
UPDATE categories SET version = 0 WHERE version IS NULL;

-- ========================================
-- UPDATE SALES & INVENTORY ENTITIES
-- ========================================

-- Update sales_transactions with NULL version
UPDATE sales_transactions SET version = 0 WHERE version IS NULL;

-- Update receipts with NULL version
UPDATE receipts SET version = 0 WHERE version IS NULL;

-- Update line_items with NULL version
UPDATE line_items SET version = 0 WHERE version IS NULL;

-- Update inventory with NULL version
UPDATE inventory SET version = 0 WHERE version IS NULL;

-- Update inventory_history with NULL version
UPDATE inventory_history SET version = 0 WHERE version IS NULL;

-- ========================================
-- UPDATE INVESTMENT ENTITIES
-- ========================================

-- Update investments with NULL version
UPDATE investments SET version = 0 WHERE version IS NULL;

-- Update investor_shares with NULL version
UPDATE investor_shares SET version = 0 WHERE version IS NULL;

-- Update investor_distributions with NULL version
UPDATE investor_distributions SET version = 0 WHERE version IS NULL;

-- ========================================
-- UPDATE FRAUD & SECURITY ENTITIES
-- ========================================

-- Update fraud_alerts with NULL version (only if column exists)
DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'fraud_alerts' AND column_name = 'version') THEN
        UPDATE fraud_alerts SET version = 0 WHERE version IS NULL;
    END IF;
END$$;

-- Update fraud_rules with NULL version (only if column exists)
DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'fraud_rules' AND column_name = 'version') THEN
        UPDATE fraud_rules SET version = 0 WHERE version IS NULL;
    END IF;
END$$;

-- Update risk_assessments with NULL version (only if column exists)
DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'risk_assessments' AND column_name = 'version') THEN
        UPDATE risk_assessments SET version = 0 WHERE version IS NULL;
    END IF;
END$$;

-- ========================================
-- UPDATE EXPENSE ENTITIES
-- ========================================

-- Update expenses with NULL version
UPDATE expenses SET version = 0 WHERE version IS NULL;

-- Update expense_categories with NULL version
UPDATE expense_categories SET version = 0 WHERE version IS NULL;

-- ========================================
-- UPDATE OTHER ENTITIES
-- ========================================

-- Update product_returns with NULL version
UPDATE product_returns SET version = 0 WHERE version IS NULL;

-- Update audit_logs with NULL version
UPDATE audit_logs SET version = 0 WHERE version IS NULL;

-- Update feature_flags with NULL version
UPDATE feature_flags SET version = 0 WHERE version IS NULL;

-- Update analytics_cache with NULL version
UPDATE analytics_cache SET version = 0 WHERE version IS NULL;

-- Update shop_customizations with NULL version
UPDATE shop_customizations SET version = 0 WHERE version IS NULL;

-- ========================================
-- ADD NOT NULL CONSTRAINTS WHERE APPROPRIATE
-- ========================================

-- Make version NOT NULL for critical tables (can be extended to others)
ALTER TABLE shops ALTER COLUMN version SET DEFAULT 0;
ALTER TABLE shops ALTER COLUMN version SET NOT NULL;

ALTER TABLE tenants ALTER COLUMN version SET DEFAULT 0;
ALTER TABLE tenants ALTER COLUMN version SET NOT NULL;

ALTER TABLE users ALTER COLUMN version SET DEFAULT 0;
ALTER TABLE users ALTER COLUMN version SET NOT NULL;

ALTER TABLE products ALTER COLUMN version SET DEFAULT 0;
ALTER TABLE products ALTER COLUMN version SET NOT NULL;

ALTER TABLE sales_transactions ALTER COLUMN version SET DEFAULT 0;
ALTER TABLE sales_transactions ALTER COLUMN version SET NOT NULL;

ALTER TABLE inventory ALTER COLUMN version SET DEFAULT 0;
ALTER TABLE inventory ALTER COLUMN version SET NOT NULL;

-- ========================================
-- VERIFY COUNTS
-- ========================================

-- Log counts of updated records (as comments for documentation)
-- SELECT COUNT(*) FROM shops WHERE version = 0;
-- SELECT COUNT(*) FROM tenants WHERE version = 0;
-- SELECT COUNT(*) FROM users WHERE version = 0;
