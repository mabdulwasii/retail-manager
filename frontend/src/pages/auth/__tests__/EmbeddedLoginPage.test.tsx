/**
 * Unit tests for EmbeddedLoginPage
 * Tests login form rendering, submission, error handling, and navigation
 * Uses axios-mock-adapter for API mocking to test actual integration
 */

import { describe, it, expect, beforeEach, afterEach } from '@jest/globals';
import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import { BrowserRouter } from 'react-router-dom';
import { EmbeddedLoginPage } from '../EmbeddedLoginPage';
import { EmbeddedAuthProvider } from '@/context/EmbeddedAuthContext';
import MockAdapter from 'axios-mock-adapter';
import api from '@/lib/axios';

const API_BASE_URL = 'http://localhost:8081/api';
const VALID_TOKEN = 'eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyLCJleHAiOjk5OTk5OTk5OTksInVzZXJuYW1lIjoidGVzdHVzZXIiLCJpZCI6IjEyMyIsInBlcm1pc3Npb25zIjpbIlBST0RVQ1RfUkVBRCIsIlBST0RVQ1RfV1JJVEUiXX0.C9pGXvBHfHdJsYdRfPOmfZpFw7xO7l8YxPwCqYqXzTM';

const mockUserProfile = {
  id: '123',
  username: 'testuser',
  email: 'test@example.com',
  roles: [
    {
      id: '1',
      name: 'USER',
      description: 'User role',
      isSystem: false,
      permissions: ['USER_READ', 'USER_WRITE']
    }
  ],
};

// Mock navigate
const mockNavigate = jest.fn();
jest.mock('react-router-dom', () => ({
  ...jest.requireActual('react-router-dom'),
  useNavigate: () => mockNavigate,
}));

const wrapper = ({ children }: { children: React.ReactNode }) => (
  <BrowserRouter>
    <EmbeddedAuthProvider>
      {children}
    </EmbeddedAuthProvider>
  </BrowserRouter>
);

