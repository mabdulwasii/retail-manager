import React, { useState } from 'react'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import { Label } from '@/components/ui/label'
import { DateRange } from '@/hooks/useAnalytics'
import {
  CalendarIcon,
  FilterIcon,
  RefreshCwIcon,
  XIcon
} from 'lucide-react'

interface AnalyticsFiltersProps {
  dateRange: DateRange
  onDateRangeChange: (dateRange: DateRange) => void
  onApplyFilters: () => void
  onClearFilters: () => void
  isLoading?: boolean
}

export const AnalyticsFilters: React.FC<AnalyticsFiltersProps> = ({
  dateRange,
  onDateRangeChange,
  onApplyFilters,
  onClearFilters,
  isLoading = false
}) => {
  const [localDateRange, setLocalDateRange] = useState<DateRange>(dateRange)

  const handleDateChange = (field: keyof DateRange, value: string) => {
    const newDateRange = { ...localDateRange, [field]: value }
    setLocalDateRange(newDateRange)
  }

  const applyFilters = () => {
    onDateRangeChange(localDateRange)
    onApplyFilters()
  }

  const clearFilters = () => {
    const now = new Date()
    const thirtyDaysAgo = new Date(now.getTime() - 30 * 24 * 60 * 60 * 1000)

    const defaultRange: DateRange = {
      startDate: thirtyDaysAgo.toISOString().split('T')[0] + 'T00:00:00',
      endDate: now.toISOString().split('T')[0] + 'T23:59:59'
    }

    setLocalDateRange(defaultRange)
    onDateRangeChange(defaultRange)
    onClearFilters()
  }

  const setQuickRange = (days: number) => {
    const now = new Date()
    const startDate = new Date(now.getTime() - days * 24 * 60 * 60 * 1000)

    const quickRange: DateRange = {
      startDate: startDate.toISOString().split('T')[0] + 'T00:00:00',
      endDate: now.toISOString().split('T')[0] + 'T23:59:59'
    }

    setLocalDateRange(quickRange)
  }

  const formatDateForInput = (dateString: string): string => {
    return dateString.split('T')[0]
  }

  const hasChanges = () => {
    return localDateRange.startDate !== dateRange.startDate ||
           localDateRange.endDate !== dateRange.endDate
  }

  return (
    <Card>
      <CardHeader>
        <CardTitle className="flex items-center space-x-2">
          <FilterIcon className="h-5 w-5" />
          <span>Analytics Filters</span>
        </CardTitle>
      </CardHeader>
      <CardContent className="space-y-4">
        {/* Quick Date Range Buttons */}
        <div>
          <Label className="text-sm font-medium text-gray-700">Quick Ranges</Label>
          <div className="grid grid-cols-2 md:grid-cols-4 gap-2 mt-2">
            <Button
              variant="outline"
              size="sm"
              onClick={() => setQuickRange(7)}
              className="text-xs"
            >
              Last 7 days
            </Button>
            <Button
              variant="outline"
              size="sm"
              onClick={() => setQuickRange(30)}
              className="text-xs"
            >
              Last 30 days
            </Button>
            <Button
              variant="outline"
              size="sm"
              onClick={() => setQuickRange(90)}
              className="text-xs"
            >
              Last 90 days
            </Button>
            <Button
              variant="outline"
              size="sm"
              onClick={() => setQuickRange(365)}
              className="text-xs"
            >
              Last year
            </Button>
          </div>
        </div>

        {/* Custom Date Range */}
        <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
          <div className="space-y-2">
            <Label htmlFor="startDate">Start Date</Label>
            <div className="relative">
              <CalendarIcon className="absolute left-3 top-3 h-4 w-4 text-gray-400" />
              <Input
                id="startDate"
                type="date"
                value={formatDateForInput(localDateRange.startDate)}
                onChange={(e) => handleDateChange('startDate', e.target.value + 'T00:00:00')}
                className="pl-10"
              />
            </div>
          </div>

          <div className="space-y-2">
            <Label htmlFor="endDate">End Date</Label>
            <div className="relative">
              <CalendarIcon className="absolute left-3 top-3 h-4 w-4 text-gray-400" />
              <Input
                id="endDate"
                type="date"
                value={formatDateForInput(localDateRange.endDate)}
                onChange={(e) => handleDateChange('endDate', e.target.value + 'T23:59:59')}
                className="pl-10"
              />
            </div>
          </div>
        </div>

        {/* Filter Actions */}
        <div className="flex space-x-2 pt-4 border-t">
          <Button
            onClick={applyFilters}
            disabled={isLoading || !hasChanges()}
            className="flex-1"
          >
            <RefreshCwIcon className={`h-4 w-4 mr-2 ${isLoading ? 'animate-spin' : ''}`} />
            Apply Filters
          </Button>
          <Button
            variant="outline"
            onClick={clearFilters}
            disabled={isLoading}
          >
            <XIcon className="h-4 w-4 mr-2" />
            Clear
          </Button>
        </div>

        {/* Current Selection Display */}
        <div className="text-xs text-gray-500 pt-2 border-t">
          <p>
            Selected period: {new Date(localDateRange.startDate).toLocaleDateString()} - {new Date(localDateRange.endDate).toLocaleDateString()}
          </p>
          {hasChanges() && (
            <p className="text-blue-600 mt-1">
              Changes pending - click "Apply Filters" to update
            </p>
          )}
        </div>
      </CardContent>
    </Card>
  )
}