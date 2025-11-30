# Permission Matrix

This document describes the complete permission structure for Shop Manager, including role-based access control (RBAC) for all system operations.

## Overview

Shop Manager uses **granular permission-based authorization** rather than simple role checks. This allows for flexible permission assignment and follows the principle of least privilege.

### Key Concepts

- **Permission**: A specific action that can be performed (e.g., `PRODUCT_CREATE`, `SALES_READ`)
- **Role**: A collection of permissions assigned to users (e.g., `OWNER`, `MANAGER`, `EMPLOYEE`)
- **Resource**: The entity being accessed (e.g., `PRODUCT`, `SALES`, `INVESTMENT`)

## Roles

### SYSTEM_ADMIN
- Full system access across all tenants
- Can create and manage tenants
- Has all permissions globally

### TENANT_ADMIN
- Administrative access within a single tenant
- Can manage shops, users, and configuration
- Same permissions as OWNER for shop operations
- Cannot create/delete tenants

### OWNER
- Full control within assigned shops
- Can manage all shop operations, users, and financial data
- Cannot access system-level or other tenant's data

### MANAGER
- Operational management within shops
- Can manage products, inventory, sales, and expenses
- Cannot manage investments or critical financial operations
- Limited user management (create/read/update, no delete)

### EMPLOYEE
- Day-to-day operations
- Can process sales, manage inventory, create expenses
- Read-only access to products and categories
- No access to financial reports or investments

### INVESTOR
- View-only access to investments and analytics
- Can view their own investment performance
- No operational permissions

## Permission Matrix

The complete permission matrix is maintained in `src/main/resources/permission-matrix.csv`.

### Legend
- ✓ = Has permission
- (blank) = No permission

---

## Investment Permissions

Investment operations are restricted to leadership roles only.

| Permission | Description | SYSTEM_ADMIN | TENANT_ADMIN | OWNER | MANAGER | EMPLOYEE | INVESTOR |
|-----------|-------------|--------------|--------------|-------|---------|----------|----------|
| **INVESTMENT_CREATE** | Create investment rounds | ✓ | ✓ | ✓ | | | |
| **INVESTMENT_READ** | View investment details | ✓ | ✓ | ✓ | ✓ | | ✓ |
| **INVESTMENT_LIST** | List and search investments | ✓ | ✓ | ✓ | ✓ | | ✓ |
| **INVESTMENT_UPDATE** | Update investment rounds | ✓ | ✓ | ✓ | | | |
| **INVESTMENT_DELETE** | Delete investment records | ✓ | ✓ | ✓ | | | |
| **INVESTMENT_CLOSE** | Close investment rounds | ✓ | ✓ | ✓ | | | |
| **INVESTMENT_PROFIT_DISTRIBUTE** | Distribute profits | ✓ | ✓ | ✓ | | | |

**Key Points**:
- Only **SYSTEM_ADMIN**, **TENANT_ADMIN**, and **OWNER** can create/manage investments
- **MANAGER** and **INVESTOR** have **view-only** access
- **EMPLOYEE** has **no** investment access
- This prevents unauthorized creation of financial obligations

---

## Product & Inventory Permissions