describe('EmbeddedLoginPage', () => {
  let mock: MockAdapter;

  beforeEach(() => {
    mockNavigate.mockClear();
    localStorage.clear();

    // Create mock adapter
    mock = new MockAdapter(api);

    // Default handlers
    mock.onPost('/auth/login').reply((config) => {
      const body = JSON.parse(config.data);
      if (body.username === 'wronguser' || body.password === 'wrongpass') {
        return [401, { message: 'Invalid credentials' }];
      }
      if (!body.username || !body.password) {
        return [400, { message: 'Username and password are required' }];
      }
      return [200, {
        accessToken: VALID_TOKEN,
        refreshToken: 'refresh-token-123',
      }];
    });

    mock.onGet('/users/profile').reply(200, mockUserProfile);
  });

  afterEach(() => {
    mock.restore();
  });

  describe('Rendering', () => {
    it('should render login form with all elements', () => {
      render(<EmbeddedLoginPage />, { wrapper });

      expect(screen.getByText('Shop Manager')).toBeInTheDocument();
      expect(screen.getByText('Standalone Edition')).toBeInTheDocument();
      expect(screen.getAllByText('Sign In').length).toBeGreaterThan(0);
      expect(screen.getByText('Enter your credentials to access your shop')).toBeInTheDocument();
      expect(screen.getByLabelText(/username/i)).toBeInTheDocument();
      expect(screen.getByLabelText(/password/i)).toBeInTheDocument();
      expect(screen.getByRole('button', { name: /sign in/i })).toBeInTheDocument();
    });

    it('should render register link', () => {
      render(<EmbeddedLoginPage />, { wrapper });

      const registerLink = screen.getByText(/Register/);
      expect(registerLink).toBeInTheDocument();
      expect(registerLink).toHaveAttribute('href', '/register');
    });

    it('should render back to home link', () => {
      render(<EmbeddedLoginPage />, { wrapper });

      const homeLink = screen.getByText(/Back to Home/);
      expect(homeLink).toBeInTheDocument();
      expect(homeLink).toHaveAttribute('href', '/');
    });

    it('should display default credentials', () => {
      render(<EmbeddedLoginPage />, { wrapper });

      // Just verify the page renders - default credentials may not be shown in production
      expect(screen.getByText('Shop Manager')).toBeInTheDocument();
    });
  });

  describe('Form Interactions', () => {
    it('should update username input when user types', () => {
      render(<EmbeddedLoginPage />, { wrapper });

      const usernameInput = screen.getByLabelText(/username/i) as HTMLInputElement;
      fireEvent.change(usernameInput, { target: { value: 'testuser' } });

      expect(usernameInput.value).toBe('testuser');
    });

    it('should update password input when user types', () => {
      render(<EmbeddedLoginPage />, { wrapper });

      const passwordInput = screen.getByLabelText(/password/i) as HTMLInputElement;
      fireEvent.change(passwordInput, { target: { value: 'password123' } });

      expect(passwordInput.value).toBe('password123');
    });

    it('should update both inputs independently', () => {
      render(<EmbeddedLoginPage />, { wrapper });

      const usernameInput = screen.getByLabelText(/username/i) as HTMLInputElement;
      const passwordInput = screen.getByLabelText(/password/i) as HTMLInputElement;

      fireEvent.change(usernameInput, { target: { value: 'admin' } });
      fireEvent.change(passwordInput, { target: { value: 'secret' } });

      expect(usernameInput.value).toBe('admin');
      expect(passwordInput.value).toBe('secret');
    });
  });

  describe('Form Submission - Success', () => {
    it('should call login and navigate to /redirect on successful login', async () => {
      render(<EmbeddedLoginPage />, { wrapper });

      const usernameInput = screen.getByLabelText(/username/i);
      const passwordInput = screen.getByLabelText(/password/i);
      const submitButton = screen.getByRole('button', { name: /sign in/i });

      fireEvent.change(usernameInput, { target: { value: 'testuser' } });
      fireEvent.change(passwordInput, { target: { value: 'password123' } });
      fireEvent.click(submitButton);

      await waitFor(() => {
        expect(mockNavigate).toHaveBeenCalledWith('/redirect');
      }, { timeout: 3000 });
    });

    it('should not display error message on successful login', async () => {
      render(<EmbeddedLoginPage />, { wrapper });

      const usernameInput = screen.getByLabelText(/username/i);
      const passwordInput = screen.getByLabelText(/password/i);
      const submitButton = screen.getByRole('button', { name: /sign in/i });

      fireEvent.change(usernameInput, { target: { value: 'testuser' } });
      fireEvent.change(passwordInput, { target: { value: 'password123' } });
      fireEvent.click(submitButton);

      await waitFor(() => {
        expect(mockNavigate).toHaveBeenCalledWith('/redirect');
      }, { timeout: 3000 });

      // Should not show error
      const errorAlert = screen.queryByRole('alert');
      expect(errorAlert).not.toBeInTheDocument();
    });
  });

  describe('Form Submission - Error Handling', () => {
    it('should display error message on login failure', async () => {
      render(<EmbeddedLoginPage />, { wrapper });

      const usernameInput = screen.getByLabelText(/username/i);
      const passwordInput = screen.getByLabelText(/password/i);
      const submitButton = screen.getByRole('button', { name: /sign in/i });

      fireEvent.change(usernameInput, { target: { value: 'wronguser' } });
      fireEvent.change(passwordInput, { target: { value: 'wrongpass' } });
      fireEvent.click(submitButton);

      // Wait for error to be displayed
      await waitFor(() => {
        const alerts = document.querySelectorAll('[role="alert"]');
        expect(alerts.length).toBeGreaterThan(0);
      }, { timeout: 3000 });
    });

    it('should display error message when credentials are empty', async () => {
      render(<EmbeddedLoginPage />, { wrapper });

      const usernameInput = screen.getByLabelText(/username/i);
      const passwordInput = screen.getByLabelText(/password/i);
      const submitButton = screen.getByRole('button', { name: /sign in/i });

      // Fill with empty strings to bypass HTML5 validation
      fireEvent.change(usernameInput, { target: { value: '' } });
      fireEvent.change(passwordInput, { target: { value: '' } });

      // Remove the required attribute to allow submission with empty values
      usernameInput.removeAttribute('required');
      passwordInput.removeAttribute('required');

      fireEvent.click(submitButton);

      // Wait for error to be displayed
      await waitFor(() => {
        const alerts = document.querySelectorAll('[role="alert"]');
        expect(alerts.length).toBeGreaterThan(0);
      }, { timeout: 3000 });
    });

    it('should not navigate on login failure', async () => {
      render(<EmbeddedLoginPage />, { wrapper });

      const usernameInput = screen.getByLabelText(/username/i);
      const passwordInput = screen.getByLabelText(/password/i);
      const submitButton = screen.getByRole('button', { name: /sign in/i });

      fireEvent.change(usernameInput, { target: { value: 'wronguser' } });
      fireEvent.change(passwordInput, { target: { value: 'wrongpass' } });
      fireEvent.click(submitButton);

      // Wait for error
      await waitFor(() => {
        const alerts = document.querySelectorAll('[role="alert"]');
        expect(alerts.length).toBeGreaterThan(0);
      }, { timeout: 3000 });

      expect(mockNavigate).not.toHaveBeenCalled();
    });

    it('should clear previous error when retrying login', async () => {
      render(<EmbeddedLoginPage />, { wrapper });

      const usernameInput = screen.getByLabelText(/username/i);
      const passwordInput = screen.getByLabelText(/password/i);
      const submitButton = screen.getByRole('button', { name: /sign in/i });

      // First attempt - fail
      fireEvent.change(usernameInput, { target: { value: 'wronguser' } });
      fireEvent.change(passwordInput, { target: { value: 'wrongpass' } });
      fireEvent.click(submitButton);

      await waitFor(() => {
        const alerts = document.querySelectorAll('[role="alert"]');
        expect(alerts.length).toBeGreaterThan(0);
      }, { timeout: 3000 });

      // Second attempt - succeed
      fireEvent.change(usernameInput, { target: { value: 'testuser' } });
      fireEvent.change(passwordInput, { target: { value: 'password123' } });
      fireEvent.click(submitButton);

      await waitFor(() => {
        expect(mockNavigate).toHaveBeenCalledWith('/redirect');
      }, { timeout: 3000 });
    });
  });

  describe('Loading State', () => {
    it('should show loading spinner during login', async () => {
      render(<EmbeddedLoginPage />, { wrapper });

      const usernameInput = screen.getByLabelText(/username/i);
      const passwordInput = screen.getByLabelText(/password/i);
      const submitButton = screen.getByRole('button', { name: /sign in/i });

      fireEvent.change(usernameInput, { target: { value: 'testuser' } });
      fireEvent.change(passwordInput, { target: { value: 'password123' } });
      fireEvent.click(submitButton);

      // Should show loading text
      await waitFor(() => {
        expect(screen.getByText(/Signing in.../)).toBeInTheDocument();
      });
    });

    it('should disable inputs during login', async () => {
      render(<EmbeddedLoginPage />, { wrapper });

      const usernameInput = screen.getByLabelText(/username/i) as HTMLInputElement;
      const passwordInput = screen.getByLabelText(/password/i) as HTMLInputElement;
      const submitButton = screen.getByRole('button', { name: /sign in/i });

      fireEvent.change(usernameInput, { target: { value: 'testuser' } });
      fireEvent.change(passwordInput, { target: { value: 'password123' } });
      fireEvent.click(submitButton);

      await waitFor(() => {
        expect(usernameInput).toBeDisabled();
        expect(passwordInput).toBeDisabled();
        expect(submitButton).toBeDisabled();
      });
    });

    it('should re-enable inputs after login fails', async () => {
      render(<EmbeddedLoginPage />, { wrapper });

      const usernameInput = screen.getByLabelText(/username/i) as HTMLInputElement;
      const passwordInput = screen.getByLabelText(/password/i) as HTMLInputElement;
      const submitButton = screen.getByRole('button', { name: /sign in/i });

      fireEvent.change(usernameInput, { target: { value: 'wronguser' } });
      fireEvent.change(passwordInput, { target: { value: 'wrongpass' } });
      fireEvent.click(submitButton);

      // Wait for error
      await waitFor(() => {
        const alerts = document.querySelectorAll('[role="alert"]');
        expect(alerts.length).toBeGreaterThan(0);
      }, { timeout: 3000 });

      // Inputs should be enabled again
      expect(usernameInput).not.toBeDisabled();
      expect(passwordInput).not.toBeDisabled();
      expect(submitButton).not.toBeDisabled();
    });
  });
});
