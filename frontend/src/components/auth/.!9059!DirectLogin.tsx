import React, { useEffect } from 'react'
import { Loader2 } from 'lucide-react'

export const DirectLogin: React.FC = () => {
  useEffect(() => {
    const redirectToKeycloak = async () => {
      try {
        // Build Keycloak authorization URL for OAuth flow
        const keycloakUrl = import.meta.env.VITE_KEYCLOAK_URL || 'https://auth.shop-manager.local'
        const clientId = 'shop-manager-frontend'
        const redirectUri = encodeURIComponent(window.location.origin + '/auth/callback')

        // Generate state and nonce for security
        const state = btoa(Math.random().toString()).substring(10, 25)
        const nonce = btoa(Math.random().toString()).substring(10, 25)

        // Generate PKCE code verifier and challenge
        const codeVerifier = btoa(String.fromCharCode(...crypto.getRandomValues(new Uint8Array(32))))
          .replace(/\+/g, '-')
          .replace(/\//g, '_')
          .replace(/=/g, '')

        const encoder = new TextEncoder()
        const data = encoder.encode(codeVerifier)
        const digest = await crypto.subtle.digest('SHA-256', data)
        const codeChallenge = btoa(String.fromCharCode(...new Uint8Array(digest)))
          .replace(/\+/g, '-')
          .replace(/\//g, '_')
          .replace(/=/g, '')

        // Store values for validation later
        sessionStorage.setItem('auth_state', state)
        sessionStorage.setItem('auth_nonce', nonce)
        sessionStorage.setItem('code_verifier', codeVerifier)

        // Build authorization URL for standard OAuth flow with PKCE
        const authUrl = `${keycloakUrl}/realms/shop-manager/protocol/openid-connect/auth?` +
          `client_id=${clientId}&` +
          `redirect_uri=${redirectUri}&` +
          `response_type=code&` +
          `scope=openid%20profile%20email&` +
          `state=${state}&` +
          `nonce=${nonce}&` +
          `code_challenge=${codeChallenge}&` +
          `code_challenge_method=S256`

