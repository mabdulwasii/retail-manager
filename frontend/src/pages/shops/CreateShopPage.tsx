import React from 'react'
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card'

export const CreateShopPage: React.FC = () => {
  return (
    <div className="space-y-6">
      <h1 className="text-3xl font-bold">Create New Shop</h1>
      <Card>
        <CardHeader>
          <CardTitle>Shop Information</CardTitle>
          <CardDescription>Create a new shop with basic information</CardDescription>
        </CardHeader>
        <CardContent>
          <p>Shop creation form implementation coming soon...</p>
        </CardContent>
      </Card>
    </div>
  )
}