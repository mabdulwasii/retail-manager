/**
 * Unit tests for EmbeddedAuthContext
 * Tests React context provider and authentication hooks
 * Uses MSW for API mocking to test actual service integration
 */

import { describe, it, expect, beforeEach, jest, afterEach } from '@jest/globals';
import { renderHook, waitFor, act } from '@testing-library/react';
import { EmbeddedAuthProvider, useEmbeddedAuth } from '../EmbeddedAuthContext';
import { server } from '@/test/mocks/server';
import { http, HttpResponse } from 'msw';
import React from 'react';

const API_BASE_URL = 'http://localhost:8081/api';

// Valid token with far future expiration
const VALID_TOKEN = 'eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyLCJleHAiOjk5OTk5OTk5OTksInVzZXJuYW1lIjoidGVzdHVzZXIiLCJpZCI6IjEyMyIsInBlcm1pc3Npb25zIjpbIlBST0RVQ1RfUkVBRCIsIlBST0RVQ1RfV1JJVEUiXX0.C9pGXvBHfHdJsYdRfPOmfZpFw7xO7l8YxPwCqYqXzTM';

// Expired token (exp: 1)
const EXPIRED_TOKEN = 'eyJhbGciOiJIUzI1NiJ9.eyJleHAiOjF9.test';

