import React, { useState, useEffect } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { Button } from '@/components/ui/button'
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card'
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
  ArrowLeft,
  Calendar,
  AlertTriangle,
  Package,
  DollarSign,
  Loader2,
  AlertCircle,
  Eye,
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
import { useAuth } from '@/context/UnifiedAuthContext'
import { useShopContext } from '@/context/ShopContext'
import { ShopSelector } from '@/components/ui/shop-selector'
import { useCurrency } from '@/hooks/useCurrency'
import { format, differenceInDays } from 'date-fns'
import { downloadCSV, exportToPDF, formatExpiringItemsForExport } from '@/lib/exportHelpers'

export const ExpiringItemsPage: React.FC = () => {
  const navigate = useNavigate()
  const { user } = useAuth()
  const { selectedShopId, setSelectedShopId, canManageMultipleShops } = useShopContext()
  const { formatCurrency } = useCurrency()
  const shopId = selectedShopId || user?.shopId || ''

  const {
    isLoading,
    error,
    canViewInventory,
    getExpiringItems,
    clearError,
  } = useInventory()

  const [expiringItems, setExpiringItems] = useState<any[]>([])
  const [daysThreshold, setDaysThreshold] = useState('30')

  // Load expiring items data on mount and when threshold changes
  useEffect(() => {
    if (shopId && canViewInventory) {
      loadExpiringItems()
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [shopId, daysThreshold, canViewInventory])

  const loadExpiringItems = async () => {
    const items = await getExpiringItems(shopId, parseInt(daysThreshold))
    setExpiringItems(items)
  }

  // Calculate stats
  const expiredCount = (expiringItems || []).filter(item => item.isExpired).length
  const expiringSoonCount = (expiringItems || []).filter(item => item.isExpiringSoon && !item.isExpired).length
  const totalValue = (expiringItems || []).reduce((sum, item) => 
    sum + (item.unitCost ? item.currentStock * item.unitCost : 0), 0
  )

  // Handle export
  const handleExport = (format: 'csv' | 'pdf') => {
    if (!expiringItems || expiringItems.length === 0) {
      alert('No data to export')
      return
    }
    
    const filename = `expiring-items-${daysThreshold}days-${new Date().toISOString().split('T')[0]}`
    
    if (format === 'csv') {
      // Export to CSV
      const formattedData = formatExpiringItemsForExport(expiringItems)
      downloadCSV(formattedData, `${filename}.csv`)
    } else {
      // Export to PDF
      exportToPDF('expiring-items-content', `Expiring Items Report (${daysThreshold} days)`)
    }
  }

  // Get days until expiry
  const getDaysUntilExpiry = (expiryDate: string): number => {
    return differenceInDays(new Date(expiryDate), new Date())
  }

  // Get urgency badge
  const getUrgencyBadge = (item: any) => {
    if (item.isExpired) {
      return <Badge variant="destructive">Expired</Badge>
    }
    
    const days = getDaysUntilExpiry(item.expiryDate)
    if (days <= 7) {
      return <Badge variant="destructive">Critical (≤7 days)</Badge>
    }
    if (days <= 14) {
      return <Badge className="bg-orange-500">High (≤14 days)</Badge>
    }
    if (days <= 30) {
      return <Badge className="bg-yellow-500">Medium (≤30 days)</Badge>
    }
    return <Badge variant="secondary">Low (&gt;30 days)</Badge>
  }

  // Get recommended action
  const getRecommendedAction = (item: any): string => {
    if (item.isExpired) return 'Remove from sale'
    
    const days = getDaysUntilExpiry(item.expiryDate)
    if (days <= 3) return 'Immediate clearance'
    if (days <= 7) return 'Apply 50% discount'
    if (days <= 14) return 'Apply 25% discount'
    if (days <= 30) return 'Promote product'
    return 'Monitor closely'
  }

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex flex-col sm:flex-row justify-between items-start sm:items-center gap-4">
        <div className="flex items-center gap-4">
          <Button variant="ghost" onClick={() => navigate(-1)}>
            <ArrowLeft className="mr-2 h-4 w-4" />
            Back
          </Button>
          <div>
            <h1 className="text-3xl font-bold tracking-tight">Expiring Items</h1>
            <p className="text-muted-foreground mt-1">
              Items that are expired or expiring soon
            </p>
          </div>
        </div>
        <div className="flex gap-2">
          {canManageMultipleShops && selectedShopId && (
            <ShopSelector
              value={selectedShopId}
              onValueChange={setSelectedShopId}
              className="w-[200px]"
            />
          )}
          
          <Select value={daysThreshold} onValueChange={setDaysThreshold}>
            <SelectTrigger className="w-[180px]">
              <SelectValue placeholder="Threshold" />
            </SelectTrigger>
            <SelectContent>
              <SelectItem value="7">Next 7 days</SelectItem>
              <SelectItem value="14">Next 14 days</SelectItem>
              <SelectItem value="30">Next 30 days</SelectItem>
              <SelectItem value="60">Next 60 days</SelectItem>
              <SelectItem value="90">Next 90 days</SelectItem>
            </SelectContent>
          </Select>
          
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
      <div id="expiring-items-content" className="space-y-6">
      {/* Summary Cards */}
      <div className="grid gap-4 md:grid-cols-3">
        <Card>
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium">Expired Items</CardTitle>
            <AlertTriangle className="h-4 w-4 text-red-600" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold text-red-600">
              {expiredCount}
            </div>
            <p className="text-xs text-muted-foreground mt-1">
              Must be removed
            </p>
          </CardContent>
        </Card>

        <Card>
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium">Expiring Soon</CardTitle>
            <Calendar className="h-4 w-4 text-orange-600" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold text-orange-600">
              {expiringSoonCount}
            </div>
            <p className="text-xs text-muted-foreground mt-1">
              Within {daysThreshold} days
            </p>
          </CardContent>
        </Card>

        <Card>
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium">Value at Risk</CardTitle>
            <DollarSign className="h-4 w-4 text-yellow-600" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold">
              {formatCurrency(totalValue)}
            </div>
            <p className="text-xs text-muted-foreground mt-1">
              Total inventory value
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
      {!isLoading && expiringItems.length === 0 && (
        <Card>
          <CardContent className="flex flex-col items-center justify-center py-12">
            <Package className="h-12 w-12 text-green-600 mb-4" />
            <h3 className="text-lg font-semibold mb-2">No expiring items!</h3>
            <p className="text-muted-foreground text-center mb-4">
              No items are expiring within the next {daysThreshold} days
            </p>
            <Button variant="outline" onClick={() => navigate(-1)}>
              <ArrowLeft className="mr-2 h-4 w-4" />
              Back
            </Button>
          </CardContent>
        </Card>
      )}

      {/* Expiring Items Table */}
      {!isLoading && expiringItems.length > 0 && (
        <Card>
          <CardHeader>
            <CardTitle>Expiring Items ({expiringItems.length})</CardTitle>
            <CardDescription>
              Items sorted by expiry date - most urgent first
            </CardDescription>
          </CardHeader>
          <CardContent className="p-0">
            <Table>
              <TableHeader>
                <TableRow>
                  <TableHead>Product</TableHead>
                  <TableHead>Batch #</TableHead>
                  <TableHead className="text-right">Quantity</TableHead>
                  <TableHead className="text-right">Value</TableHead>
                  <TableHead>Expiry Date</TableHead>
                  <TableHead className="text-right">Days Left</TableHead>
                  <TableHead>Urgency</TableHead>
                  <TableHead>Recommended Action</TableHead>
                  <TableHead className="text-right">Actions</TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                {expiringItems
                  .sort((a, b) => {
                    // Sort by expiry date (soonest first)
                    return new Date(a.expiryDate).getTime() - new Date(b.expiryDate).getTime()
                  })
                  .map((item) => {
                    const daysLeft = getDaysUntilExpiry(item.expiryDate)
                    const itemValue = item.unitCost ? item.currentStock * item.unitCost : 0
                    
                    return (
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
                        <TableCell>
                          <span className="font-mono text-sm">
                            {item.batchNumber || '—'}
                          </span>
                        </TableCell>
                        <TableCell className="text-right">
                          <span className="font-semibold">
                            {item.currentStock}
                          </span>
                        </TableCell>
                        <TableCell className="text-right">
                          {itemValue > 0 ? formatCurrency(itemValue) : '—'}
                        </TableCell>
                        <TableCell>
                          <div className="flex items-center gap-2">
                            <Calendar className="h-4 w-4 text-muted-foreground" />
                            <span className={item.isExpired ? 'text-red-600 font-semibold' : ''}>
                              {format(new Date(item.expiryDate), 'PPP')}
                            </span>
                          </div>
                        </TableCell>
                        <TableCell className="text-right">
                          {item.isExpired ? (
                            <span className="text-red-600 font-bold">Expired</span>
                          ) : (
                            <span className={
                              daysLeft <= 7 
                                ? 'text-red-600 font-bold' 
                                : daysLeft <= 14
                                ? 'text-orange-600 font-semibold'
                                : 'text-yellow-600'
                            }>
                              {daysLeft} {daysLeft === 1 ? 'day' : 'days'}
                            </span>
                          )}
                        </TableCell>
                        <TableCell>
                          {getUrgencyBadge(item)}
                        </TableCell>
                        <TableCell>
                          <span className="text-sm font-medium">
                            {getRecommendedAction(item)}
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
                    )
                  })}
              </TableBody>
            </Table>
          </CardContent>
        </Card>
      )}

      {/* Action Recommendations */}
      {!isLoading && expiringItems.length > 0 && (
        <Card>
          <CardHeader>
            <CardTitle>Recommended Actions</CardTitle>
            <CardDescription>Steps to minimize losses from expiring inventory</CardDescription>
          </CardHeader>
          <CardContent>
            <div className="space-y-3">
              {expiredCount > 0 && (
                <Alert variant="destructive">
                  <AlertTriangle className="h-4 w-4" />
                  <AlertDescription>
                    <strong>{expiredCount} items have already expired!</strong> These should be removed from sale immediately and disposed of properly.
                  </AlertDescription>
                </Alert>
              )}
              
              <div className="space-y-2">
                <h4 className="font-medium">Quick Actions by Priority:</h4>
                <div className="grid gap-3 md:grid-cols-2">
                  <Card className="border-red-200 bg-red-50">
                    <CardHeader className="pb-3">
                      <CardTitle className="text-sm text-red-900">Critical (≤7 days)</CardTitle>
                    </CardHeader>
                    <CardContent>
                      <ul className="list-disc list-inside space-y-1 text-xs text-red-800">
                        <li>Apply 50-75% discount immediately</li>
                        <li>Move to clearance section</li>
                        <li>Consider bulk sales</li>
                        <li>Donate if unsellable</li>
                      </ul>
                    </CardContent>
                  </Card>

                  <Card className="border-orange-200 bg-orange-50">
                    <CardHeader className="pb-3">
                      <CardTitle className="text-sm text-orange-900">High (7-14 days)</CardTitle>
                    </CardHeader>
                    <CardContent>
                      <ul className="list-disc list-inside space-y-1 text-xs text-orange-800">
                        <li>Apply 25-50% discount</li>
                        <li>Promote on social media</li>
                        <li>Create bundle deals</li>
                        <li>Alert loyal customers</li>
                      </ul>
                    </CardContent>
                  </Card>

                  <Card className="border-yellow-200 bg-yellow-50">
                    <CardHeader className="pb-3">
                      <CardTitle className="text-sm text-yellow-900">Medium (14-30 days)</CardTitle>
                    </CardHeader>
                    <CardContent>
                      <ul className="list-disc list-inside space-y-1 text-xs text-yellow-800">
                        <li>Feature in promotions</li>
                        <li>Apply 10-25% discount</li>
                        <li>Increase product visibility</li>
                        <li>Monitor closely</li>
                      </ul>
                    </CardContent>
                  </Card>

                  <Card className="border-blue-200 bg-blue-50">
                    <CardHeader className="pb-3">
                      <CardTitle className="text-sm text-blue-900">General Tips</CardTitle>
                    </CardHeader>
                    <CardContent>
                      <ul className="list-disc list-inside space-y-1 text-xs text-blue-800">
                        <li>Use FIFO (First In, First Out)</li>
                        <li>Review order quantities</li>
                        <li>Track expiry patterns</li>
                        <li>Train staff on rotation</li>
                      </ul>
                    </CardContent>
                  </Card>
                </div>
              </div>
            </div>
          </CardContent>
        </Card>
      )}
      </div>
    </div>
  )
}
