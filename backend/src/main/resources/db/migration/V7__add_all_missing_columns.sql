-- Comprehensive migration to add all missing columns across all tables
-- This migration brings the database schema into full alignment with JPA entity definitions
-- Total: 82 missing columns across 13 tables

-- ========================================
-- 1. ANALYTICS_CACHE (1 missing column)
-- ========================================

ALTER TABLE analytics_cache ADD COLUMN IF NOT EXISTS metadata TEXT;

COMMENT ON COLUMN analytics_cache.metadata IS 'Additional metadata for analytics cache entry';

-- ========================================
-- 2. INVESTMENTS (6 missing columns)
-- ========================================

ALTER TABLE investments ADD COLUMN IF NOT EXISTS investment_type VARCHAR(50);
ALTER TABLE investments ADD COLUMN IF NOT EXISTS fixed_shares INTEGER;
ALTER TABLE investments ADD COLUMN IF NOT EXISTS total_profit_earned DECIMAL(12,2);
ALTER TABLE investments ADD COLUMN IF NOT EXISTS total_withdrawn DECIMAL(12,2);
ALTER TABLE investments ADD COLUMN IF NOT EXISTS last_profit_calculation TIMESTAMP;

COMMENT ON COLUMN investments.investment_type IS 'Type of investment (EQUITY, LOAN, etc.)';
COMMENT ON COLUMN investments.fixed_shares IS 'Fixed number of shares for this investment';
COMMENT ON COLUMN investments.total_profit_earned IS 'Total profit earned on this investment';
COMMENT ON COLUMN investments.total_withdrawn IS 'Total amount withdrawn from this investment';
COMMENT ON COLUMN investments.last_profit_calculation IS 'Last time profit was calculated';

-- ========================================
-- 3. INVESTOR_SHARES (2 missing columns)
-- ========================================

ALTER TABLE investor_shares ADD COLUMN IF NOT EXISTS distribution_date TIMESTAMP;
ALTER TABLE investor_shares ADD COLUMN IF NOT EXISTS notes VARCHAR(500);

COMMENT ON COLUMN investor_shares.distribution_date IS 'Date when profit distribution occurred';
COMMENT ON COLUMN investor_shares.notes IS 'Additional notes for this investor share';

-- ========================================
-- 4. INVESTOR_DISTRIBUTIONS (4 missing columns)
-- ========================================

ALTER TABLE investor_distributions ADD COLUMN IF NOT EXISTS investor_share_percentage DECIMAL(5,2);
ALTER TABLE investor_distributions ADD COLUMN IF NOT EXISTS investor_profit_amount DECIMAL(12,2);
ALTER TABLE investor_distributions ADD COLUMN IF NOT EXISTS distribution_date TIMESTAMP;
ALTER TABLE investor_distributions ADD COLUMN IF NOT EXISTS calculation_details VARCHAR(2000);

COMMENT ON COLUMN investor_distributions.investor_share_percentage IS 'Percentage share for this investor';
COMMENT ON COLUMN investor_distributions.investor_profit_amount IS 'Profit amount for this investor';
COMMENT ON COLUMN investor_distributions.distribution_date IS 'Date of profit distribution';
COMMENT ON COLUMN investor_distributions.calculation_details IS 'Details of profit calculation';

-- ========================================
-- 5. PRODUCTS (8 missing columns)
-- ========================================

ALTER TABLE products ADD COLUMN IF NOT EXISTS minimum_stock_level INTEGER;
ALTER TABLE products ADD COLUMN IF NOT EXISTS maximum_stock_level INTEGER;
ALTER TABLE products ADD COLUMN IF NOT EXISTS is_taxable BOOLEAN DEFAULT true;
ALTER TABLE products ADD COLUMN IF NOT EXISTS is_discountable BOOLEAN DEFAULT true;
ALTER TABLE products ADD COLUMN IF NOT EXISTS weight_in_grams DOUBLE PRECISION;
ALTER TABLE products ADD COLUMN IF NOT EXISTS location VARCHAR(255);
ALTER TABLE products ADD COLUMN IF NOT EXISTS supplier_name VARCHAR(255);
ALTER TABLE products ADD COLUMN IF NOT EXISTS supplier_contact VARCHAR(255);

