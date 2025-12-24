# Linux Package Build Guide

This directory contains the configuration for building Linux packages (.deb, .rpm, AppImage) for Shop Manager.

## Prerequisites

1. **Linux Build Machine** (Ubuntu 20.04+, RHEL 8+, or Fedora 35+)
2. **Java JDK 21** - For running the application
3. **Package Build Tools**:
   - For .deb: `sudo apt-get install dpkg`
   - For .rpm: `sudo yum install rpm-build` (RHEL/Fedora) or `sudo apt-get install rpm` (Debian/Ubuntu)
4. **Embedded JAR** - Built using `mvnw package -Pembedded`

## Directory Structure

```
installers/linux/
├── build-packages.sh          # Package builder script
├── scripts/
│   └── shop-manager          # Linux launcher script
├── config/
│   ├── .env.template         # Environment template
│   └── application.yml       # Spring Boot config
├── assets/
│   └── shop-manager.png      # Application icon (256x256px)
└── README.md                 # This file
```

## Building Packages

### Quick Start

```bash
# Make script executable
chmod +x build-packages.sh

# Build all packages
./build-packages.sh
```

The script will:
1. Check prerequisites
2. Build .deb package (if dpkg-deb available)
3. Build .rpm package (if rpmbuild available)
4. Build AppImage tarball
5. Output packages to `../../build/installers/linux/`

### Build Specific Package Types

#### Debian/Ubuntu (.deb)

```bash
# Install build tools
sudo apt-get install dpkg

# Build
./build-packages.sh
# Creates: shop-manager_1.0.0_all.deb
```

#### RHEL/Fedora (.rpm)

```bash
# Install build tools
sudo yum install rpm-build

# Build
./build-packages.sh
# Creates: shop-manager-1.0.0-1.*.rpm
```

#### AppImage

```bash
# Build
./build-packages.sh
# Creates: shop-manager-1.0.0-x86_64.AppImage.tar.gz
```

## Package Contents

### Installation Locations

#### System Installation (.deb/.rpm)

```
/opt/shop-manager/
├── lib/
│   └── shop-manager.jar              # Embedded JAR
└── bin/
    └── shop-manager                  # Launcher script

/etc/shop-manager/
├── shop-manager.env                  # Configuration
└── application.yml                   # Spring Boot config

/var/lib/shop-manager/
└── data/
    ├── h2/                           # Database files
    ├── uploads/                      # File uploads
    ├── logs/                         # Application logs
    └── backups/                      # Backup storage

/usr/share/applications/
└── shop-manager.desktop              # Desktop entry

/lib/systemd/system/
└── shop-manager.service              # Systemd service
```

#### User Installation (AppImage)

```
~/.shopmanager/
├── shop-manager.env                  # Configuration
├── shop-manager.pid                  # Process ID
└── data/
    ├── h2/                           # Database files
    ├── uploads/                      # File uploads
    ├── logs/                         # Application logs
    └── backups/                      # Backup storage
```

## Installation

### Debian/Ubuntu (.deb)

```bash
# Install package
sudo dpkg -i shop-manager_1.0.0_all.deb

# If dependencies missing
sudo apt-get install -f

# Start service
sudo systemctl start shop-manager

# Enable auto-start
sudo systemctl enable shop-manager

# Check status
sudo systemctl status shop-manager
```

### RHEL/Fedora (.rpm)

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

### AppImage

```bash
# Extract tarball
tar -xzf shop-manager-1.0.0-x86_64.AppImage.tar.gz

# Run launcher
./shop-manager.AppDir/usr/bin/shop-manager

# Or make it executable
chmod +x shop-manager.AppDir/AppRun
./shop-manager.AppDir/AppRun
```

## Post-Installation

### For System Installations

The installation script automatically:
- ✅ Creates `shopmanager` system user
- ✅ Creates data directories in `/var/lib/shop-manager/data/`
- ✅ Generates secure JWT secret
- ✅ Sets proper file permissions
- ✅ Registers systemd service

**Access the application:**
```bash
# Check health
curl http://localhost:8081/actuator/health

# Open in browser
xdg-open http://localhost:8081
```

### Configuration

