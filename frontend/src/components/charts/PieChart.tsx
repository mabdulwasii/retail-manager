import React from 'react'
import {
  PieChart as RechartsPieChart,
  Pie,
  Cell,
  Tooltip,
  Legend,
  ResponsiveContainer
} from 'recharts'
import { useCurrency } from '@/hooks/useCurrency'

interface DataPoint {
  [key: string]: any
}

interface PieChartProps {
  data: DataPoint[]
  colors?: string[]
  height?: number
  title?: string
  currency?: boolean
  showLabels?: boolean
  showLegend?: boolean
  innerRadius?: number
  outerRadius?: number
  labelKey?: string
  valueKey?: string
}

export const PieChart: React.FC<PieChartProps> = ({
  data,
  colors = ['#3b82f6', '#ef4444', '#10b981', '#f59e0b', '#8b5cf6', '#f97316', '#06b6d4', '#84cc16'],
  height = 300,
  title,
  currency = false,
  showLabels = true,
  showLegend = true,
  innerRadius = 0,
  outerRadius = 100,
  labelKey = 'name',
  valueKey = 'value'
}) => {
  const { formatCurrency } = useCurrency()

  const formatTooltipValue = (value: any, name: string) => {
    if (currency && typeof value === 'number') {
      return [formatCurrency(value), name]
    }
    return [value, name]
  }

  const formatLabel = (entry: any) => {
    if (!showLabels) return ''

    const percent = ((entry.value / data.reduce((sum, item) => sum + item[valueKey], 0)) * 100).toFixed(1)
    return `${entry[labelKey]} (${percent}%)`
  }

  return (
    <div className="w-full">
      {title && (
        <h3 className="text-lg font-semibold text-gray-900 mb-4">{title}</h3>
      )}
      <ResponsiveContainer width="100%" height={height}>
        <RechartsPieChart>
          <Pie
            data={data}
            cx="50%"
            cy="50%"
            labelLine={false}
            label={showLabels ? formatLabel : false}
            outerRadius={outerRadius}
            innerRadius={innerRadius}
            fill="#8884d8"
            dataKey={valueKey}
          >
            {data.map((entry, index) => (
              <Cell key={`cell-${index}`} fill={colors[index % colors.length]} />
            ))}
          </Pie>
          <Tooltip
            contentStyle={{
              backgroundColor: '#ffffff',
              border: '1px solid #e5e7eb',
              borderRadius: '6px',
              boxShadow: '0 4px 6px -1px rgba(0, 0, 0, 0.1)'
            }}
            formatter={formatTooltipValue}
          />
          {showLegend && <Legend />}
        </RechartsPieChart>
      </ResponsiveContainer>
    </div>
  )
}