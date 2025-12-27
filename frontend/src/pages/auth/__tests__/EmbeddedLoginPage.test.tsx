/**
 * Unit tests for EmbeddedLoginPage
 * Tests login form rendering, submission, error handling, and navigation
 * Uses MSW for API mocking to test actual integration
 */

import { describe, it, expect, beforeEach } from '@jest/globals';
import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import { BrowserRouter } from 'react-router-dom';
import { EmbeddedLoginPage } from '../EmbeddedLoginPage';
import { EmbeddedAuthProvider } from '@/context/EmbeddedAuthContext';
import { server } from '@/test/mocks/server';
import { http, HttpResponse } from 'msw';

const API_BASE_URL = 'http://localhost:8081/api';

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
  beforeEach(() => {
    mockNavigate.mockClear();
    localStorage.clear();
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

      expect(screen.getByText(/Default credentials:/)).toBeInTheDocument();
      expect(screen.getByText(/superadmin \/ Admin123!/)).toBeInTheDocument();
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
      server.use(
        http.post(`${API_BASE_URL}/auth/login`, () => {
          return HttpResponse.json(
            { message: 'Username and password are required' },
            { status: 400 }
          );
        })
      );

      render(<EmbeddedLoginPage />, { wrapper });

      const submitButton = screen.getByRole('button', { name: /sign in/i });
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
