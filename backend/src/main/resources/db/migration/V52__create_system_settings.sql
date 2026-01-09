-- ============================================================================
-- V52: Create System Settings Table for Docker Lite Configuration
-- ============================================================================
-- This migration creates a flexible key-value store for system-wide settings
-- that can be managed through the UI in Docker Lite (embedded mode).
-- ============================================================================

-- System Settings Table
CREATE TABLE IF NOT EXISTS system_settings (
    id VARCHAR(36) PRIMARY KEY,
    setting_key VARCHAR(100) NOT NULL UNIQUE,
    setting_value TEXT,
    setting_category VARCHAR(50) NOT NULL, -- SYSTEM, DOMAIN, SYNC, STORAGE, SECURITY, DATABASE
    data_type VARCHAR(20) NOT NULL DEFAULT 'STRING', -- STRING, NUMBER, BOOLEAN, JSON, ENCRYPTED
    description TEXT,
    requires_restart BOOLEAN NOT NULL DEFAULT FALSE,
    is_sensitive BOOLEAN NOT NULL DEFAULT FALSE, -- For API keys, passwords, etc.
    default_value TEXT,
    validation_regex VARCHAR(255),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(36),
    version BIGINT NOT NULL DEFAULT 0
);

-- Create index for faster lookups
CREATE INDEX idx_system_settings_category ON system_settings(setting_category);
CREATE INDEX idx_system_settings_key ON system_settings(setting_key);

-- Insert default settings for Docker Lite
INSERT INTO system_settings (id, setting_key, setting_value, setting_category, data_type, description, requires_restart, is_sensitive, default_value) VALUES
-- Domain Configuration
('setting-domain-custom', 'custom.domain', 'shopmanager.local', 'DOMAIN', 'STRING', 'Custom domain for the application', TRUE, FALSE, 'shopmanager.local'),
('setting-domain-port', 'app.port', '80', 'DOMAIN', 'NUMBER', 'Application port (nginx reverse proxy)', TRUE, FALSE, '80'),
('setting-shop-name', 'shop.name', 'Shop Manager', 'SYSTEM', 'STRING', 'Shop/Business name displayed in UI', FALSE, FALSE, 'Shop Manager'),

-- Cloud Sync Configuration
('setting-sync-enabled', 'cloud.sync.enabled', 'false', 'SYNC', 'BOOLEAN', 'Enable cloud synchronization', FALSE, FALSE, 'false'),
('setting-sync-url', 'cloud.api.url', '', 'SYNC', 'STRING', 'Cloud API endpoint URL', FALSE, FALSE, 'https://api.retailhq.app'),
('setting-sync-api-key', 'cloud.api.key', '', 'SYNC', 'ENCRYPTED', 'API key for cloud authentication', FALSE, TRUE, ''),
('setting-sync-store-id', 'cloud.store.id', '', 'SYNC', 'STRING', 'Unique store identifier in cloud', FALSE, FALSE, ''),
('setting-sync-cron', 'cloud.sync.cron', '0 0 * * * ?', 'SYNC', 'STRING', 'Sync schedule (cron expression)', FALSE, FALSE, '0 0 * * * ?'),
('setting-sync-anonymize-pii', 'cloud.sync.anonymize.pii', 'false', 'SYNC', 'BOOLEAN', 'Anonymize customer PII before syncing', FALSE, FALSE, 'false'),
('setting-sync-required', 'cloud.sync.required', 'false', 'SYNC', 'BOOLEAN', 'Require cloud connection to operate', FALSE, FALSE, 'false'),
('setting-sync-allow-offline', 'cloud.sync.allow.offline', 'true', 'SYNC', 'BOOLEAN', 'Allow offline operation when cloud unreachable', FALSE, FALSE, 'true'),

-- Storage Configuration
('setting-storage-type', 'storage.type', 'filesystem', 'STORAGE', 'STRING', 'File storage type (filesystem, s3, minio)', TRUE, FALSE, 'filesystem'),
('setting-storage-location', 'storage.location', '/app/data/uploads', 'STORAGE', 'STRING', 'Local storage directory path', TRUE, FALSE, '/app/data/uploads'),
('setting-storage-max-file-size', 'storage.max.file.size', '10485760', 'STORAGE', 'NUMBER', 'Maximum file upload size (bytes)', TRUE, FALSE, '10485760'),

-- Application Configuration
('setting-app-version', 'app.version', '1.0.0', 'SYSTEM', 'STRING', 'Application version', FALSE, FALSE, '1.0.0'),
('setting-app-environment', 'app.environment', 'standalone', 'SYSTEM', 'STRING', 'Application environment (standalone, cloud)', FALSE, FALSE, 'standalone'),
('setting-app-auth-mode', 'app.auth.mode', 'embedded', 'SYSTEM', 'STRING', 'Authentication mode (embedded, keycloak)', TRUE, FALSE, 'embedded'),

-- JWT Configuration
('setting-jwt-expiration', 'jwt.expiration.ms', '86400000', 'SECURITY', 'NUMBER', 'JWT access token expiration (ms)', TRUE, FALSE, '86400000'),
('setting-jwt-refresh-expiration', 'jwt.refresh.expiration.ms', '604800000', 'SECURITY', 'NUMBER', 'JWT refresh token expiration (ms)', TRUE, FALSE, '604800000'),

-- Database Configuration
('setting-db-backup-enabled', 'database.backup.enabled', 'true', 'DATABASE', 'BOOLEAN', 'Enable automatic database backups', FALSE, FALSE, 'true'),
('setting-db-backup-schedule', 'database.backup.schedule', '0 0 2 * * ?', 'DATABASE', 'STRING', 'Backup schedule (cron expression)', FALSE, FALSE, '0 0 2 * * ?'),
('setting-db-backup-retention-days', 'database.backup.retention.days', '30', 'DATABASE', 'NUMBER', 'Backup retention period (days)', FALSE, FALSE, '30');

-- Trigger to update updated_at timestamp
CREATE OR REPLACE FUNCTION update_system_settings_timestamp()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    NEW.version = OLD.version + 1;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER system_settings_update_timestamp
BEFORE UPDATE ON system_settings
FOR EACH ROW
EXECUTE FUNCTION update_system_settings_timestamp();

-- Comments
COMMENT ON TABLE system_settings IS 'System-wide configuration settings manageable through UI';
COMMENT ON COLUMN system_settings.setting_key IS 'Unique key identifier for the setting';
COMMENT ON COLUMN system_settings.setting_value IS 'Current value of the setting';
COMMENT ON COLUMN system_settings.setting_category IS 'Category grouping (SYSTEM, DOMAIN, SYNC, STORAGE, SECURITY, DATABASE)';
COMMENT ON COLUMN system_settings.data_type IS 'Data type for validation (STRING, NUMBER, BOOLEAN, JSON, ENCRYPTED)';
COMMENT ON COLUMN system_settings.requires_restart IS 'Whether changing this setting requires application restart';
COMMENT ON COLUMN system_settings.is_sensitive IS 'Whether this setting contains sensitive data (API keys, passwords)';
COMMENT ON COLUMN system_settings.default_value IS 'Default value to use if setting is not configured';
COMMENT ON COLUMN system_settings.validation_regex IS 'Optional regex pattern for validating setting value';