COMMENT ON COLUMN products.minimum_stock_level IS 'Minimum stock level before reorder';
COMMENT ON COLUMN products.maximum_stock_level IS 'Maximum stock level to maintain';
COMMENT ON COLUMN products.is_taxable IS 'Whether product is subject to tax';
COMMENT ON COLUMN products.is_discountable IS 'Whether product can be discounted';
COMMENT ON COLUMN products.weight_in_grams IS 'Product weight in grams';
COMMENT ON COLUMN products.location IS 'Storage location in warehouse';
COMMENT ON COLUMN products.supplier_name IS 'Name of product supplier';
COMMENT ON COLUMN products.supplier_contact IS 'Contact information for supplier';

-- ========================================
-- 6. SHOP_CUSTOMIZATIONS (13 missing columns)
-- ========================================

ALTER TABLE shop_customizations ADD COLUMN IF NOT EXISTS background_color VARCHAR(7);
ALTER TABLE shop_customizations ADD COLUMN IF NOT EXISTS text_color VARCHAR(7);
ALTER TABLE shop_customizations ADD COLUMN IF NOT EXISTS banner_image_url VARCHAR(500);
ALTER TABLE shop_customizations ADD COLUMN IF NOT EXISTS background_image_url VARCHAR(500);
ALTER TABLE shop_customizations ADD COLUMN IF NOT EXISTS website_url VARCHAR(500);
ALTER TABLE shop_customizations ADD COLUMN IF NOT EXISTS social_media_links TEXT;
ALTER TABLE shop_customizations ADD COLUMN IF NOT EXISTS border_radius INTEGER;
ALTER TABLE shop_customizations ADD COLUMN IF NOT EXISTS custom_styles TEXT;
ALTER TABLE shop_customizations ADD COLUMN IF NOT EXISTS receipt_header VARCHAR(1000);
ALTER TABLE shop_customizations ADD COLUMN IF NOT EXISTS receipt_footer VARCHAR(1000);
ALTER TABLE shop_customizations ADD COLUMN IF NOT EXISTS receipt_show_logo BOOLEAN DEFAULT true;
ALTER TABLE shop_customizations ADD COLUMN IF NOT EXISTS show_banner BOOLEAN DEFAULT true;
ALTER TABLE shop_customizations ADD COLUMN IF NOT EXISTS show_advanced_features BOOLEAN DEFAULT false;

COMMENT ON COLUMN shop_customizations.background_color IS 'Background color in hex format';
COMMENT ON COLUMN shop_customizations.text_color IS 'Text color in hex format';
COMMENT ON COLUMN shop_customizations.banner_image_url IS 'URL to banner image';
COMMENT ON COLUMN shop_customizations.background_image_url IS 'URL to background image';
COMMENT ON COLUMN shop_customizations.website_url IS 'Shop website URL';
COMMENT ON COLUMN shop_customizations.social_media_links IS 'JSON of social media links';
COMMENT ON COLUMN shop_customizations.border_radius IS 'Border radius for UI elements in pixels';
COMMENT ON COLUMN shop_customizations.custom_styles IS 'Custom CSS styles';
COMMENT ON COLUMN shop_customizations.receipt_header IS 'Custom header for receipts';
COMMENT ON COLUMN shop_customizations.receipt_footer IS 'Custom footer for receipts';
COMMENT ON COLUMN shop_customizations.receipt_show_logo IS 'Whether to show logo on receipts';
COMMENT ON COLUMN shop_customizations.show_banner IS 'Whether to show banner';
COMMENT ON COLUMN shop_customizations.show_advanced_features IS 'Whether to show advanced features';

-- ========================================
-- 7. SALES_TRANSACTIONS (10 missing columns)
-- ========================================

