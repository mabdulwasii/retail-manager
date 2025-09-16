import React from 'react'
import { render, screen, fireEvent, waitFor } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { CurrencySelector } from '../currency-selector'
import { CurrencyProvider } from '@/context/CurrencyContext'
import { SUPPORTED_CURRENCIES } from '@/hooks/useCurrency'

// Mock the dropdown menu components
jest.mock('@/components/ui/dropdown-menu', () => ({
  DropdownMenu: ({ children }: { children: React.ReactNode }) => <div data-testid="dropdown-menu">{children}</div>,
  DropdownMenuContent: ({ children }: { children: React.ReactNode }) => <div data-testid="dropdown-content">{children}</div>,
  DropdownMenuTrigger: ({ children, asChild }: { children: React.ReactNode, asChild?: boolean }) =>
    asChild ? children : <div data-testid="dropdown-trigger">{children}</div>,
  DropdownMenuItem: ({ children, onClick }: { children: React.ReactNode, onClick?: () => void }) =>
    <div data-testid="dropdown-item" onClick={onClick}>{children}</div>,
}))

const CurrencySelectorWrapper: React.FC<{ children: React.ReactNode }> = ({ children }) => (
  <CurrencyProvider>
    {children}
  </CurrencyProvider>
)

describe('CurrencySelector', () => {
  const user = userEvent.setup()

  beforeEach(() => {
    // Clear localStorage before each test
    localStorage.clear()
  })

  it('should render with default props', () => {
    render(
      <CurrencySelectorWrapper>
        <CurrencySelector />
      </CurrencySelectorWrapper>
    )

    expect(screen.getByTestId('dropdown-menu')).toBeInTheDocument()
    expect(screen.getByText('₦ NGN')).toBeInTheDocument()
  })

  it('should show only symbol on small screens when showLabel is true', () => {
    render(
      <CurrencySelectorWrapper>
        <CurrencySelector showLabel={true} />
      </CurrencySelectorWrapper>
    )

    // The component uses responsive classes (hidden on small, visible on sm and up)
    const fullLabel = screen.getByText('₦ NGN')
    const symbolOnly = screen.getByText('₦')

    expect(fullLabel).toBeInTheDocument()
    expect(symbolOnly).toBeInTheDocument()
  })

  it('should not show label when showLabel is false', () => {
    render(
      <CurrencySelectorWrapper>
        <CurrencySelector showLabel={false} />
      </CurrencySelectorWrapper>
    )

    expect(screen.queryByText('₦ NGN')).not.toBeInTheDocument()
    expect(screen.getByText('₦')).toBeInTheDocument()
  })

  it('should apply custom className', () => {
    render(
      <CurrencySelectorWrapper>
        <CurrencySelector className="custom-class" />
      </CurrencySelectorWrapper>
    )

    const button = screen.getByRole('button')
    expect(button).toHaveClass('custom-class')
  })

  it('should render all supported currencies in dropdown', () => {
    render(
      <CurrencySelectorWrapper>
        <CurrencySelector />
      </CurrencySelectorWrapper>
    )

    // Check that all currencies are rendered
    SUPPORTED_CURRENCIES.forEach(currency => {
      expect(screen.getByText(currency.code)).toBeInTheDocument()
      expect(screen.getByText(currency.name)).toBeInTheDocument()
      expect(screen.getByText(currency.symbol)).toBeInTheDocument()
    })
  })

  it('should show current currency as selected', () => {
    render(
      <CurrencySelectorWrapper>
        <CurrencySelector />
      </CurrencySelectorWrapper>
    )

    // Nigerian Naira should be selected by default
    const ngnItem = screen.getAllByTestId('dropdown-item').find(item =>
      item.textContent?.includes('NGN')
    )

    expect(ngnItem).toBeInTheDocument()
    // The selected currency should have a selection indicator
    expect(ngnItem?.querySelector('.w-2.h-2.bg-primary.rounded-full')).toBeInTheDocument()
  })

  it('should handle currency selection', async () => {
    render(
      <CurrencySelectorWrapper>
        <CurrencySelector />
      </CurrencySelectorWrapper>
    )

    // Find and click USD option
    const usdItem = screen.getAllByTestId('dropdown-item').find(item =>
      item.textContent?.includes('USD')
    )

    expect(usdItem).toBeInTheDocument()

    if (usdItem) {
      await user.click(usdItem)
    }

    // After selection, the button should show USD
    await waitFor(() => {
      expect(screen.getByText('$ USD')).toBeInTheDocument()
    })
  })

  it('should handle different button variants', () => {
    const { rerender } = render(
      <CurrencySelectorWrapper>
        <CurrencySelector variant="default" />
      </CurrencySelectorWrapper>
    )

    let button = screen.getByRole('button')
    expect(button).toBeInTheDocument()

    rerender(
      <CurrencySelectorWrapper>
        <CurrencySelector variant="outline" />
      </CurrencySelectorWrapper>
    )

    button = screen.getByRole('button')
    expect(button).toBeInTheDocument()

    rerender(
      <CurrencySelectorWrapper>
        <CurrencySelector variant="ghost" />
      </CurrencySelectorWrapper>
    )

    button = screen.getByRole('button')
    expect(button).toBeInTheDocument()
  })

  it('should handle different button sizes', () => {
    const { rerender } = render(
      <CurrencySelectorWrapper>
        <CurrencySelector size="sm" />
      </CurrencySelectorWrapper>
    )

    let button = screen.getByRole('button')
    expect(button).toBeInTheDocument()

    rerender(
      <CurrencySelectorWrapper>
        <CurrencySelector size="default" />
      </CurrencySelectorWrapper>
    )

    button = screen.getByRole('button')
    expect(button).toBeInTheDocument()

    rerender(
      <CurrencySelectorWrapper>
        <CurrencySelector size="lg" />
      </CurrencySelectorWrapper>
    )

    button = screen.getByRole('button')
    expect(button).toBeInTheDocument()
  })

  it('should persist currency selection in localStorage', async () => {
    render(
      <CurrencySelectorWrapper>
        <CurrencySelector />
      </CurrencySelectorWrapper>
    )

    // Find and click EUR option
    const eurItem = screen.getAllByTestId('dropdown-item').find(item =>
      item.textContent?.includes('EUR')
    )

    if (eurItem) {
      await user.click(eurItem)
    }

    // Check localStorage
    await waitFor(() => {
      const savedCurrency = localStorage.getItem('shop-manager-currency')
      expect(savedCurrency).toBeTruthy()

      if (savedCurrency) {
        const parsed = JSON.parse(savedCurrency)
        expect(parsed.code).toBe('EUR')
      }
    })
  })

  it('should load saved currency from localStorage on mount', () => {
    // Pre-populate localStorage with USD
    const usdCurrency = SUPPORTED_CURRENCIES.find(c => c.code === 'USD')
    localStorage.setItem('shop-manager-currency', JSON.stringify(usdCurrency))

    render(
      <CurrencySelectorWrapper>
        <CurrencySelector />
      </CurrencySelectorWrapper>
    )

    // Should show USD as selected
    expect(screen.getByText('$ USD')).toBeInTheDocument()
  })

  it('should fallback to default currency if localStorage has invalid data', () => {
    // Set invalid data in localStorage
    localStorage.setItem('shop-manager-currency', 'invalid-json')

    render(
      <CurrencySelectorWrapper>
        <CurrencySelector />
      </CurrencySelectorWrapper>
    )

    // Should fallback to Nigerian Naira
    expect(screen.getByText('₦ NGN')).toBeInTheDocument()
  })
})