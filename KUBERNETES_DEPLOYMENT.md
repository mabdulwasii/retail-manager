# 🚀 Shop Manager - Complete Kubernetes Deployment Guide

## 📋 Table of Contents
1. [Overview](#overview)
2. [Prerequisites](#prerequisites)
3. [Quick Start](#quick-start)
4. [Automated Certificate Installation](#automated-certificate-installation)
5. [Detailed Deployment Steps](#detailed-deployment-steps)
6. [Helm Chart Deployment](#helm-chart-deployment)
7. [Custom Keycloak Theme](#custom-keycloak-theme)
8. [SSL/TLS Configuration](#ssltls-configuration)
9. [Production Deployment](#production-deployment)
10. [Monitoring and Operations](#monitoring-and-operations)
11. [Troubleshooting](#troubleshooting)
12. [Backup and Recovery](#backup-and-recovery)

## Overview

Shop Manager is a comprehensive retail management platform designed for Kubernetes deployment. This guide covers everything needed for deploying Shop Manager on Kubernetes, from development to production.

### Architecture Components
- **Backend**: Spring Boot microservices with Spring Modulith
- **Frontend**: React TypeScript application
- **Database**: PostgreSQL with Flyway migrations
- **Authentication**: Keycloak SSO with custom theme
- **Message Queue**: Apache Kafka
- **Storage**: MinIO (S3-compatible)
- **Monitoring**: Prometheus & Grafana (optional)

## Prerequisites

### Required Tools
- Kubernetes cluster (1.19+)
- kubectl (1.19+)
- Helm 3.x
- Docker Desktop or Minikube (for local development)
- Git

### System Requirements
- **Development**: 8GB RAM, 4 CPU cores
- **Production**: 16GB RAM, 8 CPU cores (minimum)
- **Storage**: 100GB available disk space

## Quick Start

### 1. Clone Repository
```bash
git clone https://github.com/yourusername/shop-manager.git
cd shop-manager
```

### 2. Create Namespace
```bash
kubectl create namespace shop-manager
kubectl config set-context --current --namespace=shop-manager
```

### 3. Deploy with Helm (Recommended)
```bash
# Install with default values
helm install shop-manager ./helm-chart/shop-manager --namespace shop-manager

# Or with custom values
helm install shop-manager ./helm-chart/shop-manager \
  --namespace shop-manager \
  --values ./helm-chart/shop-manager/values-tls.yaml
```

### 4. Verify Deployment
```bash
kubectl get pods -n shop-manager
kubectl get services -n shop-manager
```

### 5. Access Application
```bash
# Port forward for local access
kubectl port-forward service/shop-manager-frontend 3000:80 -n shop-manager &
kubectl port-forward service/shop-manager-keycloak 8080:80 -n shop-manager &

# Access URLs
# Frontend: http://localhost:3000
# Keycloak: http://localhost:8080
```

## 🔐 Automated Certificate Installation

### ✅ Quick Certificate Setup

Shop Manager includes automated SSL certificate installation for on-premise deployments:

```bash
# Deploy with automated certificates
helm install shop-manager ./helm-chart/shop-manager \
  --namespace shop-manager \
  --create-namespace \
  --set tls.localCertInstallation.enabled=true \
  --set global.domain="shop-manager.local"

# Wait for certificate installation job
kubectl wait --for=condition=complete job/shop-manager-cert-installer -n shop-manager --timeout=300s

# Extract and run installation script
kubectl cp shop-manager/$(kubectl get pods -n shop-manager -l job-name=shop-manager-cert-installer -o jsonpath='{.items[0].metadata.name}'):/shared/install-macos.sh ./install-macos.sh
chmod +x install-macos.sh && sudo ./install-macos.sh
```

### 🔧 Certificate Installation Configuration

```yaml
# In values.yaml or production-values.yaml
tls:
  enabled: true
  issuer: local-ca-issuer
  localCertInstallation:
    enabled: true  # Enable automated certificate installation
    platforms: [macOS, linux, windows]

    installMethods:
      macOS:
        keychain: true
        trust: trustRoot  # System-wide trust
        browsers: [safari, chrome, firefox]

      linux:
        distributions: [debian, redhat, alpine]
        updateCaStore: true

      windows:
        scope: LocalMachine  # or CurrentUser
        store: Root

    postInstall:
      verifyInstallation: true
      testConnectivity: true
      generateInstructions: true
```

### 📱 Platform-Specific Installation

After deployment, extract and run the appropriate script:

```bash
# macOS Installation
kubectl cp shop-manager/cert-installer:/shared/install-macos.sh ./install-macos.sh
chmod +x install-macos.sh
sudo ./install-macos.sh  # System-wide installation

# Linux Installation
kubectl cp shop-manager/cert-installer:/shared/install-linux.sh ./install-linux.sh
chmod +x install-linux.sh
sudo ./install-linux.sh

# Windows Installation
kubectl cp shop-manager/cert-installer:/shared/install-windows.ps1 ./install-windows.ps1
# Run as Administrator in PowerShell: .\install-windows.ps1
```

### ✅ Verification

After certificate installation:

```bash
# Test HTTPS connectivity
curl -I https://shop-manager.local
curl -I https://api.shop-manager.local/actuator/health
curl -I https://auth.shop-manager.local

# Access in browser (no SSL warnings)
open https://shop-manager.local
open https://auth.shop-manager.local/admin
```

### 🎯 Benefits

- **No SSL Warnings**: Browser shows secure connection
- **Automated Setup**: Zero manual certificate management
- **Cross-Platform**: Works on macOS, Linux, Windows
- **Production Ready**: Suitable for enterprise deployments
- **Easy Installation**: One-command deployment with certificates

## Detailed Deployment Steps

### Step 1: Infrastructure Components

#### Deploy PostgreSQL
```bash
kubectl apply -f k8s/postgres/
# Or with Helm
helm install postgresql bitnami/postgresql \
  --namespace shop-manager \
  --set auth.postgresPassword=postgres \
  --set auth.database=shopmanager
```

#### Deploy Kafka
```bash
kubectl apply -f k8s/kafka/
# Or with Helm
helm install kafka bitnami/kafka \
  --namespace shop-manager \
  --set kraft.enabled=true
```

#### Deploy MinIO
```bash
kubectl apply -f k8s/minio/
# Or with Helm
helm install minio bitnami/minio \
  --namespace shop-manager \
  --set auth.rootUser=admin \
  --set auth.rootPassword=minio123
```

### Step 2: Deploy Keycloak with Custom Theme

**Note**: When using the Helm chart deployment (recommended), Keycloak with custom theme is deployed automatically. This manual deployment is only needed for custom setups.

#### Deploy Keycloak
```bash
# Using Bitnami Helm chart
helm install keycloak bitnami/keycloak \
  --namespace shop-manager \
  --set auth.adminPassword='KeycloakAdm1n@2024!SecureAuth#CompliantPassword' \
  --set postgresql.enabled=false \
  --set externalDatabase.host=postgresql \
  --set externalDatabase.database=keycloak \
  --set externalDatabase.user=postgres \
  --set externalDatabase.password=postgres
```

#### Verify Custom Theme Installation
```bash
# Theme is automatically deployed via Helm chart
# Verify theme ConfigMap exists
kubectl get configmap shop-manager-keycloak-theme -n shop-manager

# Verify theme installation job completed
kubectl get job shop-manager-keycloak-theme-init -n shop-manager

# Check theme files in Keycloak pod
kubectl exec -n shop-manager shop-manager-keycloak-0 -- \
  ls -la /opt/bitnami/keycloak/themes/shop-manager/login/
```

#### Configure Realm and Users
```bash
# Create shop-manager realm
kubectl exec -n shop-manager shop-manager-keycloak-0 -- \
  /opt/bitnami/keycloak/bin/kcadm.sh create realms \
  -s realm=shop-manager \
  -s enabled=true \
  -s loginTheme=shop-manager \
  --server http://localhost:8080 \
  --realm master \
  --user admin \
  --password 'KeycloakAdm1n@2024!SecureAuth#CompliantPassword'

# Create test users (development only)
kubectl exec -n shop-manager shop-manager-keycloak-0 -- \
  /opt/bitnami/keycloak/bin/kcadm.sh create users \
  -r shop-manager \
  -s username=admin@shopmanager.com \
  -s email=admin@shopmanager.com \
  -s enabled=true \
  --server http://localhost:8080 \
  --realm master \
  --user admin \
  --password 'KeycloakAdm1n@2024!SecureAuth#CompliantPassword'

# Set user password
kubectl exec -n shop-manager shop-manager-keycloak-0 -- \
  /opt/bitnami/keycloak/bin/kcadm.sh set-password \
  -r shop-manager \
  --username admin@shopmanager.com \
  --new-password admin123 \
  --server http://localhost:8080 \
  --realm master \
  --user admin \
  --password 'KeycloakAdm1n@2024!SecureAuth#CompliantPassword'
```

### Step 3: Deploy Backend Services

#### Apply Database Migrations
```bash
kubectl apply -f k8s/migrations/flyway-job.yaml
kubectl wait --for=condition=complete job/flyway-migration -n shop-manager
```

#### Deploy Backend
```bash
kubectl apply -f k8s/backend/
# Or build and deploy custom image
docker build -t shop-manager-backend:latest ./backend
docker tag shop-manager-backend:latest your-registry/shop-manager-backend:latest
docker push your-registry/shop-manager-backend:latest
kubectl set image deployment/shop-manager-backend \
  backend=your-registry/shop-manager-backend:latest \
  -n shop-manager
```

### Step 4: Deploy Frontend

```bash
kubectl apply -f k8s/frontend/
# Or build and deploy custom image
cd frontend
npm run build
docker build -t shop-manager-frontend:latest .
docker tag shop-manager-frontend:latest your-registry/shop-manager-frontend:latest
docker push your-registry/shop-manager-frontend:latest
kubectl set image deployment/shop-manager-frontend \
  frontend=your-registry/shop-manager-frontend:latest \
  -n shop-manager
```

## Helm Chart Deployment

### Chart Structure
```
helm-chart/shop-manager/
├── Chart.yaml
├── Chart.lock
├── values.yaml
├── charts/
│   ├── kafka/
│   ├── keycloak/
│   ├── minio/
│   └── postgresql/
├── config/
│   ├── keycloak-realm.json
│   └── shop-manager-realm-config.yaml
├── keycloak-theme/               # ← NEW: Consolidated theme structure
│   └── shop-manager/
│       └── login/
│           ├── theme.properties  # Theme configuration
│           ├── template.ftl      # Base template
│           ├── login.ftl         # Login page template
│           └── resources/css/
│               └── shop-manager.css  # Custom styles
├── templates/
│   ├── backend-deployment.yaml
│   ├── backend-service.yaml
│   ├── frontend-deployment.yaml
│   ├── frontend-service.yaml
│   ├── ingress.yaml
│   ├── configmap.yaml
│   ├── secrets.yaml
│   ├── keycloak-theme-configmap.yaml    # ← NEW: Auto-deployed theme
│   ├── keycloak-theme-init-job.yaml     # ← NEW: Theme installation job
│   ├── keycloak-theme-rbac.yaml         # ← NEW: Theme installer permissions
│   ├── keycloak-init-job.yaml
│   └── NOTES.txt
└── values files:
    ├── values-dns.yaml
    ├── values-infra.yaml
    ├── values-minimal.yaml
    ├── values-nodeport.yaml
    ├── values-simple.yaml
    └── values-tls.yaml
```

### Configuration Examples

#### Development Values (values/development.yaml)
```yaml
global:
  environment: development
  domain: shop-manager.local

backend:
  replicas: 1
  resources:
    requests:
      memory: "512Mi"
      cpu: "250m"
    limits:
      memory: "1Gi"
      cpu: "500m"

frontend:
  replicas: 1

keycloak:
  enabled: true
  customTheme:
    enabled: true

postgresql:
  enabled: true
  persistence:
    size: 10Gi

kafka:
  enabled: true
  persistence:
    size: 10Gi

minio:
  enabled: true
  persistence:
    size: 20Gi
```

#### Production Values (values/production.yaml)
```yaml
global:
  environment: production
  domain: shop-manager.example.com

backend:
  replicas: 3
  autoscaling:
    enabled: true
    minReplicas: 3
    maxReplicas: 10
    targetCPU: 70
  resources:
    requests:
      memory: "2Gi"
      cpu: "1000m"
    limits:
      memory: "4Gi"
      cpu: "2000m"

frontend:
  replicas: 3
  autoscaling:
    enabled: true

keycloak:
  enabled: true
  replicas: 2
  customTheme:
    enabled: true

postgresql:
  enabled: true
  architecture: replication
  persistence:
    size: 100Gi
  backup:
    enabled: true
    schedule: "0 2 * * *"

kafka:
  enabled: true
  replicas: 3
  persistence:
    size: 100Gi

minio:
  enabled: true
  mode: distributed
  persistence:
    size: 200Gi

monitoring:
  enabled: true
  prometheus:
    enabled: true
  grafana:
    enabled: true
```

### Deployment Commands

```bash
# Install with environment-specific values
helm install shop-manager ./helm-chart/shop-manager \
  --namespace shop-manager \
  --values ./helm-chart/shop-manager/values-tls.yaml

# Upgrade deployment
helm upgrade shop-manager ./helm-chart/shop-manager \
  --namespace shop-manager \
  --values ./helm-chart/shop-manager/values-tls.yaml

# Rollback if needed
helm rollback shop-manager 1 --namespace shop-manager

# Uninstall
helm uninstall shop-manager --namespace shop-manager

# Clean up namespace (optional)
kubectl delete namespace shop-manager
```

## Custom Keycloak Theme

### Automated Deployment (Default)

The custom Keycloak theme is **automatically deployed** with every Helm installation. No manual intervention required!

```bash
# Theme is deployed automatically when installing shop-manager
helm install shop-manager ./helm-chart/shop-manager --namespace shop-manager

# Verify theme deployment
kubectl get configmap shop-manager-keycloak-theme -n shop-manager
kubectl get job shop-manager-keycloak-theme-init -n shop-manager
```

### Theme Configuration

The theme is configured via Helm values:

```yaml
keycloak:
  enabled: true
  customTheme:
    enabled: true          # Enable custom theme (default: true)
    name: "shop-manager"    # Theme name (default: "shop-manager")
    applyToRealm: true      # Auto-apply to shop-manager realm (default: true)
```

### Theme Structure

```
helm-chart/shop-manager/keycloak-theme/
└── shop-manager/
    └── login/
        ├── theme.properties      # Theme configuration
        ├── template.ftl          # Base template with branding
        ├── login.ftl             # Login form with enhancements
        └── resources/css/
            └── shop-manager.css  # Custom styles and animations
```

### Manual Theme Update (Advanced)

To update theme files after deployment:

```bash
# Update theme files in helm-chart/shop-manager/keycloak-theme/
# Then upgrade the Helm release
helm upgrade shop-manager ./helm-chart/shop-manager --namespace shop-manager

# Or manually update ConfigMap
kubectl create configmap shop-manager-keycloak-theme \
  --from-file=helm-chart/shop-manager/keycloak-theme/shop-manager/login \
  --namespace=shop-manager \
  --dry-run=client -o yaml | kubectl apply -f -

# Restart Keycloak to reload theme
kubectl rollout restart statefulset/shop-manager-keycloak -n shop-manager
```

### Theme Features
- **🎨 Custom Shop Manager branding** - Logo, colors, and layout
- **👁️ Password visibility toggle** - Enhanced user experience
- **☑️ Enhanced remember me** - Improved checkbox styling
- **🧪 Development auto-fill** - Quick login buttons for testing
- **✨ Animated backgrounds** - Floating retail-themed elements
- **📱 Responsive design** - Mobile-optimized login experience
- **♿ Accessibility** - ARIA labels and keyboard navigation
- **🌙 High contrast support** - Better visibility for all users

## SSL/TLS Configuration

### Using cert-manager

#### Install cert-manager
```bash
kubectl apply -f https://github.com/cert-manager/cert-manager/releases/download/v1.12.0/cert-manager.yaml
```

#### Create ClusterIssuer for Let's Encrypt
```yaml
apiVersion: cert-manager.io/v1
kind: ClusterIssuer
metadata:
  name: letsencrypt-prod
spec:
  acme:
    server: https://acme-v02.api.letsencrypt.org/directory
    email: admin@example.com
    privateKeySecretRef:
      name: letsencrypt-prod
    solvers:
    - http01:
        ingress:
          class: nginx
```

#### Configure Ingress with TLS
```yaml
apiVersion: networking.k8s.io/v1
kind: Ingress
metadata:
  name: shop-manager-ingress
  namespace: shop-manager
  annotations:
    cert-manager.io/cluster-issuer: letsencrypt-prod
    nginx.ingress.kubernetes.io/force-ssl-redirect: "true"
spec:
  ingressClassName: nginx
  tls:
  - hosts:
    - shop-manager.example.com
    - auth.shop-manager.example.com
    - api.shop-manager.example.com
    secretName: shop-manager-tls
  rules:
  - host: shop-manager.example.com
    http:
      paths:
      - path: /
        pathType: Prefix
        backend:
          service:
            name: shop-manager-frontend
            port:
              number: 80
  - host: auth.shop-manager.example.com
    http:
      paths:
      - path: /
        pathType: Prefix
        backend:
          service:
            name: shop-manager-keycloak
            port:
              number: 80
  - host: api.shop-manager.example.com
    http:
      paths:
      - path: /
        pathType: Prefix
        backend:
          service:
            name: shop-manager-backend
            port:
              number: 8081
```

### Self-Signed Certificates (Development)
```bash
# Generate self-signed certificate
openssl req -x509 -nodes -days 365 -newkey rsa:2048 \
  -keyout tls.key -out tls.crt \
  -subj "/CN=*.shop-manager.local"

# Create TLS secret
kubectl create secret tls shop-manager-tls \
  --cert=tls.crt \
  --key=tls.key \
  --namespace=shop-manager
```

## Production Deployment

### Pre-Production Checklist

#### Security
- [ ] Change all default passwords
- [ ] Enable RBAC for Kubernetes
- [ ] Configure network policies
- [ ] Enable pod security policies
- [ ] Set up audit logging
- [ ] Configure secrets encryption at rest
- [ ] Enable TLS for all services
- [ ] Configure firewall rules

#### High Availability
- [ ] Deploy multiple replicas (minimum 3)
- [ ] Configure pod anti-affinity
- [ ] Set up database replication
- [ ] Configure Kafka cluster (3+ brokers)
- [ ] Enable Keycloak clustering
- [ ] Set up load balancing

#### Monitoring
- [ ] Deploy Prometheus and Grafana
- [ ] Configure alerting rules
- [ ] Set up log aggregation (ELK/EFK)
- [ ] Enable application metrics
- [ ] Configure health checks

#### Backup
- [ ] Database backup strategy
- [ ] Persistent volume backups
- [ ] Configuration backup
- [ ] Disaster recovery plan

### Production Configuration

#### Resource Limits
```yaml
resources:
  requests:
    memory: "2Gi"
    cpu: "1000m"
  limits:
    memory: "4Gi"
    cpu: "2000m"
```

#### Autoscaling
```yaml
apiVersion: autoscaling/v2
kind: HorizontalPodAutoscaler
metadata:
  name: shop-manager-backend-hpa
  namespace: shop-manager
spec:
  scaleTargetRef:
    apiVersion: apps/v1
    kind: Deployment
    name: shop-manager-backend
  minReplicas: 3
  maxReplicas: 10
  metrics:
  - type: Resource
    resource:
      name: cpu
      target:
        type: Utilization
        averageUtilization: 70
  - type: Resource
    resource:
      name: memory
      target:
        type: Utilization
        averageUtilization: 80
```

#### Network Policies
```yaml
apiVersion: networking.k8s.io/v1
kind: NetworkPolicy
metadata:
  name: shop-manager-network-policy
  namespace: shop-manager
spec:
  podSelector:
    matchLabels:
      app: shop-manager
  policyTypes:
  - Ingress
  - Egress
  ingress:
  - from:
    - namespaceSelector:
        matchLabels:
          name: shop-manager
    - podSelector:
        matchLabels:
          app: shop-manager
  egress:
  - to:
    - namespaceSelector:
        matchLabels:
          name: shop-manager
  - to:
    - namespaceSelector:
        matchLabels:
          name: kube-system
    ports:
    - protocol: TCP
      port: 53
    - protocol: UDP
      port: 53
```

## Monitoring and Operations

### Health Checks

#### Liveness Probe
```yaml
livenessProbe:
  httpGet:
    path: /actuator/health/liveness
    port: 8081
  initialDelaySeconds: 120
  periodSeconds: 10
  timeoutSeconds: 5
  failureThreshold: 3
```

#### Readiness Probe
```yaml
readinessProbe:
  httpGet:
    path: /actuator/health/readiness
    port: 8081
  initialDelaySeconds: 30
  periodSeconds: 10
  timeoutSeconds: 5
  failureThreshold: 3
```

### Monitoring Stack

#### Deploy Prometheus
```bash
helm install prometheus prometheus-community/kube-prometheus-stack \
  --namespace monitoring \
  --create-namespace
```

#### Deploy Grafana Dashboards
```bash
kubectl apply -f monitoring/grafana-dashboards/
```

### Logging

#### Deploy Elasticsearch, Fluentd, Kibana (EFK)
```bash
helm install efk elastic/elasticsearch \
  --namespace logging \
  --create-namespace

helm install fluentd fluent/fluentd \
  --namespace logging

helm install kibana elastic/kibana \
  --namespace logging
```

## Troubleshooting

### Common Issues

#### Pods Not Starting
```bash
# Check pod status
kubectl describe pod <pod-name> -n shop-manager

# Check logs
kubectl logs <pod-name> -n shop-manager

# Check events
kubectl get events -n shop-manager --sort-by='.lastTimestamp'
```

#### Database Connection Issues
```bash
# Test database connectivity
kubectl run -it --rm debug --image=postgres:14 --restart=Never -- \
  psql -h postgresql -U postgres -d shopmanager

# Check database logs
kubectl logs postgresql-0 -n shop-manager
```

#### Keycloak Issues
```bash
# Check Keycloak logs
kubectl logs shop-manager-keycloak-0 -n shop-manager

# Access Keycloak admin console via ingress
open https://auth.shop-manager.local/admin/master/console/
# Credentials: admin / KeycloakAdm1n@2024!SecureAuth#CompliantPassword

# Port forward if ingress not working
kubectl port-forward service/shop-manager-keycloak 8080:80 -n shop-manager
open http://localhost:8080/admin/

# Reset admin password if needed
kubectl exec -n shop-manager shop-manager-keycloak-0 -- \
  /opt/bitnami/keycloak/bin/kcadm.sh set-password \
  --username admin \
  --new-password NewPassword123! \
  --server http://localhost:8080 \
  --realm master
```

#### Keycloak Admin Console Iframe Timeout
If the admin console shows "Loading the Admin UI" or iframe timeout errors:

```bash
# Fix hostname configuration
kubectl patch configmap shop-manager-keycloak-env-vars -n shop-manager --patch '{"data":{"KC_HOSTNAME":"auth.shop-manager.local","KC_HOSTNAME_STRICT_BACKCHANNEL":"false","KC_SPI_LOGIN_PROTOCOL_OPENID_CONNECT_SUPPRESS_LOGOUT_CONFIRMATION_SCREEN":"true"}}'

# Restart Keycloak
kubectl rollout restart statefulset/shop-manager-keycloak -n shop-manager

# Wait for restart
kubectl wait --for=condition=ready pod/shop-manager-keycloak-0 -n shop-manager --timeout=120s

# Verify access
curl -I https://auth.shop-manager.local/admin/master/console/ -k
```

#### Theme Not Appearing
```bash
# Clear Keycloak theme cache
kubectl exec -n shop-manager shop-manager-keycloak-0 -- \
  rm -rf /opt/bitnami/keycloak/standalone/tmp/

# Restart Keycloak
kubectl rollout restart statefulset/shop-manager-keycloak -n shop-manager

# Verify theme files
kubectl exec -n shop-manager shop-manager-keycloak-0 -- \
  ls -la /opt/bitnami/keycloak/themes/shop-manager/login/
```

### Debug Commands

```bash
# Get cluster info
kubectl cluster-info

# Check node status
kubectl get nodes

# Check all resources in namespace
kubectl get all -n shop-manager

# Describe specific resource
kubectl describe deployment/shop-manager-backend -n shop-manager

# Execute commands in pod
kubectl exec -it <pod-name> -n shop-manager -- /bin/bash

# Copy files from pod
kubectl cp shop-manager/<pod-name>:/path/to/file ./local-file

# Port forwarding for debugging
kubectl port-forward pod/<pod-name> 8080:8080 -n shop-manager

# Check resource usage
kubectl top nodes
kubectl top pods -n shop-manager
```

## Backup and Recovery

### Database Backup

#### Manual Backup
```bash
# Create backup
kubectl exec -n shop-manager postgresql-0 -- \
  pg_dump -U postgres shopmanager > backup-$(date +%Y%m%d).sql

# Restore backup
kubectl exec -i -n shop-manager postgresql-0 -- \
  psql -U postgres shopmanager < backup-20240121.sql
```

#### Automated Backup with CronJob
```yaml
apiVersion: batch/v1
kind: CronJob
metadata:
  name: postgres-backup
  namespace: shop-manager
spec:
  schedule: "0 2 * * *"
  jobTemplate:
    spec:
      template:
        spec:
          containers:
          - name: postgres-backup
            image: postgres:14
            env:
            - name: PGPASSWORD
              valueFrom:
                secretKeyRef:
                  name: postgresql
                  key: postgres-password
            command:
            - /bin/bash
            - -c
            - |
              DATE=$(date +%Y%m%d_%H%M%S)
              pg_dump -h postgresql -U postgres shopmanager > /backup/shopmanager-$DATE.sql
              # Upload to S3/MinIO
              mc cp /backup/shopmanager-$DATE.sql minio/backups/
            volumeMounts:
            - name: backup
              mountPath: /backup
          volumes:
          - name: backup
            persistentVolumeClaim:
              claimName: backup-pvc
          restartPolicy: OnFailure
```

### Persistent Volume Backup

```bash
# Using Velero
velero install --provider aws \
  --bucket velero \
  --secret-file ./credentials-velero \
  --backup-location-config region=minio,s3ForcePathStyle=true,s3Url=http://minio:9000

# Create backup
velero backup create shop-manager-backup --include-namespaces shop-manager

# Restore
velero restore create --from-backup shop-manager-backup
```

### Configuration Backup

```bash
# Export all resources
kubectl get all,cm,secret,pvc,pv -n shop-manager -o yaml > shop-manager-backup.yaml

# Export Helm values
helm get values shop-manager -n shop-manager > values-backup.yaml
```

## Maintenance

### Rolling Updates

```bash
# Update backend image
kubectl set image deployment/shop-manager-backend \
  backend=your-registry/shop-manager-backend:v2.0.0 \
  -n shop-manager

# Watch rollout status
kubectl rollout status deployment/shop-manager-backend -n shop-manager

# Rollback if needed
kubectl rollout undo deployment/shop-manager-backend -n shop-manager
```

### Scaling

```bash
# Manual scaling
kubectl scale deployment shop-manager-backend --replicas=5 -n shop-manager

# Autoscaling
kubectl autoscale deployment shop-manager-backend \
  --min=3 --max=10 --cpu-percent=70 -n shop-manager
```

### Certificate Renewal

```bash
# Check certificate expiration
kubectl get certificate -n shop-manager
kubectl describe certificate shop-manager-tls -n shop-manager

# Force renewal (cert-manager)
kubectl delete secret shop-manager-tls -n shop-manager
# cert-manager will automatically recreate it
```

## Security Best Practices

1. **Use RBAC**: Implement proper role-based access control
2. **Network Policies**: Restrict pod-to-pod communication
3. **Secrets Management**: Use sealed-secrets or external secret operators
4. **Image Scanning**: Scan container images for vulnerabilities
5. **Admission Controllers**: Use OPA or Kyverno for policy enforcement
6. **Audit Logging**: Enable and monitor Kubernetes audit logs
7. **Regular Updates**: Keep Kubernetes and all components updated
8. **Least Privilege**: Run containers as non-root users
9. **Resource Quotas**: Set appropriate resource limits and quotas
10. **Backup Strategy**: Regular backups with tested restore procedures

## Support and Documentation

- **GitHub Repository**: [shop-manager](https://github.com/yourusername/shop-manager)
- **Issue Tracking**: GitHub Issues
- **Documentation**: This guide and inline code documentation
- **Helm Chart**: `helm-chart/` directory in the repository

## License

This deployment guide is part of the Shop Manager platform and follows the same licensing terms.

---

**Last Updated**: January 2025
**Version**: 1.0.0