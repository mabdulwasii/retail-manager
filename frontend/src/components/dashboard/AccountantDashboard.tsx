import React, { useState, useEffect } from 'react'
import { useAuth } from '@/context/ManualAuthContext'
import { useRevenueAnalytics, useExpenseSummary, TimePeriod } from '@/hooks/useDashboard'
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card'
import { Button } from '@/components/ui/button'
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select'
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
import { useCurrency } from '@/hooks/useCurrency'
import { ShopSelector } from '@/components/ui/shop-selector'

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
  const { formatCurrency } = useCurrency()
  const [period, setPeriod] = useState<TimePeriod>('month')
  const [selectedShopId, setSelectedShopId] = useState<string | undefined>(undefined)

  // Set selectedShopId once user is loaded to prevent double API calls
  useEffect(() => {
    if (user?.shopId && !selectedShopId) {
      setSelectedShopId(user.shopId)
    }
  }, [user?.shopId, selectedShopId])
  
  const { data: revenueAnalytics, isLoading: analyticsLoading } = useRevenueAnalytics(selectedShopId, period)
  const { data: expenseSummary, isLoading: expensesLoading } = useExpenseSummary(selectedShopId, period)
  
  const statsLoading = analyticsLoading || expensesLoading

  // Calculate financial metrics from real API data
  const currentRevenue = revenueAnalytics?.currentRevenue || 0
  const previousRevenue = revenueAnalytics?.previousRevenue || 0
  const totalExpenses = expenseSummary?.totalAmount || 0
  const netProfit = currentRevenue - totalExpenses
  const profitMargin = currentRevenue > 0 ? (netProfit / currentRevenue) * 100 : 0
  
  // Calculate expense growth (mock for now - would need historical expense data)
  const expenseGrowth = 3.2 // TODO: Calculate from historical data when available
  const profitGrowth = revenueAnalytics?.growthRate || 0
  
  const financialStats = [
    {
      title: 'Total Revenue',
      value: formatCurrency(currentRevenue),
      description: `This ${period}`,
      icon: DollarSign,
      trend: `${revenueAnalytics?.growthRate || 0 > 0 ? '+' : ''}${(revenueAnalytics?.growthRate || 0).toFixed(1)}%`,
      trendUp: (revenueAnalytics?.growthRate || 0) >= 0,
      color: 'text-green-600'
    },
    {
      title: 'Total Expenses',
      value: formatCurrency(totalExpenses),
      description: `This ${period}`,
      icon: TrendingDown,
      trend: `+${expenseGrowth.toFixed(1)}%`,
      trendUp: false,
      color: 'text-red-600'
    },
    {
      title: 'Net Profit',
      value: formatCurrency(netProfit),
      description: `This ${period}`,
      icon: TrendingUp,
      trend: `${profitGrowth > 0 ? '+' : ''}${profitGrowth.toFixed(1)}%`,
      trendUp: profitGrowth >= 0,
      color: 'text-blue-600'
    },
    {
      title: 'Profit Margin',
      value: `${profitMargin.toFixed(1)}%`,
      description: 'Overall margin',
      icon: BarChart3,
      trend: `${profitGrowth > 0 ? '+' : ''}${(profitGrowth / 10).toFixed(1)}%`,
      trendUp: profitGrowth >= 0,
      color: 'text-purple-600'
    }
  ]

  // Recent transactions - TODO: Replace with real API when available
  // For now, generate from real revenue/expense data
  const recentTransactions: FinancialRecord[] = [
    {
      id: '1',
      type: 'income',
      category: 'Sales Revenue',
      amount: currentRevenue > 0 ? Math.round(currentRevenue * 0.055) : 15750,
      date: new Date().toISOString().split('T')[0],
      description: 'Recent sales revenue',
      status: 'approved'
    },
    {
      id: '2',
      type: 'expense',
      category: 'Inventory Purchase',
      amount: totalExpenses > 0 ? Math.round(totalExpenses * 0.054) : 8500,
      date: new Date().toISOString().split('T')[0],
      description: 'Inventory and supplies',
      status: expenseSummary?.pendingApproval && expenseSummary?.pendingApproval > 0 ? 'pending' : 'approved'
    },
    {
      id: '3',
      type: 'income',
      category: 'Transaction Revenue',
      amount: currentRevenue > 0 ? Math.round(currentRevenue * 0.011) : 3200,
      date: new Date(Date.now() - 86400000).toISOString().split('T')[0],
      description: 'Transaction fees and charges',
      status: 'approved'
    },
    {
      id: '4',
      type: 'expense',
      category: 'Operating Costs',
      amount: totalExpenses > 0 ? Math.round(totalExpenses * 0.015) : 2400,
      date: new Date(Date.now() - 86400000).toISOString().split('T')[0],
      description: 'Utilities and operational expenses',
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

  // Revenue breakdown from expense summary category data or calculate from totals
  const categoryBreakdown = expenseSummary?.categoryBreakdown && expenseSummary?.categoryBreakdown?.length > 0
    ? expenseSummary.categoryBreakdown.map((cat, idx) => ({
        category: cat.category,
        amount: cat.totalValue,
        percentage: currentRevenue > 0 ? (cat.totalValue / currentRevenue) * 100 : 0,
        color: idx === 0 ? 'bg-green-500' : idx === 1 ? 'bg-blue-500' : 'bg-purple-500'
      }))
    : [
        { 
          category: 'Sales Revenue', 
          amount: currentRevenue * 0.785, 
          percentage: 78.5, 
          color: 'bg-green-500' 
        },
        { 
          category: 'Transaction Fees', 
          amount: currentRevenue * 0.125, 
          percentage: 12.5, 
          color: 'bg-blue-500' 
        },
        { 
          category: 'Service Revenue', 
          amount: currentRevenue * 0.09, 
          percentage: 9.0, 
          color: 'bg-purple-500' 
        }
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
          <ShopSelector 
            value={selectedShopId || ''}
            onValueChange={setSelectedShopId}
            className="w-[200px]"
          />
          <Select value={period} onValueChange={(value) => setPeriod(value as TimePeriod)}>
            <SelectTrigger className="w-32">
              <SelectValue placeholder="Period" />
            </SelectTrigger>
            <SelectContent>
              <SelectItem value="today">Today</SelectItem>
              <SelectItem value="week">This Week</SelectItem>
              <SelectItem value="month">This Month</SelectItem>
              <SelectItem value="year">This Year</SelectItem>
            </SelectContent>
          </Select>
          <Button variant="outline" asChild>
            <Link to="/reports">
              <FileText className="mr-2 h-4 w-4" />
              Reports
            </Link>
          </Button>
          <Button asChild>
            <Link to="/expenses/create">
              <Calculator className="mr-2 h-4 w-4" />
              New Expense
            </Link>
          </Button>
        </div>
      </div>

      {/* Financial Stats */}
      <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-4">
        {financialStats.map((stat, index) => (
          <Card key={stat.title + `-${index}`} className="hover:shadow-md transition-shadow">
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

      <div className="grid gap-4 md:grid-cols-2">
        {/* Revenue Breakdown */}
        <Card>
          <CardHeader>
            <CardTitle>Revenue Breakdown</CardTitle>
            <CardDescription>
              Income distribution by category this {period}
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
                    <span>{category.percentage.toFixed(1)}% of total revenue</span>
                  </div>
                </div>
              ))}
            </div>
          </CardContent>
        </Card>

        {/* Expense Summary */}
        {expenseSummary && (
          <Card>
            <CardHeader>
              <CardTitle>Expense Summary</CardTitle>
              <CardDescription>
                Expense tracking and approvals
              </CardDescription>
            </CardHeader>
            <CardContent>
              <div className="space-y-4">
                <div className="grid grid-cols-2 gap-4">
                  <div className="p-3 border rounded-lg">
                    <p className="text-xs text-muted-foreground">Total Expenses</p>
                    <p className="text-2xl font-bold">{expenseSummary.totalExpenses}</p>
                  </div>
                  <div className="p-3 border rounded-lg">
                    <p className="text-xs text-muted-foreground">Total Amount</p>
                    <p className="text-2xl font-bold">{formatCurrency(expenseSummary.totalAmount || 0)}</p>
                  </div>
                </div>
                
                <div className="space-y-2">
                  <div className="flex items-center justify-between p-2 bg-orange-50 rounded">
                    <span className="text-sm">Pending Approval</span>
                    <span className="text-sm font-semibold text-orange-700">{expenseSummary.pendingApproval}</span>
                  </div>
                  <div className="flex items-center justify-between p-2 bg-green-50 rounded">
                    <span className="text-sm">Approved</span>
                    <span className="text-sm font-semibold text-green-700">{expenseSummary.approvedExpenses}</span>
                  </div>
                  <div className="flex items-center justify-between p-2 bg-blue-50 rounded">
                    <span className="text-sm">This {period === 'month' ? 'Month' : period}</span>
                    <span className="text-sm font-semibold text-blue-700">{formatCurrency(expenseSummary?.monthlyTotal ||0)}</span>
                  </div>
                </div>

                <Button variant="outline" className="w-full" asChild>
                  <Link to="/expenses">View All Expenses</Link>
                </Button>
              </div>
            </CardContent>
          </Card>
        )}
      </div>

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
            <div className={`text-center p-4 rounded-lg ${
              (revenueAnalytics?.growthRate || 0) >= 15 ? 'bg-green-50' :
              (revenueAnalytics?.growthRate || 0) >= 5 ? 'bg-blue-50' :
              'bg-yellow-50'
            }`}>
              <div className={`text-2xl font-bold ${
                (revenueAnalytics?.growthRate || 0) >= 15 ? 'text-green-600' :
                (revenueAnalytics?.growthRate || 0) >= 5 ? 'text-blue-600' :
                'text-yellow-600'
              }`}>
                {(revenueAnalytics?.growthRate || 0) >= 15 ? 'Excellent' :
                 (revenueAnalytics?.growthRate || 0) >= 5 ? 'Good' :
                 (revenueAnalytics?.growthRate || 0) >= 0 ? 'Fair' : 'Watch'}
              </div>
              <div className="text-sm text-muted-foreground">Cash Flow</div>
              <div className={`text-xs mt-1 ${
                (revenueAnalytics?.growthRate || 0) >= 15 ? 'text-green-600' :
                (revenueAnalytics?.growthRate || 0) >= 5 ? 'text-blue-600' :
                'text-yellow-600'
              }`}>
                {(revenueAnalytics?.growthRate || 0) > 0 ? '+' : ''}{(revenueAnalytics?.growthRate || 0).toFixed(1)}% this {period}
              </div>
            </div>
            <div className={`text-center p-4 rounded-lg ${
              profitMargin >= 40 ? 'bg-green-50' :
              profitMargin >= 25 ? 'bg-blue-50' :
              profitMargin >= 10 ? 'bg-yellow-50' :
              'bg-red-50'
            }`}>
              <div className={`text-2xl font-bold ${
                profitMargin >= 40 ? 'text-green-600' :
                profitMargin >= 25 ? 'text-blue-600' :
                profitMargin >= 10 ? 'text-yellow-600' :
                'text-red-600'
              }`}>
                {profitMargin >= 40 ? 'Excellent' :
                 profitMargin >= 25 ? 'Good' :
                 profitMargin >= 10 ? 'Fair' : 'Needs Attention'}
              </div>
              <div className="text-sm text-muted-foreground">Profitability</div>
              <div className={`text-xs mt-1 ${
                profitMargin >= 40 ? 'text-green-600' :
                profitMargin >= 25 ? 'text-blue-600' :
                profitMargin >= 10 ? 'text-yellow-600' :
                'text-red-600'
              }`}>
                {profitMargin.toFixed(1)}% margin
              </div>
            </div>
            <div className={`text-center p-4 rounded-lg ${
              expenseGrowth <= 5 ? 'bg-green-50' :
              expenseGrowth <= 10 ? 'bg-yellow-50' :
              'bg-red-50'
            }`}>
              <div className={`text-2xl font-bold ${
                expenseGrowth <= 5 ? 'text-green-600' :
                expenseGrowth <= 10 ? 'text-yellow-600' :
                'text-red-600'
              }`}>
                {expenseGrowth <= 5 ? 'Good' :
                 expenseGrowth <= 10 ? 'Watch' : 'High'}
              </div>
              <div className="text-sm text-muted-foreground">Expenses</div>
              <div className={`text-xs mt-1 ${
                expenseGrowth <= 5 ? 'text-green-600' :
                expenseGrowth <= 10 ? 'text-yellow-600' :
                'text-red-600'
              }`}>
                +{expenseGrowth.toFixed(1)}% increase
              </div>
            </div>
          </div>
        </CardContent>
      </Card>
    </div>
  )
}