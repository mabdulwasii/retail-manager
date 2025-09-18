-- Complete Shop Manager Database Schema
-- Generated from all JPA entities in the backend
-- Supports all domain models with proper types and relationships

-- Enable UUID extension
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- ========================================
-- CORE DOMAIN TABLES
-- ========================================

-- Tenants table (organization/company)
CREATE TABLE tenants (
    id VARCHAR(36) PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description VARCHAR(500),
    contact_email VARCHAR(255),
    contact_phone VARCHAR(50),
    address VARCHAR(500),
    city VARCHAR(100),
    state VARCHAR(100),
    country VARCHAR(100),
    postal_code VARCHAR(20),
    contact_user VARCHAR(36),
    status VARCHAR(20) DEFAULT 'ACTIVE',
    -- BaseEntity fields
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    version BIGINT DEFAULT 0
);

-- Users table
CREATE TABLE users (
    id VARCHAR(36) PRIMARY KEY,
    tenant_id VARCHAR(36),
    keycloak_id VARCHAR(255) UNIQUE,
    username VARCHAR(100) NOT NULL UNIQUE,
    email VARCHAR(255) NOT NULL UNIQUE,
    first_name VARCHAR(100),
    last_name VARCHAR(100),
    phone_number VARCHAR(50),
    status VARCHAR(20) DEFAULT 'ACTIVE',
    is_investor BOOLEAN DEFAULT FALSE,
    investor_since DATE,
    -- BaseEntity fields
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    version BIGINT DEFAULT 0,
    FOREIGN KEY (tenant_id) REFERENCES tenants(id)
);

-- Update tenants with contact_user foreign key
ALTER TABLE tenants ADD CONSTRAINT fk_tenant_contact_user
    FOREIGN KEY (contact_user) REFERENCES users(id);

-- Shops table
CREATE TABLE shops (
    id VARCHAR(36) PRIMARY KEY,
    tenant_id VARCHAR(36) NOT NULL,
    name VARCHAR(255) NOT NULL,
    description VARCHAR(500),
    email VARCHAR(255) NOT NULL,
    phone_number VARCHAR(50),
    address VARCHAR(500),
    city VARCHAR(100),
    state VARCHAR(100),
    country VARCHAR(100),
    postal_code VARCHAR(20),
    tax_id VARCHAR(100),
    registration_number VARCHAR(100),
    status VARCHAR(20) DEFAULT 'ACTIVE',
    opening_date DATE,
    closing_time TIME,
    opening_time TIME,
    -- Embedded ShopConfiguration fields
    currency VARCHAR(10) DEFAULT 'NGN',
    timezone VARCHAR(50) DEFAULT 'Africa/Lagos',
    language VARCHAR(10) DEFAULT 'en',
    date_format VARCHAR(20) DEFAULT 'dd/MM/yyyy',
    allow_negative_stock BOOLEAN DEFAULT FALSE,
    auto_generate_sku BOOLEAN DEFAULT TRUE,
    enable_loyalty_program BOOLEAN DEFAULT FALSE,
    loyalty_points_rate DECIMAL(5,2) DEFAULT 0.01,
    enable_tax BOOLEAN DEFAULT TRUE,
    default_tax_rate DECIMAL(5,2) DEFAULT 7.5,
    enable_service_charge BOOLEAN DEFAULT FALSE,
    service_charge_rate DECIMAL(5,2) DEFAULT 0,
    receipt_footer_text VARCHAR(500),
    invoice_prefix VARCHAR(20) DEFAULT 'INV',
    receipt_prefix VARCHAR(20) DEFAULT 'RCP',
    enable_barcode_scanning BOOLEAN DEFAULT TRUE,
    enable_quick_sale BOOLEAN DEFAULT TRUE,
    enable_table_ordering BOOLEAN DEFAULT FALSE,
    business_type VARCHAR(50) DEFAULT 'RETAIL',
    enable_online_ordering BOOLEAN DEFAULT FALSE,
    online_store_url VARCHAR(255),
    delivery_enabled BOOLEAN DEFAULT FALSE,
    minimum_delivery_amount DECIMAL(10,2) DEFAULT 0,
    delivery_fee DECIMAL(10,2) DEFAULT 0,
    free_delivery_threshold DECIMAL(10,2),
    -- BaseEntity fields
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    version BIGINT DEFAULT 0,
    FOREIGN KEY (tenant_id) REFERENCES tenants(id)
);

