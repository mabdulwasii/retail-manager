-- ========================================
-- Migration: V31 - Fix Shop Required Fields
-- ========================================
-- Purpose: Add default values for address and phone_number to shops
--          that are missing these required fields (added in entity validation)
--
-- Background:
-- The Shop entity has @NotEmpty validation for address and phoneNumber fields,
-- but the V2 migration created the default shop without these fields.
-- This migration ensures all existing shops have valid values.
--
-- Author: Claude Code
-- Date: 2025-01-19
-- ========================================

-- Fix default shop with proper address and phone number
UPDATE shops
SET
    address = '123 Main Street, Default City',
    phone_number = '+1-555-0100',
    updated_at = CURRENT_TIMESTAMP
WHERE id = 'default-shop-id'
  AND (address IS NULL OR address = '' OR phone_number IS NULL OR phone_number = '');

-- Fix any other shops with missing address
UPDATE shops
SET
    address = COALESCE(NULLIF(TRIM(address), ''), 'Address Not Provided'),
    updated_at = CURRENT_TIMESTAMP
WHERE address IS NULL OR TRIM(address) = '';

-- Fix any other shops with missing phone_number
UPDATE shops
SET
    phone_number = COALESCE(NULLIF(TRIM(phone_number), ''), '+1-000-0000'),
    updated_at = CURRENT_TIMESTAMP
WHERE phone_number IS NULL OR TRIM(phone_number) = '';

-- Verify all shops now have required fields
DO $$
DECLARE
    missing_count INTEGER;
BEGIN
    SELECT COUNT(*) INTO missing_count
    FROM shops
    WHERE address IS NULL OR address = '' OR phone_number IS NULL OR phone_number = '';

    IF missing_count > 0 THEN
        RAISE EXCEPTION 'Migration failed: % shops still have missing address or phone_number', missing_count;
    END IF;

    RAISE NOTICE 'Migration successful: All shops now have address and phone_number';
END $$;
