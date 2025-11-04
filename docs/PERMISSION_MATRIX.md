# Permission Matrix

**Version**: 1.1.0
**Last Updated**: 2025-11-04
**Migration**: V19

This document provides a comprehensive overview of all permissions, resources, and role assignments in the Shop Manager system.

---

## Table of Contents
1. [Overview](#overview)
2. [Permission Naming Convention](#permission-naming-convention)
3. [Complete Permission Matrix](#complete-permission-matrix)
4. [Role Definitions](#role-definitions)
5. [API Endpoint Permissions](#api-endpoint-permissions)
6. [Changelog](#changelog)

---

## Overview

The Shop Manager system uses **granular permission-based access control (PBAC)** instead of role-based access control (RBAC). This provides fine-grained control over who can perform specific actions on resources.

### Key Concepts

- **Resource**: An entity in the system (e.g., PRODUCT, SHOP, USER)
- **Action**: An operation that can be performed (e.g., CREATE, READ, UPDATE, DELETE)
- **Permission**: Combination of Resource + Action (e.g., `PRODUCT_CREATE`, `SHOP_UPDATE`)
- **Role**: A collection of permissions assigned to users

---

## Permission Naming Convention

Permissions follow the pattern: `{RESOURCE}_{ACTION}`

### Standard Actions (CRUD)
- `CREATE` - Create new resources
- `READ` - View a single resource by ID
- `LIST` - List/search multiple resources
- `UPDATE` - Modify existing resources
- `DELETE` - Remove resources

### Special Actions
- `VIEW` - Read-only access (used for scoped viewing)
- `SEND` - Send/transmit resources (e.g., email receipts)
- `VIEW_SHOP` - View at shop level
- `VIEW_TENANT` - View at tenant level
- `MANAGE` - Combined permission for full resource management
- `ADMIN` - Full administrative access

---

## Complete Permission Matrix

### Legend
- ✅ = Permission granted
- ❌ = Permission denied
- 🔒 = Shop-scoped (user can only access their assigned shop)
- 🏢 = Tenant-scoped (user can access all shops in their tenant)

---

### System & Core Resources

| Permission | Resource | Action | SYSTEM_ADMIN | TENANT_ADMIN | OWNER | MANAGER | EMPLOYEE | INVESTOR |
|-----------|----------|--------|--------------|--------------|-------|---------|----------|----------|
| SYSTEM_ADMIN | SYSTEM | ADMIN | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ |
| TENANT_CREATE | TENANT | CREATE | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ |
| TENANT_READ | TENANT | READ | ✅ | ✅ 🏢 | ✅ 🏢 | ❌ | ❌ | ❌ |
| TENANT_LIST | TENANT | LIST | ✅ | ✅ 🏢 | ✅ 🏢 | ❌ | ❌ | ❌ |
| TENANT_UPDATE | TENANT | UPDATE | ✅ | ✅ 🏢 | ❌ | ❌ | ❌ | ❌ |
| TENANT_DELETE | TENANT | DELETE | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ |

---

### Shop Resources

| Permission | Resource | Action | SYSTEM_ADMIN | TENANT_ADMIN | OWNER | MANAGER | EMPLOYEE | INVESTOR |
|-----------|----------|--------|--------------|--------------|-------|---------|----------|----------|
| SHOP_CREATE | SHOP | CREATE | ✅ | ✅ 🏢 | ❌ | ❌ | ❌ | ❌ |
| SHOP_READ | SHOP | READ | ✅ | ✅ 🏢 | ✅ 🏢 | ✅ 🔒 | ✅ 🔒 | ❌ |
| SHOP_LIST | SHOP | LIST | ✅ | ✅ 🏢 | ✅ 🏢 | ✅ 🔒 | ❌ | ❌ |
| SHOP_UPDATE | SHOP | UPDATE | ✅ | ✅ 🏢 | ✅ 🏢 | ❌ | ❌ | ❌ |
| SHOP_DELETE | SHOP | DELETE | ✅ | ✅ 🏢 | ❌ | ❌ | ❌ | ❌ |

---

### User & Role Management

| Permission | Resource | Action | SYSTEM_ADMIN | TENANT_ADMIN | OWNER | MANAGER | EMPLOYEE | INVESTOR |
|-----------|----------|--------|--------------|--------------|-------|---------|----------|----------|
| USER_CREATE | USER | CREATE | ✅ | ✅ 🏢 | ✅ 🏢 | ❌ | ❌ | ❌ |
| USER_READ | USER | READ | ✅ | ✅ 🏢 | ✅ 🏢 | ✅ 🔒 | ❌ | ❌ |
| USER_LIST | USER | LIST | ✅ | ✅ 🏢 | ✅ 🏢 | ✅ 🔒 | ❌ | ❌ |
| USER_UPDATE | USER | UPDATE | ✅ | ✅ 🏢 | ✅ 🏢 | ❌ | ❌ | ❌ |
| USER_DELETE | USER | DELETE | ✅ | ✅ 🏢 | ✅ 🏢 | ❌ | ❌ | ❌ |
| ROLE_CREATE | ROLE | CREATE | ✅ | ✅ 🏢 | ✅ | ❌ | ❌ | ❌ |
| ROLE_READ | ROLE | READ | ✅ | ✅ 🏢 | ✅ 🏢 | ✅ | ❌ | ❌ |
| ROLE_LIST | ROLE | LIST | ✅ | ✅ 🏢 | ✅ 🏢 | ✅ | ❌ | ❌ |
| ROLE_UPDATE | ROLE | UPDATE | ✅ | ✅ 🏢 | ✅ | ❌ | ❌ | ❌ |
| ROLE_DELETE | ROLE | DELETE | ✅ | ✅ 🏢 | ✅ | ❌ | ❌ | ❌ |
| ROLE_ASSIGN | ROLE | ASSIGN | ✅ | ✅ 🏢 | ✅ | ❌ | ❌ | ❌ |
| ROLE_PERMISSION_ADD | ROLE | PERMISSION_ADD | ✅ | ✅ 🏢 | ✅ | ❌ | ❌ | ❌ |
| ROLE_PERMISSION_REMOVE | ROLE | PERMISSION_REMOVE | ✅ | ✅ 🏢 | ✅ | ❌ | ❌ | ❌ |
| PERMISSION_READ | PERMISSION | READ | ✅ | ✅ | ✅ | ❌ | ❌ | ❌ |
| PERMISSION_LIST | PERMISSION | LIST | ✅ | ✅ | ✅ | ❌ | ❌ | ❌ |

---

### Product & Inventory

| Permission | Resource | Action | SYSTEM_ADMIN | TENANT_ADMIN | OWNER | MANAGER | EMPLOYEE | INVESTOR |
|-----------|----------|--------|--------------|--------------|-------|---------|----------|----------|
| PRODUCT_CREATE | PRODUCT | CREATE | ✅ | ✅ 🏢 | ✅ 🏢 | ✅ 🔒 | ❌ | ❌ |
| PRODUCT_READ | PRODUCT | READ | ✅ | ✅ 🏢 | ✅ 🏢 | ✅ 🔒 | ✅ 🔒 | ❌ |
| PRODUCT_LIST | PRODUCT | LIST | ✅ | ✅ 🏢 | ✅ 🏢 | ✅ 🔒 | ✅ 🔒 | ❌ |
| PRODUCT_UPDATE | PRODUCT | UPDATE | ✅ | ✅ 🏢 | ✅ 🏢 | ✅ 🔒 | ❌ | ❌ |
| PRODUCT_DELETE | PRODUCT | DELETE | ✅ | ✅ 🏢 | ✅ 🏢 | ✅ 🔒 | ❌ | ❌ |
| CATEGORY_CREATE | CATEGORY | CREATE | ✅ | ✅ 🏢 | ✅ 🏢 | ✅ 🔒 | ❌ | ❌ |
| CATEGORY_READ | CATEGORY | READ | ✅ | ✅ 🏢 | ✅ 🏢 | ✅ 🔒 | ✅ 🔒 | ❌ |
| CATEGORY_LIST | CATEGORY | LIST | ✅ | ✅ 🏢 | ✅ 🏢 | ✅ 🔒 | ✅ 🔒 | ❌ |
| CATEGORY_UPDATE | CATEGORY | UPDATE | ✅ | ✅ 🏢 | ✅ 🏢 | ✅ 🔒 | ❌ | ❌ |
| CATEGORY_DELETE | CATEGORY | DELETE | ✅ | ✅ 🏢 | ✅ 🏢 | ✅ 🔒 | ❌ | ❌ |
| INVENTORY_CREATE | INVENTORY | CREATE | ✅ | ✅ 🏢 | ✅ 🏢 | ✅ 🔒 | ❌ | ❌ |
| INVENTORY_READ | INVENTORY | READ | ✅ | ✅ 🏢 | ✅ 🏢 | ✅ 🔒 | ✅ 🔒 | ❌ |
| INVENTORY_LIST | INVENTORY | LIST | ✅ | ✅ 🏢 | ✅ 🏢 | ✅ 🔒 | ✅ 🔒 | ❌ |
| INVENTORY_UPDATE | INVENTORY | UPDATE | ✅ | ✅ 🏢 | ✅ 🏢 | ✅ 🔒 | ❌ | ❌ |
| INVENTORY_DELETE | INVENTORY | DELETE | ✅ | ✅ 🏢 | ✅ 🏢 | ❌ | ❌ | ❌ |
| INVENTORY_HISTORY_VIEW | INVENTORY | HISTORY_VIEW | ✅ | ✅ 🏢 | ✅ 🏢 | ✅ 🔒 | ❌ | ❌ |

---

### Sales & Receipts

| Permission | Resource | Action | SYSTEM_ADMIN | TENANT_ADMIN | OWNER | MANAGER | EMPLOYEE | INVESTOR |
|-----------|----------|--------|--------------|--------------|-------|---------|----------|----------|
| SALES_CREATE | SALES | CREATE | ✅ | ✅ 🏢 | ✅ 🏢 | ✅ 🔒 | ✅ 🔒 | ❌ |
| SALES_READ | SALES | READ | ✅ | ✅ 🏢 | ✅ 🏢 | ✅ 🔒 | ✅ 🔒 | ✅ 🔒 |
| SALES_LIST | SALES | LIST | ✅ | ✅ 🏢 | ✅ 🏢 | ✅ 🔒 | ✅ 🔒 | ✅ 🔒 |
| SALES_UPDATE | SALES | UPDATE | ✅ | ✅ 🏢 | ✅ 🏢 | ✅ 🔒 | ❌ | ❌ |
| SALES_DELETE | SALES | DELETE | ✅ | ✅ 🏢 | ✅ 🏢 | ❌ | ❌ | ❌ |
| RECEIPT_CREATE | RECEIPT | CREATE | ✅ | ✅ 🏢 | ✅ 🏢 | ✅ 🔒 | ✅ 🔒 | ❌ |
| RECEIPT_READ | RECEIPT | READ | ✅ | ✅ 🏢 | ✅ 🏢 | ✅ 🔒 | ✅ 🔒 | ❌ |
| RECEIPT_LIST | RECEIPT | LIST | ✅ | ✅ 🏢 | ✅ 🏢 | ✅ 🔒 | ✅ 🔒 | ❌ |
| RECEIPT_SEND | RECEIPT | SEND | ✅ | ✅ 🏢 | ✅ 🏢 | ✅ 🔒 | ✅ 🔒 | ❌ |

---

### Financial Resources

| Permission | Resource | Action | SYSTEM_ADMIN | TENANT_ADMIN | OWNER | MANAGER | EMPLOYEE | INVESTOR |
|-----------|----------|--------|--------------|--------------|-------|---------|----------|----------|
| EXPENSE_CREATE | EXPENSE | CREATE | ✅ | ✅ 🏢 | ✅ 🏢 | ✅ 🔒 | ❌ | ❌ |
| EXPENSE_READ | EXPENSE | READ | ✅ | ✅ 🏢 | ✅ 🏢 | ✅ 🔒 | ❌ | ❌ |
| EXPENSE_LIST | EXPENSE | LIST | ✅ | ✅ 🏢 | ✅ 🏢 | ✅ 🔒 | ❌ | ❌ |
| EXPENSE_UPDATE | EXPENSE | UPDATE | ✅ | ✅ 🏢 | ✅ 🏢 | ✅ 🔒 | ❌ | ❌ |
| EXPENSE_DELETE | EXPENSE | DELETE | ✅ | ✅ 🏢 | ✅ 🏢 | ❌ | ❌ | ❌ |
| EXPENSE_CATEGORY_CREATE | EXPENSE_CATEGORY | CREATE | ✅ | ✅ 🏢 | ✅ 🏢 | ✅ 🏢 | ❌ | ❌ |
| EXPENSE_CATEGORY_READ | EXPENSE_CATEGORY | READ | ✅ | ✅ 🏢 | ✅ 🏢 | ✅ 🏢 | ❌ | ❌ |
| EXPENSE_CATEGORY_LIST | EXPENSE_CATEGORY | LIST | ✅ | ✅ 🏢 | ✅ 🏢 | ✅ 🏢 | ❌ | ❌ |
| EXPENSE_CATEGORY_UPDATE | EXPENSE_CATEGORY | UPDATE | ✅ | ✅ 🏢 | ✅ 🏢 | ✅ 🏢 | ❌ | ❌ |
| EXPENSE_CATEGORY_DELETE | EXPENSE_CATEGORY | DELETE | ✅ | ✅ 🏢 | ✅ 🏢 | ✅ 🏢 | ❌ | ❌ |
| INVESTMENT_CREATE | INVESTMENT | CREATE | ✅ | ✅ 🏢 | ✅ 🏢 | ❌ | ❌ | ❌ |
| INVESTMENT_READ | INVESTMENT | READ | ✅ | ✅ 🏢 | ✅ 🏢 | ❌ | ❌ | ✅ (own) |
| INVESTMENT_LIST | INVESTMENT | LIST | ✅ | ✅ 🏢 | ✅ 🏢 | ❌ | ❌ | ✅ (own) |
| INVESTMENT_UPDATE | INVESTMENT | UPDATE | ✅ | ✅ 🏢 | ✅ 🏢 | ❌ | ❌ | ❌ |
| INVESTMENT_DELETE | INVESTMENT | DELETE | ✅ | ✅ 🏢 | ✅ 🏢 | ❌ | ❌ | ❌ |
| RETURN_CREATE | RETURN | CREATE | ✅ | ✅ 🏢 | ✅ 🏢 | ✅ 🔒 | ✅ 🔒 | ❌ |
| RETURN_READ | RETURN | READ | ✅ | ✅ 🏢 | ✅ 🏢 | ✅ 🔒 | ✅ 🔒 | ❌ |
| RETURN_LIST | RETURN | LIST | ✅ | ✅ 🏢 | ✅ 🏢 | ✅ 🔒 | ✅ 🔒 | ❌ |
| RETURN_UPDATE | RETURN | UPDATE | ✅ | ✅ 🏢 | ✅ 🏢 | ✅ 🔒 | ❌ | ❌ |
| RETURN_DELETE | RETURN | DELETE | ✅ | ✅ 🏢 | ✅ 🏢 | ❌ | ❌ | ❌ |

---

### Audit & Analytics

| Permission | Resource | Action | SYSTEM_ADMIN | TENANT_ADMIN | OWNER | MANAGER | EMPLOYEE | INVESTOR |
|-----------|----------|--------|--------------|--------------|-------|---------|----------|----------|
| AUDIT_LOG_VIEW_SHOP | AUDIT_LOG | VIEW_SHOP | ✅ | ✅ 🏢 | ✅ 🏢 | ✅ 🔒 | ❌ | ❌ |
| AUDIT_LOG_VIEW_TENANT | AUDIT_LOG | VIEW_TENANT | ✅ | ✅ 🏢 | ✅ 🏢 | ❌ | ❌ | ❌ |
| ANALYTICS_VIEW_SHOP | ANALYTICS | VIEW_SHOP | ✅ | ✅ 🏢 | ✅ 🏢 | ✅ 🔒 | ❌ | ❌ |
| ANALYTICS_VIEW_TENANT | ANALYTICS | VIEW_TENANT | ✅ | ✅ 🏢 | ✅ 🏢 | ❌ | ❌ | ❌ |
| FRAUD_VIEW | FRAUD | VIEW | ✅ | ✅ 🏢 | ✅ 🏢 | ✅ 🔒 | ❌ | ❌ |
| FRAUD_MANAGE | FRAUD | MANAGE | ✅ | ✅ 🏢 | ✅ 🏢 | ❌ | ❌ | ❌ |

---

## Role Definitions

### SYSTEM_ADMIN
- **Scope**: Global
- **Description**: Full system administrator with unrestricted access
- **Permissions**: ALL

### TENANT_ADMIN
- **Scope**: Tenant-wide (all shops in tenant)
- **Description**: Administrator for an entire tenant organization
- **Permissions**: All except SYSTEM_ADMIN and TENANT_DELETE
- **Key Capabilities**:
  - Create/manage shops
  - Create/manage users
  - Create/manage custom roles
  - Full access to all shops in tenant

### OWNER
- **Scope**: Tenant-wide (all shops they own)
- **Description**: Business owner with full operational access
- **Permissions**: All operational permissions except role management
- **Key Capabilities**:
  - Manage shop settings
  - Create/manage users
  - Full product, inventory, sales access
  - View tenant-level analytics and audit logs

### MANAGER
- **Scope**: Shop-level (assigned shop only)
- **Description**: Shop manager with operational permissions
- **Permissions**: Shop operations, product management, sales, expenses
- **Key Capabilities**:
  - Manage products and inventory for their shop
  - Process sales and returns
  - Create expense categories (tenant-wide)
  - View shop-level audit logs and analytics

### EMPLOYEE (CASHIER)
- **Scope**: Shop-level (assigned shop only)
- **Description**: Shop employee with transaction permissions
- **Permissions**: Sales, receipts, returns (read/create only)
- **Key Capabilities**:
  - Process sales transactions
  - Generate and send receipts
  - Process returns
  - View products and inventory

### INVESTOR
- **Scope**: Limited
- **Description**: Investor with read-only access to investments
- **Permissions**: View own investments and related sales data
- **Key Capabilities**:
  - View own investment records
  - View sales data for profit calculations

---

## API Endpoint Permissions

### Product API (`/api/shops/{shopId}/products`)
| Endpoint | Method | Permission Required |
|----------|--------|-------------------|
| Create Product | POST | `PRODUCT_CREATE` |
| List Products | GET | `PRODUCT_LIST` |
| Get Product | GET | `PRODUCT_READ` |
| Update Product | PUT | `PRODUCT_UPDATE` |
| Delete Product | DELETE | `PRODUCT_DELETE` |

### Category API (`/api/shops/{shopId}/categories`)
| Endpoint | Method | Permission Required |
|----------|--------|-------------------|
| Create Category | POST | `CATEGORY_CREATE` |
| List Categories | GET | `CATEGORY_LIST` |
| Get Category | GET | `CATEGORY_READ` |
| Update Category | PUT | `CATEGORY_UPDATE` |
| Delete Category | DELETE | `CATEGORY_DELETE` |

### Expense Category API (`/api/tenants/{tenantId}/expense-categories`)
| Endpoint | Method | Permission Required |
|----------|--------|-------------------|
| Create Expense Category | POST | `EXPENSE_CATEGORY_CREATE` |
| List Expense Categories | GET | `EXPENSE_CATEGORY_LIST` |
| Get Expense Category | GET | `EXPENSE_CATEGORY_READ` |
| Update Expense Category | PUT | `EXPENSE_CATEGORY_UPDATE` |
| Delete Expense Category | DELETE | `EXPENSE_CATEGORY_DELETE` |

### Audit Log API
| Endpoint | Method | Permission Required |
|----------|--------|-------------------|
| Shop-level Audit Logs | GET `/api/shops/{shopId}/audit-logs` | `AUDIT_LOG_VIEW_SHOP` |
| Tenant-level Audit Logs | GET `/api/tenants/{tenantId}/audit-logs` | `AUDIT_LOG_VIEW_TENANT` |

### Inventory History API
| Endpoint | Method | Permission Required |
|----------|--------|-------------------|
| Shop Inventory History | GET `/api/shops/{shopId}/inventory-history` | `INVENTORY_HISTORY_VIEW` |
| Product Inventory History | GET `/api/products/{productId}/inventory-history` | `INVENTORY_HISTORY_VIEW` |

### Role & Permission API
| Endpoint | Method | Permission Required |
|----------|--------|-------------------|
| Create Role | POST `/api/roles` | `ROLE_CREATE` |
| List Roles | GET `/api/roles` | `ROLE_LIST` |
| Get Role by ID | GET `/api/roles/{roleId}` | `ROLE_READ` |
| Update Role | PUT `/api/roles/{roleId}` | `ROLE_UPDATE` |
| Delete Role | DELETE `/api/roles/{roleId}` | `ROLE_DELETE` |
| Assign Role to User | POST `/api/users/{userId}/roles` | `ROLE_ASSIGN` |
| Remove Role from User | DELETE `/api/users/{userId}/roles/{roleId}` | `ROLE_ASSIGN` |
| Get User Roles | GET `/api/users/{userId}/roles` | `ROLE_LIST` |
| Add Permission to Role | POST `/api/roles/{roleId}/permissions/{permissionId}` | `ROLE_PERMISSION_ADD` |
| Remove Permission from Role | DELETE `/api/roles/{roleId}/permissions/{permissionId}` | `ROLE_PERMISSION_REMOVE` |
| Bulk Update Role Permissions | PUT `/api/roles/{roleId}/permissions` | `ROLE_UPDATE` |
| List Permissions | GET `/api/permissions` | `PERMISSION_LIST` |
| List Permissions Grouped | GET `/api/permissions/grouped` | `PERMISSION_LIST` |

---

## Changelog

### Version 1.1.0 (2025-11-04) - Migration V19
- Added granular role permission management permissions
- `ROLE_PERMISSION_ADD` - Add individual permissions to roles
- `ROLE_PERMISSION_REMOVE` - Remove individual permissions from roles
- Enhanced RoleController with complete CRUD operations
- Created PermissionController for permission listing and grouping
- Added new API endpoints:
  - POST `/api/roles/{roleId}/permissions/{permissionId}`
  - DELETE `/api/roles/{roleId}/permissions/{permissionId}`
  - GET `/api/permissions/grouped`

### Version 1.0.0 (2025-10-30) - Migration V12
- Initial granular permission system
- Migrated from role-based to permission-based authorization
- Added ~80 granular permissions across all resources
- Implemented CRUD permissions for: PRODUCT, CATEGORY, INVENTORY, SALES, EXPENSE, INVESTMENT, RETURN, USER, ROLE
- Added scoped audit log viewing (shop vs tenant level)
- Added expense category management permissions
- Refactored all controllers to use `@PreAuthorize` with permissions instead of roles

---

## Notes

1. **Permission Inheritance**: Higher-level roles (e.g., TENANT_ADMIN) inherit all permissions from lower-level roles
2. **Scope Enforcement**: Shop-scoped permissions are enforced at the service layer via validation
3. **System Roles**: Roles with `isSystem=true` cannot be modified or deleted
4. **Dynamic Roles**: Tenant admins can create custom roles with any combination of permissions
5. **Migration Path**: Existing role-based checks are being migrated to permission-based checks in controllers

---

**For questions or updates to this matrix, please contact the development team.**
