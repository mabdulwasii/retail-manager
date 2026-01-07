import React, { useState, useEffect } from 'react'
import { useNavigate } from 'react-router-dom'
import { ArrowLeft, Package, Search, AlertCircle, X } from 'lucide-react'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import { NumericInput } from '@/components/ui/numeric-input'
import { Label } from '@/components/ui/label'
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card'
import { useInventory, CreateInventoryRequest } from '@/hooks/useInventory'
import { useAuth } from '@/context/UnifiedAuthContext'
import { useProducts } from '@/hooks/useProducts'
import { toast } from 'sonner'
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select'
import { ShopSelector } from '@/components/ui/shop-selector'
import { useShopContext } from '@/context/ShopContext'

export const CreateInventoryPage: React.FC = () => {
  const navigate = useNavigate()
  const { user } = useAuth()
  const { selectedShopId, setSelectedShopId, canManageMultipleShops } = useShopContext()
  
  // Use selectedShopId for multi-shop users, fall back to user.shopId for single-shop users
  const effectiveShopId = canManageMultipleShops ? selectedShopId : user?.shopId
  
  const { createInventoryItem, isLoading, canManageInventory } = useInventory()
  const { products, isLoading: productsLoading } = useProducts({
    shopId: effectiveShopId || undefined
  })

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
    minimumStock: '0',
    maximumStock: '',
    reorderPoint: '0',
    costPrice: '',
    sellingPrice: '',
    location: '',
    expiryDate: ''
  })

  const [errors, setErrors] = useState<Record<string, string>>({})
  const [productSearch, setProductSearch] = useState('')

  console.log('product', products)

   useEffect(() => {
    console.log('producte', products,productsLoading)
  }, [products,productsLoading])

  const filteredProducts = products?.filter(product =>
    // Exclude already selected product from the list
    product.id !== formData.productId &&
    (product.name.toLowerCase().includes(productSearch.toLowerCase()) ||
    product.sku?.toLowerCase().includes(productSearch.toLowerCase()))
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

    // Minimum stock is now optional, defaults to 0
    if (formData.minimumStock && formData.minimumStock.trim() !== '') {
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

    // Reorder point is now optional, defaults to 0
    if (formData.reorderPoint && formData.reorderPoint.trim() !== '') {
      const reorderPoint = parseInt(formData.reorderPoint, 10)
      if (isNaN(reorderPoint) || reorderPoint < 0) {
        newErrors.reorderPoint = 'Reorder point must be a non-negative number'
      }
    }

    if (!formData.costPrice || formData.costPrice.trim() === '') {
      newErrors.costPrice = 'Cost price is required'
    } else {
      const cost = parseFloat(formData.costPrice)
      if (isNaN(cost) || cost < 0) {
        newErrors.costPrice = 'Cost price must be a non-negative number'
      }
    }

    if (!formData.sellingPrice || formData.sellingPrice.trim() === '') {
      newErrors.sellingPrice = 'Selling price is required'
    } else {
      const sellingPrice = parseFloat(formData.sellingPrice)
      const costPrice = parseFloat(formData.costPrice)
      if (isNaN(sellingPrice) || sellingPrice < 0) {
        newErrors.sellingPrice = 'Selling price must be a non-negative number'
      } else if (!isNaN(costPrice) && sellingPrice < costPrice) {
        newErrors.sellingPrice = 'Selling price should not be less than cost price'
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

    if (!effectiveShopId) {
      toast.error('Shop ID not found. Please select a shop or log in again.')
      return
    }

    const request: CreateInventoryRequest = {
      productId: formData.productId,
      currentStock: parseInt(formData.currentStock, 10),
      minimumStock: formData.minimumStock ? parseInt(formData.minimumStock, 10) : 0,
      reorderPoint: formData.reorderPoint ? parseInt(formData.reorderPoint, 10) : 0,
      costPrice: parseFloat(formData.costPrice),
      sellingPrice: parseFloat(formData.sellingPrice),
      ...(formData.maximumStock && {
        maximumStock: parseInt(formData.maximumStock, 10),
      }),
      ...(formData.location && { location: formData.location }),
      ...(formData.expiryDate && { expiryDate: formData.expiryDate }),
    };

    const result = await createInventoryItem(effectiveShopId, request)
    if (result) {
      toast.success('Inventory item created successfully')
      navigate('/inventory')
    }
  }

  const handleCancel = () => {
    navigate('/inventory')
  }

  return (
    <div className="space-y-6 max-w-6xl">
      {/* Header */}
      <div>
        <Button
          variant="ghost"
          onClick={() => navigate("/inventory")}
          className="mb-4"
        >
          <ArrowLeft className="h-4 w-4 mr-2" />
          Back to Inventory
        </Button>

        <div className="flex items-start justify-between">
          <div>
            <h1 className="text-3xl font-bold flex items-center gap-2">
              <Package className="h-8 w-8" />
              Add Inventory Item
            </h1>
            <p className="text-muted-foreground mt-1">
              Add a new product to your inventory tracking
            </p>
          </div>
          {canManageMultipleShops && selectedShopId && (
            <ShopSelector
              value={selectedShopId}
              onValueChange={setSelectedShopId}
              className="w-[200px]"
            />
          )}
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
                {/* <div className="relative">
                  <Search className="absolute left-3 top-3 h-4 w-4 text-muted-foreground" />
                  <Input
                    placeholder="Search products by name or SKU..."
                    value={productSearch}
                    onChange={(e) => setProductSearch(e.target.value)}
                    className="pl-10"
                  />
                </div> */}

                <Select
                  //value={formData.productId}
                  onValueChange={(value) =>
                    handleInputChange("productId", value)
                  }
                  disabled={productsLoading}
                >
                  <SelectTrigger>
                    <SelectValue
                      placeholder={
                        productsLoading
                          ? "Loading products..."
                          : "Select a product"
                      }
                    />
                  </SelectTrigger>
                  <SelectContent>
                    {filteredProducts?.length === 0 ? (
                      <div className="px-2 py-4 text-sm text-muted-foreground text-center">
                        {productSearch
                          ? "No products found"
                          : "No products available"}
                      </div>
                    ) : (
                      filteredProducts?.map((product) => (
                        <SelectItem key={product.id} value={product.id}>
                          <div className="flex flex-col">
                            <span className="font-medium">{product.name}</span>
                            {product.sku && (
                              <span className="text-xs text-muted-foreground">
                                SKU: {product.sku}
                              </span>
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
                <div className="p-3 bg-blue-50 border border-blue-200 rounded-md relative">
                  <Button
                    type="button"
                    variant="ghost"
                    size="sm"
                    className="absolute top-2 right-2 h-6 w-6 p-0"
                    onClick={() => {
                      handleInputChange("productId", "");
                      setProductSearch("");
                    }}
                  >
                    <X className="h-4 w-4" />
                  </Button>
                  <p className="text-sm font-medium pr-8">
                    {selectedProduct.name}
                  </p>
                  <div className="text-xs text-muted-foreground mt-1 space-y-1">
                    {selectedProduct.sku && <p>SKU: {selectedProduct.sku}</p>}
                    {selectedProduct.category && (
                      <p>Category: {selectedProduct.category}</p>
                    )}
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
                  onChange={(e) =>
                    handleInputChange("currentStock", e.target.value)
                  }
                  disabled={isLoading}
                />
                {errors.currentStock && (
                  <p className="text-sm text-red-500">{errors.currentStock}</p>
                )}
              </div>

              <div className="space-y-2">
                <Label htmlFor="minimumStock">Minimum Stock (optional)</Label>
                <Input
                  id="minimumStock"
                  type="number"
                  min="0"
                  placeholder="0"
                  value={formData.minimumStock}
                  onChange={(e) =>
                    handleInputChange("minimumStock", e.target.value)
                  }
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
                  onChange={(e) =>
                    handleInputChange("maximumStock", e.target.value)
                  }
                  disabled={isLoading}
                />
                {errors.maximumStock && (
                  <p className="text-sm text-red-500">{errors.maximumStock}</p>
                )}
              </div>

              <div className="space-y-2">
                <Label htmlFor="reorderPoint">Reorder Point (optional)</Label>
                <Input
                  id="reorderPoint"
                  type="number"
                  min="0"
                  placeholder="0"
                  value={formData.reorderPoint}
                  onChange={(e) =>
                    handleInputChange("reorderPoint", e.target.value)
                  }
                  disabled={isLoading}
                />
                {errors.reorderPoint && (
                  <p className="text-sm text-red-500">{errors.reorderPoint}</p>
                )}
              </div>
            </div>

            {/* Pricing */}
            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
              <div className="space-y-2">
                <Label htmlFor="costPrice">
                  Cost Price <span className="text-red-500">*</span>
                </Label>
                <NumericInput
                  id="costPrice"
                  value={formData.costPrice}
                  onValueChange={(values) => {
                    handleInputChange("costPrice", values.value || "");
                  }}
                  placeholder="0.00"
                  disabled={isLoading}
                  prefix="₦ "
                  decimalScale={2}
                  fixedDecimalScale={false}
                  allowNegative={false}
                />
                <p className="text-xs text-muted-foreground">
                  Purchase price per unit
                </p>
                {errors.costPrice && (
                  <p className="text-sm text-red-500">{errors.costPrice}</p>
                )}
              </div>

              <div className="space-y-2">
                <Label htmlFor="sellingPrice">
                  Selling Price <span className="text-red-500">*</span>
                </Label>
                <NumericInput
                  id="sellingPrice"
                  value={formData.sellingPrice}
                  onValueChange={(values) => {
                    handleInputChange("sellingPrice", values.value || "");
                  }}
                  placeholder="0.00"
                  disabled={isLoading}
                  prefix="₦ "
                  decimalScale={2}
                  fixedDecimalScale={false}
                  allowNegative={false}
                />
                <p className="text-xs text-muted-foreground">
                  Retail price per unit
                </p>
                {errors.sellingPrice && (
                  <p className="text-sm text-red-500">{errors.sellingPrice}</p>
                )}
              </div>
            </div>

            {/* Location */}
            <div className="space-y-2">
              <Label htmlFor="location">Storage Location</Label>
              <Input
                id="location"
                placeholder="e.g., Aisle 3, Shelf B"
                value={formData.location}
                onChange={(e) => handleInputChange("location", e.target.value)}
                disabled={isLoading}
              />
            </div>

            {/* Expiry Date (Optional) */}
            <div className="space-y-2">
              <Label htmlFor="expiryDate">Expiry Date (Optional)</Label>
              <Input
                id="expiryDate"
                type="date"
                min={
                  new Date(new Date().setDate(new Date().getDate() + 1))
                    .toISOString()
                    .split("T")[0]
                }
                value={formData.expiryDate}
                onChange={(e) =>
                  handleInputChange("expiryDate", e.target.value)
                }
                disabled={isLoading}
              />
              <p className="text-xs text-muted-foreground">
                Optional: Set if product has an expiration date
              </p>
              {errors.expiryDate && (
                <p className="text-sm text-red-500">{errors.expiryDate}</p>
              )}
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
                {isLoading ? "Creating..." : "Create Inventory Item"}
              </Button>
            </div>
          </CardContent>
        </Card>
      </form>
    </div>
  );
}
