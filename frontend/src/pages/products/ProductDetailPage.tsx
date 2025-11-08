import React from 'react'
import { useNavigate, useParams, Link } from 'react-router-dom'
import { ArrowLeft, Edit, Trash2, Package, DollarSign, Tag, Barcode, Loader2 } from 'lucide-react'
import { Button } from '@/components/ui/button'
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card'
import { Badge } from '@/components/ui/badge'
import { useProduct, useDeleteProduct } from '@/hooks/useProducts'
import { ProductStatus } from '@/types/api'
import { formatDate } from '@/lib/utils'
import {
  AlertDialog,
  AlertDialogAction,
  AlertDialogCancel,
  AlertDialogContent,
  AlertDialogDescription,
  AlertDialogFooter,
  AlertDialogHeader,
  AlertDialogTitle,
} from '@/components/ui/alert-dialog'
import { useCurrency } from '@/hooks/useCurrency'

export const ProductDetailPage: React.FC = () => {
  const navigate = useNavigate()
  const { productId } = useParams<{ productId: string }>()
  const { formatCurrency } = useCurrency()
  
  const { data: product, isLoading, error } = useProduct(productId)
  const deleteProductMutation = useDeleteProduct()

  const [showDeleteDialog, setShowDeleteDialog] = React.useState(false)

  const handleDelete = async () => {
    if (!productId) return

    try {
      await deleteProductMutation.mutateAsync(productId)
      navigate('/products')
    } catch (error) {
      // Error handled by mutation
      setShowDeleteDialog(false)
    }
  }

  const getStatusBadge = (status: ProductStatus) => {
    const variants = {
      [ProductStatus.ACTIVE]: 'success',
      [ProductStatus.INACTIVE]: 'secondary',
      [ProductStatus.DISCONTINUED]: 'destructive',
    } as const

    return (
      <Badge variant={variants[status] || 'secondary'}>
        {status}
      </Badge>
    )
  }

  const calculateProfitMargin = () => {
    if (!product || !product.costPrice) return 'N/A'
    if (product.price <= product.costPrice) return '0%'
    
    const margin = ((product.price - product.costPrice) / product.price) * 100
    return `${margin.toFixed(1)}%`
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
    <div className="space-y-6">
      {/* Header */}
      <div>
        <Button
          variant="ghost"
          onClick={() => navigate('/products')}
          className="mb-4"
        >
          <ArrowLeft className="h-4 w-4 mr-2" />
          Back to Products
        </Button>

        <div className="flex justify-between items-start">
          <div>
            <div className="flex items-center gap-3">
              <h1 className="text-3xl font-bold">{product.name}</h1>
              {getStatusBadge(product.status)}
            </div>
            {product.description && (
              <p className="text-gray-600 mt-2">{product.description}</p>
            )}
          </div>

          <div className="flex gap-2">
            <Button
              variant="outline"
              onClick={() => navigate(`/products/${productId}/edit`)}
            >
              <Edit className="h-4 w-4 mr-2" />
              Edit
            </Button>
            <Button
              variant="destructive"
              onClick={() => setShowDeleteDialog(true)}
            >
              <Trash2 className="h-4 w-4 mr-2" />
              Delete
            </Button>
          </div>
        </div>
      </div>

      {/* Product Information Cards */}
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4">
        {/* Price Card */}
        <Card>
          <CardContent className="p-6">
            <div className="flex items-center justify-between">
              <div>
                <p className="text-sm font-medium text-gray-600">Selling Price</p>
                <p className="text-2xl font-bold mt-1">
                  {formatCurrency(product.price)}
                </p>
              </div>
              <div className="p-3 rounded-lg bg-green-50">
                <DollarSign className="h-6 w-6 text-green-600" />
              </div>
            </div>
          </CardContent>
        </Card>

        {/* Cost Price Card */}
        <Card>
          <CardContent className="p-6">
            <div className="flex items-center justify-between">
              <div>
                <p className="text-sm font-medium text-gray-600">Cost Price</p>
                <p className="text-2xl font-bold mt-1">
                  {product.costPrice ? formatCurrency(product.costPrice) : 'N/A'}
                </p>
              </div>
              <div className="p-3 rounded-lg bg-blue-50">
                <DollarSign className="h-6 w-6 text-blue-600" />
              </div>
            </div>
          </CardContent>
        </Card>

        {/* Profit Margin Card */}
        <Card>
          <CardContent className="p-6">
            <div className="flex items-center justify-between">
              <div>
                <p className="text-sm font-medium text-gray-600">Profit Margin</p>
                <p className="text-2xl font-bold mt-1">
                  {calculateProfitMargin()}
                </p>
              </div>
              <div className="p-3 rounded-lg bg-purple-50">
                <Tag className="h-6 w-6 text-purple-600" />
              </div>
            </div>
          </CardContent>
        </Card>

        {/* Category Card */}
        <Card>
          <CardContent className="p-6">
            <div className="flex items-center justify-between">
              <div>
                <p className="text-sm font-medium text-gray-600">Category</p>
                <p className="text-lg font-bold mt-1">{product.categoryName}</p>
              </div>
              <div className="p-3 rounded-lg bg-orange-50">
                <Package className="h-6 w-6 text-orange-600" />
              </div>
            </div>
          </CardContent>
        </Card>
      </div>

      {/* Product Details */}
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        {/* Basic Information */}
        <Card>
          <CardHeader>
            <CardTitle>Product Information</CardTitle>
            <CardDescription>Basic product details and identifiers</CardDescription>
          </CardHeader>
          <CardContent className="space-y-4">
            <div className="flex justify-between py-2 border-b">
              <span className="text-gray-600">SKU:</span>
              <span className="font-mono font-medium">{product.sku}</span>
            </div>
            
            {product.barcode && (
              <div className="flex justify-between py-2 border-b">
                <span className="text-gray-600">Barcode:</span>
                <span className="font-mono font-medium flex items-center gap-2">
                  <Barcode className="h-4 w-4" />
                  {product.barcode}
                </span>
              </div>
            )}

            <div className="flex justify-between py-2 border-b">
              <span className="text-gray-600">Category:</span>
              <Badge variant="outline">{product.categoryName}</Badge>
            </div>

            <div className="flex justify-between py-2 border-b">
              <span className="text-gray-600">Status:</span>
              {getStatusBadge(product.status)}
            </div>

            <div className="flex justify-between py-2 border-b">
              <span className="text-gray-600">Created:</span>
              <span className="font-medium">{formatDate(product.createdAt)}</span>
            </div>

            <div className="flex justify-between py-2">
              <span className="text-gray-600">Last Updated:</span>
              <span className="font-medium">{formatDate(product.updatedAt)}</span>
            </div>
          </CardContent>
        </Card>

        {/* Inventory Information (Placeholder) */}
        <Card>
          <CardHeader>
            <CardTitle>Inventory Status</CardTitle>
            <CardDescription>Stock levels across your shops</CardDescription>
          </CardHeader>
          <CardContent>
            <div className="text-center py-8 text-gray-500">
              <Package className="h-12 w-12 mx-auto mb-2 text-gray-400" />
              <p>Inventory information will be displayed here</p>
              <Link to={`/inventory?product=${productId}`}>
                <Button variant="link" className="mt-2">
                  View in Inventory
                </Button>
              </Link>
            </div>
          </CardContent>
        </Card>
      </div>

      {/* Sales History (Placeholder) */}
      <Card>
        <CardHeader>
          <CardTitle>Sales History</CardTitle>
          <CardDescription>Recent sales transactions for this product</CardDescription>
        </CardHeader>
        <CardContent>
          <div className="text-center py-8 text-gray-500">
            <p>Sales history will be displayed here</p>
          </div>
        </CardContent>
      </Card>

      {/* Delete Confirmation Dialog */}
      <AlertDialog open={showDeleteDialog} onOpenChange={setShowDeleteDialog}>
        <AlertDialogContent>
          <AlertDialogHeader>
            <AlertDialogTitle>Delete Product</AlertDialogTitle>
            <AlertDialogDescription>
              Are you sure you want to delete "{product.name}"? This action cannot
              be undone and will affect all associated inventory records.
            </AlertDialogDescription>
          </AlertDialogHeader>
          <AlertDialogFooter>
            <AlertDialogCancel>Cancel</AlertDialogCancel>
            <AlertDialogAction
              onClick={handleDelete}
              className="bg-red-600 hover:bg-red-700"
            >
              {deleteProductMutation.isPending ? 'Deleting...' : 'Delete'}
            </AlertDialogAction>
          </AlertDialogFooter>
        </AlertDialogContent>
      </AlertDialog>
    </div>
  )
}
