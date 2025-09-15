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
      onLoad: 'login-required',
      checkLoginIframe: false,
      enableLogging: true,
      pkceMethod: 'S256',
    })

    if (authenticated) {
      console.log('User is authenticated')
      // Set up token refresh
      setInterval(() => {
        keycloak?.updateToken(70).catch(() => {
          console.error('Failed to refresh token')
          keycloak?.logout()
        })
      }, 60000) // Check every minute
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
    await kc.login()
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