-- Fix line_items.quantity from DECIMAL to INTEGER
-- Quantity should be a whole number, not a decimal

ALTER TABLE line_items
ALTER COLUMN quantity TYPE INTEGER USING quantity::INTEGER;
