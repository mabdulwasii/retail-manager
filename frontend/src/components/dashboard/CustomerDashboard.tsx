import React from 'react'
import { useAuth } from '@/context/ManualAuthContext'
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card'
import { Button } from '@/components/ui/button'
import {
  ShoppingBag,
  Store,
  Info
} from 'lucide-react'
import { Link } from 'react-router-dom'

export const CustomerDashboard: React.FC = () => {
  const { user } = useAuth()







  return (
    <div className="space-y-6">
      <div className="flex justify-center items-center min-h-[400px]">
        <Card className="max-w-2xl w-full">
          <CardHeader className="text-center">
            <div className="flex justify-center mb-4">
              <div className="p-4 bg-blue-100 rounded-full">
                <ShoppingBag className="h-12 w-12 text-blue-600" />
              </div>
            </div>
            <CardTitle className="text-2xl">Customer Portal</CardTitle>
            <CardDescription>
              Welcome, {user?.firstName || user?.username}!
            </CardDescription>
          </CardHeader>
          <CardContent className="space-y-6">
            <div className="p-4 bg-blue-50 border border-blue-200 rounded-lg">
              <div className="flex items-start space-x-3">
                <Info className="h-5 w-5 text-blue-600 mt-0.5" />
                <div>
                  <p className="text-sm font-medium text-blue-900">
                    Customer Features Coming Soon
                  </p>
                  <p className="text-sm text-blue-700 mt-1">
                    We're building exciting features for customers including order tracking, 
                    loyalty rewards, and personalized shopping experiences.
                  </p>
                </div>
              </div>
            </div>

            <div className="space-y-4">
              <h3 className="font-semibold text-lg">Available Actions</h3>
              <div className="grid gap-3">
                <Button variant="outline" className="h-auto py-4 justify-start" asChild>
                  <Link to="/shops">
                    <Store className="mr-3 h-5 w-5" />
                    <div className="text-left">
                      <div className="font-medium">Browse Shops</div>
                      <div className="text-sm text-muted-foreground">Explore available retail locations</div>
                    </div>
                  </Link>
                </Button>
                <Button variant="outline" className="h-auto py-4 justify-start" asChild>
                  <Link to="/products">
                    <ShoppingBag className="mr-3 h-5 w-5" />
                    <div className="text-left">
                      <div className="font-medium">View Products</div>
                      <div className="text-sm text-muted-foreground">Browse available products</div>
                    </div>
                  </Link>
                </Button>
              </div>
            </div>

            <div className="pt-4 border-t text-center text-sm text-muted-foreground">
              <p>For assistance, please contact your shop administrator</p>
            </div>
          </CardContent>
        </Card>
      </div>
    </div>
  )
}