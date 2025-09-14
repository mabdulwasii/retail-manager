-- V6: Tenant-Shop Refactoring and Enhanced Features
-- This migration separates Tenant from Shop and adds inventory, expense tracking, and other enhancements

-- Create tenants table
CREATE TABLE tenants (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(255) NOT NULL UNIQUE,
    description TEXT,
    company_registration VARCHAR(255),
    tax_id VARCHAR(255),
    contact_email VARCHAR(255) NOT NULL,
    contact_phone VARCHAR(50),
    primary_address TEXT,
    city VARCHAR(100),
    state VARCHAR(100),
    country VARCHAR(100),
    postal_code VARCHAR(20),
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    version BIGINT DEFAULT 0
);

-- Insert default tenant for existing data
INSERT INTO tenants (id, name, contact_email, created_date)
VALUES ('00000000-0000-0000-0000-000000000001', 'Default Organization', 'admin@shopmanager.com', CURRENT_TIMESTAMP);

-- Add tenant_id column to shops and make it reference tenants
ALTER TABLE shops DROP CONSTRAINT IF EXISTS uk_shops_tenant_id;
ALTER TABLE shops ALTER COLUMN tenant_id TYPE UUID USING tenant_id::UUID;
ALTER TABLE shops ADD CONSTRAINT fk_shops_tenant_id FOREIGN KEY (tenant_id) REFERENCES tenants(id);

-- Update existing shops to reference the default tenant
UPDATE shops SET tenant_id = '00000000-0000-0000-0000-000000000001' WHERE tenant_id IS NULL;

-- Add tenant_id column to users and make it reference tenants
ALTER TABLE users ADD COLUMN tenant_id UUID;
ALTER TABLE users ADD CONSTRAINT fk_users_tenant_id FOREIGN KEY (tenant_id) REFERENCES tenants(id);

-- Update existing users to reference the default tenant
UPDATE users SET tenant_id = '00000000-0000-0000-0000-000000000001' WHERE tenant_id IS NULL;
ALTER TABLE users ALTER COLUMN tenant_id SET NOT NULL;

-- Remove shop_users junction table (users now belong to tenant, not shop)
DROP TABLE IF EXISTS shop_users;

-- Create inventory table
CREATE TABLE inventory (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    shop_id UUID NOT NULL REFERENCES shops(id),
    product_id UUID NOT NULL REFERENCES products(id),
    current_stock INTEGER NOT NULL DEFAULT 0,
    reserved_stock INTEGER NOT NULL DEFAULT 0,
    available_stock INTEGER GENERATED ALWAYS AS (current_stock - reserved_stock) STORED,
    minimum_stock INTEGER NOT NULL DEFAULT 0,
    maximum_stock INTEGER,
    reorder_point INTEGER NOT NULL DEFAULT 0,
    unit_cost DECIMAL(10,2),
    location VARCHAR(255),
    batch_number VARCHAR(100),
    expiry_date DATE,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    last_stock_update TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    version BIGINT DEFAULT 0,
    UNIQUE(shop_id, product_id, batch_number)
);

