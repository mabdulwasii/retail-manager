# Embedded Deployment Guide

## Overview

The **Shop Manager Embedded Edition** is a lightweight, standalone version designed for deployment on:
- Customer laptops and desktops
- Small retail stores with limited IT infrastructure
- Offline-first scenarios
- Low-memory environments (4-8 GB RAM)

This guide covers deployment, configuration, and management of the embedded edition.

---

## Architecture

### Components

The embedded edition replaces heavy cloud services with lightweight alternatives:

| Cloud Edition | Embedded Edition | Memory Savings |
|---------------|------------------|----------------|
| PostgreSQL (400 MB) | H2 Database (50 MB) | 350 MB |
| Keycloak (800 MB) | Local JWT Auth (5 MB) | 795 MB |
| Kafka (700 MB) | Spring Events (included) | 700 MB |
| MinIO (200 MB) | File System Storage (included) | 200 MB |
| **Total: ~2.1 GB** | **Total: ~500 MB** | **1.6 GB** |

### Key Features

- ✅ **Offline-First**: Works 100% offline with optional cloud sync
- ✅ **Low Memory**: Runs on 512 MB-1 GB RAM (recommended 2 GB)
- ✅ **Single JAR**: All-in-one executable JAR file
- ✅ **Auto-Updates**: Optional cloud sync for transaction backup
- ✅ **Local Data**: All data stored locally in `./data` directory
- ✅ **Simple Setup**: No external services required

---

## System Requirements

### Minimum
- **OS**: Windows 10+, macOS 10.14+, Linux (Ubuntu 18.04+)
- **RAM**: 512 MB available
- **Storage**: 500 MB free space
- **Java**: JRE 21+

### Recommended
- **OS**: Windows 11, macOS 12+, Linux (Ubuntu 20.04+)
- **RAM**: 2 GB available
- **Storage**: 2 GB free space (for transaction data growth)
- **Java**: JRE 21+ (bundled in installer versions)

---

## Installation

### Option 1: JAR File (All Platforms)

1. **Download** the embedded JAR:
   ```bash
   wget https://github.com/yourorg/shop-manager/releases/latest/shop-manager-embedded.jar
   ```

2. **Create configuration** file (optional):
   ```bash
   # Create .env file for custom settings
   cat > .env <<EOF
   SERVER_PORT=8081
   STORAGE_PATH=./data/uploads
   JWT_SECRET=your-secure-random-generated-secret-key-256-bits
   EOF
   ```

3. **Run** the application:
   ```bash
   java -jar shop-manager-embedded.jar
   ```

4. **Access** the application:
   - URL: http://localhost:8081
   - Default admin credentials: admin / admin (change immediately)

### Option 2: Windows Installer

1. Download `shop-manager-setup.exe`
2. Run installer with administrator privileges
3. Follow installation wizard
4. Launch from Start Menu or Desktop shortcut

### Option 3: macOS Installer

1. Download `shop-manager.dmg`
2. Open DMG and drag to Applications
3. Right-click and select "Open" (first time only)
4. Launch from Applications folder

### Option 4: Linux Package

**Debian/Ubuntu (.deb)**:
```bash
sudo dpkg -i shop-manager_1.0.0_amd64.deb
sudo systemctl start shop-manager
sudo systemctl enable shop-manager
```

**RedHat/Fedora (.rpm)**:
```bash
sudo rpm -i shop-manager-1.0.0.x86_64.rpm
sudo systemctl start shop-manager
sudo systemctl enable shop-manager
```

**AppImage (Universal)**:
```bash
chmod +x shop-manager-1.0.0.AppImage
./shop-manager-1.0.0.AppImage
```

---

## Configuration

### Environment Variables

