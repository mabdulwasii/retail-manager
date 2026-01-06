# Shop Manager - Platform Installers

This directory contains all platform-specific installer configurations and build scripts for Shop Manager **Embedded Mode** (standalone deployment).

## 🔑 Quick Start - Default Credentials

After installation, login with:

**Username:** `superadmin`
**Email:** `superadmin@retailhq.local`
**Password:** `changeme`

⚠️ **IMPORTANT**: Change this password immediately after first login!

**Default URLs:**
- **Frontend:** http://localhost:3001
- **Backend API:** http://localhost:8081
- **With mDNS:** http://shopmanager.local (no port needed)

### 🌐 Cloud Sync

Embedded installations can optionally sync to cloud aggregator for multi-shop analytics:

**Setup During Installation** (Windows):
- Option 1: Standalone (no cloud sync)
- Option 2: Register new account (auto-generate API key)
- Option 3: Use existing API key

**Setup After Installation** (All Platforms):
- Navigate to Settings → Cloud Sync
- Enable/disable, register account, test connection
- Manual sync trigger

**See**: [docs/CLOUD_SYNC_ARCHITECTURE.md](../docs/CLOUD_SYNC_ARCHITECTURE.md) for full details.

---

## Installation Modes Explained

Shop Manager supports two deployment modes:

### 1. **Embedded Mode** (These Installers) ✅
- **What it is**: Standalone, all-in-one native application
- **Components**: Embedded PostgreSQL + Local JWT authentication
- **Best for**: Single shop, offline-capable, simple deployment
- **No Docker required**: Runs as native .exe (Windows) or .app (macOS)
- **Size**: ~120-130 MB per installer

### 2. **Cloud Mode** (Separate Installation)
- **What it is**: Full-featured deployment with enterprise SSO
- **Components**: PostgreSQL + Keycloak + Kafka + MinIO (Docker Compose)
- **Best for**: Multi-shop, centralized authentication, cloud deployment
- **Requires Docker**: Uses Docker Compose
- **See**: `DEPLOYMENT_GUIDE.md` for Docker deployment

---

## Directory Structure

```
installers/
├── windows/                    # Windows installer (Inno Setup)
│   ├── shop-manager.iss       # Inno Setup script
│   ├── scripts/               # Launcher scripts (.bat files)
│   ├── config/                # Configuration templates
│   └── README.md              # Windows build guide
├── macos/                      # macOS installer (DMG)
│   ├── build-dmg.sh           # DMG builder script
│   ├── scripts/               # Launcher script
│   ├── config/                # Configuration templates
│   └── README.md              # macOS build guide
└── linux/                      # Linux packages (.deb, .rpm, AppImage)
    ├── build-packages.sh      # Package builder script
    ├── scripts/               # Launcher script
    ├── config/                # Configuration templates
    └── README.md              # Linux build guide
```

## Quick Build

### Prerequisites

1. **Embedded JAR** must be built first:
   ```bash
   cd backend
   ./mvnw clean package -Pembedded -DskipTests
   ```

2. **Platform-specific tools** (depending on target platform):
   - Windows: Inno Setup 6.x
   - macOS: create-dmg (via Homebrew)
   - Linux: dpkg, rpmbuild

### Build All Installers

From project root:

```bash
# Make script executable
chmod +x build-installers.sh

# Run interactive build
./build-installers.sh
```

This script will:
1. Detect your platform
2. Check prerequisites
3. Build embedded JAR (if needed)
4. Offer build options:
   - Build for current platform only
   - Build all available installers
   - JAR only

### Build Individual Platforms

#### Windows

```bash
cd installers/windows

# Option 1: Using Inno Setup GUI
# - Open shop-manager.iss in Inno Setup Compiler
# - Click Build → Compile

# Option 2: Using command line
"C:\Program Files (x86)\Inno Setup 6\ISCC.exe" shop-manager.iss
```

Output: `../../build/installers/windows/shop-manager-1.0.0-windows-x64-setup.exe`

#### macOS

```bash
cd installers/macos

# Make script executable
chmod +x build-dmg.sh

# Build DMG
./build-dmg.sh
```

Output: `../../build/installers/macos/shop-manager-1.0.0-macos-x64.dmg`

#### Linux

```bash
cd installers/linux

# Make script executable
chmod +x build-packages.sh

# Build all Linux packages
./build-packages.sh
```

Outputs:
- `../../build/installers/linux/shop-manager_1.0.0_all.deb`
- `../../build/installers/linux/shop-manager-1.0.0-1.*.rpm`
- `../../build/installers/linux/shop-manager-1.0.0-x86_64.AppImage.tar.gz`

## Installer Features

### Windows (.exe)

- ✅ Automatic Java version detection
- ✅ JWT secret auto-generation
- ✅ **Cloud sync configuration wizard**
- ✅ Hostname and mDNS setup wizard
- ✅ Desktop icon and Start Menu shortcuts
- ✅ Windows Service installation option
- ✅ Uninstaller included
- ✅ Configuration editor shortcuts
- 📦 Size: ~120 MB

### macOS (.dmg)

