import React from 'react'
import { Alert, AlertDescription } from '../ui/alert'
import { Button } from '../ui/button'
import { X, Download, ExternalLink } from 'lucide-react'
import { useUpdateCheck } from '../../hooks/useUpdateCheck'

interface UpdateNotificationBannerProps {
  className?: string
}

export function UpdateNotificationBanner({ className }: UpdateNotificationBannerProps) {
  const { updateStatus, isDismissed, dismissNotification } = useUpdateCheck()

  // Don't show if no update available, already dismissed, or error
  if (
    !updateStatus?.updateAvailable ||
    isDismissed ||
    updateStatus.status === 'ERROR'
  ) {
    return null
  }

  const handleDownload = () => {
    // Determine current platform
    const platform = getPlatform()
    const downloadUrl = updateStatus.downloadUrls?.[platform]

    if (downloadUrl) {
      window.open(downloadUrl, '_blank')
    }
  }

  const handleViewReleaseNotes = () => {
    if (updateStatus.releaseNotesUrl) {
      window.open(updateStatus.releaseNotesUrl, '_blank')
    }
  }

  return (
    <Alert
      className={`border-blue-200 bg-blue-50 dark:bg-blue-950 dark:border-blue-800 ${className || ''}`}
      data-testid="update-notification-banner"
    >
      <div className="flex items-center justify-between gap-4">
        <AlertDescription className="flex-1">
          <div className="flex items-center gap-2">
            <Download className="h-4 w-4 text-blue-600 dark:text-blue-400" />
            <span className="font-medium text-blue-900 dark:text-blue-100">
              Update Available:
            </span>
            <span className="text-blue-700 dark:text-blue-300">
              Version {updateStatus.latestVersion} is now available
              {updateStatus.releaseDate && ` (released ${new Date(updateStatus.releaseDate).toLocaleDateString()})`}
            </span>
          </div>
        </AlertDescription>

        <div className="flex items-center gap-2">
          {updateStatus.releaseNotesUrl && (
            <Button
              variant="ghost"
              size="sm"
              onClick={handleViewReleaseNotes}
              className="text-blue-700 hover:text-blue-900 dark:text-blue-300 dark:hover:text-blue-100"
              data-testid="view-release-notes-button"
            >
              <ExternalLink className="h-4 w-4 mr-1" />
              Release Notes
            </Button>
          )}

          <Button
            variant="default"
            size="sm"
            onClick={handleDownload}
            className="bg-blue-600 hover:bg-blue-700 text-white"
            data-testid="download-update-button"
          >
            <Download className="h-4 w-4 mr-1" />
            Download
          </Button>

          <Button
            variant="ghost"
            size="sm"
            onClick={dismissNotification}
            className="text-blue-600 hover:text-blue-800 dark:text-blue-400 dark:hover:text-blue-200"
            data-testid="dismiss-button"
          >
            <X className="h-4 w-4" />
          </Button>
        </div>
      </div>
    </Alert>
  )
}

/**
 * Detect the current platform for download URL selection
 */
function getPlatform(): string {
  const userAgent = window.navigator.userAgent.toLowerCase()

  if (userAgent.includes('win')) {
    return 'windows'
  } else if (userAgent.includes('mac')) {
    return 'macos'
  } else if (userAgent.includes('linux')) {
    // Prefer .deb for Debian/Ubuntu-based systems
    return 'linux_deb'
  }

  // Default to windows if unknown
  return 'windows'
}
