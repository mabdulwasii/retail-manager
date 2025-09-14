# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Shop Manager is a modular retail management platform for managing shops, investments, sales, and inventory. Built with Spring Boot (Java 21) and Spring Modulith, it supports multi-tenancy, configurable feature modules, and both on-premise and cloud deployments.

## Architecture

### Core Modules
- **com.princely.shopmanager.core** - Core entities and shared services
- **com.princely.shopmanager.investment** - Investment management and profit sharing
- **com.princely.shopmanager.analytics** - Analytics and reporting (event-driven via Kafka)
- **com.princely.shopmanager.sales** - Sales transactions and receipt generation
- **com.princely.shopmanager.auth** - Authentication/authorization with Keycloak SSO
- **com.princely.shopmanager.shared** - Logging, auditing, utilities

### Tech Stack
- **Backend**: Spring Boot 3.3, Java 21, Spring Modulith
- **Database**: PostgreSQL with Flyway migrations
- **Auth**: Keycloak (SSO, OAuth2, OpenID Connect)
- **Messaging**: Kafka for event streaming
- **Storage**: MinIO/S3-compatible for backups
- **Testing**: Testcontainers, ArchUnit for module boundaries
- **Frontend**: React TypeScript (planned)

## Development Commands

### Build and Test
```bash
# Build the project
./mvnw clean install

# Run unit tests
./mvnw test

# Run integration tests with Testcontainers
./mvnw verify -Pintegration-tests

# Run a single test
./mvnw test -Dtest=ClassName#methodName

# Skip tests during build
./mvnw clean install -DskipTests
```

### Local Development
```bash
# Start all infrastructure services
docker-compose up -d

# Start infrastructure with SonarQube for code quality
docker-compose --profile sonar up -d

# Run the backend application
./mvnw spring-boot:run

# Stop infrastructure services
docker-compose down
```

### Docker Operations
```bash
# Build backend Docker image
docker build -t shop-manager:latest ./backend

# Run with docker-compose (includes all dependencies)
docker-compose up

# Run with SonarQube profile for code analysis
docker-compose --profile sonar up
```

## Key Configuration

### Feature Toggles
The system uses feature flags for modular functionality:
- `investment.enabled` - Investment module
- `analytics.enabled` - Analytics module
- `fraud.enabled` - Fraud detection module

### Multi-Tenancy
- **Tenant-Shop Hierarchy**: Tenant is a company/organization that owns multiple shops
- **Data Isolation**: Complete tenant-level data isolation with TenantContext
- **Feature Flags**: Hierarchical feature flags scoped to tenant level
- **Permission matrix**: Different roles per tenant with RBAC/ABAC authorization
- **Users belong to tenants**: Multi-shop access within tenant boundaries

### Database Schema
Key tables managed by Flyway:
- `tenants` (companies/organizations)
- `shops` (tenant-owned retail locations)
- `users`, `roles`, `permissions` (tenant-scoped)
- `products`, `sales_transactions`, `inventory`
- `investments`, `investor_shares`, `product_returns`
- `audit_logs`, `feature_flags`, `expenses`
- **V6 Migration**: Tenant-shop refactoring with comprehensive inventory system

## Important Use Cases

### Shop Owner/Manager
- Add and manage multiple shops (tenants)
- Configure products and categories (groceries, electronics, bakery)
- Manage inventory (stocking, activation/deactivation)
- Generate sales receipts with line items
- View analytics and profitability reports

### Investor
- Contribute funds to specific shops or product lines
- Select investment models (proportional, fixed shares, time-weighted)
- Track allocations and profit sharing
- Withdraw or reinvest profits dynamically

### Customer
- Purchase products from shops
- Receive itemized receipts
- Access reliably stocked products

### System Administrator
- Configure SSO with Keycloak
- Manage tenants and users with RBAC/ABAC permissions
- Enable/disable feature modules per tenant
- Configure backup locations (on-prem or cloud)

### Auditor/Compliance Officer
- Review audit logs for major events
- Monitor fraud detection and risk management
- Verify financial allocations and distributions

## Development Best Practices

### Module Development
- Follow Spring Modulith patterns for module isolation
- Each module should have clear boundaries and interfaces
- Use ArchUnit tests to enforce module dependencies
- Modules should communicate via events when possible

### Security Considerations
- All endpoints must be secured with Keycloak authentication
- Implement tenant isolation at repository level
- Audit all financial transactions and role changes
- Never log sensitive data (passwords, tokens, PII)

