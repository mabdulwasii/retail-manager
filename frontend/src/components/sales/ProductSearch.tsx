import { Badge } from '@/components/ui/badge'
import { Button } from '@/components/ui/button'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import { Input } from '@/components/ui/input'
import { LoadingSpinner } from '@/components/ui/loading-spinner'
import { useCurrency } from '@/hooks/useCurrency'
import { useInventory, InventoryItem } from '@/hooks/useInventory'
import { Product, ProductUnitDefinition, InventoryUnitPrice } from '@/types/api'
import { debounce } from 'lodash'
import { PackageIcon, PlusIcon, SearchIcon } from 'lucide-react'
import React, { useCallback, useEffect, useState } from 'react'

interface ProductSearchProps {
  onProductSelect: (
    product: Product,
    inventoryId: string,
    sellingPrice: number,
    unitDefinitions?: ProductUnitDefinition[],
    unitPrices?: InventoryUnitPrice[]
  ) => void
  shopId?: string // Optional shop ID to search products in
}

export const ProductSearch: React.FC<ProductSearchProps> = ({ onProductSelect, shopId }) => {
  const { fetchInventory } = useInventory()
  const [inventoryItems, setInventoryItems] = useState<InventoryItem[]>([])
  const [searchQuery, setSearchQuery] = useState("")
  const [isLoading, setIsLoading] = useState(false)
  
  const { formatCurrency } = useCurrency()

  // Fetch inventory items
  const loadInventory = useCallback(async (query?: string) => {
    if (!shopId) return
    
    setIsLoading(true)
    const items = await fetchInventory(shopId, {
      status: 'ACTIVE',
      searchQuery: query || undefined
    })
    // Filter only items with available stock
    setInventoryItems(items.filter(item => item.availableStock > 0))
    setIsLoading(false)
  }, [shopId, fetchInventory])

  // Debounced search
  const debouncedSearch = useCallback(
    debounce((query: string) => {
      loadInventory(query)
    }, 300),
    [loadInventory]
  )

  useEffect(() => {
    // Load initial inventory on mount
    loadInventory()
  }, [loadInventory])

  useEffect(() => {
    if (searchQuery.trim().length >= 2) {
      debouncedSearch(searchQuery)
    } else if (searchQuery.trim().length === 0) {
      loadInventory()
    }
    return () => {
      debouncedSearch.cancel()
    }
  }, [searchQuery, debouncedSearch, loadInventory])

  const handleInventorySelect = (item: InventoryItem) => {
    // Convert inventory item to product format for backward compatibility
    const product: Product = {
      id: item.productId,
      name: item.productName || '',
      sku: item.productSku || '',
      categoryId: '',
      status: 'ACTIVE' as any,
      availableStock: item.availableStock,
      createdAt: '',
      updatedAt: ''
    }
    // Pass unit definitions and unit prices for multi-unit POS support
    onProductSelect(product, item.id, item.sellingPrice, item.unitDefinitions, item.unitPrices)
    setSearchQuery('')
  }

  const getStockBadge = (stock: number) => {
    if (stock === 0) {
      return <Badge variant="destructive">Out of Stock</Badge>
    } else if (stock <= 10) {
      return <Badge variant="outline" className="text-orange-600 border-orange-600">Low Stock ({stock})</Badge>
    } else {
      return <Badge variant="success">In Stock ({stock})</Badge>
    }
  }

  return (
    <Card>
      <CardHeader>
        <CardTitle className="flex items-center space-x-2">
          <SearchIcon className="h-5 w-5" />
          <span>Product Search</span>
        </CardTitle>
      </CardHeader>
      <CardContent className="space-y-4">
        {/* Search Input */}
        <div className="relative">
          <SearchIcon className="absolute left-3 top-3 h-4 w-4 text-gray-400" />
          <Input
            type="text"
            placeholder="Search products by name, category, or barcode..."
            value={searchQuery}
            onChange={(e) => setSearchQuery(e.target.value)}
            className="pl-10"
          />
        </div>

        {/* Loading State */}
        {isLoading && (
          <div className="flex items-center justify-center py-8">
            <LoadingSpinner size="md" />
            <span className="ml-2 text-muted-foreground">Loading inventory...</span>
          </div>
        )}

        {/* Inventory List */}
        {!isLoading && (
          <div className="space-y-2 max-h-96 overflow-y-auto">
            {inventoryItems.length === 0 ? (
              <div className="text-center py-8 text-muted-foreground">
                <PackageIcon className="h-12 w-12 mx-auto mb-2 text-muted-foreground/40" />
                <p>No items in stock</p>
                <p className="text-sm">Try a different search term</p>
              </div>
            ) : (
              inventoryItems.map((item) => (
                <div
                  key={item.id}
                  className="border rounded-lg p-4 hover:bg-muted/50 transition-colors"
                >
                  <div className="flex items-center justify-between">
                    <div className="flex-1 min-w-0">
                      <div className="flex items-center space-x-2">
                        <h3 className="font-medium text-foreground truncate">
                          {item.productName}
                        </h3>
                        {getStockBadge(item.availableStock)}
                        {item.status !== 'ACTIVE' && (
                          <Badge variant="outline" className="text-red-600 dark:text-red-400 border-red-600 dark:border-red-400">
                            {item.status}
                          </Badge>
                        )}
                      </div>

                      <div className="flex items-center space-x-4 mt-2 text-sm text-muted-foreground">
                        <span>SKU: {item.productSku}</span>
                        {item.location && (
                          <span>Location: {item.location}</span>
                        )}
                        {item.batchNumber && (
                          <span>Batch: {item.batchNumber}</span>
                        )}
                      </div>
                    </div>

                    <div className="flex items-center space-x-3 ml-4">
                      <div className="text-right">
                        <div className="text-lg font-semibold text-foreground">
                          {formatCurrency(item.sellingPrice)}
                        </div>
                        <div className="text-xs text-muted-foreground">
                          Stock: {item.availableStock}
                        </div>
                      </div>

                      <Button
                        onClick={() => handleInventorySelect(item)}
                        disabled={item.availableStock <= 0}
                        size="sm"
                        className="shadow-sm"
                      >
                        <PlusIcon className="h-4 w-4 mr-1" />
                        <span>Add</span>
                      </Button>
                    </div>
                  </div>
                </div>
              ))
            )}
          </div>
        )}

        {/* Showing Info */}
        {!isLoading && inventoryItems.length > 0 && (
          <div className="text-xs text-muted-foreground text-center py-2 border-t">
            {searchQuery ? `Found ${inventoryItems.length} result(s)` : `Showing ${inventoryItems.length} items in stock`}
          </div>
        )}
      </CardContent>
    </Card>
  )
}

export default ProductSearch