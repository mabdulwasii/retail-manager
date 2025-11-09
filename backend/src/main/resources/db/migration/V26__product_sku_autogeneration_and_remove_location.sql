-- V26: Product SKU Auto-Generation and Remove Location Field
--
-- Changes:
-- 1. Drop location column from products table (moved to inventory table)
-- 2. Add barcode index for faster lookups (barcode scanner support)
-- 3. SKU is now auto-generated on backend (no schema change needed, SKU column remains)
--
-- Migration Date: 2025-01-09
-- Related Features:
--   - SKU auto-generation (format: SHOP-CAT-YYYYMMDD-RAND)
--   - Barcode scanner integration
--   - Product location moved to Inventory.location for batch-specific storage

-- =====================================================
-- 1. Remove location column from products table
-- =====================================================
-- Location is now managed at inventory level (Inventory.location)
-- Each inventory batch can have its own storage location

ALTER TABLE products DROP COLUMN IF EXISTS location;

-- =====================================================
-- 2. Add barcode index for performance
-- =====================================================
-- Enables fast product lookup for barcode scanner integration
-- GET /api/products/search?barcode={barcode}&shopId={shopId}

CREATE INDEX IF NOT EXISTS idx_product_barcode ON products(barcode) WHERE barcode IS NOT NULL;

-- =====================================================
-- Notes:
-- =====================================================
-- 1. SKU Generation:
--    - Format: {SHOP_CODE}-{CATEGORY_CODE}-{YYYYMMDD}-{RANDOM4}
--    - Example: GOM-BEV-20250109-A7F3
--    - Generated automatically in ProductService.generateUniqueSku()
--
-- 2. Location Management:
--    - Product.location removed (was redundant)
--    - Use Inventory.location for batch-specific storage locations
--    - Allows different batches to be stored in different locations
--
-- 3. Barcode Scanner Workflow:
--    - Manual entry during product creation
--    - Scanner acts as keyboard wedge (types + Enter)
--    - Sales page uses GET /api/products/search?barcode={scanned}
--    - Product auto-populates in cart
--
-- 4. Jackson Configuration:
--    - spring.jackson.deserialization.fail-on-unknown-properties: false
--    - Allows frontend to send extra fields without errors
--    - Fixes SKU field error when editing products (SKU not in UpdateRequest)