-- Shop customization table
CREATE TABLE shop_customizations (
    id VARCHAR(36) PRIMARY KEY,
    shop_id VARCHAR(36) NOT NULL UNIQUE,
    primary_color VARCHAR(20) DEFAULT '#007bff',
    secondary_color VARCHAR(20) DEFAULT '#6c757d',
    accent_color VARCHAR(20) DEFAULT '#28a745',
    logo_url VARCHAR(500),
    favicon_url VARCHAR(500),
    theme_variant VARCHAR(20) DEFAULT 'LIGHT',
    font_size VARCHAR(10) DEFAULT 'MEDIUM',
    font_family VARCHAR(50) DEFAULT 'Inter',
    show_logo_in_receipt BOOLEAN DEFAULT TRUE,
    show_shop_name_in_receipt BOOLEAN DEFAULT TRUE,
    custom_css TEXT,
    dashboard_layout VARCHAR(20) DEFAULT 'GRID',
    menu_position VARCHAR(20) DEFAULT 'LEFT',
    enable_animations BOOLEAN DEFAULT TRUE,
    enable_sound_notifications BOOLEAN DEFAULT TRUE,
    compact_mode BOOLEAN DEFAULT FALSE,
    -- BaseEntity fields
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    version BIGINT DEFAULT 0,
    FOREIGN KEY (shop_id) REFERENCES shops(id)
);

-- Roles table
CREATE TABLE roles (
    id VARCHAR(36) PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    description VARCHAR(500),
    is_system BOOLEAN DEFAULT FALSE,
    tenant_id VARCHAR(36),
    -- BaseEntity fields
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    version BIGINT DEFAULT 0,
    FOREIGN KEY (tenant_id) REFERENCES tenants(id)
);

-- Permissions table
CREATE TABLE permissions (
    id VARCHAR(36) PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    description VARCHAR(500),
    resource VARCHAR(100) NOT NULL,
    action VARCHAR(100) NOT NULL,
    -- BaseEntity fields
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    version BIGINT DEFAULT 0
);

-- User-Role join table
CREATE TABLE user_roles (
    user_id VARCHAR(36) NOT NULL,
    role_id VARCHAR(36) NOT NULL,
    PRIMARY KEY (user_id, role_id),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE CASCADE
);

-- Role-Permission join table
CREATE TABLE role_permissions (
    role_id VARCHAR(36) NOT NULL,
    permission_id VARCHAR(36) NOT NULL,
    PRIMARY KEY (role_id, permission_id),
    FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE CASCADE,
    FOREIGN KEY (permission_id) REFERENCES permissions(id) ON DELETE CASCADE
);

-- Categories table
CREATE TABLE categories (
    id VARCHAR(36) PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    description VARCHAR(500),
    parent_id VARCHAR(36),
    display_order INTEGER DEFAULT 0,
    is_active BOOLEAN DEFAULT TRUE,
    image_url VARCHAR(255),
    -- BaseEntity fields
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    version BIGINT DEFAULT 0,
    FOREIGN KEY (parent_id) REFERENCES categories(id) ON DELETE SET NULL,
    UNIQUE(name)
);

-- Products table
CREATE TABLE products (
    id VARCHAR(36) PRIMARY KEY,
    shop_id VARCHAR(36) NOT NULL,
    category_id VARCHAR(36),
    name VARCHAR(255) NOT NULL,
    description VARCHAR(1000),
    sku VARCHAR(100),
    barcode VARCHAR(100) UNIQUE,
    brand VARCHAR(100),
    unit VARCHAR(50),
    price DECIMAL(10,2) NOT NULL,
    cost_price DECIMAL(10,2),
    quantity_in_stock DECIMAL(10,2) DEFAULT 0,
    min_stock_level DECIMAL(10,2) DEFAULT 0,
    max_stock_level DECIMAL(10,2),
    reorder_point DECIMAL(10,2),
    reorder_quantity DECIMAL(10,2),
    is_active BOOLEAN DEFAULT TRUE,
    is_service BOOLEAN DEFAULT FALSE,
    track_inventory BOOLEAN DEFAULT TRUE,
    allow_negative_stock BOOLEAN DEFAULT FALSE,
    tax_rate DECIMAL(5,2),
    discount_rate DECIMAL(5,2),
    image_url VARCHAR(500),
    weight DECIMAL(10,3),
    dimensions VARCHAR(100),
    status VARCHAR(20) DEFAULT 'ACTIVE',
    metadata JSONB,
    -- BaseEntity fields
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    version BIGINT DEFAULT 0,
    FOREIGN KEY (shop_id) REFERENCES shops(id),
    FOREIGN KEY (category_id) REFERENCES categories(id)
);

