import React from 'react'
import { useNavigate } from 'react-router-dom'
import { ArrowLeft } from 'lucide-react'
import { Button } from '@/components/ui/button'
import { ProductForm, ProductFormData } from '@/components/products/ProductForm'
import { useCreateProduct } from '@/hooks/useProducts'
import { useAuth } from '@/context/ManualAuthContext'
import { toast } from 'sonner'

export const CreateProductPage: React.FC = () => {
  const navigate = useNavigate()
  const { user } = useAuth()
  const createProductMutation = useCreateProduct()

  const handleSubmit = async (data: ProductFormData) => {
    if (!user?.shopId) {
      toast.error('Shop ID not found. Please log in again.')
      return
    }

    try {
      // Build product data with shopId and proper type handling
      const productData = {
        ...data,
        shopId: user.shopId, // Add shopId from current user
        costPrice: data.costPrice ?? undefined,
        weightInGrams: data.weightInGrams ?? undefined,
        unit: data.unit || undefined,
        location: data.location || undefined,
        dimensions: data.dimensions || undefined,
        supplierName: data.supplierName || undefined,
        supplierContact: data.supplierContact || undefined,
        imageUrl: data.imageUrl || undefined,
        isTaxable: data.isTaxable ?? true,
        isDiscountable: data.isDiscountable ?? true,
      }
      const newProduct = await createProductMutation.mutateAsync(productData)
      navigate(`/products/${newProduct.id}`)
    } catch (error) {
      // Error handled by mutation
    }
  }

  const handleCancel = () => {
    navigate('/products')
  }

  return (
    <div className="space-y-6 max-w-4xl">
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
        
        <div>
          <h1 className="text-3xl font-bold">Create New Product</h1>
          <p className="text-gray-600 mt-1">
            Add a new product to your catalog
          </p>
        </div>
      </div>

      {/* Product Form */}
      <ProductForm
        onSubmit={handleSubmit}
        onCancel={handleCancel}
        isSubmitting={createProductMutation.isPending}
      />
    </div>
  )
}
