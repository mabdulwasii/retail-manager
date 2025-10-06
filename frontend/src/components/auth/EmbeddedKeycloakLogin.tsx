import React, { useState, useRef, useEffect } from 'react'
import { Button } from '@/components/ui/button'
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card'
import { Alert, AlertDescription } from '@/components/ui/alert'
import { Loader2, ExternalLink, Shield } from 'lucide-react'
import configService from '@/config/runtime-config'

interface EmbeddedKeycloakLoginProps {
  onSuccess: (tokens: { accessToken: string; refreshToken: string; idToken: string }) => void
  onError: (error: string) => void
}

export const EmbeddedKeycloakLogin: React.FC<EmbeddedKeycloakLoginProps> = ({
  onSuccess,
  onError,
}) => {
  const [isLoading, setIsLoading] = useState(false)
  const [showIframe, setShowIframe] = useState(false)
  const [error, setError] = useState('')
  const iframeRef = useRef<HTMLIFrameElement>(null)

  const keycloakConfig = {
    url: configService.keycloakUrl,
    realm: configService.keycloakRealm,
    clientId: configService.keycloakClientId,
  }

  const generateCodeChallenge = async (codeVerifier: string): Promise<string> => {
    const encoder = new TextEncoder()
    const data = encoder.encode(codeVerifier)
    const digest = await crypto.subtle.digest('SHA-256', data)
    const base64String = btoa(String.fromCharCode(...new Uint8Array(digest)))
    return base64String.replace(/\+/g, '-').replace(/\//g, '_').replace(/=/g, '')
  }

  const generateRandomString = (length: number): string => {
    const charset = 'ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789-._~'
    let result = ''
    for (let i = 0; i < length; i++) {
      result += charset.charAt(Math.floor(Math.random() * charset.length))
    }
    return result
  }

  const handleEmbeddedLogin = async () => {
    setIsLoading(true)
    setError('')
    setShowIframe(true)

    try {
      // Generate PKCE parameters
      const codeVerifier = generateRandomString(128)
      const codeChallenge = await generateCodeChallenge(codeVerifier)
      const state = generateRandomString(32)

      // Store PKCE parameters for later use
      sessionStorage.setItem('pkce_code_verifier', codeVerifier)
      sessionStorage.setItem('oauth_state', state)

      // Build authorization URL
      const authUrl = new URL(`${keycloakConfig.url}/realms/${keycloakConfig.realm}/protocol/openid-connect/auth`)
      authUrl.searchParams.set('client_id', keycloakConfig.clientId)
      authUrl.searchParams.set('redirect_uri', window.location.origin + '/auth/callback')
      authUrl.searchParams.set('response_type', 'code')
      authUrl.searchParams.set('scope', 'openid profile email')
      authUrl.searchParams.set('state', state)
      authUrl.searchParams.set('code_challenge', codeChallenge)
      authUrl.searchParams.set('code_challenge_method', 'S256')

      // Set iframe source
      if (iframeRef.current) {
        iframeRef.current.src = authUrl.toString()
      }

      // Listen for iframe messages
      const handleMessage = async (event: MessageEvent) => {
        if (event.origin !== new URL(keycloakConfig.url).origin) {
          return
        }

        if (event.data.type === 'KEYCLOAK_AUTH_SUCCESS') {
          try {
            const { code, state: returnedState } = event.data

            // Verify state
            const storedState = sessionStorage.getItem('oauth_state')
            if (returnedState !== storedState) {
              throw new Error('State mismatch - possible CSRF attack')
            }

            // Exchange code for tokens
            const tokens = await exchangeCodeForTokens(code, codeVerifier)
            onSuccess(tokens)

            // Cleanup
            sessionStorage.removeItem('pkce_code_verifier')
            sessionStorage.removeItem('oauth_state')
            window.removeEventListener('message', handleMessage)
            setShowIframe(false)
          } catch (error) {
            console.error('Token exchange failed:', error)
            onError('Authentication failed. Please try again.')
          }
        } else if (event.data.type === 'KEYCLOAK_AUTH_ERROR') {
          onError(event.data.error || 'Authentication failed')
          setShowIframe(false)
        }
      }

      window.addEventListener('message', handleMessage)

      // Timeout after 5 minutes
      setTimeout(() => {
        window.removeEventListener('message', handleMessage)
        setShowIframe(false)
        setIsLoading(false)
        if (showIframe) {
          onError('Authentication timeout. Please try again.')
        }
      }, 300000)

    } catch (error) {
      console.error('Login error:', error)
      onError('Failed to initiate authentication')
      setShowIframe(false)
    }

    setIsLoading(false)
  }

  const exchangeCodeForTokens = async (code: string, codeVerifier: string) => {
    const tokenUrl = `${keycloakConfig.url}/realms/${keycloakConfig.realm}/protocol/openid-connect/token`

    const response = await fetch(tokenUrl, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/x-www-form-urlencoded',
      },
      body: new URLSearchParams({
        grant_type: 'authorization_code',
        client_id: keycloakConfig.clientId,
        code,
        redirect_uri: window.location.origin + '/auth/callback',
        code_verifier: codeVerifier,
      }),
    })

    if (!response.ok) {
      const errorData = await response.json()
      throw new Error(errorData.error_description || 'Token exchange failed')
    }

    const tokenData = await response.json()
    return {
      accessToken: tokenData.access_token,
      refreshToken: tokenData.refresh_token,
      idToken: tokenData.id_token,
    }
  }

  const handleDirectLogin = () => {
    // Open in new window for better UX
    const authUrl = new URL(`${keycloakConfig.url}/realms/${keycloakConfig.realm}/protocol/openid-connect/auth`)
    authUrl.searchParams.set('client_id', keycloakConfig.clientId)
    authUrl.searchParams.set('redirect_uri', window.location.origin + '/dashboard')
    authUrl.searchParams.set('response_type', 'code')
    authUrl.searchParams.set('scope', 'openid profile email')

    window.location.href = authUrl.toString()
  }

  return (
    <div className=\"space-y-4\">
      {error && (
        <Alert variant=\"destructive\">
          <AlertDescription>{error}</AlertDescription>
        </Alert>
      )}

      {!showIframe ? (
        <div className=\"space-y-4\">
          <div className=\"p-4 bg-blue-50 border border-blue-200 rounded-lg\">
            <p className=\"text-sm font-medium text-blue-700 mb-2\">
              <Shield className=\"inline h-4 w-4 mr-1\" />
              Secure OAuth2 Authorization Code Flow
            </p>
            <p className=\"text-xs text-blue-600\">
              Uses industry-standard OAuth2 with PKCE for maximum security.
            </p>
          </div>

          <div className=\"grid grid-cols-1 gap-3\">
            <Button
              onClick={handleEmbeddedLogin}
              className=\"w-full\"
              size=\"lg\"
              disabled={isLoading}
            >
              {isLoading ? (
                <>
                  <Loader2 className=\"mr-2 h-4 w-4 animate-spin\" />
                  Loading...
                </>
              ) : (
                'Login with Embedded Auth'
              )}
            </Button>

            <Button
              onClick={handleDirectLogin}
              variant=\"outline\"
              className=\"w-full\"
              size=\"lg\"
            >
              <ExternalLink className=\"mr-2 h-4 w-4\" />
              Login with Redirect
            </Button>
          </div>

          <div className=\"text-center\">
            <p className=\"text-xs text-gray-500\">
              Test accounts: admin@shopmanager.com / admin123
            </p>
          </div>
        </div>
      ) : (
        <Card>
          <CardHeader>
            <CardTitle className=\"text-lg\">Keycloak Authentication</CardTitle>
            <CardDescription>
              Please login using your credentials below
            </CardDescription>
          </CardHeader>
          <CardContent>
            <div className=\"relative\">
              <iframe
                ref={iframeRef}
                className=\"w-full h-96 border rounded\"
                title=\"Keycloak Login\"
                sandbox=\"allow-same-origin allow-scripts allow-forms allow-top-navigation\"
              />
              <div className=\"absolute top-2 right-2\">
                <Button
                  size=\"sm\"
                  variant=\"ghost\"
                  onClick={() => setShowIframe(false)}
                >
                  ✕
                </Button>
              </div>
            </div>
          </CardContent>
        </Card>
      )}
    </div>
  )
}