-- ========================================
-- SALES DOMAIN TABLES
-- ========================================

-- Sales transactions table
CREATE TABLE sales_transactions (
    id VARCHAR(36) PRIMARY KEY,
    shop_id VARCHAR(36) NOT NULL,
    transaction_number VARCHAR(50) NOT NULL UNIQUE,
    transaction_date TIMESTAMP NOT NULL,
    customer_name VARCHAR(255),
    customer_phone VARCHAR(50),
    customer_email VARCHAR(255),
    cashier_id VARCHAR(36),
    subtotal DECIMAL(12,2) NOT NULL,
    discount_amount DECIMAL(12,2) DEFAULT 0,
    tax_amount DECIMAL(12,2) DEFAULT 0,
    service_charge DECIMAL(12,2) DEFAULT 0,
    total_amount DECIMAL(12,2) NOT NULL,
    payment_method VARCHAR(50),
    payment_status VARCHAR(20) DEFAULT 'PAID',
    transaction_status VARCHAR(20) DEFAULT 'COMPLETED',
    notes VARCHAR(500),
    -- BaseEntity fields
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    version BIGINT DEFAULT 0,
    FOREIGN KEY (shop_id) REFERENCES shops(id),
    FOREIGN KEY (cashier_id) REFERENCES users(id)
);

-- Line items table
CREATE TABLE line_items (
    id VARCHAR(36) PRIMARY KEY,
    transaction_id VARCHAR(36) NOT NULL,
    product_id VARCHAR(36) NOT NULL,
    product_name VARCHAR(255) NOT NULL,
    quantity DECIMAL(10,2) NOT NULL,
    unit_price DECIMAL(10,2) NOT NULL,
    discount_amount DECIMAL(10,2) DEFAULT 0,
    tax_amount DECIMAL(10,2) DEFAULT 0,
    line_total DECIMAL(12,2) NOT NULL,
    cost_price DECIMAL(10,2),
    profit_amount DECIMAL(10,2),
    notes VARCHAR(500),
    -- BaseEntity fields
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    version BIGINT DEFAULT 0,
    FOREIGN KEY (transaction_id) REFERENCES sales_transactions(id) ON DELETE CASCADE,
    FOREIGN KEY (product_id) REFERENCES products(id)
);

-- Receipts table
CREATE TABLE receipts (
    id VARCHAR(36) PRIMARY KEY,
    transaction_id VARCHAR(36) NOT NULL UNIQUE,
    receipt_number VARCHAR(50) NOT NULL UNIQUE,
    issued_date TIMESTAMP NOT NULL,
    receipt_type VARCHAR(20) DEFAULT 'SALE',
    printed_count INTEGER DEFAULT 0,
    last_printed_at TIMESTAMP,
    email_sent BOOLEAN DEFAULT FALSE,
    email_sent_at TIMESTAMP,
    email_sent_to VARCHAR(255),
    sms_sent BOOLEAN DEFAULT FALSE,
    sms_sent_at TIMESTAMP,
    sms_sent_to VARCHAR(50),
    format VARCHAR(20) DEFAULT 'PDF',
    storage_url VARCHAR(500),
    qr_code VARCHAR(500),
    digital_signature VARCHAR(500),
    status VARCHAR(20) DEFAULT 'ACTIVE',
    -- BaseEntity fields
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    version BIGINT DEFAULT 0,
    FOREIGN KEY (transaction_id) REFERENCES sales_transactions(id)
);

-- ========================================
-- INVENTORY DOMAIN TABLES
-- ========================================

