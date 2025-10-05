# Shop Manager Deployment Guide

Complete guide for deploying Shop Manager in various environments.

---

## 🚀 Quick Start (3 Steps)

**Prerequisites**: Kubernetes cluster with kubectl and helm installed.

### Step 1: Run Automated Installation
```bash
cd helm-chart
./install-shop-manager.sh
```

### Step 2: Install SSL Certificates and DNS
```bash
sudo /tmp/install-shop-manager-ssl.sh
```

### Step 3: Access Application
```bash
# Restart your browser, then open:
open https://retail.gomco.com

# Login with test credentials (see TESTING-GUIDE.md):
# Email: admin@shopmanager.com
# Password: DevAdmin@2024!Test
```

**That's it!** The automated script handles cert-manager, ingress-nginx, namespace creation, and all deployments.

---

## Quick Links

- **[Helm Chart Documentation](./helm-chart/shop-manager/README.md)** - Complete deployment guide with SSL/DNS setup
- **[Developer Guide](./DEVELOPER_GUIDE.md)** - Local development setup
- **[Testing Guide](./TESTING-GUIDE.md)** - Testing and QA procedures

---

## Deployment Options

Shop Manager supports multiple deployment strategies:

### 1. Local Development (Docker Compose)

**Best for**: Development, testing, quick prototyping

```bash
# Start all services
docker-compose up -d

# Access services
# Frontend: http://localhost:3001
# Backend: http://localhost:8081
# Keycloak: http://localhost:8080
```

See [Developer Guide](./DEVELOPER_GUIDE.md) for complete local setup.

### 2. Kubernetes (Helm)

**Best for**: Production, staging, on-premise deployments

```bash
# Quick deployment
helm install retail ./helm-chart/shop-manager -n gomco --create-namespace

# Production deployment
helm install retail ./helm-chart/shop-manager \
  -n production \
  -f production-values.yaml \
  --timeout 20m \
  --atomic
```

See [Helm Chart README](./helm-chart/shop-manager/README.md) for comprehensive Kubernetes deployment guide.

### 3. Cloud Platforms

**Supported platforms**:
- AWS (EKS)
- Azure (AKS)
- Google Cloud (GKE)
- DigitalOcean Kubernetes
- On-premise Kubernetes

All cloud platforms use the same Helm chart. See platform-specific guides below.

---

## Prerequisites

### For All Deployments

- **Kubernetes**: 1.24+ (for Helm deployments)
- **Helm**: 3.10+ (for Helm deployments)
- **Docker**: 20.10+ (for local development)
- **kubectl**: 1.24+ (for Kubernetes)

### Infrastructure Requirements

**Minimum (Development)**:
- 4 CPU cores
- 8 GB RAM
- 20 GB storage

**Recommended (Production)**:
- 8+ CPU cores
- 16+ GB RAM
- 100+ GB SSD storage
- Load balancer
- Monitoring solution

---

## Quick Deployment

### Local Development (5 minutes)

```bash
# Clone repository
git clone <repository-url>
cd shop-manager

# Start infrastructure
docker-compose up -d

# Verify services
docker-compose ps

# Access application
open http://localhost:3001
```

### Kubernetes Production (15 minutes)

**Automated Installation** (Recommended):
```bash
# Run the automated installation script
cd helm-chart
./install-shop-manager.sh

# Follow the on-screen instructions to complete SSL/DNS setup
sudo /tmp/install-shop-manager-ssl.sh

# Restart browser and access
open https://retail.gomco.com
```

**Manual Installation** (Step-by-step):

**Step 1: Install cert-manager**
```bash
kubectl apply -f https://github.com/cert-manager/cert-manager/releases/download/v1.13.2/cert-manager.yaml

# Wait for cert-manager to be ready
kubectl wait --for=condition=available --timeout=300s deployment -n cert-manager --all
```

**Step 2: Install NGINX Ingress Controller**
```bash
helm repo add ingress-nginx https://kubernetes.github.io/ingress-nginx
helm repo update

helm install ingress-nginx ingress-nginx/ingress-nginx \
  --namespace ingress-nginx \
  --create-namespace \
  --set controller.service.type=LoadBalancer \
  --wait \
  --timeout 5m
```

**Step 3: Create namespace**
```bash
kubectl create namespace gomco
```

**Step 4: Deploy Shop Manager**
```bash
cd helm-chart

helm install retail ./shop-manager \
  -n gomco \
  -f ../gomco-values.yaml \
  --wait \
  --timeout 10m
```

