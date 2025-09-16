import { useState, useCallback } from 'react'
import { api } from '@/services/api'

export interface FraudAlert {
  id: string
  alertNumber: string
  alertType: AlertType
  severity: AlertSeverity
  status: AlertStatus
  title: string
  description: string
  shopId?: string
  shopName?: string
  userId?: string
  userName?: string
  transactionId?: string
  investmentId?: string
  riskScore: number
  confidenceLevel: number
  evidence: Record<string, string>
  detectionRule: string
  detectionTimestamp: string
  acknowledgedBy?: string
  acknowledgedAt?: string
  resolvedBy?: string
  resolvedAt?: string
  resolutionNotes?: string
  falsePositive: boolean
  createdAt: string
  updatedAt?: string
}

export interface RiskAssessment {
  id: string
  shopId?: string
  shopName?: string
  transactionId?: string
  transactionNumber?: string
  assessmentType: AssessmentType
  riskLevel: RiskLevel
  riskScore: number
  assessmentDate: string
  flags: string[]
  details: string
  status: AssessmentStatus
  reviewedBy?: string
  reviewedAt?: string
  reviewNotes?: string
  resolutionAction?: ResolutionAction
  createdAt: string
  updatedAt?: string
}

export interface FraudRule {
  id: string
  shopId?: string
  ruleName: string
  ruleType: FraudRuleType
  description?: string
  enabled: boolean
  thresholdAmount?: number
  thresholdCount?: number
  timeWindowMinutes?: number
  riskScoreWeight: number
  severity: RiskLevel
  autoBlock: boolean
  requiresManualReview: boolean
  ruleConfiguration?: string
  createdAt: string
  updatedAt?: string
}

export interface CreateFraudRuleRequest {
  ruleName: string
  ruleType: FraudRuleType
  description?: string
  shopId?: string
  enabled?: boolean
  thresholdAmount?: number
  thresholdCount?: number
  timeWindowMinutes?: number
  riskScoreWeight?: number
  severity?: RiskLevel
  autoBlock?: boolean
  requiresManualReview?: boolean
  ruleConfiguration?: string
}

export interface FraudStatistics {
  alerts: {
    total: number
    highSeverity: number
    critical: number
    byType: Array<{ type: string; count: number }>
  }
  riskAssessments: {
    pending: number
    underReview: number
    byRiskLevel: Array<{ level: string; count: number }>
  }
  rules: {
    total: number
    byType: Array<{ type: string; count: number }>
  }
  dateRange: {
    startDate: string
    endDate: string
  }
}

export type AlertType =
  | 'SUSPICIOUS_TRANSACTION'
  | 'UNUSUAL_INVESTMENT_PATTERN'
  | 'EXCESSIVE_WITHDRAWALS'
  | 'DUPLICATE_TRANSACTIONS'
  | 'VELOCITY_FRAUD'
  | 'ACCOUNT_TAKEOVER'
  | 'PRICE_MANIPULATION'
  | 'RETURN_FRAUD'
  | 'COLLUSION_DETECTION'
  | 'ANOMALOUS_BEHAVIOR'

export type AlertSeverity = 'LOW' | 'MEDIUM' | 'HIGH' | 'CRITICAL'

export type AlertStatus =
  | 'ACTIVE'
  | 'ACKNOWLEDGED'
  | 'INVESTIGATING'
  | 'RESOLVED'
  | 'FALSE_POSITIVE'
  | 'DISMISSED'

export type AssessmentType =
  | 'TRANSACTION_FRAUD'
  | 'INVESTMENT_RISK'
  | 'OPERATIONAL_RISK'
  | 'COMPLIANCE_CHECK'

export type RiskLevel = 'LOW' | 'MEDIUM' | 'HIGH' | 'CRITICAL'

export type AssessmentStatus =
  | 'PENDING'
  | 'UNDER_REVIEW'
  | 'APPROVED'
  | 'REJECTED'
  | 'ESCALATED'

export type ResolutionAction =
  | 'NO_ACTION'
  | 'MONITOR'
  | 'INVESTIGATE'
  | 'BLOCK_TRANSACTION'
  | 'SUSPEND_ACCOUNT'
  | 'REPORT_AUTHORITIES'

export type FraudRuleType =
  | 'HIGH_AMOUNT_TRANSACTION'
  | 'HIGH_FREQUENCY_TRANSACTIONS'
  | 'UNUSUAL_TIME_TRANSACTION'
  | 'RAPID_SUCCESSIVE_TRANSACTIONS'
  | 'UNUSUAL_PAYMENT_METHOD'
  | 'SUSPICIOUS_CUSTOMER_PATTERN'
  | 'INVENTORY_MISMATCH'
  | 'GEOGRAPHIC_ANOMALY'
  | 'VELOCITY_CHECK'
  | 'BLACKLIST_CHECK'
  | 'CUSTOM_RULE'

