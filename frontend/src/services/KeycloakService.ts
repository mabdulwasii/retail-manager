import Keycloak from 'keycloak-js'
import configService from '@/config/runtime-config'

/**
 * KeycloakService - Centralized service for Keycloak authentication
 * Provides a clean interface for authentication operations
 */
class KeycloakService {
  private static instance: KeycloakService
  private keycloak: Keycloak | null = null
  private initialized = false

  private constructor() {}

  public static getInstance(): KeycloakService {
    if (!KeycloakService.instance) {
      KeycloakService.instance = new KeycloakService()
    }
    return KeycloakService.instance
  }

  /**
   * Initialize Keycloak with configuration from runtime config
   */
  public init(): Keycloak {
    if (!this.keycloak) {
      this.keycloak = new Keycloak({
        url: configService.keycloakUrl,
        realm: configService.keycloakRealm,
        clientId: configService.keycloakClientId,
      })
    }
    return this.keycloak
  }

  /**
   * Get the Keycloak instance
   */
  public getKeycloak(): Keycloak | null {
    return this.keycloak
  }

  /**
   * Check if Keycloak is initialized
   */
  public isInitialized(): boolean {
    return this.initialized
  }

  /**
   * Set initialization status
   */
  public setInitialized(status: boolean): void {
    this.initialized = status
  }

  /**
   * Get user profile information
   */
  public getUserProfile() {
    if (!this.keycloak?.tokenParsed) {
      return null
    }

    const token = this.keycloak.tokenParsed as any

    return {
      id: token.sub,
      username: token.preferred_username || token.email,
      email: token.email,
      firstName: token.given_name,
      lastName: token.family_name,
      fullName: token.name,
      roles: this.getUserRoles(),
      tenantId: token.tenant_id,
      shopId: token.shop_id,
    }
  }

  /**
   * Get user roles
   */
  public getUserRoles(): string[] {
    if (!this.keycloak?.tokenParsed) {
      return []
    }

    const token = this.keycloak.tokenParsed as any
    const realmRoles = token.realm_access?.roles || []
    const resourceRoles = token.resource_access?.[this.keycloak.clientId!]?.roles || []

    return [...realmRoles, ...resourceRoles]
  }

  /**
   * Check if user has a specific role
   */
  public hasRole(role: string): boolean {
    return this.getUserRoles().includes(role)
  }

  /**
   * Check if user has any of the specified roles
   */
  public hasAnyRole(roles: string[]): boolean {
    const userRoles = this.getUserRoles()
    return roles.some(role => userRoles.includes(role))
  }

  /**
   * Check if user has all of the specified roles
   */
  public hasAllRoles(roles: string[]): boolean {
    const userRoles = this.getUserRoles()
    return roles.every(role => userRoles.includes(role))
  }

  /**
   * Get the current access token
   */
  public getToken(): string | undefined {
    return this.keycloak?.token
  }

  /**
   * Get the token for API requests
   */
  public getAuthHeader(): Record<string, string> {
    const token = this.getToken()
    return token ? { Authorization: `Bearer ${token}` } : {}
  }

  /**
   * Login using Keycloak
   */
  public async login(options?: Keycloak.KeycloakLoginOptions): Promise<void> {
    if (!this.keycloak) {
      throw new Error('Keycloak not initialized')
    }
    await this.keycloak.login(options)
  }

  /**
   * Logout from Keycloak
   */
  public async logout(options?: Keycloak.KeycloakLogoutOptions): Promise<void> {
    if (!this.keycloak) {
      throw new Error('Keycloak not initialized')
    }
    await this.keycloak.logout(options)
  }

  /**
   * Register a new user
   */
  public async register(options?: Keycloak.KeycloakRegisterOptions): Promise<void> {
    if (!this.keycloak) {
      throw new Error('Keycloak not initialized')
    }
    await this.keycloak.register(options)
  }

  /**
   * Update token if needed
   */
  public async updateToken(minValidity = 5): Promise<boolean> {
    if (!this.keycloak) {
      throw new Error('Keycloak not initialized')
    }
    try {
      const refreshed = await this.keycloak.updateToken(minValidity)
      return refreshed
    } catch (error) {
      console.error('Failed to refresh token:', error)
      return false
    }
  }

  /**
   * Load user profile from Keycloak
   */
  public async loadUserProfile(): Promise<Keycloak.KeycloakProfile> {
    if (!this.keycloak) {
      throw new Error('Keycloak not initialized')
    }
    return await this.keycloak.loadUserProfile()
  }

  /**
   * Get account management URL
   */
  public getAccountUrl(): string | undefined {
    return this.keycloak?.createAccountUrl()
  }

  /**
   * Check if user is authenticated
   */
  public isAuthenticated(): boolean {
    return this.keycloak?.authenticated || false
  }

  /**
   * Clear all tokens and reset state
   */
  public clearTokens(): void {
    if (this.keycloak) {
      this.keycloak.clearToken()
    }
  }
}

export default KeycloakService.getInstance()