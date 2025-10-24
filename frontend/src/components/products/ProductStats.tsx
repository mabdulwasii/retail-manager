import React from 'react'
import { Card, CardContent } from '@/components/ui/card'
import { Package, CheckCircle, AlertTriangle, TrendingUp } from 'lucide-react'
import { Product, ProductStatus } from '@/types/api'
import { formatCurrency } from '@/lib/utils'

interface ProductStatsProps {
  products: Product[]
  isLoading?: boolean
}

export const ProductStats: React.FC<ProductStatsProps> = ({
  products,
  isLoading = false,
}) => {
  const stats = React.useMemo(() => {
    if (!products || products.length === 0) {
      return {
        total: 0,
        active: 0,
        inactive: 0,
        averagePrice: 0,
      }
    }

    const total = products.length
    const active = products.filter((p) => p.status === ProductStatus.ACTIVE).length
    const inactive = products.filter(
      (p) => p.status === ProductStatus.INACTIVE || p.status === ProductStatus.DISCONTINUED
    ).length
    const totalPrice = products.reduce((sum, p) => sum + p.price, 0)
    const averagePrice = totalPrice / total

    return {
      total,
      active,
      inactive,
      averagePrice,
    }
  }, [products])

  const statCards = [
    {
      title: 'Total Products',
      value: stats.total,
      icon: Package,
      color: 'text-blue-600',
      bgColor: 'bg-blue-50',
    },
    {
      title: 'Active Products',
      value: stats.active,
      icon: CheckCircle,
      color: 'text-green-600',
      bgColor: 'bg-green-50',
    },
    {
      title: 'Inactive Products',
      value: stats.inactive,
      icon: AlertTriangle,
      color: 'text-orange-600',
      bgColor: 'bg-orange-50',
    },
    {
      title: 'Average Price',
      value: formatCurrency(stats.averagePrice),
      icon: TrendingUp,
      color: 'text-purple-600',
      bgColor: 'bg-purple-50',
    },
  ]

  if (isLoading) {
    return (
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4">
        {[1, 2, 3, 4].map((i) => (
          <Card key={i}>
            <CardContent className="p-6">
              <div className="animate-pulse">
                <div className="h-4 bg-gray-200 rounded w-3/4 mb-2"></div>
                <div className="h-8 bg-gray-200 rounded w-1/2"></div>
              </div>
            </CardContent>
          </Card>
        ))}
      </div>
    )
  }

  return (
    <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4">
      {statCards.map((stat) => (
        <Card key={stat.title}>
          <CardContent className="p-6">
            <div className="flex items-center justify-between">
              <div>
                <p className="text-sm font-medium text-gray-600">{stat.title}</p>
                <p className="text-2xl font-bold mt-1">{stat.value}</p>
              </div>
              <div className={`p-3 rounded-lg ${stat.bgColor}`}>
                <stat.icon className={`h-6 w-6 ${stat.color}`} />
              </div>
            </div>
          </CardContent>
        </Card>
      ))}
    </div>
  )
}
