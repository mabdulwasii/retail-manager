import React from 'react'
import { Button } from '@/components/ui/button'
import { Badge } from '@/components/ui/badge'
import { InventoryItem } from '@/hooks/useInventory'
import { useCurrency } from '@/hooks/useCurrency'
import {
  PackageIcon,
  EditIcon,
  AlertTriangleIcon,
  TrendingDownIcon,
  ClockIcon,
  XCircleIcon,
  CheckCircleIcon,
  MapPinIcon,
  CalendarIcon,
  HashIcon
} from 'lucide-react'

interface InventoryListProps {
  items: InventoryItem[]
  onStockAdjustment?: (inventoryId: string) => void
  showActions?: boolean
  compact?: boolean
  highlightLowStock?: boolean
  highlightExpiring?: boolean
}

export const InventoryList: React.FC<InventoryListProps> = ({
  items,
  onStockAdjustment,
  showActions = false,
  compact = false,
  highlightLowStock = false,
  highlightExpiring = false
}) => {
  const { formatCurrency } = useCurrency()

  const getStatusBadge = (status: string) => {
    const variants: Record<string, { class: string; text: string; icon?: React.ReactNode }> = {
      ACTIVE: {
        class: 'bg-green-100 text-green-800',
        text: 'Active',
        icon: <CheckCircleIcon className="h-3 w-3" />
      },
      INACTIVE: {
        class: 'bg-gray-100 text-gray-800',
        text: 'Inactive',
        icon: <XCircleIcon className="h-3 w-3" />
      },
      DISCONTINUED: {
        class: 'bg-yellow-100 text-yellow-800',
        text: 'Discontinued',
        icon: <AlertTriangleIcon className="h-3 w-3" />
      },
      QUARANTINED: {
        class: 'bg-red-100 text-red-800',
        text: 'Quarantined',
        icon: <AlertTriangleIcon className="h-3 w-3" />
      },
      EXPIRED: {
        class: 'bg-red-100 text-red-800',
        text: 'Expired',
        icon: <XCircleIcon className="h-3 w-3" />
      }
    }

    const variant = variants[status] || variants.ACTIVE
    return (
      <Badge className={variant.class}>
        <span className="flex items-center space-x-1">
          {variant.icon}
          <span>{variant.text}</span>
        </span>
      </Badge>
    )
  }

  const getStockLevelBadge = (item: InventoryItem) => {
    if (item.availableStock === 0) {
      return (
        <Badge className="bg-red-100 text-red-800">
          <XCircleIcon className="h-3 w-3 mr-1" />
          Out of Stock
        </Badge>
      )
    } else if (item.isLowStock) {
      return (
        <Badge className="bg-orange-100 text-orange-800">
          <TrendingDownIcon className="h-3 w-3 mr-1" />
          Low Stock
        </Badge>
      )
    } else {
      return (
        <Badge className="bg-green-100 text-green-800">
          <CheckCircleIcon className="h-3 w-3 mr-1" />
          In Stock
        </Badge>
      )
    }
  }

  const formatDate = (dateString?: string) => {
    if (!dateString) return null
    return new Date(dateString).toLocaleDateString('en-US', {
      year: 'numeric',
      month: 'short',
      day: 'numeric'
    })
  }

  const isHighlighted = (item: InventoryItem) => {
    if (highlightLowStock && item.isLowStock) return true
    if (highlightExpiring && (item.isExpiringSoon || item.isExpired)) return true
    return false
  }

  if (items.length === 0) {
    return (
      <div className="text-center py-8 text-gray-500">
        <PackageIcon className="h-12 w-12 mx-auto mb-2 text-gray-300" />
        <p>No inventory items found</p>
        <p className="text-sm">Items will appear here when added</p>
      </div>
    )
  }

  return (
    <div className="space-y-3">
      {items.map((item) => (
        <div
          key={item.id}
          className={`border rounded-lg p-4 transition-colors ${
            isHighlighted(item)
              ? 'border-orange-300 bg-orange-50'
              : 'hover:bg-gray-50'
          }`}
        >
          <div className="flex items-start justify-between">
            <div className="flex-1 min-w-0">
              {/* Header */}
              <div className="flex items-center space-x-3 mb-2">
                <h3 className="font-medium text-gray-900 truncate">
                  {item.product.name}
                </h3>
                {getStatusBadge(item.status)}
                {getStockLevelBadge(item)}

                {item.isExpired && (
                  <Badge className="bg-red-100 text-red-800">
                    <AlertTriangleIcon className="h-3 w-3 mr-1" />
                    Expired
                  </Badge>
                )}

                {item.isExpiringSoon && !item.isExpired && (
                  <Badge className="bg-yellow-100 text-yellow-800">
                    <ClockIcon className="h-3 w-3 mr-1" />
                    Expiring Soon
                  </Badge>
                )}
              </div>

              {/* Product Details */}
              {!compact && item.product.description && (
                <p className="text-sm text-gray-600 mb-2 line-clamp-2">
                  {item.product.description}
                </p>
              )}

              {/* Stock Information Grid */}
              <div className={`grid ${
                compact ? 'grid-cols-2 md:grid-cols-4' : 'grid-cols-2 md:grid-cols-3 lg:grid-cols-6'
              } gap-3 text-sm`}>
                <div>
                  <span className="text-gray-600">Available:</span>
                  <div className="font-semibold text-green-600">
                    {item.availableStock}{item.baseUnit ? ` ${item.baseUnit}s` : ''}
                  </div>
                </div>

                <div>
                  <span className="text-gray-600">Total Stock:</span>
                  <div className="font-medium">
                    {item.currentStock}{item.baseUnit ? ` ${item.baseUnit}s` : ''}
                  </div>
                </div>

                {item.reservedStock > 0 && (
                  <div>
                    <span className="text-gray-600">Reserved:</span>
                    <div className="font-medium text-orange-600">
                      {item.reservedStock}{item.baseUnit ? ` ${item.baseUnit}s` : ''}
                    </div>
                  </div>
                )}

                <div>
                  <span className="text-gray-600">Min Stock:</span>
                  <div className="font-medium">
                    {item.minimumStock}{item.baseUnit ? ` ${item.baseUnit}s` : ''}
                  </div>
                </div>

                <div>
                  <span className="text-gray-600">Cost Price:</span>
                  <div className="font-medium text-blue-600">
                    {formatCurrency(item.costPrice)}
                  </div>
                </div>

                <div>
                  <span className="text-gray-600">Selling Price:</span>
                  <div className="font-medium text-green-600">
                    {formatCurrency(item.sellingPrice)}
                  </div>
                </div>
              </div>

              {/* Profit Margin */}
              {!compact && (
                <div className="mt-2 text-sm">
                  <span className="text-gray-600">Profit Margin: </span>
                  <span className="font-semibold text-purple-600">
                    {formatCurrency(item.sellingPrice - item.costPrice)}
                    {' '}({((item.sellingPrice - item.costPrice) / item.sellingPrice * 100).toFixed(1)}%)
                  </span>
                </div>
              )}

              {/* Additional Details */}
              {!compact && (
                <div className="mt-3 flex flex-wrap gap-4 text-xs text-gray-500">
                  {item.product.sku && (
                    <div className="flex items-center space-x-1">
                      <HashIcon className="h-3 w-3" />
                      <span>SKU: {item.product.sku}</span>
                    </div>
                  )}

                  {item.location && (
                    <div className="flex items-center space-x-1">
                      <MapPinIcon className="h-3 w-3" />
                      <span>Location: {item.location}</span>
                    </div>
                  )}

                  {item.batchNumber && (
                    <div className="flex items-center space-x-1">
                      <PackageIcon className="h-3 w-3" />
                      <span>Batch: {item.batchNumber}</span>
                    </div>
                  )}

                  {item.expiryDate && (
                    <div className="flex items-center space-x-1">
                      <CalendarIcon className="h-3 w-3" />
                      <span>Expires: {formatDate(item.expiryDate)}</span>
                    </div>
                  )}
                </div>
              )}

              {/* Alerts and Warnings */}
              {(item.isLowStock || item.isExpired || item.isExpiringSoon) && (
                <div className="mt-3 space-y-1">
                  {item.isLowStock && (
                    <div className="text-xs text-orange-600 flex items-center space-x-1">
                      <TrendingDownIcon className="h-3 w-3" />
                      <span>
                        Stock level ({item.availableStock}) is below minimum ({item.minimumStock})
                      </span>
                    </div>
                  )}

                  {item.isExpired && (
                    <div className="text-xs text-red-600 flex items-center space-x-1">
                      <AlertTriangleIcon className="h-3 w-3" />
                      <span>
                        Item expired on {formatDate(item.expiryDate)}
                      </span>
                    </div>
                  )}

                  {item.isExpiringSoon && !item.isExpired && (
                    <div className="text-xs text-yellow-600 flex items-center space-x-1">
                      <ClockIcon className="h-3 w-3" />
                      <span>
                        Item expires on {formatDate(item.expiryDate)}
                      </span>
                    </div>
                  )}
                </div>
              )}
            </div>

            {/* Actions */}
            {showActions && (
              <div className="flex space-x-2 ml-4">
                {onStockAdjustment && (
                  <Button
                    variant="outline"
                    size="sm"
                    onClick={() => onStockAdjustment(item.id)}
                    disabled={item.status !== 'ACTIVE'}
                  >
                    <EditIcon className="h-4 w-4" />
                  </Button>
                )}
              </div>
            )}
          </div>
        </div>
      ))}
    </div>
  )
}

export default InventoryList