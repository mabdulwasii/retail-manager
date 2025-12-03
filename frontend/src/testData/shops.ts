/**
 * Test Data: Shops
 * Mock shop data for testing
 */

export const getMockShop = (overrides = {}) => ({
  id: 'shop1',
  name: 'Electronics Store',
  status: 'ACTIVE',
  location: 'Downtown',
  address: '123 Main St',
  city: 'Lagos',
  state: 'Lagos',
  country: 'Nigeria',
  phone: '+234-xxx-xxx-xxxx',
  email: 'electronics@example.com',
  ownerId: '2',
  createdAt: new Date('2024-01-01').toISOString(),
  ...overrides
})

export const getMockShopsList = () => ({
  content: [
    getMockShop(),
    getMockShop({
      id: 'shop2',
      name: 'Fashion Boutique',
      location: 'Mall',
      address: '456 Shopping Center',
      email: 'fashion@example.com'
    }),
    getMockShop({
      id: 'shop3',
      name: 'Grocery Mart',
      location: 'Suburb',
      address: '789 Market Road',
      email: 'grocery@example.com'
    })
  ],
  totalElements: 3,
  totalPages: 1,
  number: 0,
  size: 20
})

export const getMockActiveShops = () => [
  getMockShop(),
  getMockShop({
    id: 'shop2',
    name: 'Fashion Boutique',
    location: 'Mall'
  })
]

export const getMockShopPerformance = (overrides = {}) => ({
  id: 'shop1',
  name: 'Downtown Electronics',
  revenue: 125000,
  salesCount: 342,
  growth: 15,
  status: 'excellent',
  ...overrides
})

export const getMockShopsPerformance = () => [
  getMockShopPerformance(),
  getMockShopPerformance({
    id: 'shop2',
    name: 'Fashion Store',
    revenue: 85000,
    salesCount: 287,
    growth: 8,
    status: 'good'
  }),
  getMockShopPerformance({
    id: 'shop3',
    name: 'Grocery Mart',
    revenue: 62000,
    salesCount: 198,
    growth: -2,
    status: 'needs_attention'
  })
]
