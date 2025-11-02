import React, { useState } from 'react'
import { useParams, useNavigate } from 'react-router-dom'
import { ArrowLeft, Download, MoreVertical, TrendingUp, Calendar, DollarSign } from 'lucide-react'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import { Button } from '@/components/ui/button'
import { Badge } from '@/components/ui/badge'
import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/components/ui/tabs'
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuTrigger,
} from '@/components/ui/dropdown-menu'
import { useInvestment } from '@/hooks/investment/useInvestments'
import { useInvestmentDistributions } from '@/hooks/investment/useDistributions'
import { ROIChart } from '@/components/investment/ROIChart'
import { calculateROI } from '@/hooks/investment/usePortfolioSummary'
import { useCurrency } from '@/hooks/useCurrency'
import { format } from 'date-fns'
import type { ROIChartData, InvestorDistribution } from '@/types/investment'

export const InvestmentDetailPage: React.FC = () => {
  const { id } = useParams<{ id: string }>()
  const navigate = useNavigate()
  const { formatCurrency } = useCurrency()
  const [activeTab, setActiveTab] = useState('overview')

  const { data: investment, isLoading: investmentLoading } = useInvestment(id!)
  const { data: distributions = [], isLoading: distributionsLoading } = useInvestmentDistributions(id!)

  if (investmentLoading) {
    return (
      <div className="flex items-center justify-center min-h-screen">
        <div className="text-center">
          <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-primary mx-auto mb-4" />
          <p className="text-muted-foreground">Loading investment details...</p>
        </div>
      </div>
    )
  }

  if (!investment) {
    return (
      <div className="flex items-center justify-center min-h-screen">
        <div className="text-center">
          <p className="text-xl font-semibold mb-2">Investment not found</p>
          <Button onClick={() => navigate('/investments')}>
            <ArrowLeft className="h-4 w-4 mr-2" />
            Back to Investments
          </Button>
        </div>
      </div>
    )
  }

  const roi = calculateROI(investment)
  const netProfit = investment.totalProfitEarned - investment.totalWithdrawn

  // Prepare chart data from distributions
  const chartData: ROIChartData[] = distributions.map((dist, index) => {
    const cumulativeProfit = distributions
      .slice(0, index + 1)
      .reduce((sum, d) => sum + d.distributionAmount, 0)
    
    return {
      date: dist.periodEnd,
      actualReturn: dist.distributionAmount,
      expectedReturn: investment.amount * 0.02, // 2% monthly estimate
      cumulativeROI: (cumulativeProfit / investment.amount) * 100,
      profit: dist.totalProfit,
    }
  })

  const getStatusColor = (status: string) => {
    const colors = {
      ACTIVE: 'bg-green-100 text-green-800',
      MATURED: 'bg-blue-100 text-blue-800',
      WITHDRAWN: 'bg-gray-100 text-gray-800',
      DEFAULTED: 'bg-red-100 text-red-800',
    }
    return colors[status as keyof typeof colors] || 'bg-gray-100 text-gray-800'
  }

  const getTypeIcon = (type: string) => {
    const icons = {
      SHOP_WIDE: '🏪',
      PRODUCT_SPECIFIC: '📦',
      CATEGORY_BASED: '📂',
    }
    return icons[type as keyof typeof icons] || '🏪'
  }

  return (
    <div className="space-y-6 p-6">
      {/* Header */}
      <div className="flex items-center justify-between">
        <div className="flex items-center gap-4">
          <Button variant="ghost" size="sm" onClick={() => navigate('/investments')}>
            <ArrowLeft className="h-4 w-4 mr-2" />
            Back
          </Button>
          <div>
            <div className="flex items-center gap-3">
              <h1 className="text-3xl font-bold">{investment.investmentNumber}</h1>
              <Badge className={getStatusColor(investment.status)}>
                {investment.status}
              </Badge>
            </div>
            <p className="text-muted-foreground mt-1">
              Investment Details
            </p>
          </div>
        </div>

        <div className="flex gap-2">
          <Button variant="outline">
            <Download className="h-4 w-4 mr-2" />
            Download Report
          </Button>
          <DropdownMenu>
            <DropdownMenuTrigger asChild>
              <Button variant="outline" size="icon">
                <MoreVertical className="h-4 w-4" />
              </Button>
            </DropdownMenuTrigger>
            <DropdownMenuContent align="end">
              <DropdownMenuItem>Request Withdrawal</DropdownMenuItem>
              <DropdownMenuItem>Update Status</DropdownMenuItem>
              <DropdownMenuItem>View Agreement</DropdownMenuItem>
              <DropdownMenuItem className="text-red-600">Delete Investment</DropdownMenuItem>
            </DropdownMenuContent>
          </DropdownMenu>
        </div>
      </div>

      {/* Investor Info Card */}
      <Card>
        <CardContent className="pt-6">
          <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
            <div>
              <div className="flex items-center gap-3 mb-2">
                <div className="h-12 w-12 rounded-full bg-primary/10 flex items-center justify-center text-xl font-semibold">
                  {investment.investorName.charAt(0)}
                </div>
                <div>
                  <p className="font-semibold">{investment.investorName}</p>
                  <p className="text-sm text-muted-foreground">{investment.investorEmail}</p>
                </div>
              </div>
            </div>
            <div>
              <p className="text-sm text-muted-foreground mb-1">Shop</p>
              <p className="font-semibold">{investment.shopName}</p>
              <p className="text-sm text-muted-foreground mt-2">Type</p>
              <div className="flex items-center gap-2 mt-1">
                <span className="text-lg">{getTypeIcon(investment.investmentType)}</span>
                <span className="font-medium">{investment.investmentType.replace('_', ' ')}</span>
              </div>
            </div>
            <div>
              <p className="text-sm text-muted-foreground mb-1">Investment Date</p>
              <p className="font-semibold">
                {format(new Date(investment.investmentDate), 'MMM dd, yyyy')}
              </p>
              {investment.maturityDate && (
                <>
                  <p className="text-sm text-muted-foreground mt-2">Maturity Date</p>
                  <p className="font-semibold">
                    {format(new Date(investment.maturityDate), 'MMM dd, yyyy')}
                  </p>
                </>
              )}
            </div>
          </div>
        </CardContent>
      </Card>

      {/* Financial Summary Cards */}
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4">
        <Card>
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium text-muted-foreground">
              Initial Investment
            </CardTitle>
            <DollarSign className="h-4 w-4 text-blue-600" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold">{formatCurrency(investment.amount)}</div>
            <p className="text-xs text-muted-foreground mt-1">Principal amount</p>
          </CardContent>
        </Card>

        <Card>
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium text-muted-foreground">
              Total Earned
            </CardTitle>
            <TrendingUp className="h-4 w-4 text-green-600" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold text-green-600">
              {formatCurrency(investment.totalProfitEarned)}
            </div>
            <p className="text-xs text-muted-foreground mt-1">
              {distributions.length} distributions
            </p>
          </CardContent>
        </Card>

        <Card>
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium text-muted-foreground">
              Available Balance
            </CardTitle>
            <Calendar className="h-4 w-4 text-purple-600" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold text-purple-600">
              {formatCurrency(investment.availableBalance)}
            </div>
            <p className="text-xs text-muted-foreground mt-1">Ready for withdrawal</p>
          </CardContent>
        </Card>

        <Card>
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium text-muted-foreground">
              Current ROI
            </CardTitle>
            <TrendingUp className="h-4 w-4 text-orange-600" />
          </CardHeader>
          <CardContent>
            <div className={`text-2xl font-bold ${roi >= 0 ? 'text-green-600' : 'text-red-600'}`}>
              {roi.toFixed(2)}%
            </div>
            <p className="text-xs text-muted-foreground mt-1">
              Net: {formatCurrency(netProfit)}
            </p>
          </CardContent>
        </Card>
      </div>

      {/* Tabbed Content */}
      <Tabs value={activeTab} onValueChange={setActiveTab}>
        <TabsList>
          <TabsTrigger value="overview">Overview</TabsTrigger>
          <TabsTrigger value="distributions">
            Distributions
            {distributions.length > 0 && (
              <Badge variant="secondary" className="ml-2">
                {distributions.length}
              </Badge>
            )}
          </TabsTrigger>
          <TabsTrigger value="timeline">Timeline</TabsTrigger>
        </TabsList>

        <TabsContent value="overview" className="space-y-6 mt-6">
          {/* Performance Chart */}
          <ROIChart
            investmentId={investment.id}
            data={chartData}
            height={350}
            showExpected={true}
            showCumulative={true}
          />

          {/* Investment Terms */}
          <Card>
            <CardHeader>
              <CardTitle>Investment Terms</CardTitle>
            </CardHeader>
            <CardContent>
              <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                <div className="space-y-4">
                  <div>
                    <p className="text-sm text-muted-foreground mb-1">Investment Type</p>
                    <p className="font-semibold">
                      {investment.investmentType.replace('_', ' ')}
                    </p>
                  </div>
                  <div>
                    <p className="text-sm text-muted-foreground mb-1">Profit Sharing Model</p>
                    <p className="font-semibold">
                      {investment.profitSharingModel.replace('_', ' ')}
                    </p>
                  </div>
                  {investment.profitPercentage && (
                    <div>
                      <p className="text-sm text-muted-foreground mb-1">Profit Share Percentage</p>
                      <p className="font-semibold">{investment.profitPercentage}%</p>
                    </div>
                  )}
                </div>
                <div className="space-y-4">
                  <div>
                    <p className="text-sm text-muted-foreground mb-1">Investment Amount</p>
                    <p className="font-semibold">{formatCurrency(investment.amount)}</p>
                  </div>
                  {investment.maturityDate && (
                    <div>
                      <p className="text-sm text-muted-foreground mb-1">Duration</p>
                      <p className="font-semibold">
                        {format(new Date(investment.investmentDate), 'MMM yyyy')} -{' '}
                        {format(new Date(investment.maturityDate), 'MMM yyyy')}
                      </p>
                    </div>
                  )}
                  {investment.products && investment.products.length > 0 && (
                    <div>
                      <p className="text-sm text-muted-foreground mb-1">Products</p>
                      <p className="font-semibold">{investment.products.length} products</p>
                    </div>
                  )}
                </div>
              </div>
              {investment.notes && (
                <div className="mt-6 pt-6 border-t">
                  <p className="text-sm text-muted-foreground mb-2">Notes</p>
                  <p className="text-sm">{investment.notes}</p>
                </div>
              )}
            </CardContent>
          </Card>
        </TabsContent>

        <TabsContent value="distributions" className="mt-6">
          <DistributionHistoryTable
            distributions={distributions}
            isLoading={distributionsLoading}
            investmentId={investment.id}
          />
        </TabsContent>

        <TabsContent value="timeline" className="mt-6">
          <Card>
            <CardContent className="pt-6">
              <p className="text-center text-muted-foreground">
                Timeline feature coming soon...
              </p>
            </CardContent>
          </Card>
        </TabsContent>
      </Tabs>
    </div>
  )
}

