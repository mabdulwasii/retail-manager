/// <reference types="vite/client" />

interface ImportMetaEnv {
  readonly VITE_API_BASE_URL: string
  readonly VITE_KEYCLOAK_URL: string
  readonly VITE_KEYCLOAK_REALM: string
  readonly VITE_KEYCLOAK_CLIENT_ID: string
  readonly VITE_APP_VERSION: string
  readonly VITE_APP_ENVIRONMENT: string
}

interface ImportMeta {
  readonly env: ImportMetaEnv
}