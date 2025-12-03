import { http, HttpResponse } from 'msw'
import { mockData } from '../data'

// Export mock data for backward compatibility
export const mockUserProfile = mockData.users.profile
export const mockUserProfileInvestor = mockData.users.investor

// Empty handlers array - tests will define their own using server.use()
export const userHandlers: any[] = []