# Role & Permission Management Guide

## Overview

The Shop Manager platform provides a comprehensive Role-Based Access Control (RBAC) system with granular permission management. This guide covers how to create custom roles, assign permissions, and manage user access.

## Table of Contents

- [Core Concepts](#core-concepts)
- [System Roles](#system-roles)
- [Permission Categories](#permission-categories)
- [API Reference](#api-reference)
- [Use Cases](#use-cases)
- [Best Practices](#best-practices)

---

## Core Concepts

### Roles

A **Role** is a collection of permissions that can be assigned to users. Roles define what actions users can perform within the system.

**Types of Roles:**
- **System Roles**: Predefined roles (`SYSTEM_ADMIN`, `OWNER`, `MANAGER`, `EMPLOYEE`, `INVESTOR`) that cannot be modified or deleted
- **Custom Roles**: User-defined roles with specific permission combinations

**Role Properties:**
- `id`: Unique identifier (UUID)
- `name`: Unique role name (e.g., "CUSTOM_SUPERVISOR")
- `description`: Human-readable description
- `isSystem`: Boolean flag indicating if role is a system role
- `permissions`: List of permission names assigned to the role

### Permissions

**Permissions** are atomic privileges that grant access to specific operations. They follow the naming pattern: `{RESOURCE}_{ACTION}`

**Examples:**
- `PRODUCT_CREATE` - Create new products
- `SALES_READ` - View sales transaction details
- `INVENTORY_UPDATE` - Update inventory records

### Permission Groups

Permissions are organized by resource category for easier management:
- **PRODUCT**: Product catalog management
- **SALES**: Sales transaction operations
- **INVENTORY**: Stock and inventory management
- **USER**: User account management
- **ROLE**: Role and permission management
- **SHOP**: Shop configuration
- **TENANT**: Multi-tenant administration (System Admin only)
- **INVESTMENT**: Investment tracking
- **EXPENSE**: Expense management
- **ANALYTICS**: Reporting and analytics
- **AUDIT_LOG**: Audit trail access
- **FRAUD**: Fraud detection management

---

## System Roles

### SYSTEM_ADMIN
- **Purpose**: Platform-wide administration across all tenants
- **Permissions**: All 94 permissions
- **Use Cases**: Platform operators, DevOps teams

### OWNER
- **Purpose**: Tenant-wide administrative access
- **Permissions**: 77 permissions (excludes `SYSTEM_ADMIN`, `TENANT_*`)
- **Use Cases**: Business owners, organization administrators

### MANAGER
- **Purpose**: Shop-level management access
- **Permissions**: 62 permissions
- **Use Cases**: Store managers, department heads
- **Notable Exclusions**: Cannot delete shops, users, or products permanently

### EMPLOYEE
- **Purpose**: Operational access for daily tasks
- **Permissions**: 26 permissions (mostly read + sales operations)
- **Use Cases**: Sales associates, cashiers, warehouse staff
- **Notable Exclusions**: Cannot create/modify products, cannot manage users

### INVESTOR
- **Purpose**: Read-only investment analytics access
- **Permissions**: 3 permissions (`INVESTMENT_READ`, `INVESTMENT_LIST`, `ANALYTICS_INVESTMENT_VIEW`)
- **Use Cases**: Financial stakeholders, external investors

---

## Permission Categories

### Product Catalog
```
PRODUCT_CREATE, PRODUCT_READ, PRODUCT_LIST, PRODUCT_UPDATE, PRODUCT_DELETE
CATEGORY_CREATE, CATEGORY_READ, CATEGORY_LIST, CATEGORY_UPDATE, CATEGORY_DELETE
```

### Inventory Management
```
INVENTORY_CREATE, INVENTORY_READ, INVENTORY_LIST, INVENTORY_UPDATE, INVENTORY_DELETE
INVENTORY_ADJUST, INVENTORY_RESERVE, INVENTORY_HISTORY, INVENTORY_FORECAST
```

### Sales & Receipts
```
SALES_CREATE, SALES_READ, SALES_LIST, SALES_UPDATE, SALES_DELETE, SALES_VOID
RECEIPT_CREATE, RECEIPT_READ, RECEIPT_LIST, RECEIPT_SEND, RECEIPT_EMAIL
```

### User & Role Management
```
USER_CREATE, USER_READ, USER_LIST, USER_UPDATE, USER_DELETE
ROLE_CREATE, ROLE_READ, ROLE_LIST, ROLE_UPDATE, ROLE_DELETE, ROLE_ASSIGN
ROLE_PERMISSION_ADD, ROLE_PERMISSION_REMOVE
PERMISSION_READ, PERMISSION_LIST
```

### Investment Management
```
INVESTMENT_CREATE, INVESTMENT_READ, INVESTMENT_LIST, INVESTMENT_UPDATE,
INVESTMENT_DELETE, INVESTMENT_CLOSE, INVESTMENT_PROFIT_DISTRIBUTE
```

### Analytics & Audit
```
ANALYTICS_SALES_VIEW, ANALYTICS_INVESTMENT_VIEW, ANALYTICS_MANAGE
AUDIT_LOG_VIEW_SHOP, AUDIT_LOG_VIEW_TENANT
```

### Fraud Detection
```
FRAUD_VIEW, FRAUD_MANAGE, FRAUD_LIST, FRAUD_INVESTIGATE,
FRAUD_RESOLVE, FRAUD_DETECT
```

**For complete permission matrix, see**: [permission-matrix.adoc](../backend/src/docs/asciidoc/permission-matrix.adoc)

---

## API Reference

### Base URL
```
/api
```

### Authentication
All endpoints require JWT authentication via Keycloak. Include the token in the `Authorization` header:
```
Authorization: Bearer <jwt-token>
```

---

### List All Permissions

**Endpoint:** `GET /api/permissions`

**Required Permission:** `PERMISSION_LIST`

**Response:**
```json
[
  {
    "id": "perm-001",
    "name": "PRODUCT_CREATE",
    "description": "Create new products"
  },
  {
    "id": "perm-002",
    "name": "PRODUCT_READ",
    "description": "View product details"
  }
]
```

**Example:**
```bash
curl -X GET https://api.shopmanager.com/api/permissions \
  -H "Authorization: Bearer ${TOKEN}"
```

---

### Get Grouped Permissions

**Endpoint:** `GET /api/permissions/grouped`

**Required Permission:** `PERMISSION_LIST`

**Response:**
```json
{
  "PRODUCT": [
    "PRODUCT_CREATE",
    "PRODUCT_READ",
    "PRODUCT_LIST",
    "PRODUCT_UPDATE",
    "PRODUCT_DELETE"
  ],
  "SALES": [
    "SALES_CREATE",
    "SALES_READ",
    "SALES_LIST"
  ],
  "INVENTORY": [...]
}
```

**Use Case:** Building UI dropdowns for role permission selection

---

### List All Roles

**Endpoint:** `GET /api/roles`

**Required Permission:** `ROLE_LIST`

**Response:**
```json
[
  {
    "id": "role-001",
    "name": "MANAGER",
    "description": "Shop-level management access",
    "isSystem": true,
    "permissions": ["PRODUCT_CREATE", "SALES_READ", ...]
  }
]
```

---

### Get Role by ID

**Endpoint:** `GET /api/roles/{roleId}`

**Required Permission:** `ROLE_READ`

**Response:**
```json
{
  "id": "role-001",
  "name": "MANAGER",
  "description": "Shop-level management access",
  "isSystem": true,
  "permissions": ["PRODUCT_CREATE", "SALES_READ", "INVENTORY_LIST"]
}
```

---

### Create Custom Role

**Endpoint:** `POST /api/roles`

**Required Permission:** `ROLE_CREATE`

**Request Body:**
```json
{
  "name": "CUSTOM_SUPERVISOR",
  "description": "Supervisor with custom permissions",
  "permissionNames": [
    "PRODUCT_READ",
    "PRODUCT_LIST",
    "SALES_CREATE",
    "INVENTORY_READ",
    "INVENTORY_LIST"
  ]
}
```

**Response:** `201 Created`
```json
{
  "id": "custom-role-001",
  "name": "CUSTOM_SUPERVISOR",
  "description": "Supervisor with custom permissions",
  "isSystem": false,
  "permissions": ["PRODUCT_READ", "PRODUCT_LIST", "SALES_CREATE", "INVENTORY_READ", "INVENTORY_LIST"]
}
```

**Validation Rules:**
- Role name must be unique
- Role name cannot be empty
- Permission names must exist in the system
- Cannot create system roles via API

**Example:**
```bash
curl -X POST https://api.shopmanager.com/api/roles \
  -H "Authorization: Bearer ${TOKEN}" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "CUSTOM_SUPERVISOR",
    "description": "Supervisor with custom permissions",
    "permissionNames": ["PRODUCT_READ", "SALES_CREATE"]
  }'
```

---

### Update Custom Role

**Endpoint:** `PUT /api/roles/{roleId}`

**Required Permission:** `ROLE_UPDATE`

**Request Body:**
```json
{
  "description": "Updated description",
  "permissionNames": [
    "PRODUCT_READ",
    "PRODUCT_LIST",
    "SALES_CREATE"
  ]
}
```

**Response:** `200 OK`
```json
{
  "id": "custom-role-001",
  "name": "CUSTOM_SUPERVISOR",
  "description": "Updated description",
  "isSystem": false,
  "permissions": ["PRODUCT_READ", "PRODUCT_LIST", "SALES_CREATE"]
}
```

**Constraints:**
- Cannot update system roles
- Cannot change role name
- Returns `400 Bad Request` if attempting to modify system role

---

### Delete Custom Role

**Endpoint:** `DELETE /api/roles/{roleId}`

**Required Permission:** `ROLE_DELETE`

**Response:** `204 No Content`

**Constraints:**
- Cannot delete system roles
- Cannot delete roles assigned to users
- Returns `400 Bad Request` if constraints violated

**Example:**
```bash
curl -X DELETE https://api.shopmanager.com/api/roles/custom-role-001 \
  -H "Authorization: Bearer ${TOKEN}"
```

---

### Add Permission to Role

**Endpoint:** `POST /api/roles/{roleId}/permissions/{permissionName}`

**Required Permission:** `ROLE_PERMISSION_ADD`

**Response:** `204 No Content`

**Constraints:**
- Cannot modify system roles
- Permission must exist in the system
- Idempotent (adding existing permission returns 204)

**Example:**
```bash
curl -X POST https://api.shopmanager.com/api/roles/custom-role-001/permissions/SALES_VOID \
  -H "Authorization: Bearer ${TOKEN}"
```

---

### Remove Permission from Role

**Endpoint:** `DELETE /api/roles/{roleId}/permissions/{permissionName}`

**Required Permission:** `ROLE_PERMISSION_REMOVE`

**Response:** `204 No Content`

**Constraints:**
- Cannot modify system roles
- Idempotent (removing non-existent permission returns 204)

**Example:**
```bash
curl -X DELETE https://api.shopmanager.com/api/roles/custom-role-001/permissions/SALES_VOID \
  -H "Authorization: Bearer ${TOKEN}"
```

---

### Bulk Update Role Permissions

**Endpoint:** `PUT /api/roles/{roleId}/permissions`

**Required Permission:** `ROLE_PERMISSION_ADD` and `ROLE_PERMISSION_REMOVE`

**Request Body:**
```json
{
  "permissionNames": [
    "INVENTORY_READ",
    "INVENTORY_LIST",
    "EXPENSE_READ"
  ]
}
```

**Response:** `200 OK`
```json
{
  "id": "custom-role-001",
  "name": "CUSTOM_SUPERVISOR",
  "description": "Supervisor with custom permissions",
  "isSystem": false,
  "permissions": ["INVENTORY_READ", "INVENTORY_LIST", "EXPENSE_READ"]
}
```

**Behavior:**
- Replaces all existing permissions with the provided list
- More efficient than multiple add/remove calls
- Atomic operation (all or nothing)

---

### Assign Role to User

**Endpoint:** `POST /api/users/{userId}/roles`

**Required Permission:** `ROLE_ASSIGN`

**Request Body:**
```json
{
  "roleName": "CUSTOM_SUPERVISOR"
}
```

**Response:** `204 No Content`

**Notes:**
- Users can have multiple roles (permissions are union of all roles)
- Idempotent operation

---

### Remove Role from User

**Endpoint:** `DELETE /api/users/{userId}/roles/{roleId}`

**Required Permission:** `ROLE_ASSIGN`

**Response:** `204 No Content`

---

### Get User Roles

**Endpoint:** `GET /api/users/{userId}/roles`

**Required Permission:** `ROLE_LIST`

**Response:**
```json
[
  {
    "id": "role-001",
    "name": "MANAGER",
    "description": "Shop-level management access",
    "isSystem": true,
    "permissions": [...]
  },
  {
    "id": "custom-role-001",
    "name": "CUSTOM_SUPERVISOR",
    "description": "Custom supervisor role",
    "isSystem": false,
    "permissions": [...]
  }
]
```

---

## Use Cases

### Use Case 1: Creating a "Warehouse Supervisor" Role

**Scenario:** Your business needs a role for warehouse supervisors who can manage inventory but not process sales.

**Steps:**

1. **List available permissions** to understand what's available:
```bash
curl -X GET https://api.shopmanager.com/api/permissions/grouped \
  -H "Authorization: Bearer ${TOKEN}"
```

2. **Create the custom role** with appropriate permissions:
```bash
curl -X POST https://api.shopmanager.com/api/roles \
  -H "Authorization: Bearer ${TOKEN}" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "WAREHOUSE_SUPERVISOR",
    "description": "Manages warehouse operations and inventory",
    "permissionNames": [
      "PRODUCT_READ",
      "PRODUCT_LIST",
      "INVENTORY_CREATE",
      "INVENTORY_READ",
      "INVENTORY_LIST",
      "INVENTORY_UPDATE",
      "INVENTORY_ADJUST",
      "INVENTORY_HISTORY"
    ]
  }'
```

3. **Assign the role to users:**
```bash
curl -X POST https://api.shopmanager.com/api/users/user-123/roles \
  -H "Authorization: Bearer ${TOKEN}" \
  -H "Content-Type: application/json" \
  -d '{"roleName": "WAREHOUSE_SUPERVISOR"}'
```

---

### Use Case 2: Creating a "Sales Analyst" Role

**Scenario:** You need a read-only role for analysts who review sales data but cannot make changes.

**Steps:**

```bash
curl -X POST https://api.shopmanager.com/api/roles \
  -H "Authorization: Bearer ${TOKEN}" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "SALES_ANALYST",
    "description": "Read-only access to sales and analytics data",
    "permissionNames": [
      "SALES_READ",
      "SALES_LIST",
      "RECEIPT_READ",
      "RECEIPT_LIST",
      "ANALYTICS_SALES_VIEW",
      "PRODUCT_READ",
      "PRODUCT_LIST"
    ]
  }'
```

---

### Use Case 3: Temporarily Elevating User Permissions

**Scenario:** A regular employee needs temporary access to process returns during a busy period.

**Steps:**

1. **Create a temporary role:**
```bash
curl -X POST https://api.shopmanager.com/api/roles \
  -H "Authorization: Bearer ${TOKEN}" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "TEMP_RETURNS_HANDLER",
    "description": "Temporary role for processing returns",
    "permissionNames": [
      "RETURN_CREATE",
      "RETURN_READ",
      "RETURN_LIST",
      "RETURN_APPROVE",
      "SALES_READ"
    ]
  }'
```

2. **Assign to user temporarily:**
```bash
curl -X POST https://api.shopmanager.com/api/users/employee-456/roles \
  -H "Authorization: Bearer ${TOKEN}" \
  -H "Content-Type: application/json" \
  -d '{"roleName": "TEMP_RETURNS_HANDLER"}'
```

3. **Remove role after period:**
```bash
curl -X DELETE https://api.shopmanager.com/api/users/employee-456/roles/temp-role-id \
  -H "Authorization: Bearer ${TOKEN}"
```

4. **Delete the temporary role:**
```bash
curl -X DELETE https://api.shopmanager.com/api/roles/temp-role-id \
  -H "Authorization: Bearer ${TOKEN}"
```

---

### Use Case 4: Modifying an Existing Custom Role

**Scenario:** You need to add expense tracking permissions to the "WAREHOUSE_SUPERVISOR" role.

**Steps:**

1. **Get current role details:**
```bash
curl -X GET https://api.shopmanager.com/api/roles \
  -H "Authorization: Bearer ${TOKEN}" \
  | jq '.[] | select(.name == "WAREHOUSE_SUPERVISOR")'
```

2. **Add new permissions individually:**
```bash
curl -X POST https://api.shopmanager.com/api/roles/warehouse-role-id/permissions/EXPENSE_CREATE \
  -H "Authorization: Bearer ${TOKEN}"

curl -X POST https://api.shopmanager.com/api/roles/warehouse-role-id/permissions/EXPENSE_READ \
  -H "Authorization: Bearer ${TOKEN}"
```

**OR** use bulk update:
```bash
curl -X PUT https://api.shopmanager.com/api/roles/warehouse-role-id/permissions \
  -H "Authorization: Bearer ${TOKEN}" \
  -H "Content-Type: application/json" \
  -d '{
    "permissionNames": [
      "PRODUCT_READ",
      "PRODUCT_LIST",
      "INVENTORY_CREATE",
      "INVENTORY_READ",
      "INVENTORY_LIST",
      "INVENTORY_UPDATE",
      "INVENTORY_ADJUST",
      "INVENTORY_HISTORY",
      "EXPENSE_CREATE",
      "EXPENSE_READ"
    ]
  }'
```

---

## Best Practices

### 1. Principle of Least Privilege
- **Always** start with minimal permissions and add more as needed
- Avoid granting `SYSTEM_ADMIN` role unless absolutely necessary
- Use read-only permissions (`*_READ`, `*_LIST`) for non-critical users

### 2. Role Naming Conventions
- Use `UPPER_SNAKE_CASE` for consistency with system roles
- Make names descriptive: `WAREHOUSE_SUPERVISOR` not `WH_SUP`
- Prefix temporary roles: `TEMP_RETURNS_HANDLER`

### 3. Permission Grouping
- Group related permissions together
- Example: For a cashier role, include `SALES_CREATE` + `RECEIPT_CREATE` + `PRODUCT_READ`
- Don't mix incompatible permissions (e.g., `PRODUCT_DELETE` + `EMPLOYEE` role)

### 4. Audit and Review
- Periodically review custom roles and their assignments
- Remove unused roles to reduce complexity
- Check audit logs for unauthorized access attempts:
  ```bash
  GET /api/audit-logs?action=ROLE_UPDATED&userId=user-123
  ```

### 5. Multi-Role Strategy
- Assign multiple roles to users instead of creating overly broad roles
- Example: Assign both `EMPLOYEE` and `CUSTOM_SUPERVISOR` instead of creating `SUPER_EMPLOYEE`
- Effective permissions = union of all assigned roles

### 6. Testing
- Always test new roles in a development environment first
- Use integration tests to verify permission checks
- Validate that system roles cannot be modified via API

### 7. Documentation
- Document the purpose of each custom role
- Maintain a mapping of roles to business functions
- Keep permission descriptions up-to-date

### 8. Security Considerations
- **Never** expose role/permission management endpoints to end users
- Require re-authentication for sensitive operations
- Monitor for privilege escalation attempts
- Use strong authentication (MFA) for users with `ROLE_CREATE` permission

### 9. Error Handling
- Handle `403 Forbidden` responses gracefully in UI
- Provide clear error messages when role modifications fail
- Log all permission-related errors for security auditing

### 10. Performance
- Cache user permissions in the JWT token (already implemented)
- Avoid excessive permission checks in hot paths
- Use grouped permissions endpoint for UI dropdowns instead of individual calls

---

## Common Errors

### 400 Bad Request - Duplicate Role Name
```json
{
  "error": "Role with name 'MANAGER' already exists"
}
```
**Solution:** Choose a unique role name

### 400 Bad Request - System Role Modification
```json
{
  "error": "Cannot modify system role"
}
```
**Solution:** Only custom roles (isSystem=false) can be modified

### 400 Bad Request - Role Assigned to Users
```json
{
  "error": "Cannot delete role assigned to users"
}
```
**Solution:** Remove role from all users first, then delete

### 403 Forbidden - Insufficient Permissions
```json
{
  "error": "User does not have required permission: ROLE_CREATE"
}
```
**Solution:** User needs `ROLE_CREATE` permission to create roles

### 404 Not Found - Role Not Found
```json
{
  "error": "Role with id 'invalid-id' not found"
}
```
**Solution:** Verify role ID is correct

---

## Database Schema

Roles and permissions are stored in the following tables:

### `roles` Table
```sql
CREATE TABLE roles (
    id VARCHAR(255) PRIMARY KEY,
    name VARCHAR(100) UNIQUE NOT NULL,
    description TEXT,
    is_system BOOLEAN DEFAULT FALSE,
    tenant_id VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

### `permissions` Table
```sql
CREATE TABLE permissions (
    id VARCHAR(255) PRIMARY KEY,
    name VARCHAR(100) UNIQUE NOT NULL,
    description TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

### `role_permissions` Table (Join)
```sql
CREATE TABLE role_permissions (
    role_id VARCHAR(255) REFERENCES roles(id) ON DELETE CASCADE,
    permission_id VARCHAR(255) REFERENCES permissions(id) ON DELETE CASCADE,
    PRIMARY KEY (role_id, permission_id)
);
```

### `user_roles` Table (Join)
```sql
CREATE TABLE user_roles (
    user_id VARCHAR(255) REFERENCES users(id) ON DELETE CASCADE,
    role_id VARCHAR(255) REFERENCES roles(id) ON DELETE CASCADE,
    assigned_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (user_id, role_id)
);
```

---

## Related Documentation

- [Permission Matrix](../backend/src/docs/asciidoc/permission-matrix.adoc) - Complete permission reference
- [Testing Guide](../TESTING-GUIDE.md) - Testing with different roles
- [Security Best Practices](../README.md#security-guidelines)
- [API Documentation](../README.md#api-reference)

---

## Support

For questions or issues:
- Check existing GitHub issues
- Review audit logs for access-related errors
- Consult permission matrix for correct permission names

---

**Version:** 1.0
**Last Updated:** 2025-01-04
**Contributors:** Shop Manager Development Team
