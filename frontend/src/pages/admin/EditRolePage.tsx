import React, { useEffect, useState } from 'react'
import { useParams, useNavigate } from 'react-router-dom'
import { useForm } from 'react-hook-form'
import { yupResolver } from '@hookform/resolvers/yup'
import * as yup from 'yup'
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import { Label } from '@/components/ui/label'
import { Textarea } from '@/components/ui/textarea'
import { Badge } from '@/components/ui/badge'
import { Checkbox } from '@/components/ui/checkbox'
import { Alert, AlertDescription } from '@/components/ui/alert'
import { Separator } from '@/components/ui/separator'
import { ArrowLeft, Shield, Loader2, Save, AlertCircle, Key, Check } from 'lucide-react'
import {
  useRole,
  useUpdateRole,
  usePermissions,
  useAssignPermissions,
  useRemovePermissions,
} from '@/hooks/useRoles'
import { RoleUpdateRequest, Permission } from '@/types/role'
import { useAuth } from '@/context/ManualAuthContext'
import { Permission as permissions } from '@/types/permissions'

const roleSchema = yup.object().shape({
  name: yup
    .string()
    .required('Role name is required')
    .min(2, 'Role name must be at least 2 characters')
    .max(50, 'Role name must not exceed 50 characters'),
  description: yup
    .string()
    .default('') // Ensure it's always a string, not undefined
    .max(200, 'Description must not exceed 200 characters'),
})

type RoleFormData = yup.InferType<typeof roleSchema>

