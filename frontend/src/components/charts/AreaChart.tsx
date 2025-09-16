import React from 'react'
import {
  AreaChart as RechartsAreaChart,
  Area,
  XAxis,
  YAxis,
  CartesianGrid,
  Tooltip,
  Legend,
  ResponsiveContainer
} from 'recharts'
import { useCurrency } from '@/hooks/useCurrency'

interface DataPoint {
  name: string
  value: number
  [key: string]: any
}

interface AreaChartProps {
  data: DataPoint[]
  dataKeys: string[]
  colors?: string[]
  height?: number
  title?: string
  currency?: boolean
  xAxisKey?: string
  showGrid?: boolean
  showLegend?: boolean
  stacked?: boolean
  fillOpacity?: number
}

export const AreaChart: React.FC<AreaChartProps> = ({
  data,
  dataKeys,
  colors = ['#3b82f6', '#ef4444', '#10b981', '#f59e0b'],
  height = 300,
  title,
  currency = false,
  xAxisKey = 'name',
  showGrid = true,
  showLegend = true,
  stacked = false,
  fillOpacity = 0.3
}) => {
  const { formatCurrency } = useCurrency()

  const formatTooltipValue = (value: any, name: string) => {
    if (currency && typeof value === 'number') {
      return [formatCurrency(value), name]
    }
    return [value, name]
  }

  const formatYAxisTick = (value: any) => {
    if (currency && typeof value === 'number') {
      return formatCurrency(value)
    }
    return value
  }

  return (
    <div className="w-full">
      {title && (
        <h3 className="text-lg font-semibold text-gray-900 mb-4">{title}</h3>
      )}
      <ResponsiveContainer width="100%" height={height}>
        <RechartsAreaChart data={data} margin={{ top: 5, right: 30, left: 20, bottom: 5 }}>
          {showGrid && <CartesianGrid strokeDasharray="3 3" stroke="#e5e7eb" />}
          <XAxis
            dataKey={xAxisKey}
            stroke="#6b7280"
            fontSize={12}
            tickLine={false}
          />
          <YAxis
            stroke="#6b7280"
            fontSize={12}
            tickLine={false}
            tickFormatter={formatYAxisTick}
          />
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
          {dataKeys.map((key, index) => (
            <Area
              key={key}
              type="monotone"
              dataKey={key}
              stackId={stacked ? 'stack' : undefined}
              stroke={colors[index % colors.length]}
              fill={colors[index % colors.length]}
              fillOpacity={fillOpacity}
            />
          ))}
        </RechartsAreaChart>
      </ResponsiveContainer>
    </div>
  )
}