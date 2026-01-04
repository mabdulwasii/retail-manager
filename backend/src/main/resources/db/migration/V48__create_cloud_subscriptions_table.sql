-- =====================================================
-- Migration: V48 - Create cloud subscriptions and billing tables
-- Description: Add subscription management and billing for cloud tenants
-- Author: Shop Manager Development Team
-- Date: 2026-01-04
-- =====================================================

-- Create cloud_subscriptions table
CREATE TABLE cloud_subscriptions (
    id VARCHAR(255) PRIMARY KEY,
    tenant_id VARCHAR(255) NOT NULL,
    tier VARCHAR(50) NOT NULL,
    billing_cycle VARCHAR(50) NOT NULL,
    status VARCHAR(50) NOT NULL,
    price DECIMAL(19, 4) NOT NULL,
    currency VARCHAR(3) NOT NULL DEFAULT 'USD',
    max_shops INTEGER,
    max_users_per_shop INTEGER,
    max_api_requests_per_month BIGINT,
    current_api_requests BIGINT NOT NULL DEFAULT 0,
    storage_limit_gb INTEGER,
    current_storage_gb DECIMAL(10, 2) NOT NULL DEFAULT 0,
    start_date TIMESTAMP NOT NULL,
    trial_end_date TIMESTAMP,
    next_billing_date TIMESTAMP,
    end_date TIMESTAMP,
    auto_renew BOOLEAN NOT NULL DEFAULT true,
    cancellation_reason VARCHAR(500),
    cancelled_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(255),
    updated_at TIMESTAMP,
    updated_by VARCHAR(255),
    version INTEGER NOT NULL DEFAULT 0,
    CONSTRAINT fk_cloud_subscription_tenant
        FOREIGN KEY (tenant_id) REFERENCES cloud_tenants(id) ON DELETE CASCADE
);

-- Create indexes for performance
CREATE INDEX idx_cloud_subscription_tenant ON cloud_subscriptions(tenant_id);
CREATE INDEX idx_cloud_subscription_status ON cloud_subscriptions(status);
CREATE INDEX idx_cloud_subscription_next_billing ON cloud_subscriptions(next_billing_date);

-- Add comments for documentation
COMMENT ON TABLE cloud_subscriptions IS 'Subscription management for cloud tenants with pricing tiers and billing cycles';
COMMENT ON COLUMN cloud_subscriptions.tier IS 'Subscription tier: FREE, BASIC, PREMIUM, ENTERPRISE';
COMMENT ON COLUMN cloud_subscriptions.billing_cycle IS 'Billing cycle: MONTHLY, QUARTERLY, YEARLY';
COMMENT ON COLUMN cloud_subscriptions.status IS 'Subscription status: TRIAL, ACTIVE, PAST_DUE, CANCELLED, SUSPENDED, EXPIRED';
COMMENT ON COLUMN cloud_subscriptions.price IS 'Price per billing cycle in specified currency';
COMMENT ON COLUMN cloud_subscriptions.max_shops IS 'Maximum number of shops allowed (-1 = unlimited)';
COMMENT ON COLUMN cloud_subscriptions.max_users_per_shop IS 'Maximum users per shop (-1 = unlimited)';
COMMENT ON COLUMN cloud_subscriptions.max_api_requests_per_month IS 'Maximum API requests per month (-1 = unlimited)';
COMMENT ON COLUMN cloud_subscriptions.current_api_requests IS 'Current API request count for this billing cycle';
COMMENT ON COLUMN cloud_subscriptions.storage_limit_gb IS 'Storage limit in GB (-1 = unlimited)';
COMMENT ON COLUMN cloud_subscriptions.current_storage_gb IS 'Current storage usage in GB';
COMMENT ON COLUMN cloud_subscriptions.trial_end_date IS 'Trial period end date (NULL if not on trial)';
COMMENT ON COLUMN cloud_subscriptions.next_billing_date IS 'Next billing date for invoice generation';
COMMENT ON COLUMN cloud_subscriptions.auto_renew IS 'Auto-renewal flag for subscription';

