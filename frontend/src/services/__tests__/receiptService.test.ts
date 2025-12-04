/* eslint-disable @typescript-eslint/no-explicit-any */
import { receiptService } from '../receiptService'
import { getMockReceipts, getMockReceipt, getMockPrintableContent, getMockReceiptContent } from '@/testData/receipts'
import { http, HttpResponse } from 'msw'
import { setupServer } from 'msw/node'

const server = setupServer(
  // Get receipts with filters
  http.get('*/receipts', () => {
    return HttpResponse.json(getMockReceipts())
  }),

  // Get receipt by ID
  http.get('*/receipts/:receiptId', ({ params }) => {
    // Handle different patterns
    if (params.receiptId === 'printable' || params.receiptId === 'content') {
      return HttpResponse.json(getMockPrintableContent())
    }
    return HttpResponse.json(getMockReceipt({ id: params.receiptId as string }))
  }),

  // Get receipt by number
  http.get('*/receipts/by-number/:receiptNumber', ({ params }) => {
    return HttpResponse.json(getMockReceipt({ receiptNumber: params.receiptNumber as string }))
  }),

  // Get receipt by transaction
  http.get('*/receipts/transaction/:transactionId', ({ params }) => {
    return HttpResponse.json(getMockReceipt({ transactionId: params.transactionId as string }))
  }),

  // Get printable content
  http.get('*/receipts/:receiptId/printable', () => {
    return HttpResponse.json(getMockPrintableContent())
  }),

  // Get receipt content
  http.get('*/receipts/:receiptId/content', () => {
    return HttpResponse.json(getMockReceiptContent())
  }),

  // Generate receipt
  http.post('*/receipts/generate/:transactionId', () => {
    return HttpResponse.json(getMockReceipt({ id: 'new-receipt' }))
  }),

  // Regenerate receipt
  http.post('*/receipts/regenerate/:transactionId', () => {
    return new HttpResponse(null, { status: 200 })
  }),

  // Mark as printed
  http.post('*/receipts/:receiptId/mark-printed', ({ params }) => {
    return HttpResponse.json(getMockReceipt({ 
      id: params.receiptId as string,
      printed: true,
      printedAt: new Date().toISOString()
    }))
  }),

  // Mark as emailed
  http.post('*/receipts/:receiptId/mark-emailed', ({ params }) => {
    return HttpResponse.json(getMockReceipt({ 
      id: params.receiptId as string,
      emailed: true,
      emailedAt: new Date().toISOString()
    }))
  }),

  // Download/Print receipt (blob)
  http.get('*/sales/:transactionId/receipt', async () => {
    const blob = new Blob(['PDF content'], { type: 'application/pdf' })
    return HttpResponse.arrayBuffer(await blob.arrayBuffer())
  })
)

