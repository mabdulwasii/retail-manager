Claude, generate a React frontend for the Shop Manager system with the following requirements:

### Tech Stack
- React 18 with TypeScript
- Vite (preferred over CRA for performance)
- Yarn as the package manager (latest stable version)
- TailwindCSS for styling
- shadcn/ui for UI components
- Recharts for data visualization
- Axios for API calls
- React Router v6 for routing
- Keycloak JS adapter for authentication (OIDC)
- State management: React Query (preferred) or Redux Toolkit

### Authentication & Tenant Management
- Integrate with Keycloak for login/logout
- Store tokens securely in memory or session
- Implement Role-Based UI:
    - Admins: manage tenants, users, permissions
    - Managers: manage products, sales, investments
    - Investors: view profit distributions, portfolio
- Support Attribute-Based Access Control (ABAC) from API (disable/enable buttons, features)
- Tenant switcher dropdown (for users managing multiple shops)

### UI Modules
1. **Dashboard**
    - Show sales summary, profit distribution summary, fraud alerts
    - Cards + charts (Recharts for trends)

2. **Products Module**
    - List products with CRUD (create, update, activate/deactivate)
    - Stock management
    - Table with filters and search

3. **Sales Module**
    - Register sales transactions
    - Show transaction history with filters
    - Print/download receipts

4. **Investments Module**
    - View available investment opportunities
    - Track personal investments
    - Show profit distributions (tables + charts)
    - Configurable views (per product or per shop)

5. **Analytics Module**
    - Sales by product/category
    - Investor ROI reports
    - Fraud detection reports
    - Export to CSV/JSON

6. **Audit & Logs**
    - View activity logs (security events, entity changes)
    - Filters by date/user/module

### Cross-Cutting Features
- Dark/light mode toggle
- Multi-language support (i18n skeleton with English default)
- Responsive design (mobile-friendly)
- Changelog.md file at root (auto-updated via commit hooks or manually maintained)

### Integration
- Axios interceptors for attaching Keycloak tokens
- Error boundary for handling API errors
- Toast notifications (e.g., sonner or shadcn/ui toast)

### Testing
- Unit & Integration:
    - React Testing Library + Jest
    - MSW (Mock Service Worker) for mocking API responses
- ❌ Do not include Playwright here (Playwright is Phase 4 in a separate module)

### Deployment
- Provide Dockerfile for React app
- Provide Helm chart entry to deploy React frontend as a service
- Connect to backend via API gateway (Ingress)
- Configure CORS + Keycloak redirect URIs

### Deliverables
- React project scaffold with modules, routing, and Keycloak integration
- Pages for dashboard, products, sales, investments, analytics, audit logs
- Components using Tailwind + shadcn/ui
- Charts with Recharts
- Configurable feature toggles for modules (hide/show tabs)
- Testing setup (Jest + RTL + MSW only)
- Yarn.lock and latest stable dependencies
- Changelog.md initialized
- Dockerfile + Helm chart for deployment
