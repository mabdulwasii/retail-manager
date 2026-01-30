-- Add shop_id column to categories table to support shop-scoped categories
-- Categories should belong to shops since products belong to shops

-- Add shop_id column (nullable initially to allow data migration)
ALTER TABLE categories ADD COLUMN IF NOT EXISTS shop_id VARCHAR(36);

-- Add foreign key constraint
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'fk_category_shop') THEN
        ALTER TABLE categories ADD CONSTRAINT fk_category_shop
            FOREIGN KEY (shop_id) REFERENCES shops(id) ON DELETE CASCADE;
    END IF;
END$$;

-- Create index for shop_id lookups
CREATE INDEX IF NOT EXISTS idx_category_shop ON categories(shop_id);

-- Update the unique constraint to be shop-scoped
-- Drop old constraint
ALTER TABLE categories DROP CONSTRAINT IF EXISTS categories_name_key;

-- Add new composite unique constraint (name must be unique per shop)
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'uk_category_name_shop') THEN
        ALTER TABLE categories ADD CONSTRAINT uk_category_name_shop
            UNIQUE (shop_id, name);
    END IF;
END$$;

-- Make shop_id non-nullable after data migration (skip if already not null)
DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM information_schema.columns
               WHERE table_name='categories' AND column_name='shop_id' AND is_nullable='YES') THEN
        ALTER TABLE categories ALTER COLUMN shop_id SET NOT NULL;
    END IF;
END$$;