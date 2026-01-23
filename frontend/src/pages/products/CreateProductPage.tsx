import React from 'react'
import { useNavigate } from 'react-router-dom'
import { ArrowLeft } from 'lucide-react'
import { Button } from '@/components/ui/button'
import { ProductForm, ProductFormData } from '@/components/products/ProductForm'
import { useCreateProduct } from '@/hooks/useProducts'
import { useAuth } from '@/context/UnifiedAuthContext'
import { toast } from 'sonner'
import { ShopSelector } from '@/components/ui/shop-selector'
import { useShopContext } from '@/context/ShopContext'

export const CreateProductPage: React.FC = () => {
  const navigate = useNavigate()
  const { user } = useAuth()
  const { selectedShopId, setSelectedShopId, canManageMultipleShops } = useShopContext()
  
  // Use selectedShopId for multi-shop users, fall back to user.shopId for single-shop users
  const effectiveShopId = canManageMultipleShops ? selectedShopId : user?.shopId
  const createProductMutation = useCreateProduct(effectiveShopId || undefined)

  const handleSubmit = async (data: ProductFormData) => {
    if (!effectiveShopId) {
      toast.error('Shop ID not found. Please select a shop or log in again.')
      return
    }

    try {
      // Convert weight from kg to grams for API
      const weightInGrams = data.weightInKg ? data.weightInKg * 1000 : undefined

      const productData = {
        ...data,
        shopId: effectiveShopId,
        weightInGrams: weightInGrams,
        dimensions: data.dimensions || undefined,
        supplierName: data.supplierName || undefined,
        supplierContact: data.supplierContact || undefined,
        imageUrl: data.imageUrl || undefined,
        barcode: data.barcode || undefined,
        isTaxable: data.isTaxable ?? true,
        isDiscountable: data.isDiscountable ?? true,
      }

      const { category, status, weightInKg, ...rest } = productData;
      const newProduct = await createProductMutation.mutateAsync(rest)
      navigate(`/products/${newProduct.id}`)
    } catch (error) {
      console.error('Error creating product:', error)
      // Error handled by mutation
    }
  }

  const handleCancel = () => {
    navigate('/products')
  }

  return (
    <div className="space-y-6 max-w-6xl">
      {/* Breadcrumb / Back Navigation */}
      <div>
        <Button
          variant="ghost"
          onClick={() => navigate('/products')}
          className="mb-4"
        >
          <ArrowLeft className="h-4 w-4 mr-2" />
          Back to Products
        </Button>
        
        <div className="flex items-start justify-between">
          <div>
            <h1 className="text-3xl font-bold">Create New Product</h1>
            <p className="text-gray-600 mt-1">
              Add a new product to your catalog
            </p>
          </div>
          {canManageMultipleShops && selectedShopId && (
            <ShopSelector
              value={selectedShopId}
              onValueChange={setSelectedShopId}
              className="w-[200px]"
            />
          )}
        </div>
      </div>

      {/* Product Form */}
      <ProductForm
        onSubmit={handleSubmit}
        onCancel={handleCancel}
        isSubmitting={createProductMutation.isPending}
        shopId={effectiveShopId as string}
      />
    </div>
  )
}