| Permission | Description | SYSTEM_ADMIN | TENANT_ADMIN | OWNER | MANAGER | EMPLOYEE | INVESTOR |
|-----------|-------------|--------------|--------------|-------|---------|----------|----------|
| **PRODUCT_CREATE** | Create products | ✓ | ✓ | ✓ | ✓ | | |
| **PRODUCT_READ** | View product details | ✓ | ✓ | ✓ | ✓ | ✓ | |
| **PRODUCT_LIST** | List products | ✓ | ✓ | ✓ | ✓ | ✓ | |
| **PRODUCT_UPDATE** | Edit products | ✓ | ✓ | ✓ | ✓ | | |
| **PRODUCT_DELETE** | Delete products | ✓ | ✓ | ✓ | | | |
| **CATEGORY_CREATE** | Create categories | ✓ | ✓ | ✓ | ✓ | | |
| **CATEGORY_READ** | View categories | ✓ | ✓ | ✓ | ✓ | ✓ | |
| **CATEGORY_LIST** | List categories | ✓ | ✓ | ✓ | ✓ | ✓ | |
| **CATEGORY_UPDATE** | Update categories | ✓ | ✓ | ✓ | ✓ | | |
| **CATEGORY_DELETE** | Delete categories | ✓ | ✓ | ✓ | | | |
| **INVENTORY_CREATE** | Add stock | ✓ | ✓ | ✓ | ✓ | ✓ | |
| **INVENTORY_READ** | View inventory | ✓ | ✓ | ✓ | ✓ | ✓ | |
| **INVENTORY_LIST** | List inventory | ✓ | ✓ | ✓ | ✓ | ✓ | |
| **INVENTORY_UPDATE** | Update inventory | ✓ | ✓ | ✓ | ✓ | | |
| **INVENTORY_DELETE** | Delete inventory | ✓ | ✓ | ✓ | ✓ | | |
| **INVENTORY_ADJUST** | Adjust stock levels | ✓ | ✓ | ✓ | ✓ | | |

---

## Sales & Financial Permissions

| Permission | Description | SYSTEM_ADMIN | TENANT_ADMIN | OWNER | MANAGER | EMPLOYEE | INVESTOR |
|-----------|-------------|--------------|--------------|-------|---------|----------|----------|
| **SALES_CREATE** | Process sales | ✓ | ✓ | ✓ | ✓ | ✓ | |
| **SALES_READ** | View sale details | ✓ | ✓ | ✓ | ✓ | ✓ | |
| **SALES_LIST** | List sales | ✓ | ✓ | ✓ | ✓ | ✓ | |
| **SALES_UPDATE** | Update sales | ✓ | ✓ | ✓ | ✓ | | |
| **SALES_DELETE** | Delete sales | ✓ | ✓ | ✓ | | | |
| **SALES_VOID** | Void transactions | ✓ | ✓ | ✓ | ✓ | | |
| **EXPENSE_CREATE** | Create expenses | ✓ | ✓ | ✓ | ✓ | ✓ | |
| **EXPENSE_READ** | View expenses | ✓ | ✓ | ✓ | ✓ | ✓ | |
| **EXPENSE_LIST** | List expenses | ✓ | ✓ | ✓ | ✓ | ✓ | |
| **EXPENSE_UPDATE** | Update expenses | ✓ | ✓ | ✓ | ✓ | | |
| **EXPENSE_DELETE** | Delete expenses | ✓ | ✓ | ✓ | | | |
| **EXPENSE_APPROVE** | Approve expenses | ✓ | ✓ | ✓ | ✓ | | |

---

## User & Role Management

| Permission | Description | SYSTEM_ADMIN | TENANT_ADMIN | OWNER | MANAGER | EMPLOYEE | INVESTOR |
|-----------|-------------|--------------|--------------|-------|---------|----------|----------|
| **USER_CREATE** | Create users | ✓ | ✓ | ✓ | ✓ | | |
| **USER_READ** | View user details | ✓ | ✓ | ✓ | ✓ | | |
| **USER_LIST** | List users (all scopes: system, tenant, shop) | ✓ | ✓ | ✓ | ✓ | | |
| **USER_UPDATE** | Update users | ✓ | ✓ | ✓ | ✓ | | |
| **USER_DELETE** | Delete users | ✓ | ✓ | ✓ | | | |
| **ROLE_CREATE** | Create custom roles | ✓ | ✓ | ✓ | | | |
| **ROLE_READ** | View roles | ✓ | ✓ | ✓ | ✓ | | |
| **ROLE_LIST** | List roles | ✓ | ✓ | ✓ | ✓ | | |
| **ROLE_UPDATE** | Update roles | ✓ | ✓ | ✓ | | | |
| **ROLE_DELETE** | Delete roles | ✓ | ✓ | ✓ | | | |
| **ROLE_ASSIGN** | Assign roles to users | ✓ | ✓ | ✓ | | | |
| **ROLE_PERMISSION_ADD** | Add permissions to roles | ✓ | ✓ | ✓ | | | |
| **ROLE_PERMISSION_REMOVE** | Remove permissions from roles | ✓ | ✓ | ✓ | | | |

