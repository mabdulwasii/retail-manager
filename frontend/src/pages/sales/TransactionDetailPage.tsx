import React, { useState, useEffect } from 'react'
import { useParams, useNavigate } from 'react-router-dom'
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card'
import { Button } from '@/components/ui/button'
import { Badge } from '@/components/ui/badge'
import { Alert, AlertDescription } from '@/components/ui/alert'
import { Separator } from '@/components/ui/separator'
import { Input } from '@/components/ui/input'
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from '@/components/ui/dialog'
import { useSales } from '@/hooks/useSales'
import { useCurrency } from '@/hooks/useCurrency'
import { useAuth } from '@/context/UnifiedAuthContext'
import { useShopContext } from '@/context/ShopContext'
import { 
  useReceiptByTransaction, 
  useGenerateReceipt, 
  useMarkAsPrinted, 
  useMarkAsEmailed 
} from '@/hooks/useReceipts'
import { usePDFReceipt } from '@/hooks/usePDFReceipt'
import { toast } from 'sonner'
import {
  ArrowLeft,
  Printer,
  Mail,
  RotateCcw,
  XCircle,
  Calendar,
  User,
  CreditCard,
  ShoppingBag,
  FileText,
  CheckCircle,
  AlertCircle,
  Clock,
  Download
} from 'lucide-react'
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from '@/components/ui/table'

