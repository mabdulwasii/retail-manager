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
  RiskAssessment,
  RiskLevel,
  AssessmentStatus,
  AssessmentType,
  ResolutionAction
} from '@/hooks/useFraudDetection'
import {
  TrendingUpIcon,
  SearchIcon,
  FilterIcon,
  EyeIcon,
  CheckCircleIcon,
  XCircleIcon,
  AlertCircleIcon,
  ClockIcon,
  ShieldIcon,
  RefreshCwIcon,
  BarChart3Icon
} from 'lucide-react'

interface RiskAssessmentListProps {
  shopId?: string
  onViewAssessment?: (assessment: RiskAssessment) => void
}

export const RiskAssessmentList: React.FC<RiskAssessmentListProps> = ({
  shopId,
  onViewAssessment
}) => {
  const {
    getRiskAssessments,
    approveRiskAssessment,
    rejectRiskAssessment,
    isLoading
  } = useFraudDetection()

  const [assessments, setAssessments] = useState<RiskAssessment[]>([])
  const [filteredAssessments, setFilteredAssessments] = useState<RiskAssessment[]>([])
  const [searchTerm, setSearchTerm] = useState('')
  const [statusFilter, setStatusFilter] = useState<AssessmentStatus | 'ALL'>('ALL')
  const [riskLevelFilter, setRiskLevelFilter] = useState<RiskLevel | 'ALL'>('ALL')
  const [typeFilter, setTypeFilter] = useState<AssessmentType | 'ALL'>('ALL')
  const [currentPage, setCurrentPage] = useState(0)
  const [totalPages, setTotalPages] = useState(0)
  const [isRefreshing, setIsRefreshing] = useState(false)

  // Modal states
  const [selectedAssessment, setSelectedAssessment] = useState<RiskAssessment | null>(null)
  const [isApproveModalOpen, setIsApproveModalOpen] = useState(false)
  const [isRejectModalOpen, setIsRejectModalOpen] = useState(false)
  const [reviewNotes, setReviewNotes] = useState('')
  const [resolutionAction, setResolutionAction] = useState<ResolutionAction>('NO_ACTION')
  const [isProcessing, setIsProcessing] = useState(false)

  const fetchAssessments = useCallback(async () => {
    try {
      setIsRefreshing(true)
      const result = await getRiskAssessments({
        shopId,
        status: statusFilter !== 'ALL' ? statusFilter : undefined,
        riskLevel: riskLevelFilter !== 'ALL' ? riskLevelFilter : undefined,
        assessmentType: typeFilter !== 'ALL' ? typeFilter : undefined,
        page: currentPage,
        size: 20,
        sortBy: 'assessmentDate',
        sortDir: 'desc'
      })

      if (result) {
        setAssessments(result.content)
        setTotalPages(result.totalPages)
      }
    } catch (error) {
      console.error('Failed to fetch risk assessments:', error)
    } finally {
      setIsRefreshing(false)
    }
  }, [shopId, statusFilter, riskLevelFilter, typeFilter, currentPage, getRiskAssessments])

  useEffect(() => {
    fetchAssessments()
  }, [fetchAssessments])

  useEffect(() => {
    filterAssessments()
  }, [assessments, searchTerm])

  const filterAssessments = () => {
    let filtered = assessments

    if (searchTerm) {
      filtered = filtered.filter(assessment =>
        (assessment.transactionNumber && assessment.transactionNumber.toLowerCase().includes(searchTerm.toLowerCase())) ||
        (assessment.shopName && assessment.shopName.toLowerCase().includes(searchTerm.toLowerCase())) ||
        assessment.details.toLowerCase().includes(searchTerm.toLowerCase())
      )
    }

    setFilteredAssessments(filtered)
  }

  const getRiskLevelColor = (level: RiskLevel) => {
    switch (level) {
      case 'CRITICAL': return 'bg-red-100 text-red-800 border-red-200'
      case 'HIGH': return 'bg-orange-100 text-orange-800 border-orange-200'
      case 'MEDIUM': return 'bg-yellow-100 text-yellow-800 border-yellow-200'
      case 'LOW': return 'bg-green-100 text-green-800 border-green-200'
      default: return 'bg-gray-100 text-gray-800 border-gray-200'
    }
  }

  const getStatusColor = (status: AssessmentStatus) => {
    switch (status) {
      case 'PENDING': return 'bg-yellow-100 text-yellow-800'
      case 'UNDER_REVIEW': return 'bg-blue-100 text-blue-800'
      case 'APPROVED': return 'bg-green-100 text-green-800'
      case 'REJECTED': return 'bg-red-100 text-red-800'
      case 'ESCALATED': return 'bg-purple-100 text-purple-800'
      default: return 'bg-gray-100 text-gray-800'
    }
  }

  const getStatusIcon = (status: AssessmentStatus) => {
    switch (status) {
      case 'PENDING': return <ClockIcon className="h-4 w-4" />
      case 'UNDER_REVIEW': return <EyeIcon className="h-4 w-4" />
      case 'APPROVED': return <CheckCircleIcon className="h-4 w-4" />
      case 'REJECTED': return <XCircleIcon className="h-4 w-4" />
      case 'ESCALATED': return <AlertCircleIcon className="h-4 w-4" />
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

  const getAssessmentTypeLabel = (type: AssessmentType) => {
    switch (type) {
      case 'TRANSACTION_FRAUD': return 'Transaction Fraud'
      case 'INVESTMENT_RISK': return 'Investment Risk'
      case 'OPERATIONAL_RISK': return 'Operational Risk'
      case 'COMPLIANCE_CHECK': return 'Compliance Check'
      default: return type
    }
  }

  const getResolutionActionLabel = (action: ResolutionAction) => {
    switch (action) {
      case 'NO_ACTION': return 'No Action Required'
      case 'MONITOR': return 'Monitor Activity'
      case 'INVESTIGATE': return 'Further Investigation'
      case 'BLOCK_TRANSACTION': return 'Block Transaction'
      case 'SUSPEND_ACCOUNT': return 'Suspend Account'
      case 'REPORT_AUTHORITIES': return 'Report to Authorities'
      default: return action
    }
  }

  const handleApprove = (assessment: RiskAssessment) => {
    setSelectedAssessment(assessment)
    setReviewNotes('')
    setIsApproveModalOpen(true)
  }

  const confirmApprove = async () => {
    if (!selectedAssessment) return

    try {
      setIsProcessing(true)
      const result = await approveRiskAssessment(selectedAssessment.id, reviewNotes || undefined)
      if (result) {
        setAssessments(prev =>
          prev.map(a => a.id === selectedAssessment.id ? result : a)
        )
        setIsApproveModalOpen(false)
        setSelectedAssessment(null)
        setReviewNotes('')
      }
    } catch (error) {
      console.error('Failed to approve assessment:', error)
    } finally {
      setIsProcessing(false)
    }
  }

  const handleReject = (assessment: RiskAssessment) => {
    setSelectedAssessment(assessment)
    setReviewNotes('')
    setResolutionAction('INVESTIGATE')
    setIsRejectModalOpen(true)
  }

  const confirmReject = async () => {
    if (!selectedAssessment || !reviewNotes.trim()) return

    try {
      setIsProcessing(true)
      const result = await rejectRiskAssessment(selectedAssessment.id, reviewNotes, resolutionAction)
      if (result) {
        setAssessments(prev =>
          prev.map(a => a.id === selectedAssessment.id ? result : a)
        )
        setIsRejectModalOpen(false)
        setSelectedAssessment(null)
        setReviewNotes('')
      }
    } catch (error) {
      console.error('Failed to reject assessment:', error)
    } finally {
      setIsProcessing(false)
    }
  }

  const canApprove = (assessment: RiskAssessment) => {
    return assessment.status === 'PENDING' || assessment.status === 'UNDER_REVIEW'
  }

  const canReject = (assessment: RiskAssessment) => {
    return assessment.status === 'PENDING' || assessment.status === 'UNDER_REVIEW'
  }

  if (isLoading && assessments.length === 0) {
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
          <h2 className="text-2xl font-bold">Risk Assessments</h2>
          <p className="text-gray-600">Review and manage risk assessments</p>
        </div>
        <Button
          variant="outline"
          onClick={fetchAssessments}
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
                placeholder="Search assessments..."
                value={searchTerm}
                onChange={(e) => setSearchTerm(e.target.value)}
                className="pl-10"
              />
            </div>
            <select
              value={statusFilter}
              onChange={(e) => setStatusFilter(e.target.value as AssessmentStatus | 'ALL')}
              className="px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
            >
              <option value="ALL">All Status</option>
              <option value="PENDING">Pending</option>
              <option value="UNDER_REVIEW">Under Review</option>
              <option value="APPROVED">Approved</option>
              <option value="REJECTED">Rejected</option>
              <option value="ESCALATED">Escalated</option>
            </select>
            <select
              value={riskLevelFilter}
              onChange={(e) => setRiskLevelFilter(e.target.value as RiskLevel | 'ALL')}
              className="px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
            >
              <option value="ALL">All Risk Levels</option>
              <option value="CRITICAL">Critical</option>
              <option value="HIGH">High</option>
              <option value="MEDIUM">Medium</option>
              <option value="LOW">Low</option>
            </select>
            <select
              value={typeFilter}
              onChange={(e) => setTypeFilter(e.target.value as AssessmentType | 'ALL')}
              className="px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
            >
              <option value="ALL">All Types</option>
              <option value="TRANSACTION_FRAUD">Transaction Fraud</option>
              <option value="INVESTMENT_RISK">Investment Risk</option>
              <option value="OPERATIONAL_RISK">Operational Risk</option>
              <option value="COMPLIANCE_CHECK">Compliance Check</option>
            </select>
            <div className="flex items-center text-sm text-gray-600">
              <FilterIcon className="h-4 w-4 mr-1" />
              {filteredAssessments.length} of {assessments.length} assessments
            </div>
          </div>
        </CardContent>
      </Card>

      {/* Assessment List */}
      {filteredAssessments.length === 0 ? (
        <Card>
          <CardContent className="text-center py-12">
            <ShieldIcon className="h-12 w-12 mx-auto text-gray-300 mb-4" />
            <h3 className="text-lg font-medium text-gray-900 mb-2">No assessments found</h3>
            <p className="text-gray-600">
              {assessments.length === 0
                ? 'No risk assessments have been created yet.'
                : 'No assessments match your current filters.'
              }
            </p>
          </CardContent>
        </Card>
      ) : (
        <div className="space-y-4">
          {filteredAssessments.map((assessment) => (
            <Card key={assessment.id} className="hover:shadow-md transition-shadow">
              <CardContent className="p-6">
                <div className="flex items-start justify-between mb-4">
                  <div className="flex items-start space-x-4">
                    <div className="flex-shrink-0">
                      {getStatusIcon(assessment.status)}
                    </div>
                    <div className="flex-grow">
                      <div className="flex items-center space-x-2 mb-2">
                        <Badge className={getRiskLevelColor(assessment.riskLevel)}>
                          {assessment.riskLevel} RISK
                        </Badge>
                        <Badge className={getStatusColor(assessment.status)}>
                          {assessment.status}
                        </Badge>
                        <span className="text-sm text-gray-500">
                          Score: {assessment.riskScore.toFixed(1)}/100
                        </span>
                      </div>

                      <div className="grid grid-cols-1 md:grid-cols-3 gap-4 text-sm mb-4">
                        <div>
                          <p className="text-gray-500">Assessment Type</p>
                          <p className="font-medium">{getAssessmentTypeLabel(assessment.assessmentType)}</p>
                        </div>
                        <div>
                          <p className="text-gray-500">Date</p>
                          <p className="font-medium">{formatDate(assessment.assessmentDate)}</p>
                        </div>
                        {assessment.shopName && (
                          <div>
                            <p className="text-gray-500">Shop</p>
                            <p className="font-medium">{assessment.shopName}</p>
                          </div>
                        )}
                        {assessment.transactionNumber && (
                          <div>
                            <p className="text-gray-500">Transaction</p>
                            <p className="font-medium">{assessment.transactionNumber}</p>
                          </div>
                        )}
                      </div>

                      {assessment.details && (
                        <div className="mb-4">
                          <p className="text-gray-500 text-sm mb-1">Details</p>
                          <p className="text-sm bg-gray-50 p-3 rounded">{assessment.details}</p>
                        </div>
                      )}

                      {assessment.flags && assessment.flags.length > 0 && (
                        <div className="mb-4">
                          <p className="text-gray-500 text-sm mb-2">Risk Flags</p>
                          <div className="flex flex-wrap gap-1">
                            {assessment.flags.map((flag, index) => (
                              <Badge key={index} variant="outline" className="text-xs">
                                {flag}
                              </Badge>
                            ))}
                          </div>
                        </div>
                      )}

                      {(assessment.reviewedBy || assessment.resolutionAction) && (
                        <div className="mt-4 pt-4 border-t">
                          {assessment.reviewedBy && (
                            <p className="text-sm text-gray-600 mb-2">
                              Reviewed by {assessment.reviewedBy} on {formatDate(assessment.reviewedAt!)}
                            </p>
                          )}
                          {assessment.reviewNotes && (
                            <p className="text-sm text-gray-600 mb-2 italic">
                              "{assessment.reviewNotes}"
                            </p>
                          )}
                          {assessment.resolutionAction && (
                            <div className="flex items-center space-x-2">
                              <span className="text-sm text-gray-500">Action:</span>
                              <Badge variant="outline" className="text-xs">
                                {getResolutionActionLabel(assessment.resolutionAction)}
                              </Badge>
                            </div>
                          )}
                        </div>
                      )}
                    </div>
                  </div>
                </div>

                {/* Actions */}
                <div className="flex items-center justify-between pt-4 border-t">
                  <div className="flex items-center space-x-2">
                    {canApprove(assessment) && (
                      <Button
                        size="sm"
                        onClick={() => handleApprove(assessment)}
                        disabled={isProcessing}
                      >
                        <CheckCircleIcon className="h-4 w-4 mr-2" />
                        Approve
                      </Button>
                    )}
                    {canReject(assessment) && (
                      <Button
                        size="sm"
                        variant="outline"
                        onClick={() => handleReject(assessment)}
                        disabled={isProcessing}
                      >
                        <XCircleIcon className="h-4 w-4 mr-2" />
                        Reject
                      </Button>
                    )}
                  </div>
                  <div className="flex items-center space-x-2">
                    <Button
                      variant="ghost"
                      size="sm"
                      onClick={() => onViewAssessment?.(assessment)}
                    >
                      <EyeIcon className="h-4 w-4 mr-2" />
                      View Details
                    </Button>
                    <Button variant="ghost" size="sm">
                      <BarChart3Icon className="h-4 w-4" />
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

      {/* Approve Modal */}
      <Dialog open={isApproveModalOpen} onOpenChange={setIsApproveModalOpen}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle>Approve Risk Assessment</DialogTitle>
          </DialogHeader>
          <div className="space-y-4">
            <p>
              Are you sure you want to approve this risk assessment?
            </p>
            <div>
              <label className="block text-sm font-medium mb-2">
                Review Notes (Optional)
              </label>
              <Textarea
                value={reviewNotes}
                onChange={(e) => setReviewNotes(e.target.value)}
                placeholder="Add any notes about your approval decision..."
                rows={3}
              />
            </div>
          </div>
          <DialogFooter>
            <Button
              variant="outline"
              onClick={() => setIsApproveModalOpen(false)}
              disabled={isProcessing}
            >
              Cancel
            </Button>
            <Button
              onClick={confirmApprove}
              disabled={isProcessing}
            >
              {isProcessing && <LoadingSpinner size="sm" className="mr-2" />}
              Approve Assessment
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>

      {/* Reject Modal */}
      <Dialog open={isRejectModalOpen} onOpenChange={setIsRejectModalOpen}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle>Reject Risk Assessment</DialogTitle>
          </DialogHeader>
          <div className="space-y-4">
            <p>
              Rejecting this risk assessment requires a resolution action and notes.
            </p>
            <div>
              <label className="block text-sm font-medium mb-2">
                Resolution Action *
              </label>
              <select
                value={resolutionAction}
                onChange={(e) => setResolutionAction(e.target.value as ResolutionAction)}
                className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
                required
              >
                <option value="NO_ACTION">No Action Required</option>
                <option value="MONITOR">Monitor Activity</option>
                <option value="INVESTIGATE">Further Investigation</option>
                <option value="BLOCK_TRANSACTION">Block Transaction</option>
                <option value="SUSPEND_ACCOUNT">Suspend Account</option>
                <option value="REPORT_AUTHORITIES">Report to Authorities</option>
              </select>
            </div>
            <div>
              <label className="block text-sm font-medium mb-2">
                Rejection Reason *
              </label>
              <Textarea
                value={reviewNotes}
                onChange={(e) => setReviewNotes(e.target.value)}
                placeholder="Explain why this assessment is being rejected and what action will be taken..."
                rows={4}
                required
              />
            </div>
          </div>
          <DialogFooter>
            <Button
              variant="outline"
              onClick={() => setIsRejectModalOpen(false)}
              disabled={isProcessing}
            >
              Cancel
            </Button>
            <Button
              onClick={confirmReject}
              disabled={isProcessing || !reviewNotes.trim()}
            >
              {isProcessing && <LoadingSpinner size="sm" className="mr-2" />}
              Reject Assessment
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>
    </div>
  )
}