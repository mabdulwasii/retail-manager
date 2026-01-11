# Shop Manager - Kubernetes Quick Start Guide

This guide will help you deploy Shop Manager to your Kubernetes cluster in minutes.

## Overview

Shop Manager is deployed using Helm charts pulled from Docker Hub. The entire process is automated and takes 5-10 minutes.

**What you'll need:**
- Kubernetes cluster (1.24+)
- kubectl and Helm installed
- Internet access
- 5-10 minutes

---

## Quick Installation

### 1. Download and Extract

```bash
# Download the latest release
wget https://github.com/princely/shop-manager/releases/download/v0.2.8/shop-manager-kubernetes-0.2.8.tar.gz

# Extract
tar -xzf shop-manager-kubernetes-0.2.8.tar.gz
cd shop-manager-kubernetes-0.2.8
```

### 2. Check Prerequisites

**Linux/macOS:**
```bash
chmod +x check-prerequisites.sh
./check-prerequisites.sh
```

**Windows:**
```powershell
# Double-click: check-prerequisites.ps1
# OR run in PowerShell:
.\check-prerequisites.ps1
```

### 3. Edit Configuration

Edit `values-template.yaml` with your settings:

```yaml
global:
  domain: "your-domain.com"  # CHANGE THIS

postgresql:
  auth:
    postgresPassword: "strong-password-here"  # CHANGE THIS
    password: "strong-password-here"          # CHANGE THIS

keycloak:
  auth:
    adminPassword: "strong-password-here"     # CHANGE THIS
  postgresql:
    auth:
      postgresPassword: "strong-password-here"  # CHANGE THIS
      password: "strong-password-here"          # CHANGE THIS

application:
  jwt:
    secret: "generate-random-32-char-string"    # CHANGE THIS

tls:
  email: "admin@your-domain.com"              # CHANGE THIS
```

**Generate JWT secret:**
```bash
# Linux/macOS
openssl rand -base64 32

# Windows PowerShell
[Convert]::ToBase64String((1..32 | ForEach-Object { Get-Random -Maximum 256 }))
```

### 4. Run Installer

**Linux/macOS:**
```bash
chmod +x install-kubernetes.sh
./install-kubernetes.sh
```

**Windows:**
```powershell
# Double-click: install-kubernetes.bat
# OR run in PowerShell:
.\install-kubernetes.ps1
```

The installer will:
- ✅ Install cert-manager (if needed)
- ✅ Install NGINX Ingress Controller (if needed)
- ✅ Create namespace
- ✅ Deploy Shop Manager using Helm
- ✅ Display access URLs

**Installation takes 5-10 minutes** as Kubernetes pulls Docker images (~500MB).

### 5. Access Shop Manager

Once installation completes:

```
Frontend:  https://retail.your-domain.com
API:       https://api.retail.your-domain.com/swagger-ui.html
Keycloak:  https://auth.retail.your-domain.com
```

**Default Credentials** (change immediately after first login):
- **System Admin:** `superadmin` / `changeme`
- **Tenant Admin:** `admin@shopmanager.com` / `admin123`

---

## Verification

### Check Deployment Status

```bash
# Check pods
kubectl get pods -n gomco

# All pods should show "Running" status:
# NAME                                    READY   STATUS    RESTARTS   AGE
# shop-manager-backend-xxxxxxxxxx-xxxxx   1/1     Running   0          5m
# shop-manager-frontend-xxxxxxxxxx-xxxxx  1/1     Running   0          5m
# postgresql-xxxxxxxxxx-xxxxx             1/1     Running   0          5m
# keycloak-xxxxxxxxxx-xxxxx               1/1     Running   0          5m
```

### View Logs

```bash
# Backend logs
kubectl logs -f deployment/shop-manager-backend -n gomco

# Frontend logs
kubectl logs -f deployment/shop-manager-frontend -n gomco

# Keycloak logs
kubectl logs -f deployment/keycloak -n gomco
```

### Check Services

```bash
kubectl get svc -n gomco
kubectl get ingress -n gomco
```

---

## Troubleshooting

### Pods Not Starting

