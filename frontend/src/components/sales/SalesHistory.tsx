import React from 'react'
import { useNavigate } from 'react-router-dom'
import { Button } from '@/components/ui/button'
import { Badge } from '@/components/ui/badge'
import { SalesTransaction } from '@/hooks/useSales'
import { useCurrency } from '@/hooks/useCurrency'
import { usePDFReceipt } from '@/hooks/usePDFReceipt'
import { useShopContext } from '@/context/ShopContext'
import {
  ReceiptIcon,
  PrinterIcon,
  EyeIcon,
} from 'lucide-react'

interface SalesHistoryProps {
  transactions: SalesTransaction[]
}

export const SalesHistory: React.FC<SalesHistoryProps> = ({ transactions }) => {
  const navigate = useNavigate()
  const { formatCurrency } = useCurrency()
  const { printReceiptByTransactionId } = usePDFReceipt()
  const { selectedShop } = useShopContext()

  const handleViewTransaction = (transactionId: string) => {
    navigate(`/sales/${transactionId}`)
  }

  const handlePrintReceipt = async (transactionId: string) => {
    try {
      await printReceiptByTransactionId(transactionId, {
        shopAddress: selectedShop?.address || '',
        shopPhone: selectedShop?.phoneNumber || '',
        shopEmail: selectedShop?.email || '',
      })
    } catch (error) {
      // Error already handled in hook
    }
  }

  const getStatusBadge = (status: string) => {
    switch (status) {
      case 'COMPLETED':
        return <Badge className="bg-green-100 text-green-800">Completed</Badge>
      case 'PENDING':
        return <Badge className="bg-yellow-100 text-yellow-800">Pending</Badge>
      case 'CANCELLED':
        return <Badge className="bg-red-100 text-red-800">Cancelled</Badge>
      case 'REFUNDED':
        return <Badge className="bg-gray-100 text-gray-800">Refunded</Badge>
      default:
        return <Badge variant="secondary">{status}</Badge>
    }
  }

  const getPaymentMethodBadge = (method: string) => {
    const variants: Record<string, string> = {
      cash: 'bg-green-100 text-green-800',
      card: 'bg-blue-100 text-blue-800',
      mobile: 'bg-purple-100 text-purple-800',
      bank_transfer: 'bg-indigo-100 text-indigo-800'
    }

    return (
      <Badge className={variants[method] || 'bg-gray-100 text-gray-800'}>
        {method.replace('_', ' ').replace(/\b\w/g, l => l.toUpperCase())}
      </Badge>
    )
  }

  const formatDate = (dateString: string) => {
    return new Date(dateString).toLocaleDateString('en-US', {
      year: 'numeric',
      month: 'short',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    })
  }

  return (
    <div className="space-y-4">
      {transactions.length === 0 ? (
        <div className="text-center py-8 text-gray-500">
          <ReceiptIcon className="h-12 w-12 mx-auto mb-2 text-gray-300" />
          <p>No sales found</p>
          <p className="text-sm">Try adjusting your filters</p>
        </div>
      ) : (
        <div className="space-y-4">
          {transactions.map((sale) => (
                <div key={sale.id} className="border rounded-lg p-4 hover:bg-gray-50">
                  <div className="flex items-center justify-between">
                    <div className="flex-1">
                      <div className="flex items-center space-x-4">
                        <div>
                          <h3 className="font-medium text-gray-900">
                            Receipt #{sale.receiptNumber || sale.transactionNumber}
                          </h3>
                          <p className="text-sm text-gray-600">
                            {formatDate(sale.transactionDate)}
                          </p>
                        </div>

                        <div className="flex space-x-2">
                          {getStatusBadge(sale.status)}
                          {getPaymentMethodBadge(sale.paymentMethod)}
                        </div>
                      </div>

                      <div className="mt-2 grid grid-cols-2 md:grid-cols-4 gap-4 text-sm">
                        <div>
                          <span className="text-gray-600">Customer:</span>
                          <p className="font-medium">
                            {sale.customerName || 'Walk-in Customer'}
                          </p>
                        </div>

                        <div>
                          <span className="text-gray-600">Items:</span>
                          <p className="font-medium">{sale.lineItems?.length || 0}</p>
                        </div>

                        <div>
                          <span className="text-gray-600">Total:</span>
                          <p className="font-medium text-green-600">
                            {formatCurrency(sale.totalAmount)}
                          </p>
                        </div>

                        <div>
                          <span className="text-gray-600">Cashier:</span>
                          <p className="font-medium">{sale.cashierName || sale.cashierId}</p>
                        </div>
                      </div>
                    </div>

                    <div className="flex space-x-2 ml-4">
                      <Button
                        variant="outline"
                        size="sm"
                        onClick={() => handleViewTransaction(sale.id)}
                        title="View Details"
                      >
                        <EyeIcon className="h-4 w-4" />
                      </Button>

                      <Button
                        variant="outline"
                        size="sm"
                        onClick={() => handlePrintReceipt(sale.id)}
                        title="Print Receipt"
                      >
                        <PrinterIcon className="h-4 w-4" />
                      </Button>
                    </div>
                  </div>
                </div>
          ))}
        </div>
      )}
    </div>
  )
}

export default SalesHistory