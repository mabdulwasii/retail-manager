# Shop Manager Testing Guide

**🔐 Complete Authentication & Testing Reference**

This guide provides comprehensive testing instructions and credentials for the Shop Manager application with Keycloak authentication.

## 📋 Quick Reference

### System Status ✅

#### Docker Compose Deployment
- **Frontend**: http://localhost:3001 (React + Keycloak)
- **Backend**: http://localhost:8081 (Spring Boot + JWT)
- **Keycloak**: http://localhost:8080 (shop-manager realm)
- **Database**: localhost:5432/shopdb (PostgreSQL)
- **Kafka**: localhost:9093 (KRaft mode)

#### Kubernetes Deployment (Helm)
- **Frontend**: Port forward to access: `kubectl port-forward svc/shop-manager-frontend 3000:3000 -n shop-manager`
- **Backend**: Port forward to access: `kubectl port-forward svc/shop-manager 8081:8081 -n shop-manager`
- **Keycloak**: Port forward to access: `kubectl port-forward svc/shop-manager-keycloak 8080:80 -n shop-manager`
- **Database**: Port forward to access: `kubectl port-forward svc/shop-manager-postgresql 5432:5432 -n shop-manager`

**Last Updated**: January 2025

---

## 🧪 Test User Accounts

⚠️ **SECURITY WARNING**: These credentials are for DEVELOPMENT/TESTING ONLY and are automatically created during Keycloak realm import. They MUST be disabled in production environments.

### Primary Test Users (shop-manager Realm)

```
👤 SYSTEM ADMINISTRATOR
Email: admin@shopmanager.com
Password: DevAdmin@2024!Test
Role: TENANT_ADMIN
Access: Full system administration, tenant management
Default Context: default-tenant/default-shop

👤 SHOP MANAGER
Email: manager@shopmanager.com
Password: DevManager@2024!Test
Role: SHOP_MANAGER
Access: Shop operations, inventory, sales, reports
Default Context: default-tenant/default-shop

👤 SHOP EMPLOYEE
Email: employee@shopmanager.com
Password: DevEmployee@2024!Test
Role: SHOP_EMPLOYEE
Access: Sales transactions, basic inventory queries
Default Context: default-tenant/default-shop

👤 INVESTOR
Email: investor@shopmanager.com
Password: DevInvestor@2024!Test
Role: INVESTOR
Access: Investment tracking, profit reports, analytics
Default Context: default-tenant

👤 CUSTOMER
Email: customer@shopmanager.com
Password: DevCustomer@2024!Test
Role: CUSTOMER
Access: Purchase history, receipts, order tracking
Default Context: default-tenant

👤 CASHIER
Email: cashier@shopmanager.com
Password: DevCashier@2024!Test
Role: CASHIER
Access: Cashier operations, sales transactions, receipt printing
Default Context: default-tenant/default-shop

👤 ACCOUNTANT
Email: accountant@shopmanager.com
Password: DevAccountant@2024!Test
Role: ACCOUNTANT
Access: Financial reports, accounting, expense tracking
Default Context: default-tenant

👤 AUDITOR
Email: auditor@shopmanager.com
Password: DevAuditor@2024!Test
Role: AUDITOR
Access: Audit logs, compliance reports, system monitoring
Default Context: default-tenant
```

---

## 🔧 Service Credentials

### Infrastructure Services

```
🗄️ POSTGRESQL DATABASE
Host: localhost:5432
Database: shopdb
Username: shop
Password: shop
Connection: jdbc:postgresql://localhost:5432/shopdb

🔐 KEYCLOAK ADMIN CONSOLE
URL: http://localhost:8080
Username: admin
Password: admin
Realm: shop-manager

📦 MINIO OBJECT STORAGE
Console: http://localhost:9001
Access Key: minioadmin
Secret Key: minioadmin

📊 SONARQUBE (Optional)
URL: http://localhost:9090
Default: admin/admin (first time setup required)

📨 KAFKA MESSAGE BROKER
URL: localhost:9093
Port: 9093 (KRaft mode, no Zookeeper required)
Health: Run `docker exec shop-manager-kafka kafka-broker-api-versions --bootstrap-server localhost:9093`
```

