/**
 * MSW Request Handlers
 * Mocks for all API endpoints used in tests
 */

import { http, HttpResponse } from 'msw';

const API_BASE_URL = 'http://localhost:8081/api';

export const handlers = [
  // Login - successful
  http.post(`${API_BASE_URL}/auth/login`, async ({ request }) => {
    const body = await request.json() as { username: string; password: string };

    // Simulate invalid credentials
    if (body.username === 'wronguser' || body.password === 'wrongpass') {
      return HttpResponse.json(
        { message: 'Invalid credentials' },
        { status: 401 }
      );
    }

    // Simulate empty credentials
    if (!body.username || !body.password) {
      return HttpResponse.json(
        { message: 'Username and password are required' },
        { status: 400 }
      );
    }

    // Successful login
    return HttpResponse.json({
      accessToken: 'eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyLCJleHAiOjk5OTk5OTk5OTksInVzZXJuYW1lIjoidGVzdHVzZXIiLCJpZCI6IjEyMyIsInBlcm1pc3Npb25zIjpbIlBST0RVQ1RfUkVBRCIsIlBST0RVQ1RfV1JJVEUiXX0.C9pGXvBHfHdJsYdRfPOmfZpFw7xO7l8YxPwCqYqXzTM',
      refreshToken: 'refresh-token-123',
    });
  }),

  // Register - successful
  http.post(`${API_BASE_URL}/auth/register`, async ({ request }) => {
    const body = await request.json() as { username: string; email: string; password: string };

    // Simulate duplicate username
    if (body.username === 'existinguser') {
      return HttpResponse.json(
        { message: 'Username already exists' },
        { status: 409 }
      );
    }

    // Successful registration - return tokens
    return HttpResponse.json({
      accessToken: 'eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiI0NTYiLCJuYW1lIjoidGVzdHVzZXIiLCJpYXQiOjE1MTYyMzkwMjIsImV4cCI6OTk5OTk5OTk5OSwidXNlcm5hbWUiOiJ0ZXN0dXNlciIsImlkIjoiNDU2IiwicGVybWlzc2lvbnMiOlsiUFJPRFVDVF9SRUFEIiwiUFJPRFVDVF9XUklURSJdfQ.abc123def456',
      refreshToken: 'refresh-token-456',
    }, { status: 201 });
  }),

  // Get Profile
  http.get(`${API_BASE_URL}/users/profile`, ({ request }) => {
    const authHeader = request.headers.get('Authorization');

    // Check for valid token
    if (!authHeader || !authHeader.startsWith('Bearer ')) {
      return HttpResponse.json(
        { message: 'Unauthorized' },
        { status: 401 }
      );
    }

    return HttpResponse.json({
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
        },
        {
          id: '2',
          name: 'ADMIN',
          description: 'Admin role',
          isSystem: true,
          permissions: ['ADMIN_READ', 'SYSTEM_ADMIN']
        }
      ]
    });
  }),

  // Refresh Token
  http.post(`${API_BASE_URL}/auth/refresh`, async ({ request }) => {
    const body = await request.json() as { refreshToken: string };

    // Simulate invalid refresh token
    if (body.refreshToken === 'invalid-refresh-token') {
      return HttpResponse.json(
        { message: 'Invalid refresh token' },
        { status: 401 }
      );
    }

    return HttpResponse.json({
      accessToken: 'new-access-token',
      refreshToken: 'new-refresh-token',
    });
  }),

  // Logout
  http.post(`${API_BASE_URL}/auth/logout`, () => {
    return HttpResponse.json({ message: 'Logged out successfully' });
  }),

  // Check Permissions
  http.post(`${API_BASE_URL}/auth/check-permissions`, async ({ request }) => {
    const body = await request.json() as { permissions: string[] };
    const authHeader = request.headers.get('Authorization');

    if (!authHeader) {
      return HttpResponse.json(
        { message: 'Unauthorized' },
        { status: 401 }
      );
    }

    // Mock user has PRODUCT_READ and PRODUCT_WRITE
    const userPermissions = ['PRODUCT_READ', 'PRODUCT_WRITE'];
    const hasPermission = body.permissions.every(p => userPermissions.includes(p));

    return HttpResponse.json({ hasPermission });
  }),
];

// Helper to create handlers with custom responses
export const createMockHandler = {
  login: (response: any, status = 200) =>
    http.post(`${API_BASE_URL}/auth/login`, () =>
      HttpResponse.json(response, { status })
    ),

  register: (response: any, status = 201) =>
    http.post(`${API_BASE_URL}/auth/register`, () =>
      HttpResponse.json(response, { status })
    ),

  getProfile: (response: any, status = 200) =>
    http.get(`${API_BASE_URL}/users/profile`, () =>
      HttpResponse.json(response, { status })
    ),

  refreshToken: (response: any, status = 200) =>
    http.post(`${API_BASE_URL}/auth/refresh`, () =>
      HttpResponse.json(response, { status })
    ),
};
