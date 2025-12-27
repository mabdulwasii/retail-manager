/**
 * Test Setup File
 * Configures the test environment with necessary mocks and polyfills
 * Based on best practices from production React applications with MSW v2
 */

import '@testing-library/jest-dom'
import { cleanup } from '@testing-library/react'

// Mock the runtime-config module to avoid import.meta issues
jest.mock('@/config/runtime-config', () => {
  const mockConfigService = {
    apiBaseUrl: 'http://localhost:8081/api',
    keycloakUrl: 'http://localhost:8080',
    keycloakRealm: 'shop-manager',
    keycloakClientId: 'shop-manager-frontend',
    appVersion: '1.0.0',
    appEnv: 'test',
    authMode: 'embedded' as const,
    isEmbeddedMode: true,
    keycloakConfig: {
      url: 'http://localhost:8080',
      realm: 'shop-manager',
      clientId: 'shop-manager-frontend'
    },
    logConfig: () => {}  // Plain function instead of jest.fn() for hoisting compatibility
  };

  return {
    default: mockConfigService,
    configService: mockConfigService
  };
})

// Global mock for ManualAuthContext - can be overridden in individual tests
jest.mock('@/context/ManualAuthContext', () => {
  const actual = jest.requireActual('@/context/ManualAuthContext')
  return {
    ...actual,
    useAuth: jest.fn(() => ({
      user: null,
      login: jest.fn(),
      logout: jest.fn(),
      isLoading: false,
      isAuthenticated: false,
      hasAnyPermission: jest.fn(() => false),
      hasPermission: jest.fn(() => false),
    })),
    ManualAuthProvider: ({ children }: any) => children,
  }
})

// Global mock for usePermissions hook - can be overridden in individual tests
jest.mock('@/hooks/usePermissions', () => ({
  usePermissions: jest.fn(() => ({
    hasPermission: jest.fn(() => true),
    hasAnyPermission: jest.fn(() => true),
    hasAllPermissions: jest.fn(() => true),
    
    // Specific permission checks - all return true by default
    canCreateShop: jest.fn(() => true),
    canViewShops: jest.fn(() => true),
    canEditShop: jest.fn(() => true),
    canDeleteShop: jest.fn(() => true),
    
    canCreateProduct: jest.fn(() => true),
    canViewProducts: jest.fn(() => true),
    canEditProduct: jest.fn(() => true),
    canDeleteProduct: jest.fn(() => true),
    
    canCreateCategory: jest.fn(() => true),
    canViewCategories: jest.fn(() => true),
    canEditCategory: jest.fn(() => true),
    canDeleteCategory: jest.fn(() => true),
    
    canCreateSale: jest.fn(() => true),
    canViewSales: jest.fn(() => true),
    canEditSale: jest.fn(() => true),
    canDeleteSale: jest.fn(() => true),
    
    canViewInventory: jest.fn(() => true),
    canUpdateInventory: jest.fn(() => true),
    
    canCreateInvestment: jest.fn(() => true),
    canViewInvestments: jest.fn(() => true),
    canEditInvestment: jest.fn(() => true),
    canDeleteInvestment: jest.fn(() => true),
    
    canViewReceipts: jest.fn(() => true),
    canCreateReceipt: jest.fn(() => true),
    
    canViewExpenses: jest.fn(() => true),
    canApproveExpenses: jest.fn(() => true),
    
    canViewAnalytics: jest.fn(() => true),
    canViewFraudDetection: jest.fn(() => true),
    
    canManageUsers: jest.fn(() => true),
    canViewUsers: jest.fn(() => true),
    
    canViewRoles: jest.fn(() => true),
    canManageRoles: jest.fn(() => true),
    
    canViewAuditLogs: jest.fn(() => true),
  })),
}))

// Mock import.meta for Vite compatibility (fallback)
globalThis.import = {
  meta: {
    env: {
      VITE_API_BASE_URL: 'http://localhost:8081/api',
      VITE_KEYCLOAK_URL: 'http://localhost:8080',
      VITE_KEYCLOAK_REALM: 'shop-manager',
      VITE_KEYCLOAK_CLIENT_ID: 'shop-manager-frontend',
      VITE_APP_VERSION: '1.0.0',
      VITE_APP_ENV: 'test',
      VITE_AUTH_MODE: 'embedded'
    }
  }
} as any

