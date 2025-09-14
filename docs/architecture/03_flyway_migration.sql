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
