-- Enhanced features migration for Shop Manager
-- Adds tables for investment distributions, risk assessments, analytics cache, audit logs, and feature flags

-- Investor Distributions table
CREATE TABLE investor_distributions (
    id VARCHAR(36) PRIMARY KEY,
    investment_id VARCHAR(36) NOT NULL,
    period_start TIMESTAMP NOT NULL,
    period_end TIMESTAMP NOT NULL,
    total_sales_revenue DECIMAL(12,2) NOT NULL,
    total_profit DECIMAL(12,2) NOT NULL,
    investor_share_percentage DECIMAL(5,2) NOT NULL,
    investor_profit_amount DECIMAL(12,2) NOT NULL,
    distribution_amount DECIMAL(12,2) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'CALCULATED',
    distribution_date TIMESTAMP,
    payment_reference VARCHAR(255),
    notes VARCHAR(500),
    calculation_details VARCHAR(2000),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version BIGINT NOT NULL DEFAULT 0,
    FOREIGN KEY (investment_id) REFERENCES investments(id) ON DELETE CASCADE
);

CREATE INDEX idx_distribution_investment ON investor_distributions(investment_id);
CREATE INDEX idx_distribution_date ON investor_distributions(distribution_date);
CREATE INDEX idx_distribution_period ON investor_distributions(period_start, period_end);

-- Risk Assessments table
CREATE TABLE risk_assessments (
    id VARCHAR(36) PRIMARY KEY,
    shop_id VARCHAR(36) NOT NULL,
    transaction_id VARCHAR(36),
    assessment_type VARCHAR(50) NOT NULL,
    risk_level VARCHAR(20) NOT NULL,
    risk_score DECIMAL(5,2) NOT NULL,
    assessment_date TIMESTAMP NOT NULL,
    details VARCHAR(2000),
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    reviewed_by VARCHAR(255),
    reviewed_at TIMESTAMP,
    review_notes VARCHAR(1000),
    resolution_action VARCHAR(50),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version BIGINT NOT NULL DEFAULT 0,
    FOREIGN KEY (shop_id) REFERENCES shops(id) ON DELETE CASCADE,
    FOREIGN KEY (transaction_id) REFERENCES sales_transactions(id) ON DELETE SET NULL
);

CREATE INDEX idx_risk_shop ON risk_assessments(shop_id);
CREATE INDEX idx_risk_transaction ON risk_assessments(transaction_id);
CREATE INDEX idx_risk_date ON risk_assessments(assessment_date);
CREATE INDEX idx_risk_level ON risk_assessments(risk_level);

-- Risk Assessment Flags junction table
CREATE TABLE risk_assessment_flags (
    risk_assessment_id VARCHAR(36) NOT NULL,
    flag VARCHAR(255) NOT NULL,
    FOREIGN KEY (risk_assessment_id) REFERENCES risk_assessments(id) ON DELETE CASCADE
);

CREATE INDEX idx_risk_flags_assessment ON risk_assessment_flags(risk_assessment_id);

-- Analytics Cache table
CREATE TABLE analytics_cache (
    id VARCHAR(36) PRIMARY KEY,
    shop_id VARCHAR(36) NOT NULL,
    analytics_type VARCHAR(50) NOT NULL,
    cache_key VARCHAR(255) NOT NULL,
    cache_data TEXT NOT NULL,
    cache_date TIMESTAMP NOT NULL,
    expires_at TIMESTAMP NOT NULL,
    cache_version VARCHAR(50),
    metadata TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version BIGINT NOT NULL DEFAULT 0,
    FOREIGN KEY (shop_id) REFERENCES shops(id) ON DELETE CASCADE
);

CREATE INDEX idx_analytics_shop ON analytics_cache(shop_id);
CREATE INDEX idx_analytics_key ON analytics_cache(cache_key);
CREATE INDEX idx_analytics_type ON analytics_cache(analytics_type);
CREATE INDEX idx_analytics_date ON analytics_cache(cache_date);
CREATE INDEX idx_analytics_expiry ON analytics_cache(expires_at);
CREATE UNIQUE INDEX uk_analytics_cache ON analytics_cache(shop_id, cache_key, analytics_type);

