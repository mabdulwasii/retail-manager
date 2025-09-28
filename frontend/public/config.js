// Default configuration for local development
// This file will be overridden by ConfigMap in Kubernetes deployments
window.RUNTIME_CONFIG = {
  API_BASE_URL: "http://localhost:8081/api",
  KEYCLOAK_URL: "http://localhost:8080",
  KEYCLOAK_REALM: "shop-manager",
  KEYCLOAK_CLIENT_ID: "shop-manager-frontend",
  APP_VERSION: "1.0.0",
  APP_ENV: "development"
};