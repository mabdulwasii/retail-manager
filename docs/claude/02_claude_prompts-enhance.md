Claude, enhance the previously scaffolded Shop Manager project with advanced business logic and domain rules.

### Investment & Profit-Sharing
- Implement investment domain logic:
    - Investors can invest in:
        - specific products
        - or the entire shop
    - Configurable via feature flags
- Profit-sharing:
    - Calculate ratios based on:
        - sales of product(s) tied to investment
        - or total shop revenue (depending on configuration)
    - Handle active and inactive investors
    - Implement scheduled job (Spring Scheduler) to compute and distribute profits
    - Store history of distributions

### Fraud Detection & Risk Management
- Add pluggable fraud detection module:
    - Configurable rules engine
    - Example rules:
        - unusually high transaction frequency
        - suspiciously high single transaction
        - sales outside business hours
    - Mark suspicious transactions for review
- Add RiskAssessment entity and service
- Allow feature toggle for fraud detection

### Analytics & Reporting
- Add analytics module:
    - Sales performance per product, category, tenant
    - Investment ROI dashboards
    - Fraud detection reports
- Provide REST endpoints returning JSON
- Prepare skeleton for integration with BI tools (e.g., exporting CSV/JSON)

### Auditing & Logs
- Expand audit logging:
    - Log entity lifecycle events (create, update, delete)
    - Log security events (logins, failed logins, permission checks)
- Store in audit_logs table + configurable filesystem backup
- Ensure log rotation policy is configurable

### Receipts
- Enhance receipt generation:
    - Add template rendering (e.g., Thymeleaf or simple text template)
    - Include shop info, timestamp, product line items, subtotal, tax, total
- Expose REST endpoint to generate and fetch receipt by transactionId
- Support printing-ready output (plain text / PDF ready)

### Multi-Tenant Features
- Ensure TenantContext filters all queries by shopId
- Add Tenant-based isolation in repositories/services
- ABAC rules:
    - Example: user with role=MANAGER can only manage products within their shop
    - Example: investor role only accesses investment module

### Testing Enhancements
- Add integration tests for:
    - Investment profit distribution logic
    - Fraud detection rules
    - Multi-tenant isolation
- Use Testcontainers for Postgres
- Mock Keycloak if container is too heavy

### Deployment Updates
- Ensure Helm values allow enabling/disabling modules:
    - investment.enabled
    - analytics.enabled
    - fraud.enabled
- Update Dockerfile and docker-compose to include a scheduler service if needed

### Deliverables
- Enhanced domain services and entities for investment, fraud detection, analytics
- REST controllers exposing necessary APIs
- Configurable feature toggles for each module
- Updated Flyway migrations for new tables (investor_distributions, fraud_flags, analytics_cache, etc.)
- Integration + unit tests covering new business logic
- Updated Helm charts with module toggles
