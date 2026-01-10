-- ============================================================================
-- V53: Add missing created_by column to system_settings table
-- ============================================================================
-- Fix for missing audit field that caused schema validation failure
-- ============================================================================

-- Add created_by column (inherited from BaseEntity)
ALTER TABLE system_settings
ADD COLUMN IF NOT EXISTS created_by VARCHAR(36);

-- Update existing rows to set created_by to updated_by if available
UPDATE system_settings
SET created_by = updated_by
WHERE created_by IS NULL AND updated_by IS NOT NULL;

-- Comment
COMMENT ON COLUMN system_settings.created_by IS 'User ID who created this setting (for audit trail)';
