# 🚀 Shop Manager Production Deployment Guide

## 🎯 Overview

This guide provides step-by-step instructions for deploying Shop Manager on-site using Helm and Kubernetes with automated SSL certificate installation for enterprise environments.

## 📋 Prerequisites

### Infrastructure Requirements
- **Kubernetes Cluster**: v1.25+ (on-premise or cloud)
- **Helm**: v3.8+ installed and configured
- **Storage Class**: Dynamic provisioning for persistent volumes
- **Ingress Controller**: NGINX Ingress or equivalent
- **DNS**: Local DNS resolution or external DNS management

### Resource Requirements
- **CPU**: 4 cores minimum (8 cores recommended)
- **Memory**: 8GB minimum (16GB recommended)
- **Storage**: 50GB persistent storage
- **Network**: Load balancer or NodePort access

## 🔧 Installation Steps

### Step 1: Prepare Kubernetes Environment

```bash
# Verify cluster access
kubectl cluster-info

# Create namespace
kubectl create namespace shop-manager

# Verify storage class exists
kubectl get storageclass
```

### Step 2: Install cert-manager (Required for SSL)

```bash
# Add cert-manager repository
helm repo add jetstack https://charts.jetstack.io
helm repo update

# Install cert-manager with CRDs
helm install cert-manager jetstack/cert-manager \
  --namespace cert-manager \
  --create-namespace \
  --version v1.13.2 \
  --set installCRDs=true \
  --set securityContext.runAsNonRoot=false \
  --set containerSecurityContext.runAsNonRoot=false

# Verify installation
kubectl get pods -n cert-manager
kubectl get crd | grep cert-manager
```

### Step 3: Configure Domain Names

```bash
# Example: Add to /etc/hosts or configure DNS
echo "192.168.1.100 shop-manager.local" >> /etc/hosts
echo "192.168.1.100 api.shop-manager.local" >> /etc/hosts
echo "192.168.1.100 auth.shop-manager.local" >> /etc/hosts
```

Replace `192.168.1.100` with your actual cluster IP or load balancer IP.

### Step 4: Create Production Values File

Create `production-values.yaml`:

```yaml
# Production Configuration for Shop Manager
global:
  environment: production
  domain: "shop-manager.local"  # Replace with your domain

# Database Configuration
postgresql:
  enabled: true
  auth:
    postgresPassword: "YOUR_SECURE_DB_PASSWORD"  # Change this!
    database: "shopmanager"
  primary:
    persistence:
      enabled: true
      size: 20Gi
      storageClass: "your-storage-class"  # Update this

# Backend Configuration
backend:
  image:
    repository: princely/shop-manager-backend
    tag: "clean-roles"
  replicaCount: 2
  resources:
    requests:
      memory: "1Gi"
      cpu: "500m"
    limits:
      memory: "2Gi"
      cpu: "1000m"
  env:
    SPRING_PROFILES_ACTIVE: "production"

# Frontend Configuration
frontend:
  image:
    repository: princely/shop-manager-frontend
    tag: "latest"
  replicaCount: 2
  resources:
    requests:
      memory: "512Mi"
      cpu: "250m"
    limits:
      memory: "1Gi"
      cpu: "500m"

# Keycloak Configuration
keycloak:
  auth:
    adminPassword: "YOUR_SECURE_KEYCLOAK_PASSWORD"  # Change this!
  postgresql:
    auth:
      password: "YOUR_SECURE_KEYCLOAK_DB_PASSWORD"  # Change this!
  production: true
  proxy: edge
  httpRelativePath: "/"

# SSL/TLS Configuration with Automated Installation
tls:
  enabled: true
  issuer: local-ca-issuer
  localCertInstallation:
    enabled: true
    platforms: [macOS, linux, windows]
    installMethods:
      macOS:
        keychain: true
        trust: trustRoot
        browsers: [safari, chrome, firefox]
      linux:
        distributions: [debian, redhat]
        updateCaStore: true
      windows:
        scope: LocalMachine
        store: Root
    postInstall:
      verifyInstallation: true
      testConnectivity: true
      generateInstructions: true

# Ingress Configuration
ingress:
  enabled: true
  className: "nginx"  # Update based on your ingress controller
  annotations:
    nginx.ingress.kubernetes.io/ssl-redirect: "true"
    nginx.ingress.kubernetes.io/force-ssl-redirect: "true"
  tls:
    enabled: true

# Monitoring and Logging
monitoring:
  enabled: true
  prometheus:
    enabled: true
  grafana:
    enabled: true

# Backup Configuration
backup:
  enabled: true
  schedule: "0 2 * * *"  # Daily at 2 AM
  retention: "30d"
  storage:
    type: "s3"  # or "local", "gcs", "azure"
    # Configure based on your storage provider
```

