
export const getMockNotification = (overrides = {}) => ({
  id: '1',
  type: 'FRAUD_ALERT' as const,
  title: 'Critical Fraud Alert',
  message: 'High-risk transaction detected',
  severity: 'critical' as const,
  timestamp: new Date('2024-01-15T10:30:00Z'),
  read: false,
  metadata: {
    alertId: 'ALERT-123',
    transactionId: 'TXN-456',
    riskScore: 85
  },
  actionUrl: '/fraud/alerts/ALERT-123',
  ...overrides
})

export const getMockNotifications = () => [
  getMockNotification(),
  getMockNotification({
    id: '2',
    type: 'RISK_ASSESSMENT' as const,
    title: 'Risk Assessment Review',
    message: 'New high-risk assessment requires review',
    severity: 'high' as const,
    metadata: {
      assessmentId: 'RISK-789',
      riskLevel: 'HIGH'
    },
    actionUrl: '/fraud/assessments/RISK-789'
  }),
  getMockNotification({
    id: '3',
    type: 'SYSTEM' as const,
    title: 'System Update',
    message: 'Fraud detection rules updated',
    severity: 'medium' as const,
    read: true,
    actionUrl: '/fraud/rules'
  }),
  getMockNotification({
    id: '4',
    type: 'INFO' as const,
    title: 'New Feature Available',
    message: 'Check out the new dashboard',
    severity: 'low' as const,
    read: false
  })
]

export const getMockNotificationPreferences = (overrides = {}) => ({
  emailEnabled: true,
  smsEnabled: false,
  pushEnabled: true,
  fraudAlerts: true,
  riskAssessments: true,
  systemUpdates: true,
  ...overrides
})