export const TransactionDetailPage: React.FC = () => {
  const { transactionId } = useParams<{ transactionId: string }>()
  const navigate = useNavigate()
  const { user } = useAuth()
  const { selectedShop } = useShopContext()
  const { formatCurrency } = useCurrency()
  const { getSaleById, isLoading, error } = useSales()
  
  const [transaction, setTransaction] = useState<any>(null)
  const [isProcessing, setIsProcessing] = useState(false)
  const [emailDialogOpen, setEmailDialogOpen] = useState(false)
  const [emailAddress, setEmailAddress] = useState('')
  
  // Receipt hooks
  const { data: receipt, isLoading: receiptLoading, refetch: refetchReceipt } = useReceiptByTransaction(transactionId)
  const generateReceiptMutation = useGenerateReceipt()
  const markPrintedMutation = useMarkAsPrinted()
  const markEmailedMutation = useMarkAsEmailed()
  const { 
    printReceiptByTransactionId, 
    downloadReceiptByTransactionId,
    previewReceiptByTransactionId 
  } = usePDFReceipt()

  useEffect(() => {
    if (transactionId) {
      loadTransaction()
    }
  }, [transactionId])

  const loadTransaction = async () => {
    if (!transactionId) return
    
    const data = await getSaleById(transactionId)
    if (data) {
      setTransaction(data)
    } else {
      toast.error('Transaction not found')
    }
  }

  const handleGenerateReceipt = async () => {
    if (!transactionId) return
    
    setIsProcessing(true)
    try {
      await generateReceiptMutation.mutateAsync(transactionId)
      await refetchReceipt()
      toast.success('Receipt generated successfully')
    } catch (err) {
      toast.error('Failed to generate receipt')
    } finally {
      setIsProcessing(false)
    }
  }

  const handlePrintReceipt = async () => {
    if (!transactionId) return
    
    // Generate receipt if it doesn't exist
    if (!receipt) {
      await handleGenerateReceipt()
      return
    }
    
    setIsProcessing(true)
    try {
      await printReceiptByTransactionId(transactionId, {
        shopAddress: selectedShop?.address || '',
        shopPhone: selectedShop?.phoneNumber || '',
        shopEmail: selectedShop?.email || '',
      })
      
      // Mark as printed
      if (receipt?.id) {
        await markPrintedMutation.mutateAsync({
          receiptId: receipt.id,
          printedBy: user?.username || 'Unknown'
        })
      }
      toast.success('Receipt sent to printer')
    } catch (err) {
      toast.error('Failed to print receipt')
    } finally {
      setIsProcessing(false)
    }
  }

  const handleDownloadReceipt = async () => {
    if (!transactionId) return
    
    // Generate receipt if it doesn't exist
    if (!receipt) {
      await handleGenerateReceipt()
      return
    }
    
    setIsProcessing(true)
    try {
      await downloadReceiptByTransactionId(transactionId, {
        shopAddress: selectedShop?.address || '',
        shopPhone: selectedShop?.phoneNumber || '',
        shopEmail: selectedShop?.email || '',
      })
      toast.success('Receipt downloaded')
    } catch (err) {
      toast.error('Failed to download receipt')
    } finally {
      setIsProcessing(false)
    }
  }

  const handlePreviewReceipt = async () => {
    if (!transactionId) return
    
    // Generate receipt if it doesn't exist
    if (!receipt) {
      await handleGenerateReceipt()
      return
    }
    
    try {
      await previewReceiptByTransactionId(transactionId, {
        shopAddress: selectedShop?.address || '',
        shopPhone: selectedShop?.phoneNumber || '',
        shopEmail: selectedShop?.email || '',
      })
    } catch (err) {
      toast.error('Failed to preview receipt')
    }
  }

  const handleEmailReceipt = () => {
    if (!receipt) {
      toast.error('Please generate a receipt first')
      return
    }
    setEmailDialogOpen(true)
  }

  const handleSendEmail = async () => {
    if (!emailAddress || !receipt) {
      toast.error('Please enter an email address')
      return
    }

    setIsProcessing(true)
    try {
      await markEmailedMutation.mutateAsync({
        receiptId: receipt.id,
        emailAddress
      })
      setEmailDialogOpen(false)
      setEmailAddress('')
      toast.success(`Receipt sent to ${emailAddress}`)
    } catch (err) {
      toast.error('Failed to send receipt')
    } finally {
      setIsProcessing(false)
    }
  }

  const handleRefund = () => {
    // TODO: Implement refund flow
    toast.info('Refund feature coming soon')
    // navigate(`/returns/create?transactionId=${transactionId}`)
  }

  const handleVoidTransaction = () => {
    // TODO: Implement void transaction
    const confirmed = window.confirm('Are you sure you want to void this transaction? This action cannot be undone.')
    if (confirmed) {
      toast.info('Void transaction feature coming soon')
    }
  }

  const getStatusBadge = (status: string) => {
    const variants: Record<string, { variant: any; icon: any }> = {
      COMPLETED: { variant: 'default', icon: CheckCircle },
      PENDING: { variant: 'secondary', icon: Clock },
      CANCELLED: { variant: 'destructive', icon: XCircle },
      REFUNDED: { variant: 'outline', icon: RotateCcw },
    }

    const config = variants[status] || variants.PENDING
    const Icon = config.icon

    return (
      <Badge variant={config.variant} className="flex items-center gap-1">
        <Icon className="w-3 h-3" />
        {status}
      </Badge>
    )
  }

  const getPaymentMethodIcon = (method: string) => {
    switch (method?.toUpperCase()) {
      case 'CASH':
        return '💵'
      case 'CARD':
        return '💳'
      case 'MOBILE':
        return '📱'
      default:
        return '💰'
    }
  }

  if (isLoading && !transaction) {
    return (
      <div className="flex justify-center items-center h-96">
        <div className="text-center">
          <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-primary mx-auto mb-4"></div>
          <p className="text-muted-foreground">Loading transaction...</p>
        </div>
      </div>
    )
  }

  if (error || !transaction) {
    return (
      <div className="space-y-6">
        <Button variant="ghost" onClick={() => navigate('/sales')}>
          <ArrowLeft className="w-4 h-4 mr-2" />
          Back to Sales
        </Button>
        <Alert variant="destructive">
          <AlertCircle className="h-4 w-4" />
          <AlertDescription>
            {error || 'Transaction not found'}
          </AlertDescription>
        </Alert>
      </div>
    )
  }

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <div className="flex items-center gap-4">
          <Button variant="ghost" onClick={() => navigate('/sales')}>
            <ArrowLeft className="w-4 h-4 mr-2" />
            Back
          </Button>
          <div>
            <h1 className="text-3xl font-bold">Transaction Details</h1>
            <p className="text-muted-foreground mt-1">
              {receipt ? `Receipt #${receipt.receiptNumber}` : `Transaction #${transaction.transactionNumber}`}
            </p>
            {!receipt && (
              <p className="text-xs text-amber-600 mt-1">
                ⚠️ Receipt not generated yet
              </p>
            )}
          </div>
        </div>
        <div className="flex gap-2">
          {!receipt && (
            <Button onClick={handleGenerateReceipt} disabled={isProcessing || generateReceiptMutation.isPending}>
              <FileText className="w-4 h-4 mr-2" />
              Generate Receipt
            </Button>
          )}
          <Button variant="outline" onClick={handlePreviewReceipt} disabled={isProcessing}>
            <FileText className="w-4 h-4 mr-2" />
            Preview Receipt
          </Button>
          <Button variant="outline" onClick={handlePrintReceipt} disabled={isProcessing}>
            <Printer className="w-4 h-4 mr-2" />
            {receipt ? 'Print Receipt' : 'Generate & Print'}
          </Button>
          <Button variant="outline" onClick={handleDownloadReceipt} disabled={isProcessing}>
            <Download className="w-4 h-4 mr-2" />
            Download PDF
          </Button>
          <Button variant="outline" onClick={handleEmailReceipt} disabled={!receipt}>
            <Mail className="w-4 h-4 mr-2" />
            Email Receipt
          </Button>
          {transaction.status === 'COMPLETED' && (
            <>
              <Button variant="outline" onClick={handleRefund}>
                <RotateCcw className="w-4 h-4 mr-2" />
                Refund
              </Button>
              <Button variant="destructive" onClick={handleVoidTransaction}>
                <XCircle className="w-4 h-4 mr-2" />
                Void
              </Button>
            </>
          )}
        </div>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-4 gap-4">
        <Card>
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium">Status</CardTitle>
            <FileText className="h-4 w-4 text-muted-foreground" />
          </CardHeader>
          <CardContent>
            {getStatusBadge(transaction.status)}
          </CardContent>
        </Card>

        <Card>
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium">Total Amount</CardTitle>
            <ShoppingBag className="h-4 w-4 text-muted-foreground" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold">{formatCurrency(transaction.totalAmount)}</div>
            <p className="text-xs text-muted-foreground mt-1">
              {transaction.lineItems?.length || 0} item(s)
            </p>
          </CardContent>
        </Card>

        <Card>
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium">Payment Method</CardTitle>
            <CreditCard className="h-4 w-4 text-muted-foreground" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl">
              {getPaymentMethodIcon(transaction.paymentMethod)}
            </div>
            <p className="text-sm font-medium mt-1">
              {transaction.paymentMethod}
            </p>
          </CardContent>
        </Card>

        <Card>
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium">Date & Time</CardTitle>
            <Calendar className="h-4 w-4 text-muted-foreground" />
          </CardHeader>
          <CardContent>
            <div className="text-sm font-medium">
              {new Date(transaction.transactionDate).toLocaleDateString()}
            </div>
            <p className="text-xs text-muted-foreground mt-1">
              {new Date(transaction.transactionDate).toLocaleTimeString()}
            </p>
          </CardContent>
        </Card>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        {/* Main Content */}
        <div className="lg:col-span-2 space-y-6">
          {/* Line Items */}
          <Card>
            <CardHeader>
              <CardTitle>Items Purchased</CardTitle>
              <CardDescription>
                {transaction.lineItems?.length || 0} item(s) in this transaction
              </CardDescription>
            </CardHeader>
            <CardContent>
              <Table>
                <TableHeader>
                  <TableRow>
                    <TableHead>Product</TableHead>
                    <TableHead className="text-right">Qty</TableHead>
                    <TableHead className="text-right">Price</TableHead>
                    {/* <TableHead className="text-right">Subtotal</TableHead> */}
                    <TableHead className="text-right">Tax</TableHead>
                    <TableHead className="text-right">Total</TableHead>
                  </TableRow>
                </TableHeader>
                <TableBody>
                  {transaction.lineItems?.map((item: any, index: number) => (
                    <TableRow key={index}>
                      <TableCell>
                        <div>
                          <div className="font-medium">{item.product?.name || item.productName || 'Unknown Product'}</div>
                          {item.product?.description && (
                            <div className="text-sm text-muted-foreground">
                              {item.product.description}
                            </div>
                          )}
                        </div>
                      </TableCell>
                      <TableCell className="text-right">{item.quantity}</TableCell>
                      <TableCell className="text-right">
                        {formatCurrency(item.product?.price || item.unitPrice || 0)}
                      </TableCell>
                      {/* <TableCell className="text-right">
                        {formatCurrency(Number(item.lineTotal))}
                      </TableCell> */}
                      <TableCell className="text-right">
                        {formatCurrency(item.taxAmount || 0)}
                      </TableCell>
                      <TableCell className="text-right font-medium">
                        {formatCurrency(item.lineTotal)}
                      </TableCell>
                    </TableRow>
                  ))}
                </TableBody>
              </Table>

              <Separator className="my-4" />

              <div className="space-y-2">
                <div className="flex justify-between text-sm">
                  <span className="text-muted-foreground">Subtotal:</span>
                  <span className="font-medium">{formatCurrency(transaction.subtotal)}</span>
                </div>
                <div className="flex justify-between text-sm">
                  <span className="text-muted-foreground">Tax:</span>
                  <span className="font-medium">{formatCurrency(transaction.taxAmount)}</span>
                </div>
                {transaction.discountAmount > 0 && (
                  <div className="flex justify-between text-sm text-green-600">
                    <span>Discount:</span>
                    <span className="font-medium">-{formatCurrency(transaction.discountAmount)}</span>
                  </div>
                )}
                <Separator />
                <div className="flex justify-between text-lg font-bold">
                  <span>Total:</span>
                  <span>{formatCurrency(transaction.totalAmount)}</span>
                </div>
              </div>
            </CardContent>
          </Card>

          {/* Transaction History / Audit Trail */}
          {transaction.history && transaction.history.length > 0 && (
            <Card>
              <CardHeader>
                <CardTitle>Transaction History</CardTitle>
                <CardDescription>Audit trail for this transaction</CardDescription>
              </CardHeader>
              <CardContent>
                <div className="space-y-4">
                  {transaction.history.map((event: any, index: number) => (
                    <div key={index} className="flex items-start gap-3 pb-3 border-b last:border-0">
                      <div className="mt-1">
                        <div className="w-2 h-2 rounded-full bg-primary"></div>
                      </div>
                      <div className="flex-1">
                        <div className="flex items-center justify-between">
                          <span className="font-medium">{event.action}</span>
                          <span className="text-sm text-muted-foreground">
                            {new Date(event.timestamp).toLocaleString()}
                          </span>
                        </div>
                        {event.notes && (
                          <p className="text-sm text-muted-foreground mt-1">{event.notes}</p>
                        )}
                        {event.userId && (
                          <p className="text-xs text-muted-foreground mt-1">
                            By: {event.userName || event.userId}
                          </p>
                        )}
                      </div>
                    </div>
                  ))}
                </div>
              </CardContent>
            </Card>
          )}
        </div>

        <div className="space-y-6">
          <Card>
            <CardHeader>
              <CardTitle className="flex items-center gap-2">
                <CreditCard className="w-5 h-5" />
                Payment Details
              </CardTitle>
            </CardHeader>
            <CardContent className="space-y-3">
              <div>
                <p className="text-sm text-muted-foreground">Payment Method</p>
                <p className="font-medium flex items-center gap-2 mt-1">
                  <span className="text-xl">{getPaymentMethodIcon(transaction.paymentMethod)}</span>
                  {transaction.paymentMethod}
                </p>
              </div>
              <Separator />
              <div>
                <p className="text-sm text-muted-foreground">Amount Paid</p>
                <p className="font-medium text-lg mt-1">{formatCurrency(transaction.totalAmount)}</p>
              </div>
              {transaction.notes && (
                <>
                  <Separator />
                  <div>
                    <p className="text-sm text-muted-foreground">Notes</p>
                    <p className="text-sm mt-1">{transaction.notes}</p>
                  </div>
                </>
              )}
            </CardContent>
          </Card>

          {/* Customer Information */}
          {transaction.customerName && (
            <Card>
              <CardHeader>
                <CardTitle className="flex items-center gap-2">
                  <User className="w-5 h-5" />
                  Customer
                </CardTitle>
              </CardHeader>
              <CardContent className="space-y-3">
                <div>
                  <p className="text-sm text-muted-foreground">Name</p>
                  <p className="font-medium mt-1">{transaction.customerName}</p>
                </div>
                {transaction.customerId && (
                  <>
                    <Separator />
                    <div>
                      <p className="text-sm text-muted-foreground">Customer ID</p>
                      <p className="text-sm font-mono mt-1">{transaction.customerId}</p>
                    </div>
                  </>
                )}
              </CardContent>
            </Card>
          )}

          {/* Cashier Information */}
          <Card>
            <CardHeader>
              <CardTitle className="flex items-center gap-2">
                <User className="w-5 h-5" />
                Cashier
              </CardTitle>
            </CardHeader>
            <CardContent className="space-y-3">
              <div>
                <p className="text-sm text-muted-foreground">Cashier ID</p>
                <p className="text-sm font-mono mt-1">{transaction.cashierId}</p>
              </div>
              <Separator />
              <div>
                <p className="text-sm text-muted-foreground">Shop ID</p>
                <p className="text-sm font-mono mt-1">{transaction.shopId}</p>
              </div>
            </CardContent>
          </Card>

          <Card>
            <CardHeader>
              <CardTitle className="flex items-center gap-2">
                <FileText className="w-5 h-5" />
                Transaction Info
              </CardTitle>
            </CardHeader>
            <CardContent className="space-y-3">
              <div>
                <p className="text-sm text-muted-foreground">Transaction ID</p>
                <p className="text-xs font-mono mt-1 break-all">{transaction.id}</p>
              </div>
              <Separator />
              <div>
                <p className="text-sm text-muted-foreground">Receipt Number</p>
                {receipt ? (
                  <div>
                    <p className="font-medium mt-1">{receipt.receiptNumber}</p>
                    <Badge variant="outline" className="mt-1">
                      {receipt.status}
                    </Badge>
                  </div>
                ) : (
                  <p className="text-sm text-amber-600 mt-1">Not generated</p>
                )}
              </div>
              <Separator />
              <div>
                <p className="text-sm text-muted-foreground">Transaction Date</p>
                <p className="text-sm mt-1">
                  {new Date(transaction.transactionDate).toLocaleString()}
                </p>
              </div>
            </CardContent>
          </Card>

          <Card>
            <CardHeader>
              <CardTitle>Quick Actions</CardTitle>
            </CardHeader>
            <CardContent className="space-y-2">
              {!receipt && (
                <Button 
                  variant="default" 
                  className="w-full justify-start"
                  onClick={handleGenerateReceipt}
                  disabled={isProcessing || generateReceiptMutation.isPending}
                >
                  <FileText className="w-4 h-4 mr-2" />
                  Generate Receipt
                </Button>
              )}
              <Button 
                variant="outline" 
                className="w-full justify-start"
                onClick={handlePreviewReceipt}
                disabled={isProcessing}
              >
                <FileText className="w-4 h-4 mr-2" />
                Preview Receipt
              </Button>
              <Button 
                variant="outline" 
                className="w-full justify-start"
                onClick={handlePrintReceipt}
                disabled={isProcessing}
              >
                <Printer className="w-4 h-4 mr-2" />
                {receipt ? 'Print Receipt' : 'Generate & Print'}
              </Button>
              <Button 
                variant="outline" 
                className="w-full justify-start"
                onClick={handleDownloadReceipt}
                disabled={isProcessing}
              >
                <Download className="w-4 h-4 mr-2" />
                Download PDF
              </Button>
              <Button 
                variant="outline" 
                className="w-full justify-start"
                onClick={handleEmailReceipt}
                disabled={!receipt}
              >
                <Mail className="w-4 h-4 mr-2" />
                Email Receipt
              </Button>
            </CardContent>
          </Card>
        </div>
      </div>

      {/* Email Dialog */}
      <Dialog open={emailDialogOpen} onOpenChange={setEmailDialogOpen}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle>Email Receipt</DialogTitle>
            <DialogDescription>
              Send receipt {receipt?.receiptNumber} to customer via email
            </DialogDescription>
          </DialogHeader>
          <div className="space-y-4 py-4">
            <div>
              <label className="text-sm font-medium mb-2 block">Email Address</label>
              <Input
                type="email"
                placeholder="customer@example.com"
                value={emailAddress}
                onChange={(e) => setEmailAddress(e.target.value)}
                autoFocus
              />
            </div>
          </div>
          <DialogFooter>
            <Button variant="outline" onClick={() => {
              setEmailDialogOpen(false)
              setEmailAddress('')
            }}>
              Cancel
            </Button>
            <Button 
              onClick={handleSendEmail} 
              disabled={!emailAddress || isProcessing || markEmailedMutation.isPending}
            >
              <Mail className="w-4 h-4 mr-2" />
              Send Email
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>
    </div>
  )
}

export default TransactionDetailPage
