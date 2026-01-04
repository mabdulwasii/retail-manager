/**
 * Unified authentication context that switches between Keycloak (cloud)
 * and embedded JWT (standalone) based on AUTH_MODE configuration.
 */

import React from 'react';
import configService from '@/config/runtime-config';
import { ManualAuthProvider, useAuth as useKeycloakAuth } from './ManualAuthContext';
import { EmbeddedAuthProvider, useEmbeddedAuth } from './EmbeddedAuthContext';

/**
 * Unified auth provider that nests both providers to satisfy React's Rules of Hooks.
 * The unused provider becomes a no-op but both contexts are available.
 */
export const UnifiedAuthProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const isEmbedded = configService.isEmbeddedMode;

  console.log(`Using ${isEmbedded ? 'Embedded JWT' : 'Keycloak'} authentication (mode: ${configService.authMode})`);

  // Nest both providers so useAuth can safely call both hooks
  return (
    <ManualAuthProvider>
      <EmbeddedAuthProvider>
        {children}
      </EmbeddedAuthProvider>
    </ManualAuthProvider>
  );
};

/**
 * Unified auth hook that works with both Keycloak and embedded auth.
 *
 * Both hooks are called unconditionally to comply with React's Rules of Hooks.
 * The appropriate auth context is returned based on the auth mode.
 */
export const useAuth = () => {
  const isEmbedded = configService.isEmbeddedMode;

  // Call both hooks unconditionally (React Rules of Hooks requirement)
  // Both providers are present in the tree, so this is safe
  const embeddedAuth = useEmbeddedAuth();
  const keycloakAuth = useKeycloakAuth();

  // Return the appropriate auth based on mode
  return isEmbedded ? embeddedAuth : keycloakAuth;
};
