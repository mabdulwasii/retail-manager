-- Migration V55: Create product_unit_definitions table for multi-unit pricing
-- This table stores unit structure for products (e.g., piece, pack, carton)
-- Product defines the structure, Inventory defines the prices

CREATE TABLE product_unit_definitions (
    id VARCHAR(36) PRIMARY KEY,
    product_id VARCHAR(36) NOT NULL,
    unit_type VARCHAR(50) NOT NULL,
    unit_label VARCHAR(100) NOT NULL,
    conversion_factor DECIMAL(10, 4) NOT NULL,
    is_base_unit BOOLEAN DEFAULT false,
    sort_order INTEGER DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_product_unit_product FOREIGN KEY (product_id)
        REFERENCES products(id) ON DELETE CASCADE,
    CONSTRAINT unique_product_unit UNIQUE (product_id, unit_type),
    CONSTRAINT positive_conversion CHECK (conversion_factor > 0)
);

-- Create indexes for performance
CREATE INDEX idx_product_unit_definitions_product_id ON product_unit_definitions(product_id);
CREATE INDEX idx_product_unit_definitions_unit_type ON product_unit_definitions(unit_type);
CREATE INDEX idx_product_unit_definitions_base_unit ON product_unit_definitions(is_base_unit) WHERE is_base_unit = true;

-- Add comment
COMMENT ON TABLE product_unit_definitions IS 'Defines available units for products (catalog level - no prices)';
COMMENT ON COLUMN product_unit_definitions.unit_type IS 'Unit identifier: piece, pack, half_pack, carton, roll, custom, etc.';
COMMENT ON COLUMN product_unit_definitions.unit_label IS 'Display name: "Piece", "Pack (12pcs)", "Half Pack (6pcs)"';
COMMENT ON COLUMN product_unit_definitions.conversion_factor IS 'How many base units in this unit (e.g., pack = 12 pieces)';
COMMENT ON COLUMN product_unit_definitions.is_base_unit IS 'True for the smallest unit (usually piece/kg/liter)';
