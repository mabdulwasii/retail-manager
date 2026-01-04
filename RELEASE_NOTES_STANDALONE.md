# Shop Manager v1.0.0 - Standalone Release Notes

## 🎉 What's New

Shop Manager now offers **three flexible deployment options** to suit different use cases, technical expertise levels, and resource constraints:

### 1. Embedded JAR - Lightweight & Portable ✨
**Best for**: Individual PCs, laptops, minimal resource usage

- **Single executable JAR** with embedded H2 database
- **500-700 MB RAM** usage (vs 2-3 GB for full cloud version)
- **No external dependencies** except Java 21+
- **Offline-first** with optional cloud sync
- **Quick setup**: 5 minutes from download to running
- **Cross-platform**: Works on Windows, macOS, and Linux

### 2. Docker Compose Lite - Containerized & Easy  🐳
**Best for**: Containerized deployment, easy updates, process isolation

- **Lightweight containers** (~1-1.5 GB RAM total)
- **Backend + Frontend** in separate containers
- **H2 embedded database** (no PostgreSQL required)
- **One-command setup** with initialization script
- **Easy updates** via Docker Compose
- **Automated health checks** and restart policies

### 3. Platform-Specific Installers - Native Experience 📦
**Best for**: End users, non-technical staff, system integration

**Windows (.exe)**:
- Professional Inno Setup installer with wizard
- Automatic Java detection and download prompt
- Desktop shortcuts and Start Menu integration
- Windows Service installation option
- Full uninstaller with data preservation

**macOS (.dmg)**:
- Standard macOS app bundle
- Drag-to-Applications installation
- Native notifications and Gatekeeper support
- launchd service integration
- Clean uninstall process

**Linux (.deb, .rpm, AppImage)**:
- Debian/Ubuntu .deb package
- RHEL/Fedora .rpm package
- Universal AppImage tarball
- systemd service integration
- Desktop menu entries

---

## 🚀 Key Features

### Embedded Mode Features

All deployment options include:

- ✅ **H2 Embedded Database** - No PostgreSQL setup required
- ✅ **JWT Authentication** - Secure local authentication (no Keycloak)
- ✅ **Filesystem Storage** - No MinIO required
- ✅ **Spring Events** - No Kafka required
- ✅ **Auto-configuration** - Secure JWT secret generation
- ✅ **Cloud Sync** - Optional transaction data sync to cloud
- ✅ **Low Memory** - Runs on as little as 500 MB RAM
- ✅ **100% Offline** - Works completely offline
- ✅ **Quick Startup** - 10-30 seconds startup time

### Cloud Sync (Optional)

Perfect for franchise/multi-location businesses:

- **Offline-First**: Local stores work 100% offline
- **Scheduled Sync**: Hourly automatic sync (configurable)
- **Incremental**: Only new/modified transactions synced
- **Secure**: TLS + API key authentication
- **Privacy**: Optional PII anonymization
- **Resilient**: Auto-retry with exponential backoff
- **Auditable**: Full sync history logging

---

## 📦 Downloads

Choose the package that fits your needs:

### For Non-Technical Users (Recommended)

**Windows (64-bit)**:
```
shop-manager-1.0.0-windows-x64-setup.exe (120 MB)
```
- Double-click installer, follow wizard
- Automatic Java detection
- Desktop shortcuts included
- Optional Windows Service

**macOS (Intel & Apple Silicon)**:
```
shop-manager-1.0.0-macos-x64.dmg (130 MB)
```
- Drag to Applications
- Native macOS app
- Right-click "Open" on first launch

**Linux (64-bit)**:
```
shop-manager_1.0.0_all.deb          (Debian/Ubuntu)
shop-manager-1.0.0-1.*.rpm          (RHEL/Fedora)
shop-manager-1.0.0-x86_64.AppImage  (Universal)
```
- One-command installation
- systemd service integration
- Desktop menu entries

### For Technical Users / Developers

**Embedded JAR (All Platforms)**:
```
shop-manager-1.0.0-SNAPSHOT-embedded.jar (112 MB)
```
- Requires Java 21+
- Run with: `java -jar shop-manager-*.jar`
- Cross-platform compatibility

**Docker Compose Lite**:
```
shop-manager-docker-lite-20251224.tar.gz (250+ MB)
```
- Requires Docker Desktop or Engine
- Extract, run `./lite-init.sh`, start with `docker compose up -d`
- Easy updates and management

---

## 📋 System Requirements

### Minimum Requirements

| Component | Embedded JAR | Docker Lite | Platform Installers |
|-----------|--------------|-------------|---------------------|
| **OS** | Windows 10+, macOS 11+, Linux (Ubuntu 18.04+) | Same | Same |
| **RAM** | 1 GB free | 2 GB free | 1 GB free |
| **Disk** | 2 GB | 5 GB | 2 GB |
| **Dependencies** | Java 21+ | Docker | Java 21+ (auto-detected) |

