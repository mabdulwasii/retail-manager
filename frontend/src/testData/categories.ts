/**
 * Test Data: Categories
 * Mock category data for testing
 */

export const getMockCategory = (overrides = {}) => ({
  id: 'cat1',
  name: 'Electronics',
  description: 'Electronic devices and accessories',
  shopId: 'shop1',
  parentId: null,
  level: 0,
  isActive: true,
  createdAt: new Date('2024-01-01').toISOString(),
  updatedAt: new Date('2024-01-01').toISOString(),
  ...overrides
})

export const getMockCategories = () => [
  getMockCategory(),
  getMockCategory({
    id: 'cat2',
    name: 'Computers',
    description: 'Laptops, desktops, and accessories',
    parentId: 'cat1',
    level: 1
  }),
  getMockCategory({
    id: 'cat3',
    name: 'Mobile Phones',
    description: 'Smartphones and tablets',
    parentId: 'cat1',
    level: 1
  }),
  getMockCategory({
    id: 'cat4',
    name: 'Clothing',
    description: 'Apparel and fashion items',
    parentId: null,
    level: 0
  })
]

export const getMockCategoryTree = () => [
  {
    ...getMockCategory(),
    children: [
      getMockCategory({
        id: 'cat2',
        name: 'Computers',
        parentId: 'cat1',
        level: 1,
        children: []
      }),
      getMockCategory({
        id: 'cat3',
        name: 'Mobile Phones',
        parentId: 'cat1',
        level: 1,
        children: []
      })
    ]
  },
  {
    ...getMockCategory({
      id: 'cat4',
      name: 'Clothing',
      parentId: null,
      level: 0
    }),
    children: []
  }
]

export const getMockCategoryNames = () => [
  { id: 'cat1', name: 'Electronics' },
  { id: 'cat2', name: 'Computers' },
  { id: 'cat3', name: 'Mobile Phones' },
  { id: 'cat4', name: 'Clothing' }
]
