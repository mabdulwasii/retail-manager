# Shop Manager - Build Guide

This guide explains how to build distribution packages for Shop Manager from source.

## Table of Contents

1. [Prerequisites](#prerequisites)
2. [Building Docker Compose Packages](#building-docker-compose-packages)
3. [Building Electron Desktop Apps](#building-electron-desktop-apps)
4. [Building for Specific Platforms](#building-for-specific-platforms)
5. [Testing Builds Locally](#testing-builds-locally)
6. [Troubleshooting](#troubleshooting)

---

## Prerequisites

### Required Software

| Software | Version | Purpose |
|----------|---------|---------|
| **Git** | 2.0+ | Source control |
| **Docker** | 20.0+ | Building images |
| **Docker Compose** | 2.0+ | Multi-container builds |
| **Node.js** | 18.0+ LTS | Electron builds |
| **npm** | 9.0+ | Package management |
| **Python** | 3.7+ | Config generation |
| **Bash** | 4.0+ | Build scripts (Linux/macOS) |

### Optional Software

| Software | Purpose |
|----------|---------|
| **Wine** | Building Windows apps on Linux |
| **Code signing certificate** | Signing installers |
| **Notarization credentials** | macOS app notarization |

### System Requirements

**Minimum:**
- 16 GB RAM
- 50 GB free disk space
- 4 CPU cores
- Fast internet connection (first build)

**Recommended:**
- 32 GB RAM
- 100 GB SSD
- 8 CPU cores
- Gigabit internet

---

## Building Docker Compose Packages

### 1. Clone Repository

```bash
# Clone the repository
git checkout https://github.com/yourorg/shop-manager.git
cd shop-manager

# Checkout the version you want to build
git checkout v1.0.0

# Or stay on latest
git checkout main
```

### 2. Install Python Dependencies

```bash
cd standalone
pip3 install -r requirements.txt
```

Expected output:
```
Successfully installed PyYAML-6.0 Jinja2-3.1.0 MarkupSafe-2.1.0
```

### 3. Build Backend Docker Image

```bash
cd ../backend
docker build -t shop-manager-backend:latest .
```

**Build time:** 5-10 minutes (first time)

Expected final output:
```
Successfully built abc123def456
Successfully tagged shop-manager-backend:latest
```

### 4. Build Frontend Docker Image

```bash
cd ../frontend
docker build -t shop-manager-frontend:latest .
```

**Build time:** 3-5 minutes

### 5. Create Distribution Package

```bash
cd ../standalone/scripts

# Lightweight package (online installation)
./create-distribution.sh --version 1.0.0

# Full package (offline installation with images)
./create-distribution.sh --version 1.0.0 --include-images
```

**Output location:** `standalone/scripts/dist/`

**Expected files:**
```
dist/
├── shop-manager-standalone-v1.0.0.zip          (50 MB)
├── shop-manager-standalone-v1.0.0-full.zip     (2 GB) - if --include-images
├── shop-manager-standalone-v1.0.0.zip.sha256
├── shop-manager-standalone-v1.0.0.zip.md5
└── RELEASE_NOTES_v1.0.0.md
```

### 6. Verify Package

```bash
cd dist

# Verify checksum
sha256sum -c shop-manager-standalone-v1.0.0.zip.sha256

# Expected output:
# shop-manager-standalone-v1.0.0.zip: OK

# Extract and inspect
unzip -l shop-manager-standalone-v1.0.0.zip | head -20
```

---

## Building Electron Desktop Apps

### 1. Install Node Dependencies

```bash
cd standalone/electron-app
npm install
```

**Install time:** 2-5 minutes

Expected output:
```
added 450 packages in 3m
```

### 2. Update Version

```bash
# Edit package.json
nano package.json

# Or use npm version command
npm version 1.0.0 --no-git-tag-version
```

### 3. Build for All Platforms

```bash
# Build for all platforms (Windows, macOS, Linux)
npm run build:all
```

**Build time:** 10-20 minutes

**Output location:** `electron-app/dist/`

**Expected files:**
```
dist/
├── Shop Manager-Setup-1.0.0.exe           (135 MB) - Windows NSIS
├── Shop Manager-1.0.0.exe                 (130 MB) - Windows Portable
├── Shop Manager-1.0.0.dmg                 (128 MB) - macOS
├── Shop Manager-1.0.0-mac.zip             (125 MB) - macOS (auto-update)
├── Shop Manager-1.0.0.AppImage            (142 MB) - Linux
├── shop-manager_1.0.0_amd64.deb           (140 MB) - Debian/Ubuntu
└── shop-manager-1.0.0.x86_64.rpm          (145 MB) - RHEL/Fedora
```

### 4. Test Built Apps

**Windows (on Windows):**
```cmd
cd dist
Shop-Manager-Setup-1.0.0.exe
```

**macOS (on macOS):**
```bash
cd dist
open "Shop Manager-1.0.0.dmg"
```

**Linux (on Linux):**
```bash
cd dist
chmod +x "Shop Manager-1.0.0.AppImage"
./"Shop Manager-1.0.0.AppImage"
```

---

## Building for Specific Platforms

### Windows Only

```bash
cd standalone/electron-app
npm run build:win
```

**Output:**
- `Shop Manager-Setup-1.0.0.exe` - Installer
- `Shop Manager-1.0.0.exe` - Portable

**Requirements:**
- Windows 10+ (native)
- OR Linux/macOS with Wine installed

### macOS Only

```bash
cd standalone/electron-app
npm run build:mac
```

**Output:**
- `Shop Manager-1.0.0.dmg` - Disk image
- `Shop Manager-1.0.0-mac.zip` - ZIP for auto-updates

**Requirements:**
- macOS 11+ (native builds only)
- Xcode Command Line Tools
- Apple Developer account (for signing)

**Signing (Optional but Recommended):**
```bash
# Set environment variables
export APPLE_ID="your@email.com"
export APPLE_ID_PASSWORD="app-specific-password"
export TEAM_ID="ABCD1234"

# Build with signing
npm run build:mac
```

### Linux Only

```bash
cd standalone/electron-app
npm run build:linux
```

**Output:**
- `Shop Manager-1.0.0.AppImage` - Universal
- `shop-manager_1.0.0_amd64.deb` - Debian/Ubuntu
- `shop-manager-1.0.0.x86_64.rpm` - RHEL/Fedora

**Requirements:**
- Linux (any distro)
- fpm gem (for .deb and .rpm)

**Install fpm:**
```bash
sudo gem install fpm
```

---

## Advanced Build Options

### Custom Output Directory

```bash
./create-distribution.sh \
  --version 1.0.0 \
  --output /path/to/custom/dir
```

### Build Specific Platform Only

```bash
./create-distribution.sh \
  --version 1.0.0 \
  --platform windows
```

Supported platforms:
- `windows`
- `macos`
- `linux`
- `all` (default)

### Skip Docker Image Builds

If you already have images:

```bash
./create-distribution.sh \
  --version 1.0.0 \
  --skip-images
```

### Custom Electron Build Configuration

Edit `electron-app/package.json`:

```json
{
  "build": {
    "appId": "com.yourcompany.shopmanager",
    "productName": "Your Shop Manager",
    "compression": "maximum",
    "win": {
      "target": ["nsis", "portable"],
      "icon": "build/custom-icon.ico"
    }
  }
}
```

---

## Code Signing

### Windows Code Signing

**1. Get Certificate:**
- Purchase from DigiCert, Sectigo, or similar
- Cost: ~$200-500/year

**2. Sign Executable:**
```bash
# Using signtool (Windows)
signtool sign /f certificate.pfx /p password /t http://timestamp.digicert.com "Shop Manager-Setup-1.0.0.exe"

# Or set in package.json
{
  "build": {
    "win": {
      "certificateFile": "cert.pfx",
      "certificatePassword": "password"
    }
  }
}
```

### macOS Code Signing & Notarization

**1. Get Apple Developer Account:**
- Cost: $99/year
- Sign up at developer.apple.com

**2. Create App-Specific Password:**
```bash
# Visit appleid.apple.com
# Security → App-Specific Passwords → Generate

# Save credentials in keychain
xcrun notarytool store-credentials "AC_PASSWORD" \
  --apple-id "your@email.com" \
  --team-id "TEAMID1234" \
  --password "app-specific-password"
```

**3. Build with Signing:**
```bash
export CSC_LINK="/path/to/Developer ID Application.p12"
export CSC_KEY_PASSWORD="certificate-password"

npm run build:mac
```

**4. Notarize (automatically done by electron-builder):**
```json
{
  "build": {
    "mac": {
      "hardenedRuntime": true,
      "gatekeeperAssess": false,
      "entitlements": "build/entitlements.mac.plist",
      "notarize": {
        "teamId": "TEAMID1234"
      }
    }
  }
}
```

**5. Verify Notarization:**
```bash
spctl -a -vv "Shop Manager.app"
# Expected: accepted
```

---

## Testing Builds Locally

### Test Docker Compose Package

```bash
# Extract package
cd /tmp
unzip /path/to/shop-manager-standalone-v1.0.0.zip
cd shop-manager-standalone-v1.0.0

# Run installer
./install.sh

# Verify services
docker compose ps

# Access application
open http://localhost:3001
```

### Test Electron App

**Create Fresh Test Environment:**

```bash
# macOS: Create new user account
# Windows: Create new Windows VM
# Linux: Use Docker container

docker run -it --rm \
  -v /tmp/.X11-unix:/tmp/.X11-unix \
  -e DISPLAY=$DISPLAY \
  ubuntu:22.04 bash
```

**Test Installation:**
```bash
# Copy installer to test machine
scp Shop-Manager-1.0.0.dmg user@test-machine:~/

# On test machine
open Shop-Manager-1.0.0.dmg
# Drag to Applications
# Launch and test
```

### Verify Package Contents

```bash
# Docker Compose package
unzip -l shop-manager-standalone-v1.0.0.zip

# Should contain:
# - config.yaml
# - install.sh / install.bat
# - docker-compose.yml
# - scripts/
# - templates/
# - docs/

# Electron app (macOS)
pkgutil --check-signature Shop-Manager-1.0.0.dmg

# Electron app (Windows)
sigcheck.exe Shop-Manager-Setup-1.0.0.exe
```

---

## Troubleshooting

### Docker Build Fails

**Error:** `Cannot connect to Docker daemon`

**Solution:**
```bash
# Start Docker
sudo systemctl start docker  # Linux
# OR open Docker Desktop      # macOS/Windows

# Verify
docker info
```

**Error:** `Disk space full`

**Solution:**
```bash
# Clean up Docker
docker system prune -a --volumes

# Free space needed: 20GB minimum
```

### Electron Build Fails

**Error:** `ENOSPC: no space left on device`

**Solution:**
```bash
# Increase inotify watchers (Linux)
echo fs.inotify.max_user_watches=524288 | sudo tee -a /etc/sysctl.conf
sudo sysctl -p

# Or free up space
npm cache clean --force
```

**Error:** `Cannot find module 'electron'`

**Solution:**
```bash
# Reinstall dependencies
rm -rf node_modules package-lock.json
npm install
```

**Error:** `Platform not supported`

**Solution:**
```bash
# You're trying to build macOS on Linux
# Use GitHub Actions or actual macOS machine
# Or use Docker with electron-builder

docker run --rm -ti \
  --env-file <(env | grep -iE 'DEBUG|NODE_|ELECTRON_|YARN_|NPM_|CI|CIRCLE|TRAVIS_TAG|TRAVIS|TRAVIS_REPO_|TRAVIS_BUILD_|TRAVIS_BRANCH|TRAVIS_PULL_REQUEST_|APPVEYOR|CSC_|GH_|GITHUB_|BT_|AWS_|STRIP|BUILD_') \
  --env ELECTRON_CACHE="/root/.cache/electron" \
  --env ELECTRON_BUILDER_CACHE="/root/.cache/electron-builder" \
  -v ${PWD}:/project \
  -v ~/.cache/electron:/root/.cache/electron \
  -v ~/.cache/electron-builder:/root/.cache/electron-builder \
  electronuserland/builder:wine
```

### Code Signing Issues

**Error:** `Code signing failed`

**macOS Solution:**
```bash
# Verify certificate
security find-identity -v -p codesigning

# Should show: "Developer ID Application: Your Name (TEAMID)"

# If not found, import certificate
security import certificate.p12 -k ~/Library/Keychains/login.keychain
```

**Windows Solution:**
```bash
# Verify certificate
certutil -dump certificate.pfx

# Test signing manually
signtool sign /f certificate.pfx /p password /d "Shop Manager" test.exe
```

### Build is Slow

**Optimize:**
```bash
# Use faster Docker backend
export DOCKER_BUILDKIT=1

# Parallel builds
npm run build:all -- --parallel

# Use local cache
npm run build -- --cache-dir ~/.electron-builder-cache
```

---

## Build Performance Tips

### Speed Up Docker Builds

```dockerfile
# In Dockerfile, add:
# Use BuildKit cache mounts
RUN --mount=type=cache,target=/root/.m2 mvn package
RUN --mount=type=cache,target=/root/.npm npm install
```

### Speed Up Electron Builds

```json
{
  "build": {
    "compression": "normal",  // Instead of "maximum"
    "electronDownload": {
      "cache": "~/.electron-builder-cache"
    }
  }
}
```

### Parallelize Builds

```bash
# Build Docker images in parallel
docker compose build --parallel

# Build Electron apps in parallel (if multiple architectures)
npm run build:mac -- --x64 --arm64
```

---

## Continuous Integration

See `.github/workflows/build-standalone-release.yml` for automated builds.

**Manual Trigger:**
```bash
# Create and push tag
git tag v1.0.0
git push origin v1.0.0

# GitHub Actions will automatically build all platforms
```

---

## Build Checklist

Before creating a release build:

- [ ] Update version in `package.json`
- [ ] Update version in `config.yaml`
- [ ] Update `CHANGELOG.md`
- [ ] Test installation on fresh VM
- [ ] Verify all services start correctly
- [ ] Check Docker images are latest
- [ ] Run security scan (`npm audit`)
- [ ] Code signing certificates are valid
- [ ] Build artifacts are under 200MB each
- [ ] Checksums are generated
- [ ] Release notes are complete

---

## Next Steps

After building packages:
1. [Test packages](TESTING_GUIDE.md)
2. [Create GitHub release](RELEASE_PROCESS.md)
3. [Deploy to distribution channels](DEPLOYMENT_GUIDE.md)

---

## Support

- **Build Issues:** https://github.com/yourorg/shop-manager/issues
- **Slack:** #builds channel
- **Email:** dev@shopmanager.com
