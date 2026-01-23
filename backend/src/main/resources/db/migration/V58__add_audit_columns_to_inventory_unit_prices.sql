-- Migration V58: Add missing audit columns to multi-unit tables
-- Both entities extend BaseEntity which requires created_by, updated_by, and version columns

-- Fix product_unit_definitions table (created in V55)
ALTER TABLE product_unit_definitions ADD COLUMN IF NOT EXISTS created_by VARCHAR(36);
ALTER TABLE product_unit_definitions ADD COLUMN IF NOT EXISTS updated_by VARCHAR(36);
ALTER TABLE product_unit_definitions ADD COLUMN IF NOT EXISTS version BIGINT NOT NULL DEFAULT 0;

-- Fix inventory_unit_prices table (created in V56)
ALTER TABLE inventory_unit_prices ADD COLUMN IF NOT EXISTS created_by VARCHAR(36);
ALTER TABLE inventory_unit_prices ADD COLUMN IF NOT EXISTS updated_by VARCHAR(36);
ALTER TABLE inventory_unit_prices ADD COLUMN IF NOT EXISTS version BIGINT NOT NULL DEFAULT 0;

-- Add comments for product_unit_definitions
COMMENT ON COLUMN product_unit_definitions.created_by IS 'User ID who created this record';
COMMENT ON COLUMN product_unit_definitions.updated_by IS 'User ID who last updated this record';
COMMENT ON COLUMN product_unit_definitions.version IS 'Optimistic locking version number';

-- Add comments for inventory_unit_prices
COMMENT ON COLUMN inventory_unit_prices.created_by IS 'User ID who created this record';
COMMENT ON COLUMN inventory_unit_prices.updated_by IS 'User ID who last updated this record';
COMMENT ON COLUMN inventory_unit_prices.version IS 'Optimistic locking version number';
