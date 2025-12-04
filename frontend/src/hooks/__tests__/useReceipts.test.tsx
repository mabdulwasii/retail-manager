import React from 'react'
import { renderHook, waitFor } from '@testing-library/react'
import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import {
  useReceipts,
  useReceiptById,
  useReceiptByNumber,
  useReceiptByTransaction,
  usePrintableContent,
  useReceiptContent,
  useGenerateReceipt,
  useRegenerateReceipt,
  useMarkAsPrinted,
  useMarkAsEmailed,
  useDownloadReceipt,
  usePrintReceipt
} from '../useReceipts'
import { getMockReceipt, getMockReceipts, getMockPrintableContent, getMockReceiptContent } from '@/testData/receipts'
import { receiptService } from '@/services/receiptService'
import { toast } from 'sonner'


jest.mock('@/services/receiptService', () => ({
  receiptService: {
    getReceipts: jest.fn(),
    getReceiptById: jest.fn(),
    getReceiptByNumber: jest.fn(),
    getReceiptByTransaction: jest.fn(),
    getPrintableContent: jest.fn(),
    getReceiptContent: jest.fn(),
    generateReceipt: jest.fn(),
    regenerateReceipt: jest.fn(),
    markAsPrinted: jest.fn(),
    markAsEmailed: jest.fn(),
    downloadReceiptPDF: jest.fn(),
    printReceipt: jest.fn()
  }
}))

jest.mock('sonner', () => ({
  toast: {
    success: jest.fn(),
    error: jest.fn()
  }
}))

jest.mock('@/context/ManualAuthContext', () => ({
  useAuth: () => ({
    user: {
      id: 'user1',
      username: 'cashier',
      email: 'cashier@example.com',
      roles: ['ROLE_CASHIER'],
      shopId: 'shop1'
    },
    isAuthenticated: true
  })
}))

const mockReceiptService = receiptService as jest.Mocked<typeof receiptService>
const mockToast = toast as jest.Mocked<typeof toast>

