-- Drop maximum_stock and reorder_point columns from inventory table
-- These fields are no longer used in the application

ALTER TABLE inventory DROP COLUMN IF EXISTS maximum_stock;
ALTER TABLE inventory DROP COLUMN IF EXISTS reorder_point;
