Claude, Scaffolscaffold a complete Shop Manager system based on the following architecture and requirements:

### Project Setup
- Spring Boot 3.3 + Java 21
- Use Maven with provided dependencies:
    - Spring Web, Spring Data JPA, Spring Security
    - Keycloak Spring Boot Starter
    - PostgreSQL
    - Spring Modulith
    - ArchUnit (architecture rules)
    - Testcontainers (Postgres + optional Keycloak)
    - Lombok
- Package structure:
    - com.shopmanager.core
    - com.shopmanager.investment
    - com.shopmanager.analytics
    - com.shopmanager.sales
    - com.shopmanager.auth
    - com.shopmanager.shared (logging, auditing, utils)

### Authentication & Authorization
- SSO via Keycloak with Spring Security integration
- Role-Based Access Control (RBAC) + Attribute-Based Access Control (ABAC)
- TenantContext to resolve shop/tenant per request
- Permission matrix for multi-tenant support

### Features
- Product CRUD (activate/deactivate, stock, categories)
- Sales transactions with line items
- Receipt generation (return JSON + printable format)
- Investment management:
    - Investors fund specific products or whole shop (configurable)
    - Profit-sharing ratios calculated from sales
    - Investors may be active or inactive dynamically
- Analytics module (pluggable)
- Fraud detection placeholder module (rules engine, configurable)
- Feature toggles:
    - investment.enabled
    - analytics.enabled
    - fraud.enabled

### Persistence
- PostgreSQL database
- Flyway migrations for baseline schema:
    - users, roles, permissions
    - shops (tenants)
    - products
    - sales_transactions
    - investments, investor_shares
    - audit_logs
    - feature_flags

### Auditing & Logging
- Activity and audit logs on all major events
- Logs stored in DB and backed up to filesystem (configurable path)

### Testing
- Integration tests with Testcontainers (Postgres, optional Keycloak)
- ArchUnit tests to enforce Modulith boundaries
- Standard JUnit tests

### Deployment
- Provide Dockerfile for backend
- Provide docker-compose.yml including Postgres, Keycloak, MinIO
- Provide Helm charts:
    - values.yaml
    - deployment.yaml
    - service.yaml
- Externalize all configs via environment variables

### CI/CD
- Use GitHub Actions workflow with:
    - Maven build + test (with Testcontainers)
    - Build + push Docker image to GitHub Container Registry
    - Deploy to Kubernetes using Helm

### Deliverables
- Complete Spring Boot project scaffold with modules, configs, and initial entities
- Flyway SQL migration files
- Dockerfile and docker-compose.yml
- Helm chart templates
- GitHub Actions workflow under `.github/workflows/ci.yml`
- Example integration tests with Testcontainers
- Example ArchUnit tests

Please generate all necessary code, configuration, and scaffolding files so that I can build, run, and deploy this project end-to-end with Docker and Kubernetes.
