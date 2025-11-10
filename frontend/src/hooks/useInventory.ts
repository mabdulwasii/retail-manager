import { useState, useCallback, useEffect } from 'react'
import { useAuth } from '@/context/ManualAuthContext'
import { useCurrency } from './useCurrency'
import { api } from '@/services/api'

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
  productName?: string
  productSku?: string
  productBarcode?: string
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
  changeType?: 'STOCK_IN' | 'STOCK_OUT' | 'ADJUSTMENT'
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

      const data = await api.get<any>(`/shops/${shopId}/inventory?${queryParams}`)
      // Handle paginated response
      const items = Array.isArray(data) ? data : (data?.content || [])
      setInventory(items)
      return items
    } catch (err) {
      const errorMessage = err instanceof Error ? err.message : 'Unknown error occurred'
      setError(errorMessage)
      return []
    } finally {
      setIsLoading(false)
    }
  }, [])

  // Fetch a single inventory item by ID
  const fetchInventoryItem = useCallback(async (inventoryId: string): Promise<InventoryItem | null> => {
    try {
      setIsLoading(true)
      setError(null)

      const item = await api.get<InventoryItem>(`/inventory/${inventoryId}`)
      // Update the inventory array with the fetched item
      setInventory(prevInventory => {
        const existing = prevInventory.find(i => i.id === inventoryId)
        if (existing) {
          return prevInventory.map(i => i.id === inventoryId ? item : i)
        }
        return [item, ...prevInventory]
      })
      return item
    } catch (err) {
      const errorMessage = err instanceof Error ? err.message : 'Unknown error occurred'
      setError(errorMessage)
      return null
    } finally {
      setIsLoading(false)
    }
  }, [])

  // Create new inventory item
  const createInventoryItem = useCallback(async (shopId: string, request: CreateInventoryRequest): Promise<InventoryItem | null> => {
    try {
      setIsLoading(true)
      setError(null)

      const item = await api.post<InventoryItem>(`/shops/${shopId}/inventory`, request)
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

      const updatedItem = await api.put<InventoryItem>(`/inventory/${inventoryId}/adjust-stock`, request)
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

      const updatedItem = await api.post<InventoryItem>(`/inventory/${inventoryId}/reserve`, request)
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

      const updatedItem = await api.post<InventoryItem>(`/inventory/${inventoryId}/release`, { quantity })
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

      const data = await api.get<any>(`/inventory/${inventoryId}/history`)
      // Handle paginated response
      const history = Array.isArray(data) ? data : (data?.content || [])
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

      const summaryData = await api.get<InventorySummary>(`/shops/${shopId}/inventory/summary`)
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

      const updatedItem = await api.put<InventoryItem>(`/inventory/${inventoryId}`, updates)
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

      const updatedItem = await api.patch<InventoryItem>(`/inventory/${inventoryId}/status`, { status })
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
      const data = await api.get<any>(`/shops/${shopId}/inventory/low-stock`)
      // Handle paginated response
      return Array.isArray(data) ? data : (data?.content || [])
    } catch (err) {
      const errorMessage = err instanceof Error ? err.message : 'Unknown error occurred'
      setError(errorMessage)
      return []
    }
  }, [])

  // Get expiring items
  const getExpiringItems = useCallback(async (shopId: string, daysAhead: number = 30): Promise<InventoryItem[]> => {
    try {
      const data = await api.get<any>(`/shops/${shopId}/inventory/expiring?daysAhead=${daysAhead}`)
      // Handle paginated response
      return Array.isArray(data) ? data : (data?.content || [])
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

      const blob = await api.getBlob(`/shops/${shopId}/inventory/export?format=${format}`)
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

  // Permission checks based on backend permission matrix
  const { hasPermission } = useAuth();
  const canManageInventory = hasPermission('INVENTORY_CREATE') || hasPermission('INVENTORY_UPDATE');  // MANAGER and above
  const canViewInventory = hasPermission('INVENTORY_LIST');        // EMPLOYEE and above
  const canAdjustStock = hasPermission('INVENTORY_UPDATE');        // MANAGER and above
  const canDeleteInventory = hasPermission('INVENTORY_DELETE');    // OWNER and above
  const canViewHistory = hasPermission('INVENTORY_HISTORY_VIEW');  // MANAGER and above

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
    canDeleteInventory,
    canViewHistory,

    // Operations
    fetchInventory,
    fetchInventoryItem,
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