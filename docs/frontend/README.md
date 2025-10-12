# Frontend Documentation

**Shop Manager - React Frontend Documentation Suite**

---

## 📚 Documentation Overview

This directory contains comprehensive frontend development documentation for the Shop Manager application. All documents are designed to help UI/UX designers and React developers understand, design, and implement the complete frontend application.

---

## 📖 Documentation Files

### 1. [USER_JOURNEY_MAP.md](./USER_JOURNEY_MAP.md) (58K)
**Complete user journey flows for all 8 roles with screen inventory**

- User journey diagrams for each role (Super Admin, Tenant Admin, Shop Owner, Shop Manager, Cashier, Shop Employee, Investor, Accountant)
- Complete screen inventory (75+ screens) organized by module
- API-to-screen mapping with request/response DTOs
- Navigation hierarchy and entry points
- Figma design structure recommendations

**When to use**: 
- Starting UI/UX design work
- Understanding user flows and requirements
- Planning screen development
- Creating Figma designs

---

### 2. [FRONTEND_ARCHITECTURE.md](./FRONTEND_ARCHITECTURE.md) (35K)
**Technical architecture and implementation patterns**

- Project structure and folder organization
- State management strategy (React Query, Context API, React Hook Form)
- Routing configuration with React Router
- API integration patterns with Axios
- Authentication & authorization with Keycloak
- Form handling with React Hook Form + Yup
- Error handling and boundary strategies
- Performance optimization techniques
- Build and deployment configuration

**When to use**:
- Setting up new React project
- Understanding state management
- Implementing API integrations
- Configuring authentication
- Performance optimization

---

### 3. [DESIGN_SYSTEM.md](./DESIGN_SYSTEM.md) (16K)
**Complete design system with tokens and components**

- Color palette (primary, secondary, semantic colors)
- Typography scale and font weights
- Spacing system (4px grid)
- Component styles (buttons, inputs, cards, badges, alerts, tables)
- Icon system (Lucide React)
- Shadows and effects
- Animations and transitions
- Responsive design breakpoints
- Dark mode implementation
- Accessibility standards

**When to use**:
- Creating design mockups
- Implementing UI components
- Ensuring design consistency
- Building reusable components
- Setting up TailwindCSS configuration

---

### 4. [ROUTING_MAP.md](./ROUTING_MAP.md) (12K)
**Complete application route structure**

- Public routes (login, register, callback)
- Protected routes by role
- Role-based access control matrix
- Route configuration examples
- Protected route implementation
- Navigation patterns (Link, Navigate, Breadcrumbs)
- Deep linking with URL parameters

**When to use**:
- Understanding application navigation
- Implementing routing
- Setting up role-based guards
- Creating navigation menus
- Implementing breadcrumbs

---

### 5. [API_INTEGRATION_GUIDE.md](./API_INTEGRATION_GUIDE.md) (7K)
**API integration patterns and endpoint reference**

- Axios configuration with interceptors
- API service layer pattern
- React Query hooks pattern
- Complete API endpoint reference (all 100+ endpoints)
- Error handling strategies
- File upload implementation
- Request cancellation and retry logic

**When to use**:
- Integrating with backend APIs
- Creating service layer files
- Implementing React Query hooks
- Handling API errors
- Uploading files

---

### 6. [COMPONENT_LIBRARY.md](./COMPONENT_LIBRARY.md) (7K)
**Reusable component catalog with usage examples**

- Base UI components (shadcn/ui)
  - Form components (Button, Input, Select)
  - Data display (Card, Badge, Avatar)
  - Feedback (Alert, Dialog)
  - Navigation (Tabs, Dropdown)
- Domain-specific components
  - Dashboard components
  - Inventory components
  - Investment components
  - Sales components
  - Chart components
- Layout components (Layout, Navbar, Sidebar)
- Utility components (LoadingSpinner, CurrencySelector)

**When to use**:
- Building new features
- Understanding existing components
- Creating consistent UI
- Implementing forms and tables
- Adding charts and visualizations

---

### 7. [BUSINESS_RULES.md](./BUSINESS_RULES.md) (6K)
**Validation rules, business logic, and workflows**

- Validation schemas (Yup) for all forms
  - Inventory validation
  - Investment validation
  - Expense validation
  - Shop validation
