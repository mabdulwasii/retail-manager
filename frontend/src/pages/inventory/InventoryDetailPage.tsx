import React, { useState, useEffect } from 'react'
import { Link, useParams, useNavigate } from 'react-router-dom'
import { Button } from '@/components/ui/button'
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card'
import { Badge } from '@/components/ui/badge'
import { Alert, AlertDescription } from '@/components/ui/alert'
import { Separator } from '@/components/ui/separator'
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from '@/components/ui/table'
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from '@/components/ui/dialog'
import { Input } from '@/components/ui/input'
import { Label } from '@/components/ui/label'
import { Textarea } from '@/components/ui/textarea'
import {
  ArrowLeft,
  Package,
  TrendingUp,
  TrendingDown,
  Calendar,
  MapPin,
  DollarSign,
  AlertCircle,
  Loader2,
  Edit,
  Minus,
  Plus,
  Lock,
  Unlock,
  History,
  BarChart3,
} from 'lucide-react'
import { useInventory, InventoryItem, InventoryHistory, InventoryStatus } from '@/hooks/useInventory'
import { useCurrency } from '@/hooks/useCurrency'
import { format } from 'date-fns'

export const InventoryDetailPage: React.FC = () => {
  const { inventoryId } = useParams<{ inventoryId: string }>()
  const navigate = useNavigate()
  const { formatCurrency } = useCurrency()

  const {
    inventory,
    inventoryHistory,
    isLoading,
    error,
    canManageInventory,
    canAdjustStock,
    fetchInventoryItem,
    fetchInventoryHistory,
    adjustStock,
    reserveStock,
    releaseReservedStock,
    updateInventoryStatus,
    clearError,
  } = useInventory()

  // Local state
  const [adjustStockOpen, setAdjustStockOpen] = useState(false)
  const [reserveStockOpen, setReserveStockOpen] = useState(false)
  const [releaseStockOpen, setReleaseStockOpen] = useState(false)
  
  // Form states
  const [adjustQuantity, setAdjustQuantity] = useState('')
  const [adjustReason, setAdjustReason] = useState('')
  const [reserveQuantity, setReserveQuantity] = useState('')
  const [reserveReason, setReserveReason] = useState('')
  const [releaseQuantity, setReleaseQuantity] = useState('')

  // Find current item
  const currentItem = (inventory || []).find(item => item.id === inventoryId)

  // Load data on mount
  useEffect(() => {
    if (inventoryId) {
      fetchInventoryItem(inventoryId)
      fetchInventoryHistory(inventoryId)
    }
  }, [inventoryId, fetchInventoryItem, fetchInventoryHistory])

  // Handle adjust stock
  const handleAdjustStock = async () => {
    if (!inventoryId || !adjustQuantity || !adjustReason) return

    const newStock = parseInt(adjustQuantity)
    if (isNaN(newStock)) return

    const result = await adjustStock(inventoryId, {
      newStock,
      reason: adjustReason,
    })

    if (result) {
      setAdjustStockOpen(false)
      setAdjustQuantity('')
      setAdjustReason('')
      fetchInventoryHistory(inventoryId)
    }
  }

  // Handle reserve stock
  const handleReserveStock = async () => {
    if (!inventoryId || !reserveQuantity) return

    const quantity = parseInt(reserveQuantity)
    if (isNaN(quantity)) return

    const success = await reserveStock(inventoryId, {
      quantity,
      reason: reserveReason,
    })

    if (success) {
      setReserveStockOpen(false)
      setReserveQuantity('')
      setReserveReason('')
      fetchInventoryHistory(inventoryId)
    }
  }

  // Handle release stock
  const handleReleaseStock = async () => {
    if (!inventoryId || !releaseQuantity) return

    const quantity = parseInt(releaseQuantity)
    if (isNaN(quantity)) return

    const success = await releaseReservedStock(inventoryId, quantity)

    if (success) {
      setReleaseStockOpen(false)
      setReleaseQuantity('')
      fetchInventoryHistory(inventoryId)
    }
  }

  // Get stock level color
  const getStockLevelColor = (item: InventoryItem): string => {
    if (item.currentStock === 0) return 'text-gray-500'
    if (item.currentStock < item.minimumStock) return 'text-red-600'
    if (item.isLowStock) return 'text-yellow-600'
    return 'text-green-600'
  }

  // Get stock level badge
  const getStockLevelBadge = (item: InventoryItem) => {
    if (item.currentStock === 0) {
      return <Badge variant="secondary" className="bg-gray-100">Out of Stock</Badge>
    }
    if (item.currentStock < item.minimumStock) {
      return <Badge variant="destructive">Critical</Badge>
    }
    if (item.isLowStock) {
      return <Badge className="bg-yellow-500">Low Stock</Badge>
    }
    return <Badge className="bg-green-500">Adequate</Badge>
  }

  // Get status badge
  const getStatusBadge = (status: InventoryStatus) => {
    switch (status) {
      case 'ACTIVE':
        return <Badge className="bg-green-500">Active</Badge>
      case 'INACTIVE':
        return <Badge variant="secondary">Inactive</Badge>
      case 'DISCONTINUED':
        return <Badge variant="secondary" className="bg-gray-400">Discontinued</Badge>
      case 'QUARANTINED':
        return <Badge className="bg-orange-500">Quarantined</Badge>
      case 'EXPIRED':
        return <Badge variant="destructive">Expired</Badge>
      default:
        return <Badge variant="secondary">{status}</Badge>
    }
  }

  // Get change type badge
  const getChangeTypeBadge = (type: string) => {
    switch (type) {
      case 'STOCK_IN':
        return <Badge className="bg-green-500">Stock In</Badge>
      case 'STOCK_OUT':
        return <Badge className="bg-red-500">Stock Out</Badge>
      case 'ADJUSTMENT':
        return <Badge className="bg-blue-500">Adjustment</Badge>
      case 'RETURN':
        return <Badge className="bg-purple-500">Return</Badge>
      case 'SALE':
        return <Badge className="bg-orange-500">Sale</Badge>
      default:
        return <Badge variant="secondary">{type}</Badge>
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

  const stockPercentage = currentItem?.maximumStock 
    ? (currentItem?.currentStock / currentItem?.maximumStock) * 100
    : 0

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex flex-col sm:flex-row justify-between items-start sm:items-center gap-4">
        <div className="flex items-center gap-4">
          <Button variant="ghost" onClick={() => navigate('/inventory')}>
            <ArrowLeft className="mr-2 h-4 w-4" />
            Back
          </Button>
          <div>
            <h1 className="text-3xl font-bold tracking-tight">{currentItem?.productName}</h1>
            <p className="text-muted-foreground mt-1">
              {currentItem?.product?.category || 'Uncategorized'}
            </p>
          </div>
        </div>
        <div className="flex gap-2">
          {canAdjustStock && (
            <>
              <Button
                variant="outline"
                onClick={() => {
                  setAdjustQuantity(currentItem?.currentStock.toString())
                  setAdjustStockOpen(true)
                }}
              >
                <Minus className="mr-2 h-4 w-4" />
                Adjust Stock
              </Button>
              {currentItem?.reservedStock > 0 && (
                <Button
                  variant="outline"
                  onClick={() => {
                    setReleaseQuantity(currentItem?.reservedStock.toString())
                    setReleaseStockOpen(true)
                  }}
                >
                  <Unlock className="mr-2 h-4 w-4" />
                  Release Reserved
                </Button>
              )}
              <Button
                variant="outline"
                onClick={() => setReserveStockOpen(true)}
              >
                <Lock className="mr-2 h-4 w-4" />
                Reserve Stock
              </Button>
            </>
          )}
          {canManageInventory && (
            <Button onClick={() => navigate(`/inventory/${inventoryId}/edit`)}>
              <Edit className="mr-2 h-4 w-4" />
              Edit Settings
            </Button>
          )}
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

      {/* Stock Overview Cards */}
      <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-4">
        <Card>
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium">Current Stock</CardTitle>
            <Package className="h-4 w-4 text-primary" />
          </CardHeader>
          <CardContent>
            <div className={`text-2xl font-bold ${getStockLevelColor(currentItem)}`}>
              {currentItem?.currentStock}
            </div>
            <div className="mt-2">
              {getStockLevelBadge(currentItem)}
            </div>
          </CardContent>
        </Card>

        <Card>
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium">Reserved Stock</CardTitle>
            <Lock className="h-4 w-4 text-orange-600" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold text-orange-600">
              {currentItem?.reservedStock}
            </div>
            <p className="text-xs text-muted-foreground mt-1">
              Allocated for orders
            </p>
          </CardContent>
        </Card>

        <Card>
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium">Available Stock</CardTitle>
            <TrendingUp className="h-4 w-4 text-green-600" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold text-green-600">
              {currentItem?.availableStock}
            </div>
            <p className="text-xs text-muted-foreground mt-1">
              Ready to sell
            </p>
          </CardContent>
        </Card>

        <Card>
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium">Inventory Value</CardTitle>
            <DollarSign className="h-4 w-4 text-blue-600" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold">
              {currentItem?.unitCost 
                ? formatCurrency(currentItem?.currentStock * currentItem?.unitCost)
                : '—'}
            </div>
            <p className="text-xs text-muted-foreground mt-1">
              Total value
            </p>
          </CardContent>
        </Card>
      </div>

      {/* Product & Inventory Details */}
      <div className="grid gap-6 md:grid-cols-2">
        {/* Product Information */}
        <Card>
          <CardHeader>
            <CardTitle>Product Information</CardTitle>
            <CardDescription>Details about this product</CardDescription>
          </CardHeader>
          <CardContent className="space-y-4">
            <div className="grid grid-cols-2 gap-4">
              <div>
                <Label className="text-muted-foreground">Product Name</Label>
                <p className="font-medium">{currentItem?.productName}</p>
              </div>
              <div>
                <Label className="text-muted-foreground">Category</Label>
                <p className="font-medium">{currentItem?.product?.category || '—'}</p>
              </div>
            </div>

            {currentItem?.product?.description && (
              <div>
                <Label className="text-muted-foreground">Description</Label>
                <p className="text-sm">{currentItem?.product?.description}</p>
              </div>
            )}

            <Separator />

            <div className="grid grid-cols-2 gap-4">
              {currentItem?.productSku && (
                <div>
                  <Label className="text-muted-foreground">SKU</Label>
                  <p className="font-mono text-sm">{currentItem?.productSku}</p>
                </div>
              )}
              {currentItem?.product?.barcode && (
                <div>
                  <Label className="text-muted-foreground">Barcode</Label>
                  <p className="font-mono text-sm">{currentItem?.product?.barcode}</p>
                </div>
              )}
            </div>

            <div className="grid grid-cols-2 gap-4">
              <div>
                <Label className="text-muted-foreground">Retail Price</Label>
                <p className="font-medium">{formatCurrency(currentItem?.sellingPrice)}</p>
              </div>
              {currentItem?.sellingPrice && (
                <div>
                  <Label className="text-muted-foreground">Unit Cost</Label>
                  <p className="font-medium">{formatCurrency(currentItem?.costPrice)}</p>
                </div>
              )}
            </div>
          </CardContent>
        </Card>

        {/* Inventory Settings */}
        <Card>
          <CardHeader>
            <CardTitle>Inventory Settings</CardTitle>
            <CardDescription>Stock management configuration</CardDescription>
          </CardHeader>
          <CardContent className="space-y-4">
            <div className="grid grid-cols-2 gap-4">
              <div>
                <Label className="text-muted-foreground">Minimum Stock</Label>
                <p className="font-medium text-red-600">{currentItem?.minimumStock}</p>
              </div>
              <div>
                <Label className="text-muted-foreground">Reorder Point</Label>
                <p className="font-medium text-yellow-600">{currentItem?.reorderPoint}</p>
              </div>
            </div>

            {currentItem?.maximumStock && (
              <div>
                <Label className="text-muted-foreground">Maximum Stock</Label>
                <p className="font-medium">{currentItem?.maximumStock}</p>
                <div className="mt-2 h-2 bg-gray-200 rounded-full overflow-hidden">
                  <div
                    className="h-full bg-blue-500"
                    style={{ width: `${Math.min(stockPercentage, 100)}%` }}
                  />
                </div>
                <p className="text-xs text-muted-foreground mt-1">
                  {stockPercentage.toFixed(1)}% capacity
                </p>
              </div>
            )}

            <Separator />

            <div className="grid grid-cols-2 gap-4">
              {currentItem?.location && (
                <div>
                  <Label className="text-muted-foreground">Location</Label>
                  <div className="flex items-center gap-2">
                    <MapPin className="h-4 w-4 text-muted-foreground" />
                    <p className="font-medium">{currentItem?.location}</p>
                  </div>
                </div>
              )}
              {currentItem?.batchNumber && (
                <div>
                  <Label className="text-muted-foreground">Batch Number</Label>
                  <p className="font-mono text-sm">{currentItem?.batchNumber}</p>
                </div>
              )}
            </div>

            {currentItem?.expiryDate && (
              <div>
                <Label className="text-muted-foreground">Expiry Date</Label>
                <div className="flex items-center gap-2">
                  <Calendar className="h-4 w-4 text-muted-foreground" />
                  <p className="font-medium">
                    {format(new Date(currentItem?.expiryDate), 'PPP')}
                  </p>
                </div>
                {currentItem?.isExpired && (
                  <Badge variant="destructive" className="mt-2">Expired</Badge>
                )}
                {currentItem?.isExpiringSoon && !currentItem?.isExpired && (
                  <Badge className="bg-orange-500 mt-2">Expiring Soon</Badge>
                )}
              </div>
            )}

            <Separator />

            <div className="grid grid-cols-2 gap-4">
              <div>
                <Label className="text-muted-foreground">Status</Label>
                <div className="mt-1">
                  {getStatusBadge(currentItem?.status)}
                </div>
              </div>
              <div>
                <Label className="text-muted-foreground">Last Updated</Label>
                <p className="text-sm">
                  {format(new Date(currentItem?.lastStockUpdate), 'PPp')}
                </p>
              </div>
            </div>
          </CardContent>
        </Card>
      </div>

      {/* Stock History */}
      <Card>
        <CardHeader>
          <div className="flex items-center justify-between">
            <div>
              <CardTitle>Stock History</CardTitle>
              <CardDescription>Recent stock movements and adjustments</CardDescription>
            </div>
            <History className="h-5 w-5 text-muted-foreground" />
          </div>
        </CardHeader>
        <CardContent>
          {isLoading && inventoryHistory.length === 0 ? (
            <div className="flex justify-center py-8">
              <Loader2 className="h-6 w-6 animate-spin text-muted-foreground" />
            </div>
          ) : inventoryHistory.length === 0 ? (
            <div className="text-center py-8 text-muted-foreground">
              No stock history available
            </div>
          ) : (
            <Table>
              <TableHeader>
                <TableRow>
                  <TableHead>Date & Time</TableHead>
                  <TableHead>Type</TableHead>
                  <TableHead className="text-right">Change</TableHead>
                  <TableHead className="text-right">Previous</TableHead>
                  <TableHead className="text-right">New</TableHead>
                  <TableHead>Reason</TableHead>
                  <TableHead>Performed By</TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                {inventoryHistory.map((history) => (
                  <TableRow key={history.id}>
                    <TableCell className="text-sm">
                      {format(new Date(history.createdAt), 'PPp')}
                    </TableCell>
                    <TableCell>
                      {getChangeTypeBadge(history.changeType)}
                    </TableCell>
                    <TableCell className="text-right">
                      <span className={history.quantityChange >= 0 ? 'text-green-600' : 'text-red-600'}>
                        {history.quantityChange >= 0 ? '+' : ''}
                        {history.quantityChange}
                      </span>
                    </TableCell>
                    <TableCell className="text-right font-mono text-sm">
                      {history.previousStock}
                    </TableCell>
                    <TableCell className="text-right font-mono text-sm font-semibold">
                      {history.newStock}
                    </TableCell>
                    <TableCell className="text-sm max-w-xs truncate">
                      {history.reason || '—'}
                    </TableCell>
                    <TableCell className="text-sm">
                      {history.performedBy || '—'}
                    </TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>
          )}
        </CardContent>
      </Card>

      {/* Adjust Stock Dialog */}
      <Dialog open={adjustStockOpen} onOpenChange={setAdjustStockOpen}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle>Adjust Stock</DialogTitle>
            <DialogDescription>
              Update stock levels for {currentItem?.productName}
            </DialogDescription>
          </DialogHeader>
          <div className="space-y-4 py-4">
            <div className="space-y-2">
              <Label>Current Stock</Label>
              <div className="text-2xl font-bold">
                {currentItem?.currentStock} units
              </div>
            </div>

            <div className="space-y-2">
              <Label htmlFor="adjust-quantity">New Quantity</Label>
              <Input
                id="adjust-quantity"
                type="number"
                value={adjustQuantity}
                onChange={(e) => setAdjustQuantity(e.target.value)}
                placeholder="Enter new stock quantity"
                min="0"
              />
            </div>

            <div className="space-y-2">
              <Label htmlFor="adjust-reason">Reason</Label>
              <Textarea
                id="adjust-reason"
                value={adjustReason}
                onChange={(e) => setAdjustReason(e.target.value)}
                placeholder="Why is this stock adjustment being made?"
                rows={3}
              />
            </div>
          </div>
          <DialogFooter>
            <Button
              variant="outline"
              onClick={() => {
                setAdjustStockOpen(false)
                setAdjustQuantity('')
                setAdjustReason('')
              }}
            >
              Cancel
            </Button>
            <Button
              onClick={handleAdjustStock}
              disabled={!adjustQuantity || !adjustReason}
            >
              Adjust Stock
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>

      {/* Reserve Stock Dialog */}
      <Dialog open={reserveStockOpen} onOpenChange={setReserveStockOpen}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle>Reserve Stock</DialogTitle>
            <DialogDescription>
              Reserve stock for {currentItem?.productName}
            </DialogDescription>
          </DialogHeader>
          <div className="space-y-4 py-4">
            <div className="grid grid-cols-2 gap-4">
              <div className="space-y-2">
                <Label>Available Stock</Label>
                <div className="text-2xl font-bold text-green-600">
                  {currentItem?.availableStock}
                </div>
              </div>
              <div className="space-y-2">
                <Label>Already Reserved</Label>
                <div className="text-2xl font-bold text-orange-600">
                  {currentItem?.reservedStock}
                </div>
              </div>
            </div>

            <div className="space-y-2">
              <Label htmlFor="reserve-quantity">Quantity to Reserve</Label>
              <Input
                id="reserve-quantity"
                type="number"
                value={reserveQuantity}
                onChange={(e) => setReserveQuantity(e.target.value)}
                placeholder="Enter quantity"
                min="1"
                max={currentItem?.availableStock}
              />
            </div>

            <div className="space-y-2">
              <Label htmlFor="reserve-reason">Reason (optional)</Label>
              <Textarea
                id="reserve-reason"
                value={reserveReason}
                onChange={(e) => setReserveReason(e.target.value)}
                placeholder="Why is this stock being reserved?"
                rows={2}
              />
            </div>
          </div>
          <DialogFooter>
            <Button
              variant="outline"
              onClick={() => {
                setReserveStockOpen(false)
                setReserveQuantity('')
                setReserveReason('')
              }}
            >
              Cancel
            </Button>
            <Button
              onClick={handleReserveStock}
              disabled={
                !reserveQuantity ||
                 Number.parseInt(reserveQuantity) > currentItem?.availableStock
              }
            >
              Reserve Stock
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>

      {/* Release Stock Dialog */}
      <Dialog open={releaseStockOpen} onOpenChange={setReleaseStockOpen}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle>Release Reserved Stock</DialogTitle>
            <DialogDescription>
              Release reserved stock for {currentItem?.productName}
            </DialogDescription>
          </DialogHeader>
          <div className="space-y-4 py-4">
            <div className="space-y-2">
              <Label>Currently Reserved</Label>
              <div className="text-2xl font-bold text-orange-600">
                {currentItem?.reservedStock} units
              </div>
            </div>

            <div className="space-y-2">
              <Label htmlFor="release-quantity">Quantity to Release</Label>
              <Input
                id="release-quantity"
                type="number"
                value={releaseQuantity}
                onChange={(e) => setReleaseQuantity(e.target.value)}
                placeholder="Enter quantity"
                min="1"
                max={currentItem?.reservedStock}
              />
            </div>
          </div>
          <DialogFooter>
            <Button
              variant="outline"
              onClick={() => {
                setReleaseStockOpen(false)
                setReleaseQuantity('')
              }}
            >
              Cancel
            </Button>
            <Button
              onClick={handleReleaseStock}
              disabled={
                !releaseQuantity ||
                Number.parseInt(releaseQuantity) > currentItem?.reservedStock
              }
            >
              Release Stock
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>
    </div>
  )
}
