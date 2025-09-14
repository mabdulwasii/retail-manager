import React from 'react'
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card'

export const SalesPage: React.FC = () => {
  return (
    <div className="space-y-6">
      <h1 className="text-3xl font-bold">Sales</h1>
      <Card>
        <CardHeader>
          <CardTitle>Sales Management</CardTitle>
          <CardDescription>Process sales transactions and manage orders</CardDescription>
        </CardHeader>
        <CardContent>
          <p>Sales management implementation coming soon...</p>
        </CardContent>
      </Card>
    </div>
  )
}