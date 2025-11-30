import React from 'react'
import { useNavigate } from 'react-router-dom'
import { useForm } from 'react-hook-form'
import { yupResolver } from '@hookform/resolvers/yup'
import * as yup from 'yup'
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import { Label } from '@/components/ui/label'
import { Textarea } from '@/components/ui/textarea'
import { ArrowLeft, Shield, Loader2, CheckCircle2 } from 'lucide-react'
import { useCreateRole } from '@/hooks/useRoles'
import { RoleCreateRequest } from '@/types/role'
import { useAuth } from '@/context/ManualAuthContext'
import { Permission } from '@/types/permissions'

// Validation schema
const roleSchema = yup.object().shape({
  name: yup
    .string()
    .required('Role name is required')
    .min(2, 'Role name must be at least 2 characters')
    .max(50, 'Role name must not exceed 50 characters')
    .matches(/^[A-Z_]+$/, 'Role name must be uppercase with underscores only (e.g., SHOP_MANAGER)'),
  description: yup
    .string()
    .max(200, 'Description must not exceed 200 characters'),
})

type RoleFormData = yup.InferType<typeof roleSchema>

export const CreateRolePage: React.FC = () => {
  const navigate = useNavigate()
  const { hasPermission, hasAnyPermission, user } = useAuth()
  const createRoleMutation = useCreateRole()

  // Check if user can create roles
  const canCreateRoles = hasPermission(Permission.ROLE_CREATE)
  const isSystemAdmin = hasAnyPermission([Permission.SYSTEM_ADMIN, Permission.TENANT_MANAGE])

  React.useEffect(() => {
    if (!canCreateRoles) {
      navigate('/')
    }
  }, [canCreateRoles, navigate])

  if (!canCreateRoles) {
    return null
  }

  const {
    register,
    handleSubmit,
    formState: { errors, isSubmitting },
  } = useForm<RoleFormData>({
    resolver: yupResolver(roleSchema),
    defaultValues: {
      name: '',
      description: '',
    },
  })

  const onSubmit = async (data: RoleFormData) => {
    try {
      const roleData: RoleCreateRequest = {
        name: data.name,
        ...(data.description && { description: data.description }),
        // Custom roles are tenant-specific (unless creating as SYSTEM_ADMIN)
        ...(!isSystemAdmin && user?.tenantId && { tenantId: user.tenantId }),
      }

      const newRole = await createRoleMutation.mutateAsync(roleData)

      if (newRole) {
        // Navigate to edit page to assign permissions
        navigate(`/admin/roles/${newRole.id}/edit`)
      }
    } catch (error) {
      console.error('Failed to create role:', error)
    }
  }

  const handleCancel = () => {
    navigate('/admin/roles')
  }

  return (
    <div className="space-y-6 max-w-2xl">
      {/* Header */}
      <div className="flex flex-col gap-4">
        <Button variant="ghost" className="w-fit" onClick={handleCancel}>
          <ArrowLeft className="mr-2 h-4 w-4" />
          Back to Roles
        </Button>

        <div className="flex items-center gap-3">
          <div className="p-2 bg-primary/10 rounded-lg">
            <Shield className="h-6 w-6 text-primary" />
          </div>
          <div>
            <h1 className="text-3xl font-bold tracking-tight">Create New Role</h1>
            <p className="text-muted-foreground mt-1">
              Define a new role that can be assigned to users
            </p>
          </div>
        </div>
      </div>

      <form onSubmit={handleSubmit(onSubmit)} className="space-y-6">
        {/* Basic Information */}
        <Card>
          <CardHeader>
            <CardTitle>Role Information</CardTitle>
            <CardDescription>
              Provide basic details about the role
            </CardDescription>
          </CardHeader>
          <CardContent className="space-y-4">
            {/* Role Name */}
            <div className="space-y-2">
              <Label htmlFor="name">
                Role Name <span className="text-destructive">*</span>
              </Label>
              <Input
                id="name"
                {...register('name')}
                placeholder="SHOP_MANAGER"
                aria-invalid={!!errors.name}
                className="font-mono"
              />
              {errors.name && (
                <p className="text-sm text-destructive">{errors.name.message}</p>
              )}
              <p className="text-xs text-muted-foreground">
                Use UPPERCASE with underscores. Example: SHOP_MANAGER, SALES_AGENT
              </p>
            </div>

            {/* Description */}
            <div className="space-y-2">
              <Label htmlFor="description">Description</Label>
              <Textarea
                id="description"
                {...register('description')}
                placeholder="Manages shop operations and staff"
                rows={3}
                aria-invalid={!!errors.description}
              />
              {errors.description && (
                <p className="text-sm text-destructive">{errors.description.message}</p>
              )}
              <p className="text-xs text-muted-foreground">
                Optional: Brief description of the role's responsibilities
              </p>
            </div>
          </CardContent>
        </Card>

        {/* Action Buttons */}
        <Card>
          <CardContent className="pt-6">
            <div className="flex flex-col sm:flex-row gap-3 justify-end">
              <Button
                type="button"
                variant="outline"
                onClick={handleCancel}
                disabled={isSubmitting || createRoleMutation.isPending}
              >
                Cancel
              </Button>
              <Button
                type="submit"
                disabled={isSubmitting || createRoleMutation.isPending}
              >
                {(isSubmitting || createRoleMutation.isPending) ? (
                  <>
                    <Loader2 className="mr-2 h-4 w-4 animate-spin" />
                    Creating Role...
                  </>
                ) : (
                  <>
                    <CheckCircle2 className="mr-2 h-4 w-4" />
                    Create Role
                  </>
                )}
              </Button>
            </div>
          </CardContent>
        </Card>

        {/* Info Card */}
        <Card className="border-blue-200 bg-blue-50">
          <CardContent className="pt-6">
            <div className="flex gap-2">
              <Shield className="h-5 w-5 text-blue-600 flex-shrink-0 mt-0.5" />
              <div className="space-y-1">
                <p className="text-sm font-medium text-blue-900">Next Step: Assign Permissions</p>
                <p className="text-sm text-blue-700">
                  After creating the role, you'll be redirected to assign permissions.
                  You can always modify permissions later from the role management page.
                </p>
              </div>
            </div>
          </CardContent>
        </Card>
      </form>
    </div>
  )
}