### OAuth2/OIDC Client Configuration

```
🖥️ FRONTEND CLIENT
Client ID: shop-manager-frontend
Client Type: Public (no secret required)
Flow: Authorization Code + PKCE
Redirect URIs:
  - http://localhost:3000/*
  - http://localhost:3000/auth/callback

🔧 BACKEND SERVICE CLIENT
Client ID: shop-manager-backend
Client Secret: shop-manager-backend-secret
Flow: Client Credentials
Grant Types: Client Credentials, Service Account
```

---

## 🚀 Testing Procedures

### 1. Environment Setup

#### Option A: Docker Compose (Recommended for Development)
```bash
# Clone and start services
git clone <repository-url>
cd shop-manager

# Start all services
docker-compose up -d

# Verify services are running
docker ps | grep shop-manager

# Check service health
curl http://localhost:8081/actuator/health
curl http://localhost:8080/realms/shop-manager/.well-known/openid-configuration
```

#### Option B: Kubernetes with Helm (Production-like)
```bash
# Create namespace
kubectl create namespace shop-manager

# Deploy with Helm
helm install shop-manager ./helm-chart/shop-manager \
  -f ./helm-chart/shop-manager/values-simple.yaml \
  -n shop-manager --wait

# Check pods are running
kubectl get pods -n shop-manager

# Port forward services for local access
kubectl port-forward svc/shop-manager-keycloak 8080:80 -n shop-manager &
kubectl port-forward svc/shop-manager 8081:8081 -n shop-manager &
kubectl port-forward svc/shop-manager-frontend 3000:3000 -n shop-manager &
```

### 2. Frontend Authentication Testing

```bash
# Open application
open http://localhost:3000

# Test login flow:
# 1. Click login/sign-in button
# 2. Should redirect to: http://localhost:8080/realms/shop-manager/protocol/openid-connect/auth
# 3. Enter test credentials (any user above)
# 4. Should redirect back to http://localhost:3000 with JWT token
# 5. Verify user info is displayed in UI
```

### 3. API Authentication Testing

```bash
# Test direct token acquisition
curl -X POST "http://localhost:8080/realms/shop-manager/protocol/openid-connect/token" \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "username=admin@shopmanager.com" \
  -d "password=DevAdmin@2024!Test" \
  -d "grant_type=password" \
  -d "client_id=shop-manager-frontend"

# Extract token and test API call
TOKEN="<access_token_from_above>"
curl -H "Authorization: Bearer $TOKEN" http://localhost:8081/api/shops
```

### 4. Role-Based Access Testing

| User Type | Expected Access | Test URLs |
|-----------|----------------|-----------|
| **TENANT_ADMIN** | Full system access | All endpoints |
| **SHOP_MANAGER** | Shop operations | `/api/shops`, `/api/products`, `/api/sales` |
| **SHOP_EMPLOYEE** | Limited operations | `/api/sales`, `/api/products` (read-only) |
| **INVESTOR** | Investment data | `/api/investments`, `/api/analytics` |
| **CUSTOMER** | Personal data | `/api/receipts`, `/api/orders` |

---

## 🔍 Authentication Endpoints Reference

### Keycloak OpenID Connect Endpoints

```
# Discovery Document
GET http://localhost:8080/realms/shop-manager/.well-known/openid-configuration

# Authorization Endpoint (User Login)
GET http://localhost:8080/realms/shop-manager/protocol/openid-connect/auth
Parameters: client_id, response_type, scope, redirect_uri, state

# Token Endpoint (Token Exchange)
POST http://localhost:8080/realms/shop-manager/protocol/openid-connect/token
Content-Type: application/x-www-form-urlencoded

# User Information Endpoint
GET http://localhost:8080/realms/shop-manager/protocol/openid-connect/userinfo
Authorization: Bearer <access_token>

# Logout Endpoint
POST http://localhost:8080/realms/shop-manager/protocol/openid-connect/logout
```

