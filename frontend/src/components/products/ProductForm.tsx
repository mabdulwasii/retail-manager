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
import { Checkbox } from '@/components/ui/checkbox'
import { Loader2, Sparkles } from 'lucide-react'
import { Product, ProductStatus } from '@/types/api'
import { productService } from '@/services/productService'
import { useCategories } from '@/hooks/useCategories'

const productSchema = yup.object({
  name: yup.string()
    .required('Product name is required')
    .min(2, 'Product name must be at least 2 characters')
    .max(200, 'Product name must be at most 200 characters'),
  description: yup.string()
    .optional()
    .max(1000, 'Description must be at most 1000 characters'),
  categoryId: yup.string()
    .required('Category is required'),
  category: yup.string().optional(), // For display only
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
  unit: yup.string().optional(),
  weightInGrams: yup.number()
    .typeError('Weight must be a number')
    .min(0, 'Weight must be 0 or greater')
    .optional()
    .nullable()
    .transform((value, originalValue) => originalValue === '' ? null : value),
  location: yup.string().optional(),
  dimensions: yup.string().optional(),
  supplierName: yup.string().optional(),
  supplierContact: yup.string().optional(),
  imageUrl: yup.string().url('Must be a valid URL').optional(),
  isTaxable: yup.boolean().optional(),
  isDiscountable: yup.boolean().optional(),
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
  const { data: categories = [], isLoading: categoriesLoading } = useCategories()

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
      categoryId: product?.categoryId || '',
      category: product?.category || '', // For display
      price: product?.price || 0,
      costPrice: product?.costPrice || undefined,
      unit: product?.unit || '',
      weightInGrams: product?.weightInGrams || undefined,
      location: product?.location || '',
      dimensions: product?.dimensions || '',
      supplierName: product?.supplierName || '',
      supplierContact: product?.supplierContact || '',
      imageUrl: product?.imageUrl || '',
      isTaxable: product?.isTaxable ?? product?.taxable ?? true,
      isDiscountable: product?.isDiscountable ?? product?.discountable ?? true,
      sku: product?.sku || '',
      barcode: product?.barcode || '',
      status: product?.status || ProductStatus.ACTIVE,
    },
  })

  const categoryId = watch('categoryId')
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
            <Label htmlFor="categoryId">
              Category <span className="text-red-500">*</span>
            </Label>
            {categoriesLoading ? (
              <div className="flex items-center gap-2 text-sm text-gray-500">
                <Loader2 className="h-4 w-4 animate-spin" />
                Loading categories...
              </div>
            ) : categories.length === 0 ? (
              <div className="text-sm text-gray-500">
                No categories available. Please create a category first.
              </div>
            ) : (
              <Select
                value={categoryId}
                onValueChange={(value) => {
                  setValue('categoryId', value)
                  const selectedCat = categories.find(c => c.id === value)
                  if (selectedCat) {
                    setValue('category', selectedCat.name)
                  }
                }}
                disabled={isSubmitting}
              >
                <SelectTrigger>
                  <SelectValue placeholder="Select a category" />
                </SelectTrigger>
                <SelectContent>
                  {categories.map((cat) => (
                    <SelectItem key={cat.id} value={cat.id}>
                      {cat.name}
                    </SelectItem>
                  ))}
                </SelectContent>
              </Select>
            )}
            {errors.categoryId && (
              <p className="text-sm text-red-500">{errors.categoryId.message}</p>
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

          {/* Unit and Weight */}
          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
            <div className="space-y-2">
              <Label htmlFor="unit">Unit</Label>
              <Input
                id="unit"
                {...register('unit')}
                placeholder="e.g., bottle, pack, kg"
                disabled={isSubmitting}
              />
              {errors.unit && (
                <p className="text-sm text-red-500">{errors.unit.message}</p>
              )}
            </div>

            <div className="space-y-2">
              <Label htmlFor="weightInGrams">Weight (grams)</Label>
              <Input
                id="weightInGrams"
                type="number"
                step="0.01"
                {...register('weightInGrams')}
                placeholder="520.5"
                disabled={isSubmitting}
              />
              {errors.weightInGrams && (
                <p className="text-sm text-red-500">{errors.weightInGrams.message}</p>
              )}
            </div>
          </div>

          {/* Location and Dimensions */}
          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
            <div className="space-y-2">
              <Label htmlFor="location">Location</Label>
              <Input
                id="location"
                {...register('location')}
                placeholder="Aisle 3, Shelf B"
                disabled={isSubmitting}
              />
              {errors.location && (
                <p className="text-sm text-red-500">{errors.location.message}</p>
              )}
            </div>

            <div className="space-y-2">
              <Label htmlFor="dimensions">Dimensions</Label>
              <Input
                id="dimensions"
                {...register('dimensions')}
                placeholder="20cm x 10cm x 25cm"
                disabled={isSubmitting}
              />
              {errors.dimensions && (
                <p className="text-sm text-red-500">{errors.dimensions.message}</p>
              )}
            </div>
          </div>

          {/* Supplier Information */}
          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
            <div className="space-y-2">
              <Label htmlFor="supplierName">Supplier Name</Label>
              <Input
                id="supplierName"
                {...register('supplierName')}
                placeholder="Coca-Cola Bottling Company"
                disabled={isSubmitting}
              />
              {errors.supplierName && (
                <p className="text-sm text-red-500">{errors.supplierName.message}</p>
              )}
            </div>

            <div className="space-y-2">
              <Label htmlFor="supplierContact">Supplier Contact</Label>
              <Input
                id="supplierContact"
                {...register('supplierContact')}
                placeholder="+234-800-COCA-COLA"
                disabled={isSubmitting}
              />
              {errors.supplierContact && (
                <p className="text-sm text-red-500">{errors.supplierContact.message}</p>
              )}
            </div>
          </div>

          {/* Image URL */}
          <div className="space-y-2">
            <Label htmlFor="imageUrl">Image URL</Label>
            <Input
              id="imageUrl"
              type="url"
              {...register('imageUrl')}
              placeholder="https://cdn.example.com/products/product-image.jpg"
              disabled={isSubmitting}
            />
            {errors.imageUrl && (
              <p className="text-sm text-red-500">{errors.imageUrl.message}</p>
            )}
          </div>

          {/* Tax and Discount Options */}
          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
            <div className="flex items-center space-x-2">
              <Checkbox
                id="isTaxable"
                checked={watch('isTaxable') ?? true}
                onCheckedChange={(checked) => setValue('isTaxable', checked === true)}
                disabled={isSubmitting}
              />
              <Label htmlFor="isTaxable" className="cursor-pointer">
                Taxable Product
              </Label>
            </div>

            <div className="flex items-center space-x-2">
              <Checkbox
                id="isDiscountable"
                checked={watch('isDiscountable') ?? true}
                onCheckedChange={(checked) => setValue('isDiscountable', checked === true)}
                disabled={isSubmitting}
              />
              <Label htmlFor="isDiscountable" className="cursor-pointer">
                Discountable Product
              </Label>
            </div>
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
