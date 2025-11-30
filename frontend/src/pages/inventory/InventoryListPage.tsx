import React, { useState, useEffect } from 'react'
import { Link, useSearchParams } from 'react-router-dom'
import { Button } from '@/components/ui/button'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import { Input } from '@/components/ui/input'
import { Badge } from '@/components/ui/badge'
import { Alert, AlertDescription } from '@/components/ui/alert'
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/components/ui/select'
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from '@/components/ui/table'
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuLabel,
  DropdownMenuSeparator,
  DropdownMenuTrigger,
} from '@/components/ui/dropdown-menu'
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from '@/components/ui/dialog'
import { Label } from '@/components/ui/label'
import { Textarea } from '@/components/ui/textarea'
import {
  Plus,
  Package,
  AlertCircle,
  Loader2,
  Search,
  MoreVertical,
  Download,
  Edit,
  Eye,
  PackageX,
  TrendingUp,
  TrendingDown,
  Minus,
  Calendar,
  Lock,
  FileDown,
} from 'lucide-react'
import { useInventory, InventoryItem, InventoryStatus } from '@/hooks/useInventory'
import { ShopSelector } from '@/components/ui/shop-selector'
import { useShopContext } from '@/context/ShopContext'
import { useCurrency } from '@/hooks/useCurrency'
import { downloadCSV, exportToPDF, formatInventoryForExport } from '@/lib/exportHelpers'

