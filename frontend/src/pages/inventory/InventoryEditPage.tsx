import React, { useState, useEffect } from 'react'
import { useParams, useNavigate } from 'react-router-dom'
import { Button } from '@/components/ui/button'
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card'
import { Input } from '@/components/ui/input'
import { NumericInput } from '@/components/ui/numeric-input'
import { Label } from '@/components/ui/label'
import { Alert, AlertDescription } from '@/components/ui/alert'
import { ArrowLeft, Loader2, AlertCircle, Save } from 'lucide-react'
import { useInventory } from '@/hooks/useInventory'
import { InventoryUnitPricing, UnitPrice } from '@/components/inventory/InventoryUnitPricing'
import { useProducts } from '@/hooks/useProducts'
import { useShopContext } from '@/context/ShopContext'

export const InventoryEditPage: React.FC = () => {
  const { inventoryId } = useParams<{ inventoryId: string }>()
  const navigate = useNavigate()
  const { selectedShopId } = useShopContext()

  const {
    inventory,
    isLoading,
    error,
    canManageInventory,
    fetchInventoryItem,
    updateInventorySettings,
    clearError,
  } = useInventory()

  const { products } = useProducts({
    shopId: selectedShopId || undefined
  })

  // Check if user has permission to manage inventory
  useEffect(() => {
    if (!canManageInventory) {
      navigate('/inventory')
    }
  }, [canManageInventory, navigate])

  if (!canManageInventory) {
    return null
  }

  // Find current item
  const currentItem = (inventory || []).find(item => item.id === inventoryId)

  // Form state
  const [minimumStock, setMinimumStock] = useState('')
  const [maximumStock, setMaximumStock] = useState('')
  const [reorderPoint, setReorderPoint] = useState('')
  const [costPrice, setCostPrice] = useState('')
  const [unitPrices, setUnitPrices] = useState<UnitPrice[]>([])
  const [location, setLocation] = useState('')
  const [batchNumber, setBatchNumber] = useState('')
  const [expiryDate, setExpiryDate] = useState('')

  // Load data on mount
  useEffect(() => {
    if (inventoryId) {
      fetchInventoryItem(inventoryId)
    }
  }, [inventoryId, fetchInventoryItem])

  // Get the product for this inventory item
  const currentProduct = products?.find(p => p.id === currentItem?.productId)

  // Populate form when item loads
  useEffect(() => {
    if (currentItem) {
      setMinimumStock(currentItem.minimumStock.toString())
      setMaximumStock(currentItem.maximumStock?.toString() || '')
      setReorderPoint(currentItem.reorderPoint.toString())
      setCostPrice(currentItem.costPrice?.toString() || currentItem.unitCost?.toString() || '')
      setLocation(currentItem.location || '')
      setBatchNumber(currentItem.batchNumber || '')
      setExpiryDate(currentItem.expiryDate ? currentItem.expiryDate.split('T')[0] : '')

      // Load unit prices if available
      if (currentItem.unitPrices && currentItem.unitPrices.length > 0) {
        setUnitPrices(currentItem.unitPrices.map(up => ({
          unitType: up.unitType,
          sellingPrice: up.sellingPrice,
        })))
      }
    }
  }, [currentItem])

  // Handle form submit
  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    if (!inventoryId) return

    // Calculate base selling price for backward compatibility
    let baseSellingPrice = 0
    if (unitPrices.length > 0) {
      const baseUnit = currentProduct?.unitDefinitions?.find(u => u.isBaseUnit)
      const baseUnitPrice = unitPrices.find(up => up.unitType === baseUnit?.unitType)
      baseSellingPrice = baseUnitPrice?.sellingPrice || unitPrices[0].sellingPrice
    }

    const updates: any = {
      minimumStock: parseInt(minimumStock),
      reorderPoint: parseInt(reorderPoint),
    }

    if (maximumStock) updates.maximumStock = parseInt(maximumStock)
    if (costPrice) updates.costPrice = parseFloat(costPrice)
    if (unitPrices.length > 0) {
      updates.sellingPrice = baseSellingPrice
      updates.unitPrices = unitPrices
    }
    if (location) updates.location = location
    if (batchNumber) updates.batchNumber = batchNumber
    if (expiryDate) updates.expiryDate = expiryDate

    const result = await updateInventorySettings(inventoryId, updates)
    if (result) {
      navigate(`/inventory/${inventoryId}`)
    }
  }

  // Loading state
  if (isLoading && !currentItem) {
    return (
      <div className="flex justify-center items-center min-h-screen">
        <Loader2 className="h-8 w-8 animate-spin text-muted-foreground" />
      </div>
    )
  }

  // Not found state
  if (!currentItem && !isLoading) {
    return (
      <div className="space-y-6">
        <div className="flex items-center gap-4">
          <Button variant="ghost" onClick={() => navigate('/inventory')}>
            <ArrowLeft className="mr-2 h-4 w-4" />
            Back to Inventory
          </Button>
        </div>
        <Alert variant="destructive">
          <AlertCircle className="h-4 w-4" />
          <AlertDescription>
            Inventory item not found. It may have been deleted or you don't have access to it.
          </AlertDescription>
        </Alert>
      </div>
    )
  }

  if (!currentItem) return null

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex items-center gap-4">
        <Button variant="ghost" onClick={() => navigate(`/inventory/${inventoryId}`)}>
          <ArrowLeft className="mr-2 h-4 w-4" />
          Back
        </Button>
        <div>
          <h1 className="text-3xl font-bold tracking-tight">Edit Inventory Settings</h1>
          <p className="text-muted-foreground mt-1">
            {currentItem.productName}
          </p>
        </div>
      </div>

      {/* Error State */}
      {error && (
        <Alert variant="destructive">
          <AlertCircle className="h-4 w-4" />
          <AlertDescription>
            {error}
            <Button
              variant="link"
              className="ml-2 p-0 h-auto"
              onClick={clearError}
            >
              Dismiss
            </Button>
          </AlertDescription>
        </Alert>
      )}

      {/* Edit Form */}
      <form onSubmit={handleSubmit}>
        <div className="grid gap-6 md:grid-cols-2">
          {/* Stock Levels */}
          <Card>
            <CardHeader>
              <CardTitle>Stock Levels</CardTitle>
              <CardDescription>Configure stock thresholds and limits</CardDescription>
            </CardHeader>
            <CardContent className="space-y-4">
              <div className="space-y-2">
                <Label htmlFor="minimumStock">
                  Minimum Stock <span className="text-red-500">*</span>
                </Label>
                <Input
                  id="minimumStock"
                  type="number"
                  value={minimumStock}
                  onChange={(e) => setMinimumStock(e.target.value)}
                  placeholder="0"
                  min="0"
                  required
                />
                <p className="text-xs text-muted-foreground">
                  Alert threshold for low stock
                </p>
              </div>

              <div className="space-y-2">
                <Label htmlFor="reorderPoint">
                  Reorder Point <span className="text-red-500">*</span>
                </Label>
                <Input
                  id="reorderPoint"
                  type="number"
                  value={reorderPoint}
                  onChange={(e) => setReorderPoint(e.target.value)}
                  placeholder="0"
                  min="0"
                  required
                />
                <p className="text-xs text-muted-foreground">
                  Stock level to trigger reorder
                </p>
              </div>

              <div className="space-y-2">
                <Label htmlFor="maximumStock">Maximum Stock</Label>
                <Input
                  id="maximumStock"
                  type="number"
                  value={maximumStock}
                  onChange={(e) => setMaximumStock(e.target.value)}
                  placeholder="Optional"
                  min="0"
                />
                <p className="text-xs text-muted-foreground">
                  Maximum storage capacity
                </p>
              </div>
            </CardContent>
          </Card>

          {/* Pricing - Unit Based */}
          <InventoryUnitPricing
            product={currentProduct}
            costPrice={costPrice}
            onCostPriceChange={setCostPrice}
            unitPrices={unitPrices}
            onUnitPricesChange={setUnitPrices}
          />

          {/* Location */}
          <Card>
            <CardHeader>
              <CardTitle>Storage Location</CardTitle>
              <CardDescription>Physical storage information</CardDescription>
            </CardHeader>
            <CardContent className="space-y-4">
              <div className="space-y-2">
                <Label htmlFor="location">Storage Location</Label>
                <Input
                  id="location"
                  type="text"
                  value={location}
                  onChange={(e) => setLocation(e.target.value)}
                  placeholder="e.g., Aisle 3, Shelf B"
                />
                <p className="text-xs text-muted-foreground">
                  Physical location in your storage area
                </p>
              </div>
            </CardContent>
          </Card>

          {/* Batch & Expiry */}
          <Card className="md:col-span-2">
            <CardHeader>
              <CardTitle>Batch & Expiry Information</CardTitle>
              <CardDescription>Track batch numbers and expiration dates</CardDescription>
            </CardHeader>
            <CardContent className="space-y-4">
              <div className="grid gap-4 md:grid-cols-2">
                <div className="space-y-2">
                  <Label htmlFor="batchNumber">Batch Number</Label>
                  <Input
                    id="batchNumber"
                    type="text"
                    value={batchNumber}
                    onChange={(e) => setBatchNumber(e.target.value)}
                    placeholder="Batch or lot number"
                  />
                  <p className="text-xs text-muted-foreground">
                    Manufacturer batch or lot identifier
                  </p>
                </div>

                <div className="space-y-2">
                  <Label htmlFor="expiryDate">Expiry Date</Label>
                  <Input
                    id="expiryDate"
                    type="date"
                    value={expiryDate}
                    onChange={(e) => setExpiryDate(e.target.value)}
                  />
                  <p className="text-xs text-muted-foreground">
                    Optional: Set if product has an expiration date
                  </p>
                </div>
              </div>
            </CardContent>
          </Card>
        </div>

        {/* Action Buttons */}
        <div className="flex justify-end gap-4 mt-6">
          <Button
            type="button"
            variant="outline"
            onClick={() => navigate(`/inventory/${inventoryId}`)}
            disabled={isLoading}
          >
            Cancel
          </Button>
          <Button type="submit" disabled={isLoading}>
            {isLoading ? (
              <>
                <Loader2 className="mr-2 h-4 w-4 animate-spin" />
                Saving...
              </>
            ) : (
              <>
                <Save className="mr-2 h-4 w-4" />
                Save Changes
              </>
            )}
          </Button>
        </div>
      </form>
    </div>
  )
}
