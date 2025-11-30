import React, { useEffect, useState } from 'react'
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import { Badge } from '@/components/ui/badge'
import { LoadingSpinner } from '@/components/ui/loading-spinner'
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/components/ui/select'
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from '@/components/ui/table'
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuSeparator,
  DropdownMenuTrigger,
} from '@/components/ui/dropdown-menu'
import { 
  useReceipts, 
  useMarkAsPrinted, 
  useMarkAsEmailed, 
  useRegenerateReceipt,
  Receipt,
  ReceiptFilter 
} from '@/hooks/useReceipts'
import { useAuth } from '@/context/ManualAuthContext'
import { useCurrency } from '@/hooks/useCurrency'
import { usePDFReceipt } from '@/hooks/usePDFReceipt'
import {
  Download,
  Mail,
  MoreVertical,
  Printer,
  Receipt as ReceiptIcon,
  RefreshCw,
  Search,
  Filter,
  Eye,
  FileText,
} from 'lucide-react'
import { useNavigate } from 'react-router-dom'
import { toast } from 'sonner'

export const ReceiptsPage: React.FC = () => {
  const navigate = useNavigate()
  const { user } = useAuth()
  const { formatCurrency } = useCurrency()

  // State for filters and UI
  const [searchQuery, setSearchQuery] = useState('')
  const [statusFilter, setStatusFilter] = useState<string>('all')
  const [dateRange, setDateRange] = useState({ startDate: '', endDate: '' })
  const [page, setPage] = useState(0)
  const [emailDialogOpen, setEmailDialogOpen] = useState(false)
  const [selectedReceipt, setSelectedReceipt] = useState<Receipt | null>(null)
  const [emailAddress, setEmailAddress] = useState('')

  // Build filter object
  const filter: ReceiptFilter = {
    page,
    size: 20,
    sort: 'generatedAt,desc',
    ...(user?.shopId && { shopId: user.shopId }),
    ...(statusFilter !== 'all' && { status: statusFilter }),
    ...(dateRange.startDate && { startDate: dateRange.startDate }),
    ...(dateRange.endDate && { endDate: dateRange.endDate }),
  }

  // Fetch receipts with React Query
  const { data: receiptsData, isLoading, error, refetch } = useReceipts(filter)

  // PDF Receipt hook
  const { 
    downloadReceiptByTransactionId, 
    printReceiptByTransactionId,
    previewReceiptByTransactionId 
  } = usePDFReceipt()

  // Mutations
  const markPrintedMutation = useMarkAsPrinted()
  const markEmailedMutation = useMarkAsEmailed()
  const regenerateMutation = useRegenerateReceipt()

  const handleReset = () => {
    setSearchQuery('')
    setStatusFilter('all')
    setDateRange({ startDate: '', endDate: '' })
    setPage(0)
  }

  const handlePrint = async (receipt: Receipt) => {
    try {
      await printReceiptByTransactionId(receipt.transactionId, {
        shopAddress: 'Shop Address Here', // TODO: Get from shop settings
        shopPhone: 'Shop Phone Here', // TODO: Get from shop settings
        shopEmail: 'shop@email.com', // TODO: Get from shop settings
      })
      await markPrintedMutation.mutateAsync({ 
        receiptId: receipt.id, 
        printedBy: user?.username || 'Unknown' 
      })
    } catch (error) {
      // Error already handled in hook
    }
  }

  const handleDownload = async (receipt: Receipt) => {
    try {
      await downloadReceiptByTransactionId(receipt.transactionId, {
        shopAddress: 'Shop Address Here', // TODO: Get from shop settings
        shopPhone: 'Shop Phone Here', // TODO: Get from shop settings
        shopEmail: 'shop@email.com', // TODO: Get from shop settings
      })
    } catch (error) {
      // Error already handled in hook
    }
  }

  const handlePreview = async (receipt: Receipt) => {
    try {
      await previewReceiptByTransactionId(receipt.transactionId, {
        shopAddress: 'Shop Address Here', // TODO: Get from shop settings
        shopPhone: 'Shop Phone Here', // TODO: Get from shop settings
        shopEmail: 'shop@email.com', // TODO: Get from shop settings
      })
    } catch (error) {
      // Error already handled in hook
    }
  }

  const handleEmailClick = (receipt: Receipt) => {
    setSelectedReceipt(receipt)
    setEmailDialogOpen(true)
  }

  const handleSendEmail = async () => {
    if (!selectedReceipt || !emailAddress) {
      toast.error('Please enter an email address')
      return
    }

    await markEmailedMutation.mutateAsync({ 
      receiptId: selectedReceipt.id, 
      emailAddress 
    })
    setEmailDialogOpen(false)
    setEmailAddress('')
    setSelectedReceipt(null)
  }

  const handleRegenerate = async (transactionId: string) => {
    const confirmed = window.confirm('Are you sure you want to regenerate this receipt?')
    if (confirmed) {
      regenerateMutation.mutate(transactionId)
    }
  }

  const handleViewTransaction = (transactionId: string) => {
    navigate(`/sales/${transactionId}`)
  }

  // Extract data from query result
  const receipts = receiptsData?.content || []
  const totalPages = receiptsData?.totalPages || 0
  const totalElements = receiptsData?.totalElements || 0
  const currentPage = receiptsData?.number || 0

  const getStatusBadge = (status: string) => {
    const statusConfig: Record<string, { label: string; className: string }> = {
      GENERATED: { label: 'Generated', className: 'bg-blue-100 text-blue-800' },
      PRINTED: { label: 'Printed', className: 'bg-green-100 text-green-800' },
      EMAILED: { label: 'Emailed', className: 'bg-purple-100 text-purple-800' },
      REGENERATED: { label: 'Regenerated', className: 'bg-yellow-100 text-yellow-800' },
    }

    const config = statusConfig[status] || { label: status, className: 'bg-gray-100 text-gray-800' }
    return <Badge className={config.className}>{config.label}</Badge>
  }

  const formatDate = (dateString: string) => {
    return new Date(dateString).toLocaleDateString('en-US', {
      year: 'numeric',
      month: 'short',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit',
    })
  }

  // Filter receipts by search query
  const filteredReceipts = receipts.filter(receipt => {
    if (!searchQuery) return true
    const query = searchQuery.toLowerCase()
    return (
      receipt.receiptNumber.toLowerCase().includes(query) ||
      receipt.transactionNumber.toLowerCase().includes(query) ||
      receipt.shopName?.toLowerCase().includes(query)
    )
  })

  const renderContent = () => {
    if (isLoading) {
      return (
        <div className="flex items-center justify-center py-8">
          <LoadingSpinner size="lg" />
        </div>
      )
    }

    if (filteredReceipts.length === 0) {
      return (
        <div className="text-center py-8 text-gray-500">
          <ReceiptIcon className="h-12 w-12 mx-auto mb-2 text-gray-300" />
          <p>No receipts found</p>
          <p className="text-sm">Try adjusting your filters</p>
        </div>
      )
    }

    return (
      <>
        <div className="overflow-x-auto">
          <Table>
            <TableHeader>
              <TableRow>
                <TableHead>Receipt #</TableHead>
                <TableHead>Transaction #</TableHead>
                <TableHead>Shop</TableHead>
                <TableHead>Generated</TableHead>
                <TableHead>Status</TableHead>
                <TableHead>Last Action</TableHead>
                <TableHead className="text-right">Actions</TableHead>
              </TableRow>
            </TableHeader>
            <TableBody>
              {filteredReceipts.map((receipt) => (
                <TableRow key={receipt.id}>
                  <TableCell>
                    <div className="font-medium">{receipt.receiptNumber}</div>
                  </TableCell>
                  <TableCell>
                    <Button
                      variant="link"
                      className="p-0 h-auto"
                      onClick={() => handleViewTransaction(receipt.transactionId)}
                    >
                      {receipt.transactionNumber}
                    </Button>
                  </TableCell>
                  <TableCell>{receipt.shopName || 'N/A'}</TableCell>
                  <TableCell>
                    <div className="text-sm">{formatDate(receipt.generatedAt)}</div>
                  </TableCell>
                  <TableCell>{getStatusBadge(receipt.status)}</TableCell>
                  <TableCell>
                    <div className="text-sm space-y-1">
                      {receipt.printedAt && (
                        <div className="text-green-600">
                          Printed: {formatDate(receipt.printedAt)}
                        </div>
                      )}
                      {receipt.emailedAt && (
                        <div className="text-purple-600">
                          Emailed: {formatDate(receipt.emailedAt)}
                        </div>
                      )}
                      {!receipt.printedAt && !receipt.emailedAt && (
                        <div className="text-gray-400">No action yet</div>
                      )}
                    </div>
                  </TableCell>
                  <TableCell className="text-right">
                    <DropdownMenu>
                      <DropdownMenuTrigger asChild>
                        <Button variant="ghost" size="sm">
                          <MoreVertical className="h-4 w-4" />
                        </Button>
                      </DropdownMenuTrigger>
                      <DropdownMenuContent align="end">
                        <DropdownMenuItem onClick={() => handlePreview(receipt)}>
                          <FileText className="w-4 h-4 mr-2" />
                          Preview Receipt
                        </DropdownMenuItem>
                        <DropdownMenuSeparator />
                        <DropdownMenuItem onClick={() => handlePrint(receipt)}>
                          <Printer className="w-4 h-4 mr-2" />
                          Print
                        </DropdownMenuItem>
                        <DropdownMenuItem onClick={() => handleDownload(receipt)}>
                          <Download className="w-4 h-4 mr-2" />
                          Download PDF
                        </DropdownMenuItem>
                        <DropdownMenuItem onClick={() => handleEmailClick(receipt)}>
                          <Mail className="w-4 h-4 mr-2" />
                          Email
                        </DropdownMenuItem>
                        <DropdownMenuSeparator />
                        <DropdownMenuItem onClick={() => handleViewTransaction(receipt.transactionId)}>
                          <Eye className="w-4 h-4 mr-2" />
                          View Transaction
                        </DropdownMenuItem>
                        <DropdownMenuItem onClick={() => handleRegenerate(receipt.transactionId)}>
                          <RefreshCw className="w-4 h-4 mr-2" />
                          Regenerate
                        </DropdownMenuItem>
                      </DropdownMenuContent>
                    </DropdownMenu>
                  </TableCell>
                </TableRow>
              ))}
            </TableBody>
          </Table>
        </div>

        {/* Pagination */}
        {totalPages > 1 && (
          <div className="flex items-center justify-between mt-4 pt-4 border-t">
            <div className="text-sm text-gray-600">
              Showing {page * 20 + 1} to{' '}
              {Math.min((page + 1) * 20, totalElements)} of{' '}
              {totalElements} receipts
            </div>
            <div className="flex space-x-2">
              <Button
                variant="outline"
                size="sm"
                onClick={() => setPage(p => p - 1)}
                disabled={page === 0 || isLoading}
              >
                Previous
              </Button>
              <Button
                variant="outline"
                size="sm"
                onClick={() => setPage(p => p + 1)}
                disabled={page >= totalPages - 1 || isLoading}
              >
                Next
              </Button>
            </div>
          </div>
        )}
      </>
    )
  }

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-3xl font-bold text-gray-900">Receipts</h1>
          <p className="text-gray-600">Manage and track all receipt documents</p>
        </div>
        <Button onClick={() => refetch()} disabled={isLoading}>
          <RefreshCw className={`w-4 h-4 mr-2 ${isLoading ? 'animate-spin' : ''}`} />
          Refresh
        </Button>
      </div>

      {/* Error Alert */}
      {error && (
        <div className="bg-red-50 border border-red-200 rounded-lg p-4">
          <div className="flex items-center">
            <div className="flex-shrink-0">
              <svg className="h-5 w-5 text-red-400" viewBox="0 0 20 20" fill="currentColor">
                <path
                  fillRule="evenodd"
                  d="M10 18a8 8 0 100-16 8 8 0 000 16zM8.707 7.293a1 1 0 00-1.414 1.414L8.586 10l-1.293 1.293a1 1 0 101.414 1.414L10 11.414l1.293 1.293a1 1 0 001.414-1.414L11.414 10l1.293-1.293a1 1 0 00-1.414-1.414L10 8.586 8.707 7.293z"
                  clipRule="evenodd"
                />
              </svg>
            </div>
            <div className="ml-3">
              <p className="text-sm text-red-700">{error.message || 'An error occurred'}</p>
            </div>
          </div>
        </div>
      )}

      {/* Filters */}
      <Card>
        <CardHeader>
          <div className="flex items-center space-x-2">
            <Filter className="h-5 w-5" />
            <CardTitle>Filters</CardTitle>
          </div>
        </CardHeader>
        <CardContent>
          <div className="grid grid-cols-1 md:grid-cols-4 gap-4">
            <div>
              <label className="text-sm font-medium mb-2 block">Search</label>
              <div className="relative">
                <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 h-4 w-4 text-gray-400" />
                <Input
                  placeholder="Receipt # or Transaction #"
                  value={searchQuery}
                  onChange={(e) => setSearchQuery(e.target.value)}
                  className="pl-10"
                />
              </div>
            </div>

            <div>
              <label className="text-sm font-medium mb-2 block">Status</label>
              <Select value={statusFilter} onValueChange={setStatusFilter}>
                <SelectTrigger>
                  <SelectValue />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value="all">All Status</SelectItem>
                  <SelectItem value="GENERATED">Generated</SelectItem>
                  <SelectItem value="PRINTED">Printed</SelectItem>
                  <SelectItem value="EMAILED">Emailed</SelectItem>
                  <SelectItem value="REGENERATED">Regenerated</SelectItem>
                </SelectContent>
              </Select>
            </div>

            <div>
              <label className="text-sm font-medium mb-2 block">Start Date</label>
              <Input
                type="date"
                value={dateRange.startDate}
                onChange={(e) => setDateRange({ ...dateRange, startDate: e.target.value })}
              />
            </div>

            <div>
              <label className="text-sm font-medium mb-2 block">End Date</label>
              <Input
                type="date"
                value={dateRange.endDate}
                onChange={(e) => setDateRange({ ...dateRange, endDate: e.target.value })}
              />
            </div>
          </div>

          <div className="flex space-x-2 mt-4">
            <Button onClick={() => setPage(0)} disabled={isLoading}>
              <Search className="w-4 h-4 mr-2" />
              Apply Filters
            </Button>
            <Button variant="outline" onClick={handleReset}>
              <RefreshCw className="w-4 h-4 mr-2" />
              Reset
            </Button>
          </div>
        </CardContent>
      </Card>

      {/* Receipts Table */}
      <Card>
        <CardHeader>
          <CardTitle>Receipts ({totalElements})</CardTitle>
          <CardDescription>All generated receipts for your shop</CardDescription>
        </CardHeader>
        <CardContent>
          {renderContent()}
        </CardContent>
      </Card>

      {/* Email Dialog */}
      {emailDialogOpen && selectedReceipt && (
        <div className="fixed inset-0 bg-black/50 flex items-center justify-center z-50">
          <Card className="w-full max-w-md">
            <CardHeader>
              <CardTitle>Email Receipt</CardTitle>
              <CardDescription>
                Send receipt {selectedReceipt.receiptNumber} via email
              </CardDescription>
            </CardHeader>
            <CardContent className="space-y-4">
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
              <div className="flex space-x-2 justify-end">
                <Button
                  variant="outline"
                  onClick={() => {
                    setEmailDialogOpen(false)
                    setEmailAddress('')
                    setSelectedReceipt(null)
                  }}
                >
                  Cancel
                </Button>
                <Button onClick={handleSendEmail} disabled={!emailAddress || markEmailedMutation.isPending}>
                  <Mail className="w-4 h-4 mr-2" />
                  Send Email
                </Button>
              </div>
            </CardContent>
          </Card>
        </div>
      )}
    </div>
  )
}