describe('EmbeddedAuthContext', () => {
  const mockUserProfile = {
    id: '123',
    username: 'testuser',
    email: 'test@example.com',
    roles: ['USER', 'ADMIN'],
    permissions: ['USER_READ', 'USER_WRITE', 'ADMIN_READ', 'SYSTEM_ADMIN'],
  };

  const wrapper = ({ children }: { children: React.ReactNode }) => (
    <EmbeddedAuthProvider>{children}</EmbeddedAuthProvider>
  );

  beforeEach(() => {
    localStorage.clear();
    sessionStorage.clear();
    jest.useFakeTimers();
  });

  afterEach(() => {
    jest.useRealTimers();
  });

  describe('Initialization', () => {
    it('should initialize with unauthenticated state', async () => {
      const { result } = renderHook(() => useEmbeddedAuth(), { wrapper });

      await waitFor(() => {
        expect(result.current.isInitialized).toBe(true);
      });

      expect(result.current.isAuthenticated).toBe(false);
      expect(result.current.user).toBeNull();
    });

    it('should initialize with authenticated state when valid token exists', async () => {
      localStorage.setItem('embedded_access_token', VALID_TOKEN);
      localStorage.setItem('embedded_refresh_token', 'refresh-token-123');

      const { result } = renderHook(() => useEmbeddedAuth(), { wrapper });

      await waitFor(() => {
        expect(result.current.isInitialized).toBe(true);
      });

      expect(result.current.isAuthenticated).toBe(true);
      expect(result.current.user).toEqual(mockUserProfile);
    });

    it('should logout if token exists but profile fetch fails', async () => {
      localStorage.setItem('embedded_access_token', VALID_TOKEN);

      server.use(
        http.get(`${API_BASE_URL}/users/profile`, () => {
          return HttpResponse.json({ message: 'Unauthorized' }, { status: 401 });
        })
      );

      const { result } = renderHook(() => useEmbeddedAuth(), { wrapper });

      await waitFor(() => {
        expect(result.current.isInitialized).toBe(true);
      });

      expect(result.current.isAuthenticated).toBe(false);
      expect(localStorage.getItem('embedded_access_token')).toBeNull();
    });

    it('should not fetch profile if token is expired', async () => {
      localStorage.setItem('embedded_access_token', EXPIRED_TOKEN);

      const { result } = renderHook(() => useEmbeddedAuth(), { wrapper });

      await waitFor(() => {
        expect(result.current.isInitialized).toBe(true);
      });

      expect(result.current.isAuthenticated).toBe(false);
    });
  });

  describe('Login', () => {
    it('should login successfully and update state', async () => {
      const { result } = renderHook(() => useEmbeddedAuth(), { wrapper });

      await waitFor(() => {
        expect(result.current.isInitialized).toBe(true);
      });

      await act(async () => {
        await result.current.login('testuser', 'password123');
      });

      expect(result.current.isAuthenticated).toBe(true);
      expect(result.current.user).toEqual(mockUserProfile);
    });

    it('should throw error on login failure', async () => {
      const { result } = renderHook(() => useEmbeddedAuth(), { wrapper });

      await waitFor(() => {
        expect(result.current.isInitialized).toBe(true);
      });

      await expect(
        act(async () => {
          await result.current.login('wronguser', 'wrongpass');
        })
      ).rejects.toThrow();

      expect(result.current.isAuthenticated).toBe(false);
    });
  });

  describe('Registration', () => {
    it('should register successfully and update state', async () => {
      server.use(
        http.post(`${API_BASE_URL}/auth/register`, async ({ request }) => {
          const body = await request.json() as any;
          return HttpResponse.json({
            id: '456',
            username: body.username,
            email: body.email,
          }, { status: 201 });
        }),
        http.post(`${API_BASE_URL}/auth/login`, () => {
          return HttpResponse.json({
            accessToken: VALID_TOKEN,
            refreshToken: 'refresh-token-123',
          });
        })
      );

      const { result } = renderHook(() => useEmbeddedAuth(), { wrapper });

      await waitFor(() => {
        expect(result.current.isInitialized).toBe(true);
      });

      await act(async () => {
        await result.current.register(
          'newuser',
          'password123',
          'new@example.com',
          'New',
          'User'
        );
      });

      expect(result.current.isAuthenticated).toBe(true);
      expect(result.current.user).toEqual(mockUserProfile);
    });

    it('should throw error on registration failure', async () => {
      const { result } = renderHook(() => useEmbeddedAuth(), { wrapper });

      await waitFor(() => {
        expect(result.current.isInitialized).toBe(true);
      });

      await expect(
        act(async () => {
          await result.current.register('existinguser', 'password123', 'test@example.com');
        })
      ).rejects.toThrow();
    });
  });

  describe('Logout', () => {
    it('should logout and clear state', async () => {
      localStorage.setItem('embedded_access_token', VALID_TOKEN);
      localStorage.setItem('embedded_refresh_token', 'refresh-token-123');

      const { result } = renderHook(() => useEmbeddedAuth(), { wrapper });

      await waitFor(() => {
        expect(result.current.isAuthenticated).toBe(true);
      });

      await act(async () => {
        await result.current.logout();
      });

      expect(result.current.isAuthenticated).toBe(false);
      expect(result.current.user).toBeNull();
      expect(localStorage.getItem('embedded_access_token')).toBeNull();
    });
  });

  describe('Refresh User Profile', () => {
    it('should refresh user profile successfully', async () => {
      localStorage.setItem('embedded_access_token', VALID_TOKEN);

      const { result } = renderHook(() => useEmbeddedAuth(), { wrapper });

      await waitFor(() => {
        expect(result.current.isAuthenticated).toBe(true);
      });

      const updatedProfile = { ...mockUserProfile, email: 'updated@example.com' };
      server.use(
        http.get(`${API_BASE_URL}/users/profile`, () => {
          return HttpResponse.json(updatedProfile);
        })
      );

      await act(async () => {
        await result.current.refreshUserProfile();
      });

      expect(result.current.user?.email).toBe('updated@example.com');
    });

    it('should handle profile refresh failure gracefully', async () => {
      localStorage.setItem('embedded_access_token', VALID_TOKEN);

      const { result } = renderHook(() => useEmbeddedAuth(), { wrapper });

      await waitFor(() => {
        expect(result.current.isAuthenticated).toBe(true);
      });

      server.use(
        http.get(`${API_BASE_URL}/users/profile`, () => {
          return HttpResponse.json({ message: 'Network error' }, { status: 500 });
        })
      );

      await act(async () => {
        await result.current.refreshUserProfile();
      });

      // Should still be authenticated with old profile
      expect(result.current.isAuthenticated).toBe(true);
      expect(result.current.user).toEqual(mockUserProfile);
    });
  });

  describe('Role Checks', () => {
    beforeEach(async () => {
      localStorage.setItem('embedded_access_token', VALID_TOKEN);
    });

    it('should check if user has specific role', async () => {
      const { result } = renderHook(() => useEmbeddedAuth(), { wrapper });

      await waitFor(() => {
        expect(result.current.isAuthenticated).toBe(true);
      });

      expect(result.current.hasRole('USER')).toBe(true);
      expect(result.current.hasRole('ADMIN')).toBe(true);
      expect(result.current.hasRole('SUPERUSER')).toBe(false);
    });

    it('should check if user has any of the specified roles', async () => {
      const { result } = renderHook(() => useEmbeddedAuth(), { wrapper });

      await waitFor(() => {
        expect(result.current.isAuthenticated).toBe(true);
      });

      expect(result.current.hasAnyRole(['USER', 'SUPERUSER'])).toBe(true);
      expect(result.current.hasAnyRole(['SUPERUSER', 'GUEST'])).toBe(false);
    });

    it('should check if user has all specified roles', async () => {
      const { result } = renderHook(() => useEmbeddedAuth(), { wrapper });

      await waitFor(() => {
        expect(result.current.isAuthenticated).toBe(true);
      });

      expect(result.current.hasAllRoles(['USER', 'ADMIN'])).toBe(true);
      expect(result.current.hasAllRoles(['USER', 'SUPERUSER'])).toBe(false);
    });
  });

  describe('Permission Checks', () => {
    it('should check if user has specific permission', async () => {
      const userWithoutSystemAdmin = {
        id: '123',
        username: 'testuser',
        email: 'test@example.com',
        roles: ['USER'],
        permissions: ['USER_READ', 'USER_WRITE'],
      };

      server.use(
        http.get(`${API_BASE_URL}/users/profile`, () => {
          return HttpResponse.json(userWithoutSystemAdmin);
        })
      );

      localStorage.setItem('embedded_access_token', VALID_TOKEN);

      const { result } = renderHook(() => useEmbeddedAuth(), { wrapper });

      await waitFor(() => {
        expect(result.current.isAuthenticated).toBe(true);
      });

      expect(result.current.hasPermission('USER_READ')).toBe(true);
      expect(result.current.hasPermission('USER_DELETE')).toBe(false);
    });

    it('should grant all permissions if user has SYSTEM_ADMIN', async () => {
      localStorage.setItem('embedded_access_token', VALID_TOKEN);

      const { result } = renderHook(() => useEmbeddedAuth(), { wrapper });

      await waitFor(() => {
        expect(result.current.isAuthenticated).toBe(true);
      });

      // User has SYSTEM_ADMIN permission
      expect(result.current.hasPermission('ANY_PERMISSION')).toBe(true);
      expect(result.current.hasPermission('SUPER_SECRET_PERMISSION')).toBe(true);
    });

    it('should check if user has any of the specified permissions', async () => {
      localStorage.setItem('embedded_access_token', VALID_TOKEN);

      const { result } = renderHook(() => useEmbeddedAuth(), { wrapper });

      await waitFor(() => {
        expect(result.current.isAuthenticated).toBe(true);
      });

      expect(result.current.hasAnyPermission(['USER_READ', 'USER_DELETE'])).toBe(true);
      expect(result.current.hasAnyPermission(['USER_DELETE', 'SYSTEM_DELETE'])).toBe(true);
    });

    it('should check if user has all specified permissions', async () => {
      localStorage.setItem('embedded_access_token', VALID_TOKEN);

      const { result } = renderHook(() => useEmbeddedAuth(), { wrapper });

      await waitFor(() => {
        expect(result.current.isAuthenticated).toBe(true);
      });

      expect(result.current.hasAllPermissions(['USER_READ', 'USER_WRITE'])).toBe(true);
      expect(result.current.hasAllPermissions(['USER_READ', 'NON_EXISTENT'])).toBe(true);
    });

    it('should return false for permissions when user is null', async () => {
      const { result } = renderHook(() => useEmbeddedAuth(), { wrapper });

      await waitFor(() => {
        expect(result.current.isInitialized).toBe(true);
      });

      expect(result.current.hasPermission('USER_READ')).toBe(false);
      expect(result.current.hasAnyPermission(['USER_READ'])).toBe(false);
      expect(result.current.hasAllPermissions(['USER_READ'])).toBe(false);
    });
  });

  describe('Get Token', () => {
    it('should return current access token', async () => {
      localStorage.setItem('embedded_access_token', VALID_TOKEN);

      const { result } = renderHook(() => useEmbeddedAuth(), { wrapper });

      await waitFor(() => {
        expect(result.current.isAuthenticated).toBe(true);
      });

      expect(result.current.getToken()).toBe(VALID_TOKEN);
    });

    it('should return null when no token exists', async () => {
      const { result } = renderHook(() => useEmbeddedAuth(), { wrapper });

      await waitFor(() => {
        expect(result.current.isInitialized).toBe(true);
      });

      expect(result.current.getToken()).toBeNull();
    });
  });

  describe('Token Refresh Interval', () => {
    it('should set up token refresh interval when authenticated', async () => {
      localStorage.setItem('embedded_access_token', VALID_TOKEN);

      renderHook(() => useEmbeddedAuth(), { wrapper });

      await waitFor(() => {
        // Context should be initialized
      });

      // Fast-forward 1 minute
      await act(async () => {
        jest.advanceTimersByTime(60000);
      });

      // Should still be authenticated (token not expired)
      expect(localStorage.getItem('embedded_access_token')).toBeTruthy();
    });

    it('should refresh expired token automatically', async () => {
      localStorage.setItem('embedded_access_token', VALID_TOKEN);
      localStorage.setItem('embedded_refresh_token', 'valid-refresh-token');

      const { result } = renderHook(() => useEmbeddedAuth(), { wrapper });

      await waitFor(() => {
        expect(result.current.isAuthenticated).toBe(true);
      });

      // Simulate token becoming expired by setting an expired token
      localStorage.setItem('embedded_access_token', EXPIRED_TOKEN);

      // Fast-forward
      await act(async () => {
        jest.advanceTimersByTime(60000);
        await Promise.resolve();
      });

      // Verify refresh endpoint was called
      await waitFor(() => {
        const newToken = localStorage.getItem('embedded_access_token');
        expect(newToken).not.toBe(EXPIRED_TOKEN);
      }, { timeout: 2000 });
    });

    it('should logout on refresh failure', async () => {
      localStorage.setItem('embedded_access_token', VALID_TOKEN);
      localStorage.setItem('embedded_refresh_token', 'invalid-refresh-token');

      const { result } = renderHook(() => useEmbeddedAuth(), { wrapper });

      await waitFor(() => {
        expect(result.current.isAuthenticated).toBe(true);
      });

      // Simulate token expiration
      localStorage.setItem('embedded_access_token', EXPIRED_TOKEN);

      await act(async () => {
        jest.advanceTimersByTime(60000);
      });

      await waitFor(() => {
        expect(result.current.isAuthenticated).toBe(false);
      }, { timeout: 2000 });
    });
  });

  describe('Context Error Handling', () => {
    it('should throw error when useEmbeddedAuth is used outside provider', () => {
      const originalError = console.error;
      console.error = jest.fn();

      expect(() => {
        renderHook(() => useEmbeddedAuth());
      }).toThrow('useEmbeddedAuth must be used within an EmbeddedAuthProvider');

      console.error = originalError;
    });
  });
});
