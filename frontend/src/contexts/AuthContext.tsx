import React, { createContext, useContext, useState, useEffect } from 'react'

export interface User {
  id: string
  username: string
  email: string
  firstName?: string
  lastName?: string
  roles: string[]
}

export interface AuthContextType {
  user: User | null
  login: (credentials: { username: string; password: string }) => Promise<void>
  logout: () => void
  isLoading: boolean
  isAuthenticated: boolean
  hasRole: (role: string) => boolean
  hasAnyRole: (roles: string[]) => boolean
}

const AuthContext = createContext<AuthContextType | undefined>(undefined)

export const useAuth = () => {
  const context = useContext(AuthContext)
  if (context === undefined) {
    throw new Error('useAuth must be used within an AuthProvider')
  }
  return context
}

interface AuthProviderProps {
  children: React.ReactNode
}

export const AuthProvider: React.FC<AuthProviderProps> = ({ children }) => {
  const [user, setUser] = useState<User | null>(null)
  const [isLoading, setIsLoading] = useState(true)

  const login = async (credentials: { username: string; password: string }) => {
    setIsLoading(true)
    try {
      // TODO: Implement actual login logic with Keycloak
      const mockUser: User = {
        id: '1',
        username: credentials.username,
        email: credentials.username,
        roles: ['SHOP_MANAGER']
      }
      setUser(mockUser)
    } catch (error) {
      console.error('Login failed:', error)
      throw error
    } finally {
      setIsLoading(false)
    }
  }

  const logout = () => {
    setUser(null)
  }

  const hasRole = (role: string): boolean => {
    return user?.roles.includes(role) || false
  }

  const hasAnyRole = (roles: string[]): boolean => {
    return user?.roles.some(role => roles.includes(role)) || false
  }

  useEffect(() => {
    // TODO: Check for existing auth token and validate
    setIsLoading(false)
  }, [])

  const value: AuthContextType = {
    user,
    login,
    logout,
    isLoading,
    isAuthenticated: !!user,
    hasRole,
    hasAnyRole
  }

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>
}