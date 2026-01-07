import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { useAuth } from '@/context/UnifiedAuthContext'
import { Permission } from '@/types/permissions'
import { toast } from 'sonner'
import { api } from '@/services/api'

// Type definitions
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
export type AlertStatus = 'ACTIVE' | 'ACKNOWLEDGED' | 'INVESTIGATING' | 'RESOLVED' | 'FALSE_POSITIVE' | 'DISMISSED'
export type RiskLevel = 'LOW' | 'MEDIUM' | 'HIGH' | 'CRITICAL'
export type AssessmentStatus = 'PENDING' | 'UNDER_REVIEW' | 'APPROVED' | 'REJECTED' | 'ESCALATED'

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
  assessmentType: string
  riskLevel: RiskLevel
  riskScore: number
  assessmentDate: string
  flags: string[]
  details: string
  status: AssessmentStatus
  reviewedBy?: string
  reviewedAt?: string
  reviewNotes?: string
  resolutionAction?: string
  createdAt: string
  updatedAt?: string
}

export interface FraudRule {
  id: string
  shopId?: string
  ruleName: string
  ruleType: string
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

interface PaginatedResponse<T> {
  content: T[]
  totalElements: number
  totalPages: number
  number: number
  size: number
}

// Query hook for fetching fraud alerts with permission check
export const useFraudAlerts = (params?: {
  shopId?: string
  status?: AlertStatus
  severity?: AlertSeverity
  alertType?: AlertType
  page?: number
  size?: number
}) => {
  const { isAuthenticated, hasAnyPermission } = useAuth()
  const { user } = useAuth()
  const targetShopId = params?.shopId || user?.shopId

  return useQuery({
    queryKey: ["fraud", "alerts", targetShopId, params],
    queryFn: async () => {
      const searchParams = new URLSearchParams();
      if (targetShopId) searchParams.append("shopId", targetShopId);
      if (params?.status) searchParams.append("status", params.status);
      if (params?.severity) searchParams.append("severity", params.severity);
      if (params?.alertType) searchParams.append("alertType", params.alertType);
      if (params?.page !== undefined)
        searchParams.append("page", params.page.toString());
      if (params?.size !== undefined)
        searchParams.append("size", params.size.toString());

      return await api.get<PaginatedResponse<FraudAlert>>(
        `/fraud/alerts?${searchParams}`
      );
    },
    enabled: !!(
      isAuthenticated &&
      hasAnyPermission([Permission.FRAUD_VIEW, Permission.FRAUD_LIST, Permission.FRAUD_MANAGE])
    ),
    staleTime: 1 * 60 * 1000,
    retry: 1,
  });
}

// Query hook for fetching fraud alert by ID
export const useFraudAlertById = (alertId?: string) => {
  const { isAuthenticated, hasAnyPermission } = useAuth()

  return useQuery({
    queryKey: ['fraud', 'alerts', alertId],
    queryFn: () => api.get<FraudAlert>(`/fraud/alerts/${alertId}`),
    enabled: !!(isAuthenticated && alertId && 
    hasAnyPermission([Permission.FRAUD_VIEW, Permission.FRAUD_LIST, Permission.FRAUD_MANAGE])),
    staleTime: 2 * 60 * 1000,
    retry: 1
  })
}

// Query hook for fetching risk assessments
export const useRiskAssessments = (params?: {
  shopId?: string
  status?: AssessmentStatus
  riskLevel?: RiskLevel
  page?: number
  size?: number
}) => {
  const { isAuthenticated, hasAnyPermission, user } = useAuth()
  const targetShopId = params?.shopId || user?.shopId

  return useQuery({
    queryKey: ['fraud', 'assessments', targetShopId, params],
    queryFn: async () => {
      const searchParams = new URLSearchParams()
      if (targetShopId) searchParams.append('shopId', targetShopId)
      if (params?.status) searchParams.append('status', params.status)
      if (params?.riskLevel) searchParams.append('riskLevel', params.riskLevel)
      if (params?.page !== undefined) searchParams.append('page', params.page.toString())
      if (params?.size !== undefined) searchParams.append('size', params.size.toString())

      return await api.get<PaginatedResponse<RiskAssessment>>(`/fraud/risk-assessments?${searchParams}`)
    },
    enabled: !!(isAuthenticated && hasAnyPermission([Permission.FRAUD_VIEW, Permission.FRAUD_LIST, Permission.FRAUD_MANAGE])),
    staleTime: 2 * 60 * 1000,
    retry: 1
  })
}

// Query hook for fetching fraud rules
export const useFraudRules = (shopId?: string) => {
  const { isAuthenticated, hasPermission, user } = useAuth()
  const targetShopId = shopId || user?.shopId

  return useQuery({
    queryKey: ['fraud', 'rules', targetShopId],
    queryFn: async () => {
      const searchParams = new URLSearchParams()
      if (targetShopId) searchParams.append('shopId', targetShopId)
      return await api.get<FraudRule[]>(`/fraud/rules?${searchParams}`)
    },
    enabled: !!(isAuthenticated && hasPermission(Permission.FRAUD_MANAGE)),
    staleTime: 5 * 60 * 1000,
    retry: 1
  })
}

// Mutation hook for acknowledging fraud alert
export const useAcknowledgeFraudAlert = () => {
  const queryClient = useQueryClient()

  return useMutation({
    mutationFn: ({ alertId, notes }: { alertId: string; notes?: string }) => 
      api.post(`/fraud/alerts/${alertId}/acknowledge`, { notes }),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['fraud', 'alerts'] })
      toast.success('Alert acknowledged')
    },
    onError: (error: any) => {
      toast.error('Failed to acknowledge alert', {
        description: error.response?.data?.message || error.message
      })
    }
  })
}

