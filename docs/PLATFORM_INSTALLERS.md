# Platform-Specific Installers Guide

## Overview

Shop Manager provides **three deployment options** to suit different use cases and technical expertise levels:

1. **Embedded JAR** - Single executable JAR file (500-700 MB RAM)
2. **Docker Compose Lite** - Lightweight containers (1-1.5 GB RAM)
3. **Platform-Specific Installers** - Native installers for Windows, macOS, and Linux

This guide covers all three options with detailed installation instructions for each platform.

---

## Deployment Options Comparison

| Option | RAM Usage | Setup Time | Complexity | Best For |
|--------|-----------|------------|------------|----------|
| **Embedded JAR** | 500 MB | 5 min | ⭐ Very Easy | Individual PCs, laptops, minimal resources |
| **Docker Lite** | 1-1.5 GB | 10 min | ⭐⭐ Easy | Containerized deployment, easy updates |
| **Platform Installers** | 600-800 MB | 2 min | ⭐ Very Easy | End users, non-technical staff, system integration |

---

## Option 1: Embedded JAR (All Platforms)

###  Download

Download the embedded JAR from GitHub releases:
```
shop-manager-1.0.0-SNAPSHOT-embedded.jar
```

### System Requirements

- **Java 21+** (OpenJDK or Oracle JDK)
- **RAM**: 1 GB minimum, 2 GB recommended
- **Disk**: 2 GB free space

### Installation

#### Step 1: Install Java 21

**Windows:**
```powershell
# Download from Adoptium
https://adoptium.net/temurin/releases/?version=21

# Verify installation
java -version
```

**macOS:**
```bash
# Using Homebrew
brew install openjdk@21

# Verify installation
java -version
```

**Linux:**
```bash
# Ubuntu/Debian
sudo apt-get update
sudo apt-get install openjdk-21-jre-headless

# RHEL/Fedora
sudo yum install java-21-openjdk-headless

# Verify installation
java -version
```

#### Step 2: Create Configuration

Create a `.env` file in the same directory as the JAR:

```bash
# Port Configuration
BACKEND_PORT=8081

# JWT Authentication
JWT_SECRET=$(openssl rand -base64 64)

# Cloud Sync (Optional)
CLOUD_SYNC_ENABLED=false
CLOUD_API_URL=
CLOUD_API_KEY=
STORE_ID=

# JVM Memory
JAVA_OPTS=-Xms256m -Xmx512m -XX:+UseG1GC
```

#### Step 3: Run the Application

**Windows:**
```powershell
java -jar shop-manager-1.0.0-SNAPSHOT-embedded.jar
```

**macOS/Linux:**
```bash
java -jar shop-manager-1.0.0-SNAPSHOT-embedded.jar
```

#### Step 4: Access the Application

Open browser: http://localhost:8081

**Default credentials:**
- Username: `admin`
- Password: `admin`

### Advanced: Run as Service

**Windows (NSSM):**
```powershell
# Install NSSM
choco install nssm

# Create service
nssm install ShopManager "C:\Program Files\Java\jdk-21\bin\java.exe" "-jar C:\ShopManager\shop-manager-1.0.0-SNAPSHOT-embedded.jar"

# Start service
nssm start ShopManager
```

**Linux (systemd):**
```bash
# Create service file
sudo nano /etc/systemd/system/shop-manager.service

# Add content:
[Unit]
Description=Shop Manager
After=network.target

[Service]
User=shopmanager
WorkingDirectory=/opt/shop-manager
ExecStart=/usr/bin/java -jar /opt/shop-manager/shop-manager.jar
Restart=on-failure

[Install]
WantedBy=multi-user.target

# Enable and start
sudo systemctl enable shop-manager
sudo systemctl start shop-manager
```

**macOS (launchd):**
```bash
# Create plist file
nano ~/Library/LaunchAgents/com.princely.shopmanager.plist

# Add content:
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE plist PUBLIC "-//Apple//DTD PLIST 1.0//EN">
<plist version="1.0">
<dict>
    <key>Label</key>
    <string>com.princely.shopmanager</string>
    <key>ProgramArguments</key>
    <array>
        <string>/usr/bin/java</string>
        <string>-jar</string>
        <string>/Applications/ShopManager/shop-manager.jar</string>
    </array>
    <key>RunAtLoad</key>
    <true/>
</dict>
</plist>

# Load service
launchctl load ~/Library/LaunchAgents/com.princely.shopmanager.plist
```

---

## Option 2: Docker Compose Lite

### Download

Download Docker Compose Lite package from GitHub releases:
```
shop-manager-docker-lite-20251224.tar.gz
```

### System Requirements

- **Docker Desktop** (Windows/macOS) or **Docker Engine** (Linux)
- **RAM**: 4 GB minimum
- **Disk**: 5 GB free space

### Installation

#### Quick Start

