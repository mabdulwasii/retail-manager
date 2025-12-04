import { expenseService } from '../expenseService'
import { getMockExpenses, getMockExpense } from '@/testData/expenses'
import { http, HttpResponse } from 'msw'
import { setupServer } from 'msw/node'


// Setup MSW server with handlers
const server = setupServer(
  // Get expense summary
  http.get('*/shops/:shopId/expenses/summary', ({ params }) => {
    return HttpResponse.json({
      totalExpenses: 25,
      pendingApproval: 5,
      approvedExpenses: 15,
      totalAmount: 12500.00,
      monthlyTotal: 8500.00,
      categoryBreakdown: [
        { category: 'Office Supplies', itemCount: 10, totalValue: 3500 },
        { category: 'Utilities', itemCount: 8, totalValue: 5000 },
        { category: 'Marketing', itemCount: 7, totalValue: 4000 }
      ]
    })
  }),

  // Get expenses
  http.get('*/shops/:shopId/expenses', () => {
    return HttpResponse.json(getMockExpenses())
  }),

  // Get single expense
  http.get('*/expenses/:expenseId', ({ params }) => {
    return HttpResponse.json(getMockExpense({ id: params.expenseId as string }))
  }),

  // Create expense
  http.post('*/shops/:shopId/expenses', async () => {
    return HttpResponse.json(getMockExpense({ id: 'new-expense' }))
  }),

  // Update expense
  http.patch('*/expenses/:expenseId', async ({ params }) => {
    return HttpResponse.json(getMockExpense({ id: params.expenseId as string, title: 'Updated Expense' }))
  }),

  // Delete expense
  http.delete('*/expenses/:expenseId', () => {
    return new HttpResponse(null, { status: 204 })
  }),

  // Approve expense
  http.post('*/expenses/:expenseId/approve', ({ params }) => {
    return HttpResponse.json(getMockExpense({ id: params.expenseId as string, status: 'APPROVED' }))
  }),

  // Reject expense
  http.post('*/expenses/:expenseId/reject', ({ params }) => {
    return HttpResponse.json(getMockExpense({ id: params.expenseId as string, status: 'REJECTED' }))
  }),

  // Upload receipt
  http.post('*/expenses/:expenseId/receipt', () => {
    return HttpResponse.json({ receiptUrl: 'https://example.com/receipt.pdf' })
  })
)

