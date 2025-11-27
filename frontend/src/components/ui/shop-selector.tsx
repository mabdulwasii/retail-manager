import React from 'react'
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select'
import { useActiveShops } from '@/hooks/useDashboard'
import { Building2, Loader2 } from 'lucide-react'
import { useAuth } from '@/context/ManualAuthContext'

interface ShopSelectorProps {
  value?: string
  onValueChange: (value: string) => void
  className?: string
  placeholder?: string
  showAllOption?: boolean // Option to show "All Shops" for admins
}

export const ShopSelector: React.FC<ShopSelectorProps> = ({
  value,
  onValueChange,
  className,
  placeholder = "Select shop",
  showAllOption = false
}) => {
  const { user } = useAuth()
  const { data: shops, isLoading } = useActiveShops()

  // Determine current value - empty string for undefined/null to avoid uncontrolled component warnings
  const currentValue = value || ''

  if (isLoading) {
    return (
      <div className="flex items-center space-x-2 px-3 py-2 border rounded-md bg-background">
        <Loader2 className="h-4 w-4 animate-spin text-muted-foreground" />
        <span className="text-sm text-muted-foreground">Loading shops...</span>
      </div>
    )
  }

  if (!shops || shops.length === 0) {
    return (
      <div className="flex items-center space-x-2 px-3 py-2 border rounded-md bg-muted/50">
        <Building2 className="h-4 w-4 text-muted-foreground" />
        <span className="text-sm text-muted-foreground">No shops available</span>
      </div>
    )
  }

  // If there's only one shop, show it as read-only
  if (shops.length === 1) {
    return (
      <div className="flex items-center space-x-2 px-3 py-2 border rounded-md bg-muted/50">
        <Building2 className="h-4 w-4 text-muted-foreground" />
        <span className="text-sm font-medium text-foreground">{shops[0].name}</span>
      </div>
    )
  }

  return (
    <Select value={currentValue} onValueChange={onValueChange}>
      <SelectTrigger className={className}>
        <div className="flex items-center space-x-2">
          <Building2 className="h-4 w-4" />
          <SelectValue placeholder={placeholder} />
        </div>
      </SelectTrigger>
      <SelectContent>
        {showAllOption && (
          <SelectItem value="">
            <div className="flex items-center space-x-2">
              <span className="font-medium">All Shops</span>
            </div>
          </SelectItem>
        )}
        {shops.map((shop) => (
          <SelectItem key={shop.id} value={shop.id}>
            <div className="flex items-center justify-between w-full">
              <span>{shop.name}</span>
              {shop.status === 'ACTIVE' && (
                <span className="ml-2 text-xs text-green-600 dark:text-green-400">●</span>
              )}
            </div>
          </SelectItem>
        ))}
      </SelectContent>
    </Select>
  )
}
