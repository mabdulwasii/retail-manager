-- ========================================
-- Migration: V5 - Fix Expense Module shop_id Type
-- ========================================
-- Description: Change expense tables shop_id from UUID to VARCHAR(36)
--              to align with shops.id and maintain consistency across all modules
-- Author: System
-- Date: 2025-10-16
-- ========================================

-- Change expense_categories.shop_id from UUID to VARCHAR(36)
ALTER TABLE expense_categories ALTER COLUMN shop_id TYPE VARCHAR(36);

-- Change expenses.shop_id from UUID to VARCHAR(36)
ALTER TABLE expenses ALTER COLUMN shop_id TYPE VARCHAR(36);

-- Note: This migration aligns expense module with the rest of the codebase
-- where shop IDs are stored as VARCHAR(36) strings (e.g., 'default-shop-id')
