# Shop Manager Customization Guide

This guide explains how to customize every aspect of your Shop Manager installation to match your business needs and branding.

## 📋 Table of Contents

1. [Configuration Overview](#configuration-overview)
2. [Branding & Visual Identity](#branding--visual-identity)
3. [Authentication & Security](#authentication--security)
4. [Test Users & Initial Data](#test-users--initial-data)
5. [Business Rules](#business-rules)
6. [Feature Flags](#feature-flags)
7. [Domain & SSL Certificates](#domain--ssl-certificates)
8. [Advanced Configuration](#advanced-configuration)
9. [Multi-Shop Setup](#multi-shop-setup)
10. [Backup & Recovery](#backup--recovery)

## 🎯 Configuration Overview

All customization is done through the `config.yaml` file. After making changes, regenerate configuration files:

```bash
# Edit configuration
nano config.yaml

# Validate configuration
python3 scripts/generate-config.py --validate-only

# Generate new configuration files
python3 scripts/generate-config.py

# Apply changes
docker compose down
docker compose up -d
```

## 🎨 Branding & Visual Identity

### Company Information

```yaml
branding:
  platformName: "RetailMax Pro"        # Shown in app header, login page, emails
  companyName: "TechMart Solutions"    # Used in footer, documents, receipts
  platformDescription: "Advanced Retail Management System"
```

**Where it appears:**
- `platformName`: Browser title, login screen, header, emails
- `companyName`: PDF receipts, invoices, footer, legal documents
- `platformDescription`: Meta description, login page subtitle

### Logo Customization

```yaml
branding:
  logos:
    # Main logo (shown in app header)
    primary: "./assets/logo.svg"       # SVG recommended (auto-scales)
    # Or: primary: "./assets/logo.png"  # PNG also supported (min 200x50px)

    # Browser favicon (shown in browser tabs)
    favicon: "./assets/favicon.ico"    # ICO format, 32x32 or 64x64

    # Login page logo (can be different from header logo)
    loginLogo: "./assets/login-logo.svg"  # Optional, defaults to primary
```

**Logo Specifications:**

| Logo Type | Format | Size | Notes |
|-----------|--------|------|-------|
| Primary | SVG, PNG | 200x50px min | Transparent background |
| Favicon | ICO | 32x32 or 64x64 | Square, simple design |
| Login Logo | SVG, PNG | 300x100px | Can be larger/more detailed |

**Example: Add Your Logo**

1. Prepare your logo files:
   ```bash
   mkdir -p standalone/assets
   cp /path/to/your/logo.png standalone/assets/
   cp /path/to/your/favicon.ico standalone/assets/
   ```

2. Update config.yaml:
   ```yaml
   branding:
     logos:
       primary: "./assets/logo.png"
       favicon: "./assets/favicon.ico"
   ```

3. Regenerate and restart:
   ```bash
   python3 scripts/generate-config.py
   docker compose restart frontend
   ```

### Color Scheme

```yaml
branding:
  colors:
    primary: "#2E7D32"      # Main brand color (buttons, links, header)
    secondary: "#FF6F00"     # Secondary/accent color
    success: "#4CAF50"       # Success states (green)
    warning: "#FFA726"       # Warning states (orange)
    error: "#EF5350"         # Error states (red)
    background: "#FAFAFA"    # Page background
    surface: "#FFFFFF"       # Cards, modals, panels
```

**Color Guidelines:**
- Use hex codes (#RRGGBB)
- Ensure sufficient contrast (WCAG AA compliance)
- Test in both light and dark modes
- Keep brand consistency

**Pre-made Color Schemes:**

```yaml
# Professional Blue (Default)
colors:
  primary: "#1976d2"
  secondary: "#dc004e"

# Modern Green (Eco/Organic)
colors:
  primary: "#2E7D32"
  secondary: "#FF6F00"

# Bold Red (Retail/Sale)
colors:
  primary: "#D32F2F"
  secondary: "#1976D2"

# Elegant Purple (Luxury)
colors:
  primary: "#7B1FA2"
  secondary: "#F57C00"

# Tech Dark Blue
colors:
  primary: "#0D47A1"
  secondary: "#FFC107"
```

### Typography

```yaml
branding:
  fonts:
    family: "Roboto, Arial, sans-serif"  # Font stack
    sizes:
      small: "12px"
      medium: "14px"
      large: "16px"
      title: "20px"
```

**Custom Web Fonts:**

```yaml
# Use Google Fonts
fonts:
  family: "Inter, Helvetica, Arial, sans-serif"
  googleFontsUrl: "https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700&display=swap"

# Or use system fonts (faster loading)
fonts:
  family: "-apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Oxygen, Ubuntu, Cantarell, sans-serif"
```

### UI Customization

```yaml
branding:
  ui:
    showCompanyName: true         # Show company name in footer
    showPoweredBy: false          # Hide "Powered by Shop Manager"
    customFooterText: "© 2024 ACME Corp. All rights reserved."

    # Additional UI options
    headerHeight: "64px"
    sidebarWidth: "240px"
    borderRadius: "8px"           # Global border radius
    boxShadow: "0 2px 4px rgba(0,0,0,0.1)"  # Default shadow
```

### Email Templates

```yaml
branding:
  email:
    headerColor: "#1976d2"        # Email header background
    logoUrl: "https://mysite.com/logo.png"  # Logo for emails (must be publicly accessible)
    companyAddress: |              # Shown in email footer
      ACME Corporation
      123 Main Street
      San Francisco, CA 94102
      United States
    supportEmail: "support@acme.com"
    phoneNumber: "+1 (555) 123-4567"
```

## 🔐 Authentication & Security

### Keycloak Configuration

```yaml
keycloak:
  # Realm name (shown in Keycloak URL)
  realm: "acme-retail"

  # Admin credentials (CHANGE IN PRODUCTION!)
  admin:
    username: "admin"
    password: "YourVerySecurePassword123!"
    email: "admin@acme.com"

  # Session timeouts
  session:
    ssoSessionIdleTimeout: 3600     # 1 hour (in seconds)
    ssoSessionMaxLifespan: 86400    # 24 hours
    accessTokenLifespan: 300        # 5 minutes

  # Frontend client
  client:
    clientId: "acme-retail-app"
    redirectUris:
      - "http://localhost:3001/*"
      - "https://shop.acme.com/*"
      - "https://retail.acme.com/*"
```

### Security Settings

```yaml
security:
  # Multi-tenancy
  tenantIsolation: true            # Always keep true

  # Session management
  sessionTimeoutMinutes: 30
  jwtExpirationHours: 24

  # Password policy
  password:
    minLength: 12                   # Minimum 8, recommended 12+
    requireUppercase: true
    requireLowercase: true
    requireNumbers: true
    requireSpecialChars: true

    # Additional options
    maxLength: 128
    preventReuseCount: 5            # Prevent reusing last 5 passwords
    expirationDays: 90             # Force password change every 90 days

  # Account security
  maxLoginAttempts: 5
  accountLockDurationMinutes: 30

  # Two-factor authentication (future)
  twoFactorAuth:
    enabled: false
    methods: ["totp", "email", "sms"]

  # IP whitelisting (optional)
  ipWhitelist:
    enabled: false
    allowedIps:
      - "192.168.1.0/24"          # Allow local network
      - "10.0.0.0/8"              # Allow corporate VPN
```

### CORS Configuration

```yaml
security:
  cors:
    allowedOrigins: "https://shop.acme.com,https://admin.acme.com"
    allowedMethods: "GET,POST,PUT,DELETE,OPTIONS,PATCH"
    allowedHeaders: "*"
    allowCredentials: "true"
    maxAge: 3600                  # Preflight cache duration
```

## 👥 Test Users & Initial Data

### Enable/Disable Test Users

```yaml
testUsers:
  enabled: true   # Set to FALSE in production!
```

### Custom Test Users

```yaml
testUsers:
  enabled: true
  users:
    # Tenant Administrator (full access)
    - username: "owner@myshop.com"
      password: "SecurePass123!"
      email: "owner@myshop.com"
      firstName: "Shop"
      lastName: "Owner"
      role: "TENANT_ADMIN"
      tenantId: "acme-corp"
      shopId: "main-store"

    # Shop Manager
    - username: "manager@myshop.com"
      password: "Manager123!"
      email: "manager@myshop.com"
      firstName: "Store"
      lastName: "Manager"
      role: "SHOP_MANAGER"
      tenantId: "acme-corp"
      shopId: "main-store"

    # Cashier
    - username: "cashier@myshop.com"
      password: "Cashier123!"
      email: "cashier@myshop.com"
      firstName: "Front"
      lastName: "Desk"
      role: "CASHIER"
      tenantId: "acme-corp"
      shopId: "main-store"

    # Investor (view-only access to financial reports)
    - username: "investor@myshop.com"
      password: "Investor123!"
      email: "investor@myshop.com"
      firstName: "Silent"
      lastName: "Partner"
      role: "INVESTOR"
      tenantId: "acme-corp"
      shopId: "main-store"
```

### Available Roles

| Role | Permissions | Typical Use |
|------|------------|-------------|
| `TENANT_ADMIN` | Full access to all tenant resources | Business owner |
| `SHOP_MANAGER` | Manage shop operations, inventory, staff | Store manager |
| `SHOP_EMPLOYEE` | Sales, inventory viewing, basic tasks | Shop staff |
| `CASHIER` | Sales transactions only | Cashier, POS operator |
| `INVESTOR` | View financial reports only | Silent partner, investor |
| `AUDITOR` | Read-only access | External auditor |

## 📊 Business Rules

### Regional Settings

```yaml
business:
  # Currency
  defaultCurrency: "USD"          # ISO 4217 code
  currencySymbol: "$"
  currencyPosition: "before"      # before or after amount

  # Tax
  defaultTaxRate: "0.0825"        # 8.25% (California)
  taxLabel: "Sales Tax"
  taxInclusive: false             # false = tax added at checkout

  # Regional
  timezone: "America/Los_Angeles"  # IANA timezone
  locale: "en_US"                  # BCP 47 locale
  dateFormat: "MM/DD/YYYY"
  timeFormat: "12h"               # 12h or 24h
  firstDayOfWeek: 0               # 0=Sunday, 1=Monday
```

**Common Configurations:**

```yaml
# United States
business:
  defaultCurrency: "USD"
  defaultTaxRate: "0.08"
  timezone: "America/New_York"
  locale: "en_US"
  dateFormat: "MM/DD/YYYY"
  timeFormat: "12h"

# United Kingdom
business:
  defaultCurrency: "GBP"
  defaultTaxRate: "0.20"          # 20% VAT
  timezone: "Europe/London"
  locale: "en_GB"
  dateFormat: "DD/MM/YYYY"
  timeFormat: "24h"

# Nigeria
business:
  defaultCurrency: "NGN"
  defaultTaxRate: "0.075"         # 7.5% VAT
  timezone: "Africa/Lagos"
  locale: "en_NG"
  dateFormat: "DD/MM/YYYY"
  timeFormat: "12h"

# India
business:
  defaultCurrency: "INR"
  defaultTaxRate: "0.18"          # 18% GST
  timezone: "Asia/Kolkata"
  locale: "en_IN"
  dateFormat: "DD/MM/YYYY"
  timeFormat: "12h"
```

### Inventory Management

```yaml
business:
  inventory:
    # Stock thresholds
    lowStockThreshold: 20         # Trigger low stock alert
    criticalStockThreshold: 5     # Trigger critical alert

    # Expiry management
    expiryWarningDays: 30         # Warn when product expires in 30 days
    removeExpiredDays: 7          # Auto-remove expired items after 7 days

    # Automatic reordering
    autoReorderEnabled: true
    reorderMultiplier: 2.0        # Reorder qty = lowStockThreshold * 2
    stockCheckIntervalHours: 6    # Check stock every 6 hours

    # FEFO (First Expiry, First Out) strategy
    useFefo: true                 # Use FEFO for sales
    fefoGraceDays: 0              # Allow selling items expiring within X days

    # Batch tracking
    requireBatchNumber: false     # Require batch number for all products
    autoGenerateBatch: true       # Auto-generate batch numbers
    batchNumberFormat: "BTN-{date}-{sequence}"
```

### Sales & Returns

```yaml
business:
  # Return policy
  maxReturnDays: 30               # Accept returns within 30 days
  allowPartialReturns: true
  requireReceiptForReturn: true
  refundMethod: "original"        # original, store_credit, cash

  # Discount rules
  maxDiscountPercent: 50          # Max 50% discount per item
  requireManagerApproval: 30      # Require approval for discounts > 30%

  # Receipt settings
  printReceiptByDefault: true
  emailReceiptOption: true
  smsReceiptOption: false
```

## 🎛️ Feature Flags

Enable or disable specific features:

```yaml
features:
  # Core features
  investment: true                # Investment tracking & profit sharing
  analytics: true                 # Analytics dashboard with charts
  reports: true                   # PDF report generation

  # Advanced features
  fraud: false                    # Fraud detection (coming soon)
  multiCurrency: false            # Multi-currency transactions
  barcodeScanning: true           # Barcode scanner support
  loyaltyProgram: false           # Customer loyalty & rewards

  # Integrations
  accounting: false               # QuickBooks/Xero integration
  ecommerce: false                # WooCommerce/Shopify sync
  emailMarketing: false           # Mailchimp integration
  sms: false                      # SMS notifications

  # Experimental
  aiRecommendations: false        # AI-powered product recommendations
  voiceCommands: false            # Voice-activated POS
  facialRecognition: false        # Biometric authentication
```

## 🌐 Domain & SSL Certificates

### Custom Domain

```yaml
global:
  domain: "shop.acme.com"

certificates:
  # Auto-generate self-signed certificates
  autoGenerate: true

  # Certificate details
  commonName: "shop.acme.com"
  organization: "ACME Corporation"
  country: "US"
  state: "California"
  locality: "San Francisco"
  validityDays: 365

  # Subject Alternative Names (SANs)
  alternativeNames:
    - "shop.acme.com"
    - "*.shop.acme.com"
    - "retail.acme.com"
    - "localhost"
```

### Generate Certificates

```bash
# Generate and install certificates
./scripts/install-certs.sh \
  --domain shop.acme.com \
  --org "ACME Corporation" \
  --days 365
```

### Use Custom Certificates

```yaml
certificates:
  autoGenerate: false

  # Paths to your existing certificates
  customCert:
    certificatePath: "./certs/shop.acme.com.crt"
    privateKeyPath: "./certs/shop.acme.com.key"
    caChainPath: "./certs/ca-chain.crt"  # Optional
```

### Let's Encrypt (Production)

For production with public domain:

```bash
# Install certbot
sudo apt-get install certbot  # Ubuntu/Debian
brew install certbot          # macOS

# Generate certificate
sudo certbot certonly --standalone -d shop.acme.com

# Copy to project
cp /etc/letsencrypt/live/shop.acme.com/fullchain.pem ./certs/
cp /etc/letsencrypt/live/shop.acme.com/privkey.pem ./certs/
```

Update config:
```yaml
certificates:
  autoGenerate: false
  customCert:
    certificatePath: "./certs/fullchain.pem"
    privateKeyPath: "./certs/privkey.pem"
```

## ⚙️ Advanced Configuration

### Database Tuning

```yaml
database:
  postgres:
    # Connection pool
    pool:
      minIdle: 10
      maxPoolSize: 50            # Increase for high traffic
      connectionTimeout: 30000   # 30 seconds
      maxLifetime: 1800000       # 30 minutes

    # Performance
    sharedBuffers: "256MB"       # 25% of RAM
    effectiveCacheSize: "1GB"    # 50-75% of RAM
    workMem: "8MB"
    maintenanceWorkMem: "64MB"
```

### Kafka Configuration

```yaml
kafka:
  enabled: true

  # Retention
  retention:
    hours: 168                   # 7 days
    bytes: 1073741824            # 1 GB

  # Performance
  replicationFactor: 1           # Set to 3 in production cluster
  partitions: 3

  # Topics
  topics:
    - name: "sales-events"
      partitions: 3
      replicationFactor: 1
    - name: "inventory-updates"
      partitions: 2
      replicationFactor: 1
```

### Backup Configuration

```yaml
backup:
  enabled: true

  # Schedule (cron format)
  schedule: "0 2 * * *"          # Daily at 2 AM
  # Other examples:
  # - "0 */6 * * *"              # Every 6 hours
  # - "0 2 * * 0"                # Weekly (Sunday at 2 AM)
  # - "0 2 1 * *"                # Monthly (1st at 2 AM)

  # Retention
  retentionDays: 30
  maxBackups: 10                 # Keep max 10 backups

  # Location
  path: "./backups"
  # Or remote:
  # path: "s3://my-bucket/shop-manager-backups"
  # path: "/mnt/nas/backups"

  # Compression
  compression: true
  compressionLevel: 6            # 1-9 (9=best compression, slower)

  # Encryption
  encryption:
    enabled: true
    algorithm: "AES256"
    key: "your-32-character-encryption-key-here"  # KEEP SECRET!
```

## 🏪 Multi-Shop Setup

Configure multiple shops under one tenant:

```yaml
# Create additional shops
testUsers:
  enabled: true
  users:
    # Main store manager
    - username: "manager.main@acme.com"
      password: "Manager123!"
      role: "SHOP_MANAGER"
      tenantId: "acme-corp"
      shopId: "main-store"

    # Downtown store manager
    - username: "manager.downtown@acme.com"
      password: "Manager123!"
      role: "SHOP_MANAGER"
      tenantId: "acme-corp"
      shopId: "downtown-store"

    # Airport store manager
    - username: "manager.airport@acme.com"
      password: "Manager123!"
      role: "SHOP_MANAGER"
      tenantId: "acme-corp"
      shopId: "airport-store"
```

After deployment, create shops via API or UI with different:
- Locations
- Operating hours
- Inventory
- Staff
- Pricing (optional)

## 💾 Backup & Recovery

### Manual Backup

```bash
# Backup everything
./scripts/backup.sh

# Backup database only
docker compose exec postgres pg_dump -U shop shopdb > backup.sql

# Backup configuration
tar -czf config-backup.tar.gz config.yaml generated/
```

### Automatic Backups

Configured in `config.yaml`:

```yaml
backup:
  enabled: true
  schedule: "0 2 * * *"          # Daily at 2 AM
  retentionDays: 30
  path: "./backups"
  compression: true
  encryption:
    enabled: true
    key: "your-encryption-key"
```

### Restore from Backup

```bash
# Stop services
docker compose down

# Restore database
cat backup.sql | docker compose exec -T postgres psql -U shop shopdb

# Restore files
tar -xzf backup-20240115.tar.gz

# Restart
docker compose up -d
```

---

## 📞 Need Help?

- **Documentation:** See main `README.md`
- **Support:** support@shopmanager.com
- **Issues:** https://github.com/yourorg/shop-manager/issues

---

**Next:** Check out [TROUBLESHOOTING.md](TROUBLESHOOTING.md) for common issues and solutions.
