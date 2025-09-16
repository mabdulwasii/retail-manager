import React, { useState, useEffect } from 'react'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import { Badge } from '@/components/ui/badge'
import { LoadingSpinner } from '@/components/ui/loading-spinner'
import { useSales, SalesTransaction, SalesFilter } from '@/hooks/useSales'
import { useCurrency } from '@/hooks/useCurrency'
import {
  CalendarIcon,
  SearchIcon,
  ReceiptIcon,
  PrinterIcon,
  EyeIcon,
  FilterIcon,
  RefreshCwIcon,
} from 'lucide-react'

export const SalesHistory: React.FC = () => {
  const { sales, fetchSales, printReceipt, isLoading } = useSales()
  const { formatCurrency } = useCurrency()
  const [filter, setFilter] = useState<SalesFilter>({})
  const [searchQuery, setSearchQuery] = useState('')
  const [selectedTransaction, setSelectedTransaction] = useState<SalesTransaction | null>(null)

  useEffect(() => {
    fetchSales()
  }, [fetchSales])

  const handleFilterChange = (key: keyof SalesFilter, value: string) => {
    const newFilter = { ...filter, [key]: value || undefined }
    setFilter(newFilter)
  }

  const handleSearch = () => {
    const searchFilter: SalesFilter = {
      ...filter,
      ...(searchQuery && { customerId: searchQuery })
    }
    fetchSales(searchFilter)
  }

  const handleClearFilters = () => {
    setFilter({})
    setSearchQuery('')
    fetchSales()
  }

  const getStatusBadge = (status: string) => {
    switch (status) {
      case 'COMPLETED':
        return <Badge className="bg-green-100 text-green-800">Completed</Badge>
      case 'PENDING':
        return <Badge className="bg-yellow-100 text-yellow-800">Pending</Badge>
      case 'CANCELLED':
        return <Badge className="bg-red-100 text-red-800">Cancelled</Badge>
      case 'REFUNDED':
        return <Badge className="bg-gray-100 text-gray-800">Refunded</Badge>
      default:
        return <Badge variant="secondary">{status}</Badge>
    }
  }

  const getPaymentMethodBadge = (method: string) => {
    const variants: Record<string, string> = {
      cash: 'bg-green-100 text-green-800',
      card: 'bg-blue-100 text-blue-800',
      mobile: 'bg-purple-100 text-purple-800',
      bank_transfer: 'bg-indigo-100 text-indigo-800'
    }

    return (
      <Badge className={variants[method] || 'bg-gray-100 text-gray-800'}>
        {method.replace('_', ' ').replace(/\b\w/g, l => l.toUpperCase())}
      </Badge>
    )
  }

  const formatDate = (dateString: string) => {
    return new Date(dateString).toLocaleDateString('en-US', {
      year: 'numeric',
      month: 'short',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    })
  }

  const totalSales = sales.reduce((sum, sale) => sum + sale.totalAmount, 0)
  const completedSales = sales.filter(sale => sale.status === 'COMPLETED').length

  return (
    <div className="space-y-6">
      {/* Summary Cards */}
      <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
        <Card>
          <CardContent className="p-4">
            <div className="flex items-center justify-between">
              <div>
                <p className="text-sm text-gray-600">Total Sales</p>
                <p className="text-2xl font-bold">{sales.length}</p>
              </div>
              <ReceiptIcon className="h-8 w-8 text-blue-600" />
            </div>
          </CardContent>
        </Card>

        <Card>
          <CardContent className="p-4">
            <div className="flex items-center justify-between">
              <div>
                <p className="text-sm text-gray-600">Completed</p>
                <p className="text-2xl font-bold">{completedSales}</p>
              </div>
              <div className="h-8 w-8 rounded-full bg-green-100 flex items-center justify-center">
                <div className="h-4 w-4 rounded-full bg-green-600"></div>
              </div>
            </div>
          </CardContent>
        </Card>

        <Card>
          <CardContent className="p-4">
            <div className="flex items-center justify-between">
              <div>
                <p className="text-sm text-gray-600">Total Revenue</p>
                <p className="text-2xl font-bold">{formatCurrency(totalSales)}</p>
              </div>
              <div className="h-8 w-8 rounded-full bg-green-100 flex items-center justify-center">
                <span className="text-green-600 font-bold">₦</span>
              </div>
            </div>
          </CardContent>
        </Card>
      </div>

      {/* Filters and Search */}
      <Card>
        <CardHeader>
          <CardTitle className="flex items-center space-x-2">
            <FilterIcon className="h-5 w-5" />
            <span>Filters</span>
          </CardTitle>
        </CardHeader>
        <CardContent>
          <div className="grid grid-cols-1 md:grid-cols-3 lg:grid-cols-5 gap-4">
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">
                Start Date
              </label>
              <Input
                type="date"
                value={filter.startDate || ''}
                onChange={(e) => handleFilterChange('startDate', e.target.value)}
              />
            </div>

            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">
                End Date
              </label>
              <Input
                type="date"
                value={filter.endDate || ''}
                onChange={(e) => handleFilterChange('endDate', e.target.value)}
              />
            </div>

            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">
                Status
              </label>
              <select
                value={filter.status || ''}
                onChange={(e) => handleFilterChange('status', e.target.value)}
                className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
              >
                <option value="">All Status</option>
                <option value="COMPLETED">Completed</option>
                <option value="PENDING">Pending</option>
                <option value="CANCELLED">Cancelled</option>
                <option value="REFUNDED">Refunded</option>
              </select>
            </div>

            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">
                Payment Method
              </label>
              <select
                value={filter.paymentMethod || ''}
                onChange={(e) => handleFilterChange('paymentMethod', e.target.value)}
                className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
              >
                <option value="">All Methods</option>
                <option value="cash">Cash</option>
                <option value="card">Card</option>
                <option value="mobile">Mobile Money</option>
                <option value="bank_transfer">Bank Transfer</option>
              </select>
            </div>

            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">
                Search
              </label>
              <div className="flex space-x-2">
                <Input
                  placeholder="Receipt number..."
                  value={searchQuery}
                  onChange={(e) => setSearchQuery(e.target.value)}
                />
              </div>
            </div>
          </div>

          <div className="flex space-x-2 mt-4">
            <Button onClick={handleSearch} disabled={isLoading}>
              <SearchIcon className="h-4 w-4 mr-2" />
              Search
            </Button>
            <Button variant="outline" onClick={handleClearFilters}>
              Clear Filters
            </Button>
            <Button variant="outline" onClick={() => fetchSales()}>
              <RefreshCwIcon className="h-4 w-4 mr-2" />
              Refresh
            </Button>
          </div>
        </CardContent>
      </Card>

      {/* Sales List */}
      <Card>
        <CardHeader>
          <CardTitle>Sales Transactions</CardTitle>
        </CardHeader>
        <CardContent>
          {isLoading ? (
            <div className="flex items-center justify-center py-8">
              <LoadingSpinner size="md" />
              <span className="ml-2">Loading sales...</span>
            </div>
          ) : sales.length === 0 ? (
            <div className="text-center py-8 text-gray-500">
              <ReceiptIcon className="h-12 w-12 mx-auto mb-2 text-gray-300" />
              <p>No sales found</p>
              <p className="text-sm">Try adjusting your filters</p>
            </div>
          ) : (
            <div className="space-y-4">
              {sales.map((sale) => (
                <div key={sale.id} className="border rounded-lg p-4 hover:bg-gray-50">
                  <div className="flex items-center justify-between">
                    <div className="flex-1">
                      <div className="flex items-center space-x-4">
                        <div>
                          <h3 className="font-medium text-gray-900">
                            Receipt #{sale.receiptNumber}
                          </h3>
                          <p className="text-sm text-gray-600">
                            {formatDate(sale.transactionDate)}
                          </p>
                        </div>

                        <div className="flex space-x-2">
                          {getStatusBadge(sale.status)}
                          {getPaymentMethodBadge(sale.paymentMethod)}
                        </div>
                      </div>

                      <div className="mt-2 grid grid-cols-2 md:grid-cols-4 gap-4 text-sm">
                        <div>
                          <span className="text-gray-600">Customer:</span>
                          <p className="font-medium">
                            {sale.customerName || 'Walk-in Customer'}
                          </p>
                        </div>

                        <div>
                          <span className="text-gray-600">Items:</span>
                          <p className="font-medium">{sale.items.length}</p>
                        </div>

                        <div>
                          <span className="text-gray-600">Total:</span>
                          <p className="font-medium text-green-600">
                            {formatCurrency(sale.totalAmount)}
                          </p>
                        </div>

                        <div>
                          <span className="text-gray-600">Cashier:</span>
                          <p className="font-medium">{sale.cashierId}</p>
                        </div>
                      </div>
                    </div>

                    <div className="flex space-x-2 ml-4">
                      <Button
                        variant="outline"
                        size="sm"
                        onClick={() => setSelectedTransaction(sale)}
                      >
                        <EyeIcon className="h-4 w-4" />
                      </Button>

                      <Button
                        variant="outline"
                        size="sm"
                        onClick={() => printReceipt(sale.id)}
                      >
                        <PrinterIcon className="h-4 w-4" />
                      </Button>
                    </div>
                  </div>
                </div>
              ))}
            </div>
          )}
        </CardContent>
      </Card>
    </div>
  )
}

export default SalesHistory