# macOS Installer Build Guide

This directory contains the configuration for building a macOS disk image (.dmg) installer for Shop Manager.

## Prerequisites

1. **macOS Build Machine** (macOS 11.0+)
2. **Java JDK 21** - For running the application
3. **Xcode Command Line Tools** - For development tools
   ```bash
   xcode-select --install
   ```
4. **create-dmg** (Optional but recommended) - For advanced DMG creation
   ```bash
   brew install create-dmg
   ```
5. **Embedded JAR** - Built using `mvnw package -Pembedded`

## Directory Structure

```
installers/macos/
├── build-dmg.sh                # DMG builder script
├── scripts/
│   └── shop-manager           # macOS launcher script
├── config/
│   ├── .env.template          # Environment template
│   └── application.yml        # Spring Boot config
├── assets/
│   ├── shop-manager.icns      # Application icon (required)
│   └── dmg-background.png     # DMG background (optional)
└── README.md                  # This file
```

## Building the Installer

### Quick Start

```bash
# Make script executable
chmod +x build-dmg.sh

# Build DMG
./build-dmg.sh
```

The script will:
1. Check prerequisites (Java, embedded JAR)
2. Create macOS app bundle structure
3. Copy JAR, scripts, and configuration
4. Generate DMG installer
5. Verify DMG integrity

Output: `../../build/installers/macos/shop-manager-1.0.0-macos-x64.dmg`

### Advanced Build Options

#### Using create-dmg (Recommended)

For a polished DMG with custom background and layout:

```bash
# Install create-dmg
brew install create-dmg

# Create DMG background image (800x400px recommended)
# Place at: assets/dmg-background.png

# Build
./build-dmg.sh
```

#### Using hdiutil (Basic)

If `create-dmg` is not available, the script will fall back to `hdiutil` for basic DMG creation.

## App Bundle Structure

The script creates a standard macOS app bundle:

```
Shop Manager.app/
├── Contents/
│   ├── Info.plist              # Bundle metadata
│   ├── PkgInfo                 # Type/creator codes
│   ├── MacOS/
│   │   └── shop-manager        # Launcher script
│   └── Resources/
│       ├── shop-manager.jar    # Embedded JAR
│       ├── shop-manager.icns   # Application icon
│       ├── config/
│       │   ├── .env.template
│       │   └── application.yml
│       └── docs/
│           ├── EMBEDDED_DEPLOYMENT.md
│           ├── CLOUD_SYNC_SETUP.md
│           └── README.md
```

## Launcher Script

The launcher script (`scripts/shop-manager`) handles:

1. **Java Version Check** - Ensures Java 21+ is installed
2. **Configuration** - Creates `~/.shopmanager/.env` on first launch
3. **JWT Secret Generation** - Auto-generates secure secret
4. **Data Directories** - Creates `~/.shopmanager/data/`
5. **Process Management** - Prevents multiple instances
6. **Browser Launch** - Opens application in default browser
7. **Native Notifications** - Shows macOS notifications

## User Data Locations

After installation, Shop Manager stores data in the user's home directory:

```
~/.shopmanager/
├── .env                        # User configuration
├── shop-manager.pid            # Process ID
└── data/
    ├── h2/                     # H2 database files
    ├── uploads/                # File uploads
    ├── logs/                   # Application logs
    └── backups/                # Backup storage
```

## Assets Required

### Application Icon (shop-manager.icns)

**Required** - macOS icon set with multiple resolutions

**Create from PNG:**