### Testing Strategy
Shop Manager implements a comprehensive testing strategy aimed at achieving 90% code coverage and 100% business rule coverage:

#### Test Architecture Levels
1. **Unit Tests**: Pure domain logic and service testing without Spring context
   - Domain entity validation and business rules
   - Service layer logic with mocked dependencies
   - Utility classes and helper methods
   - Target: 100% coverage of business logic

2. **Integration Tests**: Component testing with focused Spring context
   - `@WebMvcTest` for REST controller testing
   - `@DataJpaTest` for repository and database operations
   - `@JsonTest` for JSON serialization/deserialization
   - Mock external dependencies (Keycloak, Kafka)

3. **End-to-End Integration Tests**: Full application context with TestContainers
   - Complete API workflow testing with real databases
   - Multi-tenant isolation validation
   - Security and authentication flow testing
   - Business rule enforcement across layers

#### Test Infrastructure
- **TestContainers Integration**: PostgreSQL, Kafka, and Keycloak containers
- **JaCoCo Code Coverage**: Automated coverage reporting with quality gates
  - Minimum 80% instruction coverage overall
  - Minimum 90% coverage for business logic (services/domain packages)
  - Branch coverage minimum 75% overall, 85% for business logic
- **Test Data Management**: SQL scripts for consistent test data setup/cleanup

#### Coverage Targets & Quality Gates
- **Overall Application**: 80% instruction, 75% branch, 85% class coverage
- **Business Logic Packages**: 90% instruction, 85% branch coverage
- **Critical Paths**: 100% coverage for financial calculations, security, and audit
- **Controller Layer**: 90% coverage with comprehensive @WebMvcTest suites
- **Repository Layer**: 95% coverage with @DataJpaTest integration

#### Test Organization
```
src/test/java/
├── unit/                    # Pure unit tests (no Spring context)
├── integration/             # Component integration tests
├── e2e/                    # End-to-end integration tests
└── testcontainers/         # Full-stack TestContainer tests
```

#### Key Testing Principles
- Mock external services (Keycloak, Kafka) in unit/integration tests
- Use real databases only for end-to-end TestContainer tests
- Test security authorization at controller layer with @WithMockUser
- Validate multi-tenant isolation in integration tests
- Test business rule enforcement with edge cases and invalid inputs
- Ensure idempotent test execution with proper setup/cleanup

### Database Migrations
- All schema changes through Flyway migrations
- Never modify existing migrations
- Include rollback scripts for production migrations
- Test migrations with production-like data volumes

### Event-Driven Architecture
- Use Kafka for analytics and fraud detection events
- Implement idempotent event handlers
- Store event schemas in a registry
- Consider event sourcing for audit trail

## Project Status

The Shop Manager backend is now feature-complete with comprehensive testing infrastructure:

### ✅ Core Architecture & Features Implemented
- **Spring Boot 3.3 + Spring Modulith**: Modular architecture with clear boundaries
- **Multi-Tenant System**: Configurable tenant isolation with feature flags
- **Complete Domain Model**: Shop, Product, User, Role, Permission entities
- **Sales Management**: SalesTransaction, LineItem, Receipt generation with PDF export
- **Investment Module**: Investment tracking, profit distribution, ROI analytics
- **Analytics Engine**: Real-time analytics with caching and fraud detection
- **Authentication & Security**: Keycloak integration with RBAC/ABAC authorization
- **Audit System**: Comprehensive audit logging for all business operations
- **Event-Driven Architecture**: Kafka integration for analytics and notifications

### ✅ Advanced Business Features
- **Shop Customization**: Logo, branding, themes, UI customization per tenant
- **Feature Flag System**: Hierarchical feature flags (global/shop-specific)
- **Receipt Management**: Auto-generation, printing, email delivery tracking
- **Fraud Detection**: ML-based transaction analysis with configurable rules
- **Investment Profit Sharing**: Automated profit distribution with multiple models
- **Advanced Analytics**: Sales summaries, ROI tracking, revenue analytics with caching
- **Comprehensive Inventory Management**: Stock tracking, reservations, history, and alerts
- **Product Return System**: Return processing with fraud validation and restocking
- **Expense Tracking**: Multi-category expense management for accurate P&L
- **Data Backup System**: Encrypted backup with configurable retention policies

### ✅ REST API & Documentation
- **Complete REST Controllers**: Shop, Receipt, Analytics, Investment management
- **OpenAPI/Swagger Integration**: Comprehensive API documentation with security
- **Input Validation**: Jakarta Validation with detailed error responses
- **Security Integration**: Role-based endpoint protection with JWT tokens
- **Multi-tenant API**: Automatic tenant context resolution and isolation

