import React, { createContext, useContext, useEffect, useState } from 'react'
import { ReactKeycloakProvider, useKeycloak } from '@react-keycloak/web'
import Keycloak from 'keycloak-js'
import { Loader2 } from 'lucide-react'

// Initialize Keycloak instance
const keycloakConfig = {
  url: import.meta.env.VITE_KEYCLOAK_URL || 'https://localhost:8443',
  realm: import.meta.env.VITE_KEYCLOAK_REALM || 'shop-manager',
  clientId: import.meta.env.VITE_KEYCLOAK_CLIENT_ID || 'shop-manager-frontend',
}

const keycloak = new Keycloak(keycloakConfig)

// User Profile Type
interface UserProfile {
  id: string
  username: string
  email?: string
  firstName?: string
  lastName?: string
  fullName?: string
  roles: string[]
  tenantId?: string
  shopId?: string
}

// Extended Auth Context Type
interface AuthContextType {
  isAuthenticated: boolean
  isLoading: boolean
  user: UserProfile | null
  keycloak: Keycloak
  login: (options?: Keycloak.KeycloakLoginOptions) => Promise<void>
  logout: (options?: Keycloak.KeycloakLogoutOptions) => Promise<void>
  register: (options?: Keycloak.KeycloakRegisterOptions) => Promise<void>
  hasRole: (role: string) => boolean
  hasAnyRole: (roles: string[]) => boolean
  hasAllRoles: (roles: string[]) => boolean
  getToken: () => string | undefined
  refreshAuth: () => Promise<void>
}

// Create Auth Context
const AuthContext = createContext<AuthContextType | undefined>(undefined)

// Loading component
const LoadingFallback: React.FC = () => (
  <div className="min-h-screen flex items-center justify-center bg-gray-50">
    <div className="text-center">
      <Loader2 className="h-12 w-12 animate-spin text-blue-600 mx-auto mb-4" />
      <h2 className="text-xl font-semibold text-gray-700">Authenticating...</h2>
      <p className="text-gray-500 mt-2">Please wait while we verify your session</p>
    </div>
  </div>
)

// Inner component that uses useKeycloak hook
const AuthContextProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const { keycloak, initialized } = useKeycloak()
  const [user, setUser] = useState<UserProfile | null>(null)
  const [initError, setInitError] = useState<string | null>(null)
  const [initTimeout, setInitTimeout] = useState(false)

  // Extract user profile from token
  const getUserProfile = (): UserProfile | null => {
    if (!keycloak.tokenParsed) return null

    const token = keycloak.tokenParsed as any
    const realmRoles = token.realm_access?.roles || []
    const resourceRoles = token.resource_access?.[keycloak.clientId!]?.roles || []

    return {
      id: token.sub,
      username: token.preferred_username || token.email,
      email: token.email,
      firstName: token.given_name,
      lastName: token.family_name,
      fullName: token.name,
      roles: [...realmRoles, ...resourceRoles],
      tenantId: token.tenant_id,
      shopId: token.shop_id,
    }
  }

  // Timeout for Keycloak initialization
  useEffect(() => {
    const timeout = setTimeout(() => {
      if (!initialized) {
        console.warn('Keycloak initialization timed out after 10 seconds')
        setInitTimeout(true)
      }
    }, 10000) // 10 second timeout

    if (initialized) {
      clearTimeout(timeout)
    }

    return () => clearTimeout(timeout)
  }, [initialized])

  // Update user profile when authentication state changes
  useEffect(() => {
    if (initialized) {
      if (keycloak.authenticated) {
        setUser(getUserProfile())
      } else {
        setUser(null)
      }
    }
  }, [initialized, keycloak.authenticated, keycloak.tokenParsed])

  // Set up automatic token refresh
  useEffect(() => {
    if (initialized && keycloak.authenticated) {
      const refreshInterval = setInterval(async () => {
        try {
          const refreshed = await keycloak.updateToken(70) // Refresh if token expires in 70 seconds
          if (refreshed) {
            console.log('Token refreshed successfully')
            setUser(getUserProfile())
          }
        } catch (error) {
          console.error('Failed to refresh token:', error)
          keycloak.logout()
        }
      }, 60000) // Check every minute

      return () => clearInterval(refreshInterval)
    }
  }, [initialized, keycloak.authenticated])

  // Helper functions
  const hasRole = (role: string): boolean => {
    return user?.roles.includes(role) || false
  }

  const hasAnyRole = (roles: string[]): boolean => {
    return roles.some(role => hasRole(role))
  }

  const hasAllRoles = (roles: string[]): boolean => {
    return roles.every(role => hasRole(role))
  }

  const refreshAuth = async (): Promise<void> => {
    if (keycloak.authenticated) {
      try {
        await keycloak.updateToken(5)
        setUser(getUserProfile())
      } catch (error) {
        console.error('Failed to refresh auth:', error)
      }
    }
  }

  const contextValue: AuthContextType = {
    isAuthenticated: keycloak.authenticated || false,
    isLoading: !initialized && !initTimeout,
    user,
    keycloak,
    login: async (options) => {
      await keycloak.login(options)
    },
    logout: async (options) => {
      await keycloak.logout(options)
    },
    register: async (options) => {
      await keycloak.register(options)
    },
    hasRole,
    hasAnyRole,
    hasAllRoles,
    getToken: () => keycloak.token,
    refreshAuth,
  }

  if (!initialized && !initTimeout) {
    return <LoadingFallback />
  }

  // If initialization timed out, render children with unauthenticated state
  if (initTimeout && !initialized) {
    console.warn('Proceeding without Keycloak authentication due to timeout')
  }

  return <AuthContext.Provider value={contextValue}>{children}</AuthContext.Provider>
}

