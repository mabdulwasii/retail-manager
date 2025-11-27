-- Migration V34: Remove duplicate INVENTORY_HISTORY_VIEW permission
-- Description: Consolidate duplicate inventory history permissions by removing INVENTORY_HISTORY_VIEW
-- The INVENTORY_HISTORY permission is currently used in controllers and serves the same purpose
-- Author: Claude Code
-- Date: 2025-11-28

-- Remove role assignments for INVENTORY_HISTORY_VIEW permission
DELETE FROM role_permissions
WHERE permission_id IN (
    SELECT id FROM permissions WHERE name = 'INVENTORY_HISTORY_VIEW'
);

-- Remove the INVENTORY_HISTORY_VIEW permission
DELETE FROM permissions
WHERE name = 'INVENTORY_HISTORY_VIEW';