| Variable | Default | Description |
|----------|---------|-------------|
| `SERVER_PORT` | 8081 | HTTP server port |
| `STORAGE_PATH` | ./data/uploads | File storage location |
| `JWT_SECRET` | (required) | Secret key for JWT tokens (256+ bits) |
| `JWT_EXPIRATION_MS` | 86400000 | JWT token expiration (24 hours) |
| `CLOUD_SYNC_ENABLED` | false | Enable cloud sync |
| `CLOUD_API_URL` | - | Cloud API endpoint |
| `CLOUD_API_KEY` | - | API key for cloud access |
| `STORE_ID` | - | Unique store identifier |
| `SYNC_CRON` | 0 0 * * * ? | Sync schedule (hourly) |

### Configuration File

Create `application-custom.yml`:

```yaml
application:
  mode: embedded

  jwt:
    secret: ${JWT_SECRET}
    expiration-ms: 86400000

  storage:
    type: filesystem
    location: ${STORAGE_PATH:./data/uploads}
    max-file-size: 10485760

  sync:
    enabled: ${CLOUD_SYNC_ENABLED:false}
    cloud-endpoint: ${CLOUD_API_URL}
    api-key: ${CLOUD_API_KEY}
    store-id: ${STORE_ID}
    schedule:
      cron: "0 0 * * * ?"
      batch-size: 100
```

---

## Cloud Sync Setup

### Prerequisites

1. Cloud installation of Shop Manager
2. API key from cloud admin
3. Unique store identifier

### Configuration Steps

1. **Enable Cloud Sync** in `.env`:
   ```bash
   CLOUD_SYNC_ENABLED=true
   CLOUD_API_URL=https://your-cloud-instance.com
   CLOUD_API_KEY=your-api-key-here
   STORE_ID=STORE-001
   ```

2. **Configure Sync Schedule**:
   ```yaml
   application:
     sync:
       schedule:
         cron: "0 0 * * * ?"  # Every hour
         batch-size: 100
         retry-max-attempts: 3
   ```

3. **Privacy Settings** (optional):
   ```yaml
   application:
     sync:
       privacy:
         anonymize-pii: true
         fields-to-anonymize:
           - customerName
           - customerPhone
           - customerEmail
   ```

4. **Test Connection**:
   ```bash
   curl -X POST http://localhost:8081/api/sync/test \
     -H "Authorization: Bearer YOUR_JWT_TOKEN"
   ```

### Sync Behavior

- **Offline-First**: Store works 100% offline
- **Automatic Sync**: Runs on schedule (default: hourly)
- **Incremental**: Only new/modified transactions synced
- **Resilient**: Auto-retry on failures with exponential backoff
- **Secure**: TLS encrypted, API key authenticated

---

## Data Management

### Backup

**Manual Backup**:
```bash
# Stop application
# Copy data directory
cp -r ./data ./backup-$(date +%Y%m%d)
# Restart application
```

**Automated Backup Script** (Linux/macOS):
```bash
#!/bin/bash
DATA_DIR="./data"
BACKUP_DIR="./backups"
DATE=$(date +%Y%m%d_%H%M%S)

mkdir -p "$BACKUP_DIR"
tar -czf "$BACKUP_DIR/backup-$DATE.tar.gz" "$DATA_DIR"

# Keep only last 7 days
find "$BACKUP_DIR" -name "backup-*.tar.gz" -mtime +7 -delete
```

### Restore

```bash
# Stop application
# Extract backup
tar -xzf backup-20250101_120000.tar.gz
# Restart application
```

### Database Location

- **File**: `./data/shopmanager.mv.db`
- **Format**: H2 Database (PostgreSQL mode)
- **Access**: H2 Console at http://localhost:8081/h2-console (disabled by default)

---

## Security

### Default Credentials

**IMPORTANT**: Change default credentials immediately after first login!

- Username: `admin`
- Password: `admin`

### JWT Secret

Generate a secure secret:

```bash
# Linux/macOS
openssl rand -base64 64

# Windows (PowerShell)
[Convert]::ToBase64String((1..64 | ForEach-Object { Get-Random -Maximum 256 }))
```