// Distribution History Table Component
interface DistributionHistoryTableProps {
  distributions: InvestorDistribution[]
  isLoading: boolean
  investmentId: string
}

function DistributionHistoryTable({
  distributions,
  isLoading,
  investmentId,
}: DistributionHistoryTableProps) {
  const { formatCurrency } = useCurrency()

  const getStatusColor = (status: string) => {
    const colors = {
      PENDING: 'bg-yellow-100 text-yellow-800',
      APPROVED: 'bg-blue-100 text-blue-800',
      PAID: 'bg-green-100 text-green-800',
      REJECTED: 'bg-red-100 text-red-800',
    }
    return colors[status as keyof typeof colors] || 'bg-gray-100 text-gray-800'
  }

  if (isLoading) {
    return (
      <Card>
        <CardContent className="pt-6">
          <div className="text-center py-8">
            <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-primary mx-auto mb-2" />
            <p className="text-muted-foreground">Loading distributions...</p>
          </div>
        </CardContent>
      </Card>
    )
  }

  if (distributions.length === 0) {
    return (
      <Card>
        <CardContent className="pt-6">
          <div className="text-center py-8">
            <p className="text-muted-foreground">No distributions yet</p>
          </div>
        </CardContent>
      </Card>
    )
  }

  return (
    <Card>
      <CardHeader>
        <CardTitle>Distribution History ({distributions.length})</CardTitle>
      </CardHeader>
      <CardContent>
        <div className="overflow-x-auto">
          <table className="w-full">
            <thead>
              <tr className="border-b">
                <th className="text-left py-3 px-4 text-sm font-medium text-muted-foreground">
                  Period
                </th>
                <th className="text-right py-3 px-4 text-sm font-medium text-muted-foreground">
                  Sales Revenue
                </th>
                <th className="text-right py-3 px-4 text-sm font-medium text-muted-foreground">
                  Total Profit
                </th>
                <th className="text-right py-3 px-4 text-sm font-medium text-muted-foreground">
                  Share %
                </th>
                <th className="text-right py-3 px-4 text-sm font-medium text-muted-foreground">
                  Distribution
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
              {distributions.map((distribution) => (
                <tr key={distribution.id} className="border-b hover:bg-muted/50">
                  <td className="py-3 px-4">
                    <p className="font-medium text-sm">
                      {format(new Date(distribution.periodStart), 'MMM yyyy')}
                    </p>
                    <p className="text-xs text-muted-foreground">
                      {format(new Date(distribution.periodStart), 'dd')} -{' '}
                      {format(new Date(distribution.periodEnd), 'dd')}
                    </p>
                  </td>
                  <td className="py-3 px-4 text-right font-medium">
                    {formatCurrency(distribution.totalSalesRevenue)}
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
                  <td className="py-3 px-4 text-center">
                    <Button variant="ghost" size="sm">
                      View
                    </Button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </CardContent>
    </Card>
  )
}
