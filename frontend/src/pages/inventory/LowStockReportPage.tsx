import React, { useState, useEffect } from 'react'
import { Link } from 'react-router-dom'
import { Button } from '@/components/ui/button'
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card'
import { Badge } from '@/components/ui/badge'
import { Alert, AlertDescription } from '@/components/ui/alert'
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from '@/components/ui/table'
import {
  ArrowLeft,
  AlertTriangle,
  Package,
  TrendingDown,
  Loader2,
  AlertCircle,
  Eye,
  Plus,
  Download,
  FileDown,
} from 'lucide-react'
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuTrigger,
} from '@/components/ui/dropdown-menu'
import { useInventory } from '@/hooks/useInventory'
import { useAuth } from '@/context/ManualAuthContext'
import { useCurrency } from '@/hooks/useCurrency'
import { downloadCSV, exportToPDF, formatLowStockForExport } from '@/lib/exportHelpers'

export const LowStockReportPage: React.FC = () => {
  const { user } = useAuth()
  const { formatCurrency } = useCurrency()
  const shopId = user?.shopId || ''

  const {
    isLoading,
    error,
    canViewInventory,
    getLowStockAlerts,
    clearError,
  } = useInventory()

  const [lowStockItems, setLowStockItems] = useState<any[]>([])

  // Load low stock data on mount
  useEffect(() => {
    if (shopId && canViewInventory) {
      loadLowStockItems()
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [shopId, canViewInventory])

  const loadLowStockItems = async () => {
    const items = await getLowStockAlerts(shopId)
    setLowStockItems(items)
  }

  // Calculate stats
  const criticalCount = (lowStockItems || []).filter(item => item.currentStock < item.minimumStock).length
  const lowCount = (lowStockItems || []).filter(item => item.currentStock >= item.minimumStock && item.isLowStock).length
  const totalValue = (lowStockItems || []).reduce((sum, item) => 
    sum + (item.unitCost ? item.currentStock * item.unitCost : 0), 0
  )

  // Handle export
  const handleExport = (format: 'csv' | 'pdf') => {
    if (!lowStockItems || lowStockItems.length === 0) {
      alert('No data to export')
      return
    }
    
    const filename = `low-stock-report-${new Date().toISOString().split('T')[0]}`
    
    if (format === 'csv') {
      // Export to CSV
      const formattedData = formatLowStockForExport(lowStockItems)
      downloadCSV(formattedData, `${filename}.csv`)
    } else {
      // Export to PDF
      exportToPDF('low-stock-content', 'Low Stock Report')
    }
  }

  // Get stock level badge
  const getStockLevelBadge = (item: any) => {
    if (item.currentStock < item.minimumStock) {
      return <Badge variant="destructive">Critical</Badge>
    }
    return <Badge className="bg-yellow-500">Low Stock</Badge>
  }

  // Calculate days until out of stock (simple estimation)
  const estimateDaysUntilOut = (item: any): string => {
    // This is a simplified calculation
    // In a real app, you'd use historical sales data
    // For now, estimate based on reorder point and stock levels
    const avgDailyUsage = item.reorderPoint 
      ? Math.max(1, Math.floor((item.maximumStock - item.reorderPoint) / 30))
      : 2 // Default fallback
    
    if (avgDailyUsage <= 0 || item.currentStock <= 0) return '—'
    
    const days = Math.floor(item.currentStock / avgDailyUsage)
    if (days === 0) return 'Today'
    if (days === 1) return 'Tomorrow'
    return `${days} days`
  }

  // Calculate suggested reorder quantity
  const getSuggestedReorder = (item: any): number => {
    if (item.maximumStock) {
      return item.maximumStock - item.currentStock
    }
    return item.reorderPoint * 2
  }

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex flex-col sm:flex-row justify-between items-start sm:items-center gap-4">
        <div className="flex items-center gap-4">
          <Link to="/inventory">
            <Button variant="ghost">
              <ArrowLeft className="mr-2 h-4 w-4" />
              Back to Inventory
            </Button>
          </Link>
          <div>
            <h1 className="text-3xl font-bold tracking-tight">Low Stock Report</h1>
            <p className="text-muted-foreground mt-1">
              Items that need reordering
            </p>
          </div>
        </div>
        <DropdownMenu>
          <DropdownMenuTrigger asChild>
            <Button variant="outline">
              <Download className="mr-2 h-4 w-4" />
              Export Report
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

      {/* Exportable Content */}
      <div id="low-stock-content">
      {/* Summary Cards */}
      <div className="grid gap-4 md:grid-cols-3">
        <Card>
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium">Total Low Stock Items</CardTitle>
            <AlertTriangle className="h-4 w-4 text-yellow-600" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold text-yellow-600">
              {lowStockItems.length}
            </div>
            <p className="text-xs text-muted-foreground mt-1">
              Items need attention
            </p>
          </CardContent>
        </Card>

        <Card>
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium">Critical Items</CardTitle>
            <Package className="h-4 w-4 text-red-600" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold text-red-600">
              {criticalCount}
            </div>
            <p className="text-xs text-muted-foreground mt-1">
              Below minimum stock
            </p>
          </CardContent>
        </Card>

        <Card>
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium">Total Value at Risk</CardTitle>
            <TrendingDown className="h-4 w-4 text-orange-600" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold">
              {formatCurrency(totalValue)}
            </div>
            <p className="text-xs text-muted-foreground mt-1">
              Low stock inventory value
            </p>
          </CardContent>
        </Card>
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
      {isLoading && (
        <div className="flex justify-center items-center py-12">
          <Loader2 className="h-8 w-8 animate-spin text-muted-foreground" />
        </div>
      )}

      {/* Empty State */}
      {!isLoading && lowStockItems.length === 0 && (
        <Card>
          <CardContent className="flex flex-col items-center justify-center py-12">
            <Package className="h-12 w-12 text-green-600 mb-4" />
            <h3 className="text-lg font-semibold mb-2">All stock levels are adequate!</h3>
            <p className="text-muted-foreground text-center mb-4">
              No items are currently below their reorder point
            </p>
            <Link to="/inventory">
              <Button variant="outline">
                <ArrowLeft className="mr-2 h-4 w-4" />
                Back to Inventory
              </Button>
            </Link>
          </CardContent>
        </Card>
      )}

      {/* Low Stock Items Table */}
      {!isLoading && lowStockItems.length > 0 && (
        <Card>
          <CardHeader>
            <CardTitle>Low Stock Items ({lowStockItems.length})</CardTitle>
            <CardDescription>
              Items sorted by urgency - critical items first
            </CardDescription>
          </CardHeader>
          <CardContent className="p-0">
            <Table>
              <TableHeader>
                <TableRow>
                  <TableHead>Product</TableHead>
                  <TableHead className="text-right">Current</TableHead>
                  <TableHead className="text-right">Minimum</TableHead>
                  <TableHead className="text-right">Reorder Point</TableHead>
                  <TableHead className="text-right">Suggested Reorder</TableHead>
                  <TableHead>Status</TableHead>
                  <TableHead>Est. Days Until Out</TableHead>
                  <TableHead className="text-right">Actions</TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                {lowStockItems
                  .sort((a, b) => {
                    // Sort by urgency: critical first, then by stock level
                    const aCritical = a.currentStock < a.minimumStock
                    const bCritical = b.currentStock < b.minimumStock
                    if (aCritical && !bCritical) return -1
                    if (!aCritical && bCritical) return 1
                    return a.currentStock - b.currentStock
                  })
                  .map((item) => (
                    <TableRow key={item.id}>
                      <TableCell>
                        <div>
                          <div className="font-semibold">{item.productName}</div>
                          {item.productSku && (
                            <div className="text-xs text-muted-foreground font-mono">
                              {item.productSku}
                            </div>
                          )}
                        </div>
                      </TableCell>
                      <TableCell className="text-right">
                        <span className={
                          item.currentStock < item.minimumStock 
                            ? 'text-red-600 font-bold' 
                            : 'text-yellow-600 font-semibold'
                        }>
                          {item.currentStock}
                        </span>
                      </TableCell>
                      <TableCell className="text-right text-red-600">
                        {item.minimumStock}
                      </TableCell>
                      <TableCell className="text-right text-yellow-600">
                        {item.reorderPoint}
                      </TableCell>
                      <TableCell className="text-right">
                        <span className="font-semibold text-green-600">
                          {getSuggestedReorder(item)}
                        </span>
                      </TableCell>
                      <TableCell>
                        {getStockLevelBadge(item)}
                      </TableCell>
                      <TableCell>
                        <span className="text-sm text-muted-foreground">
                          {estimateDaysUntilOut(item)}
                        </span>
                      </TableCell>
                      <TableCell className="text-right">
                        <Link to={`/inventory/${item.id}`}>
                          <Button variant="ghost" size="sm">
                            <Eye className="h-4 w-4" />
                          </Button>
                        </Link>
                      </TableCell>
                    </TableRow>
                  ))}
              </TableBody>
            </Table>
          </CardContent>
        </Card>
      )}

      {/* Action Recommendations */}
      {!isLoading && lowStockItems.length > 0 && (
        <Card>
          <CardHeader>
            <CardTitle>Recommended Actions</CardTitle>
            <CardDescription>Steps to resolve low stock issues</CardDescription>
          </CardHeader>
          <CardContent>
            <div className="space-y-3">
              {criticalCount > 0 && (
                <Alert variant="destructive">
                  <AlertTriangle className="h-4 w-4" />
                  <AlertDescription>
                    <strong>{criticalCount} items are critically low!</strong> These items are below minimum stock and should be reordered immediately.
                  </AlertDescription>
                </Alert>
              )}
              
              <div className="space-y-2">
                <h4 className="font-medium">Quick Actions:</h4>
                <ul className="list-disc list-inside space-y-1 text-sm text-muted-foreground">
                  <li>Review suggested reorder quantities in the table above</li>
                  <li>Contact suppliers for items marked as critical</li>
                  <li>Consider alternative suppliers for frequently low items</li>
                  <li>Review and adjust reorder points if items run low often</li>
                  <li>Export this report to share with your procurement team</li>
                </ul>
              </div>
            </div>
          </CardContent>
        </Card>
      )}
      </div>
    </div>
  )
}
