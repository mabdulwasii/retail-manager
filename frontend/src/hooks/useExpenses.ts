import { useState, useCallback, useEffect } from 'react'
import { useAuth } from '@/context/ManualAuthContext'
import { useCurrency } from './useCurrency'
import { api } from '@/services/api'

export interface Expense {
  id: string
  title: string
  description?: string
  category: ExpenseCategory
  amount: number
  date: string
  shopId: string
  requestedBy: string
  requestedByName: string
  approvedBy?: string
  approvedByName?: string
  status: ExpenseStatus
  receiptUrl?: string
  tags: string[]
  notes?: string
  createdAt: string
  updatedAt: string
}

export interface ExpenseCategory {
  id: string
  name: string
  description?: string
  isActive: boolean
  requiresApproval: boolean
  approvalLimit?: number
}

export type ExpenseStatus = 'DRAFT' | 'PENDING_APPROVAL' | 'APPROVED' | 'REJECTED' | 'PAID'

export interface CreateExpenseRequest {
  title: string
  description?: string
  categoryId: string
  amount: number
  date: string
  receiptFile?: File
  tags?: string[]
  notes?: string
}

export interface UpdateExpenseRequest {
  title?: string
  description?: string
  categoryId?: string
  amount?: number
  date?: string
  receiptFile?: File
  tags?: string[]
  notes?: string
  status?: ExpenseStatus
}

export interface ExpenseFilter {
  startDate?: string
  endDate?: string
  categoryId?: string
  status?: ExpenseStatus
  requestedBy?: string
  minAmount?: number
  maxAmount?: number
  tags?: string[]
}

export interface ExpenseSummary {
  totalExpenses: number
  pendingApproval: number
  approvedExpenses: number
  totalAmount: number
  monthlyTotal: number
  categoryBreakdown: Array<{
    categoryId: string
    categoryName: string
    amount: number
    count: number
  }>
}

const defaultCategories: ExpenseCategory[] = [
  {
    id: 'shop_maintenance',
    name: 'Shop Maintenance & Repairs',
    description: 'Shop repairs, maintenance, and improvements',
    isActive: true,
    requiresApproval: true,
    approvalLimit: 50000
  },
  {
    id: 'transport',
    name: 'Transportation',
    description: 'Transport of goods, delivery costs, fuel',
    isActive: true,
    requiresApproval: false,
    approvalLimit: 10000
  },
  {
    id: 'meals',
    name: 'Meals & Refreshments',
    description: 'Staff meals, refreshments, catering',
    isActive: true,
    requiresApproval: false,
    approvalLimit: 5000
  },
  {
    id: 'utilities',
    name: 'Utilities',
    description: 'Electricity, water, internet, phone bills',
    isActive: true,
    requiresApproval: true,
    approvalLimit: 25000
  },
  {
    id: 'supplies',
    name: 'Office & Shop Supplies',
    description: 'Stationery, cleaning supplies, packaging materials',
    isActive: true,
    requiresApproval: false,
    approvalLimit: 15000
  },
  {
    id: 'marketing',
    name: 'Marketing & Advertising',
    description: 'Promotional materials, advertising, marketing campaigns',
    isActive: true,
    requiresApproval: true,
    approvalLimit: 30000
  },
  {
    id: 'professional_services',
    name: 'Professional Services',
    description: 'Legal, accounting, consulting services',
    isActive: true,
    requiresApproval: true,
    approvalLimit: 100000
  },
  {
    id: 'equipment',
    name: 'Equipment & Technology',
    description: 'POS systems, computers, tools, equipment',
    isActive: true,
    requiresApproval: true,
    approvalLimit: 200000
  },
  {
    id: 'insurance',
    name: 'Insurance',
    description: 'Shop insurance, liability coverage',
    isActive: true,
    requiresApproval: true,
    approvalLimit: 50000
  },
  {
    id: 'other',
    name: 'Other Expenses',
    description: 'Miscellaneous expenses not fitting other categories',
    isActive: true,
    requiresApproval: true,
    approvalLimit: 20000
  }
]

