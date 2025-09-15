# Docker Compose & Kubernetes Usage Guide

## Docker Compose Deployment

The project uses a single `docker-compose.yml` file for all services with profiles for optional services.

## Available Services

### Core Services (Default Profile)
- **PostgreSQL**: Database server
- **Keycloak**: Authentication server
- **Kafka**: Event streaming platform
- **MinIO**: Object storage for backups
- **Backend**: Shop Manager application

### Optional Services

#### SonarQube (sonar profile)
- **SonarQube**: Code quality analysis
- **SonarQube DB**: PostgreSQL database for SonarQube

## Usage Commands

### Start All Core Services
```bash
docker-compose up -d
```

### Start with SonarQube
```bash
docker-compose --profile sonar up -d
```

### Start Only Specific Services
```bash
# Just infrastructure
docker-compose up -d postgres keycloak kafka minio

# Add backend
docker-compose up -d postgres keycloak kafka minio backend

# Add SonarQube
docker-compose --profile sonar up -d sonarqube sonarqube-db
```

### Service URLs
- **Backend API**: http://localhost:8081
- **Keycloak Admin**: http://localhost:8080 (admin/admin)
- **MinIO Console**: http://localhost:9001 (minioadmin/minioadmin)
- **SonarQube**: http://localhost:9090 (admin/admin)

### Stop Services
```bash
# Stop all services
docker-compose down

# Stop SonarQube services
docker-compose --profile sonar down

# Stop and remove volumes
docker-compose down -v
```

### View Logs
```bash
# All services
docker-compose logs -f

# Specific service
docker-compose logs -f backend

# SonarQube logs
docker-compose --profile sonar logs -f sonarqube
```

## Code Quality Analysis

Use the provided script for SonarQube operations:

```bash
# Interactive script
./sonar-analysis.sh

# Or manually:
docker-compose --profile sonar up -d
cd backend
./mvnw clean verify sonar:sonar
```

## Environment Variables

All services support environment variable overrides:

```bash
# Override database password
POSTGRES_PASSWORD=newpassword docker-compose up -d

# Override application profile
SPRING_PROFILES_ACTIVE=dev docker-compose up -d backend
```

## Kubernetes Deployment

### Prerequisites
- Kubernetes cluster running (Docker Desktop, minikube, etc.)
- kubectl configured and connected to cluster
- Helm 3.x installed

### Deploy with NodePort (Local Testing)

#### Option 1: Full Helm Deployment (Complex)
```bash
# Create namespace
kubectl create namespace shop-manager-test

# Deploy with Helm using NodePort values
cd helm-chart
helm install shop-manager-test ./shop-manager \
  --namespace shop-manager-test \
  --values shop-manager/values-nodeport.yaml

# Check deployment status
kubectl get pods -n shop-manager-test
kubectl get services -n shop-manager-test
```

#### Option 2: Simplified Deployment (Recommended)
```bash
# Create namespace
kubectl create namespace shop-manager-test

# Deploy simplified working services
kubectl apply -n shop-manager-test -f - <<EOF
apiVersion: v1
kind: Service
metadata:
  name: keycloak-simple
spec:
  type: NodePort
  ports:
  - port: 8080
    targetPort: 8080
    nodePort: 30080
  selector:
    app: keycloak-simple
---
apiVersion: apps/v1
kind: Deployment
metadata:
  name: keycloak-simple
spec:
  replicas: 1
  selector:
    matchLabels:
      app: keycloak-simple
  template:
    metadata:
      labels:
        app: keycloak-simple
    spec:
      containers:
      - name: keycloak
        image: quay.io/keycloak/keycloak:24.0.1
        env:
        - name: KEYCLOAK_ADMIN
          value: admin
        - name: KEYCLOAK_ADMIN_PASSWORD
          value: admin
        - name: KC_HOSTNAME
          value: localhost
        - name: KC_HOSTNAME_PORT
          value: "30080"
        - name: KC_HTTP_ENABLED
          value: "true"
        - name: KC_HOSTNAME_STRICT_HTTPS
          value: "false"
        args: ["start-dev"]
        ports:
        - containerPort: 8080
---
apiVersion: v1
kind: Service
metadata:
  name: frontend-simple
spec:
  type: NodePort
  ports:
  - port: 80
    targetPort: 80
    nodePort: 30001
  selector:
    app: frontend-simple
---
apiVersion: apps/v1
kind: Deployment
metadata:
  name: frontend-simple
spec:
  replicas: 1
  selector:
    matchLabels:
      app: frontend-simple
  template:
    metadata:
      labels:
        app: frontend-simple
    spec:
      containers:
      - name: frontend
        image: shop-manager-frontend:k8s-working
        imagePullPolicy: IfNotPresent
        ports:
        - containerPort: 80
EOF

# Wait for deployments
kubectl rollout status deployment/keycloak-simple -n shop-manager-test
kubectl rollout status deployment/frontend-simple -n shop-manager-test
```