// Add TextEncoder and TextDecoder for MSW compatibility in Node.js environment
import { TextEncoder, TextDecoder } from 'util'
global.TextEncoder = TextEncoder as any
global.TextDecoder = TextDecoder as any

// Add ReadableStream for undici (fetch API dependency)
import { ReadableStream, TransformStream, WritableStream } from 'stream/web'
global.ReadableStream = ReadableStream as any
global.TransformStream = TransformStream as any
global.WritableStream = WritableStream as any

// Add MessagePort and MessageChannel for undici
import { MessageChannel, MessagePort } from 'worker_threads'
global.MessageChannel = MessageChannel as any
global.MessagePort = MessagePort as any

// Mock BroadcastChannel for MSW WebSocket support
global.BroadcastChannel = class BroadcastChannel {
  constructor(public name: string) {}
  postMessage() {}
  close() {}
  addEventListener() {}
  removeEventListener() {}
  dispatchEvent() { return true }
} as any

// Add fetch API globals for MSW v2 in Node.js environment
import { fetch, Headers, Request, Response } from 'undici'
global.fetch = fetch as any
global.Headers = Headers as any
global.Request = Request as any
global.Response = Response as any

// Setup MSW (Mock Service Worker) for API mocking - MUST come after fetch globals
import './mocks/server'

// Mock IntersectionObserver
global.IntersectionObserver = class IntersectionObserver {
  constructor() {}
  disconnect() {}
  observe() {}
  unobserve() {}
}

// Mock ResizeObserver
global.ResizeObserver = class ResizeObserver {
  constructor() {}
  disconnect() {}
  observe() {}
  unobserve() {}
}

// Mock window.matchMedia
Object.defineProperty(window, 'matchMedia', {
  writable: true,
  value: jest.fn().mockImplementation(query => ({
    matches: false,
    media: query,
    onchange: null,
    addListener: jest.fn(), // deprecated
    removeListener: jest.fn(), // deprecated
    addEventListener: jest.fn(),
    removeEventListener: jest.fn(),
    dispatchEvent: jest.fn(),
  })),
})

// Mock window.scrollTo
Object.defineProperty(window, 'scrollTo', {
  writable: true,
  value: jest.fn(),
})

// Mock localStorage with proper implementation
const localStorageMock = (() => {
  let store: Record<string, string> = {};

  return {
    getItem: jest.fn((key: string) => store[key] || null),
    setItem: jest.fn((key: string, value: string) => {
      store[key] = value.toString();
    }),
    removeItem: jest.fn((key: string) => {
      delete store[key];
    }),
    clear: jest.fn(() => {
      store = {};
    }),
  };
})();

Object.defineProperty(window, 'localStorage', {
  value: localStorageMock,
})

// Mock sessionStorage with proper implementation
const sessionStorageMock = (() => {
  let store: Record<string, string> = {};

  return {
    getItem: jest.fn((key: string) => store[key] || null),
    setItem: jest.fn((key: string, value: string) => {
      store[key] = value.toString();
    }),
    removeItem: jest.fn((key: string) => {
      delete store[key];
    }),
    clear: jest.fn(() => {
      store = {};
    }),
  };
})();

Object.defineProperty(window, 'sessionStorage', {
  value: sessionStorageMock,
})

// Note: Do NOT mock fetch here! 
// MSW v2 requires the real fetch (undici) to intercept requests properly
// The fetch global is already set up on line 150 via undici

// Mock console methods to reduce noise in tests
global.console = {
  ...console,
  log: jest.fn(),
  debug: jest.fn(),
  info: jest.fn(),
  warn: jest.fn(),
  error: jest.fn(),
}

// Suppress act warnings for async operations
const originalError = console.error
beforeAll(() => {
  console.error = (...args: any[]) => {
    if (
      typeof args[0] === 'string' &&
      args[0].includes('Warning: An invalid form control with name=')
    ) {
      return
    }
    originalError.call(console, ...args)
  }
})

afterAll(() => {
  console.error = originalError
})

// Clean up after each test to prevent memory leaks
afterEach(() => {
  // Clean up React components
  cleanup()
  
  // Clear all mocks to prevent test pollution
  jest.clearAllMocks()
  
  // Reset storage mocks
  localStorageMock.clear()
  sessionStorageMock.clear()
  
  // MSW handlers are reset in server.ts afterEach hook
})