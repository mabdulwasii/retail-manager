import Keycloak from 'keycloak-js'
import configService from '../config/runtime-config'

let keycloak: Keycloak | null = null

export const initKeycloak = async (): Promise<Keycloak> => {
  if (keycloak) {
    return keycloak
  }

  // Get config at initialization time, not module load time
  const initOptions = configService.keycloakConfig
  keycloak = new Keycloak(initOptions)

  try {
    // Check for persisted tokens first
    const persistedAccessToken = localStorage.getItem('keycloak_access_token')
    const persistedRefreshToken = localStorage.getItem('keycloak_refresh_token')
    const persistedIdToken = localStorage.getItem('keycloak_id_token')

    let authenticated = false

    if (persistedAccessToken && persistedRefreshToken) {
      // Try to restore authentication from persisted tokens
      try {
        keycloak.token = persistedAccessToken
        keycloak.refreshToken = persistedRefreshToken
        keycloak.idToken = persistedIdToken
        keycloak.authenticated = true

        // Parse token to get user info
        const tokenPayload = JSON.parse(atob(persistedAccessToken.split('.')[1]))
        keycloak.tokenParsed = tokenPayload

        // Check if token is still valid (not expired)
        const currentTime = Date.now() / 1000
        if (tokenPayload.exp > currentTime) {
          authenticated = true
          console.log('User restored from persisted tokens')
        } else {
          console.log('Persisted token expired, trying to refresh...')
          // Try to refresh token
          try {
            await keycloak.updateToken(5)
            authenticated = keycloak.authenticated || false
            console.log('Token refreshed successfully')
          } catch (refreshError) {
            console.error('Token refresh failed, clearing persisted tokens')
            localStorage.removeItem('keycloak_access_token')
            localStorage.removeItem('keycloak_refresh_token')
            localStorage.removeItem('keycloak_id_token')
            keycloak.authenticated = false
          }
        }
      } catch (error) {
        console.error('Failed to restore from persisted tokens:', error)
        localStorage.removeItem('keycloak_access_token')
        localStorage.removeItem('keycloak_refresh_token')
        localStorage.removeItem('keycloak_id_token')
        keycloak.authenticated = false
      }
    }

    // If no persisted tokens or restoration failed, use normal Keycloak init
    if (!authenticated) {
      authenticated = await keycloak.init({
        onLoad: 'check-sso',
        checkLoginIframe: false,
        enableLogging: true,
        pkceMethod: 'S256',
      })
    }

    if (authenticated) {
      console.log('User is authenticated')
      // Set up token refresh - check every minute
      // Token expires after 5 minutes (300 seconds), refresh at 4 minutes (240 seconds)
      const refreshInterval = setInterval(() => {
        keycloak?.updateToken(240).then(() => {
          // Update persisted tokens when refreshed
          if (keycloak?.token) {
            localStorage.setItem('keycloak_access_token', keycloak.token)
          }
          if (keycloak?.refreshToken) {
            localStorage.setItem('keycloak_refresh_token', keycloak.refreshToken)
          }
          if (keycloak?.idToken) {
            localStorage.setItem('keycloak_id_token', keycloak.idToken)
          }
        }).catch(() => {
          console.error('Failed to refresh token - logging out')
          clearInterval(refreshInterval)
          // Clear persisted tokens on logout
          localStorage.removeItem('keycloak_access_token')
          localStorage.removeItem('keycloak_refresh_token')
          localStorage.removeItem('keycloak_id_token')
          keycloak?.logout()
        })
      }, 60000) // Check every minute

      // Set up inactivity timer for 5 minutes
      let inactivityTimer: NodeJS.Timeout
      const resetInactivityTimer = () => {
        clearTimeout(inactivityTimer)
        inactivityTimer = setTimeout(() => {
          console.log('User inactive for 5 minutes - logging out')
          clearInterval(refreshInterval)
          // Clear persisted tokens on logout
          localStorage.removeItem('keycloak_access_token')
          localStorage.removeItem('keycloak_refresh_token')
          localStorage.removeItem('keycloak_id_token')
          keycloak?.logout()
        }, 300000) // 5 minutes
      }

      // Reset timer on user activity
      document.addEventListener('mousedown', resetInactivityTimer)
      document.addEventListener('keydown', resetInactivityTimer)
      document.addEventListener('scroll', resetInactivityTimer)
      document.addEventListener('touchstart', resetInactivityTimer)

      // Start the timer
      resetInactivityTimer()
    }

    return keycloak
  } catch (error) {
    console.error('Keycloak initialization failed:', error)
    throw error
  }
}

