# ⚡ Shop Manager Quick Deployment Guide

## 🚀 One-Command Deployment

For on-site installations with automated SSL certificates:

```bash
# 1. Install cert-manager (if not already installed)
helm install cert-manager jetstack/cert-manager \
  --repo https://charts.jetstack.io \
  --namespace cert-manager \
  --create-namespace \
  --version v1.13.2 \
  --set installCRDs=true \
  --set securityContext.runAsNonRoot=false

# 2. Deploy Shop Manager with automated certificates
helm install shop-manager ./helm-chart/shop-manager \
  --namespace shop-manager \
  --create-namespace \
  --set tls.localCertInstallation.enabled=true \
  --set global.domain="shop-manager.local" \
  --set postgresql.auth.postgresPassword="SecurePass123!" \
  --set keycloak.auth.adminPassword="KeycloakAdmin456!"

# 3. Wait for certificate installation job to complete
kubectl wait --for=condition=complete job/shop-manager-cert-installer -n shop-manager --timeout=300s

# 4. Extract and run certificate installation script
kubectl cp shop-manager/$(kubectl get pods -n shop-manager -l job-name=shop-manager-cert-installer -o jsonpath='{.items[0].metadata.name}'):/shared/install-macos.sh ./install-macos.sh
chmod +x install-macos.sh && sudo ./install-macos.sh
```

## 🔗 Access Points

After deployment and certificate installation:

- **Frontend**: https://shop-manager.local
- **API**: https://api.shop-manager.local/actuator/health
- **Keycloak**: https://auth.shop-manager.local/admin

## 🔐 Default Credentials

**Keycloak Admin:**
- Username: `admin`
- Password: `KeycloakAdmin456!` (as set above)

**Test Users:**
- `admin@shopmanager.com` / `admin123`
- `manager@shopmanager.com` / `manager123`

## 📋 Requirements

- Kubernetes 1.25+
- Helm 3.8+
- 4GB RAM, 2 CPU cores minimum
- Local DNS resolution (add to /etc/hosts)

## 🔧 Production Deployment

For production environments:

```bash
# Copy and customize production values
cp helm-chart/production-values.yaml my-production-values.yaml

# Edit the required fields marked with ⚠️ REQUIRED
# Then deploy:
helm install shop-manager ./helm-chart/shop-manager \
  --namespace shop-manager \
  --create-namespace \
  --values my-production-values.yaml
```

## 📚 Documentation

- **Complete Guide**: `/PRODUCTION-DEPLOYMENT.md`
- **Certificate Automation**: `/CERTIFICATE_AUTOMATION.md`
- **Project Overview**: `/CLAUDE.md`