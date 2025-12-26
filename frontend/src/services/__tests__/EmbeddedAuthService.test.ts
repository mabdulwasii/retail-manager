/**
 * Unit tests for EmbeddedAuthService
 * Tests JWT token handling, authentication, and profile management
 */

import { describe, it, expect, beforeEach, jest } from '@jest/globals';

// Mock the API module before importing the service
const mockApi = {
  get: jest.fn(),
  post: jest.fn(),
};

jest.mock('@/lib/axios', () => ({
  __esModule: true,
  default: mockApi,
  setTokenProvider: jest.fn(),
}));

import embeddedAuthService from '../EmbeddedAuthService';

describe('EmbeddedAuthService', () => {
  const mockTokens = {
    accessToken: 'eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJ0ZXN0dXNlciIsImV4cCI6OTk5OTk5OTk5OX0.test',
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
        description: 'User role',
        isSystem: false,
        permissions: ['USER_READ'],
      },
    ],
  };

  beforeEach(() => {
    jest.clearAllMocks();
    localStorage.clear();
    sessionStorage.clear();
  });

  describe('Token Management', () => {
    it('should retrieve access token from localStorage', () => {
      (localStorage.getItem as jest.Mock).mockReturnValue(mockTokens.accessToken);

      const token = embeddedAuthService.getAccessToken();

      expect(localStorage.getItem).toHaveBeenCalledWith('embedded_access_token');
      expect(token).toBe(mockTokens.accessToken);
    });

    it('should return null when no access token exists', () => {
      (localStorage.getItem as jest.Mock).mockReturnValue(null);

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
      mockApi.post.mockResolvedValue({
        data: mockTokens,
      });

      const result = await embeddedAuthService.login({
        username: 'testuser',
        password: 'password123',
      });

      expect(mockApi.post).toHaveBeenCalledWith('/auth/login', {
        username: 'testuser',
        password: 'password123',
      });
      expect(result).toEqual(mockTokens);
      expect(localStorage.setItem).toHaveBeenCalledWith(
        'embedded_access_token',
        mockTokens.accessToken
      );
    });

    it('should throw error on login failure', async () => {
      mockApi.post.mockRejectedValue(new Error('Login failed'));

      await expect(
        embeddedAuthService.login({
          username: 'testuser',
          password: 'wrong',
        })
      ).rejects.toThrow();
    });
  });

  describe('Registration', () => {
    it('should register successfully and store tokens', async () => {
      mockApi.post.mockResolvedValue({
        data: mockTokens,
      });

      const result = await embeddedAuthService.register({
        username: 'newuser',
        password: 'password123',
        email: 'new@example.com',
      });

      expect(mockApi.post).toHaveBeenCalledWith('/auth/register', {
        username: 'newuser',
        password: 'password123',
        email: 'new@example.com',
      });
      expect(result).toEqual(mockTokens);
    });
  });

  describe('Logout', () => {
    it('should clear tokens from localStorage', () => {
      embeddedAuthService.logout();

      expect(localStorage.removeItem).toHaveBeenCalledWith('embedded_access_token');
      expect(localStorage.removeItem).toHaveBeenCalledWith('embedded_refresh_token');
    });
  });

  describe('Token Refresh', () => {
    it('should refresh access token using refresh token', async () => {
      (localStorage.getItem as jest.Mock).mockReturnValue(mockTokens.refreshToken);
      mockApi.post.mockResolvedValue({
        data: { accessToken: 'new.access.token' },
      });

      const result = await embeddedAuthService.refreshToken();

      expect(mockApi.post).toHaveBeenCalledWith('/auth/refresh', {
        refreshToken: mockTokens.refreshToken,
      });
      expect(result).toEqual({ accessToken: 'new.access.token' });
    });

    it('should throw error when no refresh token exists', async () => {
      (localStorage.getItem as jest.Mock).mockReturnValue(null);

      await expect(embeddedAuthService.refreshToken()).rejects.toThrow(
        'No refresh token available'
      );
    });
  });

  describe('Get Profile', () => {
    it('should fetch user profile successfully', async () => {
      (localStorage.getItem as jest.Mock).mockReturnValue(mockTokens.accessToken);
      mockApi.get.mockResolvedValue({
        data: mockUserProfile,
      });

      const profile = await embeddedAuthService.getProfile();

      expect(mockApi.get).toHaveBeenCalledWith('/users/profile', {
        headers: {
          Authorization: `Bearer ${mockTokens.accessToken}`,
        },
      });
      expect(profile).toEqual(mockUserProfile);
    });

    it('should throw error when no access token exists', async () => {
      (localStorage.getItem as jest.Mock).mockReturnValue(null);

      await expect(embeddedAuthService.getProfile()).rejects.toThrow(
        'No access token available'
      );
    });
  });
});
