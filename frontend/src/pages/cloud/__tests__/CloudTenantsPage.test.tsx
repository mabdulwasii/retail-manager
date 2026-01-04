/**
 * Component Tests: CloudTenantsPage
 * Tests for cloud tenants management page (Phase 2: Full data integration)
 */

import React from 'react';
import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { MemoryRouter } from 'react-router-dom';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { CloudTenantsPage } from '../CloudTenantsPage';
import { useCloudTenants, useSuspendTenant, useActivateTenant } from '@/hooks/useCloudTenants';
import { getMockPagedTenants } from '@/testData/cloudTenants';

// Mock hooks
jest.mock('@/hooks/useCloudTenants');

// Mock UI components
jest.mock('@/components/ui/card', () => ({
  Card: ({ children }: any) => <div data-testid="card">{children}</div>,
  CardHeader: ({ children }: any) => <div>{children}</div>,
  CardTitle: ({ children }: any) => <div>{children}</div>,
  CardDescription: ({ children }: any) => <div>{children}</div>,
  CardContent: ({ children }: any) => <div>{children}</div>,
}));

jest.mock('@/components/ui/button', () => ({
  Button: ({ children, ...props }: any) => <button {...props}>{children}</button>,
}));

const mockUseCloudTenants = useCloudTenants as jest.MockedFunction<typeof useCloudTenants>;
const mockUseSuspendTenant = useSuspendTenant as jest.MockedFunction<typeof useSuspendTenant>;
const mockUseActivateTenant = useActivateTenant as jest.MockedFunction<typeof useActivateTenant>;

const queryClient = new QueryClient({
  defaultOptions: {
    queries: { retry: false },
    mutations: { retry: false },
  },
});

const CloudTenantsPageWrapper: React.FC = () => (
  <QueryClientProvider client={queryClient}>
    <MemoryRouter>
      <CloudTenantsPage />
    </MemoryRouter>
  </QueryClientProvider>
);

describe('CloudTenantsPage', () => {
  beforeEach(() => {
    mockUseSuspendTenant.mockReturnValue({
      mutate: jest.fn(),
      isPending: false,
    } as any);

    mockUseActivateTenant.mockReturnValue({
      mutate: jest.fn(),
      isPending: false,
    } as any);
  });

  afterEach(() => {
    jest.clearAllMocks();
  });

  it('should render page title and description', () => {
    mockUseCloudTenants.mockReturnValue({
      data: getMockPagedTenants(),
      isLoading: false,
      isError: false,
      error: null,
      refetch: jest.fn(),
    } as any);

    render(<CloudTenantsPageWrapper />);

    const cloudTenantElements = screen.getAllByText('Cloud Tenants');
    expect(cloudTenantElements.length).toBeGreaterThan(0);

    expect(
      screen.getByText('Manage registered retail businesses and their subscriptions')
    ).toBeInTheDocument();
  });

  it('should render loading state', () => {
    mockUseCloudTenants.mockReturnValue({
      data: undefined,
      isLoading: true,
      isError: false,
      error: null,
      refetch: jest.fn(),
    } as any);

    render(<CloudTenantsPageWrapper />);

    expect(screen.getByText('Loading tenants...')).toBeInTheDocument();
  });

  it('should render error state', () => {
    mockUseCloudTenants.mockReturnValue({
      data: undefined,
      isLoading: false,
      isError: true,
      error: new Error('Failed to fetch'),
      refetch: jest.fn(),
    } as any);

    render(<CloudTenantsPageWrapper />);

    expect(screen.getByText('Failed to load tenants')).toBeInTheDocument();
    expect(screen.getByText('Try Again')).toBeInTheDocument();
  });

  it('should render empty state when no tenants', () => {
    mockUseCloudTenants.mockReturnValue({
      data: {
        content: [],
        totalElements: 0,
        totalPages: 0,
        number: 0,
        size: 20,
      },
      isLoading: false,
      isError: false,
      error: null,
      refetch: jest.fn(),
    } as any);

    render(<CloudTenantsPageWrapper />);

    expect(screen.getByText('No tenants found')).toBeInTheDocument();
    expect(
      screen.getByText('No retail businesses have registered yet')
    ).toBeInTheDocument();
  });

  it('should render tenants table with data', () => {
    mockUseCloudTenants.mockReturnValue({
      data: getMockPagedTenants(),
      isLoading: false,
      isError: false,
      error: null,
      refetch: jest.fn(),
    } as any);

    render(<CloudTenantsPageWrapper />);

    // Verify table structure is rendered
    expect(screen.getAllByText(/registered retail businesses/).length).toBeGreaterThan(0);

    // Check that we're not showing empty state
    expect(screen.queryByText('No tenants found')).not.toBeInTheDocument();
  });

  it('should render stats cards with real data', () => {
    mockUseCloudTenants.mockReturnValue({
      data: getMockPagedTenants(),
      isLoading: false,
      isError: false,
      error: null,
      refetch: jest.fn(),
    } as any);

    render(<CloudTenantsPageWrapper />);

    expect(screen.getByText('Total Tenants')).toBeInTheDocument();
    expect(screen.getByText('Active Subscriptions')).toBeInTheDocument();
    expect(screen.getByText('Total Shops')).toBeInTheDocument();
  });

  it('should render search input (enabled)', () => {
    mockUseCloudTenants.mockReturnValue({
      data: getMockPagedTenants(),
      isLoading: false,
      isError: false,
      error: null,
      refetch: jest.fn(),
    } as any);

    render(<CloudTenantsPageWrapper />);

    const searchInput = screen.getByPlaceholderText(
      /Search tenants by name or email/i
    );
    expect(searchInput).toBeInTheDocument();
    expect(searchInput).not.toBeDisabled();
  });

  it('should render filter button and toggle filters panel', async () => {
    mockUseCloudTenants.mockReturnValue({
      data: getMockPagedTenants(),
      isLoading: false,
      isError: false,
      error: null,
      refetch: jest.fn(),
    } as any);

    render(<CloudTenantsPageWrapper />);

    const filterButton = screen.getByText('Filters');
    expect(filterButton).toBeInTheDocument();

    await userEvent.click(filterButton);

    await waitFor(() => {
      expect(screen.getByText('All Statuses')).toBeInTheDocument();
      expect(screen.getByText('All Tiers')).toBeInTheDocument();
    });
  });

  it('should render pagination when multiple pages', () => {
    mockUseCloudTenants.mockReturnValue({
      data: {
        ...getMockPagedTenants(),
        totalPages: 3,
        number: 0,
      },
      isLoading: false,
      isError: false,
      error: null,
      refetch: jest.fn(),
    } as any);

    render(<CloudTenantsPageWrapper />);

    expect(screen.getByText(/Page 1 of 3/)).toBeInTheDocument();
    expect(screen.getByText('Previous')).toBeInTheDocument();
    expect(screen.getByText('Next')).toBeInTheDocument();
  });
});