- Business logic rules
  - Inventory management (reorder alerts, expiry management)
  - Investment constraints and profit distribution
  - Expense approval workflows
  - Product return policies
  - Fraud detection triggers
- Workflow state machines
- Calculation formulas (ROI, profit margin, variance)

**When to use**:
- Implementing form validation
- Understanding business constraints
- Implementing approval workflows
- Calculating financial metrics
- Configuring fraud detection

---

### 8. [ACCESSIBILITY_GUIDELINES.md](./ACCESSIBILITY_GUIDELINES.md) (7K)
**WCAG 2.1 AA compliance guidelines**

- Color and contrast requirements
- Keyboard navigation patterns
- Semantic HTML usage
- ARIA attributes (labels, live regions, states)
- Form accessibility
- Images and icons (alt text)
- Tables and data grids
- Modals and dialogs
- Testing checklist (automated and manual)
- Common accessible patterns
- Screen reader testing guide

**When to use**:
- Implementing accessible components
- Testing for accessibility
- Ensuring WCAG compliance
- Adding keyboard navigation
- Writing ARIA labels

---

## 🎯 Quick Start Guide

### For UI/UX Designers

1. **Start with**: [USER_JOURNEY_MAP.md](./USER_JOURNEY_MAP.md)
   - Understand all user roles and their journeys
   - Review screen inventory
   - Map screens to user flows

2. **Then review**: [DESIGN_SYSTEM.md](./DESIGN_SYSTEM.md)
   - Learn color palette and typography
   - Understand component styles
   - Review spacing and layout guidelines

3. **Create**: Figma designs following the structure in USER_JOURNEY_MAP.md

### For React Developers

1. **Start with**: [FRONTEND_ARCHITECTURE.md](./FRONTEND_ARCHITECTURE.md)
   - Understand project structure
   - Learn state management approach
   - Review routing strategy

2. **Then review**:
   - [API_INTEGRATION_GUIDE.md](./API_INTEGRATION_GUIDE.md) - For backend integration
   - [COMPONENT_LIBRARY.md](./COMPONENT_LIBRARY.md) - For reusable components
   - [BUSINESS_RULES.md](./BUSINESS_RULES.md) - For validation and logic

3. **Always follow**: [ACCESSIBILITY_GUIDELINES.md](./ACCESSIBILITY_GUIDELINES.md) when implementing features

### For Product Managers

1. **Start with**: [USER_JOURNEY_MAP.md](./USER_JOURNEY_MAP.md)
   - Understand all user roles
   - Review feature completeness
   - Identify gaps in functionality

2. **Then review**: [BUSINESS_RULES.md](./BUSINESS_RULES.md)
   - Verify business logic alignment
   - Confirm validation rules
   - Review approval workflows

---

## 🔗 Related Documentation

- [../../CLAUDE.md](../../CLAUDE.md) - AI assistant instructions for Shop Manager project
- [../../README.md](../../README.md) - Project overview and setup
- [../../DEVELOPER_GUIDE.md](../../DEVELOPER_GUIDE.md) - Local development setup
- [../../TESTING-GUIDE.md](../../TESTING-GUIDE.md) - Testing strategy and credentials

---

## 📊 Documentation Statistics

- **Total Files**: 8 comprehensive documents
- **Total Size**: ~147K
- **Total Lines**: ~10,000+ lines of documentation
- **Coverage**: 
  - 75+ screen specifications
  - 100+ API endpoints documented
  - 8 user role journeys
  - Complete component library
  - Full validation schemas
  - WCAG 2.1 AA compliance guide

---

## 🤝 Contributing

When updating these documents:

1. Maintain consistent formatting
2. Update version numbers and dates
3. Keep cross-references accurate
4. Add examples for new patterns
5. Update this README if adding new documents

---

## 📧 Questions?

For questions about:
- **Design**: Refer to DESIGN_SYSTEM.md and USER_JOURNEY_MAP.md
- **Implementation**: Refer to FRONTEND_ARCHITECTURE.md and COMPONENT_LIBRARY.md
- **APIs**: Refer to API_INTEGRATION_GUIDE.md
- **Business Logic**: Refer to BUSINESS_RULES.md
- **Accessibility**: Refer to ACCESSIBILITY_GUIDELINES.md

---

**Documentation Version**: 1.0  
**Last Updated**: January 2025  
**Maintained By**: Product, Design & Engineering Teams