### ✅ Comprehensive Testing Infrastructure (90%+ Coverage Target)
- **JaCoCo Code Coverage**: Automated reporting with quality gates (80% overall, 90% business logic)
- **Controller Tests**: Complete @WebMvcTest suites for all REST endpoints
- **Integration Tests**: TestContainers with PostgreSQL, Kafka, and Keycloak
- **Business Rule Tests**: 100% coverage of financial calculations and security logic
- **Multi-tenant Tests**: Tenant isolation validation across all layers
- **Security Tests**: Authentication and authorization flow validation
- **Database Tests**: @DataJpaTest for repository and persistence validation

### ✅ Production Infrastructure
- **Database Schema**: Complete Flyway migrations with rollback support (V6 latest)
- **Docker Integration**: Unified docker-compose with service profiles (SonarQube optional)
- **Configuration Management**: Environment variable-driven with ConfigMaps/Secrets support
- **Helm Charts**: Production-ready Kubernetes deployment with inline defaults
- **Code Quality**: SonarQube integration with quality gates and analysis profiles
- **Logging & Monitoring**: Structured logging with audit trails
- **Error Handling**: Global exception handling with proper HTTP status codes

### ✅ Testing Commands Available
```bash
# Run all tests with coverage
./mvnw clean verify

# Run only unit tests
./mvnw test

# Run integration tests with TestContainers
./mvnw verify -Pintegration-tests

# Generate coverage reports
./mvnw clean verify jacoco:report

# Run specific test patterns
./mvnw test -Dtest="*ControllerTest"
./mvnw test -Dtest="*IntegrationTest"
```

### 📁 Project Structure
```
backend/
├── src/main/java/com/princely/shopmanager/
│   ├── core/           # Tenant, Shop, User, Role entities and services
│   ├── sales/          # Sales, Receipt management
│   ├── inventory/      # Inventory management with stock tracking
│   ├── returns/        # Product return processing
│   ├── investment/     # Investment tracking and profit sharing
│   ├── analytics/      # Analytics engine with caching (Java records)
│   ├── auth/           # Authentication, authorization, JWT principal
│   ├── shared/         # Cross-cutting concerns, utilities, configuration
│   └── ShopManagerApplication.java
├── src/test/java/      # Comprehensive test suites
│   ├── unit/           # Pure unit tests
│   ├── integration/    # Component integration tests
│   └── testcontainers/ # End-to-end integration tests
├── src/main/resources/
│   ├── db/migration/   # Flyway database migrations (V6 latest)
│   ├── application.yml # Environment-driven configuration
│   └── static/docs/    # Generated API documentation
├── docker-compose.yml  # Unified compose with SonarQube profile
└── helm-chart/         # Kubernetes deployment charts
```

### 🎯 Achievement Summary
- **Code Coverage**: JaCoCo configured with 90%+ target for business logic
- **API Endpoints**: 20+ REST endpoints with comprehensive Swagger documentation
- **Test Coverage**: 30+ test classes with unit, integration, and E2E tests
- **Security**: Multi-layer security with JWT principal, RBAC, and tenant isolation
- **Business Rules**: 100% coverage of financial calculations and audit requirements
- **Production Ready**: Docker, Helm charts, SonarQube, and environment-driven configuration
- **Code Quality**: Java records for DTOs, @Builder.Default patterns, and clean architecture
- **Multi-Tenancy**: Complete tenant-shop hierarchy with comprehensive data isolation

### 🆕 Latest Updates (January 2025)
- **Tenant-Shop Refactoring**: Separated tenant as organization owning multiple shops
- **Inventory Management**: Comprehensive stock tracking, reservations, and history
- **Product Returns**: Full return processing with fraud detection integration
- **Environment Configuration**: Preference for environment variables and ConfigMaps
- **Docker Compose Unification**: Single compose file with SonarQube profile
- **Helm Chart Enhancement**: Inline defaults for seamless Kubernetes deployment
- **Java Records Migration**: Converted analytics DTOs to immutable Java records
- **Builder Pattern Compliance**: All @Builder classes properly use @Builder.Default

**Current Status**: Production-ready backend with enhanced multi-tenancy and comprehensive business features.
**Next Steps**: Frontend development, CI/CD pipeline, and production deployment.


### Code Review Guidelines
For every successful task completed, commit the changes to the current branch and add a suitable commit message.
For every code-breaking change made, prompt me
For every code change made, update the test cases and run the tests.
Prefer static imports over fully qualified names.