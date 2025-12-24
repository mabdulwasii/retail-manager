# Docker Compose Lite Deployment Guide

## Overview

**Docker Compose Lite** is a lightweight containerized deployment option for Shop Manager that combines the benefits of containerization with the low resource footprint of the embedded edition.

### Key Features

- ✅ **Lightweight**: ~1-1.5 GB RAM (vs 2-3 GB for full cloud version)
- ✅ **Containerized**: Easy deployment with Docker Compose
- ✅ **Offline-First**: Works 100% offline with optional cloud sync
- ✅ **Easy Updates**: Simple container updates via Docker
- ✅ **Portable**: Works on any system with Docker
- ✅ **Isolated**: Containers provide process isolation

### Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                    Docker Host Machine                       │
│                                                              │
│  ┌──────────────────────┐    ┌──────────────────────────┐   │
│  │  Frontend Container  │    │   Backend Container      │   │
│  │                      │    │                          │   │
│  │  - Nginx Alpine      │    │  - JRE 21 Alpine         │   │
│  │  - Static Files      │───▶│  - Embedded JAR          │   │
│  │  - 50-100 MB RAM     │    │  - H2 Database           │   │
│  │                      │    │  - 512 MB - 1 GB RAM     │   │
│  └──────────────────────┘    └──────────────────────────┘   │
│         :80                          :8081                   │
│          │                             │                     │
│  ┌───────┴─────────────────────────────┴──────────────────┐ │
│  │              shop-network-lite (bridge)                 │ │
│  └─────────────────────────────────────────────────────────┘ │
│                                                              │
│  ┌─────────────────────────────────────────────────────────┐│
│  │                    Volumes (./data)                      ││
│  │  - h2_data       (H2 database files)                    ││
│  │  - uploads_data  (File uploads)                         ││
│  │  - logs_data     (Application logs)                     ││
│  └─────────────────────────────────────────────────────────┘│
└──────────────────────────────────────────────────────────────┘
```

---

## System Requirements

### Minimum
- **OS**: Windows 10+, macOS 10.14+, Linux (Ubuntu 18.04+)
- **RAM**: 2 GB free
- **Storage**: 5 GB free space
- **Docker**: Docker Desktop 4.0+ or Docker Engine 20.10+
- **Docker Compose**: V2 (bundled with Docker Desktop)

### Recommended
- **OS**: Windows 11, macOS 12+, Linux (Ubuntu 20.04+)
- **RAM**: 4 GB free
- **Storage**: 10 GB free space
- **Docker**: Latest Docker Desktop or Docker Engine

---

## Quick Start

### 1. Prerequisites

**Install Docker**:
- **Windows/macOS**: Download [Docker Desktop](https://www.docker.com/products/docker-desktop/)
- **Linux**: Install [Docker Engine](https://docs.docker.com/engine/install/)

**Verify Installation**:
```bash
docker --version
docker compose version
```

### 2. Automated Setup

Run the initialization script:

```bash
./lite-init.sh
```

This script will:
1. Check prerequisites
2. Create directory structure
3. Generate secure JWT secret
4. Create `.env.lite` configuration
5. Build the embedded JAR
6. Verify setup

### 3. Start the Application

```bash
docker compose -f docker-compose-lite.yml --env-file .env.lite up -d
```

### 4. Access the Application

- **Frontend**: http://localhost:3001
- **Backend API**: http://localhost:8081
- **Health Check**: http://localhost:8081/actuator/health

### 5. Stop the Application

```bash
docker compose -f docker-compose-lite.yml down
```

---

## Manual Setup

If you prefer manual setup or the automated script doesn't work:

### Step 1: Build Embedded JAR

```bash
cd backend
./mvnw clean package -Pembedded -DskipTests
cd ..
```

### Step 2: Create Data Directories

```bash
mkdir -p data/h2 data/uploads data/logs data/backups
chmod 755 data data/h2 data/uploads data/logs data/backups
```

### Step 3: Configure Environment

Copy the template and edit:

```bash
cp .env.lite.template .env.lite
nano .env.lite
```

**Generate JWT Secret**:
```bash
openssl rand -base64 64
```

Paste the output into `JWT_SECRET` in `.env.lite`.

### Step 4: Start Services

```bash
docker compose -f docker-compose-lite.yml --env-file .env.lite up -d
```

---

## Configuration

### Environment Variables

Edit `.env.lite` to customize:

#### Basic Configuration

```bash
# Ports
BACKEND_PORT=8081
FRONTEND_PORT=3001

