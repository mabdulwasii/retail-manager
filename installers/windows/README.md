# Windows Installer Build Guide

This directory contains the configuration for building a Windows installer for Shop Manager using Inno Setup.

## Prerequisites

1. **Windows Build Machine** (or Windows VM)
2. **Inno Setup 6.x** - Download from [https://jrsoftware.org/isinfo.php](https://jrsoftware.org/isinfo.php)
3. **Java JDK 21** - For testing the installer
4. **Embedded JAR** - Built using `mvnw package -Pembedded`

## Directory Structure

```
installers/windows/
├── shop-manager.iss          # Inno Setup script
├── scripts/
│   ├── shop-manager.bat             # GUI launcher
│   ├── shop-manager-console.bat     # Console launcher
│   ├── install-service.bat          # Service installation
│   └── uninstall-service.bat        # Service uninstallation
├── config/
│   ├── .env.template                # Environment template
│   └── application.yml              # Spring Boot config
├── assets/
│   ├── shop-manager.ico             # Application icon
│   ├── wizard-image.bmp             # Installer wizard image (164x314)
│   └── wizard-small.bmp             # Installer wizard small image (55x58)
└── README.md                        # This file
```

## Building the Installer

### Method 1: Using Inno Setup GUI (Recommended)

1. Install Inno Setup
2. Open `shop-manager.iss` in Inno Setup Compiler
3. Click **Build** → **Compile**
4. Installer will be created in `../../build/installers/windows/`

### Method 2: Using Command Line

```cmd
"C:\Program Files (x86)\Inno Setup 6\ISCC.exe" shop-manager.iss
```

### Method 3: Using PowerShell Script

```powershell
# From project root
.\installers\windows\build-installer.ps1
```

## Installer Features

### Installation Options

- ✅ Automatic Java version detection (Java 21+)
- ✅ Desktop icon creation
- ✅ Start menu shortcuts
- ✅ Automatic startup option
- ✅ Service installation option
- ✅ Secure JWT secret generation
- ✅ Port configuration

### Post-Installation

The installer creates:

- **Installation Directory**: `C:\Program Files\Shop Manager\`
- **Data Directory**: `C:\Program Files\Shop Manager\data\`
- **Configuration**: `C:\Program Files\Shop Manager\config\.env`
- **Start Menu**: "Shop Manager" program group
- **Desktop Icon**: (optional) "Shop Manager"

### Shortcuts Created

1. **Shop Manager** - Launches application in GUI mode (no console)
2. **Shop Manager (Console)** - Launches with console for debugging
3. **Configuration** - Opens `.env` file in Notepad
4. **Data Folder** - Opens data directory
5. **Documentation** - Opens docs folder
6. **Uninstall** - Removes Shop Manager

## Launcher Scripts

### shop-manager.bat

Launches the application without console window (GUI mode).

**Usage:**
```cmd
shop-manager.bat
```

**Features:**
- Loads configuration from `.env`
- Creates data directories
- Launches with `javaw` (no console)
- Health check after startup

### shop-manager-console.bat

Launches the application with console output for debugging.

**Usage:**
```cmd
shop-manager-console.bat
```

**Features:**
- Colored console output
- Java version display
- Configuration summary
- Real-time log output

### install-service.bat

Installs Shop Manager as a Windows service (requires Administrator).

**Usage:**
```cmd
install-service.bat
```

**Features:**
- Creates Windows service
- Automatic startup on boot
- Runs in background
- Managed via Services console

### uninstall-service.bat

Removes Shop Manager Windows service.

**Usage:**
```cmd
uninstall-service.bat
```

## Configuration

### Environment Variables (.env)

The installer creates a `.env` file from the template during installation. Users can edit this file to customize:

- **BACKEND_PORT** - Backend server port (default: 8081)
- **FRONTEND_PORT** - Frontend port (default: 3001)
- **JWT_SECRET** - Auto-generated secure secret
- **CLOUD_SYNC_ENABLED** - Enable cloud sync (default: false)
- **CLOUD_API_URL** - Cloud API endpoint
- **CLOUD_API_KEY** - Cloud API key
- **STORE_ID** - Unique store identifier
- **JAVA_OPTS** - JVM memory settings

### Spring Boot Configuration

The `application.yml` file contains Spring Boot settings and is pre-configured for embedded mode.

## Assets Required

### Application Icon (shop-manager.ico)

- **Format**: ICO file with multiple resolutions
- **Sizes**: 16x16, 32x32, 48x48, 256x256
- **Create from PNG**:
  ```
  Using online tools: https://convertio.co/png-ico/
  Or ImageMagick: convert shop-manager.png -define icon:auto-resize=256,128,96,64,48,32,16 shop-manager.ico
  ```

### Wizard Images

1. **wizard-image.bmp** (164x314 pixels)
   - Displayed on left side of installer wizard
   - 8-bit color depth recommended
   - Should represent Shop Manager branding

2. **wizard-small.bmp** (55x58 pixels)
   - Displayed in top-right corner
   - 8-bit color depth recommended
   - Usually contains logo or icon

**Create placeholders:**
```powershell
# Using PowerShell
Add-Type -AssemblyName System.Drawing
$bmp = New-Object System.Drawing.Bitmap(164, 314)
$bmp.Save("assets\wizard-image.bmp")
$bmp = New-Object System.Drawing.Bitmap(55, 58)
$bmp.Save("assets\wizard-small.bmp")
```

## Testing the Installer

### Pre-Installation Test

1. Ensure embedded JAR exists:
   ```cmd
   cd backend
   mvnw clean package -Pembedded -DskipTests
   ```

2. Build installer using one of the methods above

3. Run installer on a clean Windows machine:
   ```cmd
   shop-manager-1.0.0-windows-x64-setup.exe
   ```

### Post-Installation Test

1. **Verify installation:**
   ```cmd
   dir "C:\Program Files\Shop Manager"
   ```

2. **Check configuration:**
   ```cmd
   type "C:\Program Files\Shop Manager\config\.env"
   ```

3. **Test GUI launcher:**
   ```cmd
   cd "C:\Program Files\Shop Manager"
   shop-manager.bat
   ```

4. **Verify application:**
   - Wait 30 seconds for startup
   - Open browser: http://localhost:8081/actuator/health
   - Should return: `{"status":"UP"}`

5. **Test console launcher:**
   ```cmd
   shop-manager-console.bat
   ```
   - Verify logs appear
   - Press Ctrl+C to stop

6. **Test service installation** (as Administrator):
   ```cmd
   install-service.bat
   ```
   - Open Services: `services.msc`
   - Verify "Shop Manager" service exists
   - Check status is "Running"

## Uninstallation

Users can uninstall using:

1. **Start Menu**: Programs → Shop Manager → Uninstall
2. **Control Panel**: Programs and Features → Shop Manager → Uninstall
3. **Settings**: Apps & features → Shop Manager → Uninstall

**Uninstaller will:**
- Stop Shop Manager service (if installed)
- Remove Shop Manager service
- Remove program files
- Remove start menu shortcuts
- Remove desktop icon
- **Preserve data directory** (requires manual deletion)

## Troubleshooting

### Java Not Found

**Error:** "Java is not installed or not in PATH"

**Solution:**
- Install Java 21 from https://adoptium.net
- Or add Java to PATH:
  ```cmd
  setx PATH "%PATH%;C:\Program Files\Eclipse Adoptium\jdk-21.0.x-hotspot\bin"
  ```

### Port Already in Use

**Error:** "Port 8081 is already in use"

**Solution:**
1. Edit `.env` file:
   ```
   BACKEND_PORT=8082
   ```
2. Restart application

### Service Won't Start

**Error:** Service fails to start in Services console

**Solution:**
1. Check Event Viewer: Windows Logs → Application
2. Common issues:
   - Java not in system PATH
   - Invalid JWT_SECRET in `.env`
   - Port conflict
3. Test with console launcher first:
   ```cmd
   shop-manager-console.bat
   ```

### High Memory Usage

**Solution:** Reduce memory in `.env`:
```
JAVA_OPTS=-Xms128m -Xmx256m -XX:+UseG1GC
```

## Advanced Customization

### Custom Installation Directory

Users can change the installation directory during installation.

### Silent Installation

```cmd
shop-manager-1.0.0-windows-x64-setup.exe /VERYSILENT /SUPPRESSMSGBOXES /NORESTART
```

### Custom Parameters

```cmd
shop-manager-1.0.0-windows-x64-setup.exe /DIR="D:\ShopManager" /NOICONS
```

## Building from CI/CD

See GitHub Actions workflow configuration in:
```
.github/workflows/build-standalone-release.yml
```

The workflow will:
1. Build embedded JAR
2. Set up Inno Setup on Windows runner
3. Compile installer
4. Upload to GitHub releases

## License

See [LICENSE](../../LICENSE) file in project root.

## Support

- **Documentation**: [docs/EMBEDDED_DEPLOYMENT.md](../../docs/EMBEDDED_DEPLOYMENT.md)
- **Issues**: https://github.com/yourorg/shop-manager/issues
- **Email**: support@shopmanager.com

---

**Last Updated**: 2025-12-24