**Step 5: Verify deployment**
```bash
# Check all pods are running
kubectl get pods -n gomco

# Check ingress and certificates
kubectl get ingress,certificate -n gomco
```

**Step 6: Setup SSL/DNS**
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

# For macOS: Install certificate and DNS
sudo security add-trusted-cert -d -r trustRoot \
  -k /Library/Keychains/System.keychain \
  /tmp/shop-manager-ca.crt

sudo sed -i.bak '/# Shop Manager DNS entries/,/^$/d' /etc/hosts 2>/dev/null || true
echo "" | sudo tee -a /etc/hosts >/dev/null
cat /tmp/dns-entries.txt | sudo tee -a /etc/hosts >/dev/null

# For Linux: Install certificate and DNS
sudo cp /tmp/shop-manager-ca.crt /usr/local/share/ca-certificates/shop-manager-ca.crt
sudo update-ca-certificates
sudo sed -i.bak '/# Shop Manager DNS entries/,/^$/d' /etc/hosts 2>/dev/null || true
echo "" | sudo tee -a /etc/hosts >/dev/null
cat /tmp/dns-entries.txt | sudo tee -a /etc/hosts >/dev/null
```

**Step 7: Restart browser and access**
```bash
# Access application
open https://retail.gomco.com
```

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

### Production Configuration

```yaml
# production-values.yaml
global:
  appName: "retail"
  domain: "example.com"
  environment: "production"

tls:
  enabled: true
  issuer: "letsencrypt-prod"
  email: "admin@example.com"

backend:
  replicaCount: 3
  autoscaling:
    enabled: true
    minReplicas: 3
    maxReplicas: 10

postgresql:
  persistence:
    size: 100Gi
    storageClass: "fast-ssd"

secrets:
  externalSecretsEnabled: true
  vaultAddress: "https://vault.example.com"
```

See [Helm Chart values.yaml](./helm-chart/shop-manager/values.yaml) for all configuration options.

---

## Platform-Specific Guides

### AWS (EKS)

```bash
# Create EKS cluster
eksctl create cluster \
  --name shop-manager \
  --region us-east-1 \
  --node-type t3.large \
  --nodes 3

# Deploy Shop Manager
helm install retail ./helm-chart/shop-manager \
  -n production \
  --create-namespace \
  -f aws-values.yaml
```

**AWS-specific configuration**:
```yaml
# aws-values.yaml
global:
  domain: "shop-manager.example.com"

tls:
  enabled: true
  issuer: "letsencrypt-prod"
  annotations:
    service.beta.kubernetes.io/aws-load-balancer-type: "nlb"

postgresql:
  persistence:
    storageClass: "gp3"

backup:
  storage:
    type: "s3"
    s3:
      bucket: "shop-manager-backups"
      region: "us-east-1"
```

### Azure (AKS)

```bash
# Create AKS cluster
az aks create \
  --resource-group shop-manager-rg \
  --name shop-manager-aks \
  --node-count 3 \
  --node-vm-size Standard_D4s_v3

# Deploy Shop Manager
helm install retail ./helm-chart/shop-manager \
  -n production \
  --create-namespace \
  -f azure-values.yaml
```

**Azure-specific configuration**:
```yaml
# azure-values.yaml
global:
  domain: "shop-manager.example.com"

postgresql:
  persistence:
    storageClass: "managed-premium"

backup:
  storage:
    type: "azure"
    azure:
      storageAccount: "shopmanagerbackups"
      container: "backups"
```

### Google Cloud (GKE)

```bash
# Create GKE cluster
gcloud container clusters create shop-manager \
  --zone us-central1-a \
  --num-nodes 3 \
  --machine-type n1-standard-4

# Deploy Shop Manager
helm install retail ./helm-chart/shop-manager \
  -n production \
  --create-namespace \
  -f gcp-values.yaml
```

**GCP-specific configuration**:
```yaml
# gcp-values.yaml
global:
  domain: "shop-manager.example.com"

postgresql:
  persistence:
    storageClass: "standard-rwo"

backup:
  storage:
    type: "gcs"
    gcs:
      bucket: "shop-manager-backups"
      projectId: "my-project-id"
```

---

## Post-Deployment

### 1. Verify Installation

```bash
# Check pods
kubectl get pods -n gomco

# Check services
kubectl get svc -n gomco

# Check ingress
kubectl get ingress -n gomco

# Check certificates
kubectl get certificate -n gomco
```

### 2. Access Services

After successful deployment:

- **Frontend**: `https://retail.example.com`
- **API**: `https://api.retail.example.com/swagger-ui.html`
- **Keycloak**: `https://auth.retail.example.com`

