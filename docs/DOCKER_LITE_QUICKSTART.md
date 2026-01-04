# Docker Compose Lite - Quick Start Guide

## 🚀 Get Started in 5 Minutes

This guide will get you up and running with Shop Manager Docker Lite in just a few minutes.

---

## Prerequisites ✅

- **Docker Desktop** (Windows/macOS) or **Docker Engine** (Linux)
- **4 GB RAM** available
- **5 GB** disk space

**Don't have Docker?** [Download Docker Desktop](https://www.docker.com/products/docker-desktop/)

---

## Installation Steps

### 1. Navigate to Project Directory

```bash
cd /path/to/shop-manager
```

### 2. Run Setup Script

```bash
./lite-init.sh
```

This will:
- ✅ Check prerequisites
- ✅ Create data directories
- ✅ Generate secure JWT secret
- ✅ Build embedded JAR
- ✅ Create configuration file

### 3. Start Shop Manager

```bash
docker compose -f docker-compose-lite.yml --env-file .env.lite up -d
```

### 4. Access the Application

Open your browser:
- **Shop Manager**: http://localhost:3001
- **API**: http://localhost:8081

**Default Login**:
- Username: `admin`
- Password: `admin` (change immediately!)

---

## Basic Commands

### Start

```bash
docker compose -f docker-compose-lite.yml --env-file .env.lite up -d
```

### Stop

```bash
docker compose -f docker-compose-lite.yml down
```

### View Logs

```bash
docker compose -f docker-compose-lite.yml logs -f
```

### Restart

```bash
docker compose -f docker-compose-lite.yml restart
```

### Check Status

```bash
docker compose -f docker-compose-lite.yml ps
```

---

## Common Tasks

### Enable Cloud Sync

1. **Edit configuration**:
   ```bash
   nano .env.lite
   ```

2. **Update settings**:
   ```bash
   CLOUD_SYNC_ENABLED=true
   CLOUD_API_URL=https://your-cloud-instance.com
   CLOUD_API_KEY=your-api-key-here
   STORE_ID=STORE-001
   ```

3. **Restart backend**:
   ```bash
   docker compose -f docker-compose-lite.yml restart backend
   ```

### Backup Data

```bash
# Stop services
docker compose -f docker-compose-lite.yml down

# Create backup
tar -czf backup-$(date +%Y%m%d).tar.gz data/

# Restart
docker compose -f docker-compose-lite.yml up -d
```

### Restore Data

```bash
# Stop services
docker compose -f docker-compose-lite.yml down

# Restore from backup
tar -xzf backup-YYYYMMDD.tar.gz

# Restart
docker compose -f docker-compose-lite.yml up -d
```

### Update Application

```bash
# Rebuild JAR
cd backend && ./mvnw clean package -Pembedded -DskipTests && cd ..

# Rebuild containers
docker compose -f docker-compose-lite.yml build

# Restart
docker compose -f docker-compose-lite.yml up -d
```

---

## Troubleshooting

### Application won't start

**Check logs**:
```bash
docker compose -f docker-compose-lite.yml logs
```

**Verify Docker is running**:
```bash
docker info
```

### Port already in use

**Change ports** in `.env.lite`:
```bash
BACKEND_PORT=8082
FRONTEND_PORT=3002
```

Then restart:
```bash
docker compose -f docker-compose-lite.yml up -d
```

### Out of memory

**Reduce memory** in `.env.lite`:
```bash
JAVA_OPTS=-Xms128m -Xmx256m
```

Restart backend:
```bash
docker compose -f docker-compose-lite.yml restart backend
```

### Can't access application

1. **Check if containers are running**:
   ```bash
   docker compose -f docker-compose-lite.yml ps
   ```

2. **Check health**:
   ```bash
   curl http://localhost:8081/actuator/health
   ```

3. **Check firewall settings**

---

## File Structure

```
shop-manager/
├── docker-compose-lite.yml    # Lite deployment config
├── .env.lite                  # Your configuration (DO NOT COMMIT)
├── .env.lite.template         # Template for .env.lite
├── lite-init.sh              # Setup script
├── backend/
│   ├── Dockerfile.lite       # Backend container config
│   └── target/
│       └── *-embedded.jar    # Embedded JAR
├── frontend/
│   ├── Dockerfile.lite       # Frontend container config
│   └── nginx.lite.conf       # Nginx config
└── data/                     # Persistent data
    ├── h2/                   # Database files
    ├── uploads/              # File uploads
    ├── logs/                 # Application logs
    └── backups/              # Backup storage
```

---

## Security Tips

1. **Change default password** immediately after first login

2. **Secure JWT secret**:
   ```bash
   # Generate new secret
   openssl rand -base64 64

   # Update .env.lite
   JWT_SECRET=<paste-generated-secret>
   ```

3. **Protect configuration**:
   ```bash
   chmod 600 .env.lite
   ```

4. **Regular backups**:
   - Set up automated daily backups
   - Store backups in secure location

5. **Disable H2 console** in production:
   ```bash
   H2_CONSOLE_ENABLED=false
   ```

---

## Next Steps

📖 **Learn More**:
- [Full Deployment Guide](./DOCKER_LITE_DEPLOYMENT.md)
- [Cloud Sync Setup](./CLOUD_SYNC_SETUP.md)
- [Embedded Deployment](./EMBEDDED_DEPLOYMENT.md)

💬 **Get Help**:
- [GitHub Issues](https://github.com/yourorg/shop-manager/issues)
- [Community Forum](https://forum.shopmanager.com)
- Email: support@shopmanager.com

---

## Performance Comparison

| Deployment | RAM Usage | Startup Time | Complexity |
|------------|-----------|--------------|------------|
| **Docker Lite** | 1-1.5 GB | 30-60s | ⭐⭐ Easy |
| Embedded JAR | 500 MB | 10-20s | ⭐ Very Easy |
| Full Cloud | 2-3 GB | 2-3 min | ⭐⭐⭐⭐ Complex |

---

**Ready to scale?** See [Full Cloud Deployment](../DEPLOYMENT_GUIDE.md) for production-grade setup.

---

**Last Updated**: 2025-12-24
