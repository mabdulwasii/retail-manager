import { setupServer } from 'msw/node'
import { handlers } from './handlers'

/**
 * MSW Server for Node.js (tests)
 * This configures request interception for testing environment
 * 
 * Usage:
 * - Server is automatically started before all tests
 * - Handlers are reset after each test for isolation
 * - Override handlers in individual tests using server.use()
 */
export const server = setupServer(...handlers)

// Enable API mocking before tests run
beforeAll(() => {
  server.listen({
    onUnhandledRequest: 'warn' // Warn mode: helps debug missing handlers
  })
})

// Reset handlers after each test to ensure test isolation
afterEach(() => {
  // Reset any runtime handlers tests may have added
  server.resetHandlers()
})

// Clean up after all tests are done
afterAll(() => {
  // Disable request interception and clean up
  server.close()
})