export const EditRolePage: React.FC = () => {
  const { roleId } = useParams<{ roleId: string }>();
  const navigate = useNavigate();
  const { hasPermission, hasAnyPermission } = useAuth();

  const {
    data: role,
    isLoading: loadingRole,
    isError,
    error,
  } = useRole(roleId);
  const { data: allPermissions, isLoading: loadingPermissions } =
    usePermissions();
  const updateRoleMutation = useUpdateRole();
  const assignPermissionsMutation = useAssignPermissions();
  const removePermissionsMutation = useRemovePermissions();

  const [selectedPermissions, setSelectedPermissions] = useState<Set<string>>(
    new Set()
  );
  const [searchQuery, setSearchQuery] = useState("");

  // Check if user can update roles
  const canUpdateRoles = hasPermission(permissions.ROLE_UPDATE);

  React.useEffect(() => {
    if (!canUpdateRoles) {
      navigate("/");
    }
  }, [canUpdateRoles, navigate]);

  if (!canUpdateRoles) {
    return null;
  }

  const {
    register,
    handleSubmit,
    formState: { errors, isSubmitting, isDirty },
    reset,
    watch,
    setValue,
  } = useForm<RoleFormData>({
    resolver: yupResolver(roleSchema),
    mode: "onChange", // Validate on change
    defaultValues: {
      name: "",
      description: "", // Always initialize with empty string, not undefined
    },
  });

  // Watch form values
  const watchedValues = watch();

  useEffect(() => {
    if (role) {
      // Reset form with loaded data
      reset(
        {
          name: role.name,
          description: role.description || "",
        },
        {
          keepDefaultValues: false,
        }
      );

      // Manually set description to ensure it's tracked
      setValue("description", role.description || "", { shouldDirty: false });

      setSelectedPermissions(new Set(role.permissions || []));
    }
  }, [role, reset, setValue]);

  const onSubmit = async (data: RoleFormData) => {
    if (!roleId || !role) return;

    try {
      // API only accepts description for update
      const roleData: RoleUpdateRequest = {
        name: data.name,
        description: data.description || role.description || "",
      };

      await updateRoleMutation.mutateAsync({ roleId, data: roleData });
    } catch (error) {
      console.error("Failed to update role:", error);
    }
  };

  const handleSavePermissions = async () => {
    if (!roleId || !role) return;

    try {
      // API expects ALL selected permission names (replaces entire set)
      const selectedPermissionNames = Array.from(selectedPermissions);

      // Send all selected permissions as permissionIdentifiers
      await assignPermissionsMutation.mutateAsync({
        roleId,
        permissionIds: selectedPermissionNames,
      });
    } catch (error) {
      console.error("Failed to update permissions:", error);
    }
  };

  const handleTogglePermission = (permissionName: string) => {
    const newSet = new Set(selectedPermissions);
    if (newSet.has(permissionName)) {
      newSet.delete(permissionName);
    } else {
      newSet.add(permissionName);
    }
    setSelectedPermissions(newSet);
  };

  const handleCancel = () => {
    navigate("/admin/roles");
  };

  // Group permissions by category
  const groupedPermissions = React.useMemo(() => {
    if (!allPermissions) return {};

    const groups: Record<string, Permission[]> = {};

    allPermissions.forEach((permission) => {
      const category =
        permission.category || permission.name.split("_")[0] || "OTHER";
      if (!groups[category]) {
        groups[category] = [];
      }
      groups[category].push(permission);
    });

    return groups;
  }, [allPermissions]);

  // Filter permissions by search
  const filteredGroups = React.useMemo(() => {
    if (!searchQuery) return groupedPermissions;

    const query = searchQuery.toLowerCase();
    const filtered: Record<string, Permission[]> = {};

    Object.entries(groupedPermissions).forEach(([category, permissions]) => {
      const matchedPermissions = permissions.filter(
        (p) =>
          p.name.toLowerCase().includes(query) ||
          p.description?.toLowerCase().includes(query) ||
          category.toLowerCase().includes(query)
      );
      if (matchedPermissions.length > 0) {
        filtered[category] = matchedPermissions;
      }
    });

    return filtered;
  }, [groupedPermissions, searchQuery]);

  const isLoading = loadingRole || loadingPermissions;
  const hasPermissionChanges =
    role &&
    (selectedPermissions.size !== role.permissions?.length ||
      Array.from(selectedPermissions).some(
        (name) => !role.permissions?.includes(name)
      ));

  if (isLoading) {
    return (
      <div className="flex justify-center items-center min-h-[400px]">
        <Loader2 className="h-8 w-8 animate-spin text-muted-foreground" />
      </div>
    );
  }

  if (isError || !role) {
    return (
      <div className="space-y-4">
        <Button variant="ghost" onClick={() => navigate("/admin/roles")}>
          <ArrowLeft className="mr-2 h-4 w-4" />
          Back to Roles
        </Button>
        <Alert variant="destructive">
          <AlertCircle className="h-4 w-4" />
          <AlertDescription>
            {error?.message || "Role not found"}
          </AlertDescription>
        </Alert>
      </div>
    );
  }

  return (
    <div className="space-y-6 max-w-6xl">
      {/* Header */}
      <div className="flex flex-col gap-4">
        <Button variant="ghost" className="w-fit" onClick={handleCancel}>
          <ArrowLeft className="mr-2 h-4 w-4" />
          Back to Roles
        </Button>

        <div className="flex items-start justify-between gap-4">
          <div className="flex items-center gap-3">
            <div className="p-2 bg-primary/10 rounded-lg">
              <Shield className="h-6 w-6 text-primary" />
            </div>
            <div>
              <div className="flex items-center gap-3">
                <h1 className="text-3xl font-bold tracking-tight">Edit Role</h1>
                {role.isSystem && (
                  <Badge
                    variant="secondary"
                    className="bg-blue-100 text-blue-800"
                  >
                    System Role
                  </Badge>
                )}
              </div>
              <p className="text-muted-foreground mt-1">
                Update role information and manage permissions
              </p>
            </div>
          </div>
        </div>
      </div>

      <form onSubmit={handleSubmit(onSubmit)} className="space-y-6">
        {/* Basic Information */}
        <Card>
          <CardHeader>
            <CardTitle>Role Information</CardTitle>
            <CardDescription>Basic details about the role</CardDescription>
          </CardHeader>
          <CardContent className="space-y-4">
            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
              <div className="space-y-2">
                <Label htmlFor="name">
                  Role Name <span className="text-destructive">*</span>
                </Label>
                <Input
                  id="name"
                  {...register("name")}
                  placeholder="SHOP_MANAGER"
                  aria-invalid={!!errors.name}
                  disabled={role.isSystem}
                  className="font-mono"
                />
                {errors.name && (
                  <p className="text-sm text-destructive">
                    {errors.name.message}
                  </p>
                )}
              </div>

              <div className="space-y-2">
                <Label>Role ID</Label>
                <Input value={role.id} disabled className="font-mono" />
              </div>
            </div>

            <div className="space-y-2">
              <Label htmlFor="description">Description</Label>
              <Textarea
                id="description"
                value={watchedValues.description || ""}
                onChange={(e) => {
                  setValue("description", e.target.value, {
                    shouldDirty: true,
                    shouldValidate: true,
                  });
                }}
                placeholder="Manages shop operations and staff"
                rows={3}
                aria-invalid={!!errors.description}
              />
              {errors.description && (
                <p className="text-sm text-destructive">
                  {errors.description.message}
                </p>
              )}
            </div>

            <Separator />

            {/* <div className="grid grid-cols-1 md:grid-cols-2 gap-4 text-sm">
              <div>
                <p className="text-muted-foreground">Created</p>
                <p>{new Date(role.createdAt).toLocaleString()}</p>
              </div>
              <div>
                <p className="text-muted-foreground">Last Updated</p>
                <p>{new Date(role.updatedAt).toLocaleString()}</p>
              </div>
            </div> */}

            <div className="flex justify-end">
              <Button
                type="submit"
                disabled={
                  isSubmitting || updateRoleMutation.isPending || !isDirty
                }
              >
                {isSubmitting || updateRoleMutation.isPending ? (
                  <>
                    <Loader2 className="mr-2 h-4 w-4 animate-spin" />
                    Saving...
                  </>
                ) : (
                  <>
                    <Save className="mr-2 h-4 w-4" />
                    Save Changes
                  </>
                )}
              </Button>
            </div>
          </CardContent>
        </Card>
      </form>

      {/* Permissions Management */}
      <Card>
        <CardHeader>
          <div className="flex justify-between items-start">
            <div>
              <CardTitle className="flex items-center gap-2">
                <Key className="h-5 w-5" />
                Permissions
              </CardTitle>
              <CardDescription>
                Select permissions to assign to this role
              </CardDescription>
            </div>
            <Badge variant="outline">{selectedPermissions.size} selected</Badge>
          </div>
        </CardHeader>
        <CardContent className="space-y-4">
          {/* Search */}
          <Input
            placeholder="Search permissions..."
            value={searchQuery}
            onChange={(e) => setSearchQuery(e.target.value)}
          />

          {/* Permission Groups */}
          <div className="space-y-4 max-h-[600px] overflow-y-auto">
            {Object.entries(filteredGroups).map(([category, permissions]) => (
              <div key={category} className="space-y-2">
                <h3 className="font-semibold text-sm uppercase text-muted-foreground">
                  {category}
                </h3>
                <div className="grid grid-cols-1 md:grid-cols-2 gap-2">
                  {permissions.map((permission) => (
                    <div
                      key={permission.id}
                      className="flex items-start space-x-2 p-3 rounded-lg border hover:bg-muted/50 cursor-pointer"
                      onClick={() => handleTogglePermission(permission.name)}
                    >
                      <Checkbox
                        checked={selectedPermissions.has(permission.name)}
                        onCheckedChange={() =>
                          handleTogglePermission(permission.name)
                        }
                        className="mt-0.5"
                      />
                      <div className="flex-1 space-y-1">
                        <div className="flex items-center gap-2">
                          <Label className="text-sm font-medium cursor-pointer">
                            {permission.name}
                          </Label>
                          {selectedPermissions.has(permission.name) && (
                            <Check className="h-3 w-3 text-green-600" />
                          )}
                        </div>
                        {permission.description && (
                          <p className="text-xs text-muted-foreground">
                            {permission.description}
                          </p>
                        )}
                      </div>
                    </div>
                  ))}
                </div>
              </div>
            ))}
          </div>

          <Separator />

          <div className="flex justify-end gap-2">
            <Button
              variant="outline"
              onClick={() =>
                setSelectedPermissions(new Set(role.permissions || []))
              }
              disabled={!hasPermissionChanges}
            >
              Reset
            </Button>
            <Button
              onClick={handleSavePermissions}
              disabled={
                !hasPermissionChanges ||
                assignPermissionsMutation.isPending ||
                removePermissionsMutation.isPending
              }
            >
              {assignPermissionsMutation.isPending ||
              removePermissionsMutation.isPending ? (
                <>
                  <Loader2 className="mr-2 h-4 w-4 animate-spin" />
                  Saving...
                </>
              ) : (
                <>
                  <Save className="mr-2 h-4 w-4" />
                  Save Permissions
                </>
              )}
            </Button>
          </div>
        </CardContent>
      </Card>
    </div>
  );
}
