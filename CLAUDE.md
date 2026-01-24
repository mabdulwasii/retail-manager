# CLAUDE.md

AI Assistant Instructions for Shop Manager Project

---

## Quick Reference

**Documentation**:
- [README.md](./README.md) - Project overview
- [DEVELOPER_GUIDE.md](./DEVELOPER_GUIDE.md) - Local development setup
- [DEPLOYMENT_GUIDE.md](./DEPLOYMENT_GUIDE.md) - Deployment procedures
- [TESTING-GUIDE.md](./TESTING-GUIDE.md) - Testing strategy
- [docs/SHOP_ACCESS_CONTROL.md](./docs/SHOP_ACCESS_CONTROL.md) - Shop-level access control guide
- [helm-chart/shop-manager/README.md](./helm-chart/shop-manager/README.md) - Kubernetes deployment

**Key Commands**:
```bash
# Local development
docker-compose up -d

# Backend development
./mvnw spring-boot:run

# Run tests
./mvnw test

# Deploy to Kubernetes
helm install retail ./helm-chart/shop-manager -n gomco --create-namespace
```

---

## Project Context

Shop Manager is a multi-tenant retail management platform:
- **Backend**: Spring Boot 3.3, Java 21, Spring Modulith
- **Frontend**: React TypeScript with Keycloak authentication
- **Database**: PostgreSQL with Flyway migrations
- **Messaging**: Apache Kafka (KRaft mode)
- **Deployment**: Docker Compose (dev), Kubernetes/Helm (production)

---

## Development Workflow

### Git Workflow - CRITICAL
**NEVER commit directly to the `main` branch!**
- Always create a feature/fix branch for changes (e.g., `fix/test-failures`, `feature/new-endpoint`)
- Commit changes to the feature branch
- After completing work, inform the user so they can:
  1. Create a Pull Request with appropriate labels (e.g., `release-patch`)
  2. Trigger the CI/CD pipeline through the PR
  3. Review and merge the PR
- Exception: Only commit to `main` if explicitly instructed by the user

### Code Changes
- For every successful task, commit changes with clear commit messages
- For code-breaking changes, prompt the user first
- Update test cases for every code change and run tests
- Prefer static imports over fully qualified names
- Use conventional commits (feat:, fix:, docs:, test:, refactor:)
- **NEVER add "Generated with Claude Code" or "Co-Authored-By: Claude" to commit messages** - keep commits professional

### Code Review Focus
- Business logic correctness and security
- Proper error handling and logging
- Test coverage for new functionality
- Performance implications
- Code style, formatting, and maintainability
- Docker Compose and Helm chart updates

### Critical Conventions

**IMPORTANT**: These conventions MUST be followed for all code changes:

1. **Permission Matrix Updates**
   - **ALWAYS update `backend/src/main/resources/permission-matrix.csv`** whenever a new API endpoint is added, edited, or deleted
   - The permission-matrix.csv is the single source of truth for all permissions and role assignments
   - After updating CSV, also update:
     - `docs/PERMISSION_MATRIX.md`
     - `src/docs/asciidoc/permission-matrix.adoc`
   - Create a new migration file to add the permissions to the database

2. **Test Naming Convention**
   - Integration tests **MUST** end with `*IT.java` (e.g., `RoleControllerIT.java`)
   - Unit tests **MUST** end with `*Test.java` (e.g., `RoleServiceTest.java`)
   - This convention is enforced in architecture tests

3. **Permission Granularity**
   - Make permission constants as granular as possible without making them excessive
   - CREATE, READ, UPDATE, DELETE should ALWAYS be distinct permission constants
   - Never reuse a permission for multiple distinct operations
   - Example: `ROLE_PERMISSION_ADD` and `ROLE_PERMISSION_REMOVE` are separate from `ROLE_UPDATE`

4. **Database Migrations**
   - **NEVER modify existing migration files** in `src/main/resources/db/migration/`
   - Always create a new versioned migration file (e.g., V19, V20, etc.)
   - Migrations are immutable once committed

---

## Testing Strategy

**Coverage Targets**:
- Overall: 80% instruction, 75% branch, 85% class
- Business logic: 90% instruction, 85% branch
- Critical paths: 100% (financial calculations, security, audit)

**Test Layers**:
1. **Unit Tests**: Pure domain logic without Spring context
2. **Integration Tests**: Component testing with @WebMvcTest, @DataJpaTest
3. **End-to-End**: Full application context with TestContainers

**Test Commands**:
```bash
./mvnw test                                    # Unit tests
./mvnw verify -Pintegration-tests              # Integration tests
./mvnw clean verify jacoco:report              # With coverage
```

---

## Architecture Patterns

