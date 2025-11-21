import React, { useState, useEffect } from 'react'
import { Plus, Tag, ArrowLeft, RefreshCw, AlertCircle } from 'lucide-react'
import { useNavigate } from 'react-router-dom'
import { Button } from '@/components/ui/button'
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card'
import { CategoryList, CategoryForm } from '@/components/categories'
import { useCategories, useDeleteCategory } from '@/hooks/useCategories'
import { Category } from '@/services/categoryService'
import { useAuth } from '@/context/ManualAuthContext'
import { Alert, AlertDescription } from '@/components/ui/alert'
import { usePermissions } from '@/hooks/usePermissions'
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select'
import { useActiveShops } from '@/hooks/useShops'

export const CategoriesPage: React.FC = () => {
  const navigate = useNavigate()
  const { user, hasAnyRole } = useAuth()
  const permissions = usePermissions()
  
  // Check permissions based on backend permission matrix
  const canCreateCategory = permissions.canCreateCategory()
  const canUpdateCategory = permissions.canEditCategory() 
  const canDeleteCategory = permissions.canDeleteCategory() 
  const canViewCategories = permissions.canViewCategories()
  
  // Check if user can manage multiple shops (Tenant Admin or System Admin)
  const canManageMultipleShops = hasAnyRole(['TENANT_ADMIN', 'SYSTEM_ADMIN'])
  
  // State for shop selection (for multi-shop admins)
  const [selectedShopId, setSelectedShopId] = useState<string>(user?.shopId || '')
  
  // Fetch shops for multi-shop admins
  const { data: shops = [] } = useActiveShops()

  const [isFormOpen, setIsFormOpen] = useState(false)
  const [selectedCategory, setSelectedCategory] = useState<Category | null>(null)
  
  // Update selected shop when user changes
  useEffect(() => {
    if (!canManageMultipleShops && user?.shopId) {
      setSelectedShopId(user.shopId)
    }
  }, [user?.shopId, canManageMultipleShops])

  // Fetch categories - ensure refetch on mount
  const { data: categories = [], isLoading, refetch, error } = useCategories(false)
  const deleteMutation = useDeleteCategory()
  
  // Force refetch when page mounts or shop changes
  useEffect(() => {
    if (selectedShopId) {
      refetch()
    }
  }, [selectedShopId, refetch])
  
  // Debug logging to help identify issues
  useEffect(() => {
    console.log('Categories data:', { categories, isLoading, error, selectedShopId })
  }, [categories, isLoading, error, selectedShopId])

  const handleCreate = () => {
    setSelectedCategory(null)
    setIsFormOpen(true)
  }

  const handleEdit = (category: Category) => {
    setSelectedCategory(category)
    setIsFormOpen(true)
  }

  const handleDelete = async (categoryId: string) => {
    try {
      await deleteMutation.mutateAsync(categoryId)
      refetch()
    } catch (error) {
      console.warn('Failed to delete category:', error)
    }
  }

  const handleFormSuccess = () => {
    refetch()
  }

  // Show access denied if user doesn't have view permission
  if (!canViewCategories) {
    return (
      <div className="space-y-6">
        <div className="flex items-center gap-4">
          <Button variant="ghost" size="sm" onClick={() => navigate('/products')}>
            <ArrowLeft className="h-4 w-4 mr-2" />
            Back to Products
          </Button>
        </div>
        <Alert variant="destructive">
          <AlertCircle className="h-4 w-4" />
          <AlertDescription>
            You don't have permission to view categories. Contact your administrator for access.
          </AlertDescription>
        </Alert>
      </div>
    )
  }

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex items-center justify-between">
        <div>
          <div className="flex items-center gap-2 mb-2">
            <Button variant="ghost" size="sm" onClick={() => navigate('/products')}>
              <ArrowLeft className="h-4 w-4 mr-2" />
              Back to Products
            </Button>
          </div>
          <h1 className="text-3xl font-bold flex items-center gap-2">
            <Tag className="h-8 w-8" />
            Categories
          </h1>
          <p className="text-gray-600 mt-1">
            Organize your products with categories and subcategories
          </p>
        </div>
        <div className="flex gap-2 items-center">
          {canManageMultipleShops && shops.length > 0 && (
            <Select value={selectedShopId} onValueChange={setSelectedShopId}>
              <SelectTrigger className="w-[200px]">
                <SelectValue placeholder="Select shop" />
              </SelectTrigger>
              <SelectContent>
                {shops.map((shop) => (
                  <SelectItem key={shop.id} value={shop.id}>
                    {shop.name}
                  </SelectItem>
                ))}
              </SelectContent>
            </Select>
          )}
          <Button variant="outline" size="sm" onClick={() => refetch()} disabled={isLoading}>
            <RefreshCw className={`h-4 w-4 mr-2 ${isLoading ? 'animate-spin' : ''}`} />
            Refresh
          </Button>
          {canCreateCategory && (
            <Button onClick={handleCreate}>
              <Plus className="h-4 w-4 mr-2" />
              New Category
            </Button>
          )}
        </div>
      </div>
      
      {/* Error Alert */}
      {error && (
        <Alert variant="destructive">
          <AlertCircle className="h-4 w-4" />
          <AlertDescription>
            Failed to load categories: {error.message || 'Unknown error'}. Please try refreshing.
          </AlertDescription>
        </Alert>
      )}

      {/* Stats Card */}
      <div className="grid gap-4 md:grid-cols-3">
        <Card>
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium">Total Categories</CardTitle>
            <Tag className="h-4 w-4 text-muted-foreground" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold">{categories.length}</div>
            <p className="text-xs text-muted-foreground">
              Across all products
            </p>
          </CardContent>
        </Card>

        <Card>
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium">Active Categories</CardTitle>
            <Tag className="h-4 w-4 text-green-500" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold">
              {categories.filter(cat => cat.isActive).length}
            </div>
            <p className="text-xs text-muted-foreground">
              Available for products
            </p>
          </CardContent>
        </Card>

        <Card>
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium">Inactive Categories</CardTitle>
            <Tag className="h-4 w-4 text-gray-400" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold">
              {categories.filter(cat => !cat.isActive).length}
            </div>
            <p className="text-xs text-muted-foreground">
              Hidden from products
            </p>
          </CardContent>
        </Card>
      </div>

      {/* Categories List */}
      <Card>
        <CardHeader>
          <CardTitle>All Categories</CardTitle>
          <CardDescription>
            Manage your product categories and their details
          </CardDescription>
        </CardHeader>
        <CardContent>
          <CategoryList
            categories={categories}
            isLoading={isLoading}
            {...(canUpdateCategory && { onEdit: handleEdit })}
            {...(canDeleteCategory && { onDelete: handleDelete })}
            canUpdate={canUpdateCategory}
            canDelete={canDeleteCategory}
          />
        </CardContent>
      </Card>

      {/* Category Form Dialog */}
      <CategoryForm
        open={isFormOpen}
        onOpenChange={setIsFormOpen}
        category={selectedCategory}
        onSuccess={handleFormSuccess}
        selectedShopId={canManageMultipleShops ? selectedShopId : undefined}
        showShopSelector={canManageMultipleShops}
        shops={shops}
      />
    </div>
  )
}
