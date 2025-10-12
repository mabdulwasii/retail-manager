# Routing Map

**Version**: 1.0
**Last Updated**: January 2025
**Router**: React Router v6

---

## Table of Contents

1. [Route Structure](#route-structure)
2. [Role-Based Access Control](#role-based-access-control)
3. [Route Configuration](#route-configuration)
4. [Navigation Patterns](#navigation-patterns)
5. [Deep Linking](#deep-linking)

---

## Route Structure

### Public Routes

| Path | Component | Description |
|------|-----------|-------------|
| `/` | Landing/Redirect | Redirects to `/dashboard` if authenticated |
| `/login` | LoginPage | Keycloak login page |
| `/register` | RegisterPage | Tenant registration |
| `/auth/callback` | AuthCallback | OAuth callback handler |
| `/verify-email` | VerifyEmail | Email verification |
| `/reset-password` | ResetPassword | Password reset |

### Protected Routes (All Roles)

| Path | Component | Roles | Description |
|------|-----------|-------|-------------|
| `/dashboard` | DashboardPage | All | Role-based dashboard |
| `/profile` | ProfilePage | All | User profile |
| `/profile/edit` | EditProfile | All | Edit user profile |
| `/profile/settings` | ProfileSettings | All | User preferences |

### Super Admin Routes

| Path | Component | Roles | Description |
|------|-----------|-------|-------------|
| `/admin/dashboard` | AdminDashboard | SUPER_ADMIN | System overview |
| `/admin/tenants` | TenantList | SUPER_ADMIN | All tenants |
| `/admin/tenants/pending` | PendingTenants | SUPER_ADMIN | Pending registrations |
| `/admin/tenants/:tenantId` | TenantDetail | SUPER_ADMIN | Tenant details |
| `/admin/settings` | SystemSettings | SUPER_ADMIN | Global configuration |

### Shop Management Routes

| Path | Component | Roles | Description |
|------|-----------|-------|-------------|
| `/shops` | ShopsPage | TENANT_ADMIN, SHOP_OWNER, SHOP_MANAGER | Shop list |
| `/shops/create` | CreateShopPage | TENANT_ADMIN, SHOP_OWNER | Create new shop |
| `/shops/:shopId` | ShopDetailPage | TENANT_ADMIN, SHOP_OWNER, SHOP_MANAGER | Shop details |
| `/shops/:shopId/edit` | EditShopPage | TENANT_ADMIN, SHOP_OWNER, SHOP_MANAGER | Edit shop |
| `/shops/:shopId/settings` | ShopSettings | SHOP_OWNER, SHOP_MANAGER | Shop configuration |

### Inventory Routes

| Path | Component | Roles | Description |
|------|-----------|-------|-------------|
| `/inventory` | InventoryPage | SHOP_OWNER, SHOP_MANAGER, SHOP_EMPLOYEE | Inventory list |
| `/inventory/create` | CreateInventory | SHOP_OWNER, SHOP_MANAGER | Add inventory |
| `/inventory/:inventoryId` | InventoryDetail | SHOP_OWNER, SHOP_MANAGER, SHOP_EMPLOYEE | Inventory details |
| `/inventory/low-stock` | LowStockReport | SHOP_OWNER, SHOP_MANAGER | Low stock items |
| `/inventory/expiring` | ExpiringItems | SHOP_OWNER, SHOP_MANAGER | Expiring items |

### Sales Routes

| Path | Component | Roles | Description |
|------|-----------|-------|-------------|
| `/pos` | POSPage | CASHIER, SHOP_MANAGER | Point of sale |
| `/sales` | SalesPage | SHOP_OWNER, SHOP_MANAGER, CASHIER | Sales history |
| `/sales/:transactionId` | TransactionDetail | SHOP_OWNER, SHOP_MANAGER, CASHIER | Transaction details |

### Investment Routes

| Path | Component | Roles | Description |
|------|-----------|-------|-------------|
| `/investments` | InvestmentsPage | INVESTOR, SHOP_OWNER, TENANT_ADMIN | Investment portfolio |
| `/investments/create` | CreateInvestment | INVESTOR | New investment |
| `/investments/:investmentId` | InvestmentDetail | INVESTOR, SHOP_OWNER, TENANT_ADMIN | Investment details |
| `/investments/distributions` | DistributionList | INVESTOR, SHOP_OWNER | Distributions |
| `/investments/analytics` | InvestmentAnalytics | INVESTOR, SHOP_OWNER | ROI analysis |

### Expense Routes

| Path | Component | Roles | Description |
|------|-----------|-------|-------------|
| `/expenses` | ExpensesPage | All authenticated | Expense list |
| `/expenses/create` | CreateExpense | All authenticated | New expense |
| `/expenses/:expenseId` | ExpenseDetail | All authenticated | Expense details |
| `/expenses/approvals` | ExpenseApprovals | SHOP_OWNER, SHOP_MANAGER, ACCOUNTANT | Pending approvals |

### Returns Routes

| Path | Component | Roles | Description |
|------|-----------|-------|-------------|
| `/returns` | ReturnsPage | SHOP_OWNER, SHOP_MANAGER, CASHIER | Returns list |
| `/returns/create` | CreateReturn | SHOP_MANAGER, CASHIER | Process return |
| `/returns/:returnId` | ReturnDetail | SHOP_OWNER, SHOP_MANAGER, CASHIER | Return details |

### Fraud Detection Routes

| Path | Component | Roles | Description |
|------|-----------|-------|-------------|
| `/fraud` | FraudDashboard | SHOP_OWNER, SHOP_MANAGER, TENANT_ADMIN | Fraud overview |
| `/fraud/alerts` | FraudAlerts | SHOP_OWNER, SHOP_MANAGER, TENANT_ADMIN | Alert list |
| `/fraud/alerts/:alertId` | AlertDetail | SHOP_OWNER, SHOP_MANAGER, TENANT_ADMIN | Alert details |
| `/fraud/risk-assessments` | RiskAssessments | SHOP_OWNER, TENANT_ADMIN | Risk assessments |
| `/fraud/rules` | FraudRules | SHOP_OWNER, TENANT_ADMIN | Detection rules |

### Analytics Routes

| Path | Component | Roles | Description |
|------|-----------|-------|-------------|
| `/analytics` | AnalyticsPage | SHOP_OWNER, SHOP_MANAGER | Analytics dashboard |
| `/analytics/sales` | SalesAnalytics | SHOP_OWNER, SHOP_MANAGER | Sales analytics |
| `/analytics/revenue` | RevenueAnalytics | SHOP_OWNER, SHOP_MANAGER | Revenue analytics |
| `/analytics/inventory` | InventoryAnalytics | SHOP_OWNER, SHOP_MANAGER | Inventory analytics |

### Error Routes

| Path | Component | Description |
|------|-----------|-------------|
| `/403` | ForbiddenPage | Access denied |
| `/404` | NotFoundPage | Page not found |
| `/500` | ErrorPage | Server error |
| `*` | NotFoundPage | Catch-all 404 |

---

## Role-Based Access Control

### Access Matrix

| Role | Dashboard | Shops | Inventory | POS | Investments | Expenses | Fraud | Analytics |
|------|-----------|-------|-----------|-----|-------------|----------|-------|-----------|
| SUPER_ADMIN | Admin | ✗ | ✗ | ✗ | ✗ | ✗ | ✗ | ✗ |
| TENANT_ADMIN | Multi-Shop | ✓ | ✓ | ✗ | View | ✓ | ✓ | ✓ |
| SHOP_OWNER | Business | ✓ | ✓ | ✗ | ✓ | ✓ | ✓ | ✓ |
| SHOP_MANAGER | Operations | ✓ | ✓ | ✓ | ✗ | ✓ | View | ✓ |
| CASHIER | Simple | View | View | ✓ | ✗ | ✗ | ✗ | ✗ |
| SHOP_EMPLOYEE | Simple | View | View | ✗ | ✗ | ✗ | ✗ | ✗ |
| INVESTOR | Portfolio | ✗ | ✗ | ✗ | ✓ | ✗ | ✗ | ROI Only |
| ACCOUNTANT | Financial | ✗ | ✗ | ✗ | ✗ | ✓ | ✗ | Financial |

---

## Route Configuration

### App.tsx

```typescript
import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { ProtectedRoute } from '@/components/auth/ProtectedRoute';
import { RoleGuard } from '@/components/auth/RoleGuard';
import { Layout } from '@/components/layout/Layout';

function App() {
  return (
    <BrowserRouter>
      <Routes>
        {/* Public routes */}
        <Route path="/login" element={<LoginPage />} />
        <Route path="/register" element={<RegisterPage />} />
        <Route path="/auth/callback" element={<AuthCallback />} />

        {/* Protected routes */}
        <Route element={<ProtectedRoute />}>
          <Route element={<Layout />}>
            {/* Dashboard */}
            <Route path="/" element={<Navigate to="/dashboard" replace />} />
            <Route path="/dashboard" element={<DashboardPage />} />

            {/* Admin routes */}
            <Route element={<RoleGuard roles={['SUPER_ADMIN']} />}>
              <Route path="/admin/dashboard" element={<AdminDashboard />} />
              <Route path="/admin/tenants" element={<TenantList />} />
              <Route path="/admin/tenants/pending" element={<PendingTenants />} />
            </Route>

            {/* Shop routes */}
            <Route element={<RoleGuard roles={['TENANT_ADMIN', 'SHOP_OWNER', 'SHOP_MANAGER']} />}>
              <Route path="/shops" element={<ShopsPage />} />
              <Route path="/shops/:shopId" element={<ShopDetailPage />} />
            </Route>

            {/* Inventory routes */}
            <Route element={<RoleGuard roles={['SHOP_OWNER', 'SHOP_MANAGER', 'SHOP_EMPLOYEE']} />}>
              <Route path="/inventory" element={<InventoryPage />} />
            </Route>

            {/* POS routes */}
            <Route element={<RoleGuard roles={['CASHIER', 'SHOP_MANAGER']} />}>
              <Route path="/pos" element={<POSPage />} />
            </Route>

            {/* Investment routes */}
            <Route element={<RoleGuard roles={['INVESTOR', 'SHOP_OWNER', 'TENANT_ADMIN']} />}>
              <Route path="/investments" element={<InvestmentsPage />} />
            </Route>

            {/* Profile (all authenticated users) */}
            <Route path="/profile" element={<ProfilePage />} />
          </Route>
        </Route>

        {/* Error routes */}
        <Route path="/403" element={<ForbiddenPage />} />
        <Route path="/404" element={<NotFoundPage />} />
        <Route path="*" element={<NotFoundPage />} />
      </Routes>
    </BrowserRouter>
  );
}
```

### RoleGuard Component

```typescript
import { Navigate, Outlet } from 'react-router-dom';
import { useAuth } from '@/hooks/useAuth';

interface RoleGuardProps {
  roles: string[];
}

export function RoleGuard({ roles }: RoleGuardProps) {
  const { hasAnyRole } = useAuth();

  if (!hasAnyRole(roles)) {
    return <Navigate to="/403" replace />;
  }

  return <Outlet />;
}
```

---

## Navigation Patterns

### Programmatic Navigation

```typescript
import { useNavigate } from 'react-router-dom';

function MyComponent() {
  const navigate = useNavigate();

  const handleClick = () => {
    navigate('/shops/123');
  };

  const goBack = () => {
    navigate(-1);
  };

  return (
    <>
      <button onClick={handleClick}>Go to shop</button>
      <button onClick={goBack}>Go back</button>
    </>
  );
}
```

### Link Navigation

```typescript
import { Link } from 'react-router-dom';

<Link to="/shops/123" className="text-primary-600 hover:text-primary-700">
  View Shop
</Link>
```

### Breadcrumbs

```typescript
import { Link, useLocation } from 'react-router-dom';

function Breadcrumbs() {
  const location = useLocation();
  const pathnames = location.pathname.split('/').filter((x) => x);

  return (
    <nav aria-label="Breadcrumb">
      <ol className="flex space-x-2">
        <li>
          <Link to="/">Home</Link>
        </li>
        {pathnames.map((value, index) => {
          const to = `/${pathnames.slice(0, index + 1).join('/')}`;
          const isLast = index === pathnames.length - 1;

          return (
            <li key={to}>
              {!isLast ? (
                <Link to={to}>{value}</Link>
              ) : (
                <span>{value}</span>
              )}
            </li>
          );
        })}
      </ol>
    </nav>
  );
}
```

---

## Deep Linking

### URL Parameters

```typescript
import { useParams } from 'react-router-dom';

function ShopDetailPage() {
  const { shopId } = useParams();
  
  // Use shopId to fetch shop data
  const { data } = useShop(shopId);
  
  return <div>{data.name}</div>;
}
```

### Query Parameters

```typescript
import { useSearchParams } from 'react-router-dom';

function InventoryPage() {
  const [searchParams, setSearchParams] = useSearchParams();
  
  const status = searchParams.get('status');
  const category = searchParams.get('category');
  
  const updateFilter = (key: string, value: string) => {
    setSearchParams({ ...Object.fromEntries(searchParams), [key]: value });
  };
  
  return (
    <select onChange={(e) => updateFilter('status', e.target.value)}>
      <option value="all">All</option>
      <option value="active">Active</option>
    </select>
  );
}
```

---

**Document Version**: 1.0
**Last Updated**: January 2025
