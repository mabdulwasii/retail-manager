-- ========================================
-- Migration V16: Add shop_id to users table
-- ========================================
-- This migration adds a direct shop relationship to users.
-- Users belong to ONE specific shop (branch), making the model more realistic.
--
-- Architecture:
-- - User → Shop (ManyToOne) - User works at one shop
-- - Shop → Tenant (ManyToOne) - Shop belongs to one tenant
-- - Tenant is derived via user.shop.tenant
--
-- Reason: Employees typically work at a specific branch/location, not across
-- all branches of an organization.

-- Add shop_id column (nullable initially for data migration)
ALTER TABLE users ADD COLUMN IF NOT EXISTS shop_id VARCHAR(36);

-- Add foreign key constraint
ALTER TABLE users ADD CONSTRAINT fk_user_shop
    FOREIGN KEY (shop_id) REFERENCES shops(id) ON DELETE RESTRICT;

-- Add index for shop-based user lookups
CREATE INDEX IF NOT EXISTS idx_user_shop ON users(shop_id);

-- Add comment
COMMENT ON COLUMN users.shop_id IS 'The specific shop/branch where this user works';

-- Note: In production, populate shop_id from business logic, then make it NOT NULL
-- For now, keeping it nullable to allow existing data
-- Future migration can make it NOT NULL: ALTER TABLE users ALTER COLUMN shop_id SET NOT NULL;
