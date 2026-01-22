/**
 * Component Tests: CloudTenantDetailPage
 * Tests for cloud tenant detail page (Phase 2: Full data integration)
 */

import React from 'react';
import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { MemoryRouter, Route, Routes } from 'react-router-dom';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { CloudTenantDetailPage } from '../CloudTenantDetailPage';
import {
import type {
  MockCardProps,
  MockButtonProps,
  MockSelectProps,
  MockSelectItemProps,
  MockShopSelectorProps
} from '@/test-utils/mock-types'

  useCloudTenant,
  useTenantShops,
  useSuspendTenant,
  useActivateTenant,
  useRegenerateApiKey,
} from '@/hooks/useCloudTenants';
import { getMockCloudTenant, getMockCloudShop } from '@/testData/cloudTenants';
import { CloudTenantStatus } from '@/services/cloudAggregatorService';

// Mock hooks
jest.mock('@/hooks/useCloudTenants');

// Mock UI components
jest.mock('@/components/ui/card', () => ({
  Card: ({ children, className }: MockCardProps) => <div className={className} data-testid="card">{children}</div>,
  CardHeader: ({ children }: MockCardProps) => <div>{children}</div>,
  CardTitle: ({ children }: MockCardProps) => <div>{children}</div>,
  CardDescription: ({ children }: MockCardProps) => <div>{children}</div>,
  CardContent: ({ children }: MockCardProps) => <div>{children}</div>,
}));

jest.mock('@/components/ui/button', () => ({
  Button: ({ children, asChild, ...props }: any) =>
    asChild ? <>{children}</> : <button {...props}>{children}</button>,
}));

// Mock toast
jest.mock('sonner', () => ({
  toast: {
    success: jest.fn(),
    error: jest.fn(),
  },
}));

const mockUseCloudTenant = useCloudTenant as jest.MockedFunction<typeof useCloudTenant>;
const mockUseTenantShops = useTenantShops as jest.MockedFunction<typeof useTenantShops>;
const mockUseSuspendTenant = useSuspendTenant as jest.MockedFunction<typeof useSuspendTenant>;
const mockUseActivateTenant = useActivateTenant as jest.MockedFunction<typeof useActivateTenant>;
const mockUseRegenerateApiKey = useRegenerateApiKey as jest.MockedFunction<typeof useRegenerateApiKey>;

const queryClient = new QueryClient({
  defaultOptions: {
    queries: { retry: false },
    mutations: { retry: false },
  },
});

const CloudTenantDetailPageWrapper: React.FC<{ tenantId?: string }> = ({ tenantId = 'tenant1' }) => (
  <QueryClientProvider client={queryClient}>
    <MemoryRouter initialEntries={[`/cloud/tenants/${tenantId}`]}>
      <Routes>
        <Route path="/cloud/tenants/:id" element={<CloudTenantDetailPage />} />
      </Routes>
    </MemoryRouter>
  </QueryClientProvider>
);

