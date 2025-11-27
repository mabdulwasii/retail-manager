import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { useAuth } from '@/context/ManualAuthContext'
import { expenseService } from '@/services/expenseService'
import { toast } from 'sonner'

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

// Query hook for fetching expenses
export const useExpenses = (shopId?: string, filter?: ExpenseFilter) => {
  const { isAuthenticated, user } = useAuth()
  const targetShopId = shopId || user?.shopId

  return useQuery({
    queryKey: ['expenses', targetShopId, filter],
    queryFn: () => expenseService.getExpenses(targetShopId!, filter),
    enabled: !!(isAuthenticated && targetShopId && user?.roles && 
      user.roles.some(r => ['MANAGER', 'OWNER', 'TENANT_ADMIN', 'ACCOUNTANT'].includes(r.name))),
    staleTime: 2 * 60 * 1000,
    retry: 1
  })
}

// Query hook for fetching expense by ID
export const useExpenseById = (expenseId?: string) => {
  const { isAuthenticated, user } = useAuth()

  return useQuery({
    queryKey: ['expenses', expenseId],
    queryFn: () => expenseService.getExpenseById(expenseId!),
    enabled: !!(isAuthenticated && expenseId && user?.roles && 
      user.roles.some(r => ['MANAGER', 'OWNER', 'TENANT_ADMIN', 'ACCOUNTANT'].includes(r.name))),
    staleTime: 3 * 60 * 1000,
    retry: 1
  })
}

// Mutation hook for creating expenses
export const useCreateExpense = () => {
  const queryClient = useQueryClient()

  return useMutation({
    mutationFn: ({ shopId, data }: { shopId: string; data: CreateExpenseRequest }) => 
      expenseService.createExpense(shopId, data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['expenses'] })
      toast.success('Expense created successfully')
    },
    onError: (error: any) => {
      toast.error('Failed to create expense', {
        description: error.response?.data?.message || error.message
      })
    }
  })
}

// Mutation hook for updating expenses
export const useUpdateExpense = () => {
  const queryClient = useQueryClient()

  return useMutation({
    mutationFn: ({ expenseId, updates }: { expenseId: string; updates: UpdateExpenseRequest }) => 
      expenseService.updateExpense(expenseId, updates),
    onSuccess: (updatedExpense) => {
      queryClient.invalidateQueries({ queryKey: ['expenses'] })
      queryClient.invalidateQueries({ queryKey: ['expenses', updatedExpense.id] })
      toast.success('Expense updated successfully')
    },
    onError: (error: any) => {
      toast.error('Failed to update expense', {
        description: error.response?.data?.message || error.message
      })
    }
  })
}

// Mutation hook for deleting expenses
export const useDeleteExpense = () => {
  const queryClient = useQueryClient()

  return useMutation({
    mutationFn: (expenseId: string) => expenseService.deleteExpense(expenseId),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['expenses'] })
      toast.success('Expense deleted successfully')
    },
    onError: (error: any) => {
      toast.error('Failed to delete expense', {
        description: error.response?.data?.message || error.message
      })
    }
  })
}

// Mutation hook for approving expenses
export const useApproveExpense = () => {
  const queryClient = useQueryClient()

  return useMutation({
    mutationFn: ({ expenseId, notes }: { expenseId: string; notes?: string }) => 
      expenseService.approveExpense(expenseId, notes),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['expenses'] })
      toast.success('Expense approved successfully')
    },
    onError: (error: any) => {
      toast.error('Failed to approve expense', {
        description: error.response?.data?.message || error.message
      })
    }
  })
}

// Mutation hook for rejecting expenses
export const useRejectExpense = () => {
  const queryClient = useQueryClient()

  return useMutation({
    mutationFn: ({ expenseId, notes }: { expenseId: string; notes?: string }) => 
      expenseService.rejectExpense(expenseId, notes),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['expenses'] })
      toast.success('Expense rejected')
    },
    onError: (error: any) => {
      toast.error('Failed to reject expense', {
        description: error.response?.data?.message || error.message
      })
    }
  })
}

// Mutation hook for uploading receipt
export const useUploadReceipt = () => {
  const queryClient = useQueryClient()

  return useMutation({
    mutationFn: ({ expenseId, file }: { expenseId: string; file: File }) => 
      expenseService.uploadReceipt(expenseId, file),
    onSuccess: (_, variables) => {
      queryClient.invalidateQueries({ queryKey: ['expenses', variables.expenseId] })
      toast.success('Receipt uploaded successfully')
    },
    onError: (error: any) => {
      toast.error('Failed to upload receipt', {
        description: error.response?.data?.message || error.message
      })
    }
  })
}
