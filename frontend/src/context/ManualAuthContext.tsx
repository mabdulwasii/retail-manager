import React, { createContext, useContext, useState, useCallback } from 'react'
import Keycloak from 'keycloak-js'
import { apiService } from '@/services/api'
import configService from '@/config/runtime-config'

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

// Auth Context Type
interface AuthContextType {
  isAuthenticated: boolean
  isInitialized: boolean
  user: UserProfile | null
  keycloak: Keycloak | null
  initializeKeycloak: () => Promise<Keycloak>
  login: () => void
  logout: () => Promise<void>
  hasRole: (role: string) => boolean
  hasAnyRole: (roles: string[]) => boolean
  hasAllRoles: (roles: string[]) => boolean
  getToken: () => string | undefined
}

// Create Auth Context
const AuthContext = createContext<AuthContextType | undefined>(undefined)

// Auth Provider Component
export const ManualAuthProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const [keycloak, setKeycloak] = useState<Keycloak | null>(null)
  const [isInitialized, setIsInitialized] = useState(false)
  const [isAuthenticated, setIsAuthenticated] = useState(false)
  const [user, setUser] = useState<UserProfile | null>(null)
  const initializingRef = React.useRef(false)

  // Extract user profile from token
  const getUserProfile = useCallback((kc: Keycloak): UserProfile | null => {
    if (!kc.tokenParsed) return null

    const token = kc.tokenParsed as any
    const realmRoles = token.realm_access?.roles || []
    const resourceRoles = token.resource_access?.[kc.clientId!]?.roles || []

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
  }, [])

  // Initialize Keycloak on mount to handle OAuth callback
  React.useEffect(() => {
    if (initializingRef.current || keycloak) return
    initializingRef.current = true

    const init = async () => {
      const kc = new Keycloak({
        url: configService.keycloakUrl,
        realm: configService.keycloakRealm,
        clientId: configService.keycloakClientId,
      })

      try {
        // If there's a hash with OAuth params but we've already retried, clear it
        const hasRetried = sessionStorage.getItem('kc_retry')
        if (window.location.hash && hasRetried) {
          console.log('Already retried once, clearing hash and continuing...')
          window.location.hash = ''
          sessionStorage.removeItem('kc_retry')
        }

        const authenticated = await kc.init({
          onLoad: 'check-sso',
          checkLoginIframe: false,
          pkceMethod: 'S256',
          enableLogging: true,
          responseMode: 'fragment',
          flow: 'standard',
          silentCheckSsoFallback: false,
        })

        // Clear retry flag on success
        sessionStorage.removeItem('kc_retry')

        setKeycloak(kc)
        setIsInitialized(true)

        if (authenticated) {
          setIsAuthenticated(true)
          setUser(getUserProfile(kc))
          apiService.setTokenProvider(() => kc.token)

          // Store tokens
          if (kc.token && kc.refreshToken) {
            localStorage.setItem('keycloak_token', kc.token)
            localStorage.setItem('keycloak_access_token', kc.token)
            localStorage.setItem('keycloak_refresh_token', kc.refreshToken)
            localStorage.setItem('keycloak_id_token', kc.idToken || '')
          }

          // Set up token refresh
          setInterval(async () => {
            try {
              const refreshed = await kc.updateToken(70)
              if (refreshed && kc.token && kc.refreshToken) {
                localStorage.setItem('keycloak_token', kc.token)
                localStorage.setItem('keycloak_access_token', kc.token)
                localStorage.setItem('keycloak_refresh_token', kc.refreshToken)
                localStorage.setItem('keycloak_id_token', kc.idToken || '')
                setUser(getUserProfile(kc))
              }
            } catch (error) {
              console.error('Failed to refresh token:', error)
              await kc.logout()
            }
          }, 60000)
        }
      } catch (error) {
        console.error('Failed to initialize Keycloak:', error)

        // If there's an OAuth callback error and we haven't retried yet, clear URL and retry once
        const hasRetried = sessionStorage.getItem('kc_retry')
        if (window.location.hash && !hasRetried) {
          console.log('OAuth callback error, clearing hash and retrying...')
          sessionStorage.setItem('kc_retry', 'true')
          window.location.hash = ''
          window.location.reload()
          return
        }

        // If already retried or no hash, just mark as initialized
        sessionStorage.removeItem('kc_retry')
        setKeycloak(kc)
        setIsInitialized(true)
      }
    }

    init()
  }, [getUserProfile])

  // Get or wait for Keycloak instance
  const initializeKeycloak = useCallback(async (): Promise<Keycloak> => {
    if (keycloak) return keycloak

    // Wait for initialization to complete
    return new Promise((resolve, reject) => {
      const checkInterval = setInterval(() => {
        if (keycloak) {
          clearInterval(checkInterval)
          resolve(keycloak)
        }
      }, 100)

      // Timeout after 10 seconds
      setTimeout(() => {
        clearInterval(checkInterval)
        reject(new Error('Keycloak initialization timeout'))
      }, 10000)
    })
  }, [keycloak])

  // Login - redirect to Keycloak
  const login = useCallback(async () => {
    try {
      const kc = await initializeKeycloak()
      await kc.login({
        redirectUri: window.location.origin + '/dashboard',
      })
    } catch (error) {
      console.error('Login failed:', error)
      // Redirect to home page if Keycloak init fails
      window.location.href = '/'
    }
  }, [initializeKeycloak])

  // Logout
  const logout = useCallback(async () => {
    if (keycloak) {
      await keycloak.logout()
    }

    // Clear state
    setIsAuthenticated(false)
    setUser(null)
    apiService.setTokenProvider(() => undefined)

    // Clear localStorage
    localStorage.removeItem('keycloak_token')
    localStorage.removeItem('keycloak_access_token')
    localStorage.removeItem('keycloak_refresh_token')
    localStorage.removeItem('keycloak_id_token')
  }, [keycloak])

  // Role checking functions
  const hasRole = useCallback((role: string): boolean => {
    return user?.roles.includes(role) || false
  }, [user])

  const hasAnyRole = useCallback((roles: string[]): boolean => {
    return roles.some(role => hasRole(role))
  }, [hasRole])

  const hasAllRoles = useCallback((roles: string[]): boolean => {
    return roles.every(role => hasRole(role))
  }, [hasRole])

  const getToken = useCallback((): string | undefined => {
    return keycloak?.token
  }, [keycloak])

  const contextValue: AuthContextType = {
    isAuthenticated,
    isInitialized,
    user,
    keycloak,
    initializeKeycloak,
    login,
    logout,
    hasRole,
    hasAnyRole,
    hasAllRoles,
    getToken,
  }

  return <AuthContext.Provider value={contextValue}>{children}</AuthContext.Provider>
}

// Custom hook to use auth context
export const useAuth = (): AuthContextType => {
  const context = useContext(AuthContext)
  if (context === undefined) {
    throw new Error('useAuth must be used within a ManualAuthProvider')
  }
  return context
}

// Export for backward compatibility
export const AuthProvider = ManualAuthProvider
export default ManualAuthProvider
