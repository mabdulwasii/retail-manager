# Shop Manager

**🏪 Modular Retail Management Platform**

A comprehensive multi-tenant retail management system built with Spring Boot and React, featuring investment tracking, sales analytics, and Keycloak authentication.

## 🚀 Quick Start

### Run with Docker Compose
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
| **[KUBERNETES_DEPLOYMENT.md](./KUBERNETES_DEPLOYMENT.md)** | ☸️ Complete Kubernetes deployment guide (Helm, SSL, Production) |
| **[DOCKER_COMPOSE_USAGE.md](./DOCKER_COMPOSE_USAGE.md)** | 🐳 Docker Compose local development setup |
| **[TESTING-GUIDE.md](./TESTING-GUIDE.md)** | 🔐 Complete authentication testing with all credentials |
| **[LOCAL-DEVELOPMENT-SETUP.md](./LOCAL-DEVELOPMENT-SETUP.md)** | 💻 Local development environment setup |
| **[CLAUDE.md](./CLAUDE.md)** | 🛠️ Development guidelines and project architecture |

## 🏗️ Architecture

**Multi-Tenant Retail Platform** with modular Spring Boot backend and React frontend:

### Core Modules
- **🏢 Core**: Tenant/Shop management, users, roles
- **💰 Investment**: Investment tracking and profit sharing
- **📊 Analytics**: Real-time analytics with caching
- **🛒 Sales**: Transactions, receipts, line items
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
- **Shop Management**: Inventory, products, categories
- **Sales Processing**: Transactions, receipts, reporting
- **Investment Tracking**: ROI analytics, profit distribution
- **User Management**: RBAC with Keycloak roles
- **Analytics Dashboard**: Real-time sales and performance metrics

### Technical Features
- **Event-Driven**: Spring Modulith with domain events
- **Multi-Database**: Flyway migrations with versioning
- **API Security**: JWT-based authentication
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
- **Authentication**: Full Keycloak SSO integration working
- **Frontend**: React app with TypeScript compilation resolved
- **Backend**: Spring Modulith with event store configured
- **Database**: All migrations applied (V9 latest)
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

**🔗 Quick Links**: [Test Credentials](./TESTING-GUIDE.md#-test-user-accounts) | [Deployment](./DEPLOYMENT.md) | [Architecture](./CLAUDE.md)