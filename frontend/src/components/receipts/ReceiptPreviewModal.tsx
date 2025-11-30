import React, { useEffect, useState } from 'react'
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
  DialogFooter,
} from '@/components/ui/dialog'
import { Button } from '@/components/ui/button'
import { Download, Printer, X } from 'lucide-react'
import { LoadingSpinner } from '@/components/ui/loading-spinner'

interface ReceiptPreviewModalProps {
  open: boolean
  onClose: () => void
  pdfBlobUrl: string | null
  receiptNumber?: string
  onPrint?: () => void
  onDownload?: () => void
}

export const ReceiptPreviewModal: React.FC<ReceiptPreviewModalProps> = ({
  open,
  onClose,
  pdfBlobUrl,
  receiptNumber,
  onPrint,
  onDownload,
}) => {
  const [isLoading, setIsLoading] = useState(true)

  useEffect(() => {
    if (open && pdfBlobUrl) {
      setIsLoading(true)
    }
  }, [open, pdfBlobUrl])

  const handleIframeLoad = () => {
    setIsLoading(false)
  }

  const handlePrint = () => {
    if (onPrint) {
      onPrint()
    } else if (pdfBlobUrl) {
      // Fallback: open in new window and print
      const printWindow = window.open(pdfBlobUrl, '_blank')
      if (printWindow) {
        // printWindow.onload = () => {
        //   printWindow.print()
        // }
      }
    }
  }

  return (
    <Dialog open={open} onOpenChange={onClose}>
      <DialogContent className="max-w-4xl h-[90vh] flex flex-col p-0">
        <DialogHeader className="px-6 py-4 border-b">
          <div className="flex items-center justify-between">
            <DialogTitle>
              Receipt Preview {receiptNumber && `- ${receiptNumber}`}
            </DialogTitle>
            <Button
              variant="ghost"
              size="sm"
              onClick={onClose}
              className="h-8 w-8 p-0"
            >
              <X className="h-4 w-4" />
            </Button>
          </div>
        </DialogHeader>

        <div className="flex-1 relative bg-gray-100">
          {isLoading && (
            <div className="absolute inset-0 flex items-center justify-center bg-white/80 z-10">
              <LoadingSpinner />
              <span className="ml-2 text-sm text-gray-600">Loading receipt...</span>
            </div>
          )}
          
          {pdfBlobUrl ? (
            <iframe
              src={pdfBlobUrl}
              className="w-full h-full border-0"
              title="Receipt Preview"
              onLoad={handleIframeLoad}
            />
          ) : (
            <div className="flex items-center justify-center h-full">
              <p className="text-gray-500">No receipt to display</p>
            </div>
          )}
        </div>

        <DialogFooter className="px-6 py-4 border-t bg-white">
          <div className="flex items-center justify-between w-full">
            <Button variant="outline" onClick={onClose}>
              Close
            </Button>
            <div className="flex gap-2">
              {onDownload && (
                <Button variant="outline" onClick={onDownload}>
                  <Download className="h-4 w-4 mr-2" />
                  Download
                </Button>
              )}
              <Button onClick={handlePrint}>
                <Printer className="h-4 w-4 mr-2" />
                Print
              </Button>
            </div>
          </div>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  )
}
