import React, { createContext, useContext, useEffect, useState } from 'react'
import { ReactKeycloakProvider, useKeycloak } from '@react-keycloak/web'
import Keycloak from 'keycloak-js'
import KeycloakService from '@/services/KeycloakService'
import { Loader2 } from 'lucide-react'

// Initialize Keycloak instance
const keycloak = KeycloakService.init()

// Auth Context Type
interface AuthContextType {
  isAuthenticated: boolean
  isLoading: boolean
  user: UserProfile | null
  keycloak: Keycloak | null
  login: (options?: Keycloak.KeycloakLoginOptions) => Promise<void>
  logout: (options?: Keycloak.KeycloakLogoutOptions) => Promise<void>
  register: (options?: Keycloak.KeycloakRegisterOptions) => Promise<void>
  hasRole: (role: string) => boolean
  hasAnyRole: (roles: string[]) => boolean
  hasAllRoles: (roles: string[]) => boolean
  getToken: () => string | undefined
  updateToken: (minValidity?: number) => Promise<boolean>
}

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

// Create Auth Context
const AuthContext = createContext<AuthContextType | undefined>(undefined)

// Loading component
const LoadingComponent: React.FC = () => (
  <div className="min-h-screen flex items-center justify-center bg-gray-50">
    <div className="text-center">
      <Loader2 className="h-12 w-12 animate-spin text-blue-600 mx-auto mb-4" />
      <h2 className="text-xl font-semibold text-gray-700">Initializing Authentication...</h2>
      <p className="text-gray-500 mt-2">Please wait while we set up your secure session</p>
    </div>
  </div>
)

// Error component
const ErrorComponent: React.FC<{ error?: Error }> = ({ error }) => (
  <div className="min-h-screen flex items-center justify-center bg-gray-50">
    <div className="text-center max-w-md">
      <div className="text-red-600 mb-4">
        <svg className="h-12 w-12 mx-auto" fill="none" viewBox="0 0 24 24" stroke="currentColor">
          <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z" />
        </svg>
      </div>
      <h2 className="text-xl font-semibold text-gray-700 mb-2">Authentication Error</h2>
      <p className="text-gray-500">{error?.message || 'Failed to initialize authentication'}</p>
      <button
        onClick={() => window.location.reload()}
        className="mt-4 px-4 py-2 bg-blue-600 text-white rounded hover:bg-blue-700"
      >
        Retry
      </button>
    </div>
  </div>
)

// Inner Auth Provider that uses the Keycloak hook
const InnerAuthProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const { keycloak, initialized } = useKeycloak()
  const [user, setUser] = useState<UserProfile | null>(null)

  useEffect(() => {
    if (initialized) {
      KeycloakService.setInitialized(true)
      if (keycloak.authenticated) {
        const userProfile = KeycloakService.getUserProfile()
        setUser(userProfile)
      } else {
        setUser(null)
      }
    }
  }, [initialized, keycloak.authenticated])

  // Set up token refresh
  useEffect(() => {
    if (initialized && keycloak.authenticated) {
      const interval = setInterval(async () => {
        try {
          const refreshed = await KeycloakService.updateToken(70)
          if (refreshed) {
            console.log('Token refreshed successfully')
          }
        } catch (error) {
          console.error('Token refresh failed:', error)
        }
      }, 60000) // Check every minute

      return () => clearInterval(interval)
    }
  }, [initialized, keycloak.authenticated])

  const contextValue: AuthContextType = {
    isAuthenticated: keycloak.authenticated || false,
    isLoading: !initialized,
    user,
    keycloak,
    login: async (options) => {
      await KeycloakService.login(options)
    },
    logout: async (options) => {
      await KeycloakService.logout(options)
    },
    register: async (options) => {
      await KeycloakService.register(options)
    },
    hasRole: (role) => KeycloakService.hasRole(role),
    hasAnyRole: (roles) => KeycloakService.hasAnyRole(roles),
    hasAllRoles: (roles) => KeycloakService.hasAllRoles(roles),
    getToken: () => KeycloakService.getToken(),
    updateToken: (minValidity) => KeycloakService.updateToken(minValidity),
  }

  return <AuthContext.Provider value={contextValue}>{children}</AuthContext.Provider>
}

// Main KeycloakAuthProvider component
export const KeycloakAuthProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const initOptions: Keycloak.KeycloakInitOptions = {
    onLoad: 'check-sso',
    checkLoginIframe: false,
    pkceMethod: 'S256',
    silentCheckSsoRedirectUri: window.location.origin + '/silent-check-sso.html',
    enableLogging: import.meta.env.DEV,
  }

  const onKeycloakEvent = (event: Keycloak.KeycloakEvent, error?: Keycloak.KeycloakError) => {
    console.log('Keycloak event:', event, error)

    switch (event) {
      case 'onAuthSuccess':
        console.log('Authentication successful')
        break
      case 'onAuthError':
        console.error('Authentication error:', error)
        break
      case 'onAuthRefreshSuccess':
        console.log('Token refresh successful')
        break
      case 'onAuthRefreshError':
        console.error('Token refresh error:', error)
        break
      case 'onAuthLogout':
        console.log('User logged out')
        break
      case 'onTokenExpired':
        console.log('Token expired')
        break
    }
  }

  const onKeycloakTokens = (tokens: Keycloak.KeycloakTokenParsed) => {
    console.log('Keycloak tokens updated')
    // You can store tokens in localStorage here if needed for persistence
    if (tokens) {
      localStorage.setItem('keycloak_token', keycloak.token || '')
      localStorage.setItem('keycloak_refresh_token', keycloak.refreshToken || '')
      localStorage.setItem('keycloak_id_token', keycloak.idToken || '')
    }
  }

  return (
    <ReactKeycloakProvider
      authClient={keycloak}
      initOptions={initOptions}
      LoadingComponent={<LoadingComponent />}
      isLoadingCheck={(keycloak) => !keycloak.authenticated && !keycloak.loginRequired}
      onEvent={onKeycloakEvent}
      onTokens={onKeycloakTokens}
    >
      <InnerAuthProvider>{children}</InnerAuthProvider>
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

// Higher-order component for protecting routes
export const withAuth = <P extends object>(
  Component: React.ComponentType<P>,
  requiredRoles?: string[]
): React.FC<P> => {
  return (props: P) => {
    const { isAuthenticated, isLoading, hasAllRoles, login } = useAuth()

    useEffect(() => {
      if (!isLoading && !isAuthenticated) {
        login()
      }
    }, [isLoading, isAuthenticated, login])

    if (isLoading) {
      return <LoadingComponent />
    }

    if (!isAuthenticated) {
      return null // Will redirect to login
    }

    if (requiredRoles && !hasAllRoles(requiredRoles)) {
      return (
        <div className="min-h-screen flex items-center justify-center bg-gray-50">
          <div className="text-center">
            <h2 className="text-xl font-semibold text-gray-700">Access Denied</h2>
            <p className="text-gray-500 mt-2">You don't have permission to access this page</p>
          </div>
        </div>
      )
    }

    return <Component {...props} />
  }
}

export default KeycloakAuthProvider