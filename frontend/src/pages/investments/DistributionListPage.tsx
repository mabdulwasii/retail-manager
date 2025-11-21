import React, { useState } from 'react'
import { Download, Check, X } from 'lucide-react'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import { Button } from '@/components/ui/button'
import { Badge } from '@/components/ui/badge'
import { Checkbox } from '@/components/ui/checkbox'
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/components/ui/select'
import { useDistributions, useApproveDistribution, useMarkDistributionPaid } from '@/hooks/investment/useDistributions'
import { useCurrency } from '@/hooks/useCurrency'
import { format } from 'date-fns'
import type { InvestorDistribution, DistributionStatus } from '@/types/investment'
import { ApproveDistributionModal } from '@/components/investment/modals/ApproveDistributionModal'
import { MarkPaidModal } from '@/components/investment/modals/MarkPaidModal'

export const DistributionListPage: React.FC = () => {
  const { formatCurrency } = useCurrency()
  const [statusFilter, setStatusFilter] = useState<string>('all')
  const [selectedDistributions, setSelectedDistributions] = useState<string[]>([])
  const [approveModalOpen, setApproveModalOpen] = useState(false)
  const [markPaidModalOpen, setMarkPaidModalOpen] = useState(false)
  const [selectedDistribution, setSelectedDistribution] = useState<InvestorDistribution | null>(null)
  const [page, setPage] = useState(0)
  const [pageSize] = useState(20)

  const { data: distributionsData, isLoading } = useDistributions({ page, size: pageSize })
  const approveMutation = useApproveDistribution()
  const markPaidMutation = useMarkDistributionPaid()

  // Extract distributions from paginated response
  const distributions = distributionsData?.content || []
  const totalPages = distributionsData?.totalPages || 0
  const totalElements = distributionsData?.totalElements || 0

  // Filter distributions
  const filteredDistributions = distributions.filter((dist) => {
    if (statusFilter === 'all') return true
    return dist.status === statusFilter
  })

  // Calculate summary
  const summary = {
    totalDistributed: distributions
      .filter((d) => d.status === 'PAID')
      .reduce((sum, d) => sum + d.distributionAmount, 0),
    pendingApproval: distributions.filter((d) => d.status === 'PENDING').length,
    approvedPayment: distributions.filter((d) => d.status === 'APPROVED').length,
    paidThisMonth: distributions
      .filter((d) => {
        if (d.status !== 'PAID' || !d.distributionDate) return false
        const distDate = new Date(d.distributionDate)
        const now = new Date()
        return distDate.getMonth() === now.getMonth() && distDate.getFullYear() === now.getFullYear()
      })
      .reduce((sum, d) => sum + d.distributionAmount, 0),
  }

  const getStatusColor = (status: DistributionStatus) => {
    const colors = {
      PENDING: 'bg-yellow-100 text-yellow-800',
      APPROVED: 'bg-blue-100 text-blue-800',
      PAID: 'bg-green-100 text-green-800',
      REJECTED: 'bg-red-100 text-red-800',
    }
    return colors[status] || 'bg-gray-100 text-gray-800'
  }

  const handleSelectDistribution = (id: string) => {
    setSelectedDistributions((prev) =>
      prev.includes(id) ? prev.filter((item) => item !== id) : [...prev, id]
    )
  }

  const handleSelectAll = () => {
    if (selectedDistributions.length === filteredDistributions.length) {
      setSelectedDistributions([])
    } else {
      setSelectedDistributions(filteredDistributions.map((d) => d.id))
    }
  }

  const handleApprove = (distribution: InvestorDistribution) => {
    setSelectedDistribution(distribution)
    setApproveModalOpen(true)
  }

  const handleMarkPaid = (distribution: InvestorDistribution) => {
    setSelectedDistribution(distribution)
    setMarkPaidModalOpen(true)
  }

  const handleBulkApprove = async () => {
    for (const id of selectedDistributions) {
      await approveMutation.mutateAsync({ distributionId: id })
    }
    setSelectedDistributions([])
  }

  const handleExport = () => {
    console.log('Export distributions')
  }

  if (isLoading) {
    return (
      <div className="flex items-center justify-center min-h-screen">
        <div className="text-center">
          <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-primary mx-auto mb-4" />
          <p className="text-muted-foreground">Loading distributions...</p>
        </div>
      </div>
    )
  }

  return (
    <div className="space-y-6 p-6">
      {/* Header */}
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-3xl font-bold">Profit Distributions</h1>
          <p className="text-muted-foreground mt-1">Manage and track investor distributions</p>
        </div>
        <Button variant="outline" onClick={handleExport}>
          <Download className="h-4 w-4 mr-2" />
          Export Report
        </Button>
      </div>

      {/* Summary Cards */}
      <div className="grid grid-cols-1 md:grid-cols-4 gap-4">
        <Card>
          <CardHeader className="pb-3">
            <CardTitle className="text-sm font-medium text-muted-foreground">
              Total Distributed
            </CardTitle>
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold">{formatCurrency(summary.totalDistributed)}</div>
            <p className="text-xs text-muted-foreground mt-1">All time</p>
          </CardContent>
        </Card>

        <Card>
          <CardHeader className="pb-3">
            <CardTitle className="text-sm font-medium text-muted-foreground">
              Pending Approval
            </CardTitle>
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold text-yellow-600">{summary.pendingApproval}</div>
            <p className="text-xs text-muted-foreground mt-1">Awaiting action</p>
          </CardContent>
        </Card>

        <Card>
          <CardHeader className="pb-3">
            <CardTitle className="text-sm font-medium text-muted-foreground">
              Approved Payment
            </CardTitle>
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold text-blue-600">{summary.approvedPayment}</div>
            <p className="text-xs text-muted-foreground mt-1">Ready to pay</p>
          </CardContent>
        </Card>

        <Card>
          <CardHeader className="pb-3">
            <CardTitle className="text-sm font-medium text-muted-foreground">
              Paid This Month
            </CardTitle>
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold text-green-600">
              {formatCurrency(summary.paidThisMonth)}
            </div>
            <p className="text-xs text-muted-foreground mt-1">Current month</p>
          </CardContent>
        </Card>
      </div>

      {/* Filters and Actions */}
      <div className="flex items-center justify-between">
        <div className="flex items-center gap-4">
          <Select value={statusFilter} onValueChange={setStatusFilter}>
            <SelectTrigger className="w-[180px]">
              <SelectValue placeholder="Filter by status" />
            </SelectTrigger>
            <SelectContent>
              <SelectItem value="all">All Status</SelectItem>
              <SelectItem value="PENDING">Pending</SelectItem>
              <SelectItem value="APPROVED">Approved</SelectItem>
              <SelectItem value="PAID">Paid</SelectItem>
            </SelectContent>
          </Select>
          {selectedDistributions.length > 0 && (
            <p className="text-sm text-muted-foreground">
              {selectedDistributions.length} selected
            </p>
          )}
        </div>
        {selectedDistributions.length > 0 && (
          <Button onClick={handleBulkApprove} disabled={approveMutation.isPending}>
            <Check className="h-4 w-4 mr-2" />
            Bulk Approve ({selectedDistributions.length})
          </Button>
        )}
      </div>

      {/* Distribution Table */}
      <Card>
        <CardHeader>
          <CardTitle>
            Distribution List ({filteredDistributions.length})
          </CardTitle>
        </CardHeader>
        <CardContent>
          {filteredDistributions.length === 0 ? (
            <div className="text-center py-8 text-muted-foreground">
              <p>No distributions found</p>
            </div>
          ) : (
            <div className="overflow-x-auto">
              <table className="w-full">
                <thead>
                  <tr className="border-b">
                    <th className="text-left py-3 px-4">
                      <Checkbox
                        checked={selectedDistributions.length === filteredDistributions.length}
                        onCheckedChange={handleSelectAll}
                      />
                    </th>
                    <th className="text-left py-3 px-4 text-sm font-medium text-muted-foreground">
                      ID
                    </th>
                    <th className="text-left py-3 px-4 text-sm font-medium text-muted-foreground">
                      Period
                    </th>
                    <th className="text-left py-3 px-4 text-sm font-medium text-muted-foreground">
                      Investment
                    </th>
                    <th className="text-left py-3 px-4 text-sm font-medium text-muted-foreground">
                      Investor
                    </th>
                    <th className="text-right py-3 px-4 text-sm font-medium text-muted-foreground">
                      Profit
                    </th>
                    <th className="text-right py-3 px-4 text-sm font-medium text-muted-foreground">
                      Share %
                    </th>
                    <th className="text-right py-3 px-4 text-sm font-medium text-muted-foreground">
                      Amount
                    </th>
                    <th className="text-center py-3 px-4 text-sm font-medium text-muted-foreground">
                      Status
                    </th>
                    <th className="text-center py-3 px-4 text-sm font-medium text-muted-foreground">
                      Actions
                    </th>
                  </tr>
                </thead>
                <tbody>
                  {filteredDistributions.map((distribution) => (
                    <tr key={distribution.id} className="border-b hover:bg-muted/50">
                      <td className="py-3 px-4">
                        <Checkbox
                          checked={selectedDistributions.includes(distribution.id)}
                          onCheckedChange={() => handleSelectDistribution(distribution.id)}
                        />
                      </td>
                      <td className="py-3 px-4 font-mono text-sm">
                        {distribution.id.substring(0, 8)}
                      </td>
                      <td className="py-3 px-4">
                        <p className="font-medium text-sm">
                          {format(new Date(distribution.periodStart), 'MMM yyyy')}
                        </p>
                        <p className="text-xs text-muted-foreground">
                          {format(new Date(distribution.periodStart), 'dd')} -{' '}
                          {format(new Date(distribution.periodEnd), 'dd')}
                        </p>
                      </td>
                      <td className="py-3 px-4 text-sm">
                        {distribution.investmentNumber}
                      </td>
                      <td className="py-3 px-4">
                        <p className="font-medium text-sm">{distribution.investorName}</p>
                      </td>
                      <td className="py-3 px-4 text-right font-medium text-green-600">
                        {formatCurrency(distribution.totalProfit)}
                      </td>
                      <td className="py-3 px-4 text-right">
                        {distribution.investorSharePercentage}%
                      </td>
                      <td className="py-3 px-4 text-right font-semibold">
                        {formatCurrency(distribution.distributionAmount)}
                      </td>
                      <td className="py-3 px-4 text-center">
                        <Badge className={getStatusColor(distribution.status)}>
                          {distribution.status}
                        </Badge>
                      </td>
                      <td className="py-3 px-4">
                        <div className="flex items-center justify-center gap-1">
                          {distribution.status === 'PENDING' && (
                            <Button
                              variant="ghost"
                              size="sm"
                              onClick={() => handleApprove(distribution)}
                            >
                              <Check className="h-4 w-4" />
                            </Button>
                          )}
                          {distribution.status === 'APPROVED' && (
                            <Button
                              variant="ghost"
                              size="sm"
                              onClick={() => handleMarkPaid(distribution)}
                            >
                              Pay
                            </Button>
                          )}
                          <Button variant="ghost" size="sm">
                            View
                          </Button>
                        </div>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
          
          {/* Pagination */}
          {totalPages > 1 && (
            <div className="flex items-center justify-between mt-4 pt-4 border-t">
              <div className="text-sm text-muted-foreground">
                Showing {page * pageSize + 1} to {Math.min((page + 1) * pageSize, totalElements)} of {totalElements} distributions
              </div>
              <div className="flex gap-2">
                <Button
                  variant="outline"
                  size="sm"
                  onClick={() => setPage(p => Math.max(0, p - 1))}
                  disabled={page === 0}
                >
                  Previous
                </Button>
                <Button
                  variant="outline"
                  size="sm"
                  onClick={() => setPage(p => Math.min(totalPages - 1, p + 1))}
                  disabled={page >= totalPages - 1}
                >
                  Next
                </Button>
              </div>
            </div>
          )}
        </CardContent>
      </Card>

      {/* Modals */}
      {selectedDistribution && (
        <>
          <ApproveDistributionModal
            open={approveModalOpen}
            onClose={() => setApproveModalOpen(false)}
            distribution={selectedDistribution}
          />
          <MarkPaidModal
            open={markPaidModalOpen}
            onClose={() => setMarkPaidModalOpen(false)}
            distribution={selectedDistribution}
          />
        </>
      )}
    </div>
  )
}
