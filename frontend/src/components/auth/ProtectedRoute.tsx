import React from 'react'
import { Navigate } from 'react-router-dom'
import { useAuth } from '@/context/ManualAuthContext'
import { LoadingSpinner } from '@/components/ui/loading-spinner'

interface ProtectedRouteProps {
  children: React.ReactNode
  roles?: string[]
  requireAll?: boolean
}

export const ProtectedRoute: React.FC<ProtectedRouteProps> = ({
  children,
  roles = [],
  requireAll = false,
}) => {
  const { isInitialized, isAuthenticated, hasRole, hasAnyRole, user } = useAuth()

  // Wait for auth to initialize before checking permissions
  if (!isInitialized) {
    return (
      <div className="flex items-center justify-center min-h-screen">
        <LoadingSpinner size="lg" />
      </div>
    )
  }

  // Redirect to login if not authenticated
  if (!isAuthenticated || !user) {
    return <Navigate to="/login" replace />
  }

  // Check access synchronously with loaded user data
  const hasAccess = (): boolean => {
    // No roles required means accessible to all authenticated users
    if (roles.length === 0) return true

    if (requireAll) {
      return roles.every(role => hasRole(role))
    } else {
      return hasAnyRole(roles)
    }
  }

  // Immediately check access - no delay
  if (!hasAccess()) {
    return (
      <Navigate
        to="/unauthorized"
        state={{ requiredRoles: roles }}
        replace
      />
    )
  }

  // Only render children after all checks pass
  return <>{children}</>
}