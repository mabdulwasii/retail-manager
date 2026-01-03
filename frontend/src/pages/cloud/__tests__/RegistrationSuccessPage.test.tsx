/**
 * Component Tests: RegistrationSuccessPage
 * Tests for registration success page with API key display (Phase 3)
 */

import React from 'react';
import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { MemoryRouter, Route, Routes } from 'react-router-dom';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { RegistrationSuccessPage } from '../RegistrationSuccessPage';
import { CloudTenantStatus, SubscriptionTier } from '@/services/cloudAggregatorService';

// Mock toast
jest.mock('sonner', () => ({
  toast: {
    success: jest.fn(),
  },
}));

// Mock UI components
jest.mock('@/components/ui/card', () => ({
  Card: ({ children, className }: any) => (
    <div data-testid="card" className={className}>
      {children}
    </div>
  ),
  CardHeader: ({ children }: any) => <div>{children}</div>,
  CardTitle: ({ children }: any) => <div>{children}</div>,
  CardDescription: ({ children }: any) => <div>{children}</div>,
  CardContent: ({ children }: any) => <div>{children}</div>,
}));

jest.mock('@/components/ui/button', () => ({
  Button: ({ children, asChild, ...props }: any) => {
    if (asChild) {
      return <div {...props}>{children}</div>;
    }
    return <button {...props}>{children}</button>;
  },
}));

const queryClient = new QueryClient({
  defaultOptions: {
    queries: { retry: false },
    mutations: { retry: false },
  },
});

const mockRegistrationData = {
  apiKey: 'test-api-key-1234567890abcdef1234567890abcdef',
  tenant: {
    id: 'tenant-123',
    tenantName: 'Test Business',
    tenantEmail: 'test@example.com',
    subscriptionTier: SubscriptionTier.FREE,
    status: CloudTenantStatus.ACTIVE,
    createdAt: '2024-01-01T00:00:00Z',
    shopCount: 1,
  },
  shops: [
    {
      id: 'shop-123',
      shopName: 'Main Store',
      shopEmail: 'shop@example.com',
      tenantId: 'tenant-123',
      address: '123 Main St',
      city: 'New York',
      country: 'USA',
      phoneNumber: '+1234567890',
      linkedAt: '2024-01-01T00:00:00Z',
    },
  ],
};

const RegisterPage = () => <div>Register Page</div>;

const RegistrationSuccessPageWrapper: React.FC<{ state?: any }> = ({ state }) => (
  <QueryClientProvider client={queryClient}>
    <MemoryRouter
      initialEntries={[
        {
          pathname: '/cloud/register/success',
          state: state || mockRegistrationData,
        },
      ]}
    >
      <Routes>
        <Route path="/cloud/register/success" element={<RegistrationSuccessPage />} />
        <Route path="/cloud/register" element={<RegisterPage />} />
      </Routes>
    </MemoryRouter>
  </QueryClientProvider>
);