### Multi-Tenancy & Access Control
- **Tenant-Shop Hierarchy**: Tenant (organization) owns multiple shops
- **Data Isolation**: Complete tenant-level isolation via TenantContext
- **Shop-Level Access Control**: Role-based filtering at shop level (see [SHOP_ACCESS_CONTROL.md](./docs/SHOP_ACCESS_CONTROL.md))
  - SYSTEM_ADMIN: Access to all shops across all tenants
  - TENANT_ADMIN/OWNER/INVESTOR: Access to all shops within their tenant
  - MANAGER/EMPLOYEE: Access only to their assigned shop
- **Feature Flags**: Hierarchical (global → tenant → shop)
- **RBAC/ABAC**: Role-based and attribute-based authorization

### Module Structure
```
backend/src/main/java/com/princely/shopmanager/
├── core/         # Tenant, Shop, User, Role, Product (catalog) entities
├── sales/        # Sales, Receipt management with FEFO inventory deduction
├── inventory/    # Stock tracking, batch management, reservations
├── investment/   # Investment tracking, profit sharing
├── analytics/    # Analytics engine with caching
├── auth/         # Authentication, JWT principal
├── aggregator/   # Cloud Aggregator API (tenant registration, shop tracking)
└── shared/       # Cross-cutting concerns
```

### Cloud Aggregator Module (RetailHQ Cloud)

**Purpose**: Central registration and tracking service for local RetailHQ installations.

**Deployment Model**: Dual deployment - Cloud PaaS (`api.retailhq.app`) + Local installations

**Key Components**:
- **CloudTenant**: Retail business registered in cloud (entity: `cloud_tenants`)
- **CloudShop**: Physical store/location (entity: `cloud_shops`)
- **CloudTenantService**: Tenant registration, API key management, shop linking
- **AggregatorController**: Public REST API (`/api/registration/*`)

**API Endpoints**:
```
POST   /api/registration/tenants        # Register tenant with shops (public)
POST   /api/registration/shops          # Link additional shop (API key required)
DELETE /api/registration/tenants/{id}   # Unregister tenant (API key required)
GET    /api/registration/health         # Health check (public)
```

**Authentication**: API key with `rhq_` prefix, BCrypt hashing

**Subscription Tiers**: FREE, BASIC, PREMIUM, ENTERPRISE

**Status Management**: ACTIVE, SUSPENDED, INACTIVE

**Tests**:
- `CloudTenantServiceTest.java` - 16 unit tests (100% passing)
- `AggregatorControllerIT.java` - 12 integration tests

**Documentation**: [docs/CLOUD_AGGREGATOR_API.md](./docs/CLOUD_AGGREGATOR_API.md)

**Frontend Integration**: Single unified frontend with cloud mode detection
- Local mode: Shop operations
- Cloud mode (`cloud.retailhq.app`): Multi-shop aggregation + analytics (planned)

### Product & Inventory Architecture (Two-Tier Model)

**Implemented:** January 2025 (Migration V10)

The system uses a **two-tier model** separating product catalog from inventory:

#### **Product (Master Catalog)**
- Represents **what you sell** (SKU, price, description, category)
- **No stock fields** - stock tracking moved to Inventory
- Status: ACTIVE, INACTIVE, DISCONTINUED (OUT_OF_STOCK removed)
- Endpoints: `/api/shops/{shopId}/products`, `/api/products/{productId}`

#### **Inventory (Stock Tracking)**
- Represents **what you have** (batches, locations, expiry dates)
- Fields: currentStock, reservedStock, batchNumber, expiryDate, location
- Status: ACTIVE, INACTIVE, QUARANTINED, EXPIRED
- One Product → Many Inventory records (multi-batch support)

#### **Stock Aggregation**
```java
Product.totalStock = SUM(Inventory.currentStock) WHERE productId
Product.availableStock = SUM(currentStock - reservedStock) WHERE productId AND status=ACTIVE
```

#### **FEFO Sales Strategy**
Sales use **First Expiry, First Out (FEFO)** to minimize waste:
1. Sort inventory by expiry date (ascending)
2. Allocate from oldest expiring batches first
3. FIFO for same expiry date (by creation date)
4. Automatic deduction across multiple batches if needed

**Benefits:**
- ✅ Batch/lot tracking for compliance
- ✅ Expiry date management
- ✅ Multi-location inventory
- ✅ Flexible unit costing per batch
- ✅ Product recall traceability

**Documentation:** See [docs/PRODUCT_INVENTORY_GUIDE.md](./docs/PRODUCT_INVENTORY_GUIDE.md)

---

## Shop-Level Access Control Implementation

**CRITICAL**: All new services and endpoints MUST implement shop-level access control.

### Required Patterns

1. **Service Layer**:
   - Extend `ShopAwareService` for standardized shop validation
   - Add `JwtPrincipal principal` parameter to all service methods
   - Use `validateShopAccess(shopId, principal)` before operations
   - Create helper methods like `findEntityForUser(id, principal)` for reusable validation

