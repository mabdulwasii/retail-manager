import { api } from './api'

export interface Receipt {
  id: string
  receiptNumber: string
  transactionId: string
  transactionNumber: string
  shopId: string
  shopName?: string
  generatedAt: string
  printedAt?: string
  emailedAt?: string
  printedBy?: string
  emailedTo?: string
  receiptData: string
  status: 'GENERATED' | 'PRINTED' | 'EMAILED' | 'REGENERATED'
}

export interface ReceiptFilter {
  shopId?: string
  status?: string
  startDate?: string
  endDate?: string
  page?: number
  size?: number
  sort?: string
}

export interface PagedReceiptsResponse {
  content: Receipt[]
  totalElements: number
  totalPages: number
  size: number
  number: number
}

export const receiptService = {
  // Fetch all receipts with filters
  async getReceipts(filter?: ReceiptFilter): Promise<PagedReceiptsResponse> {
    const queryParams = new URLSearchParams()
    if (filter?.shopId) queryParams.append('shopId', filter.shopId)
    if (filter?.status) queryParams.append('status', filter.status)
    if (filter?.startDate) queryParams.append('startDate', filter.startDate)
    if (filter?.endDate) queryParams.append('endDate', filter.endDate)
    if (filter?.page !== undefined) queryParams.append('page', filter.page.toString())
    if (filter?.size !== undefined) queryParams.append('size', filter.size.toString())
    if (filter?.sort) queryParams.append('sort', filter.sort)

    return api.get<PagedReceiptsResponse>(`/receipts?${queryParams}`)
  },

  // Get receipt by ID
  async getReceiptById(receiptId: string): Promise<Receipt> {
    return api.get<Receipt>(`/receipts/${receiptId}`)
  },

  // Get receipt by transaction number
  async getReceiptByNumber(receiptNumber: string): Promise<Receipt> {
    return api.get<Receipt>(`/receipts/by-number/${receiptNumber}`)
  },

  // Get receipt by transaction ID
  async getReceiptByTransaction(transactionId: string): Promise<Receipt> {
    return api.get<Receipt>(`/receipts/transaction/${transactionId}`)
  },

  // Get printable receipt content
  async getPrintableContent(receiptId: string): Promise<string> {
    return api.get<string>(`/receipts/${receiptId}/printable`)
  },

  // Get receipt content
  async getReceiptContent(receiptId: string): Promise<string> {
    return api.get<string>(`/receipts/${receiptId}/content`)
  },

  // Generate receipt for transaction
  async generateReceipt(transactionId: string): Promise<Receipt> {
    return api.post<Receipt>(`/receipts/generate/${transactionId}`)
  },

  // Regenerate receipt for transaction
  async regenerateReceipt(transactionId: string): Promise<void> {
    return api.post(`/receipts/regenerate/${transactionId}`)
  },

  // Mark receipt as printed
  async markAsPrinted(receiptId: string, printedBy: string): Promise<Receipt> {
    return api.post<Receipt>(
      `/receipts/${receiptId}/mark-printed?printedBy=${encodeURIComponent(printedBy)}`
    )
  },

  // Mark receipt as emailed
  async markAsEmailed(receiptId: string, emailAddress: string): Promise<Receipt> {
    return api.post<Receipt>(
      `/receipts/${receiptId}/mark-emailed?emailAddress=${encodeURIComponent(emailAddress)}`
    )
  },

  // Download receipt as PDF
  async downloadReceiptPDF(transactionId: string, receiptNumber: string): Promise<void> {
    const blob = await api.getBlob(`/sales/${transactionId}/receipt`)
    const url = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = `receipt-${receiptNumber}.pdf`
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
    window.URL.revokeObjectURL(url)
  },

  // Print receipt
  async printReceipt(transactionId: string): Promise<void> {
    const blob = await api.getBlob(`/sales/${transactionId}/receipt`)
    const url = window.URL.createObjectURL(blob)
    
    const printWindow = window.open(url, '_blank')
    if (printWindow) {
      printWindow.onload = () => {
        printWindow.print()
        window.URL.revokeObjectURL(url)
      }
    }
  },
}