// Main provider component that wraps ReactKeycloakProvider
export const KeycloakAuthProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const initOptions: Keycloak.KeycloakInitOptions = {
    onLoad: 'check-sso',
    checkLoginIframe: false,
    pkceMethod: 'S256',
    enableLogging: import.meta.env.DEV,
    silentCheckSsoRedirectUri: window.location.origin + '/silent-check-sso.html',
    // Enable response mode for authorization code flow
    responseMode: 'fragment',
    flow: 'standard',
    // Add timeout configuration
    timeoutInSeconds: 30,
    // Don't fail immediately if Keycloak server is unreachable
    skipLogout: true,
  }

  const handleKeycloakEvent = (event: Keycloak.KeycloakEvent, error?: Keycloak.KeycloakError) => {
    console.log('Keycloak event:', event, error)

    switch (event) {
      case 'onAuthSuccess':
        console.log('User authenticated successfully')
        break
      case 'onAuthError':
        console.error('Authentication failed:', error)
        break
      case 'onAuthRefreshSuccess':
        console.log('Token refreshed')
        break
      case 'onAuthRefreshError':
        console.error('Token refresh failed:', error)
        break
      case 'onAuthLogout':
        console.log('User logged out')
        // Clear any persisted data
        localStorage.removeItem('keycloak_token')
        localStorage.removeItem('keycloak_refresh_token')
        localStorage.removeItem('keycloak_id_token')
        break
    }
  }

  const handleKeycloakTokens = (tokens: Keycloak.KeycloakTokenParsed) => {
    // Optional: Store tokens for persistence (be careful with security)
    if (keycloak.token && keycloak.refreshToken) {
      localStorage.setItem('keycloak_token', keycloak.token)
      localStorage.setItem('keycloak_refresh_token', keycloak.refreshToken)
      localStorage.setItem('keycloak_id_token', keycloak.idToken || '')
    }
  }

  return (
    <ReactKeycloakProvider
      authClient={keycloak}
      initOptions={initOptions}
      LoadingComponent={<LoadingFallback />}
      onEvent={handleKeycloakEvent}
      onTokens={handleKeycloakTokens}
    >
      <AuthContextProvider>{children}</AuthContextProvider>
    </ReactKeycloakProvider>
  )
}

// Custom hook to use auth context
export const useAuth = (): AuthContextType => {
  const context = useContext(AuthContext)
  if (context === undefined) {
    throw new Error('useAuth must be used within a KeycloakAuthProvider')
  }
  return context
}

// Export for backward compatibility
export const AuthProvider = KeycloakAuthProvider

export default KeycloakAuthProvider