import React from 'react'
import { render, screen, fireEvent } from '@testing-library/react'
import { UpdateNotificationBanner } from '../UpdateNotificationBanner'
import { useUpdateCheck } from '../../../hooks/useUpdateCheck'

// Mock the useUpdateCheck hook
jest.mock('../../../hooks/useUpdateCheck')

const mockUseUpdateCheck = useUpdateCheck as jest.MockedFunction<typeof useUpdateCheck>

// Mock window.open
const mockWindowOpen = jest.fn()
global.window.open = mockWindowOpen

describe('UpdateNotificationBanner', () => {
  beforeEach(() => {
    jest.clearAllMocks()
  })

  it('should not render when no update is available', () => {
    mockUseUpdateCheck.mockReturnValue({
      updateStatus: {
        currentVersion: '0.1.28',
        latestVersion: '0.1.28',
        updateAvailable: false,
        checkedAt: '2026-01-06T10:00:00',
        status: 'SUCCESS'
      },
      loading: false,
      error: null,
      isDismissed: false,
      checkForUpdates: jest.fn(),
      dismissNotification: jest.fn()
    })

    const { container } = render(<UpdateNotificationBanner />)
    expect(container.firstChild).toBeNull()
  })

  it('should not render when notification is dismissed', () => {
    mockUseUpdateCheck.mockReturnValue({
      updateStatus: {
        currentVersion: '0.1.28',
        latestVersion: '0.1.29',
        updateAvailable: true,
        checkedAt: '2026-01-06T10:00:00',
        status: 'SUCCESS'
      },
      loading: false,
      error: null,
      isDismissed: true,
      checkForUpdates: jest.fn(),
      dismissNotification: jest.fn()
    })

    const { container } = render(<UpdateNotificationBanner />)
    expect(container.firstChild).toBeNull()
  })

  it('should not render when status is ERROR', () => {
    mockUseUpdateCheck.mockReturnValue({
      updateStatus: {
        currentVersion: '0.1.28',
        latestVersion: undefined,
        updateAvailable: false,
        checkedAt: '2026-01-06T10:00:00',
        status: 'ERROR',
        errorMessage: 'Failed to check updates'
      },
      loading: false,
      error: null,
      isDismissed: false,
      checkForUpdates: jest.fn(),
      dismissNotification: jest.fn()
    })

    const { container } = render(<UpdateNotificationBanner />)
    expect(container.firstChild).toBeNull()
  })

  it('should render when update is available and not dismissed', () => {
    mockUseUpdateCheck.mockReturnValue({
      updateStatus: {
        currentVersion: '0.1.28',
        latestVersion: '0.1.29',
        updateAvailable: true,
        checkedAt: '2026-01-06T10:00:00',
        releaseDate: '2026-01-06',
        status: 'SUCCESS',
        downloadUrls: {
          windows: 'https://test.com/windows.exe',
          macos: 'https://test.com/macos.dmg'
        },
        releaseNotesUrl: 'https://github.com/test/releases/tag/v0.1.29'
      },
      loading: false,
      error: null,
      isDismissed: false,
      checkForUpdates: jest.fn(),
      dismissNotification: jest.fn()
    })

    render(<UpdateNotificationBanner />)

    expect(screen.getByTestId('update-notification-banner')).toBeInTheDocument()
    expect(screen.getByText(/Version 0.1.29 is now available/i)).toBeInTheDocument()
  })

  it('should show release date when available', () => {
    mockUseUpdateCheck.mockReturnValue({
      updateStatus: {
        currentVersion: '0.1.28',
        latestVersion: '0.1.29',
        updateAvailable: true,
        checkedAt: '2026-01-06T10:00:00',
        releaseDate: '2026-01-06',
        status: 'SUCCESS'
      },
      loading: false,
      error: null,
      isDismissed: false,
      checkForUpdates: jest.fn(),
      dismissNotification: jest.fn()
    })

    render(<UpdateNotificationBanner />)

    expect(screen.getByText(/released/i)).toBeInTheDocument()
  })

  it('should call dismissNotification when dismiss button is clicked', () => {
    const mockDismiss = jest.fn()

    mockUseUpdateCheck.mockReturnValue({
      updateStatus: {
        currentVersion: '0.1.28',
        latestVersion: '0.1.29',
        updateAvailable: true,
        checkedAt: '2026-01-06T10:00:00',
        status: 'SUCCESS'
      },
      loading: false,
      error: null,
      isDismissed: false,
      checkForUpdates: jest.fn(),
      dismissNotification: mockDismiss
    })

    render(<UpdateNotificationBanner />)

    const dismissButton = screen.getByTestId('dismiss-button')
    fireEvent.click(dismissButton)

    expect(mockDismiss).toHaveBeenCalledTimes(1)
  })

  it('should open download URL when download button is clicked', () => {
    mockUseUpdateCheck.mockReturnValue({
      updateStatus: {
        currentVersion: '0.1.28',
        latestVersion: '0.1.29',
        updateAvailable: true,
        checkedAt: '2026-01-06T10:00:00',
        status: 'SUCCESS',
        downloadUrls: {
          windows: 'https://test.com/windows.exe',
          macos: 'https://test.com/macos.dmg',
          linux_deb: 'https://test.com/linux.deb'
        }
      },
      loading: false,
      error: null,
      isDismissed: false,
      checkForUpdates: jest.fn(),
      dismissNotification: jest.fn()
    })

    render(<UpdateNotificationBanner />)

    const downloadButton = screen.getByTestId('download-update-button')
    fireEvent.click(downloadButton)

    expect(mockWindowOpen).toHaveBeenCalledTimes(1)
    expect(mockWindowOpen).toHaveBeenCalledWith(expect.any(String), '_blank')
  })

  it('should open release notes when release notes button is clicked', () => {
    mockUseUpdateCheck.mockReturnValue({
      updateStatus: {
        currentVersion: '0.1.28',
        latestVersion: '0.1.29',
        updateAvailable: true,
        checkedAt: '2026-01-06T10:00:00',
        status: 'SUCCESS',
        releaseNotesUrl: 'https://github.com/test/releases/tag/v0.1.29'
      },
      loading: false,
      error: null,
      isDismissed: false,
      checkForUpdates: jest.fn(),
      dismissNotification: jest.fn()
    })

    render(<UpdateNotificationBanner />)

    const releaseNotesButton = screen.getByTestId('view-release-notes-button')
    fireEvent.click(releaseNotesButton)

    expect(mockWindowOpen).toHaveBeenCalledTimes(1)
    expect(mockWindowOpen).toHaveBeenCalledWith('https://github.com/test/releases/tag/v0.1.29', '_blank')
  })

  it('should not show release notes button when URL is not available', () => {
    mockUseUpdateCheck.mockReturnValue({
      updateStatus: {
        currentVersion: '0.1.28',
        latestVersion: '0.1.29',
        updateAvailable: true,
        checkedAt: '2026-01-06T10:00:00',
        status: 'SUCCESS'
      },
      loading: false,
      error: null,
      isDismissed: false,
      checkForUpdates: jest.fn(),
      dismissNotification: jest.fn()
    })

    render(<UpdateNotificationBanner />)

    expect(screen.queryByTestId('view-release-notes-button')).not.toBeInTheDocument()
  })
})
