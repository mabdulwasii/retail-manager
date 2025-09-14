# Claude Code Prompts

## Prompt A — Full Scaffold
```
Scaffold a Spring Boot Modulith project named 'shop-manager' (Java 17) with modules: core, sales, investment, analytics, auth, reporting. Include:
- Spring Modulith and ArchUnit tests enforcing module boundaries.
- JPA entities: Tenant, Investor, Investment, Product, Sale, Allocation, Settlement, AuditEvent, FeatureFlag.
- Flyway migration file V1__create_core_tables.sql (include SQL content).
- AllocationService with proportional_by_amount, fixed_shares, time_weighted algorithms and unit tests.
- Testcontainers-based integration tests (Postgres, Kafka, Keycloak, MinIO).
- Dockerfile, docker-compose (dev), Helm chart template, and GitHub Actions CI skeleton (ci.yml).
- README with Keycloak realm import steps and backup/restore instructions.
```

## Prompt B — Generate Migrations
```
Generate Flyway migration file V1__create_core_tables.sql with the schema described: tenants, investors, products, investments, sales, allocations, audit_events, feature_flags. Use UUID PKs and NUMERIC(18,4) for money. Provide indexes for sales.sold_at and allocations.sale_id.
```

## Prompt C — Create Docker & Helm
```
Create Dockerfile for the Spring Boot backend and a docker-compose.yml with services: postgres, keycloak, kafka, zookeeper, minio, backend. Also create a Helm chart template with values.yaml for deployment and a basic templates/deployment.yaml and service.yaml skeletons.
```
