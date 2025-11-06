import React, { useState } from 'react'
import { Plus, Tag, ArrowLeft } from 'lucide-react'
import { useNavigate } from 'react-router-dom'
import { Button } from '@/components/ui/button'
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card'
import { CategoryList, CategoryForm } from '@/components/categories'
import { useCategories, useDeleteCategory } from '@/hooks/useCategories'
import { Category } from '@/services/categoryService'
import { useAuth } from '@/context/ManualAuthContext'
import { Alert, AlertDescription } from '@/components/ui/alert'
import { AlertCircle } from 'lucide-react'

export const CategoriesPage: React.FC = () => {
  const navigate = useNavigate()
  const { hasPermission } = useAuth()
  
  // Check permissions based on backend permission matrix
  const canCreateCategory = hasPermission('CATEGORY_CREATE')  // MANAGER and above
  const canUpdateCategory = hasPermission('CATEGORY_UPDATE')  // MANAGER and above
  const canDeleteCategory = hasPermission('CATEGORY_DELETE')  // MANAGER and above
  const canViewCategories = hasPermission('CATEGORY_LIST')    // EMPLOYEE and above

  const [isFormOpen, setIsFormOpen] = useState(false)
  const [selectedCategory, setSelectedCategory] = useState<Category | null>(null)

  // Fetch categories
  const { data: categories = [], isLoading, refetch } = useCategories(false)
  const deleteMutation = useDeleteCategory()

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
      // Error handled by mutation
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
        {canCreateCategory && (
          <Button onClick={handleCreate}>
            <Plus className="h-4 w-4 mr-2" />
            New Category
          </Button>
        )}
      </div>

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
      />
    </div>
  )
}