```bash
# Extract package
tar -xzf shop-manager-docker-lite-20251224.tar.gz
cd shop-manager-docker-lite

# Run initialization script
./lite-init.sh

# Start services
docker compose -f docker-compose-lite.yml --env-file .env.lite up -d
```

#### Access the Application

- **Frontend**: http://localhost:3001
- **Backend API**: http://localhost:8081
- **Health Check**: http://localhost:8081/actuator/health

### Management Commands

```bash
# View logs
docker compose -f docker-compose-lite.yml logs -f

# Stop services
docker compose -f docker-compose-lite.yml down

# Restart services
docker compose -f docker-compose-lite.yml restart

# Check status
docker compose -f docker-compose-lite.yml ps
```

### Documentation

- [Docker Lite Deployment Guide](./DOCKER_LITE_DEPLOYMENT.md)
- [Docker Lite Quick Start](./DOCKER_LITE_QUICKSTART.md)

---

## Option 3: Platform-Specific Installers

### Windows Installer (.exe)

#### Download

```
shop-manager-1.0.0-windows-x64-setup.exe
```

#### Installation Steps

1. **Download the installer**
2. **Right-click** → **Run as Administrator**
3. **Follow the setup wizard**:
   - Accept license agreement
   - Choose installation directory (default: `C:\Program Files\Shop Manager`)
   - Select additional tasks:
     - ✅ Create desktop icon
     - ✅ Create Start Menu shortcuts
     - ☐ Start automatically with Windows
4. **Java Detection**:
   - If Java 21+ not found, you'll be prompted to download it
   - Installer will wait for you to install Java
5. **Configuration**:
   - Installer auto-generates secure JWT secret
   - Default ports: Backend 8081, Frontend 3001
6. **Click Install**
7. **Finish** - Option to launch Shop Manager

#### Post-Installation

**Launch Options:**
- **Desktop Icon**: Double-click "Shop Manager"
- **Start Menu**: Search "Shop Manager"
- **Console Mode**: Start Menu → "Shop Manager (Console)" for debugging

**Configuration:**
Edit `C:\Program Files\Shop Manager\config\.env`

**Uninstall:**
Control Panel → Programs → Uninstall Shop Manager

#### Run as Windows Service

```powershell
# Navigate to installation directory
cd "C:\Program Files\Shop Manager"

# Install service (requires Administrator)
.\install-service.bat

# Service management
sc start ShopManager
sc stop ShopManager
sc query ShopManager
```

#### Troubleshooting

**Error: "Java not found"**
```powershell
# Download Java 21
https://adoptium.net/temurin/releases/?version=21

# Verify
java -version
```

**Error: "Port 8081 already in use"**
```powershell
# Edit configuration
notepad "C:\Program Files\Shop Manager\config\.env"

# Change BACKEND_PORT=8082
# Restart application
```

---

### macOS Installer (.dmg)

#### Download

```
shop-manager-1.0.0-macos-x64.dmg
```

#### Installation Steps

1. **Download the DMG file**
2. **Double-click to mount**
3. **Drag "Shop Manager.app" to Applications folder**
4. **Eject the DMG**
5. **First Launch**:
   - Open **Applications** folder
   - **Right-click** on "Shop Manager.app" → **Open**
   - Click **"Open"** in the security dialog
   - Application will open in browser

**OR use Terminal to bypass Gatekeeper:**
```bash
xattr -cr /Applications/Shop\ Manager.app
open -a "Shop Manager"
```

#### Post-Installation

**Data Location:**
```
~/.shopmanager/
├── .env                    # Configuration
├── shop-manager.pid        # Process ID
└── data/
    ├── h2/                 # Database
    ├── uploads/            # File uploads
    ├── logs/               # Application logs
    └── backups/            # Backups
```

**Configuration:**
```bash
nano ~/.shopmanager/.env
```

**Uninstall:**
```bash
# Stop application
pkill -f "shop-manager.jar"

# Remove application
sudo rm -rf /Applications/Shop\ Manager.app

# Remove user data (optional)
rm -rf ~/.shopmanager
```

#### Troubleshooting

**Error: "Shop Manager.app is damaged"**

This is a macOS Gatekeeper security feature, not actual damage.

**Fix:**
```bash
xattr -cr /Applications/Shop\ Manager.app
```

**Error: "Java not found"**
```bash
# Install Java 21
brew install openjdk@21

# Verify
java -version
```

---

### Linux Packages (.deb, .rpm, AppImage)

#### Downloads

- **Debian/Ubuntu**: `shop-manager_1.0.0_all.deb`
- **RHEL/Fedora**: `shop-manager-1.0.0-1.*.rpm`
- **AppImage**: `shop-manager-1.0.0-x86_64.AppImage.tar.gz`

#### Debian/Ubuntu Installation

```bash
# Install package
sudo dpkg -i shop-manager_1.0.0_all.deb

# Install dependencies if missing
sudo apt-get install -f

# Start service
sudo systemctl start shop-manager

# Enable auto-start
sudo systemctl enable shop-manager

# Check status
sudo systemctl status shop-manager
```

