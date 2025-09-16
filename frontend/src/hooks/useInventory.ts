import { useState, useCallback, useEffect } from 'react'
import { useAuth } from '@/context/AuthContext'
import { useCurrency } from './useCurrency'

export interface Product {
  id: string
  name: string
  description?: string
  price: number
  category: string
  sku?: string
  barcode?: string
  isActive: boolean
  weight?: number
  dimensions?: string
  supplierName?: string
  supplierContact?: string
}

export interface InventoryItem {
  id: string
  shopId: string
  shopName: string
  productId: string
  product: Product
  currentStock: number
  reservedStock: number
  availableStock: number
  minimumStock: number
  maximumStock?: number
  reorderPoint: number
  unitCost?: number
  location?: string
  batchNumber?: string
  expiryDate?: string
  status: InventoryStatus
  lastStockUpdate: string
  isLowStock: boolean
  isExpired: boolean
  isExpiringSoon: boolean
}

export type InventoryStatus = 'ACTIVE' | 'INACTIVE' | 'DISCONTINUED' | 'QUARANTINED' | 'EXPIRED'

export interface InventoryHistory {
  id: string
  inventoryId: string
  changeType: 'STOCK_IN' | 'STOCK_OUT' | 'ADJUSTMENT' | 'RETURN' | 'SALE'
  quantityChange: number
  previousStock: number
  newStock: number
  referenceId?: string
  referenceType?: string
  reason?: string
  performedBy?: string
  createdAt: string
}

export interface CreateInventoryRequest {
  productId: string
  currentStock: number
  minimumStock: number
  maximumStock?: number
  reorderPoint: number
  unitCost?: number
  location?: string
  batchNumber?: string
  expiryDate?: string
}

export interface AdjustStockRequest {
  newStock: number
  reason: string
  changeType: 'STOCK_IN' | 'STOCK_OUT' | 'ADJUSTMENT'
}

export interface ReserveStockRequest {
  quantity: number
  referenceId?: string
  referenceType?: string
  reason?: string
}

export interface InventoryFilter {
  status?: InventoryStatus
  isLowStock?: boolean
  isExpired?: boolean
  isExpiringSoon?: boolean
  category?: string
  location?: string
  minStock?: number
  maxStock?: number
  searchQuery?: string
}

export interface InventorySummary {
  totalItems: number
  totalValue: number
  lowStockItems: number
  expiredItems: number
  expiringSoonItems: number
  outOfStockItems: number
  categoryBreakdown: Array<{
    category: string
    itemCount: number
    totalValue: number
  }>
}

