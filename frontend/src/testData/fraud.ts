/**
 * Test Data: Fraud Detection
 * Mock fraud detection data for testing
 */

export const getMockFraudAlert = (overrides = {}) => ({
  id: 'alert1',
  alertNumber: 'FRD-2024-001',
  alertType: 'SUSPICIOUS_TRANSACTION' as const,
  severity: 'HIGH' as const,
  status: 'ACTIVE' as const,
  title: 'Suspicious Large Transaction',
  description: 'Unusually large transaction detected',
  shopId: 'shop1',
  shopName: 'Electronics Store',
  userId: 'user1',
  userName: 'John Doe',
  transactionId: 'txn123',
  riskScore: 85.5,
  confidenceLevel: 92.3,
  evidence: {
    amount: '15000',
    avgTransaction: '500',
    deviation: '30x'
  },
  detectionRule: 'LARGE_TRANSACTION_RULE',
  detectionTimestamp: new Date('2024-01-15T10:30:00Z').toISOString(),
  falsePositive: false,
  createdAt: new Date('2024-01-15T10:30:00Z').toISOString(),
  ...overrides
})

export const getMockFraudAlerts = () => ({
  content: [
    getMockFraudAlert(),
    getMockFraudAlert({
      id: 'alert2',
      alertNumber: 'FRD-2024-002',
      alertType: 'VELOCITY_FRAUD' as const,
      severity: 'MEDIUM' as const,
      title: 'High Transaction Velocity',
      description: 'Multiple transactions in short time',
      riskScore: 65.0,
      status: 'ACKNOWLEDGED' as const
    }),
    getMockFraudAlert({
      id: 'alert3',
      alertNumber: 'FRD-2024-003',
      alertType: 'RETURN_FRAUD' as const,
      severity: 'LOW' as const,
      title: 'Suspicious Return Pattern',
      description: 'Multiple returns without receipts',
      riskScore: 45.0,
      status: 'RESOLVED' as const,
      resolvedBy: 'admin1',
      resolvedAt: new Date('2024-01-16').toISOString()
    })
  ],
  totalElements: 3,
  totalPages: 1,
  number: 0,
  size: 20
})

export const getMockRiskAssessment = (overrides = {}) => ({
  id: 'risk1',
  shopId: 'shop1',
  shopName: 'Electronics Store',
  transactionId: 'txn123',
  transactionNumber: 'TXN-2024-001',
  assessmentType: 'TRANSACTION_RISK',
  riskLevel: 'MEDIUM' as const,
  riskScore: 55.5,
  assessmentDate: new Date('2024-01-15').toISOString(),
  flags: ['LARGE_AMOUNT', 'NEW_CUSTOMER'],
  details: 'Transaction flagged for manual review',
  status: 'UNDER_REVIEW' as const,
  createdAt: new Date('2024-01-15').toISOString(),
  ...overrides
})

export const getMockRiskAssessments = () => ({
  content: [
    getMockRiskAssessment(),
    getMockRiskAssessment({
      id: 'risk2',
      riskLevel: 'LOW' as const,
      riskScore: 25.0,
      status: 'APPROVED' as const,
      reviewedBy: 'admin1',
      reviewedAt: new Date('2024-01-16').toISOString()
    })
  ],
  totalElements: 2,
  totalPages: 1,
  number: 0,
  size: 20
})

export const getMockFraudRule = (overrides = {}) => ({
  id: 'rule1',
  shopId: 'shop1',
  ruleName: 'Large Transaction Rule',
  ruleType: 'AMOUNT_THRESHOLD',
  description: 'Detects transactions above threshold',
  enabled: true,
  thresholdAmount: 10000,
  riskScoreWeight: 0.8,
  severity: 'HIGH' as const,
  autoBlock: false,
  requiresManualReview: true,
  createdAt: new Date('2024-01-01').toISOString(),
  ...overrides
})

export const getMockFraudRules = () => [
  getMockFraudRule(),
  getMockFraudRule({
    id: 'rule2',
    ruleName: 'Velocity Check',
    ruleType: 'TRANSACTION_VELOCITY',
    thresholdCount: 5,
    timeWindowMinutes: 60,
    severity: 'MEDIUM' as const
  }),
  getMockFraudRule({
    id: 'rule3',
    ruleName: 'Duplicate Detection',
    ruleType: 'DUPLICATE_CHECK',
    severity: 'LOW' as const,
    autoBlock: true
  })
]
