# 🎨 Shop Manager Platform Customization Guide

## 🎯 Overview

Shop Manager provides comprehensive customization capabilities that allow you to fully rebrand the platform for your organization. You can customize:

- **Release names** and DNS hostnames
- **Platform branding** (titles, logos, colors)
- **Keycloak login themes**
- **UI appearance** across all components
- **Email templates** and notifications
- **PDF reports** and documents

## 🚀 Quick Examples

### Example 1: Deploy as "RetailPro" with Custom Branding

```bash
# Create custom values file
cat > retailpro-values.yaml << EOF
global:
  appName: "retailpro"
  domain: "company.com"

branding:
  platformName: "RetailPro"
  companyName: "Acme Corporation"
  platformDescription: "Advanced Retail Management Suite"

  colors:
    primary: "#2E7D32"     # Forest Green
    secondary: "#FF6F00"   # Orange
    background: "#F8F9FA"  # Light Gray

  logo:
    primary: "data:image/svg+xml;base64,PHN2Zy4uLi4="  # Your logo

  ui:
    showCompanyName: true
    showPoweredBy: false
    customFooterText: "© 2024 Acme Corporation. All rights reserved."
EOF

# Deploy with custom branding
helm install retailpro ./helm-chart/shop-manager \
  --namespace retailpro \
  --create-namespace \
  --values retailpro-values.yaml
```

**Result URLs:**
- Frontend: `https://retailpro.company.com`
- API: `https://api.retailpro.company.com`
- Keycloak: `https://auth.retailpro.company.com`

### Example 2: Multi-Tenant Deployment

```bash
# Deploy for different clients with same cluster
helm install client-alpha ./helm-chart/shop-manager \
  --namespace client-alpha \
  --set global.appName="alpha-retail" \
  --set global.domain="clients.company.com" \
  --set branding.platformName="Alpha Retail System" \
  --set branding.companyName="Alpha Solutions Inc"

helm install client-beta ./helm-chart/shop-manager \
  --namespace client-beta \
  --set global.appName="beta-stores" \
  --set global.domain="clients.company.com" \
  --set branding.platformName="Beta Store Manager" \
  --set branding.companyName="Beta Enterprises"
```

**Result URLs:**
- **Alpha**: `https://alpha-retail.clients.company.com`
- **Beta**: `https://beta-stores.clients.company.com`

## 🎨 Complete Branding Configuration

### Full Branding Values File

```yaml
# complete-branding.yaml
global:
  appName: "my-retail-system"
  domain: "retail.mycompany.com"
  environment: "production"

branding:
  # Platform Identity
  platformName: "MyRetail Pro"
  companyName: "My Company Ltd"
  platformDescription: "Enterprise Retail Management Platform"

  # Visual Identity
  logo:
    # Base64 encoded SVG logo (recommended for scalability)
    primary: "data:image/svg+xml;base64,PHN2ZyB3aWR0aD0iMTAwIiBoZWlnaHQ9IjEwMCI+..."
    # Favicon (ICO format recommended)
    favicon: "data:image/x-icon;base64,AAABAAEAEBAAAAEAIABoBAAAFgAAACgAAAAQ..."
    # Optional different logo for login page
    loginLogo: "data:image/svg+xml;base64,PHN2ZyB3aWR0aD0iMjAwIiBoZWlnaHQ9IjEwMCI+..."

  # Color Scheme (Material Design compatible)
  colors:
    primary: "#1565C0"      # Blue 700
    secondary: "#E91E63"    # Pink 500
    success: "#2E7D32"      # Green 700
    warning: "#F57C00"      # Orange 600
    error: "#C62828"        # Red 700
    background: "#FAFAFA"   # Gray 50
    surface: "#FFFFFF"      # White

  # Typography
  fonts:
    family: "Inter, system-ui, sans-serif"
    size:
      small: "13px"
      medium: "15px"
      large: "17px"
      title: "24px"

  # Login Theme Customization
  loginTheme:
    backgroundColor: "#E3F2FD"     # Light Blue 50
    cardBackgroundColor: "#FFFFFF"
    cardShadow: "0 8px 32px rgba(0, 0, 0, 0.12)"
    borderRadius: "12px"

  # UI Customization
  ui:
    showCompanyName: true
    showPoweredBy: false
    customFooterText: "Powered by MyRetail Pro - © 2024 My Company Ltd"

  # Email Templates
  email:
    headerColor: "#1565C0"
    logoUrl: "https://cdn.mycompany.com/logo-email.png"
    companyAddress: "123 Business St, City, State 12345"

  # PDF Reports
  reports:
    headerLogo: "https://cdn.mycompany.com/logo-reports.png"
    watermark: "CONFIDENTIAL - My Company Ltd"
```

