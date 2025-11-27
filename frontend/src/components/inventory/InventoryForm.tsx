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
import { useInventory, CreateInventoryRequest, Product } from '@/hooks/useInventory'
import { useCurrency } from '@/hooks/useCurrency'
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
    currentStock: '',
    minimumStock: '0',
    maximumStock: '',
    reorderPoint: '0',
    unitCost: '',
    location: '',
    batchNumber: '',
    expiryDate: ''
  })
  
  // Auto-generate batch number on mount
  useEffect(() => {
    if (isOpen && !formData.batchNumber) {
      const timestamp = Date.now()
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
        currentStock: '',
        minimumStock: '0',
        maximumStock: '',
        reorderPoint: '0',
        unitCost: '',
        location: '',
        batchNumber: '',
        expiryDate: ''
      })
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
      // Mock product search - in real app, this would call an API
      const mockProducts: Product[] = [
        {
          id: '1',
          name: 'Apple iPhone 15 Pro',
          description: '128GB, Natural Titanium',
          price: 999.99,
          category: 'Electronics',
          sku: 'IP15-PRO-128-NT',
          barcode: '194253433989',
          isActive: true,
          supplierName: 'Apple Inc.'
        },
        {
          id: '2',
          name: 'Samsung Galaxy S24 Ultra',
          description: '256GB, Titanium Black',
          price: 1199.99,
          category: 'Electronics',
          sku: 'SGS24-ULTRA-256-TB',
          barcode: '887276706789',
          isActive: true,
          supplierName: 'Samsung'
        },
        {
          id: '3',
          name: 'Coca-Cola 500ml',
          description: 'Refreshing cola drink',
          price: 1.50,
          category: 'Beverages',
          sku: 'COKE-500ML',
          barcode: '5449000000996',
          isActive: true,
          supplierName: 'Coca-Cola Company'
        }
      ]

      const filtered = mockProducts.filter(product =>
        product.name.toLowerCase().includes(query.toLowerCase()) ||
        product.sku?.toLowerCase().includes(query.toLowerCase()) ||
        product.category.toLowerCase().includes(query.toLowerCase())
      )

      setAvailableProducts(filtered)
    } catch (error) {
      console.error('Error searching products:', error)
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

    if (!formData.currentStock || formData.currentStock.trim() === '') {
      errors.currentStock = 'Current stock is required'
    } else {
      const stock = parseInt(formData.currentStock, 10)
      if (isNaN(stock) || stock < 0) {
        errors.currentStock = 'Current stock must be a non-negative number'
      }
    }

    // Minimum stock is now optional, defaults to 0
    if (formData.minimumStock && formData.minimumStock.trim() !== '') {
      const minStock = parseInt(formData.minimumStock, 10)
      if (isNaN(minStock) || minStock < 0) {
        errors.minimumStock = 'Minimum stock must be a non-negative number'
      }
    }

    if (formData.maximumStock && formData.maximumStock.trim() !== '') {
      const maxStock = parseInt(formData.maximumStock, 10)
      const currentStock = parseInt(formData.currentStock, 10)
      if (isNaN(maxStock) || maxStock < 0) {
        errors.maximumStock = 'Maximum stock must be a non-negative number'
      } else if (!isNaN(currentStock) && maxStock < currentStock) {
        errors.maximumStock = 'Maximum stock cannot be less than current stock'
      }
    }

    // Reorder point is now optional, defaults to 0
    if (formData.reorderPoint && formData.reorderPoint.trim() !== '') {
      const reorderPoint = parseInt(formData.reorderPoint, 10)
      if (isNaN(reorderPoint) || reorderPoint < 0) {
        errors.reorderPoint = 'Reorder point must be a non-negative number'
      }
    }

    if (formData.unitCost && formData.unitCost.trim() !== '') {
      const cost = parseFloat(formData.unitCost)
      if (isNaN(cost) || cost < 0) {
        errors.unitCost = 'Unit cost must be a non-negative number'
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
      currentStock: parseInt(formData.currentStock, 10),
      minimumStock: formData.minimumStock ? parseInt(formData.minimumStock, 10) : 0,
      maximumStock: formData.maximumStock ? parseInt(formData.maximumStock, 10) : undefined,
      reorderPoint: formData.reorderPoint ? parseInt(formData.reorderPoint, 10) : 0,
      unitCost: formData.unitCost ? parseFloat(formData.unitCost) : undefined,
      location: formData.location || undefined,
      batchNumber: formData.batchNumber || undefined,
      expiryDate: formData.expiryDate || undefined
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
                      <span>Price: {formatCurrency(selectedProduct.price)}</span>
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
                      <span>Price: {formatCurrency(product.price)}</span>
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
              <Label htmlFor="currentStock">Current Stock *</Label>
              <NumericInput
                id="currentStock"
                value={formData.currentStock}
                onValueChange={(values) => {
                  handleInputChange('currentStock', values.value || '')
                }}
                placeholder="0"
                className={validationErrors.currentStock ? 'border-red-500' : ''}
                isNumberInput={true}
                allowNegative={false}
                decimalScale={0}
              />
              {validationErrors.currentStock && (
                <p className="text-sm text-red-600">{validationErrors.currentStock}</p>
              )}
            </div>

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

            <div className="space-y-2">
              <Label htmlFor="maximumStock">Maximum Stock</Label>
              <NumericInput
                id="maximumStock"
                value={formData.maximumStock}
                onValueChange={(values) => {
                  handleInputChange('maximumStock', values.value || '')
                }}
                placeholder="0"
                className={validationErrors.maximumStock ? 'border-red-500' : ''}
                isNumberInput={true}
                allowNegative={false}
                decimalScale={0}
              />
              {validationErrors.maximumStock && (
                <p className="text-sm text-red-600">{validationErrors.maximumStock}</p>
              )}
            </div>

            <div className="space-y-2">
              <Label htmlFor="reorderPoint">Reorder Point (optional)</Label>
              <NumericInput
                id="reorderPoint"
                value={formData.reorderPoint}
                onValueChange={(values) => {
                  handleInputChange('reorderPoint', values.value || '0')
                }}
                placeholder="0"
                className={validationErrors.reorderPoint ? 'border-red-500' : ''}
                isNumberInput={true}
                allowNegative={false}
                decimalScale={0}
              />
              {validationErrors.reorderPoint && (
                <p className="text-sm text-red-600">{validationErrors.reorderPoint}</p>
              )}
            </div>
          </div>

          {/* Additional Information */}
          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
            <div className="space-y-2">
              <Label htmlFor="unitCost">Unit Cost (Purchase Price)</Label>
              <NumericInput
                id="unitCost"
                value={formData.unitCost}
                onValueChange={(values) => {
                  handleInputChange('unitCost', values.value || '')
                }}
                placeholder="0.00"
                className={validationErrors.unitCost ? 'border-red-500' : ''}
                decimalScale={2}
                fixedDecimalScale={true}
                allowNegative={false}
              />
              <p className="text-xs text-muted-foreground">Cost at which this batch was purchased</p>
              {validationErrors.unitCost && (
                <p className="text-sm text-red-600">{validationErrors.unitCost}</p>
              )}
            </div>

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
                  min={new Date(new Date().setDate(new Date().getDate() + 1)).toISOString().split('T')[0]}
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