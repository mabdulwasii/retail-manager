import React, { useState } from 'react'
import { Edit, Trash2, MoreVertical, Tag } from 'lucide-react'
import { Category } from '@/services/categoryService'
import { Badge } from '@/components/ui/badge'
import { Button } from '@/components/ui/button'
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from '@/components/ui/table'
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuSeparator,
  DropdownMenuTrigger,
} from '@/components/ui/dropdown-menu'
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
import { Alert, AlertDescription } from '@/components/ui/alert'
import { Loader2 } from 'lucide-react'

interface CategoryListProps {
  categories: Category[]
  isLoading?: boolean
  onEdit?: (category: Category) => void
  onDelete?: (categoryId: string) => void
  canUpdate?: boolean
  canDelete?: boolean
}

export const CategoryList: React.FC<CategoryListProps> = ({
  categories,
  isLoading,
  onEdit,
  onDelete,
  canUpdate = false,
  canDelete = false,
}) => {
  const [categoryToDelete, setCategoryToDelete] = useState<Category | null>(null)

  const handleDelete = () => {
    if (categoryToDelete && onDelete) {
      onDelete(categoryToDelete.id)
      setCategoryToDelete(null)
    }
  }

  if (isLoading) {
    return (
      <div className="flex items-center justify-center py-12">
        <Loader2 className="h-8 w-8 animate-spin text-muted-foreground" />
        <span className="ml-2 text-muted-foreground">Loading categories...</span>
      </div>
    )
  }

  if (!categories || categories.length === 0) {
    return (
      <Alert>
        <Tag className="h-4 w-4" />
        <AlertDescription>
          No categories found. Create your first category to organize your products.
        </AlertDescription>
      </Alert>
    )
  }

  return (
    <>
      <div className="rounded-md border">
        <Table>
          <TableHeader>
            <TableRow>
              <TableHead>Name</TableHead>
              <TableHead>Description</TableHead>
              <TableHead>Products</TableHead>
              <TableHead>Status</TableHead>
              <TableHead className="w-[50px]"></TableHead>
            </TableRow>
          </TableHeader>
          <TableBody>
            {categories.map((category) => (
              <TableRow key={category.id}>
                <TableCell className="font-medium">
                  <div className="flex items-center gap-2">
                    <Tag className="h-4 w-4 text-muted-foreground" />
                    {category.name}
                  </div>
                </TableCell>
                <TableCell className="text-muted-foreground">
                  {category.description || '-'}
                </TableCell>
                <TableCell>
                  <Badge variant="secondary">
                    {category.children?.length || 0} subcategories
                  </Badge>
                </TableCell>
                <TableCell>
                  <Badge variant={category.isActive ? 'default' : 'secondary'}>
                    {category.isActive ? 'Active' : 'Inactive'}
                  </Badge>
                </TableCell>
                <TableCell>
                  {(canUpdate || canDelete) && (
                    <DropdownMenu>
                      <DropdownMenuTrigger asChild>
                        <Button variant="ghost" size="sm">
                          <MoreVertical className="h-4 w-4" />
                        </Button>
                      </DropdownMenuTrigger>
                      <DropdownMenuContent align="end">
                        {canUpdate && onEdit && (
                          <DropdownMenuItem onClick={() => onEdit(category)}>
                            <Edit className="h-4 w-4 mr-2" />
                            Edit
                          </DropdownMenuItem>
                        )}
                        {canUpdate && canDelete && <DropdownMenuSeparator />}
                        {canDelete && (
                          <DropdownMenuItem
                            onClick={() => setCategoryToDelete(category)}
                            className="text-red-600"
                          >
                            <Trash2 className="h-4 w-4 mr-2" />
                            Delete
                          </DropdownMenuItem>
                        )}
                      </DropdownMenuContent>
                    </DropdownMenu>
                  )}
                </TableCell>
              </TableRow>
            ))}
          </TableBody>
        </Table>
      </div>

      {/* Delete Confirmation Dialog */}
      <AlertDialog
        open={!!categoryToDelete}
        onOpenChange={() => setCategoryToDelete(null)}
      >
        <AlertDialogContent>
          <AlertDialogHeader>
            <AlertDialogTitle>Delete Category</AlertDialogTitle>
            <AlertDialogDescription>
              Are you sure you want to delete "{categoryToDelete?.name}"? This action
              cannot be undone. Products in this category will be uncategorized.
            </AlertDialogDescription>
          </AlertDialogHeader>
          <AlertDialogFooter>
            <AlertDialogCancel>Cancel</AlertDialogCancel>
            <AlertDialogAction
              onClick={handleDelete}
              className="bg-red-600 hover:bg-red-700"
            >
              Delete
            </AlertDialogAction>
          </AlertDialogFooter>
        </AlertDialogContent>
      </AlertDialog>
    </>
  )
}