### Recommended for Production

| Component | Requirement |
|-----------|-------------|
| **RAM** | 4 GB free |
| **Disk** | 10 GB free (for data growth) |
| **CPU** | 2+ cores |
| **Network** | Optional (for cloud sync) |

---

## ⚡ Quick Start

### Option 1: Windows Installer

```powershell
# 1. Download installer
shop-manager-1.0.0-windows-x64-setup.exe

# 2. Run as Administrator
# 3. Follow wizard
# 4. Launch from Start Menu or Desktop

# Access at http://localhost:8081
```

### Option 2: macOS Installer

```bash
# 1. Download DMG
shop-manager-1.0.0-macos-x64.dmg

# 2. Open DMG, drag to Applications
# 3. Right-click app → Open (first time only)
# 4. Or bypass Gatekeeper:
xattr -cr /Applications/Shop\ Manager.app
open -a "Shop Manager"

# Access at http://localhost:8081
```

### Option 3: Linux Package

```bash
# Debian/Ubuntu
sudo dpkg -i shop-manager_1.0.0_all.deb
sudo systemctl start shop-manager

# RHEL/Fedora
sudo rpm -i shop-manager-1.0.0-1.*.rpm
sudo systemctl start shop-manager

# Access at http://localhost:8081
```

### Option 4: Embedded JAR

```bash
# 1. Install Java 21+
# Windows: Download from https://adoptium.net
# macOS: brew install openjdk@21
# Linux: sudo apt-get install openjdk-21-jre

# 2. Run JAR
java -jar shop-manager-1.0.0-SNAPSHOT-embedded.jar

# Access at http://localhost:8081
```

### Option 5: Docker Compose Lite

```bash
# 1. Extract package
tar -xzf shop-manager-docker-lite-20251224.tar.gz
cd shop-manager-docker-lite

# 2. Initialize
./lite-init.sh

# 3. Start
docker compose -f docker-compose-lite.yml --env-file .env.lite up -d

# Access at http://localhost:3001 (frontend)
#          http://localhost:8081 (backend)
```

---

## 🔧 Configuration

All deployment options use the same configuration format:

### Basic Configuration

```bash
# Port Configuration
BACKEND_PORT=8081
FRONTEND_PORT=3001

# JWT Authentication
JWT_SECRET=<auto-generated-secure-secret>

# Cloud Sync (Optional)
CLOUD_SYNC_ENABLED=false
CLOUD_API_URL=
CLOUD_API_KEY=
STORE_ID=
```

### Configuration Locations

- **Windows**: `C:\Program Files\Shop Manager\config\.env`
- **macOS**: `~/.shopmanager/.env`
- **Linux (system)**: `/etc/shop-manager/shop-manager.env`
- **Linux (AppImage)**: `~/.shopmanager/.env`
- **Docker Lite**: `.env.lite` in project directory
- **Embedded JAR**: `.env` in same directory as JAR

### Enabling Cloud Sync

Edit configuration file:

```bash
CLOUD_SYNC_ENABLED=true
CLOUD_API_URL=https://cloud.shopmanager.com
CLOUD_API_KEY=sk_live_abc123...
STORE_ID=STORE-001
SYNC_CRON=0 0 * * * ?  # Hourly
ANONYMIZE_PII=true     # Recommended
```

Then restart the application.

---

## 📊 Performance Comparison

| Metric | Embedded JAR | Docker Lite | Full Cloud |
|--------|--------------|-------------|------------|
| **RAM Usage** | 500-700 MB | 1-1.5 GB | 2-3 GB |
| **Startup Time** | 10-20s | 30-60s | 2-3 min |
| **Disk Space** | 2 GB | 5 GB | 20 GB |
| **Setup Time** | 5 min | 10 min | 30-60 min |
| **Complexity** | ⭐ Very Easy | ⭐⭐ Easy | ⭐⭐⭐⭐ Complex |
| **Best For** | Individual PCs | Containers | Production servers |

---

## 🔐 Security Features

All deployment options include:

- ✅ **JWT Authentication** - Secure token-based auth
- ✅ **BCrypt Password Hashing** - Industry-standard encryption
- ✅ **Secure Secret Generation** - Cryptographically secure random secrets
- ✅ **TLS Support** - HTTPS for cloud sync
- ✅ **PII Anonymization** - Optional customer data privacy
- ✅ **Audit Logging** - Full sync history tracking

### Security Recommendations

1. **Change default password** immediately after first login
2. **Protect configuration files** with proper permissions:
   ```bash
   # Windows
   icacls .env /grant:r "%USERNAME%:(R)"

   # macOS/Linux
   chmod 600 ~/.shopmanager/.env
   ```
3. **Enable HTTPS** for production deployments
4. **Regular backups** of data directory
5. **Keep Java updated** to latest security patches

---

## 🆕 What's Changed from Full Cloud Version

### Removed Dependencies

