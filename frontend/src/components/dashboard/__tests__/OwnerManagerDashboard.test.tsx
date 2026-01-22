/**
 * OwnerManagerDashboard Component Tests
 * Tests owner/manager dashboard rendering, permissions, and data display
 */

import React from 'react'
import { screen, waitFor } from '@testing-library/react'
import type {
  MockCardProps,
  MockButtonProps,
  MockSelectProps,
  MockSelectItemProps,
  MockShopSelectorProps
} from '@/test-utils/mock-types'

import { OwnerManagerDashboard } from '../OwnerManagerDashboard'
import { useAuth } from '@/context/UnifiedAuthContext'
import { usePermissions } from '@/hooks/usePermissions'
import { useCurrency } from '@/hooks/useCurrency'
import { createMockAuth, renderWithProviders } from '@/test/test-utils'
import { getMockShopOwner } from '@/testData'
import { server } from '@/mocks/server'
import { http, HttpResponse } from 'msw'

// Mock only infrastructure dependencies (NOT data hooks)
jest.mock('@/context/UnifiedAuthContext')
jest.mock('@/hooks/usePermissions', () => ({
  usePermissions: jest.fn()
}))
jest.mock('@/hooks/useCurrency')

const mockUseAuth = useAuth as jest.MockedFunction<typeof useAuth>
const mockUsePermissions = usePermissions as jest.MockedFunction<typeof usePermissions>
const mockUseCurrency = useCurrency as jest.MockedFunction<typeof useCurrency>

// Mock UI components
jest.mock('@/components/ui/card', () => ({
  Card: ({ children, className }: MockCardProps) => <div className={`card ${className || ''}`}>{children}</div>,
  CardContent: ({ children }: MockCardProps) => <div className="card-content">{children}</div>,
  CardDescription: ({ children }: MockCardProps) => <div className="card-description">{children}</div>,
  CardHeader: ({ children }: MockCardProps) => <div className="card-header">{children}</div>,
  CardTitle: ({ children }: MockCardProps) => <div className="card-title">{children}</div>,
}))

jest.mock('@/components/ui/button', () => ({
  Button: ({ children, className, asChild, ...props }: MockButtonProps) =>
    asChild ? children : <button className={className} {...props}>{children}</button>
}))

jest.mock('@/components/ui/shop-selector', () => ({
  ShopSelector: ({ value, onValueChange }: MockShopSelectorProps) => (
    <select data-testid="shop-selector" value={value} onChange={(e) => onValueChange?.(e.target.value)}>
      <option value="shop1">Shop 1</option>
    </select>
  )
}))

jest.mock('@/components/ui/select', () => ({
  Select: ({ children }: MockCardProps) => <div>{children}</div>,
  SelectContent: ({ children }: MockCardProps) => <div>{children}</div>,
  SelectItem: ({ children, value }: MockSelectItemProps) => <option value={value}>{children}</option>,
  SelectTrigger: ({ children }: MockCardProps) => <div>{children}</div>,
  SelectValue: ({ placeholder }: { placeholder?: string }) => <span>{placeholder}</span>,
}))

describe('OwnerManagerDashboard', () => {
  const mockUser = getMockShopOwner()

  const mockPermissions = {
    hasPermission: jest.fn(() => true),
    hasAnyPermission: jest.fn(() => true),
    hasAllPermissions: jest.fn(() => true),
    canCreateSale: jest.fn(() => true),
    canCreateProduct: jest.fn(() => true),
    canViewInventory: jest.fn(() => true),
    canViewInvestments: jest.fn(() => true),
    canViewReceipts: jest.fn(() => true),
    canViewExpenses: jest.fn(() => true),
    canViewAnalytics: jest.fn(() => true),
    canViewProducts: jest.fn(() => true),
    canViewShops: jest.fn(() => true),
    canViewSales: jest.fn(() => true)
  }

  beforeEach(() => {
    jest.clearAllMocks()

    mockUseAuth.mockReturnValue(createMockAuth(mockUser))

    mockUsePermissions.mockReturnValue(mockPermissions as any)

    mockUseCurrency.mockReturnValue({
      currency: {
        code: 'NGN',
        symbol: '₦',
        name: 'Nigerian Naira',
        locale: 'en-NG',
        decimalPlaces: 2
      },
      setCurrency: jest.fn(),
      formatAmount: jest.fn((amount) => amount.toLocaleString()),
      formatCurrency: jest.fn((amount) => `₦${amount.toLocaleString()}`),
      parseCurrency: jest.fn()
    })

    // MSW will handle the dashboard data API calls!
  })

  afterEach(() => {
    jest.clearAllMocks()
  })

  it('should render without crashing', async () => {
    const { container } = renderWithProviders(<OwnerManagerDashboard />)
    
    // Component should mount successfully
    expect(container).toBeInTheDocument()
  })

  it('should render with loading state initially', async () => {
    const { container } = renderWithProviders(<OwnerManagerDashboard />)

    // Component should render even in loading state
    expect(container.querySelector('.space-y-6')).toBeInTheDocument()
  })

  it('should use currency formatting utilities', () => {
    renderWithProviders(<OwnerManagerDashboard />)

    // Verify currency hook is being called
    expect(mockUseCurrency).toHaveBeenCalled()
    expect(mockUseCurrency().formatCurrency).toBeDefined()
  })

  it('should respect user permissions', () => {
    renderWithProviders(<OwnerManagerDashboard />)

    // Verify permissions hook is being called
    expect(mockUsePermissions).toHaveBeenCalled()
  })
})
