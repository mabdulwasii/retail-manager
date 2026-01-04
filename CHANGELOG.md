# Changelog

All notable changes to Shop Manager will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [1.1.0] - 2026-01-04

### Added - Cloud Portal & Management Features

#### Backend APIs (17 endpoints)
- **Tenant Management** (2 endpoints)
  - GET `/api/cloud/tenants/{tenantId}` - Get tenant settings
  - PUT `/api/cloud/tenants/{tenantId}` - Update tenant settings

- **Shop Management** (5 endpoints)
  - GET `/api/cloud/tenants/{tenantId}/shops` - List all shops with pagination and filtering
  - GET `/api/cloud/tenants/{tenantId}/shops/{shopId}` - Get shop details
  - POST `/api/cloud/tenants/{tenantId}/shops` - Create new shop
  - PUT `/api/cloud/tenants/{tenantId}/shops/{shopId}` - Update shop
  - PATCH `/api/cloud/tenants/{tenantId}/shops/{shopId}/status` - Activate/deactivate shop

- **API Key Management** (3 endpoints)
  - GET `/api/cloud/tenants/{tenantId}/api-keys` - List API keys
  - POST `/api/cloud/tenants/{tenantId}/api-keys` - Generate new API key
  - DELETE `/api/cloud/tenants/{tenantId}/api-keys/{keyId}` - Revoke API key

- **Subscription Management** (2 endpoints)
  - GET `/api/cloud/subscriptions/{tenantId}` - Get current subscription
  - GET `/api/cloud/billing/{tenantId}/invoices` - Get billing history

- **Analytics** (2 endpoints)
  - GET `/api/cloud/tenants/{tenantId}/analytics` - Get tenant analytics
  - GET `/api/cloud/tenants/{tenantId}/analytics/sync-status` - Get shop sync status

- **Audit Logs** (3 endpoints)
  - GET `/api/cloud/tenants/{tenantId}/audit-logs` - Get audit logs with filtering
  - GET `/api/cloud/tenants/{tenantId}/audit-logs/export` - Export audit logs to CSV
  - Custom filters: action, entityType, dateRange, userId, shopId

#### Frontend - Cloud Portal (3 pages)
- **Tenant Settings Page**
  - Company information management
  - Contact details configuration
  - Timezone and locale settings
  - Read-only subscription information display
  - Form validation and error handling

- **Shop Management Page**
  - CRUD operations for shops
  - Advanced filtering (status, search by name)
  - Pagination (20 shops per page)
  - Activate/deactivate shops
  - Real-time shop count display
  - Empty state handling

- **Audit Logs Page**
  - Comprehensive activity tracking
  - Multi-filter support (action, entity, date range)
  - Pagination (50 logs per page)
  - CSV export functionality
  - Color-coded action badges
  - IP address and user tracking

#### Database Migrations
- **V16**: Tenant management schema
  - tenant_settings table with company info, contact details, timezone, locale

- **V17**: Shop management and API keys schema
  - cloud_shops table with status tracking
  - api_keys table with permissions and expiry

- **V18**: Subscriptions, analytics, and audit logs schema
  - subscriptions table with tier and billing info
  - tenant_analytics table for metrics
  - cloud_audit_logs table for activity tracking

#### Testing Infrastructure
- **E2E Tests** (Playwright)
  - 50 test scenarios across 3 pages
  - Accessibility-first selectors
  - Form validation testing
  - Filter and pagination testing
  - Export functionality testing
  - 691 lines of test code

- **Load Tests** (k6)
  - Performance testing for all 17 cloud endpoints
  - Staged load profiles (ramp up/down)
  - Performance thresholds: p95 < 500ms, error rate < 1%
  - 189 lines of load test code

#### Documentation
- **CLOUD_PORTAL_GUIDE.md** (764 lines)
  - Complete user guide for tenant settings, shop management, audit logs
  - API key management and subscription tracking
  - User roles, permissions, and best practices
  - Troubleshooting guide

- **CLOUD_API_REFERENCE.md** (766 lines)
  - Complete API reference for all 17 endpoints
  - Authentication guide (JWT + API keys)
  - Request/response schemas with curl examples
  - Error handling and rate limiting documentation
  - Best practices for API integration

- **E2E_TESTING_GUIDE.md** (comprehensive)
  - Playwright E2E testing setup and execution
  - k6 load testing guide
  - CI/CD integration instructions

### Technical Details

#### Code Statistics
- **Backend**: 3,043 lines of Java code
- **Frontend**: 1,837 lines of TypeScript/React
- **Tests**: 880 lines of test code (E2E + k6)
- **Documentation**: 2,644 lines

#### Architecture
- Multi-tenant cloud management module
- Role-based access control (SYSTEM_ADMIN, TENANT_ADMIN, OWNER, INVESTOR, MANAGER, EMPLOYEE)
- Shop-level data isolation
- Comprehensive audit logging for all actions
- API key-based programmatic access
- Subscription tier management (FREE, BASIC, PRO, ENTERPRISE)

### Changed
- Updated README.md with cloud portal documentation links
- Enhanced shop-level access control for cloud features
- Improved error handling across cloud endpoints

### Security
- API key encryption for secure storage
- JWT token validation on all cloud endpoints
- IP address tracking in audit logs
- Rate limiting by subscription tier
- Tenant data isolation enforced at database level

### Performance
- Pagination for all list endpoints (default: 20 items/page)
- Efficient filtering at database level
- CSV export streaming for large datasets
- Analytics data caching (configurable TTL)

---

## [1.0.0] - 2025-12-01

### Initial Release
- Multi-tenant retail management platform
- Sales, inventory, investment, and analytics modules
- Keycloak SSO integration
- Docker Compose and Kubernetes/Helm deployment
- Embedded JAR deployment for standalone installations
- Shop-level access control
- Product and inventory two-tier model with FEFO strategy
- PDF receipt generation
- Comprehensive authentication testing

---

[1.1.0]: https://github.com/yourorg/shop-manager/compare/v1.0.0...v1.1.0
[1.0.0]: https://github.com/yourorg/shop-manager/releases/tag/v1.0.0
