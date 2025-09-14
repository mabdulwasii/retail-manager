# Docker Compose Usage Guide

## Single Docker Compose File

The project now uses a single `docker-compose.yml` file for all services with profiles for optional services.

## Available Services

### Core Services (Default Profile)
- **PostgreSQL**: Database server
- **Keycloak**: Authentication server
- **Kafka**: Event streaming platform
- **MinIO**: Object storage for backups
- **Backend**: Shop Manager application

### Optional Services

#### SonarQube (sonar profile)
- **SonarQube**: Code quality analysis
- **SonarQube DB**: PostgreSQL database for SonarQube

## Usage Commands

### Start All Core Services
```bash
docker-compose up -d
```

### Start with SonarQube
```bash
docker-compose --profile sonar up -d
```

### Start Only Specific Services
```bash
# Just infrastructure
docker-compose up -d postgres keycloak kafka minio

# Add backend
docker-compose up -d postgres keycloak kafka minio backend

# Add SonarQube
docker-compose --profile sonar up -d sonarqube sonarqube-db
```

### Service URLs
- **Backend API**: http://localhost:8081
- **Keycloak Admin**: http://localhost:8080 (admin/admin)
- **MinIO Console**: http://localhost:9001 (minioadmin/minioadmin)
- **SonarQube**: http://localhost:9090 (admin/admin)

### Stop Services
```bash
# Stop all services
docker-compose down

# Stop SonarQube services
docker-compose --profile sonar down

# Stop and remove volumes
docker-compose down -v
```

### View Logs
```bash
# All services
docker-compose logs -f

# Specific service
docker-compose logs -f backend

# SonarQube logs
docker-compose --profile sonar logs -f sonarqube
```

## Code Quality Analysis

Use the provided script for SonarQube operations:

```bash
# Interactive script
./sonar-analysis.sh

# Or manually:
docker-compose --profile sonar up -d
cd backend
./mvnw clean verify sonar:sonar
```

## Environment Variables

All services support environment variable overrides:

```bash
# Override database password
POSTGRES_PASSWORD=newpassword docker-compose up -d

# Override application profile
SPRING_PROFILES_ACTIVE=dev docker-compose up -d backend
```