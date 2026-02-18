-- ============================================================================
-- V63: Re-add current_stock Column for Base Unit Stock Tracking
-- ============================================================================
-- V60 dropped current_stock in favour of tracking via purchase_quantity.
-- However purchase_quantity is in purchase units (e.g., packs) while all
-- sales deductions happen in base units (e.g., pieces). This caused a bug
-- where selling 1 piece deducted 1 pack.
--
-- This migration:
-- 1. Adds current_stock BIGINT to track stock in base units
-- 2. Initialises it from purchase_quantity × conversion_factor for existing rows
-- ============================================================================

-- Step 1: Add the column (nullable initially for migration)
ALTER TABLE inventory
ADD COLUMN current_stock BIGINT NOT NULL DEFAULT 0;

-- Step 2: Initialise current_stock for rows that have a purchase unit with a
--         known conversion factor in product_unit_definitions.
--         e.g. 20 packs × 12 pieces/pack = 240 pieces
UPDATE inventory i
SET current_stock = ROUND(
    COALESCE(i.purchase_quantity, 0) *
    COALESCE(
        (SELECT pud.conversion_factor
         FROM product_unit_definitions pud
         WHERE pud.product_id = i.product_id
           AND LOWER(pud.unit_type) = LOWER(i.purchase_unit)
         LIMIT 1),
        1  -- fallback: treat purchase unit as base unit (factor = 1)
    )
)
WHERE i.purchase_quantity IS NOT NULL
  AND i.purchase_quantity > 0;

-- ============================================================================
-- Indexes and Comments
-- ============================================================================

COMMENT ON COLUMN inventory.current_stock IS
  'Remaining stock in base units (e.g., pieces). Decremented on each sale by base-unit quantity. Separate from purchase_quantity which records original purchase amounts.';
