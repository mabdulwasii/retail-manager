/**
 * Unit tests for EmbeddedAuthService
 * Tests JWT token handling, authentication, and profile management
 * Uses MSW for API mocking to test actual service code
 */

import { describe, it, expect, beforeEach } from '@jest/globals';
import { server } from '@/test/mocks/server';
import { http, HttpResponse } from 'msw';
import embeddedAuthService from '../EmbeddedAuthService';

const API_BASE_URL = 'http://localhost:8081/api';

describe('EmbeddedAuthService', () => {
  const mockTokens = {
    accessToken: 'eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJ0ZXN0dXNlciIsImV4cCI6OTk5OTk5OTk5OX0.test',
    refreshToken: 'refresh.token.here',
  };

  const mockUserProfile = {
    id: '123',
    username: 'testuser',
    email: 'test@example.com',
    roles: ['USER'],
    permissions: ['PRODUCT_READ', 'PRODUCT_WRITE'],
  };

  beforeEach(() => {
    localStorage.clear();
    sessionStorage.clear();
  });

  describe('Token Management', () => {
    it('should retrieve access token from localStorage', () => {
      localStorage.setItem('embedded_access_token', mockTokens.accessToken);

      const token = embeddedAuthService.getAccessToken();

      expect(token).toBe(mockTokens.accessToken);
    });

    it('should return null when no access token exists', () => {
      const token = embeddedAuthService.getAccessToken();

      expect(token).toBeNull();
    });
  });

  describe('JWT Token Parsing', () => {
    it('should parse valid JWT token', () => {
      const token = 'eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJ0ZXN0dXNlciIsImV4cCI6OTk5OTk5OTk5OX0.test';

      const parsed = embeddedAuthService.parseToken(token);

      expect(parsed).toEqual({
        sub: 'testuser',
        exp: 9999999999,
      });
    });

    it('should handle JWT with URL-safe base64 characters', () => {
      const token = 'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ0ZXN0In0.test';

      const parsed = embeddedAuthService.parseToken(token);

      expect(parsed).toBeDefined();
      expect(parsed.sub).toBe('test');
    });

    it('should return null for invalid JWT format', () => {
      const invalidToken = 'invalid.token';

      const parsed = embeddedAuthService.parseToken(invalidToken);

      expect(parsed).toBeNull();
    });
  });

  describe('Token Expiration', () => {
    it('should detect expired token', () => {
      const expiredToken = 'eyJhbGciOiJIUzI1NiJ9.eyJleHAiOjF9.test';

      const isExpired = embeddedAuthService.isTokenExpired(expiredToken);

      expect(isExpired).toBe(true);
    });

    it('should detect valid non-expired token', () => {
      const validToken = 'eyJhbGciOiJIUzI1NiJ9.eyJleHAiOjk5OTk5OTk5OTl9.test';

      const isExpired = embeddedAuthService.isTokenExpired(validToken);

      expect(isExpired).toBe(false);
    });

    it('should treat token without exp claim as expired', () => {
      const noExpToken = 'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ0ZXN0In0.test';

      const isExpired = embeddedAuthService.isTokenExpired(noExpToken);

      expect(isExpired).toBe(true);
    });
  });

  describe('Login', () => {
    it('should login successfully and store tokens', async () => {
      const result = await embeddedAuthService.login({
        username: 'testuser',
        password: 'password123',
      });

      expect(result.accessToken).toBeDefined();
      expect(result.refreshToken).toBeDefined();

      // Verify tokens are stored in localStorage
      const storedAccessToken = localStorage.getItem('embedded_access_token');
      const storedRefreshToken = localStorage.getItem('embedded_refresh_token');

      expect(storedAccessToken).toBe(result.accessToken);
      expect(storedRefreshToken).toBe(result.refreshToken);
    });

    it('should throw error on login failure with invalid credentials', async () => {
      await expect(
        embeddedAuthService.login({
          username: 'wronguser',
          password: 'wrongpass',
        })
      ).rejects.toThrow();
    });

    it('should throw error on login with empty credentials', async () => {
      await expect(
        embeddedAuthService.login({
          username: '',
          password: '',
        })
      ).rejects.toThrow();
    });
  });

  describe('Registration', () => {
    it('should register successfully and return user data', async () => {
      server.use(
        http.post(`${API_BASE_URL}/auth/register`, async ({ request }) => {
          const body = await request.json() as { username: string; email: string };
          return HttpResponse.json({
            id: '456',
            username: body.username,
            email: body.email,
          }, { status: 201 });
        })
      );

      const result = await embeddedAuthService.register({
        username: 'newuser',
        password: 'password123',
        email: 'new@example.com',
      });

      expect(result.username).toBe('newuser');
      expect(result.email).toBe('new@example.com');
    });

    it('should throw error when username already exists', async () => {
      await expect(
        embeddedAuthService.register({
          username: 'existinguser',
          password: 'password123',
          email: 'test@example.com',
        })
      ).rejects.toThrow();
    });
  });

  describe('Logout', () => {
    it('should clear tokens from localStorage', () => {
      localStorage.setItem('embedded_access_token', mockTokens.accessToken);
      localStorage.setItem('embedded_refresh_token', mockTokens.refreshToken);

      embeddedAuthService.logout();

      expect(localStorage.getItem('embedded_access_token')).toBeNull();
      expect(localStorage.getItem('embedded_refresh_token')).toBeNull();
    });
  });

  describe('Token Refresh', () => {
    it('should refresh access token using refresh token', async () => {
      localStorage.setItem('embedded_refresh_token', 'valid-refresh-token');

      const result = await embeddedAuthService.refreshToken();

      expect(result.accessToken).toBe('new-access-token');
      expect(result.refreshToken).toBe('new-refresh-token');
    });

    it('should throw error when no refresh token exists', async () => {
      await expect(embeddedAuthService.refreshToken()).rejects.toThrow(
        'No refresh token available'
      );
    });

    it('should throw error with invalid refresh token', async () => {
      localStorage.setItem('embedded_refresh_token', 'invalid-refresh-token');

      await expect(embeddedAuthService.refreshToken()).rejects.toThrow();
    });
  });

  describe('Get Profile', () => {
    it('should fetch user profile successfully', async () => {
      localStorage.setItem('embedded_access_token', mockTokens.accessToken);

      const profile = await embeddedAuthService.getProfile();

      expect(profile).toEqual(mockUserProfile);
    });

    it('should throw error when unauthorized', async () => {
      // Don't set token, axios interceptor should add it but will be empty
      server.use(
        http.get(`${API_BASE_URL}/users/profile`, () => {
          return HttpResponse.json(
            { message: 'Unauthorized' },
            { status: 401 }
          );
        })
      );

      await expect(embeddedAuthService.getProfile()).rejects.toThrow();
    });
  });

  describe('Check Permissions', () => {
    it('should check if user has required permissions', async () => {
      localStorage.setItem('embedded_access_token', mockTokens.accessToken);

      const hasPermission = await embeddedAuthService.checkPermissions(['PRODUCT_READ']);

      expect(hasPermission).toBe(true);
    });

    it('should return false when user lacks permissions', async () => {
      localStorage.setItem('embedded_access_token', mockTokens.accessToken);

      const hasPermission = await embeddedAuthService.checkPermissions(['ADMIN_ACCESS']);

      expect(hasPermission).toBe(false);
    });
  });
});
