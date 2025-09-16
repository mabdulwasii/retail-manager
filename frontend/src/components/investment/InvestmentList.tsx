import React, { useState, useEffect, useCallback } from 'react'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import { Badge } from '@/components/ui/badge'
import { LoadingSpinner } from '@/components/ui/loading-spinner'
import { useCurrency } from '@/hooks/useCurrency'
import {
  useInvestment,
  Investment,
  InvestmentStatus,
  InvestmentType,
  ProfitSharingModel
} from '@/hooks/useInvestment'
import {
  SearchIcon,
  FilterIcon,
  PlusIcon,
  TrendingUpIcon,
  TrendingDownIcon,
  CalendarIcon,
  DollarSignIcon,
  EyeIcon,
  DownloadIcon,
  RefreshCwIcon
} from 'lucide-react'

interface InvestmentListProps {
  shopId?: string
  onCreateInvestment: () => void
  onViewInvestment: (investment: Investment) => void
  viewMode: 'shop' | 'investor'
}

export const InvestmentList: React.FC<InvestmentListProps> = ({
  shopId,
  onCreateInvestment,
  onViewInvestment,
  viewMode
}) => {
  const { formatCurrency } = useCurrency()
  const { getShopInvestments, getMyInvestments, isLoading } = useInvestment()

  const [investments, setInvestments] = useState<Investment[]>([])
  const [filteredInvestments, setFilteredInvestments] = useState<Investment[]>([])
  const [searchTerm, setSearchTerm] = useState('')
  const [statusFilter, setStatusFilter] = useState<InvestmentStatus | 'ALL'>('ALL')
  const [typeFilter, setTypeFilter] = useState<InvestmentType | 'ALL'>('ALL')
  const [currentPage, setCurrentPage] = useState(0)
  const [totalPages, setTotalPages] = useState(0)
  const [isRefreshing, setIsRefreshing] = useState(false)

  const fetchInvestments = useCallback(async () => {
    try {
      setIsRefreshing(true)
      let result

      if (viewMode === 'shop' && shopId) {
        result = await getShopInvestments(shopId, currentPage, 20)
      } else {
        result = await getMyInvestments(currentPage, 20)
      }

      if (result) {
        setInvestments(result.content)
        setTotalPages(result.totalPages)
      }
    } catch (error) {
      console.error('Failed to fetch investments:', error)
    } finally {
      setIsRefreshing(false)
    }
  }, [viewMode, shopId, currentPage, getShopInvestments, getMyInvestments])

  useEffect(() => {
    fetchInvestments()
  }, [fetchInvestments])

  useEffect(() => {
    let filtered = investments

    if (searchTerm) {
      filtered = filtered.filter(investment =>
        investment.investmentNumber.toLowerCase().includes(searchTerm.toLowerCase()) ||
        investment.investorName.toLowerCase().includes(searchTerm.toLowerCase()) ||
        investment.shopName.toLowerCase().includes(searchTerm.toLowerCase())
      )
    }

    if (statusFilter !== 'ALL') {
      filtered = filtered.filter(investment => investment.status === statusFilter)
    }

    if (typeFilter !== 'ALL') {
      filtered = filtered.filter(investment => investment.investmentType === typeFilter)
    }

    setFilteredInvestments(filtered)
  }, [investments, searchTerm, statusFilter, typeFilter])

  const getStatusColor = (status: InvestmentStatus) => {
    switch (status) {
      case 'ACTIVE': return 'bg-green-100 text-green-800'
      case 'PENDING': return 'bg-yellow-100 text-yellow-800'
      case 'INACTIVE': return 'bg-gray-100 text-gray-800'
      case 'MATURED': return 'bg-blue-100 text-blue-800'
      case 'WITHDRAWN': return 'bg-purple-100 text-purple-800'
      case 'CANCELLED': return 'bg-red-100 text-red-800'
      default: return 'bg-gray-100 text-gray-800'
    }
  }

  const getTypeLabel = (type: InvestmentType) => {
    switch (type) {
      case 'SHOP_WIDE': return 'Shop-Wide'
      case 'PRODUCT_SPECIFIC': return 'Product-Specific'
      case 'CATEGORY_SPECIFIC': return 'Category-Based'
      default: return type
    }
  }

  const getProfitSharingLabel = (model: ProfitSharingModel) => {
    switch (model) {
      case 'PROPORTIONAL_BY_AMOUNT': return 'Proportional'
      case 'FIXED_SHARES': return 'Fixed Shares'
      case 'TIME_WEIGHTED': return 'Time-Weighted'
      case 'TIERED': return 'Tiered'
      default: return model
    }
  }

  const getROIColor = (profit: number, amount: number) => {
    if (amount === 0) return 'text-gray-500'
    const roi = (profit / amount) * 100
    if (roi >= 15) return 'text-green-600'
    if (roi >= 5) return 'text-blue-600'
    if (roi >= 0) return 'text-yellow-600'
    return 'text-red-600'
  }

  const calculateROI = (profit: number, amount: number) => {
    if (amount === 0) return 0
    return ((profit / amount) * 100)
  }

  const formatDate = (dateString: string) => {
    return new Date(dateString).toLocaleDateString('en-NG', {
      year: 'numeric',
      month: 'short',
      day: 'numeric'
    })
  }

  const handlePageChange = (newPage: number) => {
    setCurrentPage(newPage)
  }

  if (isLoading && investments.length === 0) {
    return (
      <div className="flex items-center justify-center h-64">
        <LoadingSpinner size="lg" />
      </div>
    )
  }

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
        <div>
          <h2 className="text-2xl font-bold">
            {viewMode === 'shop' ? 'Shop Investments' : 'My Investments'}
          </h2>
          <p className="text-gray-600">
            {viewMode === 'shop'
              ? 'Manage and track investments in this shop'
              : 'Track your investment portfolio and returns'
            }
          </p>
        </div>
        <div className="flex items-center space-x-2">
          <Button
            variant="outline"
            onClick={fetchInvestments}
            disabled={isRefreshing}
          >
            <RefreshCwIcon className={`h-4 w-4 mr-2 ${isRefreshing ? 'animate-spin' : ''}`} />
            Refresh
          </Button>
          {viewMode === 'investor' && (
            <Button onClick={onCreateInvestment}>
              <PlusIcon className="h-4 w-4 mr-2" />
              New Investment
            </Button>
          )}
        </div>
      </div>

      {/* Filters */}
      <Card>
        <CardContent className="p-4">
          <div className="grid grid-cols-1 md:grid-cols-4 gap-4">
            <div className="relative">
              <SearchIcon className="absolute left-3 top-3 h-4 w-4 text-gray-400" />
              <Input
                placeholder="Search investments..."
                value={searchTerm}
                onChange={(e) => setSearchTerm(e.target.value)}
                className="pl-10"
              />
            </div>
            <select
              value={statusFilter}
              onChange={(e) => setStatusFilter(e.target.value as InvestmentStatus | 'ALL')}
              className="px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
            >
              <option value="ALL">All Status</option>
              <option value="ACTIVE">Active</option>
              <option value="PENDING">Pending</option>
              <option value="INACTIVE">Inactive</option>
              <option value="MATURED">Matured</option>
              <option value="WITHDRAWN">Withdrawn</option>
              <option value="CANCELLED">Cancelled</option>
            </select>
            <select
              value={typeFilter}
              onChange={(e) => setTypeFilter(e.target.value as InvestmentType | 'ALL')}
              className="px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
            >
              <option value="ALL">All Types</option>
              <option value="SHOP_WIDE">Shop-Wide</option>
              <option value="PRODUCT_SPECIFIC">Product-Specific</option>
              <option value="CATEGORY_SPECIFIC">Category-Based</option>
            </select>
            <div className="flex items-center text-sm text-gray-600">
              <FilterIcon className="h-4 w-4 mr-1" />
              {filteredInvestments.length} of {investments.length} investments
            </div>
          </div>
        </CardContent>
      </Card>

      {/* Investment Grid */}
      {filteredInvestments.length === 0 ? (
        <Card>
          <CardContent className="text-center py-12">
            <TrendingUpIcon className="h-12 w-12 mx-auto text-gray-300 mb-4" />
            <h3 className="text-lg font-medium text-gray-900 mb-2">No investments found</h3>
            <p className="text-gray-600 mb-4">
              {investments.length === 0
                ? 'No investments have been created yet.'
                : 'No investments match your current filters.'
              }
            </p>
            {viewMode === 'investor' && investments.length === 0 && (
              <Button onClick={onCreateInvestment}>
                <PlusIcon className="h-4 w-4 mr-2" />
                Create First Investment
              </Button>
            )}
          </CardContent>
        </Card>
      ) : (
        <div className="grid grid-cols-1 lg:grid-cols-2 xl:grid-cols-3 gap-6">
          {filteredInvestments.map((investment) => (
            <Card key={investment.id} className="hover:shadow-lg transition-shadow">
              <CardHeader className="pb-3">
                <div className="flex items-start justify-between">
                  <div>
                    <CardTitle className="text-lg">{investment.investmentNumber}</CardTitle>
                    <p className="text-sm text-gray-600 mt-1">
                      {viewMode === 'shop' ? investment.investorName : investment.shopName}
                    </p>
                  </div>
                  <Badge className={getStatusColor(investment.status)}>
                    {investment.status}
                  </Badge>
                </div>
              </CardHeader>
              <CardContent className="space-y-4">
                {/* Investment Details */}
                <div className="grid grid-cols-2 gap-4 text-sm">
                  <div>
                    <p className="text-gray-500">Amount</p>
                    <p className="font-semibold text-blue-600">
                      {formatCurrency(investment.amount)}
                    </p>
                  </div>
                  <div>
                    <p className="text-gray-500">Type</p>
                    <p className="font-medium">{getTypeLabel(investment.investmentType)}</p>
                  </div>
                  <div>
                    <p className="text-gray-500">Profit Model</p>
                    <p className="font-medium">{getProfitSharingLabel(investment.profitSharingModel)}</p>
                  </div>
                  <div>
                    <p className="text-gray-500">Date</p>
                    <p className="font-medium">{formatDate(investment.investmentDate)}</p>
                  </div>
                </div>

                {/* Performance Metrics */}
                <div className="border-t pt-4">
                  <div className="grid grid-cols-3 gap-4 text-sm">
                    <div>
                      <p className="text-gray-500">Profit</p>
                      <p className="font-semibold text-green-600">
                        {formatCurrency(investment.totalProfitEarned)}
                      </p>
                    </div>
                    <div>
                      <p className="text-gray-500">Available</p>
                      <p className="font-semibold text-purple-600">
                        {formatCurrency(investment.availableBalance)}
                      </p>
                    </div>
                    <div>
                      <p className="text-gray-500">ROI</p>
                      <div className="flex items-center space-x-1">
                        {investment.totalProfitEarned >= 0 ? (
                          <TrendingUpIcon className="h-3 w-3 text-green-600" />
                        ) : (
                          <TrendingDownIcon className="h-3 w-3 text-red-600" />
                        )}
                        <span className={`font-semibold ${getROIColor(investment.totalProfitEarned, investment.amount)}`}>
                          {calculateROI(investment.totalProfitEarned, investment.amount).toFixed(1)}%
                        </span>
                      </div>
                    </div>
                  </div>
                </div>

                {/* Maturity Date */}
                {investment.maturityDate && (
                  <div className="flex items-center text-sm text-gray-600">
                    <CalendarIcon className="h-4 w-4 mr-2" />
                    <span>Matures: {formatDate(investment.maturityDate)}</span>
                  </div>
                )}

                {/* Actions */}
                <div className="flex items-center justify-between pt-2 border-t">
                  <Button
                    variant="outline"
                    size="sm"
                    onClick={() => onViewInvestment(investment)}
                  >
                    <EyeIcon className="h-4 w-4 mr-2" />
                    View Details
                  </Button>
                  <Button variant="ghost" size="sm">
                    <DownloadIcon className="h-4 w-4" />
                  </Button>
                </div>
              </CardContent>
            </Card>
          ))}
        </div>
      )}

      {/* Pagination */}
      {totalPages > 1 && (
        <div className="flex items-center justify-center space-x-2">
          <Button
            variant="outline"
            size="sm"
            disabled={currentPage === 0}
            onClick={() => handlePageChange(currentPage - 1)}
          >
            Previous
          </Button>
          <span className="text-sm text-gray-600">
            Page {currentPage + 1} of {totalPages}
          </span>
          <Button
            variant="outline"
            size="sm"
            disabled={currentPage === totalPages - 1}
            onClick={() => handlePageChange(currentPage + 1)}
          >
            Next
          </Button>
        </div>
      )}
    </div>
  )
}