export const useExpenses = () => {
  const { user } = useAuth()
  const { formatCurrency } = useCurrency()
  const [expenses, setExpenses] = useState<Expense[]>([])
  const [categories, setCategories] = useState<ExpenseCategory[]>(defaultCategories)
  const [summary, setSummary] = useState<ExpenseSummary | null>(null)
  const [isLoading, setIsLoading] = useState(false)
  const [error, setError] = useState<string | null>(null)

  // Fetch expenses
  const fetchExpenses = useCallback(async (filter?: ExpenseFilter): Promise<Expense[]> => {
    try {
      setIsLoading(true)
      setError(null)

      const queryParams = new URLSearchParams()
      if (filter?.startDate) queryParams.append('startDate', filter.startDate)
      if (filter?.endDate) queryParams.append('endDate', filter.endDate)
      if (filter?.categoryId) queryParams.append('categoryId', filter.categoryId)
      if (filter?.status) queryParams.append('status', filter.status)
      if (filter?.requestedBy) queryParams.append('requestedBy', filter.requestedBy)
      if (filter?.minAmount) queryParams.append('minAmount', filter.minAmount.toString())
      if (filter?.maxAmount) queryParams.append('maxAmount', filter.maxAmount.toString())
      if (filter?.tags?.length) queryParams.append('tags', filter.tags.join(','))

      const data = await api.get<Expense[]>(`/expenses?${queryParams}`)
      setExpenses(data)
      return data
    } catch (err) {
      const errorMessage = err instanceof Error ? err.message : 'Unknown error occurred'
      setError(errorMessage)
      return []
    } finally {
      setIsLoading(false)
    }
  }, [])

  // Create expense
  const createExpense = useCallback(async (expenseData: CreateExpenseRequest): Promise<Expense | null> => {
    try {
      setIsLoading(true)
      setError(null)

      const formData = new FormData()
      formData.append('title', expenseData.title)
      if (expenseData.description) formData.append('description', expenseData.description)
      formData.append('categoryId', expenseData.categoryId)
      formData.append('amount', expenseData.amount.toString())
      formData.append('date', expenseData.date)
      if (expenseData.receiptFile) formData.append('receipt', expenseData.receiptFile)
      if (expenseData.tags?.length) formData.append('tags', JSON.stringify(expenseData.tags))
      if (expenseData.notes) formData.append('notes', expenseData.notes)

      const expense = await api.post<Expense>('/expenses', formData)
      setExpenses(prevExpenses => [expense, ...prevExpenses])
      return expense
    } catch (err) {
      const errorMessage = err instanceof Error ? err.message : 'Unknown error occurred'
      setError(errorMessage)
      return null
    } finally {
      setIsLoading(false)
    }
  }, [])

  // Update expense
  const updateExpense = useCallback(async (expenseId: string, updates: UpdateExpenseRequest): Promise<Expense | null> => {
    try {
      setIsLoading(true)
      setError(null)

      const formData = new FormData()
      if (updates.title) formData.append('title', updates.title)
      if (updates.description !== undefined) formData.append('description', updates.description)
      if (updates.categoryId) formData.append('categoryId', updates.categoryId)
      if (updates.amount) formData.append('amount', updates.amount.toString())
      if (updates.date) formData.append('date', updates.date)
      if (updates.receiptFile) formData.append('receipt', updates.receiptFile)
      if (updates.tags) formData.append('tags', JSON.stringify(updates.tags))
      if (updates.notes !== undefined) formData.append('notes', updates.notes)
      if (updates.status) formData.append('status', updates.status)

      const updatedExpense = await api.put<Expense>(`/expenses/${expenseId}`, formData)
      setExpenses(prevExpenses =>
        prevExpenses.map(expense =>
          expense.id === expenseId ? updatedExpense : expense
        )
      )
      return updatedExpense
    } catch (err) {
      const errorMessage = err instanceof Error ? err.message : 'Unknown error occurred'
      setError(errorMessage)
      return null
    } finally {
      setIsLoading(false)
    }
  }, [])

  // Delete expense
  const deleteExpense = useCallback(async (expenseId: string): Promise<boolean> => {
    try {
      setIsLoading(true)
      setError(null)

      await api.delete(`/expenses/${expenseId}`)

      setExpenses(prevExpenses =>
        prevExpenses.filter(expense => expense.id !== expenseId)
      )
      return true
    } catch (err) {
      const errorMessage = err instanceof Error ? err.message : 'Unknown error occurred'
      setError(errorMessage)
      return false
    } finally {
      setIsLoading(false)
    }
  }, [])

  // Approve/Reject expense
  const approveExpense = useCallback(async (expenseId: string, approved: boolean, notes?: string): Promise<boolean> => {
    try {
      setIsLoading(true)
      setError(null)

      const updatedExpense = await api.post<Expense>(`/expenses/${expenseId}/${approved ? 'approve' : 'reject'}`, { notes })
      setExpenses(prevExpenses =>
        prevExpenses.map(expense =>
          expense.id === expenseId ? updatedExpense : expense
        )
      )
      return true
    } catch (err) {
      const errorMessage = err instanceof Error ? err.message : 'Unknown error occurred'
      setError(errorMessage)
      return false
    } finally {
      setIsLoading(false)
    }
  }, [])

  // Get expense summary
  const fetchExpenseSummary = useCallback(async (period?: { startDate: string; endDate: string }): Promise<ExpenseSummary | null> => {
    try {
      setIsLoading(true)
      setError(null)

      const queryParams = new URLSearchParams()
      if (period?.startDate) queryParams.append('startDate', period.startDate)
      if (period?.endDate) queryParams.append('endDate', period.endDate)

      const summaryData = await api.get<ExpenseSummary>(`/expenses/summary?${queryParams}`)
      setSummary(summaryData)
      return summaryData
    } catch (err) {
      const errorMessage = err instanceof Error ? err.message : 'Unknown error occurred'
      setError(errorMessage)
      return null
    } finally {
      setIsLoading(false)
    }
  }, [])

  // Fetch categories
  const fetchCategories = useCallback(async (): Promise<ExpenseCategory[]> => {
    try {
      const data = await api.get<ExpenseCategory[]>('/expenses/categories')
      setCategories(data)
      return data
    } catch (err) {
      // Use default categories if API fails
      setCategories(defaultCategories)
      return defaultCategories
    }
  }, [])

  // Upload receipt
  const uploadReceipt = useCallback(async (expenseId: string, file: File): Promise<string | null> => {
    try {
      setIsLoading(true)
      setError(null)

      const formData = new FormData()
      formData.append('receipt', file)

      const result = await api.post<{ receiptUrl: string }>(`/expenses/${expenseId}/receipt`, formData)
      return result.receiptUrl
    } catch (err) {
      const errorMessage = err instanceof Error ? err.message : 'Unknown error occurred'
      setError(errorMessage)
      return null
    } finally {
      setIsLoading(false)
    }
  }, [])

  // Export expenses
  const exportExpenses = useCallback(async (filter?: ExpenseFilter, format: 'csv' | 'excel' = 'csv'): Promise<string | null> => {
    try {
      setIsLoading(true)
      setError(null)

      const queryParams = new URLSearchParams()
      queryParams.append('format', format)
      if (filter?.startDate) queryParams.append('startDate', filter.startDate)
      if (filter?.endDate) queryParams.append('endDate', filter.endDate)
      if (filter?.categoryId) queryParams.append('categoryId', filter.categoryId)
      if (filter?.status) queryParams.append('status', filter.status)

      const response = await fetch(`/api/expenses/export?${queryParams}`, {
        headers: {
          'Authorization': `Bearer ${localStorage.getItem('token')}`
        }
      })

      if (!response.ok) {
        throw new Error('Failed to export expenses')
      }

      const blob = await response.blob()
      const url = window.URL.createObjectURL(blob)
      return url
    } catch (err) {
      const errorMessage = err instanceof Error ? err.message : 'Unknown error occurred'
      setError(errorMessage)
      return null
    } finally {
      setIsLoading(false)
    }
  }, [])

  // Error handling
  const clearError = useCallback(() => {
    setError(null)
  }, [])

  // Permission checks
  const canCreateExpense = user?.roles.some((role: string) =>
    ['ROLE_SHOP_OWNER', 'ROLE_MANAGER', 'ROLE_ACCOUNTANT'].includes(role)
  ) || false

  const canApproveExpense = user?.roles.some((role: string) =>
    ['ROLE_SHOP_OWNER', 'ROLE_MANAGER'].includes(role)
  ) || false

  const canViewAllExpenses = user?.roles.some((role: string) =>
    ['ROLE_SHOP_OWNER', 'ROLE_MANAGER', 'ROLE_ACCOUNTANT', 'ROLE_AUDITOR'].includes(role)
  ) || false

  return {
    // State
    expenses,
    categories,
    summary,
    isLoading,
    error,
    user,

    // Permissions
    canCreateExpense,
    canApproveExpense,
    canViewAllExpenses,

    // Operations
    fetchExpenses,
    createExpense,
    updateExpense,
    deleteExpense,
    approveExpense,
    fetchExpenseSummary,
    fetchCategories,
    uploadReceipt,
    exportExpenses,

    // Utility
    clearError
  }
}

export default useExpenses