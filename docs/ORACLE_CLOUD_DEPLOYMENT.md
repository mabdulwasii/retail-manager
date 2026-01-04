# Oracle Cloud Deployment Guide

Deploy RetailHQ Cloud Aggregator to Oracle Cloud Infrastructure (OCI) using the Always Free tier.

---

## Overview

This guide covers deploying RetailHQ's **Cloud Aggregator API** to Oracle Cloud Infrastructure's Always Free tier, providing:

- **Zero cost** production deployment
- **99%+ uptime** with enterprise-grade infrastructure
- **2 AMD VMs** (1/8 OCPU each, 1 GB RAM each)
- **200 GB total storage** (2x 100 GB boot volumes)
- **10 GB outbound** data transfer per month
- **Public IP addresses** included

**Target URL**: `https://api.retailhq.app`

---

## Architecture

```
┌────────────────────────────────────────────────────────────┐
│                    Oracle Cloud (OCI)                       │
│  ┌──────────────────────────────────────────────────────┐  │
│  │              Load Balancer (Free tier)               │  │
│  │            SSL/TLS Termination (Let's Encrypt)       │  │
│  └────────────────────┬─────────────────────────────────┘  │
│                       │                                     │
│         ┌─────────────┴──────────────┐                     │
│         │                            │                     │
│  ┌──────▼──────┐              ┌──────▼──────┐             │
│  │   VM 1      │              │   VM 2      │             │
│  │  (Backend)  │              │ (Database)  │             │
│  │  1/8 OCPU   │              │  1/8 OCPU   │             │
│  │   1 GB RAM  │◄─────────────┤   1 GB RAM  │             │
│  │  100 GB SSD │              │  100 GB SSD │             │
│  └─────────────┘              └─────────────┘             │
│                                                             │
│  Public IP: xxx.xxx.xxx.xxx                                │
│  DNS: api.retailhq.app → xxx.xxx.xxx.xxx                  │
└────────────────────────────────────────────────────────────┘
         ▲                    ▲                    ▲
         │                    │                    │
   ┌─────┴────┐         ┌─────┴────┐         ┌────┴─────┐
   │ Store 1  │         │ Store 2  │         │ Store 3  │
   │(Embedded)│         │ (Docker) │         │(Windows) │
   └──────────┘         └──────────┘         └──────────┘
```

---

## Prerequisites

### 1. Oracle Cloud Account

