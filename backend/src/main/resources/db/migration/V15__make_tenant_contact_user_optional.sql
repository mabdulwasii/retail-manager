-- ========================================
-- Migration V15: Make tenant contact_user_id optional
-- ========================================
-- This migration makes the contact_user_id column nullable in the tenants table.
-- Contact user can be set later after tenant registration is approved.
--
-- Reason: During tenant registration workflow, the tenant is created first (INACTIVE status)
-- and the contact user is assigned later when the registration is approved.

-- Drop the NOT NULL constraint if it exists
ALTER TABLE tenants ALTER COLUMN contact_user_id DROP NOT NULL;

-- Add comment explaining the column
COMMENT ON COLUMN tenants.contact_user_id IS 'Primary contact user for the tenant (optional, can be set during approval)';
