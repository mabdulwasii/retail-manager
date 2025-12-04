
export const getMockProduct = (overrides = {}) => ({
  id: 'prod1',
  name: 'Laptop Computer',
  sku: 'LAPTOP-001',
  description: 'High-performance laptop',
  category: {
    id: 'cat1',
    name: 'Electronics'
  },
  price: 1200.00,
  cost: 800.00,
  shopId: 'shop1',
  status: 'ACTIVE',
  stockQuantity: 10,
  lowStockThreshold: 5,
  barcode: '123456789012',
  imageUrl: 'https://example.com/laptop.jpg',
  createdAt: new Date('2024-01-01').toISOString(),
  updatedAt: new Date('2024-01-01').toISOString(),
  ...overrides
})

export const getMockProductsList = () => ({
  content: [
    getMockProduct(),
    getMockProduct({
      id: 'prod2',
      name: 'Wireless Mouse',
      sku: 'MOUSE-001',
      price: 25.00,
      cost: 15.00,
      stockQuantity: 50
    }),
    getMockProduct({
      id: 'prod3',
      name: 'USB Keyboard',
      sku: 'KEYBOARD-001',
      price: 45.00,
      cost: 30.00,
      stockQuantity: 30
    })
  ],
  totalElements: 3,
  totalPages: 1,
  size: 20,
  number: 0,
  first: true,
  last: true
})
