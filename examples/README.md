# Shop Manager - Example Values Files

This directory contains example Helm values files for various deployment scenarios. Choose the one that best matches your use case and customize as needed.

---

## 📁 Available Examples

### 🚀 Quick Start

| File | Use Case | Complexity | Best For |
|------|----------|------------|----------|
| **[minimal-values.yaml](./minimal-values.yaml)** | Bare minimum configuration | ⭐ Easy | Testing, demos, POC |
| **[customer-values.yaml](./customer-values.yaml)** | Full-featured template | ⭐⭐ Medium | Customer deployments, small-medium business |

### 🏢 Production Deployments

| File | Use Case | Complexity | Best For |
|------|----------|------------|----------|
| **[production-values.yaml](./production-values.yaml)** | Production-grade setup | ⭐⭐⭐ Advanced | Enterprise production |

### ☁️ Cloud-Specific

| File | Cloud Provider | Features |
|------|---------------|----------|
| **[aws-eks-values.yaml](./aws-eks-values.yaml)** | Amazon Web Services | ALB, EBS, S3, ACM, RDS, IAM Roles |

---

## 🎯 Which Example Should I Use?

### For Quick Testing
→ **[minimal-values.yaml](./minimal-values.yaml)**

```bash
curl -o my-values.yaml https://raw.githubusercontent.com/yourorg/shop-manager/main/examples/minimal-values.yaml

# Edit domain
vi my-values.yaml

helm install retail oci://registry-1.docker.io/princely/shop-manager \
  --version 0.0.46 -n gomco --create-namespace -f my-values.yaml
```

**Perfect for:**
- Local testing on Docker Desktop
- Quick proof-of-concept
- Learning the system

---

### For Customer Installations
→ **[customer-values.yaml](./customer-values.yaml)**

```bash
curl -o my-values.yaml https://raw.githubusercontent.com/yourorg/shop-manager/main/examples/customer-values.yaml

# Customize your settings
vi my-values.yaml

helm install retail oci://registry-1.docker.io/princely/shop-manager \
  --version 0.0.46 -n gomco --create-namespace -f my-values.yaml
```

**Perfect for:**
- Small-medium business deployments
- Customer laptop installations
- Development environments
- Team testing

**Features:**
- Comprehensive comments explaining every option
- Branding customization
- SSL/TLS configuration
- All common settings

---

### For Production Deployments
→ **[production-values.yaml](./production-values.yaml)**

```bash
curl -o prod-values.yaml https://raw.githubusercontent.com/yourorg/shop-manager/main/examples/production-values.yaml

# IMPORTANT: Review and change ALL passwords and secrets!
vi prod-values.yaml

helm install retail oci://registry-1.docker.io/princely/shop-manager \
  --version 0.0.46 \
  -n production \
  --create-namespace \
  -f prod-values.yaml \
  --atomic \
  --timeout 20m
```

**Perfect for:**
- Enterprise production deployments
- High-traffic applications
- Mission-critical systems

**Features:**
- High availability (multiple replicas)
- Autoscaling
- Let's Encrypt SSL
- Production security hardening
- Monitoring and logging
- Backup configuration
- Resource limits

---

### For AWS EKS
→ **[aws-eks-values.yaml](./aws-eks-values.yaml)**

```bash
curl -o aws-values.yaml https://raw.githubusercontent.com/yourorg/shop-manager/main/examples/aws-eks-values.yaml

# Update AWS-specific settings (ARNs, endpoints, etc.)
vi aws-values.yaml

helm install retail oci://registry-1.docker.io/princely/shop-manager \
  --version 0.0.46 \
  -n production \
  --create-namespace \
  -f aws-values.yaml
```

**Perfect for:**
- AWS EKS deployments
- Integration with AWS services

**AWS-Specific Features:**
- AWS Load Balancer Controller (ALB)
- EBS storage classes (gp3)
- S3 for backups
- AWS Certificate Manager (ACM)
- RDS PostgreSQL option
- IAM Roles for Service Accounts (IRSA)
- CloudWatch logging
- MSK (Managed Kafka) support

---

## 📝 How to Customize

### 1. Download Template

