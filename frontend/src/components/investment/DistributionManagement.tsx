import React, { useState, useEffect } from 'react'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import { Textarea } from '@/components/ui/textarea'
import { Badge } from '@/components/ui/badge'
import { LoadingSpinner } from '@/components/ui/loading-spinner'
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
  DialogFooter,
} from '@/components/ui/dialog'
import { useCurrency } from '@/hooks/useCurrency'
import {
  useInvestment,
  InvestorDistribution,
  DistributionStatus
} from '@/hooks/useInvestment'
import {
  CheckCircleIcon,
  ClockIcon,
  CreditCardIcon,
  XCircleIcon,
  AlertCircleIcon,
  DollarSignIcon,
  CalendarIcon,
  UserIcon,
  FileTextIcon,
  SearchIcon,
  FilterIcon,
  RefreshCwIcon,
  TrendingUpIcon
} from 'lucide-react'

interface DistributionManagementProps {
  viewMode: 'admin' | 'investor'
  shopId?: string
}

export const DistributionManagement: React.FC<DistributionManagementProps> = ({
  viewMode,
  shopId
}) => {
  const { formatCurrency } = useCurrency()
  const { getMyDistributions, approveDistribution, markDistributionAsPaid, isLoading } = useInvestment()

  const [distributions, setDistributions] = useState<InvestorDistribution[]>([])
  const [filteredDistributions, setFilteredDistributions] = useState<InvestorDistribution[]>([])
  const [searchTerm, setSearchTerm] = useState('')
  const [statusFilter, setStatusFilter] = useState<DistributionStatus | 'ALL'>('ALL')
  const [selectedDistribution, setSelectedDistribution] = useState<InvestorDistribution | null>(null)
  const [isRefreshing, setIsRefreshing] = useState(false)

  // Modal states
  const [isApprovalModalOpen, setIsApprovalModalOpen] = useState(false)
  const [isPaidModalOpen, setIsPaidModalOpen] = useState(false)
  const [approvalNotes, setApprovalNotes] = useState('')
  const [paymentReference, setPaymentReference] = useState('')
  const [isProcessing, setIsProcessing] = useState(false)

  useEffect(() => {
    fetchDistributions()
  }, [])

  useEffect(() => {
    filterDistributions()
  }, [distributions, searchTerm, statusFilter])

  const fetchDistributions = async () => {
    try {
      setIsRefreshing(true)
      const result = await getMyDistributions()
      if (result) {
        setDistributions(result)
      }
    } catch (error) {
      console.error('Failed to fetch distributions:', error)
    } finally {
      setIsRefreshing(false)
    }
  }

  const filterDistributions = () => {
    let filtered = distributions

    if (searchTerm) {
      filtered = filtered.filter(distribution =>
        distribution.investmentNumber.toLowerCase().includes(searchTerm.toLowerCase()) ||
        distribution.investorName.toLowerCase().includes(searchTerm.toLowerCase())
      )
    }

    if (statusFilter !== 'ALL') {
      filtered = filtered.filter(distribution => distribution.status === statusFilter)
    }

    setFilteredDistributions(filtered)
  }

  const getStatusColor = (status: DistributionStatus) => {
    switch (status) {
      case 'CALCULATED': return 'bg-blue-100 text-blue-800'
      case 'APPROVED': return 'bg-green-100 text-green-800'
      case 'PAID': return 'bg-green-100 text-green-800'
      case 'FAILED': return 'bg-red-100 text-red-800'
      case 'CANCELLED': return 'bg-gray-100 text-gray-800'
      default: return 'bg-gray-100 text-gray-800'
    }
  }

  const getStatusIcon = (status: DistributionStatus) => {
    switch (status) {
      case 'CALCULATED': return <ClockIcon className="h-4 w-4" />
      case 'APPROVED': return <CheckCircleIcon className="h-4 w-4" />
      case 'PAID': return <CreditCardIcon className="h-4 w-4" />
      case 'FAILED': return <XCircleIcon className="h-4 w-4" />
      case 'CANCELLED': return <XCircleIcon className="h-4 w-4" />
      default: return <AlertCircleIcon className="h-4 w-4" />
    }
  }

  const formatDate = (dateString: string) => {
    return new Date(dateString).toLocaleDateString('en-NG', {
      year: 'numeric',
      month: 'short',
      day: 'numeric'
    })
  }

  const formatDateTime = (dateString: string) => {
    return new Date(dateString).toLocaleDateString('en-NG', {
      year: 'numeric',
      month: 'short',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    })
  }

  const handleApprove = (distribution: InvestorDistribution) => {
    setSelectedDistribution(distribution)
    setApprovalNotes('')
    setIsApprovalModalOpen(true)
  }

  const handleMarkAsPaid = (distribution: InvestorDistribution) => {
    setSelectedDistribution(distribution)
    setPaymentReference('')
    setIsPaidModalOpen(true)
  }

  const confirmApproval = async () => {
    if (!selectedDistribution) return

    try {
      setIsProcessing(true)
      const result = await approveDistribution(selectedDistribution.id, approvalNotes)
      if (result) {
        // Update the distribution in the list
        setDistributions(prev =>
          prev.map(dist =>
            dist.id === selectedDistribution.id ? result : dist
          )
        )
        setIsApprovalModalOpen(false)
        setSelectedDistribution(null)
        setApprovalNotes('')
      }
    } catch (error) {
      console.error('Failed to approve distribution:', error)
    } finally {
      setIsProcessing(false)
    }
  }

  const confirmMarkAsPaid = async () => {
    if (!selectedDistribution || !paymentReference.trim()) return

    try {
      setIsProcessing(true)
      const result = await markDistributionAsPaid(selectedDistribution.id, paymentReference.trim())
      if (result) {
        // Update the distribution in the list
        setDistributions(prev =>
          prev.map(dist =>
            dist.id === selectedDistribution.id ? result : dist
          )
        )
        setIsPaidModalOpen(false)
        setSelectedDistribution(null)
        setPaymentReference('')
      }
    } catch (error) {
      console.error('Failed to mark distribution as paid:', error)
    } finally {
      setIsProcessing(false)
    }
  }

  const getTotalsByStatus = () => {
    const totals = {
      calculated: 0,
      approved: 0,
      paid: 0,
      total: 0
    }

    distributions.forEach(dist => {
      totals.total += dist.distributionAmount
      switch (dist.status) {
        case 'CALCULATED':
          totals.calculated += dist.distributionAmount
          break
        case 'APPROVED':
          totals.approved += dist.distributionAmount
          break
        case 'PAID':
          totals.paid += dist.distributionAmount
          break
      }
    })

    return totals
  }

  const totals = getTotalsByStatus()

  if (isLoading && distributions.length === 0) {
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
            {viewMode === 'admin' ? 'Distribution Management' : 'My Profit Distributions'}
          </h2>
          <p className="text-gray-600">
            {viewMode === 'admin'
              ? 'Approve and manage profit distributions'
              : 'Track your profit distributions and payments'
            }
          </p>
        </div>
        <Button
          variant="outline"
          onClick={fetchDistributions}
          disabled={isRefreshing}
        >
          <RefreshCwIcon className={`h-4 w-4 mr-2 ${isRefreshing ? 'animate-spin' : ''}`} />
          Refresh
        </Button>
      </div>

      {/* Summary Cards */}
      <div className="grid grid-cols-1 md:grid-cols-4 gap-4">
        <Card>
          <CardContent className="p-4">
            <div className="flex items-center space-x-2">
              <ClockIcon className="h-4 w-4 text-blue-600" />
              <div>
                <p className="text-sm text-gray-600">Pending Approval</p>
                <p className="font-semibold text-blue-600">{formatCurrency(totals.calculated)}</p>
              </div>
            </div>
          </CardContent>
        </Card>
        <Card>
          <CardContent className="p-4">
            <div className="flex items-center space-x-2">
              <CheckCircleIcon className="h-4 w-4 text-green-600" />
              <div>
                <p className="text-sm text-gray-600">Approved</p>
                <p className="font-semibold text-green-600">{formatCurrency(totals.approved)}</p>
              </div>
            </div>
          </CardContent>
        </Card>
        <Card>
          <CardContent className="p-4">
            <div className="flex items-center space-x-2">
              <CreditCardIcon className="h-4 w-4 text-purple-600" />
              <div>
                <p className="text-sm text-gray-600">Paid Out</p>
                <p className="font-semibold text-purple-600">{formatCurrency(totals.paid)}</p>
              </div>
            </div>
          </CardContent>
        </Card>
        <Card>
          <CardContent className="p-4">
            <div className="flex items-center space-x-2">
              <TrendingUpIcon className="h-4 w-4 text-gray-600" />
              <div>
                <p className="text-sm text-gray-600">Total Distributions</p>
                <p className="font-semibold">{formatCurrency(totals.total)}</p>
              </div>
            </div>
          </CardContent>
        </Card>
      </div>

      {/* Filters */}
      <Card>
        <CardContent className="p-4">
          <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
            <div className="relative">
              <SearchIcon className="absolute left-3 top-3 h-4 w-4 text-gray-400" />
              <Input
                placeholder="Search distributions..."
                value={searchTerm}
                onChange={(e) => setSearchTerm(e.target.value)}
                className="pl-10"
              />
            </div>
            <select
              value={statusFilter}
              onChange={(e) => setStatusFilter(e.target.value as DistributionStatus | 'ALL')}
              className="px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
            >
              <option value="ALL">All Status</option>
              <option value="CALCULATED">Pending Approval</option>
              <option value="APPROVED">Approved</option>
              <option value="PAID">Paid</option>
              <option value="FAILED">Failed</option>
              <option value="CANCELLED">Cancelled</option>
            </select>
            <div className="flex items-center text-sm text-gray-600">
              <FilterIcon className="h-4 w-4 mr-1" />
              {filteredDistributions.length} of {distributions.length} distributions
            </div>
          </div>
        </CardContent>
      </Card>

      {/* Distributions List */}
      {filteredDistributions.length === 0 ? (
        <Card>
          <CardContent className="text-center py-12">
            <DollarSignIcon className="h-12 w-12 mx-auto text-gray-300 mb-4" />
            <h3 className="text-lg font-medium text-gray-900 mb-2">No distributions found</h3>
            <p className="text-gray-600">
              {distributions.length === 0
                ? 'No profit distributions have been calculated yet.'
                : 'No distributions match your current filters.'
              }
            </p>
          </CardContent>
        </Card>
      ) : (
        <div className="space-y-4">
          {filteredDistributions.map((distribution) => (
            <Card key={distribution.id} className="hover:shadow-md transition-shadow">
              <CardContent className="p-6">
                <div className="flex items-center justify-between mb-4">
                  <div className="flex items-center space-x-3">
                    {getStatusIcon(distribution.status)}
                    <div>
                      <h3 className="font-semibold">{distribution.investmentNumber}</h3>
                      <p className="text-sm text-gray-600">{distribution.investorName}</p>
                    </div>
                  </div>
                  <Badge className={getStatusColor(distribution.status)}>
                    {distribution.status}
                  </Badge>
                </div>

                <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4 mb-4">
                  <div>
                    <p className="text-sm text-gray-600">Period</p>
                    <p className="font-medium">
                      {formatDate(distribution.periodStart)} - {formatDate(distribution.periodEnd)}
                    </p>
                  </div>
                  <div>
                    <p className="text-sm text-gray-600">Sales Revenue</p>
                    <p className="font-medium">{formatCurrency(distribution.totalSalesRevenue)}</p>
                  </div>
                  <div>
                    <p className="text-sm text-gray-600">Total Profit</p>
                    <p className="font-medium">{formatCurrency(distribution.totalProfit)}</p>
                  </div>
                  <div>
                    <p className="text-sm text-gray-600">Distribution Amount</p>
                    <p className="font-semibold text-green-600">
                      {formatCurrency(distribution.distributionAmount)}
                    </p>
                  </div>
                </div>

                <div className="grid grid-cols-1 md:grid-cols-2 gap-4 mb-4">
                  <div>
                    <p className="text-sm text-gray-600">Investor Share</p>
                    <p className="font-medium">
                      {distribution.investorSharePercentage}% ({formatCurrency(distribution.investorProfitAmount)})
                    </p>
                  </div>
                  {distribution.distributionDate && (
                    <div>
                      <p className="text-sm text-gray-600">Distribution Date</p>
                      <p className="font-medium">{formatDateTime(distribution.distributionDate)}</p>
                    </div>
                  )}
                </div>

                {distribution.paymentReference && (
                  <div className="mb-4">
                    <p className="text-sm text-gray-600">Payment Reference</p>
                    <p className="font-mono text-sm bg-gray-100 px-2 py-1 rounded">
                      {distribution.paymentReference}
                    </p>
                  </div>
                )}

                {distribution.notes && (
                  <div className="mb-4">
                    <p className="text-sm text-gray-600">Notes</p>
                    <p className="text-sm">{distribution.notes}</p>
                  </div>
                )}

                {distribution.calculationDetails && (
                  <div className="mb-4">
                    <p className="text-sm text-gray-600">Calculation Details</p>
                    <p className="text-sm font-mono bg-gray-50 p-2 rounded">
                      {distribution.calculationDetails}
                    </p>
                  </div>
                )}

                {/* Actions */}
                {viewMode === 'admin' && (
                  <div className="flex items-center space-x-2 pt-4 border-t">
                    {distribution.status === 'CALCULATED' && (
                      <Button
                        size="sm"
                        onClick={() => handleApprove(distribution)}
                      >
                        <CheckCircleIcon className="h-4 w-4 mr-2" />
                        Approve
                      </Button>
                    )}
                    {distribution.status === 'APPROVED' && (
                      <Button
                        size="sm"
                        onClick={() => handleMarkAsPaid(distribution)}
                      >
                        <CreditCardIcon className="h-4 w-4 mr-2" />
                        Mark as Paid
                      </Button>
                    )}
                  </div>
                )}
              </CardContent>
            </Card>
          ))}
        </div>
      )}

      {/* Approval Modal */}
      <Dialog open={isApprovalModalOpen} onOpenChange={setIsApprovalModalOpen}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle>Approve Distribution</DialogTitle>
          </DialogHeader>
          <div className="space-y-4">
            <p>
              Are you sure you want to approve the distribution of{' '}
              <strong>{formatCurrency(selectedDistribution?.distributionAmount || 0)}</strong>{' '}
              for investment {selectedDistribution?.investmentNumber}?
            </p>
            <div>
              <label className="block text-sm font-medium mb-2">
                Approval Notes (Optional)
              </label>
              <Textarea
                value={approvalNotes}
                onChange={(e) => setApprovalNotes(e.target.value)}
                placeholder="Add any notes about this approval..."
                rows={3}
              />
            </div>
          </div>
          <DialogFooter>
            <Button
              variant="outline"
              onClick={() => setIsApprovalModalOpen(false)}
              disabled={isProcessing}
            >
              Cancel
            </Button>
            <Button
              onClick={confirmApproval}
              disabled={isProcessing}
            >
              {isProcessing && <LoadingSpinner size="sm" className="mr-2" />}
              Approve Distribution
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>

      {/* Mark as Paid Modal */}
      <Dialog open={isPaidModalOpen} onOpenChange={setIsPaidModalOpen}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle>Mark Distribution as Paid</DialogTitle>
          </DialogHeader>
          <div className="space-y-4">
            <p>
              Mark the distribution of{' '}
              <strong>{formatCurrency(selectedDistribution?.distributionAmount || 0)}</strong>{' '}
              for investment {selectedDistribution?.investmentNumber} as paid.
            </p>
            <div>
              <label className="block text-sm font-medium mb-2">
                Payment Reference *
              </label>
              <Input
                value={paymentReference}
                onChange={(e) => setPaymentReference(e.target.value)}
                placeholder="Enter payment reference or transaction ID"
                required
              />
            </div>
          </div>
          <DialogFooter>
            <Button
              variant="outline"
              onClick={() => setIsPaidModalOpen(false)}
              disabled={isProcessing}
            >
              Cancel
            </Button>
            <Button
              onClick={confirmMarkAsPaid}
              disabled={isProcessing || !paymentReference.trim()}
            >
              {isProcessing && <LoadingSpinner size="sm" className="mr-2" />}
              Mark as Paid
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>
    </div>
  )
}