Edit the configuration file:

```bash
# System installation
sudo nano /etc/shop-manager/shop-manager.env

# User installation
nano ~/.shopmanager/shop-manager.env
```

**Common settings:**
```bash
BACKEND_PORT=8081
CLOUD_SYNC_ENABLED=false
JAVA_OPTS=-Xms256m -Xmx512m
```

After editing, restart the service:
```bash
sudo systemctl restart shop-manager
```

## Systemd Service Management

### Start/Stop/Restart

```bash
# Start
sudo systemctl start shop-manager

# Stop
sudo systemctl stop shop-manager

# Restart
sudo systemctl restart shop-manager

# Reload configuration
sudo systemctl reload shop-manager
```

### Enable/Disable Auto-Start

```bash
# Enable auto-start on boot
sudo systemctl enable shop-manager

# Disable auto-start
sudo systemctl disable shop-manager
```

### View Status and Logs

```bash
# Check status
sudo systemctl status shop-manager

# View logs (last 100 lines)
sudo journalctl -u shop-manager -n 100

# Follow logs in real-time
sudo journalctl -u shop-manager -f

# View application log file
sudo tail -f /var/lib/shop-manager/data/logs/shop-manager.log
```

## Desktop Integration

After installation, Shop Manager appears in the application menu:

**Categories:** Office → Finance

**Launch from terminal:**
```bash
/opt/shop-manager/bin/shop-manager
```

**Launch from desktop:**
Search for "Shop Manager" in application launcher.

## Uninstallation

### Debian/Ubuntu

```bash
# Stop service
sudo systemctl stop shop-manager
sudo systemctl disable shop-manager

# Remove package
sudo dpkg -r shop-manager

# Remove with configuration
sudo dpkg -P shop-manager

# Remove data (optional)
sudo rm -rf /var/lib/shop-manager
```

### RHEL/Fedora

```bash
# Stop service
sudo systemctl stop shop-manager
sudo systemctl disable shop-manager

# Remove package
sudo rpm -e shop-manager

# Or with yum
sudo yum remove shop-manager

# Remove data (optional)
sudo rm -rf /var/lib/shop-manager
```

### AppImage

```bash
# Stop application
pkill -f "shop-manager.jar"

# Remove files
rm -rf shop-manager.AppDir
rm -rf ~/.shopmanager  # User data
```

## Troubleshooting

### Java Not Found

**Error:** "Java 21 or higher is required"

**Solution:**
```bash
# Ubuntu/Debian
sudo apt-get update
sudo apt-get install openjdk-21-jre-headless

# RHEL/Fedora
sudo yum install java-21-openjdk-headless

# Verify
java -version
```

### Service Won't Start

**Check logs:**
```bash
sudo journalctl -u shop-manager -n 50 --no-pager
```

**Common issues:**
1. **Port already in use**
   ```bash
   sudo lsof -i :8081
   # Change port in /etc/shop-manager/shop-manager.env
   ```

2. **Permission denied**
   ```bash
   sudo chown -R shopmanager:shopmanager /var/lib/shop-manager
   sudo chmod 600 /etc/shop-manager/shop-manager.env
   ```

3. **Invalid JWT secret**
   ```bash
   # Regenerate secret
   JWT_SECRET=$(openssl rand -base64 64 | tr -d '\n')
   sudo sed -i "s|JWT_SECRET=.*|JWT_SECRET=$JWT_SECRET|" /etc/shop-manager/shop-manager.env
   sudo systemctl restart shop-manager
   ```

### High Memory Usage

**Reduce memory in configuration:**
```bash
sudo nano /etc/shop-manager/shop-manager.env

# Change to:
JAVA_OPTS=-Xms128m -Xmx256m -XX:+UseG1GC

sudo systemctl restart shop-manager
```

### Database Corruption

**Restore from backup:**
```bash
sudo systemctl stop shop-manager
sudo rm -rf /var/lib/shop-manager/data/h2/*
sudo tar -xzf /var/lib/shop-manager/data/backups/backup-YYYYMMDD.tar.gz -C /
sudo chown -R shopmanager:shopmanager /var/lib/shop-manager/data
sudo systemctl start shop-manager
```

