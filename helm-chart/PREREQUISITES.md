# Shop Manager - Kubernetes Prerequisites

This document outlines all requirements for deploying Shop Manager to Kubernetes.

---

## System Requirements

### Minimum Requirements (Development/Testing)

| Resource | Minimum | Notes |
|----------|---------|-------|
| **CPU** | 4 cores | For all services |
| **RAM** | 8 GB | For all pods |
| **Storage** | 20 GB | For databases and logs |
| **Kubernetes** | 1.24+ | Any certified distribution |
| **Nodes** | 1 | Single-node cluster acceptable |

### Recommended (Production)

| Resource | Recommended | Notes |
|----------|-------------|-------|
| **CPU** | 8+ cores | For high availability |
| **RAM** | 16+ GB | Better performance |
| **Storage** | 100+ GB SSD | For database growth |
| **Kubernetes** | 1.28+ | Latest stable version |
| **Nodes** | 3+ | For high availability |

---

## Required Software

### 1. kubectl (Kubernetes CLI)

**Version:** 1.24 or later

**Installation:**

**macOS:**
```bash
brew install kubectl
```

**Linux:**
```bash
curl -LO "https://dl.k8s.io/release/$(curl -L -s https://dl.k8s.io/release/stable.txt)/bin/linux/amd64/kubectl"
sudo install -o root -g root -m 0755 kubectl /usr/local/bin/kubectl
```

**Windows (PowerShell as Administrator):**
```powershell
choco install kubernetes-cli
```

Or download from: https://kubernetes.io/docs/tasks/tools/

**Verify installation:**
```bash
kubectl version --client
```

---

### 2. Helm (Package Manager)

**Version:** 3.10 or later

**Installation:**

**macOS:**
```bash
brew install helm
```

**Linux:**
```bash
curl https://raw.githubusercontent.com/helm/helm/main/scripts/get-helm-3 | bash
```

**Windows (PowerShell as Administrator):**
```powershell
choco install kubernetes-helm
```

Or download from: https://helm.sh/docs/intro/install/

**Verify installation:**
```bash
helm version
```

---

### 3. Kubernetes Cluster

You need access to a Kubernetes cluster. Options include:

#### Cloud Providers

**AWS EKS:**
- Minimum: 2× t3.medium instances (2 vCPU, 4 GB each)
- Recommended: 3× t3.large instances (2 vCPU, 8 GB each)
- Setup: https://docs.aws.amazon.com/eks/latest/userguide/getting-started.html

**Google GKE:**
- Minimum: 2× e2-standard-2 instances (2 vCPU, 8 GB each)
- Recommended: 3× e2-standard-4 instances (4 vCPU, 16 GB each)
- Setup: https://cloud.google.com/kubernetes-engine/docs/quickstart

**Azure AKS:**
- Minimum: 2× Standard_D2s_v3 instances (2 vCPU, 8 GB each)
- Recommended: 3× Standard_D4s_v3 instances (4 vCPU, 16 GB each)
- Setup: https://learn.microsoft.com/en-us/azure/aks/learn/quick-kubernetes-deploy-cli

**DigitalOcean DOKS:**
- Minimum: 2× 4GB/2vCPU droplets
- Recommended: 3× 8GB/4vCPU droplets
- Setup: https://docs.digitalocean.com/products/kubernetes/quickstart/

#### Local Development

**Minikube:**
```bash
# macOS/Linux
minikube start --cpus=4 --memory=8192

# Windows
minikube start --cpus=4 --memory=8192 --driver=hyperv
```

**k3d (Lightweight Kubernetes):**
```bash
k3d cluster create shop-manager --agents 2
```

**Docker Desktop:**
- Enable Kubernetes in Docker Desktop settings
- Allocate 4 CPU cores and 8 GB RAM
- Platform: macOS, Windows

---

## Network Requirements

### Internet Access

Shop Manager requires internet access to:

| Service | URL | Purpose |
|---------|-----|---------|
| **Docker Hub** | `registry-1.docker.io` | Pull container images |
| **GitHub** | `github.com` | Download Helm charts |
| **Let's Encrypt** | `acme-v02.api.letsencrypt.org` | TLS certificates (if using) |

