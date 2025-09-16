import React, { useState, useEffect } from 'react'
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
} from '@/components/ui/dialog'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import { Button } from '@/components/ui/button'
import { Badge } from '@/components/ui/badge'
import { LoadingSpinner } from '@/components/ui/loading-spinner'
import { useCurrency } from '@/hooks/useCurrency'
import {
  useInvestment,
  Investment,
  InvestorDistribution,
  InvestmentStatus,
  DistributionStatus
} from '@/hooks/useInvestment'
import {
  TrendingUpIcon,
  TrendingDownIcon,
  CalendarIcon,
  DollarSignIcon,
  UserIcon,
  StoreIcon,
  InfoIcon,
  DownloadIcon,
  CreditCardIcon,
  CheckCircleIcon,
  ClockIcon,
  XCircleIcon,
  AlertCircleIcon,
  FileTextIcon
} from 'lucide-react'

interface InvestmentDetailsProps {
  investment: Investment | null
  isOpen: boolean
  onClose: () => void
  onWithdraw?: (investment: Investment) => void
}

export const InvestmentDetails: React.FC<InvestmentDetailsProps> = ({
  investment,
  isOpen,
  onClose,
  onWithdraw
}) => {
  const { formatCurrency } = useCurrency()
  const { getInvestmentDistributions, isLoading } = useInvestment()
  const [distributions, setDistributions] = useState<InvestorDistribution[]>([])
  const [isLoadingDistributions, setIsLoadingDistributions] = useState(false)

  useEffect(() => {
    if (investment && isOpen) {
      fetchDistributions()
    }
  }, [investment, isOpen])

  const fetchDistributions = async () => {
    if (!investment) return

    try {
      setIsLoadingDistributions(true)
      const result = await getInvestmentDistributions(investment.id)
      if (result) {
        setDistributions(result)
      }
    } catch (error) {
      console.error('Failed to fetch distributions:', error)
    } finally {
      setIsLoadingDistributions(false)
    }
  }

  if (!investment) return null

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

  const getDistributionStatusColor = (status: DistributionStatus) => {
    switch (status) {
      case 'CALCULATED': return 'bg-blue-100 text-blue-800'
      case 'APPROVED': return 'bg-green-100 text-green-800'
      case 'PAID': return 'bg-green-100 text-green-800'
      case 'FAILED': return 'bg-red-100 text-red-800'
      case 'CANCELLED': return 'bg-gray-100 text-gray-800'
      default: return 'bg-gray-100 text-gray-800'
    }
  }

  const getDistributionStatusIcon = (status: DistributionStatus) => {
    switch (status) {
      case 'CALCULATED': return <ClockIcon className="h-4 w-4" />
      case 'APPROVED': return <CheckCircleIcon className="h-4 w-4" />
      case 'PAID': return <CreditCardIcon className="h-4 w-4" />
      case 'FAILED': return <XCircleIcon className="h-4 w-4" />
      case 'CANCELLED': return <XCircleIcon className="h-4 w-4" />
      default: return <AlertCircleIcon className="h-4 w-4" />
    }
  }

  const calculateROI = () => {
    if (investment.amount === 0) return 0
    return ((investment.totalProfitEarned / investment.amount) * 100)
  }

  const formatDate = (dateString: string) => {
    return new Date(dateString).toLocaleDateString('en-NG', {
      year: 'numeric',
      month: 'short',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    })
  }

  const formatDateOnly = (dateString: string) => {
    return new Date(dateString).toLocaleDateString('en-NG', {
      year: 'numeric',
      month: 'short',
      day: 'numeric'
    })
  }

  const getInvestmentTypeLabel = () => {
    switch (investment.investmentType) {
      case 'SHOP_WIDE': return 'Shop-Wide Investment'
      case 'PRODUCT_SPECIFIC': return 'Product-Specific Investment'
      case 'CATEGORY_SPECIFIC': return 'Category-Based Investment'
      default: return investment.investmentType
    }
  }

  const getProfitSharingLabel = () => {
    switch (investment.profitSharingModel) {
      case 'PROPORTIONAL_BY_AMOUNT': return 'Proportional by Amount'
      case 'FIXED_SHARES': return 'Fixed Shares'
      case 'TIME_WEIGHTED': return 'Time-Weighted'
      case 'TIERED': return 'Tiered System'
      default: return investment.profitSharingModel
    }
  }

  const totalDistributed = distributions.reduce((sum, dist) => sum + dist.distributionAmount, 0)
  const pendingDistributions = distributions.filter(dist =>
    dist.status === 'CALCULATED' || dist.status === 'APPROVED'
  ).length

  return (
    <Dialog open={isOpen} onOpenChange={onClose}>
      <DialogContent className="max-w-4xl max-h-[90vh] overflow-y-auto">
        <DialogHeader>
          <DialogTitle className="flex items-center space-x-2">
            <TrendingUpIcon className="h-5 w-5" />
            <span>Investment Details - {investment.investmentNumber}</span>
          </DialogTitle>
        </DialogHeader>

        <div className="space-y-6">
          {/* Investment Overview */}
          <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
            {/* Basic Information */}
            <Card>
              <CardHeader>
                <CardTitle className="flex items-center space-x-2">
                  <InfoIcon className="h-5 w-5" />
                  <span>Investment Information</span>
                </CardTitle>
              </CardHeader>
              <CardContent className="space-y-4">
                <div className="flex items-center justify-between">
                  <span className="text-gray-600">Status</span>
                  <Badge className={getStatusColor(investment.status)}>
                    {investment.status}
                  </Badge>
                </div>
                <div className="flex items-center justify-between">
                  <span className="text-gray-600">Type</span>
                  <span className="font-medium">{getInvestmentTypeLabel()}</span>
                </div>
                <div className="flex items-center justify-between">
                  <span className="text-gray-600">Profit Model</span>
                  <span className="font-medium">{getProfitSharingLabel()}</span>
                </div>
                {investment.profitPercentage && (
                  <div className="flex items-center justify-between">
                    <span className="text-gray-600">Expected Profit %</span>
                    <span className="font-medium">{investment.profitPercentage}%</span>
                  </div>
                )}
                {investment.fixedShares && (
                  <div className="flex items-center justify-between">
                    <span className="text-gray-600">Fixed Shares</span>
                    <span className="font-medium">{investment.fixedShares}</span>
                  </div>
                )}
                <div className="flex items-center justify-between">
                  <span className="text-gray-600">Investment Date</span>
                  <span className="font-medium">{formatDateOnly(investment.investmentDate)}</span>
                </div>
                {investment.maturityDate && (
                  <div className="flex items-center justify-between">
                    <span className="text-gray-600">Maturity Date</span>
                    <span className="font-medium">{formatDateOnly(investment.maturityDate)}</span>
                  </div>
                )}
              </CardContent>
            </Card>

            {/* Parties Involved */}
            <Card>
              <CardHeader>
                <CardTitle className="flex items-center space-x-2">
                  <UserIcon className="h-5 w-5" />
                  <span>Parties Involved</span>
                </CardTitle>
              </CardHeader>
              <CardContent className="space-y-4">
                <div>
                  <div className="flex items-center space-x-2 mb-2">
                    <UserIcon className="h-4 w-4 text-blue-600" />
                    <span className="font-medium">Investor</span>
                  </div>
                  <div className="pl-6">
                    <p className="font-medium">{investment.investorName}</p>
                    <p className="text-sm text-gray-600">{investment.investorEmail}</p>
                  </div>
                </div>
                <div>
                  <div className="flex items-center space-x-2 mb-2">
                    <StoreIcon className="h-4 w-4 text-green-600" />
                    <span className="font-medium">Shop</span>
                  </div>
                  <div className="pl-6">
                    <p className="font-medium">{investment.shopName}</p>
                  </div>
                </div>
                {investment.products && investment.products.length > 0 && (
                  <div>
                    <div className="flex items-center space-x-2 mb-2">
                      <FileTextIcon className="h-4 w-4 text-purple-600" />
                      <span className="font-medium">Products ({investment.products.length})</span>
                    </div>
                    <div className="pl-6 space-y-1">
                      {investment.products.slice(0, 3).map((product) => (
                        <p key={product.id} className="text-sm">
                          {product.name} - {formatCurrency(product.price)}
                        </p>
                      ))}
                      {investment.products.length > 3 && (
                        <p className="text-sm text-gray-600">
                          +{investment.products.length - 3} more products
                        </p>
                      )}
                    </div>
                  </div>
                )}
              </CardContent>
            </Card>
          </div>

          {/* Financial Summary */}
          <Card>
            <CardHeader>
              <CardTitle className="flex items-center space-x-2">
                <DollarSignIcon className="h-5 w-5" />
                <span>Financial Summary</span>
              </CardTitle>
            </CardHeader>
            <CardContent>
              <div className="grid grid-cols-2 md:grid-cols-5 gap-6">
                <div className="text-center">
                  <p className="text-sm text-gray-600 mb-1">Investment Amount</p>
                  <p className="text-2xl font-bold text-blue-600">
                    {formatCurrency(investment.amount)}
                  </p>
                </div>
                <div className="text-center">
                  <p className="text-sm text-gray-600 mb-1">Total Profit</p>
                  <p className="text-2xl font-bold text-green-600">
                    {formatCurrency(investment.totalProfitEarned)}
                  </p>
                </div>
                <div className="text-center">
                  <p className="text-sm text-gray-600 mb-1">Total Withdrawn</p>
                  <p className="text-2xl font-bold text-orange-600">
                    {formatCurrency(investment.totalWithdrawn)}
                  </p>
                </div>
                <div className="text-center">
                  <p className="text-sm text-gray-600 mb-1">Available Balance</p>
                  <p className="text-2xl font-bold text-purple-600">
                    {formatCurrency(investment.availableBalance)}
                  </p>
                </div>
                <div className="text-center">
                  <p className="text-sm text-gray-600 mb-1">ROI</p>
                  <div className="flex items-center justify-center space-x-1">
                    {investment.totalProfitEarned >= 0 ? (
                      <TrendingUpIcon className="h-4 w-4 text-green-600" />
                    ) : (
                      <TrendingDownIcon className="h-4 w-4 text-red-600" />
                    )}
                    <span className={`text-2xl font-bold ${
                      calculateROI() >= 15 ? 'text-green-600' :
                      calculateROI() >= 5 ? 'text-blue-600' :
                      calculateROI() >= 0 ? 'text-yellow-600' : 'text-red-600'
                    }`}>
                      {calculateROI().toFixed(1)}%
                    </span>
                  </div>
                </div>
              </div>
            </CardContent>
          </Card>

          {/* Distribution History */}
          <Card>
            <CardHeader className="flex flex-row items-center justify-between">
              <CardTitle className="flex items-center space-x-2">
                <CalendarIcon className="h-5 w-5" />
                <span>Distribution History</span>
                {pendingDistributions > 0 && (
                  <Badge className="bg-orange-100 text-orange-800">
                    {pendingDistributions} Pending
                  </Badge>
                )}
              </CardTitle>
              <div className="text-sm text-gray-600">
                Total Distributed: {formatCurrency(totalDistributed)}
              </div>
            </CardHeader>
            <CardContent>
              {isLoadingDistributions ? (
                <div className="flex items-center justify-center py-8">
                  <LoadingSpinner size="md" />
                </div>
              ) : distributions.length === 0 ? (
                <div className="text-center py-8 text-gray-500">
                  <CalendarIcon className="h-12 w-12 mx-auto mb-2 text-gray-300" />
                  <p>No distributions yet</p>
                  <p className="text-sm">Profit distributions will appear here when calculated</p>
                </div>
              ) : (
                <div className="space-y-4">
                  {distributions.map((distribution) => (
                    <div key={distribution.id} className="border rounded-lg p-4">
                      <div className="flex items-center justify-between mb-3">
                        <div className="flex items-center space-x-2">
                          {getDistributionStatusIcon(distribution.status)}
                          <span className="font-medium">
                            {formatDateOnly(distribution.periodStart)} - {formatDateOnly(distribution.periodEnd)}
                          </span>
                        </div>
                        <Badge className={getDistributionStatusColor(distribution.status)}>
                          {distribution.status}
                        </Badge>
                      </div>

                      <div className="grid grid-cols-2 md:grid-cols-4 gap-4 text-sm">
                        <div>
                          <p className="text-gray-600">Sales Revenue</p>
                          <p className="font-medium">{formatCurrency(distribution.totalSalesRevenue)}</p>
                        </div>
                        <div>
                          <p className="text-gray-600">Total Profit</p>
                          <p className="font-medium">{formatCurrency(distribution.totalProfit)}</p>
                        </div>
                        <div>
                          <p className="text-gray-600">Your Share ({distribution.investorSharePercentage}%)</p>
                          <p className="font-medium">{formatCurrency(distribution.investorProfitAmount)}</p>
                        </div>
                        <div>
                          <p className="text-gray-600">Distribution Amount</p>
                          <p className="font-semibold text-green-600">
                            {formatCurrency(distribution.distributionAmount)}
                          </p>
                        </div>
                      </div>

                      {distribution.distributionDate && (
                        <div className="mt-3 pt-3 border-t">
                          <p className="text-sm text-gray-600">
                            Distributed on: {formatDate(distribution.distributionDate)}
                            {distribution.paymentReference && (
                              <span className="ml-2">
                                (Ref: {distribution.paymentReference})
                              </span>
                            )}
                          </p>
                        </div>
                      )}

                      {distribution.notes && (
                        <div className="mt-2">
                          <p className="text-sm text-gray-600">
                            <strong>Notes:</strong> {distribution.notes}
                          </p>
                        </div>
                      )}
                    </div>
                  ))}
                </div>
              )}
            </CardContent>
          </Card>

          {/* Notes */}
          {investment.notes && (
            <Card>
              <CardHeader>
                <CardTitle>Notes</CardTitle>
              </CardHeader>
              <CardContent>
                <p className="text-gray-700">{investment.notes}</p>
              </CardContent>
            </Card>
          )}

          {/* Actions */}
          <div className="flex items-center justify-between pt-4 border-t">
            <div className="space-x-2">
              <Button variant="outline">
                <DownloadIcon className="h-4 w-4 mr-2" />
                Download Report
              </Button>
            </div>
            <div className="space-x-2">
              {investment.availableBalance > 0 && onWithdraw && (
                <Button onClick={() => onWithdraw(investment)}>
                  <CreditCardIcon className="h-4 w-4 mr-2" />
                  Withdraw Funds
                </Button>
              )}
              <Button variant="outline" onClick={onClose}>
                Close
              </Button>
            </div>
          </div>
        </div>
      </DialogContent>
    </Dialog>
  )
}