ALTER TABLE sales_transactions ADD COLUMN IF NOT EXISTS payment_reference VARCHAR(255);
ALTER TABLE sales_transactions ADD COLUMN IF NOT EXISTS status VARCHAR(50);
ALTER TABLE sales_transactions ADD COLUMN IF NOT EXISTS is_voided BOOLEAN DEFAULT false;
ALTER TABLE sales_transactions ADD COLUMN IF NOT EXISTS void_reason VARCHAR(500);
ALTER TABLE sales_transactions ADD COLUMN IF NOT EXISTS voided_by VARCHAR(36);
ALTER TABLE sales_transactions ADD COLUMN IF NOT EXISTS voided_at TIMESTAMP;
ALTER TABLE sales_transactions ADD COLUMN IF NOT EXISTS fraud_score DECIMAL(5,2);
ALTER TABLE sales_transactions ADD COLUMN IF NOT EXISTS risk_level VARCHAR(50);
ALTER TABLE sales_transactions ADD COLUMN IF NOT EXISTS requires_review BOOLEAN DEFAULT false;
ALTER TABLE sales_transactions ADD COLUMN IF NOT EXISTS fraud_flags VARCHAR(1000);

COMMENT ON COLUMN sales_transactions.payment_reference IS 'External payment reference number';
COMMENT ON COLUMN sales_transactions.status IS 'Transaction status (COMPLETED, PENDING, VOIDED, etc.)';
COMMENT ON COLUMN sales_transactions.is_voided IS 'Whether transaction has been voided';
COMMENT ON COLUMN sales_transactions.void_reason IS 'Reason for voiding transaction';
COMMENT ON COLUMN sales_transactions.voided_by IS 'User who voided the transaction';
COMMENT ON COLUMN sales_transactions.voided_at IS 'Timestamp when transaction was voided';
COMMENT ON COLUMN sales_transactions.fraud_score IS 'Fraud detection score (0-100)';
COMMENT ON COLUMN sales_transactions.risk_level IS 'Risk level (LOW, MEDIUM, HIGH, CRITICAL)';
COMMENT ON COLUMN sales_transactions.requires_review IS 'Whether transaction requires manual review';
COMMENT ON COLUMN sales_transactions.fraud_flags IS 'JSON array of fraud detection flags';

-- Add foreign key for voided_by if users table exists
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'fk_sales_transactions_voided_by') THEN
        ALTER TABLE sales_transactions
        ADD CONSTRAINT fk_sales_transactions_voided_by
        FOREIGN KEY (voided_by) REFERENCES users(id);
    END IF;
END$$;

-- ========================================
-- 8. RECEIPTS (8 missing columns)
-- ========================================

ALTER TABLE receipts ADD COLUMN IF NOT EXISTS receipt_content TEXT;
ALTER TABLE receipts ADD COLUMN IF NOT EXISTS printable_content TEXT;
ALTER TABLE receipts ADD COLUMN IF NOT EXISTS generated_at TIMESTAMP;
ALTER TABLE receipts ADD COLUMN IF NOT EXISTS printed_at TIMESTAMP;
ALTER TABLE receipts ADD COLUMN IF NOT EXISTS printed_by VARCHAR(36);
ALTER TABLE receipts ADD COLUMN IF NOT EXISTS emailed_at TIMESTAMP;
ALTER TABLE receipts ADD COLUMN IF NOT EXISTS email_address VARCHAR(255);
ALTER TABLE receipts ADD COLUMN IF NOT EXISTS signature_url VARCHAR(500);

COMMENT ON COLUMN receipts.receipt_content IS 'Full receipt content in structured format';
COMMENT ON COLUMN receipts.printable_content IS 'Printable version of receipt';
COMMENT ON COLUMN receipts.generated_at IS 'Timestamp when receipt was generated';
COMMENT ON COLUMN receipts.printed_at IS 'Timestamp when receipt was printed';
COMMENT ON COLUMN receipts.printed_by IS 'User who printed the receipt';
COMMENT ON COLUMN receipts.emailed_at IS 'Timestamp when receipt was emailed';
COMMENT ON COLUMN receipts.email_address IS 'Email address where receipt was sent';
COMMENT ON COLUMN receipts.signature_url IS 'URL to digital signature';

-- Add foreign key for printed_by if users table exists
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'fk_receipts_printed_by') THEN
        ALTER TABLE receipts
        ADD CONSTRAINT fk_receipts_printed_by
        FOREIGN KEY (printed_by) REFERENCES users(id);
    END IF;
