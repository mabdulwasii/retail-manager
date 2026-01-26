-- ============================================================================
-- Migration V59: Add shop_id to receipts table
-- ============================================================================
-- Purpose: Fix LazyInitializationException when serializing receipts
--          by adding direct shop_id column instead of accessing through
--          lazy-loaded transaction relationship
-- ============================================================================

-- Add shop_id column to receipts table
ALTER TABLE receipts ADD COLUMN shop_id VARCHAR(255);

-- Populate shop_id from existing transactions
UPDATE receipts r
SET shop_id = (
    SELECT st.shop_id
    FROM sales_transactions st
    WHERE st.id = r.transaction_id
)
WHERE r.transaction_id IS NOT NULL;

-- Add index for performance (shop-based queries)
CREATE INDEX idx_receipts_shop_id ON receipts(shop_id);

-- Add foreign key constraint
ALTER TABLE receipts
ADD CONSTRAINT fk_receipts_shop
FOREIGN KEY (shop_id) REFERENCES shops(id);

-- Add NOT NULL constraint after data population
ALTER TABLE receipts ALTER COLUMN shop_id SET NOT NULL;