describe('CloudTenantDetailPage', () => {
  beforeEach(() => {
    mockUseSuspendTenant.mockReturnValue({
      mutate: jest.fn(),
      isPending: false,
    } as any);

    mockUseActivateTenant.mockReturnValue({
      mutate: jest.fn(),
      isPending: false,
    } as any);

    mockUseRegenerateApiKey.mockReturnValue({
      mutate: jest.fn(),
      isPending: false,
    } as any);
  });

  afterEach(() => {
    jest.clearAllMocks();
  });

  it('should render loading state', () => {
    mockUseCloudTenant.mockReturnValue({
      data: undefined,
      isLoading: true,
      isError: false,
      error: null,
    } as any);

    mockUseTenantShops.mockReturnValue({
      data: undefined,
      isLoading: true,
      isError: false,
    } as any);

    render(<CloudTenantDetailPageWrapper />);

    expect(screen.getByText('Loading tenant details...')).toBeInTheDocument();
  });

  it('should render error state', () => {
    mockUseCloudTenant.mockReturnValue({
      data: undefined,
      isLoading: false,
      isError: true,
      error: new Error('Failed to fetch tenant'),
    } as any);

    mockUseTenantShops.mockReturnValue({
      data: undefined,
      isLoading: false,
      isError: false,
    } as any);

    render(<CloudTenantDetailPageWrapper />);

    expect(screen.getByText('Failed to load tenant')).toBeInTheDocument();
    expect(screen.getByText('Failed to fetch tenant')).toBeInTheDocument();
  });

  it('should render tenant details with data', () => {
    const mockTenant = getMockCloudTenant();

    mockUseCloudTenant.mockReturnValue({
      data: mockTenant,
      isLoading: false,
      isError: false,
      error: null,
    } as any);

    mockUseTenantShops.mockReturnValue({
      data: [],
      isLoading: false,
      isError: false,
    } as any);

    render(<CloudTenantDetailPageWrapper />);

    expect(screen.getByText(mockTenant.tenantName)).toBeInTheDocument();
    expect(screen.getAllByText(mockTenant.tenantEmail).length).toBeGreaterThan(0);
  });

  it('should show suspend button for active tenant', () => {
    const mockTenant = getMockCloudTenant({ status: CloudTenantStatus.ACTIVE });

    mockUseCloudTenant.mockReturnValue({
      data: mockTenant,
      isLoading: false,
      isError: false,
      error: null,
    } as any);

    mockUseTenantShops.mockReturnValue({
      data: [],
      isLoading: false,
      isError: false,
    } as any);

    render(<CloudTenantDetailPageWrapper />);

    expect(screen.getByText('Suspend Tenant')).toBeInTheDocument();
  });

  it('should show activate button for suspended tenant', () => {
    const mockTenant = getMockCloudTenant({ status: CloudTenantStatus.SUSPENDED });

    mockUseCloudTenant.mockReturnValue({
      data: mockTenant,
      isLoading: false,
      isError: false,
      error: null,
    } as any);

    mockUseTenantShops.mockReturnValue({
      data: [],
      isLoading: false,
      isError: false,
    } as any);

    render(<CloudTenantDetailPageWrapper />);

    expect(screen.getByText('Activate Tenant')).toBeInTheDocument();
  });

  it('should render API key management section', () => {
    const mockTenant = getMockCloudTenant();

    mockUseCloudTenant.mockReturnValue({
      data: mockTenant,
      isLoading: false,
      isError: false,
      error: null,
    } as any);

    mockUseTenantShops.mockReturnValue({
      data: [],
      isLoading: false,
      isError: false,
    } as any);

    render(<CloudTenantDetailPageWrapper />);

    expect(screen.getByText('API Key Management')).toBeInTheDocument();
    expect(screen.getByText('Regenerate API Key')).toBeInTheDocument();
  });

  it('should render shops list with data', () => {
    const mockTenant = getMockCloudTenant();
    const mockShops = [
      getMockCloudShop({ shopName: 'Shop 1' }),
      getMockCloudShop({ shopName: 'Shop 2' }),
    ];

    mockUseCloudTenant.mockReturnValue({
      data: mockTenant,
      isLoading: false,
      isError: false,
      error: null,
    } as any);

    mockUseTenantShops.mockReturnValue({
      data: mockShops,
      isLoading: false,
      isError: false,
    } as any);

    render(<CloudTenantDetailPageWrapper />);

    expect(screen.getAllByText('Linked Shops').length).toBeGreaterThan(0);
    expect(screen.getByText('Shop 1')).toBeInTheDocument();
    expect(screen.getByText('Shop 2')).toBeInTheDocument();
  });

  it('should show empty state when no shops', () => {
    const mockTenant = getMockCloudTenant();

    mockUseCloudTenant.mockReturnValue({
      data: mockTenant,
      isLoading: false,
      isError: false,
      error: null,
    } as any);

    mockUseTenantShops.mockReturnValue({
      data: [],
      isLoading: false,
      isError: false,
    } as any);

    render(<CloudTenantDetailPageWrapper />);

    expect(screen.getByText('No shops linked')).toBeInTheDocument();
    expect(
      screen.getByText("This tenant hasn't linked any retail shops yet.")
    ).toBeInTheDocument();
  });

  it('should render tenant information fields', () => {
    const mockTenant = getMockCloudTenant();

    mockUseCloudTenant.mockReturnValue({
      data: mockTenant,
      isLoading: false,
      isError: false,
      error: null,
    } as any);

    mockUseTenantShops.mockReturnValue({
      data: [],
      isLoading: false,
      isError: false,
    } as any);

    render(<CloudTenantDetailPageWrapper />);

    expect(screen.getByText('Tenant Information')).toBeInTheDocument();
    expect(screen.getByText('Status')).toBeInTheDocument();
    expect(screen.getByText('Subscription Tier')).toBeInTheDocument();
  });

  it('should show back button', () => {
    const mockTenant = getMockCloudTenant();

    mockUseCloudTenant.mockReturnValue({
      data: mockTenant,
      isLoading: false,
      isError: false,
      error: null,
    } as any);

    mockUseTenantShops.mockReturnValue({
      data: [],
      isLoading: false,
      isError: false,
    } as any);

    render(<CloudTenantDetailPageWrapper />);

    expect(screen.getByText('Back to Tenants')).toBeInTheDocument();
  });
});
