# Docker Compose Lite - Windows Setup Guide

Complete guide for deploying Shop Manager using Docker Compose Lite on Windows 10/11.

---

## Prerequisites

### System Requirements
- **OS**: Windows 10 Pro/Enterprise/Education (Build 19044+) or Windows 11
- **RAM**: 4 GB free (8 GB recommended)
- **Storage**: 10 GB free space
- **Virtualization**: Hyper-V or WSL2 enabled

### Required Software
1. **Docker Desktop for Windows**
   - Download: https://www.docker.com/products/docker-desktop/
   - Includes Docker Compose V2

---

## Installation Steps

### Step 1: Install Docker Desktop

1. **Download Docker Desktop** from [docker.com](https://www.docker.com/products/docker-desktop/)

2. **Run the installer** (`Docker Desktop Installer.exe`)
   - Check: "Use WSL 2 instead of Hyper-V" (recommended)
   - Check: "Add shortcut to desktop"

3. **Enable WSL 2** (if not already enabled):
   ```powershell
   # Run as Administrator
   wsl --install
   wsl --set-default-version 2
   ```

4. **Restart computer** when prompted

5. **Start Docker Desktop** from Start Menu

6. **Verify installation**:
   ```powershell
   docker --version
   docker compose version
   ```

### Step 2: Download Shop Manager Package

**Option A: From GitHub Releases** (Recommended)
```powershell
# Download latest release
Invoke-WebRequest -Uri "https://github.com/yourorg/shop-manager/releases/latest/download/shop-manager-docker-lite-windows.zip" -OutFile "shop-manager-docker-lite.zip"

# Extract
Expand-Archive -Path "shop-manager-docker-lite.zip" -DestinationPath "C:\ShopManager"
```

**Option B: Clone Repository**
```powershell
git clone https://github.com/yourorg/shop-manager.git C:\ShopManager
cd C:\ShopManager
```

### Step 3: Configure Environment

1. **Navigate to installation directory**:
   ```powershell
   cd C:\ShopManager
   ```

2. **Copy environment template**:
   ```powershell
   Copy-Item .env.lite.template .env.lite
   ```

3. **Generate JWT Secret**:
   ```powershell
   # Option 1: Using OpenSSL (if installed)
   openssl rand -base64 64

   # Option 2: Using PowerShell
   $bytes = New-Object Byte[] 32
   [Security.Cryptography.RandomNumberGenerator]::Create().GetBytes($bytes)
   [Convert]::ToBase64String($bytes)
   ```

4. **Edit `.env.lite`** in Notepad:
   ```powershell
   notepad .env.lite
   ```

   Update the JWT secret:
   ```ini
   JWT_SECRET=<paste-generated-secret-here>
   ```

### Step 4: Build Embedded JAR (First Time Only)

If you cloned from source, build the JAR:

```powershell
cd backend
.\mvnw.cmd clean package -Pembedded -DskipTests
cd ..
```

**Note**: Pre-built releases already include the JAR file.

### Step 5: Start Shop Manager

```powershell
docker compose -f docker-compose-lite.yml --env-file .env.lite up -d
```

**Expected output**:
```
Network shop-manager_shop-network-lite  Created
Volume shop-manager_postgres_data  Created
Container retailhq-postgres-lite  Started
Container retailhq-backend-lite  Started
Container shop-manager-frontend-lite  Started
```

### Step 6: Access the Application

1. **Wait ~60 seconds** for startup (first time takes longer)

2. **Open browser**:
   - Frontend: http://localhost:3001
   - Backend API: http://localhost:8081
   - Health Check: http://localhost:8081/actuator/health

3. **Default login**:
   - Username: `superadmin`
   - Password: `changeme`

**✅ Installation Complete!**

---

## Windows-Specific Commands

### Using PowerShell

**Start services**:
```powershell
docker compose -f docker-compose-lite.yml --env-file .env.lite up -d
```

**Stop services**:
```powershell
docker compose -f docker-compose-lite.yml down
```

**View logs**:
```powershell
docker compose -f docker-compose-lite.yml logs -f
```

**Restart services**:
```powershell
docker compose -f docker-compose-lite.yml restart
```

**Check status**:
```powershell
docker compose -f docker-compose-lite.yml ps
```

**Update application**:
```powershell
docker compose -f docker-compose-lite.yml pull
docker compose -f docker-compose-lite.yml up -d
```

### Using Command Prompt (cmd)

Same commands work in `cmd.exe`, just remove line continuations if splitting commands.

---

## Configuration

### Change Ports

If ports 3001 or 8081 are in use, edit `.env.lite`:

```ini
BACKEND_PORT=8082
FRONTEND_PORT=3002
```

Then restart:
```powershell
docker compose -f docker-compose-lite.yml restart
```

### Enable Cloud Sync

Edit `.env.lite`:
```ini
CLOUD_SYNC_ENABLED=true
CLOUD_API_URL=https://api.retailhq.app
CLOUD_API_KEY=rhq_your_api_key_here
STORE_ID=STORE-001
```

Restart backend:
```powershell
docker compose -f docker-compose-lite.yml restart backend
```

---

## Data Management

### Backup Data

```powershell
# Stop services
docker compose -f docker-compose-lite.yml down

# Create backup
$timestamp = Get-Date -Format "yyyyMMdd_HHmmss"
Compress-Archive -Path data\ -DestinationPath "backup_$timestamp.zip"

# Restart
docker compose -f docker-compose-lite.yml up -d
```

### Restore Data

```powershell
# Stop services
docker compose -f docker-compose-lite.yml down

# Restore from backup
Expand-Archive -Path backup_20260107_120000.zip -DestinationPath . -Force

# Restart
docker compose -f docker-compose-lite.yml up -d
```

### Automated Backups

Create a PowerShell script `backup.ps1`:

```powershell
# backup.ps1
$BackupDir = "C:\ShopManager\backups"
$timestamp = Get-Date -Format "yyyyMMdd_HHmmss"

# Create backup directory
New-Item -ItemType Directory -Force -Path $BackupDir

# Stop services
docker compose -f docker-compose-lite.yml down

# Backup
Compress-Archive -Path data\ -DestinationPath "$BackupDir\backup_$timestamp.zip"

# Restart
docker compose -f docker-compose-lite.yml up -d

# Delete old backups (keep last 7 days)
Get-ChildItem $BackupDir -Filter "backup_*.zip" |
    Where-Object { $_.LastWriteTime -lt (Get-Date).AddDays(-7) } |
    Remove-Item
```

Schedule with Task Scheduler:
```powershell
$action = New-ScheduledTaskAction -Execute "PowerShell.exe" -Argument "-File C:\ShopManager\backup.ps1"
$trigger = New-ScheduledTaskTrigger -Daily -At 2am
Register-ScheduledTask -Action $action -Trigger $trigger -TaskName "ShopManagerBackup" -Description "Daily Shop Manager backup"
```

---

## Windows Firewall Configuration

### Allow Local Access Only

```powershell
# Run as Administrator
New-NetFirewallRule -DisplayName "Shop Manager Backend" -Direction Inbound -LocalPort 8081 -Protocol TCP -Action Allow -Profile Private
New-NetFirewallRule -DisplayName "Shop Manager Frontend" -Direction Inbound -LocalPort 3001 -Protocol TCP -Action Allow -Profile Private
```

### Allow Network Access

```powershell
# Run as Administrator
New-NetFirewallRule -DisplayName "Shop Manager Backend (Network)" -Direction Inbound -LocalPort 8081 -Protocol TCP -Action Allow
New-NetFirewallRule -DisplayName "Shop Manager Frontend (Network)" -Direction Inbound -LocalPort 3001 -Protocol TCP -Action Allow
```

---

## Troubleshooting

### Issue: Docker Desktop won't start

**Solution 1 - Enable Hyper-V**:
```powershell
# Run as Administrator
Enable-WindowsOptionalFeature -Online -FeatureName Microsoft-Hyper-V -All
```

**Solution 2 - Enable WSL2**:
```powershell
# Run as Administrator
wsl --install
wsl --update
wsl --set-default-version 2
```

Restart computer.

### Issue: "docker" command not found

**Solution**: Add Docker to PATH:
1. Open "Environment Variables"
2. Add to Path: `C:\Program Files\Docker\Docker\resources\bin`
3. Restart PowerShell

### Issue: Port already in use

**Check what's using the port**:
```powershell
netstat -ano | findstr :3001
netstat -ano | findstr :8081
```

**Kill the process** (use PID from netstat):
```powershell
taskkill /PID <pid> /F
```

Or **change ports** in `.env.lite`.

### Issue: Containers not starting

**View logs**:
```powershell
docker compose -f docker-compose-lite.yml logs
```

**Check Docker Desktop** is running (system tray icon).

**Restart Docker Desktop**:
```powershell
Restart-Service docker
```

### Issue: Database password error

**Remove volumes and start fresh**:
```powershell
docker compose -f docker-compose-lite.yml down -v
docker compose -f docker-compose-lite.yml up -d
```

### Issue: High memory usage

**Check container memory**:
```powershell
docker stats --no-stream
```

**Reduce memory** in `.env.lite`:
```ini
JAVA_OPTS=-Xms128m -Xmx256m
```

Restart backend:
```powershell
docker compose -f docker-compose-lite.yml restart backend
```

### Issue: Slow performance

1. **Check Docker Desktop settings**:
   - Settings → Resources
   - Increase CPU (minimum 2 cores)
   - Increase Memory (minimum 4 GB)

2. **Use WSL2 backend** (faster than Hyper-V):
   - Settings → General
   - Check "Use the WSL 2 based engine"

---

## Windows Service Setup (Optional)

Create a Windows Service to auto-start Shop Manager.

### Using NSSM (Non-Sucking Service Manager)

1. **Download NSSM**: https://nssm.cc/download

2. **Extract to** `C:\nssm`

3. **Install service**:
   ```powershell
   # Run as Administrator
   C:\nssm\nssm.exe install ShopManager "C:\Program Files\Docker\Docker\resources\bin\docker-compose.exe" "-f C:\ShopManager\docker-compose-lite.yml --env-file C:\ShopManager\.env.lite up"

   # Set working directory
   C:\nssm\nssm.exe set ShopManager AppDirectory "C:\ShopManager"

   # Set startup type
   C:\nssm\nssm.exe set ShopManager Start SERVICE_AUTO_START

   # Start service
   Start-Service ShopManager
   ```

4. **Manage service**:
   ```powershell
   # Check status
   Get-Service ShopManager

   # Stop
   Stop-Service ShopManager

   # Start
   Start-Service ShopManager

   # Remove
   C:\nssm\nssm.exe remove ShopManager confirm
   ```

---

## Performance Optimization

### For High-Traffic Stores

Edit `.env.lite`:
```ini
JAVA_OPTS=-Xms512m -Xmx1g -XX:+UseG1GC
```

### For Low-Memory Systems

Edit `.env.lite`:
```ini
JAVA_OPTS=-Xms128m -Xmx256m
```

### Docker Desktop Optimization

1. **Settings → Resources**:
   - CPUs: 2+ cores
   - Memory: 4+ GB
   - Swap: 2 GB
   - Disk image size: 60+ GB

2. **Settings → Docker Engine**:
   ```json
   {
     "experimental": false,
     "features": {
       "buildkit": true
     }
   }
   ```

---

## Security Best Practices

### 1. Secure JWT Secret
```powershell
# Generate strong secret
$bytes = New-Object Byte[] 64
[Security.Cryptography.RandomNumberGenerator]::Create().GetBytes($bytes)
[Convert]::ToBase64String($bytes)
```

### 2. Protect Configuration File
```powershell
# Set file permissions (read-only for current user)
$acl = Get-Acl .env.lite
$acl.SetAccessRuleProtection($true, $false)
$rule = New-Object System.Security.AccessControl.FileSystemAccessRule($env:UserName, "Read", "Allow")
$acl.SetAccessRule($rule)
Set-Acl .env.lite $acl
```

### 3. Disable PostgreSQL External Access

Edit `docker-compose-lite.yml`, remove postgres port mapping:
```yaml
postgres:
  # ports: - Remove this line
  #   - "5432:5432" - Remove this line
```

### 4. Add Antivirus Exclusions

Exclude Docker directories for better performance:
- `C:\Program Files\Docker`
- `C:\ProgramData\Docker`
- `C:\Users\<username>\AppData\Local\Docker`
- `C:\ShopManager\data`

**Windows Defender** (PowerShell as Admin):
```powershell
Add-MpPreference -ExclusionPath "C:\ShopManager\data"
Add-MpPreference -ExclusionPath "C:\ProgramData\Docker"
```

---

## Uninstallation

### Remove Shop Manager

```powershell
# Stop and remove containers
docker compose -f docker-compose-lite.yml down -v

# Remove application directory
Remove-Item -Recurse -Force C:\ShopManager
```

### Remove Docker Desktop

1. Open "Add or Remove Programs"
2. Find "Docker Desktop"
3. Click "Uninstall"

---

## Support

### Documentation
- [Docker Lite Quick Start](./DOCKER_LITE_QUICKSTART.md)
- [Docker Lite Deployment](./DOCKER_LITE_DEPLOYMENT.md)
- [Cloud Sync Setup](./CLOUD_SYNC_SETUP.md)

### Community
- GitHub Issues: https://github.com/yourorg/shop-manager/issues
- Email: support@shopmanager.com

---

## Comparison: Docker Lite vs Platform Installers

| Feature | Docker Lite | Windows Installer (.exe) |
|---------|-------------|---------------------------|
| **Installation** | Docker Desktop + Compose | One-click .exe installer |
| **Memory Usage** | 1-1.5 GB | 500 MB - 1 GB |
| **Startup Time** | 30-60s | 10-20s |
| **Updates** | `docker compose pull` | Download new .exe |
| **Isolation** | Full container isolation | Native Windows process |
| **Portability** | Works on any OS with Docker | Windows only |
| **Complexity** | Medium (requires Docker) | Low (native app) |
| **Best For** | IT professionals, multi-store | End users, single-store |

---

**Last Updated**: 2026-01-07
**Version**: 1.0.0