**Test connectivity:**
```bash
curl -I https://registry-1.docker.io/v2/
```

### Firewall Rules

Ensure the following ports are accessible:

| Port | Protocol | Purpose |
|------|----------|---------|
| 443 | TCP | HTTPS (Ingress) |
| 80 | TCP | HTTP (Redirect to HTTPS) |
| 6443 | TCP | Kubernetes API (if remote) |

---

## Storage Requirements

### Persistent Volumes

Shop Manager requires persistent storage for:

| Component | Storage | Purpose |
|-----------|---------|---------|
| **PostgreSQL** | 10 GB | Application database |
| **Keycloak DB** | 5 GB | Authentication database |
| **MinIO** (optional) | 50 GB | Object storage |
| **Kafka** (optional) | 10 GB | Event logs |

**Total minimum:** 20 GB (without optional components)

**Check available storage classes:**
```bash
kubectl get storageclass
```

**Common storage classes:**
- **Cloud:** `gp2` (AWS), `standard` (GKE), `default` (AKS)
- **Local:** `hostpath`, `local-path`

---

## Kubernetes Cluster Prerequisites

### Required Add-ons

Shop Manager installer will automatically install these if missing:

#### 1. cert-manager (v1.13.2+)

Manages TLS certificates.

**Manual installation:**
```bash
kubectl apply -f https://github.com/cert-manager/cert-manager/releases/download/v1.13.2/cert-manager.yaml
```

**Verify:**
```bash
kubectl get pods -n cert-manager
```

#### 2. NGINX Ingress Controller

Routes external traffic to services.

**Manual installation:**
```bash
helm repo add ingress-nginx https://kubernetes.github.io/ingress-nginx
helm repo update
helm install ingress-nginx ingress-nginx/ingress-nginx \
  --namespace ingress-nginx \
  --create-namespace \
  --set controller.service.type=LoadBalancer
```

**Verify:**
```bash
kubectl get pods -n ingress-nginx
```

---

## DNS Configuration

### Option 1: Cloud LoadBalancer (Recommended for Production)

After installation, get LoadBalancer IP:

```bash
kubectl get svc -n ingress-nginx ingress-nginx-controller
```

Create DNS A records:
```
retail.your-domain.com      → LoadBalancer IP
api.retail.your-domain.com  → LoadBalancer IP
auth.retail.your-domain.com → LoadBalancer IP
```

**DNS propagation:** 5 minutes to 24 hours

### Option 2: Local Development (/etc/hosts)

For local testing, add to `/etc/hosts`:

```bash
# Get Minikube/k3d IP
minikube ip  # or kubectl get nodes -o wide

# Edit /etc/hosts (requires sudo)
127.0.0.1  retail.shopmanager.local
127.0.0.1  api.retail.shopmanager.local
127.0.0.1  auth.retail.shopmanager.local
```

**Windows:** Edit `C:\Windows\System32\drivers\etc\hosts`

---

## Permissions and Security

### kubectl Access

Ensure you have cluster-admin permissions:

```bash
# Test permissions
kubectl auth can-i create namespace
kubectl auth can-i create deployment -n gomco
```

Required permissions:
- Create namespaces
- Create/update deployments, services, ingress
- Create persistent volume claims
- Create secrets and configmaps

### Service Account (Optional)

For production, use a dedicated service account:

```bash
kubectl create serviceaccount shop-manager-deployer -n gomco
kubectl create clusterrolebinding shop-manager-deployer \
  --clusterrole=cluster-admin \
  --serviceaccount=gomco:shop-manager-deployer
```

---

## Pre-Installation Checklist

Run this checklist before installation:

### ✅ Software Installed

- [ ] kubectl 1.24+ installed
- [ ] Helm 3.10+ installed
- [ ] Access to Kubernetes cluster

### ✅ Cluster Ready

- [ ] Cluster has 4+ CPU cores available
- [ ] Cluster has 8+ GB RAM available
- [ ] Cluster has 20+ GB storage available
- [ ] Storage class is available

