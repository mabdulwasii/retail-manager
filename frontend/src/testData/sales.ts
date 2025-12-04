
export const getMockSale = (overrides = {}) => ({
  id: 'sale1',
  transactionNumber: 'TXN-2024-001',
  receiptNumber: 'RCP-2024-001',
  shopId: 'shop1',
  customerId: null,
  customerName: 'John Doe',
  customerPhone: '+1234567890',
  customerEmail: 'john@example.com',
  cashierId: 'user1',
  cashierName: 'Cashier Name',
  paymentMethod: 'CASH',
  paymentReference: null,
  subtotal: 100.00,
  taxAmount: 10.00,
  discountAmount: 0,
  totalAmount: 110.00,
  amountPaid: 110.00,
  changeGiven: 0,
  notes: null,
  status: 'COMPLETED',
  transactionDate: new Date('2024-01-15').toISOString(),
  createdAt: new Date('2024-01-15').toISOString(),
  updatedAt: new Date('2024-01-15').toISOString(),
  lineItems: [
    {
      id: 'item1',
      productId: 'prod1',
      productName: 'Laptop',
      productSku: 'LAPTOP-001',
      quantity: 1,
      unitPrice: 100.00,
      discount: 0,
      subtotal: 100.00,
      taxAmount: 10.00,
      total: 110.00
    }
  ],
  ...overrides
})

export const getMockCartSummary = (overrides = {}) => ({
  itemCount: 2,
  subtotal: 125.00,
  taxAmount: 12.50,
  discountAmount: 0,
  total: 137.50,
  formattedSubtotal: '$125.00',
  formattedTaxAmount: '$12.50',
  formattedTotal: '$137.50',
  ...overrides
})

export const getMockCartItem = (overrides = {}) => ({
  product: {
    id: 'prod1',
    name: 'Laptop Computer',
    sku: 'LAPTOP-001',
    price: 100.00,
    barcode: '123456789012'
  },
  quantity: 1,
  unitPrice: 100.00,
  inventoryId: 'inv1',
  subtotal: 100.00,
  ...overrides
})
