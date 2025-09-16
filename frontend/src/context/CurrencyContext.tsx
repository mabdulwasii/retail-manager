import React, { createContext, useContext } from 'react'
import { useCurrencyProvider } from '@/hooks/useCurrency'
import type { Currency } from '@/hooks/useCurrency'

interface CurrencyContextType {
  currency: Currency
  setCurrency: (currency: Currency) => void
  formatAmount: (amount: number, options?: Intl.NumberFormatOptions) => string
  formatCurrency: (amount: number, showSymbol?: boolean) => string
  parseCurrency: (value: string) => number
}

const CurrencyContext = createContext<CurrencyContextType | undefined>(undefined)

interface CurrencyProviderProps {
  children: React.ReactNode
}

export const CurrencyProvider: React.FC<CurrencyProviderProps> = ({ children }) => {
  const currencyUtils = useCurrencyProvider()

  return (
    <CurrencyContext.Provider value={currencyUtils}>
      {children}
    </CurrencyContext.Provider>
  )
}

export const useCurrencyContext = (): CurrencyContextType => {
  const context = useContext(CurrencyContext)
  if (!context) {
    throw new Error('useCurrencyContext must be used within a CurrencyProvider')
  }
  return context
}