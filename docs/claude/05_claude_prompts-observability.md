Claude, extend the Shop Manager system with a dedicated DevOps & Observability setup.

### Logging & Monitoring
- Centralized logging:
    - Use ELK stack (Elasticsearch, Logstash, Kibana) OR OpenSearch stack
    - Standardize logs in JSON format from Spring Boot & React frontend
    - Include correlation IDs in logs for tracing across services
- Metrics collection:
    - Use Prometheus for metrics scraping
    - Spring Boot: expose actuator metrics
    - React frontend: basic page performance metrics
    - Grafana dashboards for visualization
- Tracing:
    - Use OpenTelemetry (OTel) instrumentation for distributed tracing
    - Export traces to Jaeger or Grafana Tempo

### Backups & Data Management
- Transactional and analytics data:
    - Scheduled backups (configurable) to cloud or on-prem file system
    - Database: recommend PostgreSQL with PITR (Point-in-time Recovery)
    - Cloud-compatible backup (S3, GCS, Azure Blob)
- Provide a backup/restore script in Helm charts

### Security & Hardening
- Enable HTTPS via Ingress controller with TLS termination
- Secure Keycloak with TLS and strong password policies
- Implement audit logs export and backup policy
- Secrets management with Kubernetes Secrets (optionally HashiCorp Vault integration)

### Deployment & CI/CD
- Helm charts extended for:
    - Prometheus + Grafana
    - ELK/OpenSearch stack
    - Keycloak
    - Shop Manager backend & frontend
- GitHub Actions pipeline extended for:
    - Deploying observability stack to Kubernetes
    - Uploading test & coverage reports
    - Backup verification job (simulate restore on staging)

### Deliverables
- Helm chart values for enabling/disabling observability components
- Dockerfiles/configs for log shippers (if Logstash/Filebeat required)
- Prometheus scrape config for backend & frontend
- Grafana dashboard JSONs for:
    - System health
    - Sales/investment analytics KPIs
    - Security/audit logs monitoring
- Backup/restore Helm jobs or Kubernetes CronJobs