```bash
# Create icon folder
mkdir -p shop-manager.iconset

# Convert PNG to various sizes
sips -z 16 16     shop-manager.png --out shop-manager.iconset/icon_16x16.png
sips -z 32 32     shop-manager.png --out shop-manager.iconset/icon_16x16@2x.png
sips -z 32 32     shop-manager.png --out shop-manager.iconset/icon_32x32.png
sips -z 64 64     shop-manager.png --out shop-manager.iconset/icon_32x32@2x.png
sips -z 128 128   shop-manager.png --out shop-manager.iconset/icon_128x128.png
sips -z 256 256   shop-manager.png --out shop-manager.iconset/icon_128x128@2x.png
sips -z 256 256   shop-manager.png --out shop-manager.iconset/icon_256x256.png
sips -z 512 512   shop-manager.png --out shop-manager.iconset/icon_256x256@2x.png
sips -z 512 512   shop-manager.png --out shop-manager.iconset/icon_512x512.png
sips -z 1024 1024 shop-manager.png --out shop-manager.iconset/icon_512x512@2x.png

# Convert to icns
iconutil -c icns shop-manager.iconset -o assets/shop-manager.icns

# Cleanup
rm -rf shop-manager.iconset
```

### DMG Background (dmg-background.png)

**Optional** - Background image for DMG window (800x400px recommended)

**Create placeholder:**

```bash
# Using ImageMagick
convert -size 800x400 -background "#f0f0f0" \
    -fill "#333333" -gravity center \
    -pointsize 48 label:"Shop Manager" \
    assets/dmg-background.png
```

## Installation Process

### For End Users

1. Download `shop-manager-1.0.0-macos-x64.dmg`
2. Double-click to mount the DMG
3. Drag "Shop Manager.app" to Applications folder
4. Eject the DMG
5. Open "Shop Manager" from Applications
6. On first launch:
   - If Java not found, prompted to install
   - Configuration generated at `~/.shopmanager/.env`
   - Application opens in browser

### First Launch

```bash
# The launcher will:
1. Check Java 21+ installation
2. Create ~/.shopmanager directory
3. Copy .env template
4. Generate secure JWT secret
5. Create data directories
6. Start application
7. Open browser to http://localhost:8081
8. Show notification when ready
```

## Code Signing (Production)

For production distribution, sign the app bundle and DMG:

### Prerequisites

- Apple Developer account ($99/year)
- Developer ID Application certificate
- Developer ID Installer certificate

### Signing Steps

```bash
# Sign the app bundle
codesign --deep --force --verify --verbose \
    --sign "Developer ID Application: Your Name (TEAM_ID)" \
    "build/Shop Manager.app"

# Verify signature
codesign --verify --deep --strict --verbose=2 \
    "build/Shop Manager.app"

# Check signature
spctl -a -vv "build/Shop Manager.app"
```

### Notarization (Required for macOS 10.15+)

```bash
# Create DMG first
./build-dmg.sh

# Sign the DMG
codesign --sign "Developer ID Application: Your Name (TEAM_ID)" \
    "../../build/installers/macos/shop-manager-1.0.0-macos-x64.dmg"

# Submit for notarization
xcrun notarytool submit \
    "../../build/installers/macos/shop-manager-1.0.0-macos-x64.dmg" \
    --apple-id "your-apple-id@example.com" \
    --team-id "TEAM_ID" \
    --password "app-specific-password" \
    --wait

# Staple notarization ticket
xcrun stapler staple \
    "../../build/installers/macos/shop-manager-1.0.0-macos-x64.dmg"

# Verify notarization
xcrun stapler validate \
    "../../build/installers/macos/shop-manager-1.0.0-macos-x64.dmg"
```

