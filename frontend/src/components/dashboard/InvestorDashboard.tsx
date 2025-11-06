import React from 'react'
import { useAuth } from '@/context/ManualAuthContext'
import { usePermissions } from '@/hooks/usePermissions'
import { useInvestments } from '@/hooks/investment/useInvestments'
import { useDistributions } from '@/hooks/investment/useDistributions'
import { usePortfolioSummary } from '@/hooks/investment/usePortfolioSummary'
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card'
import { Button } from '@/components/ui/button'
import { LoadingSpinner } from '@/components/ui/loading-spinner'
import { Badge } from '@/components/ui/badge'
import {
  TrendingUp,
  DollarSign,
  Target,
  ArrowUpRight,
  Coins,
  BarChart3,
  TrendingDown
} from 'lucide-react'
import { Link } from 'react-router-dom'
import { useCurrency } from '@/hooks/useCurrency'
import { format } from 'date-fns'

export const InvestorDashboard: React.FC = () => {
  const { user } = useAuth()
  const permissions = usePermissions()
  const { formatCurrency } = useCurrency()
  
  // Fetch real data
  const { data: investmentsData, isLoading: investmentsLoading } = useInvestments({
    page: 0,
    size: 5,
    sortBy: 'investmentDate',
    sortDir: 'desc'
  })
  
  const { data: distributionsData, isLoading: distributionsLoading } = useDistributions({
    page: 0,
    size: 5
  })
  
  // Extract investments array from paginated response
  const investments = investmentsData?.content || []
  const portfolioSummary = usePortfolioSummary(investments)
  const statsLoading = investmentsLoading || distributionsLoading

  // Portfolio stats from real data
  const portfolioStats = [
    {
      title: 'Total Invested',
      value: formatCurrency(portfolioSummary.totalInvested),
      description: 'Across all investments',
      icon: DollarSign,
      color: 'text-blue-600'
    },
    {
      title: 'Total Returns',
      value: formatCurrency(portfolioSummary.totalReturns),
      description: 'Profit generated',
      icon: TrendingUp,
      color: 'text-green-600'
    },
    {
      title: 'Available Balance',
      value: formatCurrency(portfolioSummary.availableBalance),
      description: 'Ready to withdraw',
      icon: ArrowUpRight,
      color: 'text-emerald-600'
    },
    {
      title: 'Average ROI',
      value: `${portfolioSummary.averageROI.toFixed(1)}%`,
      description: 'Return on investment',
      icon: Target,
      color: 'text-purple-600'
    }
  ]

  // Get status badge color
  const getStatusColor = (status: string) => {
    const colors: Record<string, string> = {
      ACTIVE: 'bg-green-100 text-green-800',
      MATURED: 'bg-blue-100 text-blue-800',
      WITHDRAWN: 'bg-gray-100 text-gray-800',
      DEFAULTED: 'bg-red-100 text-red-800',
    }
    return colors[status] || 'bg-gray-100 text-gray-800'
  }

  // Calculate ROI for individual investment
  const calculateROI = (totalReturns: number, amount: number) => {
    if (amount === 0) return 0
    return ((totalReturns / amount) * 100)
  }

  // Performance metrics from real data
  const performanceMetrics = [
    { 
      label: 'Active Investments', 
      value: portfolioSummary.activeCount.toString(),
      change: `${portfolioSummary.totalCount} Total` 
    },
    { 
      label: 'Best ROI', 
      value: investments.length > 0 
        ? `${Math.max(...investments.map(inv => calculateROI(inv.totalProfitEarned, inv.amount))).toFixed(1)}%`
        : '0%',
      change: 'Top performer' 
    },
    { 
      label: 'Total Withdrawn', 
      value: formatCurrency(portfolioSummary.totalWithdrawn),
      change: 'All time' 
    },
    { 
      label: 'Portfolio Value', 
      value: formatCurrency(portfolioSummary.totalInvested + portfolioSummary.totalReturns),
      change: 'Current worth' 
    }
  ]
  
  // Handle both array and paginated response for distributions
  const recentDistributions = Array.isArray(distributionsData) 
    ? distributionsData 
    : (distributionsData?.content || [])

  if (statsLoading) {
    return (
      <div className="flex items-center justify-center min-h-[400px]">
        <LoadingSpinner size="lg" />
      </div>
    )
  }

  return (
    <div className="space-y-6">
      {/* Welcome Header */}
      <div className="flex justify-between items-start">
        <div>
          <h1 className="text-3xl font-bold text-gray-900">
            Investment Portfolio
          </h1>
          <p className="text-muted-foreground">
            Welcome back, {user?.firstName || user?.username}. Track your investment performance.
          </p>
        </div>
        <div className="flex space-x-2">
          {permissions.canViewAnalytics() && (
            <Button variant="outline" asChild>
              <Link to="/analytics">
                <BarChart3 className="mr-2 h-4 w-4" />
                Analytics
              </Link>
            </Button>
          )}
          {permissions.canCreateInvestment() && (
            <Button asChild>
              <Link to="/investments/create">
                <Coins className="mr-2 h-4 w-4" />
                New Investment
              </Link>
            </Button>
          )}
        </div>
      </div>

      {/* Portfolio Overview */}
      <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-4">
        {portfolioStats.map((stat, index) => (
          <Card key={index} className="hover:shadow-md transition-shadow">
            <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
              <CardTitle className="text-sm font-medium">
                {stat.title}
              </CardTitle>
              <stat.icon className={`h-4 w-4 ${stat.color}`} />
            </CardHeader>
            <CardContent>
              <div className="text-2xl font-bold">{stat.value}</div>
              <p className="text-xs text-muted-foreground">
                {stat.description}
              </p>
            </CardContent>
          </Card>
        ))}
      </div>

      {/* Performance Metrics */}
      <Card>
        <CardHeader>
          <CardTitle>Performance Highlights</CardTitle>
          <CardDescription>Key metrics from your investment portfolio</CardDescription>
        </CardHeader>
        <CardContent>
          <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
            {performanceMetrics.map((metric, index) => (
              <div key={index} className="text-center p-4 bg-gray-50 rounded-lg">
                <div className="text-lg font-semibold text-gray-900">{metric.value}</div>
                <div className="text-sm text-muted-foreground">{metric.label}</div>
                <div className="text-sm font-medium text-green-600">{metric.change}</div>
              </div>
            ))}
          </div>
        </CardContent>
      </Card>

      <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-3">
        {/* Active Investments */}
        <Card className="col-span-2">
          <CardHeader>
            <CardTitle>Active Investments</CardTitle>
            <CardDescription>
              Your current investment positions
            </CardDescription>
          </CardHeader>
          <CardContent>
            {investments.length === 0 ? (
              <div className="text-center py-8">
                <p className="text-muted-foreground mb-4">No investments yet</p>
                {permissions.canCreateInvestment() && (
                  <Button asChild>
                    <Link to="/investments/create">Create Your First Investment</Link>
                  </Button>
                )}
              </div>
            ) : (
              <>
                <div className="space-y-4">
                  {investments.slice(0, 5).map((investment) => {
                    const roi = calculateROI(investment.totalProfitEarned, investment.amount)
                    return (
                      <Link 
                        key={investment.id} 
                        to={`/investments/${investment.id}`}
                        className="flex items-center justify-between p-4 border rounded-lg hover:bg-muted/50 transition-colors"
                      >
                        <div className="flex items-center space-x-4">
                          <Badge className={getStatusColor(investment.status)}>
                            {investment.status}
                          </Badge>
                          <div>
                            <p className="font-medium">{investment.shopName}</p>
                            <p className="text-sm text-muted-foreground">
                              {investment.investmentType.replace(/_/g, ' ')} • {format(new Date(investment.investmentDate), 'MMM dd, yyyy')}
                            </p>
                          </div>
                        </div>
                        <div className="text-right space-y-1">
                          <div className="flex items-center space-x-2">
                            <span className="text-sm text-muted-foreground">
                              {formatCurrency(investment.amount)}
                            </span>
                            <ArrowUpRight className="h-3 w-3" />
                            <span className="font-semibold">
                              {formatCurrency(investment.amount + investment.totalProfitEarned)}
                            </span>
                          </div>
                          <div className="flex items-center justify-end space-x-1">
                            {roi >= 0 ? (
                              <>
                                <TrendingUp className="h-3 w-3 text-green-600" />
                                <span className="text-sm font-medium text-green-600">
                                  +{roi.toFixed(1)}%
                                </span>
                              </>
                            ) : (
                              <>
                                <TrendingDown className="h-3 w-3 text-red-600" />
                                <span className="text-sm font-medium text-red-600">
                                  {roi.toFixed(1)}%
                                </span>
                              </>
                            )}
                          </div>
                        </div>
                      </Link>
                    )
                  })}
                </div>
                {permissions.canViewInvestments() && (
                  <Button variant="outline" className="w-full mt-4" asChild>
                    <Link to="/investments">View All Investments</Link>
                  </Button>
                )}
              </>
            )}
          </CardContent>
        </Card>

        {/* Recent Distributions */}
        <Card>
          <CardHeader>
            <CardTitle>Recent Returns</CardTitle>
            <CardDescription>
              Latest profit distributions
            </CardDescription>
          </CardHeader>
          <CardContent>
            {recentDistributions.length === 0 ? (
              <div className="text-center py-8">
                <p className="text-muted-foreground">No distributions yet</p>
              </div>
            ) : (
              <>
                <div className="space-y-4">
                  {recentDistributions.slice(0, 5).map((distribution) => (
                    <div key={distribution.id} className="flex items-start justify-between p-3 border-b last:border-0">
                      <div className="flex items-start space-x-3">
                        <Badge className={
                          distribution.status === 'PAID' ? 'bg-green-100 text-green-800' :
                          distribution.status === 'APPROVED' ? 'bg-blue-100 text-blue-800' :
                          'bg-yellow-100 text-yellow-800'
                        }>
                          {distribution.status}
                        </Badge>
                        <div className="flex-1 space-y-1">
                          <p className="text-sm font-medium leading-none">
                            {formatCurrency(distribution.distributionAmount)}
                          </p>
                          <p className="text-xs text-muted-foreground">
                            {distribution.investmentNumber}
                          </p>
                          <p className="text-xs text-muted-foreground">
                            {distribution.distributionDate 
                              ? format(new Date(distribution.distributionDate), 'MMM dd, yyyy')
                              : format(new Date(distribution.periodEnd), 'MMM dd, yyyy')}
                          </p>
                        </div>
                      </div>
                    </div>
                  ))}
                </div>
                {permissions.canViewInvestments() && (
                  <Button variant="outline" className="w-full mt-4" asChild>
                    <Link to="/investments/distributions">View All Distributions</Link>
                  </Button>
                )}
              </>
            )}
          </CardContent>
        </Card>
      </div>

      {/* Investment Allocation by Type */}
      <Card>
        <CardHeader>
          <CardTitle>Investment Breakdown</CardTitle>
          <CardDescription>
            Your investments by type
          </CardDescription>
        </CardHeader>
        <CardContent>
          {investments.length === 0 ? (
            <div className="text-center py-8">
              <p className="text-muted-foreground">No investment data available</p>
            </div>
          ) : (
            <div className="space-y-3">
              {Object.entries(
                investments.reduce((acc, inv) => {
                  const type = inv.investmentType
                  if (!acc[type]) {
                    acc[type] = { count: 0, amount: 0 }
                  }
                  acc[type].count++
                  acc[type].amount += inv.amount
                  return acc
                }, {} as Record<string, { count: number; amount: number }>)
              ).map(([type, data]) => (
                <div key={type} className="flex items-center justify-between p-3 border rounded-lg">
                  <div>
                    <p className="font-medium">{type.replace(/_/g, ' ')}</p>
                    <p className="text-sm text-muted-foreground">{data.count} investment{data.count > 1 ? 's' : ''}</p>
                  </div>
                  <p className="font-semibold">{formatCurrency(data.amount)}</p>
                </div>
              ))}
            </div>
          )}
        </CardContent>
      </Card>
    </div>
  )
}