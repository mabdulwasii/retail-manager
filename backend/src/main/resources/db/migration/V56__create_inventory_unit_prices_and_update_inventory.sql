-- Migration V56: Create inventory_unit_prices table and update inventory table
-- This enables batch-specific pricing for different units

-- Add multi-unit fields to inventory table
ALTER TABLE inventory ADD COLUMN IF NOT EXISTS base_unit VARCHAR(50) DEFAULT 'piece';
ALTER TABLE inventory ADD COLUMN IF NOT EXISTS purchase_unit VARCHAR(50);
ALTER TABLE inventory ADD COLUMN IF NOT EXISTS purchase_quantity DECIMAL(10, 2);
ALTER TABLE inventory ADD COLUMN IF NOT EXISTS purchase_unit_cost DECIMAL(10, 2);

-- Add comments to new inventory columns
COMMENT ON COLUMN inventory.base_unit IS 'Smallest sellable unit for stock tracking (piece, kg, liter, etc.)';
COMMENT ON COLUMN inventory.purchase_unit IS 'Unit in which this batch was purchased (e.g., pack, carton)';
COMMENT ON COLUMN inventory.purchase_quantity IS 'Quantity purchased in purchase_unit (e.g., 10 packs)';
COMMENT ON COLUMN inventory.purchase_unit_cost IS 'Cost per purchase_unit (e.g., ₦12,000 per pack)';

-- Create inventory_unit_prices table for batch-specific unit pricing
CREATE TABLE inventory_unit_prices (
    id VARCHAR(36) PRIMARY KEY,
    inventory_id VARCHAR(36) NOT NULL,
    unit_type VARCHAR(50) NOT NULL,
    selling_price DECIMAL(10, 2) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_inventory_unit_price_inventory FOREIGN KEY (inventory_id)
        REFERENCES inventory(id) ON DELETE CASCADE,
    CONSTRAINT unique_inventory_unit UNIQUE (inventory_id, unit_type),
    CONSTRAINT positive_selling_price CHECK (selling_price >= 0)
);

-- Create indexes for performance
CREATE INDEX idx_inventory_unit_prices_inventory_id ON inventory_unit_prices(inventory_id);
CREATE INDEX idx_inventory_unit_prices_unit_type ON inventory_unit_prices(unit_type);

-- Add comments
COMMENT ON TABLE inventory_unit_prices IS 'Batch-specific selling prices for each unit type';
COMMENT ON COLUMN inventory_unit_prices.unit_type IS 'Must match a unit_type in product_unit_definitions';
COMMENT ON COLUMN inventory_unit_prices.selling_price IS 'Selling price for this unit in this specific batch';
