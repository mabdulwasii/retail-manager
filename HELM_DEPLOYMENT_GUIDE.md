# Helm Deployment Guide

## Overview

Shop Manager uses Helm for Kubernetes deployments with comprehensive configuration options and sensible defaults.

## Helm Chart Structure

```
helm-chart/shop-manager/
├── Chart.yaml              # Chart metadata
├── values.yaml             # Full configuration options
├── values-minimal.yaml     # Minimal configuration
├── templates/
│   ├── deployment.yaml     # Application deployment
│   ├── service.yaml        # Kubernetes service
│   ├── configmap.yaml      # Configuration map
│   ├── secret.yaml         # Secrets
│   └── _helpers.tpl        # Template helpers
```

## Quick Start with Minimal Configuration

### 1. Deploy with Defaults
```bash
# Deploy with minimal configuration
helm install shop-manager ./helm-chart/shop-manager -f ./helm-chart/shop-manager/values-minimal.yaml

# Or use default values.yaml (more comprehensive)
helm install shop-manager ./helm-chart/shop-manager
```

### 2. Check Deployment Status
```bash
# View deployment
kubectl get deployments

# View pods
kubectl get pods

# View services
kubectl get services

# View configmaps and secrets
kubectl get configmaps,secrets
```

## Configuration Options

### Basic Configuration (values-minimal.yaml)
```yaml
config:
  springProfile: "prod"

features:
  investment:
    enabled: true
  analytics:
    enabled: true
  fraud:
    enabled: false

secrets:
  database:
    password: "your-secure-password"
```

### Production Configuration
```yaml
# production-values.yaml
replicaCount: 3

image:
  repository: your-registry/shop-manager
  tag: "v1.0.0"
  pullPolicy: IfNotPresent

resources:
  limits:
    cpu: 2000m
    memory: 2Gi
  requests:
    cpu: 1000m
    memory: 1Gi

config:
  springProfile: "prod"

  database:
    poolSize: 20
    minIdle: 10

  security:
    tenantIsolation: true
    sessionTimeoutMinutes: 30

  audit:
    enabled: true
    retentionDays: 365

features:
  investment:
    enabled: true
  analytics:
    enabled: true
  fraud:
    enabled: true

secrets:
  database:
    url: "jdbc:postgresql://prod-db:5432/shopdb"
    username: "prod_user"
    password: "secure-production-password"

  keycloak:
    clientSecret: "keycloak-client-secret"

  email:
    enabled: true
    host: "smtp.company.com"
    username: "noreply@company.com"
    password: "email-password"
```

## Deployment Commands

### Install/Upgrade
```bash
# Install new deployment
helm install shop-manager ./helm-chart/shop-manager -f production-values.yaml

# Upgrade existing deployment
helm upgrade shop-manager ./helm-chart/shop-manager -f production-values.yaml

# Install/upgrade (idempotent)
helm upgrade --install shop-manager ./helm-chart/shop-manager -f production-values.yaml
```

### Environment-Specific Deployments
```bash
# Development
helm install shop-manager-dev ./helm-chart/shop-manager \
  --set config.springProfile=dev \
  --set features.fraud.enabled=false \
  --set replicaCount=1

# Staging
helm install shop-manager-staging ./helm-chart/shop-manager \
  --set config.springProfile=staging \
  --set features.fraud.enabled=true \
  --set replicaCount=2

# Production
helm install shop-manager-prod ./helm-chart/shop-manager \
  -f production-values.yaml \
  --set replicaCount=5
```

### Rollback
```bash
# List releases
helm list

# View release history
helm history shop-manager

# Rollback to previous version
helm rollback shop-manager

# Rollback to specific revision
helm rollback shop-manager 2
```

### Uninstall
```bash
# Uninstall release
helm uninstall shop-manager

# Keep history
helm uninstall shop-manager --keep-history
```

## Monitoring and Troubleshooting

### View Configuration
```bash
# Render templates without installing
helm template shop-manager ./helm-chart/shop-manager -f production-values.yaml

# Debug template rendering
helm install shop-manager ./helm-chart/shop-manager --debug --dry-run

# Get rendered manifests of installed release
helm get manifest shop-manager
```

### Check Application Health
```bash
# Port forward to access application
kubectl port-forward service/shop-manager 8081:8081

# Check application health
curl http://localhost:8081/actuator/health

# View application logs
kubectl logs -f deployment/shop-manager
```

### Configuration Management
```bash
# View current configuration
kubectl get configmap shop-manager-config -o yaml

# View secrets (base64 encoded)
kubectl get secret shop-manager-secret -o yaml

# Restart deployment after config changes
kubectl rollout restart deployment/shop-manager
```

## Best Practices

### 1. Use External Secrets
For production, use external secret management:
```yaml
secrets:
  database:
    password: ""  # Will use external secret
```

### 2. Override Specific Values
```bash
# Override single values
helm upgrade shop-manager ./helm-chart/shop-manager \
  --set config.audit.retentionDays=180 \
  --set features.fraud.enabled=true
```

### 3. Namespace Management
```bash
# Create namespace
kubectl create namespace shop-manager-prod

# Deploy to specific namespace
helm install shop-manager ./helm-chart/shop-manager \
  --namespace shop-manager-prod \
  --create-namespace
```

### 4. Resource Management
Monitor resource usage:
```bash
# View resource usage
kubectl top pods

# View resource limits
kubectl describe deployment shop-manager
```

## Default Values

The Helm templates provide sensible defaults for all configuration options. You only need to override values that differ from defaults. All templates use the pattern:

```yaml
{{ .Values.config.setting | default "sensible-default" | quote }}
```

This means you can deploy with minimal configuration and gradually add more specific settings as needed.