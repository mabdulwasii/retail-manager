/**
 * Embedded mode authentication service using JWT tokens.
 * Replaces Keycloak authentication in standalone embedded deployments.
 */

import api from "@/lib/axios";

export interface LoginCredentials {
  username: string;
  password: string;
}

export interface RegisterData extends LoginCredentials {
  email: string;
  firstName?: string;
  lastName?: string;
}

export interface AuthTokens {
  accessToken: string;
  refreshToken: string;
}

export interface UserProfile {
  id: string;
  username: string;
  email?: string;
  firstName?: string;
  lastName?: string;
  fullName?: string;
  phoneNumber?: string;
  status?: string;
  roles: Array<{
    id: string;
    name: string;
    description: string;
    isSystem: boolean;
    permissions: string[];
  }>;
  tenantId?: string;
  shopId?: string;
  createdAt?: string;
  updatedAt?: string;
}

class EmbeddedAuthService {
  private static readonly TOKEN_KEY = 'embedded_access_token';
  private static readonly REFRESH_TOKEN_KEY = 'embedded_refresh_token';

  /**
   * Login with username and password
   */
  async login(credentials: LoginCredentials): Promise<AuthTokens> {
    const response = await api.post<AuthTokens>('/auth/login', credentials);

    // Store tokens
    this.setTokens(response.data);

    return response.data;
  }

  /**
   * Register new user (for embedded mode)
   */
  async register(data: RegisterData): Promise<AuthTokens> {
    const response = await api.post<AuthTokens>('/auth/register', data);

    // Store tokens
    this.setTokens(response.data);

    return response.data;
  }

  /**
   * Refresh access token using refresh token
   */
  async refreshToken(): Promise<AuthTokens> {
    const refreshToken = this.getRefreshToken();

    if (!refreshToken) {
      throw new Error('No refresh token available');
    }

    const response = await api.post<AuthTokens>('/auth/refresh', {
      refreshToken
    });

    this.setTokens(response.data);

    return response.data;
  }

  /**
   * Get current user profile
   */
  async getProfile(): Promise<UserProfile> {
    const response = await api.get<UserProfile>('/users/profile');
    return response.data;
  }

  /**
   * Logout - clear local tokens
   */
  logout(): void {
    localStorage.removeItem(EmbeddedAuthService.TOKEN_KEY);
    localStorage.removeItem(EmbeddedAuthService.REFRESH_TOKEN_KEY);
  }

  /**
   * Store tokens in localStorage
   */
  private setTokens(tokens: AuthTokens): void {
    localStorage.setItem(EmbeddedAuthService.TOKEN_KEY, tokens.accessToken);
    localStorage.setItem(EmbeddedAuthService.REFRESH_TOKEN_KEY, tokens.refreshToken);
  }

  /**
   * Get stored access token
   */
  getAccessToken(): string | null {
    return localStorage.getItem(EmbeddedAuthService.TOKEN_KEY);
  }

  /**
   * Get stored refresh token
   */
  getRefreshToken(): string | null {
    return localStorage.getItem(EmbeddedAuthService.REFRESH_TOKEN_KEY);
  }

  /**
   * Check if user is authenticated (has valid token)
   */
  isAuthenticated(): boolean {
    return !!this.getAccessToken();
  }

  /**
   * Parse JWT token to extract claims
   */
  parseToken(token: string): any {
    try {
      const base64Url = token.split('.')[1];
      const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/');
      const jsonPayload = decodeURIComponent(
        atob(base64)
          .split('')
          .map((c) => '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2))
          .join('')
      );
      return JSON.parse(jsonPayload);
    } catch (error) {
      console.error('Failed to parse JWT token:', error);
      return null;
    }
  }

  /**
   * Check if token is expired
   */
  isTokenExpired(token: string): boolean {
    const parsed = this.parseToken(token);
    if (!parsed || !parsed.exp) return true;

    const now = Math.floor(Date.now() / 1000);
    return parsed.exp < now;
  }
}

export default new EmbeddedAuthService();