**User Listing Endpoints** (added in V25):
- `GET /api/users?status={status}` - List all users system-wide (SYSTEM_ADMIN only)
- `GET /api/tenants/{tenantId}/users` - List all users in a tenant (USER_LIST permission)
- `GET /api/shops/{shopId}/users?status={status}` - List all users in a shop (USER_LIST permission)

All endpoints support optional status filtering: `ACTIVE`, `INACTIVE`, `PENDING`.

---

## Shop & Tenant Management

| Permission | Description | SYSTEM_ADMIN | TENANT_ADMIN | OWNER | MANAGER | EMPLOYEE | INVESTOR |
|-----------|-------------|--------------|--------------|-------|---------|----------|----------|
| **TENANT_CREATE** | Create tenants | ✓ | | | | | |
| **TENANT_READ** | View tenant details | ✓ | ✓ | ✓ | | | |
| **TENANT_LIST** | List all tenants | ✓ | | | | | |
| **TENANT_UPDATE** | Update tenants | ✓ | | | | | |
| **TENANT_DELETE** | Delete tenants | ✓ | | | | | |
| **SHOP_CREATE** | Create shops | ✓ | ✓ | ✓ | | | |
| **SHOP_READ** | View shop details | ✓ | ✓ | ✓ | ✓ | | |
| **SHOP_LIST** | List shops | ✓ | ✓ | ✓ | | | |
| **SHOP_UPDATE** | Update shops | ✓ | ✓ | ✓ | | | |
| **SHOP_DELETE** | Delete shops | ✓ | ✓ | ✓ | | | |

---

## Analytics & Reporting

| Permission | Description | SYSTEM_ADMIN | TENANT_ADMIN | OWNER | MANAGER | EMPLOYEE | INVESTOR |
|-----------|-------------|--------------|--------------|-------|---------|----------|----------|
| **ANALYTICS_SALES_VIEW** | View sales analytics | ✓ | ✓ | ✓ | ✓ | | |
| **ANALYTICS_INVESTMENT_VIEW** | View investment ROI | ✓ | ✓ | ✓ | ✓ | | ✓ |
| **ANALYTICS_MANAGE** | Manage analytics cache | ✓ | ✓ | ✓ | | | |
| **AUDIT_LOG_VIEW_SHOP** | View shop audit logs | ✓ | ✓ | ✓ | ✓ | | |
| **AUDIT_LOG_VIEW_TENANT** | View tenant audit logs | ✓ | ✓ | ✓ | | | |

---

## Fraud Detection

| Permission | Description | SYSTEM_ADMIN | TENANT_ADMIN | OWNER | MANAGER | EMPLOYEE | INVESTOR |
|-----------|-------------|--------------|--------------|-------|---------|----------|----------|
| **FRAUD_VIEW** | View fraud alerts | ✓ | ✓ | ✓ | ✓ | | |
| **FRAUD_LIST** | List fraud assessments | ✓ | ✓ | ✓ | ✓ | | |
| **FRAUD_INVESTIGATE** | Investigate alerts | ✓ | ✓ | ✓ | ✓ | | |
| **FRAUD_RESOLVE** | Resolve alerts | ✓ | ✓ | ✓ | | | |
| **FRAUD_MANAGE** | Manage detection rules | ✓ | ✓ | ✓ | | | |

---

## How Permissions Are Enforced

### In Controllers

All controller endpoints use `@PreAuthorize` annotations:

```java
@PreAuthorize("hasPermission(null, T(com.princely.shopmanager.shared.constants.PermissionConstants).INVESTMENT_CREATE)")
public ResponseEntity<InvestmentRoundResponse> createInvestmentRound(...) {
    // Only SYSTEM_ADMIN, TENANT_ADMIN, and OWNER can execute this
}
```

### Permission Constants

All permissions are defined in:
```java
com.princely.shopmanager.shared.constants.PermissionConstants
```

