import React from 'react'
import { useParams } from 'react-router-dom'
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card'

export const ShopDetailPage: React.FC = () => {
  const { shopId } = useParams()

  return (
    <div className="space-y-6">
      <h1 className="text-3xl font-bold">Shop Details</h1>
      <Card>
        <CardHeader>
          <CardTitle>Shop ID: {shopId}</CardTitle>
          <CardDescription>Detailed shop information and management</CardDescription>
        </CardHeader>
        <CardContent>
          <p>Shop detail implementation coming soon...</p>
        </CardContent>
      </Card>
    </div>
  )
}