-- Inventory table
CREATE TABLE inventory (
    id VARCHAR(36) PRIMARY KEY,
    shop_id VARCHAR(36) NOT NULL,
    product_id VARCHAR(36) NOT NULL,
    current_stock DECIMAL(10,2) NOT NULL DEFAULT 0,
    reserved_stock DECIMAL(10,2) DEFAULT 0,
    available_stock DECIMAL(10,2) GENERATED ALWAYS AS (current_stock - reserved_stock) STORED,
    minimum_stock DECIMAL(10,2) DEFAULT 0,
    maximum_stock DECIMAL(10,2),
    reorder_point DECIMAL(10,2),
    unit_cost DECIMAL(10,2),
    total_value DECIMAL(12,2) GENERATED ALWAYS AS (current_stock * unit_cost) STORED,
    last_restocked_date TIMESTAMP,
    last_sold_date TIMESTAMP,
    stock_take_date TIMESTAMP,
    batch_number VARCHAR(100),
    expiry_date DATE,
    location VARCHAR(100),
    supplier_id VARCHAR(36),
    supplier_name VARCHAR(255),
    status VARCHAR(20) DEFAULT 'ACTIVE',
    -- BaseEntity fields
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    version BIGINT DEFAULT 0,
    FOREIGN KEY (shop_id) REFERENCES shops(id),
    FOREIGN KEY (product_id) REFERENCES products(id),
    UNIQUE(shop_id, product_id, batch_number)
);

-- Inventory history table
CREATE TABLE inventory_history (
    id VARCHAR(36) PRIMARY KEY,
    inventory_id VARCHAR(36) NOT NULL,
    change_type VARCHAR(50) NOT NULL,
    quantity_change DECIMAL(10,2) NOT NULL,
    previous_stock DECIMAL(10,2),
    new_stock DECIMAL(10,2),
    unit_cost DECIMAL(10,2),
    reference_type VARCHAR(50),
    reference_id VARCHAR(36),
    reason VARCHAR(500),
    performed_by VARCHAR(36),
    performed_at TIMESTAMP NOT NULL,
    -- BaseEntity fields
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    version BIGINT DEFAULT 0,
    FOREIGN KEY (inventory_id) REFERENCES inventory(id),
    FOREIGN KEY (performed_by) REFERENCES users(id)
);

-- ========================================
-- INVESTMENT DOMAIN TABLES
-- ========================================

-- Investments table
CREATE TABLE investments (
    id VARCHAR(36) PRIMARY KEY,
    shop_id VARCHAR(36) NOT NULL,
    investor_id VARCHAR(36) NOT NULL,
    investment_number VARCHAR(50) NOT NULL UNIQUE,
    amount DECIMAL(12,2) NOT NULL,
    profit_sharing_model VARCHAR(50) NOT NULL,
    profit_percentage DECIMAL(5,2),
    fixed_return_amount DECIMAL(12,2),
    investment_date DATE NOT NULL,
    maturity_date DATE,
    status VARCHAR(20) DEFAULT 'ACTIVE',
    notes TEXT,
    -- BaseEntity fields
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    version BIGINT DEFAULT 0,
    FOREIGN KEY (shop_id) REFERENCES shops(id),
    FOREIGN KEY (investor_id) REFERENCES users(id)
);

-- Investment-Product join table
CREATE TABLE investment_products (
    investment_id VARCHAR(36) NOT NULL,
    product_id VARCHAR(36) NOT NULL,
    PRIMARY KEY (investment_id, product_id),
    FOREIGN KEY (investment_id) REFERENCES investments(id) ON DELETE CASCADE,
    FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE
);

-- Investor shares table
CREATE TABLE investor_shares (
    id VARCHAR(36) PRIMARY KEY,
    investment_id VARCHAR(36) NOT NULL,
    transaction_id VARCHAR(36),
    transaction_amount DECIMAL(12,2),
    profit_amount DECIMAL(12,2),
    share_amount DECIMAL(12,2),
    share_percentage DECIMAL(5,2),
    calculation_date TIMESTAMP NOT NULL,
    period_start DATE,
    period_end DATE,
    status VARCHAR(20) DEFAULT 'CALCULATED',
    -- BaseEntity fields
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    version BIGINT DEFAULT 0,
    FOREIGN KEY (investment_id) REFERENCES investments(id),
    FOREIGN KEY (transaction_id) REFERENCES sales_transactions(id)
);

-- Investor distributions table
CREATE TABLE investor_distributions (
    id VARCHAR(36) PRIMARY KEY,
    investment_id VARCHAR(36) NOT NULL,
    period_start DATE NOT NULL,
    period_end DATE NOT NULL,
    total_sales_revenue DECIMAL(12,2),
    total_costs DECIMAL(12,2),
    total_profit DECIMAL(12,2),
    distribution_amount DECIMAL(12,2) NOT NULL,
    payment_date DATE,
    payment_method VARCHAR(50),
    payment_reference VARCHAR(100),
    status VARCHAR(20) DEFAULT 'PENDING',
    notes TEXT,
    -- BaseEntity fields
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    version BIGINT DEFAULT 0,
    FOREIGN KEY (investment_id) REFERENCES investments(id)
);

