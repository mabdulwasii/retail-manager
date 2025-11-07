import React, { useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { Button } from '@/components/ui/button'
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card'
import { Alert, AlertDescription } from '@/components/ui/alert'
import { Input } from '@/components/ui/input'
import { Badge } from '@/components/ui/badge'
import {
  Plus,
  Shield,
  AlertCircle,
  Loader2,
  Search,
  Edit,
  Trash2,
  Eye,
  Users,
  Key,
} from 'lucide-react'
import { useRoles, useDeleteRole } from '@/hooks/useRoles'
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuLabel,
  DropdownMenuSeparator,
  DropdownMenuTrigger,
} from '@/components/ui/dropdown-menu'
import {
  AlertDialog,
  AlertDialogAction,
  AlertDialogCancel,
  AlertDialogContent,
  AlertDialogDescription,
  AlertDialogFooter,
  AlertDialogHeader,
  AlertDialogTitle,
} from '@/components/ui/alert-dialog'
import { useAuth } from '@/context/ManualAuthContext'
import { UserRole } from '@/types/roles'

export const RolesPage: React.FC = () => {
  const navigate = useNavigate()
  const { hasAnyRole } = useAuth()
  const [searchQuery, setSearchQuery] = useState('')
  const [roleToDelete, setRoleToDelete] = useState<{ id: string; name: string } | null>(null)

  const { data: roles, isLoading, isError, error } = useRoles()
  const deleteRoleMutation = useDeleteRole()

  // Only TENANT_ADMIN can access this page
  const canManageRoles = hasAnyRole([UserRole.TENANT_ADMIN, UserRole.SYSTEM_ADMIN, UserRole.SUPER_ADMIN])

  React.useEffect(() => {
    if (!canManageRoles) {
      navigate('/')
    }
  }, [canManageRoles, navigate])

  if (!canManageRoles) {
    return null
  }

  // Filter roles based on search query
  const filteredRoles = roles?.filter((role) => {
    if (!searchQuery) return true
    const query = searchQuery.toLowerCase()
    return (
      role.name.toLowerCase().includes(query) ||
      role.description?.toLowerCase().includes(query)
    )
  })

  const handleDeleteRole = async () => {
    if (!roleToDelete) return
    await deleteRoleMutation.mutateAsync(roleToDelete.id)
    setRoleToDelete(null)
  }

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex flex-col sm:flex-row justify-between items-start sm:items-center gap-4">
        <div>
          <h1 className="text-3xl font-bold tracking-tight flex items-center gap-2">
            <Shield className="h-8 w-8 text-primary" />
            Role Management
          </h1>
          <p className="text-muted-foreground mt-1">
            Create and manage user roles and their permissions
          </p>
        </div>
        <div className="flex gap-2">
          <Link to="/admin/permissions">
            <Button variant="outline">
              <Key className="mr-2 h-4 w-4" />
              Permission Matrix
            </Button>
          </Link>
          <Link to="/admin/roles/create">
            <Button>
              <Plus className="mr-2 h-4 w-4" />
              Create Role
            </Button>
          </Link>
        </div>
      </div>

      {/* Stats Cards */}
      <div className="grid gap-4 md:grid-cols-3">
        <Card>
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium">Total Roles</CardTitle>
            <Shield className="h-4 w-4 text-muted-foreground" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold">{roles?.length || 0}</div>
            <p className="text-xs text-muted-foreground mt-1">
              Across all categories
            </p>
          </CardContent>
        </Card>

        <Card>
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium">System Roles</CardTitle>
            <Shield className="h-4 w-4 text-blue-600" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold text-blue-600">
              {roles?.filter((r) => r.isSystem).length || 0}
            </div>
            <p className="text-xs text-muted-foreground mt-1">
              Protected from deletion
            </p>
          </CardContent>
        </Card>

        <Card>
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium">Custom Roles</CardTitle>
            <Users className="h-4 w-4 text-green-600" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold text-green-600">
              {roles?.filter((r) => !r.isSystem).length || 0}
            </div>
            <p className="text-xs text-muted-foreground mt-1">
              Created by admins
            </p>
          </CardContent>
        </Card>
      </div>

      {/* Search Bar */}
      <div className="relative max-w-md">
        <Search className="absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-muted-foreground" />
        <Input
          placeholder="Search roles..."
          value={searchQuery}
          onChange={(e) => setSearchQuery(e.target.value)}
          className="pl-9"
        />
      </div>

      {/* Error State */}
      {isError && (
        <Alert variant="destructive">
          <AlertCircle className="h-4 w-4" />
          <AlertDescription>
            Failed to load roles: {error?.message || 'An error occurred'}
          </AlertDescription>
        </Alert>
      )}

      {/* Loading State */}
      {isLoading && (
        <div className="flex justify-center items-center py-12">
          <Loader2 className="h-8 w-8 animate-spin text-muted-foreground" />
        </div>
      )}

      {/* Empty State */}
      {!isLoading && filteredRoles?.length === 0 && (
        <Card>
          <CardContent className="flex flex-col items-center justify-center py-12">
            <Shield className="h-12 w-12 text-muted-foreground mb-4" />
            <h3 className="text-lg font-semibold mb-2">
              {searchQuery ? 'No roles found' : 'No roles yet'}
            </h3>
            <p className="text-muted-foreground text-center mb-4">
              {searchQuery
                ? 'Try adjusting your search criteria'
                : 'Get started by creating your first role'}
            </p>
            {!searchQuery && (
              <Link to="/admin/roles/create">
                <Button>
                  <Plus className="mr-2 h-4 w-4" />
                  Create Role
                </Button>
              </Link>
            )}
          </CardContent>
        </Card>
      )}

      {/* Roles Grid */}
      {!isLoading && filteredRoles && filteredRoles.length > 0 && (
        <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-3">
          {filteredRoles.map((role) => (
            <Card key={role.id} className="hover:shadow-lg transition-shadow duration-200">
              <CardHeader>
                <div className="flex justify-between items-start gap-2">
                  <div className="flex-1 min-w-0">
                    <div className="flex items-center gap-2">
                      <CardTitle className="text-lg truncate">{role.name}</CardTitle>
                      {role.isSystem && (
                        <Badge variant="secondary" className="bg-blue-100 text-blue-800">
                          System
                        </Badge>
                      )}
                    </div>
                    <CardDescription className="line-clamp-2">
                      {role.description || 'No description'}
                    </CardDescription>
                  </div>
                </div>
              </CardHeader>

              <CardContent className="space-y-3">
                <div className="flex items-center text-sm text-muted-foreground">
                  <Key className="mr-2 h-4 w-4 flex-shrink-0" />
                  <span>{role.permissions?.length || 0} permission{role.permissions?.length === 1 ? '' : 's'}</span>
                </div>

                <div className="flex gap-2 pt-2">
                  <Link to={`/admin/roles/${role.id}`} className="flex-1">
                    <Button variant="outline" className="w-full">
                      <Eye className="mr-2 h-4 w-4" />
                      View
                    </Button>
                  </Link>

                  <DropdownMenu>
                    <DropdownMenuTrigger asChild>
                      <Button variant="outline" size="icon">
                        <Edit className="h-4 w-4" />
                      </Button>
                    </DropdownMenuTrigger>
                    <DropdownMenuContent align="end">
                      <DropdownMenuLabel>Actions</DropdownMenuLabel>
                      <DropdownMenuSeparator />
                      <DropdownMenuItem asChild>
                        <Link to={`/admin/roles/${role.id}/edit`}>
                          <Edit className="mr-2 h-4 w-4" />
                          Edit Role
                        </Link>
                      </DropdownMenuItem>
                      {!role.isSystem && (
                        <>
                          <DropdownMenuSeparator />
                          <DropdownMenuItem
                            onClick={() => setRoleToDelete({ id: role.id, name: role.name })}
                            className="text-destructive focus:text-destructive"
                          >
                            <Trash2 className="mr-2 h-4 w-4" />
                            Delete Role
                          </DropdownMenuItem>
                        </>
                      )}
                    </DropdownMenuContent>
                  </DropdownMenu>
                </div>
              </CardContent>
            </Card>
          ))}
        </div>
      )}

      {/* Delete Confirmation Dialog */}
      <AlertDialog open={!!roleToDelete} onOpenChange={() => setRoleToDelete(null)}>
        <AlertDialogContent>
          <AlertDialogHeader>
            <AlertDialogTitle>Are you sure?</AlertDialogTitle>
            <AlertDialogDescription>
              This will permanently delete the role "{roleToDelete?.name}".
              This action cannot be undone. Users with this role will lose access to
              associated permissions.
            </AlertDialogDescription>
          </AlertDialogHeader>
          <AlertDialogFooter>
            <AlertDialogCancel>Cancel</AlertDialogCancel>
            <AlertDialogAction
              onClick={handleDeleteRole}
              className="bg-destructive hover:bg-destructive/90"
              disabled={deleteRoleMutation.isPending}
            >
              {deleteRoleMutation.isPending ? (
                <>
                  <Loader2 className="mr-2 h-4 w-4 animate-spin" />
                  Deleting...
                </>
              ) : (
                'Delete Role'
              )}
            </AlertDialogAction>
          </AlertDialogFooter>
        </AlertDialogContent>
      </AlertDialog>
    </div>
  )
}
