import React from 'react'
import { useNavigate } from 'react-router-dom'
import { ArrowLeft } from 'lucide-react'
import { Button } from '@/components/ui/button'
import { ProductForm, ProductFormData } from '@/components/products/ProductForm'
import { useCreateProduct } from '@/hooks/useProducts'

export const CreateProductPage: React.FC = () => {
  const navigate = useNavigate()
  const createProductMutation = useCreateProduct()

  const handleSubmit = async (data: ProductFormData) => {
    try {
      // Transform null to undefined for API compatibility
      const productData = {
        ...data,
        costPrice: data.costPrice ?? undefined,
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
