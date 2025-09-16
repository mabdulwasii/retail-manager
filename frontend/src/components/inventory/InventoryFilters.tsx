import React, { useState } from 'react'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import { Label } from '@/components/ui/label'
import { InventoryFilter, InventoryStatus } from '@/hooks/useInventory'
import { FilterIcon, SearchIcon, XIcon } from 'lucide-react'

interface InventoryFiltersProps {
  onFilterChange: (filter: InventoryFilter) => void
  currentFilter: InventoryFilter
}

export const InventoryFilters: React.FC<InventoryFiltersProps> = ({
  onFilterChange,
  currentFilter
}) => {
  const [localFilter, setLocalFilter] = useState<InventoryFilter>(currentFilter)
  const [isExpanded, setIsExpanded] = useState(false)

  const handleFilterChange = (key: keyof InventoryFilter, value: any) => {
    const newFilter = { ...localFilter, [key]: value }
    setLocalFilter(newFilter)
  }

  const applyFilters = () => {
    onFilterChange(localFilter)
  }

  const clearFilters = () => {
    const emptyFilter: InventoryFilter = {}
    setLocalFilter(emptyFilter)
    onFilterChange(emptyFilter)
  }

  const hasActiveFilters = Object.keys(localFilter).some(key => {
    const value = localFilter[key as keyof InventoryFilter]
    return value !== undefined && value !== '' && value !== null
  })

  return (
    <Card>
      <CardHeader>
        <div className="flex items-center justify-between">
          <CardTitle className="flex items-center space-x-2">
            <FilterIcon className="h-5 w-5" />
            <span>Filters</span>
            {hasActiveFilters && (
              <span className="bg-blue-100 text-blue-800 text-xs font-medium px-2 py-1 rounded-full">
                {Object.keys(localFilter).filter(key => localFilter[key as keyof InventoryFilter]).length}
              </span>
            )}
          </CardTitle>
          <Button
            variant="ghost"
            size="sm"
            onClick={() => setIsExpanded(!isExpanded)}
          >
            {isExpanded ? 'Collapse' : 'Expand'}
          </Button>
        </div>
      </CardHeader>
      <CardContent className="space-y-4">
        {/* Search - Always visible */}
        <div className="space-y-2">
          <Label htmlFor="search">Search Products</Label>
          <div className="relative">
            <SearchIcon className="absolute left-3 top-3 h-4 w-4 text-gray-400" />
            <Input
              id="search"
              type="text"
              placeholder="Search by name, SKU, or description..."
              value={localFilter.searchQuery || ''}
              onChange={(e) => handleFilterChange('searchQuery', e.target.value)}
              className="pl-10"
            />
          </div>
        </div>

        {/* Expandable Filters */}
        {isExpanded && (
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4 pt-4 border-t">
            {/* Status Filter */}
            <div className="space-y-2">
              <Label htmlFor="status">Status</Label>
              <select
                id="status"
                value={localFilter.status || ''}
                onChange={(e) => handleFilterChange('status', e.target.value as InventoryStatus || undefined)}
                className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
              >
                <option value="">All Status</option>
                <option value="ACTIVE">Active</option>
                <option value="INACTIVE">Inactive</option>
                <option value="DISCONTINUED">Discontinued</option>
                <option value="QUARANTINED">Quarantined</option>
                <option value="EXPIRED">Expired</option>
              </select>
            </div>

            {/* Category Filter */}
            <div className="space-y-2">
              <Label htmlFor="category">Category</Label>
              <Input
                id="category"
                type="text"
                placeholder="Filter by category..."
                value={localFilter.category || ''}
                onChange={(e) => handleFilterChange('category', e.target.value)}
              />
            </div>

            {/* Location Filter */}
            <div className="space-y-2">
              <Label htmlFor="location">Location</Label>
              <Input
                id="location"
                type="text"
                placeholder="Filter by location..."
                value={localFilter.location || ''}
                onChange={(e) => handleFilterChange('location', e.target.value)}
              />
            </div>

            {/* Stock Range */}
            <div className="space-y-2">
              <Label>Stock Range</Label>
              <div className="flex space-x-2">
                <Input
                  type="number"
                  placeholder="Min"
                  value={localFilter.minStock || ''}
                  onChange={(e) => handleFilterChange('minStock', e.target.value ? parseInt(e.target.value) : undefined)}
                  className="w-full"
                />
                <Input
                  type="number"
                  placeholder="Max"
                  value={localFilter.maxStock || ''}
                  onChange={(e) => handleFilterChange('maxStock', e.target.value ? parseInt(e.target.value) : undefined)}
                  className="w-full"
                />
              </div>
            </div>
          </div>
        )}

        {/* Alert Checkboxes - Always visible when expanded */}
        {isExpanded && (
          <div className="grid grid-cols-1 md:grid-cols-3 gap-4 pt-4 border-t">
            <div className="flex items-center space-x-2">
              <input
                id="lowStock"
                type="checkbox"
                checked={localFilter.isLowStock || false}
                onChange={(e) => handleFilterChange('isLowStock', e.target.checked || undefined)}
                className="rounded border-gray-300 text-blue-600 focus:ring-blue-500"
              />
              <Label htmlFor="lowStock" className="text-sm">
                Low Stock Only
              </Label>
            </div>

            <div className="flex items-center space-x-2">
              <input
                id="expired"
                type="checkbox"
                checked={localFilter.isExpired || false}
                onChange={(e) => handleFilterChange('isExpired', e.target.checked || undefined)}
                className="rounded border-gray-300 text-blue-600 focus:ring-blue-500"
              />
              <Label htmlFor="expired" className="text-sm">
                Expired Items Only
              </Label>
            </div>

            <div className="flex items-center space-x-2">
              <input
                id="expiringSoon"
                type="checkbox"
                checked={localFilter.isExpiringSoon || false}
                onChange={(e) => handleFilterChange('isExpiringSoon', e.target.checked || undefined)}
                className="rounded border-gray-300 text-blue-600 focus:ring-blue-500"
              />
              <Label htmlFor="expiringSoon" className="text-sm">
                Expiring Soon Only
              </Label>
            </div>
          </div>
        )}

        {/* Action Buttons */}
        <div className="flex space-x-2 pt-4 border-t">
          <Button onClick={applyFilters} className="flex-1">
            <FilterIcon className="h-4 w-4 mr-2" />
            Apply Filters
          </Button>
          {hasActiveFilters && (
            <Button variant="outline" onClick={clearFilters}>
              <XIcon className="h-4 w-4 mr-2" />
              Clear
            </Button>
          )}
        </div>
      </CardContent>
    </Card>
  )
}

export default InventoryFilters