/**
 * Unit tests for UnifiedAuthContext
 * Tests unified auth provider that switches between Keycloak and embedded mode
 */

import { describe, it, expect, beforeEach, jest } from '@jest/globals';
import { renderHook, render, waitFor } from '@testing-library/react';
import { UnifiedAuthProvider, useAuth } from '../UnifiedAuthContext';
import configService from '@/config/runtime-config';
import React from 'react';
import type {
  MockCardProps,
  MockButtonProps,
  MockSelectProps,
  MockSelectItemProps,
  MockShopSelectorProps
} from '@/test-utils/mock-types'


// Mock the config service
jest.mock('@/config/runtime-config', () => ({
  default: {
    isEmbeddedMode: false,
    authMode: 'keycloak',
  },
}));

// Mock both auth contexts
jest.mock('../EmbeddedAuthContext', () => ({
  EmbeddedAuthProvider: ({ children }: MockCardProps) => (
    <div data-testid="embedded-provider">{children}</div>
  ),
  useEmbeddedAuth: jest.fn(() => ({
    isAuthenticated: true,
    isInitialized: true,
    user: { username: 'embedded-user' },
    login: jest.fn(),
    register: jest.fn(),
    logout: jest.fn(),
    refreshUserProfile: jest.fn(),
    hasRole: jest.fn(),
    hasAnyRole: jest.fn(),
    hasAllRoles: jest.fn(),
    hasPermission: jest.fn(),
    hasAnyPermission: jest.fn(),
    hasAllPermissions: jest.fn(),
    getToken: jest.fn(() => 'embedded-token'),
  })),
}));

jest.mock('../ManualAuthContext', () => ({
  ManualAuthProvider: ({ children }: MockCardProps) => (
    <div data-testid="keycloak-provider">{children}</div>
  ),
  useAuth: jest.fn(() => ({
    isAuthenticated: true,
    isInitialized: true,
    user: { username: 'keycloak-user' },
    keycloak: {},
    initializeKeycloak: jest.fn(),
    login: jest.fn(),
    logout: jest.fn(),
    hasRole: jest.fn(),
    hasAnyRole: jest.fn(),
    hasAllRoles: jest.fn(),
    hasPermission: jest.fn(),
    hasAnyPermission: jest.fn(),
    hasAllPermissions: jest.fn(),
    getToken: jest.fn(() => 'keycloak-token'),
    refreshUserProfile: jest.fn(),
  })),
}));

const mockedConfigService = configService as jest.Mocked<typeof configService>;

