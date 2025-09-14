# Shop Manager System — Background & Use Cases

## Background
The Shop Manager system is designed as a flexible, modular platform for managing retail shops, investments, and sales. 
It will allow investors to allocate funds into shops or specific products and share profits based on configurable models. 
The system also manages inventory, sales, receipts, fraud detection, auditing, and analytics. 
It is built with scalability in mind to support both small on-premise deployments and cloud-based multi-tenant shops.

## Use Cases
1. **Shop Owner / Manager**
   - Add/manage shops (tenants)
   - Configure products and categories (e.g., groceries, electronics, bakery items)
   - Manage inventory (stocking, deactivation, activation)
   - Generate sales receipts with line items
   - View analytics and profitability

2. **Investor**
   - Contribute funds to a shop or product line
   - Select investment model: proportional by amount, fixed shares, or time-weighted
   - Track allocations and profit sharing
   - Withdraw or reinvest profits

3. **Customer**
   - Purchase products in a shop
   - Receive itemized receipts
   - Benefit from reliable stocked products

4. **System Administrator**
   - Configure SSO with Keycloak
   - Manage tenants and users with role-based and ABAC permissions
   - Enable/disable feature modules (analytics, investment, fraud detection, etc.)
   - Backup transaction and analytics data to configurable locations (on-prem or cloud)

5. **Auditor / Compliance Officer**
   - Review audit logs for major events (sales, allocations, role changes)
   - Ensure fraud detection and risk management are functioning
   - Verify financial allocations and distributions

## Goals
- Provide a unified shop management solution that works both on-premise and in cloud.
- Offer modular configurability so shops can enable only the needed features.
- Ensure scalability, security, and compliance readiness.


# Tech Stack & Architecture Considerations

## Backend
- **Spring Boot (Java 17)** with **Spring Modulith** for modular design
- **Spring Security** + **Keycloak SSO** integration
- **Spring Data JPA (Hibernate)** for persistence
- **ArchUnit** tests to enforce module boundaries for future microservice migration
- **Testcontainers** for integration tests (Postgres, Kafka, Keycloak, MinIO)

## Frontend
- **React (TypeScript)** with modern component library (e.g., Material UI, Tailwind)
- Communicates with backend via REST/GraphQL APIs

## Authentication & Authorization
- **Keycloak** for identity and access management (SSO, OAuth2, OpenID Connect)
- Role-Based Access Control (RBAC) and Attribute-Based Access Control (ABAC)
- Permission matrix to allow different roles per tenant/shop

## Database & Storage
- **PostgreSQL** (primary transactional database)
- **MinIO / S3-compatible** for backups and file storage
- Flyway for database migrations

## Messaging & Event Streaming
- **Kafka** for event-driven analytics and fraud detection pipelines

## Deployment & Infrastructure
- Bootstrapped with **Docker** and **Kubernetes**
- **Helm charts** for deployment to cloud/on-prem clusters
- CI/CD with **GitHub Actions**

## Non-Functional Considerations
- **Multi-tenancy** support (multiple shops in one deployment)
- **Configurable feature modules** (investment, analytics, fraud detection, etc.)
- **Audit logs** persisted in database + backed up to file system/cloud
- **Fraud detection and risk management** to monitor suspicious activity
- **On-prem first** with option to scale to cloud


# Tech Stack & Architecture Considerations

## Backend
- **Spring Boot (Java 17)** with **Spring Modulith** for modular design
- **Spring Security** + **Keycloak SSO** integration
- **Spring Data JPA (Hibernate)** for persistence
- **ArchUnit** tests to enforce module boundaries for future microservice migration
- **Testcontainers** for integration tests (Postgres, Kafka, Keycloak, MinIO)

## Frontend
- **React (TypeScript)** with modern component library (e.g., Material UI, Tailwind)
- Communicates with backend via REST/GraphQL APIs

## Authentication & Authorization
- **Keycloak** for identity and access management (SSO, OAuth2, OpenID Connect)
- Role-Based Access Control (RBAC) and Attribute-Based Access Control (ABAC)
- Permission matrix to allow different roles per tenant/shop

## Database & Storage
- **PostgreSQL** (primary transactional database)
- **MinIO / S3-compatible** for backups and file storage
- Flyway for database migrations

## Messaging & Event Streaming
- **Kafka** for event-driven analytics and fraud detection pipelines

## Deployment & Infrastructure
- Bootstrapped with **Docker** and **Kubernetes**
- **Helm charts** for deployment to cloud/on-prem clusters
- CI/CD with **GitHub Actions**

## Non-Functional Considerations
- **Multi-tenancy** support (multiple shops in one deployment)
- **Configurable feature modules** (investment, analytics, fraud detection, etc.)
- **Audit logs** persisted in database + backed up to file system/cloud
- **Fraud detection and risk management** to monitor suspicious activity
- **On-prem first** with option to scale to cloud



