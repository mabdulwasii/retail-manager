import React, { useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { useAuth } from '@/context/AuthContext'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import { Label } from '@/components/ui/label'
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card'
import { Alert, AlertDescription } from '@/components/ui/alert'
import { Store, ShieldCheck, TrendingUp, ArrowLeft, Eye, EyeOff } from 'lucide-react'

export const LoginPage: React.FC = () => {
  const { login } = useAuth()
  const navigate = useNavigate()
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [showPassword, setShowPassword] = useState(false)
  const [isLoading, setIsLoading] = useState(false)
  const [error, setError] = useState('')
  const [showKeycloakLogin, setShowKeycloakLogin] = useState(false)

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    setIsLoading(true)
    setError('')

    try {
      // For now, redirect to Keycloak since we're using SSO
      // In a real implementation, you might validate credentials first
      await login()
    } catch (err) {
      setError('Login failed. Please check your credentials and try again.')
      console.error('Login error:', err)
    } finally {
      setIsLoading(false)
    }
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
              {error && (
                <Alert variant="destructive">
                  <AlertDescription>{error}</AlertDescription>
                </Alert>
              )}

              {!showKeycloakLogin ? (
                <>
                  {/* Custom Login Form */}
                  <form onSubmit={handleSubmit} className="space-y-4">
                    <div className="space-y-2">
                      <Label htmlFor="email">Email</Label>
                      <Input
                        id="email"
                        type="email"
                        placeholder="Enter your email"
                        value={email}
                        onChange={(e) => setEmail(e.target.value)}
                        required
                        disabled={isLoading}
                      />
                    </div>

                    <div className="space-y-2">
                      <Label htmlFor="password">Password</Label>
                      <div className="relative">
                        <Input
                          id="password"
                          type={showPassword ? "text" : "password"}
                          placeholder="Enter your password"
                          value={password}
                          onChange={(e) => setPassword(e.target.value)}
                          required
                          disabled={isLoading}
                        />
                        <Button
                          type="button"
                          variant="ghost"
                          size="sm"
                          className="absolute right-0 top-0 h-full px-3 py-2 hover:bg-transparent"
                          onClick={() => setShowPassword(!showPassword)}
                        >
                          {showPassword ? (
                            <EyeOff className="h-4 w-4" />
                          ) : (
                            <Eye className="h-4 w-4" />
                          )}
                        </Button>
                      </div>
                    </div>

                    <div className="flex items-center justify-between">
                      <div className="text-sm">
                        <Link to="/forgot-password" className="text-blue-600 hover:text-blue-500">
                          Forgot your password?
                        </Link>
                      </div>
                    </div>

                    <Button
                      type="submit"
                      className="w-full"
                      size="lg"
                      disabled={isLoading}
                    >
                      {isLoading ? 'Signing in...' : 'Sign in'}
                    </Button>
                  </form>

                  <div className="relative">
                    <div className="absolute inset-0 flex items-center">
                      <span className="w-full border-t" />
                    </div>
                    <div className="relative flex justify-center text-xs uppercase">
                      <span className="bg-white px-2 text-muted-foreground">Or</span>
                    </div>
                  </div>

                  <Button
                    variant="outline"
                    className="w-full"
                    onClick={() => setShowKeycloakLogin(true)}
                  >
                    <ShieldCheck className="mr-2 h-4 w-4" />
                    Sign in with SSO
                  </Button>
                </>
              ) : (
                <>
                  {/* Keycloak SSO Login */}
                  <Button
                    onClick={login}
                    className="w-full"
                    size="lg"
                    disabled={isLoading}
                  >
                    {isLoading ? 'Redirecting...' : 'Continue with Keycloak SSO'}
                  </Button>

                  <Button
                    variant="ghost"
                    className="w-full"
                    onClick={() => setShowKeycloakLogin(false)}
                  >
                    <ArrowLeft className="mr-2 h-4 w-4" />
                    Back to login form
                  </Button>
                </>
              )}

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
              {features.map((feature, index) => (
                <div key={index} className="flex items-start space-x-3">
                  <feature.icon className="h-6 w-6 text-blue-600 mt-1" />
                  <div>
                    <h4 className="font-medium text-gray-900">{feature.title}</h4>
                    <p className="text-sm text-gray-600">{feature.description}</p>
                  </div>
                </div>
              ))}
            </div>

            {/* Demo Users Info */}
            <div className="bg-blue-50 p-4 rounded-lg">
              <h4 className="font-medium text-blue-900 mb-2">Demo Users</h4>
              <div className="text-sm text-blue-800 space-y-1">
                <div><strong>Admin:</strong> admin@shopmanager.com / admin123</div>
                <div><strong>Manager:</strong> manager@shopmanager.com / manager123</div>
                <div><strong>Employee:</strong> employee@shopmanager.com / employee123</div>
              </div>
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