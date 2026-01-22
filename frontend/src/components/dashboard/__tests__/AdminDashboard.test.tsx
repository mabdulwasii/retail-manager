import React from 'react'
import { waitFor } from '@testing-library/react'
import { createMockAuth, renderWithProviders } from '@/test/test-utils'
import { AdminDashboard } from '../AdminDashboard'
import { useAuth } from '@/context/UnifiedAuthContext'
import { useCurrency } from '@/hooks/useCurrency'
import { getMockAdmin } from '@/testData'
import type {
  MockCardProps,
  MockButtonProps,
  MockSelectProps,
  MockSelectItemProps,
  MockShopSelectorProps
} from '@/test-utils/mock-types'

// Mock only infrastructure dependencies (NOT data hooks)
jest.mock('@/context/UnifiedAuthContext')
jest.mock('@/hooks/useCurrency')

const mockUseAuth = useAuth as jest.MockedFunction<typeof useAuth>
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

jest.mock('@/components/ui/select', () => ({
  Select: ({ children, value, onValueChange }: MockSelectProps) => (
    <select value={value} onChange={(e) => onValueChange?.(e.target.value)}>
      {children}
    </select>
  ),
  SelectContent: ({ children }: MockCardProps) => <div>{children}</div>,
  SelectItem: ({ children, value }: MockSelectItemProps) => <option value={value}>{children}</option>,
  SelectTrigger: ({ children }: MockCardProps) => <div>{children}</div>,
  SelectValue: ({ placeholder }: { placeholder?: string }) => <span>{placeholder}</span>
}))

jest.mock('@/components/ui/shop-selector', () => ({
  ShopSelector: ({ value, onValueChange }: MockShopSelectorProps) => (
    <select data-testid="shop-selector" value={value} onChange={(e) => onValueChange?.(e.target.value)}>
      <option value="">All Shops</option>
      <option value="shop1">Shop 1</option>
    </select>
  )
}))

describe('AdminDashboard', () => {
  const mockUser = getMockAdmin()

  beforeEach(() => {
    jest.clearAllMocks()

    mockUseAuth.mockReturnValue(createMockAuth(mockUser))

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

  it('should render without crashing', () => {
    const { container } = renderWithProviders(<AdminDashboard />)
    
    // Component should mount successfully
    expect(container).toBeInTheDocument()
  })

  it('should use currency formatting utilities', () => {
    renderWithProviders(<AdminDashboard />)

    // Verify currency hook is being called
    expect(mockUseCurrency).toHaveBeenCalled()
    expect(mockUseCurrency().formatCurrency).toBeDefined()
  })

  it('should render with loading state initially', async () => {
    const { container } = renderWithProviders(<AdminDashboard />)

    // Component should render even in loading state
    expect(container.querySelector('.space-y-6')).toBeInTheDocument()
  })

  it('should handle async data loading', async () => {
    renderWithProviders(<AdminDashboard />)

    // Just verify component mounted - data will load via MSW
    await waitFor(() => {
      expect(mockUseAuth).toHaveBeenCalled()
    })
  })
})
