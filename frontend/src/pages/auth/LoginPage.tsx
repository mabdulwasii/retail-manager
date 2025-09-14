import React from 'react'
import { useAuth } from '@/context/AuthContext'
import { Button } from '@/components/ui/button'
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card'
import { Store, ShieldCheck, TrendingUp } from 'lucide-react'

export const LoginPage: React.FC = () => {
  const { login } = useAuth()

  const features = [
    {
      icon: Store,
      title: 'Multi-Shop Management',
      description: 'Manage multiple shops from a single platform with tenant isolation',
    },
    {
      icon: ShieldCheck,
      title: 'Secure Authentication',
      description: 'Role-based access control with Keycloak SSO integration',
    },
    {
      icon: TrendingUp,
      title: 'Investment Tracking',
      description: 'Track investments, ROI, and profit sharing with detailed analytics',
    },
  ]

  return (
    <div className="min-h-screen flex items-center justify-center bg-gray-50 py-12 px-4 sm:px-6 lg:px-8">
      <div className="max-w-4xl w-full space-y-8">
        <div className="text-center">
          <h1 className="text-4xl font-bold text-gray-900 mb-2">Shop Manager</h1>
          <p className="text-lg text-gray-600">
            Comprehensive retail management platform for shops, investments, and analytics
          </p>
        </div>

        <div className="grid md:grid-cols-2 gap-8 items-center">
          {/* Login Card */}
          <Card className="w-full">
            <CardHeader className="space-y-1">
              <CardTitle className="text-2xl">Welcome back</CardTitle>
              <CardDescription>
                Sign in to your account to access the Shop Manager platform
              </CardDescription>
            </CardHeader>
            <CardContent className="space-y-4">
              <Button
                onClick={login}
                className="w-full"
                size="lg"
              >
                Sign in with Keycloak
              </Button>
              <div className="text-sm text-center text-gray-600">
                Secure authentication powered by Keycloak SSO
              </div>
            </CardContent>
          </Card>

          {/* Features */}
          <div className="space-y-6">
            <h3 className="text-xl font-semibold text-gray-900">Platform Features</h3>
            <div className="space-y-4">
              {features.map((feature, index) => (
                <div key={index} className="flex items-start space-x-3">
                  <feature.icon className="h-6 w-6 text-primary mt-1" />
                  <div>
                    <h4 className="font-medium text-gray-900">{feature.title}</h4>
                    <p className="text-sm text-gray-600">{feature.description}</p>
                  </div>
                </div>
              ))}
            </div>
          </div>
        </div>

        <div className="text-center text-sm text-gray-500">
          Shop Manager v1.0.0 - Multi-tenant retail management platform
        </div>
      </div>
    </div>
  )
}