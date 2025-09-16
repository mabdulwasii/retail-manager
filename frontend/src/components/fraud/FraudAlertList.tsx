import React, { useState, useEffect, useCallback } from 'react'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import { Badge } from '@/components/ui/badge'
import { LoadingSpinner } from '@/components/ui/loading-spinner'
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
  DialogFooter,
} from '@/components/ui/dialog'
import { Textarea } from '@/components/ui/textarea'
import {
  useFraudDetection,
  FraudAlert,
  AlertStatus,
  AlertSeverity,
  AlertType
} from '@/hooks/useFraudDetection'
import {
  AlertTriangleIcon,
  SearchIcon,
  FilterIcon,
  EyeIcon,
  CheckCircleIcon,
  XCircleIcon,
  AlertCircleIcon,
  ClockIcon,
  ShieldCheckIcon,
  RefreshCwIcon,
  MoreHorizontalIcon
} from 'lucide-react'

interface FraudAlertListProps {
  shopId?: string
  onViewAlert?: (alert: FraudAlert) => void
}

export const FraudAlertList: React.FC<FraudAlertListProps> = ({
  shopId,
  onViewAlert
}) => {
  const {
    getFraudAlerts,
    acknowledgeFraudAlert,
    resolveFraudAlert,
    markAlertAsFalsePositive,
    isLoading
  } = useFraudDetection()

  const [alerts, setAlerts] = useState<FraudAlert[]>([])
  const [filteredAlerts, setFilteredAlerts] = useState<FraudAlert[]>([])
  const [searchTerm, setSearchTerm] = useState('')
  const [statusFilter, setStatusFilter] = useState<AlertStatus | 'ALL'>('ALL')
  const [severityFilter, setSeverityFilter] = useState<AlertSeverity | 'ALL'>('ALL')
  const [typeFilter, setTypeFilter] = useState<AlertType | 'ALL'>('ALL')
  const [currentPage, setCurrentPage] = useState(0)
  const [totalPages, setTotalPages] = useState(0)
  const [isRefreshing, setIsRefreshing] = useState(false)

  // Modal states
  const [selectedAlert, setSelectedAlert] = useState<FraudAlert | null>(null)
  const [isResolveModalOpen, setIsResolveModalOpen] = useState(false)
  const [isFalsePositiveModalOpen, setIsFalsePositiveModalOpen] = useState(false)
  const [resolutionNotes, setResolutionNotes] = useState('')
  const [falsePositiveReason, setFalsePositiveReason] = useState('')
  const [isProcessing, setIsProcessing] = useState(false)

  const fetchAlerts = useCallback(async () => {
    try {
      setIsRefreshing(true)
      const result = await getFraudAlerts({
        shopId,
        status: statusFilter !== 'ALL' ? statusFilter : undefined,
        severity: severityFilter !== 'ALL' ? severityFilter : undefined,
        alertType: typeFilter !== 'ALL' ? typeFilter : undefined,
        page: currentPage,
        size: 20,
        sortBy: 'detectionTimestamp',
        sortDir: 'desc'
      })

      if (result) {
        setAlerts(result.content)
        setTotalPages(result.totalPages)
      }
    } catch (error) {
      console.error('Failed to fetch fraud alerts:', error)
    } finally {
      setIsRefreshing(false)
    }
  }, [shopId, statusFilter, severityFilter, typeFilter, currentPage, getFraudAlerts])

  useEffect(() => {
    fetchAlerts()
  }, [fetchAlerts])

  useEffect(() => {
    filterAlerts()
  }, [alerts, searchTerm])

  const filterAlerts = () => {
    let filtered = alerts

    if (searchTerm) {
      filtered = filtered.filter(alert =>
        alert.alertNumber.toLowerCase().includes(searchTerm.toLowerCase()) ||
        alert.title.toLowerCase().includes(searchTerm.toLowerCase()) ||
        alert.description.toLowerCase().includes(searchTerm.toLowerCase()) ||
        (alert.shopName && alert.shopName.toLowerCase().includes(searchTerm.toLowerCase()))
      )
    }

    setFilteredAlerts(filtered)
  }

  const getSeverityColor = (severity: AlertSeverity) => {
    switch (severity) {
      case 'CRITICAL': return 'bg-red-100 text-red-800 border-red-200'
      case 'HIGH': return 'bg-orange-100 text-orange-800 border-orange-200'
      case 'MEDIUM': return 'bg-yellow-100 text-yellow-800 border-yellow-200'
      case 'LOW': return 'bg-blue-100 text-blue-800 border-blue-200'
      default: return 'bg-gray-100 text-gray-800 border-gray-200'
    }
  }

  const getStatusColor = (status: AlertStatus) => {
    switch (status) {
      case 'ACTIVE': return 'bg-red-100 text-red-800'
      case 'ACKNOWLEDGED': return 'bg-yellow-100 text-yellow-800'
      case 'INVESTIGATING': return 'bg-blue-100 text-blue-800'
      case 'RESOLVED': return 'bg-green-100 text-green-800'
      case 'FALSE_POSITIVE': return 'bg-gray-100 text-gray-800'
      case 'DISMISSED': return 'bg-gray-100 text-gray-800'
      default: return 'bg-gray-100 text-gray-800'
    }
  }

  const getStatusIcon = (status: AlertStatus) => {
    switch (status) {
      case 'ACTIVE': return <AlertTriangleIcon className="h-4 w-4" />
      case 'ACKNOWLEDGED': return <ClockIcon className="h-4 w-4" />
      case 'INVESTIGATING': return <SearchIcon className="h-4 w-4" />
      case 'RESOLVED': return <CheckCircleIcon className="h-4 w-4" />
      case 'FALSE_POSITIVE': return <XCircleIcon className="h-4 w-4" />
      case 'DISMISSED': return <XCircleIcon className="h-4 w-4" />
      default: return <AlertCircleIcon className="h-4 w-4" />
    }
  }

  const formatDate = (dateString: string) => {
    return new Date(dateString).toLocaleString('en-NG', {
      year: 'numeric',
      month: 'short',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    })
  }

  const getAlertTypeLabel = (type: AlertType) => {
    switch (type) {
      case 'SUSPICIOUS_TRANSACTION': return 'Suspicious Transaction'
      case 'UNUSUAL_INVESTMENT_PATTERN': return 'Investment Pattern'
      case 'EXCESSIVE_WITHDRAWALS': return 'Excessive Withdrawals'
      case 'DUPLICATE_TRANSACTIONS': return 'Duplicate Transactions'
      case 'VELOCITY_FRAUD': return 'Velocity Fraud'
      case 'ACCOUNT_TAKEOVER': return 'Account Takeover'
      case 'PRICE_MANIPULATION': return 'Price Manipulation'
      case 'RETURN_FRAUD': return 'Return Fraud'
      case 'COLLUSION_DETECTION': return 'Collusion Detected'
      case 'ANOMALOUS_BEHAVIOR': return 'Anomalous Behavior'
      default: return type
    }
  }

  const handleAcknowledge = async (alert: FraudAlert) => {
    try {
      setIsProcessing(true)
      const result = await acknowledgeFraudAlert(alert.id)
      if (result) {
        // Update the alert in the list
        setAlerts(prev =>
          prev.map(a => a.id === alert.id ? result : a)
        )
      }
    } catch (error) {
      console.error('Failed to acknowledge alert:', error)
    } finally {
      setIsProcessing(false)
    }
  }

  const handleResolve = (alert: FraudAlert) => {
    setSelectedAlert(alert)
    setResolutionNotes('')
    setIsResolveModalOpen(true)
  }

  const confirmResolve = async () => {
    if (!selectedAlert || !resolutionNotes.trim()) return

    try {
      setIsProcessing(true)
      const result = await resolveFraudAlert(selectedAlert.id, resolutionNotes)
      if (result) {
        setAlerts(prev =>
          prev.map(a => a.id === selectedAlert.id ? result : a)
        )
        setIsResolveModalOpen(false)
        setSelectedAlert(null)
        setResolutionNotes('')
      }
    } catch (error) {
      console.error('Failed to resolve alert:', error)
    } finally {
      setIsProcessing(false)
    }
  }

  const handleMarkFalsePositive = (alert: FraudAlert) => {
    setSelectedAlert(alert)
    setFalsePositiveReason('')
    setIsFalsePositiveModalOpen(true)
  }

  const confirmFalsePositive = async () => {
    if (!selectedAlert || !falsePositiveReason.trim()) return

    try {
      setIsProcessing(true)
      const result = await markAlertAsFalsePositive(selectedAlert.id, falsePositiveReason)
      if (result) {
        setAlerts(prev =>
          prev.map(a => a.id === selectedAlert.id ? result : a)
        )
        setIsFalsePositiveModalOpen(false)
        setSelectedAlert(null)
        setFalsePositiveReason('')
      }
    } catch (error) {
      console.error('Failed to mark as false positive:', error)
    } finally {
      setIsProcessing(false)
    }
  }

  const canAcknowledge = (alert: FraudAlert) => {
    return alert.status === 'ACTIVE'
  }

  const canResolve = (alert: FraudAlert) => {
    return alert.status === 'ACKNOWLEDGED' || alert.status === 'INVESTIGATING'
  }

  const canMarkFalsePositive = (alert: FraudAlert) => {
    return alert.status !== 'RESOLVED' && alert.status !== 'FALSE_POSITIVE'
  }

  if (isLoading && alerts.length === 0) {
    return (
      <div className="flex items-center justify-center h-64">
        <LoadingSpinner size="lg" />
      </div>
    )
  }

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
        <div>
          <h2 className="text-2xl font-bold">Fraud Alerts</h2>
          <p className="text-gray-600">Monitor and manage security alerts</p>
        </div>
        <Button
          variant="outline"
          onClick={fetchAlerts}
          disabled={isRefreshing}
        >
          <RefreshCwIcon className={`h-4 w-4 mr-2 ${isRefreshing ? 'animate-spin' : ''}`} />
          Refresh
        </Button>
      </div>

      {/* Filters */}
      <Card>
        <CardContent className="p-4">
          <div className="grid grid-cols-1 md:grid-cols-5 gap-4">
            <div className="relative">
              <SearchIcon className="absolute left-3 top-3 h-4 w-4 text-gray-400" />
              <Input
                placeholder="Search alerts..."
                value={searchTerm}
                onChange={(e) => setSearchTerm(e.target.value)}
                className="pl-10"
              />
            </div>
            <select
              value={statusFilter}
              onChange={(e) => setStatusFilter(e.target.value as AlertStatus | 'ALL')}
              className="px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
            >
              <option value="ALL">All Status</option>
              <option value="ACTIVE">Active</option>
              <option value="ACKNOWLEDGED">Acknowledged</option>
              <option value="INVESTIGATING">Investigating</option>
              <option value="RESOLVED">Resolved</option>
              <option value="FALSE_POSITIVE">False Positive</option>
            </select>
            <select
              value={severityFilter}
              onChange={(e) => setSeverityFilter(e.target.value as AlertSeverity | 'ALL')}
              className="px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
            >
              <option value="ALL">All Severity</option>
              <option value="CRITICAL">Critical</option>
              <option value="HIGH">High</option>
              <option value="MEDIUM">Medium</option>
              <option value="LOW">Low</option>
            </select>
            <select
              value={typeFilter}
              onChange={(e) => setTypeFilter(e.target.value as AlertType | 'ALL')}
              className="px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
            >
              <option value="ALL">All Types</option>
              <option value="SUSPICIOUS_TRANSACTION">Suspicious Transaction</option>
              <option value="VELOCITY_FRAUD">Velocity Fraud</option>
              <option value="DUPLICATE_TRANSACTIONS">Duplicate Transactions</option>
              <option value="ANOMALOUS_BEHAVIOR">Anomalous Behavior</option>
            </select>
            <div className="flex items-center text-sm text-gray-600">
              <FilterIcon className="h-4 w-4 mr-1" />
              {filteredAlerts.length} of {alerts.length} alerts
            </div>
          </div>
        </CardContent>
      </Card>

      {/* Alert List */}
      {filteredAlerts.length === 0 ? (
        <Card>
          <CardContent className="text-center py-12">
            <ShieldCheckIcon className="h-12 w-12 mx-auto text-gray-300 mb-4" />
            <h3 className="text-lg font-medium text-gray-900 mb-2">No alerts found</h3>
            <p className="text-gray-600">
              {alerts.length === 0
                ? 'No fraud alerts have been generated yet.'
                : 'No alerts match your current filters.'
              }
            </p>
          </CardContent>
        </Card>
      ) : (
        <div className="space-y-4">
          {filteredAlerts.map((alert) => (
            <Card key={alert.id} className="hover:shadow-md transition-shadow">
              <CardContent className="p-6">
                <div className="flex items-start justify-between mb-4">
                  <div className="flex items-start space-x-4">
                    <div className="flex-shrink-0">
                      {getStatusIcon(alert.status)}
                    </div>
                    <div className="flex-grow">
                      <div className="flex items-center space-x-2 mb-2">
                        <h3 className="font-semibold text-lg">{alert.alertNumber}</h3>
                        <Badge className={getSeverityColor(alert.severity)}>
                          {alert.severity}
                        </Badge>
                        <Badge className={getStatusColor(alert.status)}>
                          {alert.status}
                        </Badge>
                      </div>
                      <h4 className="font-medium text-gray-900 mb-1">{alert.title}</h4>
                      <p className="text-gray-600 text-sm mb-3">{alert.description}</p>

                      <div className="grid grid-cols-1 md:grid-cols-3 gap-4 text-sm">
                        <div>
                          <p className="text-gray-500">Type</p>
                          <p className="font-medium">{getAlertTypeLabel(alert.alertType)}</p>
                        </div>
                        <div>
                          <p className="text-gray-500">Risk Score</p>
                          <p className="font-medium">{alert.riskScore.toFixed(1)}/100</p>
                        </div>
                        <div>
                          <p className="text-gray-500">Detected</p>
                          <p className="font-medium">{formatDate(alert.detectionTimestamp)}</p>
                        </div>
                        {alert.shopName && (
                          <div>
                            <p className="text-gray-500">Shop</p>
                            <p className="font-medium">{alert.shopName}</p>
                          </div>
                        )}
                        {alert.transactionId && (
                          <div>
                            <p className="text-gray-500">Transaction ID</p>
                            <p className="font-mono text-xs">{alert.transactionId}</p>
                          </div>
                        )}
                        <div>
                          <p className="text-gray-500">Confidence</p>
                          <p className="font-medium">{alert.confidenceLevel.toFixed(1)}%</p>
                        </div>
                      </div>

                      {alert.detectionRule && (
                        <div className="mt-3">
                          <p className="text-gray-500 text-sm">Detection Rules</p>
                          <p className="text-sm font-mono bg-gray-100 px-2 py-1 rounded">
                            {alert.detectionRule}
                          </p>
                        </div>
                      )}

                      {(alert.acknowledgedBy || alert.resolvedBy) && (
                        <div className="mt-3 pt-3 border-t">
                          {alert.acknowledgedBy && (
                            <p className="text-sm text-gray-600">
                              Acknowledged by {alert.acknowledgedBy} on {formatDate(alert.acknowledgedAt!)}
                            </p>
                          )}
                          {alert.resolvedBy && (
                            <p className="text-sm text-gray-600">
                              Resolved by {alert.resolvedBy} on {formatDate(alert.resolvedAt!)}
                              {alert.resolutionNotes && (
                                <span className="block mt-1 italic">"{alert.resolutionNotes}"</span>
                              )}
                            </p>
                          )}
                        </div>
                      )}
                    </div>
                  </div>
                </div>

                {/* Actions */}
                <div className="flex items-center justify-between pt-4 border-t">
                  <div className="flex items-center space-x-2">
                    {canAcknowledge(alert) && (
                      <Button
                        size="sm"
                        variant="outline"
                        onClick={() => handleAcknowledge(alert)}
                        disabled={isProcessing}
                      >
                        <CheckCircleIcon className="h-4 w-4 mr-2" />
                        Acknowledge
                      </Button>
                    )}
                    {canResolve(alert) && (
                      <Button
                        size="sm"
                        onClick={() => handleResolve(alert)}
                        disabled={isProcessing}
                      >
                        <CheckCircleIcon className="h-4 w-4 mr-2" />
                        Resolve
                      </Button>
                    )}
                    {canMarkFalsePositive(alert) && (
                      <Button
                        size="sm"
                        variant="outline"
                        onClick={() => handleMarkFalsePositive(alert)}
                        disabled={isProcessing}
                      >
                        <XCircleIcon className="h-4 w-4 mr-2" />
                        False Positive
                      </Button>
                    )}
                  </div>
                  <div className="flex items-center space-x-2">
                    <Button
                      variant="ghost"
                      size="sm"
                      onClick={() => onViewAlert?.(alert)}
                    >
                      <EyeIcon className="h-4 w-4 mr-2" />
                      View Details
                    </Button>
                  </div>
                </div>
              </CardContent>
            </Card>
          ))}
        </div>
      )}

      {/* Pagination */}
      {totalPages > 1 && (
        <div className="flex items-center justify-center space-x-2">
          <Button
            variant="outline"
            size="sm"
            disabled={currentPage === 0}
            onClick={() => setCurrentPage(currentPage - 1)}
          >
            Previous
          </Button>
          <span className="text-sm text-gray-600">
            Page {currentPage + 1} of {totalPages}
          </span>
          <Button
            variant="outline"
            size="sm"
            disabled={currentPage === totalPages - 1}
            onClick={() => setCurrentPage(currentPage + 1)}
          >
            Next
          </Button>
        </div>
      )}

      {/* Resolve Modal */}
      <Dialog open={isResolveModalOpen} onOpenChange={setIsResolveModalOpen}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle>Resolve Fraud Alert</DialogTitle>
          </DialogHeader>
          <div className="space-y-4">
            <p>
              Are you sure you want to resolve alert{' '}
              <strong>{selectedAlert?.alertNumber}</strong>?
            </p>
            <div>
              <label className="block text-sm font-medium mb-2">
                Resolution Notes *
              </label>
              <Textarea
                value={resolutionNotes}
                onChange={(e) => setResolutionNotes(e.target.value)}
                placeholder="Describe how this alert was resolved..."
                rows={4}
                required
              />
            </div>
          </div>
          <DialogFooter>
            <Button
              variant="outline"
              onClick={() => setIsResolveModalOpen(false)}
              disabled={isProcessing}
            >
              Cancel
            </Button>
            <Button
              onClick={confirmResolve}
              disabled={isProcessing || !resolutionNotes.trim()}
            >
              {isProcessing && <LoadingSpinner size="sm" className="mr-2" />}
              Resolve Alert
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>

      {/* False Positive Modal */}
      <Dialog open={isFalsePositiveModalOpen} onOpenChange={setIsFalsePositiveModalOpen}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle>Mark as False Positive</DialogTitle>
          </DialogHeader>
          <div className="space-y-4">
            <p>
              Mark alert <strong>{selectedAlert?.alertNumber}</strong> as a false positive.
              This will help improve the fraud detection system.
            </p>
            <div>
              <label className="block text-sm font-medium mb-2">
                Reason *
              </label>
              <Textarea
                value={falsePositiveReason}
                onChange={(e) => setFalsePositiveReason(e.target.value)}
                placeholder="Explain why this is a false positive..."
                rows={3}
                required
              />
            </div>
          </div>
          <DialogFooter>
            <Button
              variant="outline"
              onClick={() => setIsFalsePositiveModalOpen(false)}
              disabled={isProcessing}
            >
              Cancel
            </Button>
            <Button
              onClick={confirmFalsePositive}
              disabled={isProcessing || !falsePositiveReason.trim()}
            >
              {isProcessing && <LoadingSpinner size="sm" className="mr-2" />}
              Mark False Positive
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>
    </div>
  )
}