## Advanced Configuration

### Custom Data Directory

Edit systemd service:
```bash
sudo systemctl edit shop-manager

# Add override:
[Service]
Environment="DATA_DIR=/custom/path/data"

sudo systemctl daemon-reload
sudo systemctl restart shop-manager
```

### Firewall Configuration

#### UFW (Ubuntu)
```bash
sudo ufw allow 8081/tcp
sudo ufw reload
```

#### firewalld (RHEL/Fedora)
```bash
sudo firewall-cmd --permanent --add-port=8081/tcp
sudo firewall-cmd --reload
```

### Reverse Proxy with Nginx

```nginx
server {
    listen 80;
    server_name shop.example.com;

    location / {
        proxy_pass http://localhost:8081;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }
}
```

### SSL/TLS with Let's Encrypt

```bash
# Install certbot
sudo apt-get install certbot python3-certbot-nginx  # Ubuntu
sudo yum install certbot python3-certbot-nginx      # RHEL

# Obtain certificate
sudo certbot --nginx -d shop.example.com

# Auto-renewal
sudo systemctl enable certbot.timer
```

## Backup and Restore

### Automated Backup

Create cron job:
```bash
sudo crontab -e

# Add daily backup at 2 AM
0 2 * * * systemctl stop shop-manager && tar -czf /var/lib/shop-manager/data/backups/backup-$(date +\%Y\%m\%d).tar.gz /var/lib/shop-manager/data && systemctl start shop-manager && find /var/lib/shop-manager/data/backups -name "backup-*.tar.gz" -mtime +7 -delete
```

### Manual Backup

```bash
sudo systemctl stop shop-manager
sudo tar -czf ~/shop-manager-backup-$(date +%Y%m%d).tar.gz /var/lib/shop-manager/data
sudo systemctl start shop-manager
```

### Restore

```bash
sudo systemctl stop shop-manager
sudo tar -xzf ~/shop-manager-backup-YYYYMMDD.tar.gz -C /
sudo chown -R shopmanager:shopmanager /var/lib/shop-manager/data
sudo systemctl start shop-manager
```

## Building from CI/CD

GitHub Actions workflow will build Linux packages on Ubuntu runners.

See: `.github/workflows/build-standalone-release.yml`

```yaml
build-linux-packages:
  runs-on: ubuntu-latest
  steps:
    - name: Install build tools
      run: |
        sudo apt-get update
        sudo apt-get install -y dpkg rpm

    - name: Build packages
      run: |
        cd installers/linux
        chmod +x build-packages.sh
        ./build-packages.sh

    - name: Upload packages
      uses: actions/upload-artifact@v3
      with:
        name: linux-packages
        path: build/installers/linux/*
```

## Distribution

### GitHub Releases

```bash
gh release upload v1.0.0 \
    build/installers/linux/shop-manager_1.0.0_all.deb \
    build/installers/linux/shop-manager-1.0.0-1.*.rpm \
    build/installers/linux/shop-manager-1.0.0-x86_64.AppImage.tar.gz
```

### APT Repository (Optional)

Create Debian repository for easy installation:

```bash
# On repository server
dpkg-scanpackages . /dev/null | gzip -9c > Packages.gz

# On client
echo "deb [trusted=yes] https://repo.example.com/apt /" | sudo tee /etc/apt/sources.list.d/shop-manager.list
sudo apt-get update
sudo apt-get install shop-manager
```

### YUM Repository (Optional)

Create RPM repository:

```bash
# On repository server
createrepo /path/to/rpms

# On client
sudo cat > /etc/yum.repos.d/shop-manager.repo <<EOF
[shop-manager]
name=Shop Manager
baseurl=https://repo.example.com/yum
enabled=1
gpgcheck=0
EOF

sudo yum install shop-manager
```

## License

See [LICENSE](../../LICENSE) file in project root.

## Support

- **Documentation**: [docs/EMBEDDED_DEPLOYMENT.md](../../docs/EMBEDDED_DEPLOYMENT.md)
- **Issues**: https://github.com/yourorg/shop-manager/issues
- **Email**: support@shopmanager.com

---

**Last Updated**: 2025-12-24
