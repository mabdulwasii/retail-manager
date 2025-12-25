-- Fix inventory_history column types from DECIMAL to INTEGER
-- Stock quantities should be whole numbers, not decimals

ALTER TABLE inventory_history
ALTER COLUMN quantity_change TYPE INTEGER USING quantity_change::INTEGER,
ALTER COLUMN previous_stock TYPE INTEGER USING previous_stock::INTEGER,
ALTER COLUMN new_stock TYPE INTEGER USING new_stock::INTEGER;