### 3. Configure Keycloak

```bash
# Get admin password
kubectl get secret retail-keycloak -n gomco -o jsonpath='{.data.admin-password}' | base64 -d

# Access Keycloak admin console
open https://auth.retail.example.com

# Login with admin credentials
# Import realm or configure manually
```

### 4. Monitor Deployment

```bash
# View logs
kubectl logs -n gomco -l app.kubernetes.io/component=backend --tail=100 -f

# Check health
curl https://api.retail.example.com/actuator/health

# Monitor resources
kubectl top pods -n gomco
```

---

## Upgrade & Maintenance

### Upgrade

```bash
# Upgrade to new version
helm upgrade retail ./helm-chart/shop-manager \
  -n gomco \
  -f my-values.yaml \
  --wait

# Upgrade with new image
helm upgrade retail ./helm-chart/shop-manager \
  -n gomco \
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

### Backup

```bash
# PostgreSQL backup
kubectl exec retail-postgresql-0 -n gomco -- \
  pg_dump -U retail retaildb | gzip > backup-$(date +%Y%m%d).sql.gz

# Full namespace backup (with Velero)
velero backup create shop-manager-backup --include-namespaces gomco
```

---

## Troubleshooting

### Common Issues

**Pods not starting:**
```bash
kubectl get pods -n gomco
kubectl describe pod <pod-name> -n gomco
kubectl logs <pod-name> -n gomco
```

**Certificate warnings:**
```bash
# Check certificates
kubectl get certificate -n gomco

# Reinstall CA certificate (see Helm README)
```

**DNS not resolving:**
```bash
# Check hosts file or DNS configuration
cat /etc/hosts | grep retail

# Flush DNS cache
# macOS: sudo dscacheutil -flushcache
# Windows: ipconfig /flushdns
```

For detailed troubleshooting, see [Helm Chart README - Troubleshooting](./helm-chart/shop-manager/README.md#monitoring--troubleshooting).

---

## Security

### Production Security Checklist

- [ ] Change all default passwords
- [ ] Use external secret manager (Vault, AWS Secrets Manager)
- [ ] Enable network policies
- [ ] Configure RBAC properly
- [ ] Enable audit logging
- [ ] Use Let's Encrypt or valid SSL certificates
- [ ] Configure backup encryption
- [ ] Enable pod security policies
- [ ] Implement rate limiting
- [ ] Configure monitoring and alerting

### Generate Secure Secrets

```bash
# JWT Secret (32+ characters)
openssl rand -base64 32

# Encryption Key (32+ characters)
openssl rand -base64 32

# Passwords (24+ characters)
openssl rand -base64 24
```

---

## Monitoring & Observability

### Prometheus & Grafana

```yaml
# Enable monitoring in values.yaml
serviceMonitor:
  enabled: true
  namespace: monitoring
  interval: 30s
```

### Logging

```bash
# Centralized logging
kubectl logs -n gomco -l app.kubernetes.io/name=shop-manager --tail=100 -f

# Export to ELK/Loki
# Configure in values.yaml
```

### Alerts

```yaml
# Configure alerting
alerts:
  enabled: true
  rules:
    - alert: HighErrorRate
      expr: rate(http_requests_total{status="500"}[5m]) > 0.05
```

---

## Support & Documentation

- **Helm Chart README**: [Complete deployment documentation](./helm-chart/shop-manager/README.md)
- **Developer Guide**: [Local development setup](./DEVELOPER_GUIDE.md)
- **Testing Guide**: [Testing procedures](./TESTING-GUIDE.md)
- **GitHub Issues**: [Report issues](https://github.com/your-org/shop-manager/issues)
- **GitHub Discussions**: [Ask questions](https://github.com/your-org/shop-manager/discussions)

---

## Additional Resources

### Architecture

Shop Manager uses a microservices architecture:

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

### Features

- ✅ **Multi-Tenant Architecture** - Complete tenant isolation
- ✅ **Investment Management** - ROI tracking and profit sharing
- ✅ **Sales & Inventory** - Comprehensive retail management
- ✅ **Authentication** - Keycloak SSO with OAuth2/OIDC
- ✅ **Event-Driven** - Kafka-based analytics
- ✅ **Customizable Branding** - Per-tenant customization
- ✅ **Secure by Default** - TLS/SSL, audit logging

---

**For complete deployment documentation, see the [Helm Chart README](./helm-chart/shop-manager/README.md).**

---

**Last Updated**: October 2025
**Version**: 1.0.0