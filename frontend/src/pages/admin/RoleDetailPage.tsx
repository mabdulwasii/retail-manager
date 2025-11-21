import React from 'react'
import { useParams, useNavigate, Link } from 'react-router-dom'
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card'
import { Button } from '@/components/ui/button'
import { Badge } from '@/components/ui/badge'
import { Alert, AlertDescription } from '@/components/ui/alert'
import { Separator } from '@/components/ui/separator'
import { ArrowLeft, Shield, Loader2, AlertCircle, Key, Edit, Users } from 'lucide-react'
import { useRole, usePermissions } from '@/hooks/useRoles'
import { useAuth } from '@/context/ManualAuthContext'
import { UserRole } from '@/types/roles'

export const RoleDetailPage: React.FC = () => {
  const { roleId } = useParams<{ roleId: string }>()
  const navigate = useNavigate()
  const { hasAnyRole } = useAuth()

  const { data: role, isLoading: loadingRole, isError, error } = useRole(roleId)
  const { data: allPermissions, isLoading: loadingPermissions } = usePermissions()

  const canManageRoles = hasAnyRole([UserRole.TENANT_ADMIN, UserRole.SYSTEM_ADMIN, UserRole.SUPER_ADMIN])
  const isSystemAdmin = hasAnyRole([UserRole.SYSTEM_ADMIN, UserRole.SUPER_ADMIN])

  React.useEffect(() => {
    if (!canManageRoles) {
      navigate('/')
    }
  }, [canManageRoles, navigate])

  if (!canManageRoles) {
    return null
  }

  // Group permissions by category
  const groupedPermissions = React.useMemo(() => {
    if (!allPermissions) return {}
    
    return allPermissions.reduce((acc, permission) => {
      const category = permission.category || 'Other'
      if (!acc[category]) {
        acc[category] = []
      }
      acc[category].push(permission)
      return acc
    }, {} as Record<string, typeof allPermissions>)
  }, [allPermissions])

  const rolePermissionNames = new Set(role?.permissions || [])

  if (loadingRole || loadingPermissions) {
    return (
      <div className="flex items-center justify-center min-h-screen">
        <Loader2 className="h-8 w-8 animate-spin text-muted-foreground" />
        <p className="ml-2 text-muted-foreground">Loading role details...</p>
      </div>
    )
  }

  if (isError || !role) {
    return (
      <div className="space-y-6 max-w-6xl">
        <Button variant="ghost" onClick={() => navigate('/admin/roles')}>
          <ArrowLeft className="mr-2 h-4 w-4" />
          Back to Roles
        </Button>
        <Alert variant="destructive">
          <AlertCircle className="h-4 w-4" />
          <AlertDescription>
            {error?.message || 'Failed to load role. The role may not exist or you may not have permission to view it.'}
          </AlertDescription>
        </Alert>
      </div>
    )
  }

  const canEditThisRole = isSystemAdmin || !role.isSystem

  return (
    <div className="space-y-6 max-w-6xl">
      {/* Header */}
      <div className="flex flex-col gap-4">
        <Button variant="ghost" className="w-fit" onClick={() => navigate('/admin/roles')}>
          <ArrowLeft className="mr-2 h-4 w-4" />
          Back to Roles
        </Button>

        <div className="flex items-start justify-between">
          <div className="flex items-center gap-3">
            <div className="p-2 bg-primary/10 rounded-lg">
              <Shield className="h-6 w-6 text-primary" />
            </div>
            <div>
              <div className="flex items-center gap-2">
                <h1 className="text-3xl font-bold tracking-tight">{role.name}</h1>
                {role.isSystem && (
                  <Badge variant="secondary" className="bg-blue-100 text-blue-800">
                    System Role
                  </Badge>
                )}
                {role.tenantId && (
                  <Badge variant="outline">
                    Custom Role
                  </Badge>
                )}
              </div>
              <p className="text-muted-foreground mt-1">
                {role.description || 'No description provided'}
              </p>
            </div>
          </div>
          
          {canEditThisRole && (
            <Link to={`/admin/roles/${role.id}/edit`}>
              <Button>
                <Edit className="mr-2 h-4 w-4" />
                Edit Role
              </Button>
            </Link>
          )}
        </div>
      </div>

      {/* Role Information */}
      <Card>
        <CardHeader>
          <CardTitle>Role Information</CardTitle>
          <CardDescription>Basic details about this role</CardDescription>
        </CardHeader>
        <CardContent className="space-y-4">
          <div className="grid grid-cols-2 gap-4">
            <div>
              <Label className="text-sm font-medium text-muted-foreground">Role Name</Label>
              <p className="mt-1 text-sm font-medium">{role.name}</p>
            </div>
            <div>
              <Label className="text-sm font-medium text-muted-foreground">Type</Label>
              <p className="mt-1 text-sm font-medium">
                {role.isSystem ? 'System Role' : 'Custom Role'}
              </p>
            </div>
            <div>
              <Label className="text-sm font-medium text-muted-foreground">Permissions Count</Label>
              <p className="mt-1 text-sm font-medium">{role.permissions?.length || 0}</p>
            </div>
            <div>
              <Label className="text-sm font-medium text-muted-foreground">Created</Label>
              <p className="mt-1 text-sm font-medium">
                {role.createdAt ? new Date(role.createdAt).toLocaleDateString() : 'N/A'}
              </p>
            </div>
          </div>
          
          {role.description && (
            <>
              <Separator />
              <div>
                <Label className="text-sm font-medium text-muted-foreground">Description</Label>
                <p className="mt-1 text-sm">{role.description}</p>
              </div>
            </>
          )}
        </CardContent>
      </Card>

      {/* Assigned Permissions */}
      <Card>
        <CardHeader>
          <div className="flex items-center justify-between">
            <div>
              <CardTitle>Assigned Permissions</CardTitle>
              <CardDescription>
                Permissions granted to users with this role
              </CardDescription>
            </div>
            <Badge variant="outline" className="text-lg px-3 py-1">
              <Key className="mr-2 h-4 w-4" />
              {role.permissions?.length || 0}
            </Badge>
          </div>
        </CardHeader>
        <CardContent>
          {role.permissions?.length === 0 ? (
            <div className="text-center py-12 text-muted-foreground">
              <Key className="h-12 w-12 mx-auto mb-4 opacity-20" />
              <p>No permissions assigned to this role</p>
              {canEditThisRole && (
                <Link to={`/admin/roles/${role.id}/edit`}>
                  <Button variant="outline" className="mt-4">
                    <Edit className="mr-2 h-4 w-4" />
                    Assign Permissions
                  </Button>
                </Link>
              )}
            </div>
          ) : (
            <div className="space-y-6">
              {Object.entries(groupedPermissions).map(([category, permissions]) => {
                const categoryPermissions = permissions.filter(p => 
                  rolePermissionNames.has(p.name)
                )
                
                if (categoryPermissions.length === 0) return null

                return (
                  <div key={category}>
                    <div className="flex items-center gap-2 mb-3">
                      <h3 className="font-semibold text-sm uppercase tracking-wide text-muted-foreground">
                        {category}
                      </h3>
                      <Badge variant="secondary" className="text-xs">
                        {categoryPermissions.length}
                      </Badge>
                    </div>
                    <div className="grid grid-cols-1 md:grid-cols-2 gap-3">
                      {categoryPermissions.map((permission) => (
                        <div
                          key={permission.id}
                          className="flex items-start gap-3 p-3 rounded-lg border bg-card"
                        >
                          <div className="flex-shrink-0 mt-0.5">
                            <div className="rounded-full bg-green-100 p-1">
                              <Key className="h-3 w-3 text-green-600" />
                            </div>
                          </div>
                          <div className="flex-1 min-w-0">
                            <p className="font-medium text-sm">{permission.name}</p>
                            {permission.description && (
                              <p className="text-xs text-muted-foreground mt-1">
                                {permission.description}
                              </p>
                            )}
                          </div>
                        </div>
                      ))}
                    </div>
                  </div>
                )
              })}
            </div>
          )}
        </CardContent>
      </Card>

      {/* Actions */}
      <div className="flex justify-between items-center pt-4">
        <Button variant="outline" onClick={() => navigate('/admin/roles')}>
          Back to Roles
        </Button>
        {canEditThisRole && (
          <Link to={`/admin/roles/${role.id}/edit`}>
            <Button>
              <Edit className="mr-2 h-4 w-4" />
              Edit Role
            </Button>
          </Link>
        )}
      </div>
    </div>
  )
}

// Add Label component if not imported
const Label: React.FC<{ className?: string; children: React.ReactNode }> = ({ className, children }) => (
  <label className={className}>{children}</label>
)