describe('useReceipts', () => {
  let queryClient: QueryClient
  let wrapper: React.FC<{ children: React.ReactNode }>

  beforeEach(() => {
    queryClient = new QueryClient({
      defaultOptions: {
        queries: { retry: false },
        mutations: { retry: false }
      }
    })
    wrapper = ({ children }) => React.createElement(
      QueryClientProvider,
      { client: queryClient },
      children
    )
    jest.clearAllMocks()
  })

  afterEach(() => {
    queryClient.clear()
  })

  describe('useReceipts', () => {
    it('should fetch paginated receipts successfully', async () => {
      // Using test data from @/testData/receipts
      const mockData = getMockReceipts()
      mockReceiptService.getReceipts.mockResolvedValueOnce(mockData)

      const { result } = renderHook(() => useReceipts(), { wrapper })

      await waitFor(() => {
        expect(result.current.isSuccess).toBe(true)
      })

      expect(result.current.data?.content).toHaveLength(3)
      expect(result.current.data?.content[0].receiptNumber).toBe('RCP-2024-001')
    })

    it('should fetch receipts with filters', async () => {
      const mockData = getMockReceipts()
      mockReceiptService.getReceipts.mockResolvedValueOnce(mockData)

      const filter = {
        startDate: '2024-01-01',
        endDate: '2024-01-31',
        status: 'GENERATED' as const
      }

      const { result } = renderHook(() => useReceipts(filter), { wrapper })

      await waitFor(() => {
        expect(result.current.isSuccess).toBe(true)
      })

      expect(mockReceiptService.getReceipts).toHaveBeenCalledWith(filter)
    })

    it('should handle fetch error', async () => {
      mockReceiptService.getReceipts.mockRejectedValueOnce(new Error('Failed to fetch'))

      const { result } = renderHook(() => useReceipts(), { wrapper })

      await waitFor(() => {
        expect(result.current.isError).toBe(true)
      }, { timeout: 3000 })
    })
  })

  describe('useReceiptById', () => {
    it('should fetch single receipt successfully', async () => {
      // Using test data factory
      const mockReceipt = getMockReceipt()
      mockReceiptService.getReceiptById.mockResolvedValueOnce(mockReceipt)

      const { result } = renderHook(() => useReceiptById('receipt1'), { wrapper })

      await waitFor(() => {
        expect(result.current.isSuccess).toBe(true)
      })

      expect(result.current.data?.id).toBe('receipt1')
      expect(result.current.data?.receiptNumber).toBe('RCP-2024-001')
    })

    it('should not fetch if receiptId is undefined', () => {
      const { result } = renderHook(() => useReceiptById(undefined), { wrapper })

      expect(result.current.isLoading).toBe(false)
      expect(mockReceiptService.getReceiptById).not.toHaveBeenCalled()
    })
  })

  describe('useReceiptByNumber', () => {
    it('should fetch receipt by number', async () => {
      const mockReceipt = getMockReceipt()
      mockReceiptService.getReceiptByNumber.mockResolvedValueOnce(mockReceipt)

      const { result } = renderHook(() => useReceiptByNumber('RCP-2024-001'), { wrapper })

      await waitFor(() => {
        expect(result.current.isSuccess).toBe(true)
      })

      expect(result.current.data?.receiptNumber).toBe('RCP-2024-001')
      expect(mockReceiptService.getReceiptByNumber).toHaveBeenCalledWith('RCP-2024-001')
    })
  })

  describe('useReceiptByTransaction', () => {
    it('should fetch receipt by transaction ID', async () => {
      const mockReceipt = getMockReceipt()
      mockReceiptService.getReceiptByTransaction.mockResolvedValueOnce(mockReceipt)

      const { result } = renderHook(() => useReceiptByTransaction('txn1'), { wrapper })

      await waitFor(() => {
        expect(result.current.isSuccess).toBe(true)
      })

      expect(result.current.data?.transactionId).toBe('txn1')
      expect(mockReceiptService.getReceiptByTransaction).toHaveBeenCalledWith('txn1')
    })
  })

  describe('usePrintableContent', () => {
    it('should fetch printable content', async () => {
      const mockContent = getMockPrintableContent()
      mockReceiptService.getPrintableContent.mockResolvedValueOnce(mockContent)

      const { result } = renderHook(() => usePrintableContent('receipt1'), { wrapper })

      await waitFor(() => {
        expect(result.current.isSuccess).toBe(true)
      })

      expect(result.current.data?.html).toContain('Receipt')
      expect(mockReceiptService.getPrintableContent).toHaveBeenCalledWith('receipt1')
    })
  })

  describe('useReceiptContent', () => {
    it('should fetch receipt content', async () => {
      const mockContent = getMockReceiptContent()
      mockReceiptService.getReceiptContent.mockResolvedValueOnce(mockContent)

      const { result } = renderHook(() => useReceiptContent('receipt1'), { wrapper })

      await waitFor(() => {
        expect(result.current.isSuccess).toBe(true)
      })

      expect(result.current.data?.receiptNumber).toBe('RCP-2024-001')
      expect(mockReceiptService.getReceiptContent).toHaveBeenCalledWith('receipt1')
    })
  })

  describe('useGenerateReceipt', () => {
    it('should generate receipt successfully', async () => {
      // Using test data factory
      const newReceipt = getMockReceipt({ id: 'receipt-new', receiptNumber: 'RCP-2024-004' })
      mockReceiptService.generateReceipt.mockResolvedValueOnce(newReceipt)

      const { result } = renderHook(() => useGenerateReceipt(), { wrapper })

      await result.current.mutateAsync('txn4')

      await waitFor(() => {
        expect(mockToast.success).toHaveBeenCalledWith(
          'Receipt generated successfully',
          { description: 'Receipt RCP-2024-004 has been generated.' }
        )
      })

      expect(mockReceiptService.generateReceipt).toHaveBeenCalledWith('txn4')
    })

    it('should handle generate error', async () => {
      const error = { response: { data: { message: 'Transaction not found' } } }
      mockReceiptService.generateReceipt.mockRejectedValueOnce(error)

      const { result } = renderHook(() => useGenerateReceipt(), { wrapper })

      try {
        await result.current.mutateAsync('invalid')
      } catch (e) {
        // Expected
      }

      await waitFor(() => {
        expect(mockToast.error).toHaveBeenCalledWith(
          'Failed to generate receipt',
          { description: 'Transaction not found' }
        )
      })
    })
  })

  describe('useRegenerateReceipt', () => {
    it('should regenerate receipt successfully', async () => {
      mockReceiptService.regenerateReceipt.mockResolvedValueOnce(undefined)

      const { result } = renderHook(() => useRegenerateReceipt(), { wrapper })

      await result.current.mutateAsync('txn1')

      await waitFor(() => {
        expect(mockToast.success).toHaveBeenCalledWith('Receipt regenerated successfully')
      })

      expect(mockReceiptService.regenerateReceipt).toHaveBeenCalledWith('txn1')
    })

    it('should handle regenerate error', async () => {
      const error = { message: 'Receipt already exists' }
      mockReceiptService.regenerateReceipt.mockRejectedValueOnce(error)

      const { result } = renderHook(() => useRegenerateReceipt(), { wrapper })

      try {
        await result.current.mutateAsync('txn1')
      } catch (e) {
        // Expected
      }

      await waitFor(() => {
        expect(mockToast.error).toHaveBeenCalled()
      })
    })
  })

  describe('useMarkAsPrinted', () => {
    it('should mark receipt as printed successfully', async () => {
      // Using test data with printed status
      const printedReceipt = getMockReceipt({
        printed: true,
        printedAt: new Date().toISOString(),
        printedBy: 'user1'
      })
      mockReceiptService.markAsPrinted.mockResolvedValueOnce(printedReceipt)

      const { result } = renderHook(() => useMarkAsPrinted(), { wrapper })

      await result.current.mutateAsync({
        receiptId: 'receipt1',
        printedBy: 'user1'
      })

      await waitFor(() => {
        expect(mockToast.success).toHaveBeenCalledWith('Receipt marked as printed')
      })

      expect(mockReceiptService.markAsPrinted).toHaveBeenCalledWith('receipt1', 'user1')
    })

    it('should handle mark as printed error', async () => {
      const error = { message: 'Receipt not found' }
      mockReceiptService.markAsPrinted.mockRejectedValueOnce(error)

      const { result } = renderHook(() => useMarkAsPrinted(), { wrapper })

      try {
        await result.current.mutateAsync({
          receiptId: 'invalid',
          printedBy: 'user1'
        })
      } catch (e) {
        // Expected
      }

      await waitFor(() => {
        expect(mockToast.error).toHaveBeenCalled()
      })
    })
  })

  describe('useMarkAsEmailed', () => {
    it('should mark receipt as emailed successfully', async () => {
      // Using test data with emailed status
      const emailedReceipt = getMockReceipt({
        emailed: true,
        emailedAt: new Date().toISOString(),
        emailAddress: 'customer@example.com'
      })
      mockReceiptService.markAsEmailed.mockResolvedValueOnce(emailedReceipt)

      const { result } = renderHook(() => useMarkAsEmailed(), { wrapper })

      await result.current.mutateAsync({
        receiptId: 'receipt1',
        emailAddress: 'customer@example.com'
      })

      await waitFor(() => {
        expect(mockToast.success).toHaveBeenCalledWith('Receipt sent to customer@example.com')
      })

      expect(mockReceiptService.markAsEmailed).toHaveBeenCalledWith('receipt1', 'customer@example.com')
    })
  })

  describe('useDownloadReceipt', () => {
    it('should download receipt successfully', async () => {
      mockReceiptService.downloadReceiptPDF.mockResolvedValueOnce(undefined)

      const { result } = renderHook(() => useDownloadReceipt(), { wrapper })

      await result.current.mutateAsync({
        transactionId: 'txn1',
        receiptNumber: 'RCP-2024-001'
      })

      await waitFor(() => {
        expect(mockToast.success).toHaveBeenCalledWith('Receipt downloaded')
      })

      expect(mockReceiptService.downloadReceiptPDF).toHaveBeenCalledWith('txn1', 'RCP-2024-001')
    })

    it('should handle download error', async () => {
      const error = { message: 'Download failed' }
      mockReceiptService.downloadReceiptPDF.mockRejectedValueOnce(error)

      const { result } = renderHook(() => useDownloadReceipt(), { wrapper })

      try {
        await result.current.mutateAsync({
          transactionId: 'txn1',
          receiptNumber: 'RCP-2024-001'
        })
      } catch (e) {
        // Expected
      }

      await waitFor(() => {
        expect(mockToast.error).toHaveBeenCalledWith(
          'Failed to download receipt',
          { description: 'Download failed' }
        )
      })
    })
  })

  describe('usePrintReceipt', () => {
    it('should print receipt successfully', async () => {
      mockReceiptService.printReceipt.mockResolvedValueOnce(undefined)

      const { result } = renderHook(() => usePrintReceipt(), { wrapper })

      await result.current.mutateAsync('txn1')

      expect(mockReceiptService.printReceipt).toHaveBeenCalledWith('txn1')
    })

    it('should handle print error', async () => {
      const error = { message: 'Print failed' }
      mockReceiptService.printReceipt.mockRejectedValueOnce(error)

      const { result } = renderHook(() => usePrintReceipt(), { wrapper })

      try {
        await result.current.mutateAsync('txn1')
      } catch (e) {
        // Expected
      }

      await waitFor(() => {
        expect(mockToast.error).toHaveBeenCalledWith(
          'Failed to print receipt',
          { description: 'Print failed' }
        )
      })
    })
  })
})
