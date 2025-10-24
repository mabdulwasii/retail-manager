import React, { useEffect } from 'react'
import { useForm } from 'react-hook-form'
import { yupResolver } from '@hookform/resolvers/yup'
import * as yup from 'yup'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import { Textarea } from '@/components/ui/textarea'
import { Label } from '@/components/ui/label'
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/components/ui/select'
import { Card, CardContent } from '@/components/ui/card'
import { Loader2, Sparkles } from 'lucide-react'
import { Product, ProductStatus } from '@/types/api'
import { productService } from '@/services/productService'
import { useProductCategories } from '@/hooks/useProducts'

const productSchema = yup.object({
  name: yup.string()
    .required('Product name is required')
    .min(2, 'Product name must be at least 2 characters')
    .max(200, 'Product name must be at most 200 characters'),
  description: yup.string()
    .optional()
    .max(1000, 'Description must be at most 1000 characters'),
  category: yup.string()
    .required('Category is required')
    .min(2, 'Category must be at least 2 characters'),
  price: yup.number()
    .typeError('Price must be a number')
    .required('Price is required')
    .min(0.01, 'Price must be greater than 0')
    .max(1000000, 'Price must be less than 1,000,000'),
  costPrice: yup.number()
    .typeError('Cost price must be a number')
    .min(0, 'Cost price must be 0 or greater')
    .optional()
    .nullable()
    .transform((value, originalValue) => originalValue === '' ? null : value),
  sku: yup.string()
    .optional()
    .matches(/^[A-Z0-9-]+$/, 'SKU must contain only uppercase letters, numbers, and hyphens'),
  barcode: yup.string()
    .optional()
    .matches(/^[0-9]+$/, 'Barcode must contain only numbers'),
  status: yup.string()
    .oneOf(Object.values(ProductStatus))
    .optional(),
})

export type ProductFormData = yup.InferType<typeof productSchema>

interface ProductFormProps {
  product?: Product
  onSubmit: (data: ProductFormData) => void | Promise<void>
  onCancel: () => void
  isSubmitting?: boolean
}

