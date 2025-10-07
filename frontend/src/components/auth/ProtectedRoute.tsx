import React from 'react'
import { useAuth } from '@/context/ManualAuthContext'
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card'

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
  const { hasRole, hasAnyRole } = useAuth()

  const hasAccess = () => {
    if (roles.length === 0) return true

    if (requireAll) {
      return roles.every(role => hasRole(role))
    } else {
      return hasAnyRole(roles)
    }
  }

  if (!hasAccess()) {
    return (
      <div className="flex items-center justify-center min-h-[400px]">
        <Card className="w-full max-w-md">
          <CardHeader>
            <CardTitle>Access Denied</CardTitle>
            <CardDescription>
              You don't have permission to access this page.
            </CardDescription>
          </CardHeader>
          <CardContent>
            <p className="text-sm text-muted-foreground">
              Required roles: {roles.join(', ')}
            </p>
          </CardContent>
        </Card>
      </div>
    )
  }

  return <>{children}</>
}