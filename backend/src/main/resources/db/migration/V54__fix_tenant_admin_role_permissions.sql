-- ============================================================================
-- V54: Fix TENANT_ADMIN Role Permissions (Embedded & Cloud Modes)
-- ============================================================================
-- Ensures TENANT_ADMIN role has ALL necessary permissions for full admin
-- functionality in both embedded mode (Docker Lite) and cloud mode (Kubernetes).
--
-- IMPORTANT: This migration is safe for both deployment modes:
-- - Embedded Mode: Fixes 403 errors for admin users
-- - Cloud Mode: Ensures TENANT_ADMIN has consistent permissions (Keycloak users)
-- - Uses idempotent INSERT with NOT EXISTS - safe to run multiple times
-- - Only ADDS permissions, never removes - backward compatible
--
-- Root Cause:
-- 1. V12 migration assigned permissions that existed at that time
-- 2. Later migrations (V19, V24, V30, V33, V51) added NEW permissions
-- 3. These new permissions were only assigned to specific roles, skipping TENANT_ADMIN
-- 4. Result: TENANT_ADMIN missing many permissions → 403 errors on multiple APIs
--
-- Examples of 403 errors:
-- - /api/roles (missing ROLE_LIST)
-- - /api/inventory (missing INVENTORY_*)
-- - /api/sales (missing SALES_*)
-- - /api/investments (missing INVESTMENT_*)
-- - /api/receipts (missing RECEIPT_*)
-- - /api/audit-logs (missing AUDIT_LOG_*)
--
-- Solution:
-- Re-run V12 logic: Assign ALL current permissions to TENANT_ADMIN
-- except SYSTEM_ADMIN, TENANT_DELETE, and TENANT_CREATE.
-- This catches all permissions added in V12-V53 and any future additions.
-- ============================================================================

-- ============================================================================
-- Assign ALL permissions to TENANT_ADMIN (except system-admin-only ones)
-- ============================================================================
-- This is the same logic as V12 but run again to catch permissions added
-- in migrations V13-V53 that weren't automatically assigned to TENANT_ADMIN.
--
-- Excluded permissions:
-- - SYSTEM_ADMIN: Reserved for system administrators only
-- - TENANT_DELETE: Tenant deletion is system-admin-only operation
-- - TENANT_CREATE: Tenant creation is system-admin-only operation
-- ============================================================================

INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r, permissions p
WHERE r.name = 'TENANT_ADMIN'
AND p.name NOT IN ('SYSTEM_ADMIN', 'TENANT_DELETE', 'TENANT_CREATE')
AND NOT EXISTS (
    SELECT 1 FROM role_permissions rp
    WHERE rp.role_id = r.id AND rp.permission_id = p.id
);

-- ============================================================================
-- Summary
-- ============================================================================
-- This migration gives TENANT_ADMIN access to ALL operational permissions:
-- - Role & Permission Management (ROLE_*, PERMISSION_*)
-- - User Management (USER_*)
-- - Tenant & Shop Management (TENANT_*, SHOP_*)
-- - Products & Categories (PRODUCT_*, CATEGORY_*)
-- - Inventory (INVENTORY_*)
-- - Sales & Receipts (SALES_*, RECEIPT_*)
-- - Financial (EXPENSE_*, INVESTMENT_*, RETURN_*)
-- - Audit & Analytics (AUDIT_LOG_*, ANALYTICS_*, FRAUD_*)
-- - Cloud & Feature Flags (CLOUD_*, FEATURE_FLAG_*)
--
-- After this migration, admin users in Docker Lite will have full access to
-- all tenant-level operations without 403 errors.
-- ============================================================================
