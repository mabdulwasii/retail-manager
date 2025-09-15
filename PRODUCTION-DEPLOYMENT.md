# Shop Manager Production Deployment Guide for Kubernetes Engine

This guide provides comprehensive instructions for deploying Shop Manager to production Kubernetes clusters (GKE, EKS, AKS, or self-managed).

## 📋 Table of Contents

- [Prerequisites](#prerequisites)
- [Pre-Deployment Checklist](#pre-deployment-checklist)
- [Cloud Provider Setup](#cloud-provider-setup)
- [Deployment Steps](#deployment-steps)
- [Post-Deployment Configuration](#post-deployment-configuration)
- [Security Hardening](#security-hardening)
- [Monitoring & Observability](#monitoring--observability)
- [Backup & Disaster Recovery](#backup--disaster-recovery)
- [Maintenance & Operations](#maintenance--operations)

## 🔧 Prerequisites

### Infrastructure Requirements
- Kubernetes cluster v1.25+ with at least 3 nodes
- Node specifications: minimum 4 vCPU, 16GB RAM per node
- 500GB+ persistent storage available
- Load balancer support
- DNS management access

### Tools Required
```bash
# Install required CLI tools
kubectl version --client
helm version
gcloud version  # For GKE
aws --version   # For EKS
az --version    # For AKS

# Install additional tools
kubectl krew install neat
kubectl krew install tree
helm plugin install https://github.com/databus23/helm-diff
```

### Required Kubernetes Add-ons
- **Ingress Controller**: NGINX Ingress Controller
- **Cert Manager**: For TLS certificate management
- **Metrics Server**: For HPA functionality
- **Monitoring Stack**: Prometheus & Grafana
- **Secret Management**: Sealed Secrets or External Secrets Operator

## 📝 Pre-Deployment Checklist

### Domain & Certificates
- [ ] Domain registered and DNS configured
- [ ] Subdomains created:
  - `shop-manager.example.com` (frontend)
  - `api.shop-manager.example.com` (backend API)
  - `auth.shop-manager.example.com` (Keycloak)
- [ ] SSL certificates ready or cert-manager configured

### Container Registry
- [ ] Container registry configured (GCR, ECR, ACR, or private)
- [ ] Images built and pushed:
  ```bash
  # Build and push backend
  docker build -t gcr.io/your-project/shop-manager:1.0.0 ./backend
  docker push gcr.io/your-project/shop-manager:1.0.0

  # Build and push frontend
  docker build -t gcr.io/your-project/shop-manager-frontend:1.0.0 ./frontend
  docker push gcr.io/your-project/shop-manager-frontend:1.0.0
  ```

### Secrets Preparation
- [ ] Database passwords generated
- [ ] JWT secrets created
- [ ] Keycloak admin credentials prepared
- [ ] Backup encryption keys generated
- [ ] API keys configured

## ☁️ Cloud Provider Setup

### Google Kubernetes Engine (GKE)

```bash
# Create GKE cluster
gcloud container clusters create shop-manager-prod \
  --zone us-central1-a \
  --node-pool-name default-pool \
  --machine-type n2-standard-4 \
  --num-nodes 3 \
  --enable-autoscaling \
  --min-nodes 3 \
  --max-nodes 10 \
  --enable-autorepair \
  --enable-autoupgrade \
  --enable-stackdriver-kubernetes \
  --addons HorizontalPodAutoscaling,HttpLoadBalancing,GcePersistentDiskCsiDriver \
  --workload-pool=your-project.svc.id.goog \
  --enable-shielded-nodes

# Get credentials
gcloud container clusters get-credentials shop-manager-prod --zone us-central1-a

# Create node pools for specific workloads
gcloud container node-pools create database-pool \
  --cluster=shop-manager-prod \
  --zone=us-central1-a \
  --machine-type=n2-highmem-4 \
  --num-nodes=2 \
  --node-labels=workload=database \
  --node-taints=database=true:NoSchedule \
  --disk-size=100GB \
  --disk-type=pd-ssd
```

### Amazon Elastic Kubernetes Service (EKS)

```bash
# Create EKS cluster using eksctl
eksctl create cluster \
  --name shop-manager-prod \
  --region us-east-1 \
  --version 1.27 \
  --nodegroup-name standard-workers \
  --node-type m5.xlarge \
  --nodes 3 \
  --nodes-min 3 \
  --nodes-max 10 \
  --managed \
  --alb-ingress-access \
  --asg-access \
  --full-ecr-access

# Create node group for database
eksctl create nodegroup \
  --cluster shop-manager-prod \
  --name database-ng \
  --node-type r5.xlarge \
  --nodes 2 \
  --node-labels "workload=database" \
  --node-taints "database=true:NoSchedule"

# Update kubeconfig
aws eks update-kubeconfig --name shop-manager-prod --region us-east-1
```

### Azure Kubernetes Service (AKS)

```bash
# Create resource group
az group create --name shop-manager-rg --location eastus

# Create AKS cluster
az aks create \
  --resource-group shop-manager-rg \
  --name shop-manager-prod \
  --node-count 3 \
  --node-vm-size Standard_D4s_v3 \
  --enable-cluster-autoscaler \
  --min-count 3 \
  --max-count 10 \
  --enable-addons monitoring,azure-policy,azure-keyvault-secrets-provider \
  --generate-ssh-keys \
  --network-plugin azure \
  --network-policy calico

# Add node pool for database
az aks nodepool add \
  --resource-group shop-manager-rg \
  --cluster-name shop-manager-prod \
  --name dbpool \
  --node-count 2 \
  --node-vm-size Standard_E4s_v3 \
  --node-taints "database=true:NoSchedule" \
  --labels "workload=database"

# Get credentials
az aks get-credentials --resource-group shop-manager-rg --name shop-manager-prod
```

## 🚀 Deployment Steps

### 1. Install Prerequisites

```bash
# Install NGINX Ingress Controller
helm repo add ingress-nginx https://kubernetes.github.io/ingress-nginx
helm install ingress-nginx ingress-nginx/ingress-nginx \
  --namespace ingress-nginx \
  --create-namespace \
  --set controller.service.type=LoadBalancer \
  --set controller.metrics.enabled=true \
  --set controller.podAnnotations."prometheus\.io/scrape"=true

# Install Cert Manager
helm repo add jetstack https://charts.jetstack.io
helm install cert-manager jetstack/cert-manager \
  --namespace cert-manager \
  --create-namespace \
  --set installCRDs=true \
  --set global.leaderElection.namespace=cert-manager

# Install Sealed Secrets
helm repo add sealed-secrets https://bitnami-labs.github.io/sealed-secrets
helm install sealed-secrets sealed-secrets/sealed-secrets \
  --namespace kube-system \
  --set-string fullnameOverride=sealed-secrets-controller

# Install Metrics Server (if not present)
kubectl apply -f https://github.com/kubernetes-sigs/metrics-server/releases/latest/download/components.yaml
```

### 2. Create Production Namespace and Resources

```bash
# Apply namespace and resource configurations
kubectl apply -f kubernetes/production/namespace.yaml
kubectl apply -f kubernetes/production/storage-class.yaml
kubectl apply -f kubernetes/production/rbac.yaml
kubectl apply -f kubernetes/production/network-policy.yaml
```

### 3. Configure Secrets

```bash
# Generate secure passwords
export POSTGRES_PASSWORD=$(openssl rand -base64 32)
export KEYCLOAK_ADMIN_PASSWORD=$(openssl rand -base64 32)
export JWT_SECRET=$(openssl rand -base64 64)
export REDIS_PASSWORD=$(openssl rand -base64 32)

# Create sealed secrets
cat <<EOF | kubeseal --format=yaml > kubernetes/production/sealed-secrets.yaml
apiVersion: v1
kind: Secret
metadata:
  name: shop-manager-secrets
  namespace: shop-manager-prod
type: Opaque
stringData:
  POSTGRES_PASSWORD: "${POSTGRES_PASSWORD}"
  POSTGRES_ADMIN_PASSWORD: "${POSTGRES_PASSWORD}"
  KEYCLOAK_ADMIN_USER: "admin"
  KEYCLOAK_ADMIN_PASSWORD: "${KEYCLOAK_ADMIN_PASSWORD}"
  KEYCLOAK_CLIENT_SECRET: "$(openssl rand -base64 32)"
  KEYCLOAK_DB_PASSWORD: "$(openssl rand -base64 32)"
  KAFKA_PASSWORD: "$(openssl rand -base64 32)"
  REDIS_PASSWORD: "${REDIS_PASSWORD}"
  JWT_SECRET: "${JWT_SECRET}"
  JWT_REFRESH_SECRET: "$(openssl rand -base64 64)"
  BACKUP_ENCRYPTION_KEY: "$(openssl rand -base64 32)"
  BACKUP_ENCRYPTION_SALT: "$(openssl rand -base64 16)"
EOF

# Apply sealed secrets
kubectl apply -f kubernetes/production/sealed-secrets.yaml
```

### 4. Configure Container Registry Access

```bash
# For GCR
kubectl create secret docker-registry gcr-secret \
  --docker-server=gcr.io \
  --docker-username=_json_key \
  --docker-password="$(cat service-account-key.json)" \
  --namespace=shop-manager-prod

# For ECR
kubectl create secret docker-registry ecr-secret \
  --docker-server=${AWS_ACCOUNT_ID}.dkr.ecr.${AWS_REGION}.amazonaws.com \
  --docker-username=AWS \
  --docker-password=$(aws ecr get-login-password) \
  --namespace=shop-manager-prod

# For ACR
kubectl create secret docker-registry acr-secret \
  --docker-server=${ACR_NAME}.azurecr.io \
  --docker-username=${SERVICE_PRINCIPAL_ID} \
  --docker-password=${SERVICE_PRINCIPAL_PASSWORD} \
  --namespace=shop-manager-prod
```

### 5. Update Production Values

```bash
# Edit kubernetes/production/values-production.yaml
# Update the following:
# - global.domain: Your actual domain
# - image.repository: Your container registry
# - ingress.hosts: Your actual domains
# - config.keycloak URLs: Your auth domain

# Create a copy with your values
cp kubernetes/production/values-production.yaml my-values-production.yaml
# Edit my-values-production.yaml with your specific values
```

### 6. Deploy with Helm

```bash
# Add Bitnami repo for dependencies
helm repo add bitnami https://charts.bitnami.com/bitnami
helm repo update

# Update dependencies
cd helm-chart/shop-manager
helm dependency update

# Deploy to production
helm install shop-manager . \
  --namespace shop-manager-prod \
  --values ../../kubernetes/production/values-production.yaml \
  --values ../../my-values-production.yaml \
  --timeout 20m \
  --wait

# Monitor deployment
kubectl get pods -n shop-manager-prod -w
```

### 7. Configure DNS

```bash
# Get ingress IP address
INGRESS_IP=$(kubectl get ingress shop-manager-ingress -n shop-manager-prod -o jsonpath='{.status.loadBalancer.ingress[0].ip}')
echo "Configure DNS A records to point to: ${INGRESS_IP}"

# DNS Records to create:
# A record: shop-manager.example.com -> ${INGRESS_IP}
# A record: api.shop-manager.example.com -> ${INGRESS_IP}
# A record: auth.shop-manager.example.com -> ${INGRESS_IP}
```

## 🔧 Post-Deployment Configuration

### Verify Services

```bash
# Check all pods are running
kubectl get pods -n shop-manager-prod

# Check services
kubectl get svc -n shop-manager-prod

# Check ingress
kubectl get ingress -n shop-manager-prod

# Test endpoints
curl -I https://api.shop-manager.example.com/actuator/health
curl -I https://auth.shop-manager.example.com/realms/shop-manager/.well-known/openid-configuration
curl -I https://shop-manager.example.com
```

### Initialize Keycloak Realm

```bash
# The realm should be automatically imported via the init job
# Verify realm exists
kubectl logs -n shop-manager-prod job/shop-manager-keycloak-init

# If needed, manually import realm
kubectl exec -it deployment/shop-manager-keycloak -n shop-manager-prod -- \
  /opt/keycloak/bin/kcadm.sh config credentials \
  --server http://localhost:8080 \
  --realm master \
  --user admin \
  --password ${KEYCLOAK_ADMIN_PASSWORD}
```

### Database Migrations

```bash
# Verify Flyway migrations
kubectl logs -n shop-manager-prod deployment/shop-manager -c shop-manager | grep Flyway

# If needed, run migrations manually
kubectl exec -it deployment/shop-manager -n shop-manager-prod -- \
  java -jar /app.jar --spring.flyway.migrate=true
```

## 🔒 Security Hardening

### 1. Pod Security Policies

```yaml
# Apply pod security policies
kubectl apply -f - <<EOF
apiVersion: policy/v1beta1
kind: PodSecurityPolicy
metadata:
  name: shop-manager-psp
spec:
  privileged: false
  allowPrivilegeEscalation: false
  requiredDropCapabilities:
    - ALL
  volumes:
    - 'configMap'
    - 'emptyDir'
    - 'projected'
    - 'secret'
    - 'persistentVolumeClaim'
  hostNetwork: false
  hostIPC: false
  hostPID: false
  runAsUser:
    rule: 'MustRunAsNonRoot'
  seLinux:
    rule: 'RunAsAny'
  supplementalGroups:
    rule: 'RunAsAny'
  fsGroup:
    rule: 'RunAsAny'
  readOnlyRootFilesystem: true
EOF
```

### 2. Network Policies

```bash
# Apply network policies
kubectl apply -f kubernetes/production/network-policy.yaml

# Verify network policies
kubectl get networkpolicies -n shop-manager-prod
```

### 3. RBAC Configuration

```bash
# Create limited service accounts for CI/CD
kubectl create serviceaccount ci-deployer -n shop-manager-prod

# Grant minimal required permissions
kubectl create role ci-deployer-role \
  --verb=get,list,update,patch \
  --resource=deployments,services \
  -n shop-manager-prod

kubectl create rolebinding ci-deployer-binding \
  --role=ci-deployer-role \
  --serviceaccount=shop-manager-prod:ci-deployer \
  -n shop-manager-prod
```

### 4. Secrets Scanning

```bash
# Install and run secrets scanner
brew install trufflesecurity/trufflehog/trufflehog
trufflehog git https://github.com/your-repo/shop-manager --only-verified

# Scan container images
trivy image gcr.io/your-project/shop-manager:1.0.0
```

## 📊 Monitoring & Observability

### 1. Install Prometheus Stack

```bash
# Install kube-prometheus-stack
helm repo add prometheus-community https://prometheus-community.github.io/helm-charts
helm install monitoring prometheus-community/kube-prometheus-stack \
  --namespace monitoring \
  --create-namespace \
  --set prometheus.prometheusSpec.serviceMonitorSelectorNilUsesHelmValues=false \
  --set grafana.adminPassword=admin

# Apply ServiceMonitor for Shop Manager
kubectl apply -f kubernetes/production/monitoring.yaml
```

### 2. Configure Grafana Dashboards

```bash
# Port forward to Grafana
kubectl port-forward -n monitoring svc/monitoring-grafana 3000:80

# Access Grafana at http://localhost:3000
# Username: admin
# Password: admin (change immediately)

# Import dashboard ID: 12835 (Spring Boot Statistics)
# Import dashboard ID: 4701 (JVM Micrometer)
```

### 3. Setup Alerts

```bash
# Configure alert channels in Grafana
# 1. Add notification channel (Slack, PagerDuty, email)
# 2. Create alert rules for:
#    - High error rate (>5%)
#    - High response time (>2s)
#    - Pod restarts
#    - Memory/CPU usage >80%
#    - Database connection pool >90%
```

### 4. Application Performance Monitoring

```bash
# Optional: Setup APM (choose one)

# Datadog
helm install datadog-agent datadog/datadog \
  --set datadog.apiKey=${DATADOG_API_KEY} \
  --set datadog.apm.enabled=true \
  --set datadog.logs.enabled=true \
  --namespace monitoring

# New Relic
kubectl apply -f https://download.newrelic.com/install/kubernetes/pixie/latest/px.dev_viziers.yaml
kubectl apply -f https://download.newrelic.com/install/kubernetes/pixie/latest/olm_crd.yaml

# Elastic APM
helm install elastic-apm elastic/apm-server \
  --set apmConfig.output.elasticsearch.hosts=["elasticsearch:9200"] \
  --namespace monitoring
```

## 💾 Backup & Disaster Recovery

### 1. Database Backups

```bash
# Install Velero for cluster backup
velero install \
  --provider gcp \
  --plugins velero/velero-plugin-for-gcp:v1.7.0 \
  --bucket shop-manager-backups \
  --secret-file ./credentials-velero

# Create backup schedule
velero schedule create shop-manager-daily \
  --schedule="0 2 * * *" \
  --include-namespaces shop-manager-prod \
  --ttl 720h

# Manual database backup
kubectl exec -n shop-manager-prod deployment/shop-manager-postgresql -- \
  pg_dump -U shopmanager shopmanager_prod | \
  gzip > backup-$(date +%Y%m%d-%H%M%S).sql.gz

# Upload to cloud storage
gsutil cp backup-*.sql.gz gs://shop-manager-backups/database/
```

### 2. Application State Backup

```bash
# Backup Keycloak realm
kubectl exec -n shop-manager-prod deployment/shop-manager-keycloak -- \
  /opt/keycloak/bin/kc.sh export \
  --file /tmp/realm-export.json \
  --realm shop-manager

kubectl cp shop-manager-prod/shop-manager-keycloak-xxx:/tmp/realm-export.json \
  ./keycloak-realm-backup-$(date +%Y%m%d).json

# Backup persistent volumes
velero backup create pv-backup \
  --include-resources persistentvolumes,persistentvolumeclaims \
  --namespace shop-manager-prod
```

### 3. Disaster Recovery Plan

```bash
# Create DR runbook
cat > dr-runbook.md << EOF
# Disaster Recovery Runbook

## Recovery Time Objective (RTO): 4 hours
## Recovery Point Objective (RPO): 1 hour

### Steps for Full Recovery:

1. **Restore Cluster** (if needed)
   velero restore create --from-backup shop-manager-daily-20240101

2. **Restore Database**
   kubectl exec -i deployment/shop-manager-postgresql -- \
     psql -U shopmanager shopmanager_prod < backup.sql

3. **Restore Keycloak**
   kubectl exec -i deployment/shop-manager-keycloak -- \
     /opt/keycloak/bin/kc.sh import --file /tmp/realm-backup.json

4. **Verify Services**
   ./scripts/health-check.sh

5. **Update DNS** (if failover region)
   Update DNS records to point to new cluster IP
EOF
```

## 🔧 Maintenance & Operations

### Rolling Updates

```bash
# Update backend image
helm upgrade shop-manager ./helm-chart/shop-manager \
  --namespace shop-manager-prod \
  --reuse-values \
  --set image.tag=1.0.1 \
  --wait

# Monitor rollout
kubectl rollout status deployment/shop-manager -n shop-manager-prod

# Rollback if needed
helm rollback shop-manager -n shop-manager-prod
```

### Scaling Operations

```bash
# Manual scaling
kubectl scale deployment shop-manager --replicas=5 -n shop-manager-prod

# Update HPA limits
kubectl patch hpa shop-manager -n shop-manager-prod \
  -p '{"spec":{"maxReplicas":15}}'

# Scale database read replicas
helm upgrade shop-manager ./helm-chart/shop-manager \
  --namespace shop-manager-prod \
  --reuse-values \
  --set postgresql.readReplicas.replicaCount=3
```

### Maintenance Mode

```bash
# Enable maintenance mode
kubectl patch configmap shop-manager-config -n shop-manager-prod \
  -p '{"data":{"APP_SYSTEM_MAINTENANCE_MODE":"true"}}'

# Restart pods to apply
kubectl rollout restart deployment/shop-manager -n shop-manager-prod

# Disable maintenance mode
kubectl patch configmap shop-manager-config -n shop-manager-prod \
  -p '{"data":{"APP_SYSTEM_MAINTENANCE_MODE":"false"}}'
```

### Log Management

```bash
# View logs
kubectl logs -n shop-manager-prod deployment/shop-manager --tail=100 -f

# Export logs
kubectl logs -n shop-manager-prod -l app.kubernetes.io/name=shop-manager \
  --since=1h > production-logs-$(date +%Y%m%d-%H%M%S).log

# Setup log aggregation (optional)
helm install elasticsearch elastic/elasticsearch \
  --namespace logging \
  --create-namespace

helm install fluent-bit fluent/fluent-bit \
  --namespace logging \
  --set output.es.host=elasticsearch-master
```

## 📈 Performance Tuning

### JVM Optimization

```bash
# Update JVM settings
helm upgrade shop-manager ./helm-chart/shop-manager \
  --namespace shop-manager-prod \
  --reuse-values \
  --set-string env.JAVA_OPTS="-Xms2g -Xmx2g -XX:+UseG1GC -XX:MaxGCPauseMillis=200"
```

### Database Tuning

```bash
# Update PostgreSQL configuration
kubectl exec -it statefulset/shop-manager-postgresql -n shop-manager-prod -- \
  psql -U postgres -c "ALTER SYSTEM SET max_connections = 400;"

kubectl exec -it statefulset/shop-manager-postgresql -n shop-manager-prod -- \
  psql -U postgres -c "ALTER SYSTEM SET shared_buffers = '2GB';"

# Restart to apply
kubectl rollout restart statefulset/shop-manager-postgresql -n shop-manager-prod
```

## 🎯 Health Checks & Validation

### Automated Health Checks

```bash
# Create health check script
cat > health-check.sh << 'EOF'
#!/bin/bash

echo "Checking Shop Manager Production Health..."

# Check pods
echo "Checking pod status..."
kubectl get pods -n shop-manager-prod | grep -v Running && echo "WARNING: Some pods not running"

# Check endpoints
echo "Checking API health..."
curl -f https://api.shop-manager.example.com/actuator/health || echo "API health check failed"

echo "Checking auth service..."
curl -f https://auth.shop-manager.example.com/realms/shop-manager || echo "Auth check failed"

echo "Checking frontend..."
curl -f https://shop-manager.example.com || echo "Frontend check failed"

# Check database
echo "Checking database..."
kubectl exec deployment/shop-manager-postgresql -n shop-manager-prod -- \
  pg_isready -U shopmanager || echo "Database check failed"

echo "Health check complete!"
EOF

chmod +x health-check.sh
./health-check.sh
```

### Load Testing

```bash
# Install k6 for load testing
brew install k6

# Create load test
cat > load-test.js << 'EOF'
import http from 'k6/http';
import { check } from 'k6';

export let options = {
  stages: [
    { duration: '5m', target: 100 },
    { duration: '10m', target: 100 },
    { duration: '5m', target: 0 },
  ],
};

export default function() {
  let response = http.get('https://api.shop-manager.example.com/api/shops');
  check(response, { 'status was 200': (r) => r.status == 200 });
}
EOF

# Run load test
k6 run load-test.js
```

## 🚨 Troubleshooting Guide

### Common Issues

```bash
# Pod crash loops
kubectl describe pod <pod-name> -n shop-manager-prod
kubectl logs <pod-name> -n shop-manager-prod --previous

# Database connection issues
kubectl exec -it deployment/shop-manager -n shop-manager-prod -- \
  nc -zv shop-manager-postgresql 5432

# Memory issues
kubectl top pods -n shop-manager-prod
kubectl describe pod <pod-name> -n shop-manager-prod | grep -A 5 "Limits:"

# Persistent volume issues
kubectl get pv
kubectl describe pvc -n shop-manager-prod

# Ingress issues
kubectl describe ingress shop-manager-ingress -n shop-manager-prod
kubectl logs -n ingress-nginx deployment/ingress-nginx-controller
```

## 📋 Production Checklist

### Pre-Production
- [ ] All container images scanned for vulnerabilities
- [ ] Secrets properly encrypted and stored
- [ ] Resource limits and requests defined
- [ ] Network policies configured
- [ ] RBAC policies implemented
- [ ] Backup strategy tested
- [ ] Monitoring and alerting configured
- [ ] Load testing completed
- [ ] Disaster recovery plan documented

### Go-Live
- [ ] DNS records configured
- [ ] SSL certificates valid
- [ ] Health checks passing
- [ ] Monitoring dashboards active
- [ ] Alerts configured and tested
- [ ] Backup jobs scheduled
- [ ] Documentation updated
- [ ] Team trained on operations

### Post-Production
- [ ] Performance baselines established
- [ ] Incident response procedures documented
- [ ] Regular security audits scheduled
- [ ] Capacity planning reviewed
- [ ] Cost optimization implemented

---

## 📞 Support & Contacts

### Escalation Matrix
- **L1 Support**: Monitor alerts, basic troubleshooting
- **L2 Support**: Application issues, configuration changes
- **L3 Support**: Infrastructure, database, security issues

### Key Contacts
- **Platform Team**: platform-team@example.com
- **Security Team**: security@example.com
- **Database Team**: dba-team@example.com

### Useful Links
- [Kubernetes Documentation](https://kubernetes.io/docs/)
- [Helm Documentation](https://helm.sh/docs/)
- [Shop Manager Wiki](https://wiki.example.com/shop-manager)
- [Incident Response Playbook](https://wiki.example.com/incident-response)

---

This production deployment guide ensures a secure, scalable, and maintainable deployment of Shop Manager on Kubernetes Engine. Follow the steps carefully and customize based on your specific requirements and cloud provider.