2. **Controller Layer**:
   - Add `@AuthenticationPrincipal JwtPrincipal principal` to all endpoints
   - Pass principal to service methods

3. **Domain Layer**:
   - Implement `ShopAware` interface for entities belonging to a shop
   - Provide `getShopId()` method

4. **Repository Layer**:
   - Use scope-based filtering for list operations (SYSTEM_WIDE, TENANT_WIDE, SHOP_SPECIFIC)
   - Apply tenant/shop filters at database level

**Complete implementation guide**: [docs/SHOP_ACCESS_CONTROL.md](./docs/SHOP_ACCESS_CONTROL.md)

---

## Java Best Practices

### Naming Conventions
- Classes: `PascalCase` (UserService, ProductRepository)
- Methods/Variables: `camelCase` (getUserById, totalAmount)
- Constants: `UPPER_SNAKE_CASE` (MAX_RETRY_ATTEMPTS)
- Packages: `lowercase.with.dots`

### Modern Java 21 Features
- Use `records` for immutable DTOs
- Use `switch` expressions for pattern matching
- Use `Optional` for null safety
- Prefer immutable objects and `final` fields

### Spring Boot Patterns
- Constructor injection with `@RequiredArgsConstructor`
- `@ConfigurationProperties` for structured configuration
- `@Transactional` on service methods
- Repository pattern with JPA and specifications

---

## Authentication & Testing

For complete test user credentials and authentication testing procedures, see **[TESTING-GUIDE.md](./TESTING-GUIDE.md)**.

### Services (Development)
- Frontend: http://localhost:3001 (with custom Keycloak theme)
- Backend API: http://localhost:8081
- Keycloak: http://localhost:8080
- PostgreSQL: localhost:5432
- Kafka: localhost:9092

---

## Deployment Instructions

### Docker Compose (Local)
```bash
# Start all services
docker-compose up -d

# With SonarQube
docker-compose --profile sonar up -d

# Stop services
docker-compose down
```

### Kubernetes (Production)
```bash
# Install prerequisites
kubectl apply -f https://github.com/cert-manager/cert-manager/releases/download/v1.13.2/cert-manager.yaml

helm repo add ingress-nginx https://kubernetes.github.io/ingress-nginx
helm install ingress-nginx ingress-nginx/ingress-nginx --namespace ingress-nginx --create-namespace

# Deploy Shop Manager
kubectl create namespace gomco
helm install retail ./helm-chart/shop-manager -n gomco -f my-values.yaml --wait --timeout 10m
```

See [DEPLOYMENT_GUIDE.md](./DEPLOYMENT_GUIDE.md) for complete instructions.

---

## Security Guidelines

- Never commit secrets or credentials to version control
- All endpoints must use Keycloak authentication
- Implement tenant isolation at repository level
- Audit all financial transactions and role changes
- Never log sensitive data (passwords, tokens, PII)
- Use parameterized queries to prevent SQL injection
- Validate all inputs at multiple layers

---

## Database Management

### Flyway Migrations
- All schema changes via Flyway migrations
- Never modify existing migrations
- Include rollback scripts for production
- Test with production-like data volumes

### Key Tables
- `tenants`, `shops` (tenant-shop hierarchy)
- `users`, `roles`, `permissions` (tenant-scoped)
- `products`, `sales_transactions`, `inventory`
- `investments`, `investor_shares`, `product_returns`
- `audit_logs`, `feature_flags`, `expenses`

---

## Current Status

**Production-Ready Features**:
- ✅ Multi-tenant architecture with complete data isolation
- ✅ Shop-level access control with role-based filtering (295 tests passing)
- ✅ Spring Boot 3.3 + Spring Modulith
- ✅ Keycloak SSO with custom enhanced theme
- ✅ Comprehensive testing (90%+ coverage target)
- ✅ Docker Compose and Kubernetes/Helm deployment
- ✅ Sales, inventory, investment, and analytics modules
- ✅ PDF receipt generation
- ✅ Fraud detection and product returns
- ✅ Automated backup system

**Next Steps**:
- Frontend development (React TypeScript)
- CI/CD pipeline setup
- Production deployment
- Performance optimization
- Advanced analytics features

---

## Important Notes

### Performance Guidelines
- Target API response time: <200ms
- Database connection pool: 50-100 connections
- Cache TTL: 1 hour (analytics), 5 minutes (real-time)
- Auto-scaling triggers: 70% CPU, 80% memory

### Monitoring KPIs
- API response time (p50, p95, p99)
- Error rate (<1% target)
- Database query performance
- Transaction success rate
- System resource utilization

---

**For detailed information, always refer to the specific documentation files listed in the Quick Reference section.**