### Backend API Endpoints

```
# Health Check (Public)
GET http://localhost:8081/actuator/health

# API Documentation (Public)
GET http://localhost:8081/swagger-ui.html

# Protected Shop Endpoints (Requires Authentication)
GET http://localhost:8081/api/shops
POST http://localhost:8081/api/shops
GET http://localhost:8081/api/shops/{id}

# Analytics Endpoints (Role-based)
GET http://localhost:8081/api/analytics/sales-summary
GET http://localhost:8081/api/analytics/investment-overview
```

---

## 🛠️ Troubleshooting

### Common Issues and Solutions

**Issue**: Frontend shows "Network Error" when logging in
```bash
# Check if Keycloak realm accepts HTTP requests
curl http://localhost:8080/realms/shop-manager

# If returns "HTTPS required", fix with:
docker exec shop-manager-keycloak /opt/keycloak/bin/kcadm.sh \
  config credentials --server http://localhost:8080 --realm master --user admin --password admin
docker exec shop-manager-keycloak /opt/keycloak/bin/kcadm.sh \
  update realms/shop-manager -s sslRequired=none
```

**Issue**: Backend returns 401 Unauthorized
```bash
# Verify JWT token format
echo "<your-token>" | cut -d'.' -f2 | base64 -d | jq

# Check Keycloak issuer configuration
curl http://localhost:8080/realms/shop-manager | jq .
```

**Issue**: Database connection errors
```bash
# Check PostgreSQL is running and accessible
docker exec shop-manager-postgres psql -U shop -d shopdb -c "SELECT version();"

# Verify migrations are applied
docker exec shop-manager-postgres psql -U shop -d shopdb -c "SELECT * FROM flyway_schema_history ORDER BY installed_rank DESC LIMIT 5;"
```

### Service Health Commands

```bash
# Check all services status
docker-compose ps

# View service logs
docker logs shop-manager-frontend --tail 50
docker logs shop-manager-backend --tail 50
docker logs shop-manager-keycloak --tail 50

# Restart specific service
docker-compose restart backend
docker-compose restart keycloak
```

---

## 📝 Testing Checklist

### Pre-Test Verification ✅
- [ ] All Docker containers running
- [ ] Database migrations applied
- [ ] Keycloak realm imported
- [ ] SSL requirement disabled for development
- [ ] Frontend builds without TypeScript errors

### Authentication Flow Testing ✅
- [ ] User can access login page
- [ ] Keycloak redirects work correctly
- [ ] All 5 test users can log in successfully
- [ ] JWT tokens are properly formatted
- [ ] User info displays correctly in frontend

### Authorization Testing ✅
- [ ] Admin users have full access
- [ ] Shop managers can manage shop operations
- [ ] Employees have limited access
- [ ] Investors can view investment data
- [ ] Customers can access personal data only

### API Integration Testing ✅
- [ ] Frontend can call backend APIs with JWT
- [ ] CORS configuration allows cross-origin requests
- [ ] Token refresh works automatically
- [ ] Logout clears session properly

---

## 🎯 Next Steps

After completing authentication testing:

1. **API Testing**: Use tools like Postman or curl to test all REST endpoints
2. **Performance Testing**: Load test with multiple concurrent users
3. **Security Testing**: Verify JWT validation, CORS policies, SQL injection protection
4. **End-to-End Testing**: Test complete business workflows across roles
5. **Mobile Testing**: Verify responsive design and mobile authentication

---

**📅 Last Updated**: January 2025
**🔗 Related Docs**: [DEPLOYMENT.md](./DEPLOYMENT.md) | [CLAUDE.md](./CLAUDE.md) | [docker-compose.yml](./docker-compose.yml)