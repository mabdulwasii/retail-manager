# Shop Manager - Upgrade Guide

This guide explains how to upgrade Shop Manager to a new version while preserving your data.

---

## 🔐 Default Login Credentials

**Email**: `superadmin@retailhq.local`
**Password**: `changeme`

⚠️ **IMPORTANT**: Change the default password after first login!

**To change credentials**:
1. Edit `{install-dir}/config/.env`
2. Update `SUPERADMIN_EMAIL` and `SUPERADMIN_PASSWORD`
3. Restart Shop Manager

---

## 📦 Before You Upgrade

### 1. Backup Your Data

**Embedded Mode** (standalone installation):
```bash
# Windows
copy "%PROGRAMFILES%\Shop Manager\data" "C:\Backups\shopmanager-backup"

# macOS/Linux
cp -r /Applications/Shop\ Manager.app/Contents/data ~/shopmanager-backup
```

**Docker Compose**:
```bash
docker-compose down
cp -r ./data ./data-backup-$(date +%Y%m%d)
```

### 2. Check for Running Instances

**Windows**:
```powershell
# Check if Shop Manager is running
tasklist | findstr "java"

# Stop Shop Manager service (if installed as service)
net stop "Shop Manager"

# Or kill the process
taskkill /F /IM javaw.exe /FI "WINDOWTITLE eq Shop Manager*"
```

**macOS**:
```bash
# Check if running
ps aux | grep shop-manager

# Stop the application
killall -9 java

# Or use Activity Monitor and quit "Shop Manager"
```

**Linux**:
```bash
# Check if running
ps aux | grep shop-manager

# Stop systemd service (if installed)
sudo systemctl stop shop-manager

# Or kill the process
pkill -f shop-manager
```

---

## 🚀 Upgrade Steps

### Windows

1. **Stop Shop Manager**:
   - Close the application window
   - Or run: `net stop "Shop Manager"` (if running as service)
   - Wait 10 seconds for clean shutdown

2. **Run the new installer**:
   - Double-click `shop-manager-X.X.X-windows-x64-setup.exe`
   - The installer will automatically:
     - Stop running processes
     - Preserve your `.env` configuration
     - Preserve your `data/` folder
     - Update only the application files

3. **Verify upgrade**:
   - Start Shop Manager
   - Log in with your credentials
   - Check version in Settings or Help menu

### macOS

1. **Stop Shop Manager**:
   ```bash
   # Quit the application via menu or:
   killall "Shop Manager"
   ```

2. **Install new version**:
   - Open `shop-manager-X.X.X-macos-x64.dmg`
   - Drag `Shop Manager.app` to Applications
   - **Replace** when prompted

3. **Data preservation**:
   - Your data is in `~/Library/Application Support/ShopManager/`
   - Configuration is in `~/.shopmanager/.env`
   - Both are preserved during upgrade

### Linux

#### Debian/Ubuntu (.deb)

```bash
# Stop the service
sudo systemctl stop shop-manager

# Install new version
sudo dpkg -i shop-manager_X.X.X_all.deb

# Start the service
sudo systemctl start shop-manager
```

#### RHEL/CentOS (.rpm)

```bash
# Stop the service
sudo systemctl stop shop-manager

# Install new version
sudo rpm -Uvh shop-manager-X.X.X-1.x86_64.rpm

# Start the service
sudo systemctl start shop-manager
```

#### AppImage

```bash
# Stop running instance
pkill -f shop-manager

# Make new AppImage executable
chmod +x shop-manager-X.X.X-x86_64.AppImage

# Replace old version
mv shop-manager-X.X.X-x86_64.AppImage ~/bin/shop-manager.AppImage

# Start new version
~/bin/shop-manager.AppImage
```

---

## 🔄 What Gets Preserved

During upgrades, the following are **automatically preserved**:

✅ **Database** (`data/shopmanager.db` or PostgreSQL)
✅ **Configuration** (`.env` file)
✅ **User uploads** (`data/uploads/`)
✅ **Logs** (`logs/`)
✅ **Keycloak data** (`data/keycloak/` in embedded mode)

### What Gets Updated

The installer **only replaces**:
- Application JAR file (`lib/shop-manager-*.jar`)
- Launch scripts (`*.bat`, `*.sh`)
- Default configuration templates
- Documentation files

---

## ⚠️ Troubleshooting Upgrades

### Issue: "File in use" error (Windows)

**Cause**: Shop Manager is still running

**Solution**:
```powershell
# Force stop all Java processes
taskkill /F /IM java.exe
taskkill /F /IM javaw.exe

# Wait 10 seconds, then retry installation
```

### Issue: Application won't start after upgrade

**Cause**: Database migration failed or corrupt `.env`

**Solution**:
1. Check logs in `{install-dir}/logs/shop-manager.log`
2. Verify `.env` file has correct syntax
3. If needed, restore from backup and retry

### Issue: "Permission denied" (macOS/Linux)

**Cause**: Installer doesn't have write permissions

**Solution**:
```bash
# macOS: Allow security settings
sudo spctl --master-disable  # Temporarily disable Gatekeeper
# After installation:
sudo spctl --master-enable   # Re-enable

# Linux: Run with sudo
sudo dpkg -i shop-manager_X.X.X_all.deb
```

### Issue: Lost admin password after upgrade

**Cause**: `.env` file was overwritten

**Solution**:
1. Stop Shop Manager
2. Edit `{install-dir}/config/.env`
3. Set `SUPERADMIN_PASSWORD=changeme`
4. Restart Shop Manager
5. Log in and change password immediately

---

## 🔔 Update Notifications

Starting from **v0.1.30**, Shop Manager includes automatic update notifications:

- **Backend**: Checks for updates every 24 hours
- **Frontend**: Shows banner when new version available
- **Action**: Click "Download" to get the latest installer

You can disable auto-checks:
```env
UPDATE_CHECK_ENABLED=false
```

---

## 📊 Version History

Check the [Releases page](https://github.com/mabdulwasii/retail-manager/releases) for:
- Release notes
- Breaking changes
- Migration guides
- Download links

---

## 🆘 Need Help?

- **Documentation**: [README.md](../README.md)
- **Issues**: [GitHub Issues](https://github.com/mabdulwasii/retail-manager/issues)
- **Logs**: Check `{install-dir}/logs/shop-manager.log`

---

**Last Updated**: 2026-01-06
**Applies to**: Shop Manager v0.1.30+
