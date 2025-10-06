import Keycloak from 'keycloak-js'
import configService from '@/config/runtime-config'

/**
 * AuthService - Handles both SSO and embedded authentication
 */
class AuthService {
  private keycloak: Keycloak | null = null

  constructor() {
    this.keycloak = new Keycloak({
      url: configService.keycloakUrl,
      realm: configService.keycloakRealm,
      clientId: configService.keycloakClientId,
    })
  }

  /**
   * Direct login using username/password (Resource Owner Password Credentials Grant)
   * Note: This is less secure than the authorization code flow but provides embedded login
   */
  async loginWithCredentials(username: string, password: string): Promise<boolean> {
    try {
      const tokenUrl = `${this.keycloak?.authServerUrl}/realms/${this.keycloak?.realm}/protocol/openid-connect/token`

      const response = await fetch(tokenUrl, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/x-www-form-urlencoded',
        },
        body: new URLSearchParams({
          grant_type: 'password',
          client_id: this.keycloak?.clientId || '',
          username,
          password,
          scope: 'openid profile email',
        }),
      })

      if (response.ok) {
        const tokenData = await response.json()

        // Store tokens
        localStorage.setItem('access_token', tokenData.access_token)
        localStorage.setItem('refresh_token', tokenData.refresh_token)
        localStorage.setItem('id_token', tokenData.id_token)

        // Initialize Keycloak with the obtained tokens
        if (this.keycloak) {
          this.keycloak.token = tokenData.access_token
          this.keycloak.refreshToken = tokenData.refresh_token
          this.keycloak.idToken = tokenData.id_token
          this.keycloak.authenticated = true
          this.keycloak.tokenParsed = JSON.parse(atob(tokenData.access_token.split('.')[1]))
          this.keycloak.refreshTokenParsed = JSON.parse(atob(tokenData.refresh_token.split('.')[1]))
        }

        // Set up token refresh
        this.setupTokenRefresh()

        return true
      } else {
        const errorData = await response.json()
        console.error('Login failed:', errorData)
        return false
      }
    } catch (error) {
      console.error('Login error:', error)
      return false
    }
  }

  /**
   * Refresh access token using refresh token
   */
  async refreshToken(): Promise<boolean> {
    try {
      const refreshToken = localStorage.getItem('refresh_token')
      if (!refreshToken) return false

      const tokenUrl = `${this.keycloak?.authServerUrl}/realms/${this.keycloak?.realm}/protocol/openid-connect/token`

      const response = await fetch(tokenUrl, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/x-www-form-urlencoded',
        },
        body: new URLSearchParams({
          grant_type: 'refresh_token',
          client_id: this.keycloak?.clientId || '',
          refresh_token: refreshToken,
        }),
      })

      if (response.ok) {
        const tokenData = await response.json()

        // Update stored tokens
        localStorage.setItem('access_token', tokenData.access_token)
        localStorage.setItem('refresh_token', tokenData.refresh_token)
        localStorage.setItem('id_token', tokenData.id_token)

        // Update Keycloak instance
        if (this.keycloak) {
          this.keycloak.token = tokenData.access_token
          this.keycloak.refreshToken = tokenData.refresh_token
          this.keycloak.idToken = tokenData.id_token
          this.keycloak.tokenParsed = JSON.parse(atob(tokenData.access_token.split('.')[1]))
        }

        return true
      }

      return false
    } catch (error) {
      console.error('Token refresh error:', error)
      return false
    }
  }

  /**
   * Set up automatic token refresh
   */
  private setupTokenRefresh(): void {
    // Refresh token 1 minute before expiry
    const refreshInterval = setInterval(async () => {
      const success = await this.refreshToken()
      if (!success) {
        clearInterval(refreshInterval)
        this.logout()
      }
    }, 60000) // Check every minute
  }

  /**
   * Check if user is authenticated
   */
  isAuthenticated(): boolean {
    const token = localStorage.getItem('access_token')
    if (!token) return false

    try {
      const tokenParsed = JSON.parse(atob(token.split('.')[1]))
      const currentTime = Date.now() / 1000
      return tokenParsed.exp > currentTime
    } catch {
      return false
    }
  }

  /**
   * Get current user info from token
   */
  getUserInfo() {
    const token = localStorage.getItem('access_token')
    if (!token) return null

    try {
      const tokenParsed = JSON.parse(atob(token.split('.')[1]))
      return {
        id: tokenParsed.sub,
        username: tokenParsed.preferred_username || tokenParsed.email,
        email: tokenParsed.email,
        firstName: tokenParsed.given_name,
        lastName: tokenParsed.family_name,
        roles: [
          ...(tokenParsed.realm_access?.roles || []),
          ...(tokenParsed.resource_access?.[this.keycloak?.clientId || '']?.roles || [])
        ],
      }
    } catch {
      return null
    }
  }

  /**
   * Logout user
   */
  logout(): void {
    localStorage.removeItem('access_token')
    localStorage.removeItem('refresh_token')
    localStorage.removeItem('id_token')

    if (this.keycloak) {
      this.keycloak.authenticated = false
      this.keycloak.token = undefined
      this.keycloak.refreshToken = undefined
      this.keycloak.idToken = undefined
    }
  }

  /**
   * Get Keycloak instance for SSO login
   */
  getKeycloak(): Keycloak | null {
    return this.keycloak
  }
}

export default new AuthService()