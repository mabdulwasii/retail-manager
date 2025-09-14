-- V8: Performance Optimizations with Indexes and Query Enhancements
-- Created for improved database performance and query optimization

-- Strategic indexes for frequently accessed data
CREATE INDEX IF NOT EXISTS idx_shops_tenant_status ON shops(tenant_id, status);
CREATE INDEX IF NOT EXISTS idx_shops_status_created ON shops(status, created_at);
CREATE INDEX IF NOT EXISTS idx_shops_name_lower ON shops(LOWER(name));

-- Inventory optimization indexes
CREATE INDEX IF NOT EXISTS idx_inventory_shop_stock ON inventory(shop_id, current_stock);
CREATE INDEX IF NOT EXISTS idx_inventory_low_stock ON inventory(shop_id, (current_stock - reserved_stock), minimum_stock);
CREATE INDEX IF NOT EXISTS idx_inventory_expiry ON inventory(shop_id, expiry_date) WHERE expiry_date IS NOT NULL;
CREATE INDEX IF NOT EXISTS idx_inventory_status_location ON inventory(status, location);

-- Sales transaction indexes for analytics
CREATE INDEX IF NOT EXISTS idx_sales_shop_date ON sales_transactions(shop_id, transaction_date);
CREATE INDEX IF NOT EXISTS idx_sales_date_total ON sales_transactions(transaction_date, total_amount);
CREATE INDEX IF NOT EXISTS idx_sales_shop_status ON sales_transactions(shop_id, status);

-- Investment and profit tracking indexes
CREATE INDEX IF NOT EXISTS idx_investments_shop_status ON investments(shop_id, status);
CREATE INDEX IF NOT EXISTS idx_investor_distributions_investment ON investor_distributions(investment_id, period_start, period_end);
CREATE INDEX IF NOT EXISTS idx_investor_distributions_status ON investor_distributions(status, period_start);

-- Audit log performance indexes
CREATE INDEX IF NOT EXISTS idx_audit_logs_entity_date ON audit_logs(entity_type, entity_id, created_at);
CREATE INDEX IF NOT EXISTS idx_audit_logs_action_date ON audit_logs(action_type, created_at);
CREATE INDEX IF NOT EXISTS idx_audit_logs_tenant_date ON audit_logs(tenant_id, created_at);

-- Product and category indexes
CREATE INDEX IF NOT EXISTS idx_products_shop_category ON products(shop_id, category_id);
CREATE INDEX IF NOT EXISTS idx_products_status_active ON products(status) WHERE status = 'ACTIVE';
CREATE INDEX IF NOT EXISTS idx_products_sku_unique ON products(sku) WHERE sku IS NOT NULL;

-- User and tenant relationship indexes
CREATE INDEX IF NOT EXISTS idx_users_tenant_status ON users(tenant_id, status);
CREATE INDEX IF NOT EXISTS idx_users_keycloak_id ON users(keycloak_id);

-- Receipt and line items indexes
CREATE INDEX IF NOT EXISTS idx_receipts_shop_date ON receipts(shop_id, created_at);
CREATE INDEX IF NOT EXISTS idx_line_items_receipt_product ON line_items(receipt_id, product_id);

-- Feature flags and configuration indexes
CREATE INDEX IF NOT EXISTS idx_feature_flags_scope_name ON feature_flags(scope, flag_name);
CREATE INDEX IF NOT EXISTS idx_feature_flags_tenant_enabled ON feature_flags(tenant_id, enabled);

-- Analytics cache indexes
CREATE INDEX IF NOT EXISTS idx_analytics_cache_shop_type ON analytics_cache(shop_id, cache_type);
CREATE INDEX IF NOT EXISTS idx_analytics_cache_updated ON analytics_cache(last_updated);

-- Create composite indexes for common query patterns
CREATE INDEX IF NOT EXISTS idx_shops_tenant_status_created ON shops(tenant_id, status, created_at);
CREATE INDEX IF NOT EXISTS idx_inventory_shop_product_batch ON inventory(shop_id, product_id, batch_number);
CREATE INDEX IF NOT EXISTS idx_sales_shop_date_status ON sales_transactions(shop_id, transaction_date, status);

-- Partial indexes for specific conditions
CREATE INDEX IF NOT EXISTS idx_inventory_active_low_stock ON inventory(shop_id, current_stock)
    WHERE status = 'ACTIVE' AND current_stock <= minimum_stock;

CREATE INDEX IF NOT EXISTS idx_products_active_in_stock ON products(shop_id, name)
    WHERE status = 'ACTIVE';

CREATE INDEX IF NOT EXISTS idx_recent_audit_logs ON audit_logs(entity_type, entity_id, created_at)
    WHERE created_at >= CURRENT_DATE - INTERVAL '30 days';

-- Comments for maintenance
COMMENT ON INDEX idx_shops_tenant_status IS 'Optimizes tenant-scoped shop queries by status';
COMMENT ON INDEX idx_inventory_low_stock IS 'Optimizes low stock detection queries';
COMMENT ON INDEX idx_sales_shop_date IS 'Optimizes analytics queries by shop and date range';
COMMENT ON INDEX idx_audit_logs_entity_date IS 'Optimizes audit trail queries for specific entities';

-- Update table statistics for better query planning
ANALYZE shops;
ANALYZE inventory;
ANALYZE sales_transactions;
ANALYZE investments;
ANALYZE products;
ANALYZE audit_logs;