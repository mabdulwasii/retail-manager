import React, { useEffect } from 'react'
import { useNavigate } from 'react-router-dom'
import { useAuth } from '@/context/KeycloakAuthContext'

export const DirectLogin: React.FC = () => {
  const { keycloak, isAuthenticated, isLoading } = useAuth()
  const navigate = useNavigate()

  useEffect(() => {
    if (isLoading) return // Wait for Keycloak to initialize

    if (isAuthenticated) {
      // User is already authenticated, redirect to dashboard
      navigate('/dashboard', { replace: true })
    } else {
      // User not authenticated, trigger Keycloak login
      keycloak.login({
        redirectUri: window.location.origin + '/dashboard',
      })
    }
  }, [isAuthenticated, isLoading, keycloak, navigate])

  // Return null since we're handling navigation
  return null
}