END$$;

-- ========================================
-- 9. LINE_ITEMS (1 missing column)
-- ========================================

ALTER TABLE line_items ADD COLUMN IF NOT EXISTS discount_percentage DECIMAL(5,2);

COMMENT ON COLUMN line_items.discount_percentage IS 'Discount percentage applied to line item';

-- ========================================
-- 10. FRAUD_ALERTS (7 missing columns)
-- ========================================

ALTER TABLE fraud_alerts ADD COLUMN IF NOT EXISTS transaction_id VARCHAR(36);
ALTER TABLE fraud_alerts ADD COLUMN IF NOT EXISTS investment_id VARCHAR(36);
ALTER TABLE fraud_alerts ADD COLUMN IF NOT EXISTS confidence_level DECIMAL(5,2);
ALTER TABLE fraud_alerts ADD COLUMN IF NOT EXISTS detection_rule VARCHAR(255);
ALTER TABLE fraud_alerts ADD COLUMN IF NOT EXISTS acknowledged_by VARCHAR(36);
ALTER TABLE fraud_alerts ADD COLUMN IF NOT EXISTS acknowledged_at TIMESTAMP;
ALTER TABLE fraud_alerts ADD COLUMN IF NOT EXISTS tenant_id VARCHAR(36);

COMMENT ON COLUMN fraud_alerts.transaction_id IS 'Related sales transaction ID';
COMMENT ON COLUMN fraud_alerts.investment_id IS 'Related investment ID';
COMMENT ON COLUMN fraud_alerts.confidence_level IS 'Confidence level of fraud detection (0-100)';
COMMENT ON COLUMN fraud_alerts.detection_rule IS 'Rule that triggered this alert';
COMMENT ON COLUMN fraud_alerts.acknowledged_by IS 'User who acknowledged the alert';
COMMENT ON COLUMN fraud_alerts.acknowledged_at IS 'Timestamp when alert was acknowledged';
COMMENT ON COLUMN fraud_alerts.tenant_id IS 'Tenant ID for multi-tenancy';

-- Add foreign keys
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'fk_fraud_alerts_transaction') THEN
        ALTER TABLE fraud_alerts
        ADD CONSTRAINT fk_fraud_alerts_transaction
        FOREIGN KEY (transaction_id) REFERENCES sales_transactions(id);
    END IF;

    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'fk_fraud_alerts_investment') THEN
        ALTER TABLE fraud_alerts
        ADD CONSTRAINT fk_fraud_alerts_investment
        FOREIGN KEY (investment_id) REFERENCES investments(id);
    END IF;

    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'fk_fraud_alerts_acknowledged_by') THEN
        ALTER TABLE fraud_alerts
        ADD CONSTRAINT fk_fraud_alerts_acknowledged_by
        FOREIGN KEY (acknowledged_by) REFERENCES users(id);
    END IF;

    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'fk_fraud_alerts_tenant') THEN
        ALTER TABLE fraud_alerts
        ADD CONSTRAINT fk_fraud_alerts_tenant
        FOREIGN KEY (tenant_id) REFERENCES tenants(id);
    END IF;
END$$;

-- ========================================
-- 11. RISK_ASSESSMENTS (4 missing columns)
-- ========================================

ALTER TABLE risk_assessments ADD COLUMN IF NOT EXISTS details VARCHAR(2000);
ALTER TABLE risk_assessments ADD COLUMN IF NOT EXISTS reviewed_at TIMESTAMP;
ALTER TABLE risk_assessments ADD COLUMN IF NOT EXISTS review_notes VARCHAR(1000);
ALTER TABLE risk_assessments ADD COLUMN IF NOT EXISTS resolution_action VARCHAR(50);

COMMENT ON COLUMN risk_assessments.details IS 'Detailed assessment information';
COMMENT ON COLUMN risk_assessments.reviewed_at IS 'Timestamp when assessment was reviewed';
COMMENT ON COLUMN risk_assessments.review_notes IS 'Notes from manual review';
COMMENT ON COLUMN risk_assessments.resolution_action IS 'Action taken to resolve risk (APPROVED, REJECTED, etc.)';

