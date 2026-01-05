-- =====================================================
-- Migration: V50 - Fix version column types to BIGINT
-- Description: Change version column from INTEGER to BIGINT to match Java Long type in BaseEntity
-- Author: Shop Manager Development Team
-- Date: 2026-01-05
-- =====================================================

-- Fix version column in billing_invoices table
ALTER TABLE billing_invoices ALTER COLUMN version TYPE BIGINT;

-- Fix version column in cloud_subscriptions table
ALTER TABLE cloud_subscriptions ALTER COLUMN version TYPE BIGINT;

-- Fix version column in cloud_api_keys table
ALTER TABLE cloud_api_keys ALTER COLUMN version TYPE BIGINT;

-- Fix version column in cloud_sync_config table
ALTER TABLE cloud_sync_config ALTER COLUMN version TYPE BIGINT;

-- Add comments for documentation
COMMENT ON COLUMN billing_invoices.version IS 'Optimistic locking version (BIGINT to match Java Long)';
COMMENT ON COLUMN cloud_subscriptions.version IS 'Optimistic locking version (BIGINT to match Java Long)';
COMMENT ON COLUMN cloud_api_keys.version IS 'Optimistic locking version (BIGINT to match Java Long)';
COMMENT ON COLUMN cloud_sync_config.version IS 'Optimistic locking version (BIGINT to match Java Long)';
