import React, { useEffect } from 'react'
import { useForm } from 'react-hook-form'
import { Category, CategoryCreateRequest, CategoryUpdateRequest } from '@/services/categoryService'
import { Button } from '@/components/ui/button'
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from '@/components/ui/dialog'
import { Input } from '@/components/ui/input'
import { Label } from '@/components/ui/label'
import { Textarea } from '@/components/ui/textarea'
import { Switch } from '@/components/ui/switch'
import { useCreateCategory, useUpdateCategory } from '@/hooks/useCategories'

interface CategoryFormProps {
  open: boolean
  onOpenChange: (open: boolean) => void
  category?: Category | null
  onSuccess?: () => void
}

export const CategoryForm: React.FC<CategoryFormProps> = ({
  open,
  onOpenChange,
  category,
  onSuccess,
}) => {
  const isEdit = !!category
  const createMutation = useCreateCategory()
  const updateMutation = useUpdateCategory()

  const {
    register,
    handleSubmit,
    reset,
    watch,
    setValue,
    formState: { errors, isSubmitting },
  } = useForm<CategoryCreateRequest>({
    defaultValues: {
      name: '',
      description: '',
      imageUrl: '',
      displayOrder: 0,
      isActive: true,
    },
  })

  // Reset form when category changes or dialog opens/closes
  useEffect(() => {
    if (open) {
      if (category) {
        reset({
          name: category.name,
          description: category.description || '',
          imageUrl: category.imageUrl || '',
          displayOrder: category.displayOrder || 0,
          isActive: category.isActive,
        })
      } else {
        reset({
          name: '',
          description: '',
          imageUrl: '',
          displayOrder: 0,
          isActive: true,
        })
      }
    }
  }, [category, open, reset])

  const onSubmit = async (data: CategoryCreateRequest) => {
    try {
      if (isEdit) {
        await updateMutation.mutateAsync({
          categoryId: category.id,
          data: data as CategoryUpdateRequest,
        })
      } else {
        await createMutation.mutateAsync(data)
      }
      onOpenChange(false)
      reset()
      onSuccess?.()
    } catch (error) {
      // Error handled by mutation
    }
  }

  const isActive = watch('isActive')

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className="sm:max-w-[500px]">
        <DialogHeader>
          <DialogTitle>{isEdit ? 'Edit Category' : 'Create Category'}</DialogTitle>
          <DialogDescription>
            {isEdit
              ? 'Update category details below.'
              : 'Create a new category to organize your products.'}
          </DialogDescription>
        </DialogHeader>

        <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
          {/* Name */}
          <div className="space-y-2">
            <Label htmlFor="name">
              Name <span className="text-red-500">*</span>
            </Label>
            <Input
              id="name"
              placeholder="e.g., Electronics, Clothing"
              {...register('name', {
                required: 'Category name is required',
                minLength: { value: 2, message: 'Name must be at least 2 characters' },
              })}
            />
            {errors.name && (
              <p className="text-sm text-red-600">{errors.name.message}</p>
            )}
          </div>

          {/* Description */}
          <div className="space-y-2">
            <Label htmlFor="description">Description</Label>
            <Textarea
              id="description"
              placeholder="Brief description of the category"
              rows={3}
              {...register('description')}
            />
          </div>

          {/* Image URL */}
          <div className="space-y-2">
            <Label htmlFor="imageUrl">
              Image URL <span className="text-muted-foreground text-xs">(optional)</span>
            </Label>
            <Input
              id="imageUrl"
              type="url"
              placeholder="https://example.com/category-image.jpg"
              {...register('imageUrl', {
                pattern: {
                  value: /^https?:\/\/.+/,
                  message: 'Please enter a valid URL starting with http:// or https://',
                },
              })}
            />
            {errors.imageUrl && (
              <p className="text-sm text-red-600">{errors.imageUrl.message}</p>
            )}
          </div>

          {/* Display Order */}
          <div className="space-y-2">
            <Label htmlFor="displayOrder">
              Display Order <span className="text-muted-foreground text-xs">(optional)</span>
            </Label>
            <Input
              id="displayOrder"
              type="number"
              min="0"
              placeholder="0"
              {...register('displayOrder', {
                valueAsNumber: true,
              })}
            />
            <p className="text-xs text-muted-foreground">
              Lower numbers appear first in lists
            </p>
          </div>

          {/* Active Status */}
          <div className="flex items-center justify-between rounded-lg border p-4">
            <div className="space-y-0.5">
              <Label htmlFor="isActive">Active Status</Label>
              <p className="text-sm text-muted-foreground">
                {isActive
                  ? 'Category is visible and can be assigned to products'
                  : 'Category is hidden from product assignment'}
              </p>
            </div>
            <Switch
              id="isActive"
              checked={isActive}
              onCheckedChange={(checked) => setValue('isActive', checked)}
            />
          </div>

          <DialogFooter>
            <Button
              type="button"
              variant="outline"
              onClick={() => onOpenChange(false)}
              disabled={isSubmitting}
            >
              Cancel
            </Button>
            <Button type="submit" disabled={isSubmitting}>
              {isSubmitting ? 'Saving...' : isEdit ? 'Update Category' : 'Create Category'}
            </Button>
          </DialogFooter>
        </form>
      </DialogContent>
    </Dialog>
  )
}
