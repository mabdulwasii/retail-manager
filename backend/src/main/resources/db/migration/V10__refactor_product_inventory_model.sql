-- Migration to refactor Product/Inventory model
-- Removes stock tracking fields from products table as stock is now managed via inventory table
--
-- This implements a two-tier model:
-- - Product: Master catalog (what you sell) - SKU, price, description
-- - Inventory: Stock tracking (what you have) - batches, locations, expiry dates
--
-- Migration Strategy:
-- 1. Migrate existing product stock data to inventory records (if products have stock)
-- 2. Remove stock-related columns from products table
-- 3. Update ProductStatus enum (remove OUT_OF_STOCK)
--
-- Author: AI Assistant
-- Date: 2025-01-XX
--================================================================================

-- Step 1: Migrate existing product stock to inventory records
-- Only create inventory for products that have non-zero stock
-- SKIP: This migration is only needed when upgrading from old schema
-- The current JPA schema (V1) already has the correct structure without current_stock
-- INSERT INTO inventory (
--     id,
--     shop_id,
--     product_id,
--     current_stock,
--     reserved_stock,
--     minimum_stock,
--     maximum_stock,
--     reorder_point,
--     unit_cost,
--     location,
--     batch_number,
--     status,
--     last_stock_update,
--     created_at,
--     updated_at,
--     created_by,
--     updated_by,
--     version
-- )
-- SELECT
--     gen_random_uuid(),                                    -- id
--     p.shop_id,                                           -- shop_id
--     p.id,                                                -- product_id
--     COALESCE(p.quantity_in_stock, 0),                   -- current_stock
--     0,                                                   -- reserved_stock (default to 0)
--     COALESCE(p.minimum_stock_level, 0),                 -- minimum_stock
--     COALESCE(p.maximum_stock_level, 1000),              -- maximum_stock
--     COALESCE(p.reorder_point, 10),                      -- reorder_point
--     p.cost_price,                                        -- unit_cost
--     p.location,                                          -- location
--     'MIGRATED-' || SUBSTRING(p.id::TEXT, 1, 8),         -- batch_number (unique identifier)
--     CASE
--         WHEN COALESCE(p.quantity_in_stock, 0) > 0 THEN 'ACTIVE'::text
--         ELSE 'INACTIVE'::text
--     END,                                                 -- status
--     CURRENT_TIMESTAMP,                                   -- last_stock_update
--     COALESCE(p.created_at, CURRENT_TIMESTAMP),          -- created_at
--     CURRENT_TIMESTAMP,                                   -- updated_at
--     COALESCE(p.created_by, 'SYSTEM'),                   -- created_by
--     'SYSTEM',                                            -- updated_by
--     0                                                    -- version
-- FROM products p
-- WHERE p.quantity_in_stock IS NOT NULL
--   AND p.quantity_in_stock > 0
--   AND NOT EXISTS (
--     -- Don't create duplicate inventory if already exists
--     SELECT 1 FROM inventory i
--     WHERE i.product_id = p.id
--     AND i.batch_number = 'MIGRATED-' || SUBSTRING(p.id::TEXT, 1, 8)
-- );

-- Step 2: Update product status from OUT_OF_STOCK to INACTIVE
-- Since OUT_OF_STOCK is being removed from enum
UPDATE products
SET status = 'INACTIVE'
WHERE status = 'OUT_OF_STOCK';

-- Step 3: Remove stock-related columns from products table
ALTER TABLE products
DROP COLUMN IF EXISTS quantity_in_stock,
DROP COLUMN IF EXISTS minimum_stock_level,
DROP COLUMN IF EXISTS maximum_stock_level,
DROP COLUMN IF EXISTS reorder_point;

-- Step 4: Add indexes for performance
-- (Inventory indexes should already exist, but add product-related ones if missing)
CREATE INDEX IF NOT EXISTS idx_inventory_product_shop ON inventory(product_id, shop_id);
-- Skipped: current_stock removed in V60, use purchase_quantity instead
-- CREATE INDEX IF NOT EXISTS idx_inventory_available_stock ON inventory((current_stock - reserved_stock)) WHERE status = 'ACTIVE';
CREATE INDEX IF NOT EXISTS idx_inventory_expiry_date ON inventory(expiry_date) WHERE expiry_date IS NOT NULL;

-- Step 5: Add comment to products table documenting the change
COMMENT ON TABLE products IS 'Product master catalog. Stock tracking moved to inventory table as of V10.';
COMMENT ON TABLE inventory IS 'Stock tracking by batch/location. Aggregates to product-level stock.';

-- Migration complete
-- Products now represent catalog only (what you sell)
-- Inventory represents stock (what you have, where, and when it expires)