```bash
# Check pod status
kubectl describe pod <pod-name> -n gomco

# Common issues:
# - Image pull errors: Check internet connectivity to registry-1.docker.io
# - Resource limits: Ensure cluster has 4+ CPU, 8GB+ RAM
# - PVC binding: Check storage class availability
```

### Cannot Access URLs

1. **Check ingress:**
   ```bash
   kubectl get ingress -n gomco
   ```

2. **Verify DNS:** Ensure your domain points to cluster LoadBalancer IP
   ```bash
   kubectl get svc -n ingress-nginx
   ```

3. **Check certificates:**
   ```bash
   kubectl get certificate -n gomco
   ```

### Database Connection Errors

```bash
# Check PostgreSQL pod
kubectl get pods -n gomco | grep postgresql

# Check PostgreSQL logs
kubectl logs deployment/postgresql -n gomco

# Verify connection from backend
kubectl exec -it deployment/shop-manager-backend -n gomco -- pg_isready -h postgresql -p 5432
```

### View All Events

```bash
kubectl get events -n gomco --sort-by='.lastTimestamp'
```

---

## Customization

### Resource Limits

Edit `values-template.yaml` to adjust CPU/memory:

```yaml
backend:
  resources:
    requests:
      cpu: "500m"
      memory: "1Gi"
    limits:
      cpu: "2000m"
      memory: "2Gi"
```

### High Availability

Increase replica count for production:

```yaml
backend:
  replicaCount: 3  # Run 3 backend pods

frontend:
  replicaCount: 2  # Run 2 frontend pods
```

Then upgrade:
```bash
helm upgrade retail oci://registry-1.docker.io/princely/shop-manager \
  --version <VERSION> \
  -f values-template.yaml \
  -n gomco
```

### Enable Optional Components

```yaml
# Enable MinIO for object storage
minio:
  enabled: true
  auth:
    rootPassword: "strong-password"

# Enable Kafka for event streaming
kafka:
  enabled: true
```

---

## Uninstallation

**Linux/macOS:**
```bash
./uninstall-kubernetes.sh
```

**Windows:**
```powershell
.\uninstall-kubernetes.ps1
```

The uninstaller will:
- Remove Helm release
- Optionally delete namespace
- Optionally delete persistent volumes (data)

---

## Upgrade to New Version

```bash
# Download new version
wget https://github.com/princely/shop-manager/releases/download/v0.3.0/shop-manager-kubernetes-0.3.0.tar.gz

# Extract
tar -xzf shop-manager-kubernetes-0.3.0.tar.gz
cd shop-manager-kubernetes-0.3.0

# Use your existing values
cp /path/to/old/values-template.yaml .

# Upgrade
helm upgrade retail oci://registry-1.docker.io/princely/shop-manager \
  --version 0.3.0 \
  -f values-template.yaml \
  -n gomco \
  --wait
```

---

## Useful Commands

### Helm Operations

```bash
# List releases
helm list -n gomco

# Get release status
helm status retail -n gomco

# Get release values
helm get values retail -n gomco

# Rollback to previous version
helm rollback retail -n gomco
```

### kubectl Operations

```bash
# Get all resources
kubectl get all -n gomco

# Describe a resource
kubectl describe pod <pod-name> -n gomco

# Execute command in pod
kubectl exec -it deployment/shop-manager-backend -n gomco -- /bin/bash

# Port forward for local access
kubectl port-forward svc/shop-manager-backend 8081:8080 -n gomco
```

### Backup and Restore

```bash
# Backup database
kubectl exec deployment/postgresql -n gomco -- \
  pg_dump -U shopmanager shopmanager > backup.sql

# Restore database
kubectl exec -i deployment/postgresql -n gomco -- \
  psql -U shopmanager shopmanager < backup.sql
```

---

## Support

- **Documentation:** [PREREQUISITES.md](PREREQUISITES.md)
- **GitHub Issues:** https://github.com/princely/shop-manager/issues
- **Full Documentation:** https://github.com/princely/shop-manager

---

## Next Steps

1. **Change default passwords** immediately after first login
2. **Configure backups** for production use
3. **Set up monitoring** (Prometheus/Grafana)
4. **Review security settings** in values-template.yaml
5. **Configure email notifications**
6. **Set up user accounts and roles**

Enjoy Shop Manager! 🚀
