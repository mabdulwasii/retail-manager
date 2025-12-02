# Shop Manager - macOS Installation Guide

Welcome to Shop Manager! This guide will help you install and configure the Shop Manager desktop application on your Mac.

## Table of Contents

1. [System Requirements](#system-requirements)
2. [Pre-Installation Checklist](#pre-installation-checklist)
3. [Installation Steps](#installation-steps)
4. [Fixing "Damaged App" Error](#fixing-damaged-app-error)
5. [First-Time Setup](#first-time-setup)
6. [Connecting to Your Shop](#connecting-to-your-shop)
7. [Troubleshooting](#troubleshooting)
8. [Getting Help](#getting-help)

---

## System Requirements

### Minimum Requirements

- **Operating System**: macOS 11 (Big Sur) or later
- **Processor**: Intel Core i3 or Apple M1
- **RAM**: 4 GB
- **Storage**: 500 MB free disk space
- **Internet**: Broadband connection

**Note**: No external dependencies required! Shop Manager is a standalone application with everything bundled.

### Recommended Requirements

- **Operating System**: macOS 12 (Monterey) or later
- **Processor**: Intel Core i5 or Apple M1 Pro/Max
- **RAM**: 8 GB or more
- **Storage**: 1 GB free disk space
- **Internet**: High-speed broadband connection

---

## Pre-Installation Checklist

Before installing Shop Manager, please ensure:

1. ✅ You have **administrator privileges** on your Mac
2. ✅ You have received your **Shop Manager installer** (file name: `Shop-Manager-1.0.0.dmg` or `Shop-Manager-1.0.0-mac.zip`)
3. ✅ You have your **connection details** from your system administrator:
   - Backend API URL (e.g., `https://api.retail.gomco.com`)
   - Keycloak authentication URL (e.g., `https://auth.retail.gomco.com`)
4. ✅ You have your **login credentials** (username and password)

---

## Installation Steps

### Step 1: Download the Installer

#### Option A: Download from GitHub Releases (Recommended)

1. Visit the **Shop Manager Releases** page:
   ```
   https://github.com/mabdulwasii/retail-manager/releases
   ```

2. Under **Assets**, download:
   - **Shop-Manager-1.0.0.dmg** (recommended for easy installation)
   - OR **Shop-Manager-1.0.0-mac.zip** (alternative format)

3. The download will be saved to your **Downloads** folder

4. **Verify the download**:
   - File name: `Shop-Manager-1.0.0.dmg` or `Shop-Manager-1.0.0-mac.zip`
   - File size: ~150-200 MB (approximate)

#### Option B: Download from Your Organization

If your organization hosts the installer internally:

1. Contact your **system administrator** for the download link
2. Save the installer to your **Downloads** folder

### Important Security Note

⚠️ **Only download Shop Manager from trusted sources:**
- Official GitHub Releases page
- Your organization's IT department
- Links provided by your system administrator

**Never** download from third-party websites or untrusted sources.

### Step 2: Install the Application

#### For DMG Files (Recommended)

1. **Locate** the DMG file in your Downloads folder
2. **Double-click** `Shop-Manager-1.0.0.dmg` to mount it
3. A window will open showing the Shop Manager app icon and Applications folder
4. **Drag** the Shop Manager icon to the Applications folder
5. **Eject** the DMG by clicking the eject button in Finder

#### For ZIP Files

1. **Locate** the ZIP file in your Downloads folder
2. **Double-click** to extract it
3. **Move** the extracted `Shop Manager.app` to your Applications folder

---

## Fixing "Damaged App" Error

⚠️ **Important**: If you see the error **"Shop Manager.app is damaged and can't be opened. You should move it to the Trash"**, this is a macOS Gatekeeper security feature blocking unsigned applications.

**This does NOT mean the app is actually damaged or contains malware.** It simply means the app isn't code-signed by Apple.

### Solution 1: Right-Click Open (Easiest)

1. **Open Finder** and go to **Applications**
2. **Find** Shop Manager.app
3. **Right-click** (or Control-click) on Shop Manager.app
4. Select **"Open"** from the menu
5. Click **"Open"** in the security dialog that appears
6. The app will now launch and be permanently allowed

### Solution 2: Remove Quarantine Attribute (Terminal)

1. **Open Terminal** (Applications → Utilities → Terminal)
2. **Run this command**:
   ```bash
   xattr -cr /Applications/Shop\ Manager.app
   ```
3. **Press Enter**
4. **Double-click** Shop Manager.app to launch

### Solution 3: System Settings (macOS 13+)

1. **Try to open** Shop Manager.app (it will be blocked)
2. **Immediately go to** System Settings → Privacy & Security
3. **Scroll down** to the Security section
4. **Click "Open Anyway"** next to the message about Shop Manager
5. **Confirm** by clicking "Open" in the dialog

### Solution 4: Bypass Gatekeeper Temporarily

⚠️ **Use with caution** - this reduces system security temporarily

1. **Open Terminal**
2. **Disable Gatekeeper**:
   ```bash
   sudo spctl --master-disable
   ```
3. **Enter your password** when prompted
4. **Open** Shop Manager.app
5. **Re-enable Gatekeeper** (important!):
   ```bash
   sudo spctl --master-enable
   ```

---

## First-Time Setup

When you launch Shop Manager for the first time, you'll need to configure your connection settings.

### Configuration Wizard

1. **Welcome Screen**
   - Click **"Get Started"**

2. **Server Connection**

   Enter the connection details provided by your system administrator:

   | Field | Example Value | Description |
   |-------|---------------|-------------|
   | **Backend API URL** | `https://api.retail.gomco.com` | Your shop's backend server |
   | **Authentication URL** | `https://auth.retail.gomco.com` | Keycloak authentication server |
   | **Shop Domain** | `retail.gomco.com` | Your organization's domain |

3. **Test Connection**
   - Click **"Test Connection"** to verify settings
   - ✅ You should see: **"Connection successful!"**
   - ❌ If connection fails, verify your URLs and internet connection

4. **Save Configuration**
   - Click **"Save & Continue"**

### Login to Your Account

1. Enter your **username** (or email address)
2. Enter your **password**
3. Click **"Login"**

4. **First Login**:
   - If this is your first login, you may be asked to:
     - ✅ Change your password
     - ✅ Set up two-factor authentication (if enabled)
     - ✅ Complete your profile information

---

## Connecting to Your Shop

### For Cloud-Hosted Deployments

If your organization uses Shop Manager hosted on Kubernetes/cloud:

1. Your system administrator will provide you with:
   - **Backend URL**: `https://api.retail.gomco.com`
   - **Keycloak URL**: `https://auth.retail.gomco.com`
   - **Realm Name**: Usually `shop-manager` or your organization name
   - **Login Credentials**: Username and initial password

2. Enter these details during first-time setup

3. **Connection Type**: Select **"Cloud Hosted"**

### For Local/Standalone Deployments

If you're running Shop Manager entirely on your local Mac:

1. The installer includes Docker Compose files for running everything locally

2. **Configuration Type**: Select **"Standalone/Local"**

3. **Default Settings** (automatically configured):
   - Backend URL: `http://localhost:8081`
   - Frontend URL: `http://localhost:3001`
   - Keycloak URL: `http://localhost:8080`

4. Click **"Start Services"** to launch all components

5. Wait for services to start (2-3 minutes on first launch)

6. Login with default credentials:
   - **Username**: `admin@shopmanager.com`
   - **Password**: `admin123`
   - ⚠️ **Important**: Change this password immediately after first login!

---

## Troubleshooting

### Installation Issues

#### Problem: "Shop Manager.app is damaged and can't be opened"

**Solution**: See [Fixing "Damaged App" Error](#fixing-damaged-app-error) section above.

#### Problem: "Shop Manager.app can't be opened because Apple cannot check it for malicious software"

**Solution**:
1. Right-click on Shop Manager.app
2. Select **"Open"**
3. Click **"Open"** in the security dialog
4. This bypasses Gatekeeper on first launch

#### Problem: App opens then immediately closes

**Solution**:
1. Open **Console.app** (Applications → Utilities → Console)
2. Check for error messages related to Shop Manager
3. Common issues:
   - Missing system libraries (update macOS)
   - Insufficient permissions (run: `chmod +x /Applications/Shop\ Manager.app/Contents/MacOS/Shop\ Manager`)
   - Corrupted download (re-download the installer)

#### Problem: "Operation not permitted" when running xattr command

**Solution**:
1. Go to **System Settings** → **Privacy & Security** → **Full Disk Access**
2. Click the **+** button
3. Add **Terminal**
4. Try the xattr command again

---

### Connection Issues

#### Problem: Cannot connect to backend server

**Checklist**:
- ✅ Check your internet connection
- ✅ Verify the Backend URL is correct
- ✅ Ensure your firewall allows Shop Manager
- ✅ Contact your system administrator if using cloud deployment

#### Problem: "Invalid credentials" error

**Solution**:
1. Double-check your username and password
2. Usernames are usually email addresses
3. Passwords are case-sensitive
4. Use **"Forgot Password"** if needed
5. Contact your system administrator to reset your password

#### Problem: "Connection timeout" error

**Solution**:
1. Check if backend server is running (contact system administrator)
2. Verify you're on the correct network (VPN required?)
3. Check firewall settings:
   - System Settings → Network → Firewall Options
   - Allow Shop Manager to accept incoming connections

---

### Application Issues

#### Problem: Shop Manager won't start

**Solution**:
1. **Force quit** if it's hanging:
   - Press **⌘ + Option + Esc**
   - Select Shop Manager
   - Click **"Force Quit"**
2. Check **Activity Monitor** for leftover processes
3. Try launching again
4. If issue persists, reinstall the application

#### Problem: Slow performance

**Solution**:
1. Check your internet connection speed
2. Close other applications to free up memory
3. Restart Shop Manager
4. Clear application cache:
   - Settings → Advanced → Clear Cache
5. Check available disk space (need at least 5 GB free)

#### Problem: Updates not working

**Solution**:
1. Check your internet connection
2. Make sure you have administrator privileges
3. Manually download the latest version
4. Delete old version, then install new version

---

## Updating Shop Manager

### Automatic Updates (Recommended)

Shop Manager automatically checks for updates on startup:

1. When an update is available, you'll see a notification
2. Click **"Download Update"**
3. Update downloads in the background
4. Click **"Install and Restart"** when ready
5. Shop Manager will close, install the update, and restart

### Manual Updates

If automatic updates are disabled:

1. Download the latest installer from your administrator
2. Quit Shop Manager
3. Install the new version (it will replace the old one)
4. Your settings and data are preserved

---

## Uninstalling Shop Manager

If you need to uninstall Shop Manager:

### Method 1: Manual Deletion

1. **Quit** Shop Manager if it's running
2. **Open Finder** and go to **Applications**
3. **Find** Shop Manager.app
4. **Drag** it to the Trash (or right-click → Move to Trash)
5. **Empty Trash** to complete removal

### Method 2: Complete Removal with Data

To completely remove all data and settings:

1. **Delete the app** (see Method 1)
2. **Open Finder** and press **⌘ + Shift + G**
3. **Delete these folders**:
   ```
   ~/Library/Application Support/Shop Manager
   ~/Library/Preferences/com.shopmanager.desktop.plist
   ~/Library/Logs/Shop Manager
   ~/Library/Caches/Shop Manager
   ```

---

## Getting Help

### In-App Support

- **Help Menu**: Click `Help` → `Support` in the menu bar
- **Documentation**: Click `Help` → `User Guide`
- **Keyboard Shortcuts**: Press `⌘ + ?`

### Contact Support

- **Email**: support@shopmanager.com
- **System Administrator**: Contact your organization's IT department
- **Phone**: (Available during business hours)

### Additional Resources

- **User Manual**: [Download PDF](https://docs.shopmanager.com/user-manual.pdf)
- **Video Tutorials**: [Watch on YouTube](https://youtube.com/shopmanager)
- **FAQ**: [Frequently Asked Questions](https://docs.shopmanager.com/faq)
- **Community Forum**: [Ask Questions](https://community.shopmanager.com)

---

## Important Security Notes

1. 🔒 **Never share your login credentials** with anyone
2. 🔒 **Change default passwords** immediately on first login
3. 🔒 **Enable two-factor authentication** if available
4. 🔒 **Keep Shop Manager updated** to get the latest security patches
5. 🔒 **Log out when done**, especially on shared computers
6. 🔒 **Use strong passwords**:
   - Minimum 8 characters
   - Include uppercase, lowercase, numbers, and special characters
   - Don't reuse passwords from other accounts

---

## Keyboard Shortcuts

### macOS-Specific Shortcuts

- **⌘ + Q** - Quit Shop Manager
- **⌘ + W** - Close current window
- **⌘ + M** - Minimize window
- **⌘ + H** - Hide Shop Manager
- **⌘ + ,** - Open Preferences
- **⌘ + ?** - Show Help

---

## Understanding macOS Security

### Why does macOS block the app?

macOS uses **Gatekeeper** to protect your Mac from potentially harmful software. Apps distributed outside the Mac App Store need to be **code-signed** and **notarized** by Apple.

Shop Manager is currently an **unsigned application**, which triggers Gatekeeper's security warning. This is common for:
- Open-source software
- Internal business applications
- Apps distributed directly (not via App Store)

### Is it safe to bypass Gatekeeper?

**Yes**, but only if you trust the source:

✅ **Safe when**:
- Downloaded from official GitHub releases
- Provided by your IT department
- You verified the checksum/signature

❌ **Not safe when**:
- Downloaded from unknown websites
- Received via email from strangers
- Can't verify the source

**The workarounds in this guide are standard practice for legitimate unsigned applications.**

---

## Apple Silicon (M1/M2/M3) Compatibility

Shop Manager is compatible with both Intel and Apple Silicon Macs:

- **Intel Macs**: Runs natively
- **Apple Silicon (M1/M2/M3)**: Runs natively via universal binary

No Rosetta 2 translation required!

---

## Appendix: Advanced Configuration

### Manual Configuration File

If you need to manually edit configuration:

1. Quit Shop Manager
2. Open **Finder** and press **⌘ + Shift + G**
3. Navigate to: `~/Library/Application Support/Shop Manager/`
4. Edit `config.yaml` with a text editor (e.g., TextEdit, VS Code)
5. Save changes
6. Restart Shop Manager

### Example config.yaml

```yaml
# Backend API configuration
backend:
  url: https://api.retail.gomco.com
  timeout: 30000

# Authentication configuration
auth:
  url: https://auth.retail.gomco.com
  realm: shop-manager
  clientId: shop-manager-desktop

# Application settings
app:
  theme: light
  language: en
  autoUpdate: true
```

---

**Thank you for choosing Shop Manager!**

For questions or support, please contact your system administrator or our support team.

---

*Version: 1.0 | Last Updated: December 2024 | Document ID: MACOS-INSTALL-v1.0.0*