1. Sign up at [cloud.oracle.com](https://cloud.oracle.com/)
2. Verify email and phone number
3. Add payment method (required even for free tier, but won't be charged)
4. Wait for account activation (~30 minutes)

### 2. Local Tools

```bash
# Install Oracle Cloud CLI
bash -c "$(curl -L https://raw.githubusercontent.com/oracle/oci-cli/master/scripts/install/install.sh)"

# Verify installation
oci --version

# Install kubectl (if not already installed)
brew install kubectl  # macOS
# OR
curl -LO "https://dl.k8s.io/release/$(curl -L -s https://dl.k8s.io/release/stable.txt)/bin/linux/amd64/kubectl"

# Install Helm (if not already installed)
brew install helm  # macOS
# OR
curl https://raw.githubusercontent.com/helm/helm/main/scripts/get-helm-3 | bash
```

### 3. Domain Name

- Purchase domain: **retailhq.app** (or your preferred domain)
- Access to DNS management (Cloudflare, Route53, etc.)

---

## Part 1: Oracle Cloud Setup

### Step 1.1: Create Compartment

```bash
# Login to OCI CLI
oci session authenticate

# Create compartment for RetailHQ
oci iam compartment create \
  --name "retailhq-production" \
  --description "RetailHQ Cloud Aggregator Production Environment" \
  --compartment-id <YOUR_TENANCY_OCID>

# Note the compartment OCID from output
```

### Step 1.2: Create Virtual Cloud Network (VCN)

1. **Console**: OCI Console → Networking → Virtual Cloud Networks
2. **Click**: "Start VCN Wizard"
3. **Select**: "Create VCN with Internet Connectivity"
4. **Configure**:
   - Name: `retailhq-vcn`
   - Compartment: `retailhq-production`
   - VCN CIDR: `10.0.0.0/16`
   - Public Subnet CIDR: `10.0.0.0/24`
   - Private Subnet CIDR: `10.0.1.0/24`
5. **Create**: Review and create

**Or via CLI**:

```bash
# Create VCN
oci network vcn create \
  --compartment-id <COMPARTMENT_OCID> \
  --display-name "retailhq-vcn" \
  --cidr-block "10.0.0.0/16"

# Create Internet Gateway
oci network internet-gateway create \
  --compartment-id <COMPARTMENT_OCID> \
  --vcn-id <VCN_OCID> \
  --display-name "retailhq-igw" \
  --is-enabled true

# Create Public Subnet
oci network subnet create \
  --compartment-id <COMPARTMENT_OCID> \
  --vcn-id <VCN_OCID> \
  --display-name "retailhq-public-subnet" \
  --cidr-block "10.0.0.0/24" \
  --route-table-id <ROUTE_TABLE_OCID> \
  --security-list-ids '["<SECURITY_LIST_OCID>"]'
```

### Step 1.3: Configure Security Lists

Add ingress rules for HTTP/HTTPS traffic:

```bash
# Allow HTTP (port 80)
oci network security-list-entry add \
  --security-list-id <SECURITY_LIST_OCID> \
  --protocol 6 \
  --source "0.0.0.0/0" \
  --destination-port-range-min 80 \
  --destination-port-range-max 80

# Allow HTTPS (port 443)
oci network security-list-entry add \
  --security-list-id <SECURITY_LIST_OCID> \
  --protocol 6 \
  --source "0.0.0.0/0" \
  --destination-port-range-min 443 \
  --destination-port-range-max 443

# Allow SSH (port 22) - restrict to your IP
oci network security-list-entry add \
  --security-list-id <SECURITY_LIST_OCID> \
  --protocol 6 \
  --source "<YOUR_IP>/32" \
  --destination-port-range-min 22 \
  --destination-port-range-max 22
```

---

## Part 2: Compute Instances

### Step 2.1: Create Backend VM (VM 1)

**Via Console**:

1. **Navigate**: Compute → Instances → Create Instance
2. **Configure**:
   - Name: `retailhq-backend`
   - Compartment: `retailhq-production`
   - Availability Domain: AD-1 (or any available)
   - Shape: **VM.Standard.A1.Flex** (Ampere ARM)
     - OCPUs: 1 (can allocate up to 4 free)
     - Memory: 6 GB (can allocate up to 24 GB free)
   - Image: **Oracle Linux 8** or **Ubuntu 22.04**
   - VCN: `retailhq-vcn`
   - Subnet: `retailhq-public-subnet`
   - Public IP: Assign public IPv4 address
3. **SSH Keys**: Upload your public key or generate new key pair
4. **Create**: Review and create

**Via CLI**:

```bash
# Create compute instance
oci compute instance launch \
  --compartment-id <COMPARTMENT_OCID> \
  --availability-domain <AD_NAME> \
  --display-name "retailhq-backend" \
  --shape "VM.Standard.A1.Flex" \
  --shape-config '{"ocpus":2,"memoryInGBs":12}' \
  --image-id <UBUNTU_22_04_IMAGE_OCID> \
  --subnet-id <PUBLIC_SUBNET_OCID> \
  --assign-public-ip true \
  --ssh-authorized-keys-file ~/.ssh/id_rsa.pub

# Note the public IP from output
```

### Step 2.2: Create Database VM (VM 2)

**Option A: Use Oracle Autonomous Database (Recommended)**

Oracle offers **2 Autonomous Databases** in Always Free tier:

```bash
# Create Autonomous Database (via Console is easier)
# Console → Databases → Autonomous Database → Create
# - Workload Type: Transaction Processing
# - Deployment Type: Serverless
# - Database Name: retailhqdb
# - OCPU: 1, Storage: 20 GB
# - Version: 19c or 21c
# - Admin Password: <SECURE_PASSWORD>
# - License: Bring Your Own License (BYOL)
```

**Option B: Self-managed PostgreSQL on VM 2**

```bash
# Create second VM for PostgreSQL (same as VM 1)
oci compute instance launch \
  --compartment-id <COMPARTMENT_OCID> \
  --availability-domain <AD_NAME> \
  --display-name "retailhq-database" \
  --shape "VM.Standard.A1.Flex" \
  --shape-config '{"ocpus":2,"memoryInGBs":12}' \
  --image-id <UBUNTU_22_04_IMAGE_OCID> \
  --subnet-id <PUBLIC_SUBNET_OCID> \
  --assign-public-ip false
```

---

## Part 3: Backend Installation

### Step 3.1: Connect to Backend VM

```bash
# SSH into backend VM
ssh -i ~/.ssh/id_rsa ubuntu@<BACKEND_PUBLIC_IP>
```

### Step 3.2: Install Docker

```bash
# Update system
sudo apt update && sudo apt upgrade -y

# Install Docker
curl -fsSL https://get.docker.com -o get-docker.sh
sudo sh get-docker.sh

# Add user to docker group
sudo usermod -aG docker $USER
newgrp docker

# Verify installation
docker --version
docker run hello-world
```

### Step 3.3: Install Docker Compose

```bash
# Install Docker Compose
sudo curl -L "https://github.com/docker/compose/releases/latest/download/docker-compose-$(uname -s)-$(uname -m)" -o /usr/local/bin/docker-compose
sudo chmod +x /usr/local/bin/docker-compose

# Verify installation
docker-compose --version
```

### Step 3.4: Setup RetailHQ Backend

```bash
# Create application directory
mkdir -p ~/retailhq
cd ~/retailhq

# Clone repository (or upload JAR directly)
# Option 1: Using Git
git clone https://github.com/yourorg/retailhq.git
cd retailhq

# Option 2: Upload JAR directly
# scp target/shop-manager-*.jar ubuntu@<BACKEND_IP>:~/retailhq/

# Create environment file
cat > .env << 'EOF'
# Database Configuration
SPRING_DATASOURCE_URL=jdbc:postgresql://retailhq-database:5432/retailhqdb
SPRING_DATASOURCE_USERNAME=retailhq
SPRING_DATASOURCE_PASSWORD=<SECURE_DB_PASSWORD>

# JWT Configuration
APPLICATION_JWT_SECRET=<GENERATE_SECURE_SECRET_256_BITS>
APPLICATION_JWT_EXPIRATION_MS=86400000
APPLICATION_JWT_REFRESH_EXPIRATION_MS=604800000

# Application Configuration
SPRING_PROFILES_ACTIVE=production
SERVER_PORT=8081

# Cloud Aggregator Configuration
APPLICATION_CLOUD_MODE=true
APPLICATION_DOMAIN=retailhq.app
EOF

# Generate secure JWT secret
openssl rand -base64 32
# Copy output and paste into .env as APPLICATION_JWT_SECRET
```

### Step 3.5: Create Docker Compose File

```bash
cat > docker-compose.yml << 'EOF'
version: '3.8'

services:
  backend:
    image: ghcr.io/yourorg/retailhq-backend:latest
    # OR build from source:
    # build:
    #   context: ./backend
    #   dockerfile: Dockerfile
    container_name: retailhq-backend
    ports:
      - "8081:8081"
    environment:
      - SPRING_PROFILES_ACTIVE=production
      - SPRING_DATASOURCE_URL=${SPRING_DATASOURCE_URL}
      - SPRING_DATASOURCE_USERNAME=${SPRING_DATASOURCE_USERNAME}
      - SPRING_DATASOURCE_PASSWORD=${SPRING_DATASOURCE_PASSWORD}
      - APPLICATION_JWT_SECRET=${APPLICATION_JWT_SECRET}
      - APPLICATION_JWT_EXPIRATION_MS=${APPLICATION_JWT_EXPIRATION_MS}
      - APPLICATION_JWT_REFRESH_EXPIRATION_MS=${APPLICATION_JWT_REFRESH_EXPIRATION_MS}
      - SERVER_PORT=8081
    restart: unless-stopped
    healthcheck:
      test: ["CMD", "curl", "-f", "http://localhost:8081/actuator/health"]
      interval: 30s
      timeout: 10s
      retries: 3
      start_period: 60s
    networks:
      - retailhq-network
    depends_on:
      - database

  database:
    image: postgres:16-alpine
    container_name: retailhq-database
    environment:
      POSTGRES_DB: retailhqdb
      POSTGRES_USER: ${SPRING_DATASOURCE_USERNAME}
      POSTGRES_PASSWORD: ${SPRING_DATASOURCE_PASSWORD}
    volumes:
      - postgres_data:/var/lib/postgresql/data
    restart: unless-stopped
    healthcheck:
      test: ["CMD-SHELL", "pg_isready -U retailhq"]
      interval: 10s
      timeout: 5s
      retries: 5
    networks:
      - retailhq-network

networks:
  retailhq-network:
    driver: bridge

volumes:
  postgres_data:
    driver: local
EOF
```

### Step 3.6: Start Backend

```bash
# Pull images and start services
docker-compose up -d

# Check logs
docker-compose logs -f backend

# Verify backend is running
curl http://localhost:8081/actuator/health
```

---

## Part 4: DNS & SSL Configuration

### Step 4.1: Configure DNS

Point your domain to the backend VM's public IP:

**Using Cloudflare**:

1. Login to Cloudflare Dashboard
2. Select domain: `retailhq.app`
3. **DNS → Records → Add Record**:
   - Type: `A`
   - Name: `api`
   - IPv4 address: `<BACKEND_PUBLIC_IP>`
   - Proxy status: DNS only (gray cloud)
   - TTL: Auto

**Using Route53**:

```bash
# Create hosted zone (if not exists)
aws route53 create-hosted-zone \
  --name retailhq.app \
  --caller-reference $(date +%s)

# Create A record
aws route53 change-resource-record-sets \
  --hosted-zone-id <ZONE_ID> \
  --change-batch '{
    "Changes": [{
      "Action": "CREATE",
      "ResourceRecordSet": {
        "Name": "api.retailhq.app",
        "Type": "A",
        "TTL": 300,
        "ResourceRecords": [{"Value": "<BACKEND_PUBLIC_IP>"}]
      }
    }]
  }'
```

**Verify DNS**:

```bash
# Wait 1-5 minutes for propagation
dig api.retailhq.app
nslookup api.retailhq.app

# Should return your backend public IP
```

### Step 4.2: Install Nginx (Reverse Proxy)

```bash
# SSH into backend VM
ssh ubuntu@<BACKEND_PUBLIC_IP>

# Install Nginx
sudo apt update
sudo apt install -y nginx

# Create Nginx configuration
sudo tee /etc/nginx/sites-available/retailhq << 'EOF'
server {
    listen 80;
    server_name api.retailhq.app;

    location / {
        proxy_pass http://localhost:8081;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;

        # WebSocket support (if needed)
        proxy_http_version 1.1;
        proxy_set_header Upgrade $http_upgrade;
        proxy_set_header Connection "upgrade";
    }

    location /actuator/health {
        proxy_pass http://localhost:8081/actuator/health;
        access_log off;
    }
}
EOF

# Enable site
sudo ln -s /etc/nginx/sites-available/retailhq /etc/nginx/sites-enabled/
sudo rm /etc/nginx/sites-enabled/default

# Test configuration
sudo nginx -t

# Restart Nginx
sudo systemctl restart nginx
sudo systemctl enable nginx

# Verify
curl http://api.retailhq.app/actuator/health
```

### Step 4.3: Install SSL Certificate (Let's Encrypt)

```bash
# Install Certbot
sudo apt install -y certbot python3-certbot-nginx

# Obtain SSL certificate
sudo certbot --nginx -d api.retailhq.app

# Follow prompts:
# - Email: admin@retailhq.app
# - Agree to Terms: Yes
# - Share email: No (optional)
# - Redirect HTTP to HTTPS: Yes (option 2)

# Verify auto-renewal
sudo certbot renew --dry-run

# Test HTTPS
curl https://api.retailhq.app/actuator/health
```

**Nginx config is automatically updated by Certbot to**:

```nginx
server {
    listen 443 ssl;
    server_name api.retailhq.app;

    ssl_certificate /etc/letsencrypt/live/api.retailhq.app/fullchain.pem;
    ssl_certificate_key /etc/letsencrypt/live/api.retailhq.app/privkey.pem;
    include /etc/letsencrypt/options-ssl-nginx.conf;
    ssl_dhparam /etc/letsencrypt/ssl-dhparams.pem;

    location / {
        proxy_pass http://localhost:8081;
        # ... proxy headers ...
    }
}

server {
    listen 80;
    server_name api.retailhq.app;
    return 301 https://$host$request_uri;
}
```

---

## Part 5: Monitoring & Maintenance

### Step 5.1: Setup Monitoring

**Install Node Exporter (for Prometheus)**:

```bash
# Download Node Exporter
cd /tmp
wget https://github.com/prometheus/node_exporter/releases/latest/download/node_exporter-*.linux-amd64.tar.gz
tar xvfz node_exporter-*.linux-amd64.tar.gz
sudo mv node_exporter-*/node_exporter /usr/local/bin/

# Create systemd service
sudo tee /etc/systemd/system/node_exporter.service << 'EOF'
[Unit]
Description=Node Exporter
After=network.target

[Service]
User=ubuntu
ExecStart=/usr/local/bin/node_exporter

[Install]
WantedBy=multi-user.target
EOF

# Start service
sudo systemctl daemon-reload
sudo systemctl start node_exporter
sudo systemctl enable node_exporter

# Verify
curl http://localhost:9100/metrics
```

**Setup Basic Monitoring Script**:

```bash
# Create monitoring script
cat > ~/check-health.sh << 'EOF'
#!/bin/bash

HEALTH_URL="https://api.retailhq.app/actuator/health"
SLACK_WEBHOOK="<YOUR_SLACK_WEBHOOK_URL>"

STATUS=$(curl -s -o /dev/null -w "%{http_code}" $HEALTH_URL)

if [ $STATUS -ne 200 ]; then
    MESSAGE="🚨 RetailHQ API is DOWN! Status code: $STATUS"
    curl -X POST -H 'Content-type: application/json' \
         --data "{\"text\":\"$MESSAGE\"}" \
         $SLACK_WEBHOOK
fi
EOF

chmod +x ~/check-health.sh

# Add to crontab (check every 5 minutes)
(crontab -l 2>/dev/null; echo "*/5 * * * * ~/check-health.sh") | crontab -
```

### Step 5.2: Setup Automated Backups

```bash
# Create backup script
cat > ~/backup-database.sh << 'EOF'
#!/bin/bash

BACKUP_DIR=/home/ubuntu/backups
DATE=$(date +%Y%m%d_%H%M%S)
BACKUP_FILE="retailhq_backup_$DATE.sql.gz"

# Create backup directory
mkdir -p $BACKUP_DIR

# Backup database
docker exec retailhq-database pg_dump -U retailhq retailhqdb | gzip > $BACKUP_DIR/$BACKUP_FILE

# Keep only last 7 days of backups
find $BACKUP_DIR -name "retailhq_backup_*.sql.gz" -mtime +7 -delete

echo "Backup completed: $BACKUP_FILE"
EOF

chmod +x ~/backup-database.sh

# Add to crontab (daily at 2 AM)
(crontab -l 2>/dev/null; echo "0 2 * * * ~/backup-database.sh") | crontab -
```

### Step 5.3: Setup Log Rotation

```bash
# Create logrotate configuration
sudo tee /etc/logrotate.d/retailhq << 'EOF'
/var/log/nginx/*.log {
    daily
    rotate 14
    compress
    delaycompress
    notifempty
    create 0640 www-data adm
    sharedscripts
    postrotate
        [ -f /var/run/nginx.pid ] && kill -USR1 `cat /var/run/nginx.pid`
    endscript
}
EOF
```

---

## Part 6: CI/CD Pipeline (Optional)

### Step 6.1: Setup GitHub Actions

Create `.github/workflows/deploy-oracle-cloud.yml`:

```yaml
name: Deploy to Oracle Cloud

on:
  push:
    branches: [main]
    tags: ['v*']

jobs:
  deploy:
    runs-on: ubuntu-latest
    steps:
      - name: Checkout code
        uses: actions/checkout@v3

      - name: Setup Java 21
        uses: actions/setup-java@v3
        with:
          java-version: '21'
          distribution: 'temurin'

      - name: Build JAR
        run: ./mvnw clean package -DskipTests

      - name: Copy JAR to Oracle Cloud VM
        uses: appleboy/scp-action@master
        with:
          host: ${{ secrets.ORACLE_VM_IP }}
          username: ubuntu
          key: ${{ secrets.SSH_PRIVATE_KEY }}
          source: "target/shop-manager-*.jar"
          target: "/home/ubuntu/retailhq/"

      - name: Deploy on Oracle Cloud
        uses: appleboy/ssh-action@master
        with:
          host: ${{ secrets.ORACLE_VM_IP }}
          username: ubuntu
          key: ${{ secrets.SSH_PRIVATE_KEY }}
          script: |
            cd ~/retailhq
            docker-compose down
            docker-compose pull
            docker-compose up -d
            echo "Deployment completed!"
```

---

## Part 7: Cost Optimization

### Always Free Tier Limits

Oracle Cloud Always Free includes:

| Resource | Free Tier Limit | Usage Strategy |
|----------|----------------|----------------|
| **Compute** | 4 OCPUs, 24 GB RAM (Ampere A1) | 2 VMs: Backend (2 OCPU, 12GB) + Database (2 OCPU, 12GB) |
| **Block Storage** | 200 GB total | 100 GB per VM boot volume |
| **Autonomous DB** | 2 databases, 20 GB each | Use for production database (optional) |
| **Load Balancer** | 1 flexible load balancer | Not needed for single backend |
| **Outbound Data** | 10 GB/month | Monitor with OCI metrics |
| **Public IP** | 2 reserved IPs | 1 for backend VM |

### Cost Monitoring

```bash
# Setup budget alert
oci budgets budget create \
  --compartment-id <COMPARTMENT_OCID> \
  --amount 5 \
  --reset-period MONTHLY \
  --target-type COMPARTMENT \
  --targets '["<COMPARTMENT_OCID>"]' \
  --alert-rule-recipients '["admin@retailhq.app"]'
```

---

## Part 8: Security Hardening

### Step 8.1: Firewall Configuration

```bash
# Install and configure UFW (Uncomplicated Firewall)
sudo apt install -y ufw

# Allow SSH (from your IP only)
sudo ufw allow from <YOUR_IP> to any port 22

# Allow HTTP and HTTPS
sudo ufw allow 80/tcp
sudo ufw allow 443/tcp

# Enable firewall
sudo ufw enable
sudo ufw status
```

### Step 8.2: Fail2Ban (Brute Force Protection)

```bash
# Install Fail2Ban
sudo apt install -y fail2ban

# Configure for SSH
sudo tee /etc/fail2ban/jail.local << 'EOF'
[sshd]
enabled = true
port = 22
filter = sshd
logpath = /var/log/auth.log
maxretry = 3
bantime = 3600
EOF

# Restart Fail2Ban
sudo systemctl restart fail2ban
sudo systemctl enable fail2ban

# Check status
sudo fail2ban-client status sshd
```

### Step 8.3: Automatic Security Updates

```bash
# Install unattended-upgrades
sudo apt install -y unattended-upgrades

# Configure automatic updates
sudo dpkg-reconfigure -plow unattended-upgrades

# Verify configuration
cat /etc/apt/apt.conf.d/50unattended-upgrades
```

---

## Part 9: Testing & Verification

### Step 9.1: API Health Check

```bash
# Test health endpoint
curl https://api.retailhq.app/actuator/health

# Expected response:
# {"status":"UP"}
```

### Step 9.2: Test Tenant Registration

```bash
# Register a new tenant
curl -X POST https://api.retailhq.app/api/registration/tenants \
  -H "Content-Type: application/json" \
  -d '{
    "tenantName": "Test Retail Business",
    "tenantEmail": "test@retailbusiness.com",
    "companyRegistration": "REG123456",
    "taxId": "TAX789",
    "address": "123 Main St",
    "city": "Test City",
    "country": "Test Country",
    "phoneNumber": "555-123-4567",
    "shops": [
      {
        "shopName": "Main Store",
        "shopEmail": "mainstore@retailbusiness.com",
        "address": "456 Store Ave",
        "city": "Store City",
        "phoneNumber": "555-987-6543"
      }
    ]
  }'

# Expected response includes API key:
# {
#   "cloudTenantId": "abc-123-def",
#   "apiKey": "rhq_xxxxxxxxxxxxxxxxxxxxxxxxxx",
#   "registeredShopsCount": 1,
#   "message": "Tenant successfully registered with 1 shop(s)"
# }
```

### Step 9.3: Performance Testing

```bash
# Install Apache Bench
sudo apt install -y apache2-utils

# Simple load test (100 requests, 10 concurrent)
ab -n 100 -c 10 https://api.retailhq.app/actuator/health

# Check response times (should be < 200ms for p95)
```

---

## Part 10: Troubleshooting

### Common Issues

**Issue 1: Backend not accessible**

```bash
# Check if backend is running
docker ps
docker-compose logs backend

# Check if port 8081 is listening
sudo netstat -tulpn | grep 8081

# Check firewall
sudo ufw status

# Check Nginx
sudo nginx -t
sudo systemctl status nginx
```

**Issue 2: SSL certificate errors**

```bash
# Check certificate status
sudo certbot certificates

# Renew certificate manually
sudo certbot renew

# Check Nginx SSL configuration
sudo nginx -t
```

**Issue 3: Database connection errors**

```bash
# Check PostgreSQL container
docker exec -it retailhq-database psql -U retailhq -d retailhqdb

# Check connection from backend
docker exec retailhq-backend curl http://database:5432
```

**Issue 4: Out of memory**

```bash
# Check memory usage
free -h
docker stats

# Optimize Docker Compose memory limits
# Add to docker-compose.yml:
services:
  backend:
    mem_limit: 800m
    mem_reservation: 512m
```

---

## Part 11: Disaster Recovery

### Backup Strategy

1. **Database Backups**: Daily automated backups (7-day retention)
2. **Configuration Backups**: Weekly `.env` and Nginx configs
3. **Off-site Storage**: Upload backups to Oracle Object Storage (free 20 GB)

### Recovery Procedure

```bash
# Restore from backup
cd ~/backups
gunzip retailhq_backup_YYYYMMDD_HHMMSS.sql.gz

# Restore to database
cat retailhq_backup_YYYYMMDD_HHMMSS.sql | docker exec -i retailhq-database psql -U retailhq -d retailhqdb

# Restart services
docker-compose restart backend
```

---

## Appendix A: Resource Optimization Tips

### Reduce Memory Usage

```yaml
# docker-compose.yml optimizations
services:
  backend:
    environment:
      - JAVA_OPTS=-Xms256m -Xmx512m -XX:+UseG1GC -XX:MaxGCPauseMillis=200
```

### Reduce Storage Usage

```bash
# Clean Docker images
docker system prune -a

# Clean logs
sudo journalctl --vacuum-time=7d
```

### Monitor Free Tier Usage

```bash
# Install OCI CLI monitoring extension
oci monitoring metric-data summarize-metric-data \
  --compartment-id <COMPARTMENT_OCID> \
  --namespace oci_compute_infrastructure_health \
  --query-text "CpuUtilization[1m].mean()"
```

---

## Appendix B: Migration from Other Clouds

### From AWS to Oracle Cloud

```bash
# Export AWS RDS database
aws rds create-db-snapshot --db-instance-identifier mydb --db-snapshot-identifier mydb-final-snapshot

# Download snapshot (convert to PostgreSQL dump)
# ... (AWS-specific commands)

# Import to Oracle Cloud PostgreSQL
cat mydb-dump.sql | docker exec -i retailhq-database psql -U retailhq -d retailhqdb
```

---

## Summary

**Deployment Checklist**:

- [x] Create Oracle Cloud account
- [x] Setup VCN and security lists
- [x] Launch compute instance (VM 1 - Backend)
- [x] Install Docker and Docker Compose
- [x] Deploy RetailHQ backend
- [x] Configure DNS (api.retailhq.app)
- [x] Install Nginx reverse proxy
- [x] Setup SSL with Let's Encrypt
- [x] Configure monitoring and backups
- [x] Setup CI/CD pipeline (optional)
- [x] Test API endpoints

**Total Cost**: $0/month (within Always Free tier limits)

**Expected Uptime**: 99%+

**Support**: For issues, contact support@retailhq.app or visit [docs.retailhq.app](https://docs.retailhq.app)

---

**Last Updated**: January 2026
**Version**: 1.0.0
