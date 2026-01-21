import React from 'react'
import { render, screen } from '@testing-library/react'
import { MemoryRouter } from 'react-router-dom'
import { RoleBasedDashboard } from '../RoleBasedDashboard'
import { useAuth } from '@/context/UnifiedAuthContext'

// Mock all dashboard components
jest.mock('../AdminDashboard', () => ({
  AdminDashboard: () => <div data-testid="admin-dashboard">Admin Dashboard</div>
}))

jest.mock('../OwnerManagerDashboard', () => ({
  OwnerManagerDashboard: () => <div data-testid="owner-manager-dashboard">Owner Manager Dashboard</div>
}))

jest.mock('../CashierDashboard', () => ({
  CashierDashboard: () => <div data-testid="cashier-dashboard">Cashier Dashboard</div>
}))

jest.mock('../InvestorDashboard', () => ({
  InvestorDashboard: () => <div data-testid="investor-dashboard">Investor Dashboard</div>
}))

jest.mock('../EmployeeDashboard', () => ({
  EmployeeDashboard: () => <div data-testid="employee-dashboard">Employee Dashboard</div>
}))

jest.mock('../AccountantDashboard', () => ({
  AccountantDashboard: () => <div data-testid="accountant-dashboard">Accountant Dashboard</div>
}))

jest.mock('../AuditorDashboard', () => ({
  AuditorDashboard: () => <div data-testid="auditor-dashboard">Auditor Dashboard</div>
}))

jest.mock('../CustomerDashboard', () => ({
  CustomerDashboard: () => <div data-testid="customer-dashboard">Customer Dashboard</div>
}))

// Mock the useAuth hook
jest.mock('@/context/UnifiedAuthContext', () => ({
  useAuth: jest.fn()
}))

const mockUseAuth = useAuth as jest.MockedFunction<typeof useAuth>

// Helper to create mock auth return value
const createMockAuth = (user: any, isAuthenticated: boolean = true) => {
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
    'SHOP_OWNER': ['SHOP_MANAGE', 'SHOP_LIST', 'ANALYTICS_VIEW'], // Without ROLE_ prefix
  }

  // Get user permissions based on their roles
  const userPermissions = new Set<string>()
  user?.roles?.forEach((role: string) => {
    const permissions = rolePermissions[role] || []
    permissions.forEach(p => userPermissions.add(p))
  })

  // Mock hasAnyPermission to check if user has any of the requested permissions
  const hasAnyPermission = jest.fn((requestedPerms: any[]) => {
    return requestedPerms.some(perm => {
      const permString = typeof perm === 'string' ? perm : perm.toString()
      return userPermissions.has(permString)
    })
  })

  return {
    user,
    login: jest.fn(),
    logout: jest.fn(),
    isLoading: false,
    isAuthenticated,
    hasAnyPermission,
  } as any
}

// Wrapper component to provide Router context
const RoleBasedDashboardWrapper: React.FC = () => (
  <MemoryRouter>
    <RoleBasedDashboard />
  </MemoryRouter>
)

