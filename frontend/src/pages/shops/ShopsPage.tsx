import React from 'react'
import { Link } from 'react-router-dom'
import { Button } from '@/components/ui/button'
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card'
import { Plus, Store, MapPin, Calendar } from 'lucide-react'

export const ShopsPage: React.FC = () => {
  // Mock data - in real app, this would come from API
  const shops = [
    {
      id: '1',
      name: 'Electronics Store',
      description: 'Premium electronics and gadgets',
      address: '123 Tech Street, Silicon Valley',
      status: 'ACTIVE',
      openingDate: '2023-01-15',
    },
    {
      id: '2',
      name: 'Grocery Market',
      description: 'Fresh groceries and daily essentials',
      address: '456 Market Ave, Downtown',
      status: 'ACTIVE',
      openingDate: '2023-03-20',
    },
    {
      id: '3',
      name: 'Fashion Boutique',
      description: 'Trendy clothing and accessories',
      address: '789 Fashion Blvd, Mall District',
      status: 'INACTIVE',
      openingDate: '2023-06-10',
    },
  ]

  const getStatusColor = (status: string) => {
    switch (status) {
      case 'ACTIVE':
        return 'text-green-600 bg-green-100'
      case 'INACTIVE':
        return 'text-yellow-600 bg-yellow-100'
      case 'SUSPENDED':
        return 'text-red-600 bg-red-100'
      default:
        return 'text-gray-600 bg-gray-100'
    }
  }

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex justify-between items-center">
        <div>
          <h1 className="text-3xl font-bold">Shops</h1>
          <p className="text-muted-foreground">
            Manage your retail locations and shop settings
          </p>
        </div>
        <Link to="/shops/create">
          <Button>
            <Plus className="mr-2 h-4 w-4" />
            Create Shop
          </Button>
        </Link>
      </div>

      {/* Stats */}
      <div className="grid gap-4 md:grid-cols-3">
        <Card>
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium">Total Shops</CardTitle>
            <Store className="h-4 w-4 text-muted-foreground" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold">{shops.length}</div>
          </CardContent>
        </Card>
        <Card>
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium">Active</CardTitle>
            <Store className="h-4 w-4 text-green-600" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold text-green-600">
              {shops.filter(shop => shop.status === 'ACTIVE').length}
            </div>
          </CardContent>
        </Card>
        <Card>
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium">Inactive</CardTitle>
            <Store className="h-4 w-4 text-yellow-600" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold text-yellow-600">
              {shops.filter(shop => shop.status === 'INACTIVE').length}
            </div>
          </CardContent>
        </Card>
      </div>

      {/* Shops Grid */}
      <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-3">
        {shops.map((shop) => (
          <Card key={shop.id} className="hover:shadow-md transition-shadow">
            <CardHeader>
              <div className="flex justify-between items-start">
                <CardTitle className="text-lg">{shop.name}</CardTitle>
                <span className={`px-2 py-1 rounded-full text-xs font-medium ${getStatusColor(shop.status)}`}>
                  {shop.status}
                </span>
              </div>
              <CardDescription>{shop.description}</CardDescription>
            </CardHeader>
            <CardContent className="space-y-3">
              <div className="flex items-center text-sm text-muted-foreground">
                <MapPin className="mr-2 h-4 w-4" />
                {shop.address}
              </div>
              <div className="flex items-center text-sm text-muted-foreground">
                <Calendar className="mr-2 h-4 w-4" />
                Opened: {new Date(shop.openingDate).toLocaleDateString()}
              </div>
              <div className="flex space-x-2 pt-2">
                <Link to={`/shops/${shop.id}`} className="flex-1">
                  <Button variant="outline" className="w-full">
                    View Details
                  </Button>
                </Link>
                <Button variant="outline" size="sm">
                  Settings
                </Button>
              </div>
            </CardContent>
          </Card>
        ))}
      </div>
    </div>
  )
}