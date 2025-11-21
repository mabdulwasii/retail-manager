import React from 'react'
import { Badge } from '@/components/ui/badge'
import { cn } from '@/lib/utils'

interface ShopStatusBadgeProps {
  status: string
  className?: string
  showIcon?: boolean
}

export const ShopStatusBadge: React.FC<ShopStatusBadgeProps> = ({ 
  status, 
  className,
  showIcon = false 
}) => {
  const getStatusConfig = (status: string) => {
    switch (status.toUpperCase()) {
      case 'ACTIVE':
        return {
          color: 'bg-green-100 text-green-700 hover:bg-green-200',
          label: 'Active',
          description: 'Shop is currently operating'
        }
      case 'INACTIVE':
        return {
          color: 'bg-yellow-100 text-yellow-700 hover:bg-yellow-200',
          label: 'Inactive',
          description: 'Shop is temporarily closed'
        }
      case 'SUSPENDED':
        return {
          color: 'bg-red-100 text-red-700 hover:bg-red-200',
          label: 'Suspended',
          description: 'Shop is under review or suspended'
        }
      case 'CLOSED':
        return {
          color: 'bg-gray-100 text-gray-700 hover:bg-gray-200',
          label: 'Closed',
          description: 'Shop has been permanently closed'
        }
      default:
        return {
          color: 'bg-gray-100 text-gray-700 hover:bg-gray-200',
          label: status,
          description: 'Unknown status'
        }
    }
  }

  const config = getStatusConfig(status)

  return (
    <Badge 
      className={cn(config.color, className)}
      title={config.description}
    >
      {showIcon && (
        <span className="mr-1.5 h-1.5 w-1.5 rounded-full bg-current inline-block" />
      )}
      {config.label}
    </Badge>
  )
}
