# Shop Manager Helm Chart

Official Helm chart for deploying Shop Manager - a comprehensive cloud-native retail management platform.

[![Version](https://img.shields.io/badge/version-1.0.0-blue.svg)](https://github.com/your-org/shop-manager)
[![Type](https://img.shields.io/badge/type-application-informational.svg)](https://helm.sh/docs/topics/charts/)
[![Kubernetes](https://img.shields.io/badge/kubernetes-1.24%2B-brightgreen.svg)](https://kubernetes.io/)

---

## Table of Contents

- [Overview](#overview)
- [Architecture](#architecture)
- [Prerequisites](#prerequisites)
- [Quick Start](#quick-start)
- [SSL & DNS Setup](#ssl--dns-setup)
- [Configuration](#configuration)
- [Multi-Tenant Setup](#multi-tenant-setup)
- [Production Deployment](#production-deployment)
- [Monitoring & Troubleshooting](#monitoring--troubleshooting)
- [Security](#security)
- [Backup & Recovery](#backup--recovery)
- [Upgrade & Maintenance](#upgrade--maintenance)
- [Advanced Topics](#advanced-topics)
- [Support](#support)

---

## Overview

### What is Shop Manager?

Shop Manager is a comprehensive, cloud-native retail management platform built with:
- **Backend**: Spring Boot 3.3, Java 21, Spring Modulith
- **Frontend**: React with TypeScript
- **Database**: PostgreSQL with Flyway migrations
- **Authentication**: Keycloak (OAuth2/OpenID Connect)
- **Messaging**: Apache Kafka (KRaft mode)
- **Storage**: MinIO (S3-compatible)
- **Deployment**: Kubernetes via Helm charts

### Key Features

- ✅ **Multi-Tenant Architecture** - Complete tenant isolation with hierarchical feature flags
- ✅ **Investment Management** - Track investments, profit sharing, and ROI analytics
- ✅ **Sales & Inventory** - Comprehensive sales tracking and inventory management
- ✅ **Authentication** - Keycloak SSO with RBAC/ABAC authorization
- ✅ **Event-Driven** - Kafka-based analytics and event streaming
- ✅ **Customizable Branding** - Per-tenant logos, themes, and UI customization
- ✅ **Secure by Default** - TLS/SSL encryption, audit logging, data isolation

### Architecture Diagram

```
┌─────────────────────────────────────────────────────────────┐
│                     Ingress (NGINX)                         │
│              SSL/TLS Termination & Routing                  │
└─────────────────────────────────────────────────────────────┘
                            │
        ┌───────────────────┼───────────────────┐
        │                   │                   │
┌───────▼────────┐  ┌──────▼──────┐   ┌───────▼────────┐
│   Frontend     │  │   Backend   │   │   Keycloak     │
│   (React)      │  │ (Spring     │   │   (Auth)       │
│                │  │  Boot)      │   │                │
└────────────────┘  └─────┬───────┘   └────────────────┘
                          │
        ┌─────────────────┼─────────────────┐
        │                 │                 │
┌───────▼────────┐ ┌─────▼──────┐  ┌──────▼────────┐
│  PostgreSQL    │ │   Kafka    │  │    MinIO      │
│  (Database)    │ │ (Messages) │  │  (Storage)    │
└────────────────┘ └────────────┘  └───────────────┘
```

---

## Prerequisites

### Required Tools

| Tool | Version | Purpose |
|------|---------|---------|
| Kubernetes | 1.24+ | Container orchestration |
| Helm | 3.10+ | Package management |
| kubectl | 1.24+ | Kubernetes CLI |
| Docker | 20.10+ | Container runtime (for local dev) |

### Infrastructure Requirements

**Minimum Resources:**
- 4 CPU cores
- 8 GB RAM
- 20 GB storage

**Recommended (Production):**
- 8+ CPU cores
- 16+ GB RAM
- 100+ GB SSD storage
- Load balancer with public IP

### Network Requirements

- Ingress controller (NGINX recommended)
- cert-manager for SSL certificates
- DNS or hosts file configuration
- Open ports: 80 (HTTP), 443 (HTTPS)

---

## Quick Start

### 1. Install Prerequisites

**Install Helm:**
```bash
# macOS
brew install helm

# Linux
curl https://raw.githubusercontent.com/helm/helm/main/scripts/get-helm-3 | bash

# Windows
choco install kubernetes-helm
```

**Install kubectl:**
```bash
# macOS
brew install kubectl

# Linux
curl -LO "https://dl.k8s.io/release/$(curl -L -s https://dl.k8s.io/release/stable.txt)/bin/linux/amd64/kubectl"
sudo install -o root -g root -m 0755 kubectl /usr/local/bin/kubectl

# Windows
choco install kubernetes-cli
```

### 2. Install cert-manager

```bash
kubectl apply -f https://github.com/cert-manager/cert-manager/releases/download/v1.13.2/cert-manager.yaml

# Wait for cert-manager to be ready
kubectl wait --for=condition=available --timeout=300s deployment -n cert-manager --all
```

### 3. Install NGINX Ingress Controller

```bash
helm repo add ingress-nginx https://kubernetes.github.io/ingress-nginx
helm repo update
helm install ingress-nginx ingress-nginx/ingress-nginx \
  --namespace ingress-nginx \
  --create-namespace \
  --set controller.service.type=LoadBalancer
```

### 4. Deploy Shop Manager

```bash
# Create namespace
kubectl create namespace gomco

# Create custom values file
cat > my-values.yaml << EOF
global:
  appName: "retail"
  domain: "gomco.com"

branding:
  platformName: "My Retail Platform"
  companyName: "My Company"

tls:
  enabled: true
  issuer: "local-ca-issuer"  # Use local CA for development
  email: "admin@gomco.com"
EOF

# Install with Helm
helm install retail ./shop-manager \
  -n gomco \
  -f my-values.yaml \
  --wait \
  --timeout 10m
```

### 5. Verify Deployment

```bash
# Check all pods are running
kubectl get pods -n gomco

# Expected output:
# NAME                               READY   STATUS    RESTARTS   AGE
# retail-backend-xxx                 1/1     Running   0          2m
# retail-frontend-xxx                1/1     Running   0          2m
# retail-kafka-0                     1/1     Running   0          2m
# retail-keycloak-0                  1/1     Running   0          2m
# retail-postgresql-0                1/1     Running   0          2m
# retail-minio-xxx                   1/1     Running   0          2m

# Check ingress
kubectl get ingress -n gomco

# Check certificates
kubectl get certificate -n gomco
```

---

## SSL & DNS Setup

After deploying Shop Manager, you need to install the CA certificate and configure DNS entries on your local machine to access services via HTTPS.

### Option 1: Local Development (Local CA)

#### For macOS

```bash
# Extract CA certificate
kubectl get secret local-ca-key-pair -n cert-manager \
  -o jsonpath='{.data.tls\.crt}' | base64 -d > /tmp/shop-manager-ca.crt

# Get domain information
NAMESPACE="gomco"
HOSTNAME=$(kubectl get ingress -n $NAMESPACE -o jsonpath='{.items[0].spec.rules[0].host}')
APP_NAME=$(echo "$HOSTNAME" | cut -d'.' -f1)
DOMAIN=$(echo "$HOSTNAME" | cut -d'.' -f2-)

# Create DNS entries
cat > /tmp/dns-entries.txt << EOF
# Shop Manager DNS entries
127.0.0.1 $APP_NAME.$DOMAIN
127.0.0.1 api.$APP_NAME.$DOMAIN
127.0.0.1 auth.$APP_NAME.$DOMAIN
EOF

# Install DNS entries to /etc/hosts
sudo sed -i.bak '/# Shop Manager DNS entries/,/^$/d' /etc/hosts 2>/dev/null || true
echo "" | sudo tee -a /etc/hosts >/dev/null
cat /tmp/dns-entries.txt | sudo tee -a /etc/hosts >/dev/null

# Install CA certificate to system keychain
sudo security add-trusted-cert -d -r trustRoot \
  -k /Library/Keychains/System.keychain \
  /tmp/shop-manager-ca.crt

echo "✅ Installation complete! Restart your browser."
```

#### For Windows (PowerShell as Administrator)

```powershell
# Extract CA certificate
$certContent = kubectl get secret local-ca-key-pair -n cert-manager -o jsonpath='{.data.tls\.crt}'
$certBytes = [System.Convert]::FromBase64String($certContent)
[System.IO.File]::WriteAllBytes("C:\temp\shop-manager-ca.crt", $certBytes)

# Get domain information
$namespace = "gomco"
$hostname = kubectl get ingress -n $namespace -o jsonpath='{.items[0].spec.rules[0].host}'
$appName = $hostname.Split('.')[0]
$domain = $hostname.Substring($hostname.IndexOf('.') + 1)

# Create DNS entries
$dnsEntries = @"
# Shop Manager DNS entries
127.0.0.1 $appName.$domain
127.0.0.1 api.$appName.$domain
127.0.0.1 auth.$appName.$domain
"@

# Install DNS entries to hosts file
$hostsFile = "$env:SystemRoot\System32\drivers\etc\hosts"
Copy-Item -Path $hostsFile -Destination "$hostsFile.backup" -Force
$content = Get-Content $hostsFile | Where-Object { $_ -notmatch "Shop Manager" }
$newContent = $content + "" + $dnsEntries
Set-Content -Path $hostsFile -Value $newContent -Force

# Install CA certificate
Import-Certificate -FilePath "C:\temp\shop-manager-ca.crt" -CertStoreLocation Cert:\LocalMachine\Root

# Flush DNS cache
ipconfig /flushdns

Write-Host "✅ Installation complete! Restart your browser."
```

### Option 2: Production (Let's Encrypt)

```yaml
# In your values file
tls:
  enabled: true
  issuer: "letsencrypt-prod"  # or "letsencrypt-staging" for testing
  email: "admin@example.com"

# Ensure your domain points to the cluster's public IP
# DNS A records should be configured:
# retail.example.com      -> <CLUSTER_PUBLIC_IP>
# api.retail.example.com  -> <CLUSTER_PUBLIC_IP>
# auth.retail.example.com -> <CLUSTER_PUBLIC_IP>
```

**Verify Let's Encrypt certificates:**

```bash
# Check certificate status
kubectl get certificate -n gomco

# Check certificate details
kubectl describe certificate retail-frontend-tls -n gomco

# Check ACME challenge
kubectl get challenges -n gomco

# Force certificate renewal
kubectl delete secret retail-frontend-tls -n gomco
kubectl delete certificate retail-frontend-tls -n gomco
helm upgrade retail ./shop-manager -n gomco -f production-values.yaml
```

### Option 3: Custom CA/Enterprise Certificates

```yaml
# Bring your own certificates
tls:
  enabled: true
  issuer: "custom-ca-issuer"

# Create a secret with your certificate
kubectl create secret tls retail-frontend-tls \
  --cert=path/to/tls.crt \
  --key=path/to/tls.key \
  -n gomco
```

### Verify SSL/DNS Installation

**Test DNS Resolution:**
```bash
# macOS/Linux
nslookup retail.gomco.com
ping retail.gomco.com

# Windows
nslookup retail.gomco.com
ping retail.gomco.com
```

**Test HTTPS Access:**
```bash
curl -I https://retail.gomco.com
curl -I https://api.retail.gomco.com/actuator/health
curl -I https://auth.retail.gomco.com
```

**Using Browser:**
1. Open browser and navigate to: `https://retail.gomco.com`
2. You should see a **secure padlock** icon (no certificate warnings)
3. Click the padlock and verify the certificate is issued by "Shop Manager Local CA"

### Access Shop Manager

After successful SSL/DNS setup, access Shop Manager at:

- **Frontend**: `https://retail.gomco.com` (or your configured domain)
- **API Docs**: `https://api.retail.gomco.com/swagger-ui.html`
- **Keycloak**: `https://auth.retail.gomco.com`

---

## Configuration

### Minimal Configuration

```yaml
# my-values.yaml
global:
  appName: "retail"
  domain: "example.com"

branding:
  platformName: "My Retail Platform"
  companyName: "My Company"
```

### Common Configurations

**Change Domain:**
```yaml
global:
  domain: "mycompany.com"
```

**Enable Let's Encrypt (for public domains):**
```yaml
tls:
  enabled: true
  issuer: "letsencrypt-prod"
  email: "admin@example.com"
```

**Scale Resources:**
```yaml
backend:
  replicaCount: 3
  resources:
    limits:
      cpu: 2000m
      memory: 2Gi
    requests:
      cpu: 500m
      memory: 512Mi
```

**Disable Services:**
```yaml
kafka:
  enabled: false

minio:
  enabled: false
```

**Use External Database:**
```yaml
postgresql:
  enabled: false

externalDatabase:
  host: "postgres.example.com"
  port: 5432
  database: "shopmanager"
  username: "shopuser"
  password: "securepassword"
```

### Branding

```yaml
branding:
  platformName: "ACME Retail Pro"
  companyName: "ACME Corporation"
  platformDescription: "Enterprise Retail Management"

  colors:
    primary: "#2E7D32"
    secondary: "#FF6F00"
    success: "#4CAF50"
    warning: "#FF9800"
    error: "#F44336"

  fonts:
    family: "Roboto, sans-serif"
```

### Configuration Reference

For complete configuration options, see [values.yaml](./values.yaml).

---

## Multi-Tenant Setup

### Architecture

Shop Manager supports multi-tenancy with complete data isolation:

```
Tenant (Organization)
├── Shop 1
│   ├── Products
│   ├── Sales
│   └── Inventory
├── Shop 2
│   ├── Products
│   ├── Sales
│   └── Inventory
└── Users (with shop-specific access)
```

### Deploy Multiple Tenants

**Method 1: Multiple Namespaces**

```bash
# Deploy for Tenant A
helm install tenant-a ./shop-manager \
  -n tenant-a \
  --create-namespace \
  -f tenant-a-values.yaml

# Deploy for Tenant B
helm install tenant-b ./shop-manager \
  -n tenant-b \
  --create-namespace \
  -f tenant-b-values.yaml
```

**Method 2: Single Deployment with Multi-Tenancy**

```yaml
# values.yaml
global:
  appName: "platform"
  domain: "example.com"

# Each tenant gets their own subdomain
# tenant-a.example.com
# tenant-b.example.com
```

### Tenant Isolation Features

- **Data Isolation**: Complete database-level isolation via tenant context
- **Feature Flags**: Hierarchical flags (global → tenant → shop)
- **Custom Branding**: Per-tenant logos, colors, and themes
- **User Management**: Tenant-scoped users and roles
- **Resource Quotas**: CPU/memory limits per tenant

---

## Production Deployment

### Production Checklist

- [ ] **Security**
  - [ ] Change all default passwords
  - [ ] Configure production-grade secrets (external secret manager)
  - [ ] Enable network policies
  - [ ] Configure RBAC properly
  - [ ] Enable audit logging
  - [ ] Configure backup encryption

- [ ] **High Availability**
  - [ ] Deploy multiple replicas (min 2)
  - [ ] Configure pod anti-affinity
  - [ ] Set up database replication
  - [ ] Configure Kafka multi-broker
  - [ ] Enable autoscaling (HPA)

- [ ] **Monitoring**
  - [ ] Install Prometheus & Grafana
  - [ ] Configure alerting rules
  - [ ] Set up log aggregation (ELK/Loki)
  - [ ] Enable distributed tracing
  - [ ] Configure uptime monitoring

- [ ] **Backup & DR**
  - [ ] Configure automated database backups
  - [ ] Test restore procedures
  - [ ] Set up cross-region replication
  - [ ] Document disaster recovery plan
  - [ ] Configure backup retention policies

- [ ] **Performance**
  - [ ] Tune database connection pools
  - [ ] Configure CDN for static assets
  - [ ] Enable Redis caching
  - [ ] Optimize container images
  - [ ] Configure resource limits properly

### Production Values Example

```yaml
# production.yaml
global:
  appName: "retail"
  domain: "example.com"
  environment: "production"

# High Availability
backend:
  replicaCount: 3
  autoscaling:
    enabled: true
    minReplicas: 3
    maxReplicas: 10
    targetCPUUtilizationPercentage: 70

frontend:
  replicaCount: 2
  autoscaling:
    enabled: true
    minReplicas: 2
    maxReplicas: 5

# Database HA
postgresql:
  enabled: true
  architecture: replication
  replication:
    enabled: true
    numReplicas: 2
  persistence:
    size: 100Gi
    storageClass: "fast-ssd"

# Kafka HA
kafka:
  enabled: true
  replicaCount: 3
  persistence:
    size: 50Gi
    storageClass: "fast-ssd"

# Monitoring
serviceMonitor:
  enabled: true
  namespace: monitoring

# Backup
config:
  backup:
    enabled: true
    scheduleCron: "0 2 * * *"  # Daily at 2 AM
    retentionDays: 30
    encryptionEnabled: true

# Security
networkPolicy:
  enabled: true
  policyTypes:
    - Ingress
    - Egress

podSecurityPolicy:
  enabled: true

secrets:
  # Use external secret manager in production
  externalSecretsEnabled: true
  vaultAddress: "https://vault.example.com"
```

### Deploy to Production

```bash
# Validate configuration
helm template retail ./shop-manager \
  -n production \
  -f production.yaml \
  --validate

# Dry run
helm install retail ./shop-manager \
  -n production \
  --create-namespace \
  -f production.yaml \
  --dry-run \
  --debug

# Deploy with rollback capability
helm install retail ./shop-manager \
  -n production \
  --create-namespace \
  -f production.yaml \
  --wait \
  --timeout 20m \
  --atomic

# Monitor deployment
kubectl rollout status deployment -n production
```

---

## Monitoring & Troubleshooting

### Health Checks

```bash
# Backend health
curl https://api.retail.gomco.com/actuator/health

# Keycloak health
curl https://auth.retail.gomco.com/health

# Frontend health
curl https://retail.gomco.com/health
```

### View Logs

```bash
# All backend logs
kubectl logs -n gomco -l app.kubernetes.io/component=backend --tail=100 -f

# Frontend logs
kubectl logs -n gomco -l app.kubernetes.io/component=frontend --tail=100 -f

# Kafka logs
kubectl logs -n gomco retail-kafka-0 --tail=100 -f

# Database logs
kubectl logs -n gomco retail-postgresql-0 --tail=100 -f
```

### Monitoring Commands

```bash
# View all pods
kubectl get pods -n gomco -o wide

# Watch pod status
kubectl get pods -n gomco -w

# Check pod resource usage
kubectl top pods -n gomco

# Check node resource usage
kubectl top nodes

# Check events
kubectl get events -n gomco --sort-by='.lastTimestamp'

# Describe problematic pod
kubectl describe pod <pod-name> -n gomco
```

### Common Issues & Solutions

#### Issue: Pods CrashLoopBackOff

**Diagnosis:**
```bash
kubectl logs <pod-name> -n gomco --previous
kubectl describe pod <pod-name> -n gomco
```

**Common causes:**
1. Configuration errors - Check environment variables and secrets
2. Resource limits - Increase CPU/memory limits
3. Dependency not ready - Check database/Kafka connectivity
4. Image pull errors - Verify image exists and credentials are correct

**Solutions:**
```bash
# Increase resources
helm upgrade retail ./shop-manager -n gomco \
  --set backend.resources.limits.memory=2Gi \
  --reuse-values

# Check secrets
kubectl get secrets -n gomco
kubectl describe secret retail-secrets -n gomco
```

#### Issue: Certificate Not Trusted

**Diagnosis:**
```bash
# Check certificate status
kubectl get certificate -n gomco
kubectl describe certificate retail-frontend-tls -n gomco

# Check CA certificate
kubectl get secret local-ca-key-pair -n cert-manager
```

**Solutions:**

**macOS:**
```bash
# Reinstall certificate
sudo security delete-certificate -c "Shop Manager Local CA" /Library/Keychains/System.keychain
sudo security add-trusted-cert -d -r trustRoot \
  -k /Library/Keychains/System.keychain \
  /tmp/shop-manager-ca.crt

# Verify trust
security verify-cert -c /tmp/shop-manager-ca.crt
```

**Windows:**
```powershell
# Remove old certificate
Get-ChildItem Cert:\LocalMachine\Root | Where-Object { $_.Subject -match "Shop Manager" } | Remove-Item

# Reinstall
Import-Certificate -FilePath "C:\temp\shop-manager-ca.crt" -CertStoreLocation Cert:\LocalMachine\Root
```

#### Issue: DNS Not Resolving

**Diagnosis:**
```bash
# Check hosts file
cat /etc/hosts | grep retail.gomco.com  # macOS/Linux
Get-Content C:\Windows\System32\drivers\etc\hosts | Select-String "retail"  # Windows

# Test DNS
ping retail.gomco.com
nslookup retail.gomco.com
```

**Solutions:**
```bash
# Flush DNS cache
# macOS
sudo dscacheutil -flushcache
sudo killall -HUP mDNSResponder

# Linux
sudo systemd-resolve --flush-caches

# Windows
ipconfig /flushdns

# Re-add DNS entries (see SSL & DNS section)
```

#### Issue: Database Connection Failures

**Diagnosis:**
```bash
# Check PostgreSQL pod
kubectl logs retail-postgresql-0 -n gomco --tail=50

# Check connection from backend
kubectl exec -it deployment/retail-backend -n gomco -- \
  bash -c 'psql -h retail-postgresql -U retail -d retaildb -c "SELECT 1"'

# Check secrets
kubectl get secret retail-postgresql -n gomco -o yaml
```

**Solutions:**
```bash
# Restart PostgreSQL
kubectl rollout restart statefulset retail-postgresql -n gomco

# Check password configuration
kubectl get secret retail-postgresql -n gomco -o jsonpath='{.data.password}' | base64 -d

# Recreate database if corrupted
kubectl delete pvc data-retail-postgresql-0 -n gomco
kubectl delete pod retail-postgresql-0 -n gomco
```

### Metrics

Enable Prometheus monitoring:

```yaml
serviceMonitor:
  enabled: true
  namespace: monitoring
  interval: 30s
```

---

## Security

### Authentication & Authorization

Shop Manager uses Keycloak for centralized authentication:

1. **OAuth2/OpenID Connect** - Industry standard protocols
2. **RBAC** - Role-Based Access Control
3. **ABAC** - Attribute-Based Access Control
4. **Multi-Factor Authentication** - Optional 2FA/MFA
5. **SSO** - Single Sign-On with social providers

### Default Roles

| Role | Permissions | Use Case |
|------|-------------|----------|
| TENANT_ADMIN | Full tenant access | Tenant administrators |
| SHOP_MANAGER | Shop management | Shop managers |
| SHOP_EMPLOYEE | Limited shop access | Cashiers, staff |
| INVESTOR | Investment tracking | Investors |
| AUDITOR | Read-only access | Compliance officers |

### Security Best Practices

**1. Change Default Credentials:**
```bash
# Generate secure passwords
openssl rand -base64 32  # For JWT secret
openssl rand -base64 32  # For encryption key
openssl rand -base64 24  # For passwords

# Update via Helm
helm upgrade retail ./shop-manager -n gomco \
  --set secrets.security.jwtSecret="<generated-jwt-secret>" \
  --set secrets.security.encryptionKey="<generated-encryption-key>" \
  --set keycloak.auth.adminPassword="<secure-password>" \
  --reuse-values
```

**2. Use External Secret Management:**
```yaml
# Use AWS Secrets Manager, HashiCorp Vault, or Azure Key Vault
secrets:
  externalSecretsEnabled: true
  provider: "vault"  # or "aws" or "azure"
  vaultAddress: "https://vault.example.com"
  vaultPath: "secret/data/shop-manager"
```

**3. Enable Network Policies:**
```yaml
networkPolicy:
  enabled: true
  policyTypes:
    - Ingress
    - Egress
  ingress:
    - from:
      - namespaceSelector:
          matchLabels:
            name: ingress-nginx
  egress:
    - to:
      - namespaceSelector: {}
      ports:
      - protocol: TCP
        port: 443  # HTTPS only
```

**4. Configure Pod Security:**
```yaml
podSecurityContext:
  runAsNonRoot: true
  runAsUser: 1000
  fsGroup: 1000
  seccompProfile:
    type: RuntimeDefault

securityContext:
  allowPrivilegeEscalation: false
  capabilities:
    drop:
      - ALL
  readOnlyRootFilesystem: true
```

**5. Enable Audit Logging:**
```yaml
config:
  audit:
    enabled: true
    retentionDays: 90
    logEntityChanges: true
    logSecurityEvents: true
    logApiRequests: true
```

---

## Backup & Recovery

### Automated Backups

```yaml
config:
  backup:
    enabled: true
    path: "/var/shop-manager/backups"
    scheduleCron: "0 2 * * *"  # Daily at 2 AM
    retentionDays: 30
    compressionEnabled: true
    encryptionEnabled: true
    encryptionAlgorithm: "AES256"
```

### Manual Backup

**PostgreSQL Backup:**
```bash
# Create backup
kubectl exec retail-postgresql-0 -n gomco -- \
  pg_dump -U retail retaildb | gzip > backup-$(date +%Y%m%d).sql.gz

# Restore backup
gunzip -c backup-20241005.sql.gz | \
  kubectl exec -i retail-postgresql-0 -n gomco -- \
  psql -U retail -d retaildb
```

**Full Namespace Backup (with Velero):**
```bash
# Install Velero
velero install \
  --provider aws \
  --bucket shop-manager-backups \
  --secret-file ./credentials-velero

# Create backup
velero backup create shop-manager-backup \
  --include-namespaces gomco \
  --wait

# List backups
velero backup get

# Restore backup
velero restore create --from-backup shop-manager-backup
```

---

## Upgrade & Maintenance

### Upgrade

```bash
# Upgrade to new version
helm upgrade retail ./shop-manager -n gomco -f my-values.yaml --wait

# Upgrade with new image version
helm upgrade retail ./shop-manager -n gomco \
  --set backend.image.tag=2.0.0 \
  --reuse-values
```

### Rollback

```bash
# List revisions
helm history retail -n gomco

# Rollback to previous version
helm rollback retail -n gomco

# Rollback to specific revision
helm rollback retail 5 -n gomco
```

### Database Migrations

```bash
# Flyway migrations run automatically on startup
# Check migration status
kubectl logs -n gomco -l app.kubernetes.io/component=backend | grep Flyway

# Manual migration (if needed)
kubectl exec -it deployment/retail-backend -n gomco -- \
  java -jar /app/app.jar --spring.flyway.baseline-on-migrate=true
```

---

## Advanced Topics

### Custom Domain Setup

```yaml
global:
  domain: "mycompany.com"

ingress:
  annotations:
    cert-manager.io/cluster-issuer: "letsencrypt-prod"
  hosts:
    - host: shop.mycompany.com
      paths:
        - path: /
          pathType: Prefix
  tls:
    - secretName: shop-tls
      hosts:
        - shop.mycompany.com
        - api.shop.mycompany.com
        - auth.shop.mycompany.com
```

### External Database

```yaml
postgresql:
  enabled: false

externalDatabase:
  host: "postgres.rds.amazonaws.com"
  port: 5432
  database: "shopmanager"
  username: "shopuser"
  existingSecret: "external-db-secret"
  existingSecretPasswordKey: "password"
```

### Redis Caching

```yaml
redis:
  enabled: true
  architecture: standalone
  auth:
    enabled: true
    password: "SECURE_PASSWORD"
  master:
    persistence:
      size: 5Gi

backend:
  env:
    SPRING_CACHE_TYPE: "redis"
    SPRING_REDIS_HOST: "retail-redis-master"
    SPRING_REDIS_PORT: "6379"
```

---

## Support

### Get Help

- **Documentation**: See this README and [values.yaml](./values.yaml)
- **Issues**: [GitHub Issues](https://github.com/your-org/shop-manager/issues)
- **Discussions**: [GitHub Discussions](https://github.com/your-org/shop-manager/discussions)
- **Email**: support@shop-manager.io
- **Slack**: [Join community](https://slack.shop-manager.io)

### Helpful Commands Cheat Sheet

```bash
# Deployment
helm install <release> ./shop-manager -n <namespace> -f values.yaml
helm upgrade <release> ./shop-manager -n <namespace> -f values.yaml
helm rollback <release> <revision> -n <namespace>
helm uninstall <release> -n <namespace>

# Debugging
kubectl get pods -n <namespace>
kubectl describe pod <pod-name> -n <namespace>
kubectl logs <pod-name> -n <namespace> --tail=100 -f
kubectl exec -it <pod-name> -n <namespace> -- bash

# Scaling
kubectl scale deployment <deployment> --replicas=<count> -n <namespace>
kubectl autoscale deployment <deployment> --min=2 --max=10 --cpu-percent=70 -n <namespace>

# Port Forwarding
kubectl port-forward -n <namespace> svc/<service> <local-port>:<remote-port>

# Secrets
kubectl create secret generic <name> --from-literal=key=value -n <namespace>
kubectl get secret <name> -n <namespace> -o yaml
```

---

## Values Reference

### Key Configuration Values

| Parameter | Description | Default |
|-----------|-------------|------------|
| `global.appName` | Application name | `shop-manager` |
| `global.domain` | Base domain | `shop-manager.local` |
| `global.environment` | Environment | `production` |
| `backend.replicaCount` | Backend replicas | `1` |
| `frontend.enabled` | Enable frontend | `true` |
| `postgresql.enabled` | Enable PostgreSQL | `true` |
| `kafka.enabled` | Enable Kafka | `true` |
| `keycloak.enabled` | Enable Keycloak | `true` |
| `tls.enabled` | Enable TLS | `true` |
| `tls.issuer` | Certificate issuer | `local-ca-issuer` |

For complete values reference, see [values.yaml](./values.yaml).

---

## License

This project is licensed under the MIT License - see the [LICENSE](../../LICENSE) file for details.

---

**Made with ❤️ by the Shop Manager Team**