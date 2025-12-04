

export const getMockExpenseCategory = (overrides = {}) => ({
  id: 'cat1',
  name: 'Office Supplies',
  description: 'Office supplies and stationery',
  isActive: true,
  requiresApproval: true,
  approvalLimit: 500,
  ...overrides
})

export const getMockExpense = (overrides = {}) => ({
  id: 'exp1',
  title: 'Office Supplies Purchase',
  description: 'Pens, paper, and folders',
  category: getMockExpenseCategory(),
  amount: 150.00,
  date: new Date('2024-01-15').toISOString(),
  shopId: 'shop1',
  requestedBy: 'user1',
  requestedByName: 'John Doe',
  status: 'PENDING_APPROVAL' as const,
  tags: ['office', 'supplies'],
  createdAt: new Date('2024-01-15').toISOString(),
  updatedAt: new Date('2024-01-15').toISOString(),
  ...overrides
})

export const getMockExpenses = () => ({
  content: [
    getMockExpense(),
    getMockExpense({
      id: 'exp2',
      title: 'Utility Bills',
      description: 'Electricity and water',
      amount: 800.00,
      status: 'APPROVED' as const,
      approvedBy: 'admin1',
      approvedByName: 'Admin User',
      category: getMockExpenseCategory({
        id: 'cat2',
        name: 'Utilities',
        requiresApproval: false
      })
    }),
    getMockExpense({
      id: 'exp3',
      title: 'Marketing Campaign',
      description: 'Social media ads',
      amount: 1500.00,
      status: 'PAID' as const,
      approvedBy: 'admin1',
      approvedByName: 'Admin User',
      category: getMockExpenseCategory({
        id: 'cat3',
        name: 'Marketing',
        approvalLimit: 2000
      }),
      tags: ['marketing', 'ads']
    })
  ],
  totalElements: 3,
  totalPages: 1,
  number: 0,
  size: 20
})

export const getMockExpenseCategories = () => [
  getMockExpenseCategory(),
  getMockExpenseCategory({
    id: 'cat2',
    name: 'Utilities',
    description: 'Electricity, water, and internet',
    requiresApproval: false
  }),
  getMockExpenseCategory({
    id: 'cat3',
    name: 'Marketing',
    description: 'Marketing and advertising expenses',
    approvalLimit: 2000
  }),
  getMockExpenseCategory({
    id: 'cat4',
    name: 'Equipment',
    description: 'Equipment purchases',
    approvalLimit: 5000
  })
]
