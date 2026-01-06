# Shop Manager - Installer Features Guide

This guide documents all features implemented in Shop Manager platform installers (Windows, macOS, Linux).

---

## 📋 Table of Contents

- [Installation Modes](#installation-modes)
- [Default Credentials](#default-credentials)
- [mDNS Support](#mdns-support)
- [Cloud Sync Setup](#cloud-sync-setup)
- [Platform-Specific Features](#platform-specific-features)
- [Post-Installation Configuration](#post-installation-configuration)
- [Security Considerations](#security-considerations)

---

## Installation Modes

Shop Manager supports two deployment modes:

### Embedded Mode (These Installers)
- **What it is**: Standalone, all-in-one native application
- **Components**: Embedded PostgreSQL + Local JWT authentication + Frontend
- **Best for**: Single shop, offline-capable, simple deployment
- **No Docker required**: Runs as native executable
- **Database**: Embedded PostgreSQL (port 5433)
- **Size**: 120-130 MB per installer

### Cloud Mode (Docker Deployment)
- **What it is**: Full-featured deployment with Keycloak SSO
- **Components**: PostgreSQL + Keycloak + Kafka + MinIO (Docker Compose)
- **Best for**: Multi-shop, centralized authentication, cloud deployment
- **Requires**: Docker and Docker Compose
- **See**: `DEPLOYMENT_GUIDE.md` for Docker deployment

---

## Default Credentials

After installation, login with:

**Username:** `superadmin`
**Email:** `superadmin@retailhq.local`
**Password:** `changeme`

⚠️ **CRITICAL SECURITY**: Change this password immediately after first login!

**Default URLs:**
- Frontend: `http://localhost:3001`
- Backend API: `http://localhost:8081`
- With mDNS: `http://shopmanager.local` (no port needed)

---

## mDNS Support

### What is mDNS?

mDNS (Multicast DNS) enables professional `.local` domain names on your local network without requiring DNS configuration.

### Features

- **Hostname**: `shopmanager.local` (configurable during installation)
- **Service Discovery**: Automatic network discovery for other devices
- **Port-less URLs**: Access via `http://shopmanager.local` instead of `http://localhost:8081`
- **Protocols**: Published on `_http._tcp.local`

### Configuration

**During Installation:**
- Windows/macOS: Wizard prompts for hostname and shop name
- Linux: Configured in .env file

**After Installation:**
Edit `.env` file:
```properties
# mDNS Configuration
SHOP_HOSTNAME=shopmanager.local
SHOP_NAME=Shop Manager
MDNS_ENABLED=true
```

### Accessing the Application

**With mDNS:**
- `http://shopmanager.local` → Frontend
- `http://api.shopmanager.local` → Backend API

**Traditional:**
- `http://localhost:3001` → Frontend
- `http://localhost:8081` → Backend API

### Network Discovery

Other devices on the same network can discover and access Shop Manager using the configured hostname.

---

## Cloud Sync Setup

### Overview

Cloud sync allows embedded installations to sync transaction data to a cloud aggregator for:
- Multi-shop analytics and reporting
- Centralized data aggregation
- Business intelligence dashboards
- Offline-first operation with periodic sync

### Setup During Installation (Windows)

The Windows installer includes a cloud sync configuration wizard with three options:

#### Option 1: Standalone Mode (Default)
- **Description**: No cloud sync, fully offline operation
- **Best for**: Single shop, no multi-location needs
- **Configuration**: None required

#### Option 2: Register New Account
- **Description**: Automatically create cloud account during first application launch
- **How it works**:
  - Installer sets `CLOUD_API_KEY=AUTO_REGISTER` in .env
  - On first run, application calls cloud registration API
  - API key auto-generated and saved
- **User provides**: Nothing (uses tenant/shop bootstrap data)
- **Best for**: New installations with no existing cloud account

#### Option 3: Use Existing API Key
- **Description**: Link to existing cloud aggregator account
- **How it works**:
  - User provides existing API key during installation
  - Installer saves API key to .env file
  - Application connects immediately on first run
- **User provides**:
  - API Key (format: `rhq_...`)
  - Cloud API URL (optional, defaults to `https://api.retailhq.app`)
- **Best for**: Additional shops linking to existing cloud account

### Setup After Installation

All platforms support post-installation cloud sync configuration via Settings UI:

**Location**: Settings → Cloud Sync (requires SYSTEM_ADMIN or TENANT_ADMIN role)

**Features:**
- Enable/disable cloud sync
- Register new account (button triggers backend `/api/cloud-sync/register`)
- Update API key
- Test connection
- Manual sync trigger
- View last sync status
- Configure sync frequency

### Cloud Sync API Endpoints

Already fully implemented in backend:

```
GET    /api/cloud-sync/config       - Get current configuration
GET    /api/cloud-sync/status       - Get sync status and health
POST   /api/cloud-sync/register     - Register new cloud account
PUT    /api/cloud-sync/config       - Update configuration
POST   /api/cloud-sync/enable       - Enable sync
POST   /api/cloud-sync/disable      - Disable sync
POST   /api/cloud-sync/sync         - Trigger manual sync
DELETE /api/cloud-sync/unregister   - Unregister from cloud
```

### Configuration Files

**.env Template (All Platforms):**
```properties
# Cloud Sync Configuration (Optional)
CLOUD_REGISTRATION_URL=https://api.retailhq.app
CLOUD_API_KEY=
CLOUD_SYNC_REQUIRED=false
CLOUD_ALLOW_OFFLINE=true
CLOUD_SYNC_CRON=0 0 * * * ?
CLOUD_SYNC_BATCH_SIZE=1000
```

### Offline Mode

- **Enabled by default**: `CLOUD_ALLOW_OFFLINE=true`
- **Behavior**: Application functions normally if cloud unreachable
- **Queue**: Transactions queued for next successful sync
- **Retry**: Automatic retry with exponential backoff

---

## Platform-Specific Features

### Windows (.exe Installer)

**Installation Wizard:**
1. Java version detection (Java 21+)
2. Java download prompt if not installed
3. Hostname and shop name configuration
4. Port configuration (backend: 8081, frontend: 3001)
5. JWT secret auto-generation
6. **Cloud sync configuration (NEW)**

**Features:**
- ✅ Desktop icon and Start Menu shortcuts
- ✅ Quick Launch shortcut (optional)
- ✅ Auto-start on Windows boot (optional)
- ✅ Configuration editor shortcut (opens Notepad)
- ✅ Data folder shortcut
- ✅ Documentation shortcut
- ✅ Windows Service installation scripts included
- ✅ Uninstaller included

**Installation Location:**
```
C:\Program Files\Shop Manager\
├── lib\
│   └── shop-manager-{version}-embedded.jar
├── config\
│   ├── .env
│   ├── .env.template
│   ├── application.yml
│   └── nginx.conf.template
├── scripts\
│   ├── shop-manager.bat
│   ├── shop-manager-console.bat
│   ├── install-service.bat
│   └── uninstall-service.bat
├── data\
│   ├── postgres\
│   ├── uploads\
│   ├── logs\
│   └── backups\
└── docs\
```

**Service Installation:**
```powershell
cd "C:\Program Files\Shop Manager"
.\install-service.bat
```

---

### macOS (.dmg Installer)

**Installation:**
- Standard macOS app bundle
- Drag-to-Applications installer
- Native notifications support
- launchd service support

**Features:**
- ✅ Standard macOS .app bundle
- ✅ Code signing (when certificate available)
- ✅ Gatekeeper compatible
- ✅ Launch Agent for auto-start
- ✅ Native macOS notifications

**Installation Location:**
```
/Applications/Shop Manager.app/
└── Contents/
    ├── MacOS/
    │   └── shop-manager (launcher script)
    ├── Resources/
    │   ├── lib/
    │   │   └── shop-manager-{version}-embedded.jar
    │   ├── config/
    │   │   ├── .env.template
    │   │   ├── application.yml
    │   │   └── nginx.conf.template
    │   └── docs/
    └── Info.plist
```

**User Data:**
```
~/.shopmanager/
├── .env
├── data/
│   ├── postgres/
│   ├── uploads/
│   ├── logs/
│   └── backups/
└── shop-manager.pid
```

**Launch Agent Installation:**
```bash
# Create launch agent
cat > ~/Library/LaunchAgents/com.princely.shopmanager.plist <<EOF
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE plist PUBLIC "-//Apple//DTD PLIST 1.0//EN" "http://www.apple.com/DTDs/PropertyList-1.0.dtd">
<plist version="1.0">
<dict>
    <key>Label</key>
    <string>com.princely.shopmanager</string>
    <key>ProgramArguments</key>
    <array>
        <string>/Applications/Shop Manager.app/Contents/MacOS/shop-manager</string>
    </array>
    <key>RunAtLoad</key>
    <true/>
</dict>
</plist>
EOF

# Load service
launchctl load ~/Library/LaunchAgents/com.princely.shopmanager.plist
```

---

### Linux (.deb, .rpm, AppImage)

**Package Types:**
- **Debian/Ubuntu**: `.deb` package (dpkg)
- **RHEL/Fedora**: `.rpm` package (yum/dnf)
- **Universal**: AppImage tarball (portable)

**Features:**
- ✅ Systemd service integration
- ✅ Desktop entry (application menu)
- ✅ Automatic user/group creation (`shopmanager` user)
- ✅ JWT secret auto-generation
- ✅ Firewall configuration guidance
- ✅ Uninstall scripts

**Installation Locations:**

**System Installation (.deb/.rpm):**
```
/opt/shop-manager/
├── lib/
│   └── shop-manager.jar
└── bin/
    └── shop-manager

/etc/shop-manager/
├── shop-manager.env
└── application.yml

/var/lib/shop-manager/
└── data/
    ├── postgres/
    ├── uploads/
    ├── logs/
    └── backups/

/lib/systemd/system/
└── shop-manager.service
```

**User Installation (AppImage):**
```
~/.shopmanager/
├── shop-manager.env
├── shop-manager.pid
└── data/
    ├── postgres/
    ├── uploads/
    ├── logs/
    └── backups/
```

**Service Management:**
```bash
# Start service
sudo systemctl start shop-manager

# Enable auto-start
sudo systemctl enable shop-manager

# Check status
sudo systemctl status shop-manager

# View logs
sudo journalctl -u shop-manager -f
```

---

## Post-Installation Configuration

### Configuration File Locations

| Platform | Configuration File |
|----------|-------------------|
| Windows | `C:\Program Files\Shop Manager\config\.env` |
| macOS | `~/.shopmanager/.env` |
| Linux (system) | `/etc/shop-manager/shop-manager.env` |
| Linux (user) | `~/.shopmanager/shop-manager.env` |

### Common Configuration Options

```properties
# Server Ports
BACKEND_PORT=8081
FRONTEND_PORT=3001

# Hostname (mDNS)
SHOP_HOSTNAME=shopmanager.local
SHOP_NAME=My Shop

# Database
POSTGRES_PORT=5433
POSTGRES_DATABASE=shopmanager
POSTGRES_DATA_DIR=./data/postgres

# JWT Security
JWT_SECRET=<auto-generated-secure-secret>
JWT_EXPIRATION_MS=86400000

# Cloud Sync
CLOUD_REGISTRATION_URL=https://api.retailhq.app
CLOUD_API_KEY=
CLOUD_SYNC_REQUIRED=false
CLOUD_ALLOW_OFFLINE=true

# Bootstrap (First Run)
BOOTSTRAP_TENANT_ENABLED=true
BOOTSTRAP_SHOP_ENABLED=true
BOOTSTRAP_SUPERADMIN_ENABLED=true
SUPERADMIN_USERNAME=superadmin
SUPERADMIN_PASSWORD=changeme

# Logging
LOG_LEVEL=INFO
```

### Restart After Configuration Changes

**Windows:**
```powershell
# If running as application
taskkill /F /IM java.exe
# Restart from Start Menu

# If running as service
net stop shop-manager
net start shop-manager
```

**macOS:**
```bash
# If running as application
pkill -f "shop-manager.jar"
# Restart from Applications

# If running as Launch Agent
launchctl stop com.princely.shopmanager
launchctl start com.princely.shopmanager
```

**Linux:**
```bash
sudo systemctl restart shop-manager
```

---

## Security Considerations

### Critical Security Steps

1. **Change Default Password**
   - Login immediately after installation
   - Navigate to Settings → User Profile
   - Change password for `superadmin` account

2. **Secure JWT Secret**
   - Auto-generated during installation (64-byte random Base64)
   - Keep .env file permissions restricted
   - Never commit to version control

3. **Cloud API Key Protection**
   - API keys are sensitive credentials
   - Store securely in .env file
   - Rotate keys periodically via cloud portal
   - Never share or commit to version control

4. **Firewall Configuration**
   - Restrict backend port (8081) to localhost if not needed externally
   - Allow frontend port (3001) only for trusted networks
   - Use reverse proxy (nginx) for HTTPS in production

5. **Database Security**
   - Embedded PostgreSQL binds to localhost only (port 5433)
   - Data encryption at rest (configure if needed)
   - Regular backups to `data/backups/`

### File Permissions

**Windows:**
- Configuration files: Read-only for standard users
- Service runs as SYSTEM account
- Data directory: Full control for SYSTEM

**macOS/Linux:**
- Configuration files: `600` (owner read/write only)
- Service user: `shopmanager` (system user)
- Data directory: `700` (owner full control)

---

## Troubleshooting

### Installation Issues

**"Java 21 not found"**
- Windows: Download from https://adoptium.net/temurin/releases/?version=21
- macOS: `brew install openjdk@21`
- Linux: `sudo apt-get install openjdk-21-jre-headless`

**"Port already in use"**
```bash
# Check what's using port 8081
netstat -ano | findstr :8081  # Windows
lsof -i :8081                  # macOS/Linux

# Change port in .env
BACKEND_PORT=8082
```

**"Application won't start"**
- Check logs:
  - Windows: `C:\Program Files\Shop Manager\data\logs\shop-manager.log`
  - macOS: `~/.shopmanager/data/logs/shop-manager.log`
  - Linux: `/var/lib/shop-manager/data/logs/shop-manager.log` or `journalctl -u shop-manager`

### mDNS Issues

**"Cannot access shopmanager.local"**
1. Verify mDNS enabled: `MDNS_ENABLED=true` in .env
2. Check hostname: `SHOP_HOSTNAME=shopmanager.local`
3. Restart application
4. Test with ping:
   - Windows: `ping shopmanager.local`
   - macOS/Linux: `ping shopmanager.local`

**"mDNS conflicts with existing service"**
- Change hostname in .env to avoid conflicts
- Use different `.local` domain name

### Cloud Sync Issues

**"Cloud registration failed"**
1. Verify cloud API URL is correct
2. Check internet connectivity
3. Verify firewall allows outbound HTTPS (port 443)
4. Check backend logs for detailed error

**"Invalid API key format"**
- API keys must start with `rhq_`
- Obtain valid key from cloud portal
- Check for typos or extra spaces

**"Sync failing repeatedly"**
1. Test connection: Settings → Cloud Sync → Test Connection
2. Verify API key is still valid (not revoked)
3. Check last error in sync status
4. Disable and re-enable sync if needed

---

## Support

- **Documentation**: [docs/](../docs/)
- **Issues**: https://github.com/yourorg/shop-manager/issues
- **Cloud Portal**: https://cloud.retailhq.app

---

**Last Updated**: 2026-01-06
