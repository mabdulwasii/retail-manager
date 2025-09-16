import React, { useState } from 'react'
import { Button } from '@/components/ui/button'
import { Badge } from '@/components/ui/badge'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import { Dialog, DialogContent, DialogHeader, DialogTitle, DialogTrigger } from '@/components/ui/dialog'
import { ScrollArea } from '@/components/ui/scroll-area'
import { Separator } from '@/components/ui/separator'
import { useNotifications, Notification } from '@/hooks/useNotifications'
import {
  BellIcon,
  ShieldAlertIcon,
  AlertTriangleIcon,
  InfoIcon,
  XIcon,
  EyeIcon,
  CheckIcon,
  ExternalLinkIcon
} from 'lucide-react'
import { formatDistanceToNow } from 'date-fns'

export const NotificationBell: React.FC = () => {
  const {
    notifications,
    unreadCount,
    markAsRead,
    markAllAsRead,
    deleteNotification
  } = useNotifications()

  const [isDialogOpen, setIsDialogOpen] = useState(false)

  const getNotificationIcon = (type: Notification['type'], severity: Notification['severity']) => {
    switch (type) {
      case 'FRAUD_ALERT':
        return <ShieldAlertIcon className={`h-5 w-5 ${getSeverityColor(severity)}`} />
      case 'RISK_ASSESSMENT':
        return <AlertTriangleIcon className={`h-5 w-5 ${getSeverityColor(severity)}`} />
      default:
        return <InfoIcon className={`h-5 w-5 ${getSeverityColor(severity)}`} />
    }
  }

  const getSeverityColor = (severity: Notification['severity']) => {
    switch (severity) {
      case 'critical': return 'text-red-600'
      case 'high': return 'text-orange-600'
      case 'medium': return 'text-yellow-600'
      case 'low': return 'text-blue-600'
      default: return 'text-gray-600'
    }
  }

  const getSeverityBadgeColor = (severity: Notification['severity']) => {
    switch (severity) {
      case 'critical': return 'bg-red-100 text-red-800 border-red-200'
      case 'high': return 'bg-orange-100 text-orange-800 border-orange-200'
      case 'medium': return 'bg-yellow-100 text-yellow-800 border-yellow-200'
      case 'low': return 'bg-blue-100 text-blue-800 border-blue-200'
      default: return 'bg-gray-100 text-gray-800 border-gray-200'
    }
  }

  const handleNotificationClick = (notification: Notification) => {
    if (!notification.read) {
      markAsRead(notification.id)
    }

    if (notification.actionUrl) {
      // In a real implementation, use router navigation
      console.log('Navigate to:', notification.actionUrl)
      setIsDialogOpen(false)
    }
  }

  const handleMarkAllRead = () => {
    markAllAsRead()
  }

  const handleDeleteNotification = (e: React.MouseEvent, notificationId: string) => {
    e.stopPropagation()
    deleteNotification(notificationId)
  }

  const criticalNotifications = notifications.filter(n => n.severity === 'critical' && !n.read)
  const hasCriticalAlerts = criticalNotifications.length > 0

  return (
    <Dialog open={isDialogOpen} onOpenChange={setIsDialogOpen}>
      <DialogTrigger asChild>
        <Button variant="ghost" size="sm" className="relative">
          <BellIcon className={`h-5 w-5 ${hasCriticalAlerts ? 'text-red-600' : 'text-gray-600'}`} />
          {unreadCount > 0 && (
            <Badge
              className={`absolute -top-1 -right-1 px-1 py-0 text-xs min-w-[1.2rem] h-5 flex items-center justify-center ${
                hasCriticalAlerts
                  ? 'bg-red-600 text-white'
                  : 'bg-blue-600 text-white'
              }`}
            >
              {unreadCount > 99 ? '99+' : unreadCount}
            </Badge>
          )}
        </Button>
      </DialogTrigger>

      <DialogContent className="max-w-md max-h-[80vh] p-0">
        <DialogHeader className="p-6 pb-4">
          <div className="flex items-center justify-between">
            <DialogTitle className="flex items-center space-x-2">
              <BellIcon className="h-5 w-5" />
              <span>Notifications</span>
              {unreadCount > 0 && (
                <Badge className="bg-blue-100 text-blue-800">
                  {unreadCount} new
                </Badge>
              )}
            </DialogTitle>
            {unreadCount > 0 && (
              <Button
                variant="ghost"
                size="sm"
                onClick={handleMarkAllRead}
                className="text-blue-600 hover:text-blue-700"
              >
                <CheckIcon className="h-4 w-4 mr-1" />
                Mark all read
              </Button>
            )}
          </div>
        </DialogHeader>

        <Separator />

        <ScrollArea className="flex-1 max-h-96">
          <div className="p-4 space-y-3">
            {notifications.length === 0 ? (
              <div className="text-center py-8 text-gray-500">
                <BellIcon className="h-12 w-12 mx-auto mb-3 text-gray-300" />
                <p>No notifications</p>
                <p className="text-sm">You're all caught up!</p>
              </div>
            ) : (
              notifications.map((notification) => (
                <Card
                  key={notification.id}
                  className={`cursor-pointer transition-colors ${
                    notification.read
                      ? 'bg-gray-50 hover:bg-gray-100'
                      : 'bg-white hover:bg-blue-50 border-blue-200'
                  }`}
                  onClick={() => handleNotificationClick(notification)}
                >
                  <CardContent className="p-4">
                    <div className="flex items-start space-x-3">
                      <div className="flex-shrink-0 mt-0.5">
                        {getNotificationIcon(notification.type, notification.severity)}
                      </div>

                      <div className="flex-1 min-w-0">
                        <div className="flex items-start justify-between">
                          <div className="flex-1">
                            <div className="flex items-center space-x-2 mb-1">
                              <h4 className={`text-sm font-medium ${
                                notification.read ? 'text-gray-700' : 'text-gray-900'
                              }`}>
                                {notification.title}
                              </h4>
                              <Badge className={getSeverityBadgeColor(notification.severity)}>
                                {notification.severity}
                              </Badge>
                            </div>

                            <p className={`text-sm ${
                              notification.read ? 'text-gray-500' : 'text-gray-700'
                            }`}>
                              {notification.message}
                            </p>

                            <div className="flex items-center justify-between mt-2">
                              <span className="text-xs text-gray-400">
                                {formatDistanceToNow(notification.timestamp, { addSuffix: true })}
                              </span>

                              <div className="flex items-center space-x-1">
                                {notification.actionUrl && (
                                  <ExternalLinkIcon className="h-3 w-3 text-gray-400" />
                                )}
                                {!notification.read && (
                                  <div className="w-2 h-2 bg-blue-600 rounded-full"></div>
                                )}
                              </div>
                            </div>
                          </div>

                          <Button
                            variant="ghost"
                            size="sm"
                            onClick={(e) => handleDeleteNotification(e, notification.id)}
                            className="ml-2 p-1 h-6 w-6 text-gray-400 hover:text-gray-600"
                          >
                            <XIcon className="h-3 w-3" />
                          </Button>
                        </div>
                      </div>
                    </div>

                    {/* Show metadata for fraud alerts */}
                    {notification.type === 'FRAUD_ALERT' && notification.metadata && (
                      <div className="mt-3 pt-3 border-t border-gray-200">
                        <div className="grid grid-cols-2 gap-2 text-xs">
                          {notification.metadata.alertId && (
                            <div>
                              <span className="font-medium text-gray-500">Alert ID:</span>
                              <span className="ml-1 text-gray-700">{notification.metadata.alertId}</span>
                            </div>
                          )}
                          {notification.metadata.riskScore && (
                            <div>
                              <span className="font-medium text-gray-500">Risk Score:</span>
                              <span className="ml-1 text-gray-700">{notification.metadata.riskScore}</span>
                            </div>
                          )}
                        </div>
                      </div>
                    )}
                  </CardContent>
                </Card>
              ))
            )}
          </div>
        </ScrollArea>

        {notifications.length > 0 && (
          <>
            <Separator />
            <div className="p-4">
              <Button
                variant="outline"
                size="sm"
                className="w-full"
                onClick={() => {
                  setIsDialogOpen(false)
                  // Navigate to notifications page
                  console.log('Navigate to all notifications')
                }}
              >
                <EyeIcon className="h-4 w-4 mr-2" />
                View All Notifications
              </Button>
            </div>
          </>
        )}
      </DialogContent>
    </Dialog>
  )
}