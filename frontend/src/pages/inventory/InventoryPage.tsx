import React from 'react'
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card'

export const InventoryPage: React.FC = () => {
  return (
    <div className="space-y-6">
      <h1 className="text-3xl font-bold">Inventory</h1>
      <Card>
        <CardHeader>
          <CardTitle>Inventory Management</CardTitle>
          <CardDescription>Stock levels, reservations, and inventory tracking</CardDescription>
        </CardHeader>
        <CardContent>
          <p>Inventory management implementation coming soon...</p>
        </CardContent>
      </Card>
    </div>
  )
}