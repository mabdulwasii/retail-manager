-- ========================================
-- Migration V23: Add Audit Fields to Investment Rounds
-- ========================================
-- Add created_by and updated_by audit fields to investment_rounds table
-- These fields are part of BaseEntity but were missing from the initial V22 migration

ALTER TABLE investment_rounds
    ADD COLUMN IF NOT EXISTS created_by VARCHAR(255),
    ADD COLUMN IF NOT EXISTS updated_by VARCHAR(255);

-- Add comments
COMMENT ON COLUMN investment_rounds.created_by IS 'User who created this investment round';
COMMENT ON COLUMN investment_rounds.updated_by IS 'User who last updated this investment round';
