import React from 'react'
import { Button } from '@/components/ui/button'
import { Badge } from '@/components/ui/badge'
import { Expense } from '@/hooks/useExpenses'
import { useCurrency } from '@/hooks/useCurrency'
import { useExpenses } from '@/hooks/useExpenses'
import {
  EyeIcon,
  EditIcon,
  CheckIcon,
  XIcon,
  ReceiptIcon,
  CalendarIcon,
  UserIcon,
  TagIcon,
  AlertCircleIcon
} from 'lucide-react'

interface ExpenseListProps {
  expenses: Expense[]
  onExpenseSelect: (expenseId: string) => void
  showActions?: boolean
  showApprovalActions?: boolean
  compact?: boolean
}

export const ExpenseList: React.FC<ExpenseListProps> = ({
  expenses,
  onExpenseSelect,
  showActions = false,
  showApprovalActions = false,
  compact = false
}) => {
  const { formatCurrency } = useCurrency()
  const { canApproveExpense, approveExpense } = useExpenses()

  const getStatusBadge = (status: string) => {
    const variants: Record<string, { class: string; text: string }> = {
      DRAFT: { class: 'bg-gray-100 text-gray-800', text: 'Draft' },
      PENDING_APPROVAL: { class: 'bg-yellow-100 text-yellow-800', text: 'Pending' },
      APPROVED: { class: 'bg-green-100 text-green-800', text: 'Approved' },
      REJECTED: { class: 'bg-red-100 text-red-800', text: 'Rejected' },
      PAID: { class: 'bg-blue-100 text-blue-800', text: 'Paid' }
    }

    const variant = variants[status] || variants.DRAFT
    return (
      <Badge className={variant.class}>
        {variant.text}
      </Badge>
    )
  }

  const formatDate = (dateString: string) => {
    return new Date(dateString).toLocaleDateString('en-US', {
      year: 'numeric',
      month: 'short',
      day: 'numeric'
    })
  }

  const handleApprove = async (expenseId: string) => {
    await approveExpense(expenseId, true)
  }

  const handleReject = async (expenseId: string) => {
    await approveExpense(expenseId, false)
  }

  if (expenses.length === 0) {
    return (
      <div className="text-center py-8 text-gray-500">
        <ReceiptIcon className="h-12 w-12 mx-auto mb-2 text-gray-300" />
        <p>No expenses found</p>
        <p className="text-sm">Expenses will appear here when created</p>
      </div>
    )
  }

  return (
    <div className="space-y-3">
      {expenses.map((expense) => (
        <div
          key={expense.id}
          className={`border rounded-lg p-4 hover:bg-gray-50 transition-colors ${
            expense.status === 'PENDING_APPROVAL' ? 'border-yellow-300 bg-yellow-50' : ''
          }`}
        >
          <div className="flex items-start justify-between">
            <div className="flex-1 min-w-0">
              {/* Header */}
              <div className="flex items-center space-x-3 mb-2">
                <h3 className="font-medium text-gray-900 truncate">
                  {expense.title}
                </h3>
                {getStatusBadge(expense.status)}
                {expense.receiptUrl && (
                  <Badge variant="outline" className="text-green-600 border-green-600">
                    <ReceiptIcon className="h-3 w-3 mr-1" />
                    Receipt
                  </Badge>
                )}
              </div>

              {/* Description */}
              {expense.description && !compact && (
                <p className="text-sm text-gray-600 mb-2 line-clamp-2">
                  {expense.description}
                </p>
              )}

              {/* Details Grid */}
              <div className={`grid ${compact ? 'grid-cols-2 md:grid-cols-4' : 'grid-cols-2 md:grid-cols-3 lg:grid-cols-5'} gap-3 text-sm`}>
                <div className="flex items-center space-x-1">
                  <CalendarIcon className="h-4 w-4 text-gray-400" />
                  <span className="text-gray-600">Date:</span>
                  <span className="font-medium">{formatDate(expense.date)}</span>
                </div>

                <div className="flex items-center space-x-1">
                  <span className="text-gray-600">Amount:</span>
                  <span className="font-semibold text-green-600">
                    {formatCurrency(expense.amount)}
                  </span>
                </div>

                <div className="flex items-center space-x-1">
                  <span className="text-gray-600">Category:</span>
                  <span className="font-medium truncate">
                    {expense.category.name}
                  </span>
                </div>

                <div className="flex items-center space-x-1">
                  <UserIcon className="h-4 w-4 text-gray-400" />
                  <span className="text-gray-600">By:</span>
                  <span className="font-medium truncate">
                    {expense.requestedByName}
                  </span>
                </div>

                {!compact && expense.approvedByName && (
                  <div className="flex items-center space-x-1">
                    <CheckIcon className="h-4 w-4 text-green-500" />
                    <span className="text-gray-600">Approved by:</span>
                    <span className="font-medium truncate">
                      {expense.approvedByName}
                    </span>
                  </div>
                )}
              </div>

              {/* Tags */}
              {expense.tags.length > 0 && !compact && (
                <div className="flex items-center space-x-2 mt-2">
                  <TagIcon className="h-4 w-4 text-gray-400" />
                  <div className="flex flex-wrap gap-1">
                    {expense.tags.slice(0, 3).map((tag, index) => (
                      <Badge key={index} variant="outline" className="text-xs">
                        {tag}
                      </Badge>
                    ))}
                    {expense.tags.length > 3 && (
                      <Badge variant="outline" className="text-xs">
                        +{expense.tags.length - 3} more
                      </Badge>
                    )}
                  </div>
                </div>
              )}

              {/* Notes */}
              {expense.notes && !compact && (
                <div className="mt-2 text-sm text-gray-600">
                  <span className="font-medium">Notes:</span> {expense.notes}
                </div>
              )}

              {/* Approval required indicator */}
              {expense.category.requiresApproval && expense.status === 'DRAFT' && (
                <div className="flex items-center space-x-1 mt-2 text-xs text-orange-600">
                  <AlertCircleIcon className="h-3 w-3" />
                  <span>Requires approval (limit: {formatCurrency(expense.category.approvalLimit || 0)})</span>
                </div>
              )}
            </div>

            {/* Actions */}
            {(showActions || showApprovalActions) && (
              <div className="flex space-x-2 ml-4">
                {showActions && (
                  <>
                    <Button
                      variant="outline"
                      size="sm"
                      onClick={() => onExpenseSelect(expense.id)}
                    >
                      <EyeIcon className="h-4 w-4" />
                    </Button>

                    {(expense.status === 'DRAFT' || expense.status === 'REJECTED') && (
                      <Button
                        variant="outline"
                        size="sm"
                        onClick={() => onExpenseSelect(expense.id)}
                      >
                        <EditIcon className="h-4 w-4" />
                      </Button>
                    )}
                  </>
                )}

                {showApprovalActions && canApproveExpense && expense.status === 'PENDING_APPROVAL' && (
                  <>
                    <Button
                      variant="outline"
                      size="sm"
                      onClick={() => handleApprove(expense.id)}
                      className="text-green-600 hover:text-green-700 hover:bg-green-50"
                    >
                      <CheckIcon className="h-4 w-4" />
                    </Button>
                    <Button
                      variant="outline"
                      size="sm"
                      onClick={() => handleReject(expense.id)}
                      className="text-red-600 hover:text-red-700 hover:bg-red-50"
                    >
                      <XIcon className="h-4 w-4" />
                    </Button>
                  </>
                )}
              </div>
            )}
          </div>
        </div>
      ))}
    </div>
  )
}

export default ExpenseList