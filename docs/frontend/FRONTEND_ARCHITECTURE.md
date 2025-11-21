# Frontend Architecture Guide

**Version**: 1.0
**Last Updated**: January 2025
**Tech Stack**: React 18 + TypeScript + Vite

---

## Table of Contents

1. [Architecture Overview](#architecture-overview)
2. [Project Structure](#project-structure)
3. [State Management](#state-management)
4. [Routing Strategy](#routing-strategy)
5. [API Integration](#api-integration)
6. [Authentication & Authorization](#authentication--authorization)
7. [Form Handling](#form-handling)
8. [Error Handling](#error-handling)
9. [Performance Optimization](#performance-optimization)
10. [Build & Deployment](#build--deployment)

---

## Architecture Overview

### Technology Stack

```
Frontend Framework
├── React 18.2 (UI library)
├── TypeScript 5.2 (Type safety)
├── Vite 4.5 (Build tool)
└── TailwindCSS 3.3 (Styling)

State Management
├── @tanstack/react-query 5.0 (Server state)
├── React Context API (Global UI state)
└── React Hook Form 7.48 (Form state)

UI Components
├── Radix UI (Headless components)
├── shadcn/ui (Component library)
├── Lucide React (Icons)
└── Recharts 2.8 (Data visualization)

Authentication
├── Keycloak 22.0 (Identity provider)
└── @react-keycloak/web 3.4 (Keycloak integration)

Validation & Forms
├── React Hook Form (Form management)
├── Yup 1.3 (Schema validation)
└── @hookform/resolvers (Validation integration)

Utilities
├── Axios 1.6 (HTTP client)
├── date-fns 2.30 (Date manipulation)
├── clsx + tailwind-merge (Class name management)
└── sonner (Toast notifications)

Internationalization
├── i18next 25.5 (i18n framework)
├── react-i18next 15.7 (React integration)
└── i18next-browser-languagedetector (Language detection)

Testing
├── Jest 29.7 (Test framework)
├── React Testing Library 13.4 (Component testing)
└── MSW 2.0 (API mocking)
```

### Architecture Principles

1. **Component-Based**: Modular, reusable components
2. **Type-Safe**: TypeScript everywhere
3. **Server State Separation**: React Query for API data
4. **Lazy Loading**: Code splitting for performance
5. **Accessibility First**: WCAG 2.1 AA compliance
6. **Mobile Responsive**: Mobile-first design
7. **Progressive Enhancement**: Works without JavaScript (where possible)
8. **Security**: JWT-based authentication, HTTPS only

---

## Project Structure

```
frontend/
├── public/                     # Static assets
│   ├── locales/               # Translation files
│   │   ├── en/
│   │   │   └── translation.json
│   │   └── fr/
│   │       └── translation.json
│   ├── favicon.ico
│   └── index.html
│
├── src/
│   ├── main.tsx               # Application entry point
│   ├── App.tsx                # Root component
│   ├── vite-env.d.ts          # Vite type definitions
│   │
│   ├── components/            # Reusable components
│   │   ├── ui/               # Base UI components (shadcn/ui)
│   │   │   ├── button.tsx
│   │   │   ├── card.tsx
│   │   │   ├── input.tsx
│   │   │   ├── select.tsx
│   │   │   ├── dialog.tsx
│   │   │   ├── dropdown-menu.tsx
│   │   │   ├── tabs.tsx
│   │   │   ├── alert.tsx
│   │   │   ├── badge.tsx
│   │   │   ├── avatar.tsx
│   │   │   ├── separator.tsx
│   │   │   ├── loading-spinner.tsx
│   │   │   ├── currency-selector.tsx
│   │   │   └── LanguageSwitcher.tsx
│   │   │
│   │   ├── layout/           # Layout components
│   │   │   ├── Layout.tsx    # Main layout wrapper
│   │   │   ├── Navbar.tsx    # Top navigation
│   │   │   ├── Sidebar.tsx   # Side navigation
│   │   │   └── Footer.tsx    # Page footer
│   │   │
│   │   ├── auth/             # Authentication components
│   │   │   ├── ProtectedRoute.tsx
│   │   │   ├── OAuthCallback.tsx
│   │   │   └── EmbeddedKeycloakLogin.tsx
│   │   │
│   │   ├── dashboard/        # Dashboard components
│   │   │   ├── RoleBasedDashboard.tsx
│   │   │   ├── AdminDashboard.tsx
│   │   │   ├── AccountantDashboard.tsx
│   │   │   ├── OwnerManagerDashboard.tsx
│   │   │   ├── CashierDashboard.tsx
│   │   │   ├── EmployeeDashboard.tsx
│   │   │   ├── InvestorDashboard.tsx
│   │   │   ├── AuditorDashboard.tsx
│   │   │   └── CustomerDashboard.tsx
│   │   │
│   │   ├── sales/            # Sales module components
│   │   │   ├── ProductSearch.tsx
│   │   │   ├── ShoppingCart.tsx
│   │   │   ├── PaymentModal.tsx
│   │   │   └── SalesHistory.tsx
│   │   │
│   │   ├── inventory/        # Inventory module components
│   │   │   ├── InventoryList.tsx
│   │   │   ├── InventoryForm.tsx
│   │   │   ├── InventoryFilters.tsx
│   │   │   ├── InventorySummaryCards.tsx
│   │   │   └── StockAdjustmentModal.tsx
│   │   │
│   │   ├── investment/       # Investment module components
│   │   │   ├── InvestmentList.tsx
│   │   │   ├── InvestmentDetails.tsx
│   │   │   ├── InvestmentForm.tsx
│   │   │   ├── InvestmentSummaryCards.tsx
│   │   │   ├── DistributionManagement.tsx
│   │   │   └── WithdrawalForm.tsx
│   │   │
│   │   ├── expenses/         # Expense module components
│   │   │   ├── ExpenseList.tsx
│   │   │   └── ExpenseSummaryCards.tsx
│   │   │
│   │   ├── fraud/            # Fraud detection components
│   │   │   ├── FraudDashboard.tsx
│   │   │   ├── FraudAlertList.tsx
│   │   │   ├── RiskAssessmentList.tsx
│   │   │   └── FraudRuleList.tsx
│   │   │
│   │   ├── analytics/        # Analytics components
│   │   │   ├── AnalyticsSummaryCards.tsx
│   │   │   ├── AnalyticsFilters.tsx
│   │   │   └── AnalyticsCharts.tsx
│   │   │
│   │   ├── charts/           # Chart components
│   │   │   ├── LineChart.tsx
│   │   │   ├── BarChart.tsx
│   │   │   ├── PieChart.tsx
│   │   │   └── AreaChart.tsx
│   │   │
│   │   ├── notifications/    # Notification components
│   │   │   ├── NotificationBell.tsx
│   │   │   └── NotificationSettings.tsx
│   │   │
│   │   └── AuthenticatedApp.tsx  # Authenticated app wrapper
│   │
│   ├── pages/                # Page components
│   │   ├── auth/
│   │   │   ├── LoginPage.tsx
│   │   │   ├── RegisterPage.tsx
│   │   │   └── AuthCallback.tsx
│   │   │
│   │   ├── dashboard/
│   │   │   └── DashboardPage.tsx
│   │   │
│   │   ├── shops/
│   │   │   ├── ShopsPage.tsx
│   │   │   ├── ShopDetailPage.tsx
│   │   │   └── CreateShopPage.tsx
│   │   │
│   │   ├── inventory/
│   │   │   └── InventoryPage.tsx
│   │   │
│   │   ├── sales/
│   │   │   └── SalesPage.tsx
│   │   │
│   │   ├── investments/
│   │   │   └── InvestmentsPage.tsx
│   │   │
│   │   ├── analytics/
│   │   │   └── AnalyticsPage.tsx
│   │   │
│   │   ├── audit/
│   │   │   └── AuditPage.tsx
│   │   │
│   │   ├── receipts/
│   │   │   └── ReceiptsPage.tsx
│   │   │
│   │   ├── ProfilePage.tsx
│   │   ├── ExpensesPage.tsx
│   │   ├── InventoryPage.tsx
│   │   ├── SalesPage.tsx
│   │   ├── InvestmentDashboard.tsx
│   │   ├── AnalyticsPage.tsx
│   │   └── NotFoundPage.tsx
│   │
│   ├── hooks/                # Custom React hooks
│   │   ├── useAuth.ts        # Authentication hook
│   │   ├── useShop.ts        # Shop context hook
│   │   ├── useTenant.ts      # Tenant context hook
│   │   ├── useInventory.ts   # Inventory operations
│   │   ├── useInvestments.ts # Investment operations
│   │   ├── useAnalytics.ts   # Analytics queries
│   │   ├── useDebounce.ts    # Debounce utility
│   │   └── useLocalStorage.ts # Local storage utility
│   │
│   ├── services/             # API service layer
│   │   ├── api.ts            # Axios configuration
│   │   ├── authService.ts    # Authentication APIs
│   │   ├── shopService.ts    # Shop management APIs
│   │   ├── inventoryService.ts # Inventory APIs
│   │   ├── investmentService.ts # Investment APIs
│   │   ├── expenseService.ts # Expense APIs
│   │   ├── fraudService.ts   # Fraud detection APIs
│   │   ├── analyticsService.ts # Analytics APIs
│   │   └── receiptService.ts # Receipt APIs
│   │
│   ├── context/              # React context providers
│   │   ├── ThemeContext.tsx  # Theme state
│   │   ├── CurrencyContext.tsx # Currency selection
│   │   ├── SidebarContext.tsx # Sidebar state
│   │   └── ManualAuthContext.tsx # Manual auth context
│   │
│   ├── providers/            # Provider components
│   │   └── KeycloakAuthProvider.tsx # Keycloak provider
│   │
│   ├── types/                # TypeScript type definitions
│   │   ├── api.ts            # API types
│   │   ├── user.ts           # User types
│   │   └── index.ts          # Barrel exports
│   │
│   ├── config/               # Configuration files
│   │   ├── runtime-config.ts # Runtime configuration
│   │   └── constants.ts      # Application constants
│   │
│   ├── utils/                # Utility functions
│   │   ├── cn.ts             # Class name utility
│   │   ├── formatters.ts     # Data formatters
│   │   ├── validators.ts     # Validation utilities
│   │   └── helpers.ts        # General helpers
│   │
│   ├── lib/                  # Third-party library configs
│   │   ├── axios.ts          # Axios setup
│   │   ├── queryClient.ts    # React Query setup
│   │   └── i18n.ts           # i18next setup
│   │
│   ├── styles/               # Global styles
│   │   ├── globals.css       # Global CSS + Tailwind
│   │   └── tailwind.css      # Tailwind imports
│   │
│   └── test/                 # Test utilities
│       ├── setupTests.ts     # Test setup
│       └── components/       # Component tests
│
├── .env.development          # Dev environment variables
├── .env.production           # Production environment variables
├── .eslintrc.cjs             # ESLint configuration
├── .prettierrc               # Prettier configuration
├── tsconfig.json             # TypeScript configuration
├── tsconfig.node.json        # TypeScript for Node
├── vite.config.ts            # Vite configuration
├── tailwind.config.js        # Tailwind configuration
├── postcss.config.js         # PostCSS configuration
├── jest.config.js            # Jest configuration
├── package.json              # Dependencies
└── README.md                 # Project documentation
```

---

## State Management

### State Management Strategy

Shop Manager uses a **hybrid state management approach**:

1. **Server State**: React Query (@tanstack/react-query)
2. **Global UI State**: React Context API
3. **Local Component State**: useState/useReducer
4. **Form State**: React Hook Form

### 1. Server State (React Query)

**Purpose**: Manage all API data (shops, inventory, sales, investments, etc.)

#### React Query Configuration

```typescript
// src/lib/queryClient.ts
import { QueryClient } from '@tanstack/react-query';

export const queryClient = new QueryClient({
  defaultOptions: {
    queries: {
      staleTime: 1000 * 60 * 5, // 5 minutes
      cacheTime: 1000 * 60 * 30, // 30 minutes
      refetchOnWindowFocus: false,
      refetchOnReconnect: true,
      retry: 1,
      retryDelay: (attemptIndex) => Math.min(1000 * 2 ** attemptIndex, 30000),
    },
    mutations: {
      retry: 0,
    },
  },
});
```

#### Query Hooks Pattern

```typescript
// src/hooks/useInventory.ts
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { inventoryService } from '@/services/inventoryService';
import type { InventoryResponse, InventoryCreateRequest } from '@/types/api';

// Query keys
export const inventoryKeys = {
  all: ['inventory'] as const,
  lists: () => [...inventoryKeys.all, 'list'] as const,
  list: (shopId: string, filters?: Record<string, any>) =>
    [...inventoryKeys.lists(), shopId, filters] as const,
  details: () => [...inventoryKeys.all, 'detail'] as const,
  detail: (id: string) => [...inventoryKeys.details(), id] as const,
  lowStock: (shopId: string) => [...inventoryKeys.all, 'lowStock', shopId] as const,
  expiring: (shopId: string, days: number) =>
    [...inventoryKeys.all, 'expiring', shopId, days] as const,
};

// Fetch inventory list
export function useInventoryList(shopId: string, filters?: Record<string, any>) {
  return useQuery({
    queryKey: inventoryKeys.list(shopId, filters),
    queryFn: () => inventoryService.getInventory(shopId, filters),
    enabled: !!shopId,
  });
}

// Fetch single inventory item
export function useInventory(inventoryId: string) {
  return useQuery({
    queryKey: inventoryKeys.detail(inventoryId),
    queryFn: () => inventoryService.getInventoryById(inventoryId),
    enabled: !!inventoryId,
  });
}

// Create inventory mutation
export function useCreateInventory() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (data: InventoryCreateRequest & { shopId: string }) =>
      inventoryService.createInventory(data.shopId, data),
    onSuccess: (_, variables) => {
      // Invalidate and refetch inventory list
      queryClient.invalidateQueries({
        queryKey: inventoryKeys.lists()
      });

      // Show success toast
      toast.success('Inventory created successfully');
    },
    onError: (error: any) => {
      toast.error(error.response?.data?.message || 'Failed to create inventory');
    },
  });
}

// Adjust stock mutation
export function useAdjustStock() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: ({ inventoryId, data }: {
      inventoryId: string;
      data: InventoryAdjustmentRequest
    }) => inventoryService.adjustStock(inventoryId, data),
    onMutate: async ({ inventoryId, data }) => {
      // Cancel outgoing refetches
      await queryClient.cancelQueries({
        queryKey: inventoryKeys.detail(inventoryId)
      });

      // Snapshot previous value
      const previousInventory = queryClient.getQueryData(
        inventoryKeys.detail(inventoryId)
      );

      // Optimistically update
      queryClient.setQueryData(
        inventoryKeys.detail(inventoryId),
        (old: InventoryResponse) => ({
          ...old,
          quantity: data.newStockLevel,
        })
      );

      return { previousInventory };
    },
    onError: (err, variables, context) => {
      // Rollback on error
      if (context?.previousInventory) {
        queryClient.setQueryData(
          inventoryKeys.detail(variables.inventoryId),
          context.previousInventory
        );
      }
      toast.error('Failed to adjust stock');
    },
    onSuccess: (_, variables) => {
      queryClient.invalidateQueries({
        queryKey: inventoryKeys.detail(variables.inventoryId)
      });
      queryClient.invalidateQueries({
        queryKey: inventoryKeys.lists()
      });
      toast.success('Stock adjusted successfully');
    },
  });
}

// Fetch low stock items
export function useLowStockItems(shopId: string) {
  return useQuery({
    queryKey: inventoryKeys.lowStock(shopId),
    queryFn: () => inventoryService.getLowStock(shopId),
    enabled: !!shopId,
  });
}

// Fetch expiring items
export function useExpiringItems(shopId: string, daysThreshold: number = 30) {
  return useQuery({
    queryKey: inventoryKeys.expiring(shopId, daysThreshold),
    queryFn: () => inventoryService.getExpiring(shopId, daysThreshold),
    enabled: !!shopId,
    staleTime: 1000 * 60 * 15, // 15 minutes (less frequent updates)
  });
}
```

#### Usage in Components

```typescript
// src/pages/inventory/InventoryPage.tsx
import { useInventoryList, useCreateInventory } from '@/hooks/useInventory';

function InventoryPage() {
  const { shopId } = useShop(); // From context
  const [filters, setFilters] = useState({});

  const { data, isLoading, error } = useInventoryList(shopId, filters);
  const createMutation = useCreateInventory();

  const handleCreate = (formData: InventoryCreateRequest) => {
    createMutation.mutate({ shopId, ...formData });
  };

  if (isLoading) return <LoadingSpinner />;
  if (error) return <ErrorState error={error} />;

  return (
    <div>
      <InventoryList items={data.content} />
      <CreateInventoryModal onSubmit={handleCreate} />
    </div>
  );
}
```

### 2. Global UI State (Context API)

**Purpose**: Manage global UI state (theme, sidebar, currency, language)

#### Theme Context

```typescript
// src/context/ThemeContext.tsx
import { createContext, useContext, useState, useEffect } from 'react';

type Theme = 'light' | 'dark' | 'system';

interface ThemeContextType {
  theme: Theme;
  setTheme: (theme: Theme) => void;
  resolvedTheme: 'light' | 'dark';
}

const ThemeContext = createContext<ThemeContextType | undefined>(undefined);

export function ThemeProvider({ children }: { children: React.ReactNode }) {
  const [theme, setTheme] = useState<Theme>(() => {
    const stored = localStorage.getItem('theme');
    return (stored as Theme) || 'system';
  });

  const [resolvedTheme, setResolvedTheme] = useState<'light' | 'dark'>('light');

  useEffect(() => {
    const root = window.document.documentElement;
    const mediaQuery = window.matchMedia('(prefers-color-scheme: dark)');

    const updateResolvedTheme = () => {
      if (theme === 'system') {
        setResolvedTheme(mediaQuery.matches ? 'dark' : 'light');
      } else {
        setResolvedTheme(theme);
      }
    };

    updateResolvedTheme();
    root.classList.remove('light', 'dark');
    root.classList.add(resolvedTheme);

    mediaQuery.addEventListener('change', updateResolvedTheme);
    return () => mediaQuery.removeEventListener('change', updateResolvedTheme);
  }, [theme, resolvedTheme]);

  useEffect(() => {
    localStorage.setItem('theme', theme);
  }, [theme]);

  return (
    <ThemeContext.Provider value={{ theme, setTheme, resolvedTheme }}>
      {children}
    </ThemeContext.Provider>
  );
}

export function useTheme() {
  const context = useContext(ThemeContext);
  if (!context) {
    throw new Error('useTheme must be used within ThemeProvider');
  }
  return context;
}
```

#### Currency Context

```typescript
// src/context/CurrencyContext.tsx
import { createContext, useContext, useState, useEffect } from 'react';

interface Currency {
  code: string;
  symbol: string;
  name: string;
}

const CURRENCIES: Currency[] = [
  { code: 'USD', symbol: '$', name: 'US Dollar' },
  { code: 'EUR', symbol: '€', name: 'Euro' },
  { code: 'GBP', symbol: '£', name: 'British Pound' },
  { code: 'NGN', symbol: '₦', name: 'Nigerian Naira' },
];

interface CurrencyContextType {
  currency: Currency;
  setCurrency: (currency: Currency) => void;
  formatAmount: (amount: number) => string;
}

const CurrencyContext = createContext<CurrencyContextType | undefined>(undefined);

export function CurrencyProvider({ children }: { children: React.ReactNode }) {
  const [currency, setCurrency] = useState<Currency>(() => {
    const stored = localStorage.getItem('currency');
    return stored ? JSON.parse(stored) : CURRENCIES[0];
  });

  useEffect(() => {
    localStorage.setItem('currency', JSON.stringify(currency));
  }, [currency]);

  const formatAmount = (amount: number) => {
    return new Intl.NumberFormat('en-US', {
      style: 'currency',
      currency: currency.code,
    }).format(amount);
  };

  return (
    <CurrencyContext.Provider value={{ currency, setCurrency, formatAmount }}>
      {children}
    </CurrencyContext.Provider>
  );
}

export function useCurrency() {
  const context = useContext(CurrencyContext);
  if (!context) {
    throw new Error('useCurrency must be used within CurrencyProvider');
  }
  return context;
}
```

#### Sidebar Context

```typescript
// src/context/SidebarContext.tsx
import { createContext, useContext, useState } from 'react';

interface SidebarContextType {
  isOpen: boolean;
  toggle: () => void;
  open: () => void;
  close: () => void;
}

const SidebarContext = createContext<SidebarContextType | undefined>(undefined);

export function SidebarProvider({ children }: { children: React.ReactNode }) {
  const [isOpen, setIsOpen] = useState(true);

  const toggle = () => setIsOpen((prev) => !prev);
  const open = () => setIsOpen(true);
  const close = () => setIsOpen(false);

  return (
    <SidebarContext.Provider value={{ isOpen, toggle, open, close }}>
      {children}
    </SidebarContext.Provider>
  );
}

export function useSidebar() {
  const context = useContext(SidebarContext);
  if (!context) {
    throw new Error('useSidebar must be used within SidebarProvider');
  }
  return context;
}
```

### 3. Form State (React Hook Form)

**Purpose**: Manage complex form state with validation

```typescript
// src/components/inventory/InventoryForm.tsx
import { useForm } from 'react-hook-form';
import { yupResolver } from '@hookform/resolvers/yup';
import * as yup from 'yup';

const inventorySchema = yup.object({
  productId: yup.string().required('Product is required'),
  quantity: yup.number()
    .required('Quantity is required')
    .min(0, 'Quantity must be at least 0'),
  reorderLevel: yup.number()
    .required('Reorder level is required')
    .min(0, 'Reorder level must be at least 0'),
  unitCost: yup.number()
    .required('Unit cost is required')
    .min(0, 'Unit cost must be at least 0'),
  location: yup.string().optional(),
  expiryDate: yup.date().optional(),
});

type InventoryFormData = yup.InferType<typeof inventorySchema>;

export function InventoryForm({ onSubmit }: { onSubmit: (data: InventoryFormData) => void }) {
  const {
    register,
    handleSubmit,
    formState: { errors, isSubmitting },
    reset,
  } = useForm<InventoryFormData>({
    resolver: yupResolver(inventorySchema),
    defaultValues: {
      quantity: 0,
      reorderLevel: 10,
    },
  });

  return (
    <form onSubmit={handleSubmit(onSubmit)}>
      <div>
        <label htmlFor="productId">Product</label>
        <select {...register('productId')}>
          {/* Product options */}
        </select>
        {errors.productId && <span>{errors.productId.message}</span>}
      </div>

      <div>
        <label htmlFor="quantity">Quantity</label>
        <input type="number" {...register('quantity')} />
        {errors.quantity && <span>{errors.quantity.message}</span>}
      </div>

      <div>
        <label htmlFor="reorderLevel">Reorder Level</label>
        <input type="number" {...register('reorderLevel')} />
        {errors.reorderLevel && <span>{errors.reorderLevel.message}</span>}
      </div>

      <div>
        <label htmlFor="unitCost">Unit Cost</label>
        <input type="number" step="0.01" {...register('unitCost')} />
        {errors.unitCost && <span>{errors.unitCost.message}</span>}
      </div>

      <button type="submit" disabled={isSubmitting}>
        {isSubmitting ? 'Creating...' : 'Create Inventory'}
      </button>
    </form>
  );
}
```

---

## Routing Strategy

### React Router Configuration

```typescript
// src/App.tsx
import { BrowserRouter, Routes, Route } from 'react-router-dom';
import { ProtectedRoute } from '@/components/auth/ProtectedRoute';

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
            <Route path="/" element={<Navigate to="/dashboard" replace />} />
            <Route path="/dashboard" element={<DashboardPage />} />

            {/* Shop routes */}
            <Route path="/shops" element={<ShopsPage />} />
            <Route path="/shops/create" element={<CreateShopPage />} />
            <Route path="/shops/:shopId" element={<ShopDetailPage />} />

            {/* Inventory routes */}
            <Route path="/inventory" element={<InventoryPage />} />

            {/* Sales routes */}
            <Route path="/pos" element={<POSPage />} />
            <Route path="/sales" element={<SalesPage />} />

            {/* Investment routes */}
            <Route path="/investments" element={<InvestmentsPage />} />

            {/* Analytics routes */}
            <Route path="/analytics" element={<AnalyticsPage />} />

            {/* Profile */}
            <Route path="/profile" element={<ProfilePage />} />
          </Route>
        </Route>

        {/* 404 */}
        <Route path="*" element={<NotFoundPage />} />
      </Routes>
    </BrowserRouter>
  );
}
```

### Protected Route Component

```typescript
// src/components/auth/ProtectedRoute.tsx
import { Navigate, Outlet, useLocation } from 'react-router-dom';
import { useKeycloak } from '@react-keycloak/web';

export function ProtectedRoute() {
  const { keycloak, initialized } = useKeycloak();
  const location = useLocation();

  if (!initialized) {
    return <LoadingSpinner />;
  }

  if (!keycloak.authenticated) {
    return <Navigate to="/login" state={{ from: location }} replace />;
  }

  return <Outlet />;
}
```

### Lazy Loading Routes

```typescript
// src/App.tsx
import { lazy, Suspense } from 'react';

const ShopsPage = lazy(() => import('@/pages/shops/ShopsPage'));
const InventoryPage = lazy(() => import('@/pages/inventory/InventoryPage'));
const InvestmentsPage = lazy(() => import('@/pages/investments/InvestmentsPage'));

function App() {
  return (
    <BrowserRouter>
      <Routes>
        <Route element={<ProtectedRoute />}>
          <Route element={<Layout />}>
            <Route
              path="/shops"
              element={
                <Suspense fallback={<LoadingSpinner />}>
                  <ShopsPage />
                </Suspense>
              }
            />
            {/* Other lazy-loaded routes */}
          </Route>
        </Route>
      </Routes>
    </BrowserRouter>
  );
}
```

---

## API Integration

### Axios Configuration

```typescript
// src/lib/axios.ts
import axios from 'axios';
import { toast } from 'sonner';

const api = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || 'http://localhost:8081',
  timeout: 30000,
  headers: {
    'Content-Type': 'application/json',
  },
});

// Request interceptor
api.interceptors.request.use(
  (config) => {
    // Add JWT token from Keycloak
    const token = localStorage.getItem('kc_token');
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }

    // Add tenant/shop context
    const tenantId = localStorage.getItem('tenantId');
    const shopId = localStorage.getItem('shopId');

    if (tenantId) {
      config.headers['X-Tenant-ID'] = tenantId;
    }
    if (shopId) {
      config.headers['X-Shop-ID'] = shopId;
    }

    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

// Response interceptor
api.interceptors.response.use(
  (response) => response,
  async (error) => {
    const originalRequest = error.config;

    // Handle 401 Unauthorized
    if (error.response?.status === 401 && !originalRequest._retry) {
      originalRequest._retry = true;

      try {
        // Attempt token refresh via Keycloak
        const keycloak = window.keycloak;
        const refreshed = await keycloak.updateToken(30);

        if (refreshed) {
          const token = keycloak.token;
          localStorage.setItem('kc_token', token);
          originalRequest.headers.Authorization = `Bearer ${token}`;
          return api(originalRequest);
        }
      } catch (refreshError) {
        // Refresh failed, redirect to login
        window.location.href = '/login';
        return Promise.reject(refreshError);
      }
    }

    // Handle other errors
    if (error.response?.status === 403) {
      toast.error('You do not have permission to perform this action');
    } else if (error.response?.status === 404) {
      toast.error('Resource not found');
    } else if (error.response?.status >= 500) {
      toast.error('Server error. Please try again later.');
    }

    return Promise.reject(error);
  }
);

export default api;
```

### Service Layer Pattern

```typescript
// src/services/inventoryService.ts
import api from '@/lib/axios';
import type {
  InventoryResponse,
  InventoryCreateRequest,
  InventoryAdjustmentRequest,
  PaginatedResponse
} from '@/types/api';

export const inventoryService = {
  async getInventory(
    shopId: string,
    params?: Record<string, any>
  ): Promise<PaginatedResponse<InventoryResponse>> {
    const { data } = await api.get(`/api/v1/shops/${shopId}/inventory`, { params });
    return data;
  },

  async getInventoryById(inventoryId: string): Promise<InventoryResponse> {
    const { data } = await api.get(`/api/v1/inventory/${inventoryId}`);
    return data;
  },

  async createInventory(
    shopId: string,
    request: InventoryCreateRequest
  ): Promise<InventoryResponse> {
    const { data } = await api.post(`/api/v1/shops/${shopId}/inventory`, request);
    return data;
  },

  async adjustStock(
    inventoryId: string,
    request: InventoryAdjustmentRequest
  ): Promise<InventoryResponse> {
    const { data } = await api.put(`/api/v1/inventory/${inventoryId}/adjust-stock`, request);
    return data;
  },

  async getLowStock(shopId: string): Promise<InventoryResponse[]> {
    const { data } = await api.get(`/api/v1/shops/${shopId}/inventory/low-stock`);
    return data;
  },

  async getExpiring(shopId: string, daysThreshold: number): Promise<InventoryResponse[]> {
    const { data } = await api.get(
      `/api/v1/shops/${shopId}/inventory/expiring`,
      { params: { daysThreshold } }
    );
    return data;
  },
};
```

---

## Authentication & Authorization

### Keycloak Integration

```typescript
// src/providers/KeycloakAuthProvider.tsx
import { ReactKeycloakProvider } from '@react-keycloak/web';
import Keycloak from 'keycloak-js';

const keycloak = new Keycloak({
  url: import.meta.env.VITE_KEYCLOAK_URL || 'http://localhost:8080',
  realm: 'shop-manager',
  clientId: 'shop-manager-frontend',
});

export function KeycloakAuthProvider({ children }: { children: React.ReactNode }) {
  return (
    <ReactKeycloakProvider
      authClient={keycloak}
      initOptions={{
        onLoad: 'check-sso',
        silentCheckSsoRedirectUri: window.location.origin + '/silent-check-sso.html',
        pkceMethod: 'S256',
      }}
      onTokens={(tokens) => {
        if (tokens.token) {
          localStorage.setItem('kc_token', tokens.token);
        }
      }}
    >
      {children}
    </ReactKeycloakProvider>
  );
}
```

### Role-Based Access Control

```typescript
// src/hooks/useAuth.ts
import { useKeycloak } from '@react-keycloak/web';

export function useAuth() {
  const { keycloak, initialized } = useKeycloak();

  const hasRole = (role: string): boolean => {
    return keycloak.hasRealmRole(role) || keycloak.hasResourceRole(role);
  };

  const hasAnyRole = (roles: string[]): boolean => {
    return roles.some((role) => hasRole(role));
  };

  const getUserRoles = (): string[] => {
    return keycloak.realmAccess?.roles || [];
  };

  return {
    isAuthenticated: keycloak.authenticated,
    initialized,
    user: keycloak.tokenParsed,
    hasRole,
    hasAnyRole,
    getUserRoles,
    login: () => keycloak.login(),
    logout: () => keycloak.logout(),
    token: keycloak.token,
  };
}
```

---

## Form Handling

See example in [State Management](#3-form-state-react-hook-form) section.

---

## Error Handling

### Error Boundary

```typescript
// src/components/ErrorBoundary.tsx
import { Component, ErrorInfo, ReactNode } from 'react';

interface Props {
  children: ReactNode;
  fallback?: ReactNode;
}

interface State {
  hasError: boolean;
  error?: Error;
}

export class ErrorBoundary extends Component<Props, State> {
  constructor(props: Props) {
    super(props);
    this.state = { hasError: false };
  }

  static getDerivedStateFromError(error: Error): State {
    return { hasError: true, error };
  }

  componentDidCatch(error: Error, errorInfo: ErrorInfo) {
    console.error('Error boundary caught:', error, errorInfo);
    // Log to error tracking service (e.g., Sentry)
  }

  render() {
    if (this.state.hasError) {
      return this.props.fallback || (
        <div className="error-page">
          <h1>Something went wrong</h1>
          <p>{this.state.error?.message}</p>
          <button onClick={() => window.location.reload()}>
            Reload Page
          </button>
        </div>
      );
    }

    return this.props.children;
  }
}
```

---

## Performance Optimization

### Code Splitting

- Route-based code splitting with `React.lazy()`
- Component-level code splitting for heavy components
- Dynamic imports for charts and analytics

### Memoization

```typescript
import { useMemo, useCallback } from 'react';

// Memoize expensive calculations
const totalValue = useMemo(() => {
  return inventory.reduce((sum, item) => sum + item.quantity * item.unitCost, 0);
}, [inventory]);

// Memoize callbacks
const handleClick = useCallback(() => {
  console.log('Clicked');
}, []);
```

### Virtual Scrolling

Use `react-window` for long lists (1000+ items).

---

## Build & Deployment

### Environment Variables

```bash
# .env.development
VITE_API_BASE_URL=http://localhost:8081
VITE_KEYCLOAK_URL=http://localhost:8080

# .env.production
VITE_API_BASE_URL=https://api.shopmanager.com
VITE_KEYCLOAK_URL=https://auth.shopmanager.com
```

### Build Commands

```bash
# Development
npm run dev

# Production build
npm run build

# Preview production build
npm run preview
```

### Vite Configuration

```typescript
// vite.config.ts
import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';
import path from 'path';

export default defineConfig({
  plugins: [react()],
  resolve: {
    alias: {
      '@': path.resolve(__dirname, './src'),
    },
  },
  build: {
    sourcemap: true,
    rollupOptions: {
      output: {
        manualChunks: {
          vendor: ['react', 'react-dom', 'react-router-dom'],
          charts: ['recharts'],
          ui: ['@radix-ui/react-dialog', '@radix-ui/react-dropdown-menu'],
        },
      },
    },
  },
});
```

---

**Document Version**: 1.0
**Last Updated**: January 2025
**Next Review**: As needed
