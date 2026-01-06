import apiService from './api'

export interface UpdateCheckResponse {
  currentVersion: string
  latestVersion?: string
  updateAvailable: boolean
  checkedAt: string
  releaseDate?: string
  downloadUrls?: Record<string, string>
  releaseNotesUrl?: string
  status: 'SUCCESS' | 'ERROR'
  errorMessage?: string
}

class UpdateService {
  private readonly BASE_URL = '/api/updates'

  /**
   * Manually trigger an update check
   */
  async checkForUpdates(): Promise<UpdateCheckResponse> {
    return apiService.post<UpdateCheckResponse>(`${this.BASE_URL}/check`, {})
  }

  /**
   * Get cached update status without triggering a new check
   */
  async getUpdateStatus(): Promise<UpdateCheckResponse | null> {
    try {
      return await apiService.get<UpdateCheckResponse>(`${this.BASE_URL}/status`)
    } catch (error: any) {
      // Return null if no cached status exists (204 No Content)
      if (error.response?.status === 204) {
        return null
      }
      throw error
    }
  }
}

export default new UpdateService()
