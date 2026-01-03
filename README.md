# RetailHQ

**🏪 Cloud-Native Retail Management Platform**

A comprehensive multi-tenant retail management system built with Spring Boot and React, featuring investment tracking, sales analytics, and cloud aggregation. Deploy as a **Cloud PaaS** (`api.retailhq.app`) or **Local Installation** (embedded JAR, Docker Lite, platform installers).

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

## 🌐 Deployment Models

RetailHQ supports **dual deployment** to meet different business needs:

### 1. Cloud PaaS (api.retailhq.app)
**Central aggregator for multi-shop analytics and management**

- **Purpose**: Cloud-hosted service for retail businesses managing multiple locations
- **Features**:
  - Central tenant registration and shop linking
  - Cross-shop analytics and reporting
  - Subscription management (FREE, BASIC, PREMIUM, ENTERPRISE)
  - API key-based authentication for local shops
- **Deployment**: Oracle Cloud Infrastructure (OCI) Always Free tier
- **Domains**:
  - `api.retailhq.app` - Cloud API backend
  - `cloud.retailhq.app` - Cloud portal frontend
- **Documentation**: [Oracle Cloud Deployment Guide](./docs/ORACLE_CLOUD_DEPLOYMENT.md), [Cloud Aggregator API](./docs/CLOUD_AGGREGATOR_API.md)

### 2. Local Installations
**Standalone retail management for individual shops**

RetailHQ can be deployed locally for shops that want full control and data privacy:

#### a) **Embedded JAR** (Recommended for single shops)
- Single executable JAR with embedded PostgreSQL
- Windows/macOS/Linux support
- Zero external dependencies
- **Documentation**: [Embedded Deployment Guide](./docs/EMBEDDED_DEPLOYMENT.md)

#### b) **Docker Compose Lite**
- Lightweight Docker deployment
- Minimal resource footprint
- Quick setup for small businesses
- **Documentation**: [Docker Lite Deployment Guide](./docs/DOCKER_LITE_DEPLOYMENT.md)

#### c) **Platform Installers**
- Native installers: `.exe` (Windows), `.dmg` (macOS), `.deb`/`.rpm` (Linux)
- One-click installation experience
- Automatic updates and system integration

#### d) **Kubernetes/Helm** (Enterprise)
- Full Kubernetes deployment with Helm charts
- High availability, auto-scaling, monitoring
- Multi-tenant enterprise deployments
- **Documentation**: [Kubernetes Deployment Guide](./KUBERNETES_DEPLOYMENT.md)

### Cloud Sync (Optional)
Local installations can optionally sync data to the cloud aggregator for analytics:
- One-way sync: Local → Cloud (no cloud data stored locally)
- Configurable sync intervals
- API key authentication
- **Documentation**: [Cloud Sync Setup](./docs/CLOUD_SYNC_SETUP.md)

## 📚 Documentation

### Cloud Deployment
| Document | Purpose |
|----------|---------|
| **[docs/ORACLE_CLOUD_DEPLOYMENT.md](./docs/ORACLE_CLOUD_DEPLOYMENT.md)** | ☁️ Oracle Cloud Infrastructure deployment (OCI Always Free tier) |
| **[docs/CLOUD_AGGREGATOR_API.md](./docs/CLOUD_AGGREGATOR_API.md)** | 🔌 Cloud Aggregator API reference and frontend integration |
| **[docs/CLOUD_SYNC_SETUP.md](./docs/CLOUD_SYNC_SETUP.md)** | 🔄 Configure local-to-cloud data synchronization |

### Local Deployment
| Document | Purpose |
|----------|---------|
| **[docs/EMBEDDED_DEPLOYMENT.md](./docs/EMBEDDED_DEPLOYMENT.md)** | 📦 Standalone embedded JAR deployment (zero dependencies) |
| **[docs/DOCKER_LITE_DEPLOYMENT.md](./docs/DOCKER_LITE_DEPLOYMENT.md)** | 🐳 Lightweight Docker Compose deployment |
| **[KUBERNETES_DEPLOYMENT.md](./KUBERNETES_DEPLOYMENT.md)** | ☸️ Complete Kubernetes deployment guide (Helm, SSL, Production) |
| **[CUSTOMER_INSTALL.md](./CUSTOMER_INSTALL.md)** | 🚀 Simple Kubernetes installation via Helm (5 minutes, no repo clone) |

### Development & Testing
| Document | Purpose |
|----------|---------|
| **[LOCAL_DEVELOPMENT.md](./LOCAL_DEVELOPMENT.md)** | 🐳 Docker Compose, Kubernetes, and local development setup |
| **[TESTING-GUIDE.md](./TESTING-GUIDE.md)** | 🔐 Complete authentication testing with all credentials |
| **[CLAUDE.md](./CLAUDE.md)** | 🛠️ Development guidelines and project architecture |

### Feature Guides
| Document | Purpose |
|----------|---------|
| **[docs/PRODUCT_INVENTORY_GUIDE.md](./docs/PRODUCT_INVENTORY_GUIDE.md)** | 📦 Product & Inventory management (Two-Tier Model, FEFO) |
| **[docs/SHOP_ACCESS_CONTROL.md](./docs/SHOP_ACCESS_CONTROL.md)** | 🔒 Shop-level access control implementation guide |

## 🏗️ Architecture

**Multi-Tenant Retail Platform** with modular Spring Boot backend and React frontend:

### Core Modules
- **🏢 Core**: Tenant/Shop management, users, roles, product catalog
- **📦 Inventory**: Multi-batch stock tracking with FEFO, expiry management
- **🛒 Sales**: Transactions with automatic inventory deduction
- **💰 Investment**: Investment tracking and profit sharing
- **📊 Analytics**: Real-time analytics with caching
- **☁️ Aggregator**: Cloud tenant registration, shop tracking, cross-shop analytics (Cloud PaaS only)
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
- **Cloud Aggregator API**: Tenant registration, shop linking, API key auth (Migration V46) ✅
- **Cloud Deployment**: Oracle Cloud deployment guide with OCI Always Free tier ✅
- **Product & Inventory Refactoring**: Two-tier model with FEFO sales (Migration V10)
- **Authentication**: Full Keycloak SSO integration working
- **Frontend**: React app with TypeScript compilation resolved
- **Backend**: Spring Modulith with event store configured
- **Database**: All migrations applied (V46 latest)
- **Testing**: 1220/1225 tests passing (99.6%), comprehensive coverage

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