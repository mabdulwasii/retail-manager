# ABAC Permission Matrix - Shop Manager

## Overview

This document defines the Attribute-Based Access Control (ABAC) permission matrix for the Shop Manager application. The ABAC system combines Role-Based Access Control (RBAC) with attribute-based policies to provide fine-grained access control across multi-tenant shop environments.

## Core Concepts

### Subjects (Users)
- **System Administrator**: Global system management
- **Shop Owner**: Full control within their shop(s)
- **Manager**: Operational management within assigned shop
- **Cashier**: Point-of-sale operations and basic inventory
- **Investor**: Investment tracking and reporting access
- **Auditor**: Read-only access for compliance and auditing

### Resources
- **Shop Management**: Shop configuration, settings, customization
- **Products**: Product catalog, inventory, pricing
- **Sales**: Transactions, receipts, customer management
- **Investments**: Investment tracking, profit distribution
- **Analytics**: Reports, dashboards, business intelligence
- **Users**: User management, roles, permissions
- **Financial Data**: Revenue, profits, financial reports
- **Audit Logs**: System logs, security events

### Actions
- **READ**: View/retrieve information
- **WRITE**: Create new records
- **UPDATE**: Modify existing records
- **DELETE**: Remove records
- **APPROVE**: Approve pending actions
- **EXECUTE**: Run operations/processes
- **EXPORT**: Export data outside system
- **ADMIN**: Administrative operations

### Attributes
- **Tenant Context**: Shop ID the user belongs to
- **Time Constraints**: Business hours, validity periods
- **IP Restrictions**: Geographic or network limitations
- **Feature Flags**: Module-specific access control
- **Data Sensitivity**: Classification of information
- **Transaction Amount**: Financial threshold restrictions

## Permission Matrix

### 1. Shop Management

| Role | Resource | Action | Conditions | Description |
|------|----------|--------|------------|-------------|
| **System Admin** | Any Shop | READ, WRITE, UPDATE, DELETE, ADMIN | None | Full global access |
| **Shop Owner** | Own Shop | READ, WRITE, UPDATE, DELETE, ADMIN | `tenant_id == shop_id` | Complete shop control |
| **Manager** | Own Shop | READ, WRITE, UPDATE | `tenant_id == shop_id` | Operational management |
| **Cashier** | Own Shop | READ | `tenant_id == shop_id` | View shop info only |
| **Investor** | Invested Shops | READ | `has_investment(shop_id)` | Limited shop information |
| **Auditor** | Any Shop | READ | `audit_role == true` | Read-only audit access |

### 2. Product Management

| Role | Resource | Action | Conditions | Description |
|------|----------|--------|------------|-------------|
| **System Admin** | Products | READ, WRITE, UPDATE, DELETE | None | Global product access |
| **Shop Owner** | Shop Products | READ, WRITE, UPDATE, DELETE | `tenant_id == shop_id` | Full product control |
| **Manager** | Shop Products | READ, WRITE, UPDATE | `tenant_id == shop_id` | Product management |
| **Cashier** | Shop Products | READ, UPDATE | `tenant_id == shop_id AND action == 'inventory_update'` | Inventory updates only |
| **Investor** | Invested Products | READ | `has_investment(product_id)` | Investment-related products |
| **Auditor** | Any Products | READ | `audit_role == true` | Audit access |

### 3. Sales Management

| Role | Resource | Action | Conditions | Description |
|------|----------|--------|------------|-------------|
| **System Admin** | All Transactions | READ, WRITE, UPDATE, DELETE | None | Global transaction access |
| **Shop Owner** | Shop Transactions | READ, WRITE, UPDATE, DELETE | `tenant_id == shop_id` | Full transaction control |
| **Manager** | Shop Transactions | READ, WRITE, UPDATE | `tenant_id == shop_id AND amount <= manager_limit` | Transaction management |
| **Cashier** | Shop Transactions | READ, WRITE | `tenant_id == shop_id AND cashier_id == user_id` | Own transactions |
| **Investor** | Investment Transactions | READ | `has_investment(shop_id)` | Investment-related sales |
| **Auditor** | All Transactions | READ | `audit_role == true` | Audit access |

### 4. Investment Management

| Role | Resource | Action | Conditions | Description |
|------|----------|--------|------------|-------------|
| **System Admin** | All Investments | READ, WRITE, UPDATE, DELETE, APPROVE | None | Global investment access |
| **Shop Owner** | Shop Investments | READ, WRITE, UPDATE, APPROVE | `tenant_id == shop_id` | Investment management |
| **Manager** | Shop Investments | READ | `tenant_id == shop_id` | View only |
| **Investor** | Own Investments | READ | `investor_id == user_id` | Personal investments |
| **Auditor** | All Investments | READ | `audit_role == true` | Audit access |

### 5. Analytics & Reporting

| Role | Resource | Action | Conditions | Description |
|------|----------|--------|------------|-------------|
| **System Admin** | All Analytics | READ, EXECUTE, EXPORT | None | Global analytics access |
| **Shop Owner** | Shop Analytics | READ, EXECUTE, EXPORT | `tenant_id == shop_id` | Own shop analytics |
| **Manager** | Shop Analytics | READ, EXECUTE | `tenant_id == shop_id` | Analytics viewing |
| **Investor** | Investment Analytics | READ | `has_investment(shop_id)` | Investment-specific reports |
| **Auditor** | All Analytics | READ, EXPORT | `audit_role == true` | Audit reporting |

### 6. User Management

