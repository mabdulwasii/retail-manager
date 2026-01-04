/**
 * Component Tests: CloudTenantRegisterPage
 * Tests for cloud tenant registration wizard (Phase 3)
 */

import React from 'react';
import { render, screen } from '@testing-library/react';
import { MemoryRouter, Route, Routes } from 'react-router-dom';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { CloudTenantRegisterPage } from '../CloudTenantRegisterPage';
import { useRegisterTenant } from '@/hooks/useCloudTenants';

// Mock hooks
jest.mock('@/hooks/useCloudTenants');

const mockUseRegisterTenant = useRegisterTenant as jest.MockedFunction<typeof useRegisterTenant>;

const queryClient = new QueryClient({
  defaultOptions: {
    queries: { retry: false },
    mutations: { retry: false },
  },
});

const SuccessPage = () => <div>Success Page</div>;

const CloudTenantRegisterPageWrapper: React.FC = () => (
  <QueryClientProvider client={queryClient}>
    <MemoryRouter initialEntries={['/cloud/register']}>
      <Routes>
        <Route path="/cloud/register" element={<CloudTenantRegisterPage />} />
        <Route path="/cloud/register/success" element={<SuccessPage />} />
      </Routes>
    </MemoryRouter>
  </QueryClientProvider>
);

describe('CloudTenantRegisterPage', () => {
  beforeEach(() => {
    mockUseRegisterTenant.mockReturnValue({
      mutate: jest.fn(),
      isPending: false,
      isError: false,
      error: null,
    } as any);
  });

  afterEach(() => {
    jest.clearAllMocks();
  });

  it('should render the registration page', () => {
    render(<CloudTenantRegisterPageWrapper />);

    // Page should render with title
    expect(screen.getByText('RetailHQ Cloud')).toBeInTheDocument();
  });

  it('should render tier selection step initially', () => {
    render(<CloudTenantRegisterPageWrapper />);

    expect(screen.getByText('Choose Your Plan')).toBeInTheDocument();
    expect(screen.getByText(/Select the subscription tier/)).toBeInTheDocument();
  });

  it('should display all subscription tiers', () => {
    render(<CloudTenantRegisterPageWrapper />);

    expect(screen.getByText('Free')).toBeInTheDocument();
    expect(screen.getByText('Basic')).toBeInTheDocument();
    expect(screen.getByText('Premium')).toBeInTheDocument();
    expect(screen.getByText('Enterprise')).toBeInTheDocument();
  });

  it('should show continue button on first step', () => {
    render(<CloudTenantRegisterPageWrapper />);

    expect(screen.getByText('Continue')).toBeInTheDocument();
  });

  it('should show step progress indicator', () => {
    render(<CloudTenantRegisterPageWrapper />);

    // Check for step numbers
    const stepNumbers = ['1', '2', '3', '4'];
    stepNumbers.forEach((num) => {
      expect(screen.getByText(num)).toBeInTheDocument();
    });
  });

  it('should display registration message for existing users', () => {
    render(<CloudTenantRegisterPageWrapper />);

    expect(screen.getByText('Already registered?')).toBeInTheDocument();
    expect(screen.getByText('Sign in')).toBeInTheDocument();
  });

  it('should show help message', () => {
    render(<CloudTenantRegisterPageWrapper />);

    expect(screen.getByText(/Need help/)).toBeInTheDocument();
    expect(screen.getByText('Contact our support team')).toBeInTheDocument();
  });

  it('should display tier features', () => {
    render(<CloudTenantRegisterPageWrapper />);

    // Check for some key features
    expect(screen.getByText('Perfect for single store startups')).toBeInTheDocument();
    expect(screen.getByText('For growing businesses')).toBeInTheDocument();
  });

  it('should display tier pricing information', () => {
    render(<CloudTenantRegisterPageWrapper />);

    // Check for tier pricing
    expect(screen.getByText('$0')).toBeInTheDocument(); // Free tier
    expect(screen.getByText('$29')).toBeInTheDocument(); // Basic tier
    expect(screen.getByText('$99')).toBeInTheDocument(); // Premium tier
  });
});