export const ProductForm: React.FC<ProductFormProps> = ({
  product,
  onSubmit,
  onCancel,
  isSubmitting = false,
}) => {
  const { data: categories = [], isLoading: categoriesLoading } = useProductCategories()

  const {
    register,
    handleSubmit,
    setValue,
    watch,
    formState: { errors },
  } = useForm<ProductFormData>({
    resolver: yupResolver(productSchema),
    defaultValues: {
      name: product?.name || '',
      description: product?.description || '',
      category: product?.category || '',
      price: product?.price || 0,
      costPrice: product?.costPrice || undefined,
      sku: product?.sku || '',
      barcode: product?.barcode || '',
      status: product?.status || ProductStatus.ACTIVE,
    },
  })

  const category = watch('category')
  const status = watch('status')

  const handleGenerateSKU = () => {
    const newSKU = productService.generateSKU()
    setValue('sku', newSKU)
  }

  const calculateProfitMargin = () => {
    const price = watch('price')
    const costPrice = watch('costPrice')
    if (price && costPrice && costPrice > 0) {
      const margin = ((price - costPrice) / price) * 100
      return margin.toFixed(2) + '%'
    }
    return 'N/A'
  }

  return (
    <form onSubmit={handleSubmit(onSubmit)} className="space-y-6">
      <Card>
        <CardContent className="pt-6 space-y-4">
          {/* Product Name */}
          <div className="space-y-2">
            <Label htmlFor="name">
              Product Name <span className="text-red-500">*</span>
            </Label>
            <Input
              id="name"
              {...register('name')}
              placeholder="Enter product name"
              disabled={isSubmitting}
            />
            {errors.name && (
              <p className="text-sm text-red-500">{errors.name.message}</p>
            )}
          </div>

          {/* Description */}
          <div className="space-y-2">
            <Label htmlFor="description">Description</Label>
            <Textarea
              id="description"
              {...register('description')}
              placeholder="Enter product description"
              rows={3}
              disabled={isSubmitting}
            />
            {errors.description && (
              <p className="text-sm text-red-500">{errors.description.message}</p>
            )}
          </div>

          {/* Category */}
          <div className="space-y-2">
            <Label htmlFor="category">
              Category <span className="text-red-500">*</span>
            </Label>
            {categoriesLoading ? (
              <div className="flex items-center gap-2 text-sm text-gray-500">
                <Loader2 className="h-4 w-4 animate-spin" />
                Loading categories...
              </div>
            ) : (
              <>
                <Select
                  value={category}
                  onValueChange={(value) => setValue('category', value)}
                  disabled={isSubmitting}
                >
                  <SelectTrigger>
                    <SelectValue placeholder="Select a category" />
                  </SelectTrigger>
                  <SelectContent>
                    {categories.map((cat) => (
                      <SelectItem key={cat} value={cat}>
                        {cat}
                      </SelectItem>
                    ))}
                    <SelectItem value="__new__">+ Add New Category</SelectItem>
                  </SelectContent>
                </Select>
                {category === '__new__' && (
                  <Input
                    placeholder="Enter new category name"
                    onChange={(e) => setValue('category', e.target.value)}
                    disabled={isSubmitting}
                  />
                )}
              </>
            )}
            {errors.category && (
              <p className="text-sm text-red-500">{errors.category.message}</p>
            )}
          </div>

          {/* Price and Cost Price */}
          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
            <div className="space-y-2">
              <Label htmlFor="price">
                Selling Price <span className="text-red-500">*</span>
              </Label>
              <Input
                id="price"
                type="number"
                step="0.01"
                {...register('price')}
                placeholder="0.00"
                disabled={isSubmitting}
              />
              {errors.price && (
                <p className="text-sm text-red-500">{errors.price.message}</p>
              )}
            </div>

            <div className="space-y-2">
              <Label htmlFor="costPrice">Cost Price</Label>
              <Input
                id="costPrice"
                type="number"
                step="0.01"
                {...register('costPrice')}
                placeholder="0.00"
                disabled={isSubmitting}
              />
              {errors.costPrice && (
                <p className="text-sm text-red-500">{errors.costPrice.message}</p>
              )}
            </div>
          </div>

          {/* Profit Margin Display */}
          {(watch('price') || 0) > 0 && (watch('costPrice') || 0) > 0 && (
            <div className="p-3 bg-green-50 border border-green-200 rounded-md">
              <p className="text-sm text-green-800">
                <strong>Profit Margin:</strong> {calculateProfitMargin()}
              </p>
            </div>
          )}

          {/* SKU */}
          <div className="space-y-2">
            <Label htmlFor="sku">SKU (Stock Keeping Unit)</Label>
            <div className="flex gap-2">
              <Input
                id="sku"
                {...register('sku')}
                placeholder="Auto-generated if empty"
                disabled={isSubmitting}
                className="flex-1"
              />
              <Button
                type="button"
                variant="outline"
                onClick={handleGenerateSKU}
                disabled={isSubmitting}
              >
                <Sparkles className="h-4 w-4 mr-2" />
                Generate
              </Button>
            </div>
            {errors.sku && (
              <p className="text-sm text-red-500">{errors.sku.message}</p>
            )}
          </div>

          {/* Barcode */}
          <div className="space-y-2">
            <Label htmlFor="barcode">Barcode</Label>
            <Input
              id="barcode"
              {...register('barcode')}
              placeholder="Enter barcode"
              disabled={isSubmitting}
            />
            {errors.barcode && (
              <p className="text-sm text-red-500">{errors.barcode.message}</p>
            )}
          </div>

          {/* Status (only show for edit mode) */}
          {product && (
            <div className="space-y-2">
              <Label htmlFor="status">Status</Label>
              <Select
                value={status || undefined}
                onValueChange={(value) => setValue('status', value as ProductStatus)}
                disabled={isSubmitting}
              >
                <SelectTrigger>
                  <SelectValue />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value={ProductStatus.ACTIVE}>Active</SelectItem>
                  <SelectItem value={ProductStatus.INACTIVE}>Inactive</SelectItem>
                  <SelectItem value={ProductStatus.DISCONTINUED}>Discontinued</SelectItem>
                </SelectContent>
              </Select>
            </div>
          )}
        </CardContent>
      </Card>

      {/* Form Actions */}
      <div className="flex justify-end gap-3">
        <Button
          type="button"
          variant="outline"
          onClick={onCancel}
          disabled={isSubmitting}
        >
          Cancel
        </Button>
        <Button type="submit" disabled={isSubmitting}>
          {isSubmitting && <Loader2 className="mr-2 h-4 w-4 animate-spin" />}
          {product ? 'Update Product' : 'Create Product'}
        </Button>
      </div>
    </form>
  )
}
