import React from 'react'
import { render, screen } from '@testing-library/react'
import { RoleBasedDashboard } from '../RoleBasedDashboard'
import { useAuth } from '@/context/KeycloakAuthContext'

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
jest.mock('@/context/KeycloakAuthContext', () => ({
  useAuth: jest.fn()
}))

const mockUseAuth = useAuth as jest.MockedFunction<typeof useAuth>

describe('RoleBasedDashboard', () => {
  beforeEach(() => {
    jest.clearAllMocks()
  })

  it('should show loading when user is null', () => {
    mockUseAuth.mockReturnValue({
      user: null,
      login: jest.fn(),
      logout: jest.fn(),
      isLoading: false,
      isAuthenticated: false,
    })

    render(<RoleBasedDashboard />)
    expect(screen.getByText('Loading dashboard...')).toBeInTheDocument()
  })

  it('should show loading when user has no roles', () => {
    mockUseAuth.mockReturnValue({
      user: { id: '1', username: 'test', email: 'test@example.com', roles: [] },
      login: jest.fn(),
      logout: jest.fn(),
      isLoading: false,
      isAuthenticated: true,
    })

    render(<RoleBasedDashboard />)
    expect(screen.getByText('Loading dashboard...')).toBeInTheDocument()
  })

  it('should render AdminDashboard for SYSTEM_ADMIN role', () => {
    mockUseAuth.mockReturnValue({
      user: {
        id: '1',
        username: 'admin',
        email: 'admin@example.com',
        roles: ['ROLE_SYSTEM_ADMIN', 'ROLE_SHOP_OWNER']
      },
      login: jest.fn(),
      logout: jest.fn(),
      isLoading: false,
      isAuthenticated: true,
    })

    render(<RoleBasedDashboard />)
    expect(screen.getByTestId('admin-dashboard')).toBeInTheDocument()
  })

  it('should render AdminDashboard for SUPER_ADMIN role', () => {
    mockUseAuth.mockReturnValue({
      user: {
        id: '1',
        username: 'superadmin',
        email: 'superadmin@example.com',
        roles: ['ROLE_SUPER_ADMIN']
      },
      login: jest.fn(),
      logout: jest.fn(),
      isLoading: false,
      isAuthenticated: true,
    })

    render(<RoleBasedDashboard />)
    expect(screen.getByTestId('admin-dashboard')).toBeInTheDocument()
  })

  it('should render OwnerManagerDashboard for SHOP_OWNER role', () => {
    mockUseAuth.mockReturnValue({
      user: {
        id: '1',
        username: 'owner',
        email: 'owner@example.com',
        roles: ['ROLE_SHOP_OWNER']
      },
      login: jest.fn(),
      logout: jest.fn(),
      isLoading: false,
      isAuthenticated: true,
    })

    render(<RoleBasedDashboard />)
    expect(screen.getByTestId('owner-manager-dashboard')).toBeInTheDocument()
  })

  it('should render OwnerManagerDashboard for SHOP_MANAGER role', () => {
    mockUseAuth.mockReturnValue({
      user: {
        id: '1',
        username: 'manager',
        email: 'manager@example.com',
        roles: ['ROLE_SHOP_MANAGER']
      },
      login: jest.fn(),
      logout: jest.fn(),
      isLoading: false,
      isAuthenticated: true,
    })

    render(<RoleBasedDashboard />)
    expect(screen.getByTestId('owner-manager-dashboard')).toBeInTheDocument()
  })

  it('should render OwnerManagerDashboard for SALES_MANAGER role', () => {
    mockUseAuth.mockReturnValue({
      user: {
        id: '1',
        username: 'salesmanager',
        email: 'salesmanager@example.com',
        roles: ['ROLE_SALES_MANAGER']
      },
      login: jest.fn(),
      logout: jest.fn(),
      isLoading: false,
      isAuthenticated: true,
    })

    render(<RoleBasedDashboard />)
    expect(screen.getByTestId('owner-manager-dashboard')).toBeInTheDocument()
  })

  it('should render CashierDashboard for CASHIER role', () => {
    mockUseAuth.mockReturnValue({
      user: {
        id: '1',
        username: 'cashier',
        email: 'cashier@example.com',
        roles: ['ROLE_CASHIER']
      },
      login: jest.fn(),
      logout: jest.fn(),
      isLoading: false,
      isAuthenticated: true,
    })

    render(<RoleBasedDashboard />)
    expect(screen.getByTestId('cashier-dashboard')).toBeInTheDocument()
  })

  it('should render InvestorDashboard for INVESTOR role', () => {
    mockUseAuth.mockReturnValue({
      user: {
        id: '1',
        username: 'investor',
        email: 'investor@example.com',
        roles: ['ROLE_INVESTOR']
      },
      login: jest.fn(),
      logout: jest.fn(),
      isLoading: false,
      isAuthenticated: true,
    })

    render(<RoleBasedDashboard />)
    expect(screen.getByTestId('investor-dashboard')).toBeInTheDocument()
  })

  it('should render AccountantDashboard for ACCOUNTANT role', () => {
    mockUseAuth.mockReturnValue({
      user: {
        id: '1',
        username: 'accountant',
        email: 'accountant@example.com',
        roles: ['ROLE_ACCOUNTANT']
      },
      login: jest.fn(),
      logout: jest.fn(),
      isLoading: false,
      isAuthenticated: true,
    })

    render(<RoleBasedDashboard />)
    expect(screen.getByTestId('accountant-dashboard')).toBeInTheDocument()
  })

  it('should render AuditorDashboard for AUDITOR role', () => {
    mockUseAuth.mockReturnValue({
      user: {
        id: '1',
        username: 'auditor',
        email: 'auditor@example.com',
        roles: ['ROLE_AUDITOR']
      },
      login: jest.fn(),
      logout: jest.fn(),
      isLoading: false,
      isAuthenticated: true,
    })

    render(<RoleBasedDashboard />)
    expect(screen.getByTestId('auditor-dashboard')).toBeInTheDocument()
  })

  it('should render EmployeeDashboard for INVENTORY_MANAGER role', () => {
    mockUseAuth.mockReturnValue({
      user: {
        id: '1',
        username: 'inventory',
        email: 'inventory@example.com',
        roles: ['ROLE_INVENTORY_MANAGER']
      },
      login: jest.fn(),
      logout: jest.fn(),
      isLoading: false,
      isAuthenticated: true,
    })

    render(<RoleBasedDashboard />)
    expect(screen.getByTestId('employee-dashboard')).toBeInTheDocument()
  })

  it('should render EmployeeDashboard for SHOP_EMPLOYEE role', () => {
    mockUseAuth.mockReturnValue({
      user: {
        id: '1',
        username: 'employee',
        email: 'employee@example.com',
        roles: ['ROLE_SHOP_EMPLOYEE']
      },
      login: jest.fn(),
      logout: jest.fn(),
      isLoading: false,
      isAuthenticated: true,
    })

    render(<RoleBasedDashboard />)
    expect(screen.getByTestId('employee-dashboard')).toBeInTheDocument()
  })

  it('should render CustomerDashboard for CUSTOMER role', () => {
    mockUseAuth.mockReturnValue({
      user: {
        id: '1',
        username: 'customer',
        email: 'customer@example.com',
        roles: ['ROLE_CUSTOMER']
      },
      login: jest.fn(),
      logout: jest.fn(),
      isLoading: false,
      isAuthenticated: true,
    })

    render(<RoleBasedDashboard />)
    expect(screen.getByTestId('customer-dashboard')).toBeInTheDocument()
  })

  it('should render CustomerDashboard as fallback for unknown roles', () => {
    mockUseAuth.mockReturnValue({
      user: {
        id: '1',
        username: 'unknown',
        email: 'unknown@example.com',
        roles: ['ROLE_UNKNOWN_ROLE']
      },
      login: jest.fn(),
      logout: jest.fn(),
      isLoading: false,
      isAuthenticated: true,
    })

    render(<RoleBasedDashboard />)
    expect(screen.getByTestId('customer-dashboard')).toBeInTheDocument()
  })

  it('should prioritize higher roles when user has multiple roles', () => {
    // User has both SHOP_EMPLOYEE and SYSTEM_ADMIN roles
    // Should render AdminDashboard due to higher priority
    mockUseAuth.mockReturnValue({
      user: {
        id: '1',
        username: 'multipleRoles',
        email: 'multiple@example.com',
        roles: ['ROLE_SHOP_EMPLOYEE', 'ROLE_SYSTEM_ADMIN']
      },
      login: jest.fn(),
      logout: jest.fn(),
      isLoading: false,
      isAuthenticated: true,
    })

    render(<RoleBasedDashboard />)
    expect(screen.getByTestId('admin-dashboard')).toBeInTheDocument()
  })

  it('should handle roles with ROLE_ prefix stripped', () => {
    mockUseAuth.mockReturnValue({
      user: {
        id: '1',
        username: 'prefixTest',
        email: 'prefix@example.com',
        roles: ['SHOP_OWNER'] // Without ROLE_ prefix
      },
      login: jest.fn(),
      logout: jest.fn(),
      isLoading: false,
      isAuthenticated: true,
    })

    render(<RoleBasedDashboard />)
    expect(screen.getByTestId('owner-manager-dashboard')).toBeInTheDocument()
  })
})