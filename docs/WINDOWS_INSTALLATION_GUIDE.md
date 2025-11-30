# Shop Manager - Windows Installation Guide

Welcome to Shop Manager! This guide will help you install and configure the Shop Manager desktop application on your Windows computer.

## Table of Contents

1. [System Requirements](#system-requirements)
2. [Pre-Installation Checklist](#pre-installation-checklist)
3. [Installation Steps](#installation-steps)
4. [First-Time Setup](#first-time-setup)
5. [Connecting to Your Shop](#connecting-to-your-shop)
6. [Troubleshooting](#troubleshooting)
7. [Getting Help](#getting-help)

---

## System Requirements

### Minimum Requirements

- **Operating System**: Windows 10 (64-bit) or later
- **Processor**: Intel Core i3 or equivalent
- **RAM**: 4 GB
- **Storage**: 500 MB free disk space
- **Internet**: Broadband connection

### Recommended Requirements

- **Operating System**: Windows 11 (64-bit)
- **Processor**: Intel Core i5 or equivalent
- **RAM**: 8 GB or more
- **Storage**: 1 GB free disk space
- **Internet**: High-speed broadband connection

---

## Pre-Installation Checklist

Before installing Shop Manager, please ensure:

1. ✅ You have **administrator privileges** on your Windows computer
2. ✅ Your antivirus software is temporarily disabled (or Shop Manager is whitelisted)
3. ✅ You have received your **Shop Manager installer** (file name: `Shop Manager-Setup-v0.1.13.exe`)
4. ✅ You have your **connection details** from your system administrator:
   - Backend API URL (e.g., `https://api.retail.gomco.com`)
   - Keycloak authentication URL (e.g., `https://auth.retail.gomco.com`)
5. ✅ You have your **login credentials** (username and password)

---

## Installation Steps

### Step 1: Download the Installer

### Option A: Download from GitHub Releases (Recommended)

1. Visit the **Shop Manager Releases** page:
   ```
   https://github.com/mabdulwasii/retail-manager/releases
   ```

2. Under **Assets**, click to download:
   - **Shop Manager-Setup-0.1.13.exe** (Windows 64-bit installer)

3. The download will be saved to your **Downloads** folder

4. **Verify the download**:
   - File name: `Shop Manager-Setup-0.1.13.exe`
   - File size: ~150-200 MB (approximate)

### Option B: Download from Your Organization

If your organization hosts the installer internally:

1. Contact your **system administrator** for the download link
2. You may receive:
   - Direct download link
   - Internal file server location
   - Email attachment (for smaller organizations)

3. Save the installer to your **Downloads** folder

### Important Security Note

⚠️ **Only download Shop Manager from trusted sources:**
- Official GitHub Releases page
- Your organization's IT department
- Links provided by your system administrator

**Never** download from third-party websites or untrusted sources.

### Step 2: Run the Installer

1. **Locate** the installer file you downloaded
2. **Right-click** on `Shop Manager-Setup-v0.1.13.exe`
3. Select **"Run as administrator"**

   ![Run as Administrator](https://via.placeholder.com/400x200.png?text=Right-click+%3E+Run+as+administrator)

4. If prompted by **Windows SmartScreen**, click **"More info"** and then **"Run anyway"**

### Step 3: Installation Wizard

1. **Welcome Screen**: Click **"Next"** to begin installation

2. **License Agreement**: Read and accept the license agreement, then click **"Next"**

3. **Installation Location**:
   - Default location: `C:\Program Files\Shop Manager\`
   - You can change this location if desired
   - Click **"Next"** to continue

   ![Installation Location](https://via.placeholder.com/600x400.png?text=Choose+Installation+Location)

4. **Start Menu Folder**:
   - Leave default setting: `Shop Manager`
   - Click **"Next"**

5. **Additional Tasks**:
   - ✅ Create a desktop shortcut (recommended)
   - ✅ Create a Start Menu entry (recommended)
   - Click **"Next"**

6. **Ready to Install**: Review your choices and click **"Install"**

7. **Installation Progress**: Wait for the installation to complete (1-2 minutes)

8. **Completion**:
   - ✅ Check **"Launch Shop Manager"** if you want to start immediately
   - Click **"Finish"**

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

2. Enter these details during first-time setup (see above)

3. **Connection Type**: Select **"Cloud Hosted"**

### For Local/Standalone Deployments

If you're running Shop Manager entirely on your local computer:

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

#### Problem: "Windows protected your PC" warning

**Solution**:
1. Click **"More info"**
2. Click **"Run anyway"**
3. This is a standard Windows SmartScreen warning for new applications

#### Problem: Antivirus blocks installation

**Solution**:
1. Temporarily disable your antivirus software
2. Run the installer
3. Re-enable antivirus after installation
4. Add Shop Manager to your antivirus whitelist:
   - `C:\Program Files\Shop Manager\`

#### Problem: "Access denied" error during installation

**Solution**:
1. Make sure you're running the installer **as administrator**
2. Right-click → "Run as administrator"

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
   - Windows Firewall → Allow an app
   - Add `Shop Manager.exe`

---

### Application Issues

#### Problem: Shop Manager won't start

**Solution**:
1. Restart your computer
2. Check if Shop Manager is already running:
   - Open Task Manager (`Ctrl + Shift + Esc`)
   - End any `Shop Manager` processes
3. Try launching again
4. If issue persists, reinstall the application

#### Problem: Slow performance

**Solution**:
1. Check your internet connection speed
2. Close other applications to free up memory
3. Restart Shop Manager
4. Clear application cache:
   - Settings → Advanced → Clear Cache

#### Problem: Updates not working

**Solution**:
1. Check your internet connection
2. Make sure you have administrator privileges
3. Manually download the latest version
4. Uninstall old version, then install new version

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
2. Close Shop Manager
3. Run the new installer
4. It will automatically upgrade your installation
5. Your settings and data are preserved

---

## Getting Help

### In-App Support

- **Help Menu**: Click `Help` → `Support` in the menu bar
- **Documentation**: Click `Help` → `User Guide`
- **Keyboard Shortcuts**: Press `F1`

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

## Uninstalling Shop Manager

If you need to uninstall Shop Manager:

### Method 1: Windows Settings

1. Press `Windows + I` to open Settings
2. Go to **Apps** → **Apps & features**
3. Search for **"Shop Manager"**
4. Click **"Uninstall"**
5. Confirm by clicking **"Uninstall"** again
6. Follow the uninstall wizard

### Method 2: Control Panel

1. Open **Control Panel**
2. Go to **Programs** → **Programs and Features**
3. Find **"Shop Manager"** in the list
4. Right-click and select **"Uninstall"**
5. Follow the uninstall wizard

### Data Removal

Uninstalling Shop Manager removes the application but keeps your configuration files:

- Configuration: `C:\Users\<YourUsername>\AppData\Roaming\Shop Manager\`
- Logs: `C:\Users\<YourUsername>\AppData\Roaming\Shop Manager\logs\`

To completely remove all data, manually delete these folders after uninstalling.

---

## Appendix: Advanced Configuration

### Manual Configuration File

If you need to manually edit configuration:

1. Close Shop Manager
2. Navigate to: `C:\Users\<YourUsername>\AppData\Roaming\Shop Manager\`
3. Edit `config.yaml` with a text editor (e.g., Notepad++)
4. Save changes
5. Restart Shop Manager

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

*Version: 1.0 | Last Updated: November 2025 | Document ID: WIN-INSTALL-v0.1.13*
