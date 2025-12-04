import { useState, useEffect, useCallback } from 'react'
import { useAuth } from '@/context/ManualAuthContext'

export interface Notification {
  id: string
  type: 'FRAUD_ALERT' | 'RISK_ASSESSMENT' | 'SYSTEM' | 'INFO' | 'WARNING' | 'ERROR'
  title: string
  message: string
  severity: 'low' | 'medium' | 'high' | 'critical'
  timestamp: Date
  read: boolean
  metadata?: Record<string, any>
  actionUrl?: string
}

export interface NotificationPreferences {
  emailEnabled: boolean
  smsEnabled: boolean
  pushEnabled: boolean
  fraudAlerts: boolean
  riskAssessments: boolean
  systemUpdates: boolean
}

export const useNotifications = () => {
  const { user } = useAuth()
  const [notifications, setNotifications] = useState<Notification[]>([])
  const [unreadCount, setUnreadCount] = useState(0)
  const [isLoading, setIsLoading] = useState(false)
  const [preferences, setPreferences] = useState<NotificationPreferences>({
    emailEnabled: true,
    smsEnabled: false,
    pushEnabled: true,
    fraudAlerts: true,
    riskAssessments: true,
    systemUpdates: true
  })

  // Simulate WebSocket connection for real-time notifications
  useEffect(() => {
    if (!user) return

    // In a real implementation, this would establish WebSocket connection
    // const ws = new WebSocket(`ws://localhost:8081/notifications?token=${token}`)

    // Simulate receiving notifications
    const simulateNotifications = () => {
      const mockNotifications: Notification[] = [
        {
          id: '1',
          type: 'FRAUD_ALERT',
          title: 'Critical Fraud Alert',
          message: 'High-risk transaction detected requiring immediate attention',
          severity: 'critical',
          timestamp: new Date(),
          read: false,
          metadata: {
            alertId: 'ALERT-123',
            transactionId: 'TXN-456',
            riskScore: 85
          },
          actionUrl: '/fraud/alerts/ALERT-123'
        },
        {
          id: '2',
          type: 'RISK_ASSESSMENT',
          title: 'Risk Assessment Review',
          message: 'New high-risk assessment requires manual review',
          severity: 'high',
          timestamp: new Date(Date.now() - 5 * 60 * 1000), // 5 minutes ago
          read: false,
          metadata: {
            assessmentId: 'RISK-789',
            riskLevel: 'HIGH'
          },
          actionUrl: '/fraud/assessments/RISK-789'
        },
        {
          id: '3',
          type: 'SYSTEM',
          title: 'System Update',
          message: 'Fraud detection rules have been updated',
          severity: 'medium',
          timestamp: new Date(Date.now() - 30 * 60 * 1000), // 30 minutes ago
          read: true,
          actionUrl: '/fraud/rules'
        }
      ]

      setNotifications(mockNotifications)
      setUnreadCount(mockNotifications.filter(n => !n.read).length)
    }

    // Simulate initial load and periodic updates
    simulateNotifications()
    const interval = setInterval(simulateNotifications, 30000) // Update every 30 seconds

    return () => {
      clearInterval(interval)
      // In real implementation: ws.close()
    }
  }, [user])

  const markAsRead = useCallback((notificationId: string) => {
    setNotifications(prev =>
      prev.map(notification =>
        notification.id === notificationId
          ? { ...notification, read: true }
          : notification
      )
    )
    setUnreadCount(prev => Math.max(0, prev - 1))
  }, [])

  const markAllAsRead = useCallback(() => {
    setNotifications(prev =>
      prev.map(notification => ({ ...notification, read: true }))
    )
    setUnreadCount(0)
  }, [])

  const deleteNotification = useCallback((notificationId: string) => {
    setNotifications(prev => {
      const updatedNotifications = prev.filter(n => n.id !== notificationId)
      const deletedNotification = prev.find(n => n.id === notificationId)

      if (deletedNotification && !deletedNotification.read) {
        setUnreadCount(current => Math.max(0, current - 1))
      }

      return updatedNotifications
    })
  }, [])

  const addNotification = useCallback((notification: Omit<Notification, 'id' | 'timestamp'>) => {
    const newNotification: Notification = {
      ...notification,
      id: Date.now().toString(),
      timestamp: new Date()
    }

    setNotifications(prev => [newNotification, ...prev])

    if (!newNotification.read) {
      setUnreadCount(prev => prev + 1)
    }

    // Show browser notification for critical alerts
    if (notification.severity === 'critical' && 'Notification' in window) {
      if (Notification.permission === 'granted') {
        new Notification(notification.title, {
          body: notification.message,
          icon: '/favicon.ico',
          tag: newNotification.id
        })
      } else if (Notification.permission !== 'denied') {
        Notification.requestPermission().then(permission => {
          if (permission === 'granted') {
            new Notification(notification.title, {
              body: notification.message,
              icon: '/favicon.ico',
              tag: newNotification.id
            })
          }
        })
      }
    }
  }, [])

  const updatePreferences = useCallback(async (newPreferences: Partial<NotificationPreferences>) => {
    setIsLoading(true)
    try {
      // In real implementation, save to backend
      // await api.put('/user/notification-preferences', newPreferences)

      setPreferences(prev => ({ ...prev, ...newPreferences }))
    } catch (error) {
      console.error('Failed to update notification preferences:', error)
    } finally {
      setIsLoading(false)
    }
  }, [])

  const getNotificationsByType = useCallback((type: Notification['type']) => {
    return notifications.filter(n => n.type === type)
  }, [notifications])

  const getUnreadNotifications = useCallback(() => {
    return notifications.filter(n => !n.read)
  }, [notifications])

  const getFraudAlertNotifications = useCallback(() => {
    return notifications.filter(n => n.type === 'FRAUD_ALERT' || n.type === 'RISK_ASSESSMENT')
  }, [notifications])

  return {
    notifications,
    unreadCount,
    isLoading,
    preferences,
    markAsRead,
    markAllAsRead,
    deleteNotification,
    addNotification,
    updatePreferences,
    getNotificationsByType,
    getUnreadNotifications,
    getFraudAlertNotifications
  }
}