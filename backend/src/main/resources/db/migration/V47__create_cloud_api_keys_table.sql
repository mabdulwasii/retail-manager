-- =====================================================
-- Migration: V47 - Create cloud_api_keys table
-- Description: Add multi-key management for cloud tenants
-- Author: Shop Manager Development Team
-- Date: 2026-01-04
-- =====================================================

-- Create cloud_api_keys table
CREATE TABLE cloud_api_keys (
    id VARCHAR(255) PRIMARY KEY,
    tenant_id VARCHAR(255) NOT NULL,
    key_prefix VARCHAR(16) NOT NULL,
    masked_key VARCHAR(100),
    key_hash VARCHAR(500) NOT NULL,
    description VARCHAR(500) NOT NULL,
    last_used_at TIMESTAMP,
    expires_at TIMESTAMP,
    is_active BOOLEAN NOT NULL DEFAULT true,
    usage_count BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(255),
    updated_at TIMESTAMP,
    updated_by VARCHAR(255),
    version INTEGER NOT NULL DEFAULT 0,
    CONSTRAINT fk_cloud_api_key_tenant
        FOREIGN KEY (tenant_id) REFERENCES cloud_tenants(id) ON DELETE CASCADE
);

-- Create indexes for performance
CREATE INDEX idx_cloud_api_key_tenant ON cloud_api_keys(tenant_id);
CREATE INDEX idx_cloud_api_key_prefix ON cloud_api_keys(key_prefix);
CREATE INDEX idx_cloud_api_key_status ON cloud_api_keys(is_active);

-- Create permissions table for API keys
CREATE TABLE cloud_api_key_permissions (
    api_key_id VARCHAR(255) NOT NULL,
    permission VARCHAR(50) NOT NULL,
    PRIMARY KEY (api_key_id, permission),
    CONSTRAINT fk_cloud_api_key_permission
        FOREIGN KEY (api_key_id) REFERENCES cloud_api_keys(id) ON DELETE CASCADE
);

-- Add comments for documentation
COMMENT ON TABLE cloud_api_keys IS 'API keys for cloud tenant authentication and authorization';
COMMENT ON COLUMN cloud_api_keys.key_prefix IS 'First 8 characters of API key for display and lookup';
COMMENT ON COLUMN cloud_api_keys.masked_key IS 'Masked version of full key: prefix...suffix';
COMMENT ON COLUMN cloud_api_keys.key_hash IS 'Bcrypt hash of the full API key';
COMMENT ON COLUMN cloud_api_keys.description IS 'Human-readable description of key purpose';
COMMENT ON COLUMN cloud_api_keys.last_used_at IS 'Timestamp of last API request with this key';
COMMENT ON COLUMN cloud_api_keys.expires_at IS 'Expiration timestamp (NULL = never expires)';
COMMENT ON COLUMN cloud_api_keys.is_active IS 'Whether the key is currently active (revoked keys are inactive)';
COMMENT ON COLUMN cloud_api_keys.usage_count IS 'Total number of API requests made with this key';

COMMENT ON TABLE cloud_api_key_permissions IS 'Permissions granted to each API key (READ, WRITE, DELETE, SYNC, ADMIN)';