| Role | Resource | Action | Conditions | Description |
|------|----------|--------|------------|-------------|
| **System Admin** | All Users | READ, WRITE, UPDATE, DELETE | None | Global user management |
| **Shop Owner** | Shop Users | READ, WRITE, UPDATE | `tenant_id == shop_id AND target_role != 'OWNER'` | Shop staff management |
| **Manager** | Shop Cashiers | READ, WRITE, UPDATE | `tenant_id == shop_id AND target_role == 'CASHIER'` | Cashier management |

### 7. Financial Data Access

| Role | Resource | Action | Conditions | Description |
|------|----------|--------|------------|-------------|
| **System Admin** | All Financial | READ, EXPORT | None | Global financial access |
| **Shop Owner** | Shop Financial | READ, EXPORT | `tenant_id == shop_id` | Own shop finances |
| **Manager** | Shop Financial | READ | `tenant_id == shop_id AND data_level <= 'SUMMARY'` | Summary financial data |
| **Investor** | Investment Financial | READ | `has_investment(shop_id)` | Investment returns |
| **Auditor** | All Financial | READ, EXPORT | `audit_role == true` | Audit access |

## Attribute-Based Rules

### 1. Time-Based Access Control

```
RULE: business_hours_access
IF (current_time BETWEEN shop.business_hours.start AND shop.business_hours.end)
   OR (user.role IN ['SYSTEM_ADMIN', 'SHOP_OWNER'])
THEN allow_access = true
ELSE allow_access = false
```

### 2. Amount-Based Restrictions

```
RULE: transaction_amount_limit
IF (transaction.amount > user.transaction_limit)
   AND (user.role NOT IN ['SHOP_OWNER', 'SYSTEM_ADMIN'])
THEN require_approval = true
```

### 3. IP-Based Access Control

```
RULE: geo_location_access
IF (user.allowed_ip_ranges CONTAINS request.ip)
   OR (user.role == 'SYSTEM_ADMIN')
THEN location_access = true
ELSE location_access = false
```

### 4. Feature Flag Integration

```
RULE: feature_based_access
IF (feature_flag.investment.enabled == true)
   AND (user.role IN ['SHOP_OWNER', 'INVESTOR', 'SYSTEM_ADMIN'])
THEN investment_access = true
```

### 5. Data Classification

```
RULE: sensitive_data_access
IF (data.classification == 'CONFIDENTIAL')
   AND (user.clearance_level >= 'HIGH')
   AND (audit_log_required == true)
THEN sensitive_access = true
```

## Multi-Tenant Isolation Rules

### 1. Tenant Boundary Enforcement

```
RULE: tenant_isolation
FOR all_operations:
  IF (resource.shop_id != user.tenant_id)
     AND (user.role != 'SYSTEM_ADMIN')
     AND (NOT has_cross_tenant_permission(user, resource.shop_id))
  THEN DENY access
```

### 2. Cross-Tenant Access

```
RULE: cross_tenant_investment
IF (user.role == 'INVESTOR')
   AND (user.investments CONTAINS resource.shop_id)
THEN allow_read_access = true
```

### 3. Audit Cross-Tenant Access

```
RULE: auditor_cross_tenant
IF (user.role == 'AUDITOR')
   AND (user.audit_scope CONTAINS resource.shop_id)
THEN allow_read_access = true
```

## Implementation Guidelines

### 1. Policy Enforcement Points (PEP)

- **API Gateway**: Initial request filtering
- **Service Layer**: Business logic enforcement
- **Repository Layer**: Data access control
- **UI Components**: Frontend access control

### 2. Policy Decision Points (PDP)

- **TenantSecurityService**: Multi-tenant decisions
- **FeatureFlagService**: Feature-based access
- **AuditService**: Audit trail requirements

### 3. Policy Information Points (PIP)

- **TenantContext**: Current tenant information
- **SecurityContext**: User authentication data
- **FeatureFlags**: Module enablement status
- **ShopConfiguration**: Shop-specific settings

### 4. Policy Administration Points (PAP)

- **Feature Flag Management**: Admin interface for flags
- **Role Management**: User role assignment
- **Permission Configuration**: Fine-grained permissions

## Security Considerations

### 1. Defense in Depth

- Multiple layers of access control
- Fail-safe defaults (deny by default)
- Regular permission audits
- Automated compliance checking

### 2. Audit Requirements

All access control decisions must be logged including:
- User identity and role
- Resource accessed
- Action attempted
- Decision result (allow/deny)
- Decision rationale
- Timestamp and IP address

### 3. Emergency Access

```
RULE: emergency_override
IF (system.emergency_mode == true)
   AND (user.role == 'SYSTEM_ADMIN')
   AND (emergency_approval_code.valid == true)
THEN bypass_normal_restrictions = true
```

## Testing and Validation

### 1. Permission Testing

- Unit tests for each permission rule
- Integration tests for complex scenarios
- Load testing for permission evaluations
- Security penetration testing

### 2. Compliance Validation

- Regular access reviews
- Permission audit reports
- Compliance dashboard monitoring
- Automated policy violations detection

## Maintenance and Updates

### 1. Regular Reviews

- Quarterly permission matrix reviews
- Annual security assessment
- Role definition updates
- New feature integration

### 2. Change Management

- Version control for permission changes
- Impact analysis for modifications
- Staged deployment of updates
- Rollback procedures for issues

## Contact and Support

For questions about the ABAC permission matrix:

- **Security Team**: security@shopmanager.com
- **Documentation**: docs@shopmanager.com
- **Support**: support@shopmanager.com

---

*Last Updated: December 2024*
*Version: 1.0*
*Classification: Internal Use*