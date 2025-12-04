/* eslint-disable @typescript-eslint/no-explicit-any */
import { userService } from '../userService'
import { getMockUser, getMockUsersList } from '@/testData/users'
import { http, HttpResponse } from 'msw'
import { setupServer } from 'msw/node'


const server = setupServer(
  // Get shop users
  http.get('*/shops/:shopId/users', () => {
    return HttpResponse.json([
      getMockUser(),
      getMockUser({ id: 'user2', username: 'cashier', email: 'cashier@example.com' })
    ])
  }),

  // Get tenant users
  http.get('*/tenants/:tenantId/users', () => {
    return HttpResponse.json([
      getMockUser(),
      getMockUser({ id: 'user2', username: 'manager2' })
    ])
  }),

  // Create user in tenant
  http.post('*/tenants/:tenantId/users', async ({ request }) => {
    const body = await request.json() as any
    return HttpResponse.json(getMockUser({ id: 'new-user', username: body.username }))
  }),

  // Get all users
  http.get('*/users', () => {
    return HttpResponse.json(getMockUsersList())
  }),

  // Get user by ID
  http.get('*/users/users/:userId', ({ params }) => {
    return HttpResponse.json(getMockUser({ id: params.userId as string }))
  }),

  // Update user
  http.patch('*/users/:userId', async ({ params, request }) => {
    const body = await request.json() as any
    return HttpResponse.json(getMockUser({ id: params.userId as string, ...body }))
  })
)

describe.skip('userService with MSW', () => {
  beforeAll(() => {
    server.listen({ onUnhandledRequest: 'warn' })
  })

  afterEach(() => {
    server.resetHandlers()
  })

  afterAll(() => {
    server.close()
  })

  describe('getShopUsers', () => {
    it('should fetch shop users successfully', async () => {
      const users = await userService.getShopUsers('shop1')

      expect(users).toBeDefined()
      expect(Array.isArray(users)).toBe(true)
      expect(users.length).toBeGreaterThan(0)
      expect(users[0].username).toBe('manager')
    })

    it('should fetch shop users with status filter', async () => {
      const users = await userService.getShopUsers('shop1', 'ACTIVE')

      expect(users).toBeDefined()
      expect(Array.isArray(users)).toBe(true)
    })

    it('should fetch shop users with pagination', async () => {
      const users = await userService.getShopUsers('shop1', undefined, 0, 10)

      expect(users).toBeDefined()
      expect(Array.isArray(users)).toBe(true)
    })

    it('should handle fetch error', async () => {
      server.use(
        http.get('*/shops/:shopId/users', () => {
          return HttpResponse.json(
            { message: 'Unauthorized' },
            { status: 403 }
          )
        })
      )

      await expect(userService.getShopUsers('shop1')).rejects.toThrow()
    })
  })

  describe('getTenantUsers', () => {
    it('should fetch tenant users successfully', async () => {
      const users = await userService.getTenantUsers('tenant1')

      expect(users).toBeDefined()
      expect(Array.isArray(users)).toBe(true)
      expect(users.length).toBeGreaterThan(0)
    })

    it('should handle fetch error', async () => {
      server.use(
        http.get('*/tenants/:tenantId/users', () => {
          return HttpResponse.json(
            { message: 'Tenant not found' },
            { status: 404 }
          )
        })
      )

      await expect(userService.getTenantUsers('invalid')).rejects.toThrow()
    })
  })

  describe('createUserInTenant', () => {
    it('should create user successfully', async () => {
      const newUser = await userService.createUserInTenant('tenant1', {
        username: 'newuser',
        email: 'newuser@example.com',
        password: 'password123',
        firstName: 'New',
        lastName: 'User',
        roles: ['ROLE_CASHIER']
      })

      expect(newUser).toBeDefined()
      expect(newUser.id).toBe('new-user')
      expect(newUser.username).toBe('newuser')
    })

    it('should create user with shop assignment', async () => {
      const newUser = await userService.createUserInTenant('tenant1', {
        username: 'shopuser',
        email: 'shopuser@example.com',
        password: 'password123',
        shopId: 'shop1',
        roles: ['ROLE_CASHIER']
      })

      expect(newUser).toBeDefined()
    })

    it('should handle validation error', async () => {
      server.use(
        http.post('*/tenants/:tenantId/users', () => {
          return HttpResponse.json(
            { message: 'Username already exists' },
            { status: 400 }
          )
        })
      )

      await expect(
        userService.createUserInTenant('tenant1', {
          username: 'duplicate',
          email: 'test@example.com',
          password: 'pass',
          roles: ['ROLE_CASHIER']
        })
      ).rejects.toThrow()
    })
  })

  describe('getAllUsers', () => {
    it('should fetch all users successfully', async () => {
      const result = await userService.getAllUsers()

      expect(result).toBeDefined()
      expect(result.content).toBeDefined()
      expect(Array.isArray(result.content)).toBe(true)
      expect(result.totalElements).toBeGreaterThan(0)
    })

    it('should fetch all users with status filter', async () => {
      const result = await userService.getAllUsers('ACTIVE')

      expect(result).toBeDefined()
      expect(result.content).toBeDefined()
    })

    it('should fetch all users with pagination', async () => {
      const result = await userService.getAllUsers(undefined, 0, 20)

      expect(result).toBeDefined()
      expect(result.content).toBeDefined()
    })

    it('should handle fetch error', async () => {
      server.use(
        http.get('*/users', () => {
          return HttpResponse.json(
            { message: 'Forbidden' },
            { status: 403 }
          )
        })
      )

      await expect(userService.getAllUsers()).rejects.toThrow()
    })
  })

  describe('getUserById', () => {
    it('should fetch user by ID successfully', async () => {
      const user = await userService.getUserById('user1')

      expect(user).toBeDefined()
      expect(user.id).toBe('user1')
      expect(user.username).toBe('manager')
    })

    it('should handle 404 error', async () => {
      server.use(
        http.get('*/users/users/:userId', () => {
          return HttpResponse.json(
            { message: 'User not found' },
            { status: 404 }
          )
        })
      )

      await expect(userService.getUserById('invalid')).rejects.toThrow()
    })
  })

  describe('updateUser', () => {
    it('should update user successfully', async () => {
      const updated = await userService.updateUser('user1', {
        firstName: 'Updated',
        lastName: 'Name'
      })

      expect(updated).toBeDefined()
      expect(updated.id).toBe('user1')
      expect(updated.firstName).toBe('Updated')
    })

    it('should update user email', async () => {
      const updated = await userService.updateUser('user1', {
        email: 'newemail@example.com'
      })

      expect(updated).toBeDefined()
      expect(updated.email).toBe('newemail@example.com')
    })

    it('should update user roles', async () => {
      const updated = await userService.updateUser('user1', {
        roles: ['ROLE_MANAGER', 'ROLE_CASHIER']
      })

      expect(updated).toBeDefined()
    })

    it('should handle update error', async () => {
      server.use(
        http.patch('*/users/:userId', () => {
          return HttpResponse.json(
            { message: 'Cannot modify system admin' },
            { status: 403 }
          )
        })
      )

      await expect(
        userService.updateUser('user1', { email: 'test@example.com' })
      ).rejects.toThrow()
    })
  })
})
