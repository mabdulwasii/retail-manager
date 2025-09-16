import { renderHook } from '@testing-library/react'
import { useCurrency, useCurrencyProvider, SUPPORTED_CURRENCIES } from '../useCurrency'

// Mock localStorage
const localStorageMock = {
  getItem: jest.fn(),
  setItem: jest.fn(),
  removeItem: jest.fn(),
  clear: jest.fn(),
}
Object.defineProperty(window, 'localStorage', { value: localStorageMock })

describe('useCurrency', () => {
  beforeEach(() => {
    jest.clearAllMocks()
    localStorageMock.getItem.mockReturnValue(null)
  })

  describe('useCurrencyProvider', () => {
    it('should initialize with Nigerian Naira as default currency', () => {
      const { result } = renderHook(() => useCurrencyProvider())

      expect(result.current.currency.code).toBe('NGN')
      expect(result.current.currency.symbol).toBe('₦')
      expect(result.current.currency.name).toBe('Nigerian Naira')
    })

    it('should load currency from localStorage if available', () => {
      const savedCurrency = JSON.stringify({ code: 'USD' })
      localStorageMock.getItem.mockReturnValue(savedCurrency)

      const { result } = renderHook(() => useCurrencyProvider())

      expect(result.current.currency.code).toBe('USD')
    })

    it('should fallback to default if localStorage has invalid data', () => {
      localStorageMock.getItem.mockReturnValue('invalid-json')

      const { result } = renderHook(() => useCurrencyProvider())

      expect(result.current.currency.code).toBe('NGN')
    })

    it('should save currency to localStorage when changed', () => {
      const { result } = renderHook(() => useCurrencyProvider())
      const usdCurrency = SUPPORTED_CURRENCIES.find(c => c.code === 'USD')!

      result.current.setCurrency(usdCurrency)

      expect(localStorageMock.setItem).toHaveBeenCalledWith(
        'shop-manager-currency',
        JSON.stringify(usdCurrency)
      )
    })

    it('should format currency with Nigerian Naira by default', () => {
      const { result } = renderHook(() => useCurrencyProvider())

      const formatted = result.current.formatCurrency(1000)
      expect(formatted).toBe('₦1,000.00')
    })

    it('should format currency without symbol when requested', () => {
      const { result } = renderHook(() => useCurrencyProvider())

      const formatted = result.current.formatCurrency(1000, false)
      expect(formatted).toBe('1,000.00')
    })

    it('should format amount with custom options', () => {
      const { result } = renderHook(() => useCurrencyProvider())

      const formatted = result.current.formatAmount(1234.567, { maximumFractionDigits: 1 })
      expect(formatted).toBe('1,234.6')
    })

    it('should parse currency string correctly', () => {
      const { result } = renderHook(() => useCurrencyProvider())

      expect(result.current.parseCurrency('₦1,234.56')).toBe(1234.56)
      expect(result.current.parseCurrency('$1,000')).toBe(1000)
      expect(result.current.parseCurrency('invalid')).toBe(0)
    })

    it('should handle fallback formatting for unsupported locales', () => {
      const { result } = renderHook(() => useCurrencyProvider())

      // Mock Intl.NumberFormat to throw an error
      const originalNumberFormat = Intl.NumberFormat
      jest.spyOn(Intl, 'NumberFormat').mockImplementation(() => {
        throw new Error('Unsupported locale')
      })

      const formatted = result.current.formatAmount(1234.56)
      expect(formatted).toBe('1234.56')

      // Restore original implementation
      Intl.NumberFormat = originalNumberFormat
    })
  })

  describe('useCurrency fallback', () => {
    it('should provide fallback functionality when used outside provider', () => {
      const { result } = renderHook(() => useCurrency())

      expect(result.current.currency.code).toBe('NGN')
      expect(result.current.formatCurrency(1000)).toBe('₦1,000.00')
      expect(result.current.parseCurrency('₦1,000')).toBe(1000)

      // setCurrency should be a no-op in fallback mode
      expect(() => result.current.setCurrency(SUPPORTED_CURRENCIES[1])).not.toThrow()
    })
  })

  describe('SUPPORTED_CURRENCIES', () => {
    it('should contain Nigerian Naira as the first currency', () => {
      expect(SUPPORTED_CURRENCIES[0].code).toBe('NGN')
      expect(SUPPORTED_CURRENCIES[0].symbol).toBe('₦')
    })

    it('should contain all expected currencies', () => {
      const codes = SUPPORTED_CURRENCIES.map(c => c.code)
      expect(codes).toContain('NGN')
      expect(codes).toContain('USD')
      expect(codes).toContain('EUR')
      expect(codes).toContain('GBP')
      expect(codes).toContain('GHS')
      expect(codes).toContain('KES')
      expect(codes).toContain('ZAR')
    })

    it('should have valid currency objects', () => {
      SUPPORTED_CURRENCIES.forEach(currency => {
        expect(currency.code).toBeTruthy()
        expect(currency.symbol).toBeTruthy()
        expect(currency.name).toBeTruthy()
        expect(currency.locale).toBeTruthy()
        expect(typeof currency.decimalPlaces).toBe('number')
        expect(currency.decimalPlaces).toBeGreaterThanOrEqual(0)
      })
    })
  })
})