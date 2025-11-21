import React from 'react'
import {
  LineChart,
  Line,
  XAxis,
  YAxis,
  CartesianGrid,
  Tooltip,
  Legend,
  ResponsiveContainer,
  Area,
  AreaChart,
} from 'recharts'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import type { ROIChartData } from '@/types/investment'

interface ROIChartProps {
  investmentId: string
  data: ROIChartData[]
  height?: number
  showExpected?: boolean
  showCumulative?: boolean
  className?: string
}

export function ROIChart({
  investmentId,
  data,
  height = 300,
  showExpected = true,
  showCumulative = true,
  className = '',
}: ROIChartProps) {
  if (!data || data.length === 0) {
    return (
      <Card className={className}>
        <CardHeader>
          <CardTitle>Returns Over Time</CardTitle>
        </CardHeader>
        <CardContent>
          <div className="flex items-center justify-center h-[300px] text-muted-foreground">
            <p>No data available yet</p>
          </div>
        </CardContent>
      </Card>
    )
  }

  const formatCurrency = (value: number) => {
    return new Intl.NumberFormat('en-US', {
      style: 'currency',
      currency: 'USD',
      minimumFractionDigits: 0,
      maximumFractionDigits: 0,
    }).format(value)
  }

  const formatDate = (dateString: string) => {
    try {
      const date = new Date(dateString)
      return date.toLocaleDateString('en-US', { month: 'short', year: 'numeric' })
    } catch {
      return dateString
    }
  }

  const CustomTooltip = ({ active, payload, label }: any) => {
    if (!active || !payload || !payload.length) return null

    return (
      <div className="bg-white border border-gray-200 rounded-lg shadow-lg p-3">
        <p className="font-semibold text-sm mb-2">{formatDate(label)}</p>
        {payload.map((entry: any, index: number) => (
          <div key={index} className="flex items-center gap-2 text-xs">
            <div
              className="w-3 h-3 rounded-full"
              style={{ backgroundColor: entry.color }}
            />
            <span className="text-gray-600">{entry.name}:</span>
            <span className="font-medium">
              {entry.name.includes('ROI')
                ? `${entry.value.toFixed(2)}%`
                : formatCurrency(entry.value)}
            </span>
          </div>
        ))}
      </div>
    )
  }

  return (
    <Card className={className}>
      <CardHeader>
        <CardTitle>Returns Over Time</CardTitle>
      </CardHeader>
      <CardContent>
        <ResponsiveContainer width="100%" height={height}>
          <LineChart data={data} margin={{ top: 5, right: 30, left: 20, bottom: 5 }}>
            <CartesianGrid strokeDasharray="3 3" stroke="#f0f0f0" />
            <XAxis
              dataKey="date"
              tickFormatter={formatDate}
              stroke="#888"
              style={{ fontSize: '12px' }}
            />
            <YAxis
              yAxisId="left"
              stroke="#888"
              style={{ fontSize: '12px' }}
              tickFormatter={(value) => `$${value / 1000}k`}
            />
            {showCumulative && (
              <YAxis
                yAxisId="right"
                orientation="right"
                stroke="#888"
                style={{ fontSize: '12px' }}
                tickFormatter={(value) => `${value}%`}
              />
            )}
            <Tooltip content={<CustomTooltip />} />
            <Legend
              wrapperStyle={{ fontSize: '12px' }}
              iconType="line"
            />

            {/* Actual Returns */}
            <Line
              yAxisId="left"
              type="monotone"
              dataKey="actualReturn"
              stroke="#10b981"
              strokeWidth={2}
              name="Actual Returns"
              dot={{ r: 4, fill: '#10b981' }}
              activeDot={{ r: 6 }}
            />

            {/* Expected Returns */}
            {showExpected && (
              <Line
                yAxisId="left"
                type="monotone"
                dataKey="expectedReturn"
                stroke="#94a3b8"
                strokeWidth={2}
                strokeDasharray="5 5"
                name="Expected Returns"
                dot={false}
              />
            )}

            {/* Cumulative ROI */}
            {showCumulative && (
              <Line
                yAxisId="right"
                type="monotone"
                dataKey="cumulativeROI"
                stroke="#3b82f6"
                strokeWidth={2}
                name="Cumulative ROI %"
                dot={{ r: 3, fill: '#3b82f6' }}
              />
            )}
          </LineChart>
        </ResponsiveContainer>

        {/* Legend */}
        <div className="mt-4 grid grid-cols-3 gap-4 text-xs text-center border-t pt-4">
          <div>
            <div className="flex items-center justify-center gap-1 mb-1">
              <div className="w-3 h-3 rounded-full bg-green-500" />
              <span className="font-medium">Actual Returns</span>
            </div>
            <p className="text-muted-foreground">Monthly profit earned</p>
          </div>
          {showExpected && (
            <div>
              <div className="flex items-center justify-center gap-1 mb-1">
                <div className="w-3 h-0.5 bg-gray-400" style={{ width: '16px' }} />
                <span className="font-medium">Expected Returns</span>
              </div>
              <p className="text-muted-foreground">Projected performance</p>
            </div>
          )}
          {showCumulative && (
            <div>
              <div className="flex items-center justify-center gap-1 mb-1">
                <div className="w-3 h-3 rounded-full bg-blue-500" />
                <span className="font-medium">Cumulative ROI</span>
              </div>
              <p className="text-muted-foreground">Total return percentage</p>
            </div>
          )}
        </div>
      </CardContent>
    </Card>
  )
}
