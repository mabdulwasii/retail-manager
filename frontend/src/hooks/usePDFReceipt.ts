import { pdf } from '@react-pdf/renderer'
import { PDFReceipt } from '@/components/receipts/PDFReceipt'
import { SalesTransaction } from '@/hooks/useSales'
import { api } from '@/services/api'
import { toast } from 'sonner'

interface ReceiptOptions {
  shopAddress?: string
  shopPhone?: string
  shopEmail?: string
}

export const usePDFReceipt = () => {
  /**
   * Fetch transaction details by ID
   */
  const fetchTransaction = async (transactionId: string): Promise<SalesTransaction> => {
    try {
      const transaction = await api.get<SalesTransaction>(`/sales/${transactionId}`)
      return transaction
    } catch (error) {
      console.error('Error fetching transaction:', error)
      toast.error('Failed to fetch transaction details')
      throw error
    }
  }
  /**
   * Generate and download PDF receipt
   */
  const downloadReceipt = async (
    transaction: SalesTransaction,
    options?: ReceiptOptions
  ) => {
    try {
      const blob = await pdf(
        PDFReceipt({
         transaction,
          shopAddress: options?.shopAddress || '',
          shopPhone: options?.shopPhone || '',
          shopEmail: options?.shopEmail || '',
        })
      ).toBlob()

      const url = window.URL.createObjectURL(blob)
      const link = document.createElement('a')
      link.href = url
      link.download = `receipt-${transaction.receiptNumber || transaction.transactionNumber}.pdf`
      document.body.appendChild(link)
      link.click()
      document.body.removeChild(link)
      window.URL.revokeObjectURL(url)

      toast.success('Receipt downloaded successfully')
    } catch (error) {
      console.error('Error generating receipt:', error)
      toast.error('Failed to generate receipt')
      throw error
    }
  }

  /**
   * Generate and print PDF receipt
   */
  const printReceipt = async (
    transaction: SalesTransaction,
    options?: ReceiptOptions
  ) => {
    try {
      const blob = await pdf(
        PDFReceipt({
          transaction,
          shopAddress: options?.shopAddress || '',
          shopPhone: options?.shopPhone || '',
          shopEmail: options?.shopEmail || '',
        })
      ).toBlob()

      const url = window.URL.createObjectURL(blob)
      const printWindow = window.open(url, '_blank')
      
      if (printWindow) {
        // printWindow.onload = () => {
        //   printWindow.print()
        //   window.URL.revokeObjectURL(url)
        // }
        toast.success('Receipt opened for printing')
      } else {
        toast.error('Please allow pop-ups to print receipts')
      }
    } catch (error) {
      console.error('Error printing receipt:', error)
      toast.error('Failed to print receipt')
      throw error
    }
  }

  /**
   * Generate PDF blob (useful for sending via email or other purposes)
   */
  const generateReceiptBlob = async (
    transaction: SalesTransaction,
    options?: ReceiptOptions
  ): Promise<Blob> => {
    try {
      const blob = await pdf(
        PDFReceipt({
          transaction,
          shopAddress: options?.shopAddress || '',
          shopPhone: options?.shopPhone || '',
          shopEmail: options?.shopEmail || '',
        })
      ).toBlob()

      return blob
    } catch (error) {
      console.error('Error generating receipt blob:', error)
      throw error
    }
  }

  /**
   * Preview receipt in new tab
   */
  const previewReceipt = async (
    transaction: SalesTransaction,
    options?: ReceiptOptions
  ) => {
    try {
      const blob = await pdf(
        PDFReceipt({
          transaction,
          shopAddress: options?.shopAddress || '',
          shopPhone: options?.shopPhone || '',
          shopEmail: options?.shopEmail || '',
        })
      ).toBlob()

      const url = window.URL.createObjectURL(blob)
      window.open(url, '_blank')
      
      // Clean up the URL after a delay to allow the browser to load it
      setTimeout(() => {
        window.URL.revokeObjectURL(url)
      }, 1000)

      toast.success('Receipt preview opened')
    } catch (error) {
      console.error('Error previewing receipt:', error)
      toast.error('Failed to preview receipt')
      throw error
    }
  }

  /**
   * Download receipt by transaction ID (fetches transaction first)
   */
  const downloadReceiptByTransactionId = async (
    transactionId: string,
    options?: ReceiptOptions
  ) => {
    try {
      const transaction = await fetchTransaction(transactionId)
      await downloadReceipt(transaction, options)
    } catch (error) {
      // Error already handled in fetchTransaction
      throw error
    }
  }

  /**
   * Print receipt by transaction ID (fetches transaction first)
   */
  const printReceiptByTransactionId = async (
    transactionId: string,
    options?: ReceiptOptions
  ) => {
    try {
      const transaction = await fetchTransaction(transactionId)
      await printReceipt(transaction, options)
    } catch (error) {
      // Error already handled in fetchTransaction
      throw error
    }
  }

  /**
   * Preview receipt by transaction ID (fetches transaction first)
   */
  const previewReceiptByTransactionId = async (
    transactionId: string,
    options?: ReceiptOptions
  ) => {
    try {
      const transaction = await fetchTransaction(transactionId)
      await previewReceipt(transaction, options)
    } catch (error) {
      // Error already handled in fetchTransaction
      throw error
    }
  }

  return {
    downloadReceipt,
    printReceipt,
    generateReceiptBlob,
    previewReceipt,
    downloadReceiptByTransactionId,
    printReceiptByTransactionId,
    previewReceiptByTransactionId,
    fetchTransaction,
  }
}
