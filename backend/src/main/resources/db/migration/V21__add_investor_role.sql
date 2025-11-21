-- Add INVESTOR role
-- Migration: V21
-- Date: 2025-11-07
-- Description: Create INVESTOR role for investors to manage their investments

-- Create INVESTOR role if it doesn't exist
INSERT INTO roles (id, name, description, is_system, created_at, updated_at, version)
SELECT 'investor-role-id', 'INVESTOR', 'Investor with investment management access', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0
WHERE NOT EXISTS (SELECT 1 FROM roles WHERE name = 'INVESTOR');

-- Grant investment permissions to INVESTOR role
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r
CROSS JOIN permissions p
WHERE r.name = 'INVESTOR'
  AND p.name IN ('INVESTMENT_CREATE', 'INVESTMENT_READ', 'INVESTMENT_LIST', 'ANALYTICS_INVESTMENT_VIEW')
  AND NOT EXISTS (
      SELECT 1
      FROM role_permissions rp
      WHERE rp.role_id = r.id
        AND rp.permission_id = p.id
  );
