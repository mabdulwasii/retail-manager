import React, { useEffect } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { useAuth } from '@/context/KeycloakAuthContext'
import { Button } from '@/components/ui/button'
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card'
import {Store, TrendingUp, ArrowLeft, ShieldCheck} from 'lucide-react'

export const LoginPage: React.FC = () => {
  const { login, isAuthenticated } = useAuth()
  const navigate = useNavigate()

  // Redirect if already authenticated
  useEffect(() => {
    if (isAuthenticated) {
      navigate('/dashboard')
    }
  }, [isAuthenticated, navigate])

  const handleLogin = async () => {
    // Configure seamless redirect back to dashboard
    await login({
      redirectUri: window.location.origin + '/dashboard'
    })
  }

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
        {/* Header with back to home link */}
        <div className="text-center">
          <div className="flex items-center justify-center mb-4">
            <Button
              variant="ghost"
              size="sm"
              onClick={() => navigate('/')}
              className="absolute left-4 top-4"
            >
              <ArrowLeft className="h-4 w-4 mr-2" />
              Back to Home
            </Button>
            <Store className="h-12 w-12 text-blue-600" />
          </div>
          <h1 className="text-4xl font-bold text-gray-900 mb-2">Shop Manager</h1>
          <p className="text-lg text-gray-600">
            Sign in to access your retail management platform
          </p>
        </div>

        <div className="grid md:grid-cols-2 gap-8 items-center">
          {/* Login Card */}
          <Card className="w-full">
            <CardHeader className="space-y-1">
              <CardTitle className="text-2xl">Welcome back</CardTitle>
              <CardDescription>
                Enter your credentials to access your account
              </CardDescription>
            </CardHeader>
            <CardContent className="space-y-4">
              {/* Login Button */}
              <Button
                onClick={handleLogin}
                className="w-full bg-blue-600 hover:bg-blue-700"
                size="lg"
              >
                Sign In
              </Button>

              <p className="text-xs text-center text-gray-500">
                Secure authentication with automatic redirect.
              </p>

              <div className="text-center">
                <p className="text-sm text-gray-600">
                  Don't have an account?{' '}
                  <Link to="/register" className="text-blue-600 hover:text-blue-500 font-medium">
                    Sign up
                  </Link>
                </p>
              </div>

              <div className="text-xs text-center text-gray-500">
                Secure authentication with enterprise-grade security
              </div>
            </CardContent>
          </Card>

          {/* Features */}
          <div className="space-y-6">
            <h3 className="text-xl font-semibold text-gray-900">Why Choose Shop Manager?</h3>
            <div className="space-y-4">
              {features.map((feature) => (
                <div key={feature.title} className="flex items-start space-x-3">
                  <feature.icon className="h-6 w-6 text-blue-600 mt-1" />
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