## 🔧 Template Usage in Helm Charts

The Helm templates automatically use these helper functions:

### DNS Hostnames
```yaml
# In ingress.yaml
spec:
  rules:
  - host: {{ include "shop-manager.frontend.hostname" . }}
  - host: {{ include "shop-manager.backend.hostname" . }}
  - host: {{ include "shop-manager.keycloak.hostname" . }}
```

### Service Names
```yaml
# In deployment.yaml
metadata:
  name: {{ include "shop-manager.appName" . }}-backend

# Environment variables
env:
- name: KEYCLOAK_URL
  value: {{ include "shop-manager.frontend.keycloakUrl" . }}
- name: API_BASE_URL
  value: {{ include "shop-manager.frontend.apiBaseUrl" . }}
```

### Certificate Names
```yaml
# In certificate.yaml
metadata:
  name: {{ include "shop-manager.frontend.certName" . }}
spec:
  dnsNames:
  - {{ include "shop-manager.frontend.hostname" . }}
```

## 🎨 Keycloak Theme Customization

### Dynamic Theme Generation

The Keycloak theme automatically uses your branding configuration:

```yaml
# In keycloak-theme-configmap.yaml
data:
  shop-manager.css: |
    :root {
      --primary-color: {{ .Values.branding.colors.primary }};
      --secondary-color: {{ .Values.branding.colors.secondary }};
      --background-color: {{ .Values.branding.loginTheme.backgroundColor }};
    }

    .login-pf-page {
      background-color: var(--background-color);
    }

    .card-pf {
      background-color: {{ .Values.branding.loginTheme.cardBackgroundColor }};
      box-shadow: {{ .Values.branding.loginTheme.cardShadow }};
      border-radius: {{ .Values.branding.loginTheme.borderRadius }};
    }

  login.ftl: |
    <#-- Platform name from branding configuration -->
    <h1>${"{{ include \"shop-manager.platformName\" . }}"}</h1>
    <p>${"{{ include \"shop-manager.platformDescription\" . }}"}</p>
```

## 📱 Frontend Branding Integration

### Environment Configuration

```yaml
# Frontend ConfigMap automatically includes branding
data:
  REACT_APP_PLATFORM_NAME: {{ include "shop-manager.platformName" . }}
  REACT_APP_COMPANY_NAME: {{ include "shop-manager.companyName" . }}
  REACT_APP_PRIMARY_COLOR: {{ .Values.branding.colors.primary }}
  REACT_APP_LOGO_URL: {{ .Values.branding.logo.primary }}
  REACT_APP_SHOW_COMPANY_NAME: {{ .Values.branding.ui.showCompanyName }}
  REACT_APP_SHOW_POWERED_BY: {{ .Values.branding.ui.showPoweredBy }}
```

### Frontend React Integration

```javascript
// In React components
const App = () => {
  const platformName = process.env.REACT_APP_PLATFORM_NAME;
  const primaryColor = process.env.REACT_APP_PRIMARY_COLOR;

  return (
    <ThemeProvider theme={{ primaryColor }}>
      <Header title={platformName} />
      <Routes>...</Routes>
    </ThemeProvider>
  );
};
```

## 🔍 Deployment Examples

### Example 1: Grocery Chain Deployment

