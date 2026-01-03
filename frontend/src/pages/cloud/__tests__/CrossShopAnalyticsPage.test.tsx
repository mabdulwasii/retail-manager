/**
 * Component Tests: CrossShopAnalyticsPage
 * Tests for cloud analytics dashboard (Phase 5)
 */

import React from 'react';
import { render, screen } from '@testing-library/react';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { CrossShopAnalyticsPage } from '../CrossShopAnalyticsPage';

// Mock hooks
jest.mock('@/hooks/useCloudAnalytics', () => ({
  useRevenueAnalytics: jest.fn(() => ({
    data: undefined,
    isLoading: false,
    error: null,
  })),
  useSalesMetrics: jest.fn(() => ({
    data: undefined,
    isLoading: false,
    error: null,
  })),
  useTopProducts: jest.fn(() => ({
    data: undefined,
    isLoading: false,
    error: null,
  })),
  useShopPerformance: jest.fn(() => ({
    data: undefined,
    isLoading: false,
    error: null,
  })),
}));

const queryClient = new QueryClient({
  defaultOptions: {
    queries: { retry: false },
    mutations: { retry: false },
  },
});

const CrossShopAnalyticsPageWrapper: React.FC = () => (
  <QueryClientProvider client={queryClient}>
    <CrossShopAnalyticsPage />
  </QueryClientProvider>
);

describe('CrossShopAnalyticsPage', () => {
  afterEach(() => {
    jest.clearAllMocks();
    queryClient.clear();
  });

  it('should render page header', () => {
    render(<CrossShopAnalyticsPageWrapper />);

    expect(screen.getByText('Cross-Shop Analytics')).toBeInTheDocument();
    expect(screen.getByText(/Aggregated insights across all your retail locations/)).toBeInTheDocument();
  });

  it('should render date filter buttons', () => {
    render(<CrossShopAnalyticsPageWrapper />);

    expect(screen.getByText('Last 7 Days')).toBeInTheDocument();
    expect(screen.getByText('Last 30 Days')).toBeInTheDocument();
    expect(screen.getByText('Last 90 Days')).toBeInTheDocument();
  });

  it('should render export button', () => {
    render(<CrossShopAnalyticsPageWrapper />);

    expect(screen.getByText('Export CSV')).toBeInTheDocument();
  });

  it('should render KPI cards section', () => {
    render(<CrossShopAnalyticsPageWrapper />);

    expect(screen.getByText('Total Revenue')).toBeInTheDocument();
    expect(screen.getByText('Total Sales')).toBeInTheDocument();
    expect(screen.getByText('Avg. Order Value')).toBeInTheDocument();
    expect(screen.getByText('Active Shops')).toBeInTheDocument();
  });

  it('should render chart sections', () => {
    render(<CrossShopAnalyticsPageWrapper />);

    expect(screen.getByText('Revenue Trends')).toBeInTheDocument();
    expect(screen.getByText('Top Products')).toBeInTheDocument();
    expect(screen.getByText('Shop Performance')).toBeInTheDocument();
  });

  it('should show info message', () => {
    render(<CrossShopAnalyticsPageWrapper />);

    expect(screen.getByText(/Analytics data is updated in real-time/)).toBeInTheDocument();
  });
});