export const useInventory = () => {
  const { user } = useAuth()
  const { formatCurrency } = useCurrency()
  const [inventory, setInventory] = useState<InventoryItem[]>([])
  const [inventoryHistory, setInventoryHistory] = useState<InventoryHistory[]>([])
  const [summary, setSummary] = useState<InventorySummary | null>(null)
  const [isLoading, setIsLoading] = useState(false)
  const [error, setError] = useState<string | null>(null)

  // Fetch inventory items
  const fetchInventory = useCallback(async (shopId: string, filter?: InventoryFilter): Promise<InventoryItem[]> => {
    try {
      setIsLoading(true)
      setError(null)

      const queryParams = new URLSearchParams()
      if (filter?.status) queryParams.append('status', filter.status)
      if (filter?.isLowStock !== undefined) queryParams.append('isLowStock', filter.isLowStock.toString())
      if (filter?.isExpired !== undefined) queryParams.append('isExpired', filter.isExpired.toString())
      if (filter?.isExpiringSoon !== undefined) queryParams.append('isExpiringSoon', filter.isExpiringSoon.toString())
      if (filter?.category) queryParams.append('category', filter.category)
      if (filter?.location) queryParams.append('location', filter.location)
      if (filter?.minStock !== undefined) queryParams.append('minStock', filter.minStock.toString())
      if (filter?.maxStock !== undefined) queryParams.append('maxStock', filter.maxStock.toString())
      if (filter?.searchQuery) queryParams.append('search', filter.searchQuery)

      const response = await fetch(`/api/v1/shops/${shopId}/inventory?${queryParams}`, {
        headers: {
          'Authorization': `Bearer ${localStorage.getItem('token')}`,
          'Content-Type': 'application/json'
        }
      })

      if (!response.ok) {
        throw new Error('Failed to fetch inventory')
      }

      const data = await response.json()
      setInventory(data)
      return data
    } catch (err) {
      const errorMessage = err instanceof Error ? err.message : 'Unknown error occurred'
      setError(errorMessage)
      return []
    } finally {
      setIsLoading(false)
    }
  }, [])

  // Create new inventory item
  const createInventoryItem = useCallback(async (shopId: string, request: CreateInventoryRequest): Promise<InventoryItem | null> => {
    try {
      setIsLoading(true)
      setError(null)

      const response = await fetch(`/api/v1/shops/${shopId}/inventory`, {
        method: 'POST',
        headers: {
          'Authorization': `Bearer ${localStorage.getItem('token')}`,
          'Content-Type': 'application/json'
        },
        body: JSON.stringify(request)
      })

      if (!response.ok) {
        throw new Error('Failed to create inventory item')
      }

      const item = await response.json()
      setInventory(prevInventory => [item, ...prevInventory])
      return item
    } catch (err) {
      const errorMessage = err instanceof Error ? err.message : 'Unknown error occurred'
      setError(errorMessage)
      return null
    } finally {
      setIsLoading(false)
    }
  }, [])

  // Adjust stock levels
  const adjustStock = useCallback(async (inventoryId: string, request: AdjustStockRequest): Promise<InventoryItem | null> => {
    try {
      setIsLoading(true)
      setError(null)

      const response = await fetch(`/api/v1/inventory/${inventoryId}/adjust`, {
        method: 'POST',
        headers: {
          'Authorization': `Bearer ${localStorage.getItem('token')}`,
          'Content-Type': 'application/json'
        },
        body: JSON.stringify(request)
      })

      if (!response.ok) {
        throw new Error('Failed to adjust stock')
      }

      const updatedItem = await response.json()
      setInventory(prevInventory =>
        prevInventory.map(item =>
          item.id === inventoryId ? updatedItem : item
        )
      )
      return updatedItem
    } catch (err) {
      const errorMessage = err instanceof Error ? err.message : 'Unknown error occurred'
      setError(errorMessage)
      return null
    } finally {
      setIsLoading(false)
    }
  }, [])

  // Reserve stock
  const reserveStock = useCallback(async (inventoryId: string, request: ReserveStockRequest): Promise<boolean> => {
    try {
      setIsLoading(true)
      setError(null)

      const response = await fetch(`/api/v1/inventory/${inventoryId}/reserve`, {
        method: 'POST',
        headers: {
          'Authorization': `Bearer ${localStorage.getItem('token')}`,
          'Content-Type': 'application/json'
        },
        body: JSON.stringify(request)
      })

      if (!response.ok) {
        throw new Error('Failed to reserve stock')
      }

      const updatedItem = await response.json()
      setInventory(prevInventory =>
        prevInventory.map(item =>
          item.id === inventoryId ? updatedItem : item
        )
      )
      return true
    } catch (err) {
      const errorMessage = err instanceof Error ? err.message : 'Unknown error occurred'
      setError(errorMessage)
      return false
    } finally {
      setIsLoading(false)
    }
  }, [])

  // Release reserved stock
  const releaseReservedStock = useCallback(async (inventoryId: string, quantity: number): Promise<boolean> => {
    try {
      setIsLoading(true)
      setError(null)

      const response = await fetch(`/api/v1/inventory/${inventoryId}/release`, {
        method: 'POST',
        headers: {
          'Authorization': `Bearer ${localStorage.getItem('token')}`,
          'Content-Type': 'application/json'
        },
        body: JSON.stringify({ quantity })
      })

      if (!response.ok) {
        throw new Error('Failed to release reserved stock')
      }

      const updatedItem = await response.json()
      setInventory(prevInventory =>
        prevInventory.map(item =>
          item.id === inventoryId ? updatedItem : item
        )
      )
      return true
    } catch (err) {
      const errorMessage = err instanceof Error ? err.message : 'Unknown error occurred'
      setError(errorMessage)
      return false
    } finally {
      setIsLoading(false)
    }
  }, [])

  // Get inventory history
  const fetchInventoryHistory = useCallback(async (inventoryId: string): Promise<InventoryHistory[]> => {
    try {
      setIsLoading(true)
      setError(null)

      const response = await fetch(`/api/v1/inventory/${inventoryId}/history`, {
        headers: {
          'Authorization': `Bearer ${localStorage.getItem('token')}`,
          'Content-Type': 'application/json'
        }
      })

      if (!response.ok) {
        throw new Error('Failed to fetch inventory history')
      }

      const history = await response.json()
      setInventoryHistory(history)
      return history
    } catch (err) {
      const errorMessage = err instanceof Error ? err.message : 'Unknown error occurred'
      setError(errorMessage)
      return []
    } finally {
      setIsLoading(false)
    }
  }, [])

  // Get inventory summary
  const fetchInventorySummary = useCallback(async (shopId: string): Promise<InventorySummary | null> => {
    try {
      setIsLoading(true)
      setError(null)

      const response = await fetch(`/api/v1/shops/${shopId}/inventory/summary`, {
        headers: {
          'Authorization': `Bearer ${localStorage.getItem('token')}`,
          'Content-Type': 'application/json'
        }
      })

      if (!response.ok) {
        throw new Error('Failed to fetch inventory summary')
      }

      const summaryData = await response.json()
      setSummary(summaryData)
      return summaryData
    } catch (err) {
      const errorMessage = err instanceof Error ? err.message : 'Unknown error occurred'
      setError(errorMessage)
      return null
    } finally {
      setIsLoading(false)
    }
  }, [])

  // Update inventory item settings
  const updateInventorySettings = useCallback(async (inventoryId: string, updates: Partial<CreateInventoryRequest>): Promise<InventoryItem | null> => {
    try {
      setIsLoading(true)
      setError(null)

      const response = await fetch(`/api/v1/inventory/${inventoryId}`, {
        method: 'PUT',
        headers: {
          'Authorization': `Bearer ${localStorage.getItem('token')}`,
          'Content-Type': 'application/json'
        },
        body: JSON.stringify(updates)
      })

      if (!response.ok) {
        throw new Error('Failed to update inventory settings')
      }

      const updatedItem = await response.json()
      setInventory(prevInventory =>
        prevInventory.map(item =>
          item.id === inventoryId ? updatedItem : item
        )
      )
      return updatedItem
    } catch (err) {
      const errorMessage = err instanceof Error ? err.message : 'Unknown error occurred'
      setError(errorMessage)
      return null
    } finally {
      setIsLoading(false)
    }
  }, [])

  // Update inventory status
  const updateInventoryStatus = useCallback(async (inventoryId: string, status: InventoryStatus): Promise<boolean> => {
    try {
      setIsLoading(true)
      setError(null)

      const response = await fetch(`/api/v1/inventory/${inventoryId}/status`, {
        method: 'PATCH',
        headers: {
          'Authorization': `Bearer ${localStorage.getItem('token')}`,
          'Content-Type': 'application/json'
        },
        body: JSON.stringify({ status })
      })

      if (!response.ok) {
        throw new Error('Failed to update inventory status')
      }

      const updatedItem = await response.json()
      setInventory(prevInventory =>
        prevInventory.map(item =>
          item.id === inventoryId ? updatedItem : item
        )
      )
      return true
    } catch (err) {
      const errorMessage = err instanceof Error ? err.message : 'Unknown error occurred'
      setError(errorMessage)
      return false
    } finally {
      setIsLoading(false)
    }
  }, [])

  // Get low stock alerts
  const getLowStockAlerts = useCallback(async (shopId: string): Promise<InventoryItem[]> => {
    try {
      const response = await fetch(`/api/v1/shops/${shopId}/inventory/low-stock`, {
        headers: {
          'Authorization': `Bearer ${localStorage.getItem('token')}`,
          'Content-Type': 'application/json'
        }
      })

      if (!response.ok) {
        throw new Error('Failed to fetch low stock alerts')
      }

      return await response.json()
    } catch (err) {
      const errorMessage = err instanceof Error ? err.message : 'Unknown error occurred'
      setError(errorMessage)
      return []
    }
  }, [])

  // Get expiring items
  const getExpiringItems = useCallback(async (shopId: string, daysAhead: number = 30): Promise<InventoryItem[]> => {
    try {
      const response = await fetch(`/api/v1/shops/${shopId}/inventory/expiring?daysAhead=${daysAhead}`, {
        headers: {
          'Authorization': `Bearer ${localStorage.getItem('token')}`,
          'Content-Type': 'application/json'
        }
      })

      if (!response.ok) {
        throw new Error('Failed to fetch expiring items')
      }

      return await response.json()
    } catch (err) {
      const errorMessage = err instanceof Error ? err.message : 'Unknown error occurred'
      setError(errorMessage)
      return []
    }
  }, [])

  // Export inventory data
  const exportInventory = useCallback(async (shopId: string, format: 'csv' | 'excel' = 'csv'): Promise<string | null> => {
    try {
      setIsLoading(true)
      setError(null)

      const response = await fetch(`/api/v1/shops/${shopId}/inventory/export?format=${format}`, {
        headers: {
          'Authorization': `Bearer ${localStorage.getItem('token')}`
        }
      })

      if (!response.ok) {
        throw new Error('Failed to export inventory')
      }

      const blob = await response.blob()
      const url = window.URL.createObjectURL(blob)
      return url
    } catch (err) {
      const errorMessage = err instanceof Error ? err.message : 'Unknown error occurred'
      setError(errorMessage)
      return null
    } finally {
      setIsLoading(false)
    }
  }, [])

  // Clear error state
  const clearError = useCallback(() => {
    setError(null)
  }, [])

  // Permission checks
  const canManageInventory = user?.roles.some(role =>
    ['ROLE_SHOP_OWNER', 'ROLE_SHOP_MANAGER', 'ROLE_INVENTORY_MANAGER'].includes(role)
  ) || false

  const canViewInventory = user?.roles.some(role =>
    ['ROLE_SHOP_OWNER', 'ROLE_SHOP_MANAGER', 'ROLE_INVENTORY_MANAGER', 'ROLE_CASHIER', 'ROLE_SHOP_EMPLOYEE'].includes(role)
  ) || false

  const canAdjustStock = user?.roles.some(role =>
    ['ROLE_SHOP_OWNER', 'ROLE_SHOP_MANAGER', 'ROLE_INVENTORY_MANAGER'].includes(role)
  ) || false

  return {
    // State
    inventory,
    inventoryHistory,
    summary,
    isLoading,
    error,
    user,

    // Permissions
    canManageInventory,
    canViewInventory,
    canAdjustStock,

    // Operations
    fetchInventory,
    createInventoryItem,
    adjustStock,
    reserveStock,
    releaseReservedStock,
    fetchInventoryHistory,
    fetchInventorySummary,
    updateInventorySettings,
    updateInventoryStatus,
    getLowStockAlerts,
    getExpiringItems,
    exportInventory,

    // Utility
    clearError
  }
}

export default useInventory