-- ========================================
-- RETURNS DOMAIN TABLES
-- ========================================

-- Product returns table
CREATE TABLE product_returns (
    id VARCHAR(36) PRIMARY KEY,
    shop_id VARCHAR(36) NOT NULL,
    sales_transaction_id VARCHAR(36),
    product_id VARCHAR(36) NOT NULL,
    return_number VARCHAR(50) NOT NULL UNIQUE,
    quantity_returned DECIMAL(10,2) NOT NULL,
    return_reason VARCHAR(100) NOT NULL,
    return_type VARCHAR(20) NOT NULL,
    return_date TIMESTAMP NOT NULL,
    refund_amount DECIMAL(10,2),
    exchange_product_id VARCHAR(36),
    store_credit_issued DECIMAL(10,2),
    condition_on_return VARCHAR(50),
    inspection_notes TEXT,
    processed_by VARCHAR(36),
    approved_by VARCHAR(36),
    fraud_check_status VARCHAR(20) DEFAULT 'NOT_CHECKED',
    fraud_check_notes TEXT,
    status VARCHAR(20) DEFAULT 'PENDING',
    -- BaseEntity fields
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    version BIGINT DEFAULT 0,
    FOREIGN KEY (shop_id) REFERENCES shops(id),
    FOREIGN KEY (sales_transaction_id) REFERENCES sales_transactions(id),
    FOREIGN KEY (product_id) REFERENCES products(id),
    FOREIGN KEY (exchange_product_id) REFERENCES products(id),
    FOREIGN KEY (processed_by) REFERENCES users(id),
    FOREIGN KEY (approved_by) REFERENCES users(id)
);

-- ========================================
-- EXPENSES DOMAIN TABLES
-- ========================================

-- Expense categories table
CREATE TABLE expense_categories (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    shop_id UUID NOT NULL,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    requires_approval BOOLEAN NOT NULL DEFAULT TRUE,
    approval_limit DECIMAL(10,2),
    default_payment_method VARCHAR(50),
    gl_account_code VARCHAR(50),
    tax_deductible BOOLEAN NOT NULL DEFAULT TRUE,
    auto_approval_enabled BOOLEAN NOT NULL DEFAULT FALSE,
    -- BaseEntity fields
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    version BIGINT DEFAULT 0,
    CONSTRAINT uk_expense_category_shop_name UNIQUE (shop_id, name)
);

-- Expenses table
CREATE TABLE expenses (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    shop_id UUID NOT NULL,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    category_id UUID NOT NULL,
    amount DECIMAL(10,2) NOT NULL,
    expense_date DATE NOT NULL,
    payment_method VARCHAR(50),
    vendor_name VARCHAR(255),
    reference_number VARCHAR(100),
    receipt_url VARCHAR(500),
    status VARCHAR(20) NOT NULL DEFAULT 'DRAFT',
    notes TEXT,
    expense_created_by UUID NOT NULL,
    created_by_name VARCHAR(255),
    approved_by UUID,
    approved_by_name VARCHAR(255),
    approval_date DATE,
    approval_notes TEXT,
    -- BaseEntity fields
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    version BIGINT DEFAULT 0,
    FOREIGN KEY (category_id) REFERENCES expense_categories(id)
);

-- Expense tags collection table
CREATE TABLE expense_tags (
    expense_id UUID NOT NULL,
    tag VARCHAR(255),
    FOREIGN KEY (expense_id) REFERENCES expenses(id) ON DELETE CASCADE
);

-- ========================================
-- SHARED DOMAIN TABLES
-- ========================================

