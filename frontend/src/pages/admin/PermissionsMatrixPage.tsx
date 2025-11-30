import { Alert, AlertDescription } from "@/components/ui/alert";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from "@/components/ui/card";
import { Input } from "@/components/ui/input";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";
import { useAuth } from "@/context/ManualAuthContext";
import { usePermissions, useRoles } from "@/hooks/useRoles";
import { Permission as AuthPermission } from "@/types/permissions";
import { Permission } from "@/types/role";
import {
  AlertCircle,
  ArrowLeft,
  Check,
  Download,
  Filter,
  Key,
  Loader2,
  Search,
  Shield,
  X,
} from "lucide-react";
import React, { useMemo, useState } from "react";
import { Link, useNavigate } from "react-router-dom";

export const PermissionsMatrixPage: React.FC = () => {
  const navigate = useNavigate();
  const { hasPermission: hasPermissions } = useAuth();
  const [searchQuery, setSearchQuery] = useState("");
  const [selectedCategory, setSelectedCategory] = useState<string>("all");

  const { data: roles, isLoading: loadingRoles } = useRoles();
  const { data: permissions, isLoading: loadingPermissions } = usePermissions();

  const canViewPermissions = hasPermissions(AuthPermission.PERMISSION_LIST);

  React.useEffect(() => {
    if (!canViewPermissions) {
      navigate("/");
    }
  }, [canViewPermissions, navigate]);

  if (!canViewPermissions) {
    return null;
  }

  // Group permissions by category
  const permissionsByCategory = useMemo(() => {
    if (!permissions) return {};

    const groups: Record<string, Permission[]> = {};
    permissions.forEach((permission) => {
      const category =
        permission.category || permission.name.split("_")[0] || "OTHER";
      if (!groups[category]) {
        groups[category] = [];
      }
      groups[category].push(permission);
    });

    return groups;
  }, [permissions]);

  // Get all categories
  const categories = useMemo(() => {
    return ["all", ...Object.keys(permissionsByCategory).sort()];
  }, [permissionsByCategory]);

  // Filter permissions
  const filteredPermissions = useMemo(() => {
    let filtered = permissions || [];

    // Filter by category
    if (selectedCategory !== "all") {
      filtered = permissionsByCategory[selectedCategory] || [];
    }

    // Filter by search
    if (searchQuery) {
      const query = searchQuery.toLowerCase();
      filtered = filtered.filter(
        (p) =>
          p.name.toLowerCase().includes(query) ||
          p.description?.toLowerCase().includes(query)
      );
    }

    return filtered;
  }, [permissions, permissionsByCategory, selectedCategory, searchQuery]);

  // Check if role has permission (permissions are strings, not objects)
  const hasPermission = (roleId: string, permissionName: string): boolean => {
    const role = roles?.find((r) => r.id === roleId);
    return role?.permissions?.includes(permissionName) || false;
  };

  // Export to CSV
  const handleExport = () => {
    if (!roles || !filteredPermissions) return;

    const headers = ["Permission", ...roles.map((r) => r.name)];
    const rows = filteredPermissions.map((permission) => [
      permission.name,
      ...roles.map((role) =>
        hasPermission(role.id, permission.name) ? "Yes" : "No"
      ),
    ]);

    const csv = [headers, ...rows].map((row) => row.join(",")).join("\n");
    const blob = new Blob([csv], { type: "text/csv" });
    const url = window.URL.createObjectURL(blob);
    const a = document.createElement("a");
    a.href = url;
    a.download = `permissions-matrix-${
      new Date().toISOString().split("T")[0]
    }.csv`;
    document.body.appendChild(a);
    a.click();
    document.body.removeChild(a);
    window.URL.revokeObjectURL(url);
  };

  const isLoading = loadingRoles || loadingPermissions;

  if (isLoading) {
    return (
      <div className="flex justify-center items-center min-h-[400px]">
        <Loader2 className="h-8 w-8 animate-spin text-muted-foreground" />
      </div>
    );
  }

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex flex-col gap-4">
        <Button
          variant="ghost"
          className="w-fit"
          onClick={() => navigate("/admin/roles")}
        >
          <ArrowLeft className="mr-2 h-4 w-4" />
          Back to Roles
        </Button>

        <div className="flex flex-col sm:flex-row justify-between items-start sm:items-center gap-4">
          <div>
            <h1 className="text-3xl font-bold tracking-tight flex items-center gap-2">
              <Key className="h-8 w-8 text-primary" />
              Permission Matrix
            </h1>
            <p className="text-muted-foreground mt-1">
              View all permissions across roles at a glance
            </p>
          </div>
          <Button onClick={handleExport}>
            <Download className="mr-2 h-4 w-4" />
            Export CSV
          </Button>
        </div>
      </div>

      {/* Stats */}
      <div className="grid gap-4 md:grid-cols-3">
        <Card>
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium">
              Total Permissions
            </CardTitle>
            <Key className="h-4 w-4 text-muted-foreground" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold">{permissions?.length || 0}</div>
            <p className="text-xs text-muted-foreground mt-1">
              Across {categories.length - 1} categories
            </p>
          </CardContent>
        </Card>

        <Card>
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium">Total Roles</CardTitle>
            <Shield className="h-4 w-4 text-muted-foreground" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold">{roles?.length || 0}</div>
            <p className="text-xs text-muted-foreground mt-1">
              {roles?.filter((r) => r.isSystem).length || 0} system roles
            </p>
          </CardContent>
        </Card>

        <Card>
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium">Showing</CardTitle>
            <Filter className="h-4 w-4 text-muted-foreground" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold">
              {filteredPermissions.length}
            </div>
            <p className="text-xs text-muted-foreground mt-1">
              {selectedCategory !== "all" ? selectedCategory : "All"}{" "}
              permissions
            </p>
          </CardContent>
        </Card>
      </div>

      {/* Filters */}
      <Card>
        <CardContent className="pt-6">
          <div className="flex flex-col sm:flex-row gap-4">
            <div className="flex-1 relative">
              <Search className="absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-muted-foreground" />
              <Input
                placeholder="Search permissions..."
                value={searchQuery}
                onChange={(e) => setSearchQuery(e.target.value)}
                className="pl-9"
              />
            </div>
            <Select
              value={selectedCategory}
              onValueChange={setSelectedCategory}
            >
              <SelectTrigger className="w-full sm:w-[200px]">
                <SelectValue placeholder="Filter by category" />
              </SelectTrigger>
              <SelectContent>
                {categories.map((category) => (
                  <SelectItem key={category} value={category}>
                    {category === "all" ? "All Categories" : category}
                  </SelectItem>
                ))}
              </SelectContent>
            </Select>
          </div>
        </CardContent>
      </Card>

      {/* Matrix */}
      {filteredPermissions.length === 0 ? (
        <Alert>
          <AlertCircle className="h-4 w-4" />
          <AlertDescription>
            No permissions found matching your criteria. Try adjusting your
            filters.
          </AlertDescription>
        </Alert>
      ) : (
        <Card>
          <CardHeader>
            <CardTitle>Permission Matrix</CardTitle>
            <CardDescription>
              Green check = permission granted, Red X = permission not granted
            </CardDescription>
          </CardHeader>
          <CardContent>
            <div className="overflow-x-auto">
              <table className="w-full border-collapse">
                <thead>
                  <tr className="border-b bg-muted/50">
                    <th className="p-3 text-left font-semibold sticky left-0 bg-muted/50 z-10 min-w-[200px]">
                      Permission
                    </th>
                    {roles?.map((role) => (
                      <th
                        key={role.id}
                        className="p-3 text-center font-semibold min-w-[120px]"
                      >
                        <Link
                          to={`/admin/roles/${role.id}/edit`}
                          className="hover:text-primary transition-colors"
                        >
                          <div className="flex flex-col items-center gap-1">
                            <span className="text-sm">{role.name}</span>
                            {role.isSystem && (
                              <Badge variant="secondary" className="text-xs">
                                System
                              </Badge>
                            )}
                          </div>
                        </Link>
                      </th>
                    ))}
                  </tr>
                </thead>
                <tbody>
                  {filteredPermissions.map((permission, idx) => (
                    <tr
                      key={permission.id}
                      className={
                        idx % 2 === 0 ? "bg-background" : "bg-muted/20"
                      }
                    >
                      <td className="p-3 border-r sticky left-0 bg-inherit z-10">
                        <div className="space-y-1">
                          <p className="font-medium text-sm">
                            {permission.name}
                          </p>
                          {permission.description && (
                            <p className="text-xs text-muted-foreground">
                              {permission.description}
                            </p>
                          )}
                        </div>
                      </td>
                      {roles?.map((role) => (
                        <td key={role.id} className="p-3 text-center border-r">
                          {hasPermission(role.id, permission.name) ? (
                            <div className="flex items-center justify-center">
                              <div className="rounded-full bg-green-100 p-1">
                                <Check className="h-4 w-4 text-green-700" />
                              </div>
                            </div>
                          ) : (
                            <div className="flex items-center justify-center">
                              <div className="rounded-full bg-red-100 p-1">
                                <X className="h-4 w-4 text-red-700" />
                              </div>
                            </div>
                          )}
                        </td>
                      ))}
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          </CardContent>
        </Card>
      )}

      {/* Legend */}
      <Card className="border-blue-200 bg-blue-50">
        <CardContent className="pt-6">
          <div className="flex flex-wrap gap-4 text-sm">
            <div className="flex items-center gap-2">
              <div className="rounded-full bg-green-100 p-1">
                <Check className="h-4 w-4 text-green-700" />
              </div>
              <span className="text-blue-900">Permission Granted</span>
            </div>
            <div className="flex items-center gap-2">
              <div className="rounded-full bg-red-100 p-1">
                <X className="h-4 w-4 text-red-700" />
              </div>
              <span className="text-blue-900">Permission Not Granted</span>
            </div>
            <div className="flex items-center gap-2">
              <Badge variant="secondary">System</Badge>
              <span className="text-blue-900">Protected system role</span>
            </div>
          </div>
        </CardContent>
      </Card>
    </div>
  );
};
