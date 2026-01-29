-- ============================================================================
-- V60: Update Inventory Cost Structure
-- ============================================================================
-- Changes:
-- 1. Remove current_stock column (redundant, stock tracked via purchase_quantity)
-- 2. Rename purchase_unit_cost to total_purchase_cost
-- 3. Update existing data to calculate total cost from unit cost
-- ============================================================================

-- Step 1: Add new total_purchase_cost column
ALTER TABLE inventory
ADD COLUMN total_purchase_cost DECIMAL(12, 2);

-- Step 2: Migrate existing data
-- Calculate total_purchase_cost = purchase_unit_cost * purchase_quantity
UPDATE inventory
SET total_purchase_cost = COALESCE(purchase_unit_cost, 0) * COALESCE(purchase_quantity, 0)
WHERE purchase_unit_cost IS NOT NULL AND purchase_quantity IS NOT NULL;

-- Step 3: For records without purchase info, use cost_price as fallback
UPDATE inventory
SET total_purchase_cost = COALESCE(cost_price, 0)
WHERE total_purchase_cost IS NULL;

-- Step 4: Drop old purchase_unit_cost column
ALTER TABLE inventory
DROP COLUMN IF EXISTS purchase_unit_cost;

-- Step 5: Drop current_stock column (stock now computed from purchase_quantity)
ALTER TABLE inventory
DROP COLUMN IF EXISTS current_stock;

-- ============================================================================
-- Indexes and Comments
-- ============================================================================

COMMENT ON COLUMN inventory.total_purchase_cost IS 'Total cost for all purchased quantity (e.g., ₦106,000 for 20 packs). System calculates cost per unit from this.';
COMMENT ON COLUMN inventory.purchase_quantity IS 'Quantity purchased in purchase_unit (e.g., 20 packs). Stock is tracked via this field.';
