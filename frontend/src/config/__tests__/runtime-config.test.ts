/**
 * Unit tests for runtime-config service
 * Tests configuration loading and getter methods
 */

import { describe, it, expect } from '@jest/globals';
import configService from '../runtime-config';

describe('ConfigService', () => {
  describe('Configuration Access', () => {
    it('should have apiBaseUrl defined', () => {
      expect(configService.apiBaseUrl).toBeDefined();
      expect(typeof configService.apiBaseUrl).toBe('string');
    });

    it('should have keycloakUrl defined', () => {
      expect(configService.keycloakUrl).toBeDefined();
      expect(typeof configService.keycloakUrl).toBe('string');
    });

    it('should have keycloakRealm defined', () => {
      expect(configService.keycloakRealm).toBeDefined();
      expect(typeof configService.keycloakRealm).toBe('string');
    });

    it('should have keycloakClientId defined', () => {
      expect(configService.keycloakClientId).toBeDefined();
      expect(typeof configService.keycloakClientId).toBe('string');
    });

    it('should have appVersion defined', () => {
      expect(configService.appVersion).toBeDefined();
      expect(typeof configService.appVersion).toBe('string');
    });

    it('should have appEnv defined', () => {
      expect(configService.appEnv).toBeDefined();
      expect(typeof configService.appEnv).toBe('string');
    });

    it('should have authMode defined', () => {
      expect(configService.authMode).toBeDefined();
      expect(['keycloak', 'embedded'].includes(configService.authMode)).toBe(true);
    });
  });

  describe('Keycloak Config', () => {
    it('should return keycloakConfig object with correct properties', () => {
      const keycloakConfig = configService.keycloakConfig;

      expect(keycloakConfig).toBeDefined();
      expect(keycloakConfig).toHaveProperty('url');
      expect(keycloakConfig).toHaveProperty('realm');
      expect(keycloakConfig).toHaveProperty('clientId');

      expect(keycloakConfig.url).toBe(configService.keycloakUrl);
      expect(keycloakConfig.realm).toBe(configService.keycloakRealm);
      expect(keycloakConfig.clientId).toBe(configService.keycloakClientId);
    });
  });

  describe('Auth Mode Detection', () => {
    it('should have isEmbeddedMode as boolean', () => {
      expect(typeof configService.isEmbeddedMode).toBe('boolean');
    });

    it('should have isEmbeddedMode match authMode', () => {
      if (configService.authMode === 'embedded') {
        expect(configService.isEmbeddedMode).toBe(true);
      } else {
        expect(configService.isEmbeddedMode).toBe(false);
      }
    });
  });

  describe('Config Logging', () => {
    it('should have logConfig method', () => {
      expect(typeof configService.logConfig).toBe('function');
    });

    it('should not throw when calling logConfig', () => {
      expect(() => configService.logConfig()).not.toThrow();
    });
  });

  describe('Configuration Consistency', () => {
    it('should return same values on multiple accesses (caching)', () => {
      const firstUrl = configService.apiBaseUrl;
      const secondUrl = configService.apiBaseUrl;

      expect(firstUrl).toBe(secondUrl);
    });

    it('should have consistent keycloakConfig on multiple accesses', () => {
      const firstConfig = configService.keycloakConfig;
      const secondConfig = configService.keycloakConfig;

      expect(firstConfig).toEqual(secondConfig);
    });
  });
});
