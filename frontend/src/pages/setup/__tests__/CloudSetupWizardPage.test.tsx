/**
 * Component Tests: CloudSetupWizardPage
 * Tests for the first-run setup wizard (Phase 4)
 */

import React from 'react';
import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { MemoryRouter, Route, Routes } from 'react-router-dom';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { CloudSetupWizardPage } from '../CloudSetupWizardPage';
import { useFirstRunDetection } from '@/hooks/useFirstRunDetection';

// Mock hooks
jest.mock('@/hooks/useFirstRunDetection');
jest.mock('@/services/shopService');

const mockUseFirstRunDetection = useFirstRunDetection as jest.MockedFunction<typeof useFirstRunDetection>;

const queryClient = new QueryClient({
  defaultOptions: {
    queries: { retry: false },
    mutations: { retry: false },
  },
});

const HomePage = () => <div>Home Page</div>;

const CloudSetupWizardPageWrapper: React.FC = () => (
  <QueryClientProvider client={queryClient}>
    <MemoryRouter initialEntries={['/setup']}>
      <Routes>
        <Route path="/setup" element={<CloudSetupWizardPage />} />
        <Route path="/" element={<HomePage />} />
      </Routes>
    </MemoryRouter>
  </QueryClientProvider>
);

const mockMarkSetupComplete = jest.fn();

