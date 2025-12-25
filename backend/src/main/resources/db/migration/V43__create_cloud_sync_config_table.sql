-- V43: Create cloud_sync_config table for embedded mode cloud synchronization
-- This table stores cloud tenant credentials and sync state for each local tenant

CREATE TABLE IF NOT EXISTS cloud_sync_config (
    id VARCHAR(255) PRIMARY KEY,
    tenant_id VARCHAR(255) NOT NULL REFERENCES tenants(id) ON DELETE CASCADE,
    cloud_tenant_id VARCHAR(255),
    cloud_api_key VARCHAR(500),  -- Encrypted API key for cloud authentication
    cloud_api_url VARCHAR(500) DEFAULT 'https://cloud.shopmanager.com/api',
    sync_enabled BOOLEAN DEFAULT false,
    last_sync_at TIMESTAMP,
    sync_status VARCHAR(50) DEFAULT 'NOT_CONFIGURED', -- NOT_CONFIGURED, CONFIGURED, SYNCING, ERROR
    last_error TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    version INTEGER DEFAULT 0,
    CONSTRAINT uk_cloud_sync_tenant UNIQUE (tenant_id)
);

-- Add indexes for performance
CREATE INDEX IF NOT EXISTS idx_cloud_sync_tenant_id ON cloud_sync_config(tenant_id);
CREATE INDEX IF NOT EXISTS idx_cloud_sync_cloud_tenant_id ON cloud_sync_config(cloud_tenant_id);
CREATE INDEX IF NOT EXISTS idx_cloud_sync_status ON cloud_sync_config(sync_status);

-- Add comments
COMMENT ON TABLE cloud_sync_config IS 'Cloud synchronization configuration for embedded mode deployments';
COMMENT ON COLUMN cloud_sync_config.cloud_tenant_id IS 'UUID of the tenant in the cloud aggregator system';
COMMENT ON COLUMN cloud_sync_config.cloud_api_key IS 'Encrypted API key for authenticating with cloud aggregator';
COMMENT ON COLUMN cloud_sync_config.sync_enabled IS 'Whether cloud sync is currently enabled';
COMMENT ON COLUMN cloud_sync_config.sync_status IS 'Current sync status: NOT_CONFIGURED, CONFIGURED, SYNCING, ERROR';
