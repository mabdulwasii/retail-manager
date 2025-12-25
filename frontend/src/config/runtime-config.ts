// Runtime configuration loader
// This loads configuration from either window.RUNTIME_CONFIG (injected by Kubernetes ConfigMap)
// or falls back to build-time environment variables

interface RuntimeConfig {
  API_BASE_URL: string;
  KEYCLOAK_URL: string;
  KEYCLOAK_REALM: string;
  KEYCLOAK_CLIENT_ID: string;
  APP_VERSION: string;
  APP_ENV: string;
  AUTH_MODE?: 'keycloak' | 'embedded'; // cloud (keycloak) vs standalone (embedded JWT)
}

declare global {
  interface Window {
    RUNTIME_CONFIG?: RuntimeConfig;
  }
}

class ConfigService {
  private config: RuntimeConfig | null = null;

  private getConfig(): RuntimeConfig {
    // Lazy initialization - check for runtime config every time
    if (!this.config) {
      if (window.RUNTIME_CONFIG) {
        console.log('Using runtime configuration from ConfigMap');
        this.config = window.RUNTIME_CONFIG;
      } else {
        console.log('Using build-time environment variables');
        this.config = {
          API_BASE_URL: import.meta.env.VITE_API_BASE_URL || 'http://localhost:8081/api',
          KEYCLOAK_URL: import.meta.env.VITE_KEYCLOAK_URL || 'http://localhost:8080',
          KEYCLOAK_REALM: import.meta.env.VITE_KEYCLOAK_REALM || 'shop-manager',
          KEYCLOAK_CLIENT_ID: import.meta.env.VITE_KEYCLOAK_CLIENT_ID || 'shop-manager-frontend',
          APP_VERSION: import.meta.env.VITE_APP_VERSION || '1.0.0',
          APP_ENV: import.meta.env.VITE_APP_ENV || 'development',
          AUTH_MODE: (import.meta.env.VITE_AUTH_MODE as 'keycloak' | 'embedded') || 'keycloak'
        };
      }
    }
    return this.config;
  }

  get apiBaseUrl(): string {
    return this.getConfig().API_BASE_URL;
  }

  get keycloakUrl(): string {
    return this.getConfig().KEYCLOAK_URL;
  }

  get keycloakRealm(): string {
    return this.getConfig().KEYCLOAK_REALM;
  }

  get keycloakClientId(): string {
    return this.getConfig().KEYCLOAK_CLIENT_ID;
  }

  get appVersion(): string {
    return this.getConfig().APP_VERSION;
  }

  get appEnv(): string {
    return this.getConfig().APP_ENV;
  }

  get authMode(): 'keycloak' | 'embedded' {
    return this.getConfig().AUTH_MODE || 'keycloak';
  }

  get isEmbeddedMode(): boolean {
    return this.authMode === 'embedded';
  }

  get keycloakConfig() {
    const config = this.getConfig();
    return {
      url: config.KEYCLOAK_URL,
      realm: config.KEYCLOAK_REALM,
      clientId: config.KEYCLOAK_CLIENT_ID
    };
  }

  // Log current configuration for debugging
  logConfig(): void {
    console.log('Current configuration:', {
      apiBaseUrl: this.apiBaseUrl,
      keycloakUrl: this.keycloakUrl,
      keycloakRealm: this.keycloakRealm,
      keycloakClientId: this.keycloakClientId,
      appVersion: this.appVersion,
      appEnv: this.appEnv,
      authMode: this.authMode
    });
  }
}

// Export singleton instance
export const configService = new ConfigService();
export default configService;