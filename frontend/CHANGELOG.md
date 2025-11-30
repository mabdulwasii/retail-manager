# Changelog

All notable changes to the Shop Manager Frontend will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [0.0.17] - 2024-11-30

### Added
- Permission-based route guards for granular access control
- ShopContext for centralized shop selection state management
- Real data visualizations to AdminDashboard (Revenue Trend, Inventory Status)
- PDF receipt generation and download functionality

### Changed
- Migrated from role-based to permission-based authorization system
- Replaced UserRole enum with Permission enum across all route guards
- Standardized dashboard Quick Action buttons (consistent heights, icons, layouts)
- Enhanced dashboard UI consistency across all user roles
- Fixed broken navigation routes to point to existing pages
- Improved sales page transaction handling and data display


## [0.0.16] - 2024-11-28

### Added
- Shop selector component with permission-based filtering
- Improved inventory form with better UX and validation
- Product form enhancements with react-number-format integration
- Payment modal improvements for better transaction handling
- Shopping cart UI/UX updates

### Changed
- Enhanced form validation across inventory and product management
- Improved numeric input handling with formatted number fields

## [1.0.0] - 2024-01-15

### Added
- Initial release of Shop Manager Frontend
- React 18 with TypeScript support
- Vite build system for optimal performance
- TailwindCSS with shadcn/ui component library
- Keycloak integration for SSO authentication
- Role-based access control (RBAC)
- Multi-tenant shop management interface
- Dashboard with analytics overview
- Shop management (CRUD operations)
- Product catalog management
- Inventory tracking interface
- Sales transaction management
- Receipt generation and management
- Investment portfolio tracking
- Business analytics dashboard
- Audit logs viewer
- Responsive design for mobile/desktop
- Dark/light theme support
- Docker containerization
- Jest + React Testing Library setup
- MSW for API mocking in tests
- Nginx configuration for production
- Helm chart ready deployment

### Tech Stack
- React 18.2+ with TypeScript
- Vite 4.5+ for build system
- TailwindCSS for styling
- shadcn/ui for UI components
- Keycloak JS for authentication
- React Query for state management
- React Router v6 for routing
- Recharts for data visualization
- Axios for API communication

### Security
- Content Security Policy headers
- XSS protection
- CSRF protection
- Secure authentication with JWT tokens
- Role-based route protection

### Performance
- Code splitting and lazy loading
- Optimized bundle size
- Gzip compression
- Static asset caching
- Tree shaking

### Development
- ESLint + TypeScript configuration
- Jest testing framework
- MSW for API mocking
- Hot module replacement
- Development proxy for backend API