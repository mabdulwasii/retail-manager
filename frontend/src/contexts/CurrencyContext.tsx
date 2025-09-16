import React, { createContext, useContext, useState } from 'react'

export interface CurrencyContextType {
  currency: string
  setCurrency: (currency: string) => void
  formatCurrency: (amount: number) => string
}

const CurrencyContext = createContext<CurrencyContextType | undefined>(undefined)

export const useCurrency = () => {
  const context = useContext(CurrencyContext)
  if (context === undefined) {
    throw new Error('useCurrency must be used within a CurrencyProvider')
  }
  return context
}

interface CurrencyProviderProps {
  children: React.ReactNode
}

export const CurrencyProvider: React.FC<CurrencyProviderProps> = ({ children }) => {
  const [currency, setCurrency] = useState('USD')

  const formatCurrency = (amount: number): string => {
    const currencySymbols: { [key: string]: string } = {
      USD: '$',
      EUR: '€',
      GBP: '£',
      NGN: '₦'
    }

    const symbol = currencySymbols[currency] || currency

    return new Intl.NumberFormat('en-US', {
      style: 'currency',
      currency: currency,
      minimumFractionDigits: 2,
      maximumFractionDigits: 2,
    }).format(amount).replace(/[A-Z]{3}/, symbol)
  }

  const value: CurrencyContextType = {
    currency,
    setCurrency,
    formatCurrency
  }

  return <CurrencyContext.Provider value={value}>{children}</CurrencyContext.Provider>
}