describe('CloudSetupWizardPage', () => {
  beforeEach(() => {
    mockUseFirstRunDetection.mockReturnValue({
      isFirstRun: true,
      isCheckingSetup: false,
      cloudConfig: null,
      isCloudSyncEnabled: false,
      triggerSetupWizard: jest.fn(),
      markSetupComplete: mockMarkSetupComplete,
      resetSetup: jest.fn(),
      updateCloudConfig: jest.fn(),
      checkSetupStatus: jest.fn(),
    });
  });

  afterEach(() => {
    jest.clearAllMocks();
    queryClient.clear();
  });

  describe('Welcome Step', () => {
    it('should render welcome screen initially', () => {
      render(<CloudSetupWizardPageWrapper />);

      expect(screen.getByText('Welcome to RetailHQ')).toBeInTheDocument();
      expect(screen.getByText(/Let's get your shop management system set up/)).toBeInTheDocument();
    });

    it('should display feature highlights', () => {
      render(<CloudSetupWizardPageWrapper />);

      expect(screen.getByText('Manage Sales & Inventory')).toBeInTheDocument();
      expect(screen.getByText('Optional Cloud Sync')).toBeInTheDocument();
      expect(screen.getByText('Advanced Analytics')).toBeInTheDocument();
    });

    it('should have Get Started button', () => {
      render(<CloudSetupWizardPageWrapper />);

      expect(screen.getByText('Get Started')).toBeInTheDocument();
    });

    it('should advance to mode selection on Get Started click', async () => {
      const user = userEvent.setup();
      render(<CloudSetupWizardPageWrapper />);

      const getStartedButton = screen.getByText('Get Started');
      await user.click(getStartedButton);

      expect(screen.getByText('Choose Your Setup Mode')).toBeInTheDocument();
    });
  });

  describe('Mode Selection Step', () => {
    beforeEach(async () => {
      const user = userEvent.setup();
      render(<CloudSetupWizardPageWrapper />);
      await user.click(screen.getByText('Get Started'));
    });

    it('should display mode selection options', () => {
      expect(screen.getByText('Cloud-Enabled Mode')).toBeInTheDocument();
      expect(screen.getByText('Standalone Mode')).toBeInTheDocument();
    });

    it('should show cloud mode features', () => {
      expect(screen.getByText(/Centralized dashboard across all shops/)).toBeInTheDocument();
      expect(screen.getByText(/Real-time sync and backup/)).toBeInTheDocument();
      expect(screen.getByText(/Advanced analytics and reporting/)).toBeInTheDocument();
    });

    it('should show standalone mode features', () => {
      expect(screen.getByText(/Works offline/)).toBeInTheDocument();
      expect(screen.getByText(/Complete data privacy/)).toBeInTheDocument();
      expect(screen.getByText(/No subscription required/)).toBeInTheDocument();
    });

    it('should disable continue button when no mode selected', () => {
      const continueButton = screen.getByRole('button', { name: /Continue/i });
      expect(continueButton).toBeDisabled();
    });

    it('should enable continue button when mode selected', async () => {
      const user = userEvent.setup();
      const cloudModeOption = screen.getByText('Cloud-Enabled Mode');
      await user.click(cloudModeOption);

      const continueButton = screen.getByRole('button', { name: /Continue/i });
      expect(continueButton).toBeEnabled();
    });

    it('should navigate back to welcome screen', async () => {
      const user = userEvent.setup();
      const backButton = screen.getByRole('button', { name: /Back/i });
      await user.click(backButton);

      expect(screen.getByText('Welcome to RetailHQ')).toBeInTheDocument();
    });

    it('should complete setup when standalone mode selected', async () => {
      const user = userEvent.setup();
      await user.click(screen.getByText('Standalone Mode'));
      await user.click(screen.getByRole('button', { name: /Continue/i }));

      await waitFor(() => {
        expect(mockMarkSetupComplete).toHaveBeenCalledWith();
        expect(screen.getByText('Setup Complete!')).toBeInTheDocument();
      });
    });
  });

  describe('Cloud Config Step', () => {
    beforeEach(async () => {
      const user = userEvent.setup();
      render(<CloudSetupWizardPageWrapper />);
      await user.click(screen.getByText('Get Started'));
      await user.click(screen.getByText('Cloud-Enabled Mode'));
      await user.click(screen.getByRole('button', { name: /Continue/i }));
    });

    it('should render cloud config form', () => {
      expect(screen.getByText('Connect to RetailHQ Cloud')).toBeInTheDocument();
      expect(screen.getByPlaceholderText(/Enter your 64-character API key/)).toBeInTheDocument();
    });

    it('should show skip option', () => {
      expect(screen.getByText('Skip for Now')).toBeInTheDocument();
    });

    it('should show register link', () => {
      expect(screen.getByText('Register New Tenant')).toBeInTheDocument();
    });

    it('should validate API key format', async () => {
      const user = userEvent.setup();
      const apiKeyInput = screen.getByPlaceholderText(/Enter your 64-character API key/);

      // Enter valid API key (64 hex characters)
      await user.type(apiKeyInput, 'a'.repeat(64));

      await waitFor(() => {
        expect(screen.getByText('Valid format')).toBeInTheDocument();
      });
    });
  });

  describe('Complete Step - Standalone', () => {
    it('should show completion screen for standalone mode', async () => {
      const user = userEvent.setup();
      render(<CloudSetupWizardPageWrapper />);

      await user.click(screen.getByText('Get Started'));
      await user.click(screen.getByText('Standalone Mode'));
      await user.click(screen.getByRole('button', { name: /Continue/i }));

      await waitFor(() => {
        expect(screen.getByText('Setup Complete!')).toBeInTheDocument();
        expect(screen.getByText(/Your shop is ready to use in standalone mode/)).toBeInTheDocument();
      });
    });

    it('should show what-next section', async () => {
      const user = userEvent.setup();
      render(<CloudSetupWizardPageWrapper />);

      await user.click(screen.getByText('Get Started'));
      await user.click(screen.getByText('Standalone Mode'));
      await user.click(screen.getByRole('button', { name: /Continue/i }));

      await waitFor(() => {
        expect(screen.getByText("What's Next?")).toBeInTheDocument();
        expect(screen.getByText(/Add products to your inventory/)).toBeInTheDocument();
      });
    });

    it('should navigate to home on Launch button click', async () => {
      const user = userEvent.setup();
      render(<CloudSetupWizardPageWrapper />);

      await user.click(screen.getByText('Get Started'));
      await user.click(screen.getByText('Standalone Mode'));
      await user.click(screen.getByRole('button', { name: /Continue/i }));

      await waitFor(() => {
        expect(screen.getByText('Setup Complete!')).toBeInTheDocument();
      });

      const launchButton = screen.getByRole('button', { name: /Launch RetailHQ/i });
      await user.click(launchButton);

      await waitFor(() => {
        expect(screen.getByText('Home Page')).toBeInTheDocument();
      });
    });
  });

  describe('Progress Tracking', () => {
    it('should not show progress bar on welcome screen', () => {
      render(<CloudSetupWizardPageWrapper />);

      expect(screen.queryByText(/Step/)).not.toBeInTheDocument();
    });

    it('should show progress bar after welcome screen', async () => {
      const user = userEvent.setup();
      render(<CloudSetupWizardPageWrapper />);

      await user.click(screen.getByText('Get Started'));

      expect(screen.getByText(/Step 1 of/)).toBeInTheDocument();
    });
  });
});