-- Audit logs table
CREATE TABLE audit_logs (
    id VARCHAR(36) PRIMARY KEY,
    shop_id VARCHAR(36),
    user_id VARCHAR(255) NOT NULL,
    username VARCHAR(255),
    category VARCHAR(50) NOT NULL,
    action_type VARCHAR(50) NOT NULL,
    entity_type VARCHAR(100),
    entity_id VARCHAR(36),
    action_description VARCHAR(500) NOT NULL,
    action_date TIMESTAMP NOT NULL,
    ip_address VARCHAR(45),
    user_agent VARCHAR(500),
    session_id VARCHAR(255),
    old_values TEXT,
    new_values TEXT,
    severity VARCHAR(20) DEFAULT 'INFO',
    success BOOLEAN DEFAULT TRUE,
    error_message VARCHAR(1000),
    -- BaseEntity fields
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    version BIGINT DEFAULT 0,
    FOREIGN KEY (shop_id) REFERENCES shops(id)
);

-- Audit log details collection table
CREATE TABLE audit_log_details (
    audit_log_id VARCHAR(36) NOT NULL,
    detail_key VARCHAR(255) NOT NULL,
    detail_value VARCHAR(1000),
    PRIMARY KEY (audit_log_id, detail_key),
    FOREIGN KEY (audit_log_id) REFERENCES audit_logs(id) ON DELETE CASCADE
);

-- Feature flags table
CREATE TABLE feature_flags (
    id VARCHAR(36) PRIMARY KEY,
    shop_id VARCHAR(36),
    feature_name VARCHAR(100) NOT NULL,
    enabled BOOLEAN NOT NULL DEFAULT FALSE,
    description VARCHAR(500),
    effective_from TIMESTAMP,
    effective_until TIMESTAMP,
    created_by VARCHAR(255),
    last_modified_by VARCHAR(255),
    -- BaseEntity fields
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(255),
    version BIGINT DEFAULT 0,
    FOREIGN KEY (shop_id) REFERENCES shops(id),
    CONSTRAINT uk_feature_shop UNIQUE (shop_id, feature_name)
);

-- Feature flag configuration collection table
CREATE TABLE feature_flag_config (
    feature_flag_id VARCHAR(36) NOT NULL,
    config_key VARCHAR(255) NOT NULL,
    config_value VARCHAR(1000),
    PRIMARY KEY (feature_flag_id, config_key),
    FOREIGN KEY (feature_flag_id) REFERENCES feature_flags(id) ON DELETE CASCADE
);

-- ========================================
-- ANALYTICS DOMAIN TABLES
-- ========================================

-- Analytics cache table
CREATE TABLE analytics_cache (
    id VARCHAR(36) PRIMARY KEY,
    shop_id VARCHAR(36),
    analytics_type VARCHAR(50) NOT NULL,
    cache_key VARCHAR(255) NOT NULL,
    cache_data TEXT,
    cache_date TIMESTAMP NOT NULL,
    expires_at TIMESTAMP,
    -- BaseEntity fields
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    version BIGINT DEFAULT 0,
    FOREIGN KEY (shop_id) REFERENCES shops(id),
    UNIQUE(shop_id, analytics_type, cache_key)
);

-- ========================================
-- FRAUD DETECTION DOMAIN TABLES
-- ========================================

-- Fraud alerts table
CREATE TABLE fraud_alerts (
    id VARCHAR(36) PRIMARY KEY,
    shop_id VARCHAR(36) NOT NULL,
    user_id VARCHAR(36),
    alert_number VARCHAR(50) NOT NULL UNIQUE,
    alert_type VARCHAR(50) NOT NULL,
    severity VARCHAR(20) NOT NULL,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    detection_timestamp TIMESTAMP NOT NULL,
    risk_score DECIMAL(5,2),
    false_positive BOOLEAN DEFAULT FALSE,
    resolution_notes TEXT,
    resolved_by VARCHAR(36),
    resolved_at TIMESTAMP,
    status VARCHAR(20) DEFAULT 'OPEN',
    -- BaseEntity fields
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    version BIGINT DEFAULT 0,
    FOREIGN KEY (shop_id) REFERENCES shops(id),
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (resolved_by) REFERENCES users(id)
);

-- Fraud alert evidence collection table
CREATE TABLE fraud_alert_evidence (
    alert_id VARCHAR(36) NOT NULL,
    evidence_key VARCHAR(255) NOT NULL,
    evidence_value VARCHAR(1000),
    PRIMARY KEY (alert_id, evidence_key),
    FOREIGN KEY (alert_id) REFERENCES fraud_alerts(id) ON DELETE CASCADE
);