### Step 5: Deploy Shop Manager

```bash
# Navigate to helm chart directory
cd helm-chart

# Deploy with production values
helm install shop-manager ./shop-manager \
  --namespace shop-manager \
  --values production-values.yaml \
  --timeout 10m

# Monitor deployment
kubectl get pods -n shop-manager -w
```

### Step 6: Verify Deployment

```bash
# Check all pods are running
kubectl get pods -n shop-manager

# Check ingress configuration
kubectl get ingress -n shop-manager

# Check certificates
kubectl get certificates -n shop-manager

# Check certificate installation job
kubectl get jobs -n shop-manager | grep cert-installer
```

### Step 7: Install SSL Certificates (Automated)

The deployment automatically creates a certificate installation job. Extract and run the installation scripts:

```bash
# Wait for cert-installer job to complete
kubectl wait --for=condition=complete job/shop-manager-cert-installer -n shop-manager --timeout=300s

# Extract installation assets
kubectl cp shop-manager/$(kubectl get pods -n shop-manager -l job-name=shop-manager-cert-installer -o jsonpath='{.items[0].metadata.name}'):/shared/install-macos.sh ./install-macos.sh
kubectl cp shop-manager/$(kubectl get pods -n shop-manager -l job-name=shop-manager-cert-installer -o jsonpath='{.items[0].metadata.name}'):/shared/install-linux.sh ./install-linux.sh
kubectl cp shop-manager/$(kubectl get pods -n shop-manager -l job-name=shop-manager-cert-installer -o jsonpath='{.items[0].metadata.name}'):/shared/install-windows.ps1 ./install-windows.ps1
kubectl cp shop-manager/$(kubectl get pods -n shop-manager -l job-name=shop-manager-cert-installer -o jsonpath='{.items[0].metadata.name}'):/shared/shop-manager-ca.crt ./shop-manager-ca.crt
kubectl cp shop-manager/$(kubectl get pods -n shop-manager -l job-name=shop-manager-cert-installer -o jsonpath='{.items[0].metadata.name}'):/shared/INSTALLATION_INSTRUCTIONS.md ./INSTALLATION_INSTRUCTIONS.md

# Install certificates based on your platform
# macOS:
chmod +x install-macos.sh
sudo ./install-macos.sh  # System-wide installation

# Linux:
chmod +x install-linux.sh
sudo ./install-linux.sh

# Windows:
# Run install-windows.ps1 as Administrator in PowerShell
```

### Step 8: Access Applications

After certificate installation, access the applications:

```bash
# Frontend
curl -I https://shop-manager.local

# Backend API
curl -I https://api.shop-manager.local/actuator/health

# Keycloak
curl -I https://auth.shop-manager.local
```

**Browser Access:**
- **Frontend**: https://shop-manager.local
- **Admin Console**: https://auth.shop-manager.local/admin
- **API Documentation**: https://api.shop-manager.local/swagger-ui.html

## 🔐 Security Configuration

### Default Credentials (Change Immediately!)

**Keycloak Admin:**
- Username: `admin`
- Password: `YOUR_SECURE_KEYCLOAK_PASSWORD` (from values.yaml)

**Test Users** (for development/testing):
- Admin: `admin@shopmanager.com` / `admin123`
- Manager: `manager@shopmanager.com` / `manager123`
- Employee: `employee@shopmanager.com` / `employee123`

### Security Checklist

- [ ] Change all default passwords
- [ ] Configure RBAC policies
- [ ] Enable audit logging
- [ ] Configure backup encryption
- [ ] Set up monitoring alerts
- [ ] Review ingress security headers
- [ ] Configure network policies (if required)

## 📊 Monitoring and Maintenance

### Health Checks

```bash
# Application health
kubectl get pods -n shop-manager
kubectl logs -n shop-manager deployment/shop-manager-backend
kubectl logs -n shop-manager deployment/shop-manager-frontend

# Certificate status
kubectl describe certificates -n shop-manager

# Database connectivity
kubectl exec -n shop-manager deployment/shop-manager-backend -- pg_isready -h shop-manager-postgresql
```

### Backup Verification

```bash
# Check backup jobs
kubectl get cronjobs -n shop-manager

# Verify last backup
kubectl logs -n shop-manager $(kubectl get pods -n shop-manager -l app=backup -o jsonpath='{.items[-1].metadata.name}')
```

### Scaling

