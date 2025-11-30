# Shop Manager - Standalone Distribution

Welcome to Shop Manager Standalone! This package provides a complete, easy-to-install version of Shop Manager that runs on your local machine using Docker Compose.

## 📋 Table of Contents

- [Overview](#overview)
- [System Requirements](#system-requirements)
- [Quick Start](#quick-start)
- [Installation Methods](#installation-methods)
- [Configuration](#configuration)
- [Customization](#customization)
- [Distribution Packages](#distribution-packages)
- [Troubleshooting](#troubleshooting)
- [Support](#support)

## 🎯 Overview

Shop Manager Standalone is designed for small businesses that need a powerful retail management system without the complexity of Kubernetes. It provides:

✅ **All Features of Enterprise Version**
- Multi-tenant architecture
- Complete inventory management
- Sales tracking with FEFO
- Investment module
- Analytics dashboard
- PDF receipt generation

✅ **Easy Installation**
- One-command installation
- Automatic Docker setup
- Pre-configured for immediate use
- Works offline after initial setup

✅ **Full Customization**
- Company branding (logo, colors, name)
- Custom domain and certificates
- Configurable test users
- Feature flags
- Business rules

## 💻 System Requirements

### Minimum Requirements

| Component | Requirement |
|-----------|------------|
| **Operating System** | Windows 10/11, macOS 11+, Ubuntu 20.04+ |
| **RAM** | 8 GB (4 GB available) |
| **Disk Space** | 20 GB free |
| **CPU** | 2 cores (4 recommended) |
| **Docker** | Docker Desktop 4.0+ or Docker Engine 20+ |
| **Python** | Python 3.7+ (for configuration) |

### Recommended Requirements

| Component | Recommendation |
|-----------|---------------|
| **RAM** | 16 GB |
| **Disk Space** | 50 GB SSD |
| **CPU** | 4 cores |
| **Network** | Broadband (for initial setup) |

## 🚀 Quick Start

### For Linux/macOS

```bash
# 1. Extract the package
unzip shop-manager-standalone-v1.0.0.zip
cd shop-manager-standalone

# 2. Run the installer
./install.sh

# 3. Wait 10-15 minutes for installation

# 4. Access at http://localhost:3001
# Login: admin@shopmanager.com / admin123
```

### For Windows

```cmd
REM 1. Extract the ZIP file
REM 2. Open PowerShell or Command Prompt as Administrator
cd shop-manager-standalone

REM 3. Run the installer
install.bat

REM 4. Wait 10-15 minutes for installation

REM 5. Open browser to http://localhost:3001
REM Login: admin@shopmanager.com / admin123
```

## 📦 Installation Methods

### Method 1: Automated Installer (Recommended)

The automated installer handles everything for you:

**Linux/macOS:**
```bash
./install.sh
```

**Windows:**
```cmd
install.bat
```

The installer will:
1. ✅ Check prerequisites (Docker, Python)
2. ✅ Generate configuration files
3. ✅ Pull Docker images
4. ✅ Build custom images
5. ✅ Start all services
6. ✅ Verify installation

### Method 2: Manual Installation

For advanced users who want more control:

```bash
# 1. Generate configuration
python3 scripts/generate-config.py

# 2. Copy generated files
cp generated/.env ../
cp generated/keycloak-realm.json ../docker/
cp generated/docker-compose.override.yml ../

# 3. Start services
cd ..
docker compose up -d
```

### Method 3: USB Offline Installation

For installations without internet access:

1. **Prepare USB drive on internet-connected machine:**
   ```bash
   # Download all Docker images
   docker compose pull

   # Save images to tar files
   docker save -o backend.tar shop-manager-backend:latest
   docker save -o frontend.tar shop-manager-frontend:latest
   docker save -o postgres.tar postgres:15-alpine
   docker save -o keycloak.tar quay.io/keycloak/keycloak:24.0.1
   docker save -o kafka.tar confluentinc/cp-kafka:7.5.0
   docker save -o minio.tar minio/minio:RELEASE.2024-03-07T00-43-48Z

   # Copy to USB
   cp *.tar /path/to/usb/docker-images/
   ```

2. **Install on offline machine:**
   ```bash
   # Load images from USB
   cd /path/to/usb/docker-images
   docker load -i backend.tar
   docker load -i frontend.tar
   docker load -i postgres.tar
   docker load -i keycloak.tar
   docker load -i kafka.tar
   docker load -i minio.tar

   # Run installation
   cd ../shop-manager-standalone
   ./install.sh --skip-pull
   ```

### Method 4: Docker Hub Installation

Pull pre-built images from Docker Hub:

```bash
# Use pre-built images
docker pull yourorg/shop-manager:backend-latest
docker pull yourorg/shop-manager:frontend-latest

# Update docker-compose.yml to use these images
# Then run
docker compose up -d
```

## ⚙️ Configuration

All configuration is managed through `config.yaml`. This file controls every aspect of your installation.

### Basic Configuration

Edit `config.yaml`:

```yaml
# Company Information
branding:
  platformName: "My Retail Pro"
  companyName: "ACME Corporation"

global:
  domain: "retail.acme.local"

# Security (CHANGE THESE!)
keycloak:
  admin:
    password: "YourSecurePassword123!"

database:
  postgres:
    app:
      password: "YourDatabasePassword!"
```

### Generate Configuration Files

After editing `config.yaml`:

```bash
python3 scripts/generate-config.py
```

This generates:
- `.env` - Environment variables
- `keycloak-realm.json` - Authentication configuration
- `docker-compose.override.yml` - Service customizations

## 🎨 Customization

### 1. Branding

**Change Company Name & Platform Name:**

```yaml
branding:
  platformName: "RetailMax Pro"
  companyName: "TechMart Solutions"
  platformDescription: "Advanced Retail Management"
```

**Upload Custom Logo:**

```yaml
branding:
  logos:
    primary: "./assets/my-logo.png"
    favicon: "./assets/favicon.ico"
    loginLogo: "./assets/login-logo.svg"
```

**Customize Colors:**

```yaml
branding:
  colors:
    primary: "#2E7D32"      # Your brand color
    secondary: "#FF6F00"     # Accent color
    success: "#4CAF50"       # Green
    warning: "#FFA726"       # Orange
    error: "#EF5350"         # Red
```

### 2. Test Users

**Enable/Disable Test Users:**

```yaml
testUsers:
  enabled: false  # Set to false in production
```

**Customize Test Users:**

```yaml
testUsers:
  enabled: true
  users:
    - username: "owner@myshop.com"
      password: "SecurePass123!"
      email: "owner@myshop.com"
      firstName: "Shop"
      lastName: "Owner"
      role: "TENANT_ADMIN"
```

### 3. Domain & SSL Certificates

**Custom Domain:**

```yaml
global:
  domain: "shop.mycompany.com"

certificates:
  commonName: "shop.mycompany.com"
  organization: "My Company Inc"
  autoGenerate: true
  autoInstall: true
```

**Generate & Install Certificates:**

```bash
./scripts/install-certs.sh \
  --domain shop.mycompany.com \
  --org "My Company Inc"
```

### 4. Business Rules

**Currency & Tax:**

```yaml
business:
  defaultCurrency: "USD"
  defaultTaxRate: "0.08"  # 8%
  timezone: "America/New_York"
  locale: "en_US"
```

**Inventory Settings:**

```yaml
business:
  inventory:
    lowStockThreshold: 20
    expiryWarningDays: 60
    autoReorderEnabled: true
    stockCheckIntervalHours: 12
```

### 5. Feature Flags

Enable/disable features:

```yaml
features:
  investment: true           # Investment tracking
  analytics: true            # Analytics dashboard
  fraud: false               # Fraud detection
  multiCurrency: true        # Multi-currency support
  barcodeScanning: true      # Barcode scanning
  loyaltyProgram: false      # Loyalty program
```

## 📂 Distribution Packages

### Package Types

#### 1. ZIP Archive (All Platforms)

**Contents:**
```
shop-manager-standalone-v1.0.0.zip (450 MB)
├── README.md
├── LICENSE
├── config.yaml
├── install.sh (Linux/macOS)
├── install.bat (Windows)
├── docker-compose.yml
├── scripts/
│   ├── generate-config.py
│   ├── install-certs.sh
│   └── backup.sh
├── templates/
│   └── keycloak-realm.json.j2
├── docs/
│   ├── INSTALL.md
│   ├── CUSTOMIZE.md
│   ├── TROUBLESHOOTING.md
│   └── API.md
└── docker-images/ (optional - for offline install)
    ├── backend.tar.gz
    ├── frontend.tar.gz
    └── ...
```

**Distribution Methods:**
- Direct download from website
- Google Drive / Dropbox link
- Email link (for licensed customers)
- USB flash drive (offline installation)

#### 2. Desktop Application (Coming Soon)

**Electron-based GUI installer:**
- Windows: `.exe` installer (135 MB)
- macOS: `.dmg` installer (130 MB)
- Linux: `.AppImage` (145 MB)

**Features:**
- Visual configuration wizard
- One-click installation
- System tray app
- Auto-updates
- Service management
- Backup/restore GUI

#### 3. Cloud Marketplace

**DigitalOcean 1-Click App:**
- Pre-configured Droplet
- Automatic SSL with Let's Encrypt
- $10/month (includes server)
- Public URL provided

**AWS Marketplace AMI:**
- EC2 instance with Shop Manager
- Pay-as-you-go licensing
- Auto-scaling support

### Creating Distribution Packages

#### Create ZIP Package

```bash
# Full package with offline images
./scripts/create-distribution.sh --include-images

# Lightweight package (online installation only)
./scripts/create-distribution.sh

# Custom version
./scripts/create-distribution.sh --version 1.2.0 --include-images
```

#### Create USB Installer

```bash
# Create bootable USB installer
./scripts/create-usb-installer.sh /dev/sdb  # Linux
./scripts/create-usb-installer.sh E:        # Windows
```

## 🔧 Troubleshooting

### Common Issues

#### Docker Not Running

**Symptoms:**
```
Error: Cannot connect to the Docker daemon
```

**Solution:**
```bash
# Start Docker Desktop
# Or on Linux:
sudo systemctl start docker
```

#### Port Already in Use

**Symptoms:**
```
Error: port is already allocated
```

**Solution:**
```bash
# Check what's using the port
sudo lsof -i :8080  # macOS/Linux
netstat -ano | findstr :8080  # Windows

# Change ports in docker-compose.yml or stop conflicting service
```

#### Services Not Starting

**Symptoms:**
```
Container exits immediately
```

**Solution:**
```bash
# View logs
docker compose logs backend
docker compose logs keycloak

# Check for errors in configuration
python3 scripts/generate-config.py --validate-only
```

#### Browser Security Warning

**Symptoms:**
```
"Your connection is not private" or "NET::ERR_CERT_AUTHORITY_INVALID"
```

**Solution:**
```bash
# Install SSL certificate
./scripts/install-certs.sh

# Or disable SSL temporarily (development only)
# Edit config.yaml: tls.enabled: false
```

#### Can't Login

**Symptoms:**
```
Invalid credentials or 401 error
```

**Solution:**
```bash
# Check test users are enabled
grep "testUsers.enabled" config.yaml

# Reset Keycloak admin password
docker compose exec keycloak \
  /opt/keycloak/bin/kcadm.sh set-password \
  --username admin --new-password "NewPassword123!"

# Verify backend is running
docker compose ps backend
docker compose logs backend
```

### Advanced Troubleshooting

#### Reset Installation

```bash
# Stop all services
docker compose down

# Remove all volumes (WARNING: deletes all data!)
docker compose down -v

# Clean up images
docker system prune -a

# Start fresh
./install.sh
```

#### Export Logs for Support

```bash
# Export all logs
docker compose logs > shop-manager-logs.txt

# Export specific service
docker compose logs backend > backend-logs.txt

# Real-time logs
docker compose logs -f --tail=100
```

#### Database Backup & Restore

```bash
# Backup database
docker compose exec postgres \
  pg_dump -U shop shopdb > backup-$(date +%Y%m%d).sql

# Restore database
cat backup-20240115.sql | \
  docker compose exec -T postgres psql -U shop shopdb
```

## 📞 Support

### Documentation

- **Installation Guide:** `docs/INSTALL.md`
- **Customization Guide:** `docs/CUSTOMIZE.md`
- **API Documentation:** `docs/API.md`
- **Troubleshooting:** `docs/TROUBLESHOOTING.md`

### Community Support

- **GitHub Issues:** https://github.com/yourorg/shop-manager/issues
- **Discussion Forum:** https://github.com/yourorg/shop-manager/discussions
- **Stack Overflow:** Tag `shop-manager`

### Professional Support

For paid support:
- **Email:** support@shopmanager.com
- **Priority Support:** Available with Professional Edition
- **Remote Installation:** Available for Enterprise customers

### Version Information

Check your version:

```bash
# View installed version
docker compose exec backend java -jar app.jar --version

# Check for updates
./scripts/check-updates.sh
```

## 🔄 Release Management

### For End Users

Shop Manager Standalone uses automated releases for easy updates:

**Check for Updates:**
```bash
# Check if a new version is available
./scripts/check-updates.sh

# Update to latest version (with automatic backup)
./scripts/update.sh
```

**Electron App Users:**
- Updates are checked automatically on startup
- You'll be notified when a new version is available
- Click "Download" to install updates

### For Developers

Releases are automated via GitHub Actions using PR labels:

#### Release Labels

Add one of these labels to your PR before merging:

| Label | Effect | Example |
|-------|--------|---------|
| `release:standalone-patch` | Bug fixes | v1.0.0 → v1.0.1 |
| `release:standalone-minor` | New features | v1.0.0 → v1.1.0 |
| `release:standalone-major` | Breaking changes | v1.0.0 → v2.0.0 |

#### Automated Release Process

1. **Create PR** with your standalone changes
2. **Add Label** (e.g., `release:standalone-minor`)
3. **Merge PR** to main branch
4. **GitHub Actions** automatically:
   - Bumps version in `standalone/VERSION`
   - Creates git tag (e.g., `v1.1.0`)
   - Builds all packages:
     - Docker Compose (lightweight ~50MB)
     - Docker Compose (full with images ~2GB)
     - Windows installer (.exe)
     - macOS installer (.dmg)
     - Linux packages (.AppImage, .deb, .rpm)
   - Publishes GitHub release (draft)
5. **Edit Release** to add changelog
6. **Publish** when ready

#### What Gets Built

Each release automatically builds:

**For Non-Technical Users:**
- ✅ Windows NSIS Installer (`.exe`)
- ✅ Windows Portable (`.exe`)
- ✅ macOS DMG Installer (`.dmg`)
- ✅ macOS Auto-Update Package (`.zip`)
- ✅ Linux AppImage (universal)
- ✅ Debian Package (`.deb`)
- ✅ RPM Package (`.rpm`)

**For Technical Users:**
- ✅ Docker Compose Package (lightweight, downloads images)
- ✅ Docker Compose Package (full, includes images)
- ✅ SHA256 checksums for verification

#### Manual Release (Alternative)

If you prefer manual releases:

```bash
# 1. Update VERSION file
echo "1.2.0" > standalone/VERSION

# 2. Commit and tag
git add standalone/VERSION
git commit -m "chore(standalone): bump version to 1.2.0"
git tag v1.2.0
git push origin main
git push origin v1.2.0

# 3. GitHub Actions builds automatically from tag
```

#### Version Tracking

Current version is stored in `standalone/VERSION`:
```bash
cat standalone/VERSION
# Output: 1.0.0
```

#### Build Workflow

The build workflow (`.github/workflows/build-standalone-release.yml`) triggers on:
- Version tags (`v*.*.*`)
- Manual dispatch with version input

Monitor builds at: https://github.com/yourorg/shop-manager/actions

## 📝 License

See `LICENSE` file for licensing information.

---

**Shop Manager Standalone** - Making retail management accessible to everyone.

For the latest updates, visit: https://shopmanager.com
