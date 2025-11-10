import React, { useState, useEffect } from 'react'
import { useNavigate } from 'react-router-dom'
import { ArrowLeft, Package, Search, AlertCircle } from 'lucide-react'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import { Label } from '@/components/ui/label'
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card'
import { useInventory, CreateInventoryRequest } from '@/hooks/useInventory'
import { useAuth } from '@/context/ManualAuthContext'
import { useProducts } from '@/hooks/useProducts'
import { toast } from 'sonner'
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select'

export const CreateInventoryPage: React.FC = () => {
  const navigate = useNavigate()
  const { user } = useAuth()
  const { createInventoryItem, isLoading, canManageInventory } = useInventory()
  const { products, isLoading: productsLoading } = useProducts()

  // Check if user has permission to create inventory
  useEffect(() => {
    if (!canManageInventory) {
      navigate('/inventory')
    }
  }, [canManageInventory, navigate])

  if (!canManageInventory) {
    return null
  }

  const [formData, setFormData] = useState({
    productId: '',
    currentStock: '',
    minimumStock: '',
    maximumStock: '',
    reorderPoint: '',
    unitCost: '',
    location: '',
    batchNumber: '',
    expiryDate: ''
  })

  const [errors, setErrors] = useState<Record<string, string>>({})
  const [productSearch, setProductSearch] = useState('')

  console.log('product', products)

   useEffect(() => {
    console.log('producte', products,productsLoading)
  }, [products,productsLoading])

  const filteredProducts = products?.filter(product =>
    product.name.toLowerCase().includes(productSearch.toLowerCase()) ||
    product.sku?.toLowerCase().includes(productSearch.toLowerCase())
  )

  useEffect(() => {
    console.log('product', products)
  }, [products])
  const selectedProduct = products?.find(p => p.id === formData.productId)

  const handleInputChange = (field: string, value: string) => {
    setFormData(prev => ({ ...prev, [field]: value }))
    // Clear error when user types
    if (errors[field]) {
      setErrors(prev => ({ ...prev, [field]: '' }))
    }
  }

  const validateForm = () => {
    const newErrors: Record<string, string> = {}

    if (!formData.productId) {
      newErrors.productId = 'Please select a product'
    }

    if (!formData.currentStock || formData.currentStock.trim() === '') {
      newErrors.currentStock = 'Current stock is required'
    } else {
      const stock = parseInt(formData.currentStock, 10)
      if (isNaN(stock) || stock < 0) {
        newErrors.currentStock = 'Current stock must be a non-negative number'
      }
    }

    if (!formData.minimumStock || formData.minimumStock.trim() === '') {
      newErrors.minimumStock = 'Minimum stock is required'
    } else {
      const minStock = parseInt(formData.minimumStock, 10)
      if (isNaN(minStock) || minStock < 0) {
        newErrors.minimumStock = 'Minimum stock must be a non-negative number'
      }
    }

    if (formData.maximumStock && formData.maximumStock.trim() !== '') {
      const maxStock = parseInt(formData.maximumStock, 10)
      const currentStock = parseInt(formData.currentStock, 10)
      if (isNaN(maxStock) || maxStock < 0) {
        newErrors.maximumStock = 'Maximum stock must be a non-negative number'
      } else if (!isNaN(currentStock) && maxStock < currentStock) {
        newErrors.maximumStock = 'Maximum stock cannot be less than current stock'
      }
    }

    if (!formData.reorderPoint || formData.reorderPoint.trim() === '') {
      newErrors.reorderPoint = 'Reorder point is required'
    } else {
      const reorderPoint = parseInt(formData.reorderPoint, 10)
      if (isNaN(reorderPoint) || reorderPoint < 0) {
        newErrors.reorderPoint = 'Reorder point must be a non-negative number'
      }
    }

    if (formData.unitCost && formData.unitCost.trim() !== '') {
      const cost = parseFloat(formData.unitCost)
      if (isNaN(cost) || cost < 0) {
        newErrors.unitCost = 'Unit cost must be a non-negative number'
      }
    }

    if (formData.expiryDate && formData.expiryDate.trim() !== '') {
      const expiryDate = new Date(formData.expiryDate)
      const today = new Date()
      today.setHours(0, 0, 0, 0)
      if (expiryDate <= today) {
        newErrors.expiryDate = 'Expiry date must be in the future'
      }
    }

    setErrors(newErrors)
    return Object.keys(newErrors).length === 0
  }

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()

    if (!validateForm()) {
      toast.error('Please fix the form errors')
      return
    }

    if (!user?.shopId) {
      toast.error('Shop ID not found. Please log in again.')
      return
    }

    const request: CreateInventoryRequest = {
      productId: formData.productId,
      currentStock: parseInt(formData.currentStock, 10),
      minimumStock: parseInt(formData.minimumStock, 10),
      reorderPoint: parseInt(formData.reorderPoint, 10),
      ...(formData.maximumStock != null && {
        maximumStock: parseInt(formData.maximumStock, 10),
      }),
      ...(formData.unitCost != null && {
        unitCost: parseFloat(formData.unitCost),
      }),
      ...(formData.location && { location: formData.location }),
      ...(formData.batchNumber && { batchNumber: formData.batchNumber }),
      ...(formData.expiryDate && { expiryDate: formData.expiryDate }),
    };

    const result = await createInventoryItem(user.shopId, request)
    if (result) {
      toast.success('Inventory item created successfully')
      navigate('/inventory')
    }
  }

  const handleCancel = () => {
    navigate('/inventory')
  }

  return (
    <div className="space-y-6 max-w-4xl">
      {/* Header */}
      <div>
        <Button
          variant="ghost"
          onClick={() => navigate('/inventory')}
          className="mb-4"
        >
          <ArrowLeft className="h-4 w-4 mr-2" />
          Back to Inventory
        </Button>
        
        <div>
          <h1 className="text-3xl font-bold flex items-center gap-2">
            <Package className="h-8 w-8" />
            Add Inventory Item
          </h1>
          <p className="text-muted-foreground mt-1">
            Add a new product to your inventory tracking
          </p>
        </div>
      </div>

      {/* Form */}
      <form onSubmit={handleSubmit}>
        <Card>
          <CardHeader>
            <CardTitle>Inventory Details</CardTitle>
            <CardDescription>
              Enter the inventory information for the product
            </CardDescription>
          </CardHeader>
          <CardContent className="space-y-6">
            {/* Product Selection */}
            <div className="space-y-2">
              <Label htmlFor="productId">
                Product <span className="text-red-500">*</span>
              </Label>
              <div className="space-y-2">
                <div className="relative">
                  <Search className="absolute left-3 top-3 h-4 w-4 text-muted-foreground" />
                  <Input
                    placeholder="Search products by name or SKU..."
                    value={productSearch}
                    onChange={(e) => setProductSearch(e.target.value)}
                    className="pl-10"
                  />
                </div>
                
                <Select
                  value={formData.productId}
                  onValueChange={(value) => handleInputChange('productId', value)}
                  disabled={productsLoading}
                >
                  <SelectTrigger>
                    <SelectValue placeholder={productsLoading ? "Loading products..." : "Select a product"} />
                  </SelectTrigger>
                  <SelectContent>
                    {filteredProducts?.length === 0 ? (
                      <div className="px-2 py-4 text-sm text-muted-foreground text-center">
                        {productSearch ? 'No products found' : 'No products available'}
                      </div>
                    ) : (
                      filteredProducts?.map((product) => (
                        <SelectItem key={product.id} value={product.id}>
                          <div className="flex flex-col">
                            <span className="font-medium">{product.name}</span>
                            {product.sku && (
                              <span className="text-xs text-muted-foreground">SKU: {product.sku}</span>
                            )}
                          </div>
                        </SelectItem>
                      ))
                    )}
                  </SelectContent>
                </Select>
              </div>
              {errors.productId && (
                <p className="text-sm text-red-500 flex items-center gap-1">
                  <AlertCircle className="h-3 w-3" />
                  {errors.productId}
                </p>
              )}
              {selectedProduct && (
                <div className="p-3 bg-blue-50 border border-blue-200 rounded-md">
                  <p className="text-sm font-medium">{selectedProduct.name}</p>
                  <div className="text-xs text-muted-foreground mt-1 space-y-1">
                    {selectedProduct.sku && <p>SKU: {selectedProduct.sku}</p>}
                    {selectedProduct.category && <p>Category: {selectedProduct.category}</p>}
                  </div>
                </div>
              )}
            </div>

            {/* Stock Levels */}
            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
              <div className="space-y-2">
                <Label htmlFor="currentStock">
                  Current Stock <span className="text-red-500">*</span>
                </Label>
                <Input
                  id="currentStock"
                  type="number"
                  min="0"
                  placeholder="0"
                  value={formData.currentStock}
                  onChange={(e) => handleInputChange('currentStock', e.target.value)}
                  disabled={isLoading}
                />
                {errors.currentStock && (
                  <p className="text-sm text-red-500">{errors.currentStock}</p>
                )}
              </div>

              <div className="space-y-2">
                <Label htmlFor="minimumStock">
                  Minimum Stock <span className="text-red-500">*</span>
                </Label>
                <Input
                  id="minimumStock"
                  type="number"
                  min="0"
                  placeholder="0"
                  value={formData.minimumStock}
                  onChange={(e) => handleInputChange('minimumStock', e.target.value)}
                  disabled={isLoading}
                />
                {errors.minimumStock && (
                  <p className="text-sm text-red-500">{errors.minimumStock}</p>
                )}
              </div>

              <div className="space-y-2">
                <Label htmlFor="maximumStock">Maximum Stock</Label>
                <Input
                  id="maximumStock"
                  type="number"
                  min="0"
                  placeholder="Optional"
                  value={formData.maximumStock}
                  onChange={(e) => handleInputChange('maximumStock', e.target.value)}
                  disabled={isLoading}
                />
                {errors.maximumStock && (
                  <p className="text-sm text-red-500">{errors.maximumStock}</p>
                )}
              </div>

              <div className="space-y-2">
                <Label htmlFor="reorderPoint">
                  Reorder Point <span className="text-red-500">*</span>
                </Label>
                <Input
                  id="reorderPoint"
                  type="number"
                  min="0"
                  placeholder="0"
                  value={formData.reorderPoint}
                  onChange={(e) => handleInputChange('reorderPoint', e.target.value)}
                  disabled={isLoading}
                />
                {errors.reorderPoint && (
                  <p className="text-sm text-red-500">{errors.reorderPoint}</p>
                )}
              </div>
            </div>

            {/* Cost & Location */}
            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
              <div className="space-y-2">
                <Label htmlFor="unitCost">Unit Cost</Label>
                <Input
                  id="unitCost"
                  type="number"
                  min="0"
                  step="0.01"
                  placeholder="0.00"
                  value={formData.unitCost}
                  onChange={(e) => handleInputChange('unitCost', e.target.value)}
                  disabled={isLoading}
                />
                {errors.unitCost && (
                  <p className="text-sm text-red-500">{errors.unitCost}</p>
                )}
              </div>

              <div className="space-y-2">
                <Label htmlFor="location">Storage Location</Label>
                <Input
                  id="location"
                  placeholder="e.g., Aisle 3, Shelf B"
                  value={formData.location}
                  onChange={(e) => handleInputChange('location', e.target.value)}
                  disabled={isLoading}
                />
              </div>
            </div>

            {/* Batch & Expiry */}
            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
              <div className="space-y-2">
                <Label htmlFor="batchNumber">Batch Number</Label>
                <Input
                  id="batchNumber"
                  placeholder="e.g., BATCH-2024-001"
                  value={formData.batchNumber}
                  onChange={(e) => handleInputChange('batchNumber', e.target.value)}
                  disabled={isLoading}
                />
              </div>

              <div className="space-y-2">
                <Label htmlFor="expiryDate">Expiry Date</Label>
                <Input
                  id="expiryDate"
                  type="date"
                  value={formData.expiryDate}
                  onChange={(e) => handleInputChange('expiryDate', e.target.value)}
                  disabled={isLoading}
                />
                {errors.expiryDate && (
                  <p className="text-sm text-red-500">{errors.expiryDate}</p>
                )}
              </div>
            </div>

            {/* Actions */}
            <div className="flex justify-end gap-3 pt-4 border-t">
              <Button
                type="button"
                variant="outline"
                onClick={handleCancel}
                disabled={isLoading}
              >
                Cancel
              </Button>
              <Button type="submit" disabled={isLoading}>
                {isLoading ? 'Creating...' : 'Create Inventory Item'}
              </Button>
            </div>
          </CardContent>
        </Card>
      </form>
    </div>
  )
}
