// Default configuration for embedded/standalone mode
// This file will be overridden by ConfigMap in Kubernetes deployments (cloud mode)
window.RUNTIME_CONFIG = {
  API_BASE_URL: "https://api.retailhq.app/api",
  KEYCLOAK_URL: "",
  KEYCLOAK_REALM: "",
  KEYCLOAK_CLIENT_ID: "",
  APP_VERSION: "1.0.0",
  APP_ENV: "production",
  AUTH_MODE: "embedded"
};