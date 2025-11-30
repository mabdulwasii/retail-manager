import { LoadingSpinner } from "@/components/ui/loading-spinner";
import { useAuth } from "@/context/ManualAuthContext";
import { Permission } from "@/types/permissions";
import React from "react";
import { Navigate } from "react-router-dom";

interface ProtectedRouteProps {
  children: React.ReactNode;
  permissions?: Permission[];
  requireAll?: boolean;
  roles?: string[];
}

export const ProtectedRoute: React.FC<ProtectedRouteProps> = ({
  children,
  permissions = [],
  requireAll = false,
  roles = [],
}) => {
  const {
    isInitialized,
    isAuthenticated,
    hasAnyPermission,
    hasAllPermissions,
    hasRole,
    hasAnyRole,
    user,
  } = useAuth();

  if (!isInitialized) {
    return (
      <div className="flex items-center justify-center min-h-screen">
        <LoadingSpinner size="lg" />
      </div>
    );
  }

  if (!isAuthenticated || !user) {
    return <Navigate to="/login" replace />;
  }

  const arePermissionsLoading =
    user.roles.length > 0 &&
    user.roles.every((role) => role.permissions.length === 0);
  if (arePermissionsLoading && (permissions.length > 0 || roles.length > 0)) {
    return (
      <div className="flex items-center justify-center min-h-screen">
        <LoadingSpinner size="lg" />
      </div>
    );
  }

  const hasAccess = (): boolean => {
    if (permissions.length > 0) {
      if (requireAll) {
        return hasAllPermissions(permissions);
      } else {
        return hasAnyPermission(permissions);
      }
    }

    if (roles.length > 0) {
      if (requireAll) {
        return roles.every((role) => hasRole(role));
      } else {
        return hasAnyRole(roles);
      }
    }

    return true;
  };

  if (!hasAccess()) {
    return (
      <Navigate
        to="/unauthorized"
        state={{ requiredPermissions: permissions, requiredRoles: roles }}
        replace
      />
    );
  }

  return <>{children}</>;
};
