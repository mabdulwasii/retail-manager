/**
 * Embedded mode authentication context using JWT.
 * Provides authentication state and methods for embedded deployments.
 */

import React, { createContext, useCallback, useContext, useEffect, useMemo, useState } from "react";
import embeddedAuthService, { UserProfile } from "@/services/EmbeddedAuthService";
import { setTokenProvider } from "@/lib/axios";

interface AuthContextType {
  isAuthenticated: boolean;
  isInitialized: boolean;
  user: UserProfile | null;
  login: (username: string, password: string) => Promise<void>;
  register: (username: string, password: string, email: string, firstName?: string, lastName?: string) => Promise<void>;
  logout: () => Promise<void>;
  refreshUserProfile: () => Promise<void>;
  hasRole: (role: string) => boolean;
  hasAnyRole: (roles: string[]) => boolean;
  hasAllRoles: (roles: string[]) => boolean;
  hasPermission: (permission: string) => boolean;
  hasAnyPermission: (permissions: string[]) => boolean;
  hasAllPermissions: (permissions: string[]) => boolean;
  getToken: () => string | null;
}

const EmbeddedAuthContext = createContext<AuthContextType | undefined>(undefined);

export const EmbeddedAuthProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const [isAuthenticated, setIsAuthenticated] = useState(false);
  const [isInitialized, setIsInitialized] = useState(false);
  const [user, setUser] = useState<UserProfile | null>(null);

  // Initialize auth state on mount
  useEffect(() => {
    const initialize = async () => {
      const token = embeddedAuthService.getAccessToken();

      if (token && !embeddedAuthService.isTokenExpired(token)) {
        // Valid token exists, fetch user profile
        try {
          setTokenProvider(() => embeddedAuthService.getAccessToken());
          const profile = await embeddedAuthService.getProfile();
          setUser(profile);
          setIsAuthenticated(true);
        } catch (error) {
          console.error('Failed to fetch user profile:', error);
          embeddedAuthService.logout();
          setIsAuthenticated(false);
        }
      }

      setIsInitialized(true);
    };

    initialize();
  }, []);

  // Set up token refresh interval
  useEffect(() => {
    if (!isAuthenticated) return;

    const refreshInterval = setInterval(async () => {
      try {
        const token = embeddedAuthService.getAccessToken();

        if (token && embeddedAuthService.isTokenExpired(token)) {
          console.log('Token expired, refreshing...');
          await embeddedAuthService.refreshToken();
          await refreshUserProfile();
        }
      } catch (error) {
        console.error('Token refresh failed:', error);
        // If refresh fails, logout
        await logout();
      }
    }, 60000); // Check every minute

    return () => clearInterval(refreshInterval);
  }, [isAuthenticated]);

  const login = useCallback(async (username: string, password: string) => {
    try {
      await embeddedAuthService.login({ username, password });
      setTokenProvider(() => embeddedAuthService.getAccessToken());

      // Fetch user profile
      const profile = await embeddedAuthService.getProfile();
      setUser(profile);
      setIsAuthenticated(true);
    } catch (error: any) {
      console.error('Login failed:', error);
      throw new Error(error.response?.data?.message || 'Login failed');
    }
  }, []);

  const register = useCallback(async (
    username: string,
    password: string,
    email: string,
    firstName?: string,
    lastName?: string
  ) => {
    try {
      await embeddedAuthService.register({
        username,
        password,
        email,
        firstName,
        lastName
      });
      setTokenProvider(() => embeddedAuthService.getAccessToken());

      // Fetch user profile
      const profile = await embeddedAuthService.getProfile();
      setUser(profile);
      setIsAuthenticated(true);
    } catch (error: any) {
      console.error('Registration failed:', error);
      throw new Error(error.response?.data?.message || 'Registration failed');
    }
  }, []);

  const logout = useCallback(async () => {
    embeddedAuthService.logout();
    setIsAuthenticated(false);
    setUser(null);
    setTokenProvider(() => undefined);
  }, []);

  const refreshUserProfile = useCallback(async () => {
    try {
      const profile = await embeddedAuthService.getProfile();
      setUser(profile);
    } catch (error) {
      console.error('Failed to refresh user profile:', error);
    }
  }, []);

  const hasRole = useCallback((role: string): boolean => {
    return user?.roles?.some(r => r.name === role) || false;
  }, [user]);

  const hasAnyRole = useCallback((roles: string[]): boolean => {
    return roles.some((role) => hasRole(role));
  }, [hasRole]);

  const hasAllRoles = useCallback((roles: string[]): boolean => {
    return roles.every((role) => hasRole(role));
  }, [hasRole]);

  const hasPermission = useCallback((permission: string): boolean => {
    if (!user?.roles) return false;

    // Check if user has SYSTEM_ADMIN permission (has all permissions)
    const hasSystemAdmin = user.roles.some(role =>
      role.permissions.includes('SYSTEM_ADMIN')
    );
    if (hasSystemAdmin) return true;

    // Check if any of user's roles has this permission
    return user.roles.some(role =>
      role.permissions.includes(permission)
    );
  }, [user]);

  const hasAnyPermission = useCallback((permissions: string[]): boolean => {
    return permissions.some((permission) => hasPermission(permission));
  }, [hasPermission]);

  const hasAllPermissions = useCallback((permissions: string[]): boolean => {
    return permissions.every((permission) => hasPermission(permission));
  }, [hasPermission]);

  const getToken = useCallback((): string | null => {
    return embeddedAuthService.getAccessToken();
  }, []);

  const contextValue: AuthContextType = useMemo(() => ({
    isAuthenticated,
    isInitialized,
    user,
    login,
    register,
    logout,
    refreshUserProfile,
    hasRole,
    hasAnyRole,
    hasAllRoles,
    hasPermission,
    hasAnyPermission,
    hasAllPermissions,
    getToken,
  }), [
    isAuthenticated,
    isInitialized,
    user,
    login,
    register,
    logout,
    refreshUserProfile,
    hasRole,
    hasAnyRole,
    hasAllRoles,
    hasPermission,
    hasAnyPermission,
    hasAllPermissions,
    getToken,
  ]);

  return (
    <EmbeddedAuthContext.Provider value={contextValue}>
      {children}
    </EmbeddedAuthContext.Provider>
  );
};

export const useEmbeddedAuth = (): AuthContextType => {
  const context = useContext(EmbeddedAuthContext);
  if (context === undefined) {
    throw new Error("useEmbeddedAuth must be used within an EmbeddedAuthProvider");
  }
  return context;
};
