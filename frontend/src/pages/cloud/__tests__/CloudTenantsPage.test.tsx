/**
 * Component Tests: CloudTenantsPage
 * Tests for cloud tenants management page (Phase 1: Empty state)
 */

import React from 'react';
import { render, screen } from '@testing-library/react';
import { MemoryRouter } from 'react-router-dom';
import { CloudTenantsPage } from '../CloudTenantsPage';

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

const CloudTenantsPageWrapper: React.FC = () => (
  <MemoryRouter>
    <CloudTenantsPage />
  </MemoryRouter>
);

describe('CloudTenantsPage', () => {
  it('should render page title and description', () => {
    render(<CloudTenantsPageWrapper />);

    // Use getAllByText for duplicate text (h1 + CardTitle both have "Cloud Tenants")
    const cloudTenantElements = screen.getAllByText('Cloud Tenants');
    expect(cloudTenantElements.length).toBeGreaterThan(0);

    expect(
      screen.getByText('Manage registered retail businesses and their subscriptions')
    ).toBeInTheDocument();
  });

  it('should render empty state message (Phase 1)', () => {
    render(<CloudTenantsPageWrapper />);

    expect(screen.getByText('Tenant Management Coming Soon')).toBeInTheDocument();
    expect(
      screen.getByText(/This feature is currently under development/i)
    ).toBeInTheDocument();
  });

  it('should render search input (disabled in Phase 1)', () => {
    render(<CloudTenantsPageWrapper />);

    const searchInput = screen.getByPlaceholderText(
      /Search tenants by name or email/i
    );
    expect(searchInput).toBeInTheDocument();
    expect(searchInput).toBeDisabled();
  });

  it('should render stats cards with placeholder data', () => {
    render(<CloudTenantsPageWrapper />);

    expect(screen.getByText('Total Tenants')).toBeInTheDocument();
    expect(screen.getByText('Active Subscriptions')).toBeInTheDocument();
    expect(screen.getByText('Total Shops')).toBeInTheDocument();
  });

  it('should render disabled action buttons', () => {
    render(<CloudTenantsPageWrapper />);

    const viewAllButton = screen.getByText('View All Tenants');
    const registerButton = screen.getByText('Register New Tenant');

    expect(viewAllButton).toBeDisabled();
    expect(registerButton).toBeDisabled();
  });

  it('should render filter button', () => {
    render(<CloudTenantsPageWrapper />);

    expect(screen.getByText('Filters')).toBeInTheDocument();
  });
});
