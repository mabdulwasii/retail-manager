// Default configuration for local development
// This file will be overridden by ConfigMap in Kubernetes deployments
window.RUNTIME_CONFIG = {
  API_BASE_URL: "https://api.retail.gomco.com/api",
  KEYCLOAK_URL: "https://auth.retail.gomco.com",
  KEYCLOAK_REALM: "retail",
  KEYCLOAK_CLIENT_ID: "retail-frontend",
  APP_VERSION: "1.0.0",
  APP_ENV: "development"
};