-- Create inventory history table for tracking stock changes
CREATE TABLE inventory_history (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    inventory_id UUID NOT NULL REFERENCES inventory(id),
    change_type VARCHAR(50) NOT NULL, -- STOCK_IN, STOCK_OUT, ADJUSTMENT, RETURN, SALE
    quantity_change INTEGER NOT NULL,
    previous_stock INTEGER NOT NULL,
    new_stock INTEGER NOT NULL,
    reference_id UUID, -- Reference to sale, return, adjustment record
    reference_type VARCHAR(50), -- SALE, RETURN, ADJUSTMENT, PROCUREMENT
    reason TEXT,
    performed_by UUID REFERENCES users(id),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create expenses table
CREATE TABLE expenses (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    shop_id UUID NOT NULL REFERENCES shops(id),
    category VARCHAR(100) NOT NULL,
    description TEXT NOT NULL,
    amount DECIMAL(10,2) NOT NULL,
    expense_date DATE NOT NULL,
    payment_method VARCHAR(50),
    vendor_name VARCHAR(255),
    reference_number VARCHAR(100),
    status VARCHAR(20) NOT NULL DEFAULT 'APPROVED',
    created_by UUID REFERENCES users(id),
    approved_by UUID REFERENCES users(id),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    version BIGINT DEFAULT 0
);

-- Create returns table for product returns
CREATE TABLE product_returns (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    shop_id UUID NOT NULL REFERENCES shops(id),
    sales_transaction_id UUID NOT NULL REFERENCES sales_transactions(id),
    product_id UUID NOT NULL REFERENCES products(id),
    quantity_returned INTEGER NOT NULL,
    return_reason VARCHAR(100) NOT NULL,
    return_type VARCHAR(50) NOT NULL DEFAULT 'FULL', -- FULL, PARTIAL, DAMAGED, EXPIRED
    refund_amount DECIMAL(10,2),
    refund_type VARCHAR(50) DEFAULT 'CASH', -- CASH, STORE_CREDIT, EXCHANGE
    condition_assessment VARCHAR(100),
    fraud_check_status VARCHAR(50) DEFAULT 'PENDING',
    fraud_check_result TEXT,
    processed_by UUID REFERENCES users(id),
    return_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    processed_date TIMESTAMP,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    version BIGINT DEFAULT 0
);

-- Add location and metadata fields to products
ALTER TABLE products ADD COLUMN location VARCHAR(255);
ALTER TABLE products ADD COLUMN metadata JSONB;
ALTER TABLE products ADD COLUMN sku VARCHAR(100) UNIQUE;
ALTER TABLE products ADD COLUMN barcode VARCHAR(100);
ALTER TABLE products ADD COLUMN weight DECIMAL(8,3);
ALTER TABLE products ADD COLUMN dimensions VARCHAR(100);
ALTER TABLE products ADD COLUMN supplier_name VARCHAR(255);
ALTER TABLE products ADD COLUMN supplier_contact VARCHAR(255);

-- Update feature_flags to be tenant-scoped
ALTER TABLE feature_flags ADD COLUMN tenant_id UUID REFERENCES tenants(id);
UPDATE feature_flags SET tenant_id = '00000000-0000-0000-0000-000000000001' WHERE tenant_id IS NULL;

-- Create backup_status table for tracking backups
CREATE TABLE backup_status (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    backup_type VARCHAR(50) NOT NULL, -- WEEKLY, MONTHLY, MANUAL
    backup_date TIMESTAMP NOT NULL,
    backup_size BIGINT,
    backup_location VARCHAR(500) NOT NULL,
    encryption_key_hash VARCHAR(255) NOT NULL,
    status VARCHAR(20) NOT NULL, -- SUCCESS, FAILED, IN_PROGRESS
    failure_reason TEXT,
    uploaded_to_remote BOOLEAN DEFAULT FALSE,
    remote_location VARCHAR(500),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create indexes for performance
CREATE INDEX idx_inventory_shop_product ON inventory(shop_id, product_id);
CREATE INDEX idx_inventory_status ON inventory(status);
CREATE INDEX idx_inventory_stock_level ON inventory(available_stock);
CREATE INDEX idx_inventory_expiry ON inventory(expiry_date) WHERE expiry_date IS NOT NULL;

CREATE INDEX idx_inventory_history_inventory ON inventory_history(inventory_id);
CREATE INDEX idx_inventory_history_date ON inventory_history(created_at);
CREATE INDEX idx_inventory_history_type ON inventory_history(change_type);

CREATE INDEX idx_expenses_shop_date ON expenses(shop_id, expense_date);
CREATE INDEX idx_expenses_category ON expenses(category);
CREATE INDEX idx_expenses_status ON expenses(status);

CREATE INDEX idx_returns_shop ON product_returns(shop_id);
CREATE INDEX idx_returns_transaction ON product_returns(sales_transaction_id);
CREATE INDEX idx_returns_status ON product_returns(status);
CREATE INDEX idx_returns_date ON product_returns(return_date);

CREATE INDEX idx_products_location ON products(location) WHERE location IS NOT NULL;
CREATE INDEX idx_products_sku ON products(sku) WHERE sku IS NOT NULL;
CREATE INDEX idx_products_supplier ON products(supplier_name) WHERE supplier_name IS NOT NULL;

CREATE INDEX idx_feature_flags_tenant ON feature_flags(tenant_id);

-- Add audit log enhancements for shop-level filtering
ALTER TABLE audit_logs ADD COLUMN shop_id UUID REFERENCES shops(id);

-- Create triggers for automatic timestamp updates
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
   NEW.updated_at = CURRENT_TIMESTAMP;
   RETURN NEW;
END;
$$ language 'plpgsql';

CREATE TRIGGER update_tenants_updated_at BEFORE UPDATE ON tenants FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
CREATE TRIGGER update_inventory_updated_at BEFORE UPDATE ON inventory FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
CREATE TRIGGER update_expenses_updated_at BEFORE UPDATE ON expenses FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
CREATE TRIGGER update_returns_updated_at BEFORE UPDATE ON product_returns FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();