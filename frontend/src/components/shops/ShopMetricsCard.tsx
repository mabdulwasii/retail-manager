import React from 'react'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import { Loader2, TrendingUp, TrendingDown } from 'lucide-react'
import { cn } from '@/lib/utils'

interface ShopMetricsCardProps {
  title: string
  value: string | number
  icon?: React.ReactNode
  subtitle?: string
  trend?: {
    value: number
    label: string
  }
  loading?: boolean
  className?: string
}

export const ShopMetricsCard: React.FC<ShopMetricsCardProps> = ({
  title,
  value,
  icon,
  subtitle,
  trend,
  loading = false,
  className
}) => {
  const formatValue = (val: string | number) => {
    if (typeof val === 'number') {
      return val.toLocaleString()
    }
    return val
  }

  const getTrendColor = (trendValue: number) => {
    if (trendValue > 0) return 'text-green-600'
    if (trendValue < 0) return 'text-red-600'
    return 'text-muted-foreground'
  }

  return (
    <Card className={className}>
      <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
        <CardTitle className="text-sm font-medium">{title}</CardTitle>
        {icon && <div className="h-4 w-4 text-muted-foreground">{icon}</div>}
      </CardHeader>
      <CardContent>
        {loading ? (
          <Loader2 className="h-6 w-6 animate-spin text-muted-foreground" />
        ) : (
          <>
            <div className="text-2xl font-bold">{formatValue(value)}</div>
            
            {subtitle && (
              <p className="text-xs text-muted-foreground mt-1">
                {subtitle}
              </p>
            )}
            
            {trend && (
              <div className={cn("flex items-center gap-1 text-xs mt-1", getTrendColor(trend.value))}>
                {trend.value > 0 ? (
                  <TrendingUp className="h-3 w-3" />
                ) : trend.value < 0 ? (
                  <TrendingDown className="h-3 w-3" />
                ) : null}
                <span>
                  {trend.value > 0 ? '+' : ''}{trend.value}% {trend.label}
                </span>
              </div>
            )}
          </>
        )}
      </CardContent>
    </Card>
  )
}
