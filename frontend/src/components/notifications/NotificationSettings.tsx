import React from 'react'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import { Label } from '@/components/ui/label'
import { Switch } from '@/components/ui/switch'
import { Button } from '@/components/ui/button'
import { Separator } from '@/components/ui/separator'
import { LoadingSpinner } from '@/components/ui/loading-spinner'
import { useNotifications } from '@/hooks/useNotifications'
import {
  BellIcon,
  MailIcon,
  MessageSquareIcon,
  SmartphoneIcon,
  ShieldAlertIcon,
  AlertTriangleIcon,
  InfoIcon,
  CheckIcon
} from 'lucide-react'

export const NotificationSettings: React.FC = () => {
  const { preferences, updatePreferences, isLoading } = useNotifications()

  const handlePreferenceChange = async (key: keyof typeof preferences, value: boolean) => {
    try {
      await updatePreferences({ [key]: value })
    } catch (error) {
      console.error('Failed to update notification preference:', error)
    }
  }

  const handleTestNotification = () => {
    // Test browser notification permission
    if ('Notification' in window) {
      if (Notification.permission === 'granted') {
        new Notification('Test Notification', {
          body: 'This is a test notification from Shop Manager fraud detection system.',
          icon: '/favicon.ico'
        })
      } else if (Notification.permission !== 'denied') {
        Notification.requestPermission().then(permission => {
          if (permission === 'granted') {
            new Notification('Test Notification', {
              body: 'Browser notifications are now enabled for Shop Manager.',
              icon: '/favicon.ico'
            })
          }
        })
      } else {
        alert('Browser notifications are blocked. Please enable them in your browser settings.')
      }
    } else {
      alert('Browser notifications are not supported in this browser.')
    }
  }

  return (
    <div className="space-y-6">
      <div>
        <h2 className="text-2xl font-bold flex items-center space-x-2">
          <BellIcon className="h-6 w-6 text-blue-600" />
          <span>Notification Settings</span>
        </h2>
        <p className="text-gray-600 mt-1">
          Configure how you receive fraud alerts and system notifications
        </p>
      </div>

      {/* Notification Channels */}
      <Card>
        <CardHeader>
          <CardTitle className="flex items-center space-x-2">
            <SmartphoneIcon className="h-5 w-5" />
            <span>Notification Channels</span>
          </CardTitle>
        </CardHeader>
        <CardContent className="space-y-6">
          {/* Email Notifications */}
          <div className="flex items-center justify-between">
            <div className="flex items-center space-x-3">
              <MailIcon className="h-5 w-5 text-blue-600" />
              <div>
                <Label className="text-sm font-medium">Email Notifications</Label>
                <p className="text-xs text-gray-500">Receive notifications via email</p>
              </div>
            </div>
            <Switch
              checked={preferences.emailEnabled}
              onCheckedChange={(checked) => handlePreferenceChange('emailEnabled', checked)}
              disabled={isLoading}
            />
          </div>

          <Separator />

          {/* SMS Notifications */}
          <div className="flex items-center justify-between">
            <div className="flex items-center space-x-3">
              <MessageSquareIcon className="h-5 w-5 text-green-600" />
              <div>
                <Label className="text-sm font-medium">SMS Notifications</Label>
                <p className="text-xs text-gray-500">Receive critical alerts via SMS</p>
              </div>
            </div>
            <Switch
              checked={preferences.smsEnabled}
              onCheckedChange={(checked) => handlePreferenceChange('smsEnabled', checked)}
              disabled={isLoading}
            />
          </div>

          <Separator />

          {/* Push Notifications */}
          <div className="flex items-center justify-between">
            <div className="flex items-center space-x-3">
              <BellIcon className="h-5 w-5 text-purple-600" />
              <div>
                <Label className="text-sm font-medium">Browser Push Notifications</Label>
                <p className="text-xs text-gray-500">Receive instant browser notifications</p>
              </div>
            </div>
            <div className="flex items-center space-x-2">
              <Switch
                checked={preferences.pushEnabled}
                onCheckedChange={(checked) => handlePreferenceChange('pushEnabled', checked)}
                disabled={isLoading}
              />
              <Button
                variant="outline"
                size="sm"
                onClick={handleTestNotification}
                className="text-xs"
              >
                Test
              </Button>
            </div>
          </div>
        </CardContent>
      </Card>

      {/* Notification Types */}
      <Card>
        <CardHeader>
          <CardTitle className="flex items-center space-x-2">
            <AlertTriangleIcon className="h-5 w-5" />
            <span>Notification Types</span>
          </CardTitle>
        </CardHeader>
        <CardContent className="space-y-6">
          {/* Fraud Alerts */}
          <div className="flex items-center justify-between">
            <div className="flex items-center space-x-3">
              <ShieldAlertIcon className="h-5 w-5 text-red-600" />
              <div>
                <Label className="text-sm font-medium">Fraud Alerts</Label>
                <p className="text-xs text-gray-500">
                  Suspicious transactions and security threats
                </p>
              </div>
            </div>
            <Switch
              checked={preferences.fraudAlerts}
              onCheckedChange={(checked) => handlePreferenceChange('fraudAlerts', checked)}
              disabled={isLoading}
            />
          </div>

          <Separator />

          {/* Risk Assessments */}
          <div className="flex items-center justify-between">
            <div className="flex items-center space-x-3">
              <AlertTriangleIcon className="h-5 w-5 text-orange-600" />
              <div>
                <Label className="text-sm font-medium">Risk Assessments</Label>
                <p className="text-xs text-gray-500">
                  High-risk transactions requiring manual review
                </p>
              </div>
            </div>
            <Switch
              checked={preferences.riskAssessments}
              onCheckedChange={(checked) => handlePreferenceChange('riskAssessments', checked)}
              disabled={isLoading}
            />
          </div>

          <Separator />

          {/* System Updates */}
          <div className="flex items-center justify-between">
            <div className="flex items-center space-x-3">
              <InfoIcon className="h-5 w-5 text-blue-600" />
              <div>
                <Label className="text-sm font-medium">System Updates</Label>
                <p className="text-xs text-gray-500">
                  System maintenance and feature updates
                </p>
              </div>
            </div>
            <Switch
              checked={preferences.systemUpdates}
              onCheckedChange={(checked) => handlePreferenceChange('systemUpdates', checked)}
              disabled={isLoading}
            />
          </div>
        </CardContent>
      </Card>

      {/* Notification Priority Guide */}
      <Card>
        <CardHeader>
          <CardTitle className="flex items-center space-x-2">
            <InfoIcon className="h-5 w-5" />
            <span>Priority Levels</span>
          </CardTitle>
        </CardHeader>
        <CardContent>
          <div className="space-y-4">
            <div className="flex items-center space-x-3">
              <div className="w-3 h-3 bg-red-500 rounded-full"></div>
              <div>
                <Label className="text-sm font-medium text-red-700">Critical</Label>
                <p className="text-xs text-gray-500">
                  Immediate action required - sent via all enabled channels
                </p>
              </div>
            </div>

            <div className="flex items-center space-x-3">
              <div className="w-3 h-3 bg-orange-500 rounded-full"></div>
              <div>
                <Label className="text-sm font-medium text-orange-700">High</Label>
                <p className="text-xs text-gray-500">
                  Review within 1 hour - email and push notifications
                </p>
              </div>
            </div>

            <div className="flex items-center space-x-3">
              <div className="w-3 h-3 bg-yellow-500 rounded-full"></div>
              <div>
                <Label className="text-sm font-medium text-yellow-700">Medium</Label>
                <p className="text-xs text-gray-500">
                  Review within 4 hours - email notifications only
                </p>
              </div>
            </div>

            <div className="flex items-center space-x-3">
              <div className="w-3 h-3 bg-blue-500 rounded-full"></div>
              <div>
                <Label className="text-sm font-medium text-blue-700">Low</Label>
                <p className="text-xs text-gray-500">
                  Informational - logged but no immediate notifications
                </p>
              </div>
            </div>
          </div>
        </CardContent>
      </Card>

      {/* Save Button */}
      <div className="flex justify-end space-x-3">
        <Button
          variant="outline"
          onClick={() => window.location.reload()}
          disabled={isLoading}
        >
          Reset to Defaults
        </Button>
        <Button disabled={isLoading}>
          {isLoading ? (
            <>
              <LoadingSpinner size="sm" />
              <span className="ml-2">Saving...</span>
            </>
          ) : (
            <>
              <CheckIcon className="h-4 w-4 mr-2" />
              Settings Saved
            </>
          )}
        </Button>
      </div>
    </div>
  )
}