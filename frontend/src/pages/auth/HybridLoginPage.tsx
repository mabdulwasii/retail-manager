import React, { useState, useEffect } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { useAuth } from '@/context/HybridAuthContext'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import { Label } from '@/components/ui/label'
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card'
import { Alert, AlertDescription } from '@/components/ui/alert'
import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/components/ui/tabs'
import { Store, ShieldCheck, TrendingUp, ArrowLeft, Eye, EyeOff, User, Key } from 'lucide-react'

export const HybridLoginPage: React.FC = () => {
  const { loginWithCredentials, loginWithSSO, isAuthenticated } = useAuth()
  const navigate = useNavigate()
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [showPassword, setShowPassword] = useState(false)
  const [isLoading, setIsLoading] = useState(false)
  const [error, setError] = useState('')

  // Redirect if already authenticated
  useEffect(() => {
    if (isAuthenticated) {
      navigate('/dashboard')
    }
  }, [isAuthenticated, navigate])

  const handleEmbeddedLogin = async (e: React.FormEvent) => {
    e.preventDefault()
    setError('')
    setIsLoading(true)

    if (!email || !password) {
      setError('Please enter both email and password.')
      setIsLoading(false)
      return
    }

    try {
      const success = await loginWithCredentials(email, password)
      if (success) {
        navigate('/dashboard')
      } else {
        setError('Invalid email or password. Please try again.')
      }
    } catch (err) {
      setError('Login failed. Please check your credentials and try again.')
      console.error('Login error:', err)
    } finally {
      setIsLoading(false)
    }
  }

  const handleSSOLogin = async () => {
    setIsLoading(true)
    try {
      await loginWithSSO()
    } catch (error) {
      console.error('SSO Login error:', error)
      setError('SSO login failed. Please try again.')
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
      description: 'Role-based access control with multiple authentication options',
    },
    {
      icon: TrendingUp,
      title: 'Investment Tracking',
      description: 'Track investments, ROI, and profit sharing with detailed analytics',
    },
  ]

  // SECURITY WARNING: Test accounts for development only
  // In production, users must be created through Keycloak administration
  // These credentials are disabled in production environments
  const testAccounts = process.env.NODE_ENV === 'development' ? [
    { email: 'admin@shopmanager.com', password: 'DevAdmin@2024!Test', role: 'Admin' },
    { email: 'manager@shopmanager.com', password: 'DevManager@2024!Test', role: 'Manager' },
    { email: 'employee@shopmanager.com', password: 'DevEmployee@2024!Test', role: 'Employee' },
    { email: 'investor@shopmanager.com', password: 'DevInvestor@2024!Test', role: 'Investor' },
    { email: 'customer@shopmanager.com', password: 'DevCustomer@2024!Test', role: 'Customer' },
  ] : []

  return (
    <div className=\"min-h-screen flex items-center justify-center bg-gray-50 py-12 px-4 sm:px-6 lg:px-8\">
      <div className=\"max-w-5xl w-full space-y-8\">
        {/* Header */}
        <div className=\"text-center\">
          <div className=\"flex items-center justify-center mb-4\">
            <Button
              variant=\"ghost\"
              size=\"sm\"
              onClick={() => navigate('/')}
              className=\"absolute left-4 top-4\"
            >
              <ArrowLeft className=\"h-4 w-4 mr-2\" />
              Back to Home
            </Button>
            <Store className=\"h-12 w-12 text-blue-600\" />
          </div>
          <h1 className=\"text-4xl font-bold text-gray-900 mb-2\">Shop Manager</h1>
          <p className=\"text-lg text-gray-600\">
            Choose your preferred authentication method
          </p>
        </div>

        <div className=\"grid lg:grid-cols-3 gap-8 items-start\">
          {/* Login Options */}
          <div className=\"lg:col-span-2\">
            <Card className=\"w-full\">
              <CardHeader className=\"space-y-1\">
                <CardTitle className=\"text-2xl\">Welcome back</CardTitle>
                <CardDescription>
                  Sign in using embedded login or SSO
                </CardDescription>
              </CardHeader>
              <CardContent>
                {error && (
                  <Alert variant=\"destructive\" className=\"mb-4\">
                    <AlertDescription>{error}</AlertDescription>
                  </Alert>
                )}

                <Tabs defaultValue=\"embedded\" className=\"w-full\">
                  <TabsList className=\"grid w-full grid-cols-2\">
                    <TabsTrigger value=\"embedded\" className=\"flex items-center gap-2\">
                      <User className=\"h-4 w-4\" />
                      Direct Login
                    </TabsTrigger>
                    <TabsTrigger value=\"sso\" className=\"flex items-center gap-2\">
                      <ShieldCheck className=\"h-4 w-4\" />
                      SSO Login
                    </TabsTrigger>
                  </TabsList>

                  <TabsContent value=\"embedded\" className=\"space-y-4 mt-4\">
                    <div className=\"p-4 bg-green-50 border border-green-200 rounded-lg\">
                      <div className=\"flex items-center gap-2 mb-2\">
                        <Key className=\"h-4 w-4 text-green-600\" />
                        <span className=\"text-sm font-medium text-green-700\">Direct Authentication</span>
                      </div>
                      <p className=\"text-xs text-green-600\">
                        Login directly without redirect. Credentials are securely validated with Keycloak.
                      </p>
                    </div>

                    <form onSubmit={handleEmbeddedLogin} className=\"space-y-4\">
                      <div className=\"space-y-2\">
                        <Label htmlFor=\"email\">Email</Label>
                        <Input
                          id=\"email\"
                          type=\"email\"
                          placeholder=\"Enter your email\"
                          value={email}
                          onChange={(e) => setEmail(e.target.value)}
                          required
                          disabled={isLoading}
                        />
                      </div>

                      <div className=\"space-y-2\">
                        <Label htmlFor=\"password\">Password</Label>
                        <div className=\"relative\">
                          <Input
                            id=\"password\"
                            type={showPassword ? \"text\" : \"password\"}
                            placeholder=\"Enter your password\"
                            value={password}
                            onChange={(e) => setPassword(e.target.value)}
                            required
                            disabled={isLoading}
                          />
                          <Button
                            type=\"button\"
                            variant=\"ghost\"
                            size=\"sm\"
                            className=\"absolute right-0 top-0 h-full px-3 py-2 hover:bg-transparent\"
                            onClick={() => setShowPassword(!showPassword)}
                          >
                            {showPassword ? (
                              <EyeOff className=\"h-4 w-4\" />
                            ) : (
                              <Eye className=\"h-4 w-4\" />
                            )}
                          </Button>
                        </div>
                      </div>

                      <Button
                        type=\"submit\"
                        className=\"w-full\"
                        size=\"lg\"
                        disabled={isLoading}
                      >
                        {isLoading ? 'Signing in...' : 'Sign in Directly'}
                      </Button>
                    </form>
                  </TabsContent>

                  <TabsContent value=\"sso\" className=\"space-y-4 mt-4\">
                    <div className=\"p-4 bg-blue-50 border border-blue-200 rounded-lg\">
                      <div className=\"flex items-center gap-2 mb-2\">
                        <ShieldCheck className=\"h-4 w-4 text-blue-600\" />
                        <span className=\"text-sm font-medium text-blue-700\">Single Sign-On</span>
                      </div>
                      <p className=\"text-xs text-blue-600\">
                        Redirect to Keycloak for secure authentication. Best for enterprise environments.
                      </p>
                    </div>

                    <Button
                      onClick={handleSSOLogin}
                      className=\"w-full\"
                      size=\"lg\"
                      disabled={isLoading}
                    >
                      <ShieldCheck className=\"mr-2 h-4 w-4\" />
                      {isLoading ? 'Redirecting...' : 'Continue with Keycloak SSO'}
                    </Button>

                    <p className=\"text-xs text-center text-gray-500\">
                      You will be redirected to the Keycloak authentication page
                    </p>
                  </TabsContent>
                </Tabs>

                <div className=\"mt-6 text-center\">
                  <p className=\"text-sm text-gray-600\">
                    Don't have an account?{' '}
                    <Link to=\"/register\" className=\"text-blue-600 hover:text-blue-500 font-medium\">
                      Sign up
                    </Link>
                  </p>
                </div>
              </CardContent>
            </Card>
          </div>

          {/* Test Accounts & Features */}
          <div className=\"space-y-6\">
            {/* Test Accounts */}
            <Card>
              <CardHeader>
                <CardTitle className=\"text-lg\">Test Accounts</CardTitle>
                <CardDescription>Use these accounts for testing</CardDescription>
              </CardHeader>
              <CardContent>
                <div className=\"space-y-2\">
                  {testAccounts.map((account, index) => (
                    <div
                      key={index}
                      className=\"p-2 bg-gray-50 rounded text-xs cursor-pointer hover:bg-gray-100\"
                      onClick={() => {
                        setEmail(account.email)
                        setPassword(account.password)
                      }}
                    >
                      <div className=\"font-medium text-gray-700\">{account.role}</div>
                      <div className=\"text-gray-500\">{account.email}</div>
                      <div className=\"text-gray-400\">{account.password}</div>
                    </div>
                  ))}
                </div>
                <p className=\"text-xs text-gray-500 mt-2\">
                  Click any account to auto-fill the form
                </p>
              </CardContent>
            </Card>

            {/* Features */}
            <div className=\"space-y-4\">
              <h3 className=\"text-lg font-semibold text-gray-900\">Key Features</h3>
              {features.map((feature, index) => (
                <div key={index} className=\"flex items-start space-x-3\">
                  <feature.icon className=\"h-5 w-5 text-blue-600 mt-1\" />
                  <div>
                    <h4 className=\"font-medium text-gray-900 text-sm\">{feature.title}</h4>
                    <p className=\"text-xs text-gray-600\">{feature.description}</p>
                  </div>
                </div>
              ))}
            </div>
          </div>
        </div>

        <div className=\"text-center text-sm text-gray-500\">
          Shop Manager v1.0.0 - Multi-tenant retail management platform
        </div>
      </div>
    </div>
  )
}