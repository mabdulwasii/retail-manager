Claude, generate a standalone Playwright E2E test module for the Shop Manager system.

### Project Setup
- Create a separate directory/module called `e2e-playwright`
- Use Playwright with TypeScript
- Maintain its own `package.json` (latest stable dependencies)
- Use Yarn as the package manager
- Ensure Playwright browsers are installed in setup

### Test Coverage
1. **Authentication**
    - Login with Keycloak
    - Logout flow
    - Handle invalid login

2. **Dashboard**
    - Load dashboard after login
    - Verify sales summary and profit distribution cards
    - Check charts render

3. **Products Module**
    - CRUD flow (create, update, activate/deactivate product)
    - Search and filter products

4. **Sales Module**
    - Register a sale
    - View sales history
    - Print/download receipt flow

5. **Investments Module**
    - View investment opportunities
    - Track profit distribution
    - Configurable view (per product / per shop)

6. **Analytics Module**
    - Sales trends chart
    - ROI reports
    - Fraud alerts display

7. **Audit Logs**
    - Navigate to logs page
    - Filter by date and user

### Cross-Cutting Features
- Test multi-tenant switcher
- Test feature toggle (hide/show module tabs)
- Test dark/light mode toggle

### CI/CD Integration
- Provide GitHub Actions workflow (`.github/workflows/e2e.yml`)
    - Install dependencies
    - Start backend + frontend via docker-compose
    - Run Playwright tests headless
    - Upload HTML report as GitHub Actions artifact

### Deployment / Scaling
- Provide Dockerfile for the Playwright module
- This module should be runnable independently in Kubernetes CI pods
- Should not be bundled with the UI frontend container

### Deliverables
- Standalone Playwright module (`e2e-playwright`)
- Configured with TypeScript + Yarn + latest Playwright
- Test scripts covering core flows
- GitHub Actions workflow for E2E pipeline
- Dockerfile to run tests headlessly in CI/CD
