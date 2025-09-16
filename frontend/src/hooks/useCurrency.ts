import { useState, useEffect, createContext, useContext } from 'react'

export interface Currency {
  code: string
  symbol: string
  name: string
  locale: string
  decimalPlaces: number
}

export const SUPPORTED_CURRENCIES: Currency[] = [
  {
    code: 'NGN',
    symbol: '₦',
    name: 'Nigerian Naira',
    locale: 'en-NG',
    decimalPlaces: 2
  },
  {
    code: 'USD',
    symbol: '$',
    name: 'US Dollar',
    locale: 'en-US',
    decimalPlaces: 2
  },
  {
    code: 'EUR',
    symbol: '€',
    name: 'Euro',
    locale: 'en-EU',
    decimalPlaces: 2
  },
  {
    code: 'GBP',
    symbol: '£',
    name: 'British Pound',
    locale: 'en-GB',
    decimalPlaces: 2
  },
  {
    code: 'GHS',
    symbol: '¢',
    name: 'Ghanaian Cedi',
    locale: 'en-GH',
    decimalPlaces: 2
  },
  {
    code: 'KES',
    symbol: 'KSh',
    name: 'Kenyan Shilling',
    locale: 'en-KE',
    decimalPlaces: 2
  },
  {
    code: 'ZAR',
    symbol: 'R',
    name: 'South African Rand',
    locale: 'en-ZA',
    decimalPlaces: 2
  }
]

const DEFAULT_CURRENCY = SUPPORTED_CURRENCIES[0] // Nigerian Naira

interface CurrencyContextType {
  currency: Currency
  setCurrency: (currency: Currency) => void
  formatAmount: (amount: number, options?: Intl.NumberFormatOptions) => string
  formatCurrency: (amount: number, showSymbol?: boolean) => string
  parseCurrency: (value: string) => number
}

const CurrencyContext = createContext<CurrencyContextType | undefined>(undefined)

export const useCurrency = (): CurrencyContextType => {
  const context = useContext(CurrencyContext)
  if (!context) {
    // Fallback implementation when used outside provider
    const [currency] = useState<Currency>(DEFAULT_CURRENCY)

    const formatAmount = (amount: number, options?: Intl.NumberFormatOptions): string => {
      try {
        return new Intl.NumberFormat(currency.locale, {
          minimumFractionDigits: currency.decimalPlaces,
          maximumFractionDigits: currency.decimalPlaces,
          ...options
        }).format(amount)
      } catch {
        return amount.toFixed(currency.decimalPlaces)
      }
    }

    const formatCurrency = (amount: number, showSymbol = true): string => {
      const formattedAmount = formatAmount(amount)
      return showSymbol ? `${currency.symbol}${formattedAmount}` : formattedAmount
    }

    const parseCurrency = (value: string): number => {
      const cleanValue = value.replace(/[^\d.-]/g, '')
      return parseFloat(cleanValue) || 0
    }

    return {
      currency,
      setCurrency: () => {},
      formatAmount,
      formatCurrency,
      parseCurrency
    }
  }
  return context
}

export const useCurrencyProvider = () => {
  const [currency, setCurrencyState] = useState<Currency>(() => {
    // Try to load from localStorage first, fallback to default
    const saved = localStorage.getItem('shop-manager-currency')
    if (saved) {
      try {
        const parsed = JSON.parse(saved)
        const found = SUPPORTED_CURRENCIES.find(c => c.code === parsed.code)
        return found || DEFAULT_CURRENCY
      } catch {
        return DEFAULT_CURRENCY
      }
    }
    return DEFAULT_CURRENCY
  })

  const setCurrency = (newCurrency: Currency) => {
    setCurrencyState(newCurrency)
    localStorage.setItem('shop-manager-currency', JSON.stringify(newCurrency))
  }

  useEffect(() => {
    // Listen for currency changes across tabs
    const handleStorageChange = (e: StorageEvent) => {
      if (e.key === 'shop-manager-currency' && e.newValue) {
        try {
          const parsed = JSON.parse(e.newValue)
          const found = SUPPORTED_CURRENCIES.find(c => c.code === parsed.code)
          if (found) {
            setCurrencyState(found)
          }
        } catch {
          // Ignore invalid JSON
        }
      }
    }

    window.addEventListener('storage', handleStorageChange)
    return () => window.removeEventListener('storage', handleStorageChange)
  }, [])

  const formatAmount = (amount: number, options?: Intl.NumberFormatOptions): string => {
    try {
      return new Intl.NumberFormat(currency.locale, {
        minimumFractionDigits: currency.decimalPlaces,
        maximumFractionDigits: currency.decimalPlaces,
        ...options
      }).format(amount)
    } catch {
      // Fallback for unsupported locales
      return amount.toFixed(currency.decimalPlaces)
    }
  }

  const formatCurrency = (amount: number, showSymbol = true): string => {
    const formattedAmount = formatAmount(amount)
    return showSymbol ? `${currency.symbol}${formattedAmount}` : formattedAmount
  }

  const parseCurrency = (value: string): number => {
    // Remove currency symbols and non-numeric characters except decimal point and minus
    const cleanValue = value.replace(/[^\d.-]/g, '')
    return parseFloat(cleanValue) || 0
  }

  return {
    currency,
    setCurrency,
    formatAmount,
    formatCurrency,
    parseCurrency
  }
}