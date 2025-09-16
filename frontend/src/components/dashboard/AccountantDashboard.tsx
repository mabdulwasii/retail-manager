import React from 'react'
import { useAuth } from '@/context/AuthContext'
import { useRevenueAnalytics } from '@/hooks/useDashboard'
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card'
import { Button } from '@/components/ui/button'
import { LoadingSpinner } from '@/components/ui/loading-spinner'
import {
  DollarSign,
  TrendingUp,
  TrendingDown,
  BarChart3,
  Calculator,
  FileText,
  PieChart,
  AlertCircle,
  CheckCircle
} from 'lucide-react'
import { Link } from 'react-router-dom'

interface FinancialRecord {
  id: string
  type: 'income' | 'expense'
  category: string
  amount: number
  date: string
  description: string
  status: 'pending' | 'approved' | 'rejected'
}

export const AccountantDashboard: React.FC = () => {
  const { user } = useAuth()
  const { data: revenueAnalytics, isLoading: analyticsLoading } = useRevenueAnalytics()
  const statsLoading = analyticsLoading

  const financialStats = [
    {
      title: 'Total Revenue',
      value: '$284,750',
      description: 'This month',
      icon: DollarSign,
      trend: '+12.5%',
      trendUp: true,
      color: 'text-green-600'
    },
    {
      title: 'Total Expenses',
      value: '$156,230',
      description: 'This month',
      icon: TrendingDown,
      trend: '+3.2%',
      trendUp: false,
      color: 'text-red-600'
    },
    {
      title: 'Net Profit',
      value: '$128,520',
      description: 'This month',
      icon: TrendingUp,
      trend: '+18.7%',
      trendUp: true,
      color: 'text-blue-600'
    },
    {
      title: 'Profit Margin',
      value: '45.1%',
      description: 'Overall margin',
      icon: BarChart3,
      trend: '+2.3%',
      trendUp: true,
      color: 'text-purple-600'
    }
  ]

  const recentTransactions: FinancialRecord[] = [
    {
      id: '1',
      type: 'income',
      category: 'Sales Revenue',
      amount: 15750,
      date: '2024-01-15',
      description: 'Downtown Electronics - Daily sales',
      status: 'approved'
    },
    {
      id: '2',
      type: 'expense',
      category: 'Inventory Purchase',
      amount: 8500,
      date: '2024-01-15',
      description: 'Wholesale electronics order',
      status: 'pending'
    },
    {
      id: '3',
      type: 'income',
      category: 'Investment Returns',
      amount: 3200,
      date: '2024-01-14',
      description: 'Q4 profit distribution',
      status: 'approved'
    },
    {
      id: '4',
      type: 'expense',
      category: 'Operating Costs',
      amount: 2400,
      date: '2024-01-14',
      description: 'Utilities and rent',
      status: 'approved'
    }
  ]

  const pendingTasks = [
    {
      id: '1',
      title: 'Monthly Financial Report',
      description: 'Prepare January 2024 financial summary',
      dueDate: '2024-01-31',
      priority: 'high'
    },
    {
      id: '2',
      title: 'Tax Filing Preparation',
      description: 'Gather Q4 documentation for tax filing',
      dueDate: '2024-02-15',
      priority: 'medium'
    },
    {
      id: '3',
      title: 'Vendor Payment Review',
      description: 'Approve pending vendor payments',
      dueDate: '2024-01-20',
      priority: 'high'
    },
    {
      id: '4',
      title: 'Budget Planning Q2',
      description: 'Draft Q2 budget allocation plan',
      dueDate: '2024-02-28',
      priority: 'low'
    }
  ]

  const quickActions = [
    {
      title: 'Record Transaction',
      description: 'Add new income/expense',
      icon: Calculator,
      href: '/accounting/transaction',
      color: 'bg-blue-500 hover:bg-blue-600'
    },
    {
      title: 'Generate Report',
      description: 'Financial statements',
      icon: FileText,
      href: '/reports/financial',
      color: 'bg-green-500 hover:bg-green-600'
    },
    {
      title: 'Review Analytics',
      description: 'Business insights',
      icon: BarChart3,
      href: '/analytics',
      color: 'bg-purple-500 hover:bg-purple-600'
    },
    {
      title: 'Budget Planning',
      description: 'Manage budgets',
      icon: PieChart,
      href: '/budgets',
      color: 'bg-orange-500 hover:bg-orange-600'
    }
  ]

  const categoryBreakdown = [
    { category: 'Sales Revenue', amount: 284750, percentage: 78.5, color: 'bg-green-500' },
    { category: 'Investment Income', amount: 45200, percentage: 12.5, color: 'bg-blue-500' },
    { category: 'Service Revenue', amount: 32800, percentage: 9.0, color: 'bg-purple-500' }
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
            Financial Dashboard
          </h1>
          <p className="text-muted-foreground">
            Welcome back, {user?.firstName || user?.username}. Here's your financial overview.
          </p>
        </div>
        <div className="flex space-x-2">
          <Button variant="outline" asChild>
            <Link to="/reports">
              <FileText className="mr-2 h-4 w-4" />
              Reports
            </Link>
          </Button>
          <Button asChild>
            <Link to="/accounting/transaction">
              <Calculator className="mr-2 h-4 w-4" />
              New Transaction
            </Link>
          </Button>
        </div>
      </div>

      {/* Financial Stats */}
      <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-4">
        {financialStats.map((stat, index) => (
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
              <div className={`text-xs mt-1 flex items-center ${
                stat.trendUp ? 'text-green-600' : 'text-red-600'
              }`}>
                {stat.trendUp ? (
                  <TrendingUp className="h-3 w-3 mr-1" />
                ) : (
                  <TrendingDown className="h-3 w-3 mr-1" />
                )}
                {stat.trend}
              </div>
            </CardContent>
          </Card>
        ))}
      </div>

      {/* Quick Actions */}
      <Card>
        <CardHeader>
          <CardTitle>Quick Actions</CardTitle>
          <CardDescription>Common accounting tasks</CardDescription>
        </CardHeader>
        <CardContent>
          <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
            {quickActions.map((action, index) => (
              <Button
                key={index}
                variant="outline"
                className="h-24 flex-col space-y-2 p-4"
                asChild
              >
                <Link to={action.href}>
                  <action.icon className="h-8 w-8" />
                  <div className="text-center">
                    <div className="font-medium text-sm">{action.title}</div>
                    <div className="text-xs text-muted-foreground">
                      {action.description}
                    </div>
                  </div>
                </Link>
              </Button>
            ))}
          </div>
        </CardContent>
      </Card>

      <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-3">
        {/* Recent Transactions */}
        <Card className="col-span-2">
          <CardHeader>
            <CardTitle>Recent Transactions</CardTitle>
            <CardDescription>
              Latest financial activities requiring attention
            </CardDescription>
          </CardHeader>
          <CardContent>
            <div className="space-y-4">
              {recentTransactions.map((transaction) => (
                <div key={transaction.id} className="flex items-center justify-between p-4 border rounded-lg">
                  <div className="flex items-center space-x-4">
                    <div className={`w-3 h-3 rounded-full ${
                      transaction.type === 'income' ? 'bg-green-500' : 'bg-red-500'
                    }`}></div>
                    <div>
                      <p className="font-medium">{transaction.description}</p>
                      <p className="text-sm text-muted-foreground">
                        {transaction.category} • {new Date(transaction.date).toLocaleDateString()}
                      </p>
                    </div>
                  </div>
                  <div className="text-right space-y-1">
                    <p className={`font-semibold ${
                      transaction.type === 'income' ? 'text-green-600' : 'text-red-600'
                    }`}>
                      {transaction.type === 'income' ? '+' : '-'}${transaction.amount.toLocaleString()}
                    </p>
                    <div className="flex items-center space-x-1">
                      {transaction.status === 'approved' ? (
                        <CheckCircle className="h-3 w-3 text-green-600" />
                      ) : transaction.status === 'pending' ? (
                        <AlertCircle className="h-3 w-3 text-yellow-600" />
                      ) : (
                        <AlertCircle className="h-3 w-3 text-red-600" />
                      )}
                      <span className={`text-xs ${
                        transaction.status === 'approved' ? 'text-green-600' :
                        transaction.status === 'pending' ? 'text-yellow-600' :
                        'text-red-600'
                      }`}>
                        {transaction.status}
                      </span>
                    </div>
                  </div>
                </div>
              ))}
            </div>
            <Button variant="outline" className="w-full mt-4" asChild>
              <Link to="/accounting/transactions">View All Transactions</Link>
            </Button>
          </CardContent>
        </Card>

        {/* Pending Tasks */}
        <Card>
          <CardHeader>
            <CardTitle>Pending Tasks</CardTitle>
            <CardDescription>
              Items requiring your attention
            </CardDescription>
          </CardHeader>
          <CardContent>
            <div className="space-y-4">
              {pendingTasks.map((task) => (
                <div key={task.id} className="p-3 border rounded-lg">
                  <div className="flex items-center justify-between mb-2">
                    <h4 className="font-medium text-sm">{task.title}</h4>
                    <span className={`px-2 py-1 text-xs rounded-full ${
                      task.priority === 'high' ? 'bg-red-100 text-red-800' :
                      task.priority === 'medium' ? 'bg-yellow-100 text-yellow-800' :
                      'bg-gray-100 text-gray-800'
                    }`}>
                      {task.priority}
                    </span>
                  </div>
                  <p className="text-xs text-muted-foreground mb-2">
                    {task.description}
                  </p>
                  <div className="flex items-center justify-between">
                    <span className="text-xs text-muted-foreground">
                      Due: {new Date(task.dueDate).toLocaleDateString()}
                    </span>
                    <Button size="sm" variant="outline">
                      Start
                    </Button>
                  </div>
                </div>
              ))}
            </div>
          </CardContent>
        </Card>
      </div>

      {/* Revenue Breakdown */}
      <Card>
        <CardHeader>
          <CardTitle>Revenue Breakdown</CardTitle>
          <CardDescription>
            Income distribution by category this month
          </CardDescription>
        </CardHeader>
        <CardContent>
          <div className="space-y-4">
            {categoryBreakdown.map((category, index) => (
              <div key={index} className="space-y-2">
                <div className="flex justify-between items-center">
                  <span className="text-sm font-medium">{category.category}</span>
                  <span className="text-sm font-semibold">${category.amount.toLocaleString()}</span>
                </div>
                <div className="w-full bg-gray-200 rounded-full h-2">
                  <div
                    className={`h-2 rounded-full ${category.color}`}
                    style={{ width: `${category.percentage}%` }}
                  ></div>
                </div>
                <div className="flex justify-between items-center text-xs text-muted-foreground">
                  <span>{category.percentage}% of total revenue</span>
                  <span>+5.2% vs last month</span>
                </div>
              </div>
            ))}
          </div>
        </CardContent>
      </Card>

      {/* Financial Health Indicators */}
      <Card>
        <CardHeader>
          <CardTitle className="flex items-center space-x-2">
            <TrendingUp className="h-5 w-5" />
            <span>Financial Health</span>
          </CardTitle>
          <CardDescription>
            Key performance indicators for business health
          </CardDescription>
        </CardHeader>
        <CardContent>
          <div className="grid md:grid-cols-3 gap-6">
            <div className="text-center p-4 bg-green-50 rounded-lg">
              <div className="text-2xl font-bold text-green-600">Excellent</div>
              <div className="text-sm text-muted-foreground">Cash Flow</div>
              <div className="text-xs text-green-600 mt-1">+15% this month</div>
            </div>
            <div className="text-center p-4 bg-blue-50 rounded-lg">
              <div className="text-2xl font-bold text-blue-600">Good</div>
              <div className="text-sm text-muted-foreground">Profitability</div>
              <div className="text-xs text-blue-600 mt-1">45.1% margin</div>
            </div>
            <div className="text-center p-4 bg-yellow-50 rounded-lg">
              <div className="text-2xl font-bold text-yellow-600">Watch</div>
              <div className="text-sm text-muted-foreground">Expenses</div>
              <div className="text-xs text-yellow-600 mt-1">+3.2% increase</div>
            </div>
          </div>
        </CardContent>
      </Card>
    </div>
  )
}