# Retail Manager

**🏪 Modular Retail Management Platform**

A comprehensive multi-tenant retail management system built with Spring Boot and React, featuring investment tracking, sales analytics, and Keycloak authentication.

## 🚀 Quick Start

### Option 1: Install with Helm (Recommended for Production)

**No repository clone needed!** Install directly from Docker Hub:

```bash
# Download configuration template
curl -o my-values.yaml https://raw.githubusercontent.com/yourorg/shop-manager/main/examples/customer-values.yaml

# Customize your values (domain, company name, etc.)
vi my-values.yaml

# Install
helm install retail oci://registry-1.docker.io/princely/shop-manager \
  --version 0.0.46 \
  -n gomco \
  --create-namespace \
  -f my-values.yaml
```

📖 **Complete guide**: [CUSTOMER_INSTALL.md](./CUSTOMER_INSTALL.md)

### Option 2: Run with Docker Compose (Development)
```bash
# Start all services
docker-compose up -d

# Access the application
open http://localhost:3000
```

### Test Authentication
Use any of these pre-configured accounts:
- **Admin**: admin@shopmanager.com / admin123
- **Manager**: manager@shopmanager.com / manager123
- **Employee**: employee@shopmanager.com / employee123

> **📖 Complete testing guide**: See [TESTING-GUIDE.md](./TESTING-GUIDE.md) for detailed authentication testing and all credentials.

## 📚 Documentation

| Document | Purpose |
|----------|---------|
| **[CUSTOMER_INSTALL.md](./CUSTOMER_INSTALL.md)** | 🚀 Simple Kubernetes installation via Helm (5 minutes, no repo clone) |
| **[KUBERNETES_DEPLOYMENT.md](./KUBERNETES_DEPLOYMENT.md)** | ☸️ Complete Kubernetes deployment guide (Helm, SSL, Production) |
| **[LOCAL_DEVELOPMENT.md](./LOCAL_DEVELOPMENT.md)** | 🐳 Docker Compose, Kubernetes, and local development setup |
| **[TESTING-GUIDE.md](./TESTING-GUIDE.md)** | 🔐 Complete authentication testing with all credentials |
| **[docs/PRODUCT_INVENTORY_GUIDE.md](./docs/PRODUCT_INVENTORY_GUIDE.md)** | 📦 Product & Inventory management (Two-Tier Model, FEFO) |
| **[docs/SHOP_ACCESS_CONTROL.md](./docs/SHOP_ACCESS_CONTROL.md)** | 🔒 Shop-level access control implementation guide |
| **[CLAUDE.md](./CLAUDE.md)** | 🛠️ Development guidelines and project architecture |

## 🏗️ Architecture

**Multi-Tenant Retail Platform** with modular Spring Boot backend and React frontend:

### Core Modules
- **🏢 Core**: Tenant/Shop management, users, roles, product catalog
- **📦 Inventory**: Multi-batch stock tracking with FEFO, expiry management
- **🛒 Sales**: Transactions with automatic inventory deduction
- **💰 Investment**: Investment tracking and profit sharing
- **📊 Analytics**: Real-time analytics with caching
- **🔐 Auth**: Keycloak SSO integration
- **🔍 Shared**: Logging, auditing, utilities

### Tech Stack
- **Backend**: Spring Boot 3.3, Java 21, Spring Modulith
- **Frontend**: React, TypeScript, Vite
- **Database**: PostgreSQL with Flyway migrations
- **Authentication**: Keycloak (OAuth2/OpenID Connect)
- **Messaging**: Kafka for event streaming
- **Storage**: MinIO/S3 for backups
- **Testing**: TestContainers, JaCoCo coverage

## 🎯 Key Features

### Business Capabilities
- **Multi-Tenant Architecture**: Complete tenant isolation
- **Product Catalog Management**: SKU, barcodes, pricing, categories
- **Advanced Inventory Control**:
  - Multi-batch tracking with expiry dates
  - FEFO (First Expiry, First Out) automatic sales allocation
  - Multi-location stock management
  - Batch traceability for compliance
- **Sales Processing**:
  - Transactions with automatic inventory deduction
  - PDF receipt generation
  - Fraud detection
- **Investment Tracking**: ROI analytics, profit distribution
- **User Management**: RBAC with Keycloak roles
- **Analytics Dashboard**: Real-time sales and performance metrics

### Technical Features
- **Event-Driven**: Spring Modulith with domain events
- **Multi-Database**: Flyway migrations with versioning
- **API Security**: JWT-based authentication with shop-level access control
- **Shop-Level Authorization**: Role-based filtering (SYSTEM_ADMIN, TENANT_ADMIN, MANAGER, etc.)
- **Comprehensive Testing**: 90%+ code coverage target
- **Production Ready**: Kubernetes Helm charts included

## 🔧 Development

### Prerequisites
- Java 21+
- Node.js 18+
- Docker & Docker Compose
- Maven 3.8+

### Local Development
```bash
# Backend development
./mvnw spring-boot:run

# Frontend development
cd frontend && npm run dev

# Run tests
./mvnw test
./mvnw verify -Pintegration-tests

# Code quality
docker-compose --profile sonar up -d
```

### Build Commands
```bash
# Build backend
./mvnw clean install

# Build frontend
cd frontend && npm run build

# Build Docker images
docker-compose build
```

## 🌐 Service URLs

| Service | URL | Status |
|---------|-----|--------|
| **Frontend** | http://localhost:3000 | ✅ |
| **Backend API** | http://localhost:8081 | ✅ |
| **Keycloak** | http://localhost:8080 | ✅ |
| **MinIO Console** | http://localhost:9001 | ✅ |
| **SonarQube** | http://localhost:9090 | 🔶 Optional |

## 📊 Project Status

**✅ Production Ready** - Complete authentication system deployed

### Latest Updates (January 2025)
- **Product & Inventory Refactoring**: Two-tier model with FEFO sales (Migration V10)
- **Authentication**: Full Keycloak SSO integration working
- **Frontend**: React app with TypeScript compilation resolved
- **Backend**: Spring Modulith with event store configured
- **Database**: All migrations applied (V10 latest)
- **Testing**: Comprehensive test users and documentation

### Coverage & Quality
- **Code Coverage**: 90%+ target with JaCoCo
- **API Documentation**: OpenAPI/Swagger integration
- **Test Users**: 5 pre-configured roles available
- **Documentation**: Complete testing and deployment guides

## 🤝 Contributing

1. Review [CLAUDE.md](./CLAUDE.md) for development guidelines
2. Use [TESTING-GUIDE.md](./TESTING-GUIDE.md) for authentication testing
3. Follow code quality standards (SonarQube integration available)
4. Ensure tests pass: `./mvnw verify`

## 📄 License

This project is part of a retail management system demonstration.

---

**🔗 Quick Links**: [Test Credentials](./TESTING-GUIDE.md#-test-user-accounts) | [Local Development](./LOCAL_DEVELOPMENT.md) | [Architecture](./CLAUDE.md)

Deploying next version installer v0.1.14