-- ============================================================================
-- Migration V60: Add cost_price to line_items table
-- ============================================================================
-- Purpose: Enable profit calculation by storing cost price at time of sale
-- Impact: Fixes sales profit showing total revenue instead of actual profit
-- ============================================================================

-- Add cost_price column to line_items table
ALTER TABLE line_items ADD COLUMN cost_price DECIMAL(10,2);

-- Add comment for documentation
COMMENT ON COLUMN line_items.cost_price IS 'Cost price per unit at time of sale for profit calculation';

-- Create index for performance (profit reporting queries)
CREATE INDEX idx_line_items_cost_price ON line_items(cost_price);

-- Note: Existing line_items will have NULL cost_price
-- Future sales will populate this field from inventory.cost_price