### File Permissions

```bash
# Restrict data directory
chmod 700 ./data

# Restrict configuration
chmod 600 .env
```

### Firewall

```bash
# Allow only local access
sudo ufw allow from 127.0.0.1 to any port 8081

# Or specific network
sudo ufw allow from 192.168.1.0/24 to any port 8081
```

---

## Troubleshooting

### Application Won't Start

1. **Check Java version**:
   ```bash
   java -version  # Should be 21+
   ```

2. **Check port availability**:
   ```bash
   # Linux/macOS
   lsof -i :8081

   # Windows
   netstat -ano | findstr :8081
   ```

3. **Check logs**:
   ```bash
   tail -f ./logs/shop-manager.log
   ```

### Database Corruption

1. **Stop application**
2. **Restore from backup**:
   ```bash
   cp ./backup/shopmanager.mv.db ./data/
   ```
3. **Restart application**

### Cloud Sync Failures

1. **Check connectivity**:
   ```bash
   curl -I https://your-cloud-instance.com
   ```

2. **Verify API key**:
   ```bash
   curl -H "X-API-Key: YOUR_KEY" \
     https://your-cloud-instance.com/api/health
   ```

3. **Review sync logs**:
   ```sql
   SELECT * FROM cloud_sync_logs
   WHERE status = 'FAILED'
   ORDER BY sync_start_time DESC LIMIT 10;
   ```

### Performance Issues

1. **Increase JVM memory**:
   ```bash
   java -Xmx2g -jar shop-manager-embedded.jar
   ```

2. **Disable cloud sync** temporarily:
   ```bash
   CLOUD_SYNC_ENABLED=false java -jar shop-manager-embedded.jar
   ```

3. **Clean old data** (if database is large):
   ```sql
   -- Archive old transactions (older than 1 year)
   DELETE FROM sales_transactions
   WHERE transaction_date < NOW() - INTERVAL '1 year';
   ```

---

## Maintenance

### Updates

**Manual Update**:
1. Download latest JAR
2. Stop application
3. Backup data directory
4. Replace JAR file
5. Start application

**Auto-Update** (if enabled):
- Application checks for updates on startup
- Downloads in background
- Prompts for restart when ready

### Database Maintenance

```bash
# Compact database
java -cp h2*.jar org.h2.tools.Compress -file ./data/shopmanager -db shopmanager

# Analyze and optimize
java -cp h2*.jar org.h2.tools.RunScript -url jdbc:h2:file:./data/shopmanager -script analyze.sql
```

### Log Rotation

Logs auto-rotate:
- **Max Size**: 10 MB per file
- **Retention**: 7 days
- **Location**: `./logs/shop-manager.log`

---

## Support

### Documentation
- [User Guide](./USER_GUIDE.md)
- [Cloud Sync Guide](./CLOUD_SYNC_SETUP.md)
- [API Documentation](http://localhost:8081/swagger-ui.html)

### Community
- GitHub Issues: https://github.com/yourorg/shop-manager/issues
- Forum: https://forum.shopmanager.com
- Email: support@shopmanager.com

---

## Appendix

### Build from Source

```bash
# Clone repository
git clone https://github.com/yourorg/shop-manager.git
cd shop-manager

# Build embedded JAR
./mvnw clean package -Pembedded -DskipTests

# Output: target/shop-manager-1.0.0-embedded.jar
```

### Development Mode

```bash
# Run with embedded profile
./mvnw spring-boot:run -Dspring-boot.run.profiles=embedded
```

### H2 Console Access

Enable in `application-embedded.yml`:

```yaml
spring:
  h2:
    console:
      enabled: true
      path: /h2-console
```

Access at: http://localhost:8081/h2-console

- **URL**: `jdbc:h2:file:./data/shopmanager`
- **Username**: `sa`
- **Password**: (empty)

---

**Last Updated**: 2025-12-24
