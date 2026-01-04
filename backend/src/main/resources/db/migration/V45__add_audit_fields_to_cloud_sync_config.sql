-- V45: Add missing audit fields to cloud_sync_config table
-- BaseEntity requires created_by, updated_by, and proper version type

-- Add created_by column
ALTER TABLE cloud_sync_config
ADD COLUMN IF NOT EXISTS created_by VARCHAR(255);

-- Add updated_by column
ALTER TABLE cloud_sync_config
ADD COLUMN IF NOT EXISTS updated_by VARCHAR(255);

-- Change version from INTEGER to BIGINT to match BaseEntity
ALTER TABLE cloud_sync_config
ALTER COLUMN version TYPE BIGINT;

-- Add comments
COMMENT ON COLUMN cloud_sync_config.created_by IS 'User ID who created this cloud sync configuration';
COMMENT ON COLUMN cloud_sync_config.updated_by IS 'User ID who last updated this cloud sync configuration';
