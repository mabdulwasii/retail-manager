import React, { useState } from 'react'
import { useAuth } from '@/context/ManualAuthContext'
import { useCurrency } from '@/hooks/useCurrency'
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card'
import { Button } from '@/components/ui/button'
import {
  ShoppingBag,
  Receipt,
  Star,
  Heart,
  Gift,
  CreditCard,
  MapPin,
  Clock,
  Tag,
  TrendingUp,
  Package,
  RefreshCw
} from 'lucide-react'
import { Link } from 'react-router-dom'

interface Purchase {
  id: string
  shop: string
  items: string[]
  total: number
  date: string
  status: 'completed' | 'processing' | 'refunded'
  trackingNumber?: string
}

interface Recommendation {
  id: string
  name: string
  shop: string
  price: number
  image: string
  rating: number
  discount?: number
  category: string
}

interface LoyaltyReward {
  id: string
  title: string
  description: string
  pointsRequired: number
  expiryDate: string
  category: 'discount' | 'gift' | 'voucher'
}

export const CustomerDashboard: React.FC = () => {
  const { user } = useAuth()
  const { formatCurrency } = useCurrency()
  const [isRefreshing, setIsRefreshing] = useState(false)

  const handleRefresh = () => {
    setIsRefreshing(true)
    setTimeout(() => setIsRefreshing(false), 1000)
  }

  const customerStats = [
    {
      title: 'Total Purchases',
      value: '47',
      description: 'This year',
      icon: ShoppingBag,
      color: 'text-blue-600'
    },
    {
      title: 'Total Spent',
      value: formatCurrency(125750),
      description: 'All time',
      icon: CreditCard,
      color: 'text-green-600'
    },
    {
      title: 'Loyalty Points',
      value: '2,340',
      description: 'Available to redeem',
      icon: Star,
      color: 'text-purple-600'
    },
    {
      title: 'Saved Amount',
      value: formatCurrency(12500),
      description: 'From discounts',
      icon: Tag,
      color: 'text-orange-600'
    }
  ]

  const recentPurchases: Purchase[] = [
    {
      id: 'P-2024-001',
      shop: 'Downtown Electronics',
      items: ['iPhone 15 Pro', 'AirPods Pro'],
      total: 425000,
      date: new Date(Date.now() - 7 * 24 * 60 * 60000).toISOString().split('T')[0],
      status: 'completed',
      trackingNumber: 'TRK-2024-12345'
    },
    {
      id: 'P-2024-002',
      shop: 'Fashion Forward',
      items: ['Designer Jeans', 'Cotton T-Shirt'],
      total: 35000,
      date: new Date(Date.now() - 12 * 24 * 60 * 60000).toISOString().split('T')[0],
      status: 'completed'
    },
    {
      id: 'P-2024-003',
      shop: 'Grocery Express',
      items: ['Fresh Groceries', 'Organic Produce'],
      total: 18500,
      date: new Date(Date.now() - 15 * 24 * 60 * 60000).toISOString().split('T')[0],
      status: 'processing',
      trackingNumber: 'TRK-2024-67890'
    }
  ]

  const recommendations: Recommendation[] = [
    {
      id: 'R-001',
      name: 'Wireless Gaming Mouse',
      shop: 'Tech Central',
      price: 2999,
      image: '/placeholder.jpg',
      rating: 4.5,
      discount: 20,
      category: 'Electronics'
    },
    {
      id: 'R-002',
      name: 'Running Shoes',
      shop: 'Sports World',
      price: 8999,
      image: '/placeholder.jpg',
      rating: 4.8,
      category: 'Sports'
    },
    {
      id: 'R-003',
      name: 'Coffee Maker',
      shop: 'Home Essentials',
      price: 15999,
      image: '/placeholder.jpg',
      rating: 4.3,
      discount: 15,
      category: 'Home'
    }
  ]

  const loyaltyRewards: LoyaltyReward[] = [
    {
      id: 'RW-001',
      title: '10% Off Next Purchase',
      description: 'Get 10% discount on your next order',
      pointsRequired: 1000,
      expiryDate: new Date(Date.now() + 30 * 24 * 60 * 60000).toISOString().split('T')[0],
      category: 'discount'
    },
    {
      id: 'RW-002',
      title: 'Free Shipping',
      description: 'Free delivery on orders over $50',
      pointsRequired: 500,
      expiryDate: new Date(Date.now() + 60 * 24 * 60 * 60000).toISOString().split('T')[0],
      category: 'voucher'
    },
    {
      id: 'RW-003',
      title: 'Gift Voucher',
      description: '$25 gift voucher for any shop',
      pointsRequired: 2000,
      expiryDate: new Date(Date.now() + 90 * 24 * 60 * 60000).toISOString().split('T')[0],
      category: 'gift'
    }
  ]

  const favoriteShops = [
    {
      name: 'Downtown Electronics',
      visits: 12,
      spent: 245000,
      category: 'Electronics'
    },
    {
      name: 'Fashion Forward',
      visits: 8,
      spent: 85000,
      category: 'Fashion'
    },
    {
      name: 'Grocery Express',
      visits: 15,
      spent: 125000,
      category: 'Grocery'
    }
  ]

  const quickActions = [
    {
      title: 'Browse Products',
      description: 'Discover new items',
      icon: ShoppingBag,
      href: '/products',
      color: 'bg-blue-500 hover:bg-blue-600'
    },
    {
      title: 'My Orders',
      description: 'Track purchases',
      icon: Receipt,
      href: '/orders',
      color: 'bg-green-500 hover:bg-green-600'
    },
    {
      title: 'Wishlist',
      description: 'Saved items',
      icon: Heart,
      href: '/wishlist',
      color: 'bg-red-500 hover:bg-red-600'
    },
    {
      title: 'Redeem Points',
      description: 'Use loyalty points',
      icon: Gift,
      href: '/rewards',
      color: 'bg-purple-500 hover:bg-purple-600'
    }
  ]

  return (
    <div className="space-y-6">
      {/* Welcome Header */}
      <div className="flex justify-between items-start">
        <div>
          <h1 className="text-3xl font-bold text-gray-900">
            Welcome back, {user?.firstName || user?.username}!
          </h1>
          <p className="text-muted-foreground">
            Discover great products and track your shopping journey.
          </p>
        </div>
        <Button variant="outline" onClick={handleRefresh} disabled={isRefreshing}>
          <RefreshCw className={`mr-2 h-4 w-4 ${isRefreshing ? 'animate-spin' : ''}`} />
          Refresh
        </Button>
      </div>

      {/* Customer Stats */}
      <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-4">
        {customerStats.map((stat, index) => (
          <Card key={index} className="hover:shadow-md transition-shadow">
            <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
              <CardTitle className="text-sm font-medium">
                {stat.title}
              </CardTitle>
              <stat.icon className={`h-4 w-4 ${stat.color}`} />
            </CardHeader>
            <CardContent>
              <div className="text-2xl font-bold">{stat.value}</div>
              <p className="text-xs text-muted-foreground">
                {stat.description}
              </p>
            </CardContent>
          </Card>
        ))}
      </div>

      {/* Quick Actions */}
      <Card>
        <CardHeader>
          <CardTitle>Quick Actions</CardTitle>
          <CardDescription>What would you like to do today?</CardDescription>
        </CardHeader>
        <CardContent>
          <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
            {quickActions.map((action, index) => (
              <Button
                key={index}
                variant="outline"
                className="h-24 flex-col space-y-2 p-4"
                asChild
              >
                <Link to={action.href}>
                  <action.icon className="h-8 w-8" />
                  <div className="text-center">
                    <div className="font-medium text-sm">{action.title}</div>
                    <div className="text-xs text-muted-foreground">
                      {action.description}
                    </div>
                  </div>
                </Link>
              </Button>
            ))}
          </div>
        </CardContent>
      </Card>

      <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-3">
        {/* Recent Purchases */}
        <Card className="col-span-2">
          <CardHeader>
            <CardTitle>Recent Purchases</CardTitle>
            <CardDescription>
              Your latest shopping activity
            </CardDescription>
          </CardHeader>
          <CardContent>
            <div className="space-y-4">
              {recentPurchases.map((purchase) => (
                <div key={purchase.id} className="flex items-center justify-between p-4 border rounded-lg">
                  <div className="flex items-center space-x-4">
                    <div className={`w-3 h-3 rounded-full ${
                      purchase.status === 'completed' ? 'bg-green-500' :
                      purchase.status === 'processing' ? 'bg-blue-500' :
                      'bg-red-500'
                    }`}></div>
                    <div>
                      <p className="font-medium">{purchase.shop}</p>
                      <p className="text-sm text-muted-foreground">
                        {purchase.items.join(', ')}
                      </p>
                      <p className="text-xs text-muted-foreground">
                        {new Date(purchase.date).toLocaleDateString()} • {purchase.status}
                      </p>
                    </div>
                  </div>
                  <div className="text-right">
                    <p className="font-semibold">{formatCurrency(purchase.total)}</p>
                    <Button size="sm" variant="outline" asChild>
                      <Link to={`/orders/${purchase.id}`}>View</Link>
                    </Button>
                  </div>
                </div>
              ))}
            </div>
            <Button variant="outline" className="w-full mt-4" asChild>
              <Link to="/orders">View All Orders</Link>
            </Button>
          </CardContent>
        </Card>

        {/* Loyalty Status */}
        <Card>
          <CardHeader>
            <CardTitle className="flex items-center space-x-2">
              <Star className="h-5 w-5 text-yellow-500" />
              <span>Loyalty Status</span>
            </CardTitle>
            <CardDescription>
              Your membership benefits
            </CardDescription>
          </CardHeader>
          <CardContent>
            <div className="space-y-4">
              <div className="text-center p-4 bg-gradient-to-r from-yellow-50 to-orange-50 rounded-lg border">
                <div className="text-2xl font-bold text-yellow-600">Gold Member</div>
                <div className="text-sm text-muted-foreground">Level 3 of 5</div>
              </div>

              <div className="space-y-2">
                <div className="flex justify-between text-sm">
                  <span>Progress to Platinum</span>
                  <span>2,340 / 5,000 points</span>
                </div>
                <div className="w-full bg-gray-200 rounded-full h-2">
                  <div
                    className="bg-yellow-500 h-2 rounded-full"
                    style={{ width: '47%' }}
                  ></div>
                </div>
              </div>

              <div className="space-y-2">
                <h4 className="font-medium">Your Benefits:</h4>
                <ul className="text-sm space-y-1">
                  <li>• 5% cashback on all purchases</li>
                  <li>• Free shipping on orders over {formatCurrency(50000)}</li>
                  <li>• Early access to sales</li>
                  <li>• Birthday bonus points</li>
                </ul>
              </div>
            </div>
          </CardContent>
        </Card>
      </div>

      {/* Recommendations */}
      <Card>
        <CardHeader>
          <CardTitle>Recommended for You</CardTitle>
          <CardDescription>
            Personalized product suggestions based on your shopping history
          </CardDescription>
        </CardHeader>
        <CardContent>
          <div className="grid md:grid-cols-3 gap-4">
            {recommendations.map((item) => (
              <div key={item.id} className="border rounded-lg p-4 hover:shadow-md transition-shadow">
                <div className="aspect-square bg-gray-100 rounded-lg mb-3 flex items-center justify-center">
                  <ShoppingBag className="h-12 w-12 text-gray-400" />
                </div>
                <h4 className="font-medium mb-1">{item.name}</h4>
                <p className="text-sm text-muted-foreground mb-2">{item.shop}</p>
                <div className="flex items-center space-x-1 mb-2">
                  {[...Array(5)].map((_, i) => (
                    <Star
                      key={i}
                      className={`h-3 w-3 ${
                        i < Math.floor(item.rating) ? 'text-yellow-400 fill-current' : 'text-gray-300'
                      }`}
                    />
                  ))}
                  <span className="text-xs text-muted-foreground">({item.rating})</span>
                </div>
                <div className="flex items-center justify-between">
                  <div>
                    {item.discount ? (
                      <div className="space-y-1">
                        <div className="flex items-center space-x-2">
                          <span className="font-semibold">{formatCurrency(item.price * (1 - item.discount / 100))}</span>
                          <span className="text-xs bg-red-100 text-red-800 px-1 rounded">
                            -{item.discount}%
                          </span>
                        </div>
                        <div className="text-xs text-muted-foreground line-through">
                          {formatCurrency(item.price)}
                        </div>
                      </div>
                    ) : (
                      <span className="font-semibold">{formatCurrency(item.price)}</span>
                    )}
                  </div>
                  <Button size="sm">Add to Cart</Button>
                </div>
              </div>
            ))}
          </div>
        </CardContent>
      </Card>

      {/* Favorite Shops */}
      <Card>
        <CardHeader>
          <CardTitle>Your Favorite Shops</CardTitle>
          <CardDescription>
            Shops you visit most frequently
          </CardDescription>
        </CardHeader>
        <CardContent>
          <div className="grid md:grid-cols-3 gap-4">
            {favoriteShops.map((shop, index) => (
              <div key={index} className="p-4 bg-gray-50 rounded-lg">
                <div className="flex items-center justify-between mb-2">
                  <h4 className="font-medium">{shop.name}</h4>
                  <span className="text-xs bg-blue-100 text-blue-800 px-2 py-1 rounded">
                    {shop.category}
                  </span>
                </div>
                <div className="space-y-1 text-sm">
                  <div className="flex justify-between">
                    <span className="text-muted-foreground">Visits:</span>
                    <span>{shop.visits} times</span>
                  </div>
                  <div className="flex justify-between">
                    <span className="text-muted-foreground">Total spent:</span>
                    <span className="font-semibold">{formatCurrency(shop.spent)}</span>
                  </div>
                </div>
                <Button size="sm" variant="outline" className="w-full mt-3" asChild>
                  <Link to={`/shops/${shop.name.toLowerCase().replace(/\s+/g, '-')}`}>
                    Visit Shop
                  </Link>
                </Button>
              </div>
            ))}
          </div>
        </CardContent>
      </Card>
    </div>
  )
}