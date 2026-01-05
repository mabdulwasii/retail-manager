-- V51: Add cloud analytics permissions for cross-shop analytics aggregation
-- Adds CLOUD_ANALYTICS permissions for revenue, sales, products, performance, export, and platform overview

-- Insert new cloud analytics permissions
INSERT INTO permissions (id, name, description, resource, action, created_at, updated_at, version)
VALUES
    (gen_random_uuid()::text, 'CLOUD_ANALYTICS_REVENUE_VIEW', 'View cross-shop revenue analytics', 'CLOUD_ANALYTICS', 'REVENUE_VIEW', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    (gen_random_uuid()::text, 'CLOUD_ANALYTICS_SALES_VIEW', 'View cross-shop sales metrics', 'CLOUD_ANALYTICS', 'SALES_VIEW', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    (gen_random_uuid()::text, 'CLOUD_ANALYTICS_PRODUCTS_VIEW', 'View cross-shop top products', 'CLOUD_ANALYTICS', 'PRODUCTS_VIEW', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    (gen_random_uuid()::text, 'CLOUD_ANALYTICS_PERFORMANCE_VIEW', 'View shop performance comparison', 'CLOUD_ANALYTICS', 'PERFORMANCE_VIEW', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    (gen_random_uuid()::text, 'CLOUD_ANALYTICS_EXPORT', 'Export analytics to CSV', 'CLOUD_ANALYTICS', 'EXPORT', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    (gen_random_uuid()::text, 'CLOUD_ANALYTICS_PLATFORM_VIEW', 'View platform-wide overview (admin only)', 'CLOUD_ANALYTICS', 'PLATFORM_VIEW', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0)
ON CONFLICT (name) DO NOTHING;

-- Assign cloud analytics permissions to roles based on permission-matrix.csv
-- SYSTEM_ADMIN: All cloud analytics permissions
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r
CROSS JOIN permissions p
WHERE r.name = 'SYSTEM_ADMIN'
  AND p.name IN (
    'CLOUD_ANALYTICS_REVENUE_VIEW',
    'CLOUD_ANALYTICS_SALES_VIEW',
    'CLOUD_ANALYTICS_PRODUCTS_VIEW',
    'CLOUD_ANALYTICS_PERFORMANCE_VIEW',
    'CLOUD_ANALYTICS_EXPORT',
    'CLOUD_ANALYTICS_PLATFORM_VIEW'
  )
ON CONFLICT (role_id, permission_id) DO NOTHING;

-- TENANT_ADMIN: All except platform view
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r
CROSS JOIN permissions p
WHERE r.name = 'TENANT_ADMIN'
  AND p.name IN (
    'CLOUD_ANALYTICS_REVENUE_VIEW',
    'CLOUD_ANALYTICS_SALES_VIEW',
    'CLOUD_ANALYTICS_PRODUCTS_VIEW',
    'CLOUD_ANALYTICS_PERFORMANCE_VIEW',
    'CLOUD_ANALYTICS_EXPORT'
  )
ON CONFLICT (role_id, permission_id) DO NOTHING;

-- OWNER: View and export permissions
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r
CROSS JOIN permissions p
WHERE r.name = 'OWNER'
  AND p.name IN (
    'CLOUD_ANALYTICS_REVENUE_VIEW',
    'CLOUD_ANALYTICS_SALES_VIEW',
    'CLOUD_ANALYTICS_PRODUCTS_VIEW',
    'CLOUD_ANALYTICS_PERFORMANCE_VIEW',
    'CLOUD_ANALYTICS_EXPORT'
  )
ON CONFLICT (role_id, permission_id) DO NOTHING;

-- MANAGER: View permissions only
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r
CROSS JOIN permissions p
WHERE r.name = 'MANAGER'
  AND p.name IN (
    'CLOUD_ANALYTICS_REVENUE_VIEW',
    'CLOUD_ANALYTICS_SALES_VIEW',
    'CLOUD_ANALYTICS_PRODUCTS_VIEW',
    'CLOUD_ANALYTICS_PERFORMANCE_VIEW'
  )
ON CONFLICT (role_id, permission_id) DO NOTHING;