export const InventoryListPage: React.FC = () => {
  const { selectedShopId, setSelectedShopId, canManageMultipleShops } = useShopContext()
  const { formatCurrency } = useCurrency()
  const shopId = selectedShopId || ''

  const {
    inventory,
    summary,
    isLoading,
    error,
    canManageInventory,
    canAdjustStock,
    fetchInventory,
    fetchInventorySummary,
    adjustStock,
    reserveStock,
    clearError,
  } = useInventory()

  // Get query params for dashboard navigation
  const [searchParams, setSearchParams] = useSearchParams()
  const filterParam = searchParams.get('filter')
  
  // Local state
  const [searchQuery, setSearchQuery] = useState('')
  const [statusFilter, setStatusFilter] = useState<string>('all')
  const [stockLevelFilter, setStockLevelFilter] = useState<string>('all')
  const [selectedItem, setSelectedItem] = useState<InventoryItem | null>(null)
  const [adjustStockOpen, setAdjustStockOpen] = useState(false)
  const [reserveStockOpen, setReserveStockOpen] = useState(false)

  // Adjust stock form
  const [adjustQuantity, setAdjustQuantity] = useState('')
  const [adjustReason, setAdjustReason] = useState('')

  // Reserve stock form
  const [reserveQuantity, setReserveQuantity] = useState('')
  const [reserveReason, setReserveReason] = useState('')

  // Handle filter from query params (e.g., from dashboard links)
  useEffect(() => {
    if (filterParam) {
      switch (filterParam) {
        case 'lowStock':
          setStockLevelFilter('low')
          break
        case 'outOfStock':
          setStockLevelFilter('out')
          break
        case 'expiringSoon':
          // Navigate to dedicated expiring items page
          window.location.href = '/inventory/expiring'
          return
        case 'expired':
          setStatusFilter('EXPIRED')
          break
        default:
          break
      }
      // Clear the query param after applying
      setSearchParams({})
    }
  }, [filterParam, setSearchParams])
  
  // Load data on mount and when filters change
  useEffect(() => {
    if (shopId) {
      const loadData = async () => {
        await Promise.all([
          fetchInventory(shopId),
          fetchInventorySummary(shopId)
        ])
      }
      loadData()
    }
  }, [shopId])

  // Filter inventory
  const filteredInventory = (inventory || []).filter((item) => {
    // Search filter
    if (searchQuery) {
      const query = searchQuery.toLowerCase()
      const matchesSearch =
        item.productName?.toLowerCase().includes(query) ||
        item.productSku?.toLowerCase().includes(query) ||
        // item.productBarcode?.toLowerCase().includes(query) ||
        item.location?.toLowerCase().includes(query)
      
      if (!matchesSearch) return false
    }

    // Status filter
    if (statusFilter !== 'all' && item.status !== statusFilter) {
      return false
    }

    // Stock level filter
    if (stockLevelFilter !== 'all') {
      if (stockLevelFilter === 'low' && !item.isLowStock) return false
      if (stockLevelFilter === 'out' && item.currentStock !== 0) return false
      if (stockLevelFilter === 'adequate' && (item.isLowStock || item.currentStock === 0)) return false
    }

    return true
  })

  // Handle adjust stock
  const handleAdjustStock = async () => {
    if (!selectedItem || !adjustQuantity || !adjustReason) return

    const newStock = parseInt(adjustQuantity)
    if (isNaN(newStock)) return

    const success = await adjustStock(selectedItem.id, {
      newStock,
      reason: adjustReason,
    })

    if (success) {
      setAdjustStockOpen(false)
      setAdjustQuantity('')
      setAdjustReason('')
      setSelectedItem(null)
      // Refresh data
      if (shopId) {
        fetchInventory(shopId)
        fetchInventorySummary(shopId)
      }
    }
  }

  // Handle reserve stock
  const handleReserveStock = async () => {
    if (!selectedItem || !reserveQuantity) return

    const quantity = parseInt(reserveQuantity)
    if (isNaN(quantity)) return

    const success = await reserveStock(selectedItem.id, {
      quantity,
      reason: reserveReason,
    })

    if (success) {
      setReserveStockOpen(false)
      setReserveQuantity('')
      setReserveReason('')
      setSelectedItem(null)
      // Refresh data
      if (shopId) {
        fetchInventory(shopId)
      }
    }
  }

  // Handle export
  const handleExport = (format: 'csv' | 'pdf') => {
    if (!filteredInventory || filteredInventory.length === 0) {
      alert('No data to export')
      return
    }
    
    const filename = `inventory-${new Date().toISOString().split('T')[0]}`
    
    if (format === 'csv') {
      // Export to CSV
      const formattedData = formatInventoryForExport(filteredInventory)
      downloadCSV(formattedData, `${filename}.csv`)
    } else {
      // Export to PDF
      exportToPDF('inventory-content', 'Inventory Report')
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

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex flex-col sm:flex-row justify-between items-start sm:items-center gap-4">
        <div>
          <h1 className="text-3xl font-bold tracking-tight">Inventory</h1>
          <p className="text-muted-foreground mt-1">
            Manage stock levels, reservations, and inventory tracking
          </p>
        </div>
        <div className="flex gap-2 items-center">
          {canManageMultipleShops && selectedShopId && (
            <ShopSelector
              value={selectedShopId}
              onValueChange={setSelectedShopId}
              className="w-[200px]"
            />
          )}
          {canManageInventory && (
            <Link to="/inventory/create">
              <Button>
                <Plus className="mr-2 h-4 w-4" />
                Add Inventory
              </Button>
            </Link>
          )}
          <DropdownMenu>
            <DropdownMenuTrigger asChild>
              <Button variant="outline">
                <Download className="mr-2 h-4 w-4" />
                Export
              </Button>
            </DropdownMenuTrigger>
            <DropdownMenuContent align="end">
              <DropdownMenuItem onClick={() => handleExport('csv')}>
                <FileDown className="mr-2 h-4 w-4" />
                Export as CSV
              </DropdownMenuItem>
              <DropdownMenuItem onClick={() => handleExport('pdf')}>
                <FileDown className="mr-2 h-4 w-4" />
                Export as PDF
              </DropdownMenuItem>
            </DropdownMenuContent>
          </DropdownMenu>
        </div>
      </div>

      {/* Exportable Content */}
      <div id="inventory-content" className="space-y-6">
      {/* Summary Cards */}
      <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-4">
        <Card>
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium">Total Items</CardTitle>
            <Package className="h-4 w-4 text-primary" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold">{summary?.totalItems || 0}</div>
            <p className="text-xs text-muted-foreground mt-1">
              Products in inventory
            </p>
          </CardContent>
        </Card>

        <Card>
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium">Total Value</CardTitle>
            <TrendingUp className="h-4 w-4 text-green-600" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold">
              {formatCurrency(summary?.totalValue || 0)}
            </div>
            <p className="text-xs text-muted-foreground mt-1">
              Inventory worth
            </p>
          </CardContent>
        </Card>

        <Card>
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium">Low Stock</CardTitle>
            <TrendingDown className="h-4 w-4 text-yellow-600" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold text-yellow-600">
              {summary?.lowStockItems || 0}
            </div>
            <p className="text-xs text-muted-foreground mt-1">
              Items need reorder
            </p>
          </CardContent>
        </Card>

        <Card>
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium">Expiring Soon</CardTitle>
            <Calendar className="h-4 w-4 text-red-600" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold text-red-600">
              {summary?.expiringSoonItems || 0}
            </div>
            <p className="text-xs text-muted-foreground mt-1">
              Within 30 days
            </p>
          </CardContent>
        </Card>
      </div>

      {/* Quick Links */}
      <div className="flex gap-2 flex-wrap">
        <Link to="/inventory/low-stock">
          <Button variant="outline" size="sm">
            <AlertCircle className="mr-2 h-4 w-4 text-yellow-600" />
            Low Stock Report
          </Button>
        </Link>
        <Link to="/inventory/expiring">
          <Button variant="outline" size="sm">
            <Calendar className="mr-2 h-4 w-4 text-red-600" />
            Expiring Items
          </Button>
        </Link>
      </div>

      {/* Search and Filters */}
      <div className="flex flex-col sm:flex-row gap-4">
        <div className="relative flex-1 max-w-md">
          <Search className="absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-muted-foreground" />
          <Input
            placeholder="Search by product name, SKU, barcode, location..."
            value={searchQuery}
            onChange={(e) => setSearchQuery(e.target.value)}
            className="pl-9"
          />
        </div>

        <Select value={statusFilter} onValueChange={setStatusFilter}>
          <SelectTrigger className="w-[180px]">
            <SelectValue placeholder="Filter by status" />
          </SelectTrigger>
          <SelectContent>
            <SelectItem value="all">All Statuses</SelectItem>
            <SelectItem value="ACTIVE">Active</SelectItem>
            <SelectItem value="INACTIVE">Inactive</SelectItem>
            <SelectItem value="DISCONTINUED">Discontinued</SelectItem>
            <SelectItem value="QUARANTINED">Quarantined</SelectItem>
            <SelectItem value="EXPIRED">Expired</SelectItem>
          </SelectContent>
        </Select>

        <Select value={stockLevelFilter} onValueChange={setStockLevelFilter}>
          <SelectTrigger className="w-[180px]">
            <SelectValue placeholder="Filter by stock" />
          </SelectTrigger>
          <SelectContent>
            <SelectItem value="all">All Stock Levels</SelectItem>
            <SelectItem value="adequate">Adequate</SelectItem>
            <SelectItem value="low">Low Stock</SelectItem>
            <SelectItem value="out">Out of Stock</SelectItem>
          </SelectContent>
        </Select>
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

      {/* Loading State */}
      {isLoading && inventory.length === 0 && (
        <div className="flex justify-center items-center py-12">
          <Loader2 className="h-8 w-8 animate-spin text-muted-foreground" />
        </div>
      )}

      {/* Empty State */}
      {!isLoading && filteredInventory.length === 0 && (
        <Card>
          <CardContent className="flex flex-col items-center justify-center py-12">
            <Package className="h-12 w-12 text-muted-foreground mb-4" />
            <h3 className="text-lg font-semibold mb-2">
              {searchQuery || statusFilter !== 'all' || stockLevelFilter !== 'all' 
                ? 'No items found' 
                : 'No inventory items yet'}
            </h3>
            <p className="text-muted-foreground text-center mb-4">
              {searchQuery || statusFilter !== 'all' || stockLevelFilter !== 'all'
                ? 'Try adjusting your filters or search criteria'
                : 'Get started by adding your first inventory item'}
            </p>
            {canManageInventory && !searchQuery && statusFilter === 'all' && stockLevelFilter === 'all' && (
              <Link to="/inventory/create">
                <Button>
                  <Plus className="mr-2 h-4 w-4" />
                  Add Inventory
                </Button>
              </Link>
            )}
          </CardContent>
        </Card>
      )}

      {/* Inventory Table */}
      {!isLoading && filteredInventory.length > 0 && (
        <Card>
          <CardContent className="p-0">
            <Table>
              <TableHeader>
                <TableRow>
                  <TableHead>Product</TableHead>
                  <TableHead>SKU</TableHead>
                  <TableHead>Location</TableHead>
                  <TableHead className="text-right">Current</TableHead>
                  <TableHead className="text-right">Reserved</TableHead>
                  <TableHead className="text-right">Available</TableHead>
                  <TableHead>Stock Level</TableHead>
                  <TableHead>Status</TableHead>
                  <TableHead className="text-right">Actions</TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                {filteredInventory?.map((item) => (
                  <TableRow key={item.id}>
                    <TableCell className="font-medium">
                      <div>
                        <div className="font-semibold">{item?.productName}</div>
                        {item?.product?.category && (
                          <div className="text-xs text-muted-foreground">
                            {item?.product?.category}
                          </div>
                        )}
                      </div>
                    </TableCell>
                    <TableCell>
                      <div className="text-sm">
                        {item?.productSku && (
                          <div className="font-mono">{item?.productSku}</div>
                        )}
                      </div>
                    </TableCell>
                    <TableCell>
                      <div className="text-sm">{item.location || '—'}</div>
                    </TableCell>
                    <TableCell className="text-right">
                      <span className={`font-semibold ${getStockLevelColor(item)}`}>
                        {item.currentStock}
                      </span>
                    </TableCell>
                    <TableCell className="text-right">
                      {item.reservedStock > 0 ? (
                        <span className="flex items-center justify-end gap-1 text-orange-600">
                          <Lock className="h-3 w-3" />
                          {item.reservedStock}
                        </span>
                      ) : (
                        <span className="text-muted-foreground">0</span>
                      )}
                    </TableCell>
                    <TableCell className="text-right">
                      <span className="font-semibold">{item.availableStock}</span>
                    </TableCell>
                    <TableCell>{getStockLevelBadge(item)}</TableCell>
                    <TableCell>{getStatusBadge(item.status)}</TableCell>
                    <TableCell className="text-right">
                      <DropdownMenu>
                        <DropdownMenuTrigger asChild>
                          <Button variant="ghost" size="sm">
                            <MoreVertical className="h-4 w-4" />
                          </Button>
                        </DropdownMenuTrigger>
                        <DropdownMenuContent align="end">
                          <DropdownMenuLabel>Actions</DropdownMenuLabel>
                          <DropdownMenuSeparator />
                          <DropdownMenuItem asChild>
                            <Link to={`/inventory/${item.id}`}>
                              <Eye className="mr-2 h-4 w-4" />
                              View Details
                            </Link>
                          </DropdownMenuItem>
                          {canAdjustStock && (
                            <>
                              <DropdownMenuItem
                                onClick={() => {
                                  setSelectedItem(item)
                                  setAdjustQuantity(item.currentStock.toString())
                                  setAdjustStockOpen(true)
                                }}
                              >
                                <Minus className="mr-2 h-4 w-4" />
                                Adjust Stock
                              </DropdownMenuItem>
                              <DropdownMenuItem
                                onClick={() => {
                                  setSelectedItem(item)
                                  setReserveStockOpen(true)
                                }}
                              >
                                <Lock className="mr-2 h-4 w-4" />
                                Reserve Stock
                              </DropdownMenuItem>
                            </>
                          )}
                          {canManageInventory && (
                            <>
                              <DropdownMenuSeparator />
                              <DropdownMenuItem asChild>
                                <Link to={`/inventory/${item.id}/edit`}>
                                  <Edit className="mr-2 h-4 w-4" />
                                  Edit Settings
                                </Link>
                              </DropdownMenuItem>
                            </>
                          )}
                        </DropdownMenuContent>
                      </DropdownMenu>
                    </TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>
          </CardContent>
        </Card>
      )}

      {/* Adjust Stock Dialog */}
      <Dialog open={adjustStockOpen} onOpenChange={setAdjustStockOpen}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle>Adjust Stock</DialogTitle>
            <DialogDescription>
              Update stock levels for {selectedItem?.productName}
            </DialogDescription>
          </DialogHeader>
          <div className="space-y-4 py-4">
            <div className="space-y-2">
              <Label>Current Stock</Label>
              <div className="text-2xl font-bold">
                {selectedItem?.currentStock || 0} units
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
                setSelectedItem(null)
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
              Reserve stock for {selectedItem?.productName}
            </DialogDescription>
          </DialogHeader>
          <div className="space-y-4 py-4">
            <div className="grid grid-cols-2 gap-4">
              <div className="space-y-2">
                <Label>Available Stock</Label>
                <div className="text-2xl font-bold text-green-600">
                  {selectedItem?.availableStock || 0}
                </div>
              </div>
              <div className="space-y-2">
                <Label>Already Reserved</Label>
                <div className="text-2xl font-bold text-orange-600">
                  {selectedItem?.reservedStock || 0}
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
                max={selectedItem?.availableStock || 0}
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
                setSelectedItem(null)
              }}
            >
              Cancel
            </Button>
            <Button
              onClick={handleReserveStock}
              disabled={
                !reserveQuantity ||
                parseInt(reserveQuantity) > (selectedItem?.availableStock || 0)
              }
            >
              Reserve Stock
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>
      </div>
    </div>
  )
}
