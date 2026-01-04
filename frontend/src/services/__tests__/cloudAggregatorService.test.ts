/**
 * Unit Tests: Cloud Aggregator Service
 * Tests for API client methods, error handling, and response transformation
 */

import { describe, it, expect, jest, beforeEach } from '@jest/globals';
import api from '@/lib/axios';
import {
  cloudAggregatorService,
  CloudTenantStatus,
  SubscriptionTier,
} from '../cloudAggregatorService';
import {
  getMockCloudTenant,
  getMockRegistrationResponse,
  getMockPagedTenants,
  getMockCloudShop,
} from '@/testData/cloudTenants';

// Mock axios instance
jest.mock('@/lib/axios');

const mockedApi = api as jest.Mocked<typeof api>;

describe('cloudAggregatorService', () => {
  beforeEach(() => {
    jest.clearAllMocks();
  });

  describe('registerTenant', () => {
    it('should successfully register a new tenant', async () => {
      // Arrange
      const request = {
        tenantName: 'Test Retail Co',
        tenantEmail: 'test@example.com',
        subscriptionTier: SubscriptionTier.FREE,
        shops: [
          {
            shopName: 'Test Shop 1',
            shopEmail: 'shop1@example.com',
          },
        ],
      };

      const mockResponse = getMockRegistrationResponse();
      mockedApi.post.mockResolvedValueOnce({ data: mockResponse });

      // Act
      const result = await cloudAggregatorService.registerTenant(request);

      // Assert
      expect(mockedApi.post).toHaveBeenCalledWith(
        '/registration/tenants',
        request
      );
      expect(result).toEqual(mockResponse);
      expect(result.apiKey).toMatch(/^rhq_/); // API key starts with rhq_
    });

    it('should handle registration errors gracefully', async () => {
      // Arrange
      const request = {
        tenantName: 'Test',
        tenantEmail: 'test@example.com',
        subscriptionTier: SubscriptionTier.FREE,
        shops: [],
      };

      mockedApi.post.mockRejectedValueOnce({
        response: {
          status: 409,
          data: { message: 'Tenant already exists' },
        },
      });

      // Act & Assert
      await expect(cloudAggregatorService.registerTenant(request)).rejects.toThrow(
        'Tenant already exists'
      );
    });
  });

  describe('listTenants', () => {
    it('should fetch paginated tenants successfully', async () => {
      // Arrange
      const mockPagedResponse = getMockPagedTenants();
      mockedApi.get.mockResolvedValueOnce({ data: mockPagedResponse });

      // Act
      const result = await cloudAggregatorService.listTenants({
        page: 0,
        size: 20,
      });

      // Assert
      expect(mockedApi.get).toHaveBeenCalledWith('/registration/tenants', {
        params: { page: 0, size: 20 },
      });
      expect(result).toEqual(mockPagedResponse);
      expect(result.content).toHaveLength(4);
    });

    it('should filter tenants by status', async () => {
      // Arrange
      const mockPagedResponse = getMockPagedTenants();
      mockedApi.get.mockResolvedValueOnce({ data: mockPagedResponse });

      // Act
      await cloudAggregatorService.listTenants({
        status: CloudTenantStatus.ACTIVE,
      });

      // Assert
      expect(mockedApi.get).toHaveBeenCalledWith('/registration/tenants', {
        params: { status: CloudTenantStatus.ACTIVE },
      });
    });
  });

  describe('getTenantById', () => {
    it('should fetch tenant by ID', async () => {
      // Arrange
      const mockTenant = getMockCloudTenant();
      mockedApi.get.mockResolvedValueOnce({ data: mockTenant });

      // Act
      const result = await cloudAggregatorService.getTenantById('tenant1');

      // Assert
      expect(mockedApi.get).toHaveBeenCalledWith('/registration/tenants/tenant1');
      expect(result).toEqual(mockTenant);
    });

    it('should handle 404 not found errors', async () => {
      // Arrange
      mockedApi.get.mockRejectedValueOnce({
        response: { status: 404 },
      });

      // Act & Assert
      await expect(cloudAggregatorService.getTenantById('invalid-id')).rejects.toThrow(
        'Resource not found'
      );
    });
  });

  describe('suspendTenant', () => {
    it('should suspend tenant successfully', async () => {
      // Arrange
      const mockTenant = getMockCloudTenant({
        status: CloudTenantStatus.SUSPENDED,
      });
      mockedApi.patch.mockResolvedValueOnce({ data: mockTenant });

      // Act
      const result = await cloudAggregatorService.suspendTenant('tenant1');

      // Assert
      expect(mockedApi.patch).toHaveBeenCalledWith(
        '/registration/tenants/tenant1/suspend'
      );
      expect(result.status).toBe(CloudTenantStatus.SUSPENDED);
    });
  });

  describe('activateTenant', () => {
    it('should activate tenant successfully', async () => {
      // Arrange
      const mockTenant = getMockCloudTenant({
        status: CloudTenantStatus.ACTIVE,
      });
      mockedApi.patch.mockResolvedValueOnce({ data: mockTenant });

      // Act
      const result = await cloudAggregatorService.activateTenant('tenant1');

      // Assert
      expect(mockedApi.patch).toHaveBeenCalledWith(
        '/registration/tenants/tenant1/activate'
      );
      expect(result.status).toBe(CloudTenantStatus.ACTIVE);
    });
  });

  describe('linkShop', () => {
    it('should link additional shop to tenant', async () => {
      // Arrange
      const shopRequest = {
        shopName: 'New Shop',
        shopEmail: 'newshop@example.com',
        address: '123 Main St',
      };

      const mockShop = getMockCloudShop();
      mockedApi.post.mockResolvedValueOnce({ data: mockShop });

      // Act
      const result = await cloudAggregatorService.linkShop('tenant1', shopRequest);

      // Assert
      expect(mockedApi.post).toHaveBeenCalledWith(
        '/registration/shops',
        shopRequest,
        {
          headers: {
            'X-Tenant-Id': 'tenant1',
          },
        }
      );
      expect(result).toEqual(mockShop);
    });
  });

  describe('regenerateApiKey', () => {
    it('should regenerate API key for tenant', async () => {
      // Arrange
      const mockResponse = {
        apiKey: 'rhq_new1234567890abcdefghijklmnopqrstuvwxyz',
        message: 'API key regenerated successfully',
      };
      mockedApi.post.mockResolvedValueOnce({ data: mockResponse });

      // Act
      const result = await cloudAggregatorService.regenerateApiKey('tenant1');

      // Assert
      expect(mockedApi.post).toHaveBeenCalledWith(
        '/registration/tenants/tenant1/regenerate-key'
      );
      expect(result.apiKey).toMatch(/^rhq_/);
      expect(result.message).toBeDefined();
    });
  });

  describe('healthCheck', () => {
    it('should return healthy status', async () => {
      // Arrange
      const mockHealth = {
        status: 'UP',
        message: 'Cloud Aggregator API is healthy',
      };
      mockedApi.get.mockResolvedValueOnce({ data: mockHealth });

      // Act
      const result = await cloudAggregatorService.healthCheck();

      // Assert
      expect(mockedApi.get).toHaveBeenCalledWith('/registration/health');
      expect(result.status).toBe('UP');
    });
  });

  describe('Error Handling', () => {
    it('should handle 401 unauthorized errors', async () => {
      // Arrange
      mockedApi.get.mockRejectedValueOnce({
        response: { status: 401 },
      });

      // Act & Assert
      await expect(cloudAggregatorService.getTenantById('test')).rejects.toThrow(
        'Unauthorized: Invalid or missing API key'
      );
    });

    it('should handle 403 forbidden errors', async () => {
      // Arrange
      mockedApi.patch.mockRejectedValueOnce({
        response: { status: 403 },
      });

      // Act & Assert
      await expect(cloudAggregatorService.suspendTenant('test')).rejects.toThrow(
        'Forbidden: Insufficient permissions'
      );
    });

    it('should handle 500 server errors', async () => {
      // Arrange
      mockedApi.get.mockRejectedValueOnce({
        response: { status: 500 },
      });

      // Act & Assert
      await expect(cloudAggregatorService.getTenantById('test')).rejects.toThrow(
        'Server error: Please try again later'
      );
    });

    it('should handle network errors', async () => {
      // Arrange
      mockedApi.get.mockRejectedValueOnce({
        request: {},
      });

      // Act & Assert
      await expect(cloudAggregatorService.getTenantById('test')).rejects.toThrow(
        'Network error: Unable to reach server'
      );
    });

    it('should handle unknown errors', async () => {
      // Arrange
      mockedApi.get.mockRejectedValueOnce(new Error('Unknown error'));

      // Act & Assert
      await expect(cloudAggregatorService.getTenantById('test')).rejects.toThrow();
    });
  });
});