describe('RoleBasedDashboard', () => {
  beforeEach(() => {
    jest.clearAllMocks()
  })

  it('should show not authenticated message when user is null', () => {
    mockUseAuth.mockReturnValue(createMockAuth(null, false))

    render(<RoleBasedDashboardWrapper />)
    expect(screen.getByText('Not authenticated')).toBeInTheDocument()
  })

  it('should show CustomerDashboard as fallback when user has no roles', () => {
    mockUseAuth.mockReturnValue(createMockAuth(
      { id: '1', username: 'test', email: 'test@example.com', roles: [] },
      true
    ))

    render(<RoleBasedDashboardWrapper />)
    expect(screen.getByTestId('customer-dashboard')).toBeInTheDocument()
  })

  it('should render AdminDashboard for SYSTEM_ADMIN role', () => {
    mockUseAuth.mockReturnValue(createMockAuth({
      id: '1',
      username: 'admin',
      email: 'admin@example.com',
      roles: ['ROLE_SYSTEM_ADMIN', 'ROLE_SHOP_OWNER']
    }))

    render(<RoleBasedDashboardWrapper />)
    expect(screen.getByTestId('admin-dashboard')).toBeInTheDocument()
  })

  it('should render AdminDashboard for SUPER_ADMIN role', () => {
    mockUseAuth.mockReturnValue(createMockAuth({
      id: '1',
      username: 'superadmin',
      email: 'superadmin@example.com',
      roles: ['ROLE_SUPER_ADMIN']
    }))

    render(<RoleBasedDashboardWrapper />)
    expect(screen.getByTestId('admin-dashboard')).toBeInTheDocument()
  })

  it('should render OwnerManagerDashboard for SHOP_OWNER role', () => {
    mockUseAuth.mockReturnValue(createMockAuth({
      id: '1',
      username: 'owner',
      email: 'owner@example.com',
      roles: ['ROLE_SHOP_OWNER']
    }))

    render(<RoleBasedDashboardWrapper />)
    expect(screen.getByTestId('owner-manager-dashboard')).toBeInTheDocument()
  })

  it('should render OwnerManagerDashboard for MANAGER role', () => {
    mockUseAuth.mockReturnValue(createMockAuth({
      id: '1',
      username: 'manager',
      email: 'manager@example.com',
      roles: ['ROLE_MANAGER']
    }))

    render(<RoleBasedDashboardWrapper />)
    expect(screen.getByTestId('owner-manager-dashboard')).toBeInTheDocument()
  })

  it('should render OwnerManagerDashboard for SALES_MANAGER role', () => {
    mockUseAuth.mockReturnValue(createMockAuth({
      id: '1',
      username: 'salesmanager',
      email: 'salesmanager@example.com',
      roles: ['ROLE_SALES_MANAGER']
    }))

    render(<RoleBasedDashboardWrapper />)
    expect(screen.getByTestId('owner-manager-dashboard')).toBeInTheDocument()
  })

  it('should render CashierDashboard for CASHIER role', () => {
    mockUseAuth.mockReturnValue(createMockAuth({
      id: '1',
      username: 'cashier',
      email: 'cashier@example.com',
      roles: ['ROLE_CASHIER']
    }))

    render(<RoleBasedDashboardWrapper />)
    expect(screen.getByTestId('cashier-dashboard')).toBeInTheDocument()
  })

  it('should render InvestorDashboard for INVESTOR role', () => {
    mockUseAuth.mockReturnValue(createMockAuth({
      id: '1',
      username: 'investor',
      email: 'investor@example.com',
      roles: ['ROLE_INVESTOR']
    }))

    render(<RoleBasedDashboardWrapper />)
    expect(screen.getByTestId('investor-dashboard')).toBeInTheDocument()
  })

  it('should render AccountantDashboard for ACCOUNTANT role', () => {
    mockUseAuth.mockReturnValue(createMockAuth({
      id: '1',
      username: 'accountant',
      email: 'accountant@example.com',
      roles: ['ROLE_ACCOUNTANT']
    }))

    render(<RoleBasedDashboardWrapper />)
    expect(screen.getByTestId('accountant-dashboard')).toBeInTheDocument()
  })

  it('should render AuditorDashboard for AUDITOR role', () => {
    mockUseAuth.mockReturnValue(createMockAuth({
      id: '1',
      username: 'auditor',
      email: 'auditor@example.com',
      roles: ['ROLE_AUDITOR']
    }))

    render(<RoleBasedDashboardWrapper />)
    expect(screen.getByTestId('auditor-dashboard')).toBeInTheDocument()
  })

  it('should render EmployeeDashboard for INVENTORY_MANAGER role', () => {
    mockUseAuth.mockReturnValue(createMockAuth({
      id: '1',
      username: 'inventory',
      email: 'inventory@example.com',
      roles: ['ROLE_INVENTORY_MANAGER']
    }))

    render(<RoleBasedDashboardWrapper />)
    expect(screen.getByTestId('employee-dashboard')).toBeInTheDocument()
  })

  it('should render EmployeeDashboard for EMPLOYEE role', () => {
    mockUseAuth.mockReturnValue(createMockAuth({
      id: '1',
      username: 'employee',
      email: 'employee@example.com',
      roles: ['ROLE_EMPLOYEE']
    }))

    render(<RoleBasedDashboardWrapper />)
    expect(screen.getByTestId('employee-dashboard')).toBeInTheDocument()
  })

  it('should render CustomerDashboard for CUSTOMER role', () => {
    mockUseAuth.mockReturnValue(createMockAuth({
      id: '1',
      username: 'customer',
      email: 'customer@example.com',
      roles: ['ROLE_CUSTOMER']
    }))

    render(<RoleBasedDashboardWrapper />)
    expect(screen.getByTestId('customer-dashboard')).toBeInTheDocument()
  })

  it('should render CustomerDashboard as fallback for unknown roles', () => {
    mockUseAuth.mockReturnValue(createMockAuth({
      id: '1',
      username: 'unknown',
      email: 'unknown@example.com',
      roles: ['ROLE_UNKNOWN_ROLE']
    }))

    render(<RoleBasedDashboardWrapper />)
    expect(screen.getByTestId('customer-dashboard')).toBeInTheDocument()
  })

  it('should prioritize higher roles when user has multiple roles', () => {
    // User has both EMPLOYEE and SYSTEM_ADMIN roles
    // Should render AdminDashboard due to higher priority
    mockUseAuth.mockReturnValue(createMockAuth({
      id: '1',
      username: 'multipleRoles',
      email: 'multiple@example.com',
      roles: ['ROLE_EMPLOYEE', 'ROLE_SYSTEM_ADMIN']
    }))

    render(<RoleBasedDashboardWrapper />)
    expect(screen.getByTestId('admin-dashboard')).toBeInTheDocument()
  })

  it('should handle roles with ROLE_ prefix stripped', () => {
    mockUseAuth.mockReturnValue(createMockAuth({
      id: '1',
      username: 'prefixTest',
      email: 'prefix@example.com',
      roles: ['SHOP_OWNER'] // Without ROLE_ prefix
    }))

    render(<RoleBasedDashboardWrapper />)
    expect(screen.getByTestId('owner-manager-dashboard')).toBeInTheDocument()
  })
})