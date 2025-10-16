import configService from "@/config/runtime-config";
import axios, { AxiosInstance, AxiosResponse } from "axios";

// Create axios instance with base configuration
const api: AxiosInstance = axios.create({
  baseURL: configService.apiBaseUrl,
  timeout: 10000,
  headers: {
    "Content-Type": "application/json",
  },
});

// Token provider callback
let tokenProvider: (() => string | undefined) | null = null;

export const setTokenProvider = (getToken: () => string | undefined) => {
  tokenProvider = getToken;
};

// Request interceptor to add auth token and Keycloak headers
api.interceptors.request.use(
  async (config) => {
    // Try to get token from callback first, then localStorage as fallback
    let token: string | undefined;
    if (tokenProvider) {
      token = tokenProvider();
    }

    // Fallback to localStorage if no token from callback
    if (!token) {
      token =
        localStorage.getItem("keycloak_token") ||
        localStorage.getItem("keycloak_access_token") ||
        undefined;
    }

    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }

    // Add Keycloak realm and client headers for proper context
    config.headers["X-Keycloak-Realm"] = configService.keycloakRealm;
    config.headers["X-Keycloak-Client"] = configService.keycloakClientId;

    return config;
  },
  (error) => Promise.reject(error)
);

// Response interceptor for error handling
api.interceptors.response.use(
  (response: AxiosResponse) => response,
  async (error) => {
    if (error.response?.status === 401) {
      console.warn(
        "API request received 401 Unauthorized - token may be invalid or expired"
      );
    }
    return Promise.reject(error);
  }
);

export default api;
