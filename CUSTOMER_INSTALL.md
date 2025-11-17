# Shop Manager - Customer Installation Guide

**Simple Kubernetes installation using Helm from Docker Hub**

No need to clone the repository - install Shop Manager directly from our published Helm chart!

---

## 📋 Prerequisites

### 1. Docker Desktop with Kubernetes

**macOS / Windows:**
1. Download and install [Docker Desktop](https://www.docker.com/products/docker-desktop)
2. Open Docker Desktop → Settings → Kubernetes
3. Check **"Enable Kubernetes"**
4. Click **"Apply & Restart"**
5. Wait for Kubernetes to start (green indicator)

**Linux:**
- Install Docker + Minikube or K3s

### 2. Helm 3

```bash
# macOS
brew install helm

# Linux
curl https://raw.githubusercontent.com/helm/helm/main/scripts/get-helm-3 | bash

# Windows (PowerShell as Administrator)
choco install kubernetes-helm
```

### 3. Verify Installation

```bash
# Check Kubernetes is running
kubectl cluster-info

# Check Helm is installed
helm version

# Should see:
# version.BuildInfo{Version:"v3.x.x", ...}
```

---

## 🚀 Quick Installation (5 Minutes)

### Option A: Automated Installation (Easiest) ⭐

The automated installer handles everything for you.

**Linux / macOS:**
```bash
# Download installer
curl -o customer-install.sh https://raw.githubusercontent.com/yourorg/shop-manager/main/examples/customer-install.sh

# Make executable
chmod +x customer-install.sh

# Run installer
./customer-install.sh
```

**Windows (PowerShell as Administrator):**
```powershell
# Download installer
Invoke-WebRequest -Uri "https://raw.githubusercontent.com/yourorg/shop-manager/main/examples/customer-install.ps1" `
    -OutFile "customer-install.ps1"

# Run installer
.\customer-install.ps1
```

**What the installer does:**
- ✅ Checks prerequisites
- ✅ Prompts for configuration (domain, company name, test users)
- ✅ Installs cert-manager (if needed)
- ✅ Installs NGINX ingress (if needed)
- ✅ Deploys Shop Manager
- ✅ Sets up SSL certificates
- ✅ Configures DNS entries

**Skip to Step 6** if you use the automated installer.

---

### Option B: Manual Installation

### Step 1: Download Configuration Template

```bash
# Create a directory for your installation
mkdir shop-manager-install
cd shop-manager-install

# Download customer values template
curl -o my-values.yaml https://raw.githubusercontent.com/yourorg/shop-manager/main/examples/customer-values.yaml
```

### Step 2: Customize Your Values

Edit `my-values.yaml`:

```bash
# Use your favorite editor
nano my-values.yaml
# or
vi my-values.yaml
# or
code my-values.yaml
```

**Minimum required changes:**

```yaml
global:
  domain: "mycompany.com"  # Change to your domain

branding:
  platformName: "Acme Retail Pro"  # Your platform name
  companyName: "Acme Corporation"   # Your company name
```

### Step 3: Install Prerequisites (One-Time)

```bash
# Install cert-manager (for SSL certificates)
kubectl apply -f https://github.com/cert-manager/cert-manager/releases/download/v1.13.2/cert-manager.yaml

# Wait for cert-manager to be ready (takes ~30 seconds)
kubectl wait --for=condition=available --timeout=300s deployment -n cert-manager --all

# Install NGINX Ingress Controller
helm repo add ingress-nginx https://kubernetes.github.io/ingress-nginx
helm repo update
helm install ingress-nginx ingress-nginx/ingress-nginx \
  --namespace ingress-nginx \
  --create-namespace \
  --wait \
  --timeout 5m
```

### Step 4: Install Shop Manager

```bash
# Install from Docker Hub
helm install retail oci://registry-1.docker.io/princely/shop-manager \
  --version 0.0.45 \
  -n gomco \
  --create-namespace \
  -f my-values.yaml \
  --wait \
  --timeout 10m
```

**What this does:**
- Creates `gomco` namespace
- Deploys Shop Manager (backend, frontend, database, Keycloak)
- Configures ingress and SSL certificates
- Takes 5-10 minutes depending on internet speed

### Step 5: Post-Installation Setup

#### A. Add DNS Entries

**macOS / Linux:**
```bash
# Add to /etc/hosts
echo "127.0.0.1 retail.mycompany.com" | sudo tee -a /etc/hosts
echo "127.0.0.1 api.retail.mycompany.com" | sudo tee -a /etc/hosts
echo "127.0.0.1 auth.retail.mycompany.com" | sudo tee -a /etc/hosts
```

**Windows (PowerShell as Administrator):**
```powershell
Add-Content -Path C:\Windows\System32\drivers\etc\hosts -Value "127.0.0.1 retail.mycompany.com"
Add-Content -Path C:\Windows\System32\drivers\etc\hosts -Value "127.0.0.1 api.retail.mycompany.com"
Add-Content -Path C:\Windows\System32\drivers\etc\hosts -Value "127.0.0.1 auth.retail.mycompany.com"
```

**Replace `mycompany.com` with your domain from `my-values.yaml`**

#### B. Install SSL Certificate (For Local Development)

Shop Manager uses self-signed certificates for local development. Trust the certificate to avoid browser warnings.

**Extract Certificate:**
```bash
kubectl get secret local-ca-key-pair -n cert-manager \
  -o jsonpath='{.data.tls\.crt}' | base64 -d > /tmp/shop-manager-ca.crt
```

**Install Certificate:**

**macOS:**
```bash
sudo security add-trusted-cert -d -r trustRoot \
  -k /Library/Keychains/System.keychain \
  /tmp/shop-manager-ca.crt
```

**Linux (Ubuntu/Debian):**
```bash
sudo cp /tmp/shop-manager-ca.crt /usr/local/share/ca-certificates/shop-manager-ca.crt
sudo update-ca-certificates
```

**Windows:**
```powershell
# Import certificate to Trusted Root Certification Authorities
Import-Certificate -FilePath C:\tmp\shop-manager-ca.crt -CertStoreLocation Cert:\LocalMachine\Root
```

### Step 6: Access Your Application

**Restart your browser**, then open:

- **Frontend**: https://retail.mycompany.com
- **API Docs**: https://api.retail.mycompany.com/swagger-ui/index.html
- **Keycloak Admin**: https://auth.retail.mycompany.com

**Default Login Credentials:**
- **Email**: `admin@shopmanager.com`
- **Password**: `DevAdmin@2024!Test`

**Other test accounts:**
- Owner: `owner@shopmanager.com` / `DevOwner@2024!Test`
- Manager: `manager@shopmanager.com` / `DevManager@2024!Test`

---

## ✅ Verify Installation

```bash
# Check all pods are running
kubectl get pods -n gomco

# Should see all pods in "Running" status:
# retail-backend-xxx        1/1  Running
# retail-frontend-xxx       1/1  Running
# retail-keycloak-xxx       1/1  Running
# retail-postgresql-xxx     1/1  Running

# Check ingress
kubectl get ingress -n gomco

# Check certificates
kubectl get certificate -n gomco

# Should see certificates in "Ready" state
```

---

## 🔄 Upgrade to New Version

```bash
# Upgrade to a new version
helm upgrade retail oci://registry-1.docker.io/princely/shop-manager \
  --version 0.0.46 \
  -n gomco \
  -f my-values.yaml \
  --wait
```

---

## 🗑️ Uninstall

```bash
# Uninstall Shop Manager
helm uninstall retail -n gomco

# Remove namespace
kubectl delete namespace gomco

# (Optional) Remove prerequisites
helm uninstall ingress-nginx -n ingress-nginx
kubectl delete -f https://github.com/cert-manager/cert-manager/releases/download/v1.13.2/cert-manager.yaml

# Remove DNS entries from /etc/hosts
sudo sed -i.bak '/retail.mycompany.com/d' /etc/hosts
```

---

## 🛠️ Troubleshooting

### Pods Not Starting

```bash
# Check pod status
kubectl get pods -n gomco

# View pod logs
kubectl logs -n gomco <pod-name>

# Describe pod for events
kubectl describe pod -n gomco <pod-name>
```

**Common issues:**
- **ImagePullBackOff**: Check `imagePullSecrets` in your values file
- **CrashLoopBackOff**: Check pod logs for errors
- **Pending**: Check if Kubernetes has enough resources

### Certificate Warnings in Browser

```bash
# Re-extract and install certificate
kubectl get secret local-ca-key-pair -n cert-manager \
  -o jsonpath='{.data.tls\.crt}' | base64 -d > /tmp/shop-manager-ca.crt

# macOS
sudo security delete-certificate -c "local-ca-issuer"
sudo security add-trusted-cert -d -r trustRoot \
  -k /Library/Keychains/System.keychain \
  /tmp/shop-manager-ca.crt

# Restart browser completely (Cmd+Q, not just close window)
```

### DNS Not Resolving

```bash
# Check /etc/hosts entries
cat /etc/hosts | grep retail

# Flush DNS cache
# macOS
sudo dscacheutil -flushcache && sudo killall -HUP mDNSResponder

# Windows
ipconfig /flushdns

# Linux
sudo systemd-resolve --flush-caches
```

### Services Not Accessible

```bash
# Check if ingress-nginx is running
kubectl get pods -n ingress-nginx

# Check ingress configuration
kubectl get ingress -n gomco -o yaml

# Check if LoadBalancer has an IP
kubectl get svc -n ingress-nginx ingress-nginx-controller
```

### Database Connection Issues

```bash
# Check PostgreSQL pod
kubectl get pods -n gomco | grep postgresql

# View PostgreSQL logs
kubectl logs -n gomco <postgresql-pod-name>

# Restart backend pod
kubectl rollout restart deployment retail-backend -n gomco
```

---

## 📊 Monitoring & Logs

### View Application Logs

```bash
# Backend logs
kubectl logs -n gomco -l app.kubernetes.io/name=backend --tail=100 -f

# Frontend logs
kubectl logs -n gomco -l app.kubernetes.io/name=frontend --tail=100 -f

# All logs
kubectl logs -n gomco --all-containers=true --tail=100 -f
```

### Check Resource Usage

```bash
# Pod resource usage
kubectl top pods -n gomco

# Node resource usage
kubectl top nodes
```

---

## 🔐 Security Best Practices

### Production Deployment

When deploying to production:

1. **Change Default Passwords**
   ```yaml
   postgresql:
     auth:
       postgresPassword: "your-secure-password"
       password: "your-secure-password"
   ```

2. **Use Let's Encrypt for SSL**
   ```yaml
   tls:
     enabled: true
     issuer: "letsencrypt-prod"
     email: "admin@mycompany.com"
   ```

3. **Disable Test Users**
   ```yaml
   application:
     testUsers:
       enabled: false
   ```

4. **Configure External Database**
   ```yaml
   postgresql:
     enabled: false
   externalDatabase:
     host: "postgres.mycompany.com"
     port: 5432
     database: "shopmanager"
     username: "shopmanager"
     password: "secure-password"
   ```

---

## 📞 Support

- **Documentation**: https://github.com/yourorg/shop-manager
- **Issues**: https://github.com/yourorg/shop-manager/issues
- **Discussions**: https://github.com/yourorg/shop-manager/discussions

---

## 🎯 Next Steps

After installation:

1. **Change admin password** in Keycloak admin console
2. **Configure your shop** settings
3. **Add users** and assign roles
4. **Import products** from your existing system
5. **Configure backups** (see Advanced Configuration)

---

## 📚 Additional Resources

- [Complete Deployment Guide](./DEPLOYMENT_GUIDE.md)
- [Developer Guide](./DEVELOPER_GUIDE.md)
- [Testing Guide](./TESTING-GUIDE.md)
- [Helm Chart Documentation](./helm-chart/shop-manager/README.md)

---

**Installation Time**: ~10 minutes
**Difficulty**: Beginner
**Support**: Community & Enterprise support available
