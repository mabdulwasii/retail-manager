/**
 * Unit tests for App component
 * Tests routing configuration and embedded mode login route
 */

import { describe, it, expect, beforeEach, jest } from '@jest/globals';
import { render, screen } from '@testing-library/react';
import { MemoryRouter } from 'react-router-dom';
import App from '../App';
import configService from '../config/runtime-config';

// Create a mock config service inside the jest.mock callback to avoid hoisting issues
jest.mock('../config/runtime-config', () => ({
  __esModule: true,
  default: {
    get isEmbeddedMode() { return this._isEmbeddedMode || false; },
    set isEmbeddedMode(value) { this._isEmbeddedMode = value; },
    _isEmbeddedMode: false,
    apiBaseUrl: 'http://localhost:8081/api',
    keycloakUrl: 'http://localhost:8080',
    keycloakRealm: 'shop-manager',
    keycloakClientId: 'shop-manager-frontend',
    appVersion: '1.0.0',
    appEnv: 'test',
    authMode: 'keycloak' as const,
    keycloakConfig: {
      url: 'http://localhost:8080',
      realm: 'shop-manager',
      clientId: 'shop-manager-frontend',
    },
    logConfig: jest.fn(),
  },
}));

// Mock all the page components
jest.mock('../pages/LandingPage', () => ({
  LandingPage: () => <div data-testid="landing-page">Landing Page</div>,
}));

jest.mock('../pages/auth/RegisterPage', () => ({
  RegisterPage: () => <div data-testid="register-page">Register Page</div>,
}));

jest.mock('../pages/auth/EmbeddedLoginPage', () => ({
  EmbeddedLoginPage: () => <div data-testid="embedded-login-page">Embedded Login Page</div>,
}));

jest.mock('../components/AuthenticatedApp', () => ({
  AuthenticatedApp: () => <div data-testid="authenticated-app">Authenticated App</div>,
}));

jest.mock('../components/auth/DashboardRedirect', () => ({
  DashboardRedirect: () => <div data-testid="dashboard-redirect">Dashboard Redirect</div>,
}));

interface MockProviderProps {
  children: React.ReactNode;
}

// Mock the auth contexts
jest.mock('../context/UnifiedAuthContext', () => ({
  UnifiedAuthProvider: ({ children }: MockProviderProps) => <div data-testid="unified-auth-provider">{children}</div>,
  useAuth: () => ({
    isAuthenticated: false,
    isInitialized: true,
    user: null,
    login: jest.fn(),
    logout: jest.fn(),
  }),
}));

jest.mock('../context/ShopContext', () => ({
  ShopProvider: ({ children }: MockProviderProps) => <div data-testid="shop-provider">{children}</div>,
}));

describe('App', () => {
  beforeEach(() => {
    jest.clearAllMocks();
  });

  describe('Routing Configuration', () => {
    it('should render landing page at root path', () => {
      render(
        <MemoryRouter initialEntries={['/']}>
          <App />
        </MemoryRouter>
      );

      expect(screen.getByTestId('landing-page')).toBeInTheDocument();
    });

    it('should render register page at /register', () => {
      render(
        <MemoryRouter initialEntries={['/register']}>
          <App />
        </MemoryRouter>
      );

      expect(screen.getByTestId('register-page')).toBeInTheDocument();
    });

    it('should render dashboard redirect at /redirect', () => {
      render(
        <MemoryRouter initialEntries={['/redirect']}>
          <App />
        </MemoryRouter>
      );

      expect(screen.getByTestId('dashboard-redirect')).toBeInTheDocument();
    });

    it('should render authenticated app for protected routes', () => {
      render(
        <MemoryRouter initialEntries={['/dashboard']}>
          <App />
        </MemoryRouter>
      );

      expect(screen.getByTestId('authenticated-app')).toBeInTheDocument();
    });
  });

  describe('Embedded Mode Routing', () => {
    it('should render embedded login page at /login when in embedded mode', () => {
      (configService as unknown as { isEmbeddedMode: boolean }).isEmbeddedMode = true;

      render(
        <MemoryRouter initialEntries={['/login']}>
          <App />
        </MemoryRouter>
      );

      expect(screen.getByTestId('embedded-login-page')).toBeInTheDocument();
    });

    it('should NOT render embedded login page when not in embedded mode', () => {
      (configService as unknown as { isEmbeddedMode: boolean }).isEmbeddedMode = false;

      render(
        <MemoryRouter initialEntries={['/login']}>
          <App />
        </MemoryRouter>
      );

      // Should fall through to authenticated app wildcard route
      expect(screen.queryByTestId('embedded-login-page')).not.toBeInTheDocument();
      expect(screen.getByTestId('authenticated-app')).toBeInTheDocument();
    });

    it('should check configService.isEmbeddedMode during render', () => {
      // Spy on the getter
      const getSpy = jest.spyOn(configService as unknown as Record<string, unknown>, 'isEmbeddedMode', 'get');

      render(
        <MemoryRouter initialEntries={['/']}>
          <App />
        </MemoryRouter>
      );

      expect(getSpy).toHaveBeenCalled();

      getSpy.mockRestore();
    });
  });

  describe('Provider Hierarchy', () => {
    it('should wrap app in UnifiedAuthProvider', () => {
      render(
        <MemoryRouter initialEntries={['/']}>
          <App />
        </MemoryRouter>
      );

      expect(screen.getByTestId('unified-auth-provider')).toBeInTheDocument();
    });

    it('should wrap app in ShopProvider', () => {
      render(
        <MemoryRouter initialEntries={['/']}>
          <App />
        </MemoryRouter>
      );

      expect(screen.getByTestId('shop-provider')).toBeInTheDocument();
    });

    it('should nest ShopProvider inside UnifiedAuthProvider', () => {
      render(
        <MemoryRouter initialEntries={['/']}>
          <App />
        </MemoryRouter>
      );

      const authProvider = screen.getByTestId('unified-auth-provider');
      const shopProvider = screen.getByTestId('shop-provider');

      expect(authProvider).toContainElement(shopProvider);
    });
  });
});
