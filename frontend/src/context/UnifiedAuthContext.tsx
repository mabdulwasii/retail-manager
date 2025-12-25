/**
 * Unified authentication context that switches between Keycloak (cloud)
 * and embedded JWT (standalone) based on AUTH_MODE configuration.
 */

import React from 'react';
import configService from '@/config/runtime-config';
import { ManualAuthProvider, useAuth as useKeycloakAuth } from './ManualAuthContext';
import { EmbeddedAuthProvider, useEmbeddedAuth } from './EmbeddedAuthContext';

/**
 * Unified auth provider that chooses the correct auth implementation
 */
export const UnifiedAuthProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const isEmbedded = configService.isEmbeddedMode;

  console.log(`Using ${isEmbedded ? 'Embedded JWT' : 'Keycloak'} authentication (mode: ${configService.authMode})`);

  if (isEmbedded) {
    return <EmbeddedAuthProvider>{children}</EmbeddedAuthProvider>;
  }

  return <ManualAuthProvider>{children}</ManualAuthProvider>;
};

/**
 * Unified auth hook that works with both Keycloak and embedded auth
 */
export const useAuth = () => {
  const isEmbedded = configService.isEmbeddedMode;

  if (isEmbedded) {
    return useEmbeddedAuth();
  }

  return useKeycloakAuth();
};
