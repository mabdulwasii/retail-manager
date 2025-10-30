-- Add shop_id column to categories table to support shop-scoped categories
-- Categories should belong to shops since products belong to shops

-- Add shop_id column (nullable initially to allow data migration)
ALTER TABLE categories ADD COLUMN shop_id VARCHAR(36);

-- Add foreign key constraint
ALTER TABLE categories ADD CONSTRAINT fk_category_shop
    FOREIGN KEY (shop_id) REFERENCES shops(id) ON DELETE CASCADE;

-- Create index for shop_id lookups
CREATE INDEX idx_category_shop ON categories(shop_id);

-- Update the unique constraint to be shop-scoped
-- Drop old constraint
ALTER TABLE categories DROP CONSTRAINT IF EXISTS categories_name_key;

-- Add new composite unique constraint (name must be unique per shop)
ALTER TABLE categories ADD CONSTRAINT uk_category_name_shop
    UNIQUE (shop_id, name);

-- Make shop_id non-nullable after data migration
-- Note: In production, you would first populate shop_id from products or a default shop
-- For now, making it nullable with the understanding it needs to be populated
ALTER TABLE categories ALTER COLUMN shop_id SET NOT NULL;