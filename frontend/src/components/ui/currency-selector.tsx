import React from 'react'
import { Button } from '@/components/ui/button'
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuTrigger,
} from '@/components/ui/dropdown-menu'
import { useCurrency, SUPPORTED_CURRENCIES } from '@/hooks/useCurrency'
import { ChevronDown, DollarSign } from 'lucide-react'

interface CurrencySelectorProps {
  variant?: 'default' | 'outline' | 'ghost'
  size?: 'sm' | 'default' | 'lg'
  showLabel?: boolean
  className?: string
}

export const CurrencySelector: React.FC<CurrencySelectorProps> = ({
  variant = 'outline',
  size = 'default',
  showLabel = true,
  className = ''
}) => {
  const { currency, setCurrency } = useCurrency()

  return (
    <DropdownMenu>
      <DropdownMenuTrigger asChild>
        <Button
          variant={variant}
          size={size}
          className={`flex items-center space-x-2 ${className}`}
        >
          <DollarSign className="h-4 w-4" />
          {showLabel && (
            <span className="hidden sm:inline">
              {currency.symbol} {currency.code}
            </span>
          )}
          <span className="sm:hidden">{currency.symbol}</span>
          <ChevronDown className="h-3 w-3" />
        </Button>
      </DropdownMenuTrigger>
      <DropdownMenuContent align="end" className="w-56">
        {SUPPORTED_CURRENCIES.map((curr) => (
          <DropdownMenuItem
            key={curr.code}
            onClick={() => setCurrency(curr)}
            className={`flex items-center justify-between ${
              currency.code === curr.code ? 'bg-accent' : ''
            }`}
          >
            <div className="flex items-center space-x-3">
              <span className="text-lg">{curr.symbol}</span>
              <div>
                <div className="font-medium">{curr.code}</div>
                <div className="text-sm text-muted-foreground">{curr.name}</div>
              </div>
            </div>
            {currency.code === curr.code && (
              <div className="w-2 h-2 bg-primary rounded-full" />
            )}
          </DropdownMenuItem>
        ))}
      </DropdownMenuContent>
    </DropdownMenu>
  )
}