describe('receiptService with MSW', () => {
  beforeAll(() => {
    server.listen({ onUnhandledRequest: 'warn' })
    
    globalThis.URL.createObjectURL = jest.fn(() => 'blob:mock-url')
    globalThis.URL.revokeObjectURL = jest.fn()
    document.createElement = jest.fn((tag) => {
      if (tag === 'a') {
        return {
          href: '',
          download: '',
          click: jest.fn(),
          remove: jest.fn()
        } as unknown
      }
      return {} as any
    })
    document.body.appendChild = jest.fn()
    document.body.removeChild = jest.fn()
    globalThis.window.open = jest.fn(() => ({
      onload: null,
      print: jest.fn()
    }) as any)
  })

  afterEach(() => {
    server.resetHandlers()
    jest.clearAllMocks()
  })

  afterAll(() => {
    server.close()
  })

  describe('getReceipts', () => {
    it('should fetch receipts successfully', async () => {
      const receipts = await receiptService.getReceipts()

      expect(receipts).toBeDefined()
      expect(receipts.content).toHaveLength(3)
      expect(receipts.content[0].receiptNumber).toBe('RCP-2024-001')
      expect(receipts.totalElements).toBe(3)
    })

    it('should fetch receipts with filters', async () => {
      const filter = {
        shopId: 'shop1',
        status: 'GENERATED',
        startDate: '2024-01-01',
        endDate: '2024-01-31',
        page: 0,
        size: 20
      }

      const receipts = await receiptService.getReceipts(filter)

      expect(receipts).toBeDefined()
      expect(Array.isArray(receipts.content)).toBe(true)
    })

    it('should handle fetch error', async () => {
      server.use(
        http.get('*/receipts', () => {
          return HttpResponse.json(
            { message: 'Internal error' },
            { status: 500 }
          )
        })
      )

      await expect(receiptService.getReceipts()).rejects.toThrow()
    })
  })

  describe('getReceiptById', () => {
    it('should fetch receipt by ID successfully', async () => {
      const receipt = await receiptService.getReceiptById('receipt1')

      expect(receipt).toBeDefined()
      expect(receipt.id).toBe('receipt1')
      expect(receipt.receiptNumber).toBe('RCP-2024-001')
    })

    it('should handle 404 error', async () => {
      server.use(
        http.get('*/receipts/:receiptId', () => {
          return HttpResponse.json(
            { message: 'Receipt not found' },
            { status: 404 }
          )
        })
      )

      await expect(receiptService.getReceiptById('invalid')).rejects.toThrow()
    })
  })

  describe('getReceiptByNumber', () => {
    it('should fetch receipt by number successfully', async () => {
      const receipt = await receiptService.getReceiptByNumber('RCP-2024-001')

      expect(receipt).toBeDefined()
      expect(receipt.receiptNumber).toBe('RCP-2024-001')
    })
  })

  describe('getReceiptByTransaction', () => {
    it('should fetch receipt by transaction ID successfully', async () => {
      const receipt = await receiptService.getReceiptByTransaction('txn1')

      expect(receipt).toBeDefined()
      expect(receipt.transactionId).toBe('txn1')
    })
  })

  describe('getPrintableContent', () => {
    it('should fetch printable content successfully', async () => {
      const content = await receiptService.getPrintableContent('receipt1')

      expect(content).toBeDefined()
    })
  })

  describe('getReceiptContent', () => {
    it('should fetch receipt content successfully', async () => {
      const content = await receiptService.getReceiptContent('receipt1')

      expect(content).toBeDefined()
    })
  })

  describe('generateReceipt', () => {
    it('should generate receipt successfully', async () => {
      const receipt = await receiptService.generateReceipt('txn1')

      expect(receipt).toBeDefined()
      expect(receipt.id).toBe('new-receipt')
    })

    it('should handle generation error', async () => {
      server.use(
        http.post('*/receipts/generate/:transactionId', () => {
          return HttpResponse.json(
            { message: 'Transaction not found' },
            { status: 404 }
          )
        })
      )

      await expect(receiptService.generateReceipt('invalid')).rejects.toThrow()
    })
  })

  describe('regenerateReceipt', () => {
    it('should regenerate receipt successfully', async () => {
      await expect(
        receiptService.regenerateReceipt('txn1')
      ).resolves.not.toThrow()
    })

    it('should handle regeneration error', async () => {
      server.use(
        http.post('*/receipts/regenerate/:transactionId', () => {
          return HttpResponse.json(
            { message: 'Cannot regenerate' },
            { status: 400 }
          )
        })
      )

      await expect(receiptService.regenerateReceipt('txn1')).rejects.toThrow()
    })
  })

  describe('markAsPrinted', () => {
    it('should mark receipt as printed successfully', async () => {
      const receipt = await receiptService.markAsPrinted('receipt1', 'user1')

      expect(receipt).toBeDefined()
      expect(receipt.printed).toBe(true)
      expect(receipt.printedAt).toBeDefined()
    })
  })

  describe('markAsEmailed', () => {
    it('should mark receipt as emailed successfully', async () => {
      const receipt = await receiptService.markAsEmailed('receipt1', 'customer@example.com')

      expect(receipt).toBeDefined()
      expect(receipt.emailed).toBe(true)
      expect(receipt.emailedAt).toBeDefined()
    })
  })

  describe('downloadReceiptPDF', () => {
    it.skip('should download receipt PDF successfully', async () => {
      await receiptService.downloadReceiptPDF('txn1', 'RCP-2024-001')

      expect(global.URL.createObjectURL).toHaveBeenCalled()
      expect(document.body.appendChild).toHaveBeenCalled()
    })
  })

  describe('printReceipt', () => {
    it.skip('should print receipt successfully', async () => {
      await receiptService.printReceipt('txn1')

      expect(global.window.open).toHaveBeenCalled()
    })
  })
})
