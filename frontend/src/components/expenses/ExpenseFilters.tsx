import React, { useState } from 'react'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import { Label } from '@/components/ui/label'
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select'
import { ExpenseFilter, ExpenseStatus } from '@/hooks/useExpenses'
import { FilterIcon, XIcon } from 'lucide-react'

interface ExpenseFiltersProps {
  onFilterChange: (filter: ExpenseFilter) => void
  initialFilter?: ExpenseFilter
}

export const ExpenseFilters: React.FC<ExpenseFiltersProps> = ({
  onFilterChange,
  initialFilter = {},
}) => {
  const [filter, setFilter] = useState<ExpenseFilter>(initialFilter)
  const [isExpanded, setIsExpanded] = useState(false)

  const handleFilterUpdate = (key: keyof ExpenseFilter, value: any) => {
    const newFilter = { ...filter, [key]: value }
    setFilter(newFilter)
  }

  const applyFilters = () => {
    onFilterChange(filter)
  }

  const clearFilters = () => {
    const emptyFilter: ExpenseFilter = {}
    setFilter(emptyFilter)
    onFilterChange(emptyFilter)
  }

  const hasActiveFilters = Object.values(filter).some(value =>
    value !== undefined && value !== '' && value !== null
  )

  return (
    <Card>
      <CardHeader className="cursor-pointer" onClick={() => setIsExpanded(!isExpanded)}>
        <div className="flex items-center justify-between">
          <div className="flex items-center gap-2">
            <FilterIcon className="h-4 w-4" />
            <CardTitle className="text-lg">Filters</CardTitle>
            {hasActiveFilters && (
              <span className="text-xs bg-blue-100 text-blue-800 px-2 py-1 rounded-full">
                Active
              </span>
            )}
          </div>
          <Button variant="ghost" size="sm">
            {isExpanded ? '▲' : '▼'}
          </Button>
        </div>
      </CardHeader>

      {isExpanded && (
        <CardContent className="space-y-4">
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
            {/* Status Filter */}
            <div>
              <Label htmlFor="status">Status</Label>
              <Select
                value={filter.status || ''}
                onValueChange={(value) => handleFilterUpdate('status', value || undefined)}
              >
                <SelectTrigger id="status">
                  <SelectValue placeholder="All statuses" />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value="__all__">All statuses</SelectItem>
                  <SelectItem value="DRAFT">Draft</SelectItem>
                  <SelectItem value="PENDING_APPROVAL">Pending Approval</SelectItem>
                  <SelectItem value="APPROVED">Approved</SelectItem>
                  <SelectItem value="REJECTED">Rejected</SelectItem>
                  <SelectItem value="PAID">Paid</SelectItem>
                </SelectContent>
              </Select>
            </div>

            {/* Start Date */}
            <div>
              <Label htmlFor="startDate">Start Date</Label>
              <Input
                id="startDate"
                type="date"
                value={filter.startDate || ''}
                onChange={(e) => handleFilterUpdate('startDate', e.target.value || undefined)}
              />
            </div>

            {/* End Date */}
            <div>
              <Label htmlFor="endDate">End Date</Label>
              <Input
                id="endDate"
                type="date"
                value={filter.endDate || ''}
                onChange={(e) => handleFilterUpdate('endDate', e.target.value || undefined)}
              />
            </div>

            {/* Min Amount */}
            <div>
              <Label htmlFor="minAmount">Min Amount</Label>
              <Input
                id="minAmount"
                type="number"
                placeholder="0.00"
                min={0}
                step={0.01}
                value={filter.minAmount ?? ''}
                onChange={(e) => handleFilterUpdate('minAmount', e.target.value ? parseFloat(e.target.value) : undefined)}
              />
            </div>

            {/* Max Amount */}
            <div>
              <Label htmlFor="maxAmount">Max Amount</Label>
              <Input
                id="maxAmount"
                type="number"
                placeholder="999999.99"
                min={0}
                step={0.01}
                value={filter.maxAmount ?? ''}
                onChange={(e) => handleFilterUpdate('maxAmount', e.target.value ? parseFloat(e.target.value) : undefined)}
              />
            </div>

            {/* Category Filter (placeholder - would need category API) */}
            <div>
              <Label htmlFor="categoryId">Category</Label>
              <Select
                value={filter.categoryId || ''}
                onValueChange={(value) => handleFilterUpdate('categoryId', value || undefined)}
              >
                <SelectTrigger id="categoryId">
                  <SelectValue placeholder="All categories" />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value="__all__">All categories</SelectItem>
                  <SelectItem value="cat1">Office Supplies</SelectItem>
                  <SelectItem value="cat2">Utilities</SelectItem>
                  <SelectItem value="cat3">Marketing</SelectItem>
                  <SelectItem value="cat4">Inventory Purchase</SelectItem>
                  <SelectItem value="cat5">Equipment</SelectItem>
                  <SelectItem value="cat6">Maintenance</SelectItem>
                  <SelectItem value="cat7">Salaries</SelectItem>
                  <SelectItem value="cat8">Transportation</SelectItem>
                  <SelectItem value="cat9">Other</SelectItem>
                </SelectContent>
              </Select>
            </div>
          </div>

          {/* Action Buttons */}
          <div className="flex justify-end gap-2 pt-4 border-t">
            <Button
              type="button"
              variant="outline"
              onClick={clearFilters}
              disabled={!hasActiveFilters}
            >
              <XIcon className="h-4 w-4 mr-2" />
              Clear Filters
            </Button>
            <Button type="button" onClick={applyFilters}>
              <FilterIcon className="h-4 w-4 mr-2" />
              Apply Filters
            </Button>
          </div>
        </CardContent>
      )}
    </Card>
  )
}