describe('RegistrationSuccessPage', () => {
  beforeEach(() => {
    // Mock clipboard API
    Object.assign(navigator, {
      clipboard: {
        writeText: jest.fn().mockResolvedValue(undefined),
      },
    });
  });

  afterEach(() => {
    jest.clearAllMocks();
  });

  it('should render success message', () => {
    render(<RegistrationSuccessPageWrapper />);

    expect(screen.getByText('Registration Successful!')).toBeInTheDocument();
    expect(
      screen.getByText('Your RetailHQ Cloud account has been created successfully')
    ).toBeInTheDocument();
  });

  it('should display API key warning', () => {
    render(<RegistrationSuccessPageWrapper />);

    expect(screen.getByText('Your API Key - Save This Now!')).toBeInTheDocument();
    expect(
      screen.getByText(
        /This API key is shown only once. Save it securely - you'll need it to configure your local shops/
      )
    ).toBeInTheDocument();
  });

  it('should render masked API key by default', () => {
    render(<RegistrationSuccessPageWrapper />);

    const apiKeyElement = screen.getByText(/test-api/);
    expect(apiKeyElement.textContent).toContain('test-api');
    expect(apiKeyElement.textContent).toContain('•••');
  });

  it('should show/hide API key when toggle clicked', async () => {
    render(<RegistrationSuccessPageWrapper />);

    // Initially masked
    expect(screen.getByText('Show')).toBeInTheDocument();

    // Click show
    const showButton = screen.getByText('Show');
    await userEvent.click(showButton);

    await waitFor(() => {
      expect(screen.getByText('Hide')).toBeInTheDocument();
    });

    // Click hide
    const hideButton = screen.getByText('Hide');
    await userEvent.click(hideButton);

    await waitFor(() => {
      expect(screen.getByText('Show')).toBeInTheDocument();
    });
  });

  it('should copy API key to clipboard', async () => {
    const { toast } = require('sonner');

    render(<RegistrationSuccessPageWrapper />);

    const copyButton = screen.getByText('Copy Key');
    await userEvent.click(copyButton);

    await waitFor(() => {
      expect(navigator.clipboard.writeText).toHaveBeenCalledWith(mockRegistrationData.apiKey);
      expect(toast.success).toHaveBeenCalledWith('API key copied to clipboard');
    });

    // Should show "Copied!" temporarily
    await waitFor(() => {
      expect(screen.getByText('Copied!')).toBeInTheDocument();
    });
  });

  it('should download API key as text file', async () => {
    const createObjectURLMock = jest.fn(() => 'blob:test');
    const revokeObjectURLMock = jest.fn();
    global.URL.createObjectURL = createObjectURLMock;
    global.URL.revokeObjectURL = revokeObjectURLMock;

    const appendChildSpy = jest.spyOn(document.body, 'appendChild');
    const removeChildSpy = jest.spyOn(document.body, 'removeChild');

    render(<RegistrationSuccessPageWrapper />);

    const downloadButton = screen.getByText('Download');
    await userEvent.click(downloadButton);

    await waitFor(() => {
      expect(createObjectURLMock).toHaveBeenCalled();
      expect(appendChildSpy).toHaveBeenCalled();
      expect(removeChildSpy).toHaveBeenCalled();
      expect(revokeObjectURLMock).toHaveBeenCalled();
    });

    appendChildSpy.mockRestore();
    removeChildSpy.mockRestore();
  });

  it('should open email client with API key', async () => {
    delete (window as any).location;
    (window as any).location = { href: '' };

    render(<RegistrationSuccessPageWrapper />);

    const emailButton = screen.getByText('Email to Me');
    await userEvent.click(emailButton);

    await waitFor(() => {
      expect(window.location.href).toContain('mailto:');
      expect(window.location.href).toContain('test@example.com');
      expect(window.location.href).toContain('Your%20RetailHQ%20Cloud%20API%20Key');
    });
  });

  it('should display account details', () => {
    render(<RegistrationSuccessPageWrapper />);

    expect(screen.getByText('Account Details')).toBeInTheDocument();
    expect(screen.getByText('Test Business')).toBeInTheDocument();
    expect(screen.getByText('test@example.com')).toBeInTheDocument();
    expect(screen.getByText('FREE')).toBeInTheDocument();
    expect(screen.getByText('1 registered')).toBeInTheDocument();
  });

  it('should display registered shops', () => {
    render(<RegistrationSuccessPageWrapper />);

    expect(screen.getByText('Registered Shops')).toBeInTheDocument();
    expect(screen.getByText('Main Store')).toBeInTheDocument();
  });

  it('should display next steps guide', () => {
    render(<RegistrationSuccessPageWrapper />);

    expect(screen.getByText('Next Steps')).toBeInTheDocument();
    expect(screen.getByText('Download RetailHQ Installer')).toBeInTheDocument();
    expect(screen.getByText('Install on Shop Computers')).toBeInTheDocument();
    expect(screen.getByText('Configure with API Key')).toBeInTheDocument();
    expect(screen.getByText('Start Selling!')).toBeInTheDocument();
  });

  it('should display documentation links', () => {
    render(<RegistrationSuccessPageWrapper />);

    expect(screen.getByText('Need Help?')).toBeInTheDocument();
    expect(screen.getByText('Getting Started Guide')).toBeInTheDocument();
    expect(screen.getByText('Cloud Sync Setup')).toBeInTheDocument();
    expect(screen.getByText('Contact Support')).toBeInTheDocument();
    expect(screen.getByText('Full Documentation')).toBeInTheDocument();
  });

  it('should display "Go to Dashboard" button', () => {
    render(<RegistrationSuccessPageWrapper />);

    expect(screen.getByText('Go to Dashboard')).toBeInTheDocument();
    expect(
      screen.getByText('Access your cloud dashboard to manage shops and view analytics')
    ).toBeInTheDocument();
  });

  it('should display multiple shops when registered', () => {
    const multiShopData = {
      ...mockRegistrationData,
      shops: [
        ...mockRegistrationData.shops,
        {
          id: 'shop-456',
          shopName: 'Second Store',
          shopEmail: 'shop2@example.com',
          tenantId: 'tenant-123',
          address: '456 Oak Ave',
          city: 'Boston',
          country: 'USA',
          phoneNumber: '+1987654321',
          linkedAt: '2024-01-01T00:00:00Z',
        },
      ],
    };

    render(<RegistrationSuccessPageWrapper state={multiShopData} />);

    expect(screen.getByText('Main Store')).toBeInTheDocument();
    expect(screen.getByText('Second Store')).toBeInTheDocument();
    expect(screen.getByText('2 registered')).toBeInTheDocument();
  });

  it('should handle Premium tier display', () => {
    const premiumData = {
      ...mockRegistrationData,
      tenant: {
        ...mockRegistrationData.tenant,
        subscriptionTier: SubscriptionTier.PREMIUM,
      },
    };

    render(<RegistrationSuccessPageWrapper state={premiumData} />);

    expect(screen.getByText('PREMIUM')).toBeInTheDocument();
  });
});
