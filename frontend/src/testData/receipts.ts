
export const getMockReceipt = (overrides = {}) => ({
  id: 'receipt1',
  receiptNumber: 'RCP-2024-001',
  transactionId: 'txn1',
  shopId: 'shop1',
  shopName: 'Electronics Store',
  customerName: 'John Doe',
  customerEmail: 'john@example.com',
  items: [
    {
      name: 'Laptop',
      quantity: 1,
      price: 1200.00,
      total: 1200.00
    },
    {
      name: 'Mouse',
      quantity: 2,
      price: 25.00,
      total: 50.00
    }
  ],
  subtotal: 1250.00,
  tax: 125.00,
  total: 1375.00,
  paymentMethod: 'CARD',
  status: 'GENERATED',
  printed: false,
  emailed: false,
  printedAt: null,
  emailedAt: null,
  printedBy: null,
  emailAddress: null,
  createdAt: new Date('2024-01-15').toISOString(),
  updatedAt: new Date('2024-01-15').toISOString(),
  ...overrides
})

export const getMockReceipts = () => ({
  content: [
    getMockReceipt(),
    getMockReceipt({
      id: 'receipt2',
      receiptNumber: 'RCP-2024-002',
      transactionId: 'txn2',
      customerName: 'Jane Smith',
      total: 350.00,
      printed: true,
      printedAt: new Date('2024-01-16').toISOString()
    }),
    getMockReceipt({
      id: 'receipt3',
      receiptNumber: 'RCP-2024-003',
      transactionId: 'txn3',
      customerName: 'Bob Johnson',
      total: 89.99,
      emailed: true,
      emailedAt: new Date('2024-01-17').toISOString(),
      emailAddress: 'bob@example.com'
    })
  ],
  totalElements: 3,
  totalPages: 1,
  number: 0,
  size: 20
})

export const getMockPrintableContent = () => ({
  html: '<html><body><h1>Receipt</h1><p>Transaction ID: txn1</p></body></html>',
  css: 'body { font-family: Arial; }'
})

export const getMockReceiptContent = () => ({
  receiptNumber: 'RCP-2024-001',
  shopName: 'Electronics Store',
  shopAddress: '123 Main St',
  items: [
    { name: 'Laptop', qty: 1, price: 1200.00 },
    { name: 'Mouse', qty: 2, price: 25.00 }
  ],
  total: 1375.00,
  date: new Date('2024-01-15').toISOString()
})
