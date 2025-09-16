import React, { createContext, useContext, useEffect, useState, ReactNode } from 'react'
import { initKeycloak, getKeycloak, getUserInfo, login, logout } from '@/lib/keycloak'

interface AuthContextType {
  isAuthenticated: boolean
  isLoading: boolean
  user: any | null
  login: () => Promise<void>
  logout: () => Promise<void>
  hasRole: (role: string) => boolean
  hasAnyRole: (roles: string[]) => boolean
  refreshAuth: () => Promise<void>
}

const AuthContext = createContext<AuthContextType | undefined>(undefined)

interface AuthProviderProps {
  children: ReactNode
}

export const AuthProvider: React.FC<AuthProviderProps> = ({ children }) => {
  const [isAuthenticated, setIsAuthenticated] = useState(false)
  const [isLoading, setIsLoading] = useState(true)
  const [user, setUser] = useState<any | null>(null)

  useEffect(() => {
    const initAuth = async () => {
      try {
        const keycloak = await initKeycloak()
        setIsAuthenticated(keycloak.authenticated || false)

        if (keycloak.authenticated) {
          const userInfo = getUserInfo()
          setUser(userInfo)
        }
      } catch (error) {
        console.error('Authentication initialization failed:', error)
      } finally {
        setIsLoading(false)
      }
    }

    initAuth()

    // Set up periodic check for authentication state changes
    const authCheckInterval = setInterval(() => {
      const kc = getKeycloak()
      if (kc) {
        const currentAuth = kc.authenticated || false
        if (currentAuth !== isAuthenticated) {
          setIsAuthenticated(currentAuth)
          if (currentAuth) {
            const userInfo = getUserInfo()
            setUser(userInfo)
          } else {
            setUser(null)
          }
        }
      }
    }, 1000) // Check every second

    return () => clearInterval(authCheckInterval)
  }, [isAuthenticated])

  const handleLogin = async () => {
    await login()
  }

  const handleLogout = async () => {
    await logout()
    setIsAuthenticated(false)
    setUser(null)
  }

  const hasRole = (role: string): boolean => {
    const kc = getKeycloak()
    return kc?.hasRealmRole(role) || false
  }

  const hasAnyRole = (roles: string[]): boolean => {
    return roles.some(role => hasRole(role))
  }

  const refreshAuth = async () => {
    try {
      const keycloak = getKeycloak()
      if (keycloak && keycloak.authenticated) {
        setIsAuthenticated(true)
        const userInfo = getUserInfo()
        setUser(userInfo)
      } else {
        setIsAuthenticated(false)
        setUser(null)
      }
    } catch (error) {
      console.error('Auth refresh failed:', error)
      setIsAuthenticated(false)
      setUser(null)
    }
  }

  const value: AuthContextType = {
    isAuthenticated,
    isLoading,
    user,
    login: handleLogin,
    logout: handleLogout,
    hasRole,
    hasAnyRole,
    refreshAuth,
  }

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>
}

export const useAuth = (): AuthContextType => {
  const context = useContext(AuthContext)
  if (context === undefined) {
    throw new Error('useAuth must be used within an AuthProvider')
  }
  return context
}