import configService from "@/config/runtime-config";
import api, { setTokenProvider } from "@/lib/axios";
import Keycloak from "keycloak-js";
import React, { createContext, useCallback, useContext, useState } from "react";

// Role with Permissions
interface Role {
  id: string;
  name: string;
  description: string;
  isSystem: boolean;
  permissions: string[];
}

// User Profile Type
interface UserProfile {
  id: string;
  username: string;
  email?: string;
  firstName?: string;
  lastName?: string;
  fullName?: string;
  phoneNumber?: string;
  status?: string;
  roles: Role[];
  tenantId?: string;
  shopId?: string;
  createdAt?: string;
  updatedAt?: string;
}

// Auth Context Type
interface AuthContextType {
  isAuthenticated: boolean;
  isInitialized: boolean;
  user: UserProfile | null;
  keycloak: Keycloak | null;
  initializeKeycloak: () => Promise<Keycloak>;
  login: () => void;
  logout: () => Promise<void>;
  hasRole: (role: string) => boolean;
  hasAnyRole: (roles: string[]) => boolean;
  hasAllRoles: (roles: string[]) => boolean;
  hasPermission: (permission: string) => boolean;
  hasAnyPermission: (permissions: string[]) => boolean;
  hasAllPermissions: (permissions: string[]) => boolean;
  getToken: () => string | undefined;
  refreshUserProfile: () => Promise<void>;
}

// Create Auth Context
const AuthContext = createContext<AuthContextType | undefined>(undefined);

