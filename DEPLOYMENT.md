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

#### ⚠️ Security Requirements

**IMPORTANT**: Never use weak or default passwords in production deployments!

#### Step 1: Create Secure Environment Configuration

```bash
# 1. Copy the secure environment template
cp .env.example .env

# 2. Generate strong passwords (20+ characters recommended)
openssl rand -base64 32  # Generate secure password
pwgen -s 32 1           # Alternative password generator

# 3. Edit .env with your secure credentials
nano .env
```

#### Step 2: Example Secure Configuration

```bash
# .env file - USE STRONG PASSWORDS IN PRODUCTION!
# Database Configuration
POSTGRES_USER=shop
POSTGRES_PASSWORD=xK9mP2$vL8qR7@nF5tW3zE6yU1bN4cA0  # Generate your own!
POSTGRES_DB=shopdb

# Keycloak Configuration  
KC_DB_USERNAME=shop
KC_DB_PASSWORD=xK9mP2$vL8qR7@nF5tW3zE6yU1bN4cA0  # Same as DB or different
KEYCLOAK_ADMIN=admin
KEYCLOAK_ADMIN_PASSWORD=B7@mQ4vK2$rP9wE6zF8xL3nT5cY1uA0s  # Generate your own!

# MinIO Configuration
MINIO_ROOT_USER=shopmanager
MINIO_ROOT_PASSWORD=N5tR8@qW2vE7mP1kL4xF9zC6yB3uA0sG  # Generate your own!

# SonarQube Configuration (if using sonar profile)
SONAR_DB_USER=sonar
SONAR_DB_PASSWORD=F3xL8@tP5qW2vE9mK7zR4nY6uC1bA0sG  # Generate your own!

# Application Profile
SPRING_PROFILES_ACTIVE=production
```

#### Security Best Practices

- **Password Requirements**: Minimum 16 characters, mix of uppercase, lowercase, numbers, and symbols
- **Unique Passwords**: Use different passwords for each service
- **Password Storage**: Use a password manager to generate and store credentials
- **Environment Isolation**: Use different passwords for staging/production environments
- **Regular Rotation**: Change passwords quarterly or when team members leave
- **Never Commit**: Ensure `.env` is in `.gitignore` (already configured)

#### Quick Security Checklist

- [ ] Generated unique passwords for each service (16+ characters)
- [ ] Different passwords for staging vs production
- [ ] Passwords stored in secure password manager
- [ ] `.env` file not committed to version control
- [ ] Team members have separate admin accounts (not shared)

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

**✅ All credentials verified and working as of latest deployment.**

After deployment, the following pre-configured test users are available in the `shop-manager` Keycloak realm:

### User Accounts (Keycloak Realm: shop-manager)

| Role | Username | Password | Keycloak Role | Description | Default Tenant/Shop |
|------|----------|----------|---------------|-------------|-------------------|
| **System Admin** | admin@shopmanager.com | admin123 | TENANT_ADMIN | System Administrator with full access | default-tenant/default-shop |
| **Shop Manager** | manager@shopmanager.com | manager123 | SHOP_MANAGER | Shop operations, inventory, sales management | default-tenant/default-shop |
| **Shop Employee** | employee@shopmanager.com | employee123 | SHOP_EMPLOYEE | Sales transactions, basic inventory access | default-tenant/default-shop |
| **Investor** | investor@shopmanager.com | investor123 | INVESTOR | Investment tracking, profit reports | default-tenant |
| **Customer** | customer@shopmanager.com | customer123 | CUSTOMER | Purchase history, receipts | default-tenant |

> **🔑 Authentication Testing**: Use any of these credentials to test the login flow at http://localhost:3000

### Service Infrastructure Accounts

| Service | Username | Password | Purpose | Access URL |
|---------|----------|----------|---------|------------|
| **PostgreSQL** | shop | shop | Application database access | localhost:5432/shopdb |
| **Keycloak Admin Console** | admin | admin | Keycloak realm administration | http://localhost:8080 |
| **MinIO Object Storage** | minioadmin | minioadmin | File storage management | http://localhost:9001 |

### API Client Configuration (OAuth2/OpenID Connect)

| Client | Client ID | Client Secret | Flow Type | Purpose |
|--------|-----------|---------------|-----------|---------|
| **Backend Service** | shop-manager-backend | shop-manager-backend-secret | Client Credentials | Service-to-service API authentication |
| **Frontend App** | shop-manager-frontend | *(public client)* | Authorization Code + PKCE | User authentication and authorization |

### Authentication Endpoints (Keycloak)

| Endpoint | URL | Purpose |
|----------|-----|---------|
| **Realm Info** | http://localhost:8080/realms/shop-manager | Public realm configuration |
| **OpenID Config** | http://localhost:8080/realms/shop-manager/.well-known/openid-configuration | OIDC discovery document |
| **Authorization** | http://localhost:8080/realms/shop-manager/protocol/openid-connect/auth | User authentication endpoint |
| **Token** | http://localhost:8080/realms/shop-manager/protocol/openid-connect/token | Token exchange endpoint |
| **User Info** | http://localhost:8080/realms/shop-manager/protocol/openid-connect/userinfo | User profile endpoint |

> **⚠️ Development Note**: SSL requirement has been disabled for the `shop-manager` realm to enable HTTP access during development. In production, ensure HTTPS is properly configured.

---

## 🌐 Access URLs

### Docker Compose URLs

**✅ All services verified and accessible as of latest deployment.**

| Service | URL | Status | Description |
|---------|-----|--------|-------------|
| **Frontend App** | http://localhost:3000 | 🟢 Healthy | React application with authentication |
| **Backend API** | http://localhost:8081 | 🟢 Healthy | REST API endpoints |
| **Health Check** | http://localhost:8081/actuator/health | 🟢 UP | Backend health monitoring |
| **Keycloak Admin** | http://localhost:8080 | 🟢 Running | Keycloak administration console |
| **Shop Manager Realm** | http://localhost:8080/realms/shop-manager | 🟢 Active | Authentication realm (SSL disabled) |
| **MinIO Console** | http://localhost:9001 | 🟢 Available | Object storage management |
| **PostgreSQL** | localhost:5432 | 🟢 Healthy | Database server (shopdb) |
| **Kafka** | localhost:9093 | 🟢 Healthy | Kafka broker (KRaft mode, fixed permissions) |
| **SonarQube** | http://localhost:9090 | 🟢 Optional | Code quality analysis |

> **⚠️ Important Notes**:
> - Kafka port changed to 9093 to avoid conflicts with existing services
> - Kafka runs as root user to fix KRaft mode permission issues
> - Keycloak realm configured for HTTP access in development
> - Frontend includes proxy for API calls to backend:8081

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