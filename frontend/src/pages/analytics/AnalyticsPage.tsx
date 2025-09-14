import React from 'react'
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card'

export const AnalyticsPage: React.FC = () => {
  return (
    <div className="space-y-6">
      <h1 className="text-3xl font-bold">Analytics</h1>
      <Card>
        <CardHeader>
          <CardTitle>Business Analytics</CardTitle>
          <CardDescription>Sales analytics, ROI reports, and business insights</CardDescription>
        </CardHeader>
        <CardContent>
          <p>Analytics dashboard implementation coming soon...</p>
        </CardContent>
      </Card>
    </div>
  )
}