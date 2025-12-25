-- Comprehensive type fixes for embedded PostgreSQL schema validation
-- Fix all remaining DECIMAL/NUMERIC to INTEGER or DOUBLE PRECISION based on entity types

-- Fix integer quantity fields
ALTER TABLE product_returns
ALTER COLUMN quantity_returned TYPE INTEGER USING quantity_returned::INTEGER;

-- Fix float/double fields (these use Double in entities without precision/scale)
ALTER TABLE shops
ALTER COLUMN max_discount_percentage TYPE DOUBLE PRECISION USING max_discount_percentage::DOUBLE PRECISION,
ALTER COLUMN tax_rate TYPE DOUBLE PRECISION USING tax_rate::DOUBLE PRECISION;

ALTER TABLE products
ALTER COLUMN discount_rate TYPE DOUBLE PRECISION USING discount_rate::DOUBLE PRECISION;

ALTER TABLE investments
ALTER COLUMN profit_percentage TYPE DOUBLE PRECISION USING profit_percentage::DOUBLE PRECISION;

-- NOTE: The following use BigDecimal with @Column(precision, scale) so they stay as NUMERIC:
-- - investor_shares.share_percentage (NUMERIC 5,2)
-- - investor_distributions.investor_share_percentage (NUMERIC 5,2)
-- - line_items.discount_percentage (NUMERIC 5,2)