- ✅ Standard macOS app bundle
- ✅ Drag-to-Applications installer
- ✅ Automatic Java version check
- ✅ Native notifications
- ✅ launchd service support
- 📦 Size: ~130 MB

### Linux (.deb, .rpm, AppImage)

- ✅ System service (systemd) integration
- ✅ Desktop entry for application menu
- ✅ Automatic user/group creation
- ✅ JWT secret auto-generation
- ✅ Uninstall scripts
- 📦 Size: ~120-130 MB each

## CI/CD Integration

GitHub Actions workflow automatically builds all platform installers on:
- Tagged releases (`v*.*.*`)
- Manual workflow dispatch

See: `.github/workflows/build-standalone-release.yml`

### Workflow Jobs

1. `build-embedded-jar` - Builds the embedded JAR
2. `build-docker-lite` - Builds Docker Compose Lite package
3. `build-windows-installer` - Builds Windows .exe (on Windows runner)
4. `build-macos-installer` - Builds macOS .dmg (on macOS runner)
5. `build-linux-packages-native` - Builds .deb, .rpm, AppImage (on Linux runner)
6. `upload-native-installers` - Uploads all installers to GitHub release

## Distribution

### GitHub Releases

All installers are automatically uploaded to GitHub releases:

```
https://github.com/yourorg/shop-manager/releases
```

Download options:
- `shop-manager-1.0.0-windows-x64-setup.exe` - Windows installer
- `shop-manager-1.0.0-macos-x64.dmg` - macOS installer
- `shop-manager_1.0.0_all.deb` - Debian/Ubuntu package
- `shop-manager-1.0.0-1.*.rpm` - RHEL/Fedora package
- `shop-manager-1.0.0-x86_64.AppImage.tar.gz` - Universal Linux
- `shop-manager-docker-lite-*.tar.gz` - Docker Compose Lite
- `shop-manager-1.0.0-SNAPSHOT-embedded.jar` - Embedded JAR (all platforms)

## Installation Guides

Comprehensive installation guides available:

- **Installer Features**: [docs/INSTALLER_FEATURES.md](../docs/INSTALLER_FEATURES.md)
- **Cloud Sync Architecture**: [docs/CLOUD_SYNC_ARCHITECTURE.md](../docs/CLOUD_SYNC_ARCHITECTURE.md)
- **Platform Installers**: [docs/PLATFORM_INSTALLERS.md](../docs/PLATFORM_INSTALLERS.md)
- **Embedded Deployment**: [docs/EMBEDDED_DEPLOYMENT.md](../docs/EMBEDDED_DEPLOYMENT.md)
- **Docker Lite**: [docs/DOCKER_LITE_DEPLOYMENT.md](../docs/DOCKER_LITE_DEPLOYMENT.md)

Platform-specific guides:
- **Windows**: [installers/windows/README.md](./windows/README.md)
- **macOS**: [installers/macos/README.md](./macos/README.md)
- **Linux**: [installers/linux/README.md](./linux/README.md)

## Testing

### Test Installers Locally

**Windows:**
```powershell
# Install
.\shop-manager-1.0.0-windows-x64-setup.exe

# Verify
curl http://localhost:8081/actuator/health

# Uninstall
# Control Panel → Programs → Uninstall Shop Manager
```

**macOS:**
```bash
# Mount DMG
open shop-manager-1.0.0-macos-x64.dmg

# Drag to Applications
# Launch from Applications

# Verify
curl http://localhost:8081/actuator/health

# Uninstall
sudo rm -rf /Applications/Shop\ Manager.app
rm -rf ~/.shopmanager
```

**Linux (Debian/Ubuntu):**
```bash
# Install
sudo dpkg -i shop-manager_1.0.0_all.deb

# Start service
sudo systemctl start shop-manager

# Verify
curl http://localhost:8081/actuator/health

# Uninstall
sudo dpkg -r shop-manager
```

## Asset Requirements

### Windows

- `assets/shop-manager.ico` - Application icon (multi-resolution ICO)
- `assets/wizard-image.bmp` - Installer wizard image (164x314px)
- `assets/wizard-small.bmp` - Installer wizard small image (55x58px)

### macOS

- `assets/shop-manager.icns` - Application icon (ICNS format)
- `assets/dmg-background.png` - DMG background (800x400px, optional)

### Linux

- `assets/shop-manager.png` - Application icon (256x256px PNG)

## Troubleshooting

### Build Failures

**"Embedded JAR not found"**
```bash
cd backend
./mvnw clean package -Pembedded -DskipTests
```

**"Inno Setup not found" (Windows)**
```powershell
choco install innosetup -y
```

**"create-dmg not found" (macOS)**
```bash
brew install create-dmg
```

**"dpkg-deb not found" (Linux)**
```bash
sudo apt-get install dpkg
```

### Installation Issues

See platform-specific troubleshooting guides:
- [Windows Troubleshooting](./windows/README.md#troubleshooting)
- [macOS Troubleshooting](./macos/README.md#troubleshooting)
- [Linux Troubleshooting](./linux/README.md#troubleshooting)

## Support

- **Documentation**: [docs/](../docs/)
- **Issues**: https://github.com/yourorg/shop-manager/issues
- **Email**: support@shopmanager.com

---

**Last Updated**: 2025-12-24
