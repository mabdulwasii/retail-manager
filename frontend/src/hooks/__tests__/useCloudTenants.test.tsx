/**
 * Unit Tests: useCloudTenants Hooks
 * Tests for TanStack Query hooks for cloud tenant management
 */

import { renderHook, waitFor } from '@testing-library/react';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import React from 'react';
import {
  useCloudTenants,
  useCloudTenant,
  useTenantShops,
  useRegisterTenant,
  useSuspendTenant,
  useActivateTenant,
  cloudTenantKeys,
} from '../useCloudTenants';
import { cloudAggregatorService } from '@/services/cloudAggregatorService';
import {
  getMockCloudTenant,
  getMockPagedTenants,
  getMockCloudShop,
  getMockRegistrationResponse,
} from '@/testData/cloudTenants';

// Mock the service
jest.mock('@/services/cloudAggregatorService');

// Mock toast
jest.mock('sonner', () => ({
  toast: {
    success: jest.fn(),
    error: jest.fn(),
  },
}));

const mockedService = cloudAggregatorService as jest.Mocked<typeof cloudAggregatorService>;

const queryClient = new QueryClient({
  defaultOptions: {
    queries: { retry: false },
    mutations: { retry: false },
  },
});

const createWrapper = () => {
  const Wrapper = ({ children }: { children: React.ReactNode }) => (
    <QueryClientProvider client={queryClient}>{children}</QueryClientProvider>
  );
  return Wrapper;
};

