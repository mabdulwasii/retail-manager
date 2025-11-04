-- ========================================
-- Migration V17: Drop is_investor column from users table
-- ========================================
-- This migration removes the redundant is_investor flag from users.
-- Investor status can be derived from:
-- - User roles (if INVESTOR role exists)
-- - investor_shares table (users with investment records)
--
-- Rationale: Avoid data duplication and maintain single source of truth

-- Drop the column if it exists
ALTER TABLE users DROP COLUMN IF EXISTS is_investor;

-- Add comment explaining the change
COMMENT ON TABLE users IS 'User entity - investor status derived from roles or investor_shares table, not stored as flag';
