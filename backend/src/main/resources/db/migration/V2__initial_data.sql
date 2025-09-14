-- Insert default roles
INSERT INTO roles (id, name, description, is_system) VALUES
    ('550e8400-e29b-41d4-a716-446655440001', 'SUPER_ADMIN', 'System super administrator with full access', true),
    ('550e8400-e29b-41d4-a716-446655440002', 'SHOP_OWNER', 'Shop owner with full shop management access', true),
    ('550e8400-e29b-41d4-a716-446655440003', 'SHOP_MANAGER', 'Shop manager with operational access', true),
    ('550e8400-e29b-41d4-a716-446655440004', 'CASHIER', 'Cashier with sales and transaction access', true),
    ('550e8400-e29b-41d4-a716-446655440005', 'INVESTOR', 'Investor with investment and reporting access', true),
    ('550e8400-e29b-41d4-a716-446655440006', 'AUDITOR', 'Auditor with read-only access to all data', true);

-- Insert default permissions
INSERT INTO permissions (id, name, description, resource, action) VALUES
    -- Shop permissions
    ('660e8400-e29b-41d4-a716-446655440001', 'shop.create', 'Create new shops', 'shop', 'create'),
    ('660e8400-e29b-41d4-a716-446655440002', 'shop.read', 'View shop details', 'shop', 'read'),
    ('660e8400-e29b-41d4-a716-446655440003', 'shop.update', 'Update shop information', 'shop', 'update'),
    ('660e8400-e29b-41d4-a716-446655440004', 'shop.delete', 'Delete shops', 'shop', 'delete'),

    -- Product permissions
    ('660e8400-e29b-41d4-a716-446655440005', 'product.create', 'Create new products', 'product', 'create'),
    ('660e8400-e29b-41d4-a716-446655440006', 'product.read', 'View product details', 'product', 'read'),
    ('660e8400-e29b-41d4-a716-446655440007', 'product.update', 'Update product information', 'product', 'update'),
    ('660e8400-e29b-41d4-a716-446655440008', 'product.delete', 'Delete products', 'product', 'delete'),

    -- Sales permissions
    ('660e8400-e29b-41d4-a716-446655440009', 'sales.create', 'Create sales transactions', 'sales', 'create'),
    ('660e8400-e29b-41d4-a716-446655440010', 'sales.read', 'View sales transactions', 'sales', 'read'),
    ('660e8400-e29b-41d4-a716-446655440011', 'sales.void', 'Void sales transactions', 'sales', 'void'),
    ('660e8400-e29b-41d4-a716-446655440012', 'sales.refund', 'Process refunds', 'sales', 'refund'),

    -- Investment permissions
    ('660e8400-e29b-41d4-a716-446655440013', 'investment.create', 'Create new investments', 'investment', 'create'),
    ('660e8400-e29b-41d4-a716-446655440014', 'investment.read', 'View investment details', 'investment', 'read'),
    ('660e8400-e29b-41d4-a716-446655440015', 'investment.update', 'Update investment information', 'investment', 'update'),
    ('660e8400-e29b-41d4-a716-446655440016', 'investment.withdraw', 'Withdraw from investments', 'investment', 'withdraw'),

    -- User permissions
    ('660e8400-e29b-41d4-a716-446655440017', 'user.create', 'Create new users', 'user', 'create'),
    ('660e8400-e29b-41d4-a716-446655440018', 'user.read', 'View user details', 'user', 'read'),
    ('660e8400-e29b-41d4-a716-446655440019', 'user.update', 'Update user information', 'user', 'update'),
    ('660e8400-e29b-41d4-a716-446655440020', 'user.delete', 'Delete users', 'user', 'delete'),

    -- Report permissions
    ('660e8400-e29b-41d4-a716-446655440021', 'report.sales', 'View sales reports', 'report', 'sales'),
    ('660e8400-e29b-41d4-a716-446655440022', 'report.inventory', 'View inventory reports', 'report', 'inventory'),
    ('660e8400-e29b-41d4-a716-446655440023', 'report.financial', 'View financial reports', 'report', 'financial'),
    ('660e8400-e29b-41d4-a716-446655440024', 'report.audit', 'View audit reports', 'report', 'audit');

-- Assign permissions to roles
-- SUPER_ADMIN gets all permissions
INSERT INTO role_permissions (role_id, permission_id)
SELECT '550e8400-e29b-41d4-a716-446655440001', id FROM permissions;

-- SHOP_OWNER gets most permissions except system-level
INSERT INTO role_permissions (role_id, permission_id)
SELECT '550e8400-e29b-41d4-a716-446655440002', id FROM permissions
WHERE name NOT IN ('shop.create', 'shop.delete');

-- SHOP_MANAGER gets operational permissions
INSERT INTO role_permissions (role_id, permission_id)
SELECT '550e8400-e29b-41d4-a716-446655440003', id FROM permissions
WHERE name IN ('shop.read', 'product.create', 'product.read', 'product.update',
               'sales.create', 'sales.read', 'sales.void', 'user.read',
               'report.sales', 'report.inventory');

-- CASHIER gets sales-related permissions
INSERT INTO role_permissions (role_id, permission_id)
SELECT '550e8400-e29b-41d4-a716-446655440004', id FROM permissions
WHERE name IN ('product.read', 'sales.create', 'sales.read', 'report.sales');

-- INVESTOR gets investment and reporting permissions
INSERT INTO role_permissions (role_id, permission_id)
SELECT '550e8400-e29b-41d4-a716-446655440005', id FROM permissions
WHERE name IN ('investment.create', 'investment.read', 'investment.update',
               'investment.withdraw', 'report.financial', 'shop.read');

-- AUDITOR gets read-only permissions
INSERT INTO role_permissions (role_id, permission_id)
SELECT '550e8400-e29b-41d4-a716-446655440006', id FROM permissions
WHERE action = 'read' OR resource = 'report';

-- Insert default feature flags
INSERT INTO feature_flags (id, name, enabled, description) VALUES
    ('770e8400-e29b-41d4-a716-446655440001', 'investment.enabled', true, 'Enable investment module'),
    ('770e8400-e29b-41d4-a716-446655440002', 'analytics.enabled', true, 'Enable analytics module'),
    ('770e8400-e29b-41d4-a716-446655440003', 'fraud.enabled', false, 'Enable fraud detection module'),
    ('770e8400-e29b-41d4-a716-446655440004', 'multi_currency.enabled', false, 'Enable multi-currency support'),
    ('770e8400-e29b-41d4-a716-446655440005', 'loyalty.enabled', false, 'Enable customer loyalty program');

-- Insert sample categories
INSERT INTO categories (id, name, description, display_order) VALUES
    ('880e8400-e29b-41d4-a716-446655440001', 'Groceries', 'Food and household items', 1),
    ('880e8400-e29b-41d4-a716-446655440002', 'Electronics', 'Electronic devices and accessories', 2),
    ('880e8400-e29b-41d4-a716-446655440003', 'Clothing', 'Apparel and fashion items', 3),
    ('880e8400-e29b-41d4-a716-446655440004', 'Beverages', 'Drinks and beverages', 4),
    ('880e8400-e29b-41d4-a716-446655440005', 'Bakery', 'Fresh baked goods', 5);