- ❌ PostgreSQL → ✅ H2 embedded database
- ❌ Keycloak → ✅ JWT local authentication
- ❌ Kafka → ✅ Spring Events
- ❌ MinIO → ✅ Filesystem storage

### Memory Savings

- PostgreSQL: 400 MB saved
- Keycloak: 800 MB saved
- Kafka: 700 MB saved
- MinIO: 200 MB saved
- **Total**: ~2.1 GB → 500 MB = **1.6 GB saved**

### Feature Parity

All core features work identically:
- ✅ Sales transactions
- ✅ Inventory management
- ✅ Investment tracking
- ✅ Analytics & reporting
- ✅ PDF receipts
- ✅ Product returns
- ✅ Multi-tenant support

---

## 📖 Documentation

### Installation Guides

- **Platform Installers**: [docs/PLATFORM_INSTALLERS.md](./docs/PLATFORM_INSTALLERS.md)
- **Embedded Deployment**: [docs/EMBEDDED_DEPLOYMENT.md](./docs/EMBEDDED_DEPLOYMENT.md)
- **Docker Lite**: [docs/DOCKER_LITE_DEPLOYMENT.md](./docs/DOCKER_LITE_DEPLOYMENT.md)
- **Cloud Sync Setup**: [docs/CLOUD_SYNC_SETUP.md](./docs/CLOUD_SYNC_SETUP.md)

### Platform-Specific Guides

- **Windows**: [installers/windows/README.md](./installers/windows/README.md)
- **macOS**: [installers/macos/README.md](./installers/macos/README.md)
- **Linux**: [installers/linux/README.md](./installers/linux/README.md)

### Build Guides

- **Master Build**: [build-installers.sh](./build-installers.sh)
- **Installers Overview**: [installers/README.md](./installers/README.md)

---

## 🐛 Known Issues

### macOS Gatekeeper Warning

**Issue**: "Shop Manager.app is damaged and can't be opened"

**Solution**: This is a security feature, not actual damage. Run:
```bash
xattr -cr /Applications/Shop\ Manager.app
```

Or right-click → Open → Open in security dialog.

### Windows Defender SmartScreen

**Issue**: "Windows protected your PC" warning

**Solution**: Click "More info" → "Run anyway". This occurs because the installer is not code-signed.

### Java Version Mismatch

**Issue**: Application won't start with Java <21

**Solution**: Install Java 21 or higher:
- Windows/macOS: https://adoptium.net/temurin/releases/?version=21
- Linux: `sudo apt-get install openjdk-21-jre-headless`

---

## 🆙 Upgrade Path

### From Full Cloud to Embedded/Docker Lite

1. Export data from cloud installation
2. Install embedded/Docker Lite
3. Import data
4. Configure cloud sync (optional)

### From Embedded to Docker Lite

1. Stop embedded JAR
2. Copy `data/` directory
3. Extract Docker Lite package
4. Copy data to Docker volume
5. Start Docker Lite

---

## 💬 Support & Community

### Getting Help

- **Documentation**: [docs/](./docs/)
- **GitHub Issues**: https://github.com/yourorg/shop-manager/issues
- **Forum**: https://forum.shopmanager.com
- **Email**: support@shopmanager.com

### Contributing

We welcome contributions! See [CONTRIBUTING.md](./CONTRIBUTING.md) for guidelines.

### Report Bugs

Found a bug? Please report it:
1. Check existing issues: https://github.com/yourorg/shop-manager/issues
2. Create new issue with:
   - Deployment option used
   - OS and version
   - Java version
   - Steps to reproduce
   - Expected vs actual behavior

---

## 📅 Changelog

### [1.0.0] - 2025-12-24

#### Added
- ✨ Embedded JAR deployment option
- ✨ Docker Compose Lite deployment
- ✨ Windows native installer (Inno Setup)
- ✨ macOS native installer (DMG)
- ✨ Linux packages (.deb, .rpm, AppImage)
- ✨ Cloud sync feature for multi-location businesses
- ✨ H2 embedded database support
- ✨ JWT local authentication
- ✨ Filesystem storage layer
- ✨ Automated installer build pipeline (GitHub Actions)

#### Changed
- 🔄 Reduced memory footprint from 2-3 GB to 500 MB - 1.5 GB
- 🔄 Startup time improved from 2-3 min to 10-60s
- 🔄 Simplified deployment from 30-60 min to 2-10 min

#### Removed
- ❌ PostgreSQL dependency (for embedded mode)
- ❌ Keycloak dependency (for embedded mode)
- ❌ Kafka dependency (for embedded mode)
- ❌ MinIO dependency (for embedded mode)

---

## 🙏 Acknowledgments

Built with:
- Spring Boot 3.5.6
- H2 Database
- JJWT 0.12.3
- Docker
- Inno Setup (Windows)
- create-dmg (macOS)

---

**Last Updated**: 2025-12-24

**🎊 Thank you for using Shop Manager!**