describe('expenseService with MSW', () => {
  beforeAll(() => {
    server.listen({ onUnhandledRequest: 'warn' })
  })

  afterEach(() => {
    server.resetHandlers()
  })

  afterAll(() => {
    server.close()
  })

  describe('getExpenseSummary', () => {
    it('should fetch expense summary successfully', async () => {
      const summary = await expenseService.getExpenseSummary('shop1')

      expect(summary).toBeDefined()
      expect(summary.totalExpenses).toBe(25)
      expect(summary.pendingApproval).toBe(5)
      expect(summary.approvedExpenses).toBe(15)
      expect(summary.totalAmount).toBe(12500.00)
      expect(summary.categoryBreakdown).toHaveLength(3)
    })

    it('should fetch summary with date filters', async () => {
      const summary = await expenseService.getExpenseSummary(
        'shop1',
        '2024-01-01',
        '2024-01-31'
      )

      expect(summary).toBeDefined()
      expect(summary.totalExpenses).toBeGreaterThanOrEqual(0)
    })

    it('should handle error when API fails', async () => {
      server.use(
        http.get('*/shops/:shopId/expenses/summary', () => {
          return HttpResponse.json(
            { message: 'Internal server error' },
            { status: 500 }
          )
        })
      )

      await expect(
        expenseService.getExpenseSummary('shop1')
      ).rejects.toThrow()
    })
  })

  describe('getExpenses', () => {
    it('should fetch expenses successfully', async () => {
      const expenses = await expenseService.getExpenses('shop1')

      expect(expenses).toBeDefined()
      expect(Array.isArray(expenses.content)).toBe(true)
      expect(expenses.content.length).toBeGreaterThan(0)
      expect(expenses.content[0].title).toBe('Office Supplies Purchase')
    })

    it('should fetch expenses with filters', async () => {
      const params = {
        status: 'APPROVED',
        startDate: '2024-01-01',
        endDate: '2024-01-31'
      }

      const expenses = await expenseService.getExpenses('shop1', params)

      expect(expenses).toBeDefined()
      expect(Array.isArray(expenses.content)).toBe(true)
    })

    it('should handle fetch error', async () => {
      server.use(
        http.get('*/shops/:shopId/expenses', () => {
          return HttpResponse.json(
            { message: 'Unauthorized' },
            { status: 403 }
          )
        })
      )

      await expect(
        expenseService.getExpenses('shop1')
      ).rejects.toThrow()
    })
  })

  describe('getExpenseById', () => {
    it('should fetch single expense successfully', async () => {
      const expense = await expenseService.getExpenseById('exp1')

      expect(expense).toBeDefined()
      expect(expense.id).toBe('exp1')
      expect(expense.title).toBe('Office Supplies Purchase')
    })

    it('should handle 404 error', async () => {
      server.use(
        http.get('*/expenses/:expenseId', () => {
          return HttpResponse.json(
            { message: 'Expense not found' },
            { status: 404 }
          )
        })
      )

      await expect(
        expenseService.getExpenseById('invalid')
      ).rejects.toThrow()
    })
  })

  describe('createExpense', () => {
    it('should create expense successfully', async () => {
      const newExpense = await expenseService.createExpense('shop1', {
        title: 'New Expense',
        description: 'Test expense',
        categoryId: 'cat1',
        amount: 500,
        date: new Date().toISOString()
      })

      expect(newExpense).toBeDefined()
      expect(newExpense.id).toBeDefined()
    })

    it('should create expense with file upload', async () => {
      const file = new File(['receipt'], 'receipt.pdf', { type: 'application/pdf' })

      const newExpense = await expenseService.createExpense('shop1', {
        title: 'Expense with Receipt',
        categoryId: 'cat1',
        amount: 300,
        date: new Date().toISOString(),
        receiptFile: file
      })

      expect(newExpense).toBeDefined()
    })

    it('should handle validation error', async () => {
      server.use(
        http.post('*/shops/:shopId/expenses', () => {
          return HttpResponse.json(
            { message: 'Amount exceeds limit' },
            { status: 400 }
          )
        })
      )

      await expect(
        expenseService.createExpense('shop1', {
          title: 'Invalid',
          categoryId: 'cat1',
          amount: 999999,
          date: new Date().toISOString()
        })
      ).rejects.toThrow()
    })
  })

  describe('updateExpense', () => {
    it('should update expense successfully', async () => {
      const updated = await expenseService.updateExpense('exp1', {
        title: 'Updated Expense'
      })

      expect(updated).toBeDefined()
      expect(updated.id).toBe('exp1')
      expect(updated.title).toBe('Updated Expense')
    })

    it('should update expense status', async () => {
      const updated = await expenseService.updateExpense('exp1', {
        status: 'APPROVED'
      })

      expect(updated).toBeDefined()
    })

    it('should handle update error', async () => {
      server.use(
        http.patch('*/expenses/:expenseId', () => {
          return HttpResponse.json(
            { message: 'Cannot update paid expense' },
            { status: 400 }
          )
        })
      )

      await expect(
        expenseService.updateExpense('exp1', { title: 'Test' })
      ).rejects.toThrow()
    })
  })

  describe('deleteExpense', () => {
    it('should delete expense successfully', async () => {
      await expect(
        expenseService.deleteExpense('exp1')
      ).resolves.not.toThrow()
    })

    it('should handle delete error', async () => {
      server.use(
        http.delete('*/expenses/:expenseId', () => {
          return HttpResponse.json(
            { message: 'Cannot delete approved expense' },
            { status: 400 }
          )
        })
      )

      await expect(
        expenseService.deleteExpense('exp1')
      ).rejects.toThrow()
    })
  })

  describe('approveExpense', () => {
    it('should approve expense successfully', async () => {
      const approved = await expenseService.approveExpense('exp1', 'Looks good')

      expect(approved).toBeDefined()
      expect(approved.id).toBe('exp1')
      expect(approved.status).toBe('APPROVED')
    })

    it('should approve without notes', async () => {
      const approved = await expenseService.approveExpense('exp1')

      expect(approved).toBeDefined()
      expect(approved.status).toBe('APPROVED')
    })

    it('should handle approval error', async () => {
      server.use(
        http.post('*/expenses/:expenseId/approve', () => {
          return HttpResponse.json(
            { message: 'Already approved' },
            { status: 400 }
          )
        })
      )

      await expect(
        expenseService.approveExpense('exp1')
      ).rejects.toThrow()
    })
  })

  describe('rejectExpense', () => {
    it('should reject expense successfully', async () => {
      const rejected = await expenseService.rejectExpense('exp1', 'Not justified')

      expect(rejected).toBeDefined()
      expect(rejected.id).toBe('exp1')
      expect(rejected.status).toBe('REJECTED')
    })

    it('should reject without notes', async () => {
      const rejected = await expenseService.rejectExpense('exp1')

      expect(rejected).toBeDefined()
      expect(rejected.status).toBe('REJECTED')
    })

    it('should handle rejection error', async () => {
      server.use(
        http.post('*/expenses/:expenseId/reject', () => {
          return HttpResponse.json(
            { message: 'Already processed' },
            { status: 400 }
          )
        })
      )

      await expect(
        expenseService.rejectExpense('exp1')
      ).rejects.toThrow()
    })
  })

  describe('uploadReceipt', () => {
    it('should upload receipt successfully', async () => {
      const file = new File(['receipt'], 'receipt.pdf', { type: 'application/pdf' })
      
      const result = await expenseService.uploadReceipt('exp1', file)

      expect(result).toBeDefined()
      expect(result.receiptUrl).toBe('https://example.com/receipt.pdf')
    })

    it('should handle upload error', async () => {
      server.use(
        http.post('*/expenses/:expenseId/receipt', () => {
          return HttpResponse.json(
            { message: 'File too large' },
            { status: 413 }
          )
        })
      )

      const file = new File(['large'], 'large.pdf', { type: 'application/pdf' })

      await expect(
        expenseService.uploadReceipt('exp1', file)
      ).rejects.toThrow()
    })
  })
})