// Auth Provider Component
export const ManualAuthProvider: React.FC<{ children: React.ReactNode }> = ({
  children,
}) => {
  const [keycloak, setKeycloak] = useState<Keycloak | null>(null);
  const [isInitialized, setIsInitialized] = useState(false);
  const [isAuthenticated, setIsAuthenticated] = useState(false);
  const [user, setUser] = useState<UserProfile | null>(null);
  const initializingRef = React.useRef(false);

  // Fetch full user profile from backend API with roles and permissions
  const fetchUserProfile = useCallback(async (): Promise<UserProfile | null> => {
    try {
      const response = await api.get('/users/profile');
      return response.data;
    } catch (error) {
      console.error('Failed to fetch user profile:', error);
      return null;
    }
  }, []);

  // Extract basic user info from token (for initial load)
  const getUserProfileFromToken = useCallback((kc: Keycloak): UserProfile | null => {
    if (!kc.tokenParsed) return null;

    const token = kc.tokenParsed as any;
    const realmRoles = token.realm_access?.roles || [];
    const resourceRoles = token.resource_access?.[kc.clientId!]?.roles || [];
    const roleNames = [...realmRoles, ...resourceRoles];

    // Create temporary role objects without permissions
    // These will be replaced with full roles from backend
    const tempRoles: Role[] = roleNames.map(name => ({
      id: `temp-${name}`,
      name,
      description: '',
      isSystem: true,
      permissions: []
    }));

    return {
      id: token.sub,
      username: token.preferred_username || token.email,
      email: token.email,
      firstName: token.given_name,
      lastName: token.family_name,
      fullName: token.name,
      roles: tempRoles,
      tenantId: token.tenant_id,
      shopId: token.shop_id,
    };
  }, []);

  // Initialize Keycloak on mount to handle OAuth callback
  React.useEffect(() => {
    // Skip Keycloak initialization in embedded mode
    if (configService.isEmbeddedMode) {
      setIsInitialized(true);
      return;
    }

    if (initializingRef.current || keycloak) return;
    initializingRef.current = true;

    const init = async () => {
      const kc = new Keycloak({
        url: configService.keycloakUrl,
        realm: configService.keycloakRealm,
        clientId: configService.keycloakClientId,
      });

      try {
        // If there's a hash with OAuth params but we've already retried, clear it
        const hasRetried = sessionStorage.getItem("kc_retry");
        if (window.location.hash && hasRetried) {
          console.log("Already retried once, clearing hash and continuing...");
          window.location.hash = "";
          sessionStorage.removeItem("kc_retry");
        }

        const authenticated = await kc.init({
          onLoad: "check-sso",
          checkLoginIframe: false,
          pkceMethod: "S256",
          enableLogging: true,
          responseMode: "fragment",
          flow: "standard",
          silentCheckSsoFallback: false,
          useNonce: false,
        });

        // Clear retry flag on success
        sessionStorage.removeItem("kc_retry");

        setKeycloak(kc);
        setIsInitialized(true);

        if (authenticated) {
          setIsAuthenticated(true);
          // Set basic user info from token first (for immediate UI update)
          setUser(getUserProfileFromToken(kc));
          setTokenProvider(() => kc.token);

          // Store tokens
          if (kc.token && kc.refreshToken) {
            localStorage.setItem("keycloak_token", kc.token);
            localStorage.setItem("keycloak_access_token", kc.token);
            localStorage.setItem("keycloak_refresh_token", kc.refreshToken);
            localStorage.setItem("keycloak_id_token", kc.idToken || "");
          }

          // Fetch full profile with roles and permissions from backend
          fetchUserProfile().then(profile => {
            if (profile) {
              setUser(profile);
              console.log('User profile loaded with permissions:', profile.roles);
            }
          });

          // Set up token refresh with cleanup
          const refreshInterval = setInterval(async () => {
            try {
              // Check if still authenticated and has refresh token
              if (!kc.authenticated || !kc.refreshToken) {
                console.warn(
                  "Token refresh skipped: not authenticated or no refresh token"
                );
                clearInterval(refreshInterval);
                return;
              }

              // Try to refresh token if it expires in less than 70 seconds
              const refreshed = await kc.updateToken(70);

              if (refreshed && kc.token && kc.refreshToken) {
                console.log("Token refreshed successfully");
                localStorage.setItem("keycloak_token", kc.token);
                localStorage.setItem("keycloak_access_token", kc.token);
                localStorage.setItem("keycloak_refresh_token", kc.refreshToken);
                localStorage.setItem("keycloak_id_token", kc.idToken || "");
                // Refresh user profile from backend
                fetchUserProfile().then(profile => {
                  if (profile) setUser(profile);
                });
              } else {
                // Token doesn't need refresh yet (still valid for more than 5 minutes)
                console.log("Token still valid, no refresh needed");
              }
            } catch (error) {
              console.error(
                "Failed to refresh token:",
                error || "Unknown error"
              );

              clearInterval(refreshInterval);

              setIsAuthenticated(false);
              setUser(null);
              localStorage.clear();
              // await kc.logout();
            }
          }, 60000);

          // Store interval ID for cleanup
          (kc as any).__refreshInterval = refreshInterval;
        }
      } catch (error) {
        console.error("Failed to initialize Keycloak:", error);

        // If there's an OAuth callback error and we haven't retried yet, clear URL and retry once
        const hasRetried = sessionStorage.getItem("kc_retry");
        if (window.location.hash && !hasRetried) {
          console.log("OAuth callback error, clearing hash and retrying...");
          sessionStorage.setItem("kc_retry", "true");
          window.location.hash = "";
          window.location.reload();
          return;
        }

        // If already retried or no hash, just mark as initialized
        sessionStorage.removeItem("kc_retry");
        setKeycloak(kc);
        setIsInitialized(true);
      }
    };

    init();
  }, [getUserProfileFromToken, fetchUserProfile]);

  // Get or wait for Keycloak instance
  const initializeKeycloak = useCallback(async (): Promise<Keycloak> => {
    if (keycloak) return keycloak;

    // Wait for initialization to complete
    return new Promise((resolve, reject) => {
      const checkInterval = setInterval(() => {
        if (keycloak) {
          clearInterval(checkInterval);
          resolve(keycloak);
        }
      }, 100);

      // Timeout after 10 seconds
      setTimeout(() => {
        clearInterval(checkInterval);
        reject(new Error("Keycloak initialization timeout"));
      }, 10000);
    });
  }, [keycloak]);

  // Login - redirect to Keycloak
  const login = useCallback(async () => {
    try {
      const kc = await initializeKeycloak();
      await kc.login({
        redirectUri: window.location.origin + "/redirect",
      });
    } catch (error) {
      console.error("Login failed:", error);
      // Redirect to home page if Keycloak init fails
      window.location.href = "/";
    }
  }, [initializeKeycloak]);

  // Logout
  const logout = useCallback(async () => {
    // Clear token refresh interval
    if (keycloak && (keycloak as any).__refreshInterval) {
      clearInterval((keycloak as any).__refreshInterval);
      console.log("Token refresh interval cleared");
    }

    if (keycloak) {
      await keycloak.logout();
    }

    // Clear state
    setIsAuthenticated(false);
    setUser(null);
    setTokenProvider(() => undefined);

    // Clear localStorage
    localStorage.removeItem("keycloak_token");
    localStorage.removeItem("keycloak_access_token");
    localStorage.removeItem("keycloak_refresh_token");
    localStorage.removeItem("keycloak_id_token");
  }, [keycloak]);

  // Refresh user profile from backend
  const refreshUserProfile = useCallback(async () => {
    const profile = await fetchUserProfile();
    if (profile) {
      setUser(profile);
    }
  }, [fetchUserProfile]);

  // Role checking functions
  const hasRole = useCallback(
    (role: string): boolean => {
      return user?.roles?.some(r => r.name === role) || false;
    },
    [user]
  );

  const hasAnyRole = useCallback(
    (roles: string[]): boolean => {
      return roles.some((role) => hasRole(role));
    },
    [hasRole]
  );

  const hasAllRoles = useCallback(
    (roles: string[]): boolean => {
      return roles.every((role) => hasRole(role));
    },
    [hasRole]
  );

  // Permission checking functions
  const hasPermission = useCallback(
    (permission: string): boolean => {
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
    },
    [user]
  );

  const hasAnyPermission = useCallback(
    (permissions: string[]): boolean => {
      return permissions.some((permission) => hasPermission(permission));
    },
    [hasPermission]
  );

  const hasAllPermissions = useCallback(
    (permissions: string[]): boolean => {
      return permissions.every((permission) => hasPermission(permission));
    },
    [hasPermission]
  );

  const getToken = useCallback((): string | undefined => {
    return keycloak?.token;
  }, [keycloak]);

  const contextValue: AuthContextType = {
    isAuthenticated,
    isInitialized,
    user,
    keycloak,
    initializeKeycloak,
    login,
    logout,
    hasRole,
    hasAnyRole,
    hasAllRoles,
    hasPermission,
    hasAnyPermission,
    hasAllPermissions,
    getToken,
    refreshUserProfile,
  };

  return (
    <AuthContext.Provider value={contextValue}>{children}</AuthContext.Provider>
  );
};

// Custom hook to use auth context
export const useAuth = (): AuthContextType => {
  const context = useContext(AuthContext);
  if (context === undefined) {
    throw new Error("useAuth must be used within a ManualAuthProvider");
  }
  return context;
};

// Export types
export type { Role, UserProfile, AuthContextType };

// Export for backward compatibility
export const AuthProvider = ManualAuthProvider;
export default ManualAuthProvider;