# Data directory
DATA_DIR=./data

# JWT Authentication
JWT_SECRET=<your-secure-secret-here>
```

#### Cloud Sync (Optional)

```bash
# Enable cloud sync
CLOUD_SYNC_ENABLED=true

# Cloud API endpoint
CLOUD_API_URL=https://cloud.shopmanager.com

# API key from cloud admin
CLOUD_API_KEY=sk_live_abc123...

# Unique store identifier
STORE_ID=STORE-001

# Sync schedule (hourly)
SYNC_CRON=0 0 * * * ?

# Anonymize customer data
ANONYMIZE_PII=true
```

#### Performance Tuning

```bash
# Increase memory for high-traffic stores
JAVA_OPTS=-Xms512m -Xmx1g -XX:+UseG1GC

# Decrease memory for low-traffic stores
JAVA_OPTS=-Xms128m -Xmx256m -XX:+UseG1GC
```

---

## Operations

### View Logs

**All services**:
```bash
docker compose -f docker-compose-lite.yml logs -f
```

**Specific service**:
```bash
docker compose -f docker-compose-lite.yml logs -f backend
docker compose -f docker-compose-lite.yml logs -f frontend
```

**Last 100 lines**:
```bash
docker compose -f docker-compose-lite.yml logs --tail=100
```

### Restart Services

**All services**:
```bash
docker compose -f docker-compose-lite.yml restart
```

**Specific service**:
```bash
docker compose -f docker-compose-lite.yml restart backend
```

### Update Application

**Pull latest images**:
```bash
docker compose -f docker-compose-lite.yml pull
docker compose -f docker-compose-lite.yml up -d
```

**Rebuild from source**:
```bash
# Rebuild embedded JAR
cd backend && ./mvnw clean package -Pembedded -DskipTests && cd ..

# Rebuild containers
docker compose -f docker-compose-lite.yml build --no-cache