-- Enhance audit_logs table with additional columns from V1 to V3
ALTER TABLE audit_logs ADD COLUMN shop_id VARCHAR(36);
ALTER TABLE audit_logs ADD COLUMN category VARCHAR(50) NOT NULL DEFAULT 'GENERAL';
ALTER TABLE audit_logs RENAME COLUMN action TO action_type;
ALTER TABLE audit_logs ADD COLUMN action_description VARCHAR(500);
ALTER TABLE audit_logs ADD COLUMN action_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP;
ALTER TABLE audit_logs ADD COLUMN session_id VARCHAR(255);
ALTER TABLE audit_logs RENAME COLUMN changes TO old_values;
ALTER TABLE audit_logs ADD COLUMN new_values TEXT;
ALTER TABLE audit_logs ADD COLUMN severity VARCHAR(20) DEFAULT 'INFO';
ALTER TABLE audit_logs ADD COLUMN success BOOLEAN NOT NULL DEFAULT TRUE;
ALTER TABLE audit_logs ADD COLUMN error_message VARCHAR(1000);
ALTER TABLE audit_logs ADD COLUMN updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP;
ALTER TABLE audit_logs ADD COLUMN version BIGINT NOT NULL DEFAULT 0;
ALTER TABLE audit_logs ADD CONSTRAINT fk_audit_logs_shop
    FOREIGN KEY (shop_id) REFERENCES shops(id) ON DELETE SET NULL;

CREATE INDEX idx_audit_shop ON audit_logs(shop_id);
CREATE INDEX idx_audit_user ON audit_logs(user_id);
CREATE INDEX idx_audit_entity ON audit_logs(entity_type, entity_id);
CREATE INDEX idx_audit_action ON audit_logs(action_type);
CREATE INDEX idx_audit_date ON audit_logs(action_date);
CREATE INDEX idx_audit_category ON audit_logs(category);

-- Audit Log Details junction table
CREATE TABLE audit_log_details (
    audit_log_id VARCHAR(36) NOT NULL,
    detail_key VARCHAR(255) NOT NULL,
    detail_value VARCHAR(1000),
    FOREIGN KEY (audit_log_id) REFERENCES audit_logs(id) ON DELETE CASCADE
);

CREATE INDEX idx_audit_details_log ON audit_log_details(audit_log_id);

-- Feature Flags table
CREATE TABLE feature_flags (
    id VARCHAR(36) PRIMARY KEY,
    shop_id VARCHAR(36),
    feature_name VARCHAR(255) NOT NULL,
    enabled BOOLEAN NOT NULL DEFAULT FALSE,
    description VARCHAR(500),
    effective_from TIMESTAMP,
    effective_until TIMESTAMP,
    created_by VARCHAR(255),
    last_modified_by VARCHAR(255),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version BIGINT NOT NULL DEFAULT 0,
    FOREIGN KEY (shop_id) REFERENCES shops(id) ON DELETE CASCADE
);

CREATE INDEX idx_feature_shop ON feature_flags(shop_id);
CREATE INDEX idx_feature_name ON feature_flags(feature_name);
CREATE INDEX idx_feature_enabled ON feature_flags(enabled);
CREATE UNIQUE INDEX uk_feature_shop ON feature_flags(shop_id, feature_name);

-- Feature Flag Configuration junction table
CREATE TABLE feature_flag_config (
    feature_flag_id VARCHAR(36) NOT NULL,
    config_key VARCHAR(255) NOT NULL,
    config_value VARCHAR(1000),
    FOREIGN KEY (feature_flag_id) REFERENCES feature_flags(id) ON DELETE CASCADE
);

CREATE INDEX idx_feature_config_flag ON feature_flag_config(feature_flag_id);

-- Add new columns to sales_transactions for fraud detection
ALTER TABLE sales_transactions
ADD COLUMN fraud_score DECIMAL(5,2) DEFAULT 0.00,
ADD COLUMN fraud_flags VARCHAR(500),
ADD COLUMN risk_level VARCHAR(20) DEFAULT 'LOW',
ADD COLUMN requires_review BOOLEAN DEFAULT FALSE;

