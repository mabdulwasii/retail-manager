-- ========================================
-- Migration V18: Fix shop opening_date column type
-- ========================================
-- The Shop entity uses LocalDateTime but the database column is DATE.
-- This causes constraint violations when setting opening_date.
--
-- Change: DATE -> TIMESTAMP
-- ========================================

ALTER TABLE shops ALTER COLUMN opening_date TYPE TIMESTAMP USING opening_date::TIMESTAMP;

COMMENT ON COLUMN shops.opening_date IS 'Shop opening date and time';
