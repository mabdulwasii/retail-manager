// Default configuration for embedded/standalone mode
// This file will be overridden by ConfigMap in Kubernetes deployments (cloud mode)
window.RUNTIME_CONFIG = {
  API_BASE_URL: "/api",  // Relative URL for embedded mode (same origin)
  KEYCLOAK_URL: "",  // Not used in embedded mode
  KEYCLOAK_REALM: "",  // Not used in embedded mode
  KEYCLOAK_CLIENT_ID: "",  // Not used in embedded mode
  APP_VERSION: "1.0.0",
  APP_ENV: "production",
  AUTH_MODE: "embedded"  // "embedded" for standalone, "keycloak" for cloud
};