// Mutation hook for resolving fraud alert
export const useResolveFraudAlert = () => {
  const queryClient = useQueryClient()

  return useMutation({
    mutationFn: ({ alertId, notes, falsePositive }: { 
      alertId: string
      notes?: string
      falsePositive?: boolean 
    }) => api.post(`/fraud/alerts/${alertId}/resolve`, { notes, falsePositive }),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['fraud', 'alerts'] })
      toast.success('Alert resolved')
    },
    onError: (error: any) => {
      toast.error('Failed to resolve alert', {
        description: error.response?.data?.message || error.message
      })
    }
  })
}

// Mutation hook for creating fraud rule
export const useCreateFraudRule = () => {
  const queryClient = useQueryClient()

  return useMutation({
    mutationFn: (data: Partial<FraudRule>) => 
      api.post('/fraud/rules', data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['fraud', 'rules'] })
      toast.success('Fraud rule created successfully')
    },
    onError: (error: any) => {
      toast.error('Failed to create fraud rule', {
        description: error.response?.data?.message || error.message
      })
    }
  })
}

// Mutation hook for updating fraud rule
export const useUpdateFraudRule = () => {
  const queryClient = useQueryClient()

  return useMutation({
    mutationFn: ({ ruleId, data }: { ruleId: string; data: Partial<FraudRule> }) => 
      api.patch(`/fraud/rules/${ruleId}`, data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['fraud', 'rules'] })
      toast.success('Fraud rule updated successfully')
    },
    onError: (error: any) => {
      toast.error('Failed to update fraud rule', {
        description: error.response?.data?.message || error.message
      })
    }
  })
}

// Mutation hook for deleting fraud rule
export const useDeleteFraudRule = () => {
  const queryClient = useQueryClient()

  return useMutation({
    mutationFn: (ruleId: string) => api.delete(`/fraud/rules/${ruleId}`),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['fraud', 'rules'] })
      toast.success('Fraud rule deleted successfully')
    },
    onError: (error: any) => {
      toast.error('Failed to delete fraud rule', {
        description: error.response?.data?.message || error.message
      })
    }
  })
}
