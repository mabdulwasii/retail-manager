import React, { useEffect, useState } from 'react'
import { useForm, Controller } from 'react-hook-form'
import { yupResolver } from '@hookform/resolvers/yup'
import * as yup from 'yup'
import { Dialog, DialogContent, DialogHeader, DialogTitle, DialogFooter } from '@/components/ui/dialog'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import { Label } from '@/components/ui/label'
import { Textarea } from '@/components/ui/textarea'
import { NumericInput } from '@/components/ui/numeric-input'
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select'
import { useCreateExpense, useUpdateExpense, Expense, CreateExpenseRequest } from '@/hooks/useExpenses'
import { useCurrency } from '@/hooks/useCurrency'
import { LoadingSpinner } from '@/components/ui/loading-spinner'
import { UploadIcon, XIcon } from 'lucide-react'

const expenseSchema = yup.object({
  title: yup
    .string()
    .required('Title is required')
    .min(3, 'Title must be at least 3 characters')
    .max(200, 'Title must be at most 200 characters'),
  description: yup
    .string()
    .optional()
    .max(1000, 'Description must be at most 1000 characters'),
  categoryId: yup.string().required('Category is required'),
  amount: yup
    .number()
    .required('Amount is required')
    .min(0.01, 'Amount must be greater than 0')
    .typeError('Amount must be a valid number'),
  date: yup.string().required('Date is required'),
  tags: yup.array().of(yup.string()).optional(),
  notes: yup.string().optional().max(500, 'Notes must be at most 500 characters'),
})

type ExpenseFormData = yup.InferType<typeof expenseSchema>

interface ExpenseFormProps {
  expense?: Expense
  shopId: string
  open: boolean
  onClose: () => void
  onSuccess?: () => void
}

// Mock expense categories - in production, these would come from an API
const EXPENSE_CATEGORIES = [
  { id: 'cat1', name: 'Office Supplies' },
  { id: 'cat2', name: 'Utilities' },
  { id: 'cat3', name: 'Marketing' },
  { id: 'cat4', name: 'Inventory Purchase' },
  { id: 'cat5', name: 'Equipment' },
  { id: 'cat6', name: 'Maintenance' },
  { id: 'cat7', name: 'Salaries' },
  { id: 'cat8', name: 'Transportation' },
  { id: 'cat9', name: 'Other' },
]

