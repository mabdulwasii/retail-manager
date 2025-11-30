import React from 'react'
import { useNavigate, useParams } from 'react-router-dom'
import { ArrowLeft, Loader2 } from 'lucide-react'
import { Button } from '@/components/ui/button'
import { Card, CardContent } from '@/components/ui/card'
import { ProductForm, ProductFormData } from '@/components/products/ProductForm'
import { useProduct, useUpdateProduct } from '@/hooks/useProducts'
import { useShopContext } from '@/context/ShopContext'

export const EditProductPage: React.FC = () => {
  const navigate = useNavigate()
  const { productId } = useParams<{ productId: string }>()
  const { selectedShopId } = useShopContext()
  
  const { data: product, isLoading, error } = useProduct(productId)
  const updateProductMutation = useUpdateProduct()

  const handleSubmit = async (data: ProductFormData) => {
    if (!productId) return

    try {
      // Handle custom unit - if 'other' is selected, use customUnit
      const finalUnit = data.unit === 'other' ? data.customUnit : data.unit
      
      // Convert weight from kg to grams for API
      const weightInGrams = data.weightInKg ? data.weightInKg * 1000 : undefined
      
      // Transform null/empty to undefined for API compatibility
      const productData = {
        ...data,
        weightInGrams: weightInGrams,
        unit: finalUnit || undefined,
        dimensions: data.dimensions || undefined,
        supplierName: data.supplierName || undefined,
        supplierContact: data.supplierContact || undefined,
        imageUrl: data.imageUrl || undefined,
        barcode: data.barcode || undefined,
        isTaxable: data.isTaxable ?? true,
        isDiscountable: data.isDiscountable ?? true,
      }

      const { category, status, customUnit, weightInKg, ...rest } = productData;
      await updateProductMutation.mutateAsync({
        productId,
        data: rest,
      })
      navigate(`/products/${productId}`)
    } catch (error) {
      console.error('Error updating product:', error)
      // Error handled by mutation
    }
  }

  const handleCancel = () => {
    navigate(`/products/${productId}`)
  }

  if (isLoading) {
    return (
      <div className="flex justify-center items-center min-h-[400px]">
        <div className="text-center">
          <Loader2 className="h-8 w-8 animate-spin mx-auto mb-2" />
          <p className="text-gray-600">Loading product...</p>
        </div>
      </div>
    )
  }

  if (error || !product) {
    return (
      <div className="space-y-6">
        <Button variant="ghost" onClick={() => navigate('/products')}>
          <ArrowLeft className="h-4 w-4 mr-2" />
          Back to Products
        </Button>
        <Card>
          <CardContent className="p-8 text-center">
            <p className="text-red-600">Failed to load product</p>
            <Button
              variant="outline"
              onClick={() => navigate('/products')}
              className="mt-4"
            >
              Return to Products
            </Button>
          </CardContent>
        </Card>
      </div>
    )
  }

  return (
    <div className="space-y-6 max-w-6xl">
      {/* Breadcrumb / Back Navigation */}
      <div>
        <Button
          variant="ghost"
          onClick={() => navigate(`/products/${productId}`)}
          className="mb-4"
        >
          <ArrowLeft className="h-4 w-4 mr-2" />
          Back to Product
        </Button>
        
        <div>
          <h1 className="text-3xl font-bold">Edit Product</h1>
          <p className="text-gray-600 mt-1">{product.name}</p>
        </div>
      </div>

      {/* Product Form */}
      <ProductForm
        product={product}
        onSubmit={handleSubmit}
        onCancel={handleCancel}
        isSubmitting={updateProductMutation.isPending}
        shopId={selectedShopId || product.shopId as string}
      />
    </div>
  )
}
