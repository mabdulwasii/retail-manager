import { useState, useEffect, useCallback } from 'react'
import updateService, { UpdateCheckResponse } from '../services/updateService'

const POLL_INTERVAL = 30 * 60 * 1000 // 30 minutes in milliseconds
const DISMISS_KEY = 'update-notification-dismissed'
const DISMISS_DURATION = 7 * 24 * 60 * 60 * 1000 // 7 days in milliseconds

interface UseUpdateCheckReturn {
  updateStatus: UpdateCheckResponse | null
  loading: boolean
  error: string | null
  isDismissed: boolean
  checkForUpdates: () => Promise<void>
  dismissNotification: () => void
}

export function useUpdateCheck(): UseUpdateCheckReturn {
  const [updateStatus, setUpdateStatus] = useState<UpdateCheckResponse | null>(null)
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const [isDismissed, setIsDismissed] = useState(false)

  // Check if notification has been dismissed recently
  const checkDismissedStatus = useCallback(() => {
    const dismissedData = localStorage.getItem(DISMISS_KEY)
    if (!dismissedData) return false

    try {
      const { version, timestamp } = JSON.parse(dismissedData)
      const now = Date.now()

      // Check if dismissed within the last 7 days
      if (now - timestamp < DISMISS_DURATION) {
        // If there's a new update status and it's a different version, show notification
        if (updateStatus?.latestVersion && version !== updateStatus.latestVersion) {
          return false
        }
        return true
      }

      // Expired, remove from storage
      localStorage.removeItem(DISMISS_KEY)
      return false
    } catch {
      localStorage.removeItem(DISMISS_KEY)
      return false
    }
  }, [updateStatus?.latestVersion])

  // Fetch cached update status
  const fetchCachedStatus = useCallback(async () => {
    try {
      setLoading(true)
      setError(null)
      const status = await updateService.getUpdateStatus()
      setUpdateStatus(status)
    } catch (err: any) {
      setError(err.message || 'Failed to check for updates')
    } finally {
      setLoading(false)
    }
  }, [])

  // Manually trigger update check
  const checkForUpdates = useCallback(async () => {
    try {
      setLoading(true)
      setError(null)
      const status = await updateService.checkForUpdates()
      setUpdateStatus(status)

      // Clear dismissed state if checking manually
      localStorage.removeItem(DISMISS_KEY)
      setIsDismissed(false)
    } catch (err: any) {
      setError(err.message || 'Failed to check for updates')
    } finally {
      setLoading(false)
    }
  }, [])

  // Dismiss notification
  const dismissNotification = useCallback(() => {
    if (updateStatus?.latestVersion) {
      const dismissData = {
        version: updateStatus.latestVersion,
        timestamp: Date.now()
      }
      localStorage.setItem(DISMISS_KEY, JSON.stringify(dismissData))
      setIsDismissed(true)
    }
  }, [updateStatus?.latestVersion])

  // Initial fetch and periodic polling
  useEffect(() => {
    // Fetch immediately
    fetchCachedStatus()

    // Set up polling every 30 minutes
    const intervalId = setInterval(fetchCachedStatus, POLL_INTERVAL)

    return () => clearInterval(intervalId)
  }, [fetchCachedStatus])

  // Update dismissed status when update status changes
  useEffect(() => {
    setIsDismissed(checkDismissedStatus())
  }, [checkDismissedStatus])

  return {
    updateStatus,
    loading,
    error,
    isDismissed,
    checkForUpdates,
    dismissNotification
  }
}
