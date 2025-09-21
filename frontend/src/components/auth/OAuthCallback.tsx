import React, { useEffect, useState } from 'react'
import { useNavigate, useLocation } from 'react-router-dom'
import { Loader2, AlertCircle } from 'lucide-react'
import { Alert, AlertDescription } from '@/components/ui/alert'

export const OAuthCallback: React.FC = () => {
  const navigate = useNavigate()
  const location = useLocation()
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    const handleCallback = async () => {
      try {
        // Get parameters from query string (OAuth2 authorization code flow)
        const params = new URLSearchParams(location.search)
        const code = params.get('code')
        const state = params.get('state')
        const errorParam = params.get('error')
        const errorDescription = params.get('error_description')

        // Check for errors
        if (errorParam) {
          setError(`Authentication failed: ${errorDescription || errorParam}`)
          return
        }

        // Verify state
        const storedState = sessionStorage.getItem('auth_state')
        if (state !== storedState) {
          setError('Invalid state parameter. Possible CSRF attack.')
          return
        }

        if (!code) {
          setError('No authorization code received.')
          return
        }

        // Get stored PKCE code verifier (optional for public clients)
        const codeVerifier = sessionStorage.getItem('code_verifier') || ''

        // Exchange code for tokens
        const keycloakUrl = import.meta.env.VITE_KEYCLOAK_URL || 'https://auth.shop-manager.local'
        const clientId = 'shop-manager-frontend'
        const redirectUri = window.location.origin + '/auth/callback'

        const tokenParams: any = {
          grant_type: 'authorization_code',
          client_id: clientId,
          code: code,
          redirect_uri: redirectUri,
        }

        // Only add code_verifier if it exists (PKCE is optional)
        if (codeVerifier) {
          tokenParams.code_verifier = codeVerifier
        }

        const tokenResponse = await fetch(`${keycloakUrl}/realms/shop-manager/protocol/openid-connect/token`, {
          method: 'POST',
          headers: {
            'Content-Type': 'application/x-www-form-urlencoded',
          },
          body: new URLSearchParams(tokenParams),
        })

        if (!tokenResponse.ok) {
          const errorData = await tokenResponse.json()
          setError(`Token exchange failed: ${errorData.error_description || 'Unknown error'}`)
          return
        }

        const tokens = await tokenResponse.json()

        // Store tokens
        localStorage.setItem('access_token', tokens.access_token)
        localStorage.setItem('refresh_token', tokens.refresh_token)
        localStorage.setItem('id_token', tokens.id_token)

        // Clean up session storage
        sessionStorage.removeItem('code_verifier')
        sessionStorage.removeItem('auth_state')
        sessionStorage.removeItem('auth_nonce')
        sessionStorage.removeItem('loginHint')

        // Navigate to dashboard
        navigate('/dashboard', { replace: true })

      } catch (err) {
        console.error('OAuth callback error:', err)
        setError('An unexpected error occurred during authentication.')
      }
    }

    handleCallback()
  }, [location, navigate])

  if (error) {
    return (
      <div className="min-h-screen flex items-center justify-center bg-gray-50">
        <div className="max-w-md w-full mx-4">
          <Alert variant="destructive">
            <AlertCircle className="h-4 w-4" />
            <AlertDescription className="ml-2">
              {error}
            </AlertDescription>
          </Alert>
          <div className="mt-4 text-center">
            <button
              onClick={() => navigate('/login')}
              className="text-blue-600 hover:text-blue-500 font-medium"
            >
              Return to Login
            </button>
          </div>
        </div>
      </div>
    )
  }

  return (
    <div className="min-h-screen flex items-center justify-center bg-gray-50">
      <div className="text-center">
        <Loader2 className="h-12 w-12 animate-spin text-blue-600 mx-auto mb-4" />
        <h2 className="text-xl font-semibold text-gray-700">Completing authentication...</h2>
        <p className="text-gray-500 mt-2">Please wait while we complete your sign-in</p>
      </div>
    </div>
  )
}