export const ExpenseForm: React.FC<ExpenseFormProps> = ({
  expense,
  shopId,
  open,
  onClose,
  onSuccess,
}) => {
  const isEditMode = !!expense
  const { mutateAsync: createExpense, isPending: isCreating } = useCreateExpense()
  const { mutateAsync: updateExpense, isPending: isUpdating } = useUpdateExpense()
  const { formatCurrency } = useCurrency()
  const [receiptFile, setReceiptFile] = useState<File | null>(null)

  const {
    control,
    handleSubmit,
    reset,
    formState: { errors, isDirty },
  } = useForm<ExpenseFormData>({
    resolver: yupResolver(expenseSchema),
    defaultValues: {
      title: '',
      description: '',
      categoryId: '',
      amount: 0,
      date: new Date().toISOString().split('T')[0],
      tags: [],
      notes: '',
    },
  })

  useEffect(() => {
    if (expense) {
      reset({
        title: expense.title,
        description: expense.description || '',
        categoryId: expense.category.id,
        amount: expense.amount,
        date: expense.date.split('T')[0],
        tags: expense.tags || [],
        notes: expense.notes || '',
      })
    } else {
      reset({
        title: '',
        description: '',
        categoryId: '',
        amount: 0,
        date: new Date().toISOString().split('T')[0],
        tags: [],
        notes: '',
      })
    }
  }, [expense, reset])

  const onSubmit = async (data: ExpenseFormData) => {
    try {
      const payload: CreateExpenseRequest = {
        title: data.title,
        description: data.description,
        categoryId: data.categoryId,
        amount: data.amount,
        date: data.date,
        tags: data.tags,
        notes: data.notes,
        receiptFile: receiptFile || undefined,
      }

      if (isEditMode && expense) {
        await updateExpense({
          expenseId: expense.id,
          updates: payload,
        })
      } else {
        await createExpense({
          shopId,
          data: payload,
        })
      }

      onSuccess?.()
      onClose()
    } catch (error) {
      console.error('Failed to save expense:', error)
    }
  }

  const handleFileChange = (event: React.ChangeEvent<HTMLInputElement>) => {
    const file = event.target.files?.[0]
    if (file) {
      // Validate file size (max 5MB)
      if (file.size > 5 * 1024 * 1024) {
        alert('File size must be less than 5MB')
        return
      }
      // Validate file type
      if (!file.type.match(/^image\/(jpeg|jpg|png|gif)|application\/pdf$/)) {
        alert('Only images (JPEG, PNG, GIF) and PDF files are allowed')
        return
      }
      setReceiptFile(file)
    }
  }

  const handleRemoveFile = () => {
    setReceiptFile(null)
  }

  const isSubmitting = isCreating || isUpdating

  return (
    <Dialog open={open} onOpenChange={onClose}>
      <DialogContent className="max-w-2xl max-h-[90vh] overflow-y-auto">
        <DialogHeader>
          <DialogTitle>
            {isEditMode ? 'Edit Expense' : 'Create New Expense'}
          </DialogTitle>
        </DialogHeader>

        <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
            {/* Title */}
            <div className="md:col-span-2">
              <Label htmlFor="title">Title *</Label>
              <Controller
                name="title"
                control={control}
                render={({ field }) => (
                  <Input
                    {...field}
                    id="title"
                    placeholder="e.g., Office supplies for February"
                    disabled={isSubmitting}
                  />
                )}
              />
              {errors.title && (
                <p className="text-sm text-red-500 mt-1">{errors.title.message}</p>
              )}
            </div>

            {/* Category */}
            <div>
              <Label htmlFor="categoryId">Category *</Label>
              <Controller
                name="categoryId"
                control={control}
                render={({ field }) => (
                  <Select
                    value={field.value}
                    onValueChange={field.onChange}
                    disabled={isSubmitting}
                  >
                    <SelectTrigger id="categoryId">
                      <SelectValue placeholder="Select category" />
                    </SelectTrigger>
                    <SelectContent>
                      {EXPENSE_CATEGORIES.map((cat) => (
                        <SelectItem key={cat.id} value={cat.id}>
                          {cat.name}
                        </SelectItem>
                      ))}
                    </SelectContent>
                  </Select>
                )}
              />
              {errors.categoryId && (
                <p className="text-sm text-red-500 mt-1">{errors.categoryId.message}</p>
              )}
            </div>

            {/* Amount */}
            <div>
              <Label htmlFor="amount">Amount *</Label>
              <Controller
                name="amount"
                control={control}
                render={({ field }) => (
                  <NumericInput
                    {...field}
                    id="amount"
                    placeholder="0.00"
                    min={0}
                    step={0.01}
                    disabled={isSubmitting}
                  />
                )}
              />
              {errors.amount && (
                <p className="text-sm text-red-500 mt-1">{errors.amount.message}</p>
              )}
            </div>

            {/* Date */}
            <div>
              <Label htmlFor="date">Date *</Label>
              <Controller
                name="date"
                control={control}
                render={({ field }) => (
                  <Input
                    {...field}
                    id="date"
                    type="date"
                    disabled={isSubmitting}
                  />
                )}
              />
              {errors.date && (
                <p className="text-sm text-red-500 mt-1">{errors.date.message}</p>
              )}
            </div>

            {/* Description */}
            <div className="md:col-span-2">
              <Label htmlFor="description">Description</Label>
              <Controller
                name="description"
                control={control}
                render={({ field }) => (
                  <Textarea
                    {...field}
                    id="description"
                    placeholder="Add details about this expense..."
                    rows={3}
                    disabled={isSubmitting}
                  />
                )}
              />
              {errors.description && (
                <p className="text-sm text-red-500 mt-1">{errors.description.message}</p>
              )}
            </div>

            {/* Receipt Upload */}
            <div className="md:col-span-2">
              <Label htmlFor="receipt">Receipt (Optional)</Label>
              <div className="mt-1">
                {!receiptFile ? (
                  <label
                    htmlFor="receipt"
                    className="flex items-center justify-center w-full h-32 border-2 border-dashed border-gray-300 rounded-lg cursor-pointer hover:bg-gray-50"
                  >
                    <div className="flex flex-col items-center">
                      <UploadIcon className="h-8 w-8 text-gray-400" />
                      <span className="mt-2 text-sm text-gray-500">
                        Click to upload receipt (Max 5MB)
                      </span>
                      <span className="text-xs text-gray-400">
                        Images (JPEG, PNG, GIF) or PDF
                      </span>
                    </div>
                    <input
                      id="receipt"
                      type="file"
                      className="hidden"
                      accept="image/*,application/pdf"
                      onChange={handleFileChange}
                      disabled={isSubmitting}
                    />
                  </label>
                ) : (
                  <div className="flex items-center justify-between p-3 border rounded-lg">
                    <span className="text-sm truncate">{receiptFile.name}</span>
                    <Button
                      type="button"
                      variant="ghost"
                      size="sm"
                      onClick={handleRemoveFile}
                      disabled={isSubmitting}
                    >
                      <XIcon className="h-4 w-4" />
                    </Button>
                  </div>
                )}
              </div>
            </div>

            {/* Notes */}
            <div className="md:col-span-2">
              <Label htmlFor="notes">Notes</Label>
              <Controller
                name="notes"
                control={control}
                render={({ field }) => (
                  <Textarea
                    {...field}
                    id="notes"
                    placeholder="Additional notes..."
                    rows={2}
                    disabled={isSubmitting}
                  />
                )}
              />
              {errors.notes && (
                <p className="text-sm text-red-500 mt-1">{errors.notes.message}</p>
              )}
            </div>
          </div>

          <DialogFooter>
            <Button
              type="button"
              variant="outline"
              onClick={onClose}
              disabled={isSubmitting}
            >
              Cancel
            </Button>
            <Button type="submit" disabled={isSubmitting || !isDirty}>
              {isSubmitting && <LoadingSpinner size="sm" className="mr-2" />}
              {isEditMode ? 'Update Expense' : 'Create Expense'}
            </Button>
          </DialogFooter>
        </form>
      </DialogContent>
    </Dialog>
  )
}
