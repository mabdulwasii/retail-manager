import { rest } from 'msw'
import { mockData } from '../data'

// Export mock data for backward compatibility
export const mockUserProfile = mockData.users.profile
export const mockUserProfileInvestor = mockData.users.investor

export const userHandlers = [
  // Get user profile - success
  rest.get('/api/users/profile', (req, res, ctx) => {
    const authHeader = req.headers.get('authorization')

    if (!authHeader || !authHeader.startsWith('Bearer ')) {
      return res(
        ctx.status(401),
        ctx.json({ message: 'Unauthorized' })
      )
    }

    return res(
      ctx.status(200),
      ctx.json(mockUserProfile)
    )
  }),

  // Get user profile - investor variant
  rest.get('/api/users/profile', (req, res, ctx) => {
    const investorParam = req.url.searchParams.get('investor')

    if (investorParam === 'true') {
      return res(
        ctx.status(200),
        ctx.json(mockUserProfileInvestor)
      )
    }

    return res(
      ctx.status(200),
      ctx.json(mockUserProfile)
    )
  }),

  // Get user profile - error case
  rest.get('/api/users/profile', (req, res, ctx) => {
    const errorParam = req.url.searchParams.get('error')

    if (errorParam === 'true') {
      return res(
        ctx.status(500),
        ctx.json({ message: 'Internal server error' })
      )
    }

    return res(
      ctx.status(200),
      ctx.json(mockUserProfile)
    )
  }),

  // Get user profile - not found
  rest.get('/api/users/profile', (req, res, ctx) => {
    const notFoundParam = req.url.searchParams.get('notfound')

    if (notFoundParam === 'true') {
      return res(
        ctx.status(404),
        ctx.json({ message: 'User not found' })
      )
    }

    return res(
      ctx.status(200),
      ctx.json(mockUserProfile)
    )
  })
]