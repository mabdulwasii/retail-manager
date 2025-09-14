-- Additional enhancements for fraud rules and receipt updates
-- Add fraud rules table and enhance receipts table

-- Fraud Rules table
CREATE TABLE fraud_rules (
    id VARCHAR(36) PRIMARY KEY,
    shop_id VARCHAR(36),
    rule_name VARCHAR(255) NOT NULL,
    rule_type VARCHAR(50) NOT NULL,
    description VARCHAR(500),
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    threshold_amount DECIMAL(12,2),
    threshold_count INTEGER,
    time_window_minutes INTEGER,
    risk_score_weight DECIMAL(3,2) DEFAULT 1.0,
    severity VARCHAR(20) NOT NULL DEFAULT 'MEDIUM',
    auto_block BOOLEAN NOT NULL DEFAULT FALSE,
    requires_manual_review BOOLEAN NOT NULL DEFAULT TRUE,
    rule_configuration VARCHAR(2000),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version BIGINT NOT NULL DEFAULT 0,
    FOREIGN KEY (shop_id) REFERENCES shops(id) ON DELETE CASCADE
);

CREATE INDEX idx_fraud_rule_shop ON fraud_rules(shop_id);
CREATE INDEX idx_fraud_rule_type ON fraud_rules(rule_type);
CREATE INDEX idx_fraud_rule_enabled ON fraud_rules(enabled);

-- Add columns to receipts table for enhanced functionality
ALTER TABLE receipts
ADD COLUMN printable_content TEXT,
ADD COLUMN format VARCHAR(20) NOT NULL DEFAULT 'TEXT',
ADD COLUMN status VARCHAR(20) NOT NULL DEFAULT 'GENERATED',
ADD COLUMN generated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
ADD COLUMN printed_at TIMESTAMP,
ADD COLUMN printed_by VARCHAR(255),
ADD COLUMN emailed_at TIMESTAMP,
ADD COLUMN email_address VARCHAR(255);

-- Update existing receipts to have generated_at set to their issued_date
UPDATE receipts SET generated_at = issued_date WHERE generated_at IS NULL;

-- Insert default fraud rules for all shops
INSERT INTO fraud_rules (id, shop_id, rule_name, rule_type, description, enabled, threshold_amount, risk_score_weight, severity, auto_block, requires_manual_review)
SELECT
    'fr-' || s.id || '-high-amount',
    s.id,
    'High Amount Transaction',
    'HIGH_AMOUNT_TRANSACTION',
    'Flags transactions above a certain amount threshold',
    TRUE,
    50000.00,
    5.0,
    'HIGH',
    FALSE,
    TRUE
FROM shops s;

INSERT INTO fraud_rules (id, shop_id, rule_name, rule_type, description, enabled, time_window_minutes, threshold_count, risk_score_weight, severity, auto_block, requires_manual_review)
SELECT
    'fr-' || s.id || '-high-freq',
    s.id,
    'High Frequency Transactions',
    'HIGH_FREQUENCY_TRANSACTIONS',
    'Flags unusually high number of transactions in a short time',
    TRUE,
    60,
    20,
    4.0,
    'MEDIUM',
    FALSE,
    TRUE
FROM shops s;

INSERT INTO fraud_rules (id, shop_id, rule_name, rule_type, description, enabled, risk_score_weight, severity, auto_block, requires_manual_review)
SELECT
    'fr-' || s.id || '-unusual-time',
    s.id,
    'Unusual Time Transaction',
    'UNUSUAL_TIME_TRANSACTION',
    'Flags transactions occurring outside normal business hours',
    TRUE,
    3.0,
    'MEDIUM',
    FALSE,
    FALSE
FROM shops s;

-- Add global fraud rules (shop_id = NULL)
INSERT INTO fraud_rules (id, shop_id, rule_name, rule_type, description, enabled, threshold_amount, risk_score_weight, severity, auto_block, requires_manual_review) VALUES
('fr-global-critical-amount', NULL, 'Critical Amount Transaction', 'HIGH_AMOUNT_TRANSACTION', 'Global rule for extremely high amount transactions', TRUE, 100000.00, 8.0, 'CRITICAL', TRUE, TRUE),
('fr-global-velocity', NULL, 'Transaction Velocity Check', 'VELOCITY_CHECK', 'Global velocity checking for rapid successive large transactions', TRUE, 25000.00, 6.0, 'HIGH', FALSE, TRUE);

-- Create indexes on new receipt columns
CREATE INDEX idx_receipt_status ON receipts(status);
CREATE INDEX idx_receipt_generated_at ON receipts(generated_at);
CREATE INDEX idx_receipt_format ON receipts(format);

-- Create view for receipt summary
CREATE VIEW receipt_summary AS
SELECT
    r.id,
    r.receipt_number,
    r.status,
    r.format,
    r.generated_at,
    r.printed_at,
    r.emailed_at,
    st.transaction_number,
    st.total_amount,
    s.name as shop_name,
    u.username as cashier_name
FROM receipts r
JOIN sales_transactions st ON r.transaction_id = st.id
JOIN shops s ON st.shop_id = s.id
JOIN users u ON st.cashier_id = u.id;

-- Create view for fraud rule summary
CREATE VIEW fraud_rule_summary AS
SELECT
    fr.id,
    fr.rule_name,
    fr.rule_type,
    fr.enabled,
    fr.severity,
    fr.auto_block,
    COALESCE(s.name, 'GLOBAL') as scope,
    fr.threshold_amount,
    fr.threshold_count,
    fr.time_window_minutes
FROM fraud_rules fr
LEFT JOIN shops s ON fr.shop_id = s.id
ORDER BY fr.shop_id NULLS FIRST, fr.rule_name;

-- Update sales_transactions table to ensure fraud columns exist
-- (These should already exist from V3 migration, but ensuring they're present)
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns
                   WHERE table_name = 'sales_transactions' AND column_name = 'fraud_score') THEN
        ALTER TABLE sales_transactions ADD COLUMN fraud_score DECIMAL(5,2) DEFAULT 0.00;
        CREATE INDEX idx_transaction_fraud_score ON sales_transactions(fraud_score);
    END IF;

    IF NOT EXISTS (SELECT 1 FROM information_schema.columns
                   WHERE table_name = 'sales_transactions' AND column_name = 'fraud_flags') THEN
        ALTER TABLE sales_transactions ADD COLUMN fraud_flags VARCHAR(500);
    END IF;

    IF NOT EXISTS (SELECT 1 FROM information_schema.columns
                   WHERE table_name = 'sales_transactions' AND column_name = 'risk_level') THEN
        ALTER TABLE sales_transactions ADD COLUMN risk_level VARCHAR(20) DEFAULT 'LOW';
        CREATE INDEX idx_transaction_risk_level ON sales_transactions(risk_level);
    END IF;

    IF NOT EXISTS (SELECT 1 FROM information_schema.columns
                   WHERE table_name = 'sales_transactions' AND column_name = 'requires_review') THEN
        ALTER TABLE sales_transactions ADD COLUMN requires_review BOOLEAN DEFAULT FALSE;
        CREATE INDEX idx_transaction_requires_review ON sales_transactions(requires_review);
    END IF;
END $$;