```bash
cat > grocery-chain-values.yaml << EOF
global:
  appName: "freshmart"
  domain: "stores.freshmart.com"

branding:
  platformName: "FreshMart POS"
  companyName: "FreshMart Grocery Chains"
  platformDescription: "Point of Sale & Inventory Management"

  colors:
    primary: "#4CAF50"     # Green for fresh/organic theme
    secondary: "#FF8F00"   # Orange for energy

  logo:
    primary: "data:image/svg+xml;base64,..."  # Grocery cart logo

  ui:
    customFooterText: "Serving fresh groceries since 1995"
EOF

helm install freshmart ./helm-chart/shop-manager \
  --namespace freshmart \
  --values grocery-chain-values.yaml
```

### Example 2: Electronics Store Deployment

```bash
cat > electronics-values.yaml << EOF
global:
  appName: "techzone"
  domain: "pos.techzone.com"

branding:
  platformName: "TechZone Pro"
  companyName: "TechZone Electronics"
  platformDescription: "Advanced Electronics Retail System"

  colors:
    primary: "#1976D2"     # Tech blue
    secondary: "#424242"   # Dark gray

  loginTheme:
    backgroundColor: "#0D47A1"  # Dark blue background
    cardBackgroundColor: "#FFFFFF"

  ui:
    showPoweredBy: false
    customFooterText: "Innovation in retail technology"
EOF

helm install techzone ./helm-chart/shop-manager \
  --namespace techzone \
  --values electronics-values.yaml
```

## 🏗️ Advanced Customization

### Custom Domain Structure

```yaml
# For complex domain structures
global:
  appName: "retail-north"
  domain: "us.company.com"

# Results in:
# Frontend: https://retail-north.us.company.com
# API: https://api.retail-north.us.company.com
# Auth: https://auth.retail-north.us.company.com
```

### Multi-Environment Setup

```bash
# Development
helm install retail-dev ./helm-chart/shop-manager \
  --namespace retail-dev \
  --set global.appName="retail" \
  --set global.domain="dev.company.com" \
  --set branding.platformName="Retail System (DEV)"

# Staging
helm install retail-staging ./helm-chart/shop-manager \
  --namespace retail-staging \
  --set global.appName="retail" \
  --set global.domain="staging.company.com" \
  --set branding.platformName="Retail System (STAGING)"

# Production
helm install retail-prod ./helm-chart/shop-manager \
  --namespace retail-prod \
  --set global.appName="retail" \
  --set global.domain="company.com" \
  --set branding.platformName="Retail System"
```

## 🛠️ Helper Commands

### Validate Configuration

```bash
# Check generated hostnames
helm template ./helm-chart/shop-manager \
  --values your-values.yaml \
  --show-only templates/ingress.yaml

# Test branding settings
helm template ./helm-chart/shop-manager \
  --values your-values.yaml \
  --show-only templates/configmap.yaml
```

### Update Existing Deployment

```bash
# Update branding without downtime
helm upgrade retail ./helm-chart/shop-manager \
  --namespace retail \
  --values updated-branding.yaml \
  --reuse-values
```

## ✅ Benefits

### Complete Platform Ownership
- ✅ **Custom branding** across all interfaces
- ✅ **Personalized login experience** with your logos and colors
- ✅ **White-label deployment** with no reference to original platform
- ✅ **Professional appearance** matching your corporate identity

### Multi-Tenant Capable
- ✅ **Deploy multiple instances** for different clients
- ✅ **Isolated DNS namespaces** preventing conflicts
- ✅ **Separate certificates** for each deployment
- ✅ **Independent customization** per client

### Maintenance Friendly
- ✅ **Consistent naming** across all Kubernetes resources
- ✅ **Easy identification** of components in cluster
- ✅ **Predictable URLs** for monitoring and operations
- ✅ **Automated certificate management** per deployment

---

**🎉 Result**: A fully customized retail management platform that looks and feels like your own product, with professional SSL certificates and complete branding control.