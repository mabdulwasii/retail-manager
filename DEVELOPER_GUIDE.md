# Shop Manager - Local Development Guide

## Table of Contents
1. [Overview](#overview)
2. [Prerequisites](#prerequisites)
3. [Quick Start Options](#quick-start-options)
4. [Docker Compose Setup](#docker-compose-setup)
5. [Local Kubernetes Setup](#local-kubernetes-setup)
6. [Frontend Development](#frontend-development)
7. [Backend Development](#backend-development)
8. [Testing and Authentication](#testing-and-authentication)
9. [Development Tools](#development-tools)
10. [Troubleshooting](#troubleshooting)

## Overview

Shop Manager supports multiple local development approaches to accommodate different workflows and preferences:

- **🐳 Docker Development**: Full containerized development with hot reload
- **💻 Hybrid Development**: Infrastructure in containers, application code running locally (Recommended)
- **🔧 Native Development**: Everything running locally (advanced setup)

## Development Approach Comparison

| Approach | Setup Time | Flexibility | Performance | Best For |
|----------|------------|-------------|-------------|----------|
| **Docker** | ⚡ Fast | 🔄 Medium | 🐌 Slower | Quick start, consistency |
| **Hybrid** | ⚡ Fast | 🚀 High | ⚡ Fast | Active development |
| **Kubernetes** | 🕐 Medium | 🔄 Medium | 🔄 Medium | K8s testing |
| **Native** | 🕐 Slow | 🚀 High | ⚡ Fastest | Advanced users |

## Prerequisites

### Required Tools
- **Docker Desktop** (with Kubernetes enabled for k8s option)
- **Git** for version control
- **Node.js 18+** for frontend development
- **Java 21+** for backend development

### Recommended Tools
- **kubectl** for Kubernetes management
- **Helm 3.x** for Kubernetes deployments
- **kubectx/kubens** for context switching
- **IntelliJ IDEA** or **VS Code** for development
- **Postman** or **curl** for API testing

### System Requirements
- **RAM**: 8GB minimum, 16GB recommended
- **CPU**: 4 cores minimum
- **Storage**: 10GB free space
- **OS**: macOS, Windows, or Linux

## Quick Start Options

Choose your preferred development approach:

### 🐳 Docker Development (Full Containerization)
```bash
git clone <repository-url> && cd shop-manager
docker-compose up -d
open http://localhost:3000
```

### 💻 Hybrid Development (Recommended)
```bash
# Infrastructure only
docker-compose up -d postgres keycloak kafka minio

# Local development
cd frontend && npm install && npm run dev &
cd backend && ./mvnw spring-boot:run &
```

### 🔧 Native Development (Advanced)
```bash
# Install PostgreSQL, Kafka, MinIO locally
# Configure application-native.yml
./mvnw spring-boot:run -Dspring-boot.run.profiles=native &
cd frontend && npm run dev &
```

## 🐳 Docker Development Approach

**Best for**: Quick start, environment consistency, team onboarding

### Full Stack Setup
```bash
# Complete environment
docker-compose up -d

# With code quality tools
docker-compose --profile sonar up -d

# Infrastructure only (for hybrid)
docker-compose up -d postgres keycloak kafka minio
```

### Development Workflow
```bash
# Hot reload development
docker-compose up -d --build  # Rebuild with changes

# View logs
docker-compose logs -f backend frontend

# Database operations
docker-compose exec postgres psql -U postgres -d shopmanager

# Reset environment
docker-compose down -v && docker-compose up -d
```

### Volume Mounting for Development
Edit `docker-compose.yml` for live code updates:
```yaml
backend:
  volumes:
    - ./backend:/app
    - /app/target  # Preserve built artifacts

frontend:
  volumes:
    - ./frontend:/app
    - /app/node_modules  # Preserve dependencies
```

## 💻 Hybrid Development Approach

**Best for**: Active development, debugging, fast iteration

### Infrastructure Setup
```bash
# Start only required services
docker-compose up -d postgres keycloak kafka minio

# Verify services
docker-compose ps
curl http://localhost:8080/realms/shop-manager
```

### Frontend Development
```bash
cd frontend

# Environment setup
cat > .env.local << EOF
VITE_API_BASE_URL=http://localhost:8081
VITE_KEYCLOAK_URL=http://localhost:8080
VITE_KEYCLOAK_REALM=shop-manager
VITE_KEYCLOAK_CLIENT_ID=shop-manager-frontend
EOF

# Development server
npm install && npm run dev
```

### Backend Development
```bash
cd backend

# Profile configuration
export SPRING_PROFILES_ACTIVE=local,debug

# Development server with live reload
./mvnw spring-boot:run

# Or with debug port
./mvnw spring-boot:run -Dspring-boot.run.jvmArguments="-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=5005"
```

### Hybrid Development Benefits
- **Fast builds**: No container rebuilding
- **IDE integration**: Full debugging, intellisense
- **Hot reload**: Instant code changes
- **Resource efficient**: Lower memory usage
- **Easy testing**: Direct access to running code

## Docker Compose Setup

Docker Compose provides the simplest way to get started with Shop Manager development.

### Architecture Overview
```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│    Frontend     │    │     Backend     │    │   PostgreSQL    │
│   React App     │◄──►│  Spring Boot    │◄──►│    Database     │
│  Port: 3000     │    │  Port: 8081     │    │   Port: 5432    │
└─────────────────┘    └─────────────────┘    └─────────────────┘
         │                       │                       │
         │              ┌─────────────────┐              │
         │              │    Keycloak     │              │
         └─────────────►│      SSO        │◄─────────────┘
                        │   Port: 8080    │
                        └─────────────────┘
                                 │
                        ┌─────────────────┐
                        │      Kafka      │
                        │  Event Stream   │
                        │   Port: 9092    │
                        └─────────────────┘
                                 │
                        ┌─────────────────┐
                        │      MinIO      │
                        │ Object Storage  │
                        │   Port: 9000    │
                        └─────────────────┘
```

### Available Services

#### Core Services (Always Available)
```yaml
services:
  postgres:        # Database
  keycloak:        # Authentication
  kafka:           # Event streaming
  minio:           # Object storage
  backend:         # Shop Manager API
```

#### Optional Services
```yaml
  frontend:        # React application
  sonarqube:       # Code quality (with --profile sonar)
  sonarqube-db:    # SonarQube database
```

### Service Commands

#### Basic Operations
```bash
# Start all core services
docker-compose up -d

# Start with frontend
docker-compose up -d frontend

# Start with SonarQube for code analysis
docker-compose --profile sonar up -d

# Stop all services
docker-compose down

# Stop and remove volumes (clean slate)
docker-compose down -v
```

#### Infrastructure Only
```bash
# Start just infrastructure for hybrid development
docker-compose up -d postgres keycloak kafka minio

# Check services are running
docker-compose ps

# View logs
docker-compose logs -f keycloak
```

#### Rebuild and Update
```bash
# Rebuild specific service
docker-compose build backend

# Pull latest images
docker-compose pull

# Restart specific service
docker-compose restart backend
```

### Service Configuration

#### Environment Variables
Create a `.env` file in the project root:
```bash
# Database
POSTGRES_DB=shopmanager
POSTGRES_USER=postgres
POSTGRES_PASSWORD=postgres

# Keycloak
KEYCLOAK_ADMIN=admin
KEYCLOAK_ADMIN_PASSWORD=KeycloakAdm1n@2024!SecureAuth#CompliantPassword

# MinIO
MINIO_ROOT_USER=admin
MINIO_ROOT_PASSWORD=minio123

# Application
SPRING_PROFILES_ACTIVE=local
LOG_LEVEL=DEBUG
```

#### Service URLs and Ports
| Service | URL | Purpose | Default Credentials |
|---------|-----|---------|-------------------|
| **Frontend** | http://localhost:3000 | React application | N/A |
| **Backend API** | http://localhost:8081 | REST API & Swagger | N/A |
| **Keycloak** | http://localhost:8080 | Authentication server with custom theme | admin / KeycloakAdm1n@2024!SecureAuth#CompliantPassword |
| **MinIO Console** | http://localhost:9001 | Object storage admin | admin / minio123 |
| **PostgreSQL** | localhost:5432 | Database | postgres / postgres |
| **Kafka** | localhost:9092 | Message broker | N/A |
| **SonarQube** | http://localhost:9000 | Code quality | admin / admin |

### Custom Keycloak Theme
The development environment automatically includes a custom Shop Manager login theme with enhanced features:

#### 🎨 Theme Features
- **Shop Manager branding** - Custom logo, colors, and layout
- **Password visibility toggle** - Eye icon to show/hide passwords
- **Development auto-fill buttons** - Quick login with test credentials
- **Animated background** - Floating retail-themed elements
- **Responsive design** - Mobile-optimized interface
- **Accessibility support** - ARIA labels and keyboard navigation

#### 🔧 Theme Deployment
The custom theme is automatically deployed when Keycloak starts:

```bash
# Theme files are automatically loaded from:
# keycloak-theme/shop-manager/login/
# ├── theme.properties      # Theme configuration
# ├── template.ftl          # Base template
# ├── login.ftl             # Login form
# └── resources/css/
#     └── shop-manager.css  # Custom styles

# Verify theme is active
curl http://localhost:8080/realms/shop-manager/login-actions/authenticate
```

#### 🧪 Development Features
The theme includes development-only features for testing:
- **Quick login buttons** for each test user role
- **Auto-fill credentials** with one click
- **Development environment indicator**
- **Enhanced debugging information**

## Local Kubernetes Setup

For developers who prefer Kubernetes-native development or want to test Kubernetes deployments locally.

### Prerequisites
1. **Enable Kubernetes in Docker Desktop**:
   - Open Docker Desktop
   - Go to Settings → Kubernetes
   - Check "Enable Kubernetes"
   - Click "Apply & Restart"

2. **Verify Kubernetes is running**:
   ```bash
   kubectl cluster-info
   kubectl get nodes
   ```

### Setup Steps

#### 1. Install Ingress Controller
```bash
# Install nginx ingress controller
kubectl apply -f https://raw.githubusercontent.com/kubernetes/ingress-nginx/controller-v1.8.2/deploy/static/provider/cloud/deploy.yaml

# Wait for deployment
kubectl wait --namespace ingress-nginx \
  --for=condition=ready pod \
  --selector=app.kubernetes.io/component=controller \
  --timeout=90s
```

#### 2. Deploy Shop Manager
```bash
# Create namespace
kubectl create namespace shop-manager
kubectl config set-context --current --namespace=shop-manager

# Deploy using Helm
helm install shop-manager ./helm-chart/shop-manager \
  --namespace shop-manager \
  --values helm-chart/values/development.yaml \
  --wait \
  --timeout=10m
```

#### 3. Configure Local DNS
Add to `/etc/hosts` (macOS/Linux) or `C:\Windows\System32\drivers\etc\hosts` (Windows):
```
127.0.0.1 shop-manager.local
127.0.0.1 auth.shop-manager.local
127.0.0.1 api.shop-manager.local
```

#### 4. Access Services
```bash
# Port forward for local access
kubectl port-forward service/shop-manager-frontend 3000:80 &
kubectl port-forward service/shop-manager-keycloak 8080:80 &
kubectl port-forward service/shop-manager-backend 8081:8081 &

# Or access via ingress
open http://shop-manager.local
```

### Kubernetes Development Workflow

#### Deploy Changes
```bash
# Build and update backend
docker build -t shop-manager-backend:latest ./backend
kubectl set image deployment/shop-manager-backend \
  backend=shop-manager-backend:latest

# Build and update frontend
docker build -t shop-manager-frontend:latest ./frontend
kubectl set image deployment/shop-manager-frontend \
  frontend=shop-manager-frontend:latest
```

#### Debug and Monitor
```bash
# View pod logs
kubectl logs -f deployment/shop-manager-backend

# Describe pod issues
kubectl describe pod <pod-name>

# Execute into pod
kubectl exec -it <pod-name> -- /bin/bash

# Check resource usage
kubectl top pods
```

## Frontend Development

### Local Development Setup
```bash
cd frontend

# Install dependencies
npm install

# Start development server
npm run dev

# Run in different port if needed
npm run dev -- --port 3001
```

### Development Workflow

#### Available Scripts
```bash
# Development
npm run dev          # Start dev server with hot reload
npm run build        # Build for production
npm run preview      # Preview production build

# Testing
npm test             # Run unit tests
npm run test:watch   # Run tests in watch mode
npm run test:coverage # Generate coverage report

# Code Quality
npm run lint         # Lint TypeScript/JavaScript
npm run lint:fix     # Fix linting issues
npm run type-check   # Check TypeScript types
```

#### Environment Configuration
Create `frontend/.env.local`:
```bash
# API Configuration
VITE_API_BASE_URL=http://localhost:8081
VITE_KEYCLOAK_URL=http://localhost:8080
VITE_KEYCLOAK_REALM=shop-manager
VITE_KEYCLOAK_CLIENT_ID=shop-manager-frontend

# Development Features
VITE_ENABLE_MOCK_DATA=false
VITE_LOG_LEVEL=debug
```

#### Hot Reload Configuration
The development server automatically reloads when files change:
- **React components**: Instant hot reload
- **TypeScript**: Type checking in background
- **CSS/SCSS**: Style injection without page reload
- **Environment variables**: Require server restart

### Frontend Architecture
```
frontend/
├── src/
│   ├── components/     # Reusable UI components
│   ├── pages/         # Route components
│   ├── hooks/         # Custom React hooks
│   ├── services/      # API clients
│   ├── utils/         # Helper functions
│   ├── types/         # TypeScript definitions
│   ├── styles/        # Global styles
│   └── i18n/          # Internationalization
├── public/            # Static assets
└── tests/             # Test files
```

## Backend Development

### Local Development Setup
```bash
cd backend

# Run with Maven
./mvnw spring-boot:run

# Run with specific profile
./mvnw spring-boot:run -Dspring-boot.run.profiles=local,debug

# Run tests
./mvnw test

# Build JAR
./mvnw clean package
```

### Development Profiles

#### Available Profiles
- **`local`**: Local development with Docker Compose services
- **`test`**: Test configuration with H2 database
- **`debug`**: Additional debugging and logging
- **`mock`**: Mock external services for offline development

#### Profile Configuration
```yaml
# application-local.yml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/shopmanager
    username: postgres
    password: postgres

  security:
    oauth2:
      resourceserver:
        jwt:
          issuer-uri: http://localhost:8080/realms/shop-manager

logging:
  level:
    com.princely.shopmanager: DEBUG
    org.springframework.security: DEBUG
```

### Development Workflow

#### Live Reload with Spring Boot DevTools
```xml
<!-- Add to pom.xml -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-devtools</artifactId>
    <scope>runtime</scope>
    <optional>true</optional>
</dependency>
```

#### Database Management
```bash
# Run migrations
./mvnw flyway:migrate

# Reset database
./mvnw flyway:clean flyway:migrate

# Generate migration from JPA entities
./mvnw flyway:baseline
```

#### Testing
```bash
# Unit tests only
./mvnw test

# Integration tests
./mvnw verify -Pintegration-tests

# Test specific class
./mvnw test -Dtest=UserServiceTest

# Test with coverage
./mvnw verify jacoco:report
```

### Backend Architecture
```
backend/
├── src/main/java/com/princely/shopmanager/
│   ├── core/          # Core domain entities
│   ├── auth/          # Authentication logic
│   ├── sales/         # Sales management
│   ├── inventory/     # Inventory tracking
│   ├── investment/    # Investment tracking
│   ├── analytics/     # Analytics engine
│   └── shared/        # Shared utilities
├── src/main/resources/
│   ├── db/migration/  # Flyway migrations
│   └── application.yml # Configuration
└── src/test/          # Test files
```

## Testing and Authentication

### Pre-configured Test Users
The development environment includes pre-configured test users for immediate testing:

```bash
# System Administrator
Username: admin@shopmanager.com
Password: admin123
Role: TENANT_ADMIN

# Shop Manager
Username: manager@shopmanager.com
Password: manager123
Role: SHOP_MANAGER

# Shop Employee
Username: employee@shopmanager.com
Password: employee123
Role: SHOP_EMPLOYEE
```

### Authentication Flow Testing

#### 1. Frontend Authentication
```bash
# Start frontend development server
cd frontend && npm run dev

# Access application
open http://localhost:3000

# Test login flow
# 1. Click "Login" button
# 2. Use any test credentials above
# 3. Verify redirect to dashboard
```

#### 2. API Authentication
```bash
# Get access token
curl -X POST http://localhost:8080/realms/shop-manager/protocol/openid-connect/token \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "grant_type=password" \
  -d "client_id=shop-manager-frontend" \
  -d "username=admin@shopmanager.com" \
  -d "password=admin123"

# Use token to access API
curl -H "Authorization: Bearer <access_token>" \
  http://localhost:8081/api/v1/shops
```

#### 3. Keycloak Admin Console
```bash
# Access admin console
open http://localhost:8080

# Admin credentials
Username: admin
Password: KeycloakAdm1n@2024!SecureAuth#CompliantPassword
```

## Development Tools

### Code Quality Tools

#### SonarQube Setup
```bash
# Start SonarQube with Docker Compose
docker-compose --profile sonar up -d

# Access SonarQube
open http://localhost:9000

# Run analysis
./mvnw sonar:sonar \
  -Dsonar.host.url=http://localhost:9000 \
  -Dsonar.login=admin \
  -Dsonar.password=admin
```

### API Development Tools

#### Swagger UI
```bash
# Access API documentation
open http://localhost:8081/swagger-ui.html

# OpenAPI specification
open http://localhost:8081/api-docs
```

## Troubleshooting

### Common Issues

#### Docker Compose Issues

**Services not starting:**
```bash
# Check service logs
docker-compose logs service-name

# Check system resources
docker system df
docker system prune  # Clean up if needed

# Restart specific service
docker-compose restart service-name
```

**Port conflicts:**
```bash
# Check what's using a port
lsof -i :8080

# Kill process using port
kill -9 $(lsof -t -i:8080)

# Or modify port in docker-compose.yml
```

**Database connection issues:**
```bash
# Check PostgreSQL logs
docker-compose logs postgres

# Test connection
docker-compose exec postgres psql -U postgres -d shopmanager

# Reset database
docker-compose down -v  # Removes volumes
docker-compose up -d
```

#### Frontend Issues

**Development server not starting:**
```bash
# Clear npm cache
npm cache clean --force

# Delete node_modules and reinstall
rm -rf node_modules package-lock.json
npm install

# Check Node.js version
node --version  # Should be 18+
```

#### Backend Issues

**Application not starting:**
```bash
# Check Java version
java --version  # Should be 21+

# Clear Maven cache
./mvnw dependency:purge-local-repository

# Run with debug logging
./mvnw spring-boot:run -Dlogging.level.root=DEBUG
```

#### Authentication Issues

**Keycloak not accessible:**
```bash
# Check Keycloak container
docker-compose logs keycloak

# Verify Keycloak is ready
curl http://localhost:8080/realms/shop-manager
```

### Debug Configuration

#### Backend Debug Mode
```bash
# Run with debug port
./mvnw spring-boot:run -Dspring-boot.run.jvmArguments="-Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=5005"

# Connect debugger to port 5005
```

#### Frontend Debug Mode
```bash
# Run with source maps
npm run dev

# Enable verbose logging
export VITE_LOG_LEVEL=debug
npm run dev
```

## Development Best Practices

### 1. Code Organization
- Use feature-based folder structure
- Keep components small and focused
- Implement proper error boundaries
- Use TypeScript for type safety

### 2. Database Development
- Always use migrations for schema changes
- Test migrations with production-like data
- Include rollback scripts for complex changes
- Use database constraints for data integrity

### 3. API Development
- Follow RESTful conventions
- Implement proper validation
- Use consistent error responses
- Document APIs with OpenAPI/Swagger

### 4. Security Development
- Never commit secrets or credentials
- Use environment variables for configuration
- Implement proper authentication checks
- Test authorization at multiple levels

### 5. Testing Strategy
- Write tests before fixing bugs
- Aim for high coverage on business logic
- Use integration tests for API endpoints
- Mock external dependencies

---

**Quick Reference Commands:**

```bash
# Start everything
docker-compose up -d

# Frontend development
cd frontend && npm run dev

# Backend development
cd backend && ./mvnw spring-boot:run

# Run tests
npm test                    # Frontend
./mvnw test                # Backend

# View logs
docker-compose logs -f backend

# Reset everything
docker-compose down -v && docker-compose up -d
```

For production deployment, see [KUBERNETES_DEPLOYMENT.md](./KUBERNETES_DEPLOYMENT.md).

---

**Last Updated**: January 2025
**Version**: 1.0.0