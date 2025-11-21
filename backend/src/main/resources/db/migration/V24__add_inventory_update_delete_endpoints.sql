-- ========================================
-- Migration V24: Add Inventory Update/Delete API Endpoints
-- ========================================
-- Ensures INVENTORY_UPDATE and INVENTORY_DELETE permissions exist
-- These permissions control access to:
--   PUT /api/inventory/{inventoryId} - Update inventory metadata
--   DELETE /api/inventory/{inventoryId} - Delete inventory (zero stock only)

-- Verify INVENTORY_UPDATE permission exists (should already exist from V12)
INSERT INTO permissions (id, name, description, resource, action, created_at, updated_at, version)
SELECT
    'perm-inventory-update',
    'INVENTORY_UPDATE',
    'Update inventory records (metadata only, not stock quantities)',
    'INVENTORY',
    'UPDATE',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP,
    0
WHERE NOT EXISTS (SELECT 1 FROM permissions WHERE name = 'INVENTORY_UPDATE');

-- Verify INVENTORY_DELETE permission exists (should already exist from V12)
INSERT INTO permissions (id, name, description, resource, action, created_at, updated_at, version)
SELECT
    'perm-inventory-delete',
    'INVENTORY_DELETE',
    'Remove inventory records (only items with zero stock)',
    'INVENTORY',
    'DELETE',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP,
    0
WHERE NOT EXISTS (SELECT 1 FROM permissions WHERE name = 'INVENTORY_DELETE');

-- Ensure SYSTEM_ADMIN role has both permissions
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r
CROSS JOIN permissions p
WHERE r.name = 'SYSTEM_ADMIN'
  AND p.name IN ('INVENTORY_UPDATE', 'INVENTORY_DELETE')
  AND NOT EXISTS (
    SELECT 1 FROM role_permissions rp
    WHERE rp.role_id = r.id AND rp.permission_id = p.id
  );

-- Ensure TENANT_ADMIN role has both permissions
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r
CROSS JOIN permissions p
WHERE r.name = 'TENANT_ADMIN'
  AND p.name IN ('INVENTORY_UPDATE', 'INVENTORY_DELETE')
  AND NOT EXISTS (
    SELECT 1 FROM role_permissions rp
    WHERE rp.role_id = r.id AND rp.permission_id = p.id
  );

-- Ensure OWNER role has both permissions
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r
CROSS JOIN permissions p
WHERE r.name = 'OWNER'
  AND p.name IN ('INVENTORY_UPDATE', 'INVENTORY_DELETE')
  AND NOT EXISTS (
    SELECT 1 FROM role_permissions rp
    WHERE rp.role_id = r.id AND rp.permission_id = p.id
  );

-- Ensure MANAGER role has both permissions
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r
CROSS JOIN permissions p
WHERE r.name = 'MANAGER'
  AND p.name IN ('INVENTORY_UPDATE', 'INVENTORY_DELETE')
  AND NOT EXISTS (
    SELECT 1 FROM role_permissions rp
    WHERE rp.role_id = r.id AND rp.permission_id = p.id
  );

-- Add comments
COMMENT ON TABLE permissions IS 'System permissions - INVENTORY_UPDATE and INVENTORY_DELETE added for new inventory management endpoints';