export interface PaginatedResponse<T> {
  content: T[]
  totalElements: number
  totalPages: number
  number: number
  size: number
}

export const useFraudDetection = () => {
  const [isLoading, setIsLoading] = useState(false)
  const [error, setError] = useState<string | null>(null)

  const getFraudAlerts = useCallback(async (
    params: {
      shopId?: string
      status?: AlertStatus
      severity?: AlertSeverity
      alertType?: AlertType
      page?: number
      size?: number
      sortBy?: string
      sortDir?: string
    } = {}
  ): Promise<PaginatedResponse<FraudAlert> | null> => {
    try {
      setIsLoading(true)
      setError(null)

      const searchParams = new URLSearchParams()
      if (params.shopId) searchParams.append('shopId', params.shopId)
      if (params.status) searchParams.append('status', params.status)
      if (params.severity) searchParams.append('severity', params.severity)
      if (params.alertType) searchParams.append('alertType', params.alertType)
      if (params.page !== undefined) searchParams.append('page', params.page.toString())
      if (params.size !== undefined) searchParams.append('size', params.size.toString())
      if (params.sortBy) searchParams.append('sortBy', params.sortBy)
      if (params.sortDir) searchParams.append('sortDir', params.sortDir)

      const response = await api.get(`/fraud/alerts?${searchParams}`)
      return response.data
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to fetch fraud alerts')
      return null
    } finally {
      setIsLoading(false)
    }
  }, [])

  const getFraudAlertById = useCallback(async (alertId: string): Promise<FraudAlert | null> => {
    try {
      setIsLoading(true)
      setError(null)

      const response = await api.get(`/fraud/alerts/${alertId}`)
      return response.data
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to fetch fraud alert')
      return null
    } finally {
      setIsLoading(false)
    }
  }, [])

  const acknowledgeFraudAlert = useCallback(async (alertId: string): Promise<FraudAlert | null> => {
    try {
      setIsLoading(true)
      setError(null)

      const response = await api.post(`/fraud/alerts/${alertId}/acknowledge`)
      return response.data
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to acknowledge fraud alert')
      return null
    } finally {
      setIsLoading(false)
    }
  }, [])

  const resolveFraudAlert = useCallback(async (
    alertId: string,
    resolutionNotes: string
  ): Promise<FraudAlert | null> => {
    try {
      setIsLoading(true)
      setError(null)

      const params = new URLSearchParams({ resolutionNotes })
      const response = await api.post(`/fraud/alerts/${alertId}/resolve?${params}`)
      return response.data
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to resolve fraud alert')
      return null
    } finally {
      setIsLoading(false)
    }
  }, [])

  const markAlertAsFalsePositive = useCallback(async (
    alertId: string,
    reason: string
  ): Promise<FraudAlert | null> => {
    try {
      setIsLoading(true)
      setError(null)

      const params = new URLSearchParams({ reason })
      const response = await api.post(`/fraud/alerts/${alertId}/false-positive?${params}`)
      return response.data
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to mark alert as false positive')
      return null
    } finally {
      setIsLoading(false)
    }
  }, [])

  const getRiskAssessments = useCallback(async (
    params: {
      shopId?: string
      riskLevel?: RiskLevel
      status?: AssessmentStatus
      assessmentType?: AssessmentType
      page?: number
      size?: number
      sortBy?: string
      sortDir?: string
    } = {}
  ): Promise<PaginatedResponse<RiskAssessment> | null> => {
    try {
      setIsLoading(true)
      setError(null)

      const searchParams = new URLSearchParams()
      if (params.shopId) searchParams.append('shopId', params.shopId)
      if (params.riskLevel) searchParams.append('riskLevel', params.riskLevel)
      if (params.status) searchParams.append('status', params.status)
      if (params.assessmentType) searchParams.append('assessmentType', params.assessmentType)
      if (params.page !== undefined) searchParams.append('page', params.page.toString())
      if (params.size !== undefined) searchParams.append('size', params.size.toString())
      if (params.sortBy) searchParams.append('sortBy', params.sortBy)
      if (params.sortDir) searchParams.append('sortDir', params.sortDir)

      const response = await api.get(`/fraud/risk-assessments?${searchParams}`)
      return response.data
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to fetch risk assessments')
      return null
    } finally {
      setIsLoading(false)
    }
  }, [])

  const approveRiskAssessment = useCallback(async (
    assessmentId: string,
    reviewNotes?: string
  ): Promise<RiskAssessment | null> => {
    try {
      setIsLoading(true)
      setError(null)

      const params = new URLSearchParams()
      if (reviewNotes) params.append('reviewNotes', reviewNotes)

      const response = await api.post(`/fraud/risk-assessments/${assessmentId}/approve?${params}`)
      return response.data
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to approve risk assessment')
      return null
    } finally {
      setIsLoading(false)
    }
  }, [])

  const rejectRiskAssessment = useCallback(async (
    assessmentId: string,
    reviewNotes: string,
    action: ResolutionAction
  ): Promise<RiskAssessment | null> => {
    try {
      setIsLoading(true)
      setError(null)

      const params = new URLSearchParams({
        reviewNotes,
        action
      })

      const response = await api.post(`/fraud/risk-assessments/${assessmentId}/reject?${params}`)
      return response.data
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to reject risk assessment')
      return null
    } finally {
      setIsLoading(false)
    }
  }, [])

  const getFraudRules = useCallback(async (
    params: {
      shopId?: string
      ruleType?: FraudRuleType
      enabled?: boolean
      page?: number
      size?: number
      sortBy?: string
      sortDir?: string
    } = {}
  ): Promise<PaginatedResponse<FraudRule> | null> => {
    try {
      setIsLoading(true)
      setError(null)

      const searchParams = new URLSearchParams()
      if (params.shopId) searchParams.append('shopId', params.shopId)
      if (params.ruleType) searchParams.append('ruleType', params.ruleType)
      if (params.enabled !== undefined) searchParams.append('enabled', params.enabled.toString())
      if (params.page !== undefined) searchParams.append('page', params.page.toString())
      if (params.size !== undefined) searchParams.append('size', params.size.toString())
      if (params.sortBy) searchParams.append('sortBy', params.sortBy)
      if (params.sortDir) searchParams.append('sortDir', params.sortDir)

      const response = await api.get(`/fraud/rules?${searchParams}`)
      return response.data
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to fetch fraud rules')
      return null
    } finally {
      setIsLoading(false)
    }
  }, [])

  const createFraudRule = useCallback(async (request: CreateFraudRuleRequest): Promise<FraudRule | null> => {
    try {
      setIsLoading(true)
      setError(null)

      const response = await api.post('/fraud/rules', request)
      return response.data
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to create fraud rule')
      return null
    } finally {
      setIsLoading(false)
    }
  }, [])

  const updateFraudRule = useCallback(async (
    ruleId: string,
    request: CreateFraudRuleRequest
  ): Promise<FraudRule | null> => {
    try {
      setIsLoading(true)
      setError(null)

      const response = await api.put(`/fraud/rules/${ruleId}`, request)
      return response.data
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to update fraud rule')
      return null
    } finally {
      setIsLoading(false)
    }
  }, [])

  const deleteFraudRule = useCallback(async (ruleId: string): Promise<boolean> => {
    try {
      setIsLoading(true)
      setError(null)

      await api.delete(`/fraud/rules/${ruleId}`)
      return true
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to delete fraud rule')
      return false
    } finally {
      setIsLoading(false)
    }
  }, [])

  const updateRuleStatus = useCallback(async (
    ruleId: string,
    enabled: boolean
  ): Promise<FraudRule | null> => {
    try {
      setIsLoading(true)
      setError(null)

      const params = new URLSearchParams({ enabled: enabled.toString() })
      const response = await api.put(`/fraud/rules/${ruleId}/status?${params}`)
      return response.data
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to update rule status')
      return null
    } finally {
      setIsLoading(false)
    }
  }, [])

  const getFraudStatistics = useCallback(async (
    shopId?: string,
    startDate?: string,
    endDate?: string
  ): Promise<FraudStatistics | null> => {
    try {
      setIsLoading(true)
      setError(null)

      const params = new URLSearchParams()
      if (shopId) params.append('shopId', shopId)
      if (startDate) params.append('startDate', startDate)
      if (endDate) params.append('endDate', endDate)

      const response = await api.get(`/fraud/statistics?${params}`)
      return response.data
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to fetch fraud statistics')
      return null
    } finally {
      setIsLoading(false)
    }
  }, [])

  return {
    isLoading,
    error,
    getFraudAlerts,
    getFraudAlertById,
    acknowledgeFraudAlert,
    resolveFraudAlert,
    markAlertAsFalsePositive,
    getRiskAssessments,
    approveRiskAssessment,
    rejectRiskAssessment,
    getFraudRules,
    createFraudRule,
    updateFraudRule,
    deleteFraudRule,
    updateRuleStatus,
    getFraudStatistics
  }
}