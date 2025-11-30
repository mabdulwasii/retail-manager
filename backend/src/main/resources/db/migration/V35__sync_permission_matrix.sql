-- V35: Sync permissions with permission-matrix.csv
-- This migration ensures database permissions match the CSV source of truth

-- Remove SHOP_LIST permission from MANAGER role
DELETE FROM role_permissions rp
WHERE rp.permission_id = (SELECT id FROM permissions WHERE name = 'SHOP_LIST')
  AND rp.role_id = (SELECT id FROM roles WHERE name = 'MANAGER' AND is_system = true);

-- Ensure INVENTORY_RESERVE permission exists for TENANT_ADMIN role
INSERT INTO role_permissions (role_id, permission_id)
SELECT
    r.id,
    p.id
FROM roles r
CROSS JOIN permissions p
WHERE r.name = 'TENANT_ADMIN'
  AND r.is_system = true
  AND p.name = 'INVENTORY_RESERVE'
  AND NOT EXISTS (
      SELECT 1
      FROM role_permissions rp2
      WHERE rp2.role_id = r.id
        AND rp2.permission_id = p.id
  );

-- Log the changes (optional, for audit purposes)
-- This assumes you have an audit or changelog mechanism in place
