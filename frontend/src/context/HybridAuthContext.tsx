import React, { createContext, useContext, useEffect, useState } from 'react'
import { useKeycloak } from '@react-keycloak/web'
import AuthService from '@/services/AuthService'

interface UserProfile {
  id: string
  username: string
  email?: string
  firstName?: string
  lastName?: string
  roles: string[]
}

interface HybridAuthContextType {
  isAuthenticated: boolean
  isLoading: boolean
  user: UserProfile | null
  // Embedded login
  loginWithCredentials: (username: string, password: string) => Promise<boolean>
  // SSO login
  loginWithSSO: () => Promise<void>
  logout: () => Promise<void>
  hasRole: (role: string) => boolean
  hasAnyRole: (roles: string[]) => boolean
  getToken: () => string | undefined
}

const HybridAuthContext = createContext<HybridAuthContextType | undefined>(undefined)

export const HybridAuthProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const { keycloak } = useKeycloak()
  const [isAuthenticated, setIsAuthenticated] = useState(false)
  const [isLoading, setIsLoading] = useState(true)
  const [user, setUser] = useState<UserProfile | null>(null)

  // Check authentication state on mount and when keycloak changes
  useEffect(() => {
    const checkAuth = () => {
      // Check if authenticated via Keycloak (SSO)
      if (keycloak.authenticated) {
        const kcUser = keycloak.tokenParsed as any
        setUser({
          id: kcUser.sub,
          username: kcUser.preferred_username || kcUser.email,
          email: kcUser.email,
          firstName: kcUser.given_name,
          lastName: kcUser.family_name,
          roles: [
            ...(kcUser.realm_access?.roles || []),
            ...(kcUser.resource_access?.[keycloak.clientId!]?.roles || [])
          ],
        })
        setIsAuthenticated(true)
      }
      // Check if authenticated via embedded login
      else if (AuthService.isAuthenticated()) {
        const userInfo = AuthService.getUserInfo()
        if (userInfo) {
          setUser(userInfo)
          setIsAuthenticated(true)
        }
      }
      // Not authenticated
      else {
        setUser(null)
        setIsAuthenticated(false)
      }

      setIsLoading(false)
    }

    checkAuth()
  }, [keycloak.authenticated, keycloak.tokenParsed])

  const loginWithCredentials = async (username: string, password: string): Promise<boolean> => {
    const success = await AuthService.loginWithCredentials(username, password)
    if (success) {
      const userInfo = AuthService.getUserInfo()
      if (userInfo) {
        setUser(userInfo)
        setIsAuthenticated(true)
      }
    }
    return success
  }

  const loginWithSSO = async (): Promise<void> => {
    await keycloak.login()
  }

  const logout = async (): Promise<void> => {
    // Clear embedded auth
    AuthService.logout()

    // Clear SSO auth if present
    if (keycloak.authenticated) {
      await keycloak.logout()
    }

    setUser(null)
    setIsAuthenticated(false)
  }

  const hasRole = (role: string): boolean => {
    return user?.roles.includes(role) || false
  }

  const hasAnyRole = (roles: string[]): boolean => {
    return roles.some(role => hasRole(role))
  }

  const getToken = (): string | undefined => {
    // Return Keycloak token if available, otherwise embedded token
    return keycloak.token || localStorage.getItem('access_token') || undefined
  }

  const contextValue: HybridAuthContextType = {
    isAuthenticated,
    isLoading,
    user,
    loginWithCredentials,
    loginWithSSO,
    logout,
    hasRole,
    hasAnyRole,
    getToken,
  }

  return (
    <HybridAuthContext.Provider value={contextValue}>
      {children}
    </HybridAuthContext.Provider>
  )
}

export const useAuth = (): HybridAuthContextType => {
  const context = useContext(HybridAuthContext)
  if (context === undefined) {
    throw new Error('useAuth must be used within a HybridAuthProvider')
  }
  return context
}

export default HybridAuthProvider