export const getKeycloak = (): Keycloak | null => {
  return keycloak
}

export const login = async (): Promise<void> => {
  const kc = getKeycloak()
  if (kc) {
    await kc.login({
      redirectUri: window.location.origin + '/dashboard'
    })
  }
}

export const loginWithCredentials = async (username: string, password: string, onAuthUpdate?: () => Promise<void>): Promise<boolean> => {
  try {
    const tokenUrl = `${configService.keycloakUrl}/realms/${configService.keycloakRealm}/protocol/openid-connect/token`

    const response = await fetch(tokenUrl, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/x-www-form-urlencoded',
      },
      body: new URLSearchParams({
        grant_type: 'password',
        client_id: configService.keycloakClientId,
        username,
        password,
      }),
    })

    if (response.ok) {
      const tokenData = await response.json()

      // Initialize Keycloak with the token
      if (!keycloak) {
        keycloak = new Keycloak(configService.keycloakConfig)
      }

      // Set the token manually
      keycloak.token = tokenData.access_token
      keycloak.refreshToken = tokenData.refresh_token
      keycloak.idToken = tokenData.id_token
      keycloak.authenticated = true

      // Persist tokens in localStorage for authentication persistence
      localStorage.setItem('keycloak_access_token', tokenData.access_token)
      localStorage.setItem('keycloak_refresh_token', tokenData.refresh_token)
      localStorage.setItem('keycloak_id_token', tokenData.id_token)

      // Parse token to get user info
      keycloak.tokenParsed = JSON.parse(atob(tokenData.access_token.split('.')[1]))

      console.log('User authenticated with credentials')

      // Set up token refresh timer like in initKeycloak
      const refreshInterval = setInterval(() => {
        keycloak?.updateToken(240).catch(() => {
          console.error('Failed to refresh token - logging out')
          clearInterval(refreshInterval)
          // Clear persisted tokens on logout
          localStorage.removeItem('keycloak_access_token')
          localStorage.removeItem('keycloak_refresh_token')
          localStorage.removeItem('keycloak_id_token')
          keycloak?.logout()
        })
      }, 60000)

      // Set up inactivity timer
      let inactivityTimer: NodeJS.Timeout
      const resetInactivityTimer = () => {
        clearTimeout(inactivityTimer)
        inactivityTimer = setTimeout(() => {
          console.log('User inactive for 5 minutes - logging out')
          clearInterval(refreshInterval)
          // Clear persisted tokens on logout
          localStorage.removeItem('keycloak_access_token')
          localStorage.removeItem('keycloak_refresh_token')
          localStorage.removeItem('keycloak_id_token')
          keycloak?.logout()
        }, 300000)
      }

      // Reset timer on user activity
      document.addEventListener('mousedown', resetInactivityTimer)
      document.addEventListener('keydown', resetInactivityTimer)
      document.addEventListener('scroll', resetInactivityTimer)
      document.addEventListener('touchstart', resetInactivityTimer)

      // Start the timer
      resetInactivityTimer()

      // Call auth update callback if provided and wait for it
      if (onAuthUpdate) {
        await onAuthUpdate()
        // Give a small delay to ensure state is updated
        await new Promise(resolve => setTimeout(resolve, 100))
      }

      return true
    } else {
      console.error('Login failed:', response.statusText)
      return false
    }
  } catch (error) {
    console.error('Login error:', error)
    return false
  }
}

export const logout = async (): Promise<void> => {
  // Clear persisted tokens
  localStorage.removeItem('keycloak_access_token')
  localStorage.removeItem('keycloak_refresh_token')
  localStorage.removeItem('keycloak_id_token')

  const kc = getKeycloak()
  if (kc) {
    await kc.logout()
  }
}

export const getToken = (): string | undefined => {
  const kc = getKeycloak()
  return kc?.token
}

export const getUserInfo = () => {
  const kc = getKeycloak()
  if (kc?.tokenParsed) {
    return {
      id: kc.tokenParsed.sub,
      username: kc.tokenParsed.preferred_username,
      email: kc.tokenParsed.email,
      firstName: kc.tokenParsed.given_name,
      lastName: kc.tokenParsed.family_name,
      roles: kc.tokenParsed.realm_access?.roles || [],
      tenantId: kc.tokenParsed.tenant_id,
    }
  }
  return null
}

export const hasRole = (role: string): boolean => {
  const kc = getKeycloak()
  return kc?.hasRealmRole(role) || false
}

export const hasAnyRole = (roles: string[]): boolean => {
  return roles.some(role => hasRole(role))
}