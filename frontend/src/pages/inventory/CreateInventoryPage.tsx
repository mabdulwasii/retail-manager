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
import { InventoryUnitPricing, UnitPrice } from '@/components/inventory/InventoryUnitPricing'

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
    minimumStock: '0',
    costPrice: '',
    purchaseUnit: '',
    purchaseQuantity: '',
    purchaseUnitCost: '',
    location: '',
    expiryDate: ''
  })

  const [unitPrices, setUnitPrices] = useState<UnitPrice[]>([])
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

    // Minimum stock is now optional, defaults to 0
    if (formData.minimumStock && formData.minimumStock.trim() !== '') {
      const minStock = parseInt(formData.minimumStock, 10)
      if (isNaN(minStock) || minStock < 0) {
        newErrors.minimumStock = 'Minimum stock must be a non-negative number'
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

    // Validate unit prices if product has unit definitions
    if (selectedProduct?.unitDefinitions && selectedProduct.unitDefinitions.length > 0) {
      const baseUnit = selectedProduct.unitDefinitions.find(u => u.isBaseUnit)
      if (baseUnit) {
        const baseUnitPrice = unitPrices.find(up => up.unitType === baseUnit.unitType)
        if (!baseUnitPrice || baseUnitPrice.sellingPrice <= 0) {
          newErrors.unitPrices = `Base unit (${baseUnit.unitLabel}) selling price is required`
        }
      }

      if (unitPrices.length === 0) {
        newErrors.unitPrices = 'At least one selling price must be set'
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

    // For products with unit definitions, use first unit price as base selling price
    // For backward compatibility
    let baseSellingPrice = 0
    if (unitPrices.length > 0) {
      const baseUnit = selectedProduct?.unitDefinitions?.find(u => u.isBaseUnit)
      const baseUnitPrice = unitPrices.find(up => up.unitType === baseUnit?.unitType)
      baseSellingPrice = baseUnitPrice?.sellingPrice || unitPrices[0].sellingPrice
    }

    const request: CreateInventoryRequest = {
      productId: formData.productId,
      minimumStock: formData.minimumStock ? parseInt(formData.minimumStock, 10) : 0,
      costPrice: parseFloat(formData.costPrice),
      sellingPrice: baseSellingPrice,
      baseUnit: selectedProduct?.unitDefinitions?.find(u => u.isBaseUnit)?.unitType || 'piece',
      unitPrices: unitPrices.map(up => ({
        unitType: up.unitType,
        sellingPrice: up.sellingPrice,
      })),
      ...(formData.purchaseUnit && { purchaseUnit: formData.purchaseUnit }),
      ...(formData.purchaseQuantity && { purchaseQuantity: parseFloat(formData.purchaseQuantity) }),
      ...(formData.purchaseUnitCost && { purchaseUnitCost: parseFloat(formData.purchaseUnitCost) }),
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

            </div>

            {/* Pricing - Unit Based */}
            <InventoryUnitPricing
              product={selectedProduct}
              costPrice={formData.costPrice}
              onCostPriceChange={(value) => handleInputChange('costPrice', value)}
              purchaseUnit={formData.purchaseUnit}
              onPurchaseUnitChange={(value) => handleInputChange('purchaseUnit', value)}
              purchaseQuantity={formData.purchaseQuantity}
              onPurchaseQuantityChange={(value) => handleInputChange('purchaseQuantity', value)}
              purchaseUnitCost={formData.purchaseUnitCost}
              onPurchaseUnitCostChange={(value) => {
                handleInputChange('purchaseUnitCost', value)
                // Auto-calculate base unit cost price
                if (value && formData.purchaseUnit && selectedProduct?.unitDefinitions) {
                  const purchaseUnitDef = selectedProduct.unitDefinitions.find(u => u.unitType === formData.purchaseUnit)
                  if (purchaseUnitDef && parseFloat(value) > 0) {
                    const baseCost = (parseFloat(value) / purchaseUnitDef.conversionFactor).toFixed(2)
                    handleInputChange('costPrice', baseCost)
                  }
                }
              }}
              unitPrices={unitPrices}
              onUnitPricesChange={setUnitPrices}
              errors={{
                costPrice: errors.costPrice,
                unitPrices: errors.unitPrices,
                purchaseUnit: errors.purchaseUnit,
                purchaseQuantity: errors.purchaseQuantity,
                purchaseUnitCost: errors.purchaseUnitCost,
              }}
            />

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