#### RHEL/Fedora Installation

```bash
# Install package
sudo rpm -i shop-manager-1.0.0-1.*.rpm

# Or with yum
sudo yum localinstall shop-manager-1.0.0-1.*.rpm

# Start service
sudo systemctl start shop-manager

# Enable auto-start
sudo systemctl enable shop-manager

# Check status
sudo systemctl status shop-manager
```

#### AppImage Installation

```bash
# Extract tarball
tar -xzf shop-manager-1.0.0-x86_64.AppImage.tar.gz

# Run launcher
./shop-manager.AppDir/usr/bin/shop-manager

# Or run AppRun
chmod +x shop-manager.AppDir/AppRun
./shop-manager.AppDir/AppRun
```

#### Post-Installation

**System Installation Locations:**
```
/opt/shop-manager/              # Application files
/etc/shop-manager/              # Configuration
/var/lib/shop-manager/data/     # Data directory
/lib/systemd/system/            # Systemd service
```

**Configuration:**
```bash
# System installation
sudo nano /etc/shop-manager/shop-manager.env

# User installation (AppImage)
nano ~/.shopmanager/shop-manager.env
```

**Service Management:**
```bash
# Start
sudo systemctl start shop-manager

# Stop
sudo systemctl stop shop-manager

# Restart
sudo systemctl restart shop-manager

# View logs
sudo journalctl -u shop-manager -f
```

**Uninstall:**

**Debian/Ubuntu:**
```bash
sudo systemctl stop shop-manager
sudo dpkg -r shop-manager
# Remove data (optional)
sudo rm -rf /var/lib/shop-manager
```

**RHEL/Fedora:**
```bash
sudo systemctl stop shop-manager
sudo rpm -e shop-manager
# Remove data (optional)
sudo rm -rf /var/lib/shop-manager
```

**AppImage:**
```bash
pkill -f "shop-manager.jar"
rm -rf shop-manager.AppDir
rm -rf ~/.shopmanager  # User data
```

#### Troubleshooting

**Error: "Java not found"**
```bash
# Ubuntu/Debian
sudo apt-get update
sudo apt-get install openjdk-21-jre-headless

# RHEL/Fedora
sudo yum install java-21-openjdk-headless

# Verify
java -version
```

**Error: "Service failed to start"**
```bash
# Check logs
sudo journalctl -u shop-manager -n 50

# Check permissions
sudo chown -R shopmanager:shopmanager /var/lib/shop-manager
sudo chmod 600 /etc/shop-manager/shop-manager.env

# Restart
sudo systemctl restart shop-manager
```

---

## Cloud Sync Configuration

All deployment options support cloud sync. Edit configuration file:

```bash
CLOUD_SYNC_ENABLED=true
CLOUD_API_URL=https://cloud.shopmanager.com
CLOUD_API_KEY=sk_live_abc123...
STORE_ID=STORE-001
SYNC_CRON=0 0 * * * ?  # Hourly
ANONYMIZE_PII=true
```

**Documentation:** [Cloud Sync Setup Guide](./CLOUD_SYNC_SETUP.md)

---

## Performance Tuning

### Memory Settings

**Low Memory (2-4 GB RAM):**
```bash
JAVA_OPTS=-Xms128m -Xmx256m -XX:+UseSerialGC
```

**Normal (4-8 GB RAM):**
```bash
JAVA_OPTS=-Xms256m -Xmx512m -XX:+UseG1GC
```

**High Memory (8+ GB RAM):**
```bash
JAVA_OPTS=-Xms512m -Xmx1g -XX:+UseG1GC -XX:ParallelGCThreads=4
```

---

## Support

### Documentation

- [Embedded Deployment Guide](./EMBEDDED_DEPLOYMENT.md)
- [Docker Lite Deployment](./DOCKER_LITE_DEPLOYMENT.md)
- [Cloud Sync Setup](./CLOUD_SYNC_SETUP.md)

### Community

- **GitHub Issues**: https://github.com/yourorg/shop-manager/issues
- **Forum**: https://forum.shopmanager.com
- **Email**: support@shopmanager.com

---

## Comparison Summary

### When to Use Each Option

**Embedded JAR:**
- ✅ Individual PCs/laptops
- ✅ Minimal resource usage
- ✅ No Docker required
- ✅ Manual updates
- ❌ No containerization

**Docker Compose Lite:**
- ✅ Containerized deployment
- ✅ Easy updates via Docker
- ✅ Process isolation
- ✅ Cross-platform consistency
- ❌ Requires Docker knowledge

**Platform Installers:**
- ✅ End users / non-technical staff
- ✅ System integration (services, desktop icons)
- ✅ Uninstaller provided
- ✅ Native look and feel
- ❌ Platform-specific

---

**Last Updated**: 2025-12-24
