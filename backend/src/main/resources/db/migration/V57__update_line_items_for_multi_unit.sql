-- Migration V57: Update line_items table to track unit information in sales
-- This enables sales tracking by unit type with proper conversion to base units

-- Add unit tracking fields to line_items
ALTER TABLE line_items ADD COLUMN IF NOT EXISTS unit_type VARCHAR(50);
ALTER TABLE line_items ADD COLUMN IF NOT EXISTS unit_label VARCHAR(100);
ALTER TABLE line_items ADD COLUMN IF NOT EXISTS unit_conversion_factor DECIMAL(10, 4) DEFAULT 1.0;
ALTER TABLE line_items ADD COLUMN IF NOT EXISTS base_unit_quantity INTEGER;

-- Create index for unit type queries
CREATE INDEX idx_line_items_unit_type ON line_items(unit_type) WHERE unit_type IS NOT NULL;

-- Add comments
COMMENT ON COLUMN line_items.unit_type IS 'Unit type sold (piece, pack, half_pack, etc.) - matches product_unit_definitions';
COMMENT ON COLUMN line_items.unit_label IS 'Display label for the unit sold (e.g., "Pack (12pcs)")';
COMMENT ON COLUMN line_items.unit_conversion_factor IS 'Conversion factor at time of sale (e.g., 12 for pack of 12)';
COMMENT ON COLUMN line_items.base_unit_quantity IS 'Quantity in base units (quantity × conversion_factor) used for FEFO';

-- Note: quantity column remains as-is (quantity sold in the selected unit)
-- base_unit_quantity = quantity × unit_conversion_factor
-- Example: Sold 5 packs → quantity=5, unit_conversion_factor=12, base_unit_quantity=60
