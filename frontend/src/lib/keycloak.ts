import Keycloak from 'keycloak-js'

let keycloak: Keycloak | null = null

const initOptions = {
  url: import.meta.env.VITE_KEYCLOAK_URL || 'http://localhost:8080',
  realm: import.meta.env.VITE_KEYCLOAK_REALM || 'shop-manager',
  clientId: import.meta.env.VITE_KEYCLOAK_CLIENT_ID || 'shop-manager-frontend',
}

export const initKeycloak = async (): Promise<Keycloak> => {
  if (keycloak) {
    return keycloak
  }

  keycloak = new Keycloak(initOptions)

  try {
    const authenticated = await keycloak.init({
      onLoad: 'check-sso',
      checkLoginIframe: false,
      enableLogging: true,
      pkceMethod: 'S256',
    })

    if (authenticated) {
      console.log('User is authenticated')
      // Set up token refresh - check every minute
      // Token expires after 5 minutes (300 seconds), refresh at 4 minutes (240 seconds)
      const refreshInterval = setInterval(() => {
        keycloak?.updateToken(240).catch(() => {
          console.error('Failed to refresh token - logging out')
          clearInterval(refreshInterval)
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

export const loginWithCredentials = async (username: string, password: string, onAuthUpdate?: () => void): Promise<boolean> => {
  try {
    const tokenUrl = `${initOptions.url}/realms/${initOptions.realm}/protocol/openid-connect/token`

    const response = await fetch(tokenUrl, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/x-www-form-urlencoded',
      },
      body: new URLSearchParams({
        grant_type: 'password',
        client_id: initOptions.clientId,
        username,
        password,
      }),
    })

    if (response.ok) {
      const tokenData = await response.json()

      // Initialize Keycloak with the token
      if (!keycloak) {
        keycloak = new Keycloak(initOptions)
      }

      // Set the token manually
      keycloak.token = tokenData.access_token
      keycloak.refreshToken = tokenData.refresh_token
      keycloak.idToken = tokenData.id_token
      keycloak.authenticated = true

      // Parse token to get user info
      keycloak.tokenParsed = JSON.parse(atob(tokenData.access_token.split('.')[1]))

      console.log('User authenticated with credentials')

      // Set up token refresh timer like in initKeycloak
      const refreshInterval = setInterval(() => {
        keycloak?.updateToken(240).catch(() => {
          console.error('Failed to refresh token - logging out')
          clearInterval(refreshInterval)
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

      // Call auth update callback if provided
      if (onAuthUpdate) {
        onAuthUpdate()
      }

      // Redirect to dashboard after successful authentication
      window.location.href = '/dashboard'

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