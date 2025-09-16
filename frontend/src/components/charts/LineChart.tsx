import React from 'react'
import {
  LineChart as RechartsLineChart,
  Line,
  XAxis,
  YAxis,
  CartesianGrid,
  Tooltip,
  Legend,
  ResponsiveContainer
} from 'recharts'
import { useCurrency } from '@/hooks/useCurrency'

interface DataPoint {
  [key: string]: any
}

interface LineChartProps {
  data: DataPoint[]
  dataKeys: string[]
  colors?: string[]
  height?: number
  title?: string
  currency?: boolean
  xAxisKey?: string
  showGrid?: boolean
  showLegend?: boolean
  strokeWidth?: number
}

export const LineChart: React.FC<LineChartProps> = ({
  data,
  dataKeys,
  colors = ['#3b82f6', '#ef4444', '#10b981', '#f59e0b'],
  height = 300,
  title,
  currency = false,
  xAxisKey = 'name',
  showGrid = true,
  showLegend = true,
  strokeWidth = 2
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
        <RechartsLineChart data={data} margin={{ top: 5, right: 30, left: 20, bottom: 5 }}>
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
            <Line
              key={key}
              type="monotone"
              dataKey={key}
              stroke={colors[index % colors.length]}
              strokeWidth={strokeWidth}
              dot={{ r: 4 }}
              activeDot={{ r: 6 }}
            />
          ))}
        </RechartsLineChart>
      </ResponsiveContainer>
    </div>
  )
}