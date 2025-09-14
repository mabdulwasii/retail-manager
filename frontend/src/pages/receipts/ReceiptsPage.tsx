import React from 'react'
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card'

export const ReceiptsPage: React.FC = () => {
  return (
    <div className="space-y-6">
      <h1 className="text-3xl font-bold">Receipts</h1>
      <Card>
        <CardHeader>
          <CardTitle>Receipt Management</CardTitle>
          <CardDescription>Generate, print, and email receipts</CardDescription>
        </CardHeader>
        <CardContent>
          <p>Receipt management implementation coming soon...</p>
        </CardContent>
      </Card>
    </div>
  )
}