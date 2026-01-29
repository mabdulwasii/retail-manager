-- Add cost_price column to inventory_unit_prices table
-- This stores the calculated cost for each unit type based on total purchase cost

ALTER TABLE inventory_unit_prices
ADD COLUMN cost_price DECIMAL(10, 2);

-- Make selling_price nullable (user may not provide it during inventory creation)
ALTER TABLE inventory_unit_prices
ALTER COLUMN selling_price DROP NOT NULL;

COMMENT ON COLUMN inventory_unit_prices.cost_price IS 'Calculated cost price for this unit type based on total purchase cost and conversion factors';
COMMENT ON COLUMN inventory_unit_prices.selling_price IS 'Selling price for this unit type (optional, must be set by user)';
