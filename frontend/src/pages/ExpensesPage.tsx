import React, { useState, useEffect } from 'react'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import { Button } from '@/components/ui/button'
import { Badge } from '@/components/ui/badge'
import { LoadingSpinner } from '@/components/ui/loading-spinner'
import { ExpenseList } from '@/components/expenses/ExpenseList'
import { ExpenseForm } from '@/components/expenses/ExpenseForm'
import { ExpenseFilters } from '@/components/expenses/ExpenseFilters'
import { ExpenseSummaryCards } from '@/components/expenses/ExpenseSummaryCards'
import { useExpenses, ExpenseFilter } from '@/hooks/useExpenses'
import { useCurrency } from '@/hooks/useCurrency'
import {
  PlusIcon,
  ReceiptIcon,
  FilterIcon,
  DownloadIcon,
  RefreshCwIcon,
  CheckCircleIcon,
  ClockIcon,
  XCircleIcon
} from 'lucide-react'

export const ExpensesPage: React.FC = () => {
  const {
    expenses,
    summary,
    isLoading,
    error,
    canCreateExpense,
    canApproveExpense,
    canViewAllExpenses,
    fetchExpenses,
    fetchExpenseSummary,
    exportExpenses,
    clearError
  } = useExpenses()

  const { formatCurrency } = useCurrency()
  const [activeTab, setActiveTab] = useState<'overview' | 'list' | 'pending'>('overview')
  const [showExpenseForm, setShowExpenseForm] = useState(false)
  const [selectedExpense, setSelectedExpense] = useState<string | null>(null)
  const [filter, setFilter] = useState<ExpenseFilter>({})

  useEffect(() => {
    fetchExpenses()
    fetchExpenseSummary()
  }, [fetchExpenses, fetchExpenseSummary])

  useEffect(() => {
    if (error) {
      const timer = setTimeout(() => {
        clearError()
      }, 5000)
      return () => clearTimeout(timer)
    }
  }, [error, clearError])

  const handleFilterChange = (newFilter: ExpenseFilter) => {
    setFilter(newFilter)
    fetchExpenses(newFilter)
  }

  const handleExport = async (format: 'csv' | 'excel' = 'csv') => {
    const url = await exportExpenses(filter, format)
    if (url) {
      const link = document.createElement('a')
      link.href = url
      link.download = `expenses-${new Date().toISOString().split('T')[0]}.${format === 'csv' ? 'csv' : 'xlsx'}`
      document.body.appendChild(link)
      link.click()
      document.body.removeChild(link)
      window.URL.revokeObjectURL(url)
    }
  }

  const handleRefresh = () => {
    fetchExpenses(filter)
    fetchExpenseSummary()
  }

  const pendingExpenses = expenses.filter(expense => expense.status === 'PENDING_APPROVAL')
  const recentExpenses = expenses.slice(0, 5)

  return (
    <div className="container mx-auto p-6 space-y-6">
      {/* Header */}
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-3xl font-bold text-gray-900">Expenses & Procurement</h1>
          <p className="text-gray-600">Manage shop expenses, procurement, and expenditures</p>
        </div>
        <div className="flex space-x-2">
          {canCreateExpense && (
            <Button
              onClick={() => setShowExpenseForm(true)}
              className="flex items-center space-x-2"
            >
              <PlusIcon className="h-4 w-4" />
              <span>New Expense</span>
            </Button>
          )}
          <Button
            variant="outline"
            onClick={handleRefresh}
            disabled={isLoading}
          >
            <RefreshCwIcon className="h-4 w-4" />
          </Button>
          <Button
            variant="outline"
            onClick={() => handleExport('csv')}
            disabled={isLoading}
          >
            <DownloadIcon className="h-4 w-4 mr-2" />
            Export
          </Button>
        </div>
      </div>

      {/* Error Alert */}
      {error && (
        <div className="bg-red-50 border border-red-200 rounded-lg p-4">
          <div className="flex items-center">
            <div className="flex-shrink-0">
              <XCircleIcon className="h-5 w-5 text-red-400" />
            </div>
            <div className="ml-3">
              <p className="text-sm text-red-700">{error}</p>
            </div>
            <div className="ml-auto pl-3">
              <button
                onClick={clearError}
                className="text-red-400 hover:text-red-600"
              >
                <span className="sr-only">Dismiss</span>
                <XCircleIcon className="h-5 w-5" />
              </button>
            </div>
          </div>
        </div>
      )}

      {/* Tabs */}
      <div className="flex space-x-1 bg-gray-100 p-1 rounded-lg">
        <Button
          variant={activeTab === 'overview' ? 'default' : 'ghost'}
          onClick={() => setActiveTab('overview')}
          className="flex-1"
        >
          Overview
        </Button>
        <Button
          variant={activeTab === 'list' ? 'default' : 'ghost'}
          onClick={() => setActiveTab('list')}
          className="flex-1"
        >
          All Expenses
        </Button>
        {canApproveExpense && (
          <Button
            variant={activeTab === 'pending' ? 'default' : 'ghost'}
            onClick={() => setActiveTab('pending')}
            className="flex-1 relative"
          >
            Pending Approval
            {pendingExpenses.length > 0 && (
              <Badge className="absolute -top-2 -right-2 bg-red-500 text-white text-xs">
                {pendingExpenses.length}
              </Badge>
            )}
          </Button>
        )}
      </div>

      {/* Content based on active tab */}
      {activeTab === 'overview' && (
        <div className="space-y-6">
          {/* Summary Cards */}
          <ExpenseSummaryCards summary={summary} isLoading={isLoading} />

          {/* Quick Stats */}
          <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
            <Card>
              <CardContent className="p-4">
                <div className="flex items-center justify-between">
                  <div>
                    <p className="text-sm text-gray-600">Pending Approval</p>
                    <p className="text-2xl font-bold text-orange-600">{pendingExpenses.length}</p>
                  </div>
                  <ClockIcon className="h-8 w-8 text-orange-600" />
                </div>
              </CardContent>
            </Card>

            <Card>
              <CardContent className="p-4">
                <div className="flex items-center justify-between">
                  <div>
                    <p className="text-sm text-gray-600">Approved This Month</p>
                    <p className="text-2xl font-bold text-green-600">
                      {expenses.filter(e =>
                        e.status === 'APPROVED' &&
                        new Date(e.date).getMonth() === new Date().getMonth()
                      ).length}
                    </p>
                  </div>
                  <CheckCircleIcon className="h-8 w-8 text-green-600" />
                </div>
              </CardContent>
            </Card>

            <Card>
              <CardContent className="p-4">
                <div className="flex items-center justify-between">
                  <div>
                    <p className="text-sm text-gray-600">Total Expenses</p>
                    <p className="text-2xl font-bold">{expenses.length}</p>
                  </div>
                  <ReceiptIcon className="h-8 w-8 text-blue-600" />
                </div>
              </CardContent>
            </Card>
          </div>

          {/* Recent Expenses */}
          <Card>
            <CardHeader>
              <CardTitle>Recent Expenses</CardTitle>
            </CardHeader>
            <CardContent>
              {isLoading ? (
                <div className="flex items-center justify-center py-8">
                  <LoadingSpinner size="md" />
                </div>
              ) : recentExpenses.length === 0 ? (
                <div className="text-center py-8 text-gray-500">
                  <ReceiptIcon className="h-12 w-12 mx-auto mb-2 text-gray-300" />
                  <p>No expenses found</p>
                  {canCreateExpense && (
                    <Button
                      onClick={() => setShowExpenseForm(true)}
                      className="mt-2"
                    >
                      Create First Expense
                    </Button>
                  )}
                </div>
              ) : (
                <ExpenseList
                  expenses={recentExpenses}
                  onExpenseSelect={setSelectedExpense}
                  showActions={true}
                  compact={true}
                />
              )}
            </CardContent>
          </Card>
        </div>
      )}

      {activeTab === 'list' && (
        <div className="space-y-6">
          {/* Filters */}
          <ExpenseFilters
            onFilterChange={handleFilterChange}
            currentFilter={filter}
          />

          {/* Expense List */}
          <Card>
            <CardHeader className="flex flex-row items-center justify-between">
              <CardTitle>All Expenses</CardTitle>
              <div className="flex items-center space-x-2">
                <Badge variant="secondary">{expenses.length} expenses</Badge>
                <Button
                  variant="outline"
                  size="sm"
                  onClick={() => handleExport('excel')}
                  disabled={isLoading}
                >
                  <DownloadIcon className="h-4 w-4 mr-1" />
                  Excel
                </Button>
              </div>
            </CardHeader>
            <CardContent>
              {isLoading ? (
                <div className="flex items-center justify-center py-8">
                  <LoadingSpinner size="md" />
                </div>
              ) : (
                <ExpenseList
                  expenses={expenses}
                  onExpenseSelect={setSelectedExpense}
                  showActions={true}
                />
              )}
            </CardContent>
          </Card>
        </div>
      )}

      {activeTab === 'pending' && canApproveExpense && (
        <div className="space-y-6">
          <Card>
            <CardHeader>
              <CardTitle className="flex items-center space-x-2">
                <ClockIcon className="h-5 w-5 text-orange-600" />
                <span>Pending Approval</span>
                <Badge className="bg-orange-100 text-orange-800">
                  {pendingExpenses.length}
                </Badge>
              </CardTitle>
            </CardHeader>
            <CardContent>
              {isLoading ? (
                <div className="flex items-center justify-center py-8">
                  <LoadingSpinner size="md" />
                </div>
              ) : pendingExpenses.length === 0 ? (
                <div className="text-center py-8 text-gray-500">
                  <CheckCircleIcon className="h-12 w-12 mx-auto mb-2 text-gray-300" />
                  <p>No expenses pending approval</p>
                  <p className="text-sm">All expenses are up to date</p>
                </div>
              ) : (
                <ExpenseList
                  expenses={pendingExpenses}
                  onExpenseSelect={setSelectedExpense}
                  showActions={true}
                  showApprovalActions={true}
                />
              )}
            </CardContent>
          </Card>
        </div>
      )}

      {/* Expense Form Modal */}
      {showExpenseForm && (
        <ExpenseForm
          isOpen={showExpenseForm}
          onClose={() => {
            setShowExpenseForm(false)
            setSelectedExpense(null)
          }}
          expenseId={selectedExpense}
          onExpenseCreated={() => {
            handleRefresh()
            setShowExpenseForm(false)
            setSelectedExpense(null)
          }}
        />
      )}
    </div>
  )
}

export default ExpensesPage