describe('UnifiedAuthContext', () => {
  let consoleLogSpy: jest.SpyInstance;

  beforeEach(() => {
    jest.clearAllMocks();
    consoleLogSpy = jest.spyOn(console, 'log').mockImplementation();
  });

  afterEach(() => {
    consoleLogSpy.mockRestore();
  });

  describe('UnifiedAuthProvider', () => {
    it('should render both auth providers (nested)', () => {
      mockedConfigService.isEmbeddedMode = false;
      mockedConfigService.authMode = 'keycloak';

      const { getByTestId } = render(
        <UnifiedAuthProvider>
          <div>Test Child</div>
        </UnifiedAuthProvider>
      );

      // Both providers should be in the tree due to nesting
      expect(getByTestId('keycloak-provider')).toBeInTheDocument();
      expect(getByTestId('embedded-provider')).toBeInTheDocument();
    });

    it('should log Keycloak authentication mode', () => {
      mockedConfigService.isEmbeddedMode = false;
      mockedConfigService.authMode = 'keycloak';

      render(
        <UnifiedAuthProvider>
          <div>Test Child</div>
        </UnifiedAuthProvider>
      );

      expect(consoleLogSpy).toHaveBeenCalledWith(
        'Using Keycloak authentication (mode: keycloak)'
      );
    });

    it('should log Embedded JWT authentication mode', () => {
      mockedConfigService.isEmbeddedMode = true;
      mockedConfigService.authMode = 'embedded';

      render(
        <UnifiedAuthProvider>
          <div>Test Child</div>
        </UnifiedAuthProvider>
      );

      expect(consoleLogSpy).toHaveBeenCalledWith(
        'Using Embedded JWT authentication (mode: embedded)'
      );
    });

    it('should render children correctly', () => {
      const { getByText } = render(
        <UnifiedAuthProvider>
          <div>Test Content</div>
        </UnifiedAuthProvider>
      );

      expect(getByText('Test Content')).toBeInTheDocument();
    });
  });

  describe('useAuth hook', () => {
    it('should return embedded auth when in embedded mode', () => {
      mockedConfigService.isEmbeddedMode = true;

      const wrapper = ({ children }: { children: React.ReactNode }) => (
        <UnifiedAuthProvider>{children}</UnifiedAuthProvider>
      );

      const { result } = renderHook(() => useAuth(), { wrapper });

      expect(result.current.user).toEqual({ username: 'embedded-user' });
      expect(result.current.getToken()).toBe('embedded-token');
    });

    it('should return Keycloak auth when not in embedded mode', () => {
      mockedConfigService.isEmbeddedMode = false;

      const wrapper = ({ children }: { children: React.ReactNode }) => (
        <UnifiedAuthProvider>{children}</UnifiedAuthProvider>
      );

      const { result } = renderHook(() => useAuth(), { wrapper });

      expect(result.current.user).toEqual({ username: 'keycloak-user' });
      expect(result.current.getToken()).toBe('keycloak-token');
    });

    it('should call both hooks unconditionally (React Rules of Hooks)', () => {
      const { useEmbeddedAuth } = require('../EmbeddedAuthContext');
      const { useAuth: useKeycloakAuth } = require('../ManualAuthContext');

      mockedConfigService.isEmbeddedMode = false;

      const wrapper = ({ children }: { children: React.ReactNode }) => (
        <UnifiedAuthProvider>{children}</UnifiedAuthProvider>
      );

      renderHook(() => useAuth(), { wrapper });

      // Both hooks should be called regardless of mode
      expect(useEmbeddedAuth).toHaveBeenCalled();
      expect(useKeycloakAuth).toHaveBeenCalled();
    });

    it('should switch auth modes correctly when config changes', () => {
      // First render with Keycloak mode
      mockedConfigService.isEmbeddedMode = false;

      const wrapper = ({ children }: { children: React.ReactNode }) => (
        <UnifiedAuthProvider>{children}</UnifiedAuthProvider>
      );

      const { result, rerender } = renderHook(() => useAuth(), { wrapper });

      expect(result.current.user).toEqual({ username: 'keycloak-user' });

      // Simulate config change to embedded mode
      mockedConfigService.isEmbeddedMode = true;

      rerender();

      expect(result.current.user).toEqual({ username: 'embedded-user' });
    });
  });

  describe('Auth Methods Delegation', () => {
    it('should delegate login to embedded auth in embedded mode', () => {
      const { useEmbeddedAuth } = require('../EmbeddedAuthContext');
      const mockEmbeddedLogin = jest.fn();
      useEmbeddedAuth.mockReturnValue({
        login: mockEmbeddedLogin,
        isAuthenticated: false,
        isInitialized: true,
      });

      mockedConfigService.isEmbeddedMode = true;

      const wrapper = ({ children }: { children: React.ReactNode }) => (
        <UnifiedAuthProvider>{children}</UnifiedAuthProvider>
      );

      const { result } = renderHook(() => useAuth(), { wrapper });

      expect(result.current.login).toBe(mockEmbeddedLogin);
    });

    it('should delegate login to Keycloak auth in Keycloak mode', () => {
      const { useAuth: useKeycloakAuth } = require('../ManualAuthContext');
      const mockKeycloakLogin = jest.fn();
      useKeycloakAuth.mockReturnValue({
        login: mockKeycloakLogin,
        isAuthenticated: false,
        isInitialized: true,
      });

      mockedConfigService.isEmbeddedMode = false;

      const wrapper = ({ children }: { children: React.ReactNode }) => (
        <UnifiedAuthProvider>{children}</UnifiedAuthProvider>
      );

      const { result } = renderHook(() => useAuth(), { wrapper });

      expect(result.current.login).toBe(mockKeycloakLogin);
    });

    it('should delegate logout correctly based on mode', () => {
      const { useEmbeddedAuth } = require('../EmbeddedAuthContext');
      const { useAuth: useKeycloakAuth } = require('../ManualAuthContext');

      const mockEmbeddedLogout = jest.fn();
      const mockKeycloakLogout = jest.fn();

      useEmbeddedAuth.mockReturnValue({
        logout: mockEmbeddedLogout,
        isAuthenticated: true,
      });

      useKeycloakAuth.mockReturnValue({
        logout: mockKeycloakLogout,
        isAuthenticated: true,
      });

      // Test embedded mode
      mockedConfigService.isEmbeddedMode = true;

      const wrapper1 = ({ children }: { children: React.ReactNode }) => (
        <UnifiedAuthProvider>{children}</UnifiedAuthProvider>
      );

      const { result: result1 } = renderHook(() => useAuth(), { wrapper: wrapper1 });
      expect(result1.current.logout).toBe(mockEmbeddedLogout);

      // Test Keycloak mode
      mockedConfigService.isEmbeddedMode = false;

      const wrapper2 = ({ children }: { children: React.ReactNode }) => (
        <UnifiedAuthProvider>{children}</UnifiedAuthProvider>
      );

      const { result: result2 } = renderHook(() => useAuth(), { wrapper: wrapper2 });
      expect(result2.current.logout).toBe(mockKeycloakLogout);
    });
  });

  describe('Permission and Role Checks Delegation', () => {
    it('should delegate hasRole to correct auth implementation', () => {
      const { useEmbeddedAuth } = require('../EmbeddedAuthContext');
      const mockHasRole = jest.fn(() => true);
      useEmbeddedAuth.mockReturnValue({
        hasRole: mockHasRole,
        isAuthenticated: true,
      });

      mockedConfigService.isEmbeddedMode = true;

      const wrapper = ({ children }: { children: React.ReactNode }) => (
        <UnifiedAuthProvider>{children}</UnifiedAuthProvider>
      );

      const { result } = renderHook(() => useAuth(), { wrapper });

      expect(result.current.hasRole).toBe(mockHasRole);
    });

    it('should delegate hasPermission to correct auth implementation', () => {
      const { useAuth: useKeycloakAuth } = require('../ManualAuthContext');
      const mockHasPermission = jest.fn(() => true);
      useKeycloakAuth.mockReturnValue({
        hasPermission: mockHasPermission,
        isAuthenticated: true,
      });

      mockedConfigService.isEmbeddedMode = false;

      const wrapper = ({ children }: { children: React.ReactNode }) => (
        <UnifiedAuthProvider>{children}</UnifiedAuthProvider>
      );

      const { result } = renderHook(() => useAuth(), { wrapper });

      expect(result.current.hasPermission).toBe(mockHasPermission);
    });

    it('should delegate hasAnyRole to correct auth implementation', () => {
      const { useEmbeddedAuth } = require('../EmbeddedAuthContext');
      const mockHasAnyRole = jest.fn(() => false);
      useEmbeddedAuth.mockReturnValue({
        hasAnyRole: mockHasAnyRole,
        isAuthenticated: true,
      });

      mockedConfigService.isEmbeddedMode = true;

      const wrapper = ({ children }: { children: React.ReactNode }) => (
        <UnifiedAuthProvider>{children}</UnifiedAuthProvider>
      );

      const { result } = renderHook(() => useAuth(), { wrapper });

      expect(result.current.hasAnyRole).toBe(mockHasAnyRole);
    });

    it('should delegate hasAllRoles to correct auth implementation', () => {
      const { useEmbeddedAuth } = require('../EmbeddedAuthContext');
      const mockHasAllRoles = jest.fn(() => true);
      useEmbeddedAuth.mockReturnValue({
        hasAllRoles: mockHasAllRoles,
        isAuthenticated: true,
      });

      mockedConfigService.isEmbeddedMode = true;

      const wrapper = ({ children }: { children: React.ReactNode }) => (
        <UnifiedAuthProvider>{children}</UnifiedAuthProvider>
      );

      const { result } = renderHook(() => useAuth(), { wrapper });

      expect(result.current.hasAllRoles).toBe(mockHasAllRoles);
    });
  });

  describe('State Delegation', () => {
    it('should delegate isAuthenticated state correctly', () => {
      const { useEmbeddedAuth } = require('../EmbeddedAuthContext');
      useEmbeddedAuth.mockReturnValue({
        isAuthenticated: true,
        isInitialized: true,
      });

      mockedConfigService.isEmbeddedMode = true;

      const wrapper = ({ children }: { children: React.ReactNode }) => (
        <UnifiedAuthProvider>{children}</UnifiedAuthProvider>
      );

      const { result } = renderHook(() => useAuth(), { wrapper });

      expect(result.current.isAuthenticated).toBe(true);
    });

    it('should delegate isInitialized state correctly', () => {
      const { useAuth: useKeycloakAuth } = require('../ManualAuthContext');
      useKeycloakAuth.mockReturnValue({
        isAuthenticated: false,
        isInitialized: false,
      });

      mockedConfigService.isEmbeddedMode = false;

      const wrapper = ({ children }: { children: React.ReactNode }) => (
        <UnifiedAuthProvider>{children}</UnifiedAuthProvider>
      );

      const { result } = renderHook(() => useAuth(), { wrapper });

      expect(result.current.isInitialized).toBe(false);
    });

    it('should delegate user state correctly', () => {
      const mockUser = { id: '123', username: 'testuser' };
      const { useEmbeddedAuth } = require('../EmbeddedAuthContext');
      useEmbeddedAuth.mockReturnValue({
        user: mockUser,
        isAuthenticated: true,
      });

      mockedConfigService.isEmbeddedMode = true;

      const wrapper = ({ children }: { children: React.ReactNode }) => (
        <UnifiedAuthProvider>{children}</UnifiedAuthProvider>
      );

      const { result } = renderHook(() => useAuth(), { wrapper });

      expect(result.current.user).toEqual(mockUser);
    });
  });

  describe('Edge Cases', () => {
    it('should handle undefined config values gracefully', () => {
      const { useEmbeddedAuth } = require('../EmbeddedAuthContext');
      const { useAuth: useKeycloakAuth } = require('../ManualAuthContext');

      // Reset mock return values
      useEmbeddedAuth.mockReturnValue({
        user: { username: 'embedded-user' },
        isAuthenticated: true,
        isInitialized: true,
      });

      useKeycloakAuth.mockReturnValue({
        user: { username: 'keycloak-user' },
        isAuthenticated: true,
        isInitialized: true,
      });

      (mockedConfigService as any).isEmbeddedMode = undefined;
      (mockedConfigService as any).authMode = undefined;

      const wrapper = ({ children }: { children: React.ReactNode }) => (
        <UnifiedAuthProvider>{children}</UnifiedAuthProvider>
      );

      // Should default to Keycloak (falsy value)
      const { result } = renderHook(() => useAuth(), { wrapper });

      expect(result.current.user).toEqual({ username: 'keycloak-user' });
    });

    it('should handle rapid mode switches', () => {
      const { useEmbeddedAuth } = require('../EmbeddedAuthContext');
      const { useAuth: useKeycloakAuth } = require('../ManualAuthContext');

      // Reset mock return values
      useEmbeddedAuth.mockReturnValue({
        user: { username: 'embedded-user' },
        isAuthenticated: true,
        isInitialized: true,
      });

      useKeycloakAuth.mockReturnValue({
        user: { username: 'keycloak-user' },
        isAuthenticated: true,
        isInitialized: true,
      });

      mockedConfigService.isEmbeddedMode = false;

      const wrapper = ({ children }: { children: React.ReactNode }) => (
        <UnifiedAuthProvider>{children}</UnifiedAuthProvider>
      );

      const { result, rerender } = renderHook(() => useAuth(), { wrapper });

      // Switch modes rapidly
      mockedConfigService.isEmbeddedMode = true;
      rerender();
      expect(result.current.user).toEqual({ username: 'embedded-user' });

      mockedConfigService.isEmbeddedMode = false;
      rerender();
      expect(result.current.user).toEqual({ username: 'keycloak-user' });

      mockedConfigService.isEmbeddedMode = true;
      rerender();
      expect(result.current.user).toEqual({ username: 'embedded-user' });
    });
  });
});
