/**
 * Test Utilities
 * Custom render functions and test helpers
 * Based on Testing Library best practices
 */

import React, { ReactElement } from 'react'
import { render, RenderOptions, renderHook, RenderHookOptions } from '@testing-library/react'
import { MemoryRouter } from 'react-router-dom'
import { QueryClient, QueryClientProvider } from '@tanstack/react-query'

interface WrapperProps {
  children: React.ReactNode
}

/**
 * Create a new QueryClient for testing
 */
export const createTestQueryClient = () =>
  new QueryClient({
    defaultOptions: {
      queries: {
        retry: false,
        gcTime: 0, // Immediately garbage collect
        staleTime: 0,
      },
      mutations: {
        retry: false,
      },
    },
    logger: {
      log: () => {},
      warn: () => {},
      error: () => {},
    },
  })

/**
 * Custom render function that wraps components with MemoryRouter
 */
export const renderWithRouter = (
  ui: ReactElement,
  options?: Omit<RenderOptions, 'wrapper'>
) => {
  const Wrapper = ({ children }: WrapperProps) => (
    <MemoryRouter>
      {children}
    </MemoryRouter>
  )

  return render(ui, { wrapper: Wrapper, ...options })
}

/**
 * Custom render function that wraps components with QueryClient and MemoryRouter
 */
export const renderWithProviders = (
  ui: ReactElement,
  options?: Omit<RenderOptions, 'wrapper'>
) => {
  const queryClient = createTestQueryClient()
  
  const Wrapper = ({ children }: WrapperProps) => (
    <QueryClientProvider client={queryClient}>
      <MemoryRouter>
        {children}
      </MemoryRouter>
    </QueryClientProvider>
  )

  return render(ui, { wrapper: Wrapper, ...options })
}

/**
 * Helper to create mock auth return value with role-based permissions
 */
export const createMockAuth = (user: any, isAuthenticated: boolean = true) => {
  // Map roles to their permissions
  const rolePermissions: Record<string, string[]> = {
    'ROLE_SYSTEM_ADMIN': ['SYSTEM_ADMIN', 'TENANT_MANAGE', 'TENANT_LIST'],
    'ROLE_SUPER_ADMIN': ['SYSTEM_ADMIN', 'TENANT_MANAGE', 'TENANT_LIST'],
    'ROLE_SHOP_OWNER': ['SHOP_MANAGE', 'SHOP_LIST', 'ANALYTICS_VIEW'],
    'ROLE_MANAGER': ['SHOP_MANAGE', 'SHOP_LIST', 'ANALYTICS_VIEW'],
    'ROLE_SALES_MANAGER': ['SHOP_MANAGE', 'SHOP_LIST', 'ANALYTICS_VIEW'],
    'ROLE_ACCOUNTANT': ['ANALYTICS_SALES_VIEW', 'ANALYTICS_INVESTMENT_VIEW', 'EXPENSE_SUMMARY'],
    'ROLE_AUDITOR': ['AUDIT_LOG_VIEW', 'AUDIT_LOG_VIEW_TENANT', 'AUDIT_LOG_VIEW_SHOP'],
    'ROLE_INVESTOR': ['INVESTMENT_VIEW', 'INVESTMENT_LIST'],
    'ROLE_INVENTORY_MANAGER': ['INVENTORY_LIST', 'INVENTORY_UPDATE', 'PRODUCT_LIST'],
    'ROLE_EMPLOYEE': ['INVENTORY_LIST', 'PRODUCT_LIST'],
    'ROLE_CASHIER': ['SALES_CREATE', 'RECEIPT_CREATE'],
    'ROLE_CUSTOMER': [],
  }

  // Get user permissions based on their roles
  const userPermissions = new Set<string>()
  user?.roles?.forEach((role: string) => {
    const permissions = rolePermissions[role] || []
    permissions.forEach(p => userPermissions.add(p))
  })

  // Mock hasAnyPermission
  const hasAnyPermission = jest.fn((requestedPerms: any[]) => {
    return requestedPerms.some(perm => {
      const permString = typeof perm === 'string' ? perm : perm.toString()
      return userPermissions.has(permString)
    })
  })

  // Mock hasPermission (singular)
  const hasPermission = jest.fn((perm: any) => {
    const permString = typeof perm === 'string' ? perm : perm.toString()
    return userPermissions.has(permString)
  })

  return {
    user,
    login: jest.fn(),
    logout: jest.fn(),
    isLoading: false,
    isAuthenticated,
    hasAnyPermission,
    hasPermission,
  } as any
}

/**
 * Wrapper for renderHook that includes QueryClient
 */
export const createQueryWrapper = () => {
  const queryClient = createTestQueryClient()
  
  return ({ children }: WrapperProps) => (
    <QueryClientProvider client={queryClient}>
      {children}
    </QueryClientProvider>
  )
}

/**
 * Custom renderHook function that wraps hooks with QueryClient and MemoryRouter
 * Similar to renderWithProviders but for hooks
 */
export const renderHookWithProviders = <TProps, TResult>(
  hook: (props: TProps) => TResult,
  options?: Omit<RenderHookOptions<TProps>, 'wrapper'>
) => {
  const queryClient = createTestQueryClient()
  
  const Wrapper = ({ children }: WrapperProps) => (
    <QueryClientProvider client={queryClient}>
      <MemoryRouter>
        {children}
      </MemoryRouter>
    </QueryClientProvider>
  )

  return renderHook(hook, { wrapper: Wrapper, ...options })
}

/**
 * Wait for async operations with a timeout
 * Useful for waiting for queries to complete
 */
export const waitForMs = (ms: number) => 
  new Promise(resolve => setTimeout(resolve, ms))

// Re-export everything from testing-library
export * from '@testing-library/react'
