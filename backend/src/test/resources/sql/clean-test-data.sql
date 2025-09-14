-- Clean up test data after integration tests
-- This script removes all test data inserted during integration tests

-- Clean up shop customizations first (foreign key dependency)
DELETE FROM shop_customizations WHERE id LIKE 'test-custom-%';

-- Clean up feature flags
DELETE FROM feature_flags WHERE id LIKE 'test-ff-%';

-- Clean up shop-user relationships if they exist
-- DELETE FROM shop_users WHERE shop_id LIKE 'test-shop-%';

-- Clean up test users if they exist
-- DELETE FROM users WHERE id LIKE 'test-user-%';

-- Clean up test shops
DELETE FROM shops WHERE id LIKE 'test-shop-%';

-- Reset sequences if needed (PostgreSQL specific)
-- This ensures clean state for subsequent tests
-- ALTER SEQUENCE IF EXISTS shops_id_seq RESTART WITH 1;
-- ALTER SEQUENCE IF EXISTS users_id_seq RESTART WITH 1;

-- Clean up any audit logs for test data
DELETE FROM audit_logs WHERE entity_id LIKE 'test-shop-%' OR entity_id LIKE 'test-user-%' OR entity_id LIKE 'test-custom-%' OR entity_id LIKE 'test-ff-%';

-- Clean up any event store data for test entities if applicable
-- DELETE FROM event_store WHERE aggregate_id LIKE 'test-shop-%' OR aggregate_id LIKE 'test-user-%';

-- Note: This cleanup script should be comprehensive enough to remove all traces of test data
-- while being safe to run multiple times (idempotent)