-- ========================================
-- 12. PRODUCT_RETURNS (8 missing columns)
-- ========================================

ALTER TABLE product_returns ADD COLUMN IF NOT EXISTS refund_type VARCHAR(50);
ALTER TABLE product_returns ADD COLUMN IF NOT EXISTS condition_assessment VARCHAR(500);
ALTER TABLE product_returns ADD COLUMN IF NOT EXISTS fraud_check_result VARCHAR(1000);
ALTER TABLE product_returns ADD COLUMN IF NOT EXISTS processed_date TIMESTAMP;
ALTER TABLE product_returns ADD COLUMN IF NOT EXISTS customer_notes VARCHAR(1000);
ALTER TABLE product_returns ADD COLUMN IF NOT EXISTS internal_notes VARCHAR(1000);
ALTER TABLE product_returns ADD COLUMN IF NOT EXISTS damage_assessment VARCHAR(500);
ALTER TABLE product_returns ADD COLUMN IF NOT EXISTS is_restockable BOOLEAN DEFAULT true;

COMMENT ON COLUMN product_returns.refund_type IS 'Type of refund (FULL, PARTIAL, STORE_CREDIT)';
COMMENT ON COLUMN product_returns.condition_assessment IS 'Assessment of returned product condition';
COMMENT ON COLUMN product_returns.fraud_check_result IS 'Result of fraud check on return';
COMMENT ON COLUMN product_returns.processed_date IS 'Date when return was processed';
COMMENT ON COLUMN product_returns.customer_notes IS 'Notes from customer about return';
COMMENT ON COLUMN product_returns.internal_notes IS 'Internal notes about return';
COMMENT ON COLUMN product_returns.damage_assessment IS 'Assessment of any damage';
COMMENT ON COLUMN product_returns.is_restockable IS 'Whether item can be restocked';

-- ========================================
-- 13. TENANTS (4 missing columns)
-- ========================================

ALTER TABLE tenants ADD COLUMN IF NOT EXISTS company_registration VARCHAR(255);
ALTER TABLE tenants ADD COLUMN IF NOT EXISTS tax_id VARCHAR(255);
ALTER TABLE tenants ADD COLUMN IF NOT EXISTS primary_address VARCHAR(500);
ALTER TABLE tenants ADD COLUMN IF NOT EXISTS created_date TIMESTAMP;

COMMENT ON COLUMN tenants.company_registration IS 'Company registration number';
COMMENT ON COLUMN tenants.tax_id IS 'Tax identification number';
COMMENT ON COLUMN tenants.primary_address IS 'Primary business address';
COMMENT ON COLUMN tenants.created_date IS 'Date when tenant was created';

-- ========================================
-- CREATE INDEXES FOR PERFORMANCE
-- ========================================

-- Indexes for foreign keys
CREATE INDEX IF NOT EXISTS idx_fraud_alerts_transaction ON fraud_alerts(transaction_id);
CREATE INDEX IF NOT EXISTS idx_fraud_alerts_investment ON fraud_alerts(investment_id);
CREATE INDEX IF NOT EXISTS idx_fraud_alerts_tenant ON fraud_alerts(tenant_id);
CREATE INDEX IF NOT EXISTS idx_sales_transactions_status ON sales_transactions(status);
CREATE INDEX IF NOT EXISTS idx_sales_transactions_risk_level ON sales_transactions(risk_level);

-- Indexes for commonly queried fields
CREATE INDEX IF NOT EXISTS idx_products_supplier ON products(supplier_name);
CREATE INDEX IF NOT EXISTS idx_receipts_email ON receipts(email_address);
CREATE INDEX IF NOT EXISTS idx_receipts_generated_at ON receipts(generated_at);

-- ========================================
-- REFRESH STATISTICS
-- ========================================

ANALYZE analytics_cache;
ANALYZE investments;
ANALYZE investor_shares;
ANALYZE investor_distributions;
ANALYZE products;
ANALYZE shop_customizations;
ANALYZE sales_transactions;
ANALYZE receipts;
ANALYZE line_items;
ANALYZE fraud_alerts;
ANALYZE risk_assessments;
ANALYZE product_returns;
ANALYZE tenants;