**See:** [Apple Notarization Guide](https://developer.apple.com/documentation/security/notarizing_macos_software_before_distribution)

## Testing the Installer

### Pre-Installation Test

```bash
# Verify embedded JAR exists
ls -lh ../../backend/target/shop-manager-*-embedded.jar

# Build DMG
./build-dmg.sh

# Verify DMG created
ls -lh ../../build/installers/macos/shop-manager-*.dmg
```

### Post-Installation Test

```bash
# Mount DMG
open ../../build/installers/macos/shop-manager-1.0.0-macos-x64.dmg

# Copy to Applications (or test from DMG)
cp -R "/Volumes/Shop Manager 1.0.0/Shop Manager.app" /Applications/

# Launch application
open -a "Shop Manager"

# Wait 30 seconds for startup

# Verify health
curl http://localhost:8081/actuator/health

# Should return: {"status":"UP"}
```

### Check Logs

```bash
# View application logs
tail -f ~/.shopmanager/data/logs/shop-manager.log

# Check PID
cat ~/.shopmanager/shop-manager.pid

# Verify process running
ps aux | grep shop-manager
```

## Uninstallation

Users can uninstall by:

1. Quit Shop Manager (if running)
2. Delete application: `sudo rm -rf /Applications/Shop\ Manager.app`
3. (Optional) Delete user data: `rm -rf ~/.shopmanager`

**Uninstall Script:**

```bash
#!/bin/bash
# Stop application
pkill -f "shop-manager.jar" || true

# Remove application
sudo rm -rf "/Applications/Shop Manager.app"

# Remove user data (optional)
read -p "Remove user data? (y/N): " remove_data
if [[ $remove_data =~ ^[Yy]$ ]]; then
    rm -rf ~/.shopmanager
    echo "User data removed"
fi

echo "Shop Manager uninstalled"
```

## Troubleshooting

### Java Not Found

**Error:** Dialog showing "Java 21 or higher is required"

**Solution:**
```bash
# Install Java 21 using Homebrew
brew install openjdk@21

# Or download from Adoptium
open https://adoptium.net/temurin/releases/?version=21
```

### Port Already in Use

**Error:** Application fails to start, port 8081 in use

**Solution:**
```bash
# Edit configuration
nano ~/.shopmanager/.env

# Change port
BACKEND_PORT=8082

# Relaunch application
open -a "Shop Manager"
```

### Application Already Running

**Error:** Dialog showing "Shop Manager is already running"

**Solution:**
```bash
# Kill existing process
pkill -f "shop-manager.jar"

# Remove PID file
rm ~/.shopmanager/shop-manager.pid

# Relaunch
open -a "Shop Manager"
```

### Permission Denied

**Error:** Application can't write to data directory

**Solution:**
```bash
# Fix permissions
chmod 755 ~/.shopmanager
chmod 755 ~/.shopmanager/data
chmod -R 644 ~/.shopmanager/data/*
```

### Gatekeeper Warning

**Error:** "Shop Manager.app can't be opened because it is from an unidentified developer"

**Solution (Development Only):**
```bash
# Remove quarantine attribute
xattr -rd com.apple.quarantine "/Applications/Shop Manager.app"

# Or use System Preferences
# System Preferences > Security & Privacy > General > "Open Anyway"
```

**Production:** Sign and notarize the application (see Code Signing section)

## Building from CI/CD

The GitHub Actions workflow will build macOS installer on macOS runners.

See: `.github/workflows/build-standalone-release.yml`

```yaml
build-macos-dmg:
  runs-on: macos-latest
  steps:
    - name: Install create-dmg
      run: brew install create-dmg

    - name: Build DMG
      run: |
        cd installers/macos
        chmod +x build-dmg.sh
        ./build-dmg.sh

    - name: Upload DMG
      uses: actions/upload-artifact@v3
      with:
        name: macos-dmg
        path: build/installers/macos/*.dmg
```

## Distribution

### GitHub Releases

Upload DMG to GitHub releases:

```bash
gh release upload v1.0.0 \
    build/installers/macos/shop-manager-1.0.0-macos-x64.dmg
```

### Homebrew Cask (Optional)

Create Homebrew formula for easy installation:

```ruby
cask "shop-manager" do
  version "1.0.0"
  sha256 "checksum-here"

  url "https://github.com/yourorg/shop-manager/releases/download/v#{version}/shop-manager-#{version}-macos-x64.dmg"
  name "Shop Manager"
  desc "Retail management platform"
  homepage "https://github.com/yourorg/shop-manager"

  app "Shop Manager.app"

  zap trash: [
    "~/.shopmanager",
  ]
end
```

## License

See [LICENSE](../../LICENSE) file in project root.

## Support

- **Documentation**: [docs/EMBEDDED_DEPLOYMENT.md](../../docs/EMBEDDED_DEPLOYMENT.md)
- **Issues**: https://github.com/yourorg/shop-manager/issues
- **Email**: support@shopmanager.com

---

**Last Updated**: 2025-12-24