-- Add indexes for fraud detection columns
CREATE INDEX idx_transaction_fraud_score ON sales_transactions(fraud_score);
CREATE INDEX idx_transaction_risk_level ON sales_transactions(risk_level);
CREATE INDEX idx_transaction_requires_review ON sales_transactions(requires_review);

-- Insert default global feature flags
INSERT INTO feature_flags (id, shop_id, feature_name, enabled, description, created_by) VALUES
('f1000000-1000-1000-1000-100000000001', NULL, 'investment.enabled', TRUE, 'Enable investment module globally', 'SYSTEM'),
('f1000000-1000-1000-1000-100000000002', NULL, 'analytics.enabled', TRUE, 'Enable analytics module globally', 'SYSTEM'),
('f1000000-1000-1000-1000-100000000003', NULL, 'fraud.enabled', FALSE, 'Enable fraud detection module globally', 'SYSTEM'),
('f1000000-1000-1000-1000-100000000004', NULL, 'reporting.advanced', FALSE, 'Enable advanced reporting features', 'SYSTEM'),
('f1000000-1000-1000-1000-100000000005', NULL, 'inventory.tracking', TRUE, 'Enable inventory tracking', 'SYSTEM');

-- Create view for active feature flags
CREATE VIEW active_feature_flags AS
SELECT
    ff.*,
    s.name as shop_name
FROM feature_flags ff
LEFT JOIN shops s ON ff.shop_id = s.id
WHERE ff.enabled = TRUE
  AND (ff.effective_from IS NULL OR ff.effective_from <= CURRENT_TIMESTAMP)
  AND (ff.effective_until IS NULL OR ff.effective_until > CURRENT_TIMESTAMP);

-- Create view for investment performance summary
CREATE VIEW investment_performance_summary AS
SELECT
    i.id,
    i.investment_number,
    i.investment_type,
    i.amount as investment_amount,
    i.total_profit_earned,
    i.total_withdrawn,
    (i.amount + i.total_profit_earned - i.total_withdrawn) as current_balance,
    i.status,
    u.username as investor_name,
    s.name as shop_name,
    COALESCE(dist_summary.total_distributions, 0) as total_distributions,
    COALESCE(dist_summary.distribution_count, 0) as distribution_count
FROM investments i
JOIN users u ON i.investor_id = u.id
JOIN shops s ON i.shop_id = s.id
LEFT JOIN (
    SELECT
        investment_id,
        COUNT(*) as distribution_count,
        SUM(distribution_amount) as total_distributions
    FROM investor_distributions
    WHERE status = 'PAID'
    GROUP BY investment_id
) dist_summary ON i.id = dist_summary.investment_id;

-- Create stored procedure for calculating profit distributions
CREATE OR REPLACE FUNCTION calculate_profit_distribution(
    p_investment_id VARCHAR(36),
    p_period_start TIMESTAMP,
    p_period_end TIMESTAMP
) RETURNS TABLE (
    investment_id VARCHAR(36),
    total_revenue DECIMAL(12,2),
    total_profit DECIMAL(12,2),
    investor_share DECIMAL(5,2),
    distribution_amount DECIMAL(12,2)
) AS $$
BEGIN
    RETURN QUERY
    WITH sales_data AS (
        SELECT
            st.shop_id,
            SUM(st.total_amount) as period_revenue,
            SUM(st.total_amount * 0.3) as period_profit -- Assuming 30% profit margin
        FROM sales_transactions st
        WHERE st.transaction_date >= p_period_start
          AND st.transaction_date < p_period_end
          AND st.status = 'COMPLETED'
          AND st.is_voided = FALSE
        GROUP BY st.shop_id
    )
    SELECT
        i.id,
        COALESCE(sd.period_revenue, 0),
        COALESCE(sd.period_profit, 0),
        i.profit_percentage,
        ROUND(COALESCE(sd.period_profit, 0) * i.profit_percentage / 100, 2)
    FROM investments i
    LEFT JOIN sales_data sd ON i.shop_id = sd.shop_id
    WHERE i.id = p_investment_id
      AND i.status = 'ACTIVE';
END;
$$ LANGUAGE plpgsql;