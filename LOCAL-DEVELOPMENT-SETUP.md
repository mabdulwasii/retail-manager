# Local Development Setup - Shop Manager

This guide provides instructions for setting up Shop Manager for local development using Kubernetes on Docker Desktop.

## Prerequisites

- Docker Desktop with Kubernetes enabled
- kubectl configured to use docker-desktop context
- Helm 3.x installed
- kubectx (optional, for context switching)

## Quick Setup

### 1. Switch to Docker Desktop Context

```bash
# List available contexts
kubectx

# Switch to docker-desktop
kubectx docker-desktop
```

### 2. Install Nginx Ingress Controller

```bash
# Install ingress-nginx controller
kubectl apply -f https://raw.githubusercontent.com/kubernetes/ingress-nginx/controller-v1.8.2/deploy/static/provider/cloud/deploy.yaml

# Wait for deployment
kubectl wait --namespace ingress-nginx \
  --for=condition=ready pod \
  --selector=app.kubernetes.io/component=controller \
  --timeout=90s
```

### 3. Deploy Shop Manager

```bash
# Navigate to helm chart directory
cd helm-chart/shop-manager

# Deploy using Helm
helm upgrade --install shop-manager . \
  --namespace shop-manager-fixed \
  --create-namespace \
  --wait \
  --timeout=10m
```

### 4. Configure Local DNS

```bash
# Run the configuration script
./configure-local-dns.sh

# Or manually add DNS entries to /etc/hosts
sudo tee -a /etc/hosts << 'EOF'
# Shop Manager Services - Docker Desktop Ingress
127.0.0.1 shop-manager.local
127.0.0.1 api.shop-manager.local
127.0.0.1 auth.shop-manager.local
EOF
```

## Access Services

### Via Domain Names (Recommended)

After running `./configure-local-dns.sh`, you'll get the HTTPS port (typically 32xxx). Access services using:

- **Frontend**: https://shop-manager.local:32286
- **Backend API**: https://api.shop-manager.local:32286/actuator/health
- **Keycloak**: https://auth.shop-manager.local:32286 (when available)

### Via NodePort (Alternative)

Direct access without DNS configuration:

- **Frontend**: http://localhost:30436
- **Backend API**: http://localhost:30081/actuator/health
- **Ingress HTTP**: http://localhost:30453
- **Ingress HTTPS**: https://localhost:32286

## Verification

### Check Service Status

```bash
# Check all pods
kubectl get pods -n shop-manager-fixed

# Check ingress configuration
kubectl get ingress -n shop-manager-fixed

# Check services
kubectl get svc -n shop-manager-fixed
```

### Test Connectivity

```bash
# Test frontend
curl -k https://shop-manager.local:32286/

# Test backend API
curl -k https://api.shop-manager.local:32286/actuator/health

# Test with verbose output
curl -k -v https://api.shop-manager.local:32286/actuator/health
```

## Architecture Overview

### Services Deployed

1. **shop-manager** (Backend)
   - Spring Boot application
   - REST API endpoints
   - Database connectivity

2. **shop-manager-frontend** (Frontend)
   - React TypeScript application
   - Keycloak authentication integration

3. **PostgreSQL Database**
   - Primary data storage
   - Flyway migrations

4. **Kafka**
   - Event streaming platform
   - Analytics processing

5. **MinIO**
   - S3-compatible object storage
   - Backup storage

6. **Keycloak** (Optional)
   - Identity and access management
   - SSO authentication

### Network Configuration

- **Ingress Controller**: nginx-ingress (NodePort on docker-desktop)
- **TLS**: Self-signed certificates via cert-manager
- **DNS**: Local /etc/hosts entries for domain resolution

## Troubleshooting

### Common Issues

#### 1. Service Returning 503 Errors

```bash
# Check pod status
kubectl get pods -n shop-manager-fixed

# Check pod logs
kubectl logs -l app.kubernetes.io/name=shop-manager -n shop-manager-fixed

# Check ingress backends
kubectl describe ingress -n shop-manager-fixed
```

#### 2. DNS Resolution Not Working

```bash
# Verify /etc/hosts entries
grep shop-manager /etc/hosts

# Test with Host header
curl -k -H "Host: shop-manager.local" https://localhost:32286/
```

#### 3. Ingress Controller Issues

```bash
# Check ingress controller status
kubectl get pods -n ingress-nginx

# Check service type (should be NodePort for docker-desktop)
kubectl get svc ingress-nginx-controller -n ingress-nginx

# If LoadBalancer is pending, patch to NodePort
kubectl patch svc ingress-nginx-controller -n ingress-nginx -p '{"spec":{"type":"NodePort"}}'
```

#### 4. Keycloak Not Running

```bash
# Check StatefulSet
kubectl get statefulset -n shop-manager-fixed

# Check Keycloak logs
kubectl logs -l app.kubernetes.io/name=keycloak -n shop-manager-fixed

# Reinstall if needed
helm upgrade shop-manager . --namespace shop-manager-fixed
```

### Log Analysis

```bash
# Backend application logs
kubectl logs -f deployment/shop-manager -n shop-manager-fixed

# Frontend logs
kubectl logs -f deployment/shop-manager-frontend -n shop-manager-fixed

# Database logs
kubectl logs -f statefulset/shop-manager-postgresql -n shop-manager-fixed

# Ingress controller logs
kubectl logs -f deployment/ingress-nginx-controller -n ingress-nginx
```

## Development Workflow

### Making Changes

1. **Code Changes**: Edit source code locally
2. **Build Images**: Rebuild Docker images
3. **Deploy Updates**: Use Helm upgrade
4. **Test**: Verify functionality via ingress

### Helm Operations

```bash
# Upgrade deployment
helm upgrade shop-manager . --namespace shop-manager-fixed

# Check deployment status
helm status shop-manager -n shop-manager-fixed

# Roll back if needed
helm rollback shop-manager 1 -n shop-manager-fixed

# Uninstall completely
helm uninstall shop-manager -n shop-manager-fixed
```

### Configuration Changes

- **Values**: Edit `values.yaml` for configuration changes
- **Secrets**: Update secret values for passwords and keys
- **Resources**: Adjust CPU/memory limits as needed

## Security Notes

- **TLS**: All ingress traffic uses HTTPS with self-signed certificates
- **Secrets**: Passwords stored in Kubernetes secrets
- **Authentication**: Keycloak provides SSO authentication
- **Network**: Services communicate internally via ClusterIP

## Performance Considerations

- **Resource Limits**: Configured for development workloads
- **Database**: PostgreSQL with persistent storage
- **Caching**: Application-level caching enabled
- **Monitoring**: Health checks and metrics endpoints available

## Next Steps

1. **Complete Keycloak Setup**: Resolve StatefulSet deployment issues
2. **Frontend Authentication**: Test end-to-end auth flow
3. **API Testing**: Verify all REST endpoints work correctly
4. **Production Configuration**: Prepare for production deployment

For production deployment, see `PRODUCTION-DEPLOYMENT.md`.