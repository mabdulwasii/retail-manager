/**
 * Unit tests for EmbeddedAuthContext
 * Tests React context provider and authentication hooks
 */

import { describe, it, expect, beforeEach, jest } from '@jest/globals';
import { renderHook, waitFor, act } from '@testing-library/react';
import { EmbeddedAuthProvider, useEmbeddedAuth } from '../EmbeddedAuthContext';
import embeddedAuthService from '@/services/EmbeddedAuthService';
import { setTokenProvider } from '@/lib/axios';
import React from 'react';

// Mock dependencies
jest.mock('@/services/EmbeddedAuthService');
jest.mock('@/lib/axios');

const mockedAuthService = embeddedAuthService as jest.Mocked<typeof embeddedAuthService>;
const mockedSetTokenProvider = setTokenProvider as jest.MockedFunction<typeof setTokenProvider>;

describe('EmbeddedAuthContext', () => {
  const mockTokens = {
    accessToken: 'access.token.here',
    refreshToken: 'refresh.token.here',
  };

  const mockUserProfile = {
    id: '123',
    username: 'testuser',
    email: 'test@example.com',
    roles: [
      {
        id: '1',
        name: 'ROLE_USER',
        permissions: ['USER_READ', 'USER_WRITE'],
      },
      {
        id: '2',
        name: 'ROLE_ADMIN',
        permissions: ['ADMIN_READ', 'SYSTEM_ADMIN'],
      },
    ],
  };

  const wrapper = ({ children }: { children: React.ReactNode }) => (
    <EmbeddedAuthProvider>{children}</EmbeddedAuthProvider>
  );

  beforeEach(() => {
    jest.clearAllMocks();
    jest.useFakeTimers();
  });

  afterEach(() => {
    jest.useRealTimers();
  });

  describe('Initialization', () => {
    it('should initialize with unauthenticated state', async () => {
      mockedAuthService.getAccessToken.mockReturnValue(null);

      const { result } = renderHook(() => useEmbeddedAuth(), { wrapper });

      await waitFor(() => {
        expect(result.current.isInitialized).toBe(true);
      });

      expect(result.current.isAuthenticated).toBe(false);
      expect(result.current.user).toBeNull();
    });

    it('should initialize with authenticated state when valid token exists', async () => {
      mockedAuthService.getAccessToken.mockReturnValue(mockTokens.accessToken);
      mockedAuthService.isTokenExpired.mockReturnValue(false);
      mockedAuthService.getProfile.mockResolvedValue(mockUserProfile);

      const { result } = renderHook(() => useEmbeddedAuth(), { wrapper });

      await waitFor(() => {
        expect(result.current.isInitialized).toBe(true);
      });

      expect(result.current.isAuthenticated).toBe(true);
      expect(result.current.user).toEqual(mockUserProfile);
      expect(mockedSetTokenProvider).toHaveBeenCalled();
    });

    it('should logout if token exists but profile fetch fails', async () => {
      mockedAuthService.getAccessToken.mockReturnValue(mockTokens.accessToken);
      mockedAuthService.isTokenExpired.mockReturnValue(false);
      mockedAuthService.getProfile.mockRejectedValue(new Error('Unauthorized'));

      const { result } = renderHook(() => useEmbeddedAuth(), { wrapper });

      await waitFor(() => {
        expect(result.current.isInitialized).toBe(true);
      });

      expect(result.current.isAuthenticated).toBe(false);
      expect(mockedAuthService.logout).toHaveBeenCalled();
    });

    it('should not fetch profile if token is expired', async () => {
      mockedAuthService.getAccessToken.mockReturnValue(mockTokens.accessToken);
      mockedAuthService.isTokenExpired.mockReturnValue(true);

      const { result } = renderHook(() => useEmbeddedAuth(), { wrapper });

      await waitFor(() => {
        expect(result.current.isInitialized).toBe(true);
      });

      expect(mockedAuthService.getProfile).not.toHaveBeenCalled();
      expect(result.current.isAuthenticated).toBe(false);
    });
  });

  describe('Login', () => {
    it('should login successfully and update state', async () => {
      mockedAuthService.getAccessToken.mockReturnValue(null);
      mockedAuthService.login.mockResolvedValue(mockTokens);
      mockedAuthService.getProfile.mockResolvedValue(mockUserProfile);

      const { result } = renderHook(() => useEmbeddedAuth(), { wrapper });

      await waitFor(() => {
        expect(result.current.isInitialized).toBe(true);
      });

      await act(async () => {
        await result.current.login('testuser', 'password123');
      });

      expect(mockedAuthService.login).toHaveBeenCalledWith({
        username: 'testuser',
        password: 'password123',
      });
      expect(result.current.isAuthenticated).toBe(true);
      expect(result.current.user).toEqual(mockUserProfile);
      expect(mockedSetTokenProvider).toHaveBeenCalled();
    });

    it('should throw error on login failure', async () => {
      mockedAuthService.getAccessToken.mockReturnValue(null);
      mockedAuthService.login.mockRejectedValue({
        response: { data: { message: 'Invalid credentials' } },
      });

      const { result } = renderHook(() => useEmbeddedAuth(), { wrapper });

      await waitFor(() => {
        expect(result.current.isInitialized).toBe(true);
      });

      await expect(
        act(async () => {
          await result.current.login('testuser', 'wrongpassword');
        })
      ).rejects.toThrow('Invalid credentials');

      expect(result.current.isAuthenticated).toBe(false);
    });
  });

  describe('Registration', () => {
    it('should register successfully and update state', async () => {
      mockedAuthService.getAccessToken.mockReturnValue(null);
      mockedAuthService.register.mockResolvedValue(mockTokens);
      mockedAuthService.getProfile.mockResolvedValue(mockUserProfile);

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

      expect(mockedAuthService.register).toHaveBeenCalledWith({
        username: 'newuser',
        password: 'password123',
        email: 'new@example.com',
        firstName: 'New',
        lastName: 'User',
      });
      expect(result.current.isAuthenticated).toBe(true);
      expect(result.current.user).toEqual(mockUserProfile);
    });

    it('should throw error on registration failure', async () => {
      mockedAuthService.getAccessToken.mockReturnValue(null);
      mockedAuthService.register.mockRejectedValue({
        response: { data: { message: 'Username already exists' } },
      });

      const { result } = renderHook(() => useEmbeddedAuth(), { wrapper });

      await waitFor(() => {
        expect(result.current.isInitialized).toBe(true);
      });

      await expect(
        act(async () => {
          await result.current.register('existing', 'password123', 'test@example.com');
        })
      ).rejects.toThrow('Username already exists');
    });
  });

  describe('Logout', () => {
    it('should logout and clear state', async () => {
      mockedAuthService.getAccessToken.mockReturnValue(mockTokens.accessToken);
      mockedAuthService.isTokenExpired.mockReturnValue(false);
      mockedAuthService.getProfile.mockResolvedValue(mockUserProfile);

      const { result } = renderHook(() => useEmbeddedAuth(), { wrapper });

      await waitFor(() => {
        expect(result.current.isAuthenticated).toBe(true);
      });

      await act(async () => {
        await result.current.logout();
      });

      expect(mockedAuthService.logout).toHaveBeenCalled();
      expect(result.current.isAuthenticated).toBe(false);
      expect(result.current.user).toBeNull();
      expect(mockedSetTokenProvider).toHaveBeenCalledWith(expect.any(Function));
    });
  });

  describe('Refresh User Profile', () => {
    it('should refresh user profile successfully', async () => {
      mockedAuthService.getAccessToken.mockReturnValue(mockTokens.accessToken);
      mockedAuthService.isTokenExpired.mockReturnValue(false);
      mockedAuthService.getProfile.mockResolvedValue(mockUserProfile);

      const { result } = renderHook(() => useEmbeddedAuth(), { wrapper });

      await waitFor(() => {
        expect(result.current.isAuthenticated).toBe(true);
      });

      const updatedProfile = { ...mockUserProfile, email: 'updated@example.com' };
      mockedAuthService.getProfile.mockResolvedValue(updatedProfile);

      await act(async () => {
        await result.current.refreshUserProfile();
      });

      expect(result.current.user?.email).toBe('updated@example.com');
    });

    it('should handle profile refresh failure gracefully', async () => {
      mockedAuthService.getAccessToken.mockReturnValue(mockTokens.accessToken);
      mockedAuthService.isTokenExpired.mockReturnValue(false);
      mockedAuthService.getProfile
        .mockResolvedValueOnce(mockUserProfile)
        .mockRejectedValueOnce(new Error('Network error'));

      const { result } = renderHook(() => useEmbeddedAuth(), { wrapper });

      await waitFor(() => {
        expect(result.current.isAuthenticated).toBe(true);
      });

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
      mockedAuthService.getAccessToken.mockReturnValue(mockTokens.accessToken);
      mockedAuthService.isTokenExpired.mockReturnValue(false);
      mockedAuthService.getProfile.mockResolvedValue(mockUserProfile);
    });

    it('should check if user has specific role', async () => {
      const { result } = renderHook(() => useEmbeddedAuth(), { wrapper });

      await waitFor(() => {
        expect(result.current.isAuthenticated).toBe(true);
      });

      expect(result.current.hasRole('ROLE_USER')).toBe(true);
      expect(result.current.hasRole('ROLE_ADMIN')).toBe(true);
      expect(result.current.hasRole('ROLE_SUPERUSER')).toBe(false);
    });

    it('should check if user has any of the specified roles', async () => {
      const { result } = renderHook(() => useEmbeddedAuth(), { wrapper });

      await waitFor(() => {
        expect(result.current.isAuthenticated).toBe(true);
      });

      expect(result.current.hasAnyRole(['ROLE_USER', 'ROLE_SUPERUSER'])).toBe(true);
      expect(result.current.hasAnyRole(['ROLE_SUPERUSER', 'ROLE_GUEST'])).toBe(false);
    });

    it('should check if user has all specified roles', async () => {
      const { result } = renderHook(() => useEmbeddedAuth(), { wrapper });

      await waitFor(() => {
        expect(result.current.isAuthenticated).toBe(true);
      });

      expect(result.current.hasAllRoles(['ROLE_USER', 'ROLE_ADMIN'])).toBe(true);
      expect(result.current.hasAllRoles(['ROLE_USER', 'ROLE_SUPERUSER'])).toBe(false);
    });
  });

  describe('Permission Checks', () => {
    beforeEach(async () => {
      mockedAuthService.getAccessToken.mockReturnValue(mockTokens.accessToken);
      mockedAuthService.isTokenExpired.mockReturnValue(false);
      mockedAuthService.getProfile.mockResolvedValue(mockUserProfile);
    });

    it('should check if user has specific permission', async () => {
      const { result } = renderHook(() => useEmbeddedAuth(), { wrapper });

      await waitFor(() => {
        expect(result.current.isAuthenticated).toBe(true);
      });

      expect(result.current.hasPermission('USER_READ')).toBe(true);
      expect(result.current.hasPermission('USER_DELETE')).toBe(false);
    });

    it('should grant all permissions if user has SYSTEM_ADMIN', async () => {
      const { result } = renderHook(() => useEmbeddedAuth(), { wrapper });

      await waitFor(() => {
        expect(result.current.isAuthenticated).toBe(true);
      });

      // User has SYSTEM_ADMIN permission via ROLE_ADMIN
      expect(result.current.hasPermission('ANY_PERMISSION')).toBe(true);
      expect(result.current.hasPermission('SUPER_SECRET_PERMISSION')).toBe(true);
    });

    it('should check if user has any of the specified permissions', async () => {
      const { result } = renderHook(() => useEmbeddedAuth(), { wrapper });

      await waitFor(() => {
        expect(result.current.isAuthenticated).toBe(true);
      });

      expect(result.current.hasAnyPermission(['USER_READ', 'USER_DELETE'])).toBe(true);
      expect(result.current.hasAnyPermission(['USER_DELETE', 'SYSTEM_DELETE'])).toBe(true); // SYSTEM_ADMIN grants all
    });

    it('should check if user has all specified permissions', async () => {
      const { result } = renderHook(() => useEmbeddedAuth(), { wrapper });

      await waitFor(() => {
        expect(result.current.isAuthenticated).toBe(true);
      });

      expect(result.current.hasAllPermissions(['USER_READ', 'USER_WRITE'])).toBe(true);
      expect(result.current.hasAllPermissions(['USER_READ', 'NON_EXISTENT'])).toBe(true); // SYSTEM_ADMIN grants all
    });

    it('should return false for permissions when user is null', async () => {
      mockedAuthService.getAccessToken.mockReturnValue(null);

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
      mockedAuthService.getAccessToken.mockReturnValue(mockTokens.accessToken);
      mockedAuthService.isTokenExpired.mockReturnValue(false);
      mockedAuthService.getProfile.mockResolvedValue(mockUserProfile);

      const { result } = renderHook(() => useEmbeddedAuth(), { wrapper });

      await waitFor(() => {
        expect(result.current.isAuthenticated).toBe(true);
      });

      expect(result.current.getToken()).toBe(mockTokens.accessToken);
    });

    it('should return null when no token exists', async () => {
      mockedAuthService.getAccessToken.mockReturnValue(null);

      const { result } = renderHook(() => useEmbeddedAuth(), { wrapper });

      await waitFor(() => {
        expect(result.current.isInitialized).toBe(true);
      });

      expect(result.current.getToken()).toBeNull();
    });
  });

  describe('Token Refresh Interval', () => {
    it('should set up token refresh interval when authenticated', async () => {
      mockedAuthService.getAccessToken.mockReturnValue(mockTokens.accessToken);
      mockedAuthService.isTokenExpired.mockReturnValue(false);
      mockedAuthService.getProfile.mockResolvedValue(mockUserProfile);

      renderHook(() => useEmbeddedAuth(), { wrapper });

      await waitFor(() => {
        expect(mockedAuthService.getProfile).toHaveBeenCalled();
      });

      // Fast-forward 1 minute
      await act(async () => {
        jest.advanceTimersByTime(60000);
      });

      // Should check token expiration
      expect(mockedAuthService.getAccessToken).toHaveBeenCalled();
    });

    it('should refresh expired token automatically', async () => {
      mockedAuthService.getAccessToken.mockReturnValue(mockTokens.accessToken);
      mockedAuthService.isTokenExpired.mockReturnValue(false);
      mockedAuthService.getProfile.mockResolvedValue(mockUserProfile);
      mockedAuthService.refreshToken.mockResolvedValue({
        accessToken: 'new.token',
      });

      renderHook(() => useEmbeddedAuth(), { wrapper });

      await waitFor(() => {
        expect(mockedAuthService.getProfile).toHaveBeenCalled();
      });

      // Simulate token expiration
      mockedAuthService.isTokenExpired.mockReturnValue(true);

      await act(async () => {
        jest.advanceTimersByTime(60000);
      });

      await waitFor(() => {
        expect(mockedAuthService.refreshToken).toHaveBeenCalled();
      });
    });

    it('should logout on refresh failure', async () => {
      mockedAuthService.getAccessToken.mockReturnValue(mockTokens.accessToken);
      mockedAuthService.isTokenExpired.mockReturnValue(false);
      mockedAuthService.getProfile.mockResolvedValue(mockUserProfile);
      mockedAuthService.refreshToken.mockRejectedValue(new Error('Refresh failed'));

      const { result } = renderHook(() => useEmbeddedAuth(), { wrapper });

      await waitFor(() => {
        expect(result.current.isAuthenticated).toBe(true);
      });

      // Simulate token expiration
      mockedAuthService.isTokenExpired.mockReturnValue(true);

      await act(async () => {
        jest.advanceTimersByTime(60000);
      });

      await waitFor(() => {
        expect(result.current.isAuthenticated).toBe(false);
      });
    });
  });

  describe('Context Error Handling', () => {
    it('should throw error when useEmbeddedAuth is used outside provider', () => {
      // Suppress console.error for this test
      const originalError = console.error;
      console.error = jest.fn();

      expect(() => {
        renderHook(() => useEmbeddedAuth());
      }).toThrow('useEmbeddedAuth must be used within an EmbeddedAuthProvider');

      console.error = originalError;
    });
  });
});
