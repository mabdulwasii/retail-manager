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
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select'
import { useCreateCategory, useUpdateCategory, useCategories } from '@/hooks/useCategories'
import { useAuth } from '@/context/ManualAuthContext'
import type { ShopResponse } from '@/services/shopService'

interface CategoryFormProps {
  open: boolean
  onOpenChange: (open: boolean) => void
  category?: Category | null
  onSuccess?: () => void
  selectedShopId?: string | undefined
  showShopSelector?: boolean
  shops?: ShopResponse[]
}

export const CategoryForm: React.FC<CategoryFormProps> = ({
  open,
  onOpenChange,
  category,
  onSuccess,
  selectedShopId,
  showShopSelector = false,
  shops = [],
}) => {
  const { user } = useAuth()
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
      shopId: selectedShopId || user?.shopId || '',
      name: '',
      description: '',
      imageUrl: '',
      displayOrder: 0,
      isActive: true,
      parentId: undefined,
    },
  })
  
  // Fetch available categories for parent selection (excluding current if editing)
  const { data: availableCategories = [] } = useCategories(false)

  // Reset form when category changes or dialog opens/closes
  useEffect(() => {
    if (open) {
      if (category) {
        reset({
          shopId: category.shopId,
          name: category.name,
          description: category.description || '',
          imageUrl: category.imageUrl || '',
          displayOrder: category.displayOrder || 0,
          isActive: category.isActive,
          parentId: category.parentId,
        })
      } else {
        reset({
          shopId: selectedShopId || user?.shopId || '',
          name: '',
          description: '',
          imageUrl: '',
          displayOrder: 0,
          isActive: true,
          parentId: undefined,
        })
      }
    }
  }, [category, open, reset, selectedShopId, user?.shopId])

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
      console.warn('Failed to create/update category:', error)
    }
  }

  const isActive = watch('isActive')
  const selectedParentId = watch('parentId')
  const formShopId = watch('shopId')
  
  // Filter categories to exclude current category when editing (prevent circular reference)
  const parentCategoryOptions = availableCategories.filter(
    cat => !category || cat.id !== category.id
  )
  const submitText = isEdit ? "Update Category" : "Create Category";

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className="sm:max-w-[500px]">
        <DialogHeader>
          <DialogTitle>
            {isEdit ? "Edit Category" : "Create Category"}
          </DialogTitle>
          <DialogDescription>
            {isEdit
              ? "Update category details below."
              : "Create a new category to organize your products."}
          </DialogDescription>
        </DialogHeader>

        <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
          {/* Shop Selector (for Tenant Admins) */}
          {showShopSelector && shops.length > 0 && (
            <div className="space-y-2">
              <Label htmlFor="shopId">
                Shop <span className="text-red-500">*</span>
              </Label>
              <Select
                value={formShopId}
                onValueChange={(value) => setValue("shopId", value)}
              >
                <SelectTrigger>
                  <SelectValue placeholder="Select a shop" />
                </SelectTrigger>
                <SelectContent>
                  {shops.map((shop) => (
                    <SelectItem key={shop.id} value={shop.id}>
                      {shop.name}
                    </SelectItem>
                  ))}
                </SelectContent>
              </Select>
              {!formShopId && (
                <p className="text-sm text-red-600">
                  Shop selection is required
                </p>
              )}
            </div>
          )}

          {/* Name */}
          <div className="space-y-2">
            <Label htmlFor="name">
              Name <span className="text-red-500">*</span>
            </Label>
            <Input
              id="name"
              placeholder="e.g., Electronics, Clothing"
              {...register("name", {
                required: "Category name is required",
                minLength: {
                  value: 2,
                  message: "Name must be at least 2 characters",
                },
              })}
            />
            {errors.name && (
              <p className="text-sm text-red-600">{errors.name.message}</p>
            )}
          </div>

          {/* Parent Category (for hierarchical categories) */}
          <div className="space-y-2">
            <Label htmlFor="parentId">
              Parent Category{" "}
              <span className="text-muted-foreground text-xs">(optional)</span>
            </Label>
            <Select
              value={selectedParentId || "none"}
              onValueChange={(value) =>
                setValue("parentId", value === "none" ? undefined : value)
              }
            >
              <SelectTrigger>
                <SelectValue placeholder="None (top-level category)" />
              </SelectTrigger>
              <SelectContent>
                <SelectItem value="none">None (top-level category)</SelectItem>
                {parentCategoryOptions.map((cat) => (
                  <SelectItem key={cat.id} value={cat.id}>
                    {cat.name}
                  </SelectItem>
                ))}
              </SelectContent>
            </Select>
            <p className="text-xs text-muted-foreground">
              Select a parent category to create a subcategory
            </p>
          </div>

          {/* Description */}
          <div className="space-y-2">
            <Label htmlFor="description">Description</Label>
            <Textarea
              id="description"
              placeholder="Brief description of the category"
              rows={3}
              {...register("description")}
            />
          </div>

          {/* Image URL */}
          <div className="space-y-2">
            <Label htmlFor="imageUrl">
              Image URL{" "}
              <span className="text-muted-foreground text-xs">(optional)</span>
            </Label>
            <Input
              id="imageUrl"
              type="url"
              placeholder="https://example.com/category-image.jpg"
              {...register("imageUrl", {
                pattern: {
                  value: /^https?:\/\/.+/,
                  message:
                    "Please enter a valid URL starting with http:// or https://",
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
              Display Order{" "}
              <span className="text-muted-foreground text-xs">(optional)</span>
            </Label>
            <Input
              id="displayOrder"
              type="number"
              min="0"
              placeholder="0"
              {...register("displayOrder", {
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
                  ? "Category is visible and can be assigned to products"
                  : "Category is hidden from product assignment"}
              </p>
            </div>
            <Switch
              id="isActive"
              checked={isActive || false}
              onCheckedChange={(checked) => setValue("isActive", checked)}
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
              {isSubmitting ? "Saving..." : submitText}
            </Button>
          </DialogFooter>
        </form>
      </DialogContent>
    </Dialog>
  );
}
