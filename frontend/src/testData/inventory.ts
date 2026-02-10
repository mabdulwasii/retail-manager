
export const getMockInventoryItem = (overrides = {}) => ({
  id: 'inv1',
  productId: 'prod1',
  productName: 'Laptop Computer',
  productSku: 'LAPTOP-001',
  shopId: 'shop1',
  shopName: 'Main Store',
  quantity: 50,
  reservedQuantity: 5,
  availableQuantity: 45,
  minStockLevel: 5,
  costPrice: 800.00,
  sellingPrice: 1200.00,
  status: 'IN_STOCK',
  location: 'A-1-1',
  supplier: 'Tech Supplier Inc',
  lastRestocked: new Date('2024-01-01').toISOString(),
  expiryDate: null,
  batchNumber: 'BATCH-001',
  notes: 'Premium laptop inventory',
  createdAt: new Date('2024-01-01').toISOString(),
  updatedAt: new Date('2024-01-01').toISOString(),
  ...overrides
})

export const getMockInventoryList = () => [
  getMockInventoryItem(),
  getMockInventoryItem({
    id: 'inv2',
    productId: 'prod2',
    productName: 'Wireless Mouse',
    productSku: 'MOUSE-001',
    quantity: 100,
    availableQuantity: 95,
    reservedQuantity: 5,
    sellingPrice: 25.00,
    costPrice: 15.00,
    status: 'IN_STOCK'
  }),
  getMockInventoryItem({
    id: 'inv3',
    productId: 'prod3',
    productName: 'USB Keyboard',
    productSku: 'KEYBOARD-001',
    quantity: 8,
    availableQuantity: 8,
    reservedQuantity: 0,
    sellingPrice: 45.00,
    costPrice: 30.00,
    status: 'LOW_STOCK'
  })
]

export const getMockInventorySummary = (overrides = {}) => ({
  totalItems: 158,
  totalValue: 125000.00,
  lowStockItems: 12,
  outOfStockItems: 3,
  expiringSoonItems: 5,
  categoriesCount: 8,
  ...overrides
})
