-- Migration V46: Create Cloud Aggregator Tables
-- Purpose: Support cloud aggregator API for local installation registration
-- Tables: cloud_tenants, cloud_shops

-- ============================================================================
-- Cloud Tenants Table
-- ============================================================================
-- Stores information about tenants registered from local embedded installations
CREATE TABLE cloud_tenants (
    id VARCHAR(255) PRIMARY KEY,
    tenant_name VARCHAR(255) NOT NULL,
    tenant_email VARCHAR(255) NOT NULL,
    company_registration VARCHAR(255),
    tax_id VARCHAR(255),
    address VARCHAR(500),
    city VARCHAR(255),
    state VARCHAR(255),
    country VARCHAR(255),
    phone_number VARCHAR(255),
    api_key_hash VARCHAR(500) NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'ACTIVE',
    shop_count INTEGER NOT NULL DEFAULT 0,
    subscription_tier VARCHAR(50) NOT NULL DEFAULT 'FREE',

    -- Audit fields (from BaseEntity)
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    version BIGINT NOT NULL DEFAULT 0,

    CONSTRAINT chk_cloud_tenant_status CHECK (status IN ('ACTIVE', 'SUSPENDED', 'INACTIVE')),
    CONSTRAINT chk_cloud_tenant_tier CHECK (subscription_tier IN ('FREE', 'BASIC', 'PREMIUM', 'ENTERPRISE'))
);

-- Create indexes for cloud_tenants
CREATE INDEX idx_cloud_tenant_email ON cloud_tenants(tenant_email);
CREATE INDEX idx_cloud_tenant_api_key ON cloud_tenants(api_key_hash);
CREATE INDEX idx_cloud_tenant_status ON cloud_tenants(status);

-- ============================================================================
-- Cloud Shops Table
-- ============================================================================
-- Represents individual shops registered under cloud tenants
CREATE TABLE cloud_shops (
    id VARCHAR(255) PRIMARY KEY,
    cloud_tenant_id VARCHAR(255) NOT NULL,
    shop_name VARCHAR(255) NOT NULL,
    shop_email VARCHAR(255),
    address VARCHAR(500),
    city VARCHAR(255),
    state VARCHAR(255),
    country VARCHAR(255),
    phone_number VARCHAR(255),
    status VARCHAR(50) NOT NULL DEFAULT 'ACTIVE',

    -- Audit fields (from BaseEntity)
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    version BIGINT NOT NULL DEFAULT 0,

    CONSTRAINT fk_cloud_shop_tenant FOREIGN KEY (cloud_tenant_id)
        REFERENCES cloud_tenants(id) ON DELETE CASCADE,
    CONSTRAINT chk_cloud_shop_status CHECK (status IN ('ACTIVE', 'INACTIVE'))
);

-- Create indexes for cloud_shops
CREATE INDEX idx_cloud_shop_tenant ON cloud_shops(cloud_tenant_id);
CREATE INDEX idx_cloud_shop_email ON cloud_shops(shop_email);
CREATE INDEX idx_cloud_shop_status ON cloud_shops(status);

-- ============================================================================
-- Comments
-- ============================================================================
COMMENT ON TABLE cloud_tenants IS 'Stores cloud tenant registrations from local RetailHQ installations';
COMMENT ON TABLE cloud_shops IS 'Stores shop information for cloud-registered tenants';

COMMENT ON COLUMN cloud_tenants.api_key_hash IS 'Hashed API key for authenticating sync requests';
COMMENT ON COLUMN cloud_tenants.shop_count IS 'Number of shops registered under this tenant';
COMMENT ON COLUMN cloud_tenants.subscription_tier IS 'Subscription tier for billing and feature access';

-- ============================================================================
-- Migration Complete
-- ============================================================================