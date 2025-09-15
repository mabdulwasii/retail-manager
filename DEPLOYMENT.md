# Shop Manager Deployment Guide

This comprehensive guide covers deploying Shop Manager using both **Docker Compose** and **Helm Charts** with all dependencies including PostgreSQL, Kafka, and Keycloak with pre-configured test users and realms.

## 📋 Table of Contents

- [Prerequisites](#prerequisites)
- [Docker Compose Deployment](#docker-compose-deployment)
- [Helm Chart Deployment](#helm-chart-deployment)
- [Test Users & Credentials](#test-users--credentials)
- [Access URLs](#access-urls)
- [Troubleshooting](#troubleshooting)
- [Monitoring & Health Checks](#monitoring--health-checks)

## 🔧 Prerequisites

### Common Requirements
- Docker and Docker Compose
- Git (for cloning the repository)

### For Helm Deployment
- Kubernetes cluster (local or cloud)
- Helm 3.x installed
- kubectl configured to connect to your cluster

### For Development
- Node.js 18+ (for frontend development)
- Java 21+ (for backend development)
- Maven 3.8+ (for building Java applications)

---

## 🐳 Docker Compose Deployment

Docker Compose provides the quickest way to get Shop Manager running locally with all dependencies.

### Quick Start Commands

```bash
# 1. Clone the repository (if not already done)
git clone <repository-url>
cd shop-manager

# 2. Start all services (backend only)
docker-compose up -d

# 3. Start with frontend included
docker-compose up -d frontend

# 4. Start with SonarQube for code analysis
docker-compose --profile sonar up -d

# 5. View logs
docker-compose logs -f backend keycloak

# 6. Check service status
docker-compose ps
```

### Service Architecture

The Docker Compose setup includes:

| Service | Port | Description |
|---------|------|-------------|
| **postgres** | 5432 | PostgreSQL database for app and Keycloak |
| **keycloak** | 8080 | Keycloak SSO server with shop-manager realm |
| **keycloak-init** | - | One-time realm import service |
| **kafka** | 9092 | Kafka message broker |
| **minio** | 9000, 9001 | MinIO object storage for backups |
| **backend** | 8081 | Spring Boot application |
| **frontend** | 3000 | React frontend application |
| **sonarqube** | 9090 | Code quality analysis (optional) |

### Step-by-Step Deployment

#### 1. Environment Setup
```bash
# Ensure Docker is running
docker --version
docker-compose --version

# Create necessary directories
mkdir -p docker/logs
```

#### 2. Database and Infrastructure
```bash
# Start infrastructure services first
docker-compose up -d postgres keycloak kafka minio

# Wait for services to be healthy
docker-compose ps
```

#### 3. Keycloak Realm Configuration
```bash
# The keycloak-init service automatically imports the realm
# Check if realm import was successful
docker-compose logs keycloak-init

# Verify realm exists
curl -s http://localhost:8080/realms/shop-manager/.well-known/openid-configuration
```

#### 4. Backend Application
```bash
# Start the backend service
docker-compose up -d backend

# Check backend health
curl http://localhost:8081/actuator/health

# View backend logs
docker-compose logs -f backend
```

#### 5. Frontend Application (Optional)
```bash
# Start the frontend service
docker-compose up -d frontend

# Access frontend at http://localhost:3000
```

### Configuration Customization

Create a `.env` file to override default values:

```bash
# .env file
POSTGRES_USER=custom_user
POSTGRES_PASSWORD=custom_password
POSTGRES_DB=custom_db
KEYCLOAK_ADMIN=custom_admin
KEYCLOAK_ADMIN_PASSWORD=custom_admin_password
SPRING_PROFILES_ACTIVE=production
```

### Docker Compose Commands Reference

```bash
# Start all services
docker-compose up -d

# Start specific services
docker-compose up -d backend frontend

# Stop all services
docker-compose down

# Stop and remove volumes (CAUTION: deletes data)
docker-compose down -v

# Restart a service
docker-compose restart backend

# View logs
docker-compose logs -f backend
docker-compose logs --tail=100 keycloak

# Scale services
docker-compose up -d --scale backend=2

# Update and rebuild
docker-compose up -d --build backend
```

---

## ⚓ Helm Chart Deployment

Helm provides production-ready deployment with proper resource management, scaling, and configuration.

### Quick Start Commands

```bash
# 1. Add required Helm repositories
helm repo add bitnami https://charts.bitnami.com/bitnami
helm repo update

# 2. Create namespace
kubectl create namespace shop-manager

# 3. Install with default configuration
helm install shop-manager ./helm-chart/shop-manager \
  --namespace shop-manager \
  --set frontend.enabled=true \
  --wait \
  --timeout 15m

# 4. Port forward to access services
kubectl port-forward -n shop-manager svc/shop-manager-backend 8081:8081 &
kubectl port-forward -n shop-manager svc/shop-manager-frontend 3000:3000 &
kubectl port-forward -n shop-manager svc/shop-manager-keycloak 8080:8080 &
```

### Helm Dependencies

```bash
# Update chart dependencies
cd helm-chart/shop-manager
helm dependency update
helm dependency list
```

### Deployment Environments

#### Development Environment
```bash
helm install shop-manager ./helm-chart/shop-manager \
  --namespace shop-manager \
  --set frontend.enabled=true \
  --set postgresql.primary.persistence.enabled=false \
  --set keycloak.postgresql.primary.persistence.enabled=false \
  --set config.logging.appLevel=DEBUG \
  --set config.security.tenantIsolation=false \
  --set resources.requests.memory=256Mi \
  --wait
```

#### Production Environment
```bash
helm install shop-manager ./helm-chart/shop-manager \
  --namespace shop-manager \
  --set replicaCount=3 \
  --set frontend.replicaCount=2 \
  --set postgresql.primary.persistence.size=50Gi \
  --set postgresql.primary.persistence.storageClass=fast-ssd \
  --set resources.limits.memory=2Gi \
  --set resources.limits.cpu=1000m \
  --set autoscaling.enabled=true \
  --set autoscaling.maxReplicas=10 \
  --set config.keycloak.authServerUrl=https://keycloak.yourdomain.com \
  --wait
```

### Custom Values File

Create `custom-values.yaml`:

```yaml
# Production configuration
replicaCount: 2

frontend:
  enabled: true
  replicaCount: 2
  config:
    apiBaseUrl: "https://api.shop-manager.com/api"
    keycloakUrl: "https://auth.shop-manager.com"

postgresql:
  primary:
    persistence:
      size: 20Gi
      storageClass: "gp2"
  auth:
    database: shopdb_prod
    username: shop_prod

keycloak:
  ingress:
    enabled: true
    hostname: auth.shop-manager.com
    tls: true

config:
  logging:
    appLevel: "INFO"
    rootLevel: "WARN"
  security:
    tenantIsolation: true
    sessionTimeoutMinutes: 60
  business:
    defaultCurrency: "EUR"
    defaultTaxRate: "0.20"

features:
  investment:
    enabled: true
  analytics:
    enabled: true
  fraud:
    enabled: true

resources:
  limits:
    memory: 2Gi
    cpu: 1000m
  requests:
    memory: 1Gi
    cpu: 500m

autoscaling:
  enabled: true
  minReplicas: 2
  maxReplicas: 10
  targetCPUUtilizationPercentage: 70
```

Deploy with custom values:
```bash
helm install shop-manager ./helm-chart/shop-manager \
  --namespace shop-manager \
  --values custom-values.yaml \
  --wait
```

### Helm Commands Reference

```bash
# Install/Upgrade
helm install shop-manager ./helm-chart/shop-manager --namespace shop-manager
helm upgrade shop-manager ./helm-chart/shop-manager --namespace shop-manager

# Check status
helm status shop-manager --namespace shop-manager
helm list --namespace shop-manager

# Get values
helm get values shop-manager --namespace shop-manager

# Rollback
helm rollback shop-manager 1 --namespace shop-manager

# Uninstall
helm uninstall shop-manager --namespace shop-manager

# Debug
helm template shop-manager ./helm-chart/shop-manager
helm install shop-manager ./helm-chart/shop-manager --dry-run --debug
```

---

## 🔐 Test Users & Credentials

After deployment, the following pre-configured test users are available:

### User Accounts

| Role | Username | Password | Description | Permissions |
|------|----------|----------|-------------|-------------|
| **Admin** | admin@shopmanager.com | admin123 | System Administrator | Full tenant access, user management |
| **Manager** | manager@shopmanager.com | manager123 | Shop Manager | Shop operations, inventory, sales |
| **Employee** | employee@shopmanager.com | employee123 | Shop Employee | Sales transactions, basic inventory |
| **Investor** | investor@shopmanager.com | investor123 | Investor | Investment tracking, reports |
| **Customer** | customer@shopmanager.com | customer123 | Customer | Purchase history, receipts |

### Service Accounts

| Service | Username | Password | Purpose |
|---------|----------|----------|---------|
| **Database** | shop | shop | Application database access |
| **Keycloak Admin** | admin | admin | Keycloak administration |
| **MinIO** | minioadmin | minioadmin | Object storage access |

### API Client Credentials

| Client | Client ID | Client Secret | Purpose |
|--------|-----------|---------------|---------|
| **Backend** | shop-manager-backend | shop-manager-backend-secret | Service-to-service authentication |
| **Frontend** | shop-manager-frontend | *(public client)* | User authentication flows |

---

## 🌐 Access URLs

### Docker Compose URLs

| Service | URL | Description |
|---------|-----|-------------|
| **Frontend** | http://localhost:3000 | React application |
| **Backend API** | http://localhost:8081 | REST API endpoints |
| **Swagger UI** | http://localhost:8081/swagger-ui.html | API documentation |
| **Keycloak Admin** | http://localhost:8080 | Keycloak administration |
| **Keycloak Realm** | http://localhost:8080/realms/shop-manager | Shop Manager realm |
| **MinIO Console** | http://localhost:9001 | Object storage management |
| **Kafka** | localhost:9092 | Kafka broker |
| **PostgreSQL** | localhost:5432 | Database server |
| **SonarQube** | http://localhost:9090 | Code quality (if enabled) |

### Helm Deployment URLs (with port-forwarding)

```bash
# Port forward commands
kubectl port-forward -n shop-manager svc/shop-manager-backend 8081:8081 &
kubectl port-forward -n shop-manager svc/shop-manager-frontend 3000:3000 &
kubectl port-forward -n shop-manager svc/shop-manager-keycloak 8080:8080 &
kubectl port-forward -n shop-manager svc/shop-manager-postgresql 5432:5432 &
```

Same URLs as Docker Compose when using port-forwarding.

### Health Check Endpoints

```bash
# Backend health
curl http://localhost:8081/actuator/health

# Backend metrics
curl http://localhost:8081/actuator/metrics

# Keycloak health
curl http://localhost:8080/health

# Frontend health (returns HTTP 200 if running)
curl http://localhost:3000

# Database health
pg_isready -h localhost -p 5432 -U shop
```

---

## 🐛 Troubleshooting

### Common Issues and Solutions

#### 1. Keycloak Realm Not Found (404 Error)

**Problem**: Frontend authentication fails with 404 on realm endpoint.

**Solution**:
```bash
# Check if keycloak-init completed successfully
docker-compose logs keycloak-init

# Or for Helm:
kubectl logs -n shop-manager job/shop-manager-keycloak-init

# Manually verify realm exists
curl http://localhost:8080/realms/shop-manager/.well-known/openid-configuration

# If realm doesn't exist, restart the init process
docker-compose restart keycloak-init
# Or for Helm: delete and let it recreate
kubectl delete job -n shop-manager shop-manager-keycloak-init
```

#### 2. Database Connection Issues

**Problem**: Backend fails to connect to database.

**Solution**:
```bash
# Check database status
docker-compose ps postgres
kubectl get pods -n shop-manager -l app.kubernetes.io/name=postgresql

# Test database connection
docker-compose exec postgres psql -U shop -d shopdb -c "SELECT 1;"

# Check database logs
docker-compose logs postgres
kubectl logs -n shop-manager -l app.kubernetes.io/name=postgresql
```

#### 3. Kafka Connection Issues

**Problem**: Analytics events not processing.

**Solution**:
```bash
# Check Kafka status
docker-compose ps kafka
kubectl get pods -n shop-manager -l app.kubernetes.io/name=kafka

# Test Kafka connectivity
docker-compose exec kafka kafka-topics --bootstrap-server localhost:9092 --list

# Check Kafka logs
docker-compose logs kafka
kubectl logs -n shop-manager -l app.kubernetes.io/name=kafka
```

#### 4. Frontend Build Issues

**Problem**: Frontend container fails to start.

**Solution**:
```bash
# Check if frontend directory exists
ls -la frontend/

# Build frontend manually
cd frontend
npm install
npm run build

# Check Docker build logs
docker-compose logs frontend
```

#### 5. Memory/Resource Issues

**Problem**: Services running out of memory.

**Solution**:
```bash
# For Docker Compose - increase memory limits
docker-compose -f docker-compose.yml -f docker-compose.override.yml up -d

# For Helm - increase resource limits
helm upgrade shop-manager ./helm-chart/shop-manager \
  --set resources.limits.memory=2Gi \
  --set postgresql.primary.resources.limits.memory=1Gi
```

### Debugging Commands

#### Docker Compose Debugging
```bash
# Check all container status
docker-compose ps

# Check container logs
docker-compose logs -f [service-name]

# Execute commands in running containers
docker-compose exec backend bash
docker-compose exec postgres psql -U shop -d shopdb

# Check container resource usage
docker stats

# Restart specific service
docker-compose restart [service-name]

# Rebuild and restart
docker-compose up -d --build [service-name]
```

#### Kubernetes Debugging
```bash
# Check pod status
kubectl get pods -n shop-manager

# Describe pod for detailed info
kubectl describe pod -n shop-manager [pod-name]

# Check pod logs
kubectl logs -n shop-manager [pod-name] -f

# Execute commands in pod
kubectl exec -it -n shop-manager [pod-name] -- bash

# Check events
kubectl get events -n shop-manager --sort-by='.lastTimestamp'

# Check services and endpoints
kubectl get svc,ep -n shop-manager
```

### Log Locations

#### Docker Compose Logs
```bash
# Application logs
docker-compose logs backend
docker-compose logs keycloak
docker-compose logs postgres

# Save logs to file
docker-compose logs backend > backend.log
```

#### Kubernetes Logs
```bash
# Application logs
kubectl logs -n shop-manager deployment/shop-manager
kubectl logs -n shop-manager deployment/shop-manager-keycloak

# Save logs to file
kubectl logs -n shop-manager deployment/shop-manager > backend.log
```

---

## 📊 Monitoring & Health Checks

### Health Check URLs

```bash
# Backend application health
curl http://localhost:8081/actuator/health

# Detailed health with components
curl http://localhost:8081/actuator/health/liveness
curl http://localhost:8081/actuator/health/readiness

# Application metrics
curl http://localhost:8081/actuator/metrics

# Prometheus metrics
curl http://localhost:8081/actuator/prometheus

# Application info
curl http://localhost:8081/actuator/info
```

### Database Health
```bash
# PostgreSQL connection test
docker-compose exec postgres pg_isready -U shop

# Database size and connections
docker-compose exec postgres psql -U shop -d shopdb -c "
  SELECT pg_size_pretty(pg_database_size('shopdb')) as db_size;
  SELECT count(*) as active_connections FROM pg_stat_activity;
"
```

### Keycloak Health
```bash
# Keycloak health endpoint
curl http://localhost:8080/health

# Realm configuration
curl http://localhost:8080/realms/shop-manager/.well-known/openid-configuration

# Admin API (requires token)
curl -H "Authorization: Bearer [admin-token]" \
  http://localhost:8080/admin/realms/shop-manager
```

### Performance Monitoring

#### Resource Usage
```bash
# Docker resource usage
docker stats

# Kubernetes resource usage
kubectl top pods -n shop-manager
kubectl top nodes
```

#### Application Metrics
```bash
# JVM metrics
curl http://localhost:8081/actuator/metrics/jvm.memory.used

# HTTP request metrics
curl http://localhost:8081/actuator/metrics/http.server.requests

# Database connection pool
curl http://localhost:8081/actuator/metrics/hikaricp.connections.active
```

---

## 🔄 Maintenance Operations

### Backup Operations

#### Database Backup
```bash
# Docker Compose backup
docker-compose exec postgres pg_dump -U shop shopdb > backup-$(date +%Y%m%d).sql

# Kubernetes backup
kubectl exec -n shop-manager deployment/shop-manager-postgresql -- pg_dump -U shop shopdb > backup-$(date +%Y%m%d).sql
```

#### Configuration Backup
```bash
# Export Keycloak realm
curl -H "Authorization: Bearer [admin-token]" \
  http://localhost:8080/admin/realms/shop-manager > realm-backup.json

# Backup Docker volumes
docker run --rm -v shop-manager_postgres_data:/data -v $(pwd):/backup alpine tar czf /backup/postgres-backup.tar.gz /data
```

### Updates and Upgrades

#### Docker Compose Updates
```bash
# Pull latest images
docker-compose pull

# Restart with new images
docker-compose up -d

# Update specific service
docker-compose up -d --build backend
```

#### Helm Upgrades
```bash
# Upgrade with new values
helm upgrade shop-manager ./helm-chart/shop-manager \
  --namespace shop-manager \
  --values custom-values.yaml

# Rollback if needed
helm rollback shop-manager [revision] --namespace shop-manager
```

### Scaling Operations

#### Docker Compose Scaling
```bash
# Scale backend service
docker-compose up -d --scale backend=3

# Scale with load balancer (requires additional configuration)
```

#### Kubernetes Scaling
```bash
# Scale backend deployment
kubectl scale deployment -n shop-manager shop-manager --replicas=5

# Scale frontend deployment
kubectl scale deployment -n shop-manager shop-manager-frontend --replicas=3

# Enable horizontal pod autoscaling
kubectl autoscale deployment -n shop-manager shop-manager --min=2 --max=10 --cpu-percent=70
```

---

## 🚀 Getting Started Checklist

### For Docker Compose
- [ ] Docker and Docker Compose installed
- [ ] Repository cloned
- [ ] Run `docker-compose up -d`
- [ ] Wait for services to be healthy
- [ ] Access frontend at http://localhost:3000
- [ ] Test login with admin@shopmanager.com / admin123

### For Helm
- [ ] Kubernetes cluster available
- [ ] Helm 3.x installed
- [ ] kubectl configured
- [ ] Add Bitnami repository
- [ ] Create namespace
- [ ] Run helm install command
- [ ] Port forward services
- [ ] Access frontend at http://localhost:3000

### Next Steps
- [ ] Explore the API at http://localhost:8081/swagger-ui.html
- [ ] Create test data using the admin account
- [ ] Configure additional features as needed
- [ ] Set up monitoring and alerting
- [ ] Plan production deployment strategy

---

This deployment guide provides comprehensive instructions for getting Shop Manager running in any environment. Choose the deployment method that best fits your needs and infrastructure requirements.