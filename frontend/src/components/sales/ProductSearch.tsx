import { Badge } from '@/components/ui/badge'
import { Button } from '@/components/ui/button'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import { Input } from '@/components/ui/input'
import { LoadingSpinner } from '@/components/ui/loading-spinner'
import { useCurrency } from '@/hooks/useCurrency'
import { useProductSearch, useProducts } from '@/hooks/useProducts'
import { useSales, } from '@/hooks/useSales'
import { Product, ProductStatus } from '@/types/api'
import { debounce } from 'lodash'
import { PackageIcon, PlusIcon, SearchIcon } from 'lucide-react'
import React, { useCallback, useEffect, useState } from 'react'

interface ProductSearchProps {
  onProductSelect: (product: Product) => void
  shopId?: string // Optional shop ID to search products in
}

export const ProductSearch: React.FC<ProductSearchProps> = ({ onProductSelect, shopId }) => {
  const { searchProducts, isLoading } = useSales()
    const [search, setSearch] = useState("");
    const [searchQuery, setSearchQuery] = useState("");

    // Fetch all products when not searching
    const { products: allProducts = [], isLoading: loadingAll } = useProducts({ 
      page: 0, 
      size: 50,
      status: 'ACTIVE'
    })
    
    // Search products when query exists
    const { data: searchResults = [], isLoading: loadingSearch } = useProductSearch(search, shopId);
    
    // Use search results if searching, otherwise show all products
    const productsData = search ? searchResults : allProducts
    const currentLoading = search ? loadingSearch : loadingAll
  
  const { formatCurrency } = useCurrency()

  const [hasSearched, setHasSearched] = useState(false)

  const debouncedSearch = useCallback(
    debounce(async (query: string) => {
      if (query.trim().length >= 2) {
        setHasSearched(true);
        setSearch(query);
      } else {
        setHasSearched(false)
      }
    }, 300),
    [searchProducts]
  )

  useEffect(() => {
    debouncedSearch(searchQuery)
    return () => {
      debouncedSearch.cancel()
    }
  }, [searchQuery, debouncedSearch])

  const handleProductSelect = (product: Product) => {
    onProductSelect(product)
    setSearchQuery('')
    setHasSearched(false)
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
        {currentLoading && (
          <div className="flex items-center justify-center py-8">
            <LoadingSpinner size="md" />
            <span className="ml-2 text-muted-foreground">{search ? 'Searching' : 'Loading'} products...</span>
          </div>
        )}

        {/* Product List */}
        {!currentLoading && (
          <div className="space-y-2 max-h-96 overflow-y-auto">
            {productsData.length === 0 ? (
              <div className="text-center py-8 text-muted-foreground">
                <PackageIcon className="h-12 w-12 mx-auto mb-2 text-muted-foreground/40" />
                <p>No products found</p>
                <p className="text-sm">Try a different search term</p>
              </div>
            ) : (
              productsData.map((product) => (
                <div
                  key={product.id}
                  className="border rounded-lg p-4 hover:bg-muted/50 transition-colors"
                >
                  <div className="flex items-center justify-between">
                    <div className="flex-1 min-w-0">
                      <div className="flex items-center space-x-2">
                        <h3 className="font-medium text-foreground truncate">
                          {product.name}
                        </h3>
                        {getStockBadge(product.availableStock || 0)}
                        {product.status !== ProductStatus.ACTIVE && (
                          <Badge variant="outline" className="text-red-600 dark:text-red-400 border-red-600 dark:border-red-400">
                            Inactive
                          </Badge>
                        )}
                      </div>

                      {product.description && (
                        <p className="text-sm text-muted-foreground mt-1 truncate">
                          {product.description}
                        </p>
                      )}

                      <div className="flex items-center space-x-4 mt-2 text-sm text-muted-foreground">
                        <span>Category: {product.category}</span>
                        {product.barcode && (
                          <span>Barcode: {product.barcode}</span>
                        )}
                      </div>
                    </div>

                    <div className="flex items-center space-x-3 ml-4">
                      <div className="text-right">
                        <div className="text-lg font-semibold text-foreground">
                          {formatCurrency(product.price)}
                        </div>
                        <div className="text-xs text-muted-foreground">
                          Stock: {product.availableStock || 0}
                        </div>
                      </div>

                      <Button
                        onClick={() => onProductSelect(product)}
                        disabled={!product.availableStock || product.availableStock <= 0}
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
        {!currentLoading && productsData.length > 0 && (
          <div className="text-xs text-muted-foreground text-center py-2 border-t">
            {search ? `Found ${productsData.length} result(s)` : `Showing ${productsData.length} available products`}
          </div>
        )}
      </CardContent>
    </Card>
  )
}

export default ProductSearch