### ✅ Network Ready

- [ ] Can reach `registry-1.docker.io`
- [ ] DNS configured (or /etc/hosts updated)
- [ ] Firewall allows ports 80, 443

### ✅ Configuration Ready

- [ ] Edited `values-template.yaml`
- [ ] Changed all passwords
- [ ] Set correct domain name
- [ ] Generated JWT secret

### ✅ Prerequisites Installed

- [ ] cert-manager installed (or will be installed automatically)
- [ ] NGINX Ingress Controller installed (or will be installed automatically)

---

## Automated Prerequisites Check

We provide scripts to check all prerequisites automatically:

**Linux/macOS:**
```bash
./check-prerequisites.sh
```

**Windows:**
```powershell
.\check-prerequisites.ps1
```

The script checks:
- ✅ kubectl and Helm versions
- ✅ Cluster connectivity
- ✅ Cluster resources (CPU, RAM, storage)
- ✅ Internet connectivity to Docker Hub
- ✅ Existing installations

---

## Common Issues

### Issue: "kubectl: command not found"

**Solution:** Install kubectl as described above

### Issue: "Cannot connect to Kubernetes cluster"

**Solutions:**
1. Check cluster is running: `kubectl cluster-info`
2. Verify kubeconfig: `kubectl config view`
3. Set correct context: `kubectl config use-context <context-name>`

### Issue: "Insufficient CPU or memory"

**Solutions:**
1. Scale up cluster nodes (cloud providers)
2. Allocate more resources (Docker Desktop, Minikube)
3. Reduce resource requests in `values-template.yaml`

### Issue: "No storage class available"

**Solutions:**
1. Cloud: Storage class created automatically
2. Local: Install local-path-provisioner:
   ```bash
   kubectl apply -f https://raw.githubusercontent.com/rancher/local-path-provisioner/master/deploy/local-path-storage.yaml
   ```

### Issue: "Cannot pull images from Docker Hub"

**Solutions:**
1. Check internet connectivity
2. Check Docker Hub status: https://status.docker.com/
3. Configure image pull secrets if using private registry

---

## Platform-Specific Notes

### macOS

- **Docker Desktop:** Enable Kubernetes in preferences
- **Minikube:** Use `hyperkit` or `docker` driver
- **Resource limits:** Adjust in Docker Desktop preferences

### Linux

- **Recommended:** k3s, microk8s, or minikube
- **Firewall:** May need to disable UFW or configure rules
- **SELinux:** May need to set to permissive mode

### Windows

- **Docker Desktop:** Enable Kubernetes in settings
- **Hyper-V:** Required for Docker Desktop
- **WSL 2:** Recommended for better performance
- **PowerShell:** Use PowerShell 5.1+ or PowerShell Core 7+

---

## Production Readiness

For production deployments, also consider:

### Monitoring

- Install Prometheus and Grafana
- Configure alerts for resource usage
- Set up log aggregation (ELK, Loki)

### Backups

- Configure automated database backups
- Test backup restoration procedures
- Store backups in external location

### High Availability

- Use 3+ nodes for redundancy
- Increase replica counts for critical services
- Configure pod disruption budgets
- Use anti-affinity rules

### Security

- Enable pod security policies
- Configure network policies
- Use secrets management (Vault, Sealed Secrets)
- Enable audit logging
- Regular security updates

---

## Support

If you encounter issues:

1. Run prerequisites check: `./check-prerequisites.sh`
2. Review [QUICKSTART.md](QUICKSTART.md)
3. Check logs: `kubectl logs -n gomco <pod-name>`
4. GitHub Issues: https://github.com/princely/shop-manager/issues

---

## Additional Resources

- **Kubernetes Documentation:** https://kubernetes.io/docs/
- **Helm Documentation:** https://helm.sh/docs/
- **cert-manager Documentation:** https://cert-manager.io/docs/
- **NGINX Ingress Documentation:** https://kubernetes.github.io/ingress-nginx/

---

Ready to install? See [QUICKSTART.md](QUICKSTART.md) for installation instructions.
