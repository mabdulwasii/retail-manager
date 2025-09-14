import React from 'react'
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card'

export const InvestmentsPage: React.FC = () => {
  return (
    <div className="space-y-6">
      <h1 className="text-3xl font-bold">Investments</h1>
      <Card>
        <CardHeader>
          <CardTitle>Investment Portfolio</CardTitle>
          <CardDescription>Track investments, ROI, and profit sharing</CardDescription>
        </CardHeader>
        <CardContent>
          <p>Investment management implementation coming soon...</p>
        </CardContent>
      </Card>
    </div>
  )
}