Example:
```java
public static final String INVESTMENT_CREATE = "INVESTMENT_CREATE";
public static final String INVESTMENT_READ = "INVESTMENT_READ";
public static final String INVESTMENT_LIST = "INVESTMENT_LIST";
```

---

## Permission Hierarchy

### No Permission → View Only → Operational → Management → Admin

1. **No Permission** (INVESTOR on most operations)
   - No access to resource

2. **View Only** (EMPLOYEE on products, INVESTOR on investments)
   - READ, LIST permissions only

3. **Operational** (EMPLOYEE on sales/inventory)
   - CREATE, READ, LIST permissions
   - Cannot UPDATE or DELETE

4. **Management** (MANAGER)
   - Full CRUD on operational resources
   - Limited access to financial/user management

5. **Admin** (OWNER, TENANT_ADMIN, SYSTEM_ADMIN)
   - Full CRUD on all resources
   - Permission management
   - Role assignment

---

## Best Practices

### 1. Use Granular Permissions

✅ **Good**:
```java
@PreAuthorize("hasPermission(null, T(...).INVESTMENT_CREATE)")
```

❌ **Bad**:
```java
@PreAuthorize("hasRole('OWNER')")  // Too broad
```

### 2. Separate CREATE/UPDATE/DELETE

Never reuse permissions:
- `INVESTMENT_CREATE` ≠ `INVESTMENT_UPDATE`
- `ROLE_UPDATE` ≠ `ROLE_PERMISSION_ADD`

### 3. Follow Least Privilege

Grant only the minimum permissions needed for each role.

### 4. Document Permission Changes

When adding new permissions:
1. Update `permission-matrix.csv`
2. Create migration to add permission to database
3. Update `docs/PERMISSION_MATRIX.md` (this file)
4. Update `src/docs/asciidoc/permission-matrix.adoc`

---

## Adding New Permissions

### Step 1: Add to permission-matrix.csv

```csv
RESOURCE,PERMISSION,Description,SYSTEM_ADMIN,TENANT_ADMIN,OWNER,MANAGER,EMPLOYEE,INVESTOR,Permission_Constant
INVOICE,INVOICE_CREATE,Create invoices,✓,✓,✓,✓,,,INVOICE_CREATE
```

### Step 2: Add to PermissionConstants.java

```java
public static final String INVOICE_CREATE = "INVOICE_CREATE";
```

### Step 3: Create Migration

```sql
-- Add new permission
INSERT INTO permissions (id, name, description, resource_type)
VALUES (gen_random_uuid()::varchar, 'INVOICE_CREATE', 'Create invoices', 'INVOICE');

-- Assign to roles
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r
CROSS JOIN permissions p
WHERE r.name IN ('SYSTEM_ADMIN', 'TENANT_ADMIN', 'OWNER', 'MANAGER')
  AND p.name = 'INVOICE_CREATE';
```

### Step 4: Use in Controller

```java
@PostMapping("/invoices")
@PreAuthorize("hasPermission(null, T(...).INVOICE_CREATE)")
public ResponseEntity<InvoiceResponse> createInvoice(...) {
    // Implementation
}
```

---

## Migration Notes

### V22 Investment Permission Changes

As of Migration V22 (January 2025):

**Removed permissions**:
- MANAGER can no longer create or update investments
- INVESTOR can no longer create or update investments

**Why**: Investment creation creates financial obligations. Only leadership roles (SYSTEM_ADMIN, TENANT_ADMIN, OWNER) should manage investments.

**Backward compatibility**: Existing investment records created by MANAGER or INVESTOR before V22 remain valid and are migrated to default rounds.

---

## See Also

- [INVESTMENT_GUIDE.md](INVESTMENT_GUIDE.md) - Investment system usage
- [API Documentation](../src/docs/asciidoc/index.adoc) - Full API reference
- [DEVELOPER_GUIDE.md](../DEVELOPER_GUIDE.md) - Development setup
- `permission-matrix.csv` - Source of truth for permissions

---

**Last Updated**: January 2025 (Migration V22)
