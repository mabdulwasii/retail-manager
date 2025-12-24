-- ========================================
-- Empty Test Data File
-- ========================================
-- This file is used by integration tests that manage their own test data.
-- It overrides AbstractIntegrationTest's default test-data.sql loading.
--
-- Tests using this file typically:
-- - Create test data in @BeforeEach methods
-- - Have full control over test data lifecycle
-- - Avoid conflicts with shared test-data.sql
--
-- Usage:
-- @Sql(scripts = "/test-data-empty.sql")
-- ========================================

-- This SELECT statement ensures the file is valid SQL and prevents parsing errors
SELECT 1;
