import React, { useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { useTranslation } from 'react-i18next'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import { Label } from '@/components/ui/label'
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card'
import { Alert, AlertDescription } from '@/components/ui/alert'
import { Store, TrendingUp, ArrowLeft, ShieldCheck, Loader2 } from 'lucide-react'
import { useAuth } from '@/context/ManualAuthContext'

export const LoginPage: React.FC = () => {
  const navigate = useNavigate()
  const { t } = useTranslation()
  const { initializeKeycloak } = useAuth()
  const [username, setUsername] = useState('')
  const [isLoading, setIsLoading] = useState(false)
  const [error, setError] = useState<string | null>(null)

  const handleLogin = async (e: React.FormEvent) => {
    e.preventDefault()
    setIsLoading(true)
    setError(null)

    try {
      // Initialize Keycloak first
      const kc = await initializeKeycloak()

      // Trigger Keycloak login with optional username hint
      const loginOptions: any = {
        redirectUri: window.location.origin + '/dashboard',
      }

      if (username) {
        loginOptions.loginHint = username
      }

      await kc.login(loginOptions)
    } catch (err: any) {
      console.error('Login error:', err)
      setError(t('errors.failedToInitializeLogin'))
      setIsLoading(false)
    }
  }

  const features = [
    {
      icon: Store,
      title: t('features.multiShopManagement.title'),
      description: t('features.multiShopManagement.description'),
    },
    {
      icon: ShieldCheck,
      title: t('features.secureAuthentication.title'),
      description: t('features.secureAuthentication.description'),
    },
    {
      icon: TrendingUp,
      title: t('features.investmentTracking.title'),
      description: t('features.investmentTracking.description'),
    },
  ]

  // Test credentials info
  const testCredentials = [
    { role: 'Admin', username: 'admin@shopmanager.com', password: 'admin123' },
    { role: 'Manager', username: 'manager@shopmanager.com', password: 'manager123' },
    { role: 'Employee', username: 'employee@shopmanager.com', password: 'employee123' },
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

        <div className="grid md:grid-cols-2 gap-8 items-start">
          {/* Login Card */}
          <Card className="w-full">
            <CardHeader className="space-y-1">
              <CardTitle className="text-2xl">Welcome back</CardTitle>
              <CardDescription>
                Continue to secure login with Keycloak
              </CardDescription>
            </CardHeader>
            <CardContent>
              <form onSubmit={handleLogin} className="space-y-4">
                {error && (
                  <Alert variant="destructive">
                    <AlertDescription>{error}</AlertDescription>
                  </Alert>
                )}

                <div className="space-y-2">
                  <Label htmlFor="username">Email (Optional)</Label>
                  <Input
                    id="username"
                    type="email"
                    placeholder="admin@shopmanager.com"
                    value={username}
                    onChange={(e) => setUsername(e.target.value)}
                    disabled={isLoading}
                  />
                  <p className="text-xs text-gray-500">
                    Enter your email to pre-fill the Keycloak login form
                  </p>
                </div>

                <Button
                  type="submit"
                  className="w-full bg-blue-600 hover:bg-blue-700"
                  size="lg"
                  disabled={isLoading}
                >
                  {isLoading ? (
                    <>
                      <Loader2 className="h-4 w-4 mr-2 animate-spin" />
                      Redirecting to Keycloak...
                    </>
                  ) : (
                    'Continue to Login'
                  )}
                </Button>

                <div className="text-center">
                  <p className="text-sm text-gray-600">
                    Don't have an account?{' '}
                    <Link to="/register" className="text-blue-600 hover:text-blue-500 font-medium">
                      Sign up
                    </Link>
                  </p>
                </div>
              </form>

              {/* Test Credentials Info */}
              <div className="mt-6 pt-6 border-t">
                <p className="text-xs text-gray-500 mb-2 font-semibold">Test Credentials for Keycloak:</p>
                <div className="space-y-1">
                  {testCredentials.map((cred) => (
                    <button
                      key={cred.username}
                      type="button"
                      onClick={() => {
                        setUsername(cred.username)
                      }}
                      className="text-xs text-blue-600 hover:text-blue-500 block w-full text-left hover:bg-blue-50 p-1 rounded"
                    >
                      <span className="font-weight: 500">{cred.role}:</span> {cred.username} / {cred.password}
                    </button>
                  ))}
                </div>
                <p className="text-xs text-gray-400 mt-2">
                  Click to pre-fill email, then enter password on Keycloak login page
                </p>
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

            <div className="bg-blue-50 rounded-lg p-4 mt-6">
              <h4 className="text-sm font-semibold text-blue-900 mb-2">Available Roles:</h4>
              <ul className="text-xs text-blue-800 space-y-1">
                <li>• <strong>Admin:</strong> Full system access</li>
                <li>• <strong>Manager:</strong> Shop operations management</li>
                <li>• <strong>Employee:</strong> Sales and inventory</li>
                <li>• <strong>Investor:</strong> Investment analytics</li>
                <li>• <strong>Customer:</strong> Purchase history</li>
              </ul>
            </div>

            <div className="bg-green-50 rounded-lg p-4 mt-4 border border-green-200">
              <h4 className="text-xs font-semibold text-green-900 mb-1">Secure OAuth2 Login:</h4>
              <ol className="text-xs text-green-800 space-y-1">
                <li>1. Click "Continue to Login" below</li>
                <li>2. You'll be redirected to our custom Keycloak login page</li>
                <li>3. Enter your credentials on the secure login form</li>
                <li>4. Return to Shop Manager dashboard automatically</li>
              </ol>
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