### Kubernetes Service URLs

**Current working NodePort mappings:**
- **Frontend**: http://localhost:30001 ✅ (Working with authentication)
- **Keycloak Admin**: http://localhost:30080 ✅ (admin/admin)
- **Backend API**: Not deployed (frontend-only demo)

### Monitor Deployment

```bash
# Watch pod status
kubectl get pods -n shop-manager-test -w

# Check pod logs
kubectl logs -f deployment/shop-manager-test -n shop-manager-test
kubectl logs -f deployment/shop-manager-test-frontend -n shop-manager-test

# Check Keycloak logs
kubectl logs -f statefulset/shop-manager-test-keycloak -n shop-manager-test
```

### Uninstall Kubernetes Deployment

```bash
# Uninstall Helm release
helm uninstall shop-manager-test --namespace shop-manager-test

# Delete namespace (optional)
kubectl delete namespace shop-manager-test
```

### Scale Deployment

```bash
# Scale backend pods
kubectl scale deployment shop-manager-test --replicas=3 -n shop-manager-test

# Scale frontend pods
kubectl scale deployment shop-manager-test-frontend --replicas=2 -n shop-manager-test
```

## Authentication

Both Docker Compose and Kubernetes deployments use the same authentication system.

### Test Credentials

```bash
# System Administrator (Full Access)
Username: admin@shopmanager.com
Password: admin123
Role: TENANT_ADMIN

# Shop Manager (Operations)
Username: manager@shopmanager.com
Password: manager123
Role: SHOP_MANAGER

# Shop Employee (Limited Access)
Username: employee@shopmanager.com
Password: employee123
Role: SHOP_EMPLOYEE

# Investor (Reports & Analytics)
Username: investor@shopmanager.com
Password: investor123
Role: INVESTOR

# Customer (Purchase History)
Username: customer@shopmanager.com
Password: customer123
Role: CUSTOMER
```

### Authentication Flow

1. Visit frontend URL (localhost:3001 for Docker Compose, localhost:31414 for Kubernetes)
2. You'll be redirected to the Keycloak login page
3. Use any of the test credentials above
4. After login, you'll be redirected back to the Shop Manager application

## Troubleshooting

### Docker Compose Issues

```bash
# Check service health
docker-compose ps

# Restart specific service
docker-compose restart keycloak

# View logs for debugging
docker-compose logs -f keycloak

# Reset everything (removes volumes)
docker-compose down -v && docker-compose up -d
```

### Kubernetes Issues

```bash
# Check pod status and events
kubectl describe pod <pod-name> -n shop-manager-test

# Check service endpoints
kubectl get endpoints -n shop-manager-test

# Port forward for debugging
kubectl port-forward service/shop-manager-test 8081:8081 -n shop-manager-test

# Check persistent volumes
kubectl get pv,pvc -n shop-manager-test
```

### Authentication Issues

#### Common Problems and Solutions

1. **"Invalid parameter: redirect_uri" Error**
   - **Cause**: Frontend trying to use wrong Keycloak port or redirect URI not configured
   - **Solution**:
     ```bash
     # Check what realm is imported
     curl http://localhost:8080/realms/shop-manager/.well-known/openid-connect  # Docker Compose
     curl http://localhost:30080/realms/shop-manager/.well-known/openid-connect # Kubernetes

     # If realm missing, reimport it
     # For Docker Compose - realm auto-imported
     # For Kubernetes - use Keycloak admin console at localhost:30080
     ```

2. **Frontend loads but stuck on loading screen**
   - **Cause**: Frontend trying to connect to wrong Keycloak port
   - **Docker Compose**: Should connect to port 8080
   - **Kubernetes**: Should connect to port 30080
   - **Solution**: Verify correct frontend image is deployed

3. **CSP frame-ancestors error**
   - **Cause**: Keycloak blocking iframe embedding
   - **Solution**: Fixed in current configuration using `onLoad: 'login-required'`

4. **Browser cache issues**
   - **Solution**: Clear browser cache completely (Ctrl+Shift+Delete / Cmd+Shift+Delete)
   - Use incognito/private browsing mode for testing

#### Frontend Images

- **Docker Compose**: Uses `shop-manager-frontend:latest` (connects to port 8080)
- **Kubernetes**: Uses `shop-manager-frontend:k8s-working` (connects to port 30080)

#### Realm Import Status

- **Docker Compose**: ✅ Auto-imported with realm init container
- **Kubernetes**: ✅ Manually imported with correct redirect URIs for port 30001