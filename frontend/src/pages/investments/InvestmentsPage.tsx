import React, { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { Plus, Download, Users } from 'lucide-react'
import { Card, CardContent } from '@/components/ui/card'
import { Button } from '@/components/ui/button'
import { useAuth } from '@/context/ManualAuthContext'
import { useInvestments } from '@/hooks/investment/useInvestments'
import { usePortfolioSummary } from '@/hooks/investment/usePortfolioSummary'
import { InvestmentSummaryCards } from '@/components/investment/InvestmentSummaryCards'
import { InvestmentList } from '@/components/investment/InvestmentList'
import type { InvestmentFilters } from '@/types/investment'

export const InvestmentsPage: React.FC = () => {
  const navigate = useNavigate()
  const { hasPermission } = useAuth()

  // Check permissions based on backend permission matrix
  const canCreateInvestment = hasPermission('INVESTMENT_CREATE')  // OWNER and above
  const canUpdateInvestment = hasPermission('INVESTMENT_UPDATE')  // OWNER and above
  const canDeleteInvestment = hasPermission('INVESTMENT_DELETE')  // OWNER and above
  const canViewInvestments = hasPermission('INVESTMENT_LIST')     // MANAGER, INVESTOR and above
  const [page, setPage] = useState(0)
  const [size] = useState(20)
  const [sortBy, setSortBy] = useState('investmentDate')
  const [sortDir, setSortDir] = useState<'asc' | 'desc'>('desc')
  // const [filters, setFilters] = useState<InvestmentFilters>({
  //   status: [],
  //   type: [],
  //   dateRange: {
  //     start: undefined,
  //     end: undefined,
  //   },
  //   amountRange: {
  //     min: undefined,
  //     max: undefined,
  //   },
  //   search: '',
  // })

  // Fetch investments
  const { data: investmentsData, isLoading } = useInvestments({
    page,
    size,
    sortBy,
    sortDir,
  })

  // Calculate portfolio summary
  const investments = investmentsData?.content || []
  const portfolioSummary = usePortfolioSummary(investments)

  // Transform for InvestmentSummaryCards component
  const summaryForCards = {
    totalInvested: portfolioSummary.totalInvested,
    totalProfitEarned: portfolioSummary.totalReturns,
    totalWithdrawn: portfolioSummary.totalWithdrawn,
    availableBalance: portfolioSummary.availableBalance,
    activeInvestments: portfolioSummary.activeCount,
    totalInvestments: investments.length,
    pendingDistributions: 0, // TODO: Get from distributions API
    totalROI: portfolioSummary.averageROI,
  }

  const handleCreateInvestment = () => {
    navigate('/investments/create')
  }

  const handleExportReport = () => {
    // TODO: Implement export functionality
    console.log('Export report clicked')
  }

  const handleSort = (field: string) => {
    if (sortBy === field) {
      setSortDir(sortDir === 'asc' ? 'desc' : 'asc')
    } else {
      setSortBy(field)
      setSortDir('desc')
    }
  }

  const handlePageChange = (newPage: number) => {
    setPage(newPage)
  }

  return (
    <div className="space-y-6 p-6">
      {/* Page Header */}
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-3xl font-bold">Investment Portfolio</h1>
          <p className="text-muted-foreground mt-1">
            Track your investments, returns, and profit distributions
          </p>
        </div>
        <div className="flex gap-2">
          <Button variant="outline" onClick={handleExportReport}>
            <Download className="h-4 w-4 mr-2" />
            Export Report
          </Button>
          <Button variant="outline" onClick={() => navigate('/investments/rounds')}>
            <Users className="h-4 w-4 mr-2" />
            Investment Rounds
          </Button>
          {canCreateInvestment && (
            <Button onClick={handleCreateInvestment}>
              <Plus className="h-4 w-4 mr-2" />
              New Investment
            </Button>
          )}
        </div>
      </div>

      {/* Summary Cards */}
      <InvestmentSummaryCards summary={summaryForCards} isLoading={isLoading} />

      {/* Investments List */}
      <Card>
        <CardContent className="pt-6">
          <InvestmentList
            investments={investments}
            isLoading={isLoading}
            onSort={handleSort}
            sortBy={sortBy}
            sortDir={sortDir}
            pagination={{
              page,
              size,
              totalPages: investmentsData?.totalPages || 0,
              totalElements: investmentsData?.totalElements || 0,
              onPageChange: handlePageChange,
            }}
          />
        </CardContent>
      </Card>
    </div>
  )
}