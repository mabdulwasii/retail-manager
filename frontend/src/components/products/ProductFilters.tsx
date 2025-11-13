import React from 'react'
import { Input } from '@/components/ui/input'
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/components/ui/select'
import { Button } from '@/components/ui/button'
import { Search, X } from 'lucide-react'
import { ProductStatus } from '@/types/api'
import { useCategories } from '@/hooks/useCategories'

export interface ProductFilterValues {
  search: string
  categoryId: string
  status: string
}

interface ProductFiltersProps {
  filters: ProductFilterValues
  onFiltersChange: (filters: ProductFilterValues) => void
  onClear: () => void
}

export const ProductFilters: React.FC<ProductFiltersProps> = ({
  filters,
  onFiltersChange,
  onClear,
}) => {
  const { data: categories = [], isLoading: categoriesLoading } = useCategories(false)

  const handleSearchChange = (value: string) => {
    onFiltersChange({ ...filters, search: value })
  }

  const handleCategoryChange = (value: string) => {
    onFiltersChange({ ...filters, categoryId: value === 'all' ? '' : value })
  }

  const handleStatusChange = (value: string) => {
    onFiltersChange({ ...filters, status: value === 'all' ? '' : value })
  }

  const hasActiveFilters =
    filters.search || filters.categoryId || filters.status

  return (
    <div className="flex flex-col sm:flex-row gap-3 items-start sm:items-center">
      {/* Search Input */}
      <div className="relative flex-1 w-full sm:w-auto">
        <Search className="absolute left-3 top-1/2 -translate-y-1/2 h-4 w-4 text-gray-400" />
        <Input
          placeholder="Search by name, SKU, or barcode..."
          value={filters.search}
          onChange={(e) => handleSearchChange(e.target.value)}
          className="pl-9"
        />
      </div>

      {/* Category Filter */}
      <Select
        value={filters.categoryId || 'all'}
        onValueChange={handleCategoryChange}
        disabled={categoriesLoading}
      >
        <SelectTrigger className="w-full sm:w-[180px]">
          <SelectValue placeholder="Category" />
        </SelectTrigger>
        <SelectContent>
          <SelectItem value="all">All Categories</SelectItem>
          {categories
            .map((category) => (
              <SelectItem key={category.id} value={category.id}>
                {category.name}
              </SelectItem>
            ))}
        </SelectContent>
      </Select>

      {/* Status Filter */}
      <Select
        value={filters.status || 'all'}
        onValueChange={handleStatusChange}
      >
        <SelectTrigger className="w-full sm:w-[150px]">
          <SelectValue placeholder="Status" />
        </SelectTrigger>
        <SelectContent>
          <SelectItem value="all">All Status</SelectItem>
          <SelectItem value={ProductStatus.ACTIVE}>Active</SelectItem>
          <SelectItem value={ProductStatus.INACTIVE}>Inactive</SelectItem>
          <SelectItem value={ProductStatus.DISCONTINUED}>Discontinued</SelectItem>
        </SelectContent>
      </Select>

      {/* Clear Filters Button */}
      {hasActiveFilters && (
        <Button
          variant="outline"
          size="icon"
          onClick={onClear}
          title="Clear filters"
        >
          <X className="h-4 w-4" />
        </Button>
      )}
    </div>
  )
}