```bash
# Scale backend
kubectl scale deployment shop-manager-backend -n shop-manager --replicas=3

# Scale frontend
kubectl scale deployment shop-manager-frontend -n shop-manager --replicas=3

# Update with Helm
helm upgrade shop-manager ./shop-manager \
  --namespace shop-manager \
  --values production-values.yaml \
  --set backend.replicaCount=3 \
  --set frontend.replicaCount=3
```

## 🔄 Upgrade Process

### Minor Updates

```bash
# Update Helm chart
helm upgrade shop-manager ./shop-manager \
  --namespace shop-manager \
  --values production-values.yaml

# Monitor rollout
kubectl rollout status deployment/shop-manager-backend -n shop-manager
kubectl rollout status deployment/shop-manager-frontend -n shop-manager
```

### Major Upgrades

```bash
# Backup database first
kubectl exec -n shop-manager shop-manager-postgresql-0 -- pg_dump -U postgres shopmanager > backup-$(date +%Y%m%d).sql

# Perform upgrade
helm upgrade shop-manager ./shop-manager \
  --namespace shop-manager \
  --values production-values.yaml \
  --set backend.image.tag="new-version"

# Verify upgrade
kubectl get pods -n shop-manager
curl https://api.shop-manager.local/actuator/health
```

## 🆘 Troubleshooting

### Common Issues

**Pods not starting:**
```bash
kubectl describe pod <pod-name> -n shop-manager
kubectl logs <pod-name> -n shop-manager
```

**Database connection issues:**
```bash
kubectl logs -n shop-manager deployment/shop-manager-backend | grep -i database
kubectl exec -n shop-manager deployment/shop-manager-backend -- pg_isready -h shop-manager-postgresql
```

**SSL certificate issues:**
```bash
kubectl describe certificates -n shop-manager
kubectl logs -n cert-manager deployment/cert-manager
```

**Ingress not working:**
```bash
kubectl describe ingress -n shop-manager
kubectl get events -n shop-manager --sort-by='.lastTimestamp'
```

### Recovery Procedures

**Database Recovery:**
```bash
# Restore from backup
kubectl cp backup-YYYYMMDD.sql shop-manager-postgresql-0:/tmp/ -n shop-manager
kubectl exec -n shop-manager shop-manager-postgresql-0 -- psql -U postgres -d shopmanager -f /tmp/backup-YYYYMMDD.sql
```

**Complete Restart:**
```bash
# Restart all services
kubectl rollout restart deployment/shop-manager-backend -n shop-manager
kubectl rollout restart deployment/shop-manager-frontend -n shop-manager
kubectl rollout restart deployment/shop-manager-keycloak -n shop-manager
```

## 📞 Support

### Log Collection

```bash
# Collect all logs
mkdir -p shop-manager-logs
kubectl logs -n shop-manager deployment/shop-manager-backend > shop-manager-logs/backend.log
kubectl logs -n shop-manager deployment/shop-manager-frontend > shop-manager-logs/frontend.log
kubectl logs -n shop-manager deployment/shop-manager-keycloak > shop-manager-logs/keycloak.log
kubectl describe pods -n shop-manager > shop-manager-logs/pod-status.log

# Create support bundle
tar -czf shop-manager-support-$(date +%Y%m%d-%H%M%S).tar.gz shop-manager-logs/
```

### Contact Information

- **Documentation**: `/CERTIFICATE_AUTOMATION.md`
- **API Docs**: `https://api.shop-manager.local/swagger-ui.html`
- **Health Checks**: `https://api.shop-manager.local/actuator/health`

---

## ✅ **Deployment Checklist**

Use this checklist for production deployments:

### Pre-Deployment
- [ ] Kubernetes cluster ready and accessible
- [ ] Helm v3.8+ installed
- [ ] Storage class configured
- [ ] Ingress controller deployed
- [ ] DNS configuration completed
- [ ] Production values.yaml created
- [ ] Security passwords generated

### Deployment
- [ ] cert-manager installed successfully
- [ ] Shop Manager deployed with Helm
- [ ] All pods running and healthy
- [ ] Certificates generated and valid
- [ ] Certificate installation completed
- [ ] Ingress routing working
- [ ] Database connectivity verified

### Post-Deployment
- [ ] HTTPS access working without warnings
- [ ] User authentication functional
- [ ] API endpoints responding
- [ ] Default passwords changed
- [ ] Monitoring configured
- [ ] Backup schedule verified
- [ ] Documentation updated

### Production Ready
- [ ] Security hardening completed
- [ ] Performance testing done
- [ ] Disaster recovery tested
- [ ] Team training completed
- [ ] Support procedures documented

---

**🎉 Congratulations!** Your Shop Manager instance is now deployed and ready for production use with automated SSL certificate management.