-- Create billing_invoices table
CREATE TABLE billing_invoices (
    id VARCHAR(255) PRIMARY KEY,
    tenant_id VARCHAR(255) NOT NULL,
    subscription_id VARCHAR(255) NOT NULL,
    invoice_number VARCHAR(50) NOT NULL UNIQUE,
    period_start TIMESTAMP NOT NULL,
    period_end TIMESTAMP NOT NULL,
    issue_date TIMESTAMP NOT NULL,
    due_date TIMESTAMP NOT NULL,
    subtotal DECIMAL(19, 4) NOT NULL,
    tax_amount DECIMAL(19, 4) NOT NULL DEFAULT 0,
    tax_rate DECIMAL(5, 2) NOT NULL DEFAULT 0,
    discount_amount DECIMAL(19, 4) NOT NULL DEFAULT 0,
    total DECIMAL(19, 4) NOT NULL,
    amount_paid DECIMAL(19, 4) NOT NULL DEFAULT 0,
    currency VARCHAR(3) NOT NULL DEFAULT 'USD',
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    payment_method VARCHAR(100),
    payment_transaction_id VARCHAR(255),
    payment_date TIMESTAMP,
    line_items TEXT,
    notes VARCHAR(1000),
    pdf_url VARCHAR(500),
    retry_count INTEGER NOT NULL DEFAULT 0,
    next_retry_date TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(255),
    updated_at TIMESTAMP,
    updated_by VARCHAR(255),
    version INTEGER NOT NULL DEFAULT 0,
    CONSTRAINT fk_billing_invoice_tenant
        FOREIGN KEY (tenant_id) REFERENCES cloud_tenants(id) ON DELETE CASCADE,
    CONSTRAINT fk_billing_invoice_subscription
        FOREIGN KEY (subscription_id) REFERENCES cloud_subscriptions(id) ON DELETE CASCADE
);

-- Create indexes for performance
CREATE INDEX idx_billing_invoice_tenant ON billing_invoices(tenant_id);
CREATE INDEX idx_billing_invoice_subscription ON billing_invoices(subscription_id);
CREATE INDEX idx_billing_invoice_status ON billing_invoices(status);
CREATE INDEX idx_billing_invoice_due_date ON billing_invoices(due_date);
CREATE INDEX idx_billing_invoice_number ON billing_invoices(invoice_number);

-- Add comments for documentation
COMMENT ON TABLE billing_invoices IS 'Invoices and billing history for cloud subscriptions';
COMMENT ON COLUMN billing_invoices.invoice_number IS 'Unique invoice number (e.g., INV-202601-001234)';
COMMENT ON COLUMN billing_invoices.period_start IS 'Billing period start date';
COMMENT ON COLUMN billing_invoices.period_end IS 'Billing period end date';
COMMENT ON COLUMN billing_invoices.issue_date IS 'Invoice issue date';
COMMENT ON COLUMN billing_invoices.due_date IS 'Payment due date';
COMMENT ON COLUMN billing_invoices.subtotal IS 'Subtotal amount before tax and discount';
COMMENT ON COLUMN billing_invoices.tax_amount IS 'Tax amount';
COMMENT ON COLUMN billing_invoices.tax_rate IS 'Tax rate percentage';
COMMENT ON COLUMN billing_invoices.discount_amount IS 'Discount amount (if any)';
COMMENT ON COLUMN billing_invoices.total IS 'Total amount (subtotal + tax - discount)';
COMMENT ON COLUMN billing_invoices.amount_paid IS 'Amount paid by customer';
COMMENT ON COLUMN billing_invoices.status IS 'Invoice status: DRAFT, PENDING, PAID, OVERDUE, FAILED, CANCELLED, REFUNDED';
COMMENT ON COLUMN billing_invoices.payment_method IS 'Payment method used (e.g., Credit Card, PayPal)';
COMMENT ON COLUMN billing_invoices.payment_transaction_id IS 'Transaction ID from payment gateway';
COMMENT ON COLUMN billing_invoices.payment_date IS 'Date when payment was received';
COMMENT ON COLUMN billing_invoices.line_items IS 'Detailed line items description';
COMMENT ON COLUMN billing_invoices.pdf_url IS 'URL to download invoice PDF';
COMMENT ON COLUMN billing_invoices.retry_count IS 'Number of payment retry attempts';
COMMENT ON COLUMN billing_invoices.next_retry_date IS 'Next scheduled payment retry date';