```bash
# Choose your starting point
curl -o my-values.yaml https://raw.githubusercontent.com/yourorg/shop-manager/main/examples/customer-values.yaml
```

### 2. Minimum Required Changes

```yaml
global:
  domain: "mycompany.com"  # Your domain

branding:
  platformName: "Acme Retail"  # Your platform name
  companyName: "Acme Corp"     # Your company
```

### 3. Security (Production Only)

**⚠️ CRITICAL for production:**

```yaml
# Change ALL passwords!
postgresql:
  auth:
    postgresPassword: "CHANGE-ME-super-secret"
    password: "CHANGE-ME-database-password"

keycloak:
  auth:
    adminPassword: "CHANGE-ME-keycloak-admin"

# Use Let's Encrypt
tls:
  issuer: "letsencrypt-prod"
  email: "admin@mycompany.com"

# Disable test users
application:
  testUsers:
    enabled: false
```

### 4. Optional Customizations

```yaml
# Branding colors
branding:
  colors:
    primary: "#007bff"    # Your primary color
    secondary: "#6c757d"   # Your secondary color

# Resource limits (adjust based on your needs)
backend:
  resources:
    requests:
      memory: "1Gi"
      cpu: "500m"

# Autoscaling
backend:
  autoscaling:
    enabled: true
    minReplicas: 3
    maxReplicas: 10
```

---

## 🔧 Installation Commands

### Install

```bash
helm install retail oci://registry-1.docker.io/princely/shop-manager \
  --version 0.0.46 \
  -n gomco \
  --create-namespace \
  -f my-values.yaml
```

### Upgrade

```bash
helm upgrade retail oci://registry-1.docker.io/princely/shop-manager \
  --version 0.0.46 \
  -n gomco \
  -f my-values.yaml
```

### View Current Values

```bash
helm get values retail -n gomco
```

### Dry Run (Test Before Install)

```bash
helm install retail oci://registry-1.docker.io/princely/shop-manager \
  --version 0.0.46 \
  -n gomco \
  -f my-values.yaml \
  --dry-run \
  --debug
```

---

## 📚 Additional Resources

- **[Customer Installation Guide](../CUSTOMER_INSTALL.md)** - Complete installation walkthrough
- **[Deployment Guide](../DEPLOYMENT_GUIDE.md)** - Advanced deployment options
- **[Helm Chart README](../helm-chart/shop-manager/README.md)** - Full chart documentation
- **[Testing Guide](../TESTING-GUIDE.md)** - Test users and credentials

---

## 🆘 Common Questions

### Q: Which file should I start with?
**A:** For most users, start with **`customer-values.yaml`** - it has comprehensive comments and covers all common scenarios.

### Q: Can I combine multiple examples?
**A:** Yes! You can merge settings from multiple files or use multiple `-f` flags:

```bash
helm install retail oci://registry-1.docker.io/princely/shop-manager \
  -f customer-values.yaml \
  -f my-overrides.yaml \
  -n gomco
```

Later files override earlier ones.

### Q: How do I see all available options?
**A:** View the chart's default values:

```bash
helm show values oci://registry-1.docker.io/princely/shop-manager --version 0.0.46
```

### Q: Do I need to fill out everything?
**A:** No! Only provide values you want to change from defaults. The minimal example shows you only need `global.domain` and `branding.*` to get started.

### Q: How do I use external database/storage?
**A:** See **`production-values.yaml`** for external database examples and cloud-specific files for storage configurations.

---

## 🔐 Security Reminders

Before deploying to production:

- [ ] Change ALL default passwords
- [ ] Use Let's Encrypt or valid SSL certificates
- [ ] Disable test users (`application.testUsers.enabled: false`)
- [ ] Use external secret management (Vault, AWS Secrets Manager)
- [ ] Enable network policies
- [ ] Configure backup storage
- [ ] Set up monitoring and alerts
- [ ] Test in staging environment first

---

## 📞 Support

- **Issues**: https://github.com/yourorg/shop-manager/issues
- **Discussions**: https://github.com/yourorg/shop-manager/discussions
- **Documentation**: https://github.com/yourorg/shop-manager

---

**Happy deploying! 🚀**