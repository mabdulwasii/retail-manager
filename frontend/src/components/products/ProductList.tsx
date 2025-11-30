import React from 'react'
import { Link } from 'react-router-dom'
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
  DropdownMenuTrigger,
  DropdownMenuSeparator,
} from '@/components/ui/dropdown-menu'
import { Button } from '@/components/ui/button'
import { Badge } from '@/components/ui/badge'
import {
  MoreVertical,
  Eye,
  Edit,
  Trash2,
  Power,
  PowerOff,
  Package,
} from 'lucide-react'
import { Product, ProductStatus } from '@/types/api'

interface ProductListProps {
  products: Product[]
  onEdit?: (product: Product) => void
  onDelete?: (product: Product) => void
  onToggleStatus?: (product: Product) => void
  isLoading?: boolean
}

export const ProductList: React.FC<ProductListProps> = ({
  products,
  onEdit,
  onDelete,
  onToggleStatus,
  isLoading = false,
}) => {
  const getStatusBadge = (status: ProductStatus) => {
    const variants = {
      [ProductStatus.ACTIVE]: 'success',
      [ProductStatus.INACTIVE]: 'secondary',
      [ProductStatus.DISCONTINUED]: 'destructive',
    } as const

    const labels = {
      [ProductStatus.ACTIVE]: 'Active',
      [ProductStatus.INACTIVE]: 'Inactive',
      [ProductStatus.DISCONTINUED]: 'Discontinued',
    }

    return (
      <Badge variant={variants[status] || 'secondary'}>
        {labels[status]}
      </Badge>
    )
  }

  if (isLoading) {
    return (
      <div className="border rounded-lg">
        <div className="p-8 text-center text-gray-500">
          <div className="animate-spin h-8 w-8 border-4 border-primary border-t-transparent rounded-full mx-auto mb-2"></div>
          Loading products...
        </div>
      </div>
    )
  }

  if (products.length === 0) {
    return (
      <div className="border rounded-lg">
        <div className="p-8 text-center text-gray-500">
          <Package className="h-12 w-12 mx-auto mb-2 text-gray-400" />
          <p className="text-lg font-medium mb-1">No products found</p>
          {/* <p className="text-sm">Create your first product to get started</p> */}
        </div>
      </div>
    )
  }

  return (
    <div className="border rounded-lg overflow-hidden">
      <Table>
        <TableHeader>
          <TableRow>
            <TableHead>Product Name</TableHead>
            <TableHead>SKU</TableHead>
            <TableHead>Category</TableHead>
            <TableHead>Barcode</TableHead>
            <TableHead className="text-center">Stock</TableHead>
            <TableHead>Status</TableHead>
            <TableHead className="w-[50px]"></TableHead>
          </TableRow>
        </TableHeader>
        <TableBody>
          {products.map((product) => (
            <TableRow key={product.id}>
              <TableCell className="font-medium">
                <Link
                  to={`/products/${product.id}`}
                  className="hover:text-primary hover:underline"
                >
                  {product.name}
                </Link>
                {product.description && (
                  <p className="text-sm text-gray-500 truncate max-w-xs">
                    {product.description}
                  </p>
                )}
              </TableCell>
              <TableCell className="font-mono text-sm">{product.sku}</TableCell>
              <TableCell>
                <Badge variant="outline">{product.categoryName}</Badge>
              </TableCell>
              <TableCell className="font-mono text-sm">{product.barcode}</TableCell>
              <TableCell className="text-center">
                <div className="text-sm">
                  {product.availableStock !== undefined ? (
                    <span className={product.availableStock <= 0 ? "text-red-600 font-medium" : product.hasLowStock ? "text-orange-600 font-medium" : ""}>
                      {product.availableStock}
                    </span>
                  ) : (
                    <span className="text-gray-400">-</span>
                  )}
                </div>
              </TableCell>
              <TableCell>{getStatusBadge(product.status)}</TableCell>
              <TableCell>
                <DropdownMenu>
                  <DropdownMenuTrigger asChild>
                    <Button variant="ghost" size="sm">
                      <MoreVertical className="h-4 w-4" />
                    </Button>
                  </DropdownMenuTrigger>
                  <DropdownMenuContent align="end">
                    <DropdownMenuItem asChild>
                      <Link to={`/products/${product.id}`}>
                        <Eye className="h-4 w-4 mr-2" />
                        View Details
                      </Link>
                    </DropdownMenuItem>
                    {onEdit && (
                      <DropdownMenuItem onClick={() => onEdit(product)}>
                        <Edit className="h-4 w-4 mr-2" />
                        Edit
                      </DropdownMenuItem>
                    )}
                    {onToggleStatus && (
                      <>
                        <DropdownMenuSeparator />
                        <DropdownMenuItem
                          onClick={() => onToggleStatus(product)}
                        >
                          {product.status === ProductStatus.ACTIVE ? (
                            <>
                              <PowerOff className="h-4 w-4 mr-2" />
                              Deactivate
                            </>
                          ) : (
                            <>
                              <Power className="h-4 w-4 mr-2" />
                              Activate
                            </>
                          )}
                        </DropdownMenuItem>
                      </>
                    )}
                    {onDelete && (
                      <>
                        <DropdownMenuSeparator />
                        <DropdownMenuItem
                          onClick={() => onDelete(product)}
                          className="text-red-600"
                        >
                          <Trash2 className="h-4 w-4 mr-2" />
                          Delete
                        </DropdownMenuItem>
                      </>
                    )}
                  </DropdownMenuContent>
                </DropdownMenu>
              </TableCell>
            </TableRow>
          ))}
        </TableBody>
      </Table>
    </div>
  );
}
