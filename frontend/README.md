# Shop Manager Frontend

A comprehensive React frontend for the Shop Manager retail management platform. Built with modern technologies and following best practices for scalability, security, and performance.

## 🚀 Features

- **Multi-Tenant Shop Management**: Manage multiple retail locations with tenant isolation
- **Role-Based Access Control**: OWNER, MANAGER, CASHIER, INVESTOR roles with specific permissions
- **Real-Time Analytics**: Sales analytics, investment ROI, and business insights
- **Inventory Management**: Stock tracking, reservations, and alerts
- **Investment Portfolio**: Track investments and profit sharing
- **Receipt Management**: Generate, print, and email receipts
- **Audit Trail**: Comprehensive logging and security events
- **Responsive Design**: Mobile-first design with dark/light themes

## 🛠️ Tech Stack

- **React 18** with TypeScript
- **Vite** for fast development and optimized builds
- **TailwindCSS** with shadcn/ui components
- **Keycloak** for SSO authentication
- **React Query** for server state management
- **React Router v6** for client-side routing
- **Recharts** for data visualization
- **Jest** + **React Testing Library** for testing
- **MSW** for API mocking

## 📦 Installation

### Prerequisites

- Node.js 18+ and Yarn
- Backend services running (Shop Manager API + Keycloak)

### Quick Start

```bash
# Install dependencies
yarn install

# Copy environment variables
cp .env.example .env

# Start development server
yarn dev
```

### Environment Variables

```bash
# API Configuration
VITE_API_BASE_URL=http://localhost:8080/api

# Keycloak Configuration
VITE_KEYCLOAK_URL=http://localhost:8081
VITE_KEYCLOAK_REALM=shop-manager
VITE_KEYCLOAK_CLIENT_ID=shop-manager-frontend
```

## 🏗️ Development

### Available Scripts

```bash
# Development
yarn dev                # Start development server
yarn build             # Build for production
yarn preview           # Preview production build

# Testing
yarn test              # Run tests
yarn test:watch        # Run tests in watch mode
yarn test:coverage     # Generate coverage report

# Code Quality
yarn lint              # Lint code
yarn type-check        # TypeScript type checking
```

### Project Structure

```
src/
├── components/         # Reusable UI components
│   ├── ui/            # shadcn/ui components
│   ├── auth/          # Authentication components
│   └── layout/        # Layout components
├── pages/             # Page components
├── context/           # React contexts
├── services/          # API services
├── hooks/             # Custom hooks
├── types/             # TypeScript types
├── lib/               # Utilities and configurations
└── test/              # Test utilities
```

## 🔐 Authentication & Authorization

The application uses Keycloak for SSO authentication with the following roles:

- **SHOP_OWNER**: Full access to all features
- **SHOP_MANAGER**: Shop and product management, analytics
- **CASHIER**: Sales transactions and receipt generation
- **INVESTOR**: Investment portfolio and ROI tracking
- **SYSTEM_ADMIN**: System administration and audit logs

## 📱 Features by Module

### Dashboard
- Overview of shops, sales, and investments
- Recent activities feed
- Quick stats and KPIs
- Alert notifications

### Shop Management
- Create and manage multiple shops
- Shop configuration and settings
- Status management (Active/Inactive)
- Location and contact information

### Product & Inventory
- Product catalog management
- Stock level tracking
- Inventory reservations
- Low stock alerts

### Sales & Receipts
- Process sales transactions
- Generate itemized receipts
- Print and email receipts
- Transaction history

### Investments
- Investment portfolio tracking
- ROI analytics and reporting
- Profit sharing management
- Risk assessment

### Analytics
- Sales performance metrics
- Revenue trend analysis
- Investment ROI reports
- Fraud detection alerts

## 🐳 Docker Deployment

### Build Docker Image

```bash
docker build -t shop-manager-frontend .
```

### Run with Docker Compose

```bash
# Development
docker-compose up -d

# Production
docker-compose -f docker-compose.prod.yml up -d
```

### Kubernetes Deployment

```bash
# Deploy with Helm
helm install shop-manager-frontend ./helm-chart

# Or use kubectl
kubectl apply -f k8s/
```

## 🧪 Testing

The application includes comprehensive testing setup:

### Test Types
- **Unit Tests**: Component logic and utilities
- **Integration Tests**: Component interactions
- **API Tests**: Mocked API responses with MSW

### Coverage Targets
- **Overall**: 80% line coverage
- **Components**: 85% line coverage
- **Critical paths**: 95% line coverage

### Running Tests

```bash
# Run all tests
yarn test

# Watch mode
yarn test:watch

# Coverage report
yarn test:coverage
```

## 🔧 Configuration

### Nginx Configuration
The application includes production-ready Nginx configuration with:
- Gzip compression
- Security headers
- API proxy setup
- SPA routing support

### Environment Configuration
- Development: `.env.development`
- Production: `.env.production`
- Testing: `.env.test`

## 🚢 Production Deployment

### Build Process
1. **Install dependencies**: `yarn install --frozen-lockfile`
2. **Build application**: `yarn build`
3. **Static file serving**: Nginx serves optimized assets

### Performance Optimizations
- Code splitting and lazy loading
- Tree shaking for minimal bundle size
- Static asset caching
- Gzip compression
- CDN-ready static assets

### Security Considerations
- Content Security Policy headers
- XSS and CSRF protection
- Secure token handling
- Role-based route protection

## 📊 Monitoring & Analytics

### Health Checks
- Application health endpoint: `/health`
- Readiness probe support
- Liveness probe support

### Performance Monitoring
- Bundle size analysis
- Runtime performance tracking
- Error boundary implementation

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch
3. Follow TypeScript and ESLint rules
4. Write tests for new features
5. Submit a pull request

## 📄 License

This project is part of the Shop Manager platform. See the main project repository for license information.

## 🆘 Support

For support and questions:
- Check the [documentation](../docs/)
- Review [common issues](../docs/troubleshooting.md)
- Submit an issue on GitHub

---

**Shop Manager Frontend v1.0.0** - Production-ready React application for retail management