-- Migration V27: Create tenant_configurations table
-- Description: Add support for tenant-wide configuration settings with flexible key-value storage
-- Author: Claude Code
-- Date: 2025-11-11

-- Create tenant_configurations table
CREATE TABLE IF NOT EXISTS tenant_configurations (
    id VARCHAR(255) PRIMARY KEY,
    tenant_id VARCHAR(255) NOT NULL,
    config_key VARCHAR(100) NOT NULL,
    config_value TEXT,
    default_value TEXT,
    value_type VARCHAR(20) NOT NULL DEFAULT 'STRING',
    category VARCHAR(30) NOT NULL DEFAULT 'BUSINESS',
    description VARCHAR(500),
    editable BOOLEAN NOT NULL DEFAULT TRUE,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    version BIGINT NOT NULL DEFAULT 0,

    -- Foreign key constraint
    CONSTRAINT fk_tenant_config_tenant FOREIGN KEY (tenant_id)
        REFERENCES tenants(id) ON DELETE CASCADE,

    -- Unique constraint: one key per tenant
    CONSTRAINT uk_tenant_config_key UNIQUE (tenant_id, config_key),

    -- Check constraints for enum values
    CONSTRAINT chk_value_type CHECK (value_type IN ('STRING', 'NUMBER', 'BOOLEAN', 'JSON')),
    CONSTRAINT chk_category CHECK (category IN ('BUSINESS', 'DISPLAY', 'NOTIFICATION', 'INTEGRATION', 'OPERATIONAL', 'SECURITY', 'FEATURE'))
);

-- Create indexes for performance
CREATE INDEX IF NOT EXISTS idx_tenant_config ON tenant_configurations(tenant_id, config_key);
CREATE INDEX IF NOT EXISTS idx_config_category ON tenant_configurations(category);
CREATE INDEX IF NOT EXISTS idx_config_active ON tenant_configurations(active);

-- Add configuration permissions to permissions table
INSERT INTO permissions (id, name, description, resource_type, created_date, last_modified_date, version)
VALUES
    ('perm-tenant-config-read', 'TENANT_CONFIG_READ', 'View tenant configuration settings', 'TENANT', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    ('perm-tenant-config-create', 'TENANT_CONFIG_CREATE', 'Create tenant configuration settings', 'TENANT', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    ('perm-tenant-config-update', 'TENANT_CONFIG_UPDATE', 'Update tenant configuration settings', 'TENANT', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    ('perm-tenant-config-delete', 'TENANT_CONFIG_DELETE', 'Delete tenant configuration settings', 'TENANT', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0)
ON CONFLICT (name) DO NOTHING;

-- Assign configuration permissions to appropriate roles
-- SYSTEM_ADMIN gets all permissions
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r
CROSS JOIN permissions p
WHERE r.name = 'SYSTEM_ADMIN'
  AND p.name IN ('TENANT_CONFIG_READ', 'TENANT_CONFIG_CREATE', 'TENANT_CONFIG_UPDATE', 'TENANT_CONFIG_DELETE')
ON CONFLICT DO NOTHING;

-- TENANT_ADMIN gets all configuration permissions
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r
CROSS JOIN permissions p
WHERE r.name = 'TENANT_ADMIN'
  AND p.name IN ('TENANT_CONFIG_READ', 'TENANT_CONFIG_CREATE', 'TENANT_CONFIG_UPDATE', 'TENANT_CONFIG_DELETE')
ON CONFLICT DO NOTHING;

-- OWNER gets read, create, and update permissions
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r
CROSS JOIN permissions p
WHERE r.name = 'OWNER'
  AND p.name IN ('TENANT_CONFIG_READ', 'TENANT_CONFIG_CREATE', 'TENANT_CONFIG_UPDATE')
ON CONFLICT DO NOTHING;

-- MANAGER gets read permission only
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r
CROSS JOIN permissions p
WHERE r.name = 'MANAGER'
  AND p.name = 'TENANT_CONFIG_READ'
ON CONFLICT DO NOTHING;

-- Comment on table and columns
COMMENT ON TABLE tenant_configurations IS 'Tenant-wide configuration settings with flexible key-value storage';
COMMENT ON COLUMN tenant_configurations.config_key IS 'Unique configuration key within tenant scope';
COMMENT ON COLUMN tenant_configurations.config_value IS 'Actual configuration value (null means use default)';
COMMENT ON COLUMN tenant_configurations.default_value IS 'Default value if config_value is null';
COMMENT ON COLUMN tenant_configurations.value_type IS 'Type of value: STRING, NUMBER, BOOLEAN, or JSON';
COMMENT ON COLUMN tenant_configurations.category IS 'Configuration category for logical grouping';
COMMENT ON COLUMN tenant_configurations.editable IS 'Whether the configuration can be modified by users';
COMMENT ON COLUMN tenant_configurations.active IS 'Whether the configuration is currently active';