-- Fraud rules table
CREATE TABLE fraud_rules (
    id VARCHAR(255) PRIMARY KEY,
    shop_id VARCHAR(255),
    rule_name VARCHAR(255) NOT NULL,
    rule_type VARCHAR(255) NOT NULL,
    description VARCHAR(500),
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    threshold_amount DECIMAL(12,2),
    threshold_count INTEGER,
    time_window_minutes INTEGER,
    risk_score_weight DECIMAL(3,2) DEFAULT 1.0,
    severity VARCHAR(255) NOT NULL DEFAULT 'MEDIUM',
    auto_block BOOLEAN NOT NULL DEFAULT FALSE,
    requires_manual_review BOOLEAN NOT NULL DEFAULT TRUE,
    rule_configuration VARCHAR(2000),
    -- BaseEntity fields
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    version BIGINT DEFAULT 0,
    FOREIGN KEY (shop_id) REFERENCES shops(id),
    CONSTRAINT chk_severity CHECK (severity IN ('LOW','MEDIUM','HIGH','CRITICAL'))
);

-- Risk assessments table
CREATE TABLE risk_assessments (
    id VARCHAR(36) PRIMARY KEY,
    shop_id VARCHAR(36) NOT NULL,
    transaction_id VARCHAR(36),
    assessment_type VARCHAR(50) NOT NULL,
    risk_level VARCHAR(20) NOT NULL,
    risk_score DECIMAL(5,2),
    assessment_date TIMESTAMP NOT NULL,
    factors TEXT,
    recommendations TEXT,
    auto_blocked BOOLEAN DEFAULT FALSE,
    manual_review_required BOOLEAN DEFAULT FALSE,
    reviewed_by VARCHAR(36),
    review_date TIMESTAMP,
    review_decision VARCHAR(20),
    review_notes TEXT,
    status VARCHAR(20) DEFAULT 'PENDING',
    -- BaseEntity fields
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    version BIGINT DEFAULT 0,
    FOREIGN KEY (shop_id) REFERENCES shops(id),
    FOREIGN KEY (transaction_id) REFERENCES sales_transactions(id),
    FOREIGN KEY (reviewed_by) REFERENCES users(id)
);

-- Risk assessment flags collection table
CREATE TABLE risk_assessment_flags (
    risk_assessment_id VARCHAR(36) NOT NULL,
    flag VARCHAR(255),
    FOREIGN KEY (risk_assessment_id) REFERENCES risk_assessments(id) ON DELETE CASCADE
);

-- ========================================
-- SPRING MODULITH EVENT STORE
-- ========================================

-- Event publication table (for Spring Modulith)
CREATE TABLE event_publication (
    id UUID NOT NULL,
    listener_id VARCHAR(255) NOT NULL,
    event_type VARCHAR(255) NOT NULL,
    serialized_event TEXT NOT NULL,
    publication_date TIMESTAMP NOT NULL,
    completion_date TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE(listener_id, event_type, publication_date)
);

-- ========================================
-- INDEXES FOR PERFORMANCE
-- ========================================

-- Core domain indexes
CREATE INDEX idx_tenants_status ON tenants(status);
CREATE INDEX idx_users_tenant ON users(tenant_id);
CREATE INDEX idx_users_status ON users(status);
CREATE INDEX idx_users_keycloak ON users(keycloak_id);
CREATE INDEX idx_shops_tenant ON shops(tenant_id);
CREATE INDEX idx_shops_status ON shops(status);
CREATE INDEX idx_products_shop ON products(shop_id);
CREATE INDEX idx_products_category ON products(category_id);
CREATE INDEX idx_products_sku ON products(sku);
CREATE INDEX idx_products_status ON products(status);

-- Sales domain indexes
CREATE INDEX idx_sales_shop ON sales_transactions(shop_id);
CREATE INDEX idx_sales_date ON sales_transactions(transaction_date);
CREATE INDEX idx_sales_cashier ON sales_transactions(cashier_id);
CREATE INDEX idx_sales_status ON sales_transactions(transaction_status);
CREATE INDEX idx_line_items_transaction ON line_items(transaction_id);
CREATE INDEX idx_line_items_product ON line_items(product_id);
CREATE INDEX idx_receipts_transaction ON receipts(transaction_id);
CREATE INDEX idx_receipts_number ON receipts(receipt_number);

