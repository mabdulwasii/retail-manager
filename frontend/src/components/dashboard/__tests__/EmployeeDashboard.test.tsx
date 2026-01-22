import React from 'react'
import { render, screen } from '@testing-library/react'
import type {
  MockCardProps,
  MockButtonProps,
  MockSelectProps,
  MockSelectItemProps,
  MockShopSelectorProps
} from '@/test-utils/mock-types'

import { createMockAuth } from '@/test/test-utils'
import { MemoryRouter } from 'react-router-dom'
import { EmployeeDashboard } from '../EmployeeDashboard'
import { useAuth } from '@/context/UnifiedAuthContext'
import { usePermissions } from '@/hooks/usePermissions'
import { useInventorySummary } from '@/hooks/useDashboard'

// Mock dependencies
jest.mock('@/context/UnifiedAuthContext')
jest.mock('@/hooks/usePermissions', () => ({
  usePermissions: jest.fn()
}))
jest.mock('@/hooks/useDashboard')

const mockUseAuth = useAuth as jest.MockedFunction<typeof useAuth>
const mockUsePermissions = usePermissions as jest.MockedFunction<typeof usePermissions>
const mockUseInventorySummary = useInventorySummary as jest.MockedFunction<typeof useInventorySummary>

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

const EmployeeDashboardWrapper: React.FC = () => (
  <MemoryRouter>
    <EmployeeDashboard />
  </MemoryRouter>
)

describe('EmployeeDashboard', () => {
  const mockUser = {
    id: '1',
    username: 'employee',
    email: 'employee@example.com',
    firstName: 'John',
    shopId: 'shop1',
    roles: ['ROLE_EMPLOYEE']
  }

  const mockPermissions = {
    canViewInventory: jest.fn(() => true),
    canViewProducts: jest.fn(() => true)
  }

  const mockInventoryData = {
    totalItems: 250,
    lowStockItems: 15,
    expiredItems: 5,
    expiringSoonItems: 10,
    totalValue: 1500000
  }

  beforeEach(() => {
    jest.clearAllMocks()

    mockUseAuth.mockReturnValue(createMockAuth(mockUser))

    mockUsePermissions.mockReturnValue(mockPermissions as any)

    mockUseInventorySummary.mockReturnValue({
      data: mockInventoryData,
      isLoading: false,
      error: null,
      refetch: jest.fn()
    })
  })

  it('should render welcome message', () => {
    render(<EmployeeDashboardWrapper />)

    expect(screen.getByText('Inventory Management')).toBeInTheDocument()
    expect(screen.getByText(/Welcome, John/)).toBeInTheDocument()
  })

  it('should render inventory statistics cards', () => {
    render(<EmployeeDashboardWrapper />)

    expect(screen.getByText('Total Items')).toBeInTheDocument()
    
    // Use getAllByText for items that appear multiple times
    const lowStockElements = screen.getAllByText('Low Stock')
    expect(lowStockElements.length).toBeGreaterThan(0)
    
    const expiredElements = screen.getAllByText('Expired Items')
    expect(expiredElements.length).toBeGreaterThan(0)
    
    const expiringSoonElements = screen.getAllByText('Expiring Soon')
    expect(expiringSoonElements.length).toBeGreaterThan(0)
  })

  it('should show priority alerts when there are issues', () => {
    render(<EmployeeDashboardWrapper />)

    expect(screen.getByText(/Action Required/)).toBeInTheDocument()
    expect(screen.getByText('Restock Low Inventory')).toBeInTheDocument()
    expect(screen.getByText('Remove Expired Items')).toBeInTheDocument()
    expect(screen.getByText('Monitor Expiring Items')).toBeInTheDocument()
  })

  it('should render quick action buttons', () => {
    render(<EmployeeDashboardWrapper />)

    expect(screen.getByText('Quick Actions')).toBeInTheDocument()
    expect(screen.getByText('View Inventory')).toBeInTheDocument()
    
    // Use getAllByText for items that appear multiple times
    const viewProductsElements = screen.getAllByText('View Products')
    expect(viewProductsElements.length).toBeGreaterThan(0)
  })

  it('should show correct routes for inventory filters', () => {
    render(<EmployeeDashboardWrapper />)

    // Use getAllByText since these appear multiple times (in cards and priority alerts)
    const lowStockButtons = screen.getAllByText('Low Stock Items')
    const lowStockLink = lowStockButtons.find(el => el.closest('a'))
    expect(lowStockLink?.closest('a')).toHaveAttribute('href', '/inventory?filter=lowStock')

    const expiredButtons = screen.getAllByText('Expired Items')
    const expiredLink = expiredButtons.find(el => el.closest('a'))
    expect(expiredLink?.closest('a')).toHaveAttribute('href', '/inventory?filter=expired')
  })

  it('should calculate inventory health percentages', () => {
    render(<EmployeeDashboardWrapper />)

    expect(screen.getByText('Inventory Health')).toBeInTheDocument()
    // Should show healthy stock percentage
    expect(screen.getByText(/92.0%/)).toBeInTheDocument() // (250 - 15 - 5) / 250 * 100
  })

  it('should handle loading state', () => {
    mockUseInventorySummary.mockReturnValue({
      data: null,
      isLoading: true,
      error: null,
      refetch: jest.fn()
    })

    render(<EmployeeDashboardWrapper />)

    expect(screen.getByText(/Loading Dashboard/)).toBeInTheDocument()
  })

  it('should hide quick actions when permissions deny', () => {
    mockPermissions.canViewInventory.mockReturnValue(false)
    mockPermissions.canViewProducts.mockReturnValue(false)

    render(<EmployeeDashboardWrapper />)

    expect(screen.queryByText('View Inventory')).not.toBeInTheDocument()
    expect(screen.queryByText('View Products')).not.toBeInTheDocument()
  })

  it('should show Review button for priority tasks', () => {
    render(<EmployeeDashboardWrapper />)

    const reviewButtons = screen.getAllByText('Review')
    expect(reviewButtons.length).toBeGreaterThan(0)
  })

  it('should render shop selector', () => {
    render(<EmployeeDashboardWrapper />)

    const shopSelector = screen.getByTestId('shop-selector')
    expect(shopSelector).toBeInTheDocument()
  })
})
