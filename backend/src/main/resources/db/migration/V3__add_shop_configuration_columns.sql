-- Add missing ShopConfiguration embedded columns to shops table
-- These columns are required by the @Embedded ShopConfiguration in Shop entity

ALTER TABLE shops
ADD COLUMN IF NOT EXISTS investment_enabled BOOLEAN DEFAULT TRUE,
ADD COLUMN IF NOT EXISTS analytics_enabled BOOLEAN DEFAULT TRUE,
ADD COLUMN IF NOT EXISTS fraud_detection_enabled BOOLEAN DEFAULT FALSE,
ADD COLUMN IF NOT EXISTS auto_backup_enabled BOOLEAN DEFAULT TRUE,
ADD COLUMN IF NOT EXISTS currency VARCHAR(10) DEFAULT 'NGN',
ADD COLUMN IF NOT EXISTS tax_rate DECIMAL(5,2) DEFAULT 0.0,
ADD COLUMN IF NOT EXISTS max_discount_percentage DECIMAL(5,2) DEFAULT 20.0,
ADD COLUMN IF NOT EXISTS receipt_footer VARCHAR(500);

-- Update existing records to have default values for the new columns
UPDATE shops SET
    investment_enabled = COALESCE(investment_enabled, TRUE),
    analytics_enabled = COALESCE(analytics_enabled, TRUE),
    fraud_detection_enabled = COALESCE(fraud_detection_enabled, FALSE),
    auto_backup_enabled = COALESCE(auto_backup_enabled, TRUE),
    currency = COALESCE(currency, 'NGN'),
    tax_rate = COALESCE(tax_rate, 0.0),
    max_discount_percentage = COALESCE(max_discount_percentage, 20.0)
WHERE currency IS NULL OR investment_enabled IS NULL OR analytics_enabled IS NULL
   OR fraud_detection_enabled IS NULL OR auto_backup_enabled IS NULL
   OR tax_rate IS NULL OR max_discount_percentage IS NULL;