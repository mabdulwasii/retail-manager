-- ========================================
-- Migration V32: Migrate Price/Cost to Inventory + Update LineItems
-- ========================================
-- Description:
--   1. Add product_sku and product_category to line_items table for denormalization
--   2. Add selling_price to inventory (batch-specific selling price)
--   3. Rename inventory.unit_cost to cost_price for consistency
--   4. Remove price and cost_price from products (catalog should not have pricing)
--
-- Rationale:
--   - Product is a pure catalog (metadata: name, SKU, category)
--   - Inventory holds batch-specific pricing (cost_price, selling_price)
--   - LineItems denormalize product info for historical accuracy
--   - Supports per-batch pricing for inflation/market changes
-- ========================================

-- ========================================
-- Part 1: Update line_items table
-- ========================================

-- Add product SKU and category columns to line_items
ALTER TABLE line_items
ADD COLUMN product_sku VARCHAR(255),
ADD COLUMN product_category VARCHAR(255);

-- Populate product_sku and product_category from products table
UPDATE line_items li
SET
    product_sku = (SELECT p.sku FROM products p WHERE p.id = li.product_id),
    product_category = (SELECT c.name FROM products p
                        LEFT JOIN categories c ON p.category_id = c.id
                        WHERE p.id = li.product_id);

-- Make product_sku NOT NULL after population
ALTER TABLE line_items
ALTER COLUMN product_sku SET NOT NULL;

-- ========================================
-- Part 2: Update inventory table
-- ========================================

-- Add selling_price column to inventory (batch-specific selling price)
ALTER TABLE inventory
ADD COLUMN selling_price DECIMAL(10,2);

-- Populate selling_price from products.price (migration strategy)
UPDATE inventory i
SET selling_price = (SELECT p.price FROM products p WHERE p.id = i.product_id);

-- Make selling_price NOT NULL after population
ALTER TABLE inventory
ALTER COLUMN selling_price SET NOT NULL;

-- Rename unit_cost to cost_price for consistency
ALTER TABLE inventory
RENAME COLUMN unit_cost TO cost_price;

-- Make cost_price NOT NULL (batch must have a cost)
UPDATE inventory
SET cost_price = 0
WHERE cost_price IS NULL;

ALTER TABLE inventory
ALTER COLUMN cost_price SET NOT NULL;

-- ========================================
-- Part 3: Remove pricing from products table
-- ========================================

-- Drop price and cost_price columns from products
ALTER TABLE products
DROP COLUMN IF EXISTS price,
DROP COLUMN IF EXISTS cost_price;

-- ========================================
-- Add helpful comment
-- ========================================
COMMENT ON COLUMN inventory.selling_price IS 'Batch-specific selling price - can vary per inventory batch';
COMMENT ON COLUMN inventory.cost_price IS 'Batch-specific cost price - what was paid for this batch';
COMMENT ON COLUMN line_items.product_sku IS 'Denormalized SKU for historical accuracy';
COMMENT ON COLUMN line_items.product_category IS 'Denormalized category name for historical accuracy';
