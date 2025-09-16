import React from 'react'
import { useAuth } from '@/context/AuthContext'
import { useDashboardStats } from '@/hooks/useDashboard'
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card'
import { Button } from '@/components/ui/button'
import { LoadingSpinner } from '@/components/ui/loading-spinner'
import {
  TrendingUp,
  DollarSign,
  PieChart,
  Target,
  Calendar,
  ArrowUpRight,
  ArrowDownRight,
  Coins,
  BarChart3
} from 'lucide-react'
import { Link } from 'react-router-dom'

interface Investment {
  id: string
  name: string
  amount: number
  currentValue: number
  roi: number
  status: 'active' | 'completed' | 'pending'
  startDate: string
  category: string
}

interface Distribution {
  id: string
  amount: number
  date: string
  source: string
  type: 'dividend' | 'profit_share' | 'return'
}

export const InvestorDashboard: React.FC = () => {
  const { user } = useAuth()
  const { stats, loading: statsLoading } = useDashboardStats()

  // Mock data - would come from API
  const portfolioStats = [
    {
      title: 'Total Invested',
      value: '$125,000',
      description: 'Across all investments',
      icon: DollarSign,
      color: 'text-blue-600'
    },
    {
      title: 'Current Value',
      value: '$156,750',
      description: 'Portfolio worth',
      icon: TrendingUp,
      color: 'text-green-600'
    },
    {
      title: 'Total Returns',
      value: '$31,750',
      description: 'Profit generated',
      icon: ArrowUpRight,
      color: 'text-emerald-600'
    },
    {
      title: 'ROI',
      value: '25.4%',
      description: 'Return on investment',
      icon: Target,
      color: 'text-purple-600'
    }
  ]

  const investments: Investment[] = [
    {
      id: '1',
      name: 'Tech Electronics Store',
      amount: 50000,
      currentValue: 62500,
      roi: 25.0,
      status: 'active',
      startDate: '2024-01-15',
      category: 'Electronics'
    },
    {
      id: '2',
      name: 'Fashion Retail Chain',
      amount: 35000,
      currentValue: 42000,
      roi: 20.0,
      status: 'active',
      startDate: '2024-02-01',
      category: 'Fashion'
    },
    {
      id: '3',
      name: 'Grocery Expansion',
      amount: 25000,
      currentValue: 31250,
      roi: 25.0,
      status: 'active',
      startDate: '2024-03-10',
      category: 'Grocery'
    },
    {
      id: '4',
      name: 'Sports Equipment',
      amount: 15000,
      currentValue: 21000,
      roi: 40.0,
      status: 'completed',
      startDate: '2023-10-15',
      category: 'Sports'
    }
  ]

  const recentDistributions: Distribution[] = [
    {
      id: '1',
      amount: 2500,
      date: '2024-01-01',
      source: 'Tech Electronics Store',
      type: 'profit_share'
    },
    {
      id: '2',
      amount: 1800,
      date: '2023-12-01',
      source: 'Fashion Retail Chain',
      type: 'dividend'
    },
    {
      id: '3',
      amount: 3200,
      date: '2023-11-15',
      source: 'Sports Equipment',
      type: 'return'
    }
  ]

  const performanceMetrics = [
    { label: 'Best Performing', value: 'Sports Equipment', change: '+40%' },
    { label: 'Highest Volume', value: 'Tech Electronics', change: '$62.5K' },
    { label: 'Most Recent', value: 'Grocery Expansion', change: 'Mar 2024' },
    { label: 'Total Active', value: '3 Investments', change: '$135.75K' }
  ]

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
          <Button variant="outline" asChild>
            <Link to="/analytics">
              <BarChart3 className="mr-2 h-4 w-4" />
              Analytics
            </Link>
          </Button>
          <Button asChild>
            <Link to="/investments/new">
              <Coins className="mr-2 h-4 w-4" />
              New Investment
            </Link>
          </Button>
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
            <div className="space-y-4">
              {investments.map((investment) => (
                <div key={investment.id} className="flex items-center justify-between p-4 border rounded-lg">
                  <div className="flex items-center space-x-4">
                    <div className={`w-3 h-3 rounded-full ${
                      investment.status === 'active' ? 'bg-green-500' :
                      investment.status === 'completed' ? 'bg-blue-500' :
                      'bg-yellow-500'
                    }`}></div>
                    <div>
                      <p className="font-medium">{investment.name}</p>
                      <p className="text-sm text-muted-foreground">
                        {investment.category} • Started {new Date(investment.startDate).toLocaleDateString()}
                      </p>
                    </div>
                  </div>
                  <div className="text-right space-y-1">
                    <div className="flex items-center space-x-2">
                      <span className="text-sm text-muted-foreground">
                        ${investment.amount.toLocaleString()}
                      </span>
                      <ArrowUpRight className="h-3 w-3" />
                      <span className="font-semibold">
                        ${investment.currentValue.toLocaleString()}
                      </span>
                    </div>
                    <div className="flex items-center justify-end space-x-1">
                      <TrendingUp className="h-3 w-3 text-green-600" />
                      <span className="text-sm font-medium text-green-600">
                        +{investment.roi}%
                      </span>
                    </div>
                  </div>
                </div>
              ))}
            </div>
            <Button variant="outline" className="w-full mt-4" asChild>
              <Link to="/investments">View All Investments</Link>
            </Button>
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
            <div className="space-y-4">
              {recentDistributions.map((distribution) => (
                <div key={distribution.id} className="flex items-start space-x-3">
                  <div className={`w-2 h-2 rounded-full mt-2 ${
                    distribution.type === 'profit_share' ? 'bg-green-500' :
                    distribution.type === 'dividend' ? 'bg-blue-500' :
                    'bg-purple-500'
                  }`}></div>
                  <div className="flex-1 space-y-1">
                    <p className="text-sm font-medium leading-none">
                      ${distribution.amount.toLocaleString()} {distribution.type.replace('_', ' ')}
                    </p>
                    <p className="text-xs text-muted-foreground">
                      {distribution.source}
                    </p>
                    <p className="text-xs text-muted-foreground">
                      {new Date(distribution.date).toLocaleDateString()}
                    </p>
                  </div>
                </div>
              ))}
            </div>
            <Button variant="outline" className="w-full mt-4" asChild>
              <Link to="/distributions">View All Distributions</Link>
            </Button>
          </CardContent>
        </Card>
      </div>

      {/* Investment Allocation Chart */}
      <Card>
        <CardHeader>
          <CardTitle className="flex items-center space-x-2">
            <PieChart className="h-5 w-5" />
            <span>Portfolio Allocation</span>
          </CardTitle>
          <CardDescription>
            Distribution of investments by category
          </CardDescription>
        </CardHeader>
        <CardContent>
          <div className="grid md:grid-cols-2 gap-6">
            <div className="space-y-4">
              {investments.reduce((acc, investment) => {
                const existingCategory = acc.find(item => item.category === investment.category)
                if (existingCategory) {
                  existingCategory.amount += investment.currentValue
                } else {
                  acc.push({
                    category: investment.category,
                    amount: investment.currentValue,
                    percentage: 0
                  })
                }
                return acc
              }, [] as Array<{ category: string; amount: number; percentage: number }>)
              .map(item => {
                const totalValue = investments.reduce((sum, inv) => sum + inv.currentValue, 0)
                item.percentage = (item.amount / totalValue) * 100
                return item
              })
              .map((allocation, index) => (
                <div key={index} className="flex items-center justify-between">
                  <div className="flex items-center space-x-3">
                    <div className={`w-4 h-4 rounded ${
                      index === 0 ? 'bg-blue-500' :
                      index === 1 ? 'bg-green-500' :
                      index === 2 ? 'bg-purple-500' :
                      'bg-orange-500'
                    }`}></div>
                    <span className="text-sm font-medium">{allocation.category}</span>
                  </div>
                  <div className="text-right">
                    <div className="text-sm font-semibold">
                      ${allocation.amount.toLocaleString()}
                    </div>
                    <div className="text-xs text-muted-foreground">
                      {allocation.percentage.toFixed(1)}%
                    </div>
                  </div>
                </div>
              ))}
            </div>
            <div className="flex items-center justify-center">
              <div className="text-muted-foreground text-center">
                <PieChart className="h-16 w-16 mx-auto mb-2" />
                <p>Portfolio allocation chart would be rendered here</p>
              </div>
            </div>
          </div>
        </CardContent>
      </Card>

      {/* Investment Goals */}
      <Card>
        <CardHeader>
          <CardTitle className="flex items-center space-x-2">
            <Target className="h-5 w-5" />
            <span>Investment Goals</span>
          </CardTitle>
          <CardDescription>
            Track progress towards your financial objectives
          </CardDescription>
        </CardHeader>
        <CardContent>
          <div className="space-y-4">
            <div className="p-4 bg-blue-50 border border-blue-200 rounded-lg">
              <div className="flex justify-between items-center mb-2">
                <h4 className="font-medium text-blue-900">Portfolio Growth Target</h4>
                <span className="text-blue-600 font-semibold">78%</span>
              </div>
              <div className="w-full bg-blue-200 rounded-full h-2">
                <div className="bg-blue-600 h-2 rounded-full" style={{ width: '78%' }}></div>
              </div>
              <p className="text-sm text-blue-800 mt-1">
                $156,750 of $200,000 target reached
              </p>
            </div>
            <div className="p-4 bg-green-50 border border-green-200 rounded-lg">
              <div className="flex justify-between items-center mb-2">
                <h4 className="font-medium text-green-900">Annual ROI Target</h4>
                <span className="text-green-600 font-semibold">127%</span>
              </div>
              <div className="w-full bg-green-200 rounded-full h-2">
                <div className="bg-green-600 h-2 rounded-full w-full"></div>
              </div>
              <p className="text-sm text-green-800 mt-1">
                25.4% ROI exceeds 20% target
              </p>
            </div>
          </div>
        </CardContent>
      </Card>
    </div>
  )
}