# Restart
docker compose -f docker-compose-lite.yml up -d
```

### Check Service Health

```bash
docker compose -f docker-compose-lite.yml ps
```

Or check health endpoints:
```bash
curl http://localhost:8081/actuator/health
curl http://localhost:3001/health
```

---

## Data Management

### Backup

**Stop services**:
```bash
docker compose -f docker-compose-lite.yml down
```

**Backup data directory**:
```bash
tar -czf backup-$(date +%Y%m%d_%H%M%S).tar.gz data/
```

**Restart services**:
```bash
docker compose -f docker-compose-lite.yml up -d
```

### Restore

**Stop services**:
```bash
docker compose -f docker-compose-lite.yml down
```

**Restore data**:
```bash
tar -xzf backup-YYYYMMDD_HHMMSS.tar.gz
```

**Restart services**:
```bash
docker compose -f docker-compose-lite.yml up -d
```

### Automated Backups

Create a cron job (Linux/macOS):

```bash
crontab -e
```

Add:
```cron
# Daily backup at 2 AM
0 2 * * * cd /path/to/shop-manager && tar -czf backups/backup-$(date +\%Y\%m\%d).tar.gz data/ && find backups/ -name "backup-*.tar.gz" -mtime +7 -delete
```

---

## Monitoring

### Resource Usage

**Container stats**:
```bash
docker stats
```

**Disk usage**:
```bash
docker system df
```

**Volume sizes**:
```bash
du -sh data/*
```

### Application Metrics

Access Prometheus metrics:
```bash
curl http://localhost:8081/actuator/prometheus
```

---

## Troubleshooting

### Issue: Containers won't start

**Check logs**:
```bash
docker compose -f docker-compose-lite.yml logs
```

**Common causes**:
1. **Port conflicts**: Check if ports 3001/8081 are in use
   ```bash
   # Linux/macOS
   lsof -i :3001
   lsof -i :8081

   # Windows
   netstat -ano | findstr :3001
   netstat -ano | findstr :8081
   ```

2. **Insufficient memory**: Increase Docker memory limit in Docker Desktop settings

3. **Missing JAR**: Build embedded JAR first
   ```bash
   cd backend && ./mvnw package -Pembedded -DskipTests
   ```

### Issue: Database corruption

**Stop services**:
```bash
docker compose -f docker-compose-lite.yml down
```

**Restore from backup**:
```bash
rm -rf data/h2/*
tar -xzf backup-YYYYMMDD.tar.gz data/h2/
```

**Restart**:
```bash
docker compose -f docker-compose-lite.yml up -d
```

### Issue: High memory usage

**Check container memory**:
```bash
docker stats --no-stream
```

**Reduce JVM memory** in `.env.lite`:
```bash
JAVA_OPTS=-Xms128m -Xmx256m
```

**Restart**:
```bash
docker compose -f docker-compose-lite.yml restart backend
```

### Issue: Cloud sync failures

**Check sync logs**:
```bash
docker compose -f docker-compose-lite.yml logs backend | grep -i sync
```

**Test cloud connectivity**:
```bash
docker compose -f docker-compose-lite.yml exec backend curl -I https://cloud.shopmanager.com
```

**Verify API key** in `.env.lite`

---

## Security

### Best Practices

1. **Secure JWT Secret**:
   - Generate with `openssl rand -base64 64`
   - Never commit to version control
   - Rotate every 90 days

2. **File Permissions**:
   ```bash
   chmod 600 .env.lite
   chmod 755 data
   ```

3. **Network Isolation**:
   - Use Docker's internal network
   - Only expose necessary ports

4. **Regular Updates**:
   ```bash
   docker compose -f docker-compose-lite.yml pull
   docker compose -f docker-compose-lite.yml up -d
   ```

5. **Disable H2 Console** in production:
   ```bash
   H2_CONSOLE_ENABLED=false
   ```

### Firewall Configuration

**Allow only local access**:
```bash
# Linux (UFW)
sudo ufw allow from 127.0.0.1 to any port 8081
sudo ufw allow from 127.0.0.1 to any port 3001

# Or allow specific network
sudo ufw allow from 192.168.1.0/24 to any port 8081
sudo ufw allow from 192.168.1.0/24 to any port 3001
```

---

## Performance Optimization

### For High-Traffic Stores

**.env.lite**:
```bash
# Increase memory
JAVA_OPTS=-Xms512m -Xmx1g -XX:+UseG1GC -XX:MaxGCPauseMillis=200

# More aggressive GC tuning
JAVA_OPTS=-Xms1g -Xmx2g -XX:+UseG1GC -XX:ParallelGCThreads=4
```

### For Low-Memory Systems

**.env.lite**:
```bash
# Minimum memory
JAVA_OPTS=-Xms128m -Xmx256m -XX:+UseSerialGC
```

---

## Migration

### From JAR to Docker Lite

1. **Stop JAR application**
2. **Copy H2 database**:
   ```bash
   cp -r /path/to/jar/data/shopmanager.mv.db data/h2/
   ```
3. **Copy uploads**:
   ```bash
   cp -r /path/to/jar/data/uploads/* data/uploads/
   ```
4. **Start Docker Lite**:
   ```bash
   docker compose -f docker-compose-lite.yml up -d
   ```

### From Docker Lite to Full Cloud

1. **Export data** (documented in cloud migration guide)
2. **Deploy cloud version**
3. **Import data**
4. **Verify**

---

## Support

### Documentation
- [Embedded Deployment Guide](./EMBEDDED_DEPLOYMENT.md)
- [Cloud Sync Setup](./CLOUD_SYNC_SETUP.md)
- [Quick Start Guide](./DOCKER_LITE_QUICKSTART.md)

### Community
- GitHub Issues: https://github.com/yourorg/shop-manager/issues
- Forum: https://forum.shopmanager.com
- Email: support@shopmanager.com

---

**Last Updated**: 2025-12-24