-- Inventory domain indexes
CREATE INDEX idx_inventory_shop ON inventory(shop_id);
CREATE INDEX idx_inventory_product ON inventory(product_id);
CREATE INDEX idx_inventory_batch ON inventory(batch_number);
CREATE INDEX idx_inventory_status ON inventory(status);
CREATE INDEX idx_inventory_history_inventory ON inventory_history(inventory_id);
CREATE INDEX idx_inventory_history_performed_by ON inventory_history(performed_by);
CREATE INDEX idx_inventory_history_date ON inventory_history(performed_at);

-- Investment domain indexes
CREATE INDEX idx_investments_shop ON investments(shop_id);
CREATE INDEX idx_investments_investor ON investments(investor_id);
CREATE INDEX idx_investments_status ON investments(status);
CREATE INDEX idx_investor_shares_investment ON investor_shares(investment_id);
CREATE INDEX idx_investor_shares_transaction ON investor_shares(transaction_id);
CREATE INDEX idx_investor_distributions_investment ON investor_distributions(investment_id);
CREATE INDEX idx_investor_distributions_status ON investor_distributions(status);

-- Returns domain indexes
CREATE INDEX idx_returns_shop ON product_returns(shop_id);
CREATE INDEX idx_returns_transaction ON product_returns(sales_transaction_id);
CREATE INDEX idx_returns_product ON product_returns(product_id);
CREATE INDEX idx_returns_status ON product_returns(status);

-- Expenses domain indexes
CREATE INDEX idx_expense_categories_shop ON expense_categories(shop_id);
CREATE INDEX idx_expense_categories_active ON expense_categories(is_active);
CREATE INDEX idx_expense_categories_name ON expense_categories(name);
CREATE INDEX idx_expenses_shop_date ON expenses(shop_id, expense_date);
CREATE INDEX idx_expenses_category ON expenses(category_id);
CREATE INDEX idx_expenses_status ON expenses(status);
CREATE INDEX idx_expenses_created_by ON expenses(expense_created_by);
CREATE INDEX idx_expenses_approved_by ON expenses(approved_by);
CREATE INDEX idx_expense_tags_expense_id ON expense_tags(expense_id);
CREATE INDEX idx_expense_tags_tag ON expense_tags(tag);

-- Shared domain indexes
CREATE INDEX idx_audit_shop ON audit_logs(shop_id);
CREATE INDEX idx_audit_user ON audit_logs(user_id);
CREATE INDEX idx_audit_entity ON audit_logs(entity_type, entity_id);
CREATE INDEX idx_audit_action ON audit_logs(action_type);
CREATE INDEX idx_audit_date ON audit_logs(action_date);
CREATE INDEX idx_audit_category ON audit_logs(category);
CREATE INDEX idx_feature_shop ON feature_flags(shop_id);
CREATE INDEX idx_feature_name ON feature_flags(feature_name);
CREATE INDEX idx_feature_enabled ON feature_flags(enabled);
CREATE INDEX idx_feature_flag_config_flag_id ON feature_flag_config(feature_flag_id);
CREATE INDEX idx_feature_flag_config_key ON feature_flag_config(config_key);

-- Analytics domain indexes
CREATE INDEX idx_analytics_cache_shop ON analytics_cache(shop_id);
CREATE INDEX idx_analytics_cache_type ON analytics_cache(analytics_type);
CREATE INDEX idx_analytics_cache_expires ON analytics_cache(expires_at);

-- Fraud detection domain indexes
CREATE INDEX idx_fraud_alerts_shop ON fraud_alerts(shop_id);
CREATE INDEX idx_fraud_alerts_user ON fraud_alerts(user_id);
CREATE INDEX idx_fraud_alerts_status ON fraud_alerts(status);
CREATE INDEX idx_fraud_alerts_severity ON fraud_alerts(severity);
CREATE INDEX idx_fraud_alerts_detection_date ON fraud_alerts(detection_timestamp);
CREATE INDEX idx_fraud_rules_shop ON fraud_rules(shop_id);
CREATE INDEX idx_fraud_rules_enabled ON fraud_rules(enabled);
CREATE INDEX idx_risk_assessments_shop ON risk_assessments(shop_id);
CREATE INDEX idx_risk_assessments_transaction ON risk_assessments(transaction_id);
CREATE INDEX idx_risk_assessments_status ON risk_assessments(status);
CREATE INDEX idx_risk_assessments_level ON risk_assessments(risk_level);

-- Event publication indexes
CREATE INDEX idx_event_publication_completion ON event_publication(completion_date);
CREATE INDEX idx_event_publication_listener ON event_publication(listener_id);