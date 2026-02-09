import React, { useState, useEffect } from 'react'
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
  DialogFooter,
} from '@/components/ui/dialog'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import { NumericInput } from '@/components/ui/numeric-input'
import { Label } from '@/components/ui/label'
import { Textarea } from '@/components/ui/textarea'
import { LoadingSpinner } from '@/components/ui/loading-spinner'
import { UnitPricingForm } from '@/components/inventory/UnitPricingForm'
import { useInventory, CreateInventoryRequest, Product } from '@/hooks/useInventory'
import { useCurrency } from '@/hooks/useCurrency'
import { productService } from '@/services/productService'
import { InventoryUnitPriceRequest } from '@/types/api'
import {
  PackageIcon,
  SearchIcon,
  PlusIcon,
  CalendarIcon,
  MapPinIcon,
  DollarSignIcon
} from 'lucide-react'

interface InventoryFormProps {
  isOpen: boolean
  onClose: () => void
  shopId: string
  onInventoryCreated: () => void
}

export const InventoryForm: React.FC<InventoryFormProps> = ({
  isOpen,
  onClose,
  shopId,
  onInventoryCreated
}) => {
  const { createInventoryItem, isLoading } = useInventory()
  const { formatCurrency } = useCurrency()

  // Form state
  const [selectedProduct, setSelectedProduct] = useState<Product | null>(null)
  const [productSearch, setProductSearch] = useState('')
  const [availableProducts, setAvailableProducts] = useState<Product[]>([])
  const [searchLoading, setSearchLoading] = useState(false)

  const [formData, setFormData] = useState({
    minimumStock: '0',
    costPrice: '',
    sellingPrice: '',
    location: '',
    batchNumber: '',
    expiryDate: '',
    baseUnit: 'piece',
    purchaseUnit: '',
    purchaseQuantity: '',
    totalPurchaseCost: ''
  })

  const [unitPrices, setUnitPrices] = useState<InventoryUnitPriceRequest[]>([])
  
  // Auto-generate batch number on mount
  useEffect(() => {
    if (isOpen && !formData.batchNumber) {
      const timestamp = Date.now()
      // Safe: Math.random() used for non-cryptographic batch number generation
      const randomNum = Math.floor(Math.random() * 1000).toString().padStart(3, '0')
      const generatedBatch = `BATCH-${timestamp}-${randomNum}`
      setFormData(prev => ({ ...prev, batchNumber: generatedBatch }))
    }
  }, [isOpen])

  const [validationErrors, setValidationErrors] = useState<Record<string, string>>({})

  useEffect(() => {
    if (!isOpen) {
      // Reset form when modal closes
      setSelectedProduct(null)
      setProductSearch('')
      setAvailableProducts([])
      setFormData({
        minimumStock: '0',
        costPrice: '',
        sellingPrice: '',
        location: '',
        batchNumber: '',
        expiryDate: '',
        baseUnit: 'piece',
        purchaseUnit: '',
        purchaseQuantity: '',
        totalPurchaseCost: ''
      })
      setUnitPrices([])
      setValidationErrors({})
    }
  }, [isOpen])

  const searchProducts = async (query: string) => {
    if (query.length < 2) {
      setAvailableProducts([])
      return
    }

    try {
      setSearchLoading(true)
      const products = await productService.searchProducts(shopId, query)
      setAvailableProducts(products)
    } catch (error) {
      console.error('Error searching products:', error)
      setAvailableProducts([])
    } finally {
      setSearchLoading(false)
    }
  }

  useEffect(() => {
    const timeoutId = setTimeout(() => {
      searchProducts(productSearch)
    }, 300)

    return () => clearTimeout(timeoutId)
  }, [productSearch])

  const handleInputChange = (field: string, value: string) => {
    setFormData(prev => ({ ...prev, [field]: value }))
    // Clear validation error when user starts typing
    if (validationErrors[field]) {
      setValidationErrors(prev => ({ ...prev, [field]: '' }))
    }
  }

  const validateForm = () => {
    const errors: Record<string, string> = {}

    if (!selectedProduct) {
      errors.product = 'Please select a product'
    }

    // Minimum stock is now optional, defaults to 0
    if (formData.minimumStock && formData.minimumStock.trim() !== '') {
      const minStock = parseInt(formData.minimumStock, 10)
      if (isNaN(minStock) || minStock < 0) {
        errors.minimumStock = 'Minimum stock must be a non-negative number'
      }
    }

    if (!formData.costPrice || formData.costPrice.trim() === '') {
      errors.costPrice = 'Cost price is required'
    } else {
      const cost = parseFloat(formData.costPrice)
      if (isNaN(cost) || cost < 0) {
        errors.costPrice = 'Cost price must be a non-negative number'
      }
    }

    if (!formData.sellingPrice || formData.sellingPrice.trim() === '') {
      errors.sellingPrice = 'Selling price is required'
    } else {
      const price = parseFloat(formData.sellingPrice)
      if (isNaN(price) || price < 0) {
        errors.sellingPrice = 'Selling price must be a non-negative number'
      } else if (formData.costPrice && parseFloat(formData.costPrice) > price) {
        errors.sellingPrice = 'Selling price should be greater than or equal to cost price'
      }
    }

    if (formData.expiryDate && formData.expiryDate.trim() !== '') {
      const expiryDate = new Date(formData.expiryDate)
      const today = new Date()
      today.setHours(0, 0, 0, 0)
      if (expiryDate <= today) {
        errors.expiryDate = 'Expiry date must be in the future'
      }
    }

    setValidationErrors(errors)
    return Object.keys(errors).length === 0
  }

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()

    if (!validateForm() || !selectedProduct) {
      return
    }

    const request: CreateInventoryRequest = {
      productId: selectedProduct.id,
      minimumStock: formData.minimumStock ? parseInt(formData.minimumStock, 10) : 0,
      costPrice: parseFloat(formData.costPrice),
      sellingPrice: parseFloat(formData.sellingPrice),
      location: formData.location || undefined,
      batchNumber: formData.batchNumber || undefined,
      expiryDate: formData.expiryDate || undefined,
      baseUnit: formData.baseUnit || 'piece',
      purchaseUnit: formData.purchaseUnit || undefined,
      purchaseQuantity: formData.purchaseQuantity ? parseFloat(formData.purchaseQuantity) : undefined,
      totalPurchaseCost: formData.totalPurchaseCost ? parseFloat(formData.totalPurchaseCost) : undefined,
      unitPrices: unitPrices.length > 0 ? unitPrices : undefined
    }

    const success = await createInventoryItem(shopId, request)
    if (success) {
      onInventoryCreated()
    }
  }

  const handleClose = () => {
    if (!isLoading) {
      onClose()
    }
  }

  return (
    <Dialog open={isOpen} onOpenChange={handleClose}>
      <DialogContent className="sm:max-w-2xl max-h-[90vh] overflow-y-auto">
        <DialogHeader>
          <DialogTitle className="flex items-center space-x-2">
            <PackageIcon className="h-5 w-5" />
            <span>Add Inventory Item</span>
          </DialogTitle>
        </DialogHeader>

        <form onSubmit={handleSubmit} className="space-y-6">
          {/* Product Selection */}
          <div className="space-y-3">
            <Label htmlFor="productSearch">Select Product</Label>
            <div className="relative">
              <SearchIcon className="absolute left-3 top-3 h-4 w-4 text-gray-400" />
              <Input
                id="productSearch"
                type="text"
                placeholder="Search for a product..."
                value={productSearch}
                onChange={(e) => setProductSearch(e.target.value)}
                className={`pl-10 ${validationErrors.product ? 'border-red-500' : ''}`}
              />
              {searchLoading && (
                <div className="absolute right-3 top-3">
                  <LoadingSpinner size="sm" />
                </div>
              )}
            </div>

            {/* Selected Product Display */}
            {selectedProduct && (
              <div className="bg-blue-50 border border-blue-200 rounded-lg p-3">
                <div className="flex items-center justify-between">
                  <div>
                    <h4 className="font-medium text-blue-900">{selectedProduct.name}</h4>
                    <p className="text-sm text-blue-700">{selectedProduct.description}</p>
                    <div className="flex space-x-4 text-xs text-blue-600 mt-1">
                      <span>SKU: {selectedProduct.sku}</span>
                      <span>Category: {selectedProduct.category}</span>
                      <span>Stock: {selectedProduct.availableStock || 0} available</span>
                    </div>
                  </div>
                  <Button
                    type="button"
                    variant="ghost"
                    size="sm"
                    onClick={() => setSelectedProduct(null)}
                    className="text-blue-600 hover:text-blue-700"
                  >
                    Change
                  </Button>
                </div>
              </div>
            )}

            {/* Product Search Results */}
            {!selectedProduct && availableProducts.length > 0 && (
              <div className="border rounded-lg max-h-48 overflow-y-auto">
                {availableProducts.map((product) => (
                  <button
                    key={product.id}
                    type="button"
                    onClick={() => {
                      setSelectedProduct(product)
                      setProductSearch('')
                      setAvailableProducts([])
                    }}
                    className="w-full text-left p-3 hover:bg-gray-50 border-b last:border-b-0"
                  >
                    <div className="font-medium">{product.name}</div>
                    <div className="text-sm text-gray-600">{product.description}</div>
                    <div className="flex space-x-4 text-xs text-gray-500 mt-1">
                      <span>SKU: {product.sku}</span>
                      <span>Category: {product.category}</span>
                      <span>Stock: {product.availableStock || 0} available</span>
                    </div>
                  </button>
                ))}
              </div>
            )}

            {validationErrors.product && (
              <p className="text-sm text-red-600">{validationErrors.product}</p>
            )}
          </div>

          {/* Stock Information */}
          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
            <div className="space-y-2">
              <Label htmlFor="minimumStock">Minimum Stock (optional)</Label>
              <NumericInput
                id="minimumStock"
                value={formData.minimumStock}
                onValueChange={(values) => {
                  handleInputChange('minimumStock', values.value || '0')
                }}
                placeholder="0"
                className={validationErrors.minimumStock ? 'border-red-500' : ''}
                isNumberInput={true}
                allowNegative={false}
                decimalScale={0}
              />
              {validationErrors.minimumStock && (
                <p className="text-sm text-red-600">{validationErrors.minimumStock}</p>
              )}
            </div>
          </div>

          {/* Purchase Details */}
          <div className="space-y-4">
            <h3 className="text-sm font-semibold text-gray-700">Purchase Details</h3>
            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
              <div className="space-y-2">
                <Label htmlFor="purchaseUnit">Purchase Unit</Label>
                <Input
                  id="purchaseUnit"
                  type="text"
                  value={formData.purchaseUnit}
                  onChange={(e) => handleInputChange('purchaseUnit', e.target.value)}
                  placeholder="e.g., carton, pack"
                />
                <p className="text-xs text-muted-foreground">Unit in which you purchased this batch</p>
              </div>

              <div className="space-y-2">
                <Label htmlFor="purchaseQuantity">Purchase Quantity</Label>
                <NumericInput
                  id="purchaseQuantity"
                  value={formData.purchaseQuantity}
                  onValueChange={(values) => {
                    handleInputChange('purchaseQuantity', values.value || '')
                  }}
                  placeholder="0"
                  decimalScale={2}
                  allowNegative={false}
                />
                <p className="text-xs text-muted-foreground">Number of purchase units bought</p>
              </div>

              <div className="space-y-2">
                <Label htmlFor="totalPurchaseCost">Total Purchase Cost</Label>
                <NumericInput
                  id="totalPurchaseCost"
                  value={formData.totalPurchaseCost}
                  onValueChange={(values) => {
                    handleInputChange('totalPurchaseCost', values.value || '')
                  }}
                  placeholder="0.00"
                  decimalScale={2}
                  fixedDecimalScale={true}
                  allowNegative={false}
                />
                <p className="text-xs text-muted-foreground">Total cost for all purchase units</p>
              </div>

              <div className="space-y-2">
                <Label htmlFor="baseUnit">Base Unit</Label>
                <Input
                  id="baseUnit"
                  type="text"
                  value={formData.baseUnit}
                  onChange={(e) => handleInputChange('baseUnit', e.target.value)}
                  placeholder="piece"
                />
                <p className="text-xs text-muted-foreground">Smallest unit for inventory tracking</p>
              </div>
            </div>
          </div>

          {/* Pricing Information */}
          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
            <div className="space-y-2">
              <Label htmlFor="costPrice">Cost Price (Purchase Price) *</Label>
              <NumericInput
                id="costPrice"
                value={formData.costPrice}
                onValueChange={(values) => {
                  handleInputChange('costPrice', values.value || '')
                }}
                placeholder="0.00"
                className={validationErrors.costPrice ? 'border-red-500' : ''}
                decimalScale={2}
                fixedDecimalScale={true}
                allowNegative={false}
              />
              <p className="text-xs text-muted-foreground">Cost at which this batch was purchased</p>
              {validationErrors.costPrice && (
                <p className="text-sm text-red-600">{validationErrors.costPrice}</p>
              )}
            </div>

            <div className="space-y-2">
              <Label htmlFor="sellingPrice">Selling Price (Base Unit) *</Label>
              <NumericInput
                id="sellingPrice"
                value={formData.sellingPrice}
                onValueChange={(values) => {
                  handleInputChange('sellingPrice', values.value || '')
                }}
                placeholder="0.00"
                className={validationErrors.sellingPrice ? 'border-red-500' : ''}
                decimalScale={2}
                fixedDecimalScale={true}
                allowNegative={false}
              />
              <p className="text-xs text-muted-foreground">Base unit selling price for this batch</p>
              {validationErrors.sellingPrice && (
                <p className="text-sm text-red-600">{validationErrors.sellingPrice}</p>
              )}
            </div>

          </div>

          {/* Multi-Unit Pricing */}
          {selectedProduct && selectedProduct.unitDefinitions && selectedProduct.unitDefinitions.length > 0 && (
            <div className="border-t pt-4">
              <UnitPricingForm
                productName={selectedProduct.name}
                unitDefinitions={selectedProduct.unitDefinitions}
                unitPrices={unitPrices}
                onChange={setUnitPrices}
                disabled={isLoading}
              />
            </div>
          )}

          {/* Additional Information */}
          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
            <div className="space-y-2">
              <Label htmlFor="location">Location</Label>
              <div className="relative">
                <MapPinIcon className="absolute left-3 top-3 h-4 w-4 text-gray-400" />
                <Input
                  id="location"
                  type="text"
                  value={formData.location}
                  onChange={(e) => handleInputChange('location', e.target.value)}
                  placeholder="e.g., Aisle 5, Shelf B"
                  className="pl-10"
                />
              </div>
            </div>

            <div className="space-y-2">
              <Label htmlFor="batchNumber">Batch Number (auto-generated)</Label>
              <Input
                id="batchNumber"
                type="text"
                value={formData.batchNumber}
                onChange={(e) => handleInputChange('batchNumber', e.target.value)}
                placeholder="Auto-generated batch number"
              />
              <p className="text-xs text-muted-foreground">Auto-generated, but you can modify if needed</p>
            </div>

            <div className="space-y-2">
              <Label htmlFor="expiryDate">Expiry Date</Label>
              <div className="relative">
                <CalendarIcon className="absolute left-3 top-3 h-4 w-4 text-gray-400" />
                <Input
                  id="expiryDate"
                  type="date"
                  value={formData.expiryDate}
                  onChange={(e) => handleInputChange('expiryDate', e.target.value)}
                  className={`pl-10 ${validationErrors.expiryDate ? 'border-red-500' : ''}`}
                />
              </div>
              <p className="text-xs text-muted-foreground">Must be a future date</p>
              {validationErrors.expiryDate && (
                <p className="text-sm text-red-600">{validationErrors.expiryDate}</p>
              )}
            </div>
          </div>

          <DialogFooter className="space-x-2">
            <Button
              type="button"
              variant="outline"
              onClick={handleClose}
              disabled={isLoading}
            >
              Cancel
            </Button>
            <Button
              type="submit"
              disabled={isLoading || !selectedProduct}
              className="min-w-32"
            >
              {isLoading ? (
                <>
                  <LoadingSpinner size="sm" />
                  <span className="ml-2">Adding...</span>
                </>
              ) : (
                <>
                  <PlusIcon className="h-4 w-4 mr-2" />
                  Add to Inventory
                </>
              )}
            </Button>
          </DialogFooter>
        </form>
      </DialogContent>
    </Dialog>
  )
}

export default InventoryForm