describe('useCloudTenants Hooks', () => {
  beforeEach(() => {
    jest.clearAllMocks();
    queryClient.clear();
  });

  describe('Query Key Factory', () => {
    it('should generate correct query keys', () => {
      expect(cloudTenantKeys.all).toEqual(['cloudTenants']);
      expect(cloudTenantKeys.lists()).toEqual(['cloudTenants', 'list']);
      expect(cloudTenantKeys.list({ page: 0 })).toEqual(['cloudTenants', 'list', { page: 0 }]);
      expect(cloudTenantKeys.details()).toEqual(['cloudTenants', 'detail']);
      expect(cloudTenantKeys.detail('tenant1')).toEqual(['cloudTenants', 'detail', 'tenant1']);
      expect(cloudTenantKeys.shops('tenant1')).toEqual([
        'cloudTenants',
        'detail',
        'tenant1',
        'shops',
      ]);
    });
  });

  describe('useCloudTenants', () => {
    it('should fetch tenants successfully', async () => {
      const mockData = getMockPagedTenants();
      mockedService.listTenants.mockResolvedValueOnce(mockData);

      const { result } = renderHook(() => useCloudTenants(), {
        wrapper: createWrapper(),
      });

      await waitFor(() => expect(result.current.isSuccess).toBe(true));

      expect(result.current.data).toEqual(mockData);
      expect(mockedService.listTenants).toHaveBeenCalledTimes(1);
    });

    it('should fetch tenants with filters', async () => {
      const mockData = getMockPagedTenants();
      const filters = { page: 1, size: 10, search: 'test' };
      mockedService.listTenants.mockResolvedValueOnce(mockData);

      const { result } = renderHook(() => useCloudTenants(filters), {
        wrapper: createWrapper(),
      });

      await waitFor(() => expect(result.current.isSuccess).toBe(true));

      expect(mockedService.listTenants).toHaveBeenCalledWith(filters);
    });

    it('should handle fetch error', async () => {
      mockedService.listTenants.mockRejectedValueOnce(new Error('Network error'));

      const { result } = renderHook(() => useCloudTenants(), {
        wrapper: createWrapper(),
      });

      await waitFor(() => expect(result.current.isError).toBe(true));

      expect(result.current.error).toBeTruthy();
    });
  });

  describe('useCloudTenant', () => {
    it('should fetch single tenant successfully', async () => {
      const mockTenant = getMockCloudTenant();
      mockedService.getTenantById.mockResolvedValueOnce(mockTenant);

      const { result } = renderHook(() => useCloudTenant('tenant1'), {
        wrapper: createWrapper(),
      });

      await waitFor(() => expect(result.current.isSuccess).toBe(true));

      expect(result.current.data).toEqual(mockTenant);
      expect(mockedService.getTenantById).toHaveBeenCalledWith('tenant1');
    });

    it('should not fetch when ID is empty', () => {
      const { result } = renderHook(() => useCloudTenant(''), {
        wrapper: createWrapper(),
      });

      expect(result.current.fetchStatus).toBe('idle');
      expect(mockedService.getTenantById).not.toHaveBeenCalled();
    });
  });

  describe('useTenantShops', () => {
    it('should fetch tenant shops successfully', async () => {
      const mockShops = [getMockCloudShop(), getMockCloudShop()];
      mockedService.getShopsByTenant.mockResolvedValueOnce(mockShops);

      const { result } = renderHook(() => useTenantShops('tenant1'), {
        wrapper: createWrapper(),
      });

      await waitFor(() => expect(result.current.isSuccess).toBe(true));

      expect(result.current.data).toEqual(mockShops);
      expect(mockedService.getShopsByTenant).toHaveBeenCalledWith('tenant1');
    });

    it('should not fetch when tenant ID is empty', () => {
      const { result } = renderHook(() => useTenantShops(''), {
        wrapper: createWrapper(),
      });

      expect(result.current.fetchStatus).toBe('idle');
      expect(mockedService.getShopsByTenant).not.toHaveBeenCalled();
    });
  });

  describe('useRegisterTenant', () => {
    it('should register tenant successfully', async () => {
      const mockResponse = getMockRegistrationResponse();
      mockedService.registerTenant.mockResolvedValueOnce(mockResponse);

      const { result } = renderHook(() => useRegisterTenant(), {
        wrapper: createWrapper(),
      });

      const request = {
        tenantName: 'Test Tenant',
        tenantEmail: 'test@example.com',
        subscriptionTier: 'FREE' as any,
        shops: [],
      };

      result.current.mutate(request);

      await waitFor(() => expect(result.current.isSuccess).toBe(true));

      expect(mockedService.registerTenant).toHaveBeenCalledWith(request);
    });

    it('should handle registration error', async () => {
      mockedService.registerTenant.mockRejectedValueOnce(new Error('Registration failed'));

      const { result } = renderHook(() => useRegisterTenant(), {
        wrapper: createWrapper(),
      });

      result.current.mutate({
        tenantName: 'Test',
        tenantEmail: 'test@example.com',
        subscriptionTier: 'FREE' as any,
        shops: [],
      });

      await waitFor(() => expect(result.current.isError).toBe(true));
    });
  });

  describe('useSuspendTenant', () => {
    it('should suspend tenant successfully', async () => {
      const mockTenant = getMockCloudTenant({ status: 'SUSPENDED' as any });
      mockedService.suspendTenant.mockResolvedValueOnce(mockTenant);

      const { result } = renderHook(() => useSuspendTenant(), {
        wrapper: createWrapper(),
      });

      result.current.mutate('tenant1');

      await waitFor(() => expect(result.current.isSuccess).toBe(true));

      expect(mockedService.suspendTenant).toHaveBeenCalledWith('tenant1');
    });

    it('should handle suspend error', async () => {
      mockedService.suspendTenant.mockRejectedValueOnce(new Error('Suspend failed'));

      const { result } = renderHook(() => useSuspendTenant(), {
        wrapper: createWrapper(),
      });

      result.current.mutate('tenant1');

      await waitFor(() => expect(result.current.isError).toBe(true));
    });
  });

  describe('useActivateTenant', () => {
    it('should activate tenant successfully', async () => {
      const mockTenant = getMockCloudTenant({ status: 'ACTIVE' as any });
      mockedService.activateTenant.mockResolvedValueOnce(mockTenant);

      const { result } = renderHook(() => useActivateTenant(), {
        wrapper: createWrapper(),
      });

      result.current.mutate('tenant1');

      await waitFor(() => expect(result.current.isSuccess).toBe(true));

      expect(mockedService.activateTenant).toHaveBeenCalledWith('tenant1');
    });

    it('should handle activate error', async () => {
      mockedService.activateTenant.mockRejectedValueOnce(new Error('Activate failed'));

      const { result } = renderHook(() => useActivateTenant(), {
        wrapper: createWrapper(),
      });

      result.current.mutate('tenant1');

      await waitFor(() => expect(result.current.isError).toBe(true));
    });
  });
});
