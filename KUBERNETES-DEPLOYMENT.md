# Shop Manager Kubernetes Deployment Guide

## Prerequisites
- Docker Desktop with Kubernetes enabled OR access to a Kubernetes cluster
- kubectl CLI installed and configured
- Helm 3.x installed
- Docker images built locally (for local deployment)

## Quick Start

### 1. Build Docker Images Locally
```bash
# Build backend image
docker build -f backend/Dockerfile -t shop-manager:latest .

# Build frontend image
docker build -f frontend/Dockerfile -t shop-manager-frontend:latest .
```

### 2. Deploy with Helm

#### Option A: Deploy Everything (App + Infrastructure)
```bash
# Create namespace
kubectl create namespace shop-manager

# Deploy with Helm
helm install shop-manager ./helm-chart/shop-manager \
  -f ./helm-chart/shop-manager/values-simple.yaml \
  -n shop-manager \
  --wait --timeout=10m
```

#### Option B: Deploy Infrastructure Only
```bash
# Create namespace
kubectl create namespace shop-manager

# Deploy only infrastructure services
helm install shop-manager ./helm-chart/shop-manager \
  -f ./helm-chart/shop-manager/values-infra.yaml \
  -n shop-manager \
  --wait --timeout=10m
```

### 3. Verify Deployment
```bash
# Check all pods are running
kubectl get pods -n shop-manager

# Expected output:
# NAME                                       READY   STATUS
# shop-manager-postgresql-0                  1/1     Running
# shop-manager-kafka-controller-0            1/1     Running
# shop-manager-kafka-controller-1            1/1     Running
# shop-manager-kafka-controller-2            1/1     Running
# shop-manager-keycloak-0                    1/1     Running
```

## Service Access

### Local Access (Port Forwarding)

#### PostgreSQL Database
```bash
kubectl port-forward svc/shop-manager-postgresql 5432:5432 -n shop-manager
# Connection: postgresql://shop:shop@localhost:5432/shopdb
```

#### Keycloak Admin Console
```bash
kubectl port-forward svc/shop-manager-keycloak 8080:80 -n shop-manager
# Access: http://localhost:8080
# Admin: admin / admin
```

#### Kafka Broker
```bash
kubectl port-forward svc/shop-manager-kafka 9092:9092 -n shop-manager
# Bootstrap server: localhost:9092
```

#### Backend API (if deployed)
```bash
kubectl port-forward svc/shop-manager 8081:8081 -n shop-manager
# API: http://localhost:8081
# Swagger: http://localhost:8081/swagger-ui.html
```

#### Frontend UI (if deployed)
```bash
kubectl port-forward svc/shop-manager-frontend 3000:3000 -n shop-manager
# UI: http://localhost:3000
```

## Configuration

### Values Files

- `values.yaml` - Full configuration with all options
- `values-simple.yaml` - Simplified configuration for testing
- `values-infra.yaml` - Infrastructure services only
- `values-minimal.yaml` - Minimal viable configuration

### Key Configuration Options

```yaml
# Enable/disable services
postgresql:
  enabled: true
kafka:
  enabled: true
keycloak:
  enabled: true

# Frontend configuration
frontend:
  enabled: true
  image:
    repository: shop-manager-frontend
    tag: latest

# Backend configuration
image:
  repository: shop-manager
  tag: latest
```

## Troubleshooting

### Check Pod Status
```bash
kubectl describe pod <pod-name> -n shop-manager
kubectl logs <pod-name> -n shop-manager
```

### Common Issues

#### 1. ImagePullBackOff
**Problem**: Kubernetes cannot pull Docker images
**Solution**:
- For local deployment, ensure images are built locally
- For remote clusters, push images to a registry

#### 2. CrashLoopBackOff
**Problem**: Application crashes on startup
**Solution**: Check logs for configuration errors
```bash
kubectl logs <pod-name> -n shop-manager --previous
```

#### 3. Pending Pods
**Problem**: Pods stuck in Pending state
**Solution**: Check PVC and resource availability
```bash
kubectl describe pod <pod-name> -n shop-manager
kubectl get pvc -n shop-manager
```

## Clean Up

### Uninstall Helm Release
```bash
helm uninstall shop-manager -n shop-manager
```

### Delete Namespace
```bash
kubectl delete namespace shop-manager
```

### Force Delete Stuck Resources
```bash
kubectl delete namespace shop-manager --grace-period=0 --force
```

## Production Deployment

For production deployments:

1. Use external container registry
2. Configure proper resource limits
3. Enable autoscaling
4. Set up ingress controllers
5. Configure TLS/SSL
6. Use external databases
7. Set up monitoring and logging

See `PRODUCTION-DEPLOYMENT.md` for detailed production configuration.

## Health Checks

### Service Health Endpoints
- Backend: `http://<backend-url>:8081/actuator/health`
- Keycloak: `http://<keycloak-url>:8080/health/ready`
- PostgreSQL: Use `pg_isready` command

### Kubernetes Probes
All services are configured with:
- Liveness probes - Restarts unhealthy containers
- Readiness probes - Controls traffic routing

## Next Steps

1. Configure Keycloak realm and users
2. Import test data
3. Set up monitoring with Prometheus/Grafana
4. Configure backup strategies
5. Implement CI/CD pipeline

## Support

For issues or questions:
- Check logs: `kubectl logs -n shop-manager <pod-name>`
- View events: `kubectl get events -n shop-manager`
- Describe resources: `kubectl describe <resource> -n shop-manager`